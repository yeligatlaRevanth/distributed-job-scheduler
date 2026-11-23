package com.revanth.scheduler_api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "outbox_events")
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class OutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @Column(name = "event_type", nullable = false, length = 30)
    private String eventType = "JOB_READY";

    @Column(name = "available_at", nullable = false)
    private LocalDateTime availableAt;

    @Column(
            name = "created_at",
            nullable = false,
            insertable = false,
            updatable = false
    )
    private LocalDateTime createdAt;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    public OutboxEvent(Job job, LocalDateTime availableAt) {
        this.job = job;
        this.availableAt = availableAt;
    }

    public void markPublished(LocalDateTime publishedAt) {
        this.publishedAt = publishedAt;
    }
}