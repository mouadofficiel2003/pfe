package com.pfe.candidats.web.dto;



import jakarta.validation.Valid;

import jakarta.validation.constraints.Min;

import jakarta.validation.constraints.NotBlank;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;



/**

 * Lot d'affectations calculé par repartition-service. Chaque ligne reporte sur un candidat son centre,

 * établissement, salle et numéro de place. Les valeurs sont supposées déjà cohérentes (capacité validée

 * en amont par la répartition) : ce chemin d'écriture est volontairement léger.

 */

public record CandidatAffectationBatchRequest(@NotEmpty @Valid List<Item> affectations) {



    public record Item(

            @NotBlank String numeroInscription,

            Long idCentre,

            Long idEtablissement,

            Long idSalle,

            @Min(1) Integer numeroPlace) {}

}

