package com.pfe.convocation.support;

import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public final class HttpRequestContext {

    private HttpRequestContext() {}

    /** En-tête {@code Authorization} de la requête HTTP courante, ou {@code null}. */
    public static String authorizationHeaderOrNull() {
        var attrs = RequestContextHolder.getRequestAttributes();
        if (!(attrs instanceof ServletRequestAttributes sra)) {
            return null;
        }
        return sra.getRequest().getHeader(HttpHeaders.AUTHORIZATION);
    }

    /** Nom d'utilisateur authentifié (subject du JWT), ou {@code null}. */
    public static String currentUsernameOrNull() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null || auth.getName().isBlank()) {
            return null;
        }
        return auth.getName();
    }
}
