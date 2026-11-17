package com.zeeniya.expense_tracker.controller;

import com.zeeniya.expense_tracker.dto.AlertResponse;
import com.zeeniya.expense_tracker.dto.AlertsResponse;
import com.zeeniya.expense_tracker.entity.User;
import com.zeeniya.expense_tracker.service.BudgetAlertService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alerts")
public class BudgetAlertController {

    private final BudgetAlertService budgetAlertService;

    public BudgetAlertController(BudgetAlertService budgetAlertService) {
        this.budgetAlertService = budgetAlertService;
    }

    @GetMapping("/active")
    public AlertsResponse getActiveAlerts(@AuthenticationPrincipal User currentUser) {
        List<AlertResponse> alerts = budgetAlertService.getActiveAlerts(currentUser)
                .stream()
                .map(AlertResponse::from)
                .toList();
        return new AlertsResponse(currentUser.getAlertsCheckedAt(), alerts);
    }

    @PutMapping("/{id}/dismiss")
    public void dismissAlert(@PathVariable Long id, @AuthenticationPrincipal User currentUser) {
        budgetAlertService.dismissAlert(id, currentUser);
    }
}
