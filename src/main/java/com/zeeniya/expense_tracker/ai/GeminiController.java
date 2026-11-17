package com.zeeniya.expense_tracker.ai;

import com.zeeniya.expense_tracker.dto.ExpenseResponse;
import com.zeeniya.expense_tracker.dto.ai.ExpenseParseRequest;
import com.zeeniya.expense_tracker.dto.ai.ExpenseParseResponse;
import com.zeeniya.expense_tracker.dto.ai.InsightResponse;
import com.zeeniya.expense_tracker.entity.Expense;
import com.zeeniya.expense_tracker.entity.User;
import com.zeeniya.expense_tracker.service.BudgetAlertService;

import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
public class GeminiController {

    private final GeminiService geminiService;
    private final BudgetAlertService budgetAlertService;

    public GeminiController(GeminiService geminiService, BudgetAlertService budgetAlertService) {
        this.geminiService = geminiService;
        this.budgetAlertService = budgetAlertService;
    }

    @PostMapping("/api/expenses/parse")
    public ExpenseResponse parseAndSave(
            @Valid @RequestBody ExpenseParseRequest request,
            @AuthenticationPrincipal User currentUser) {

        Expense saved = geminiService.parseAndSaveExpense(request.getUserInput(), currentUser);
        budgetAlertService.checkBurnRateAndCreateAlert(currentUser);
        return ExpenseResponse.from(saved);
    }

    // Same parsing as /parse, but nothing is saved.
    @PostMapping("/api/expenses/parse/preview")
    public ExpenseParseResponse previewParse(@Valid @RequestBody ExpenseParseRequest request) {
        return geminiService.previewExpense(request.getUserInput());
    }

    @GetMapping("/api/ai/insights")
    public InsightResponse getInsights(@AuthenticationPrincipal User currentUser) {
        return geminiService.getSpendingInsights(currentUser);
    }
}

