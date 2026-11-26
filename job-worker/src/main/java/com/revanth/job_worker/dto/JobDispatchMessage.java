package com.revanth.job_worker.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class JobDispatchMessage {

    private Long eventId;
    private Long jobId;
    private String eventType;
}