package com.zeeniya.expense_tracker.service;

import com.zeeniya.expense_tracker.dto.BudgetHistoryItem;
import com.zeeniya.expense_tracker.dto.BudgetRequest;
import com.zeeniya.expense_tracker.dto.MonthlySummary;
import com.zeeniya.expense_tracker.entity.AlertType;
import com.zeeniya.expense_tracker.entity.Budget;
import com.zeeniya.expense_tracker.entity.User;
import com.zeeniya.expense_tracker.exception.ResourceNotFoundException;
import com.zeeniya.expense_tracker.repository.BudgetAlertRepository;
import com.zeeniya.expense_tracker.repository.BudgetRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final BudgetAlertRepository budgetAlertRepository;
    private final ExpenseService expenseService;

    public BudgetService(BudgetRepository budgetRepository, BudgetAlertRepository budgetAlertRepository,
                         ExpenseService expenseService) {
        this.budgetRepository = budgetRepository;
        this.budgetAlertRepository = budgetAlertRepository;
        this.expenseService = expenseService;
    }


    public Budget setBudget(BudgetRequest request, User currentUser) {
        if (budgetRepository.findByMonthAndUserId(request.getMonth(), currentUser.getId()).isPresent()) {
            throw new IllegalStateException(
                    "Budget already exists for " + request.getMonth()
            );
        }

        Budget budget = new Budget(request.getMonth(), request.getAmount());
        budget.setUser(currentUser);
        return budgetRepository.save(budget);
    }

    public Budget getBudget(YearMonth month, User currentUser) {
        return budgetRepository.findByMonthAndUserId(month, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found for " + month));
    }

    public List<Budget> getAllBudgets(User currentUser) {
        return budgetRepository.findByUserId(currentUser.getId());
    }

    // The over-budget alert was based on the old amount, so it is removed (even if dismissed).
    // The next alert check decides again with the new amount.
    @Transactional
    public Budget updateBudget(YearMonth month, BudgetRequest request, User currentUser) {
        Budget existingBudget = getBudget(month, currentUser);
        existingBudget.setAmount(request.getAmount());

        budgetAlertRepository.deleteByMonthAndTypeAndUserId(month, AlertType.OVER_BUDGET, currentUser.getId());
        return budgetRepository.save(existingBudget);
    }

    // The over-budget alert was based on this budget, so it goes too.
    // Unusual-expense alerts do not depend on the budget and stay.
    @Transactional
    public void deleteBudget(YearMonth month, User currentUser) {
        Budget existingBudget = getBudget(month, currentUser);

        budgetRepository.delete(existingBudget);
        budgetAlertRepository.deleteByMonthAndTypeAndUserId(month, AlertType.OVER_BUDGET, currentUser.getId());
    }

    // Past months that had a budget, newest first, with what was actually spent.
    // The current month is not included because it is not over yet.
    public List<BudgetHistoryItem> getBudgetHistory(User currentUser) {
        YearMonth currentMonth = YearMonth.now();

        Map<YearMonth, BigDecimal> spentPerMonth = new HashMap<>();
        for (MonthlySummary summary : expenseService.getMonthlySummary(currentUser)) {
            spentPerMonth.put(summary.getMonth(), summary.getTotal());
        }

        List<BudgetHistoryItem> history = new ArrayList<>();
        for (Budget budget : budgetRepository.findByUserId(currentUser.getId())) {
            if (budget.getMonth().isBefore(currentMonth)) {
                BigDecimal spent = spentPerMonth.getOrDefault(budget.getMonth(), BigDecimal.ZERO);
                history.add(new BudgetHistoryItem(budget.getMonth(), budget.getAmount(), spent));
            }
        }
        history.sort(Comparator.comparing(BudgetHistoryItem::getMonth).reversed());
        return history;
    }
}
