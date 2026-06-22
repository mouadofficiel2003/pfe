package com.pfe.convocation.service;

import java.time.Instant;

/**
 * Données complètes d'une convocation d'un candidat, agrégées depuis candidat-service,
 * concours-service et lieux-service. Sert à la fois à la génération du PDF, à l'aperçu API
 * et à l'envoi par e-mail.
 */
public record ConvocationData(
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
        Integer numeroPlace) {

    public String nomComplet() {
        String p = prenom == null ? "" : prenom;
        String n = nom == null ? "" : nom;
        return (p + " " + n).trim();
    }
}
