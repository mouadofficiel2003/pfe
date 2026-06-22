package com.pfe.convocation.web;

import java.util.Map;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

/**
 * Uniformise le format des erreurs ({@code {"message": ...}}) pour rester cohérent avec les autres services.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    ResponseEntity<Map<String, String>> responseStatus(ResponseStatusException ex) {
        HttpStatusCode status = ex.getStatusCode();
        String message = ex.getReason() != null ? ex.getReason() : status.toString();
        return ResponseEntity.status(status).body(Map.of("message", message));
    }
}
