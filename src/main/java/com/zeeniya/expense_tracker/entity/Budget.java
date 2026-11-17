package com.zeeniya.expense_tracker.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.YearMonth;

@Entity

@Table(uniqueConstraints = @UniqueConstraint(name = "uk_budget_user_month", columnNames = {"user_id", "budget_month"}))
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "budget_month")
    private YearMonth month;

    private BigDecimal amount;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public Budget() {}

    public Budget(YearMonth month, BigDecimal amount) {
        this.month = month;
        this.amount = amount;
    }

    public Long getId() {
        return id;
    }

    public YearMonth getMonth() {
        return month;
    }

    public void setMonth(YearMonth month) {
        this.month = month;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}