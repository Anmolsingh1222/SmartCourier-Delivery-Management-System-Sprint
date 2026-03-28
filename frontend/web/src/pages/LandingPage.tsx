import { useEffect, useMemo, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
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

interface EstimateResponse {
  estimatedPrice: number;
}

export function LandingPage() {
  const [payload, setPayload] = useState<ServicesPayload | null>(null);
  const [trackingInput, setTrackingInput] = useState("");
  const [trackingError, setTrackingError] = useState("");
  const [estimateWeight, setEstimateWeight] = useState("1");
  const [estimateService, setEstimateService] = useState("STANDARD");
  const [estimateValue, setEstimateValue] = useState<number | null>(null);
  const [estimateBusy, setEstimateBusy] = useState(false);
  const [originPin, setOriginPin] = useState("");
  const [destinationPin, setDestinationPin] = useState("");
  const [coverageResult, setCoverageResult] = useState("");
  const [coverageError, setCoverageError] = useState("");
  const navigate = useNavigate();

  useEffect(() => {
    api.get("/services").then((res) => setPayload(res.data)).catch(() => setPayload(null));
  }, []);

  useEffect(() => {
    const timer = setTimeout(() => {
      const parsedWeight = Number(estimateWeight);
      if (!Number.isFinite(parsedWeight) || parsedWeight <= 0) {
        setEstimateValue(null);
        return;
      }

      setEstimateBusy(true);
      api.post<EstimateResponse>("/deliveries/estimate", {
        packageWeightKg: parsedWeight,
        serviceType: estimateService
      })
        .then((response) => setEstimateValue(Number(response.data?.estimatedPrice ?? 0)))
        .catch(() => setEstimateValue(null))
        .finally(() => setEstimateBusy(false));
    }, 350);

    return () => clearTimeout(timer);
  }, [estimateWeight, estimateService]);

  const liveServices = useMemo(() => payload?.services ?? [], [payload]);

  function trackNow() {
    const value = trackingInput.trim();
    if (!value) {
      setTrackingError("Enter tracking number or delivery id.");
      return;
    }
    setTrackingError("");
    navigate(`/track/${value}`);
  }

  function checkCoverage() {
    const source = originPin.trim();
    const target = destinationPin.trim();
    const pinRegex = /^\d{6}$/;

    if (!pinRegex.test(source) || !pinRegex.test(target)) {
      setCoverageError("Enter valid 6-digit source and destination pincodes.");
      setCoverageResult("");
      return;
    }

    setCoverageError("");
    const sameRegion = source[0] === target[0];
    const estimatedDays = estimateService === "EXPRESS" ? (sameRegion ? 1 : 2) : (sameRegion ? 2 : 4);
    const laneType = sameRegion ? "intra-region lane" : "inter-region lane";
    setCoverageResult(`Coverage available via ${laneType}. Estimated transit for ${estimateService}: ${estimatedDays} day(s).`);
  }

  return (
    <section>
      <div className="page-head">
        <h2>Courier Command Center</h2>
        <p>Built with the operational feel of premium courier portals: live tracking, lane visibility, and smart shipment tools.</p>
      </div>

      <div className="card-grid">
        <article className="card hero-card elevated" style={{ gridColumn: "span 12" }}>
          <h3>Track Shipment In Real Time</h3>
          <p>Enter your AWB/tracking id to fetch the live movement timeline across pickup, transit, and final delivery.</p>
          <div className="quick-track-bar">
            <input
              value={trackingInput}
              onChange={(event) => setTrackingInput(event.target.value)}
              placeholder="Example: SC1774589288808245 or 7"
            />
            <button type="button" onClick={trackNow}>Track Now</button>
          </div>
          {trackingError && <p className="alert">{trackingError}</p>}
          <div className="hero-actions">
            <Link to="/customer/deliveries/new" className="hero-link">Book Shipment</Link>
            <Link to="/auth/signup" className="hero-link secondary">Create Business Account</Link>
          </div>
        </article>

        <article className="card feature-card" style={{ gridColumn: "span 4" }}>
          <h3>Express Prime</h3>
          <p>Priority lanes for urgent city-to-city parcels with milestone-level visibility.</p>
          <span className="tag">Same-day eligible lanes</span>
        </article>
        <article className="card feature-card" style={{ gridColumn: "span 4" }}>
          <h3>Surface Economy</h3>
          <p>Cost-optimized routing with predictable ETA and consistent scan checkpoints.</p>
          <span className="tag">Budget friendly mode</span>
        </article>
        <article className="card feature-card" style={{ gridColumn: "span 4" }}>
          <h3>Enterprise Control</h3>
          <p>Admin dashboard with customer shipment feed, exceptions queue, and SLA signals.</p>
          <span className="tag">Role-protected operations</span>
        </article>

        <article className="card tool-card" style={{ gridColumn: "span 6" }}>
          <h3>Freight Rate Estimator</h3>
          <p className="muted-line">Get instant pricing preview before booking.</p>
          <div className="tool-grid">
            <label>
              Weight (kg)
              <input
                value={estimateWeight}
                onChange={(event) => setEstimateWeight(event.target.value)}
                placeholder="1.0"
              />
            </label>
            <label>
              Service
              <select value={estimateService} onChange={(event) => setEstimateService(event.target.value)}>
                <option value="STANDARD">STANDARD</option>
                <option value="EXPRESS">EXPRESS</option>
                <option value="INTERNATIONAL">INTERNATIONAL</option>
              </select>
            </label>
          </div>
          <p className="tool-output">
            {estimateBusy ? "Estimating..." : estimateValue === null ? "Estimate unavailable" : `Estimated Price: INR ${estimateValue.toFixed(2)}`}
          </p>
        </article>

        <article className="card tool-card" style={{ gridColumn: "span 6" }}>
          <h3>Pincode Serviceability Check</h3>
          <p className="muted-line">Quick source-to-destination lane feasibility check.</p>
          <div className="tool-grid">
            <label>
              Origin PIN
              <input
                value={originPin}
                onChange={(event) => setOriginPin(event.target.value)}
                placeholder="110001"
              />
            </label>
            <label>
              Destination PIN
              <input
                value={destinationPin}
                onChange={(event) => setDestinationPin(event.target.value)}
                placeholder="141001"
              />
            </label>
          </div>
          <button type="button" onClick={checkCoverage}>Check Coverage</button>
          {coverageError ? <p className="alert">{coverageError}</p> : null}
          {coverageResult ? <p className="tool-output">{coverageResult}</p> : null}
        </article>

        <article className="card" style={{ gridColumn: "span 12" }}>
          <h3>Live Service Network</h3>
          <p className="muted-line">System-level health from API gateway and service discovery.</p>
          <div className="service-strip">
            {liveServices.map((svc) => (
              <div key={svc.name} className="status-pill">
                <strong>{svc.name.replace("-SERVICE", "")}</strong>
                <span>{svc.path}</span>
                <em className={svc.status === "UP" ? "status-up" : "status-down"}>{svc.status}</em>
              </div>
            ))}
            {liveServices.length === 0 && (
              <p className="muted-line">Service registry snapshot will appear once gateway is reachable.</p>
            )}
          </div>
        </article>

        <article className="card" style={{ gridColumn: "span 12" }}>
          <h3>SmartCourier Lanes</h3>
          <ul className="lane-list">
            <li className="lane-item">
              <strong>North Priority Corridor</strong>
              <span>Delhi NCR to Chandigarh to Jammu</span>
            </li>
            <li className="lane-item">
              <strong>Punjab Commerce Ring</strong>
              <span>Ludhiana to Jalandhar to Amritsar</span>
            </li>
            <li className="lane-item">
              <strong>Metro Velocity Route</strong>
              <span>Mumbai to Pune to Bengaluru</span>
            </li>
          </ul>
        </article>

        <article className="card" style={{ gridColumn: "span 12" }}>
          <p>
            {payload
              ? `Network snapshot captured at ${new Date(payload.timestamp).toLocaleString()}.`
              : "Service snapshot unavailable. Start gateway to view live network telemetry."}
          </p>
        </article>
      </div>
    </section>
  );
}
