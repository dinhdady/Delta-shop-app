package com.hoangdinh.delta_shop_app.service.impl;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {

    @Mock
    private JavaMailSender mailSender;

    @Test
    void sendEmailVerificationBuildsOtpTemplateAndSendsEmail() {
        EmailServiceImpl emailService = new EmailServiceImpl(mailSender);
        ReflectionTestUtils.setField(emailService, "fromEmail", "sender@example.com");

        MimeMessage message = new MimeMessage(Session.getInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(message);

        assertThatCode(() -> emailService.sendEmailVerification("customer@example.com", "Customer", "123456"))
                .doesNotThrowAnyException();

        verify(mailSender).send(any(MimeMessage.class));
    }
}
