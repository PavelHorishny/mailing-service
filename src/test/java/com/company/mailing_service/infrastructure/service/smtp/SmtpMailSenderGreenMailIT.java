package com.company.mailing_service.infrastructure.service.smtp;

import com.company.mailing_service.domain.MailRecord;
import com.company.mailing_service.domain.MailSender;
import com.company.mailing_service.fixtures.MailRecordFixture;
import com.company.mailing_service.testConf.ITConfig;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetupTest;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;


@TestPropertySource(properties = {
        "mailing.sender-mode=smtp",
        "spring.mail.host=localhost",
        "spring.mail.port=3025"
})
class SmtpMailSenderGreenMailIT extends ITConfig {

    @RegisterExtension
    static GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP);

    @Autowired
    private MailSender mailSender;

    @BeforeEach
    void resetInbox() {
        greenMail.reset();
    }

    @Test
    void sendsMailThroughRealSmtpProtocol() throws Exception {
        MailRecord record = MailRecordFixture.getInstance()
                .withRecipient("someone@example.com")
                .toMailRecord();

        mailSender.send(record, "<p>Hello!</p>");

        MimeMessage[] received = greenMail.getReceivedMessages();
        assertThat(received).hasSize(1);
        assertThat(received[0].getAllRecipients()[0].toString()).isEqualTo("someone@example.com");
        assertThat(GreenMailUtil.getBody(received[0])).contains("Hello!");
    }
}
