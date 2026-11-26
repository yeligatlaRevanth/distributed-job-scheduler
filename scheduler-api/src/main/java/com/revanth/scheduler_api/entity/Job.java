package com.revanth.scheduler_api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(
        name = "jobs",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_jobs_user_idempotency",
                        columnNames = {"user_id", "idempotency_key"}
                )
        }
)
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "idempotency_key", length = 100)
    private String idempotencyKey;

    @Column(nullable = false, length = 50)
    private String type;

    @Column(columnDefinition = "LONGTEXT")
    private String payload;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(nullable = false)
    private Short priority = 5;

    @Column(name = "requested_scheduled_at")
    private LocalDateTime requestedScheduledAt;

    @Column(name = "next_run_at", nullable = false)
    private LocalDateTime nextRunAt;

    @Column(name = "attempt_count", nullable = false)
    private Integer attemptCount = 0;

    @Column(name = "max_attempts", nullable = false)
    private Integer maxAttempts = 5;

    @Column(name = "timeout_seconds", nullable = false)
    private Integer timeoutSeconds = 300;


    @Column(name = "worker_id", length = 100)
    private String workerId;

    @Column(name = "lease_until")
    private LocalDateTime leaseUntil;

    @Column(name = "fencing_token", nullable = false)
    private Long fencingToken = 0L;

    @Column(name = "last_error", columnDefinition = "TEXT")
    private String lastError;


    @Generated(event = EventType.INSERT)
    @Column(
            name = "created_at",
            nullable = false,
            insertable = false,
            updatable = false
    )
    private LocalDateTime createdAt;

    @Generated(event = {EventType.INSERT, EventType.UPDATE})
    @Column(
            name = "updated_at",
            nullable = false,
            insertable = false,
            updatable = false
    )
    private LocalDateTime updatedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;



    protected Job() {
    }

    public Job(
            User user,
            String idempotencyKey,
            String type,
            String payload,
            LocalDateTime requestedScheduledAt,
            LocalDateTime nextRunAt,
            Short priority,
            Integer maxAttempts,
            Integer timeoutSeconds
    ) {
        this.user = user;
        this.idempotencyKey = idempotencyKey;
        this.type = type;
        this.payload = payload;
        this.status = "PENDING";
        this.requestedScheduledAt = requestedScheduledAt;
        this.nextRunAt = nextRunAt;
        this.priority = priority;
        this.attemptCount = 0;
        this.maxAttempts = maxAttempts;
        this.timeoutSeconds = timeoutSeconds;
        this.fencingToken = 0L;
    }
}
