package com.company.mailing_service.testConf;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.kafka.KafkaContainer;

@TestPropertySource(properties = "spring.kafka.listener.auto-startup=true")
public class KafkaITConfig extends PostgresITConfig {

  @ServiceConnection
  protected static final KafkaContainer kafka = new KafkaContainer("apache/kafka-native:3.8.0");

  static {
    kafka.start();
  }
}
