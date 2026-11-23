package com.revanth.scheduler_api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class CreateJobResponse {

    private final Long jobId;
    private final String type;
    private final String status;
    private final Short priority;
    private final LocalDateTime scheduledAt;
    private final LocalDateTime createdAt;
}
