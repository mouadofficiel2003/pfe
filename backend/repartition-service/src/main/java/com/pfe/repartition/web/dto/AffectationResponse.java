package com.pfe.repartition.web.dto;



public record AffectationResponse(

        String numeroInscription,

        String candidatNom,

        String ville,

        String numeroConcours,

        String nomConcours,

        Long idCentre,

        String nomCentre,

        Long idEtablissement,

        String nomEtablissement,

        Long idSalle,

        String nomSalle,

        Integer numeroPlace) {}

