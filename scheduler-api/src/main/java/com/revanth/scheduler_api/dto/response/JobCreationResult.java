package com.revanth.scheduler_api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class JobCreationResult {

    private final CreateJobResponse job;
    private final boolean replayed;
}