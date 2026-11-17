package com.zeeniya.expense_tracker.mail;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class SmtpEmailSenderTest {

    @Test
    void sendsTheEmailFromTheConfiguredAddress() {
        JavaMailSender mailSender = mock(JavaMailSender.class);
        SmtpEmailSender sender = new SmtpEmailSender(mailSender, "runway@example.com");

        sender.send("user@example.com", "Reset your password", "link here");

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        SimpleMailMessage message = captor.getValue();
        assertThat(message.getFrom()).isEqualTo("runway@example.com");
        assertThat(message.getTo()).containsExactly("user@example.com");
        assertThat(message.getSubject()).isEqualTo("Reset your password");
        assertThat(message.getText()).isEqualTo("link here");
    }

    @Test
    void aFailedSendIsLoggedNotThrown() {
        JavaMailSender mailSender = mock(JavaMailSender.class);
        doThrow(new MailSendException("SMTP down")).when(mailSender).send(any(SimpleMailMessage.class));
        SmtpEmailSender sender = new SmtpEmailSender(mailSender, "runway@example.com");

        assertThatCode(() -> sender.send("user@example.com", "s", "b")).doesNotThrowAnyException();
    }
}
