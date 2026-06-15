package com.pfe.concours.lieux;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

/**
 * Vérifie auprès de lieux-service qu'un centre existe (cohérence des {@code id_centre}).
 */
@Component
public class LieuxCentreClient {

    private final RestClient lieuxRestClient;

    public LieuxCentreClient(RestClient lieuxRestClient) {
        this.lieuxRestClient = lieuxRestClient;
    }

    /**
     * Appelle {@code GET /api/centres/{id}} sur lieux-service. Nécessite le même JWT que la requête utilisateur.
     *
     * @param idCentre identifiant {@code lieux.centre.id_centre}
     * @param authorizationHeader valeur complète de l'en-tête {@code Authorization} (ex. {@code Bearer …})
     */
    public void assertCentreExists(Long idCentre, String authorizationHeader) {
        if (idCentre == null) {
            return;
        }
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "En-tête Authorization (Bearer) requis pour valider le centre auprès du service lieux");
        }
        if (!authorizationHeader.regionMatches(true, 0, "Bearer ", 0, 7)) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "En-tête Authorization doit être de type Bearer pour valider le centre auprès du service lieux");
        }
        try {
            lieuxRestClient
                    .get()
                    .uri("/api/centres/{id}", idCentre)
                    .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                    .retrieve()
                    .onStatus(
                            status -> status.value() == 404,
                            (request, response) -> {
                                throw new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST,
                                        "Le centre d'identifiant "
                                                + idCentre
                                                + " n'existe pas dans le service lieux");
                            })
                    .onStatus(
                            status -> status.value() == 401 || status.value() == 403,
                            (request, response) -> {
                                throw new ResponseStatusException(
                                        HttpStatus.BAD_GATEWAY,
                                        "Le service lieux a refusé l'authentification lors de la validation du centre");
                            })
                    .onStatus(
                            HttpStatusCode::is5xxServerError,
                            (request, response) -> {
                                throw new ResponseStatusException(
                                        HttpStatus.BAD_GATEWAY,
                                        "Le service lieux a renvoyé une erreur lors de la validation du centre");
                            })
                    .toBodilessEntity();
        } catch (ResourceAccessException e) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Service lieux indisponible : " + e.getMessage());
        }
    }
}
