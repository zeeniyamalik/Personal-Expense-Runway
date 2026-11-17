package com.zeeniya.expense_tracker.dto;

import java.util.List;

public class CategoryForecast {
    private final String category;
    private final List<MonthForecast> months;

    public CategoryForecast(String category, List<MonthForecast> months) {
        this.category = category;
        this.months = months;
    }

    public String getCategory() {
        return category;
    }

    public List<MonthForecast> getMonths() {
        return months;
    }
}
