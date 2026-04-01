import { API_BASE } from "../config/api";

function getAuthHeaders() {
  const token = localStorage.getItem("token");

  return {
    Authorization: `Bearer ${token}`,
  };
}

export async function getAudit(customerId) {
  const res = await fetch(`${API_BASE}/audit/${customerId}`, {
    headers: getAuthHeaders(),
  });

  return res.json();
}