import { Link, Navigate, Route, Routes, useLocation } from "react-router-dom";
import { LoginPage } from "./pages/LoginPage";
import { SignupPage } from "./pages/SignupPage";
import { LandingPage } from "./pages/LandingPage";
import { CustomerDashboardPage } from "./pages/CustomerDashboardPage";
import { DeliveryWizardPage } from "./pages/DeliveryWizardPage";
import { TrackPage } from "./pages/TrackPage";
import { AdminDashboardPage } from "./pages/AdminDashboardPage";
import { ProtectedRoute } from "./components/ProtectedRoute";
import { logoutSession } from "./lib/api";

const navLinks = [
  { to: "/auth/login", label: "Login" },
  { to: "/auth/signup", label: "Signup" },
  { to: "/customer/dashboard", label: "Dashboard" },
  { to: "/customer/deliveries/new", label: "Create Delivery" },
  { to: "/admin/dashboard", label: "Admin" }
];

function TopBar() {
  const location = useLocation();

  return (
    <header className="topbar fade-in">
      <div className="brand-block">
        <p className="badge">Live Dispatch Network</p>
        <h1>SmartCourier</h1>
        <p className="subtitle">Fast parcel operations with full lifecycle visibility.</p>
      </div>
      <nav className="nav-grid">
        {navLinks.map((link) => (
          <Link
            key={link.to}
            to={link.to}
            className={location.pathname === link.to ? "nav-link active" : "nav-link"}
          >
            {link.label}
          </Link>
        ))}
        <button
          type="button"
          className="nav-link logout"
          onClick={() => {
            logoutSession().finally(() => {
              window.location.href = "/auth/login";
            });
          }}
        >
          Logout
        </button>
      </nav>
    </header>
  );
}

export default function App() {
  return (
    <div className="page-bg">
      <div className="orb orb-a" />
      <div className="orb orb-b" />
      <div className="shell">
        <TopBar />
        <main className="content-panel slide-up">
          <Routes>
            <Route path="/" element={<LandingPage />} />
            <Route path="/auth/login" element={<LoginPage />} />
            <Route path="/auth/signup" element={<SignupPage />} />
            <Route path="/customer/dashboard" element={<ProtectedRoute><CustomerDashboardPage /></ProtectedRoute>} />
            <Route path="/customer/deliveries/new" element={<ProtectedRoute><DeliveryWizardPage /></ProtectedRoute>} />
            <Route path="/customer/track/:id" element={<ProtectedRoute><TrackPage /></ProtectedRoute>} />
            <Route path="/admin/dashboard" element={<ProtectedRoute><AdminDashboardPage /></ProtectedRoute>} />
          </Routes>
        </main>
      </div>
    </div>
  );
}
