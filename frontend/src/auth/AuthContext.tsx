import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from "react";
import type { LoginResponse, RoleUtilisateur } from "./types";
import { fetchMe, getStoredToken, loginRequest, setStoredToken } from "./api";

type AuthState =
  | { status: "loading" }
  | { status: "anonymous" }
  | { status: "authenticated"; user: LoginResponse };

type AuthContextValue = {
  state: AuthState;
  login: (username: string, password: string) => Promise<void>;
  logout: () => void;
};

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [state, setState] = useState<AuthState>({ status: "loading" });

  useEffect(() => {
    let cancelled = false;
    (async () => {
      const restored = await tryRestoreSession();
      if (cancelled) return;
      setState(
        restored
          ? { status: "authenticated", user: restored }
          : { status: "anonymous" },
      );
    })();
    return () => {
      cancelled = true;
    };
  }, []);

  const login = useCallback(async (username: string, password: string) => {
    const data = await loginRequest(username, password);
    setStoredToken(data.accessToken);
    setState({ status: "authenticated", user: data });
  }, []);

  const logout = useCallback(() => {
    setStoredToken(null);
    setState({ status: "anonymous" });
  }, []);

  // Déconnexion automatique si un appel API protégé renvoie 401 (jeton expiré en cours d'usage).
  useEffect(() => {
    const onUnauthorized = () => {
      setStoredToken(null);
      setState((current) =>
        current.status === "authenticated" ? { status: "anonymous" } : current,
      );
    };
    window.addEventListener("auth:unauthorized", onUnauthorized);
    return () => window.removeEventListener("auth:unauthorized", onUnauthorized);
  }, []);

  const value = useMemo(() => ({ state, login, logout }), [state, login, logout]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext);
  if (!ctx) {
    throw new Error("useAuth doit être utilisé dans AuthProvider");
  }
  return ctx;
}

/** Optionnel : restaurer la session si un jeton existe déjà (après F5). */
export async function tryRestoreSession(): Promise<LoginResponse | null> {
  const token = getStoredToken();
  if (!token) return null;
  try {
    const me = await fetchMe(token);
    return {
      accessToken: token,
      tokenType: "Bearer",
      username: me.username,
      role: me.role as RoleUtilisateur,
    };
  } catch {
    setStoredToken(null);
    return null;
  }
}
