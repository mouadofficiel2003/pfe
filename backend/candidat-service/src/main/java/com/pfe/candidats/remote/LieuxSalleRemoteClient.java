package com.pfe.candidats.remote;

import com.pfe.candidats.remote.dto.SalleLieuxHeadJson;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

@Component
public class LieuxSalleRemoteClient {

    private final RestClient lieuxRestClient;

    public LieuxSalleRemoteClient(@Qualifier(RemoteClientsConfig.LIEUX_REST_CLIENT) RestClient lieuxRestClient) {
        this.lieuxRestClient = lieuxRestClient;
    }

    /** GET /api/salles/{id} */
    public SalleLieuxHeadJson fetchSalle(Long salleId, String authorizationHeader) {
        requireBearer(authorizationHeader);
        try {
            return lieuxRestClient
                    .get()
                    .uri("/api/salles/{id}", salleId)
                    .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                    .retrieve()
                    .onStatus(
                            status -> status.value() == 404,
                            (request, response) -> {
                                throw new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST,
                                        "La salle d'identifiant " + salleId + " n'existe pas dans le service lieux");
                            })
                    .onStatus(
                            status -> status.value() == 401 || status.value() == 403,
                            (request, response) -> {
                                throw new ResponseStatusException(
                                        HttpStatus.BAD_GATEWAY,
                                        "Le service lieux a refusé l'authentification lors de la validation de la salle");
                            })
                    .onStatus(
                            HttpStatusCode::is5xxServerError,
                            (request, response) -> {
                                throw new ResponseStatusException(
                                        HttpStatus.BAD_GATEWAY,
                                        "Le service lieux a renvoyé une erreur lors de la validation de la salle");
                            })
                    .body(SalleLieuxHeadJson.class);
        } catch (ResourceAccessException e) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Service lieux indisponible : " + e.getMessage());
        }
    }

    private static void requireBearer(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "En-tête Authorization (Bearer) requis pour valider la salle auprès du service lieux");
        }
        if (!authorizationHeader.regionMatches(true, 0, "Bearer ", 0, 7)) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "En-tête Authorization doit être de type Bearer pour valider la salle");
        }
    }
}
