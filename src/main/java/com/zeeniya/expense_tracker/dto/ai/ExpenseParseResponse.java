package com.zeeniya.expense_tracker.dto.ai;

public class ExpenseParseResponse {

    private String category;
    private double amount;
    private String date;
    private String note;
    private boolean multipleDetected;

    // set by us, not the AI; drives the "We filed this under ..." note
    private boolean categoryChanged;

    // for Jackson
    public ExpenseParseResponse() {}

    public ExpenseParseResponse(String category, double amount, String date, String note, boolean multipleDetected) {
        this.category = category;
        this.amount = amount;
        this.date = date;
        this.note = note;
        this.multipleDetected = multipleDetected;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public boolean isMultipleDetected() {
        return multipleDetected;
    }

    public void setMultipleDetected(boolean multipleDetected) {
        this.multipleDetected = multipleDetected;
    }

    public boolean isCategoryChanged() {
        return categoryChanged;
    }

    public void setCategoryChanged(boolean categoryChanged) {
        this.categoryChanged = categoryChanged;
    }

}