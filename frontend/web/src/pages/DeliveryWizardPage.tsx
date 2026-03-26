import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { api } from "../lib/api";

export function DeliveryWizardPage() {
  const [senderName, setSenderName] = useState("");
  const [receiverName, setReceiverName] = useState("");
  const [receiverPhone, setReceiverPhone] = useState("");
  const [pickupAddress, setPickupAddress] = useState("");
  const [destinationAddress, setDestinationAddress] = useState("");
  const [packageWeightKg, setPackageWeightKg] = useState("1");
  const [packageType, setPackageType] = useState("BOX");
  const [serviceType, setServiceType] = useState("STANDARD");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  async function submit() {
    setLoading(true);
    setError("");
    try {
      await api.post("/deliveries", {
        senderName,
        receiverName,
        receiverPhone,
        pickupAddress,
        destinationAddress,
        packageWeightKg: Number(packageWeightKg),
        packageType,
        serviceType
      });
      navigate("/customer/dashboard");
    } catch {
      setError("Failed to create delivery.");
    } finally {
      setLoading(false);
    }
  }

  return (
    <section>
      <div className="page-head">
        <h2>Delivery Wizard</h2>
        <p>Step through sender, receiver, package, and route details to launch shipment.</p>
      </div>

      <div className="card-grid">
        <article className="card" style={{ gridColumn: "span 12" }}>
          <form>
            <div>
              <label htmlFor="sender">Sender</label>
              <input id="sender" placeholder="Sender Name" value={senderName} onChange={(e) => setSenderName(e.target.value)} />
            </div>
            <div>
              <label htmlFor="receiver">Receiver</label>
              <input id="receiver" placeholder="Receiver Name" value={receiverName} onChange={(e) => setReceiverName(e.target.value)} />
            </div>
            <div>
              <label htmlFor="phone">Receiver Phone</label>
              <input id="phone" placeholder="9999999999" value={receiverPhone} onChange={(e) => setReceiverPhone(e.target.value)} />
            </div>
            <div>
              <label htmlFor="pickup">Pickup Address</label>
              <input id="pickup" placeholder="Street, Area, City" value={pickupAddress} onChange={(e) => setPickupAddress(e.target.value)} />
            </div>
            <div>
              <label htmlFor="destination">Destination Address</label>
              <input id="destination" placeholder="Street, Area, City" value={destinationAddress} onChange={(e) => setDestinationAddress(e.target.value)} />
            </div>
            <div>
              <label htmlFor="weight">Weight (kg)</label>
              <input id="weight" placeholder="1.0" value={packageWeightKg} onChange={(e) => setPackageWeightKg(e.target.value)} />
            </div>
            <div>
              <label htmlFor="packageType">Package Type</label>
              <input id="packageType" placeholder="BOX, ENVELOPE, FRAGILE" value={packageType} onChange={(e) => setPackageType(e.target.value)} />
            </div>
            <div>
              <label htmlFor="serviceType">Service Type</label>
              <select id="serviceType" value={serviceType} onChange={(e) => setServiceType(e.target.value)}>
                <option value="STANDARD">STANDARD</option>
                <option value="EXPRESS">EXPRESS</option>
                <option value="INTERNATIONAL">INTERNATIONAL</option>
              </select>
            </div>
            <button type="button" onClick={submit} disabled={loading}>{loading ? "Creating..." : "Create Delivery"}</button>
            {error && <p className="alert">{error}</p>}
          </form>
        </article>
      </div>
    </section>
  );
}
