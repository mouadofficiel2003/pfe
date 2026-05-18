package com.pfe.candidats.remote.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ConcoursHeadJson(Long id, String nomConcours) {}
