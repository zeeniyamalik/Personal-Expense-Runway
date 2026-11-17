package com.zeeniya.expense_tracker.controller;

import com.zeeniya.expense_tracker.dto.ExpenseRequest;
import com.zeeniya.expense_tracker.dto.*;
import com.zeeniya.expense_tracker.entity.Expense;
import com.zeeniya.expense_tracker.entity.User;
import com.zeeniya.expense_tracker.service.ExpenseService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.time.LocalDate;
import java.util.List;


@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

   private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    // Optional filters, for example /api/expenses?category=Food&from=2026-11-01&to=2026-11-30
    @GetMapping
    public List<ExpenseResponse> getAllExpenses(@RequestParam(required = false) String category,
                                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                                                @AuthenticationPrincipal User currentUser){
        return expenseService.getExpenses(currentUser, category, from, to)
                .stream()
                .map(ExpenseResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public ExpenseResponse getExpenseById(@PathVariable Long id, @AuthenticationPrincipal User currentUser){
        return ExpenseResponse.from(expenseService.getExpenseById(id, currentUser));
    }

    @PostMapping
    public ExpenseResponse addExpense(@Valid @RequestBody ExpenseRequest request, @AuthenticationPrincipal User currentUser){
        return ExpenseResponse.from(expenseService.addExpense(request, currentUser));
    }


    @PutMapping("/{id}")
    public ExpenseResponse updateExpense(@PathVariable Long id, @Valid @RequestBody ExpenseRequest request, @AuthenticationPrincipal User currentUser) {
        return ExpenseResponse.from(expenseService.updateExpense(id, request, currentUser));
    }

    @DeleteMapping("/{id}")
    public void deleteExpense(@PathVariable Long id,  @AuthenticationPrincipal User currentUser) {
         expenseService.deleteExpense(id, currentUser);
    }


    @GetMapping("/summary/category")
    public List<CategorySummary> getCategorySummary(@AuthenticationPrincipal User currentUser) {
            return expenseService.getCategorySummary(currentUser);
        }

    @GetMapping("/summary/monthly")
    public List<MonthlySummary> getMonthlySummary( @AuthenticationPrincipal User currentUser) {
        return expenseService.getMonthlySummary(currentUser);
    }

    @GetMapping("/burn-rate")
    public BurnRateWarning getBurnRate( @AuthenticationPrincipal User currentUser) {
        return expenseService.getBurnRate(currentUser);
    }

    @GetMapping("/forecast")
    public List<ForecastResponse> getForecast( @AuthenticationPrincipal User currentUser) {
        return expenseService.getForecast(currentUser);
    }

}

