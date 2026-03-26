import { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { api } from "../lib/api";

interface Profile {
  id: number;
  name: string;
  email: string;
  role: string;
}

interface Delivery {
  id: number;
  trackingNumber: string;
  status: string;
  receiverName: string;
  destinationAddress: string;
  quotedPrice: number;
}

export function CustomerDashboardPage() {
  const [profile, setProfile] = useState<Profile | null>(null);
  const [deliveries, setDeliveries] = useState<Delivery[]>([]);

  useEffect(() => {
    api.get("/auth/me").then((res) => setProfile(res.data)).catch(() => setProfile(null));
    api.get("/deliveries/my").then((res) => setDeliveries(res.data)).catch(() => setDeliveries([]));
  }, []);

  const totalValue = useMemo(
    () => deliveries.reduce((sum, d) => sum + Number(d.quotedPrice || 0), 0),
    [deliveries]
  );

  const inTransit = useMemo(
    () => deliveries.filter((d) => ["BOOKED", "PICKED_UP", "IN_TRANSIT", "OUT_FOR_DELIVERY"].includes(d.status)).length,
    [deliveries]
  );

  return (
    <section>
      <div className="page-head">
        <h2>Customer Operations Board</h2>
        <p>{profile ? `Welcome back, ${profile.name}. Here's your active parcel pulse.` : "Loading your account context..."}</p>
      </div>

      <div className="card-grid">
        <article className="card metric" style={{ gridColumn: "span 4" }}>
          <span className="metric-value">{deliveries.length}</span>
          <span className="metric-label">Total Deliveries</span>
        </article>
        <article className="card metric" style={{ gridColumn: "span 4" }}>
          <span className="metric-value">{inTransit}</span>
          <span className="metric-label">Operational Deliveries</span>
        </article>
        <article className="card metric" style={{ gridColumn: "span 4" }}>
          <span className="metric-value">INR {totalValue.toFixed(2)}</span>
          <span className="metric-label">Total Booked Value</span>
        </article>

        <article className="card" style={{ gridColumn: "span 12" }}>
          <h3>Recent Deliveries</h3>
          {deliveries.length === 0 ? <p>No deliveries yet. Create your first parcel request.</p> : (
            <ul className="list">
              {deliveries.map((d) => (
                <li key={d.id} className="list-item">
                  <strong>{d.trackingNumber}</strong>
                  <span>Status: {d.status}</span>
                  <span>Receiver: {d.receiverName}</span>
                  <span>Destination: {d.destinationAddress}</span>
                  <span>Quoted: INR {Number(d.quotedPrice).toFixed(2)}</span>
                  <Link to={`/customer/track/${d.trackingNumber}`}>Open Tracking Timeline</Link>
                </li>
              ))}
            </ul>
          )}
        </article>
      </div>
    </section>
  );
}
