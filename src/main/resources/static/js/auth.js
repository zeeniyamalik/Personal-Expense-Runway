// The hash decides the view: auth.html = login, #signup, #forgot, #sent, #reset, #done, #expired.
const screen = document.querySelector('.screen');
const DURATION = window.matchMedia('(prefers-reduced-motion: reduce)').matches ? 0 : 900;

const VIEWS = {
  login: 'Log In',
  signup: 'Create Account',
  forgot: 'Forgot Password',
  sent: 'Check Your Email',
  reset: 'Set New Password',
  done: 'Password Updated',
  expired: 'Link Expired',
};

// ---------- Background blobs ----------

// Pull every number out of a path string, e.g. "M 470 367 C ..." becomes [470, 367, ...]
const numbers = (text) => text.match(/-?\d*\.?\d+/g).map(Number);

// Each blob remembers its path text (as a template), its two shapes, and where it is now.
// The stroke width is added as the last number so it blends along with the curve.
const blobs = [...document.querySelectorAll('.blob')].map((el) => {
  const template = el.getAttribute('d');
  const shapes = {
    login:  [...numbers(template), Number(el.getAttribute('stroke-width'))],
    signup: [...numbers(el.dataset.signup), Number(el.dataset.signupWidth)],
  };
  return { el, template, shapes, now: [...shapes.login] };
});

// Write the current numbers back into the path text and stroke width.
function draw(blob) {
  const values = blob.now;
  let i = 0;
  blob.el.setAttribute('d', blob.template.replace(/-?\d*\.?\d+/g, () => values[i++].toFixed(1)));
  blob.el.setAttribute('stroke-width', values[values.length - 1].toFixed(1));
}

const easeInOut = (t) => (t < 0.5 ? 4 * t * t * t : 1 - Math.pow(-2 * t + 2, 3) / 2);

let frameId;
function morphTo(shape) {
  cancelAnimationFrame(frameId);
  const from = blobs.map((b) => [...b.now]);   // start from where the blob is right now
  const startTime = performance.now();

  function step(time) {
    const t = DURATION === 0 ? 1 : Math.min((time - startTime) / DURATION, 1);
    const k = easeInOut(t);
    blobs.forEach((blob, n) => {
      blob.now = from[n].map((v, j) => v + (blob.shapes[shape][j] - v) * k);
      draw(blob);
    });
    if (t < 1) frameId = requestAnimationFrame(step);
  }
  frameId = requestAnimationFrame(step);
}

// ---------- Arriving on the page ----------

const banner = document.getElementById('session-banner');
const params = new URLSearchParams(location.search);

// The email link is auth.html?reset=TOKEN. The token is kept for this tab only and
// removed from the address bar, so it does not stay in the browser history.
if (params.has('reset')) {
  sessionStorage.setItem('runway.resetToken', params.get('reset'));
  history.replaceState(null, '', 'auth.html#reset');
} else if (params.get('session') === 'expired') {
  banner.hidden = false;
  history.replaceState(null, '', 'auth.html' + location.hash);
} else if (getToken()) {
  location.replace('home.html');   // already logged in
}

function sync() {
  let view = location.hash.slice(1);
  if (!(view in VIEWS)) view = 'login';
  // #reset without a token (e.g. typed by hand) cannot work
  if (view === 'reset' && !sessionStorage.getItem('runway.resetToken')) view = 'expired';
  if (view !== 'login') banner.hidden = true;

  screen.dataset.view = view;
  document.querySelectorAll('.card').forEach((card) => {
    card.classList.toggle('is-active', card.dataset.card === view);
  });
  document.title = VIEWS[view] + ' | Expense Runway';
  morphTo(view === 'signup' ? 'signup' : 'login');
}

window.addEventListener('hashchange', sync);   // links change the hash, so Back works too
sync();

// ---------- Form helpers ----------

const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
const USERNAME_PATTERN = /^[a-zA-Z0-9_.]{4,20}$/;
const PASSWORD_RULES = {
  length: (p) => p.length >= 8,
  number: (p) => /\d/.test(p),
  special: (p) => /[^a-zA-Z0-9]/.test(p),
};
const passwordOk = (p) => Object.values(PASSWORD_RULES).every((rule) => rule(p));

function clearErrors(form) {
  form.querySelectorAll('.field__error, .form__error').forEach((el) => { el.textContent = ''; });
  form.querySelectorAll('[aria-invalid]').forEach((el) => el.removeAttribute('aria-invalid'));
}

function setFieldError(form, name, message) {
  const box = form.querySelector(`[data-error-for="${name}"]`);
  if (!box) return false;
  box.textContent = message;
  form.elements[name].setAttribute('aria-invalid', 'true');
  return true;
}

function setFormError(form, message) {
  form.querySelector('.form__error').textContent = message;
}

// Shows the backend's field errors under their fields, otherwise the general message.
function showServerError(form, error) {
  let shown = false;
  for (const [name, message] of Object.entries(error.fieldErrors || {})) {
    if (setFieldError(form, name, message)) shown = true;
  }
  if (!shown) setFormError(form, error.message);
}

