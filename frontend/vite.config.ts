import react from "@vitejs/plugin-react";
import { defineConfig, loadEnv } from "vite";

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), "");
  // Toutes les requetes passent par l'API Gateway (point d'entree unique).
  const gatewayOrigin = env.VITE_GATEWAY_ORIGIN || "http://localhost:8080";

  const gatewayProxy = { target: gatewayOrigin, changeOrigin: true } as const;

  return {
    plugins: [react()],
    server: {
      port: 5173,
      proxy: {
        "/auth": gatewayProxy,
        "/api/candidats": gatewayProxy,
        "/api/concours": gatewayProxy,
        "/api/centres": gatewayProxy,
        "/api/etablissements": gatewayProxy,
        "/api/salles": gatewayProxy,
        "/api/repartition": gatewayProxy,
        "/api/convocations": gatewayProxy,
      },
    },
  };
});
