package com.zeeniya.expense_tracker.dto;

import com.zeeniya.expense_tracker.entity.AlertType;
import com.zeeniya.expense_tracker.entity.BudgetAlert;

import java.time.LocalDateTime;
import java.time.YearMonth;

// What the API sends back for an alert, without the user object.
public class AlertResponse {
    private Long id;
    private AlertType type;
    private String message;
    private YearMonth month;
    private Long expenseId;
    private LocalDateTime createdAt;

    public static AlertResponse from(BudgetAlert alert) {
        AlertResponse r = new AlertResponse();
        r.id = alert.getId();
        r.type = alert.getType();
        r.message = alert.getMessage();
        r.month = alert.getMonth();
        r.expenseId = alert.getExpenseId();
        r.createdAt = alert.getCreatedAt();
        return r;
    }

    public Long getId() { return id; }
    public AlertType getType() { return type; }
    public String getMessage() { return message; }
    public YearMonth getMonth() { return month; }
    public Long getExpenseId() { return expenseId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
