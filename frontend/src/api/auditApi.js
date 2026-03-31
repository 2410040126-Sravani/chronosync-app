const API_BASE = import.meta.env.VITE_API_URL + "/api";

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