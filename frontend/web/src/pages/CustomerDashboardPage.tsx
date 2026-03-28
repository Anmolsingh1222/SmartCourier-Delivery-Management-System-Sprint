import { useEffect, useMemo, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { motion } from "framer-motion";
import { api } from "../lib/api";

interface Profile { id: number; name: string; email: string; role: string; }
interface Delivery {
  id: number; trackingNumber: string; status: string;
  receiverName: string; destinationAddress: string;
  quotedPrice: number; serviceType: string; createdAt: string;
}

const STATUS_META: Record<string, { label: string; cls: string }> = {
  DRAFT:            { label: "Draft",            cls: "badge-draft" },
  BOOKED:           { label: "Booked",           cls: "badge-booked" },
  PICKED_UP:        { label: "Picked Up",        cls: "badge-transit" },
  IN_TRANSIT:       { label: "In Transit",       cls: "badge-transit" },
  OUT_FOR_DELIVERY: { label: "Out for Delivery", cls: "badge-ofd" },
  DELIVERED:        { label: "Delivered",        cls: "badge-delivered" },
  CANCELLED:        { label: "Cancelled",        cls: "badge-cancelled" },
};

function StatusBadge({ status }: { status: string }) {
  const meta = STATUS_META[status] ?? { label: status, cls: "badge-draft" };
  return <span className={`status-badge ${meta.cls}`}>{meta.label}</span>;
}

export function CustomerDashboardPage() {
  const [profile, setProfile] = useState<Profile | null>(null);
  const [deliveries, setDeliveries] = useState<Delivery[]>([]);
  const [statusFilter, setStatusFilter] = useState("ALL");
  const [query, setQuery] = useState("");
  const navigate = useNavigate();

  useEffect(() => {
    api.get("/auth/me").then((res) => setProfile(res.data)).catch(() => {});
    api.get("/deliveries/my").then((res) => setDeliveries(res.data ?? [])).catch(() => {});
  }, []);

  const totalValue = useMemo(() => deliveries.reduce((s, d) => s + Number(d.quotedPrice || 0), 0), [deliveries]);
  const inTransit = useMemo(() => deliveries.filter((d) => ["BOOKED","PICKED_UP","IN_TRANSIT","OUT_FOR_DELIVERY"].includes(d.status)).length, [deliveries]);
  const delivered = useMemo(() => deliveries.filter((d) => d.status === "DELIVERED").length, [deliveries]);

  const statusOptions = useMemo(() => ["ALL", ...Array.from(new Set(deliveries.map((d) => d.status)))], [deliveries]);

  const filtered = useMemo(() => {
    const q = query.trim().toLowerCase();
    return deliveries.filter((d) => {
      const sm = statusFilter === "ALL" || d.status === statusFilter;
      const qm = !q || d.trackingNumber.toLowerCase().includes(q) || d.receiverName.toLowerCase().includes(q) || d.destinationAddress.toLowerCase().includes(q);
      return sm && qm;
    });
  }, [deliveries, query, statusFilter]);

  return (
    <section>
      <div className="page-head">
        <h2>My Shipments</h2>
        <p>{profile ? `Welcome back, ${profile.name}.` : "Loading..."}</p>
      </div>

      <div className="card-grid">
        <article className="card metric" style={{ gridColumn: "span 3" }}>
          <span className="metric-value">{deliveries.length}</span>
          <span className="metric-label">Total Shipments</span>
        </article>
        <article className="card metric" style={{ gridColumn: "span 3" }}>
          <span className="metric-value">{inTransit}</span>
          <span className="metric-label">In Transit</span>
        </article>
        <article className="card metric" style={{ gridColumn: "span 3" }}>
          <span className="metric-value">{delivered}</span>
          <span className="metric-label">Delivered</span>
        </article>
        <article className="card metric" style={{ gridColumn: "span 3" }}>
          <span className="metric-value pay-amount">INR {totalValue.toFixed(0)}</span>
          <span className="metric-label">Total Value</span>
        </article>

        <article className="card" style={{ gridColumn: "span 12" }}>
          <div className="toolbar">
            <input value={query} onChange={(e) => setQuery(e.target.value)} placeholder="Search tracking, receiver, destination..." />
            <select value={statusFilter} onChange={(e) => setStatusFilter(e.target.value)}>
              {statusOptions.map((s) => <option key={s} value={s}>{s}</option>)}
            </select>
          </div>
        </article>

        <article className="card" style={{ gridColumn: "span 12" }}>
          <div className="dash-header-row">
            <h3>Shipments ({filtered.length})</h3>
            <button type="button" className="btn-primary-sm" onClick={() => navigate("/customer/deliveries/new")}>+ New Shipment</button>
          </div>
          {filtered.length === 0 ? (
            <div className="empty-state">
              <span className="empty-icon">📭</span>
              <p>No shipments found. Create your first delivery.</p>
              <button type="button" onClick={() => navigate("/customer/deliveries/new")}>Book a Shipment</button>
            </div>
          ) : (
            <div className="delivery-cards">
              {filtered.map((d, i) => (
                <motion.div
                  key={d.id}
                  className="delivery-card"
                  initial={{ opacity: 0, y: 20 }}
                  animate={{ opacity: 1, y: 0 }}
                  transition={{ delay: i * 0.05, duration: 0.3 }}
                >
                  <div className="delivery-card-top">
                    <div>
                      <span className="delivery-tracking">{d.trackingNumber}</span>
                      <StatusBadge status={d.status} />
                    </div>
                    <span className="delivery-price">INR {Number(d.quotedPrice).toFixed(2)}</span>
                  </div>
                  <div className="delivery-card-body">
                    <div className="delivery-info-row">
                      <span className="delivery-info-label">To</span>
                      <span>{d.receiverName} · {d.destinationAddress}</span>
                    </div>
                    <div className="delivery-info-row">
                      <span className="delivery-info-label">Service</span>
                      <span>{d.serviceType}</span>
                    </div>
                  </div>
                  <div className="delivery-card-actions">
                    <Link to={`/customer/track/${d.trackingNumber}`} className="btn-link">Track</Link>
                    {d.status === "DRAFT" && (
                      <button
                        type="button"
                        className="btn-pay-sm"
                        onClick={() => navigate("/customer/payment", {
                          state: {
                            deliveryId: d.id,
                            trackingNumber: d.trackingNumber,
                            amount: Number(d.quotedPrice),
                            receiverName: d.receiverName,
                            serviceType: d.serviceType,
                          }
                        })}
                      >
                        Pay Now
                      </button>
                    )}
                  </div>
                </motion.div>
              ))}
            </div>
          )}
        </article>
      </div>
    </section>
  );
}
