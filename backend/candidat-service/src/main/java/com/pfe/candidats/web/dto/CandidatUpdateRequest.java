package com.pfe.candidats.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CandidatUpdateRequest(
        @NotBlank @Size(max = 120) String nom,
        @NotBlank @Size(max = 120) String prenom,
        @NotBlank @Size(max = 32) String cin,
        @NotBlank @Size(max = 32) String numeroTelephone,
        @NotBlank @Size(max = 120) String ville,
        @NotNull @Min(10) @Max(120) Integer age,
        @NotBlank @Email @Size(max = 255) String email,
        @NotBlank @Size(max = 200) String specialite,
        @NotBlank @Size(max = 80) String numeroInscription,
        @NotBlank @Size(max = 200) String nomConcours,
        @Size(max = 80) String numeroConcours,
        Long idCentre,
        Long idEtablissement,
        Long idSalle,
        @Min(1) Integer numeroPlace) {}
