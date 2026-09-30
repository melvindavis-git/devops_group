// Small shared helpers used by every page.
// All requests are relative, so this must be served by the same
// Spring Boot app that exposes /api/... (static/ folder on the same origin).

// User-facing fallback text. Kept low-key and specific to what's affected —
// never phrased as "the system/microservice is down", just "not available
// right now", the same tone as the inline "Ej tillgänglig" labels.
const CUSTOMER_UNAVAILABLE_MESSAGE = "Tjänsten för kunder kan ej nås just nu.";
const REVIEW_UNAVAILABLE_MESSAGE = "Tjänsten för recensioner kan ej nås just nu.";
const GENERIC_UNAVAILABLE_MESSAGE = "Kan inte hämta informationen just nu. Försök igen senare.";

// Turns raw backend/network error text into a readable Swedish message.
// Returns null when the text doesn't match a known pattern, so a real
// backend message (e.g. "Inga rum tillgängliga.") can still be shown as-is.
function friendlyErrorMessage(rawMessage) {
  const msg = (rawMessage || "").toLowerCase();

  if (
    msg.includes("connection refused") ||
    msg.includes("i/o error") ||
    msg.includes("econnrefused") ||
    msg.includes("unknownhostexception") ||
    msg.includes("connect timed out") ||
    msg.includes("resourceaccessexception")
  ) {
    return CUSTOMER_UNAVAILABLE_MESSAGE;
  }

  // Booking's own backend produces this vague message when the reviews
  // service specifically can't be reached.
  if (msg.includes("något gick fel")) {
    return REVIEW_UNAVAILABLE_MESSAGE;
  }

  // Spring Boot's default Whitelabel error body for 5xx responses leaks
  // this text verbatim when a service has no custom error handling. The
  // origin isn't identifiable from the text alone, so fall back to a
  // generic (still low-key) message rather than guessing.
  if (
    msg.includes("internal server error") ||
    msg.includes("bad gateway") ||
    msg.includes("service unavailable") ||
    msg.includes("gateway timeout")
  ) {
    return GENERIC_UNAVAILABLE_MESSAGE;
  }

  if (msg.includes("failed to fetch") || msg.includes("networkerror") || msg.includes("load failed")) {
    return "Kan inte nå appen just nu. Försök igen senare.";
  }

  return null;
}

// Pulls a human-readable message out of a JSON error body, trying the
// shapes Spring commonly produces: {message}, {error}, or a Spring
// validation body with an {errors:[{defaultMessage}]} array. Returns null
// if nothing usable is found (many of this app's error responses carry no
// message at all — an empty DTO or an empty body).
function extractBackendMessage(body, isJson) {
  if (isJson && body && typeof body === "object") {
    if (typeof body.message === "string" && body.message.trim()) return body.message;
    if (typeof body.error === "string" && body.error.trim()) return body.error;
    if (Array.isArray(body.errors) && body.errors.length) {
      const combined = body.errors
        .map((e) => e.defaultMessage || e.message)
        .filter(Boolean)
        .join(" ");
      if (combined.trim()) return combined;
    }
  }
  if (!isJson && typeof body === "string" && body.trim()) {
    return body;
  }
  return null;
}

function defaultStatusMessage(status) {
  if (status >= 500) return GENERIC_UNAVAILABLE_MESSAGE;
  switch (status) {
    case 400:
      return "Ogiltig förfrågan. Kontrollera uppgifterna du skickade in.";
    case 404:
      return "Hittades inte.";
    case 409:
      return "Kunde inte genomföras eftersom det skulle skapa en konflikt.";
    default:
      return `Anropet misslyckades (status ${status}).`;
  }
}

const Api = {
  // statusMessages: optional { [httpStatus]: "text" } map so each call site
  // can supply a context-specific fallback (used only when the backend
  // itself didn't send a usable message).
  async request(url, options = {}, statusMessages = {}) {
    let res;
    try {
      res = await fetch(url, options);
    } catch (networkErr) {
      const err = new Error(
        friendlyErrorMessage(networkErr.message) || "Kan inte nå appen just nu. Försök igen senare."
      );
      err.status = 0;
      throw err;
    }

    const contentType = res.headers.get("content-type") || "";
    const isJson = contentType.includes("application/json");
    const body = isJson ? await res.json().catch(() => null) : await res.text();

    if (!res.ok) {
      const backendMessage = extractBackendMessage(body, isJson);
      const message =
        friendlyErrorMessage(backendMessage) ||
        backendMessage ||
        statusMessages[res.status] ||
        defaultStatusMessage(res.status);
      const err = new Error(message);
      err.status = res.status;
      err.rawBody = body;
      throw err;
    }
    return body;
  },

  get(url, statusMessages) {
    return this.request(url, {}, statusMessages);
  },

  postJson(url, data, statusMessages) {
    return this.request(
      url,
      {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(data),
      },
      statusMessages
    );
  },

  putJson(url, data, statusMessages) {
    return this.request(
      url,
      {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(data),
      },
      statusMessages
    );
  },

  // For endpoints declared with @RequestParam (not @RequestBody),
  // the values must go in the query string, not a JSON body.
  post(url, params, statusMessages) {
    const qs = new URLSearchParams(params).toString();
    return this.request(`${url}?${qs}`, { method: "POST" }, statusMessages);
  },

  put(url, params, statusMessages) {
    const qs = new URLSearchParams(params).toString();
    return this.request(`${url}?${qs}`, { method: "PUT" }, statusMessages);
  },

  delete(url, statusMessages) {
    return this.request(url, { method: "DELETE" }, statusMessages);
  },
};

function showAlert(container, message, type = "error") {
  container.innerHTML = "";
  if (!message) return;
  const div = document.createElement("div");
  div.className = `alert alert-${type}`;
  div.textContent = message;
  container.appendChild(div);
}

function qs(name) {
  return new URLSearchParams(window.location.search).get(name);
}

function escapeHtml(str) {
  const div = document.createElement("div");
  div.textContent = str ?? "";
  return div.innerHTML;
}

// Highlight the current page in the nav bar.
document.addEventListener("DOMContentLoaded", () => {
  const path = window.location.pathname.replace(/\/$/, "") || "/";
  document.querySelectorAll("nav a[data-nav]").forEach((a) => {
    const target = a.getAttribute("href").replace(/\/$/, "") || "/";
    if (target === path) a.classList.add("active");
  });
});
