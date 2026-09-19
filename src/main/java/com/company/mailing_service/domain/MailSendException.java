package com.company.mailing_service.domain;

public class MailSendException extends RuntimeException {

    public MailSendException(String message) {
        super(message);
    }

    public MailSendException(String message, Throwable cause) {
        super(message, cause);
    }
}
