package com.pfe.concours.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CentreAffectationRequest(
        @NotNull Long idCentre,
        @NotBlank @Size(max = 200) String nomCentre) {}
