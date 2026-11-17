package com.zeeniya.expense_tracker.dto;

import java.math.BigDecimal;
import java.time.YearMonth;

// One past month on the Budget history screen.
// difference = budget - spent: positive means money left over, negative means overspent.
// overspent and message say the same thing in a form the screen can show directly.
public class BudgetHistoryItem {
    private final YearMonth month;
    private final BigDecimal budget;
    private final BigDecimal spent;
    private final BigDecimal difference;
    private final boolean overspent;
    private final String message;

    public BudgetHistoryItem(YearMonth month, BigDecimal budget, BigDecimal spent) {
        this.month = month;
        this.budget = budget;
        this.spent = spent;
        this.difference = budget.subtract(spent);
        this.overspent = difference.signum() < 0;

        if (overspent) {
            this.message = "You overspent by Rs." + difference.negate().toPlainString();
        } else if (difference.signum() == 0) {
            this.message = "You spent exactly your budget";
        } else {
            this.message = "You stayed within budget, Rs." + difference.toPlainString() + " left over";
        }
    }

    public YearMonth getMonth() { return month; }
    public BigDecimal getBudget() { return budget; }
    public BigDecimal getSpent() { return spent; }
    public BigDecimal getDifference() { return difference; }
    public boolean isOverspent() { return overspent; }
    public String getMessage() { return message; }
}
