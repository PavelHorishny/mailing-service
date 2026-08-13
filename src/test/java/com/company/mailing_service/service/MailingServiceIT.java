package com.company.mailing_service.service;

import com.company.mailing_service.domain.MailEvent;
import com.company.mailing_service.domain.MailRecord;
import com.company.mailing_service.repository.MailRepository;
import com.company.mailing_service.repository.jpa.MailJpaDao;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
class MailingServiceIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16-alpine");

    @Autowired
    private MailingService mailingService;

    @Autowired
    private MailRepository mailRepository;

    @Autowired
    private MailJpaDao mailJpaDao;

    @AfterEach
    void tearDown() {
        mailJpaDao.deleteAll();
    }

    @Test
    void persistsNewMailEventAsMailRecord() {
        MailEvent event = new MailEvent("evt-int-1", "user@example.com", "welcome", "ru", Map.of());

        mailingService.process(event);

        Optional<MailRecord> saved = mailRepository.findByIdempotencyKey("evt-int-1");
        assertThat(saved).isPresent();
        assertThat(saved.get().getRecipient()).isEqualTo("user@example.com");
    }

    @Test
    void doesNotCreateDuplicateRecordForSameEventId() {
        MailEvent event = new MailEvent("evt-int-2", "user@example.com", "welcome", "ru", Map.of());

        mailingService.process(event);
        mailingService.process(event);

        assertThat(mailJpaDao.count()).isEqualTo(1);
    }
}