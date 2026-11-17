package com.zeeniya.expense_tracker.ai;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ExpenseCategoriesTest {

    @Test
    void keepsListedNamesAndFixesTheirCase() {
        assertThat(ExpenseCategories.toListed("Food")).isEqualTo("Food");
        assertThat(ExpenseCategories.toListed("medical")).isEqualTo("Medical");
        assertThat(ExpenseCategories.toListed("  UTILITIES ")).isEqualTo("Utilities");
    }

    @Test
    void mapsOldAndSimilarNamesToTheClosestListedOne() {
        assertThat(ExpenseCategories.toListed("Bills")).isEqualTo("Utilities");
        assertThat(ExpenseCategories.toListed("Electricity Bill")).isEqualTo("Utilities");
        assertThat(ExpenseCategories.toListed("Groceries")).isEqualTo("Food");
        assertThat(ExpenseCategories.toListed("Healthcare")).isEqualTo("Medical");
        assertThat(ExpenseCategories.toListed("Transportation")).isEqualTo("Travel");
        assertThat(ExpenseCategories.toListed("Clothing")).isEqualTo("Shopping");
        assertThat(ExpenseCategories.toListed("Movies")).isEqualTo("Entertainment");
    }

    @Test
    void usesOtherWhenNothingFits() {
        assertThat(ExpenseCategories.toListed("Extra")).isEqualTo("Other");
        assertThat(ExpenseCategories.toListed("Education")).isEqualTo("Other");
        assertThat(ExpenseCategories.toListed("")).isEqualTo("Other");
        assertThat(ExpenseCategories.toListed(null)).isEqualTo("Other");
    }

    @Test
    void isChanged_onlyWhenADifferentCategoryWasPicked() {
        assertThat(ExpenseCategories.isChanged("Food")).isFalse();
        assertThat(ExpenseCategories.isChanged("food")).isFalse();
        assertThat(ExpenseCategories.isChanged("Bills")).isTrue();
        assertThat(ExpenseCategories.isChanged("Education")).isTrue();
        assertThat(ExpenseCategories.isChanged(null)).isTrue();
    }
}
