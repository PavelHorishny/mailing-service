package com.company.mailing_service.fixtures;

import com.company.mailing_service.domain.MailRecord;
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
    private UUID id = null;
    private String idempotencyKey = UUID.randomUUID().toString();
    private String recipient = "test@example.com";
    private String templateKey = "none";
    private String locale = "en";
    private MailStatus status = MailStatus.NEW;
    private int attemptCount = 0;
    private String lastError = "";
    private Instant createdAt = Instant.now();
    private Instant updatedAt = Instant.now();

    public static MailRecordFixture getInstance() {
        return new MailRecordFixture();
    }

    public MailRecord toMailRecord(){
        return MailRecord.builder()
                .id(id)
                .idempotencyKey(idempotencyKey)
                .recipient(recipient)
                .templateKey(templateKey)
                .locale(locale)
                .status(status)
                .attemptCount(attemptCount)
                .lastError(lastError)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }
}
