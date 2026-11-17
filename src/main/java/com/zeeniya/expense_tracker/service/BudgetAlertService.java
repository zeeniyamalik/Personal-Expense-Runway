package com.zeeniya.expense_tracker.service;

import com.zeeniya.expense_tracker.dto.BurnRateWarning;
import com.zeeniya.expense_tracker.dto.UnusualExpense;
import com.zeeniya.expense_tracker.entity.AlertType;
import com.zeeniya.expense_tracker.entity.BudgetAlert;
import com.zeeniya.expense_tracker.entity.User;
import com.zeeniya.expense_tracker.exception.ResourceNotFoundException;
import com.zeeniya.expense_tracker.repository.BudgetAlertRepository;

import com.zeeniya.expense_tracker.repository.UserRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class BudgetAlertService {

    private final BudgetAlertRepository budgetAlertRepository;
    private final ExpenseService expenseService;
    private final UserRepository userRepository;

    public BudgetAlertService(BudgetAlertRepository budgetAlertRepository, ExpenseService expenseService, UserRepository userRepository) {
        this.budgetAlertRepository = budgetAlertRepository;
        this.expenseService = expenseService;
        this.userRepository = userRepository;
    }

    @Scheduled(cron = "0 0 8 * * *")          //scheduled method calling normal method
    public void checkBurnRateAndCreateAlertsForAllUsers() {
        List<User> allUsers = userRepository.findAll();
        for (User user : allUsers) {
            checkBurnRateAndCreateAlert(user);
        }
    }

    // over budget: one per month, removed if the pace drops back under the budget
    // unusual expense: one per expense, removed if it's deleted or no longer unusual
    // a dismissed alert is never created again
    public void checkBurnRateAndCreateAlert(User currentUser) {
        YearMonth currentMonth = YearMonth.now();
        Long userId = currentUser.getId();
        List<BudgetAlert> activeAlerts = budgetAlertRepository.findByMonthAndDismissedFalseAndUserId(currentMonth, userId);

        BurnRateWarning burnRate = expenseService.getBurnRate(currentUser);
        if (burnRate.isOverBudget()) {
            if (!budgetAlertRepository.existsByMonthAndTypeAndUserId(currentMonth, AlertType.OVER_BUDGET, userId)) {
                String message = "You're projected to spend Rs." + burnRate.getProjectedTotal()
                        + " this month, over your budget of Rs." + burnRate.getMonthlyBudget() + "!";
                save(new BudgetAlert(currentMonth, AlertType.OVER_BUDGET, message, null), currentUser);
            }
        } else {
            for (BudgetAlert alert : activeAlerts) {
                if (alert.getType() == AlertType.OVER_BUDGET) {
                    budgetAlertRepository.delete(alert);
                }
            }
        }

        Set<Long> unusualExpenseIds = new HashSet<>();
        for (UnusualExpense unusual : expenseService.getUnusualExpenses(currentUser)) {
            Long expenseId = unusual.getExpense().getId();
            unusualExpenseIds.add(expenseId);
            if (!budgetAlertRepository.existsByTypeAndExpenseIdAndUserId(AlertType.UNUSUAL_EXPENSE, expenseId, userId)) {
                save(new BudgetAlert(currentMonth, AlertType.UNUSUAL_EXPENSE, unusual.getMessage(), expenseId), currentUser);
            }
        }
        for (BudgetAlert alert : activeAlerts) {
            if (alert.getType() == AlertType.UNUSUAL_EXPENSE && !unusualExpenseIds.contains(alert.getExpenseId())) {
                budgetAlertRepository.delete(alert);
            }
        }

        userRepository.updateAlertsCheckedAt(userId, LocalDateTime.now());
    }

    private void save(BudgetAlert alert, User user) {
        alert.setUser(user);
        budgetAlertRepository.save(alert);
    }

    //alert banner goes away when dismissed
    public List<BudgetAlert> getActiveAlerts(User currentUser) {
        YearMonth currentMonth = YearMonth.now();
        return budgetAlertRepository.findByMonthAndDismissedFalseAndUserId(currentMonth, currentUser.getId());
    }

    public void dismissAlert(Long id, User currentUser) {
        BudgetAlert alert = budgetAlertRepository.findByIdAndUserId(id, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found with ID " + id));
        alert.setDismissed(true);
        budgetAlertRepository.save(alert);
    }

}
