import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { motion, AnimatePresence } from "framer-motion";
import { api } from "../lib/api";

const SERVICE_INFO = {
  STANDARD: { label: "Standard", days: "3–5 days", icon: "📦", desc: "Balanced pricing for regular domestic parcel movement." },
  EXPRESS: { label: "Express", days: "1–2 days", icon: "⚡", desc: "Priority processing with faster handovers and transit scans." },
  INTERNATIONAL: { label: "International", days: "7–14 days", icon: "🌐", desc: "Cross-border lane handling with premium pricing slab." },
};

const PACKAGE_TYPES = ["BOX", "ENVELOPE", "FRAGILE", "PALLET", "TUBE"];

export function DeliveryWizardPage() {
  const [step, setStep] = useState(1);
  const [senderName, setSenderName] = useState("");
  const [receiverName, setReceiverName] = useState("");
  const [receiverPhone, setReceiverPhone] = useState("");
  const [pickupAddress, setPickupAddress] = useState("");
  const [destinationAddress, setDestinationAddress] = useState("");
  const [packageWeightKg, setPackageWeightKg] = useState("1");
  const [packageType, setPackageType] = useState("BOX");
  const [serviceType, setServiceType] = useState("STANDARD");
  const [specialInstructions, setSpecialInstructions] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const [estimate, setEstimate] = useState<number | null>(null);
  const navigate = useNavigate();

  useEffect(() => {
    const timer = setTimeout(() => {
      const weight = Number(packageWeightKg);
      if (!Number.isFinite(weight) || weight <= 0) { setEstimate(null); return; }
      api.post("/deliveries/estimate", { packageWeightKg: weight, serviceType })
        .then((res) => setEstimate(Number(res.data?.estimatedPrice ?? 0)))
        .catch(() => setEstimate(null));
    }, 350);
    return () => clearTimeout(timer);
  }, [packageWeightKg, serviceType]);

  function validateStep1() {
    if (!senderName.trim()) return "Sender name is required.";
    if (!receiverName.trim()) return "Receiver name is required.";
    if (!receiverPhone.trim() || !/^\d{10}$/.test(receiverPhone.trim())) return "Enter a valid 10-digit phone number.";
    return "";
  }

  function validateStep2() {
    if (!pickupAddress.trim()) return "Pickup address is required.";
    if (!destinationAddress.trim()) return "Destination address is required.";
    return "";
  }

  function nextStep() {
    const err = step === 1 ? validateStep1() : step === 2 ? validateStep2() : "";
    if (err) { setError(err); return; }
    setError("");
    setStep((s) => s + 1);
  }

  async function submit() {
    setLoading(true);
    setError("");
    try {
      const res = await api.post("/deliveries", {
        senderName, receiverName, receiverPhone,
        pickupAddress, destinationAddress,
        packageWeightKg: Number(packageWeightKg),
        packageType, serviceType,
      });
      navigate("/customer/payment", {
        state: {
          deliveryId: res.data.id,
          trackingNumber: res.data.trackingNumber,
          amount: Number(res.data.quotedPrice),
          receiverName: res.data.receiverName,
          serviceType: res.data.serviceType,
        },
      });
    } catch {
      setError("Failed to create delivery. Please try again.");
    } finally {
      setLoading(false);
    }
  }

  const svc = SERVICE_INFO[serviceType as keyof typeof SERVICE_INFO];

  return (
    <section>
      <div className="page-head">
        <h2>Shipment Booking Console</h2>
        <p>Step through sender, package, and service details to launch your shipment.</p>
      </div>

      <div className="card-grid">
        {/* Step indicator */}
        <article className="card" style={{ gridColumn: "span 12" }}>
          <div className="wizard-steps">
            {["Parties", "Route", "Package", "Review"].map((label, i) => (
              <div key={label} className={`wizard-step ${step === i + 1 ? "active" : step > i + 1 ? "done" : ""}`}>
                <div className="wizard-step-num">{step > i + 1 ? "✓" : i + 1}</div>
                <span>{label}</span>
              </div>
            ))}
          </div>
        </article>

        {/* Live estimate */}
        <article className="card metric" style={{ gridColumn: "span 4" }}>
          <span className="metric-value">{packageWeightKg} kg</span>
          <span className="metric-label">Billable Weight</span>
        </article>
        <article className="card metric" style={{ gridColumn: "span 4" }}>
          <span className={`metric-value svc-badge svc-${serviceType.toLowerCase()}`}>{svc.icon} {svc.label}</span>
          <span className="metric-label">ETA: {svc.days}</span>
        </article>
        <article className="card metric" style={{ gridColumn: "span 4" }}>
          <span className="metric-value pay-amount">{estimate === null ? "--" : `INR ${estimate.toFixed(2)}`}</span>
          <span className="metric-label">Live Price Estimate</span>
        </article>

        {/* Step 1 — Parties */}
        <AnimatePresence mode="wait">
        {step === 1 && (
          <motion.article
            key="step1"
            className="card"
            style={{ gridColumn: "span 12" }}
            initial={{ opacity: 0, x: 40 }}
            animate={{ opacity: 1, x: 0 }}
            exit={{ opacity: 0, x: -40 }}
            transition={{ duration: 0.25 }}
          >
            <h3>Step 1 — Sender & Receiver</h3>
            <div className="form-grid">
              <div>
                <label htmlFor="sender">Sender Name</label>
                <input id="sender" placeholder="Full name of sender" value={senderName} onChange={(e) => setSenderName(e.target.value)} />
              </div>
              <div>
                <label htmlFor="receiver">Receiver Name</label>
                <input id="receiver" placeholder="Full name of receiver" value={receiverName} onChange={(e) => setReceiverName(e.target.value)} />
              </div>
              <div className="col-span-2">
                <label htmlFor="phone">Receiver Phone</label>
                <input id="phone" placeholder="10-digit mobile number" value={receiverPhone} onChange={(e) => setReceiverPhone(e.target.value)} />
              </div>
            </div>
            {error && <p className="alert">{error}</p>}
            <div className="wizard-nav">
              <button type="button" onClick={nextStep}>Next: Route Details →</button>
            </div>
          </motion.article>
        )}

        {/* Step 2 — Route */}
        {step === 2 && (
          <motion.article
            key="step2"
            className="card"
            style={{ gridColumn: "span 12" }}
            initial={{ opacity: 0, x: 40 }}
            animate={{ opacity: 1, x: 0 }}
            exit={{ opacity: 0, x: -40 }}
            transition={{ duration: 0.25 }}
          >
            <h3>Step 2 — Pickup & Destination</h3>
            <div className="form-grid">
              <div>
                <label htmlFor="pickup">Pickup Address</label>
                <input id="pickup" placeholder="Street, Area, City, PIN" value={pickupAddress} onChange={(e) => setPickupAddress(e.target.value)} />
              </div>
              <div>
                <label htmlFor="destination">Destination Address</label>
                <input id="destination" placeholder="Street, Area, City, PIN" value={destinationAddress} onChange={(e) => setDestinationAddress(e.target.value)} />
              </div>
            </div>
            {error && <p className="alert">{error}</p>}
            <div className="wizard-nav">
              <button type="button" className="btn-secondary" onClick={() => { setError(""); setStep(1); }}>← Back</button>
              <button type="button" onClick={nextStep}>Next: Package Details →</button>
            </div>
          </motion.article>
        )}

        {/* Step 3 — Package */}
        {step === 3 && (
          <motion.article
            key="step3"
            className="card"
            style={{ gridColumn: "span 12" }}
            initial={{ opacity: 0, x: 40 }}
            animate={{ opacity: 1, x: 0 }}
            exit={{ opacity: 0, x: -40 }}
            transition={{ duration: 0.25 }}
          >
            <h3>Step 3 — Package & Service</h3>
            <div className="service-selector">
              {Object.entries(SERVICE_INFO).map(([key, info]) => (
                <div
                  key={key}
                  className={`service-option ${serviceType === key ? "selected" : ""}`}
                  onClick={() => setServiceType(key)}
                >
                  <span className="service-icon">{info.icon}</span>
                  <strong>{info.label}</strong>
                  <span className="service-days">{info.days}</span>
                  <p>{info.desc}</p>
                </div>
              ))}
            </div>
            <div className="form-grid" style={{ marginTop: "16px" }}>
              <div>
                <label htmlFor="weight">Weight (kg)</label>
                <input id="weight" type="number" min="0.1" step="0.1" placeholder="1.0" value={packageWeightKg} onChange={(e) => setPackageWeightKg(e.target.value)} />
              </div>
              <div>
                <label htmlFor="packageType">Package Type</label>
                <select id="packageType" value={packageType} onChange={(e) => setPackageType(e.target.value)}>
                  {PACKAGE_TYPES.map((t) => <option key={t} value={t}>{t}</option>)}
                </select>
              </div>
              <div className="col-span-2">
                <label htmlFor="instructions">Special Instructions (optional)</label>
                <textarea id="instructions" rows={2} placeholder="Fragile, call before delivery, landmark..." value={specialInstructions} onChange={(e) => setSpecialInstructions(e.target.value)} />
              </div>
            </div>
            {error && <p className="alert">{error}</p>}
            <div className="wizard-nav">
              <button type="button" className="btn-secondary" onClick={() => { setError(""); setStep(2); }}>← Back</button>
              <button type="button" onClick={nextStep}>Review Order →</button>
            </div>
          </motion.article>
        )}

        {/* Step 4 — Review */}
        {step === 4 && (
          <motion.article
            key="step4"
            className="card"
            style={{ gridColumn: "span 12" }}
            initial={{ opacity: 0, x: 40 }}
            animate={{ opacity: 1, x: 0 }}
            exit={{ opacity: 0, x: -40 }}
            transition={{ duration: 0.25 }}
          >
            <h3>Step 4 — Review & Confirm</h3>
            <div className="review-grid">
              <div className="review-section">
                <h4>Parties</h4>
                <div className="review-row"><span>Sender</span><strong>{senderName}</strong></div>
                <div className="review-row"><span>Receiver</span><strong>{receiverName}</strong></div>
                <div className="review-row"><span>Phone</span><strong>{receiverPhone}</strong></div>
              </div>
              <div className="review-section">
                <h4>Route</h4>
                <div className="review-row"><span>Pickup</span><strong>{pickupAddress}</strong></div>
                <div className="review-row"><span>Destination</span><strong>{destinationAddress}</strong></div>
              </div>
              <div className="review-section">
                <h4>Package</h4>
                <div className="review-row"><span>Weight</span><strong>{packageWeightKg} kg</strong></div>
                <div className="review-row"><span>Type</span><strong>{packageType}</strong></div>
                <div className="review-row"><span>Service</span><strong>{svc.icon} {svc.label} ({svc.days})</strong></div>
                {specialInstructions && <div className="review-row"><span>Notes</span><strong>{specialInstructions}</strong></div>}
              </div>
              <div className="review-section review-total">
                <h4>Total</h4>
                <div className="review-row">
                  <span>Estimated Price</span>
                  <strong className="pay-amount">{estimate !== null ? `INR ${estimate.toFixed(2)}` : "Calculating..."}</strong>
                </div>
              </div>
            </div>
            {error && <p className="alert">{error}</p>}
            <div className="wizard-nav">
              <button type="button" className="btn-secondary" onClick={() => { setError(""); setStep(3); }}>← Back</button>
              <button type="button" className="pay-btn" onClick={submit} disabled={loading}>
                {loading ? "Creating Shipment..." : `Confirm & Pay INR ${estimate?.toFixed(2) ?? "--"}`}
              </button>
            </div>
          </motion.article>
        )}
        </AnimatePresence>
      </div>
    </section>
  );
}
