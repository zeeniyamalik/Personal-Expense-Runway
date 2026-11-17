package com.zeeniya.expense_tracker.service;

import com.zeeniya.expense_tracker.dto.BurnRateWarning;
import com.zeeniya.expense_tracker.dto.UnusualExpense;
import com.zeeniya.expense_tracker.entity.AlertType;
import com.zeeniya.expense_tracker.entity.BudgetAlert;
import com.zeeniya.expense_tracker.entity.Expense;
import com.zeeniya.expense_tracker.entity.Role;
import com.zeeniya.expense_tracker.entity.User;
import com.zeeniya.expense_tracker.exception.ResourceNotFoundException;
import com.zeeniya.expense_tracker.repository.BudgetAlertRepository;
import com.zeeniya.expense_tracker.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BudgetAlertServiceTest {

    @Mock
    private BudgetAlertRepository budgetAlertRepository;

    @Mock
    private ExpenseService expenseService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BudgetAlertService budgetAlertService;

    private User currentUser;
    private YearMonth currentMonth;

    @BeforeEach
    void setUp() {
        currentUser = new User("user@example.com", "user", "Test User", "password", Role.USER);
        currentUser.setId(1L);
        currentMonth = YearMonth.now();
    }

    private BurnRateWarning burnRate(boolean overBudget) {
        return new BurnRateWarning(
                new BigDecimal("5000.00"),
                new BigDecimal("3000.00"),
                new BigDecimal("200.00"),
                new BigDecimal("6000.00"),
                overBudget,
                true,
                new BigDecimal("166.66"),
                false,
                null
        );
    }

    private UnusualExpense unusual(long expenseId) {
        Expense expense = new Expense("Shopping", new BigDecimal("9000.00"), LocalDate.now(), "");
        expense.setId(expenseId);
        return new UnusualExpense(expense, "Unusually large expense: Rs.9000.00 on Shopping");
    }

    private BudgetAlert alert(AlertType type, Long expenseId) {
        BudgetAlert alert = new BudgetAlert(currentMonth, type, "old alert", expenseId);
        alert.setUser(currentUser);
        return alert;
    }

    private List<BudgetAlert> savedAlerts(int times) {
        ArgumentCaptor<BudgetAlert> captor = ArgumentCaptor.forClass(BudgetAlert.class);
        verify(budgetAlertRepository, times(times)).save(captor.capture());
        return captor.getAllValues();
    }

    @Test
    void check_createsOverBudgetAlert_whenOverBudget() {
        when(expenseService.getBurnRate(currentUser)).thenReturn(burnRate(true));

        budgetAlertService.checkBurnRateAndCreateAlert(currentUser);

        BudgetAlert saved = savedAlerts(1).get(0);
        assertThat(saved.getType()).isEqualTo(AlertType.OVER_BUDGET);
        assertThat(saved.getMonth()).isEqualTo(currentMonth);
        assertThat(saved.getMessage()).contains("6000.00");
        assertThat(saved.getExpenseId()).isNull();
        assertThat(saved.getUser()).isSameAs(currentUser);
    }

    @Test
    void check_createsNoOverBudgetAlert_whenOneAlreadyExistsThisMonth_evenIfDismissed() {
        when(expenseService.getBurnRate(currentUser)).thenReturn(burnRate(true));
        when(budgetAlertRepository.existsByMonthAndTypeAndUserId(currentMonth, AlertType.OVER_BUDGET, 1L)).thenReturn(true);

        budgetAlertService.checkBurnRateAndCreateAlert(currentUser);

        verify(budgetAlertRepository, never()).save(any(BudgetAlert.class));
    }

    @Test
    void check_removesActiveOverBudgetAlert_whenBackUnderBudget() {
        BudgetAlert overBudget = alert(AlertType.OVER_BUDGET, null);
        when(budgetAlertRepository.findByMonthAndDismissedFalseAndUserId(currentMonth, 1L)).thenReturn(List.of(overBudget));
        when(expenseService.getBurnRate(currentUser)).thenReturn(burnRate(false));

        budgetAlertService.checkBurnRateAndCreateAlert(currentUser);

        verify(budgetAlertRepository).delete(overBudget);
        verify(budgetAlertRepository, never()).save(any(BudgetAlert.class));
    }

    @Test
    void check_createsOneAlertPerUnusualExpense_withExpenseId() {
        when(expenseService.getBurnRate(currentUser)).thenReturn(burnRate(false));
        when(expenseService.getUnusualExpenses(currentUser)).thenReturn(List.of(unusual(7L), unusual(8L)));

        budgetAlertService.checkBurnRateAndCreateAlert(currentUser);

        List<BudgetAlert> saved = savedAlerts(2);
        assertThat(saved).allSatisfy(a -> assertThat(a.getType()).isEqualTo(AlertType.UNUSUAL_EXPENSE));
        assertThat(saved).extracting(BudgetAlert::getExpenseId).containsExactly(7L, 8L);
    }

    @Test
    void check_dismissedOverBudgetAlert_doesNotBlockUnusualExpenseAlert() {
        when(expenseService.getBurnRate(currentUser)).thenReturn(burnRate(true));
        when(budgetAlertRepository.existsByMonthAndTypeAndUserId(currentMonth, AlertType.OVER_BUDGET, 1L)).thenReturn(true);
        when(expenseService.getUnusualExpenses(currentUser)).thenReturn(List.of(unusual(7L)));

        budgetAlertService.checkBurnRateAndCreateAlert(currentUser);

        assertThat(savedAlerts(1).get(0).getType()).isEqualTo(AlertType.UNUSUAL_EXPENSE);
    }

    @Test
    void check_createsNoUnusualAlert_whenThatExpenseAlreadyHasOne() {
        when(expenseService.getBurnRate(currentUser)).thenReturn(burnRate(false));
        when(expenseService.getUnusualExpenses(currentUser)).thenReturn(List.of(unusual(7L)));
        when(budgetAlertRepository.existsByTypeAndExpenseIdAndUserId(AlertType.UNUSUAL_EXPENSE, 7L, 1L)).thenReturn(true);

        budgetAlertService.checkBurnRateAndCreateAlert(currentUser);

        verify(budgetAlertRepository, never()).save(any(BudgetAlert.class));
    }

    @Test
    void check_removesActiveUnusualAlert_whenExpenseIsNoLongerUnusual() {
        BudgetAlert stillUnusual = alert(AlertType.UNUSUAL_EXPENSE, 7L);
        BudgetAlert gone = alert(AlertType.UNUSUAL_EXPENSE, 8L);
        when(budgetAlertRepository.findByMonthAndDismissedFalseAndUserId(currentMonth, 1L)).thenReturn(List.of(stillUnusual, gone));
        when(budgetAlertRepository.existsByTypeAndExpenseIdAndUserId(AlertType.UNUSUAL_EXPENSE, 7L, 1L)).thenReturn(true);
        when(expenseService.getBurnRate(currentUser)).thenReturn(burnRate(false));
        when(expenseService.getUnusualExpenses(currentUser)).thenReturn(List.of(unusual(7L)));

        budgetAlertService.checkBurnRateAndCreateAlert(currentUser);

        verify(budgetAlertRepository).delete(gone);
        verify(budgetAlertRepository, never()).delete(stillUnusual);
    }

    @Test
    void check_recordsLastCheckedTime() {
        when(expenseService.getBurnRate(currentUser)).thenReturn(burnRate(false));

        budgetAlertService.checkBurnRateAndCreateAlert(currentUser);

        verify(userRepository).updateAlertsCheckedAt(eq(1L), any());
    }

    @Test
    void getActiveAlerts_returnsCurrentMonthAlertsForUser() {
        BudgetAlert alert = alert(AlertType.OVER_BUDGET, null);
        when(budgetAlertRepository.findByMonthAndDismissedFalseAndUserId(currentMonth, currentUser.getId()))
                .thenReturn(List.of(alert));

        List<BudgetAlert> result = budgetAlertService.getActiveAlerts(currentUser);

        assertThat(result).containsExactly(alert);
    }

    @Test
    void dismissAlert_marksAlertAsDismissed_whenFoundForUser() {
        BudgetAlert alert = alert(AlertType.OVER_BUDGET, null);
        when(budgetAlertRepository.findByIdAndUserId(1L, currentUser.getId()))
                .thenReturn(Optional.of(alert));

        budgetAlertService.dismissAlert(1L, currentUser);

        assertThat(alert.isDismissed()).isTrue();
        verify(budgetAlertRepository).save(alert);
    }

    @Test
    void dismissAlert_throwsException_whenNotFoundForUser() {
        when(budgetAlertRepository.findByIdAndUserId(99L, currentUser.getId()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> budgetAlertService.dismissAlert(99L, currentUser))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(budgetAlertRepository, never()).save(any(BudgetAlert.class));
    }

    @Test
    void checkBurnRateForAllUsers_checksEachUser() {
        User otherUser = new User("other@example.com", "other", "Other User", "password", Role.USER);
        otherUser.setId(2L);
        when(userRepository.findAll()).thenReturn(List.of(currentUser, otherUser));
        when(expenseService.getBurnRate(currentUser)).thenReturn(burnRate(false));
        when(expenseService.getBurnRate(otherUser)).thenReturn(burnRate(false));

        budgetAlertService.checkBurnRateAndCreateAlertsForAllUsers();

        verify(expenseService).getBurnRate(currentUser);
        verify(expenseService).getBurnRate(otherUser);
    }
}
