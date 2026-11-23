package com.revanth.scheduler_api.repository;

import com.revanth.scheduler_api.entity.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OutboxEventRepository
        extends JpaRepository<OutboxEvent, Long> {
}