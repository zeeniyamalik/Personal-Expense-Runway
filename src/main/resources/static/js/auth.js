// The URL decides the view: auth.html = login, auth.html#signup = signup.
const screen = document.querySelector('.screen');
const DURATION = window.matchMedia('(prefers-reduced-motion: reduce)').matches ? 0 : 900;

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
function morphTo(view) {
  cancelAnimationFrame(frameId);
  const from = blobs.map((b) => [...b.now]);   // start from where the blob is right now
  const startTime = performance.now();

  function step(time) {
    const t = DURATION === 0 ? 1 : Math.min((time - startTime) / DURATION, 1);
    const k = easeInOut(t);
    blobs.forEach((blob, n) => {
      blob.now = from[n].map((v, j) => v + (blob.shapes[view][j] - v) * k);
      draw(blob);
    });
    if (t < 1) frameId = requestAnimationFrame(step);
  }
  frameId = requestAnimationFrame(step);
}

function sync() {
  const view = location.hash === '#signup' ? 'signup' : 'login';
  screen.dataset.view = view;
  document.title = (view === 'signup' ? 'Create Account' : 'Log In') + ' | Expense Runway';
  morphTo(view);
}

window.addEventListener('hashchange', sync);   // links change the hash, so Back works too
sync();