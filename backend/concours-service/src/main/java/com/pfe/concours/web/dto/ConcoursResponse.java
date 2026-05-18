package com.pfe.concours.web.dto;

import java.time.Instant;
import java.util.List;

public record ConcoursResponse(
        Long id,
        String nomConcours,
        String numeroConcours,
        Instant dateHeureExamen,
        List<CentreAffectationResponse> centres,
        Instant creeLe,
        Instant modifieLe) {}
