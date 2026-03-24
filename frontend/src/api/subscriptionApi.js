const API_BASE = "http://localhost:8082/api";

async function readJsonSafe(res, fallbackMsg) {
  if (!res.ok) {
    const txt = await res.text().catch(() => "");
    throw new Error(txt || fallbackMsg);
  }
  const text = await res.text().catch(() => "");
  return text ? JSON.parse(text) : null;
}

export async function getSubscription(customerId) {
  const res = await fetch(`${API_BASE}/subscriptions/${customerId}`);
  return readJsonSafe(res, "Failed to load subscription");
}

export async function updateQty(customerId, value) {
  const res = await fetch(
    `${API_BASE}/subscriptions/${customerId}/qty?value=${value}`,
    { method: "PUT" }
  );
  return readJsonSafe(res, "Failed to update quantity");
}

export async function pauseSubscription(customerId, startDate, endDate) {
  const res = await fetch(
    `${API_BASE}/subscriptions/${customerId}/pause?start=${encodeURIComponent(startDate)}&end=${encodeURIComponent(endDate)}`,
    { method: "PUT" }
  );
  return readJsonSafe(res, "Failed to pause subscription");
}

export async function resumeSubscription(customerId) {
  const res = await fetch(`${API_BASE}/subscriptions/${customerId}/resume`, {
    method: "PUT",
  });
  return readJsonSafe(res, "Failed to resume subscription");
}

export async function getPauseSuggestion(customerId) {
  const res = await fetch(`${API_BASE}/subscriptions/${customerId}/pause-suggestion`);
  return readJsonSafe(res, "Failed to load pause suggestion");
}