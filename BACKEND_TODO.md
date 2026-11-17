# BACKEND_TODO

Everything the Figma design needs that the Spring Boot backend does not do yet.
Order of work: 1) Figma design, 2) this list, 3) frontend.

Status key: [ ] not started

## 0. Must fix first (app does not work without these)

- [x] `GlobalExceptionHandler.java:71` has a stray `23` after `}`. The project does not compile.
- [x] `SecurityConfig`: allows `/login.html` and `/signup.html` (they do not exist). Allow `/`, `/*.html`, `/css/**`, `/js/**`, fonts and images. Keep `/api/**` (except `/api/auth/**`) authenticated.
- [x] `Budget.month` is `unique = true` for the whole table. Now unique per `(user_id, budget_month)`.
  - [x] **You:** old one-column unique index on `budget` is gone (checked with `SHOW INDEX`, 2026-11-16). Only `PRIMARY` and `uk_budget_user_month` remain.
- [x] `BudgetAlert.month` had `@JoinColumn(name = "alert_month", unique = true)`. Replaced with `@Column(name = "alert_month")`.
  - [x] **DB:** `SHOW INDEX FROM budget_alert` (2026-11-16) shows only `PRIMARY` and the `user_id` foreign key index, so no global unique index. Not checked: whether an empty `month` column exists (run `SHOW COLUMNS FROM budget_alert`).
- [ ] `YearMonth` is stored as `varbinary` (a serialized Java object), not readable text like `2026-10`. Add a JPA converter to store `VARCHAR(7)`, and migrate the existing rows (section 4, budgets).
- [x] `DataIntegrityViolationException` handler now says "This already exists." and logs the cause. Signup gets its own email check in section 1.
- [x] `GeminiService` key: reads `gemini.api.key`, then `GEMINI_API_KEY`, then empty. Empty key or a 4xx from Gemini now gives 503, not a crash.
- [x] `ExpenseParseResponse` has a no-argument constructor.

## 1. Accounts

- [x] **Username on signup.** The signup card now has a Username field. Keep `username` required. Add a `message` to the `@Pattern` (4 to 20 characters: letters, numbers, `_` and `.`). Usernames are unique (case-insensitive): `uk_users_username` plus a check in `UserService` that gives 409 "This username is already taken."
- [x] **Password rule.** Design says: 8+ characters, one number, one special character. Backend only requires a letter and a number. Change the `UserSignUpRequest.password` pattern and add a message.
- [x] **Bug: `username` in responses is the email.** Fixed with `User.getDisplayUsername()`. `User.getUsername()` returns `email` (needed by Spring Security), and `UserService.toAuthResponse` uses it, so signup returns `"username":"test1@example.com"`. Add a separate getter for the real field and use it in `UserResponse`/`toAuthResponse` (and in `/api/users/me`).
- [x] **Missing page gives 500.** `GET /` with no `index.html` logs `NoResourceFoundException` and returns "Something went wrong." with 500. `GlobalExceptionHandler` should answer 404 for `NoResourceFoundException`.
- [x] **Signup returns a token.** `POST /api/auth/signup` and `/api/auth/login` both return `{token, user:{id, email, username, name}}` (`AuthResponse`).
- [x] `GET /api/users/me` returns `{id, email, username, name}`. Needed by Welcome and Account.
- [x] `PUT /api/users/me` edits name and username (Edit profile details).
- [x] `PUT /api/users/me/password` with `{currentPassword, newPassword}`. Change password now has a "Current password" field.
- [x] **Forgot password** (4 screens in Figma: enter email, check your email, new password, done). Built with a development email stub: the reset link is printed in the console (`LoggingEmailSender`), nothing is emailed. See section 7 for the real sender. The reset link opens `/reset-password.html?token=...` (base URL from `app.frontend-base-url`), so the frontend needs that page:
  - `POST /api/auth/forgot-password` `{email}`. Always answer the same, whether or not the email exists.
  - `POST /api/auth/reset-password` `{token, newPassword}`.
  - Needs a reset-token table, expiry (for example 30 minutes), one use only, and an email sender.
- [x] Delete account (Account screen has the button). `DELETE /api/users/me` with password confirmation. Remove that user's expenses, budgets and alerts.

## 2. Expenses

