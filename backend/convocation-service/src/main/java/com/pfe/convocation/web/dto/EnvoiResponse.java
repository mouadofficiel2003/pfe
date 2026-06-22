package com.pfe.convocation.web.dto;

import java.time.Instant;
import java.util.List;

/** Synthèse de l'envoi groupé (« envoyer toutes les convocations »). */
public record EnvoiResponse(
        int total,
        int envoyes,
        int echecs,
        Instant lanceLe,
        List<EnvoiDetailResponse> details) {}
