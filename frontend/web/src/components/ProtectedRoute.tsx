import type { ReactElement } from "react";
import { Navigate } from "react-router-dom";

const TOKEN_KEY = "smartcourier_access_token";

interface ProtectedRouteProps {
  children: ReactElement;
}

export function ProtectedRoute({ children }: ProtectedRouteProps) {
  const token = localStorage.getItem(TOKEN_KEY);
  if (!token) {
    return <Navigate to="/auth/login" replace />;
  }
  return children;
}
