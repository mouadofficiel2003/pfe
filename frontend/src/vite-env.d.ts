/// <reference types="vite/client" />

interface ImportMetaEnv {
  /** Base URL absolue du candidat-service pour Axios (vide = relatif → proxy en dev). */
  readonly VITE_CANDIDAT_API_BASE_URL?: string;
}

interface ImportMeta {
  readonly env: ImportMetaEnv;
}
