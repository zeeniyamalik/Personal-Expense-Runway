package com.zeeniya.expense_tracker.dto.ai;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ExpenseParseRequest {
    // one expense fits easily in 300 characters, and it keeps the AI prompt small
    @NotBlank(message = "Please describe your expense.")
    @Size(max = 300, message = "Please keep it under 300 characters.")
    private String userInput;

    public String getUserInput() {
        return userInput;
    }

    public void setUserInput(String userInput) {
        this.userInput = userInput;
    }
}

