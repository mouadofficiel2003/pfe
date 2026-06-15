package com.pfe.candidats.web.dto;



import java.time.Instant;



/**

 * Contrat JSON API (camelCase, défaut Jackson). Colonnes SQL : snake_case, mappées sur l'entité {@code Candidat} via {@code @Column}.

 */

public record CandidatResponse(

        String numeroInscription,

        String nom,

        String prenom,

        String cin,

        String numeroTelephone,

        String ville,

        int age,

        String email,

        String specialite,

        String nomConcours,

        String numeroConcours,

        Long idCentre,

        Long idEtablissement,

        Long idSalle,

        Integer numeroPlace,

        Instant creeLe,

        Instant modifieLe) {}


