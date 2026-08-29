package com.company.mailing_service.infrastructure.retry;

import com.company.mailing_service.domain.MailRecord;
import com.company.mailing_service.domain.MailRepository;
import com.company.mailing_service.domain.MailStatus;
import com.company.mailing_service.service.MailingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class RetryScheduler {
    private final RetryService retryService;
    private final MailRepository mailRepository;
    private final MailingService mailingService;

    @Scheduled(fixedDelayString = "${mailing.retry.poll-interval-ms:10000}")
    public void pollAndRetry(){
        List<MailRecord> candidates = mailRepository.findByStatus(MailStatus.FAILED_RETRYING);
        if(candidates.isEmpty()){
            log.debug("No failed mail records to retry");
            return;
        }

        log.debug("Found {} failed mail records to retry", candidates.size());
        for(MailRecord record : candidates){
            if(retryService.isDue(record)){
                mailingService.retry(record);
            }
        }
    }
}
