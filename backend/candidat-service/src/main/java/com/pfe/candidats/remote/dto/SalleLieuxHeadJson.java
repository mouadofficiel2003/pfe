package com.pfe.candidats.remote.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SalleLieuxHeadJson(
        Long id,
        String nomSalle,
        int nombrePlaces,
        String numeroConcours,
        Long etablissementId,
        String nomEtablissement,
        Long centreId,
        String nomCentre) {}
