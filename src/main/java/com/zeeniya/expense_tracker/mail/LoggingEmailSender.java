package com.zeeniya.expense_tracker.mail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

// Development stand-in: prints the email in the console instead of sending it.
// The body holds a live reset link, so this must not be the sender in production.
@Component
@ConditionalOnProperty(name = "app.mail.sender", havingValue = "log", matchIfMissing = true)
public class LoggingEmailSender implements EmailSender {

    private static final Logger log = LoggerFactory.getLogger(LoggingEmailSender.class);

    public LoggingEmailSender() {
        log.warn("Emails are NOT sent: LoggingEmailSender prints them here. Set app.mail.sender to a real sender before going live.");
    }

    @Override
    public void send(String to, String subject, String body) {
        log.info("[DEV EMAIL] to={} subject={}\n{}", to, subject, body);
    }
}
