package com.zeeniya.expense_tracker.ai;

import com.zeeniya.expense_tracker.dto.ExpenseResponse;
import com.zeeniya.expense_tracker.dto.ai.ExpenseParseRequest;
import com.zeeniya.expense_tracker.dto.ai.ExpenseParseResponse;
import com.zeeniya.expense_tracker.dto.ai.InsightResponse;
import com.zeeniya.expense_tracker.entity.User;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
public class GeminiController {

    private final GeminiService geminiService;

    public GeminiController(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    @PostMapping("/api/expenses/parse")
    public ExpenseResponse parseAndSave(
            @RequestBody ExpenseParseRequest request,
            @AuthenticationPrincipal User currentUser) {

        return ExpenseResponse.from(geminiService.parseAndSaveExpense(request.getUserInput(), currentUser));
    }

    // Same parsing as /parse, but nothing is saved.
    @PostMapping("/api/expenses/parse/preview")
    public ExpenseParseResponse previewParse(@RequestBody ExpenseParseRequest request) {
        return geminiService.previewExpense(request.getUserInput());
    }

    @GetMapping("/api/ai/insights")
    public InsightResponse getInsights(@AuthenticationPrincipal User currentUser) {
        return geminiService.getSpendingInsights(currentUser);
    }
}

