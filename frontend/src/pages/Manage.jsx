import { useEffect, useMemo, useState } from "react";
import {
  getSubscription,
  updateQty,
  pauseSubscription,
  resumeSubscription,
  getPauseSuggestion,
  
} from "../api/subscriptionApi";

export default function Manage() {
  const subId = 1; // later we’ll make dropdown (no hardcode)

  const today = useMemo(() => {
  const d = new Date();
  d.setMinutes(d.getMinutes() - d.getTimezoneOffset());
  return d.toISOString().slice(0, 10);
}, []);

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const [sub, setSub] = useState(null);

  const [start, setStart] = useState("");
  const [end, setEnd] = useState("");

  const [suggestion, setSuggestion] = useState(null);
  const [suggestionDismissed, setSuggestionDismissed] = useState(false);

  // ---------- helpers: map backend fields safely ----------
  const qty = sub?.qty ?? sub?.qtyLitres ?? sub?.quantity ?? 0;
  const status = sub?.status ?? "—";
  const nextDelivery = sub?.nextDelivery ?? sub?.nextDeliveryDate ?? "—";
 const pauses =
    sub?.pauses ??
    sub?.pauseWindows ??
    sub?.pauseRanges ??
    [];
  const isPausedActive = pauses.some((p) => {
  const start = p.startDate || p.pauseStartDate;
  const end = p.endDate || p.pauseEndDate;

  return start <= today && end >= today;
});
  const endDate = sub?.endDate ?? "—";
  const effectiveEndDate = sub?.effectiveEndDate ?? sub?.effectiveEndDateISO ?? endDate;

 
    const activePause = pauses.find((p) => {
  const end = p.endDate || p.pauseEndDate;
  return end >= today;
});

  async function loadAll() {
    try {
      setError("");
      setLoading(true);

      const [s, sug] = await Promise.all([
        getSubscription(subId),
        getPauseSuggestion(subId),
      ]);

      setSub(s);

      // show suggestion only if OPEN and not dismissed
      if (!suggestionDismissed && sug?.hasSuggestion) setSuggestion(sug);
else setSuggestion(null);
    } catch (e) {
      setError(e?.message || "Something went wrong");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadAll();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  async function onQtyChange(newQty) {
    if (newQty < 1) return;
    try {
      setError("");
      setLoading(true);
      await updateQty(subId, newQty);
      const s = await getSubscription(subId);
      setSub(s);
    } catch (e) {
      setError(e?.message || "Qty update failed");
    } finally {
      setLoading(false);
    }
  }

  async function addPause() {
    if (!start || !end) return;

    // block past dates
    if (start < today || end < today) {
      alert("Only today/future dates are allowed ✅");
      return;
    }
    if (end < start) {
      alert("End date must be after start date ✅");
      return;
    }

    try {
      setError("");
      setLoading(true);
      await pauseSubscription(subId, start, end);
      setStart("");
      setEnd("");
      await loadAll();
    } catch (e) {
      setError(e?.message || "Pause failed");
      setLoading(false);
    }
  }

  async function onResume() {
    try {
      setError("");
      setLoading(true);
      await resumeSubscription(subId);
      await loadAll();
    } catch (e) {
      setError(e?.message || "Resume failed");
      setLoading(false);
    }
  }

  // If your backend doesn't have "clear pauses", we treat it as "resume + reload"
  async function onClearPauses() {
    await onResume();
  }

  

  async function acceptSuggestion() {
    try {
      setError("");
      setLoading(true);

      // try to use suggestion dates if present
      const s = suggestion || {};
      const from = s.startDate || s.fromDate || today;
      const to = s.endDate || s.toDate || from;

      await pauseSubscription(subId, from, to);
      setSuggestion(null);
      setSuggestionDismissed(true);
      await loadAll();
    } catch (e) {
      setError(e?.message || "Accept suggestion failed");
      setLoading(false);
    }
  }

  function dismissSuggestion() {
    setSuggestionDismissed(true);
    setSuggestion(null);
  }

  return (
    <div className="glass pageCard">
      <h3 style={{ marginTop: 0 }}>Manage Subscription</h3>
      <p style={{ opacity: 0.8, marginTop: 6 }}>
        Pause-aware automation: paused days extend your effective end date.
      </p>

      {error && (
        <div className="glass" style={{ padding: 12, borderRadius: 14, marginTop: 12 }}>
          <b>Error:</b> <span style={{ opacity: 0.85 }}>{error}</span>
        </div>
      )}

      <div style={{ display: "flex", gap: 12, flexWrap: "wrap", marginTop: 14 }}>
        <div className="glass" style={{ padding: 12, borderRadius: 14 }}>
          <div style={{ fontSize: 12, opacity: 0.7 }}>Quantity</div>
          <div style={{ fontWeight: 900, fontSize: 18 }}>{qty} L</div>
          <div style={{ display: "flex", gap: 8, marginTop: 8 }}>
            <button className="miniBtn" disabled={loading || qty <= 1} onClick={() => onQtyChange(qty - 1)}>
              -
            </button>
            <button className="miniBtn" disabled={loading} onClick={() => onQtyChange(qty + 1)}>
              +
            </button>
          </div>
        </div>

        <div className="glass" style={{ padding: 12, borderRadius: 14 }}>
          <div style={{ fontSize: 12, opacity: 0.7 }}>Status</div>
          <div style={{ fontWeight: 900, fontSize: 18 }}>
  {sub?.status ?? (isPausedActive ? "PAUSED" : "ACTIVE")}
</div>

<div style={{ fontSize: 12, opacity: 0.7, marginTop: 6 }}>
  {activePause
    ? `${activePause.startDate || activePause.pauseStartDate} → ${activePause.endDate || activePause.pauseEndDate}`
    : "No active pause"}
</div>
        </div>

        <div className="glass" style={{ padding: 12, borderRadius: 14 }}>
          <div style={{ fontSize: 12, opacity: 0.7 }}>Effective End Date</div>
          <div style={{ fontWeight: 900, fontSize: 18 }}>{effectiveEndDate}</div>
          <div style={{ fontSize: 12, opacity: 0.7, marginTop: 6 }}>
            Original: {endDate}
          </div>

          
        </div>
      </div>

      <div style={{ display: "flex", gap: 12, flexWrap: "wrap", marginTop: 18 }}>
        <div>
          <div style={{ fontSize: 12, opacity: 0.7, marginBottom: 6 }}>Pause start</div>
          <input
            className="input"
            type="date"
            min={today}
            value={start}
            onChange={(e) => {
              const v = e.target.value;
              setStart(v);
              if (end && v && end < v) setEnd("");
            }}
            onClick={(e) => e.currentTarget.showPicker?.()}
          />
        </div>

        <div>
          <div style={{ fontSize: 12, opacity: 0.7, marginBottom: 6 }}>Pause end</div>
          <input
            className="input"
            type="date"
            min={start || today}
            value={end}
            onChange={(e) => setEnd(e.target.value)}
            onClick={(e) => e.currentTarget.showPicker?.()}
          />
        </div>

        <div style={{ alignSelf: "end", display: "flex", gap: 10, flexWrap: "wrap" }}>
          <button className="btn" disabled={loading} onClick={addPause}>
            Add Pause
          </button>
          <button className="btn" disabled={loading} onClick={onResume}>
            Resume
          </button>
          <button className="btn" disabled={loading} onClick={onClearPauses}>
            Clear Pauses
          </button>
        </div>
      </div>

     <div style={{ marginTop: 18 }}>
  <div style={{ fontWeight: 700 }}>Current Pause</div>
{pauses.filter(p => {
  const endDate = p.endDate || p.pauseEndDate;
  return endDate >= today; // 🔥 hide past pauses
}).length > 0 ? (

  pauses
    .filter(p => {
      const endDate = p.endDate || p.pauseEndDate;
      return endDate >= today;
    })
    .map((p, i) => (
      <div key={i} style={{ marginTop: 6, opacity: 0.9 }}>
        {p.startDate || p.pauseStartDate} → {p.endDate || p.pauseEndDate}
      </div>
    ))

) : (
  <p style={{ opacity: 0.7 }}>
    {loading ? "Loading..." : "No active pause."}
  </p>
)}
   
</div>

            {suggestion?.hasSuggestion && (
        <div className="glass" style={{ padding: 12, borderRadius: 14, marginTop: 14 }}>
          <div style={{ fontWeight: 900 }}>Pause suggestion</div>

          <div style={{ opacity: 0.8, marginTop: 6 }}>
            {suggestion.suggestion ?? "—"}
          </div>

          <div style={{ fontSize: 12, opacity: 0.7, marginTop: 6 }}>
            {suggestion.reason && <span>{suggestion.reason}</span>}

            {suggestion.suggestedDate && (
              <span>{suggestion.reason ? " • " : ""}Date: {suggestion.suggestedDate}</span>
            )}

            {suggestion.confidence != null && (
              <span>
                {(suggestion.reason || suggestion.suggestedDate) ? " • " : ""}
                Confidence: {suggestion.confidence}
              </span>
            )}
          </div>

          <div style={{ display: "flex", gap: 10, marginTop: 10, flexWrap: "wrap" }}>
            <button className="btn" disabled={loading} onClick={acceptSuggestion}>
              Accept & Pause
            </button>
            <button className="btn" disabled={loading} onClick={dismissSuggestion}>
              Dismiss
            </button>
          </div>
        </div>
      )}
       <div style={{ marginTop: 18, fontSize: 12, opacity: 0.75 }}>
        Vendor change alerts are updated when the vendor clicks{" "}
        <b>Acknowledge Updates (Sync)</b> on the Vendor Dashboard.
      </div>
    </div>
  );
}