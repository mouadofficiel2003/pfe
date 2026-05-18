package com.pfe.lieux.web.dto;

public record SalleAvecLieuxResponse(
        Long id,
        String nomSalle,
        int nombrePlaces,
        Long concoursId,
        Long etablissementId,
        String nomEtablissement,
        Long centreId,
        String nomCentre) {}
