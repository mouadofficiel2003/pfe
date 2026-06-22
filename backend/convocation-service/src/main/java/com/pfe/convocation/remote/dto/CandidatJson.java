package com.pfe.convocation.remote.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Vue partielle d'un candidat (candidat-service, GET /api/candidats), avec son affectation
 * issue de la répartition (centre / établissement / salle / place) et son adresse e-mail.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record CandidatJson(
        String numeroInscription,
        String nom,
        String prenom,
        String email,
        String nomConcours,
        String numeroConcours,
        Long idCentre,
        Long idEtablissement,
        Long idSalle,
        Integer numeroPlace) {

    /** Le candidat a une affectation complète (placé lors de la répartition). */
    public boolean estAffecte() {
        return idCentre != null && idEtablissement != null && idSalle != null && numeroPlace != null;
    }
}
