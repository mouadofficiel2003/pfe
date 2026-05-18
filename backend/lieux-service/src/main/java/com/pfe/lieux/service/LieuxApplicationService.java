package com.pfe.lieux.service;

import com.pfe.lieux.domain.Centre;
import com.pfe.lieux.domain.Etablissement;
import com.pfe.lieux.domain.Salle;
import com.pfe.lieux.concours.ConcoursExistenceClient;
import com.pfe.lieux.support.HttpRequestContext;
import com.pfe.lieux.repository.CentreRepository;
import com.pfe.lieux.repository.EtablissementRepository;
import com.pfe.lieux.repository.SalleRepository;
import com.pfe.lieux.web.dto.CentreDetailResponse;
import com.pfe.lieux.web.dto.CentreListItemResponse;
import com.pfe.lieux.web.dto.CentreNomRequest;
import com.pfe.lieux.web.dto.EtablissementDetailResponse;
import com.pfe.lieux.web.dto.EtablissementNomRequest;
import com.pfe.lieux.web.dto.SalleAvecLieuxResponse;
import com.pfe.lieux.web.dto.SalleResponse;
import com.pfe.lieux.web.dto.SalleWriteRequest;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class LieuxApplicationService {

    private final CentreRepository centreRepository;
    private final EtablissementRepository etablissementRepository;
    private final SalleRepository salleRepository;
    private final ConcoursExistenceClient concoursExistenceClient;

    public LieuxApplicationService(
            CentreRepository centreRepository,
            EtablissementRepository etablissementRepository,
            SalleRepository salleRepository,
            ConcoursExistenceClient concoursExistenceClient) {
        this.centreRepository = centreRepository;
        this.etablissementRepository = etablissementRepository;
        this.salleRepository = salleRepository;
        this.concoursExistenceClient = concoursExistenceClient;
    }

    public List<CentreListItemResponse> listerCentres() {
        List<Centre> centres = listerCentresDepuisBase();
        return centres.stream()
                .map(c -> new CentreListItemResponse(
                        c.getId(),
                        c.getNomCentre(),
                        etablissementRepository.countByCentreId(c.getId()),
                        concoursIdsPourCentre(c.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    List<Centre> listerCentresDepuisBase() {
        return centreRepository.findAllOrderByNom();
    }

    @Transactional(readOnly = true)
    public CentreDetailResponse obtenirCentre(Long id) {
        Centre centre = centreRepository
                .findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Centre introuvable"));
        List<EtablissementDetailResponse> etabs = etablissementRepository.findByCentreIdWithSalles(centre.getId()).stream()
                .sorted(Comparator.comparing(Etablissement::getNomEtablissement, String.CASE_INSENSITIVE_ORDER))
                .map(this::toEtablissementDetail)
                .toList();
        return new CentreDetailResponse(centre.getId(), centre.getNomCentre(), concoursIdsPourCentre(centre.getId()), etabs);
    }

    @Transactional
    public CentreDetailResponse creerCentre(CentreNomRequest req) {
        String nom = req.nomCentre().trim();
        assertNomCentreLibre(nom, null);
        Instant now = Instant.now();
        Centre c = new Centre();
        c.setNomCentre(nom);
        c.setCreeLe(now);
        c.setModifieLe(now);
        try {
            centreRepository.save(c);
        } catch (DataIntegrityViolationException e) {
            throw conflitNomCentre();
        }
        return obtenirCentre(c.getId());
    }

    @Transactional
    public CentreDetailResponse mettreAJourCentre(Long id, CentreNomRequest req) {
        Centre centre = centreRepository
                .findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Centre introuvable"));
        String nom = req.nomCentre().trim();
        assertNomCentreLibre(nom, id);
        centre.setNomCentre(nom);
        centre.setModifieLe(Instant.now());
        try {
            centreRepository.save(centre);
        } catch (DataIntegrityViolationException e) {
            throw conflitNomCentre();
        }
        return obtenirCentre(id);
    }

    @Transactional
    public void supprimerCentre(Long id) {
        if (!centreRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Centre introuvable");
        }
        centreRepository.deleteById(id);
    }

    @Transactional
    public EtablissementDetailResponse creerEtablissement(Long centreId, EtablissementNomRequest req) {
        Centre centre = centreRepository
                .findById(centreId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Centre introuvable"));
        String nom = req.nomEtablissement().trim();
        if (etablissementRepository.existsByCentreIdAndNomEtablissementIgnoreCase(centreId, nom)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Un établissement avec ce nom existe déjà dans ce centre");
        }
        Instant now = Instant.now();
        Etablissement e = new Etablissement();
        e.setCentre(centre);
        e.setNomEtablissement(nom);
        e.setCreeLe(now);
        e.setModifieLe(now);
        try {
            etablissementRepository.save(e);
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Un établissement avec ce nom existe déjà dans ce centre");
        }
        return toEtablissementDetail(etablissementRepository.findByIdWithSalles(e.getId()).orElse(e));
    }

    @Transactional(readOnly = true)
    public EtablissementDetailResponse obtenirEtablissement(Long id) {
        Etablissement e = etablissementRepository
                .findByIdWithSalles(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Établissement introuvable"));
        return toEtablissementDetail(e);
    }

    @Transactional
    public EtablissementDetailResponse mettreAJourEtablissement(Long id, EtablissementNomRequest req) {
        Etablissement e = etablissementRepository
                .findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Établissement introuvable"));
        Long centreId = e.getCentre().getId();
        String nom = req.nomEtablissement().trim();
        if (etablissementRepository.existsByCentreIdAndNomEtablissementIgnoreCaseAndIdNot(centreId, nom, id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Un établissement avec ce nom existe déjà dans ce centre");
        }
        e.setNomEtablissement(nom);
        e.setModifieLe(Instant.now());
        try {
            etablissementRepository.save(e);
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Un établissement avec ce nom existe déjà dans ce centre");
        }
        return obtenirEtablissement(id);
    }

    @Transactional
    public void supprimerEtablissement(Long id) {
        if (!etablissementRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Établissement introuvable");
        }
        etablissementRepository.deleteById(id);
    }

    @Transactional
    public SalleResponse creerSalle(Long etablissementId, SalleWriteRequest req) {
        Etablissement etab = etablissementRepository
                .findById(etablissementId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Établissement introuvable"));
        String nom = req.nomSalle().trim();
        if (salleRepository.existsByEtablissementIdAndNomSalleIgnoreCase(etablissementId, nom)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Une salle avec ce nom existe déjà dans cet établissement");
        }
        validerConcoursId(req.concoursId());
        Instant now = Instant.now();
        Salle s = new Salle();
        s.setEtablissement(etab);
        s.setNomSalle(nom);
        s.setNombrePlaces(req.nombrePlaces());
        s.setConcoursId(req.concoursId());
        s.setCreeLe(now);
        s.setModifieLe(now);
        try {
            salleRepository.save(s);
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Une salle avec ce nom existe déjà dans cet établissement");
        }
        return toSalleResponse(s);
    }

    @Transactional(readOnly = true)
    public List<SalleAvecLieuxResponse> listerSallesParConcours(Long concoursId) {
        validerConcoursId(concoursId);
        return salleRepository.findByConcoursIdOrderByNomSalleAsc(concoursId).stream()
                .map(this::toSalleAvecLieux)
                .toList();
    }

    @Transactional(readOnly = true)
    public SalleAvecLieuxResponse obtenirSalle(Long id) {
        Salle s = salleRepository
                .findByIdWithEtablissementAndCentre(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Salle introuvable"));
        return toSalleAvecLieux(s);
    }

    @Transactional
    public SalleResponse mettreAJourSalle(Long id, SalleWriteRequest req) {
        Salle s = salleRepository
                .findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Salle introuvable"));
        Long etabId = s.getEtablissement().getId();
        String nom = req.nomSalle().trim();
        if (salleRepository.existsByEtablissementIdAndNomSalleIgnoreCaseAndIdNot(etabId, nom, id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Une salle avec ce nom existe déjà dans cet établissement");
        }
        validerConcoursId(req.concoursId());
        s.setNomSalle(nom);
        s.setNombrePlaces(req.nombrePlaces());
        s.setConcoursId(req.concoursId());
        s.setModifieLe(Instant.now());
        try {
            salleRepository.save(s);
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Une salle avec ce nom existe déjà dans cet établissement");
        }
        return toSalleResponse(s);
    }

    @Transactional
    public void supprimerSalle(Long id) {
        if (!salleRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Salle introuvable");
        }
        salleRepository.deleteById(id);
    }

    /** {@code concours_id} est une référence logique vers concours-service (pas de FK inter-bases). */
    private void validerConcoursId(Long concoursId) {
        concoursExistenceClient.assertConcoursExists(concoursId, HttpRequestContext.authorizationHeaderOrNull());
    }

    private void assertNomCentreLibre(String nom, Long excludeId) {
        boolean taken =
                excludeId == null
                        ? centreRepository.existsByNomCentreIgnoreCase(nom)
                        : centreRepository.existsByNomCentreIgnoreCaseAndIdNot(nom, excludeId);
        if (taken) {
            throw conflitNomCentre();
        }
    }

    private static ResponseStatusException conflitNomCentre() {
        return new ResponseStatusException(HttpStatus.CONFLICT, "Un centre avec ce nom existe déjà");
    }

    private EtablissementDetailResponse toEtablissementDetail(Etablissement e) {
        List<SalleResponse> salles = e.getSalles().stream()
                .sorted(Comparator.comparing(Salle::getNomSalle, String.CASE_INSENSITIVE_ORDER))
                .map(this::toSalleResponse)
                .toList();
        return new EtablissementDetailResponse(
                e.getId(), e.getNomEtablissement(), concoursIdsPourEtablissement(e.getId()), salles);
    }

    private List<Long> concoursIdsPourCentre(Long centreId) {
        TreeSet<Long> ids = new TreeSet<>(salleRepository.findDistinctConcoursIdsByCentreId(centreId));
        ids.addAll(concoursExistenceClient.listConcoursIdsByCentre(
                centreId, HttpRequestContext.authorizationHeaderOrNull()));
        return List.copyOf(ids);
    }

    /** Concours d'un établissement : distincts des salles de l'établissement. */
    private List<Long> concoursIdsPourEtablissement(Long etablissementId) {
        return salleRepository.findDistinctConcoursIdsByEtablissementId(etablissementId);
    }

    private SalleResponse toSalleResponse(Salle s) {
        return new SalleResponse(s.getId(), s.getNomSalle(), s.getNombrePlaces(), s.getConcoursId());
    }

    private SalleAvecLieuxResponse toSalleAvecLieux(Salle s) {
        Etablissement e = s.getEtablissement();
        Centre c = e.getCentre();
        return new SalleAvecLieuxResponse(
                s.getId(),
                s.getNomSalle(),
                s.getNombrePlaces(),
                s.getConcoursId(),
                e.getId(),
                e.getNomEtablissement(),
                c.getId(),
                c.getNomCentre());
    }
}
