package com.pfe.lieux.concours;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ConcoursHeadJson(String numeroConcours, String nomConcours) {}
