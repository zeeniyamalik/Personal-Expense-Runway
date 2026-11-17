package com.zeeniya.expense_tracker.dto;

import java.time.LocalDateTime;
import java.util.List;

// The Alerts screen: the active alerts plus when they were last checked
// (null if the check has never run for this user).
public class AlertsResponse {
    private final LocalDateTime lastChecked;
    private final List<AlertResponse> alerts;

    public AlertsResponse(LocalDateTime lastChecked, List<AlertResponse> alerts) {
        this.lastChecked = lastChecked;
        this.alerts = alerts;
    }

    public LocalDateTime getLastChecked() { return lastChecked; }
    public List<AlertResponse> getAlerts() { return alerts; }
}
