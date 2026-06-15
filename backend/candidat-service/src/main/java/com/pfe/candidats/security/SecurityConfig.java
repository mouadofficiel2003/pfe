package com.pfe.candidats.security;

import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .httpBasic(b -> b.disable())
                .formLogin(f -> f.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(e -> e.authenticationEntryPoint(
                        new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**")
                        .permitAll()
                        // Inclure "/api/candidats" sans sous-chemin : avec PathPattern, "/api/candidats/**"
                        // peut ne pas matcher la racine exacte, ce qui provoquait un 403 (denyAll).
                        .requestMatchers(HttpMethod.GET, "/api/candidats", "/api/candidats/**")
                        .hasAnyRole("ADMINISTRATEUR", "GESTIONNAIRE")
                        .requestMatchers(HttpMethod.POST, "/api/candidats", "/api/candidats/**")
                        .hasRole("GESTIONNAIRE")
                        .requestMatchers(HttpMethod.PUT, "/api/candidats", "/api/candidats/**")
                        .hasRole("GESTIONNAIRE")
                        .requestMatchers(HttpMethod.PATCH, "/api/candidats", "/api/candidats/**")
                        .hasRole("GESTIONNAIRE")
                        .requestMatchers(HttpMethod.DELETE, "/api/candidats", "/api/candidats/**")
                        .hasRole("GESTIONNAIRE")
                        .anyRequest()
                        .denyAll())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:5173"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
