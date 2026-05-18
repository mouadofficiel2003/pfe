import { apiClient } from "./httpClient";

export type CentreListItemDto = {
  id: number;
  nomCentre: string;
  nombreEtablissements: number;
  concoursIds: number[];
};

export type SalleDto = {
  id: number;
  nomSalle: string;
  nombrePlaces: number;
  concoursId: number | null;
};

export type EtablissementDetailDto = {
  id: number;
  nomEtablissement: string;
  concoursIds: number[];
  salles: SalleDto[];
};

export type CentreDetailDto = {
  id: number;
  nomCentre: string;
  concoursIds: number[];
  etablissements: EtablissementDetailDto[];
};

export type CentreNomPayload = { nomCentre: string };
export type EtablissementNomPayload = { nomEtablissement: string };
export type SalleWritePayload = {
  nomSalle: string;
  nombrePlaces: number;
  concoursId: number | null;
};

export async function fetchCentres(): Promise<CentreListItemDto[]> {
  const { data } = await apiClient.get<CentreListItemDto[]>("/api/centres");
  return data;
}

export async function fetchCentre(id: number): Promise<CentreDetailDto> {
  const { data } = await apiClient.get<CentreDetailDto>(`/api/centres/${id}`);
  return data;
}

export async function createCentre(payload: CentreNomPayload): Promise<CentreDetailDto> {
  const { data } = await apiClient.post<CentreDetailDto>("/api/centres", payload);
  return data;
}

export async function updateCentre(id: number, payload: CentreNomPayload): Promise<CentreDetailDto> {
  const { data } = await apiClient.put<CentreDetailDto>(`/api/centres/${id}`, payload);
  return data;
}

export async function deleteCentre(id: number): Promise<void> {
  await apiClient.delete(`/api/centres/${id}`);
}

export async function createEtablissement(
  centreId: number,
  payload: EtablissementNomPayload,
): Promise<EtablissementDetailDto> {
  const { data } = await apiClient.post<EtablissementDetailDto>(
    `/api/centres/${centreId}/etablissements`,
    payload,
  );
  return data;
}

export async function updateEtablissement(
  id: number,
  payload: EtablissementNomPayload,
): Promise<EtablissementDetailDto> {
  const { data } = await apiClient.put<EtablissementDetailDto>(`/api/etablissements/${id}`, payload);
  return data;
}

export async function deleteEtablissement(id: number): Promise<void> {
  await apiClient.delete(`/api/etablissements/${id}`);
}

export async function createSalle(
  etablissementId: number,
  payload: SalleWritePayload,
): Promise<SalleDto> {
  const { data } = await apiClient.post<SalleDto>(
    `/api/etablissements/${etablissementId}/salles`,
    payload,
  );
  return data;
}

export async function updateSalle(id: number, payload: SalleWritePayload): Promise<SalleDto> {
  const { data } = await apiClient.put<SalleDto>(`/api/salles/${id}`, payload);
  return data;
}

export async function deleteSalle(id: number): Promise<void> {
  await apiClient.delete(`/api/salles/${id}`);
}
