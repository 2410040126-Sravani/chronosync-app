const API_BASE = "http://localhost:8082/api";

export async function getAudit(customerId) {
  const res = await fetch(`${API_BASE}/audit/${customerId}`);
  return res.json();
}