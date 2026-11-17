package com.zeeniya.expense_tracker.dto;

import java.math.BigDecimal;
import java.time.YearMonth;

// One past month on the Budget history screen.
// difference = budget - spent: positive means money left over, negative means overspent.
public class BudgetHistoryItem {
    private final YearMonth month;
    private final BigDecimal budget;
    private final BigDecimal spent;
    private final BigDecimal difference;

    public BudgetHistoryItem(YearMonth month, BigDecimal budget, BigDecimal spent) {
        this.month = month;
        this.budget = budget;
        this.spent = spent;
        this.difference = budget.subtract(spent);
    }

    public YearMonth getMonth() { return month; }
    public BigDecimal getBudget() { return budget; }
    public BigDecimal getSpent() { return spent; }
    public BigDecimal getDifference() { return difference; }
}
