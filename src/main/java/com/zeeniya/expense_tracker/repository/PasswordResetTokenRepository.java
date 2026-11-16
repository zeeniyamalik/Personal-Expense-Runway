package com.zeeniya.expense_tracker.repository;

import com.zeeniya.expense_tracker.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByTokenHash(String tokenHash);
    List<PasswordResetToken> findByUserIdAndUsedFalse(Long userId);
    void deleteByUserId(Long userId);
}
