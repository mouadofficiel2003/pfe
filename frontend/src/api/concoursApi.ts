import { apiClient } from "./httpClient";



export type CentreAffectationDto = {

  id: number;

  idCentre: number;

  nomCentre: string;

};



export type ConcoursDto = {

  numeroConcours: string;

  nomConcours: string;

  dateHeureExamen: string;

  centres: CentreAffectationDto[];

  creeLe: string;

  modifieLe: string;

};



export type CentreAffectationWrite = {

  idCentre: number;

  nomCentre: string;

};



export type ConcoursWritePayload = {

  nomConcours: string;

  numeroConcours: string;

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



export async function updateConcours(

  numeroConcours: string,

  payload: ConcoursWritePayload,

): Promise<ConcoursDto> {

  const { data } = await apiClient.put<ConcoursDto>(

    `/api/concours/${encodeURIComponent(numeroConcours)}`,

    payload,

  );

  return data;

}



export async function deleteConcours(numeroConcours: string): Promise<void> {

  await apiClient.delete(`/api/concours/${encodeURIComponent(numeroConcours)}`);

}

