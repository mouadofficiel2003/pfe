package com.pfe.convocation.remote.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Salle avec son établissement et son centre (lieux-service, GET /api/salles?numeroConcours). */
@JsonIgnoreProperties(ignoreUnknown = true)
public record SalleAvecLieuxJson(
        Long idSalle,
        String nomSalle,
        int nombrePlaces,
        String numeroConcours,
        Long idEtablissement,
        String nomEtablissement,
        Long idCentre,
        String nomCentre) {}
