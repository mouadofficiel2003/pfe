package com.pfe.concours.web.dto;

import java.time.Instant;
import java.util.List;

public record ConcoursResponse(
        String numeroConcours,
        String nomConcours,
        Instant dateHeureExamen,
        List<CentreAffectationResponse> centres,
        Instant creeLe,
        Instant modifieLe) {}
