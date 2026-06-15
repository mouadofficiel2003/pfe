package com.pfe.repartition.remote.dto;



import com.fasterxml.jackson.annotation.JsonIgnoreProperties;



/** Salle éligible avec son établissement et son centre (lieux-service, GET /api/salles?numeroConcours). */

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

