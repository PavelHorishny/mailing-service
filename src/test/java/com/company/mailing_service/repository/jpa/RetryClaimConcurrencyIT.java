package com.company.mailing_service.repository.jpa;

import com.company.mailing_service.domain.MailRecord;
import com.company.mailing_service.domain.MailRepository;
import com.company.mailing_service.domain.MailStatus;
import com.company.mailing_service.fixtures.MailRecordFixture;
import com.company.mailing_service.testConf.ITConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class RetryClaimConcurrencyIT extends ITConfig {

    @Autowired
    private MailRepository mailRepository;

    @Test
    void concurrentInstancesNeverClaimTheSameRecord() throws Exception {
        int recordCount = 20;
        int instanceCount = 4;
        int batchSize = 5;

        IntStream.range(0, recordCount).forEach(i -> mailRepository.save(MailRecordFixture.getInstance()
                .withStatus(MailStatus.FAILED_RETRYING)
                .toMailRecord()));

        ExecutorService executor = Executors.newFixedThreadPool(instanceCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        List<Future<List<MailRecord>>> futures = new ArrayList<>();
        for (int i = 0; i < instanceCount; i++) {
            futures.add(executor.submit(() -> {
                startLatch.await();
                return mailRepository.claimFailedRetrying(batchSize, record -> true);
            }));
        }

        startLatch.countDown();

        List<UUID> claimedIds = new ArrayList<>();
        try {
            for (Future<List<MailRecord>> future : futures) {
                future.get(10, TimeUnit.SECONDS)
                        .forEach(record -> claimedIds.add(record.getId()));
            }
        } finally {
            executor.shutdown();
        }

        assertThat(claimedIds).hasSize(recordCount);
        assertThat(new HashSet<>(claimedIds)).hasSize(recordCount);
    }
}
