package com.company.mailing_service.fixtures;

import com.company.mailing_service.domain.MailStatus;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@With
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class MailRecordFixture {
    private UUID id = UUID.randomUUID();
    private String idempotencyKey = UUID.randomUUID().toString();
    private String recipient = "test";
    private String templateKey = "none";
    private String locale = "en";
    private MailStatus status = MailStatus.NEW;
    private int attemptCount = 0;
    private String lastError = "";
    private Instant createdAt = Instant.now();
    private Instant updatedAt = Instant.now();

    public static MailRecordFixture getInstance() {
        return MailRecordFixture.builder().build();
}
}
