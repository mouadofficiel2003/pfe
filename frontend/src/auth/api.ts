import type { LoginResponse } from "./types";

const TOKEN_KEY = "pfe_access_token";

export function getStoredToken(): string | null {
  return sessionStorage.getItem(TOKEN_KEY);
}

export function setStoredToken(token: string | null): void {
  if (token === null) {
    sessionStorage.removeItem(TOKEN_KEY);
  } else {
    sessionStorage.setItem(TOKEN_KEY, token);
  }
}

export async function loginRequest(username: string, password: string): Promise<LoginResponse> {
  const res = await fetch("/auth/login", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ username, password }),
  });
  const data: unknown = await res.json().catch(() => ({}));
  if (!res.ok) {
    const msg =
      typeof data === "object" &&
      data !== null &&
      "message" in data &&
      typeof (data as { message: unknown }).message === "string"
        ? (data as { message: string }).message
        : "Échec de la connexion.";
    throw new Error(msg);
  }
  return data as LoginResponse;
}

export async function fetchMe(accessToken: string): Promise<{ username: string; role: string }> {
  const res = await fetch("/auth/me", {
    headers: { Authorization: `Bearer ${accessToken}` },
  });
  if (!res.ok) {
    throw new Error("Session invalide.");
  }
  return res.json() as Promise<{ username: string; role: string }>;
}
