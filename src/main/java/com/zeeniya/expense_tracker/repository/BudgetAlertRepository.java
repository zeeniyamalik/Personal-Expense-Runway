package com.zeeniya.expense_tracker.repository;

import com.zeeniya.expense_tracker.entity.AlertType;
import com.zeeniya.expense_tracker.entity.BudgetAlert;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.YearMonth;
import java.util.*;

public interface BudgetAlertRepository extends JpaRepository<BudgetAlert, Long> {
    Optional<BudgetAlert> findByIdAndUserId(Long id, Long userId);
    void deleteByUserId(Long userId);
    void deleteByMonthAndTypeAndUserId(YearMonth month, AlertType type, Long userId);
    List<BudgetAlert> findByMonthAndDismissedFalseAndUserId(YearMonth month, Long userId);

    // Used to avoid creating the same alert twice. Dismissed alerts count too,
    // so dismissing an alert does not make it come straight back.
    boolean existsByMonthAndTypeAndUserId(YearMonth month, AlertType type, Long userId);
    boolean existsByTypeAndExpenseIdAndUserId(AlertType type, Long expenseId, Long userId);
}
