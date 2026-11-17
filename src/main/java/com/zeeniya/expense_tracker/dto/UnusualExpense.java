package com.zeeniya.expense_tracker.dto;

import com.zeeniya.expense_tracker.entity.Expense;


public class UnusualExpense {
    private final Expense expense;
    private final String message;

    public UnusualExpense(Expense expense, String message) {
        this.expense = expense;
        this.message = message;
    }

    public Expense getExpense() { return expense; }
    public String getMessage() { return message; }
}
