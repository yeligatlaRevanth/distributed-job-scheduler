package com.revanth.scheduler_api.controller;

import com.revanth.scheduler_api.dto.request.CreateJobRequest;
import com.revanth.scheduler_api.dto.response.CreateJobResponse;
import com.revanth.scheduler_api.dto.response.JobCreationResult;
import com.revanth.scheduler_api.entity.User;
import com.revanth.scheduler_api.service.JobService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/jobs")
public class JobController {

    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    @PostMapping
    public ResponseEntity<CreateJobResponse> createJob(
            @AuthenticationPrincipal User user,
            @RequestHeader(value = "Idempotency-Key", required = false)
            String idempotencyKey,
            @Valid @RequestBody CreateJobRequest request
    ) {

        JobCreationResult result = jobService.createJob(
                user.getId(),
                request,
                idempotencyKey
        );

        HttpStatus status = result.isReplayed()
                ? HttpStatus.OK
                : HttpStatus.ACCEPTED;

        return ResponseEntity
                .status(status)
                .body(result.getJob());
    }
}
