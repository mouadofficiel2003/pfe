package com.pfe.candidats.service;

import com.pfe.candidats.remote.LieuxSalleRemoteClient;
import com.pfe.candidats.remote.dto.SalleLieuxHeadJson;
import com.pfe.candidats.support.HttpRequestContext;
import com.pfe.candidats.web.dto.CandidatUpdateRequest;
import java.util.Objects;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

/** Vérifie les références lieux (salle) une fois le concours résolu. */
@Component
public class CandidatRemoteRefsValidator {

    private final LieuxSalleRemoteClient lieuxSalleRemoteClient;

    public CandidatRemoteRefsValidator(LieuxSalleRemoteClient lieuxSalleRemoteClient) {
        this.lieuxSalleRemoteClient = lieuxSalleRemoteClient;
    }

    public Optional<SalleLieuxHeadJson> validateLieux(CandidatUpdateRequest req, ResolvedConcours concours) {
        String auth = HttpRequestContext.authorizationHeaderOrNull();

        boolean hasCentre = req.idCentre() != null;
        boolean hasEtab = req.idEtablissement() != null;
        boolean hasSalle = req.idSalle() != null;

        if (!hasSalle && (hasCentre || hasEtab)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Affectation lieu : précisez la salle (centre ou établissement sans salle est invalide)");
        }

        if (!hasSalle) {
            if (req.numeroPlace() != null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Numéro de place sans salle : affectez une salle ou effacez le numéro de place");
            }
            return Optional.empty();
        }

        SalleLieuxHeadJson salle = lieuxSalleRemoteClient.fetchSalle(req.idSalle(), auth);

        if (hasCentre && !Objects.equals(req.idCentre(), salle.centreId())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Le centre indiqué ne correspond pas au centre de la salle " + req.idSalle());
        }
        if (hasEtab && !Objects.equals(req.idEtablissement(), salle.etablissementId())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "L'établissement indiqué ne correspond pas à l'établissement de la salle " + req.idSalle());
        }

        String salleConcours = salle.numeroConcours();
        if (salleConcours != null
                && !salleConcours.isBlank()
                && !Objects.equals(concours.numeroConcours(), salleConcours)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La salle est réservée au concours "
                            + salleConcours
                            + " : le candidat doit être inscrit au même concours");
        }

        if (req.numeroPlace() != null && req.numeroPlace() > salle.nombrePlaces()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Numéro de place "
                            + req.numeroPlace()
                            + " dépasse la capacité de la salle ("
                            + salle.nombrePlaces()
                            + " places)");
        }

        return Optional.of(salle);
    }
}
