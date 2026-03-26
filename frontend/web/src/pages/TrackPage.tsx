import { useEffect, useState } from "react";
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

  useEffect(() => {
    if (!id) {
      return;
    }
    api.get(`/tracking/${id}/events`).then((res) => setEvents(res.data)).catch(() => setEvents([]));
  }, [id]);

  return (
    <section>
      <div className="page-head">
        <h2>Tracking Timeline</h2>
        <p>Tracking Number: {id}</p>
      </div>

      <div className="card-grid">
        <article className="card" style={{ gridColumn: "span 12" }}>
          {events.length === 0 ? <p>No tracking events yet.</p> : (
            <ul className="list">
              {events.map((event) => (
                <li key={event.id} className="list-item">
                  <strong>{event.eventCode}</strong>
                  <span>{event.description}</span>
                  <span>{new Date(event.eventTime).toLocaleString()}</span>
                </li>
              ))}
            </ul>
          )}
        </article>
      </div>
    </section>
  );
}
