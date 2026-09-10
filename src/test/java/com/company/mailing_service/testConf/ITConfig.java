package com.company.mailing_service.testConf;

import com.company.mailing_service.infrastructure.persistence.jpa.MailJpaDao;
import lombok.Getter;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.postgresql.PostgreSQLContainer;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Getter
public class ITConfig {

  @ServiceConnection
  static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16-alpine");

  static {
    postgres.start();
  }

  @Autowired private MailJpaDao mailJpaDao;

  @AfterEach
  void tearDown() {
    mailJpaDao.deleteAll();
  }
}
