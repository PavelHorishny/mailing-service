package com.company.mailing_service.domain;

public enum MailStatus {
  NEW,
  RETRYING,
  SENT,
  FAILED_RETRYING,
  UNDELIVERED
}
