package com.pfe.convocation.remote.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.Instant;

/** Vue partielle d'un concours (concours-service, GET /api/concours) : libellé + date/heure d'examen. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ConcoursJson(String numeroConcours, String nomConcours, Instant dateHeureExamen) {}
