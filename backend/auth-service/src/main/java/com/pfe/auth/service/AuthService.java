package com.pfe.auth.service;

import com.pfe.auth.domain.Utilisateur;
import com.pfe.auth.dto.LoginRequest;
import com.pfe.auth.dto.LoginResponse;
import com.pfe.auth.repository.UtilisateurRepository;
import com.pfe.auth.security.JwtService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UtilisateurRepository utilisateurRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {
        this.utilisateurRepository = utilisateurRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        Utilisateur user =
                utilisateurRepository
                        .findByNomUtilisateur(request.username())
                        .orElseThrow(() -> new BadCredentialsException("invalid"));
        if (!user.isActif()) {
            throw new BadCredentialsException("inactive");
        }
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("invalid");
        }
        String token = jwtService.generateAccessToken(user);
        return new LoginResponse(token, "Bearer", user.getNomUtilisateur(), user.getRole());
    }
}
