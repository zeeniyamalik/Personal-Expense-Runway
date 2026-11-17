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
- [x] **Forgot password** (4 screens in Figma: enter email, check your email, new password, done). Built with a development email stub: the reset link is printed in the console (`LoggingEmailSender`), nothing is emailed. See section 7 for the real sender. The reset link opens `/auth.html?reset=...` (base URL from `app.frontend-base-url`); the auth page handles it (changed 2026-11-17 with the frontend):
  - `POST /api/auth/forgot-password` `{email}`. Always answer the same, whether or not the email exists.
  - `POST /api/auth/reset-password` `{token, newPassword}`.
  - Needs a reset-token table, expiry (for example 30 minutes), one use only, and an email sender.
- [x] Delete account (Account screen has the button). `DELETE /api/users/me` with password confirmation. Remove that user's expenses, budgets and alerts.

## 2. Expenses

- [x] **Preview parse.** `POST /api/expenses/parse/preview` `{userInput}` returns `ExpenseParseResponse` and saves nothing. The Review screen then saves with `POST /api/expenses`. The old `/parse` is kept (it saves directly), and both use the same checks (`GeminiService.previewExpense`).
  - [ ] **You:** not yet checked against a real Gemini answer. On 2026-11-16 Google returned 503 "high demand" for every call, so only the unit tests (`GeminiServiceTest`) and the 503 path were verified. Try it once from the frontend or curl when Gemini is back. Still overloaded on 2026-11-17 (503 and 60 s stalls), so the new category list is not checked against a real answer either.
- [x] **Gemini timeouts.** `RestClientConfig` had no timeouts, so a stalled call hung forever. Now 5 s to connect and 20 s to read, and any read failure gives 503 (`RestClientException` catch in `GeminiService.callGemini`).
- [x] **Categories.** Figma list: Food, Travel, Entertainment, Shopping, Medical, Utilities, Other. The Gemini prompt now uses this list (`ExpenseCategories.ALL`) and may not invent categories. Whatever the AI answers goes through `ExpenseCategories.toListed`: exact name (any case), then a keyword match ("Bills" to Utilities, "Groceries" to Food), else Other. Tested in `ExpenseCategoriesTest`. Old rows in the database are left as they are. Manual `POST /api/expenses` still accepts any category name.
- [x] **History filters.** `GET /api/expenses?category=&from=YYYY-MM-DD&to=YYYY-MM-DD`, all optional, still a plain list (decision: no paging, no text search), sorted newest first. Category ignores case, both dates are included, `from` after `to` gives 400. Tested in `ExpenseServiceTest` and by curl.
- [x] `GET /api/expenses/summary/category?month=YYYY-MM`, month optional (without it: all time, as before). Sorted biggest first, so the first item is the category that "leads".
- [x] Monthly summary sorted by month, oldest first.
- [x] **Forecast, 3 months.** `GET /api/expenses/forecast` returns `{completedMonths, monthsNeeded, forecasts:[{category, months:[{month, forecast}]}]}` for the next 3 months. Each category's forecast is the average of the last 3 full months, a month without spending counts as 0 (before, it used the last 3 months that had spending, however old). All 3 months show the same average, sorted biggest first.
- [x] "Not enough history": `forecasts` is empty and `completedMonths` (full months since the first expense, current month not counted) tells the screen how far along the user is.

## 3. Budgets

- [x] Return DTOs, not the `Budget` entity (it leaks the whole `user` object). All budget endpoints now return `BudgetResponse` `{month, amount}`.
- [x] **Budget history.** `GET /api/budgets/history` returns past months that had a budget, newest first, with `{month, budget, spent, difference}`. `difference` = budget - spent (negative means overspent), plus `overspent` (true/false) and a ready-to-show `message` in the Figma wording ("Over budget · Rs. 1,500 extra" / "Within budget · Rs. 2,000 left"). The Budget history frame (2:208) already shows this, so no design gap. The current month is left out because it is not over.
- [x] Deleting a budget also deletes that month's alert (`BudgetAlertRepository.deleteByMonthAndUserId`). Checked with curl and MySQL.
- [x] **Decided: leave as is.** `budget.budget_month` and `budget_alert.alert_month` are stored as serialized Java objects (`varbinary`), not as text like `2026-11`. It does not affect the app or the UX; it only means the values cannot be read or filtered directly in MySQL. If that is ever needed: a JPA `AttributeConverter<YearMonth, String>` plus a one-off update of existing rows.

