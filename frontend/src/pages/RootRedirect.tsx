import { Navigate } from "react-router-dom";
import { useAuth } from "../auth/AuthContext";

export default function RootRedirect() {
  const { state } = useAuth();
  if (state.status === "authenticated") {
    return <Navigate to="/candidats" replace />;
  }
  return <Navigate to="/login" replace />;
}
