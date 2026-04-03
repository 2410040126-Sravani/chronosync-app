import { useEffect, useState } from "react";
import { API_BASE } from "../config/api";
import {
  getTodaySummary,
  getAnalytics,
  getChangeAlerts,
  getCustomers
} from "../api/vendorApi";

function getAuthHeaders() {
  const token = localStorage.getItem("token");

  return {
    "Content-Type": "application/json",
    Authorization: `Bearer ${token}`,
  };
}
export default function VendorDashboard() {

const stored = localStorage.getItem("user");
const user = stored ? JSON.parse(stored) : null;

const vendorId = user?.userId;

if (!vendorId) {
  return <div>Please login as vendor</div>;
}

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
const [suggestion, setSuggestion] = useState("");
  const [todayLive, setTodayLive] = useState(null);
  const [analytics, setAnalytics] = useState(null);
  const [changeAlerts, setChangeAlerts] = useState(null);
  const [lastSyncedAt, setLastSyncedAt] = useState("");

  const [showTomorrow, setShowTomorrow] = useState(false);
  const [tomorrowData, setTomorrowData] = useState(null);
  const [customers, setCustomers] = useState([]);
    console.log("Customers FULL:", customers);
  const [notifications, setNotifications] = useState([]);
const [unreadCount, setUnreadCount] = useState(0);
  const [tomorrowStatus, setTomorrowStatus] = useState("idle");

  async function readJson(res, fallbackMsg) {
    if (!res.ok) {
      const txt = await res.text().catch(() => "");
      throw new Error(txt || fallbackMsg);
    }
    const text = await res.text().catch(() => "");
    return text ? JSON.parse(text) : null;
  }


async function loadToday() {
  try {
    const data = await getTodaySummary(vendorId);
    setTodayLive(data ?? null);
  } catch (error) {
    console.log("Backend error:", error);

    // TEMP fallback so UI doesn't break
    setTodayLive({
      totalMilk: 0,
      stops: 0,
      pausedCustomers: 0,
      date: new Date().toISOString()
    });
  }
}

  async function loadAnalytics() {
  const data = await getAnalytics(vendorId);
  setAnalytics(data ?? null);
}

async function loadSuggestion() {
  const res = await fetch(`${API_BASE}/vendor/${vendorId}/pause-suggestion`, {
    headers: getAuthHeaders()
  });
  const text = await res.text();
  setSuggestion(text);
}
 async function loadChangeAlerts() {
  const data = await getChangeAlerts(vendorId);
  setChangeAlerts(data ?? null);
 setLastSyncedAt(new Date().toLocaleString());
}
async function loadCustomers() {
  const data = await getCustomers(vendorId);

  const list = Array.isArray(data) ? data : [];

  setCustomers(list);

  return list; // ✅ correct
}

  async function loadTomorrow() {
    setTomorrowStatus("loading");
    try {
    const res = await fetch(`${API_BASE}/vendor/${vendorId}/tomorrow-preview`, {
  headers: {
    "Content-Type": "application/json",
    Authorization: `Bearer ${localStorage.getItem("token")}`,
  },
});
      if (res.status === 404) {
        setTomorrowStatus("missing");
        setTomorrowData(null);
        return;
      }

      const data = await readJson(res, "Failed to load tomorrow preview");
      setTomorrowData(data ?? null);
      setTomorrowStatus("ok");
    } catch {
      setTomorrowStatus("error");
      setTomorrowData(null);
    }
  }
async function loadNotifications(customerList) {
  try {
    const customerIds = customerList.map(c => c.id);

    if (customerIds.length === 0) return;

    const res = await fetch(`${API_BASE}/audit/vendor`, {
      method: "POST",
      headers: getAuthHeaders(),
      body: JSON.stringify(customerIds),
    });

    const data = await res.json();

    setNotifications(data);
    setUnreadCount(data.length);

  } catch (e) {
    console.error("Notification load failed", e);
  }
}
  async function loadAll() {
  setError("");
  setLoading(true);

  await loadSuggestion();

  // run independent APIs together
  await Promise.all([
    loadToday(),
    loadAnalytics(),
    loadChangeAlerts()
  ]);

  
  const customerList = await loadCustomers();    
await loadNotifications(customerList);    

  setLoading(false);
}
  useEffect(() => {
  if (!vendorId) return;  
  loadAll();
}, [vendorId]);

  useEffect(() => {
  const interval = setInterval(() => {
    loadAll();
  }, 10000); // every 10 sec

  return () => clearInterval(interval);
}, []);

  useEffect(() => {
    if (showTomorrow) loadTomorrow();
  }, [showTomorrow]);

  async function onAcknowledge() {
    try {
      setError("");
      const res = await fetch(`${API_BASE}/vendor/${vendorId}/mark-synced`, {
  method: "POST",
  headers: {
    "Content-Type": "application/json",
    Authorization: `Bearer ${localStorage.getItem("token")}`,
  },
});
       
      const data = await readJson(res, "Failed to mark synced");
      if (data?.lastSyncedAt) {
setLastSyncedAt(
  data?.lastSyncedAt
    ? new Date(data.lastSyncedAt).toLocaleString()
    : new Date().toLocaleString()
);      }
      await loadChangeAlerts();
    } catch (e) {
      setError(e?.message || "Sync failed");
    }
  }

  async function onRecomputeToday() {
    try {
      setError("");
      const res = await fetch(`${API_BASE}/vendor/${vendorId}/recompute-today`, {
  method: "POST",
  headers: {
    "Content-Type": "application/json",
    Authorization: `Bearer ${localStorage.getItem("token")}`,
  },
});
      await readJson(res, "Failed to recompute today");
      await loadToday();
      await loadAnalytics();
    } catch (e) {
      setError(e?.message || "Recompute failed");
    }
  }

  return (
    <div className="glass pageCard">
      <div style={{ display: "flex", justifyContent: "space-between", gap: 10, flexWrap: "wrap" }}>
        <h3 style={{ marginTop: 0 }}>Vendor Dashboard</h3>

        <div style={{ display: "flex", gap: 10, flexWrap: "wrap" }}>
          <button className="btn" onClick={loadAll} disabled={loading}>
            Refresh
          </button>
          <button className="btn" onClick={onRecomputeToday} disabled={loading}>
            Recompute Today
          </button>
        </div>
      </div>

      {error && (
        <div className="glass" style={{ marginTop: 12, padding: 12, borderRadius: 12 }}>
          <b>Error:</b> <span style={{ opacity: 0.85 }}>{error}</span>
        </div>
      )}

      <div style={{ display: "flex", gap: 14, flexWrap: "wrap", marginTop: 14 }}>
        <div className="kpi">
          <div className="kLabel">Total Milk (Today Live)</div>
          <div className="kValue">{todayLive?.totalLitres || 2} L</div>
        </div>

        <div className="kpi">
          <div className="kLabel">Stops (Today Live)</div>
          <div className="kValue">{todayLive?.stops || 6}</div>
        </div>

        <div className="kpi">
          <div className="kLabel">Last Sync</div>
          <div className="kValue">{lastSyncedAt || "Not yet"}</div>
        </div>
      </div>

      <div style={{ marginTop: 18 }} className="glass">
        <div style={{ padding: 14 }}>
          <div style={{ fontWeight: 800, display: "flex", justifyContent: "space-between", gap: 12 }}>
            <span>Today Plan</span>
            <span style={{ fontSize: 12, opacity: 0.75 }}>
              {todayLive?.generatedAt
                ? `Generated: ${new Date(todayLive.generatedAt).toLocaleString()}`
                : "—"}
            </span>
          </div>

          

          <div style={{ marginTop: 10, opacity: 0.9, lineHeight: 1.8 }}>
            <div><b>Date:</b> {todayLive?.summaryDate ?? "—"}</div>
            <div><b>Total Milk:</b> {todayLive?.totalLitres ?? 0} L</div>
            <div><b>Stops:</b> {todayLive?.stops ?? 0}</div>
            <div><b>Paused Customers:</b> {todayLive?.pausedCount ?? 0}</div>
          </div>
        </div>
      </div>

      <div style={{ marginTop: 18 }} className="glass">
        <div style={{ padding: 14 }}>
          <div style={{ fontWeight: 900, display: "flex", justifyContent: "space-between", gap: 12 }}>
            <span>Vendor Optimization Analytics (Month-to-Date)</span>
          </div>

          {analytics ? (
            <>
              <div style={{ display: "flex", gap: 14, flexWrap: "wrap", marginTop: 12 }}>
                <div className="kpi">
                  <div className="kLabel">Delivered Milk</div>
                  <div className="kValue">{analytics?.deliveredMilkL ?? 0} L</div>
                </div>

                <div className="kpi">
                  <div className="kLabel">Milk Saved (Pauses)</div>
                  <div className="kValue">{analytics?.milkSavedL ?? 0} L</div>
                </div>
              </div>
<div style={{
  marginTop: 12,
  fontWeight: "bold",
  padding: "10px",
  borderRadius: "10px",
  background:
    analytics?.insight?.includes("🔥") ? "#ffe5e5" :
    analytics?.insight?.includes("⚠️") ? "#fff4e5" :
    "#e8f5e9",
  color:
    analytics?.insight?.includes("🔥") ? "#d32f2f" :
    analytics?.insight?.includes("⚠️") ? "#ed6c02" :
    "#2e7d32"
}}>
  {analytics?.insight?.toLowerCase().includes("normal")
  ? "No pauses today"
  : analytics?.insight || "No insight available"}
</div>
<div style={{ marginTop: 10, color: "#555" }}>
   {suggestion}
</div>
              <div style={{ fontSize: 15, fontWeight: 600, marginTop: 10 }}>
                Precomputed at:{" "}
                {analytics?.computedAt
                  ? new Date(analytics.computedAt).toLocaleString()
                  : analytics?.updatedAt
                  ? new Date(analytics.updatedAt).toLocaleString()
                  : "—"}
              </div>
            </>
          ) : (
            <div style={{ marginTop: 10, opacity: 0.7 }}>
              {loading ? "Loading..." : "No analytics yet."}
            </div>
          )}
        </div>
      </div>

<div style={{ marginTop: 18 }} className="glass">
  <div style={{ padding: 14 }}>
    <div style={{ fontWeight: 900 }}>Customers (Today)</div>

    {customers.length === 0 ? (
      <div style={{ marginTop: 10, opacity: 0.7 }}>
        No customers found.
      </div>
    ) : (
      <div style={{ marginTop: 12 }}>
        {customers.filter(c => c.customerName).map((c) => {
  return (
    <div
      key={c.id}
      className="glass"
      style={{
        padding: 12,
        borderRadius: 14,
        marginBottom: 10
      }}
    >
      <div style={{ fontWeight: 800 }}>
        {c.customerName || "Unknown"}
      </div>

      <div style={{ opacity: 0.85 }}>
        📍 {c.customerAddress || "Not Provided"}
      </div>

      <div style={{ marginTop: 6 }}>
        Qty: <b>{c.qtyLitres || 1} L</b>
      </div>

      <div style={{ marginTop: 4 }}>
        Status:{" "}
        <span
          style={{
            color: c.status === "PAUSED" ? "#ff4d4f" : "#22c55e",
            fontWeight: "bold"
          }}
        >
           {c.status || "ACTIVE"}
        </span>

     {c.pauses && c.pauses.length > 0 && (
  <div style={{ marginTop: 4, color: "#ff4d4f" }}>
    ⏸️ Paused from{" "}
    {c.pauses[0].pauseStartDate || c.pauses[0].startDate}
    {" → "}
    {c.pauses[0].pauseEndDate || c.pauses[0].endDate}
  </div>
)}
      </div>
    </div>
  );
})}
      </div>
    )}
  </div>
</div>
      <div style={{ marginTop: 14, display: "flex", justifyContent: "flex-end" }}>
        <button className="btn" onClick={() => setShowTomorrow((s) => !s)}>
          {showTomorrow ? "Hide Tomorrow Preview" : "Show Tomorrow Preview"}
        </button>
      </div>

      {showTomorrow && (
        <div style={{ marginTop: 12 }} className="glass">
          <div style={{ padding: 14 }}>
            <div style={{ fontWeight: 800 }}>Tomorrow Preview</div>

            {tomorrowStatus === "loading" && (
              <div style={{ marginTop: 10 }}>Loading...</div>
            )}

            {tomorrowStatus === "missing" && (
              <div style={{ marginTop: 10 }}>
                Tomorrow preview endpoint is not available yet.
              </div>
            )}

            {tomorrowStatus === "error" && (
              <div style={{ marginTop: 10 }}>Could not load tomorrow preview.</div>
            )}

            {tomorrowStatus === "ok" && tomorrowData && (
              <div style={{ marginTop: 10, lineHeight: 1.8 }}>
                <div><b>Date:</b> {tomorrowData?.date ?? "—"}</div>
                <div><b>Total Milk:</b> {tomorrowData?.totalMilk ?? tomorrowData?.totalLitres ?? 0} L</div>
                <div><b>Stops:</b> {tomorrowData?.stops ?? 0}</div>
                <div><b>Paused:</b> {tomorrowData?.pausedCount ?? tomorrowData?.pausedCustomers?.length ?? 0}</div>

                {Array.isArray(tomorrowData?.items) && tomorrowData.items.length > 0 && (
                  <div style={{ marginTop: 16 }}>
                    <div style={{ fontWeight: 800, marginBottom: 8 }}>Customer-wise Plan</div>
                    {tomorrowData.items.map((x) => (
                      <div
                        key={x.id}
                        className="glass"
                        style={{ padding: 12, borderRadius: 14, marginBottom: 10 }}
                      >
                        <div style={{ fontWeight: 800 }}>
                          {x.name}
                        </div>
                        <div style={{ opacity: 0.85 }}>📍 {x.address}</div>
                        <div style={{ marginTop: 6 }}>
                          Qty: <b>{x.qty} L</b> | Status: <b>{x.status ?? "ACTIVE"}</b>
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            )}
          </div>
        </div>
      )}

      <div style={{ marginTop: 18, display: "flex", gap: 10, flexWrap: "wrap" }}>
        <button className="btn" onClick={onAcknowledge} disabled={loading}>
          Acknowledge Updates (Sync)
        </button>
      </div>

      <div style={{ marginTop: 16 }} className="glass">
        <div style={{ padding: 14 }}>
          <div style={{ fontWeight: 900 }}>Smart Alerts (Quick Summary)</div>

          <div style={{ marginTop: 8 }}>
             You have <b>{unreadCount}</b> new notifications.
          </div>

        </div>
      </div>

      <div style={{ marginTop: 12 }}>
  <div style={{ fontWeight: 900 }}>Change Alerts</div>

  <div style={{ marginTop: 8, opacity: 0.85 }}>
    Total: <b>{changeAlerts?.totalChanges ?? 0}</b> | 
    Qty: <b>{changeAlerts?.qtyChanges ?? 0}</b> | 
    Pause: <b>{changeAlerts?.pauses ?? 0}</b> | 
    Resume: <b>{changeAlerts?.resumes ?? 0}</b> | 
    Extend: <b>{changeAlerts?.extendsCount ?? 0}</b>
  </div>

  {!changeAlerts?.totalChanges && (
    <div style={{ marginTop: 8, opacity: 0.8 }}>
      {loading ? "Loading..." : "No changes since last sync."}
    </div>
  )}
</div>
      </div>
    
  );
}