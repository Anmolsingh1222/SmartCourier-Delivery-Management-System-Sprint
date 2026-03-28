import { useEffect, useState } from "react";
import { Link, Route, Routes, useLocation } from "react-router-dom";
import { motion, AnimatePresence } from "framer-motion";
import { LoginPage } from "./pages/LoginPage";
import { SignupPage } from "./pages/SignupPage";
import { LandingPage } from "./pages/LandingPage";
import { CustomerDashboardPage } from "./pages/CustomerDashboardPage";
import { DeliveryWizardPage } from "./pages/DeliveryWizardPage";
import { TrackPage } from "./pages/TrackPage";
import { AdminDashboardPage } from "./pages/AdminDashboardPage";
import { PaymentPage } from "./pages/PaymentPage";
import { ProfilePage } from "./pages/ProfilePage";
import { HubManagementPage } from "./pages/HubManagementPage";
import { ProtectedRoute } from "./components/ProtectedRoute";
import { getAccessToken, getTokenRole, logoutSession } from "./lib/api";

const NAV_ICONS: Record<string, string> = {
  "/": "🏠",
  "/auth/login": "🔑",
  "/auth/signup": "✨",
  "/track": "📍",
  "/admin/dashboard": "⚡",
  "/admin/hubs": "🏭",
  "/customer/dashboard": "📊",
  "/customer/deliveries/new": "📦",
  "/profile": "👤",
};

function Navbar({ theme, onToggleTheme }: { theme: "light" | "dark"; onToggleTheme: () => void }) {
  const location = useLocation();
  const isLoggedIn = Boolean(getAccessToken());
  const role = getTokenRole();
  const [mobileOpen, setMobileOpen] = useState(false);

  const navLinks = !isLoggedIn
    ? [
        { to: "/", label: "Home" },
        { to: "/auth/login", label: "Login" },
        { to: "/auth/signup", label: "Sign Up" },
        { to: "/track", label: "Track Parcel" },
      ]
    : role === "ADMIN"
    ? [
        { to: "/admin/dashboard", label: "Dashboard" },
        { to: "/admin/hubs", label: "Hubs" },
        { to: "/customer/deliveries/new", label: "New Shipment" },
        { to: "/track", label: "Track" },
        { to: "/profile", label: "Profile" },
      ]
    : [
        { to: "/customer/dashboard", label: "Dashboard" },
        { to: "/customer/deliveries/new", label: "New Shipment" },
        { to: "/track", label: "Track" },
        { to: "/profile", label: "Profile" },
      ];

  return (
    <motion.header
      className="navbar"
      initial={{ y: -80, opacity: 0 }}
      animate={{ y: 0, opacity: 1 }}
      transition={{ duration: 0.45, ease: "easeOut" }}
    >
      <div className="navbar-inner">
        {/* Brand */}
        <Link to="/" className="navbar-brand">
          <div className="brand-logo">SC</div>
          <div className="brand-text">
            <span className="brand-name">SmartCourier</span>
            <span className="brand-tagline">Logistics Grid</span>
          </div>
        </Link>

        {/* Desktop nav */}
        <nav className="navbar-links">
          {navLinks.map((link) => (
            <Link
              key={link.to}
              to={link.to}
              className={`navbar-link ${location.pathname === link.to ? "navbar-link-active" : ""}`}
            >
              <span className="navbar-link-icon">{NAV_ICONS[link.to] ?? "•"}</span>
              {link.label}
            </Link>
          ))}
        </nav>

        {/* Right actions */}
        <div className="navbar-right">
          <button
            type="button"
            className="theme-btn"
            onClick={onToggleTheme}
            title="Toggle theme"
          >
            {theme === "dark" ? "☀️" : "🌙"}
          </button>
          {isLoggedIn && (
            <button
              type="button"
              className="logout-btn"
              onClick={() => logoutSession().finally(() => { window.location.href = "/auth/login"; })}
            >
              Sign Out
            </button>
          )}
          {/* Mobile hamburger */}
          <button
            type="button"
            className="hamburger"
            onClick={() => setMobileOpen((o) => !o)}
            aria-label="Toggle menu"
          >
            <span className={`ham-line ${mobileOpen ? "open" : ""}`} />
            <span className={`ham-line ${mobileOpen ? "open" : ""}`} />
            <span className={`ham-line ${mobileOpen ? "open" : ""}`} />
          </button>
        </div>
      </div>

      {/* Mobile drawer */}
      <AnimatePresence>
        {mobileOpen && (
          <motion.div
            className="mobile-drawer"
            initial={{ height: 0, opacity: 0 }}
            animate={{ height: "auto", opacity: 1 }}
            exit={{ height: 0, opacity: 0 }}
            transition={{ duration: 0.25 }}
          >
            {navLinks.map((link) => (
              <Link
                key={link.to}
                to={link.to}
                className={`mobile-nav-link ${location.pathname === link.to ? "navbar-link-active" : ""}`}
                onClick={() => setMobileOpen(false)}
              >
                {NAV_ICONS[link.to] ?? "•"} {link.label}
              </Link>
            ))}
            {isLoggedIn && (
              <button
                type="button"
                className="mobile-nav-link logout-mobile"
                onClick={() => logoutSession().finally(() => { window.location.href = "/auth/login"; })}
              >
                🚪 Sign Out
              </button>
            )}
          </motion.div>
        )}
      </AnimatePresence>
    </motion.header>
  );
}

export default function App() {
  const [theme, setTheme] = useState<"light" | "dark">(() =>
    localStorage.getItem("smartcourier_theme") === "dark" ? "dark" : "light"
  );

  useEffect(() => {
    document.body.classList.toggle("theme-dark", theme === "dark");
    localStorage.setItem("smartcourier_theme", theme);
  }, [theme]);

  return (
    <div className="app-root">
      <div className="bg-mesh" />
      <Navbar theme={theme} onToggleTheme={() => setTheme((t) => (t === "dark" ? "light" : "dark"))} />
      <main className="app-main">
        <motion.div
          className="content-shell"
          initial={{ opacity: 0, y: 16 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.4, delay: 0.1 }}
        >
          <Routes>
            <Route path="/" element={<LandingPage />} />
            <Route path="/auth/login" element={<LoginPage />} />
            <Route path="/auth/signup" element={<SignupPage />} />
            <Route path="/customer/dashboard" element={<ProtectedRoute><CustomerDashboardPage /></ProtectedRoute>} />
            <Route path="/customer/deliveries/new" element={<ProtectedRoute><DeliveryWizardPage /></ProtectedRoute>} />
            <Route path="/customer/track/:id" element={<ProtectedRoute><TrackPage /></ProtectedRoute>} />
            <Route path="/track" element={<TrackPage />} />
            <Route path="/track/:id" element={<TrackPage />} />
            <Route path="/admin/dashboard" element={<ProtectedRoute requiredRole="ADMIN"><AdminDashboardPage /></ProtectedRoute>} />
            <Route path="/customer/payment" element={<ProtectedRoute><PaymentPage /></ProtectedRoute>} />
            <Route path="/profile" element={<ProtectedRoute><ProfilePage /></ProtectedRoute>} />
            <Route path="/admin/hubs" element={<ProtectedRoute requiredRole="ADMIN"><HubManagementPage /></ProtectedRoute>} />
          </Routes>
        </motion.div>
      </main>
    </div>
  );
}
