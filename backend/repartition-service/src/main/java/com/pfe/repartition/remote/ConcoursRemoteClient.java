package com.pfe.repartition.remote;

import com.pfe.repartition.remote.dto.ConcoursJson;
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

    /** GET /api/concours — liste complète des concours avec leurs centres affectés. */
    public List<ConcoursJson> listConcours(String authorizationHeader) {
        RemoteBearer.require(authorizationHeader, "concours");
        try {
            ConcoursJson[] body = concoursRestClient
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
                                        HttpStatus.BAD_GATEWAY, "Le service concours a renvoyé une erreur");
                            })
                    .body(ConcoursJson[].class);
            return body == null ? List.of() : Arrays.asList(body);
        } catch (ResourceAccessException e) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE, "Service concours indisponible : " + e.getMessage());
        }
    }
}
