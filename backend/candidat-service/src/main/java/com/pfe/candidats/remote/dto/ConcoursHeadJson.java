package com.pfe.candidats.remote.dto;



import com.fasterxml.jackson.annotation.JsonIgnoreProperties;



@JsonIgnoreProperties(ignoreUnknown = true)

public record ConcoursHeadJson(String numeroConcours, String nomConcours) {}

