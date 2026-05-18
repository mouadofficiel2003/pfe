package com.pfe.candidats.service;

import com.pfe.candidats.domain.Candidat;
import com.pfe.candidats.repository.CandidatRepository;
import com.pfe.candidats.service.CandidatExcelParser.ParsedRow;
import com.pfe.candidats.service.CandidatExcelParser.SheetRow;
import com.pfe.candidats.web.dto.CandidatResponse;
import com.pfe.candidats.web.dto.CandidatUpdateRequest;
import com.pfe.candidats.web.dto.ImportCandidatsResponse;
import com.pfe.candidats.web.dto.ImportCandidatsResponse.ImportCandidatsError;
import com.pfe.candidats.remote.dto.SalleLieuxHeadJson;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CandidatService {

    private final CandidatRepository candidatRepository;
    private final CandidatConcoursResolver candidatConcoursResolver;
    private final CandidatRemoteRefsValidator candidatRemoteRefsValidator;

    public CandidatService(
            CandidatRepository candidatRepository,
            CandidatConcoursResolver candidatConcoursResolver,
            CandidatRemoteRefsValidator candidatRemoteRefsValidator) {
        this.candidatRepository = candidatRepository;
        this.candidatConcoursResolver = candidatConcoursResolver;
        this.candidatRemoteRefsValidator = candidatRemoteRefsValidator;
    }

    @Transactional(readOnly = true)
    public List<CandidatResponse> listerTous() {
        return candidatRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public CandidatResponse obtenir(Long id) {
        Candidat c = candidatRepository
                .findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Candidat introuvable"));
        return toResponse(c);
    }

    @Transactional
    public CandidatResponse mettreAJour(Long id, CandidatUpdateRequest req) {
        Candidat c = candidatRepository
                .findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Candidat introuvable"));
        if (candidatRepository.existsByCinAndIdNot(req.cin().trim(), id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ce CIN est déjà utilisé par un autre candidat");
        }
        ResolvedConcours concours = candidatConcoursResolver.resolve(req.nomConcours(), req.concoursId());
        if (candidatRepository.existsByNumeroInscriptionAndConcoursIdAndIdNot(
                req.numeroInscription().trim(), concours.concoursId(), id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Ce numéro d'inscription existe déjà pour ce concours");
        }
        var salleInfo = candidatRemoteRefsValidator.validateLieux(req, concours);
        appliquerChamps(c, req, concours, salleInfo);
        candidatRepository.save(c);
        return toResponse(c);
    }

    @Transactional
    public void supprimer(Long id) {
        if (!candidatRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Candidat introuvable");
        }
        candidatRepository.deleteById(id);
    }

    @Transactional
    public ImportCandidatsResponse importerExcel(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Fichier vide");
        }
        String name = file.getOriginalFilename() != null ? file.getOriginalFilename() : "";
        if (!name.toLowerCase().endsWith(".xlsx")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Format attendu : .xlsx");
        }
        List<SheetRow> rows;
        try {
            rows = CandidatExcelParser.parseAll(file.getInputStream());
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Impossible de lire le fichier Excel");
        }
        int inserted = 0;
        int updated = 0;
        int skipped = 0;
        List<ImportCandidatsError> errors = new ArrayList<>();
        CandidatConcoursResolver.ConcoursCatalog concoursCatalog;
        try {
            concoursCatalog = candidatConcoursResolver.loadCatalog();
        } catch (ResponseStatusException ex) {
            throw ex;
        }
        for (SheetRow sr : rows) {
            Optional<String> validation = validerLigne(sr.data());
            if (validation.isPresent()) {
                errors.add(new ImportCandidatsError(sr.rowNumber(), validation.get()));
                skipped++;
                continue;
            }
            ParsedRow d = sr.data();
            short age;
            try {
                age = parseAge(d.ageRaw());
            } catch (IllegalArgumentException e) {
                errors.add(new ImportCandidatsError(sr.rowNumber(), "Âge invalide"));
                skipped++;
                continue;
            }
            String cin = d.cin().trim();
            String numIns = d.numeroInscription().trim();
            String nomCo = d.nomConcours().trim();
            ResolvedConcours concours;
            try {
                concours = concoursCatalog.resolveByNom(nomCo);
            } catch (ResponseStatusException ex) {
                errors.add(new ImportCandidatsError(
                        sr.rowNumber(), ex.getReason() != null ? ex.getReason() : ex.getMessage()));
                skipped++;
                continue;
            }
            Optional<Candidat> exist = candidatRepository.findByCin(cin);
            try {
                if (exist.isPresent()) {
                    Candidat c = exist.get();
                    if (candidatRepository.existsByNumeroInscriptionAndConcoursIdAndIdNot(
                            numIns, concours.concoursId(), c.getId())) {
                        errors.add(new ImportCandidatsError(
                                sr.rowNumber(),
                                "Numéro d'inscription / concours déjà pris par un autre candidat"));
                        skipped++;
                        continue;
                    }
                    appliquerImport(c, d, age, concours, false);
                    candidatRepository.save(c);
                    updated++;
                } else {
                    if (candidatRepository.existsByNumeroInscriptionAndConcoursId(numIns, concours.concoursId())) {
                        errors.add(new ImportCandidatsError(
                                sr.rowNumber(),
                                "Numéro d'inscription / concours déjà utilisé"));
                        skipped++;
                        continue;
                    }
                    Candidat c = new Candidat();
                    Instant now = Instant.now();
                    c.setCreeLe(now);
                    c.setModifieLe(now);
                    appliquerImport(c, d, age, concours, true);
                    candidatRepository.save(c);
                    inserted++;
                }
            } catch (DataIntegrityViolationException ex) {
                errors.add(new ImportCandidatsError(sr.rowNumber(), "Doublon ou contrainte base de données"));
                skipped++;
            }
        }
        return new ImportCandidatsResponse(inserted, updated, skipped, errors);
    }

    private void appliquerImport(Candidat c, ParsedRow d, short age, ResolvedConcours concours, boolean nouveau) {
        c.setNom(truncate(d.nom().trim(), 120));
        c.setPrenom(truncate(d.prenom().trim(), 120));
        c.setCin(truncate(d.cin().trim(), 32));
        c.setNumeroTelephone(truncate(d.numeroTelephone().trim(), 32));
        c.setVille(truncate(d.ville().trim(), 120));
        c.setAge(age);
        c.setEmail(truncate(d.email().trim(), 255));
        c.setSpecialite(truncate(d.specialite().trim(), 200));
        c.setNumeroInscription(truncate(d.numeroInscription().trim(), 80));
        c.setConcoursId(concours.concoursId());
        c.setNomConcours(truncate(concours.nomConcours(), 200));
        if (nouveau) {
            c.setIdCentre(null);
            c.setIdEtablissement(null);
            c.setIdSalle(null);
            c.setNumeroPlace(null);
        }
    }

    private static String truncate(String s, int max) {
        if (s.length() <= max) {
            return s;
        }
        return s.substring(0, max);
    }

    private void appliquerChamps(
            Candidat c,
            CandidatUpdateRequest req,
            ResolvedConcours concours,
            Optional<SalleLieuxHeadJson> salleResolue) {
        c.setNom(req.nom().trim());
        c.setPrenom(req.prenom().trim());
        c.setCin(req.cin().trim());
        c.setNumeroTelephone(req.numeroTelephone().trim());
        c.setVille(req.ville().trim());
        c.setAge(req.age().shortValue());
        c.setEmail(req.email().trim());
        c.setSpecialite(req.specialite().trim());
        c.setNumeroInscription(req.numeroInscription().trim());
        c.setConcoursId(concours.concoursId());
        c.setNomConcours(concours.nomConcours());
        if (req.idSalle() == null) {
            c.setIdCentre(req.idCentre());
            c.setIdEtablissement(req.idEtablissement());
            c.setIdSalle(null);
            c.setNumeroPlace(req.numeroPlace());
            return;
        }
        SalleLieuxHeadJson s = salleResolue.orElseThrow(
                () -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "État affectation incohérent"));
        Long centreId = req.idCentre() != null ? req.idCentre() : s.centreId();
        Long etabId = req.idEtablissement() != null ? req.idEtablissement() : s.etablissementId();
        c.setIdCentre(centreId);
        c.setIdEtablissement(etabId);
        c.setIdSalle(req.idSalle());
        c.setNumeroPlace(req.numeroPlace());
    }

    private static Optional<String> validerLigne(ParsedRow d) {
        if (d.nom().isBlank()) {
            return Optional.of("Nom manquant");
        }
        if (d.prenom().isBlank()) {
            return Optional.of("Prénom manquant");
        }
        if (d.cin().isBlank()) {
            return Optional.of("CIN manquant");
        }
        if (d.numeroTelephone().isBlank()) {
            return Optional.of("Téléphone manquant");
        }
        if (d.ville().isBlank()) {
            return Optional.of("Ville manquante");
        }
        if (d.ageRaw().isBlank()) {
            return Optional.of("Âge manquant");
        }
        if (d.email().isBlank()) {
            return Optional.of("Email manquant");
        }
        if (d.specialite().isBlank()) {
            return Optional.of("Spécialité manquante");
        }
        if (d.numeroInscription().isBlank()) {
            return Optional.of("Numéro d'inscription manquant");
        }
        if (d.nomConcours().isBlank()) {
            return Optional.of("Nom du concours manquant");
        }
        try {
            short a = parseAge(d.ageRaw());
            if (a < 10 || a > 120) {
                return Optional.of("Âge hors plage (10–120)");
            }
        } catch (IllegalArgumentException e) {
            return Optional.of("Âge invalide");
        }
        return Optional.empty();
    }

    private static short parseAge(String raw) {
        String s = raw.trim().replace(',', '.');
        double v = Double.parseDouble(s);
        int rounded = (int) Math.round(v);
        if (rounded < Short.MIN_VALUE || rounded > Short.MAX_VALUE) {
            throw new IllegalArgumentException();
        }
        return (short) rounded;
    }

    private CandidatResponse toResponse(Candidat c) {
        return new CandidatResponse(
                c.getId(),
                c.getNom(),
                c.getPrenom(),
                c.getCin(),
                c.getNumeroTelephone(),
                c.getVille(),
                c.getAge(),
                c.getEmail(),
                c.getSpecialite(),
                c.getNumeroInscription(),
                c.getNomConcours(),
                c.getConcoursId(),
                c.getIdCentre(),
                c.getIdEtablissement(),
                c.getIdSalle(),
                c.getNumeroPlace(),
                c.getCreeLe(),
                c.getModifieLe());
    }
}
