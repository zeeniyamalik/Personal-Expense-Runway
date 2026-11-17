package com.zeeniya.expense_tracker.dto;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.YearMonth;

public class BudgetRequest {
    @NotNull(message = "Month is required" )
    private YearMonth month;

    @NotNull(message = "Amount is required" )
    @Positive(message = "Amount must be greater than 0")
    @DecimalMax(value = ExpenseRequest.MAX_AMOUNT, message = ExpenseRequest.MAX_AMOUNT_MESSAGE)
    private BigDecimal amount;

    public BudgetRequest() {}

    public BudgetRequest(YearMonth month, BigDecimal amount) {
        this.month = month;
        this.amount = amount;
    }

    public YearMonth getMonth() {
        return month;
    }

    public void setMonth(YearMonth month) {
        this.month = month;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
