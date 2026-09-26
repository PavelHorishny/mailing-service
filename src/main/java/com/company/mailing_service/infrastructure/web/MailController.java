package com.company.mailing_service.infrastructure.web;

import com.company.mailing_service.domain.MailRecord;
import com.company.mailing_service.infrastructure.service.impl.MailingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/mails")
@RequiredArgsConstructor
@Tag(name = "Mails", description = "Operations on mail records")
public class MailController {

    private final MailingService mailingService;

    @Operation(summary = "Force retry a failed mail record",
            description = "Retries sending a mail record that is currently in FAILED_RETRYING status.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Retry attempted, current record status returned",
                    content = @Content(schema = @Schema(implementation = MailStatusResponse.class))),
            @ApiResponse(responseCode = "404", description = "Mail record not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Mail record is not in FAILED_RETRYING status",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/{id}/retry")
    public MailStatusResponse forceRetry(@Parameter(description = "Mail record id") @PathVariable UUID id) {
        MailRecord record = mailingService.forceRetry(id);
        return MailStatusResponse.from(record);
    }
}
