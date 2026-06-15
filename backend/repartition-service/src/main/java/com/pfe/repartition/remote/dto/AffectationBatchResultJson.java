package com.pfe.repartition.remote.dto;



import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;



/** Réponse de candidat-service après application du lot d'affectations. */

@JsonIgnoreProperties(ignoreUnknown = true)

public record AffectationBatchResultJson(int misAJour, List<String> introuvables) {}