## 4. Alerts

- [x] Return a DTO, not the `BudgetAlert` entity. `GET /api/alerts/active` now returns `{lastChecked, alerts:[{id, type, message, month, expenseId, createdAt}]}`.
- [x] Add `type` (`OVER_BUDGET` or `UNUSUAL_EXPENSE`), `expenseId` (for "View expense"), `createdAt`.
- [x] Add "last checked" time: `users.alerts_checked_at`, set every time the check runs (null until the first check).
- [x] Run the check when an expense or budget changes, not only at 8am. The controllers call `BudgetAlertService.checkBurnRateAndCreateAlert` after add/edit/delete expense (also the AI `/parse`) and set/edit/delete budget. The 8am run stays, because the pace changes as days pass.
- [x] Allow more than one alert per month, and do not let a dismissed alert block a new one. Rules:
  - Over budget: one per month. Removed when the pace drops back under the budget. Changing or deleting the budget resets it, so a lower budget can alert again even if the old alert was dismissed.
  - Unusual expense: one per expense (no longer needs a budget). Removed when that expense is deleted or no longer unusual. Every unusual expense this month gets its own alert, not only the first.
  - A dismissed alert never comes back by itself.
- [x] Dismiss exists (`PUT /api/alerts/{id}/dismiss`). Kept.

## 5. AI insights

- [x] `GET /api/ai/insights` returns 503 with a message when Gemini is down or its answer cannot be read. With no expenses it still returns 200 with a summary that explains what is missing (not an error). Checked with curl on 2026-11-17 (Gemini was overloaded, the API answered 503).
- [x] `severity` is always `low`/`medium`/`high` (anything else counts as medium), and each item has a `priorityLabel`: "Low priority", "Moderate priority", "High priority". The design only shows Low and Moderate; High follows the same pattern.

## 6. Smaller things

- [x] One JSON error shape for every error: `{message}`, plus `fieldErrors` `{field: message}` for form validation (then `message` is the first field error). Also the 401s from the security filter, broken JSON bodies (400 instead of 500), wrong HTTP method (405), and the forgot-password success answer (`{message}`). A duplicate budget is now 409, like a taken username. Checked every case with curl.
- [x] Gemini model name `gemini-3.8-flash` exists: Google answers 503 "high demand" for it, not 404.
- [x] `ExpenseParseRequest.userInput`: required, max 300 characters, checked before Gemini is called (`@Valid` on `/parse` and `/parse/preview`).
- [x] `ExpenseRequest.note`: max 255 characters (the database column size). Before, a longer note failed with a database error.

## 7. After the backend is done (before posting the project)

- [x] **Real email sending (SMTP), code done.** `SmtpEmailSender` (Spring `JavaMailSender`), chosen with `MAIL_SENDER=smtp` in `.env`; without it the app keeps printing emails in the console. Settings come from `.env`: `MAIL_HOST` (default smtp.gmail.com), `MAIL_PORT` (587), `MAIL_USERNAME`, `MAIL_PASSWORD`, `MAIL_FROM` (defaults to the username), `APP_BASE_URL` for the link (default http://localhost:8080). A failed send is logged, not shown to the user, so the answer never reveals which emails have accounts. Checked: the app connects to Gmail and logs "Authentication failed" with a fake password.
  - [x] Real Gmail account set in `.env` and tested on 2026-11-17: the reset email arrived. Full reset flow tested in the browser on 2026-11-17 (console email): link opens Set new password, new password works, a used link shows Link expired.
  - [ ] **You, when the site is online:** follow `public_launch.md` (set `APP_BASE_URL` and the other server values).
