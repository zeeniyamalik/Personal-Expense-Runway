package com.zeeniya.expense_tracker.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.time.YearMonth;

@Entity
public class BudgetAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // not unique: a month can have several alerts
    @Column(name = "alert_month")
    private YearMonth month;

    // older alerts have no type
    @Enumerated(EnumType.STRING)
    private AlertType type;

    // only for UNUSUAL_EXPENSE
    private Long expenseId;

    private String message;
    private boolean dismissed;
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public BudgetAlert() {}

    public BudgetAlert(YearMonth month, AlertType type, String message, Long expenseId) {
        this.month = month;
        this.type = type;
        this.message = message;
        this.expenseId = expenseId;
        this.dismissed = false;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public YearMonth getMonth() { return month; }
    public AlertType getType() { return type; }
    public Long getExpenseId() { return expenseId; }
    public String getMessage() { return message; }
    public boolean isDismissed() { return dismissed; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setDismissed(boolean dismissed) { this.dismissed = dismissed; }
}
