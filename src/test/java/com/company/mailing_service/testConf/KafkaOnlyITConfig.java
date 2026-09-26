package com.company.mailing_service.testConf;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.kafka.KafkaContainer;

@SpringBootTest
@ActiveProfiles("mock")
@TestPropertySource(properties = "spring.kafka.listener.auto-startup=true")
public class KafkaOnlyITConfig {

  @ServiceConnection
  protected static final KafkaContainer kafka = new KafkaContainer("apache/kafka-native:3.8.0");

  static {
    kafka.start();
  }
}