- [x] **Preview parse.** `POST /api/expenses/parse/preview` `{userInput}` returns `ExpenseParseResponse` and saves nothing. The Review screen then saves with `POST /api/expenses`. The old `/parse` is kept (it saves directly), and both use the same checks (`GeminiService.previewExpense`).
  - [ ] **You:** not yet checked against a real Gemini answer. On 2026-11-16 Google returned 503 "high demand" for every call, so only the unit tests (`GeminiServiceTest`) and the 503 path were verified. Try it once from the frontend or curl when Gemini is back.
- [x] **Gemini timeouts.** `RestClientConfig` had no timeouts, so a stalled call hung forever. Now 5 s to connect and 20 s to read, and any read failure gives 503 (`RestClientException` catch in `GeminiService.callGemini`).
- [x] **Categories.** Figma list: Food, Travel, Entertainment, Shopping, Medical, Utilities, Other. The Gemini prompt now uses this list (`ExpenseCategories.ALL`) and may not invent categories. Whatever the AI answers goes through `ExpenseCategories.toListed`: exact name (any case), then a keyword match ("Bills" to Utilities, "Groceries" to Food), else Other. Tested in `ExpenseCategoriesTest`. Old rows in the database are left as they are. Manual `POST /api/expenses` still accepts any category name.
- [x] **History filters.** `GET /api/expenses?category=&from=YYYY-MM-DD&to=YYYY-MM-DD`, all optional, still a plain list (decision: no paging, no text search), sorted newest first. Category ignores case, both dates are included, `from` after `to` gives 400. Tested in `ExpenseServiceTest` and by curl.
- [x] `GET /api/expenses/summary/category?month=YYYY-MM`, month optional (without it: all time, as before). Sorted biggest first, so the first item is the category that "leads".
- [x] Monthly summary sorted by month, oldest first.
- [x] **Forecast, 3 months.** `GET /api/expenses/forecast` returns `{completedMonths, monthsNeeded, forecasts:[{category, months:[{month, forecast}]}]}` for the next 3 months. Each category's forecast is the average of the last 3 full months, a month without spending counts as 0 (before, it used the last 3 months that had spending, however old). All 3 months show the same average, sorted biggest first.
- [x] "Not enough history": `forecasts` is empty and `completedMonths` (full months since the first expense, current month not counted) tells the screen how far along the user is.

## 3. Budgets

- [x] Return DTOs, not the `Budget` entity (it leaks the whole `user` object). All budget endpoints now return `BudgetResponse` `{month, amount}`.
- [x] **Budget history.** `GET /api/budgets/history` returns past months that had a budget, newest first, with `{month, budget, spent, difference}`. `difference` = budget - spent (negative means overspent), plus `overspent` (true/false) and a ready-to-show `message` ("You overspent by Rs.50.00" / "You stayed within budget, Rs.400.00 left over"). The current month is left out because it is not over.
- [x] Deleting a budget also deletes that month's alert (`BudgetAlertRepository.deleteByMonthAndUserId`). Checked with curl and MySQL.
- [x] **Decided: leave as is.** `budget.budget_month` and `budget_alert.alert_month` are stored as serialized Java objects (`varbinary`), not as text like `2026-11`. It does not affect the app or the UX; it only means the values cannot be read or filtered directly in MySQL. If that is ever needed: a JPA `AttributeConverter<YearMonth, String>` plus a one-off update of existing rows.

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

## 7. After the backend is done (before posting the project)

- [ ] **Real email sending (SMTP).** Today `LoggingEmailSender` prints reset emails in the console, so only someone who can read the server log can reset a password.
  - Add `spring-boot-starter-mail`, write `SmtpEmailSender implements EmailSender` using `JavaMailSender`, and choose it with `app.mail.sender=smtp`.
  - Put the SMTP host, username and password in `.env` (never committed), like `DB_PASSWORD`.
  - Pick a sender: Gmail app password is fine for a personal project (daily limit, may land in spam); Resend, Brevo or SES are better for real use.
  - Set `app.frontend-base-url` to the real site address.
  - Test with a real inbox: link works, arrives (not spam), expires after 30 minutes.
  - **If you decide not to do this:** add it to the README under a "Scope" section as a known limitation ("password reset emails are printed to the server log; no email provider is connected"). The README has no Scope section yet.
