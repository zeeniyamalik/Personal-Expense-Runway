package com.zeeniya.expense_tracker.service;

import com.zeeniya.expense_tracker.dto.ResetPasswordRequest;
import com.zeeniya.expense_tracker.entity.PasswordResetToken;
import com.zeeniya.expense_tracker.entity.User;
import com.zeeniya.expense_tracker.mail.EmailSender;
import com.zeeniya.expense_tracker.repository.PasswordResetTokenRepository;
import com.zeeniya.expense_tracker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;

@Service
public class PasswordResetService {

    private static final long TOKEN_MINUTES = 30;

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailSender emailSender;
    private final String frontendBaseUrl;
    private final SecureRandom secureRandom = new SecureRandom();

    public PasswordResetService(UserRepository userRepository, PasswordResetTokenRepository tokenRepository,
                                PasswordEncoder passwordEncoder, EmailSender emailSender,
                                @Value("${app.frontend-base-url:http://localhost:8080}") String frontendBaseUrl) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailSender = emailSender;
        this.frontendBaseUrl = frontendBaseUrl;
    }

    // silent for unknown emails, so nobody can check who has an account
    @Transactional
    public void requestReset(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            // only the newest link works.
            tokenRepository.findByUserIdAndUsedFalse(user.getId()).forEach(old -> old.setUsed(true));

            String token = newToken();
            tokenRepository.save(new PasswordResetToken(hash(token), user, LocalDateTime.now().plusMinutes(TOKEN_MINUTES)));

            String link = frontendBaseUrl + "/auth.html?reset=" + token;
            emailSender.send(user.getEmail(), "Reset your Expense Runway password",
                    "Use this link to choose a new password. It works once and expires in " + TOKEN_MINUTES + " minutes:\n" + link);
        });
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken token = tokenRepository.findByTokenHash(hash(request.getToken()))
                .filter(t -> !t.isUsed() && t.getExpiresAt().isAfter(LocalDateTime.now()))
                .orElseThrow(() -> new IllegalArgumentException("This reset link is invalid or has expired."));

        User user = token.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        token.setUsed(true);
    }

    private String newToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
