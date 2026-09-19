package com.company.mailing_service.infrastructure.service.smtp;

import com.company.mailing_service.domain.MailRecord;
import com.company.mailing_service.domain.MailSender;
import com.company.mailing_service.fixtures.MailRecordFixture;
import com.company.mailing_service.testConf.ITConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Heavier smoke test: sends through the same haravich/fake-smtp-server (MailCatcher) image
 * used for local manual verification, to confirm the real wire protocol works end to end.
 * Assertions go through MailCatcher's REST API rather than in-process objects.
 */
class SmtpMailSenderFakeSmtpIT extends ITConfig {

    private static final GenericContainer<?> fakeSmtp =
            new GenericContainer<>("haravich/fake-smtp-server:latest")
                    .withExposedPorts(1025, 1080)
                    .waitingFor(Wait.forListeningPort());

    static {
        fakeSmtp.start();
    }

    @DynamicPropertySource
    static void mailProperties(DynamicPropertyRegistry registry) {
        registry.add("mailing.sender-mode", () -> "smtp");
        registry.add("spring.mail.host", fakeSmtp::getHost);
        registry.add("spring.mail.port", () -> fakeSmtp.getMappedPort(1025));
    }

    @Autowired
    private MailSender mailSender;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    private String messagesApiUrl() {
        return "http://%s:%d/messages".formatted(fakeSmtp.getHost(), fakeSmtp.getMappedPort(1080));
    }

    @Test
    void sendsMailThroughFakeSmtpServer() throws Exception {
        httpClient.send(
                HttpRequest.newBuilder(URI.create(messagesApiUrl())).DELETE().build(),
                HttpResponse.BodyHandlers.discarding());

        MailRecord record = MailRecordFixture.getInstance()
                .withRecipient("someone@example.com")
                .toMailRecord();

        mailSender.send(record, "<p>Hello from fake-smtp-server!</p>");

        HttpResponse<String> response = httpClient.send(
                HttpRequest.newBuilder(URI.create(messagesApiUrl())).GET().build(),
                HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains("someone@example.com");
    }
}
