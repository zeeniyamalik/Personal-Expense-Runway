package com.zeeniya.expense_tracker.ai;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// The fixed category list from the Figma design. The AI is asked to use it, and whatever it
// answers goes through toListed(), so a new category can never reach the database from the AI.
public final class ExpenseCategories {

    public static final String OTHER = "Other";

    public static final List<String> ALL = List.of(
            "Food", "Travel", "Entertainment", "Shopping", "Medical", "Utilities", OTHER
    );

    // Words the AI might use instead of a listed name. Checked in order with "contains",
    // so "electricity bill" and "Bills" both end up in Utilities.
    private static final Map<String, String> KEYWORDS = new LinkedHashMap<>();

    static {
        KEYWORDS.put("grocer", "Food");
        KEYWORDS.put("restaurant", "Food");
        KEYWORDS.put("dining", "Food");
        KEYWORDS.put("meal", "Food");
        KEYWORDS.put("drink", "Food");

        KEYWORDS.put("transport", "Travel");
        KEYWORDS.put("taxi", "Travel");
        KEYWORDS.put("fuel", "Travel");
        KEYWORDS.put("petrol", "Travel");
        KEYWORDS.put("flight", "Travel");
        KEYWORDS.put("hotel", "Travel");

        KEYWORDS.put("movie", "Entertainment");
        KEYWORDS.put("game", "Entertainment");
        KEYWORDS.put("subscription", "Entertainment");
        KEYWORDS.put("streaming", "Entertainment");

        KEYWORDS.put("cloth", "Shopping");
        KEYWORDS.put("electronic", "Shopping");

        KEYWORDS.put("health", "Medical");
        KEYWORDS.put("doctor", "Medical");
        KEYWORDS.put("pharmac", "Medical");
        KEYWORDS.put("medicine", "Medical");
        KEYWORDS.put("hospital", "Medical");

        KEYWORDS.put("bill", "Utilities");
        KEYWORDS.put("electric", "Utilities");
        KEYWORDS.put("water", "Utilities");
        KEYWORDS.put("internet", "Utilities");
        KEYWORDS.put("phone", "Utilities");
        KEYWORDS.put("rent", "Utilities");
    }

    private ExpenseCategories() {
    }

    // Returns the listed name for any text: an exact match (ignoring case), then a keyword
    // match, and Other when nothing fits.
    public static String toListed(String category) {
        if (category == null || category.isBlank()) {
            return OTHER;
        }
        String text = category.trim().toLowerCase();

        for (String listed : ALL) {
            if (listed.equalsIgnoreCase(text)) {
                return listed;
            }
        }
        for (Map.Entry<String, String> keyword : KEYWORDS.entrySet()) {
            if (text.contains(keyword.getKey())) {
                return keyword.getValue();
            }
        }
        return OTHER;
    }
}
