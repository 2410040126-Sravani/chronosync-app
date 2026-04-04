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

export async function getSubscription(id) {
  const res = await fetch(`${API_BASE}/subscriptions/${id}`, {
    headers: authHeaders(),
  });

  return readJsonSafe(res, "Failed to load subscription");
}

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

// ✅ FIXED (PUT → POST)
export async function resumeSubscription(id) {
  const res = await fetch(`${API_BASE}/subscriptions/${id}/resume`, {
    method: "POST",
    headers: authHeaders(),
  });

  return readJsonSafe(res, "Resume failed");
}

// ✅ ADD THIS (you were missing clear API)
export async function clearPauses(id) {
  const res = await fetch(`${API_BASE}/subscriptions/${id}/clear-pauses`, {
    method: "POST",
    headers: authHeaders(),
  });

  return readJsonSafe(res, "Clear pauses failed");
}

export async function getPauseSuggestion(id) {
  const res = await fetch(
    `${API_BASE}/subscriptions/${id}/pause-suggestion`,
    {
      headers: authHeaders(),
    }
  );

  if (!res.ok) return null;
  return res.json();
}