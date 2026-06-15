package com.pfe.repartition.web.dto;

import java.time.Instant;

/** Vue allégée d'un run (liste de l'historique). */
public record RepartitionRunSummaryResponse(
        Long id,
        String declenchePar,
        String statut,
        int totalCandidats,
        int totalAffectes,
        int totalAlertes,
        Instant demarreLe,
        Instant termineLe,
        String message) {}
