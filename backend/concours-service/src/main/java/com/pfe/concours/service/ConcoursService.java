package com.pfe.concours.service;

import com.pfe.concours.domain.Concours;
import com.pfe.concours.domain.ConcoursAffectationCentre;
import com.pfe.concours.lieux.LieuxCentreClient;
import com.pfe.concours.repository.ConcoursRepository;
import com.pfe.concours.web.dto.ConcoursHeadResponse;
import com.pfe.concours.support.HttpRequestContext;
import com.pfe.concours.web.dto.CentreAffectationRequest;
import com.pfe.concours.web.dto.CentreAffectationResponse;
import com.pfe.concours.web.dto.ConcoursResponse;
import com.pfe.concours.web.dto.ConcoursWriteRequest;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ConcoursService {

    private final ConcoursRepository concoursRepository;
    private final LieuxCentreClient lieuxCentreClient;

    public ConcoursService(ConcoursRepository concoursRepository, LieuxCentreClient lieuxCentreClient) {
        this.concoursRepository = concoursRepository;
        this.lieuxCentreClient = lieuxCentreClient;
    }

    @Transactional(readOnly = true)
    public List<ConcoursResponse> listerTous() {
        return concoursRepository.findAllWithAffectationsOrderByDateDesc().stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Concours planifiés dans un centre (affectations avec {@code centre_id} renseigné).
     * Pas d'appel HTTP vers lieux-service : évite un deadlock avec {@code GET /api/centres}
     * (lieux appelle ce endpoint pendant la liste des centres).
     */
    @Transactional(readOnly = true)
    public List<ConcoursHeadResponse> listerEnTeteParCentre(Long centreId) {
        return concoursRepository.findDistinctByAffectationCentreId(centreId).stream()
                .map(c -> new ConcoursHeadResponse(c.getId(), c.getNomConcours()))
                .toList();
    }

    @Transactional(readOnly = true)
    public ConcoursResponse obtenir(Long id) {
        Concours c = concoursRepository
                .findByIdWithAffectations(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Concours introuvable"));
        return toResponse(c);
    }

    @Transactional
    public ConcoursResponse creer(ConcoursWriteRequest req) {
        validerCentresDistincts(req.centres());
        validerCentresExistantsDansLieux(req.centres());
        Instant now = Instant.now();
        Concours c = new Concours();
        c.setNomConcours(req.nomConcours().trim());
        c.setNumeroConcours(blankToNull(req.numeroConcours()));
        c.setDateHeureExamen(req.dateHeureExamen());
        c.setCreeLe(now);
        c.setModifieLe(now);
        assertNumeroConcoursLibre(c.getNumeroConcours(), null);
        c.setAffectationsCentres(mapAffectations(req.centres()));
        c.lierAffectations();
        try {
            return toResponse(concoursRepository.save(c));
        } catch (DataIntegrityViolationException e) {
            throw conflictNumeroConcours();
        }
    }

    @Transactional
    public ConcoursResponse mettreAJour(Long id, ConcoursWriteRequest req) {
        validerCentresDistincts(req.centres());
        validerCentresExistantsDansLieux(req.centres());
        Concours c = concoursRepository
                .findByIdWithAffectations(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Concours introuvable"));
        assertNumeroConcoursLibre(blankToNull(req.numeroConcours()), id);
        c.setNomConcours(req.nomConcours().trim());
        c.setNumeroConcours(blankToNull(req.numeroConcours()));
        c.setDateHeureExamen(req.dateHeureExamen());
        c.setModifieLe(Instant.now());
        c.getAffectationsCentres().clear();
        c.getAffectationsCentres().addAll(mapAffectations(req.centres()));
        c.lierAffectations();
        try {
            return toResponse(concoursRepository.save(c));
        } catch (DataIntegrityViolationException e) {
            throw conflictNumeroConcours();
        }
    }

    @Transactional
    public void supprimer(Long id) {
        if (!concoursRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Concours introuvable");
        }
        concoursRepository.deleteById(id);
    }

    private static void validerCentresDistincts(List<CentreAffectationRequest> centres) {
        Set<String> vus = new HashSet<>();
        for (CentreAffectationRequest a : centres) {
            String key = a.nomCentre().trim().toLowerCase();
            if (!vus.add(key)) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Liste des centres : doublon sur le nom « " + a.nomCentre().trim() + " »");
            }
        }
    }

    /** Pour chaque {@code centreId} non nul, vérifie que le centre existe dans lieux-service (même JWT que l'appelant). */
    private void validerCentresExistantsDansLieux(List<CentreAffectationRequest> centres) {
        String auth = HttpRequestContext.authorizationHeaderOrNull();
        Set<Long> idsDistincts =
                centres.stream().map(CentreAffectationRequest::centreId).filter(Objects::nonNull).collect(Collectors.toSet());
        for (Long centreId : idsDistincts) {
            lieuxCentreClient.assertCentreExists(centreId, auth);
        }
    }

    private void assertNumeroConcoursLibre(String numero, Long excludeId) {
        if (numero == null) {
            return;
        }
        boolean taken =
                excludeId == null
                        ? concoursRepository.existsByNumeroConcours(numero)
                        : concoursRepository.existsByNumeroConcoursAndIdNot(numero, excludeId);
        if (taken) {
            throw conflictNumeroConcours();
        }
    }

    private static ResponseStatusException conflictNumeroConcours() {
        return new ResponseStatusException(HttpStatus.CONFLICT, "Ce numéro de concours est déjà utilisé");
    }

    private static String blankToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private static List<ConcoursAffectationCentre> mapAffectations(List<CentreAffectationRequest> centres) {
        List<ConcoursAffectationCentre> list = new ArrayList<>();
        for (CentreAffectationRequest r : centres) {
            ConcoursAffectationCentre a = new ConcoursAffectationCentre();
            a.setNomCentre(r.nomCentre().trim());
            a.setCentreId(r.centreId());
            list.add(a);
        }
        return list;
    }

    private ConcoursResponse toResponse(Concours c) {
        List<CentreAffectationResponse> centres =
                c.getAffectationsCentres().stream()
                        .map(a -> new CentreAffectationResponse(a.getId(), a.getNomCentre(), a.getCentreId()))
                        .toList();
        return new ConcoursResponse(
                c.getId(),
                c.getNomConcours(),
                c.getNumeroConcours(),
                c.getDateHeureExamen(),
                centres,
                c.getCreeLe(),
                c.getModifieLe());
    }
}
