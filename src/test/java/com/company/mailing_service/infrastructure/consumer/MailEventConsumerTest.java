package com.company.mailing_service.infrastructure.consumer;

import com.company.mailing_service.domain.MailEvent;
import com.company.mailing_service.fixtures.MailEventFixture;
import com.company.mailing_service.infrastructure.service.impl.MailingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MailEventConsumerTest {

    @Mock
    private MailingService mailingService;

    @Mock
    private Acknowledgment acknowledgment;

    private MailEventConsumer consumer;
    private MailEvent event;

    @BeforeEach
    void setUp() {
        consumer = new MailEventConsumer(mailingService);
        event = MailEventFixture.getInstance().toMailEvent();
    }

    @Test
    void processesEventAndAcknowledgesOnSuccess() {
        consumer.handle(event, acknowledgment);

        verify(mailingService).process(event);
        verify(acknowledgment).acknowledge();
    }

    @Test
    void doesNotAcknowledgeAndPropagatesExceptionWhenProcessingFails() {
        RuntimeException failure = new RuntimeException("Something went wrong");
        doThrow(failure).when(mailingService).process(event);

        assertThatThrownBy(() -> consumer.handle(event, acknowledgment))
                .isSameAs(failure);

        verify(acknowledgment, never()).acknowledge();
    }
}
