package com.company.mailing_service.infrastructure.consumer;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.support.Acknowledgment;

@Slf4j
public abstract class AbstractLoggingConsumer<T> {

    @KafkaHandler
    protected void handle(T event, Acknowledgment acknowledgment) {
        String eventId = extractEventId(event);
        MDC.put("eventId", eventId);
        try {
            log.info("Received event: {}", describe(event));
            processEvent(event);
            acknowledgment.acknowledge();
            log.debug("Committed offset for event {}", eventId);
        } catch (Exception e) {
            log.error("Failed to process event {}: {}", eventId, describe(event), e);
            throw e;
        } finally {
            MDC.remove("eventId");
        }
    }

    protected abstract String extractEventId(T event);

    protected abstract String describe(T event);

    protected abstract void processEvent(T event);
}