package com.pfe.candidats.support;

import org.springframework.http.HttpHeaders;
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
}
