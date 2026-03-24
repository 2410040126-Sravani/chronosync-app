// ===============================
// ChronoSync Global Store
// ===============================

let listeners = [];

// helper: current time
function nowTime() {
  return new Date().toLocaleString();
}

// ===============================
// STORE STATE
// ===============================
export const store = {
  // pause suggestion (used in CustomerDashboard)
  suggestion: null,

  // activity timeline (used for notifications + Activity page)
  activity: [
    {
      id: "boot",
      at: nowTime(),
      type: "SYSTEM",
      title: "ChronoSync started",
      details: {},
    },
  ],
};

// ===============================
// REACT SUBSCRIBE (for AppLayout)
// ===============================
export function subscribe(listener) {
  listeners.push(listener);

  return () => {
    listeners = listeners.filter((l) => l !== listener);
  };
}

// ===============================
// SNAPSHOT (required for useSyncExternalStore)
// ===============================
export function getSnapshot() {
  return store;
}

// ===============================
// ADD ACTIVITY (LOCAL EVENTS)
// ===============================
export function addActivity(event) {
  const item = {
    id: Date.now().toString(),
    at: nowTime(),
    type: event.type || "EVENT",
    title: event.title || "Update",
    details: event.details || {},
  };

  // newest first
  store.activity = [item, ...store.activity];

  listeners.forEach((l) => l());
}

// ===============================
// SET ACTIVITY (FROM BACKEND)
// ===============================
export function setActivity(items) {
  if (!Array.isArray(items)) {
    store.activity = [];
  } else {
    // normalize backend → store format
    store.activity = items.map((e, idx) => ({
      id: e?.id ?? idx,
      at:
        e?.at || e?.timestamp || e?.time
          ? new Date(e?.at ?? e?.timestamp ?? e?.time).toLocaleString()
          : nowTime(),

      type: e?.type ?? guessType(e?.action ?? e?.message ?? ""),

      title:
        e?.meta ||
        e?.action ||
        e?.message ||
        e?.type ||
        "Update",

      details: e?.details || {},
    }));
  }

  listeners.forEach((l) => l());
}

// ===============================
// HELPER: TYPE DETECTION
// ===============================
function guessType(action = "") {
  const a = action.toLowerCase();

  if (a.includes("quantity")) return "QTY";
  if (a.includes("paused")) return "PAUSE";
  if (a.includes("resumed")) return "RESUME";
  if (a.includes("sync")) return "SYNC";
  if (a.includes("extended")) return "AUTO";

  return "EVENT";
}