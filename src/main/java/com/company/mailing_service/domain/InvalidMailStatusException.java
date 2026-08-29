package com.company.mailing_service.domain;

import java.util.UUID;

public class InvalidMailStatusException extends RuntimeException {

    public InvalidMailStatusException(UUID id, MailStatus actualStatus) {
        super("Mail record " + id + " cannot be retried, current status: " + actualStatus);
    }
}
