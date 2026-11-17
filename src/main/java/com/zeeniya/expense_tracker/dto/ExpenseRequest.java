package com.zeeniya.expense_tracker.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ExpenseRequest {

    public static final String MAX_AMOUNT = "10000000";
    public static final String MAX_AMOUNT_MESSAGE = "Amount can't be more than Rs. 10,000,000 (1 crore).";

    @NotBlank(message = "Category is required")
    private String category;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be greater than 0")
    @DecimalMax(value = MAX_AMOUNT, message = MAX_AMOUNT_MESSAGE)
    private BigDecimal amount;

    @NotNull(message = "Date is required")
    private LocalDate date;

    // database column is varchar(255)
    @Size(max = 255, message = "Note must be 255 characters or less.")
    private String note;

    public ExpenseRequest() {}

    public ExpenseRequest(String category, BigDecimal amount, LocalDate date, String note) {
        this.category = category;
        this.amount = amount;
        this.date = date;
        this.note = note;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getCategory() {
        return category;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getNote() {
        return note;
    }
}
