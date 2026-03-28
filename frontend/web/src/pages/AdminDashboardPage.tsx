import { useEffect, useMemo, useState } from "react";
import axios from "axios";
import { motion, AnimatePresence } from "framer-motion";
import { api } from "../lib/api";

interface AdminDelivery {
  id: number;
  trackingNumber: string;
  customerId: number;
  status: string;
  receiverName: string;
  destinationAddress: string;
  quotedPrice: number;
  serviceType: string;
  updatedAt: string;
}

interface DeliveryException {
  id: number;
  deliveryId: number;
  status: string;
  reason: string;
  resolved: boolean;
  createdAt: string;
}

interface Reports { totalExceptions: number; resolved: number; open: number; }
interface ZoneSnapshot { zone: string; count: number; intensity: "low" | "medium" | "high"; }

const STATUS_TRANSITIONS: Record<string, { label: string; endpoint: string; color: string }[]> = {
  DRAFT:            [{ label: "Force Book",            endpoint: "book",           color: "#6366f1" }],
  BOOKED:           [{ label: "Mark Picked Up",        endpoint: "picked-up",      color: "#f97316" }],
  PICKED_UP:        [{ label: "Mark In Transit",       endpoint: "in-transit",     color: "#eab308" }],
  IN_TRANSIT:       [{ label: "Mark Out for Delivery", endpoint: "out-for-delivery", color: "#3b82f6" }],
  OUT_FOR_DELIVERY: [{ label: "Mark Delivered",        endpoint: "delivered",      color: "#22c55e" }],
};

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
  const m = STATUS_META[status] ?? { label: status, cls: "badge-draft" };
  return <span className={`status-badge ${m.cls}`}>{m.label}</span>;
}

function resolveZone(addr: string) {
  const v = addr.toLowerCase();
  if (/(delhi|gurgaon|noida|ghaziabad)/.test(v)) return "North Hub";
  if (/(punjab|jalandhar|ludhiana|amritsar|chandigarh)/.test(v)) return "Punjab Cluster";
  if (/(mumbai|pune|thane)/.test(v)) return "West Metro";
  if (/(bengaluru|bangalore|hyderabad|chennai)/.test(v)) return "South Corridor";
  if (/(kolkata|patna|guwahati)/.test(v)) return "East Corridor";
  return "National General";
}

function formatDate(v?: string) {
  return v ? new Date(v).toLocaleString() : "-";
}

