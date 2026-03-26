import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { api } from "../lib/api";

interface ServiceInfo {
  name: string;
  path: string;
  status: string;
}

interface ServicesPayload {
  project: string;
  timestamp: string;
  services: ServiceInfo[];
}

export function LandingPage() {
  const [payload, setPayload] = useState<ServicesPayload | null>(null);

  useEffect(() => {
    api.get("/services").then((res) => setPayload(res.data)).catch(() => setPayload(null));
  }, []);

  return (
    <section>
      <div className="page-head">
        <h2>SmartCourier Demo Console</h2>
        <p>Enterprise courier platform with service discovery, secure APIs, and live tracking workflows.</p>
      </div>

      <div className="card-grid">
        <article className="card hero-card" style={{ gridColumn: "span 12" }}>
          <h3>Microservices + Gateway + Eureka</h3>
          <p>Production-ready architecture with JWT security, role controls, Flyway migrations, Swagger, and Postman suites.</p>
          <div className="hero-actions">
            <Link to="/auth/login" className="hero-link">Open Login</Link>
            <Link to="/auth/signup" className="hero-link secondary">Create Account</Link>
          </div>
        </article>

        {(payload?.services ?? []).map((svc) => (
          <article key={svc.name} className="card metric" style={{ gridColumn: "span 3" }}>
            <span className="metric-value">{svc.name.replace("-SERVICE", "")}</span>
            <span className="metric-label">{svc.path}</span>
            <span className="metric-label">Status: {svc.status}</span>
          </article>
        ))}

        <article className="card" style={{ gridColumn: "span 12" }}>
          <p>
            {payload
              ? `Service snapshot captured at ${new Date(payload.timestamp).toLocaleString()}.`
              : "Service snapshot unavailable. Start gateway to see live service data."}
          </p>
        </article>
      </div>
    </section>
  );
}
