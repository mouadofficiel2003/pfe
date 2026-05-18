package com.pfe.lieux.concours;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ConcoursHeadJson(Long id, String nomConcours) {}
