package com.company.mailing_service.infrastructure.web;

import com.company.mailing_service.domain.InvalidMailStatusException;
import com.company.mailing_service.domain.MailRecordNotFoundException;
import com.company.mailing_service.infrastructure.web.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class MailExceptionHandler {

    @ExceptionHandler(MailRecordNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(MailRecordNotFoundException exception) {
        log.debug("Mail record not found: {}", exception.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(exception.getMessage()));
    }

    @ExceptionHandler(InvalidMailStatusException.class)
    public ResponseEntity<ErrorResponse> handleInvalidStatus (InvalidMailStatusException exception) {
        log.debug("Invalid mail status: {}", exception.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(exception.getMessage()));
    }
}
