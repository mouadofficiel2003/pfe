package com.pfe.candidats.web.dto;



import java.util.List;



/** Bilan de l'application d'un lot d'affectations. */

public record CandidatAffectationBatchResponse(int misAJour, List<String> introuvables) {}