export function AdminDashboardPage() {
  const [deliveries, setDeliveries] = useState<AdminDelivery[]>([]);
  const [exceptions, setExceptions] = useState<DeliveryException[]>([]);
  const [reports, setReports] = useState<Reports | null>(null);
  const [error, setError] = useState("");
  const [refreshKey, setRefreshKey] = useState(0);
  const [statusFilter, setStatusFilter] = useState("ALL");
  const [query, setQuery] = useState("");
  const [actionLoading, setActionLoading] = useState<number | null>(null);
  const [actionMsg, setActionMsg] = useState<{ id: number; msg: string; ok: boolean } | null>(null);
  const [selectedDelivery, setSelectedDelivery] = useState<AdminDelivery | null>(null);
  const [exceptionForm, setExceptionForm] = useState<{ deliveryId: number; status: string; reason: string } | null>(null);
  const [resolveForm, setResolveForm] = useState<{ exceptionId: number; resolution: string } | null>(null);

  useEffect(() => {
    setError("");
    api.get("/admin/deliveries/customer")
      .then((res) => {
        const sorted = (res.data ?? []).slice().sort((a: AdminDelivery, b: AdminDelivery) =>
          new Date(b.updatedAt).getTime() - new Date(a.updatedAt).getTime());
        setDeliveries(sorted);
      })
      .catch((err) => {
        if (axios.isAxiosError(err) && err.response?.status === 403)
          setError("Access denied: please login with ADMIN account.");
        else setError("Failed to load deliveries.");
      });
    api.get("/admin/deliveries").then((res) => setExceptions(res.data ?? [])).catch(() => {});
    api.get("/admin/reports").then((res) => setReports(res.data ?? null)).catch(() => {});
  }, [refreshKey]);

  async function transitionStatus(delivery: AdminDelivery, endpoint: string) {
    setActionLoading(delivery.id);
    setActionMsg(null);
    try {
      if (endpoint === "book") {
        await api.post(`/deliveries/${delivery.id}/book`);
      } else {
        await api.post(`/deliveries/${delivery.id}/status/${endpoint}`);
      }
      setActionMsg({ id: delivery.id, msg: "Updated", ok: true });
      setRefreshKey((k) => k + 1);
    } catch {
      setActionMsg({ id: delivery.id, msg: "Failed", ok: false });
    } finally {
      setActionLoading(null);
    }
  }

  async function submitException() {
    if (!exceptionForm) return;
    try {
      await api.post(`/admin/deliveries/${exceptionForm.deliveryId}/exception`, {
        deliveryId: exceptionForm.deliveryId,
        status: exceptionForm.status,
        reason: exceptionForm.reason,
      });
      setExceptionForm(null);
      setRefreshKey((k) => k + 1);
    } catch { alert("Failed to create exception."); }
  }

  async function submitResolve() {
    if (!resolveForm) return;
    try {
      await api.put(`/admin/deliveries/${resolveForm.exceptionId}/resolve`, {
        resolution: resolveForm.resolution,
      });
      setResolveForm(null);
      setRefreshKey((k) => k + 1);
    } catch { alert("Failed to resolve exception."); }
  }

  const totalValue = useMemo(() => deliveries.reduce((s, d) => s + Number(d.quotedPrice || 0), 0), [deliveries]);
  const openExceptions = reports?.open ?? exceptions.filter((e) => !e.resolved).length;
  const statusOptions = useMemo(() => ["ALL", ...Array.from(new Set(deliveries.map((d) => d.status)))], [deliveries]);

  const filtered = useMemo(() => {
    const q = query.trim().toLowerCase();
    return deliveries.filter((d) => {
      const sm = statusFilter === "ALL" || d.status === statusFilter;
      const qm = !q || d.trackingNumber.toLowerCase().includes(q)
        || d.receiverName.toLowerCase().includes(q)
        || d.destinationAddress.toLowerCase().includes(q)
        || String(d.customerId).includes(q);
      return sm && qm;
    });
  }, [deliveries, query, statusFilter]);

  const zoneHeatmap = useMemo<ZoneSnapshot[]>(() => {
    const map = new Map<string, number>();
    deliveries.forEach((d) => map.set(resolveZone(d.destinationAddress), (map.get(resolveZone(d.destinationAddress)) ?? 0) + 1));
    return Array.from(map.entries()).map(([zone, count]) => ({
      zone, count,
      intensity: (count >= 4 ? "high" : count >= 2 ? "medium" : "low") as ZoneSnapshot["intensity"],
    })).sort((a, b) => b.count - a.count);
  }, [deliveries]);

  const slaBreaches = useMemo(() => {
    const now = Date.now();
    return deliveries
      .filter((d) => !["DELIVERED", "CANCELLED"].includes(d.status))
      .map((d) => ({ ...d, ageHours: Math.max(0, Math.floor((now - new Date(d.updatedAt).getTime()) / 3600000)) }))
      .filter((d) => d.ageHours >= 24)
      .sort((a, b) => b.ageHours - a.ageHours);
  }, [deliveries]);

  return (
    <section>
      <div className="page-head">
        <div style={{ display: "flex", justifyContent: "space-between", alignItems: "flex-start" }}>
          <div>
            <h2>Admin Control Center</h2>
            <p>Manage deliveries, update statuses, handle exceptions and monitor operations.</p>
          </div>
          <button type="button" className="btn-primary-sm" onClick={() => setRefreshKey((k) => k + 1)}>↻ Refresh</button>
        </div>
      </div>

      <div className="card-grid">
        {/* Metrics */}
        {[
          { value: deliveries.length, label: "Total Deliveries" },
          { value: openExceptions, label: "Open Exceptions" },
          { value: `INR ${totalValue.toFixed(0)}`, label: "Book Value" },
          { value: slaBreaches.length, label: "SLA Breaches" },
        ].map((m, i) => (
          <motion.article key={i} className="card metric" style={{ gridColumn: "span 3" }}
            initial={{ opacity: 0, y: 16 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: i * 0.07 }}>
            <span className="metric-value">{m.value}</span>
            <span className="metric-label">{m.label}</span>
          </motion.article>
        ))}

        {/* Search & Filter */}
        <article className="card" style={{ gridColumn: "span 12" }}>
          <div className="toolbar">
            <input value={query} onChange={(e) => setQuery(e.target.value)} placeholder="Search tracking, receiver, destination, customer ID..." />
            <select value={statusFilter} onChange={(e) => setStatusFilter(e.target.value)}>
              {statusOptions.map((s) => <option key={s} value={s}>{s}</option>)}
            </select>
          </div>
          <p className="muted-line">Showing {filtered.length} of {deliveries.length} deliveries</p>
        </article>

        {/* Heatmap + SLA */}
        <article className="card" style={{ gridColumn: "span 6" }}>
          <h3>Zone Heatmap</h3>
          {zoneHeatmap.length === 0 ? <p className="muted-line">No data yet.</p> : (
            <div className="heatmap-grid">
              {zoneHeatmap.map((z) => (
                <div key={z.zone} className={`heatmap-cell ${z.intensity}`}>
                  <strong>{z.zone}</strong><span>{z.count} deliveries</span>
                </div>
              ))}
            </div>
          )}
        </article>
        <article className="card" style={{ gridColumn: "span 6" }}>
          <h3>SLA Breach Alerts</h3>
          {slaBreaches.length === 0 ? <p className="muted-line">All deliveries within SLA.</p> : (
            <ul className="sla-list">
              {slaBreaches.slice(0, 6).map((d) => (
                <li key={d.id} className="sla-item">
                  <div><strong>{d.trackingNumber}</strong><span>{d.receiverName} · {d.destinationAddress}</span></div>
                  <span className="breach-age">{d.ageHours}h</span>
                </li>
              ))}
            </ul>
          )}
        </article>

        {/* Delivery Management Table */}
        <article className="card" style={{ gridColumn: "span 12" }}>
          <h3>Delivery Management</h3>
          {filtered.length === 0 ? (
            <div className="empty-state"><span className="empty-icon">📭</span><p>No deliveries found.</p></div>
          ) : (
            <div className="admin-delivery-table">
              <div className="admin-table-head">
                <span>Tracking</span><span>Customer</span><span>Receiver</span>
                <span>Status</span><span>Price</span><span>Updated</span><span>Actions</span>
              </div>
              {filtered.slice(0, 30).map((d) => {
                const transitions = STATUS_TRANSITIONS[d.status] ?? [];
                const isLoading = actionLoading === d.id;
                const msg = actionMsg?.id === d.id ? actionMsg : null;
                return (
                  <motion.div key={d.id} className="admin-table-row"
                    initial={{ opacity: 0 }} animate={{ opacity: 1 }}>
                    <span className="mono-text">{d.trackingNumber}</span>
                    <span>#{d.customerId}</span>
                    <span>{d.receiverName}</span>
                    <span><StatusBadge status={d.status} /></span>
                    <span className="pay-amount">INR {Number(d.quotedPrice).toFixed(0)}</span>
                    <span className="muted-line" style={{ fontSize: "0.78rem" }}>{formatDate(d.updatedAt)}</span>
                    <div className="admin-actions">
                      {transitions.map((t) => (
                        <motion.button key={t.endpoint} type="button"
                          className="admin-action-btn"
                          style={{ background: t.color }}
                          disabled={isLoading}
                          whileHover={{ scale: 1.04 }} whileTap={{ scale: 0.96 }}
                          onClick={() => transitionStatus(d, t.endpoint)}>
                          {isLoading ? "..." : t.label}
                        </motion.button>
                      ))}
                      <button type="button" className="admin-action-btn-ghost"
                        onClick={() => setExceptionForm({ deliveryId: d.id, status: "LOST", reason: "" })}>
                        + Exception
                      </button>
                      <button type="button" className="admin-action-btn-ghost"
                        onClick={() => setSelectedDelivery(d)}>
                        Details
                      </button>
                      {msg && (
                        <span className={msg.ok ? "action-ok" : "action-err"}>{msg.msg}</span>
                      )}
                    </div>
                  </motion.div>
                );
              })}
            </div>
          )}
        </article>

        {/* Exceptions */}
        <article className="card" style={{ gridColumn: "span 12" }}>
          <h3>Exception Queue</h3>
          {exceptions.length === 0 ? <p className="muted-line">No exceptions logged.</p> : (
            <div className="admin-delivery-table">
              <div className="admin-table-head">
                <span>ID</span><span>Delivery</span><span>Status</span>
                <span>Reason</span><span>Resolved</span><span>Created</span><span>Action</span>
              </div>
              {exceptions.slice(0, 15).map((ex) => (
                <div key={ex.id} className="admin-table-row">
                  <span>#{ex.id}</span>
                  <span>#{ex.deliveryId}</span>
                  <span><StatusBadge status={ex.status} /></span>
                  <span style={{ fontSize: "0.82rem" }}>{ex.reason}</span>
                  <span>{ex.resolved
                    ? <span className="action-ok">Resolved</span>
                    : <span className="action-err">Open</span>}
                  </span>
                  <span className="muted-line" style={{ fontSize: "0.78rem" }}>{formatDate(ex.createdAt)}</span>
                  <div className="admin-actions">
                    {!ex.resolved && (
                      <button type="button" className="admin-action-btn" style={{ background: "#22c55e" }}
                        onClick={() => setResolveForm({ exceptionId: ex.id, resolution: "" })}>
                        Resolve
                      </button>
                    )}
                  </div>
                </div>
              ))}
            </div>
          )}
        </article>

        {error && (
          <article className="card" style={{ gridColumn: "span 12" }}>
            <p className="alert">{error}</p>
          </article>
        )}
      </div>

      {/* Delivery Detail Modal */}
      <AnimatePresence>
        {selectedDelivery && (
          <motion.div className="modal-overlay" initial={{ opacity: 0 }} animate={{ opacity: 1 }} exit={{ opacity: 0 }}
            onClick={() => setSelectedDelivery(null)}>
            <motion.div className="modal-box" initial={{ scale: 0.9, opacity: 0 }} animate={{ scale: 1, opacity: 1 }}
              exit={{ scale: 0.9, opacity: 0 }} onClick={(e) => e.stopPropagation()}>
              <div className="modal-head">
                <h3>Delivery Details</h3>
                <button type="button" className="modal-close" onClick={() => setSelectedDelivery(null)}>✕</button>
              </div>
              <div className="pay-detail-grid">
                {[
                  ["Tracking", selectedDelivery.trackingNumber],
                  ["Delivery ID", `#${selectedDelivery.id}`],
                  ["Customer ID", `#${selectedDelivery.customerId}`],
                  ["Status", selectedDelivery.status],
                  ["Receiver", selectedDelivery.receiverName],
                  ["Destination", selectedDelivery.destinationAddress],
                  ["Service", selectedDelivery.serviceType],
                  ["Quoted Price", `INR ${Number(selectedDelivery.quotedPrice).toFixed(2)}`],
                  ["Last Updated", formatDate(selectedDelivery.updatedAt)],
                ].map(([label, value]) => (
                  <div key={label} className="pay-detail-row">
                    <span className="pay-detail-label">{label}</span>
                    <span className="pay-detail-value">{value}</span>
                  </div>
                ))}
              </div>
              <div style={{ marginTop: "16px" }}>
                <StatusBadge status={selectedDelivery.status} />
              </div>
            </motion.div>
          </motion.div>
        )}
      </AnimatePresence>

      {/* Create Exception Modal */}
      <AnimatePresence>
        {exceptionForm && (
          <motion.div className="modal-overlay" initial={{ opacity: 0 }} animate={{ opacity: 1 }} exit={{ opacity: 0 }}
            onClick={() => setExceptionForm(null)}>
            <motion.div className="modal-box" initial={{ scale: 0.9 }} animate={{ scale: 1 }} exit={{ scale: 0.9 }}
              onClick={(e) => e.stopPropagation()}>
              <div className="modal-head">
                <h3>Create Exception — Delivery #{exceptionForm.deliveryId}</h3>
                <button type="button" className="modal-close" onClick={() => setExceptionForm(null)}>✕</button>
              </div>
              <div style={{ display: "grid", gap: "12px", marginTop: "12px" }}>
                <div>
                  <label>Exception Status</label>
                  <select value={exceptionForm.status} onChange={(e) => setExceptionForm({ ...exceptionForm, status: e.target.value })}>
                    {["LOST", "DAMAGED", "DELAYED", "RETURNED", "UNDELIVERABLE"].map((s) => <option key={s} value={s}>{s}</option>)}
                  </select>
                </div>
                <div>
                  <label>Reason</label>
                  <textarea rows={3} placeholder="Describe the issue..." value={exceptionForm.reason}
                    onChange={(e) => setExceptionForm({ ...exceptionForm, reason: e.target.value })} />
                </div>
                <button type="button" onClick={submitException}>Submit Exception</button>
              </div>
            </motion.div>
          </motion.div>
        )}
      </AnimatePresence>

      {/* Resolve Exception Modal */}
      <AnimatePresence>
        {resolveForm && (
          <motion.div className="modal-overlay" initial={{ opacity: 0 }} animate={{ opacity: 1 }} exit={{ opacity: 0 }}
            onClick={() => setResolveForm(null)}>
            <motion.div className="modal-box" initial={{ scale: 0.9 }} animate={{ scale: 1 }} exit={{ scale: 0.9 }}
              onClick={(e) => e.stopPropagation()}>
              <div className="modal-head">
                <h3>Resolve Exception #{resolveForm.exceptionId}</h3>
                <button type="button" className="modal-close" onClick={() => setResolveForm(null)}>✕</button>
              </div>
              <div style={{ display: "grid", gap: "12px", marginTop: "12px" }}>
                <div>
                  <label>Resolution Notes</label>
                  <textarea rows={3} placeholder="Describe how this was resolved..." value={resolveForm.resolution}
                    onChange={(e) => setResolveForm({ ...resolveForm, resolution: e.target.value })} />
                </div>
                <button type="button" className="pay-btn" onClick={submitResolve}>Mark Resolved</button>
              </div>
            </motion.div>
          </motion.div>
        )}
      </AnimatePresence>
    </section>
  );
}
