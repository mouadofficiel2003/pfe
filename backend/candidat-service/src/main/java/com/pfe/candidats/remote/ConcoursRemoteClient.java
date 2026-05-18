package com.pfe.candidats.remote;

import com.pfe.candidats.remote.dto.ConcoursHeadJson;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

@Component
public class ConcoursRemoteClient {

    private final RestClient concoursRestClient;

    public ConcoursRemoteClient(@Qualifier(RemoteClientsConfig.CONCOURS_REST_CLIENT) RestClient concoursRestClient) {
        this.concoursRestClient = concoursRestClient;
    }

    /**
     * GET /api/concours/{id}. Retourne le concours ou lève une {@link ResponseStatusException} si absent / service
     * indisponible.
     */
    /** GET /api/concours — catalogue pour résolution nom → id à l'import. */
    public List<ConcoursHeadJson> listConcours(String authorizationHeader) {
        requireBearer(authorizationHeader);
        try {
            ConcoursHeadJson[] body = concoursRestClient
                    .get()
                    .uri("/api/concours")
                    .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                    .retrieve()
                    .onStatus(
                            status -> status.value() == 401 || status.value() == 403,
                            (request, response) -> {
                                throw new ResponseStatusException(
                                        HttpStatus.BAD_GATEWAY,
                                        "Le service concours a refusé l'authentification");
                            })
                    .onStatus(
                            HttpStatusCode::is5xxServerError,
                            (request, response) -> {
                                throw new ResponseStatusException(
                                        HttpStatus.BAD_GATEWAY,
                                        "Le service concours a renvoyé une erreur");
                            })
                    .body(ConcoursHeadJson[].class);
            return body == null ? List.of() : Arrays.asList(body);
        } catch (ResourceAccessException e) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Service concours indisponible : " + e.getMessage());
        }
    }

    public ConcoursHeadJson fetchConcours(Long concoursId, String authorizationHeader) {
        requireBearer(authorizationHeader);
        try {
            return concoursRestClient
                    .get()
                    .uri("/api/concours/{id}", concoursId)
                    .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                    .retrieve()
                    .onStatus(
                            status -> status.value() == 404,
                            (request, response) -> {
                                throw new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST,
                                        "Le concours d'identifiant " + concoursId + " n'existe pas");
                            })
                    .onStatus(
                            status -> status.value() == 401 || status.value() == 403,
                            (request, response) -> {
                                throw new ResponseStatusException(
                                        HttpStatus.BAD_GATEWAY,
                                        "Le service concours a refusé l'authentification lors de la validation");
                            })
                    .onStatus(
                            HttpStatusCode::is5xxServerError,
                            (request, response) -> {
                                throw new ResponseStatusException(
                                        HttpStatus.BAD_GATEWAY,
                                        "Le service concours a renvoyé une erreur lors de la validation");
                            })
                    .body(ConcoursHeadJson.class);
        } catch (ResourceAccessException e) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Service concours indisponible : " + e.getMessage());
        }
    }

    private static void requireBearer(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "En-tête Authorization (Bearer) requis pour valider le concours auprès du service concours");
        }
        if (!authorizationHeader.regionMatches(true, 0, "Bearer ", 0, 7)) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "En-tête Authorization doit être de type Bearer pour valider le concours");
        }
    }
}
