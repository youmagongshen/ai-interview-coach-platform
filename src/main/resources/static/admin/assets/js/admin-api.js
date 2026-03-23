(function () {
  const BASE = "/inv";
  const USER_KEY = "aiInterviewAdminUserId";

  function getUserId() {
    const raw = localStorage.getItem(USER_KEY);
    const id = Number(raw || 1);
    return Number.isFinite(id) && id > 0 ? id : 1;
  }

  function setUserId(id) {
    const num = Number(id);
    if (!Number.isFinite(num) || num <= 0) {
      throw new Error("User ID must be a positive number");
    }
    localStorage.setItem(USER_KEY, String(Math.floor(num)));
  }

  function buildUrl(path, query) {
    const url = new URL(BASE + path, window.location.origin);
    if (query) {
      Object.entries(query).forEach(([k, v]) => {
        if (v === undefined || v === null || v === "") return;
        url.searchParams.set(k, String(v));
      });
    }
    return url.toString();
  }

  async function request(path, options) {
    const opt = options || {};
    const method = opt.method || "GET";
    const headers = {
      Accept: "application/json",
      "X-User-Id": String(getUserId())
    };

    let body;
    if (opt.formData) {
      body = opt.formData;
    } else if (opt.body !== undefined) {
      headers["Content-Type"] = "application/json";
      body = JSON.stringify(opt.body);
    }

    const res = await fetch(buildUrl(path, opt.query), {
      method,
      headers,
      body
    });

    let payload;
    try {
      payload = await res.json();
    } catch (e) {
      throw new Error("Server returned non-JSON response");
    }

    if (!res.ok) {
      throw new Error((payload && payload.message) || `HTTP ${res.status}`);
    }

    if (!payload || payload.code !== 0) {
      throw new Error((payload && payload.message) || "Request failed");
    }

    return payload.data;
  }

  function showError(err) {
    const message = err && err.message ? err.message : String(err);
    window.alert(message);
  }

  window.AdminAPI = {
    request,
    getUserId,
    setUserId,
    showError
  };
})();

