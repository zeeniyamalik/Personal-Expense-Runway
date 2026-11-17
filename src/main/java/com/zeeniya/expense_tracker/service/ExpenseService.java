package com.zeeniya.expense_tracker.service;

import com.zeeniya.expense_tracker.dto.*;
import com.zeeniya.expense_tracker.entity.Budget;
import com.zeeniya.expense_tracker.entity.Expense;
import com.zeeniya.expense_tracker.entity.User;
import com.zeeniya.expense_tracker.exception.ResourceNotFoundException;
import com.zeeniya.expense_tracker.repository.BudgetRepository;
import com.zeeniya.expense_tracker.repository.ExpenseRepository;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class ExpenseService {
    private final ExpenseRepository expenseRepository;
    private final BudgetRepository budgetRepository;


    public ExpenseService(ExpenseRepository expenseRepository, BudgetRepository budgetRepository) {
        this.expenseRepository = expenseRepository;
        this.budgetRepository = budgetRepository;
    }

    //CRUD Operations start

    public List<Expense> getAllExpenses(User currentUser) {
        return expenseRepository.findByUserId(currentUser.getId());
    }

    // null filters are ignored, newest first
    public List<Expense> getExpenses(User currentUser, String category, LocalDate from, LocalDate to) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new IllegalArgumentException("\"from\" date must be on or before the \"to\" date.");
        }
        return expenseRepository.findByUserId(currentUser.getId())
                .stream()
                .filter(e -> category == null || category.isBlank() || e.getCategory().equalsIgnoreCase(category.trim()))
                .filter(e -> from == null || !e.getDate().isBefore(from))
                .filter(e -> to == null || !e.getDate().isAfter(to))
                .sorted(Comparator.comparing(Expense::getDate).thenComparing(Expense::getId).reversed())
                .toList();
    }

    public Expense getExpenseById(Long id, User currentUser) {
        return expenseRepository.findByIdAndUserId(id, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found with ID " + id));
    }

    public Expense addExpense(ExpenseRequest request, User currentUser) {
        validateExpenseDate(request.getDate());
        Expense expense = new Expense(
                request.getCategory(),
                request.getAmount(),
                request.getDate(),
                request.getNote()
        );
        expense.setUser(currentUser);
        return expenseRepository.save(expense);
    }

    public void deleteExpense(Long id, User currentUser) {
        Expense existingExpense = getExpenseById(id, currentUser);
        expenseRepository.delete(existingExpense);
    }

    public Expense updateExpense(Long id, ExpenseRequest request, User currentUser) {
        validateExpenseDate(request.getDate());
        Expense existingExpense = getExpenseById(id, currentUser);
        existingExpense.setCategory(request.getCategory());
        existingExpense.setAmount(request.getAmount());
        existingExpense.setDate(request.getDate());
        existingExpense.setNote(request.getNote());
        return expenseRepository.save(existingExpense);
    }

    private void validateExpenseDate(LocalDate date) {
        if (date.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Expense date cannot be in the future: " + date);
        }
    }

    //CRUD Operations end

    //CATEGORY SUMMARY FEATURE
    // month null = all time; biggest first, so the first one "leads"
    public List<CategorySummary> getCategorySummary(User currentUser, YearMonth month) {
        List<CategorySummary> categorySummary = new ArrayList<>();
        List<Expense> expenses = expenseRepository.findByUserId(currentUser.getId());

        Map<String, BigDecimal> categoryTotals = new HashMap<>();
        BigDecimal totalSpent = BigDecimal.ZERO;

        for (Expense expense : expenses) {
            if (month != null && !YearMonth.from(expense.getDate()).equals(month)) {
                continue;
            }
            totalSpent = totalSpent.add(expense.getAmount());
            categoryTotals.merge(expense.getCategory(), expense.getAmount(), BigDecimal::add);
        }

        for (Map.Entry<String, BigDecimal> entry : categoryTotals.entrySet()) {
            String category = entry.getKey();
            BigDecimal total = entry.getValue();

            double percentage = total.doubleValue() / totalSpent.doubleValue() * 100;

            categorySummary.add(new CategorySummary(category, total, percentage));
        }
        categorySummary.sort(Comparator.comparing(CategorySummary::getTotal).reversed());
        return categorySummary;
    }

    //MONTHLY SUMMARY FEATURE
    public List<MonthlySummary> getMonthlySummary(User currentUser) {
        List<MonthlySummary> monthlySummaryList = new ArrayList<>();
        List<Expense> expenses = expenseRepository.findByUserId(currentUser.getId());

        Map<YearMonth, BigDecimal> monthlyTotal = new HashMap<>();

        for (Expense expense : expenses) {
            YearMonth month = YearMonth.from(expense.getDate());
            monthlyTotal.merge(month, expense.getAmount(), BigDecimal::add);
        }

        for (Map.Entry<YearMonth, BigDecimal> entry : monthlyTotal.entrySet()) {
            monthlySummaryList.add(new MonthlySummary(entry.getKey(), entry.getValue()));
        }
        // a HashMap has no order, so sort oldest month first
        monthlySummaryList.sort(Comparator.comparing(MonthlySummary::getMonth));
        return monthlySummaryList;
    }

    //BURN RATE WARNING FEATURE
    public BurnRateWarning getBurnRate(User currentUser) {
        List<Expense> expenses = expenseRepository.findByUserId(currentUser.getId());
        LocalDate today = LocalDate.now();
        int daysPassed = today.getDayOfMonth();
        int daysInMonth = today.lengthOfMonth();
        YearMonth currentMonth = YearMonth.from(today);
        Budget budget = budgetRepository.findByMonthAndUserId(currentMonth, currentUser.getId()).orElse(null);

        BigDecimal spentSoFar = BigDecimal.ZERO;
        List<Expense> thisMonthExpenses = new ArrayList<>();
        for (Expense expense : expenses) {
            if (expense.getDate().getMonth() == today.getMonth()
                    && expense.getDate().getYear() == today.getYear()) {
                spentSoFar = spentSoFar.add(expense.getAmount());
                thisMonthExpenses.add(expense);
            }
        }

        BigDecimal dailyAverage = spentSoFar.divide(BigDecimal.valueOf(daysPassed), 4, RoundingMode.HALF_UP);
        BigDecimal projectedTotal = dailyAverage.multiply(BigDecimal.valueOf(daysInMonth))
                .setScale(2, RoundingMode.HALF_UP);

        boolean budgetSet = budget != null;
        BigDecimal monthlyBudget = budgetSet ? budget.getAmount() : BigDecimal.ZERO;
        boolean isOverBudget = budgetSet && projectedTotal.compareTo(monthlyBudget) > 0;

        // Required daily average
        BigDecimal requiredDailyAverage = null;
        if (budgetSet) {
            int remainingDays = daysInMonth - daysPassed + 1;
            BigDecimal remainingBudget = monthlyBudget.subtract(spentSoFar);
            requiredDailyAverage = remainingBudget.divide(BigDecimal.valueOf(remainingDays), 2, RoundingMode.HALF_UP);
        }

        // the burn rate only mentions the first one
        List<UnusualExpense> unusualExpenses = findUnusualExpenses(expenses, thisMonthExpenses);
        boolean hasAnomaly = !unusualExpenses.isEmpty();
        String anomalyMessage = hasAnomaly ? unusualExpenses.get(0).getMessage() : null;

        return new BurnRateWarning(
                monthlyBudget,
                spentSoFar,
                dailyAverage,
                projectedTotal,
                isOverBudget,
                budgetSet,
                requiredDailyAverage,
                hasAnomaly,
                anomalyMessage
        );
    }
    //FORECAST FEATURE
    // this month's expenses over 3x the user's average
    public List<UnusualExpense> getUnusualExpenses(User currentUser) {
        List<Expense> expenses = expenseRepository.findByUserId(currentUser.getId());
        YearMonth currentMonth = YearMonth.now();
        List<Expense> thisMonthExpenses = expenses.stream()
                .filter(e -> YearMonth.from(e.getDate()).equals(currentMonth))
                .toList();
        return findUnusualExpenses(expenses, thisMonthExpenses);
    }

    private List<UnusualExpense> findUnusualExpenses(List<Expense> allExpenses, List<Expense> thisMonthExpenses) {
        List<UnusualExpense> unusual = new ArrayList<>();
        // too few expenses to know what is usual
        if (allExpenses.size() < 3) {
            return unusual;
        }
        BigDecimal totalAllTime = BigDecimal.ZERO;
        for (Expense expense : allExpenses) {
            totalAllTime = totalAllTime.add(expense.getAmount());
        }
        BigDecimal overallAverage = totalAllTime.divide(BigDecimal.valueOf(allExpenses.size()), 4, RoundingMode.HALF_UP);
        BigDecimal anomalyThreshold = overallAverage.multiply(BigDecimal.valueOf(3));

        for (Expense expense : thisMonthExpenses) {
            if (expense.getAmount().compareTo(anomalyThreshold) > 0) {
                String message = "Unusually large expense: Rs." + expense.getAmount() + " on " + expense.getCategory()
                        + " (your typical expense is around Rs." + overallAverage.setScale(2, RoundingMode.HALF_UP) + ")";
                unusual.add(new UnusualExpense(expense, message));
            }
        }
        return unusual;
    }

    // average of the last 3 full months (a month without spending counts as 0)
    public static final int FORECAST_MONTHS_NEEDED = 3;

    public ForecastResponse getForecast(User currentUser) {
        List<Expense> expenses = expenseRepository.findByUserId(currentUser.getId());
        YearMonth currentMonth = YearMonth.now();

        // full months since the first expense; the current month doesn't count
        int completedMonths = 0;
        Optional<YearMonth> firstMonth = expenses.stream()
                .map(e -> YearMonth.from(e.getDate()))
                .min(Comparator.naturalOrder());
        if (firstMonth.isPresent() && firstMonth.get().isBefore(currentMonth)) {
            completedMonths = (int) firstMonth.get().until(currentMonth, ChronoUnit.MONTHS);
        }

        List<CategoryForecast> forecasts = new ArrayList<>();
        if (completedMonths < FORECAST_MONTHS_NEEDED) {
            return new ForecastResponse(completedMonths, FORECAST_MONTHS_NEEDED, forecasts);
        }

        YearMonth firstCounted = currentMonth.minusMonths(FORECAST_MONTHS_NEEDED);
        Map<String, BigDecimal> categoryTotals = new HashMap<>();
        for (Expense expense : expenses) {
            YearMonth month = YearMonth.from(expense.getDate());
            if (!month.isBefore(firstCounted) && month.isBefore(currentMonth)) {
                categoryTotals.merge(expense.getCategory(), expense.getAmount(), BigDecimal::add);
            }
        }

        for (Map.Entry<String, BigDecimal> entry : categoryTotals.entrySet()) {
            BigDecimal average = entry.getValue()
                    .divide(BigDecimal.valueOf(FORECAST_MONTHS_NEEDED), 2, RoundingMode.HALF_UP);

            List<MonthForecast> months = new ArrayList<>();
            for (int i = 1; i <= 3; i++) {
                months.add(new MonthForecast(currentMonth.plusMonths(i), average));
            }
            forecasts.add(new CategoryForecast(entry.getKey(), months));
        }
        // biggest category first
        forecasts.sort(Comparator.comparing((CategoryForecast f) -> f.getMonths().get(0).getForecast()).reversed());

        return new ForecastResponse(completedMonths, FORECAST_MONTHS_NEEDED, forecasts);
    }


}