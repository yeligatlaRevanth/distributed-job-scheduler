package com.revanth.scheduler_api.repository;

import com.revanth.scheduler_api.entity.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OutboxEventRepository
        extends JpaRepository<OutboxEvent, Long> {

    @Query(value = """
        SELECT *
        FROM outbox_events
        WHERE published_at IS NULL
          AND available_at <= UTC_TIMESTAMP(3)
        ORDER BY available_at, id
        LIMIT :batchSize
        FOR UPDATE SKIP LOCKED
        """, nativeQuery = true)
    List<OutboxEvent> findNextBatchForPublishing(
            @Param("batchSize") int batchSize
    );
}