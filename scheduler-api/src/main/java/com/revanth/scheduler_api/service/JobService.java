package com.revanth.scheduler_api.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.revanth.scheduler_api.dto.response.JobCreationResult;
import com.revanth.scheduler_api.exception.DuplicateJobException;
import com.revanth.scheduler_api.repository.JobRepository;
import com.revanth.scheduler_api.repository.UserRepository;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.revanth.scheduler_api.dto.request.CreateJobRequest;
import com.revanth.scheduler_api.entity.Job;
import com.revanth.scheduler_api.entity.User;
import org.springframework.transaction.annotation.Transactional;
import com.revanth.scheduler_api.dto.response.CreateJobResponse;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class JobService {

    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public JobService(
            JobRepository jobRepository,
            UserRepository userRepository,
            ObjectMapper objectMapper
    ) {
        this.jobRepository = jobRepository;
        this.userRepository = userRepository;
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
        LocalDateTime nextRunAt = request.getScheduledAt() != null
                ? request.getScheduledAt()
                : LocalDateTime.now();

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
                nextRunAt,
                priority,
                maxAttempts,
                timeoutSeconds
        );

        Job savedJob = jobRepository.save(job);

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

            boolean sameSchedule =
                    request.getScheduledAt() == null ||
                            request.getScheduledAt().equals(
                                    existingJob.getNextRunAt()
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
