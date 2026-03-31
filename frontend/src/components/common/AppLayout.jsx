import { NavLink, useLocation } from "react-router-dom";
import { useEffect, useMemo, useRef, useState, useSyncExternalStore } from "react";
import "../../styles/layout.css";

const STORAGE_KEY = "chronosync_read_activity_ids";

function loadReadIds() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    const arr = raw ? JSON.parse(raw) : [];
    return new Set(Array.isArray(arr) ? arr : []);
  } catch {
    return new Set();
  }
}

function saveReadIds(set) {
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(Array.from(set)));
  } catch {}
}

export default function AppLayout({ children }) {
  const [notifications, setNotifications] = useState([]);
  async function loadNotifications() {
  try {
    const user = JSON.parse(localStorage.getItem("user"));
    const vendorId = user?.id;

    const resCustomers = await fetch(`http://localhost:8082/api/vendor/${vendorId}/customers`, {
      headers: { Authorization: `Bearer ${localStorage.getItem("token")}` }
    });

    const customers = await resCustomers.json();
    const customerIds = customers.map(c => c.id);

    const res = await fetch("http://localhost:8082/api/audit/vendor", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${localStorage.getItem("token")}`
      },
      body: JSON.stringify(customerIds),
    });

    const data = await res.json();

    setNotifications(data);

  } catch (e) {
    console.error(e);
  }
}
useEffect(() => {
  loadNotifications();

  const interval = setInterval(loadNotifications, 10000);
  return () => clearInterval(interval);
}, []);
  
  
  const location = useLocation();

  const [open, setOpen] = useState(false);
  const panelRef = useRef(null);
  const rect = panelRef.current?.getBoundingClientRect();
  const [readIds, setReadIds] = useState(() => loadReadIds());

 
  
  const unreadCount = useMemo(
    () => notifications.filter((n) => !n.read).length,
    [notifications]
  );

  useEffect(() => {
    function handleClickOutside(e) {
      if (panelRef.current && !panelRef.current.contains(e.target)) setOpen(false);
    }
    if (open) document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, [open]);

  useEffect(() => {
    function onKey(e) {
      if (e.key === "Escape") setOpen(false);
    }
    if (open) document.addEventListener("keydown", onKey);
    return () => document.removeEventListener("keydown", onKey);
  }, [open]);

  useEffect(() => {
    setOpen(false);
  }, [location.pathname]);

  function markRead(id) {
    setReadIds((prev) => {
      const next = new Set(prev);
      next.add(id);
      saveReadIds(next);
      return next;
    });
  }

  function markAllRead() {
    setReadIds((prev) => {
      const next = new Set(prev);
      notifications.forEach((n) => next.add(n.id));
      saveReadIds(next);
      return next;
    });
  }

  function clearUnreadOnly() {
    markAllRead();
    setOpen(false);
  }

  function iconFor(type) {
    if (type === "QTY") return "🥛";
    if (type === "PAUSE") return "⏸️";
    if (type === "RESUME") return "▶️";
    if (type === "SYNC") return "🔄";
    return "ℹ️";
  }

  return (
    <div className="appContainer">
      <div className="appHeader glass">
        <div className="logo appTitle">
          CHRONOSYNC
          <div className="tagline">Smart Milk Delivery System</div>
        </div>

        <div style={{ display: 'flex', alignItems: 'center', gap: '15px' }}>
  <div className="notifWrap">
  <button
    className="bellBtn"
    ref={panelRef}   // ✅ MOVE ref HERE
    onClick={() => setOpen((v) => !v)}
  >
    🔔
  </button>
            {unreadCount > 0 && <span className="badge">{unreadCount}</span>}

            {open && (
         <div
  className="notifPanel"
  style={{
    position: "fixed",
    top: rect?.bottom + 10,
    left: rect?.left + rect?.width / 2,
    transform: "translateX(-50%)",
  }}
>
                <div className="notifHeader">
                  <div className="notifTitle">Smart Alerts</div>
                  <div className="notifHeaderActions">
                    <button className="miniBtn ghost" onClick={markAllRead}>
                      Mark read
                    </button>
                    <button className="miniBtn ghost" onClick={clearUnreadOnly}>
                      Close
                    </button>
                  </div>
                </div>

                <div className="notifList">
                  {notifications.length === 0 ? (
                    <div className="notifEmpty">No activity yet.</div>
                  ) : (
                    notifications.map((n,i) => (
                      <div
                        key={i}
                        className={"notifItem"}
                        onClick={() => markRead(n.id)}
                        role="button"
                        tabIndex={0}
                      >
                        <div className="notifItemTop">
                          <span className="notifItemHead">
  {iconFor(n.type)} {n.meta}
</span>
<span className="notifTime">
  {new Date(n.at).toLocaleString()}
</span>
                        </div>

                        {n.details && Object.keys(n.details).length > 0 && (
                          <div className="notifMsg">
                            {Object.entries(n.details)
                              .slice(0, 2)
                              .map(([k, v]) => `${k}: ${String(v)}`)
                              .join(" • ")}
                          </div>
                        )}
                      </div>
                    ))
                  )}
                </div>

                <div className="notifFooter">
                  <button className="notifClearBtn" onClick={() => setOpen(false)}>
                    Done
                  </button>
                </div>
              </div>
            )}
          </div>

          {/* LOGOUT BUTTON */}
          <button 
            onClick={() => {
              localStorage.removeItem('token');
              localStorage.removeItem('user');
              window.location.href = '/login';
            }}
            style={{
              background: 'transparent',
              border: 'none',
              cursor: 'pointer',
              fontSize: '16px',
              padding: '8px 12px',
              borderRadius: '8px',
              display: 'flex',
              alignItems: 'center',
              gap: '5px',
              color: '#666',
              fontWeight: '500'
            }}
            title="Logout"
          >
            🚪 Logout
          </button>
        </div>
      </div>

      <div className="appTabs glass">
        <NavLink to="/customer" className={({ isActive }) => `tab ${isActive ? "active" : ""}`}>
          Customer
        </NavLink>
        <NavLink to="/manage" className={({ isActive }) => `tab ${isActive ? "active" : ""}`}>
          Manage
        </NavLink>
        <NavLink to="/activity" className={({ isActive }) => `tab ${isActive ? "active" : ""}`}>
          Activity
        </NavLink>
        <NavLink to="/vendor" className={({ isActive }) => `tab ${isActive ? "active" : ""}`}>
          Vendor
        </NavLink>
      </div>

      <div className="appContent">{children}</div>
    </div>
  );
}