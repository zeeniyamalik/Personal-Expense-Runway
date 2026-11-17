package com.zeeniya.expense_tracker.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

// one error shape for the whole API; fieldErrors only for form validation
public class ErrorResponse {
    private final String message;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private final Map<String, String> fieldErrors;

    public ErrorResponse(String message) {
        this(message, Map.of());
    }

    public ErrorResponse(String message, Map<String, String> fieldErrors) {
        this.message = message;
        this.fieldErrors = fieldErrors;
    }

    public String getMessage() { return message; }
    public Map<String, String> getFieldErrors() { return fieldErrors; }
}
