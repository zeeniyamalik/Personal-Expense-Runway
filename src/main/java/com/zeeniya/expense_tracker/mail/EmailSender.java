package com.zeeniya.expense_tracker.mail;

// The reset logic only knows this interface. LoggingEmailSender is the default;
// a real SMTP sender can replace it later without touching the reset code.
public interface EmailSender {
    void send(String to, String subject, String body);
}
