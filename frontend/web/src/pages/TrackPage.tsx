import { useEffect, useMemo, useState } from "react";
import { useParams } from "react-router-dom";
import { api } from "../lib/api";

interface EventItem {
  id: number;
  eventCode: string;
  description: string;
  eventTime: string;
}

export function TrackPage() {
  const { id } = useParams();
  const [events, setEvents] = useState<EventItem[]>([]);
  const [trackingInput, setTrackingInput] = useState(id ?? "");
  const [activeId, setActiveId] = useState(id ?? "");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    if (id) {
      setTrackingInput(id);
      setActiveId(id);
    }
  }, [id]);

  useEffect(() => {
    if (!activeId) {
      return;
    }
    setLoading(true);
    setError("");
    api.get(`/tracking/${activeId}/events`)
      .then((res) => setEvents(res.data))
      .catch(() => {
        setEvents([]);
        setError("No movement data found for this tracking id.");
      })
      .finally(() => setLoading(false));
  }, [activeId]);

  const latestEvent = useMemo(() => (events.length > 0 ? events[0] : null), [events]);
  const flowSteps = ["BOOKED", "PICKED_UP", "IN_TRANSIT", "OUT_FOR_DELIVERY", "DELIVERED"];
  const activeStepIndex = useMemo(() => {
    if (!latestEvent) {
      return -1;
    }
    const idx = flowSteps.indexOf(latestEvent.eventCode);
    return idx >= 0 ? idx : 0;
  }, [latestEvent]);
  const progressPercent = activeStepIndex < 0
    ? 0
    : Math.round(((activeStepIndex + 1) / flowSteps.length) * 100);

  function submitTracking() {
    const value = trackingInput.trim();
    if (!value) {
      setError("Enter a tracking number or delivery id.");
      return;
    }
    setActiveId(value);
  }

  return (
    <section>
      <div className="page-head">
        <h2>Shipment Tracking Desk</h2>
        <p>View movement scans, latest status, and delivery flow checkpoints.</p>
      </div>

      <div className="card-grid">
        <article className="card" style={{ gridColumn: "span 12" }}>
          <div className="quick-track-bar">
            <input
              value={trackingInput}
              onChange={(event) => setTrackingInput(event.target.value)}
              placeholder="Enter tracking id (example: SC1774589288808245)"
            />
            <button type="button" onClick={submitTracking}>Fetch Timeline</button>
          </div>
          <p className="muted-line">Currently tracking: {activeId || "Not selected"}</p>
        </article>

        <article className="card metric" style={{ gridColumn: "span 4" }}>
          <span className="metric-value">{events.length}</span>
          <span className="metric-label">Scan Events</span>
        </article>
        <article className="card metric" style={{ gridColumn: "span 8" }}>
          <span className="metric-value">{latestEvent ? latestEvent.eventCode : "Pending"}</span>
          <span className="metric-label">
            Latest Movement {latestEvent ? `at ${new Date(latestEvent.eventTime).toLocaleString()}` : ""}
          </span>
        </article>

        <article className="card" style={{ gridColumn: "span 12" }}>
          <h3>Shipment Progress</h3>
          <div className="progress-wrap" role="progressbar" aria-valuemin={0} aria-valuemax={100} aria-valuenow={progressPercent}>
            <div className="progress-fill" style={{ width: `${progressPercent}%` }} />
          </div>
          <div className="progress-steps">
            {flowSteps.map((step, index) => (
              <span key={step} className={index <= activeStepIndex ? "progress-step active" : "progress-step"}>{step}</span>
            ))}
          </div>
        </article>

        <article className="card" style={{ gridColumn: "span 12" }}>
          <h3>Movement Timeline</h3>
          {loading ? <p>Loading tracking timeline...</p> : null}
          {!loading && events.length === 0 ? <p>No tracking events yet.</p> : (
            <ul className="list">
              {events.map((event) => (
                <li key={event.id} className="list-item timeline-item">
                  <strong className="timeline-code">{event.eventCode}</strong>
                  <span className="timeline-description">{event.description}</span>
                  <span className="timeline-time">{new Date(event.eventTime).toLocaleString()}</span>
                </li>
              ))}
            </ul>
          )}
          {error && <p className="alert">{error}</p>}
        </article>
      </div>
    </section>
  );
}
