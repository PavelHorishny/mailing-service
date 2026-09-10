package com.company.mailing_service.infrastructure.web;

import com.company.mailing_service.domain.MailSendException;
import com.company.mailing_service.domain.MailSender;
import com.company.mailing_service.domain.MailStatus;
import com.company.mailing_service.fixtures.MailRecordFixture;
import com.company.mailing_service.infrastructure.persistence.jpa.MailEntity;
import com.company.mailing_service.infrastructure.persistence.jpa.MailEntityMapper;
import com.company.mailing_service.infrastructure.persistence.jpa.MailJpaDao;
import com.company.mailing_service.testConf.ITConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.ObjectMapper;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MailControllerIT extends ITConfig {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MailJpaDao mailJpaDao;

    @Autowired
    private MailEntityMapper mailEntityMapper;

    @MockitoBean
    private MailSender mailSender;

    private UUID seed(MailRecordFixture fixture) {
        MailEntity entity = mailEntityMapper.toEntity(fixture.toMailRecord());
        return mailJpaDao.save(entity).getId();
    }

    private <T> T readBody(MvcResult result, Class<T> type) throws UnsupportedEncodingException {
        return objectMapper.readValue(result.getResponse().getContentAsString(), type);
    }

    @Test
    void returnsSentStatusWhenRetryingFailedRetryingRecord() throws Exception {
        doNothing().when(mailSender).send(any(), any());
        UUID id = seed(MailRecordFixture.getInstance().withStatus(MailStatus.FAILED_RETRYING));

        MvcResult result = mockMvc.perform(post("/api/mails/{id}/retry", id))
                .andExpect(status().isOk())
                .andReturn();

        MailStatusResponse response = readBody(result, MailStatusResponse.class);
        assertThat(response.id()).isEqualTo(id);
        assertThat(response.status()).isEqualTo(MailStatus.SENT);
    }

    @Test
    void returnsNotFoundWhenRecordDoesNotExist() throws Exception {
        UUID id = UUID.randomUUID();

        MvcResult result = mockMvc.perform(post("/api/mails/{id}/retry", id))
                .andExpect(status().isNotFound())
                .andReturn();

        ErrorResponse response = readBody(result, ErrorResponse.class);
        assertThat(response.message()).isEqualTo("Mail record not found: " + id);
    }

    @Test
    void returnsConflictWhenRecordStatusIsNotFailedRetrying() throws Exception {
        UUID id = seed(MailRecordFixture.getInstance().withStatus(MailStatus.NEW));

        MvcResult result = mockMvc.perform(post("/api/mails/{id}/retry", id))
                .andExpect(status().isConflict())
                .andReturn();

        ErrorResponse response = readBody(result, ErrorResponse.class);
        assertThat(response.message())
                .isEqualTo("Mail record " + id + " cannot be retried, current status: NEW");
    }

    @Test
    void marksUndeliveredWhenSendFailsAndAttemptsAreExhausted() throws Exception {
        doThrow(new MailSendException("boom")).when(mailSender).send(any(), any());
        UUID id = seed(MailRecordFixture.getInstance()
                .withStatus(MailStatus.FAILED_RETRYING)
                .withAttemptCount(4));

        MvcResult result = mockMvc.perform(post("/api/mails/{id}/retry", id))
                .andExpect(status().isOk())
                .andReturn();

        MailStatusResponse response = readBody(result, MailStatusResponse.class);
        assertThat(response.status()).isEqualTo(MailStatus.UNDELIVERED);
        assertThat(response.attemptCount()).isEqualTo(5);
    }
}
