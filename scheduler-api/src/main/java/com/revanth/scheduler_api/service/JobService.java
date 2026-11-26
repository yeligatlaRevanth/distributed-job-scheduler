package com.revanth.scheduler_api.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.revanth.scheduler_api.dto.response.JobCreationResult;
import com.revanth.scheduler_api.entity.OutboxEvent;
import com.revanth.scheduler_api.exception.DuplicateJobException;
import com.revanth.scheduler_api.repository.JobRepository;
import com.revanth.scheduler_api.repository.UserRepository;
import com.revanth.scheduler_api.repository.OutboxEventRepository;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.revanth.scheduler_api.dto.request.CreateJobRequest;
import com.revanth.scheduler_api.entity.Job;
import com.revanth.scheduler_api.entity.User;
import org.springframework.transaction.annotation.Transactional;
import com.revanth.scheduler_api.dto.response.CreateJobResponse;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.Optional;


@Service
public class JobService {

    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final OutboxEventRepository outboxEventRepository;

    public JobService(
            JobRepository jobRepository,
            UserRepository userRepository,
            OutboxEventRepository outboxEventRepository,
            ObjectMapper objectMapper
    ) {
        this.jobRepository = jobRepository;
        this.userRepository = userRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
    }


    @Transactional
    public JobCreationResult createJob(
            Long userId,
            CreateJobRequest request,
            String idempotencyKey
    ) {

        // 1. Validate payload
        if (request.getPayload() == null ||
                !request.getPayload().isObject()) {
            throw new IllegalArgumentException(
                    "Payload must be a valid JSON object"
            );
        }

        // 2. Check idempotency
        if (idempotencyKey != null) {

            Optional<Job> existingJob =
                    jobRepository.findByUser_IdAndIdempotencyKey(
                            userId,
                            idempotencyKey
                    );

            if (existingJob.isPresent()) {

                Job job = existingJob.get();

                if (!isSameRequest(job, request)) {
                    throw new DuplicateJobException(
                            "Idempotency key was already used with a different request"
                    );
                }

                return new JobCreationResult(
                        mapToResponse(job),
                        true
                );
            }
        }

        // 3. Convert JSON payload into String
        String payloadJson;

        try {
            payloadJson = objectMapper.writeValueAsString(
                    request.getPayload()
            );
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException(
                    "Failed to serialize job payload",
                    exception
            );
        }

        // 4. Retrieve the user reference
        User user = userRepository.getReferenceById(userId);

        // 5. Apply default values
        ZoneId indiaZone = ZoneId.of("Asia/Kolkata");
        LocalDateTime nextRunAt;
        if (request.getScheduledAt() != null) {
            nextRunAt = request.getScheduledAt()
                    .atZone(indiaZone)
                    .withZoneSameInstant(ZoneOffset.UTC)
                    .toLocalDateTime()
                    .truncatedTo(ChronoUnit.MILLIS);
        } else {
            nextRunAt = LocalDateTime.now(ZoneOffset.UTC)
                    .truncatedTo(ChronoUnit.MILLIS);
        }

        if (request.getScheduledAt() != null) {
            LocalDateTime indiaNow = LocalDateTime.now(
                    ZoneId.of("Asia/Kolkata")
            );

            if (request.getScheduledAt().isBefore(indiaNow)) {
                throw new IllegalArgumentException(
                        "Scheduled time cannot be in the past"
                );
            }
        }

        LocalDateTime requestedScheduledAt = null;

        if (request.getScheduledAt() != null) {
            requestedScheduledAt = request.getScheduledAt()
                    .atZone(ZoneId.of("Asia/Kolkata"))
                    .withZoneSameInstant(ZoneOffset.UTC)
                    .toLocalDateTime()
                    .truncatedTo(ChronoUnit.MILLIS);
        }

        Short priority = request.getPriority() != null
                ? request.getPriority()
                : 5;

        Integer maxAttempts = request.getMaxAttempts() != null
                ? request.getMaxAttempts()
                : 5;

        Integer timeoutSeconds = request.getTimeoutSeconds() != null
                ? request.getTimeoutSeconds()
                : 300;

        // 6. Create and persist the job
        Job job = new Job(
                user,
                idempotencyKey,
                request.getType(),
                payloadJson,
                requestedScheduledAt,
                nextRunAt,
                priority,
                maxAttempts,
                timeoutSeconds
        );


        Job savedJob = jobRepository.save(job);

        OutboxEvent outboxEvent = new OutboxEvent(
                savedJob,
                nextRunAt
        );

        outboxEventRepository.save(outboxEvent);

        return new JobCreationResult(
                mapToResponse(savedJob),
                false
        );
    }

    private CreateJobResponse mapToResponse(Job job) {
        return new CreateJobResponse(
                job.getId(),
                job.getType(),
                job.getStatus(),
                job.getPriority(),
                job.getNextRunAt(),
                job.getCreatedAt()
        );
    }

    private boolean isSameRequest(
            Job existingJob,
            CreateJobRequest request
    ) {

        try {
            JsonNode existingPayload =
                    objectMapper.readTree(existingJob.getPayload());

            boolean samePayload =
                    existingPayload.equals(request.getPayload());

            LocalDateTime requestedScheduledAt = null;

            if (request.getScheduledAt() != null) {
                requestedScheduledAt = request.getScheduledAt()
                        .atZone(ZoneId.of("Asia/Kolkata"))
                        .withZoneSameInstant(ZoneOffset.UTC)
                        .toLocalDateTime()
                        .truncatedTo(ChronoUnit.MILLIS);
            }

            boolean sameSchedule = Objects.equals(
                    requestedScheduledAt,
                    existingJob.getRequestedScheduledAt()
            );

            Short priority = request.getPriority() != null
                    ? request.getPriority()
                    : 5;

            Integer maxAttempts = request.getMaxAttempts() != null
                    ? request.getMaxAttempts()
                    : 5;

            Integer timeoutSeconds = request.getTimeoutSeconds() != null
                    ? request.getTimeoutSeconds()
                    : 300;

            return existingJob.getType().equals(request.getType())
                    && samePayload
                    && sameSchedule
                    && existingJob.getPriority().equals(priority)
                    && existingJob.getMaxAttempts().equals(maxAttempts)
                    && existingJob.getTimeoutSeconds().equals(timeoutSeconds);

        } catch (JsonProcessingException exception) {
            throw new IllegalStateException(
                    "Failed to compare existing job payload",
                    exception
            );
        }
    }
}
