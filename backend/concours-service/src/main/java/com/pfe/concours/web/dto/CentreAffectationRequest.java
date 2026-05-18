package com.pfe.concours.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CentreAffectationRequest(
        @NotBlank @Size(max = 200) String nomCentre,
        Long centreId) {}
