import { NavLink, useLocation } from "react-router-dom";
import { useEffect, useMemo, useRef, useState, useSyncExternalStore } from "react";
import { subscribe, getSnapshot, store } from "../../mock/store";
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
  useSyncExternalStore(subscribe, getSnapshot); // ✅ re-render when store updates
  const location = useLocation();

  const [open, setOpen] = useState(false);
  const panelRef = useRef(null);

  // local “read state” (persisted)
  const [readIds, setReadIds] = useState(() => loadReadIds());

  // Map activity -> notifications (top 8)
const notifications = useMemo(() => {
  const items = store.activity || [];

  return items.slice(0, 8).map((a) => ({
    id: a.id,
    type: a.type,
     title: (a.text || "Update").replace(/\d{1,2}\/\d{1,2}\/\d{4}.*/, "").trim(),
    time: a.at,
    details: a.details,
    read: readIds.has(a.id),
  }));
}, [readIds, store.activity]);
  const unreadCount = useMemo(
    () => notifications.filter((n) => !n.read).length,
    [notifications]
  );

  // Close on outside click
  useEffect(() => {
    function handleClickOutside(e) {
      if (panelRef.current && !panelRef.current.contains(e.target)) setOpen(false);
    }
    if (open) document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, [open]);

  // Close on Escape
  useEffect(() => {
    function onKey(e) {
      if (e.key === "Escape") setOpen(false);
    }
    if (open) document.addEventListener("keydown", onKey);
    return () => document.removeEventListener("keydown", onKey);
  }, [open]);

  // Close notifications when route changes
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
    // “Clear” should not delete activity; just mark all as read + close
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

        <div className="notifWrap" ref={panelRef}>
          <button
            className="bellBtn"
            onClick={() => setOpen((v) => !v)}
            aria-label="Notifications"
            aria-expanded={open}
          >
            🔔
             </button>
            {unreadCount > 0 && <span className="badge">{unreadCount}</span>}
         

          {open && (
            <div className="notifPanel" role="dialog" aria-label="Notifications panel">
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
                  notifications.map((n) => (
                    <div
                      key={n.id}
                      className={`notifItem ${n.read ? "read" : "unread"}`}
                      onClick={() => markRead(n.id)}
                      role="button"
                      tabIndex={0}
                    >
                      <div className="notifItemTop">
                        <span className="notifItemHead">
                          {iconFor(n.type)} {n.title}
                        </span>
                        <span className="notifTime">{n.time}</span>
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