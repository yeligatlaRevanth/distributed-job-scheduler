CREATE DATABASE IF NOT EXISTS mock_receiver
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

GRANT ALL PRIVILEGES ON mock_receiver.* TO 'scheduler'@'%';
FLUSH PRIVILEGES;

USE mock_receiver;

CREATE TABLE IF NOT EXISTS calls (
    id               BIGINT       NOT NULL AUTO_INCREMENT,
    idempotency_key  VARCHAR(100) NOT NULL,
    received_at      DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    KEY idx_calls_key (idempotency_key)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS effects (
    idempotency_key  VARCHAR(100) NOT NULL,
    result           TEXT         NULL,
    created_at       DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (idempotency_key)
) ENGINE=InnoDB;