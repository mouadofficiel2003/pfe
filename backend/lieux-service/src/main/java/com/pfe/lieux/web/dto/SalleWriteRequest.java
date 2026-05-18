package com.pfe.lieux.web.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SalleWriteRequest(
        @NotBlank @Size(max = 200) String nomSalle,
        @NotNull @Min(1) @Max(1_000_000) Integer nombrePlaces,
        Long concoursId) {}
