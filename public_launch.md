# Public launch checklist

What to do when Expense Runway goes online. Everything secret goes in the server's `.env`, never in git. `.env.example` lists every value.

## 1. Settings on the server

Copy `.env.example` to `.env` on the server and fill it in:

| Value | What to put |
|---|---|
| `DB_PASSWORD` | Password of the hosted MySQL database |
| `JWT_SECRET` | A new long random string (not the one from your laptop) |
| `GEMINI_API_KEY` | Your Gemini API key |
| `MAIL_SENDER` | `smtp` |
| `MAIL_USERNAME` | The Gmail address that sends the emails |
| `MAIL_PASSWORD` | The Gmail app password, **16 letters with no spaces** |
| `APP_BASE_URL` | The real site address, e.g. `https://expense-runway.example.com` (no `/` at the end) |

If the database is not on the same machine, also change `spring.datasource.url` in `application.properties` to the hosted database address.

## 2. Password reset emails

How it works for a user:

1. On the login screen they tap **Forgot password?** and enter the email they signed up with.
2. The app creates a one-time link that expires after 30 minutes.
3. The app sends the email from the Gmail account in `.env`. The user sees it as coming from that address.
4. The user clicks the link, the **Set new password** page opens, and they choose a new password.

If the email has no account, nothing is sent, but the user sees the same message. This way nobody can find out who has an account.

### Gmail app password

1. Turn on 2-Step Verification for the Gmail account.
2. Google Account → Security → **App passwords** → create one.
3. Google shows it as `abcd efgh ijkl mnop`. Put it in `.env` **without the spaces**: `MAIL_PASSWORD=abcdefghijklmnop`. With spaces, the password is read as empty and Gmail refuses to log in.

### Things to know

- **`APP_BASE_URL` must be the real address.** The reset link is built from it. Left at the default (`http://localhost:8080`), the link only works on your own computer.
- **Gmail sends about 500 emails a day.** Enough for a portfolio project.
- **Emails from a personal Gmail can land in spam.** Tell testers to check spam.
- **For real users, switch to an email service** (Brevo or Resend, both have free plans). Only `.env` changes, no code:
  ```
  MAIL_HOST=smtp-relay.brevo.com
  MAIL_PORT=587
  MAIL_USERNAME=<from the service>
  MAIL_PASSWORD=<from the service>
  MAIL_FROM=no-reply@your-domain.com
  ```
- **If an email does not arrive,** look in the server log for `Email to ... could not be sent`. "Authentication failed" means a wrong username or app password.

## 3. Test after going online

- [ ] Sign up, log in, add an expense.
- [ ] Forgot password with a real inbox: the email arrives, the link opens the **real site** (not localhost), the new password works, and the same link does not work a second time.
- [ ] A link older than 30 minutes is refused.
- [ ] AI features: add an expense by sentence. If Gemini is down, the app shows "AI service is busy" and manual entry still works.
