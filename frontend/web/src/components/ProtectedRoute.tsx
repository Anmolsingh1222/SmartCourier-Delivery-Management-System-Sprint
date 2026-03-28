import type { ReactElement } from "react";
import { Navigate } from "react-router-dom";
import { getTokenRole } from "../lib/api";

const TOKEN_KEY = "smartcourier_access_token";

interface ProtectedRouteProps {
  children: ReactElement;
  requiredRole?: string;
}

export function ProtectedRoute({ children, requiredRole }: ProtectedRouteProps) {
  const token = localStorage.getItem(TOKEN_KEY);
  if (!token) {
    return <Navigate to="/auth/login" replace />;
  }

  if (requiredRole) {
    const role = getTokenRole();
    if (!role || role !== requiredRole.toUpperCase()) {
      return <Navigate to="/customer/dashboard" replace />;
    }
  }

  return children;
}
