package com.zeeniya.expense_tracker.service;
// ./mvnw -Dtest=ExpenseServiceTest#getExpenseById_returnsExpense_ifFound test

import com.zeeniya.expense_tracker.dto.*;
import com.zeeniya.expense_tracker.entity.Budget;
import com.zeeniya.expense_tracker.entity.Expense;
import com.zeeniya.expense_tracker.entity.Role;
import com.zeeniya.expense_tracker.entity.User;
import com.zeeniya.expense_tracker.exception.ResourceNotFoundException;

import com.zeeniya.expense_tracker.repository.BudgetRepository;
import com.zeeniya.expense_tracker.repository.ExpenseRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpenseServiceTest {
    private Expense sampleExpense;
    private User currentUser;
    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private BudgetRepository budgetRepository;

    @InjectMocks
    private ExpenseService expenseService;

    @BeforeEach
    void setUp() {
        currentUser = new User("user@example.com", "user", "Test User", "password", Role.USER);
        currentUser.setId(1L);
        sampleExpense = new Expense("Food", new BigDecimal("500.00"), LocalDate.now(), "");
        sampleExpense.setId(1L);
        sampleExpense.setUser(currentUser);
    }

    //get
    @Test
    void getAllExpenses_returnsAllExpenses(){
        Expense expense = new Expense("Food", new BigDecimal("500.00"), LocalDate.now(), "");
        expense.setUser(currentUser);

        when(expenseRepository.findByUserId(currentUser.getId())).thenReturn(List.of(expense));

        List<Expense> result = expenseService.getAllExpenses(currentUser);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCategory()).isEqualTo("Food");
        assertThat(result.get(0).getAmount()).isEqualByComparingTo("500.00");
        assertThat(result.get(0).getUser()).isSameAs(currentUser);
        verify(expenseRepository).findByUserId(currentUser.getId());
    }

    private Expense expenseOn(long id, String category, String date) {
        Expense expense = new Expense(category, new BigDecimal("100.00"), LocalDate.parse(date), "");
        expense.setId(id);
        expense.setUser(currentUser);
        return expense;
    }

    @Test
    void getExpenses_withoutFilters_returnsAllNewestFirst() {
        when(expenseRepository.findByUserId(currentUser.getId())).thenReturn(List.of(
                expenseOn(1, "Food", "2026-10-05"),
                expenseOn(2, "Travel", "2026-11-10"),
                expenseOn(3, "Food", "2026-11-10")));

        List<Expense> result = expenseService.getExpenses(currentUser, null, null, null);

        assertThat(result).extracting(Expense::getId).containsExactly(3L, 2L, 1L);
    }

    @Test
    void getExpenses_filtersByCategoryIgnoringCase() {
        when(expenseRepository.findByUserId(currentUser.getId())).thenReturn(List.of(
                expenseOn(1, "Food", "2026-10-05"),
                expenseOn(2, "Travel", "2026-11-10")));

        List<Expense> result = expenseService.getExpenses(currentUser, "food", null, null);

        assertThat(result).extracting(Expense::getId).containsExactly(1L);
    }

    @Test
    void getExpenses_filtersByDateRange_includingBothEnds() {
        when(expenseRepository.findByUserId(currentUser.getId())).thenReturn(List.of(
                expenseOn(1, "Food", "2026-10-31"),
                expenseOn(2, "Food", "2026-11-01"),
                expenseOn(3, "Food", "2026-11-30"),
                expenseOn(4, "Food", "2026-12-01")));

        List<Expense> result = expenseService.getExpenses(currentUser, null,
                LocalDate.parse("2026-11-01"), LocalDate.parse("2026-11-30"));

        assertThat(result).extracting(Expense::getId).containsExactly(3L, 2L);
    }

    @Test
    void getExpenses_rejectsFromAfterTo() {
        assertThatThrownBy(() -> expenseService.getExpenses(currentUser, null,
                LocalDate.parse("2026-12-01"), LocalDate.parse("2026-11-01")))
                .isInstanceOf(IllegalArgumentException.class);
        verify(expenseRepository, never()).findByUserId(any());
    }

    @Test
    void getExpenseById_returnsExpense_ifFound(){
        Expense expense = new Expense("Food", new BigDecimal("500.00"), LocalDate.now(), "");
        expense.setUser(currentUser);

        when(expenseRepository.findByIdAndUserId(1L, currentUser.getId())).thenReturn(Optional.of(expense));

        Expense result = expenseService.getExpenseById(1L, currentUser);
        assertThat(result.getCategory()).isEqualTo("Food");
        assertThat(result.getAmount()).isEqualByComparingTo("500.00");
        assertThat(result.getUser()).isSameAs(currentUser);
    }

    @Test
    void getExpenseById_throwsException_ifNotFound(){
        when(expenseRepository.findByIdAndUserId(1L, currentUser.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> expenseService.getExpenseById(1L, currentUser)).isInstanceOf(ResourceNotFoundException.class);

    }

    @Test
    void getExpenseById_rejectsExpenseOwnedByAnotherUser() {
        User otherUser = new User("other@example.com", "other", "Other User", "password", Role.USER);
        otherUser.setId(2L);
        Expense otherUsersExpense = new Expense("Food", new BigDecimal("500.00"), LocalDate.now(), "");
        otherUsersExpense.setId(2L);
        otherUsersExpense.setUser(otherUser);
        when(expenseRepository.findByIdAndUserId(otherUsersExpense.getId(), currentUser.getId()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> expenseService.getExpenseById(otherUsersExpense.getId(), currentUser))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(expenseRepository).findByIdAndUserId(otherUsersExpense.getId(), currentUser.getId());
        verify(expenseRepository, never()).findById(otherUsersExpense.getId());
    }

    //add
    @Test
    void addExpense_savesExpense_whenDataIsValid(){
        ExpenseRequest request = new ExpenseRequest("Food", new BigDecimal("500.00"), LocalDate.now(), "");
        Expense expense = new Expense("Food", new BigDecimal("500.00"), LocalDate.now(), "");
        expense.setUser(currentUser);

        when(expenseRepository.save(any(Expense.class))).thenReturn(expense);

        expenseService.addExpense(request, currentUser);

        ArgumentCaptor<Expense> captor = ArgumentCaptor.forClass(Expense.class);
        verify(expenseRepository, times(1)).save(captor.capture());
        Expense actuallySaved = captor.getValue();

        assertThat(actuallySaved.getCategory()).isEqualTo("Food");
        assertThat(actuallySaved.getAmount()).isEqualByComparingTo("500.00");
        assertThat(actuallySaved.getUser()).isSameAs(currentUser);
    }

    @Test
    void addExpense_throwsException_whenDataIsNotValid(){
        ExpenseRequest request = new ExpenseRequest("Food", new BigDecimal("500.00"), LocalDate.now().plusDays(1), "");

        assertThatThrownBy(() -> expenseService.addExpense(request, currentUser)).isInstanceOf(IllegalArgumentException.class);

        verify(expenseRepository, never()).save(any(Expense.class));
    }

    //update
    @Test
    void updateExpense_updatesFields_whenValid() {
        ExpenseRequest request = new ExpenseRequest("Food", new BigDecimal("500.00"), LocalDate.now(), "");

        when(expenseRepository.findByIdAndUserId(1L, currentUser.getId())).thenReturn(Optional.of(sampleExpense));
        when(expenseRepository.save(any(Expense.class))).thenAnswer(inv -> inv.getArgument(0));

        Expense result = expenseService.updateExpense(1L, request, currentUser);

        assertThat(result.getCategory()).isEqualTo("Food");
        assertThat(result.getAmount()).isEqualByComparingTo("500.00");
        assertThat(result.getUser()).isSameAs(currentUser);
    }


    @Test
    void updateExpense_throwsIllegalArgumentException_whenDateIsInFuture() {
        ExpenseRequest request = new ExpenseRequest("Food", new BigDecimal("500.00"), LocalDate.now().plusDays(1), "");

        assertThatThrownBy(() -> expenseService.updateExpense(1L, request, currentUser)).isInstanceOf(IllegalArgumentException.class);

        verify(expenseRepository, never()).findByIdAndUserId(any(), any());
    }


    @Test
    void updateExpense_throwsResourceNotFoundException_whenExpenseNotFound() {
        ExpenseRequest request = new ExpenseRequest("Food", new BigDecimal("500.00"), LocalDate.now(), "");

        when(expenseRepository.findByIdAndUserId(99L, currentUser.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> expenseService.updateExpense(99L, request, currentUser)).isInstanceOf(ResourceNotFoundException.class);
    }

    //delete
    @Test
    void deleteExpense_deletesExpense_whenFound() {
        when(expenseRepository.findByIdAndUserId(1L, currentUser.getId())).thenReturn(Optional.of(sampleExpense));

        expenseService.deleteExpense(1L, currentUser);

        verify(expenseRepository, times(1)).delete(sampleExpense);
    }

    @Test
    void deleteExpense_throwsResourceNotFoundException_whenNotFound() {
        when(expenseRepository.findByIdAndUserId(99L, currentUser.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> expenseService.deleteExpense(99L, currentUser)).isInstanceOf(ResourceNotFoundException.class);

        verify(expenseRepository, never()).delete(any());
    }

    //getCategorySummary
    @Test
    void getCategorySummary_calculatesPercentagePerCategory(){
        Expense expense1 = new Expense("Food", new BigDecimal("300.00"), LocalDate.now(), "");
        Expense expense2 = new Expense("Food", new BigDecimal("200.00"), LocalDate.now(), "");
        Expense expense3 = new Expense("Transport", new BigDecimal("500.00"), LocalDate.now(), "");
        expense1.setUser(currentUser);
        expense2.setUser(currentUser);
        expense3.setUser(currentUser);
        when(expenseRepository.findByUserId(currentUser.getId())).thenReturn(List.of(expense1, expense2, expense3));

        List<CategorySummary> result = expenseService.getCategorySummary(currentUser);

        CategorySummary food = result.stream().filter(c -> c.getCategory().equals("Food")).findFirst().orElseThrow();
        assertThat(food.getTotal()).isEqualByComparingTo("500.00");
        assertThat(food.getPercentage()).isEqualTo(50.0);

    }

    @Test
    void getCategorySummary_returnsEmptyList_whenNoExpenseFound(){
        when(expenseRepository.findByUserId(currentUser.getId())).thenReturn(List.of());

        List<CategorySummary> result = expenseService.getCategorySummary(currentUser);

        assertThat(result).isEmpty();
    }

    @Test
    void getMonthlySummary_totalsExpensesForCurrentUserByMonth() {
        YearMonth currentMonth = YearMonth.now();
        Expense first = new Expense("Food", new BigDecimal("300.00"), currentMonth.atDay(1), "");
        Expense second = new Expense("Transport", new BigDecimal("200.00"), currentMonth.atDay(2), "");
        Expense previous = new Expense("Food", new BigDecimal("100.00"), currentMonth.minusMonths(1).atDay(1), "");
        first.setUser(currentUser);
        second.setUser(currentUser);
        previous.setUser(currentUser);
        when(expenseRepository.findByUserId(currentUser.getId())).thenReturn(List.of(first, second, previous));

        List<MonthlySummary> result = expenseService.getMonthlySummary(currentUser);

        assertThat(result).hasSize(2);
        assertThat(result).anySatisfy(summary -> {
            assertThat(summary.getMonth()).isEqualTo(currentMonth);
            assertThat(summary.getTotal()).isEqualByComparingTo("500.00");
        });
        assertThat(result).anySatisfy(summary -> {
            assertThat(summary.getMonth()).isEqualTo(currentMonth.minusMonths(1));
            assertThat(summary.getTotal()).isEqualByComparingTo("100.00");
        });
        verify(expenseRepository).findByUserId(currentUser.getId());
    }

    //getBurnRate
    @Test
    void getBurnRate_flagsOverBudget_whenProjectedSpendExceedsBudget() {
        YearMonth currentMonth = YearMonth.now();
        Budget lowBudget = new Budget(currentMonth, new BigDecimal("10.00"));
        Expense bigExpense = new Expense("Shopping", new BigDecimal("10000.00"), LocalDate.now(), "");
        lowBudget.setUser(currentUser);
        bigExpense.setUser(currentUser);

        when(expenseRepository.findByUserId(currentUser.getId())).thenReturn(List.of(bigExpense));
        when(budgetRepository.findByMonthAndUserId(currentMonth, currentUser.getId())).thenReturn(Optional.of(lowBudget));

        BurnRateWarning result = expenseService.getBurnRate(currentUser);

        assertThat(result.isBudgetSet()).isTrue();
        assertThat(result.isOverBudget()).isTrue();
    }

    @Test
    void getBurnRate_flagsBudgetNotSet_whenNoBudgetExistsForCurrentMonth() {
        YearMonth currentMonth = YearMonth.now();

        when(expenseRepository.findByUserId(currentUser.getId())).thenReturn(List.of(sampleExpense));
        when(budgetRepository.findByMonthAndUserId(currentMonth, currentUser.getId())).thenReturn(Optional.empty());
        BurnRateWarning result = expenseService.getBurnRate(currentUser);

        assertThat(result.isBudgetSet()).isFalse();
        assertThat(result.isOverBudget()).isFalse();
    }

    @Test
    void getForecast_excludesCurrentInProgressMonth() {
        YearMonth thisMonth = YearMonth.now();
        Expense m1 = new Expense("Food", new BigDecimal("300.00"), thisMonth.minusMonths(1).atDay(1), "");
        Expense m2 = new Expense("Food", new BigDecimal("300.00"), thisMonth.minusMonths(2).atDay(1), "");
        Expense m3 = new Expense("Food", new BigDecimal("300.00"), thisMonth.minusMonths(3).atDay(1), "");
        Expense current = new Expense("Food", new BigDecimal("999999.00"), LocalDate.now(), null);
        m1.setUser(currentUser);
        m2.setUser(currentUser);
        m3.setUser(currentUser);
        current.setUser(currentUser);

        when(expenseRepository.findByUserId(currentUser.getId())).thenReturn(List.of(m1, m2, m3, current));

        List<ForecastResponse> result = expenseService.getForecast(currentUser);
        ForecastResponse food = result.stream().filter(f -> f.getCategory().equals("Food")).findFirst().orElseThrow();
        //will exclude current month
        assertThat(food.getForecast()).isEqualByComparingTo("300.00");
    }

    @Test
    void getForecast_excludesCategoriesWithLessThan3Months() {
        YearMonth thisMonth = YearMonth.now();
        Expense onlyOne = new Expense("Rent", new BigDecimal("1000.00"), thisMonth.minusMonths(1).atDay(1), "");
        onlyOne.setUser(currentUser);

        when(expenseRepository.findByUserId(currentUser.getId())).thenReturn(List.of(onlyOne));

        List<ForecastResponse> result = expenseService.getForecast(currentUser);
        assertThat(result).isEmpty();
    }

    @Test
    void updateExpense_rejectsExpenseOwnedByAnotherUser() {
        ExpenseRequest request = new ExpenseRequest("Food", new BigDecimal("500.00"), LocalDate.now(), "");

        // id 2L belongs to someone else, so the lookup scoped to currentUser finds nothing
        when(expenseRepository.findByIdAndUserId(2L, currentUser.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> expenseService.updateExpense(2L, request, currentUser))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(expenseRepository).findByIdAndUserId(2L, currentUser.getId());
        verify(expenseRepository, never()).findById(any());
        verify(expenseRepository, never()).save(any(Expense.class));
    }

    @Test
    void deleteExpense_rejectsExpenseOwnedByAnotherUser() {
        when(expenseRepository.findByIdAndUserId(2L, currentUser.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> expenseService.deleteExpense(2L, currentUser))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(expenseRepository).findByIdAndUserId(2L, currentUser.getId());
        verify(expenseRepository, never()).findById(any());
        verify(expenseRepository, never()).delete(any());
    }

    @Test
    void getBurnRate_flagsAnomaly_whenOneExpenseIsFarAboveTheUsualSize() {
        YearMonth currentMonth = YearMonth.now();
        List<Expense> expenses = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            Expense small = new Expense("Food", new BigDecimal("100.00"), LocalDate.now().minusDays(i), "");
            small.setUser(currentUser);
            expenses.add(small);
        }
        Expense huge = new Expense("Shopping", new BigDecimal("10000.00"), LocalDate.now(), "");
        huge.setUser(currentUser);
        expenses.add(huge);

        when(expenseRepository.findByUserId(currentUser.getId())).thenReturn(expenses);
        when(budgetRepository.findByMonthAndUserId(currentMonth, currentUser.getId())).thenReturn(Optional.empty());

        BurnRateWarning result = expenseService.getBurnRate(currentUser);

        assertThat(result.isHasAnomaly()).isTrue();
        assertThat(result.getAnomalyMessage()).isNotBlank();
    }

    @Test
    void getBurnRate_noAnomaly_whenExpensesAreSimilarInSize() {
        YearMonth currentMonth = YearMonth.now();
        List<Expense> expenses = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Expense normal = new Expense("Food", new BigDecimal("100.00"), LocalDate.now().minusDays(i), "");
            normal.setUser(currentUser);
            expenses.add(normal);
        }

        when(expenseRepository.findByUserId(currentUser.getId())).thenReturn(expenses);
        when(budgetRepository.findByMonthAndUserId(currentMonth, currentUser.getId())).thenReturn(Optional.empty());

        BurnRateWarning result = expenseService.getBurnRate(currentUser);

        assertThat(result.isHasAnomaly()).isFalse();
    }

}
