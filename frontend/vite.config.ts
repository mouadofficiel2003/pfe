import react from "@vitejs/plugin-react";
import { defineConfig, loadEnv } from "vite";

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), "");
  const authOrigin = env.VITE_AUTH_SERVICE_ORIGIN || "http://localhost:8081";
  const candidatOrigin = env.VITE_CANDIDAT_SERVICE_ORIGIN || "http://localhost:8082";
  const concoursOrigin = env.VITE_CONCOURS_SERVICE_ORIGIN || "http://localhost:8083";
  const lieuxOrigin = env.VITE_LIEUX_SERVICE_ORIGIN || "http://localhost:8084";

  return {
    plugins: [react()],
    server: {
      port: 5173,
      proxy: {
        "/auth": {
          target: authOrigin,
          changeOrigin: true,
        },
        "/api/candidats": {
          target: candidatOrigin,
          changeOrigin: true,
        },
        "/api/concours": {
          target: concoursOrigin,
          changeOrigin: true,
        },
        "/api/centres": {
          target: lieuxOrigin,
          changeOrigin: true,
        },
        "/api/etablissements": {
          target: lieuxOrigin,
          changeOrigin: true,
        },
        "/api/salles": {
          target: lieuxOrigin,
          changeOrigin: true,
        },
      },
    },
  };
});
