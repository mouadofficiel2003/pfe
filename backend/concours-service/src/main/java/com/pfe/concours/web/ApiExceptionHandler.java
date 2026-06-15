package com.pfe.concours.web;

import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

/**
 * Uniformise le format des erreurs ({@code {"message": ...}}) pour rester cohérent avec auth-service.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    ResponseEntity<Map<String, String>> responseStatus(ResponseStatusException ex) {
        HttpStatusCode status = ex.getStatusCode();
        String message = ex.getReason() != null ? ex.getReason() : status.toString();
        return ResponseEntity.status(status).body(Map.of("message", message));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<Map<String, String>> validation(MethodArgumentNotValidException ex) {
        FieldError first = ex.getBindingResult().getFieldErrors().stream().findFirst().orElse(null);
        String msg = first != null && first.getDefaultMessage() != null
                ? first.getDefaultMessage()
                : "Requête invalide.";
        return ResponseEntity.badRequest().body(Map.of("message", msg));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<Map<String, String>> dataIntegrity() {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("message", "Conflit : la ressource existe déjà ou viole une contrainte."));
    }
}
