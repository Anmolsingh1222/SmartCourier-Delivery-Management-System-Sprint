import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { api, storeTokens } from "../lib/api";

export function LoginPage() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  async function handleLogin() {
    setLoading(true);
    setError("");
    try {
      const { data } = await api.post("/auth/login", { email, password });
      storeTokens(data.accessToken, data.refreshToken);
      navigate("/customer/dashboard");
    } catch {
      setError("Login failed. Check your credentials.");
    } finally {
      setLoading(false);
    }
  }

  return (
    <section>
      <div className="page-head">
        <h2>Dispatch Control Login</h2>
        <p>Sign in to manage parcel movement, tracking, and customer operations.</p>
      </div>
      <div className="card-grid">
        <article className="card">
          <form>
            <div>
              <label htmlFor="email">Email</label>
              <input id="email" placeholder="ops@smartcourier.com" value={email} onChange={(e) => setEmail(e.target.value)} />
            </div>
            <div>
              <label htmlFor="password">Password</label>
              <input id="password" placeholder="Enter password" type="password" value={password} onChange={(e) => setPassword(e.target.value)} />
            </div>
            <button type="button" onClick={handleLogin} disabled={loading}>{loading ? "Signing in..." : "Sign In"}</button>
            {error && <p className="alert">{error}</p>}
          </form>
        </article>
      </div>
    </section>
  );
}
