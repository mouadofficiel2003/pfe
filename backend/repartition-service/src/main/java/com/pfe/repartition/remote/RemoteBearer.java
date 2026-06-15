package com.pfe.repartition.remote;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/** Validation commune de l'en-tête Authorization (Bearer) relayé aux services aval. */
final class RemoteBearer {

    private RemoteBearer() {}

    static void require(String authorizationHeader, String service) {
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "En-tête Authorization (Bearer) requis pour appeler le service " + service);
        }
        if (!authorizationHeader.regionMatches(true, 0, "Bearer ", 0, 7)) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "En-tête Authorization doit être de type Bearer pour appeler le service " + service);
        }
    }
}
