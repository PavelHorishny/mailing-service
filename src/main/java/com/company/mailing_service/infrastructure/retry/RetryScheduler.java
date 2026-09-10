package com.company.mailing_service.infrastructure.retry;

import com.company.mailing_service.domain.MailRecord;
import com.company.mailing_service.domain.MailRepository;
import com.company.mailing_service.domain.MailStatus;
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

        List<MailRecord> claimed = mailRepository.claimFailedRetrying(batchSize,retryService::isDue);
        if(claimed.isEmpty()){
            log.debug("No failed mail records to retry");
            return;
        }
        log.debug("Claimed {} failed mail records to retry", claimed.size());
        for (MailRecord record : claimed) {
            processClaimed(record);
        }
    }

    private void processClaimed(MailRecord record) {
        try{
            mailingService.retry(record);
        }catch(Exception e){
            log.error ("Unexpected error retrying mail record {}", record.getId(), e);
        }finally {
            mailRepository.findById(record.getId()).filter(current->current.getStatus() == MailStatus.RETRYING).ifPresent(stuck ->{
                log.warn("Mail record {} still RETRYING after retry attempt, marking as FAILED_RETRYING", stuck.getId());
                mailRepository.updateStatus(stuck.getId(), MailStatus.FAILED_RETRYING);
            });
        }
    }
}
