package com.company.mailing_service.infrastructure.web;

import com.company.mailing_service.domain.MailRecord;
import com.company.mailing_service.service.MailingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/mails")
@RequiredArgsConstructor
public class MailController {

    private final MailingService mailingService;

    @PostMapping("/{id}/retry")
    public MailStatusResponse forceRetry(@PathVariable UUID id) {
        MailRecord record = mailingService.forceRetry(id);
        return MailStatusResponse.from(record);
    }
}
