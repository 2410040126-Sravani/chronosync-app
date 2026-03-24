import { useEffect, useState } from "react";
import { API_BASE } from "../config/api";

export default function CustomerDashboard() {
  const customerId = 1;

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [subscription, setSubscription] = useState(null);

  async function readJson(res, fallbackMsg) {
    if (!res.ok) {
      const txt = await res.text().catch(() => "");
      throw new Error(txt || fallbackMsg);
    }
    const text = await res.text().catch(() => "");
    return text ? JSON.parse(text) : null;
  }

  async function loadSubscription() {
    const res = await fetch(`${API_BASE}/api/subscriptions/${customerId}`);
    const data = await readJson(res, "Failed to load subscription");
    setSubscription(data ?? null);
  }


   async function updateQty(value) {
  try {
    setError("");

    const newQty = Number(value);
    if (!Number.isFinite(newQty) || newQty < 1) {
      throw new Error("Quantity must be at least 1");
    }

    const res = await fetch(
      `${API_BASE}/api/subscriptions/${customerId}/qty?value=${newQty}`,
      { method: "PUT" }
    );

    const updated = await readJson(res, "Update failed");
    setSubscription(updated ?? null);

    await loadSubscription();
  } catch (e) {
    setError(e?.message || "Quantity update failed");
  }
}

  async function loadAll() {
    try {
      setError("");
      setLoading(true);
      await loadSubscription();
    } catch (e) {
      setError(e?.message || "Something went wrong");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadAll();
  }, []);

  const qty = subscription?.qtyLitres ?? subscription?.qty ?? 0;
  const status = subscription?.status ?? "—";
  const nextDelivery = subscription?.nextDeliveryDate ?? subscription?.nextDelivery ?? "—";
  const effectiveEndDate =
    subscription?.effectiveEndDate ?? subscription?.endDate ?? "—";

  return (
    <div className="glass pageCard">
      <div style={{ display: "flex", justifyContent: "space-between", gap: 10, flexWrap: "wrap" }}>
        <h3 style={{ marginTop: 0 }}>Customer Dashboard</h3>

        <button className="btn" onClick={loadAll} disabled={loading}>
          {loading ? "Refreshing..." : "Refresh"}
        </button>
      </div>

      {error && (
        <div className="glass" style={{ marginTop: 12, padding: 12, borderRadius: 12 }}>
          <b>Error:</b> <span style={{ opacity: 0.85 }}>{error}</span>
        </div>
      )}

      <div style={{ display: "flex", gap: 14, flexWrap: "wrap", marginTop: 14 }}>
        {/* Quantity */}
<div className="kpi">
  <div className="kLabel">Quantity</div>
  <div className="kValue">{qty} L</div>

  <div style={{ display: "flex", gap: 10, marginTop: 12 }}>
   <button
  className="btn"
  style={{ width: 40, height: 40, padding: 0 }}
  onClick={() => updateQty(qty - 1)}
  disabled={loading || qty <= 1}
>
  -
</button>

<button
  className="btn"
  style={{ width: 40, height: 40, padding: 0 }}
  onClick={() => updateQty(qty + 1)}
  disabled={loading}
>
  +
</button>
  </div>
</div>
        <div className="kpi">
          <div className="kLabel">Status</div>
          <div className="kValue">{status}</div>
          <div style={{ marginTop: 6, fontWeight: 700, opacity: 0.75 }}>
            {nextDelivery}
          </div>
        </div>

        <div className="kpi">
          <div className="kLabel">Effective End Date</div>
          <div className="kValue" style={{ fontSize: 22 }}>{effectiveEndDate}</div>
          <div style={{ marginTop: 6, opacity: 0.7 }}>
            (Auto-extended during pauses)
          </div>
        </div>
      </div>
    </div>
  );
}