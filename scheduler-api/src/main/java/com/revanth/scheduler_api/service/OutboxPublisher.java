package com.revanth.scheduler_api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.revanth.scheduler_api.dto.request.JobDispatchMessage;
import com.revanth.scheduler_api.entity.OutboxEvent;
import com.revanth.scheduler_api.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class OutboxPublisher {

    private static final String TOPIC = "job.dispatch";
    private static final int BATCH_SIZE = 20;

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Transactional
    @Scheduled(fixedDelay = 1000)
    public void publishBatch() {
        List<OutboxEvent> events =
                outboxEventRepository.findNextBatchForPublishing(BATCH_SIZE);

        for (OutboxEvent event : events) {
            try {
                JobDispatchMessage message = new JobDispatchMessage(
                        event.getId(),
                        event.getJob().getId(),
                        event.getEventType()
                );

                String json = objectMapper.writeValueAsString(message);

                kafkaTemplate.send(
                        TOPIC,
                        String.valueOf(event.getJob().getId()),
                        json
                ).get(10, TimeUnit.SECONDS);

                event.markPublished(LocalDateTime.now(ZoneOffset.UTC));

            } catch (Exception exception) {
                throw new IllegalStateException(
                        "Failed to publish outbox event: " + event.getId(),
                        exception
                );
            }
        }
    }
}