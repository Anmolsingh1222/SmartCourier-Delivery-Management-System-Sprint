import { useEffect, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { motion, AnimatePresence } from "framer-motion";
import { api } from "../lib/api";

interface PaymentState {
  deliveryId: number;
  trackingNumber: string;
  amount: number;
  receiverName: string;
  serviceType: string;
}

type PaymentStatus = "idle" | "processing" | "success" | "failed";

function loadRazorpayScript(): Promise<boolean> {
  return new Promise((resolve) => {
    if (document.getElementById("razorpay-script")) {
      resolve(true);
      return;
    }
    const script = document.createElement("script");
    script.id = "razorpay-script";
    script.src = "https://checkout.razorpay.com/v1/checkout.js";
    script.onload = () => resolve(true);
    script.onerror = () => resolve(false);
    document.body.appendChild(script);
  });
}

export function PaymentPage() {
  const location = useLocation();
  const navigate = useNavigate();
  const state = location.state as PaymentState | null;
  const [status, setStatus] = useState<PaymentStatus>("idle");
  const [paymentId, setPaymentId] = useState("");
  const [error, setError] = useState("");
  const [profile, setProfile] = useState<{ name: string; email: string } | null>(null);

  useEffect(() => {
    api.get("/auth/me").then((res) => setProfile(res.data)).catch(() => {});
    loadRazorpayScript();
  }, []);

  if (!state) {
    return (
      <section>
        <div className="page-head">
          <h2>Payment</h2>
          <p>No payment details found.</p>
        </div>
        <div className="card-grid">
          <article className="card" style={{ gridColumn: "span 12" }}>
            <p>Please create a delivery first.</p>
            <button type="button" onClick={() => navigate("/customer/deliveries/new")}>Create Delivery</button>
          </article>
        </div>
      </section>
    );
  }

  async function handlePay() {
    setError("");
    const loaded = await loadRazorpayScript();
    if (!loaded || !window.Razorpay) {
      setError("Payment gateway failed to load. Check your internet connection.");
      return;
    }

    setStatus("processing");

    const options: RazorpayOptions = {
      key: "rzp_test_SWWUNbIw7dRWtg",
      amount: Math.round(state!.amount * 100), // paise
      currency: "INR",
      name: "SmartCourier",
      description: `Payment for ${state!.trackingNumber}`,
      handler: (response: RazorpayResponse) => {
        setPaymentId(response.razorpay_payment_id);
        setStatus("success");
      },
      prefill: {
        name: profile?.name ?? "",
        email: profile?.email ?? "",
      },
      theme: { color: "#008da2" },
      modal: {
        ondismiss: () => {
          setStatus("idle");
        },
      },
    };

    try {
      const rzp = new window.Razorpay(options);
      rzp.open();
    } catch {
      setStatus("failed");
      setError("Failed to open payment gateway.");
    }
  }

  if (status === "success") {
    return (
      <section>
        <div className="page-head">
          <h2>Payment Successful</h2>
          <p>Your shipment is confirmed and ready for pickup scheduling.</p>
        </div>
        <div className="card-grid">
          <motion.article
            className="card pay-success-card"
            style={{ gridColumn: "span 12" }}
            initial={{ opacity: 0, scale: 0.92 }}
            animate={{ opacity: 1, scale: 1 }}
            transition={{ duration: 0.4, ease: "easeOut" }}
          >
            <motion.div
              className="pay-success-icon"
              initial={{ scale: 0 }}
              animate={{ scale: 1 }}
              transition={{ delay: 0.2, type: "spring", stiffness: 200 }}
            >✓</motion.div>
            <h3>Payment Confirmed</h3>
            <div className="pay-detail-grid">
              <div className="pay-detail-row"><span className="pay-detail-label">Tracking Number</span><span className="pay-detail-value">{state.trackingNumber}</span></div>
              <div className="pay-detail-row"><span className="pay-detail-label">Amount Paid</span><span className="pay-detail-value pay-amount">INR {state.amount.toFixed(2)}</span></div>
              <div className="pay-detail-row"><span className="pay-detail-label">Payment ID</span><span className="pay-detail-value pay-id">{paymentId}</span></div>
              <div className="pay-detail-row"><span className="pay-detail-label">Receiver</span><span className="pay-detail-value">{state.receiverName}</span></div>
              <div className="pay-detail-row"><span className="pay-detail-label">Service</span><span className="pay-detail-value">{state.serviceType}</span></div>
            </div>
            <div className="pay-actions">
              <button type="button" onClick={() => navigate(`/customer/track/${state.trackingNumber}`)}>Track Shipment</button>
              <button type="button" className="btn-secondary" onClick={() => navigate("/customer/dashboard")}>Go to Dashboard</button>
            </div>
          </motion.article>
        </div>
      </section>
    );
  }

  return (
    <section>
      <div className="page-head">
        <h2>Complete Payment</h2>
        <p>Review your shipment details and pay securely via Razorpay.</p>
      </div>
      <div className="card-grid">
        <motion.article
          className="card pay-summary-card"
          style={{ gridColumn: "span 6" }}
          initial={{ opacity: 0, x: -24 }}
          animate={{ opacity: 1, x: 0 }}
          transition={{ duration: 0.35 }}
        >
          <h3>Order Summary</h3>
          <div className="pay-detail-grid">
            <div className="pay-detail-row"><span className="pay-detail-label">Tracking Number</span><span className="pay-detail-value">{state.trackingNumber}</span></div>
            <div className="pay-detail-row"><span className="pay-detail-label">Delivery ID</span><span className="pay-detail-value">#{state.deliveryId}</span></div>
            <div className="pay-detail-row"><span className="pay-detail-label">Receiver</span><span className="pay-detail-value">{state.receiverName}</span></div>
            <div className="pay-detail-row"><span className="pay-detail-label">Service Type</span><span className="pay-detail-value">{state.serviceType}</span></div>
            <div className="pay-detail-row pay-total-row"><span className="pay-detail-label">Total Amount</span><span className="pay-detail-value pay-amount">INR {state.amount.toFixed(2)}</span></div>
          </div>
        </motion.article>

        <motion.article
          className="card pay-action-card"
          style={{ gridColumn: "span 6" }}
          initial={{ opacity: 0, x: 24 }}
          animate={{ opacity: 1, x: 0 }}
          transition={{ duration: 0.35 }}
        >
          <h3>Secure Payment</h3>
          <div className="pay-secure-badge"><span className="pay-lock">🔒</span><span>256-bit SSL encrypted · Powered by Razorpay</span></div>
          <div className="pay-methods">
            {["UPI","Cards","Net Banking","Wallets"].map((m) => (
              <motion.span key={m} className="pay-method-tag" whileHover={{ scale: 1.08 }}>{m}</motion.span>
            ))}
          </div>
          <div className="pay-amount-display">
            <span className="pay-amount-label">Amount Due</span>
            <span className="pay-amount-big">INR {state.amount.toFixed(2)}</span>
          </div>
          <motion.button
            type="button"
            className="pay-btn"
            onClick={handlePay}
            disabled={status === "processing"}
            whileHover={{ scale: 1.02 }}
            whileTap={{ scale: 0.97 }}
          >
            {status === "processing" ? "Opening Payment..." : `Pay INR ${state.amount.toFixed(2)}`}
          </motion.button>
          <button type="button" className="btn-ghost" onClick={() => navigate("/customer/dashboard")}>Pay Later</button>
          {error && <p className="alert">{error}</p>}
          <p className="pay-note">By proceeding, you agree to SmartCourier's terms of service. Payment is processed securely by Razorpay.</p>
        </motion.article>
      </div>
    </section>
  );
}
