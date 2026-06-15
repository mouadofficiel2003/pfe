package com.pfe.repartition.remote.dto;



import com.fasterxml.jackson.annotation.JsonIgnoreProperties;



/** Vue partielle d'un candidat (candidat-service, GET /api/candidats). */

@JsonIgnoreProperties(ignoreUnknown = true)

public record CandidatJson(

        String numeroInscription,

        String nom,

        String prenom,

        String ville,

        String nomConcours,

        String numeroConcours) {}

