import type { CSSProperties } from "react";

/** Écran neutre affiché pendant la restauration de session (évite le flash /login au F5). */
export function AuthLoadingScreen() {
  return (
    <div style={layout}>
      <p style={{ color: "#475569" }}>Chargement…</p>
    </div>
  );
}

const layout: CSSProperties = {
  minHeight: "100vh",
  display: "flex",
  alignItems: "center",
  justifyContent: "center",
  padding: "1.5rem",
};
