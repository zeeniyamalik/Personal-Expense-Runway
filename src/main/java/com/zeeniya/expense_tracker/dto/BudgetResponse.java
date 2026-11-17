package com.zeeniya.expense_tracker.dto;

import com.zeeniya.expense_tracker.entity.Budget;

import java.math.BigDecimal;
import java.time.YearMonth;

// What the API sends back for a budget. Returning the Budget entity itself also sent
// the whole user object, so only the month and amount go out now.
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
