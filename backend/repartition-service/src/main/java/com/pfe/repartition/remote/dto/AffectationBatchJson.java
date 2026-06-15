package com.pfe.repartition.remote.dto;



import java.util.List;



/** Corps envoyé à candidat-service (PATCH /api/candidats/affectations). */

public record AffectationBatchJson(List<Item> affectations) {



    public record Item(

            String numeroInscription, Long idCentre, Long idEtablissement, Long idSalle, Integer numeroPlace) {}

}

