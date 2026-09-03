package com.company.mailing_service.domain;

public class MailRenderException extends RuntimeException {

    public MailRenderException(String message, Throwable cause) {
        super(message, cause);
    }
}
