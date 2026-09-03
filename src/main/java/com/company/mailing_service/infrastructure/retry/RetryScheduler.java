package com.company.mailing_service.infrastructure.retry;

import com.company.mailing_service.domain.MailRecord;
import com.company.mailing_service.domain.MailRepository;
import com.company.mailing_service.infrastructure.service.impl.MailingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${mailing.retry.batch-size:100}")
    private int batchSize;

    @Scheduled(cron = "${mailing.retry.poll-cron:0 * * * * *}")
    public void pollAndRetry(){
        List<MailRecord> candidates =
                mailRepository.findFailedRetrying(batchSize);
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
