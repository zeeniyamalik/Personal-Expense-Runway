package com.zeeniya.expense_tracker.controller;

import com.zeeniya.expense_tracker.dto.BudgetHistoryItem;
import com.zeeniya.expense_tracker.dto.BudgetRequest;
import com.zeeniya.expense_tracker.dto.BudgetResponse;
import com.zeeniya.expense_tracker.entity.User;
import com.zeeniya.expense_tracker.entity.Budget;
import com.zeeniya.expense_tracker.service.BudgetAlertService;
import com.zeeniya.expense_tracker.service.BudgetService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/budgets")
public class BudgetController {

    private final BudgetService budgetService;
    private final BudgetAlertService budgetAlertService;

    // alerts are re-checked after every budget change
    public BudgetController(BudgetService budgetService, BudgetAlertService budgetAlertService) {
        this.budgetService = budgetService;
        this.budgetAlertService = budgetAlertService;
    }

    @PostMapping
    public BudgetResponse setBudget(@Valid @RequestBody BudgetRequest request, @AuthenticationPrincipal User currentUser) {
        Budget saved = budgetService.setBudget(request, currentUser);
        budgetAlertService.checkBurnRateAndCreateAlert(currentUser);
        return BudgetResponse.from(saved);
    }

    @GetMapping("/history")
    public List<BudgetHistoryItem> getBudgetHistory(@AuthenticationPrincipal User currentUser) {
        return budgetService.getBudgetHistory(currentUser);
    }

    @GetMapping("/{month}")
    public BudgetResponse getBudget(@PathVariable YearMonth month, @AuthenticationPrincipal User currentUser) {
        return BudgetResponse.from(budgetService.getBudget(month, currentUser));
    }

    @GetMapping
    public List<BudgetResponse> getAllBudgets(@AuthenticationPrincipal User currentUser) {
        return budgetService.getAllBudgets(currentUser)
                .stream()
                .map(BudgetResponse::from)
                .toList();
    }

    @PutMapping("/{month}")
    public BudgetResponse updateBudget(@PathVariable YearMonth month, @Valid @RequestBody BudgetRequest request, @AuthenticationPrincipal User currentUser) {
        Budget updated = budgetService.updateBudget(month, request, currentUser);
        budgetAlertService.checkBurnRateAndCreateAlert(currentUser);
        return BudgetResponse.from(updated);
    }

    @DeleteMapping("/{month}")
    public void deleteBudget(@PathVariable YearMonth month, @AuthenticationPrincipal User currentUser) {
        budgetService.deleteBudget(month, currentUser);
        budgetAlertService.checkBurnRateAndCreateAlert(currentUser);
    }

}
