package com.company.mailing_service.mapper;

import com.company.mailing_service.domain.MailRecord;
import com.company.mailing_service.fixtures.MailRecordFixture;
import com.company.mailing_service.repository.jpa.MailEntity;
import com.company.mailing_service.repository.jpa.MailEntityMapper;
import com.company.mailing_service.repository.jpa.MailEntityMapperImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class MailEntityMapperTest {
    private final MailEntityMapper mailEntityMapper = new MailEntityMapperImpl();
    private MailRecord record;
    private MailEntity entity;

    @BeforeEach
    void setUp() {
        MailRecordFixture fixture = MailRecordFixture.getInstance();

        record = MailRecord.builder()
                .id(fixture.getId())
                .idempotencyKey(fixture.getIdempotencyKey())
                .recipient(fixture.getRecipient())
                .status(fixture.getStatus())
                .attemptCount(fixture.getAttemptCount())
                .lastError(fixture.getLastError())
                .locale(fixture.getLocale())
                .templateKey(fixture.getTemplateKey())
                .createdAt(fixture.getCreatedAt())
                .updatedAt(fixture.getUpdatedAt())
                .build();

       entity = new MailEntity(
                fixture.getId(),
                fixture.getIdempotencyKey(),
                fixture.getRecipient(),
                fixture.getTemplateKey(),
                fixture.getLocale(),
                fixture.getStatus(),
                fixture.getAttemptCount(),
                fixture.getLastError(),
                fixture.getCreatedAt(),
                fixture.getUpdatedAt()
        );
    }

    @Test
    void mapsRecordToEntity(){

        MailEntity testEntity = mailEntityMapper.toEntity(record);

        assertThat(testEntity.getId()).isEqualTo(record.getId());
        assertThat(testEntity.getIdempotencyKey()).isEqualTo(record.getIdempotencyKey());
        assertThat(testEntity.getRecipient()).isEqualTo(record.getRecipient());
        assertThat(testEntity.getTemplateKey()).isEqualTo(record.getTemplateKey());
        assertThat(testEntity.getLocale()).isEqualTo(record.getLocale());
        assertThat(testEntity.getStatus()).isEqualTo(record.getStatus());
        assertThat(testEntity.getAttemptCount()).isEqualTo(record.getAttemptCount());
        assertThat(testEntity.getLastError()).isEqualTo(record.getLastError());
        assertThat(testEntity.getCreatedAt()).isEqualTo(record.getCreatedAt());
        assertThat(testEntity.getUpdatedAt()).isEqualTo(record.getUpdatedAt());
    }

    @Test
    void mapsEntityToRecord() {


        MailRecord testRecord = mailEntityMapper.toRecord(entity);

        assertThat(testRecord.getId()).isEqualTo(entity.getId());
        assertThat(testRecord.getIdempotencyKey()).isEqualTo(entity.getIdempotencyKey());
        assertThat(testRecord.getRecipient()).isEqualTo(entity.getRecipient());
        assertThat(testRecord.getTemplateKey()).isEqualTo(entity.getTemplateKey());
        assertThat(testRecord.getLocale()).isEqualTo(entity.getLocale());
        assertThat(testRecord.getStatus()).isEqualTo(entity.getStatus());
        assertThat(testRecord.getAttemptCount()).isEqualTo(entity.getAttemptCount());
        assertThat(testRecord.getLastError()).isEqualTo(entity.getLastError());
        assertThat(testRecord.getCreatedAt()).isEqualTo(entity.getCreatedAt());
        assertThat(testRecord.getUpdatedAt()).isEqualTo(entity.getUpdatedAt());
    }
}
