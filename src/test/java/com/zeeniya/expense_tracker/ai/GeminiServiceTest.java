package com.zeeniya.expense_tracker.ai;

import com.zeeniya.expense_tracker.dto.ai.ExpenseParseResponse;
import com.zeeniya.expense_tracker.exception.MultipleExpensesDetectedException;
import com.zeeniya.expense_tracker.service.ExpenseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

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
}
