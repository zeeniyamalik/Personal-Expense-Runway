# BACKEND_TODO

Everything the Figma design needs that the Spring Boot backend does not do yet.
Order of work: 1) Figma design, 2) this list, 3) frontend.

Status key: [ ] not started

## 0. Must fix first (app does not work without these)

- [x] `GlobalExceptionHandler.java:71` has a stray `23` after `}`. The project does not compile.
- [x] `SecurityConfig`: allows `/login.html` and `/signup.html` (they do not exist). Allow `/`, `/*.html`, `/css/**`, `/js/**`, fonts and images. Keep `/api/**` (except `/api/auth/**`) authenticated.
- [x] `Budget.month` is `unique = true` for the whole table. Now unique per `(user_id, budget_month)`.
  - [ ] **You:** drop the old one-column unique index in MySQL (see section 0 report). `ddl-auto=update` does not drop it.
- [x] `BudgetAlert.month` had `@JoinColumn(name = "alert_month", unique = true)`. Replaced with `@Column(name = "alert_month")`.
  - [ ] **DB:** drop the global unique index on `budget_alert.alert_month`, and the empty `month` column the app added by mistake.
- [ ] `YearMonth` is stored as `varbinary` (a serialized Java object), not readable text like `2026-10`. Add a JPA converter to store `VARCHAR(7)`, and migrate the existing rows (section 4, budgets).
- [x] `DataIntegrityViolationException` handler now says "This already exists." and logs the cause. Signup gets its own email check in section 1.
- [x] `GeminiService` key: reads `gemini.api.key`, then `GEMINI_API_KEY`, then empty. Empty key or a 4xx from Gemini now gives 503, not a crash.
- [x] `ExpenseParseResponse` has a no-argument constructor.

## 1. Accounts

- [ ] **Username on signup.** The signup card now has a Username field. Keep `username` required. Add a `message` to the `@Pattern` (4 to 20 characters: letters, numbers, `_` and `.`). Decide if usernames must be unique (the 409 text says they are, the column is not unique).
- [ ] **Password rule.** Design says: 8+ characters, one number, one special character. Backend only requires a letter and a number. Change the `UserSignUpRequest.password` pattern and add a message.
- [ ] **Signup returns a token** (or the frontend logs in right after). Pick one.
- [ ] `GET /api/users/me` returns `{id, email, username, name}`. Needed by Welcome and Account.
- [ ] `PUT /api/users/me` edits name and username (Edit profile details).
- [ ] `PUT /api/users/me/password` with `{currentPassword, newPassword}`. Change password now has a "Current password" field.
- [ ] **Forgot password** (4 screens in Figma: enter email, check your email, new password, done):
  - `POST /api/auth/forgot-password` `{email}`. Always answer the same, whether or not the email exists.
  - `POST /api/auth/reset-password` `{token, newPassword}`.
  - Needs a reset-token table, expiry (for example 30 minutes), one use only, and an email sender.
- [ ] Delete account (Account screen has the button). `DELETE /api/users/me` with password confirmation. Remove that user's expenses, budgets and alerts.

## 2. Expenses

- [ ] **Preview parse.** `POST /api/expenses/parse/preview` `{userInput}` returns `ExpenseParseResponse` and saves nothing. The Review screen then saves with `POST /api/expenses`. Keep the old `/parse` or remove it.
- [ ] **Categories.** Figma list: Food, Travel, Entertainment, Shopping, Medical, Utilities, Other. Change the Gemini prompt list to match. Decision: if the AI returns a name outside the list, map it to the closest listed category, and use Other if nothing fits. Do this on the server so the AI cannot create new categories.
- [ ] **History search and filters.** `GET /api/expenses?search=&category=&month=YYYY-MM&from=&to=` with sorting (newest first) and paging (`page`, `size`). Search matches note, category and date.
- [ ] `GET /api/expenses/summary/category?month=YYYY-MM` (the Analysis month picker and "Food leads" on Home need a month).
- [ ] Monthly summary sorted by month.
- [ ] **Forecast, 3 months.** `GET /api/expenses/forecast` returns the next 3 months per category: `[{category, months:[{month, forecast}]}]`. Today it returns one number per category.
- [ ] "Not enough history" must be a clear response (empty list is fine), with the number of completed months so far.

## 3. Budgets

- [ ] Return DTOs, not the `Budget` entity (it leaks the whole `user` object).
- [ ] **Budget history.** `GET /api/budgets/history` returns past months with `{month, budget, spent, difference}`. Today the frontend would have to join two endpoints.
- [ ] Deleting a budget must also remove or hide that month's alert.

## 4. Alerts

- [ ] Return a DTO, not the `BudgetAlert` entity.
- [ ] Add `type` (`OVER_BUDGET` or `UNUSUAL_EXPENSE`), `expenseId` (for "View expense"), `createdAt`.
- [ ] Add "last checked" time (shown on the Alerts screen).
- [ ] Run the check when an expense or budget changes, not only at 8am.
- [ ] Allow more than one alert per month, and do not let a dismissed alert block a new one.
- [ ] Dismiss exists (`PUT /api/alerts/{id}/dismiss`). The design now has a dismiss control. Keep it.

## 5. AI insights

- [ ] `GET /api/ai/insights` must return 503 when Gemini is down. Today it returns 200 with an error sentence in `summary`.
- [ ] `severity` is low/medium/high. Design shows "Low priority" and "Moderate priority". Map medium to Moderate or send a label.

## 6. Smaller things

- [ ] Plain-text errors vs JSON field errors. Consider one JSON error shape `{message, fieldErrors}` for every error.
- [ ] Gemini model name `gemini-3.8-flash` in `GeminiService`: check it exists.
- [ ] Missing `@Valid` or length limit on `ExpenseParseRequest.userInput` (empty or very long input).
- [ ] `ExpenseRequest.note` has no length limit.
