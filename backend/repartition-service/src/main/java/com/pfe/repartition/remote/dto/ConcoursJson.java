package com.pfe.repartition.remote.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ConcoursJson(String numeroConcours, String nomConcours) {}
