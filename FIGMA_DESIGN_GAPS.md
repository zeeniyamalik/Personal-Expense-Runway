# FIGMA_DESIGN_GAPS

Screens, states and links still to design in the Expense Runway Figma file.
Existing frame ids are in brackets. "Clone" means copy that frame and edit it, so spacing and type stay identical.
Backend items for each are in BACKEND_TODO.md.

## A. Change existing frames

1. **Signup [21:65]**: add a Username field between Full Name and Email. Hint under it: "4 to 20 characters. Letters, numbers, _ and ." Taller card; check the blobs still sit behind it.
2. **Signup / Set password / Change password [35:120]**: show the rule as it is now written (8+ characters, one number, one special character) as a live checklist under the field.
3. **Change password [35:120]**: add "Current password" above "New Password". Rename the screen's entry link from "Forgot password?" to "Change password". Keep "Forgot password?" for the login screen only.
4. **Login [21:40]**: add an inline error line under the password ("Invalid email or password."), and a banner at the top for "Session ended, please log in again."
5. **Home [2:15]**: "1 new notice to review" must be its own tap target (now it sits inside Quick actions).
6. **Alerts [2:302]**: add a small dismiss (x) on each alert card. Show "Last checked" as a time.
7. **Manual expense (service unavailable) [2:82]**: make it the same as the normal Manual frame, with the "Service unavailable" note as a banner on top.
8. **Review expense [2:59]**: add a one-line note for when the AI picked a category that was mapped to another ("We filed this under Other."). Shown when the preview answer has `categoryChanged: true`; the text uses the category it was filed under.
9. **Edit your budget [35:283]**: add a back arrow and a Cancel link.
10. **Account [2:320]**: add Delete account (red text link, below Log out).

## B. New screens

**Forgot password flow** (opened from Login):
1. Forgot password: email field, "Send reset link", back.
2. Check your email: "If an account exists for this address, we sent a link." Button: Back to log in.
3. Set new password: new and confirm fields, checklist, "Save password".
4. Password updated: "Log in" button.
5. Link expired or already used: "Request a new link" button.

**Account**
6. Change password error states: wrong current password, passwords do not match, rule not met.
7. Edit profile error states: username taken, empty name, bad characters.
8. Delete account: confirm with password, warning that expenses and budgets are removed.
9. Account deleted: short confirmation, goes to Log in.

**Home**
10. Home with expenses but no budget: runway card replaced by "Set your monthly budget" card (clone the Welcome card), plus the spending list below.

**Expenses**
11. Add expense, "more than one expense detected": inline message "Please add them one at a time." under the sentence box.
12. Add expense, empty input error.
13. Expense history, empty (no expenses): one line + "Add expense" button.
14. Expense history, no matches for the search or filter: one line + "Clear filters" button.
15. Expense history, search active (keyboard open, results list). Search runs in the browser over the loaded list (note, category, date); the backend only filters by category and dates.
16. Month picker sheet (used by History and Analysis).
17. Category filter sheet (the existing dropdown, with "All categories" on top).
18. Expense not found (opened a deleted expense): "This expense no longer exists." + Back to history.
19. Generic "Couldn't load" section state with Retry (used inside any list or card).
20. Offline / server not reachable banner.

**Budgets**
21. Monthly budget, no budget set (empty state): one line + "Set budget".
22. Next month already has a budget: same screen as Set next month, prefilled, button "Save changes".
23. Budget history, empty: "No past budgets yet."
24. Budget amount errors: empty, zero, too large ("Amount can't be more than Rs. 10,000,000 (1 crore)."). The same limit applies to a single expense.

**Analysis and insights**
25. Spending analysis, no spending (empty): one line + "Add expense".
26. Spending analysis, a month with no spending.
27. Forecast, not enough history: "Forecast needs 3 full months of history." + Back to analysis.
28. Your insights with no tips: show only the summary.

**Alerts**
29. Alert dismissed (card collapses). No Undo (decided 2026-11-18).
30. Alerts, over-budget card (the existing frame only shows the unusual-expense card). Button: Review budget.

**System**
31. Loading skeleton (one style, used on every screen that fetches).
32. Session expired (shown on Login, see A4).

## C. Prototype links to add

| From | To |
|---|---|
| HOME: Home tab, and Home tab on every frame that lacks it | HOME |
| Add expense (2) overlay: close | Add expense |
| Expense not saved: Try saving again | Expense saved (retry) |
| Invalid amount: Edit amount | Add expense |
| Watch your pace: Review budget | Monthly budget |
| Budget exceeded: Review spending | Spending analysis |
| Your insights: Refresh insights | Finding insights |
| Finding insights | Your insights, or Insights unavailable |
| Alerts: View expense | Expense details of that expense (alerts carry `expenseId`) |
| Alerts: dismiss | Alert dismissed |
| Login: Forgot password? | Forgot password |
| Expense history: every row | Expense details |
| Expense history: search / month / category | search, month picker, category sheet |
| Spending analysis: month | Month picker sheet |
| Edit your budget: Cancel / back | Monthly budget |
| Next month budget: back | where the user came from |
| Budget history rows | no link (no detail screen) |
| Edit expense (dropdown variant): Cancel | Expense details |
| Account: Delete account | Delete account |
| Log out confirm: No / close | Account |

## D. Decisions already made

- Signup goes straight to Welcome. "Set account" and "Set password" frames stay unused.
- AI category outside the Figma list: map it to the closest listed category, else "Other" (done on the server).
- Forecast stays a 3-month table.
- Search expenses: the search box filters the loaded list in the browser (note, category, date). The backend filters only by category and date range (decided 2026-11-17/18).
- Alerts: dismiss has no Undo.
- Amounts: 1 crore (Rs. 10,000,000) maximum for budgets and single expenses.
- Home states: No expenses yet, Watch your pace, Budget exceeded, and the new no-budget state.

## E. Built in code so far

The Figma work (sections A to C) is still to do; this tracks what the app already has.

| Item | Status |
|---|---|
| A1 Username field with hint (signup) | built (auth.html) |
| A2 Password checklist | built for signup and set new password; change password comes with Account |
| A4 Login error line and "Session ended" banner | built |
| B1 to B5 Forgot password flow | built, tested with a real email |
| B32 Session expired | built |
| Everything else | backend ready, screens not built yet |
