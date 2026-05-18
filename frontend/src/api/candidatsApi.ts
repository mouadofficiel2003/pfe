import { apiClient } from "./httpClient";

/** Même forme que la réponse JSON GET /api/candidats (camelCase, candidat-service). */
export type CandidatDto = {
  id: number;
  nom: string;
  prenom: string;
  cin: string;
  numeroTelephone: string;
  ville: string;
  age: number;
  email: string;
  specialite: string;
  numeroInscription: string;
  nomConcours: string;
  concoursId: number | null;
  idCentre: number | null;
  idEtablissement: number | null;
  idSalle: number | null;
  numeroPlace: number | null;
  /** ISO-8601 (Instant côté serveur). */
  creeLe: string;
  modifieLe: string;
};

export type CandidatUpdatePayload = {
  nom: string;
  prenom: string;
  cin: string;
  numeroTelephone: string;
  ville: string;
  age: number;
  email: string;
  specialite: string;
  numeroInscription: string;
  nomConcours: string;
  concoursId: number | null;
  idCentre: number | null;
  idEtablissement: number | null;
  idSalle: number | null;
  numeroPlace: number | null;
};

export type ImportCandidatsResult = {
  inserted: number;
  updated: number;
  skipped: number;
  errors: { rowNumber: number; message: string }[];
};

/** Liste des candidats — GET /api/candidats (microservice candidat-service). */
export async function fetchCandidats(): Promise<CandidatDto[]> {
  const { data } = await apiClient.get<CandidatDto[]>("/api/candidats");
  return data;
}

export async function updateCandidat(
  id: number,
  payload: CandidatUpdatePayload,
): Promise<CandidatDto> {
  const { data } = await apiClient.put<CandidatDto>(`/api/candidats/${id}`, payload);
  return data;
}

export async function deleteCandidat(id: number): Promise<void> {
  await apiClient.delete(`/api/candidats/${id}`);
}

/** Import Excel .xlsx — POST /api/candidats/import (multipart, champ {@code file}). */
export async function importCandidatsExcel(file: File): Promise<ImportCandidatsResult> {
  const form = new FormData();
  form.append("file", file);
  const { data } = await apiClient.post<ImportCandidatsResult>("/api/candidats/import", form);
  return data;
}
