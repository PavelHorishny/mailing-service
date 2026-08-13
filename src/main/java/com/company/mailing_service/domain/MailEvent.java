package com.company.mailing_service.domain;

import lombok.Builder;

import java.util.Map;
@Builder
public record MailEvent(
        String eventId,
        String recipient,
        String templateKey,
        String locale,
        Map<String, Object> variables
) {}
