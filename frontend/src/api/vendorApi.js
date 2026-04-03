import { API_BASE } from "../config/api";
function getAuthHeaders() {
  const token = localStorage.getItem("token");

  return {
    "Content-Type": "application/json",
    Authorization: `Bearer ${token}`,
  };
}

// safe reader
async function readJsonSafe(res, msg) {
  const text = await res.text().catch(() => "");

  if (!res.ok) {
    console.error("API ERROR:", text);
    throw new Error(text || msg);
  }

  return text ? JSON.parse(text) : null;
}

export async function getTodaySummary(vendorId) {
  const res = await fetch(`${API_BASE}/vendor/${vendorId}/today-summary`, {
    headers: getAuthHeaders(),
  });

  return readJsonSafe(res, "Failed to load today summary");
}

export async function getAnalytics(vendorId) {
  const res = await fetch(`${API_BASE}/vendor/${vendorId}/analytics`, {
    headers: getAuthHeaders(),
  });

  return readJsonSafe(res, "Failed to load analytics");
}

export async function getChangeAlerts(vendorId) {
  const res = await fetch(`${API_BASE}/vendor/${vendorId}/change-alerts`, {
    headers: getAuthHeaders(),
  });

  return readJsonSafe(res, "Failed to load change alerts");
}
export async function getCustomers(vendorId) {
  const token = localStorage.getItem("token");

  const res = await fetch(
    `https://chronosync-docker.onrender.com/api/vendor/${vendorId}/customers`,
    {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    }
  );

  return await res.json();
}