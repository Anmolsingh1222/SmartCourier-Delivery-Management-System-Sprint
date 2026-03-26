import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { api, storeTokens } from "../lib/api";

export function SignupPage() {
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  async function handleSignup() {
    setLoading(true);
    setError("");
    try {
      const { data } = await api.post("/auth/signup", { name, email, password });
      storeTokens(data.accessToken, data.refreshToken);
      navigate("/customer/dashboard");
    } catch {
      setError("Signup failed. Try another email.");
    } finally {
      setLoading(false);
    }
  }

  return (
    <section>
      <div className="page-head">
        <h2>Create New Operator Account</h2>
        <p>Register to start booking deliveries and monitoring end-to-end parcel flow.</p>
      </div>
      <div className="card-grid">
        <article className="card">
          <form>
            <div>
              <label htmlFor="name">Full Name</label>
              <input id="name" placeholder="Alex Dispatch" value={name} onChange={(e) => setName(e.target.value)} />
            </div>
            <div>
              <label htmlFor="signup-email">Email</label>
              <input id="signup-email" placeholder="alex@smartcourier.com" value={email} onChange={(e) => setEmail(e.target.value)} />
            </div>
            <div>
              <label htmlFor="signup-password">Password</label>
              <input id="signup-password" placeholder="Minimum 8 characters" type="password" value={password} onChange={(e) => setPassword(e.target.value)} />
            </div>
            <button type="button" onClick={handleSignup} disabled={loading}>{loading ? "Creating..." : "Create Account"}</button>
            {error && <p className="alert">{error}</p>}
          </form>
        </article>
      </div>
    </section>
  );
}
