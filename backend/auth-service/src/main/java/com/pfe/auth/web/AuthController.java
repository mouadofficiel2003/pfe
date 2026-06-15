package com.pfe.auth.web;

import com.pfe.auth.domain.RoleUtilisateur;
import com.pfe.auth.dto.LoginRequest;
import com.pfe.auth.dto.LoginResponse;
import com.pfe.auth.dto.UserInfoResponse;
import com.pfe.auth.repository.UtilisateurRepository;
import com.pfe.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final UtilisateurRepository utilisateurRepository;

    public AuthController(AuthService authService, UtilisateurRepository utilisateurRepository) {
        this.authService = authService;
        this.utilisateurRepository = utilisateurRepository;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    public UserInfoResponse me(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentification requise");
        }
        String username = authentication.getName();
        RoleUtilisateur role =
                utilisateurRepository
                        .findByNomUtilisateur(username)
                        .map(u -> u.getRole())
                        .orElseThrow(
                                () ->
                                        new ResponseStatusException(
                                                HttpStatus.UNAUTHORIZED,
                                                "L'utilisateur associé au jeton n'existe plus"));
        return new UserInfoResponse(username, role);
    }
}
