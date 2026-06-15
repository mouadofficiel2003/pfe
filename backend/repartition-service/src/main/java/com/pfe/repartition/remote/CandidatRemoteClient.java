package com.pfe.repartition.remote;

import com.pfe.repartition.remote.dto.AffectationBatchJson;
import com.pfe.repartition.remote.dto.AffectationBatchResultJson;
import com.pfe.repartition.remote.dto.CandidatJson;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

@Component
public class CandidatRemoteClient {

    private final RestClient candidatRestClient;

    public CandidatRemoteClient(@Qualifier(RemoteClientsConfig.CANDIDAT_REST_CLIENT) RestClient candidatRestClient) {
        this.candidatRestClient = candidatRestClient;
    }

    /** GET /api/candidats — catalogue complet des candidats (filtré par concours côté répartition). */
    public List<CandidatJson> listCandidats(String authorizationHeader) {
        RemoteBearer.require(authorizationHeader, "candidat");
        try {
            CandidatJson[] body = candidatRestClient
                    .get()
                    .uri("/api/candidats")
                    .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                    .retrieve()
                    .onStatus(
                            status -> status.value() == 401 || status.value() == 403,
                            (request, response) -> {
                                throw new ResponseStatusException(
                                        HttpStatus.BAD_GATEWAY, "Le service candidat a refusé l'authentification");
                            })
                    .onStatus(
                            HttpStatusCode::is5xxServerError,
                            (request, response) -> {
                                throw new ResponseStatusException(
                                        HttpStatus.BAD_GATEWAY, "Le service candidat a renvoyé une erreur");
                            })
                    .body(CandidatJson[].class);
            return body == null ? List.of() : Arrays.asList(body);
        } catch (ResourceAccessException e) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE, "Service candidat indisponible : " + e.getMessage());
        }
    }

    /** PATCH /api/candidats/affectations — écrit l'affectation (centre/établissement/salle/place) sur les candidats. */
    public AffectationBatchResultJson appliquerAffectations(AffectationBatchJson body, String authorizationHeader) {
        RemoteBearer.require(authorizationHeader, "candidat");
        if (body == null || body.affectations() == null || body.affectations().isEmpty()) {
            return new AffectationBatchResultJson(0, List.of());
        }
        try {
            return candidatRestClient
                    .patch()
                    .uri("/api/candidats/affectations")
                    .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .onStatus(
                            status -> status.value() == 401 || status.value() == 403,
                            (request, response) -> {
                                throw new ResponseStatusException(
                                        HttpStatus.BAD_GATEWAY,
                                        "Le service candidat a refusé l'authentification lors de l'écriture des affectations");
                            })
                    .onStatus(
                            HttpStatusCode::is5xxServerError,
                            (request, response) -> {
                                throw new ResponseStatusException(
                                        HttpStatus.BAD_GATEWAY,
                                        "Le service candidat a renvoyé une erreur lors de l'écriture des affectations");
                            })
                    .body(AffectationBatchResultJson.class);
        } catch (ResourceAccessException e) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE, "Service candidat indisponible : " + e.getMessage());
        }
    }
}
