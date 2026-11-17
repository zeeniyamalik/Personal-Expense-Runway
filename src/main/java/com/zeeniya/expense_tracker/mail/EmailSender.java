package com.zeeniya.expense_tracker.mail;

// LoggingEmailSender in development, SmtpEmailSender for real emails
public interface EmailSender {
    void send(String to, String subject, String body);
}
