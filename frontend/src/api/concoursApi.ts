import { apiClient } from "./httpClient";

export type CentreAffectationDto = {
  id: number;
  nomCentre: string;
  centreId: number | null;
};

export type ConcoursDto = {
  id: number;
  nomConcours: string;
  numeroConcours: string | null;
  dateHeureExamen: string;
  centres: CentreAffectationDto[];
  creeLe: string;
  modifieLe: string;
};

export type CentreAffectationWrite = {
  nomCentre: string;
  centreId: number | null;
};

export type ConcoursWritePayload = {
  nomConcours: string;
  numeroConcours?: string | null;
  dateHeureExamen: string;
  centres: CentreAffectationWrite[];
};

export async function fetchConcours(): Promise<ConcoursDto[]> {
  const { data } = await apiClient.get<ConcoursDto[]>("/api/concours");
  return data;
}

export async function createConcours(payload: ConcoursWritePayload): Promise<ConcoursDto> {
  const { data } = await apiClient.post<ConcoursDto>("/api/concours", payload);
  return data;
}

export async function updateConcours(id: number, payload: ConcoursWritePayload): Promise<ConcoursDto> {
  const { data } = await apiClient.put<ConcoursDto>(`/api/concours/${id}`, payload);
  return data;
}

export async function deleteConcours(id: number): Promise<void> {
  await apiClient.delete(`/api/concours/${id}`);
}
