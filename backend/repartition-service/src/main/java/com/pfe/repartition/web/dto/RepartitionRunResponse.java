package com.pfe.repartition.web.dto;

import java.time.Instant;
import java.util.List;

/** Synthèse complète d'un run (affectations réussies + alertes). */
public record RepartitionRunResponse(
        Long id,
        String declenchePar,
        String statut,
        int totalCandidats,
        int totalAffectes,
        int totalAlertes,
        Instant demarreLe,
        Instant termineLe,
        String message,
        List<AffectationResponse> affectations,
        List<AlerteResponse> alertes) {}
