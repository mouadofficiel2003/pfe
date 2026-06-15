package com.pfe.repartition.web.dto;



public record AlerteResponse(

        String type,

        String numeroInscription,

        String candidatNom,

        String ville,

        String numeroConcours,

        String nomConcours,

        Long idCentre,

        String nomCentre,

        String message) {}

