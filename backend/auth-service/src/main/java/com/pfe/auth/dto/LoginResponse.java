package com.pfe.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.pfe.auth.domain.RoleUtilisateur;

public record LoginResponse(
        @JsonProperty("accessToken") String accessToken,
        @JsonProperty("tokenType") String tokenType,
        @JsonProperty("username") String username,
        @JsonProperty("role") RoleUtilisateur role) {}
