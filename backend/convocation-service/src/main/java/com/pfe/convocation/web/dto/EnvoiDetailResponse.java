package com.pfe.convocation.web.dto;

/** Résultat de l'envoi de la convocation d'un candidat (succès ou échec). */
public record EnvoiDetailResponse(
        String numeroInscription,
        String candidatNom,
        String email,
        String statut,
        String message) {}
