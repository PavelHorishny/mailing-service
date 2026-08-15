package com.company.mailing_service.infrastructure.persistence.jpa;

import com.company.mailing_service.domain.MailRecord;
import com.company.mailing_service.domain.MailStatus;
import com.company.mailing_service.domain.MailRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Repository
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "mailing", name = "repository-mode", havingValue = "jpa")
public class JpaMailRepository implements MailRepository {

    private final MailJpaDao dao;
    private final MailEntityMapper mapper;

    @Override
    @Transactional
    public MailRecord save(MailRecord record) {
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
        dao.findById(id).ifPresentOrElse(entity ->
            entity.setStatus(status),()-> log.warn("updateStatus called for unknown mail record id {}", id));
    }

    @Override
    @Transactional
    public void incrementAttempt(UUID id, String lastError) {
        dao.findById(id).ifPresentOrElse(entity -> {
            entity.setAttemptCount(entity.getAttemptCount() + 1);
            entity.setLastError(lastError);
        }, () -> log.warn("incrementAttempt called for unknown mail record id {}", id));
    }

}

