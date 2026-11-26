package com.revanth.job_worker.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.revanth.job_worker.dto.JobDispatchMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class JobDispatchConsumer {

    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "job.dispatch",
            groupId = "job-worker-group"
    )
    public void consume(String message) {
        try {
            JobDispatchMessage dispatchMessage =
                    objectMapper.readValue(message, JobDispatchMessage.class);

            log.info(
                    "Received job dispatch: eventId={}, jobId={}, eventType={}",
                    dispatchMessage.getEventId(),
                    dispatchMessage.getJobId(),
                    dispatchMessage.getEventType()
            );

        } catch (JsonProcessingException exception) {
            log.error("Failed to deserialize job dispatch message: {}", message, exception);
            throw new IllegalArgumentException("Invalid job dispatch message", exception);
        }
    }
}