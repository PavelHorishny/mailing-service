package com.company.mailing_service.infrastructure.persistence.jpa;

import com.company.mailing_service.domain.MailRecord;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MailEntityMapper {
  MailEntity toEntity(MailRecord record);

  MailRecord toRecord(MailEntity entity);
}
