package com.company.mailing_service.domain;

import java.util.UUID;

public class MailRecordNotFoundException extends RuntimeException {

    public MailRecordNotFoundException(UUID id) {
        super("Mail record not found: " + id);
    }
}
