package com.zeeniya.expense_tracker.dto;

import com.zeeniya.expense_tracker.entity.Budget;

import java.math.BigDecimal;
import java.time.YearMonth;


public class BudgetResponse {
    private YearMonth month;
    private BigDecimal amount;

    public static BudgetResponse from(Budget budget) {
        BudgetResponse r = new BudgetResponse();
        r.month = budget.getMonth();
        r.amount = budget.getAmount();
        return r;
    }

    public YearMonth getMonth() { return month; }
    public BigDecimal getAmount() { return amount; }
}
