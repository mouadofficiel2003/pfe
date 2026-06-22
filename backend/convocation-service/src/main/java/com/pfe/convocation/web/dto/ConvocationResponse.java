package com.pfe.convocation.web.dto;

import java.time.Instant;

/** Aperçu d'une convocation prête (contrat JSON, camelCase). */
public record ConvocationResponse(
        String numeroInscription,
        String nom,
        String prenom,
        String email,
        String numeroConcours,
        String nomConcours,
        String nomCentre,
        String nomEtablissement,
        String nomSalle,
        Instant dateHeureExamen,
        Integer numeroPlace) {}
