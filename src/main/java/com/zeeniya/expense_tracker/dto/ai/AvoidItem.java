package com.zeeniya.expense_tracker.dto.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AvoidItem {
    private String category;
    private String reason;
    private String severity;

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    // Always "low", "medium" or "high". Anything else the AI sends counts as medium.
    public String getSeverity() {
        if (severity == null) {
            return "medium";
        }
        String value = severity.trim().toLowerCase();
        if (value.equals("low") || value.equals("high")) {
            return value;
        }
        return "medium";
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    // The text shown on the insights card, same wording as the Figma design.
    public String getPriorityLabel() {
        return switch (getSeverity()) {
            case "low" -> "Low priority";
            case "high" -> "High priority";
            default -> "Moderate priority";
        };
    }
}