// Disables the button while the request runs, so it is not sent twice.
async function submitWith(form, work) {
  const button = form.querySelector('button[type="submit"]');
  const label = button.textContent;
  button.disabled = true;
  button.textContent = button.dataset.busy;
  try {
    await work();
  } catch (error) {
    showServerError(form, error);
  } finally {
    button.disabled = false;
    button.textContent = label;
  }
}

// Returns true when every check passed; otherwise shows the first problem per field.
function check(form, checks) {
  let ok = true;
  for (const [name, test, message] of checks) {
    if (form.querySelector(`[data-error-for="${name}"]`).textContent) continue;
    if (!test(form.elements[name].value)) {
      setFieldError(form, name, message);
      ok = false;
    }
  }
  if (!ok) form.querySelector('[aria-invalid="true"]').focus();
  return ok;
}

// Live password checklist under the password fields.
document.querySelectorAll('[data-rules-for]').forEach((list) => {
  const input = document.getElementById(list.dataset.rulesFor);
  input.addEventListener('input', () => {
    list.querySelectorAll('[data-rule]').forEach((item) => {
      item.classList.toggle('is-met', PASSWORD_RULES[item.dataset.rule](input.value));
    });
  });
});

const value = (form, name) => form.elements[name].value.trim();

// ---------- Log in ----------

const loginForm = document.getElementById('login-form');
loginForm.addEventListener('submit', (event) => {
  event.preventDefault();
  clearErrors(loginForm);
  const ok = check(loginForm, [
    ['email', (v) => v.trim() !== '', 'Enter your email.'],
    ['email', (v) => EMAIL_PATTERN.test(v.trim()), 'Enter a valid email, like you@example.com.'],
    ['password', (v) => v !== '', 'Enter your password.'],
  ]);
  if (!ok) return;

  submitWith(loginForm, async () => {
    const auth = await api('/auth/login', {
      method: 'POST',
      body: { email: value(loginForm, 'email'), password: loginForm.elements.password.value },
    });
    saveSession(auth);
    location.replace('home.html');
  });
});

// ---------- Sign up ----------

const signupForm = document.getElementById('signup-form');
signupForm.addEventListener('submit', (event) => {
  event.preventDefault();
  clearErrors(signupForm);
  const ok = check(signupForm, [
    ['name', (v) => v.trim() !== '', 'Enter your name.'],
    ['username', (v) => v.trim().length >= 4, 'Use at least 4 characters.'],
    ['username', (v) => USERNAME_PATTERN.test(v.trim()), 'Only letters, numbers, _ and . are allowed.'],
    ['email', (v) => EMAIL_PATTERN.test(v.trim()), 'Enter a valid email, like you@example.com.'],
    ['password', passwordOk, 'Your password needs 8+ characters, a number and a special character.'],
  ]);
  if (!ok) return;

  submitWith(signupForm, async () => {
    try {
      const auth = await api('/auth/signup', {
        method: 'POST',
        body: {
          name: value(signupForm, 'name'),
          username: value(signupForm, 'username'),
          email: value(signupForm, 'email'),
          password: signupForm.elements.password.value,
        },
      });
      saveSession(auth);
      location.replace('home.html');
    } catch (error) {
      // 409: the email or the username is already taken; show it under that field
      if (error.status === 409) {
        setFieldError(signupForm, /username/i.test(error.message) ? 'username' : 'email', error.message);
        return;
      }
      throw error;
    }
  });
});

// ---------- Forgot password ----------

const forgotForm = document.getElementById('forgot-form');
forgotForm.addEventListener('submit', (event) => {
  event.preventDefault();
  clearErrors(forgotForm);
  const ok = check(forgotForm, [
    ['email', (v) => EMAIL_PATTERN.test(v.trim()), 'Enter a valid email, like you@example.com.'],
  ]);
  if (!ok) return;

  submitWith(forgotForm, async () => {
    const email = value(forgotForm, 'email');
    await api('/auth/forgot-password', { method: 'POST', body: { email } });
    document.getElementById('sent-email').textContent = email;
    location.hash = 'sent';
  });
});

// ---------- Set new password ----------

const resetForm = document.getElementById('reset-form');
resetForm.addEventListener('submit', (event) => {
  event.preventDefault();
  clearErrors(resetForm);
  const ok = check(resetForm, [
    ['newPassword', passwordOk, 'Your password needs 8+ characters, a number and a special character.'],
    ['confirm', (v) => v === resetForm.elements.newPassword.value, 'The passwords do not match.'],
  ]);
  if (!ok) return;

  submitWith(resetForm, async () => {
    try {
      await api('/auth/reset-password', {
        method: 'POST',
        body: { token: sessionStorage.getItem('runway.resetToken'), newPassword: resetForm.elements.newPassword.value },
      });
    } catch (error) {
      if (/invalid or has expired/i.test(error.message)) {
        sessionStorage.removeItem('runway.resetToken');
        location.hash = 'expired';
        return;
      }
      throw error;
    }
    sessionStorage.removeItem('runway.resetToken');
    resetForm.reset();
    location.hash = 'done';
  });
});
