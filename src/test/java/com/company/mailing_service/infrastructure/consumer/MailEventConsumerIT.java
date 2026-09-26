package com.company.mailing_service.infrastructure.consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.company.mailing_service.domain.MailRecord;
import com.company.mailing_service.domain.MailRepository;
import com.company.mailing_service.fixtures.MailEventFixture;
import com.company.mailing_service.testConf.KafkaOnlyITConfig;
import java.time.Duration;
import java.util.Properties;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import tools.jackson.databind.ObjectMapper;

class MailEventConsumerIT extends KafkaOnlyITConfig {

    @Autowired
    private MailRepository mailRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private KafkaListenerEndpointRegistry registry;

    @Value("${mailing.topic}")
    private String topic;

    @Test
    void consumesPublishedEventAndPersistsMailRecord() throws Exception {
        // Without this, the consumer may still be joining the group when we
        // publish, so with auto.offset.reset=latest it would seek past our
        // message and never see it.
        registry.getListenerContainers()
                .forEach(container -> ContainerTestUtils.waitForAssignment(container, 1));

        MailEventFixture fx = MailEventFixture.getInstance().withEventId("evt-kafka-1");

        // Plain JSON, no Spring type headers - mirrors a producer from another
        // service that only knows the wire format, not our Java classes.
        String payload = objectMapper.writeValueAsString(fx.toMailEvent());

        Properties props = new Properties();
        props.put("bootstrap.servers", kafka.getBootstrapServers());
        props.put("key.serializer", StringSerializer.class.getName());
        props.put("value.serializer", StringSerializer.class.getName());

        try (KafkaProducer<String, String> producer = new KafkaProducer<>(props)) {
            producer.send(new ProducerRecord<>(topic, fx.getEventId(), payload)).get();
        }

        await().atMost(Duration.ofSeconds(10))
                .untilAsserted(() ->
                        assertThat(mailRepository.findByIdempotencyKey(fx.getEventId())).isPresent());

        MailRecord saved = mailRepository.findByIdempotencyKey(fx.getEventId()).orElseThrow();
        assertThat(saved.getRecipient()).isEqualTo(fx.getRecipient());
    }
}
