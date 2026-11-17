package com.zeeniya.expense_tracker.ai;

import com.zeeniya.expense_tracker.dto.BurnRateWarning;
import com.zeeniya.expense_tracker.dto.CategorySummary;
import com.zeeniya.expense_tracker.dto.ForecastResponse;
import com.zeeniya.expense_tracker.dto.ai.AvoidItem;
import com.zeeniya.expense_tracker.dto.ai.ExpenseParseResponse;
import com.zeeniya.expense_tracker.dto.ai.InsightResponse;
import com.zeeniya.expense_tracker.entity.Role;
import com.zeeniya.expense_tracker.entity.User;
import com.zeeniya.expense_tracker.exception.MultipleExpensesDetectedException;
import com.zeeniya.expense_tracker.service.ExpenseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// The call to Gemini is replaced with a fixed answer, so these tests never use the network.
class GeminiServiceTest {

    private ExpenseService expenseService;
    private GeminiService geminiService;

    @BeforeEach
    void setUp() {
        expenseService = mock(ExpenseService.class);
        geminiService = spy(new GeminiService(mock(RestClient.class), new ObjectMapper(), expenseService));
    }

    @Test
    void previewReturnsTheParsedExpenseAndSavesNothing() {
        ExpenseParseResponse parsed = new ExpenseParseResponse("Food", 250, "2026-11-15", "lunch", false);
        doReturn(parsed).when(geminiService).parseExpense("lunch 250 yesterday");

        ExpenseParseResponse result = geminiService.previewExpense("lunch 250 yesterday");

        assertThat(result.getCategory()).isEqualTo("Food");
        assertThat(result.getAmount()).isEqualTo(250);
        verify(expenseService, never()).addExpense(any(), any());
    }

    @Test
    void previewRejectsInputWithMoreThanOneExpense() {
        doReturn(new ExpenseParseResponse("Food", 500, "2026-11-15", null, true))
                .when(geminiService).parseExpense("groceries 500 and bill 2000");

        assertThatThrownBy(() -> geminiService.previewExpense("groceries 500 and bill 2000"))
                .isInstanceOf(MultipleExpensesDetectedException.class);
    }

    @Test
    void previewRejectsInputWithoutAnAmount() {
        doReturn(new ExpenseParseResponse("Other", 0, "2026-11-15", "unclear", false))
                .when(geminiService).parseExpense("hello");

        assertThatThrownBy(() -> geminiService.previewExpense("hello"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Could not detect a valid amount from your input.");
    }

    @Test
    void insightsWithoutExpenses_explainWhatIsMissing_withoutCallingGemini() {
        User user = new User("user@example.com", "user", "Test User", "password", Role.USER);
        when(expenseService.getCategorySummary(user, null)).thenReturn(List.of());

        InsightResponse result = geminiService.getSpendingInsights(user);

        assertThat(result.getSummary()).startsWith("Add some expenses first");
        assertThat(result.getAvoid()).isEmpty();
    }

    @Test
    void insightsWhenAiIsUnavailable_throwSoTheApiAnswers503() {
        // In this test there is no API key, so the call to Gemini fails like an outage would.
        User user = new User("user@example.com", "user", "Test User", "password", Role.USER);
        when(expenseService.getBurnRate(user)).thenReturn(new BurnRateWarning(BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, false, false, null, false, null));
        when(expenseService.getCategorySummary(user, null))
                .thenReturn(List.of(new CategorySummary("Food", new BigDecimal("100"), 100.0)));
        when(expenseService.getForecast(user)).thenReturn(new ForecastResponse(0, 3, List.of()));

        assertThatThrownBy(() -> geminiService.getSpendingInsights(user))
                .isInstanceOf(AiUnavailableException.class);
    }

    @Test
    void severityIsCleanedUp_andGetsTheFigmaLabel() {
        AvoidItem item = new AvoidItem();

        item.setSeverity("Low");
        assertThat(item.getSeverity()).isEqualTo("low");
        assertThat(item.getPriorityLabel()).isEqualTo("Low priority");

        item.setSeverity("medium");
        assertThat(item.getPriorityLabel()).isEqualTo("Moderate priority");

        item.setSeverity("HIGH");
        assertThat(item.getPriorityLabel()).isEqualTo("High priority");

        item.setSeverity("urgent");
        assertThat(item.getSeverity()).isEqualTo("medium");
        assertThat(item.getPriorityLabel()).isEqualTo("Moderate priority");
    }
}
