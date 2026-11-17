package com.zeeniya.expense_tracker.dto;

import java.util.List;

// completedMonths and monthsNeeded let the screen explain an empty forecast,
// for example "Forecast unlocks after 3 full months, you have 1 so far".
public class ForecastResponse {
    private final int completedMonths;
    private final int monthsNeeded;
    private final List<CategoryForecast> forecasts;

    public ForecastResponse(int completedMonths, int monthsNeeded, List<CategoryForecast> forecasts) {
        this.completedMonths = completedMonths;
        this.monthsNeeded = monthsNeeded;
        this.forecasts = forecasts;
    }

    public int getCompletedMonths() {
        return completedMonths;
    }

    public int getMonthsNeeded() {
        return monthsNeeded;
    }

    public List<CategoryForecast> getForecasts() {
        return forecasts;
    }
}
