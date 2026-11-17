package com.zeeniya.expense_tracker.dto;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.YearMonth;
import java.util.Locale;

// one past month on the budget history screen, difference < 0 means overspent
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

        // same wording as the Figma frame
        if (overspent) {
            this.message = "Over budget · Rs. " + formatAmount(difference.negate()) + " extra";
        } else {
            this.message = "Within budget · Rs. " + formatAmount(difference) + " left";
        }
    }

    // 1500.00 -> "1,500", 1500.50 -> "1,500.50"
    private static String formatAmount(BigDecimal amount) {
        boolean whole = amount.stripTrailingZeros().scale() <= 0;
        String pattern = whole ? "#,##0" : "#,##0.00";
        return new DecimalFormat(pattern, DecimalFormatSymbols.getInstance(Locale.US)).format(amount);
    }

    public YearMonth getMonth() { return month; }
    public BigDecimal getBudget() { return budget; }
    public BigDecimal getSpent() { return spent; }
    public BigDecimal getDifference() { return difference; }
    public boolean isOverspent() { return overspent; }
    public String getMessage() { return message; }
}
