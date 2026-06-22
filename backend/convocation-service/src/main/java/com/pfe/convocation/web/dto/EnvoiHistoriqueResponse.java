package com.pfe.convocation.web.dto;

import java.time.Instant;

/** Ligne d'historique d'un envoi de convocation (consultation). */
public record EnvoiHistoriqueResponse(
        Long id,
        String numeroInscription,
        String candidatNom,
        String email,
        String numeroConcours,
        String nomConcours,
        String statut,
        String message,
        String declenchePar,
        Instant envoyeLe) {}
