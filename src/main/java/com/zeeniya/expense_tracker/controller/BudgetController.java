package com.zeeniya.expense_tracker.controller;

import com.zeeniya.expense_tracker.dto.BudgetHistoryItem;
import com.zeeniya.expense_tracker.dto.BudgetRequest;
import com.zeeniya.expense_tracker.dto.BudgetResponse;
import com.zeeniya.expense_tracker.entity.User;
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

    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    @PostMapping
    public BudgetResponse setBudget(@Valid @RequestBody BudgetRequest request, @AuthenticationPrincipal User currentUser) {
        return BudgetResponse.from(budgetService.setBudget(request, currentUser));
    }

    // Spring picks an exact path over /{month}, so "history" is never read as a month.
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
        return BudgetResponse.from(budgetService.updateBudget(month, request, currentUser));
    }

    @DeleteMapping("/{month}")
    public void deleteBudget(@PathVariable YearMonth month, @AuthenticationPrincipal User currentUser) {
        budgetService.deleteBudget(month, currentUser);
    }

}
