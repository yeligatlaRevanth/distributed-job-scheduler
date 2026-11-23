USE scheduler;

CREATE TABLE IF NOT EXISTS users (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    name          VARCHAR(100) NOT NULL,
    api_key_hash  CHAR(64)     NOT NULL,
    created_at    DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    UNIQUE KEY uk_users_api_key_hash (api_key_hash)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS jobs (
    id               BIGINT       NOT NULL AUTO_INCREMENT,
    user_id          BIGINT       NOT NULL,
    idempotency_key  VARCHAR(100) NULL,
    type             VARCHAR(50)  NOT NULL,
    payload          LONGTEXT     NULL,
    status           VARCHAR(20)  NOT NULL,
    priority         SMALLINT     NOT NULL DEFAULT 5,
    next_run_at      DATETIME(3)  NOT NULL,
    attempt_count    INT          NOT NULL DEFAULT 0,
    max_attempts     INT          NOT NULL DEFAULT 5,
    timeout_seconds  INT          NOT NULL DEFAULT 300,
    worker_id        VARCHAR(100) NULL,
    lease_until      DATETIME(3)  NULL,
    fencing_token    BIGINT       NOT NULL DEFAULT 0,
    last_error       TEXT         NULL,
    created_at       DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at       DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
                                  ON UPDATE CURRENT_TIMESTAMP(3),
    completed_at     DATETIME(3)  NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_jobs_user_idempotency (user_id, idempotency_key),
    KEY idx_jobs_status_lease (status, lease_until),
    KEY idx_jobs_user_status_created (user_id, status, created_at),
    CONSTRAINT fk_jobs_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB;


CREATE TABLE IF NOT EXISTS outbox_events (
    id            BIGINT      NOT NULL AUTO_INCREMENT,
    job_id        BIGINT      NOT NULL,
    event_type    VARCHAR(30) NOT NULL DEFAULT 'JOB_READY',
    available_at  DATETIME(3) NOT NULL,
    created_at    DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    published_at  DATETIME(3) NULL,
    PRIMARY KEY (id),
    KEY idx_outbox_unpublished (published_at, available_at, id),
    CONSTRAINT fk_outbox_job FOREIGN KEY (job_id) REFERENCES jobs (id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS job_executions (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    job_id          BIGINT       NOT NULL,
    attempt_number  INT          NOT NULL,
    worker_id       VARCHAR(100) NOT NULL,
    fencing_token   BIGINT       NOT NULL,
    started_at      DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    ended_at        DATETIME(3)  NULL,
    result          VARCHAR(20)  NULL,
    error           TEXT         NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_exec_job_attempt (job_id, attempt_number),
    CONSTRAINT fk_exec_job FOREIGN KEY (job_id) REFERENCES jobs (id)
) ENGINE=InnoDB;