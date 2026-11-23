package com.revanth.scheduler_api.repository;

import com.revanth.scheduler_api.entity.Job;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JobRepository extends JpaRepository<Job, Long> {

    Optional<Job> findByIdAndUser_Id(Long jobId, Long userId);

    Page<Job> findAllByUser_Id(Long userId, Pageable pageable);

    boolean existsByUser_IdAndIdempotencyKey(
            Long userId,
            String idempotencyKey
    );

    Optional<Job> findByUser_IdAndIdempotencyKey(
            Long userId,
            String idempotencyKey
    );
}
