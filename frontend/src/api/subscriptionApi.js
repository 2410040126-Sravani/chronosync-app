import { API_BASE } from "../config/api";

const authHeaders = () => {
  const token = localStorage.getItem("token");

  return {
    "Content-Type": "application/json",
    Authorization: token ? `Bearer ${token}` : "",
  };
};

// ✅ safe reader
async function readJsonSafe(res, msg) {
  const text = await res.text().catch(() => "");

  if (!res.ok) {
    console.error("API ERROR:", text);
    throw new Error(text || msg);
  }

  return text ? JSON.parse(text) : null;
}

// ===============================
// GET
// ===============================
export async function getSubscription(id) {
  const res = await fetch(`${API_BASE}/subscriptions/${id}`, {
    headers: authHeaders(),
  });

  return readJsonSafe(res, "Failed to load subscription");
}

// ===============================
// UPDATE QTY
// ===============================
export async function updateQty(id, qty) {
  const res = await fetch(
    `${API_BASE}/subscriptions/${id}/qty?value=${qty}`,
    {
      method: "PUT",
      headers: authHeaders(),
    }
  );

  return readJsonSafe(res, "Qty update failed");
}

// ===============================
// PAUSE
// ===============================
export async function pauseSubscription(id, start, end) {
  const res = await fetch(
    `${API_BASE}/subscriptions/${id}/pause?start=${start}&end=${end}`,
    {
      method: "PUT",
      headers: authHeaders(),
    }
  );

  return readJsonSafe(res, "Pause failed");
}

// ===============================
// ✅ FIXED RESUME (PUT NOT POST)
// ===============================
export async function resumeSubscription(id) {
  const res = await fetch(`${API_BASE}/subscriptions/${id}/resume`, {
    method: "PUT", // 🔥 FIX
    headers: authHeaders(),
  });

  return readJsonSafe(res, "Resume failed");
}

// ===============================
// ✅ FIXED CLEAR PAUSES (PUT)
// ===============================
export async function clearPauses(id) {
  const res = await fetch(`${API_BASE}/subscriptions/${id}/clear-pauses`, {
    method: "PUT", // 🔥 FIX
    headers: authHeaders(),
  });

  return readJsonSafe(res, "Clear pauses failed");
}

// ===============================
// PAUSE SUGGESTION
// ===============================
export async function getPauseSuggestion(vendorId) {
  const res = await fetch(
    `${API_BASE}/subscriptions/vendor/${vendorId}/pause-suggestion`,
    {
      headers: authHeaders(),
    }
  );

  return readJsonSafe(res, "Pause suggestion failed");
}