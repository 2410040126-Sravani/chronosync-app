import { useEffect, useState } from "react";
import { API_BASE } from "../config/api";
import { getSubscription, updateQty as updateQtyApi } from "../api/subscriptionApi";

export default function CustomerDashboard() {
  const role = localStorage.getItem("role");

  const stored = localStorage.getItem("user");
  const user = stored ? JSON.parse(stored) : null;
const customerId = user?.customerId || user?.id || user?.userId;
if (!customerId) {
  return <div>Loading user...</div>;
}  
if (role !== "CUSTOMER") {
  return <div>Please login as customer</div>;
}



  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [subscription, setSubscription] = useState(null);
const today = (() => {
  const d = new Date();
  d.setMinutes(d.getMinutes() - d.getTimezoneOffset());
  return d.toISOString().slice(0, 10);
})();

  async function readJson(res, fallbackMsg) {
    if (!res.ok) {
      const txt = await res.text().catch(() => "");
      throw new Error(txt || fallbackMsg);
    }
    const text = await res.text().catch(() => "");
    return text ? JSON.parse(text) : null;
  }
async function loadSubscription() {
  if (!customerId) return;   // 🔥 prevent undefined call

  const data = await getSubscription(customerId);
  setSubscription(data ?? null);
}


   async function updateQty(value) {
  try {
    setError("");

    const newQty = Number(value);
    if (!Number.isFinite(newQty) || newQty < 1) {
      throw new Error("Quantity must be at least 1");
    }

  const updated = await updateQtyApi(customerId, newQty);
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
  if (customerId) {
    loadAll();
  }
}, [customerId]);

useEffect(() => {
  const interval = setInterval(() => {
    loadSubscription(); // auto refresh every 3 sec
  }, 3000);

  return () => clearInterval(interval);
}, [customerId])
  const qty = subscription?.qtyLitres ?? subscription?.qty ?? 0;
  const status = subscription?.status ?? "—";
  const nextDelivery = subscription?.nextDeliveryDate ?? subscription?.nextDelivery ?? "—";
  const todayDate = new Date(today);
const nextDate = nextDelivery ? new Date(nextDelivery) : null;
const isPausedActive =
  nextDelivery && nextDelivery >= today;
  const effectiveEndDate =
  subscription?.effectiveEndDate ?? subscription?.endDate ?? "2026-04-13";

const originalEndDate =
  subscription?.endDate ?? "2026-04-10";
const activePause = subscription?.pauses?.find(
  (p) => (p.pauseEndDate || p.endDate) >= today
);
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
  <div className="kValue">{status || "—"}</div>


<div style={{ marginTop: 6, fontWeight: 700 }}>
  {activePause
    ? `Pause: ${activePause.pauseStartDate || activePause.startDate} → ${activePause.pauseEndDate || activePause.endDate}`
    : "No active pause"}
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