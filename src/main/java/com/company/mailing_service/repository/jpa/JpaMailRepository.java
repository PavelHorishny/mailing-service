package com.company.mailing_service.repository.jpa;

import com.company.mailing_service.domain.MailRecord;
import com.company.mailing_service.domain.MailStatus;
import com.company.mailing_service.repository.MailRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "mailing", name = "repository-mode", havingValue = "jpa")
public class JpaMailRepository implements MailRepository {

    private final MailJpaDao dao;
    private final MailEntityMapper mapper;

    @Override
    @Transactional
    public MailRecord save(MailRecord record) {
       /* UUID id = record.getId() != null ? record.getId() : UUID.randomUUID();
        Instant now = Instant.now();

        MailRecord entity = record.toBuilder()
                .id(id)
                .createdAt(record.getCreatedAt() != null ? record.getCreatedAt() : now)
                .updatedAt(now)
                .build();*/

        return mapper.toRecord(dao.save(mapper.toEntity(record)));

    }

    @Override
    @Transactional(readOnly = true)
    public Optional<MailRecord> findByIdempotencyKey(String idempotencyKey) {
        return dao.findByIdempotencyKey(idempotencyKey).map(mapper::toRecord);
    }

    @Override
    @Transactional
    public void updateStatus(UUID id, MailStatus status) {
        dao.findById(id).ifPresent(entity -> {
            entity.setStatus(status);
            //entity.setUpdatedAt(Instant.now());
        });
    }

    @Override
    @Transactional
    public void incrementAttempt(UUID id, String lastError) {
        dao.findById(id).ifPresent(entity -> {
            entity.setAttemptCount(entity.getAttemptCount() + 1);
            entity.setLastError(lastError);
            //entity.setUpdatedAt(Instant.now());
        });
    }

}

