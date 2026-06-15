package com.pfe.repartition.remote;



import com.pfe.repartition.remote.dto.SalleAvecLieuxJson;

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

public class LieuxRemoteClient {



    private final RestClient lieuxRestClient;



    public LieuxRemoteClient(@Qualifier(RemoteClientsConfig.LIEUX_REST_CLIENT) RestClient lieuxRestClient) {

        this.lieuxRestClient = lieuxRestClient;

    }



    /** GET /api/salles?numeroConcours={numero} — salles éligibles (avec centre/établissement et capacité). */

    public List<SalleAvecLieuxJson> listSallesParConcours(String numeroConcours, String authorizationHeader) {

        RemoteBearer.require(authorizationHeader, "lieux");

        try {

            SalleAvecLieuxJson[] body = lieuxRestClient

                    .get()

                    .uri(uriBuilder -> uriBuilder

                            .path("/api/salles")

                            .queryParam("numeroConcours", numeroConcours)

                            .build())

                    .header(HttpHeaders.AUTHORIZATION, authorizationHeader)

                    .retrieve()

                    .onStatus(

                            status -> status.value() == 401 || status.value() == 403,

                            (request, response) -> {

                                throw new ResponseStatusException(

                                        HttpStatus.BAD_GATEWAY, "Le service lieux a refusé l'authentification");

                            })

                    .onStatus(

                            HttpStatusCode::is5xxServerError,

                            (request, response) -> {

                                throw new ResponseStatusException(

                                        HttpStatus.BAD_GATEWAY, "Le service lieux a renvoyé une erreur");

                            })

                    .body(SalleAvecLieuxJson[].class);

            return body == null ? List.of() : Arrays.asList(body);

        } catch (ResourceAccessException e) {

            throw new ResponseStatusException(

                    HttpStatus.SERVICE_UNAVAILABLE, "Service lieux indisponible : " + e.getMessage());

        }

    }

}

