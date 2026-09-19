package com.company.mailing_service.infrastructure.service.smtp;

import com.company.mailing_service.domain.MailRecord;
import com.company.mailing_service.domain.MailSendException;
import com.company.mailing_service.domain.MailSender;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "mailing", name = "sender-mode", havingValue = "smtp")
public class SmtpMailSender implements MailSender {

    private final JavaMailSender javaMailSender;

    @Override
    public void send(MailRecord record, String body) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setTo(record.getRecipient());
            helper.setText(body, true);
            javaMailSender.send(message);
            log.info("SMTP-sent mail to {} (template={})", record.getRecipient(), record.getTemplateKey());
        } catch (MessagingException | MailException e) {
            throw new MailSendException("Failed to send mail to " + record.getRecipient(), e);
        }
    }
}
