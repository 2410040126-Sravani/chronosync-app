import { useEffect, useMemo, useState } from "react";
import "../styles/Activity.css";
import { getAudit } from "../api/auditApi";

function readJsonSafe(res) {
  if (!res.ok) throw new Error(`HTTP ${res.status}`);
  return res.text().then((t) => (t ? JSON.parse(t) : []));
}

function getEventText(item) {
  if (!item) return "";
  if (typeof item === "string") return item;
  return item.action || item.message || item.description || "";
}

function getTimestamp(item, fallbackText = "") {
  if (item && typeof item === "object") {
    return item.createdAt || item.timestamp || item.time || "";
  }
  const parts = fallbackText.split("—");
  return parts.length > 1 ? parts[parts.length - 1].trim() : "";
}

function getCleanMessage(text) {
  const parts = text.split("—");
  return parts[0]?.trim() || text;
}

function getType(text) {
  const t = text.toLowerCase();
  if (t.includes("quantity changed")) return "QTY_CHANGE";
  if (t.includes("subscription paused")) return "PAUSE";
  if (t.includes("subscription resumed")) return "RESUME";
  if (t.includes("auto-extended")) return "PAUSE";
  return text;
}

export default function Activity() {
  const customerId = 1;
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [showAll, setShowAll] = useState(false);

  async function loadActivity(isRefresh = false) {
    try {
      setError("");
     setLoading(true);

     const data = await getAudit(customerId);
      setItems(Array.isArray(data) ? data : []);
    } catch (e) {
      setError(e.message || "Failed to load activity");
      setItems([]);
    } finally {
      setLoading(false);
      
    }
  }

  useEffect(() => {
    loadActivity();
  }, []);

  
   const events = useMemo(() => {
 const mapped = items.map((item, index) => {
  const rawText = getEventText(item);

  return {
    id: item?.id ?? `${index}`,
    type: getType(rawText),

    
    message: rawText || item?.meta || item?.type || "No message",

    timestamp: getTimestamp(item, rawText),
  };
});

  return showAll ? mapped : mapped.slice(0, 20);
}, [items, showAll]);

  return (
    <div className="pageCard glass activityCardWrap">
      <h2 className="activityHeading">Activity Timeline</h2>
      <p className="activitySubtext">
        Shows your subscription actions (qty change / pause / resume / auto-extend).
      </p>

      <div className="activityToolbar">
        <button
  className="btn activityRefreshBtn"
  onClick={loadActivity}
>
  Refresh
</button>

        <span className="activityCount">{items.length} event(s)</span>
      </div>

      {loading ? (
        <div className="activityEmpty">Loading activity...</div>
      ) : error ? (
        <div className="activityEmpty">{error}</div>
      ) : events.length === 0 ? (
        <div className="activityEmpty">No activity yet.</div>
      ) : (
        <>
          <div className="activityList">
            {events.map((event) => (
              <article className="activityItem" key={event.id}>
                <div className="activityItemTop">
                  <span className="activityType">{event.type.length > 20 ? "EVENT" : event.type}</span>
                  <span className="activityTime">{event.timestamp}</span>
                </div>
                <div className="activityMessage">{event.message}</div>
              </article>
            ))}
          </div>

          {items.length > 20 && (
            <div className="activityMoreWrap">
              <button
                className="miniBtn activityMoreBtn"
                onClick={() => setShowAll((prev) => !prev)}
              >
                {showAll ? "Show Less" : "Show More"}
              </button>
            </div>
          )}
        </>
      )}
    </div>
  );
}