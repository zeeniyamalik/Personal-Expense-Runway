package com.zeeniya.expense_tracker.repository;

import com.zeeniya.expense_tracker.entity.BudgetAlert;
import com.zeeniya.expense_tracker.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.YearMonth;
import java.util.*;

public interface BudgetAlertRepository extends JpaRepository<BudgetAlert, Long> {
    Optional<BudgetAlert> findByMonth(YearMonth month);
    Optional<BudgetAlert> findByMonthAndUserId(YearMonth month, Long userId);
    Optional<BudgetAlert> findByIdAndUserId(Long id, Long userId);
    void deleteByUserId(Long userId);
    List<BudgetAlert> findByMonthAndDismissedFalseAndUserId(YearMonth month, Long userId);
}
