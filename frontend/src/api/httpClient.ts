import axios from "axios";
import { getStoredToken } from "../auth/api";

const candidatApiBase =
  (import.meta.env.VITE_CANDIDAT_API_BASE_URL as string | undefined)?.trim() ?? "";

/**
 * Client HTTP pour les microservices protégés par JWT (chemins /api/*).
 * En dev, laisser {@link import.meta.env.VITE_CANDIDAT_API_BASE_URL} vide : chemins relatifs
 * (`/api/candidats`) passent par le proxy Vite (voir `.env.example` + `vite.config.ts`).
 */
export const apiClient = axios.create({
  baseURL: candidatApiBase,
  timeout: 20_000,
  headers: {
    "Content-Type": "application/json",
  },
});

apiClient.interceptors.request.use((config) => {
  const token = getStoredToken();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  if (config.data instanceof FormData) {
    delete config.headers["Content-Type"];
  }
  return config;
});

// Intercepteur global : un 401 (jeton expiré/invalide) déclenche la déconnexion côté AuthContext.
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (axios.isAxiosError(error) && error.response?.status === 401) {
      window.dispatchEvent(new CustomEvent("auth:unauthorized"));
    }
    return Promise.reject(error);
  },
);
