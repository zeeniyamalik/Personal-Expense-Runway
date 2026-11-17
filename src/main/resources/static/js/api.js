// token storage and API calls, shared by every page

const TOKEN_KEY = 'runway.token';
const USER_KEY = 'runway.user';

function saveSession(auth) {
  localStorage.setItem(TOKEN_KEY, auth.token);
  localStorage.setItem(USER_KEY, JSON.stringify(auth.user));
}

function getToken() {
  return localStorage.getItem(TOKEN_KEY);
}

function getUser() {
  try {
    return JSON.parse(localStorage.getItem(USER_KEY));
  } catch {
    return null;
  }
}

function clearSession() {
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem(USER_KEY);
}

// call this first on pages that need a login
function requireLogin() {
  if (!getToken()) location.replace('auth.html');
}

// returns the JSON answer, or throws { status, message, fieldErrors }
async function api(path, { method = 'GET', body } = {}) {
  const isAuthCall = path.startsWith('/auth/');
  const headers = {};
  if (body !== undefined) headers['Content-Type'] = 'application/json';
  const token = getToken();
  if (token && !isAuthCall) headers.Authorization = 'Bearer ' + token;

  let response;
  try {
    response = await fetch('/api' + path, {
      method,
      headers,
      body: body === undefined ? undefined : JSON.stringify(body),
    });
  } catch {
    throw { status: 0, message: "Can't reach the server. Check your connection and try again.", fieldErrors: {} };
  }

  const text = await response.text();
  let data = null;
  try {
    data = text ? JSON.parse(text) : null;
  } catch {
    data = null;
  }

  if (response.ok) return data;

  // token expired or invalid: back to login
  if (response.status === 401 && !isAuthCall) {
    clearSession();
    location.replace('auth.html?session=expired');
  }

  throw {
    status: response.status,
    message: (data && data.message) || 'Something went wrong. Please try again.',
    fieldErrors: (data && data.fieldErrors) || {},
  };
}
