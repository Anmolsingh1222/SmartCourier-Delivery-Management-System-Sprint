import { useEffect, useState } from "react";
import { motion } from "framer-motion";
import { api } from "../lib/api";

interface Hub { id: number; code: string; name: string; city: string; }

export function HubManagementPage() {
  const [hubs, setHubs] = useState<Hub[]>([]);
  const [code, setCode] = useState("");
  const [name, setName] = useState("");
  const [city, setCity] = useState("");
  const [msg, setMsg] = useState<{ text: string; ok: boolean } | null>(null);
  const [loading, setLoading] = useState(false);
  const [refreshKey, setRefreshKey] = useState(0);

  useEffect(() => {
    api.get("/admin/hubs").then((res) => setHubs(res.data ?? [])).catch(() => {});
  }, [refreshKey]);

  async function createHub() {
    if (!code.trim() || !name.trim() || !city.trim()) {
      setMsg({ text: "All fields are required", ok: false });
      return;
    }
    setLoading(true);
    setMsg(null);
    try {
      await api.post("/admin/hubs", { code, name, city });
      setMsg({ text: `Hub ${code.toUpperCase()} created successfully`, ok: true });
      setCode(""); setName(""); setCity("");
      setRefreshKey((k) => k + 1);
    } catch {
      setMsg({ text: "Failed to create hub", ok: false });
    } finally {
      setLoading(false);
    }
  }

  return (
    <section>
      <div className="page-head">
        <h2>Hub Management</h2>
        <p>Create and manage courier hubs across the network.</p>
      </div>

      <div className="card-grid">
        {/* Create hub */}
        <motion.article className="card" style={{ gridColumn: "span 5" }}
          initial={{ opacity: 0, x: -20 }} animate={{ opacity: 1, x: 0 }}>
          <h3>Create New Hub</h3>
          <div style={{ display: "grid", gap: "12px", marginTop: "8px" }}>
            <div>
              <label htmlFor="hub-code">Hub Code</label>
              <input id="hub-code" placeholder="DEL, MUM, BLR..." value={code}
                onChange={(e) => setCode(e.target.value.toUpperCase())} maxLength={5} />
            </div>
            <div>
              <label htmlFor="hub-name">Hub Name</label>
              <input id="hub-name" placeholder="Delhi Central Hub" value={name}
                onChange={(e) => setName(e.target.value)} />
            </div>
            <div>
              <label htmlFor="hub-city">City</label>
              <input id="hub-city" placeholder="New Delhi" value={city}
                onChange={(e) => setCity(e.target.value)} />
            </div>
            {msg && <p className={msg.ok ? "msg-ok" : "alert"}>{msg.text}</p>}
            <button type="button" onClick={createHub} disabled={loading}>
              {loading ? "Creating..." : "Create Hub"}
            </button>
          </div>
        </motion.article>

        {/* Hub list */}
        <motion.article className="card" style={{ gridColumn: "span 7" }}
          initial={{ opacity: 0, x: 20 }} animate={{ opacity: 1, x: 0 }}>
          <div className="dash-header-row">
            <h3>Active Hubs ({hubs.length})</h3>
            <button type="button" className="btn-primary-sm" onClick={() => setRefreshKey((k) => k + 1)}>↻ Refresh</button>
          </div>
          {hubs.length === 0 ? (
            <div className="empty-state">
              <span className="empty-icon">🏭</span>
              <p>No hubs created yet.</p>
            </div>
          ) : (
            <div className="hub-grid">
              {hubs.map((hub, i) => (
                <motion.div key={hub.id} className="hub-card"
                  initial={{ opacity: 0, y: 10 }} animate={{ opacity: 1, y: 0 }}
                  transition={{ delay: i * 0.05 }}>
                  <div className="hub-code">{hub.code}</div>
                  <div className="hub-info">
                    <strong>{hub.name}</strong>
                    <span className="muted-line">{hub.city}</span>
                  </div>
                </motion.div>
              ))}
            </div>
          )}
        </motion.article>
      </div>
    </section>
  );
}
