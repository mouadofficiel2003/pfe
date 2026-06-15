package com.pfe.lieux.web.dto;

public record SalleAvecLieuxResponse(
        Long idSalle,
        String nomSalle,
        int nombrePlaces,
        String numeroConcours,
        Long idEtablissement,
        String nomEtablissement,
        Long idCentre,
        String nomCentre) {}
