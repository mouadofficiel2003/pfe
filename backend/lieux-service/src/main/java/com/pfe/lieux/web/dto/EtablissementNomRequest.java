package com.pfe.lieux.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EtablissementNomRequest(@NotBlank @Size(max = 200) String nomEtablissement) {}
