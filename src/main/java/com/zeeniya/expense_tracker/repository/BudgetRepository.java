package com.zeeniya.expense_tracker.repository;

import com.zeeniya.expense_tracker.entity.Budget;
import com.zeeniya.expense_tracker.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

public interface BudgetRepository extends JpaRepository<Budget, Long> {
    Optional<Budget> findByMonthAndUserId(YearMonth month, Long userId);
    List<Budget> findByUserId(Long userId);
    void deleteByUserId(Long userId);
    List<Budget> findByMonth(YearMonth month); //used in Burn Rate - Expense Service method
}