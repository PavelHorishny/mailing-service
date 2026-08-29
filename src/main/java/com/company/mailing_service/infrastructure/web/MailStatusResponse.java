package com.company.mailing_service.infrastructure.web;

import com.company.mailing_service.domain.MailRecord;
import com.company.mailing_service.domain.MailStatus;

import java.util.UUID;

public record MailStatusResponse(UUID id, MailStatus status, int attemptCount, String lastError) {

    public static MailStatusResponse from(MailRecord record) {
        return new MailStatusResponse(
                record.getId(),
                record.getStatus(),
                record.getAttemptCount(),
                record.getLastError()
        );
    }
}
