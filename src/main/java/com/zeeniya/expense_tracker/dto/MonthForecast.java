package com.zeeniya.expense_tracker.dto;

import java.math.BigDecimal;
import java.time.YearMonth;

public class MonthForecast {
    private final YearMonth month;
    private final BigDecimal forecast;

    public MonthForecast(YearMonth month, BigDecimal forecast) {
        this.month = month;
        this.forecast = forecast;
    }

    public YearMonth getMonth() {
        return month;
    }

    public BigDecimal getForecast() {
        return forecast;
    }
}
