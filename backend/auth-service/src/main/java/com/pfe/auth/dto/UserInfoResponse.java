package com.pfe.auth.dto;

import com.pfe.auth.domain.RoleUtilisateur;

public record UserInfoResponse(String username, RoleUtilisateur role) {}
