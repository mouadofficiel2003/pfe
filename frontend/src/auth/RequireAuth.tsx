import { Navigate, useLocation } from "react-router-dom";
import { useAuth } from "./AuthContext";
import { AuthLoadingScreen } from "./AuthLoadingScreen";

export function RequireAuth({ children }: { children: React.ReactNode }) {
  const { state } = useAuth();
  const location = useLocation();

  if (state.status === "loading") {
    return <AuthLoadingScreen />;
  }

  if (state.status === "anonymous") {
    return (
      <Navigate
        to="/login"
        replace
        state={{ from: location.pathname + location.search }}
      />
    );
  }

  return <>{children}</>;
}
