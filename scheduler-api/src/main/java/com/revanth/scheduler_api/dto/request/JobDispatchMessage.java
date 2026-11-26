package com.revanth.scheduler_api.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class JobDispatchMessage {

    private final Long eventId;
    private final Long jobId;
    private final String eventType;
}
