package com.pfe.lieux.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.pfe.lieux.domain.Centre;
import com.pfe.lieux.domain.Etablissement;
import com.pfe.lieux.domain.Salle;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.ANY)
class LieuxJpaRepositoriesIT {

    @Autowired
    private CentreRepository centreRepository;

    @Autowired
    private EtablissementRepository etablissementRepository;

    @Autowired
    private SalleRepository salleRepository;

    @Test
    void persisteCentreEtablissementSalleEtCascadeSuppression() {
        Instant now = Instant.parse("2026-05-01T10:00:00Z");

        Centre centre = new Centre();
        centre.setNomCentre("Centre Rabat");
        centre.setCreeLe(now);
        centre.setModifieLe(now);

        Etablissement etab = new Etablissement();
        etab.setNomEtablissement("Lycée Alpha");
        etab.setCreeLe(now);
        etab.setModifieLe(now);
        centre.getEtablissements().add(etab);
        etab.setCentre(centre);
        centre.lierEtablissements();

        Salle salle = new Salle();
        salle.setNomSalle("Salle 1");
        salle.setNombrePlaces(40);
        salle.setConcoursId(99L);
        salle.setCreeLe(now);
        salle.setModifieLe(now);
        etab.getSalles().add(salle);
        salle.setEtablissement(etab);
        etab.lierSalles();

        Centre savedCentre = centreRepository.save(centre);
        assertThat(savedCentre.getId()).isNotNull();
        assertThat(savedCentre.getEtablissements()).hasSize(1);

        Long etabId = savedCentre.getEtablissements().get(0).getId();
        assertThat(etabId).isNotNull();

        Optional<Salle> loadedSalle =
                salleRepository.findByIdWithEtablissementAndCentre(
                        savedCentre.getEtablissements().get(0).getSalles().get(0).getId());
        assertThat(loadedSalle).isPresent();
        assertThat(loadedSalle.get().getNomSalle()).isEqualTo("Salle 1");
        assertThat(loadedSalle.get().getNombrePlaces()).isEqualTo(40);
        assertThat(loadedSalle.get().getConcoursId()).isEqualTo(99L);
        assertThat(loadedSalle.get().getEtablissement().getNomEtablissement()).isEqualTo("Lycée Alpha");
        assertThat(loadedSalle.get().getEtablissement().getCentre().getNomCentre()).isEqualTo("Centre Rabat");

        assertThat(salleRepository.findByConcoursIdOrderByNomSalleAsc(99L)).hasSize(1);
        assertThat(salleRepository.findDistinctConcoursIdsByEtablissementId(etabId)).containsExactly(99L);
        assertThat(salleRepository.findDistinctConcoursIdsByCentreId(savedCentre.getId())).containsExactly(99L);

        centreRepository.deleteById(savedCentre.getId());

        assertThat(centreRepository.findAll()).isEmpty();
        assertThat(etablissementRepository.findAll()).isEmpty();
        assertThat(salleRepository.findAll()).isEmpty();
    }

    @Test
    void findByCentreIdWithSalles_inclutEtablissementSansSalle() {
        Instant now = Instant.parse("2026-05-02T10:00:00Z");

        Centre centre = new Centre();
        centre.setNomCentre("Centre Test");
        centre.setCreeLe(now);
        centre.setModifieLe(now);

        Etablissement avecSalle = new Etablissement();
        avecSalle.setNomEtablissement("Avec salle");
        avecSalle.setCreeLe(now);
        avecSalle.setModifieLe(now);

        Etablissement sansSalle = new Etablissement();
        sansSalle.setNomEtablissement("Sans salle");
        sansSalle.setCreeLe(now);
        sansSalle.setModifieLe(now);

        centre.getEtablissements().add(avecSalle);
        centre.getEtablissements().add(sansSalle);
        avecSalle.setCentre(centre);
        sansSalle.setCentre(centre);
        centre.lierEtablissements();

        Salle salle = new Salle();
        salle.setNomSalle("S1");
        salle.setNombrePlaces(20);
        salle.setCreeLe(now);
        salle.setModifieLe(now);
        avecSalle.getSalles().add(salle);
        salle.setEtablissement(avecSalle);
        avecSalle.lierSalles();

        Centre saved = centreRepository.saveAndFlush(centre);

        var loaded = etablissementRepository.findByCentreIdWithSalles(saved.getId());
        assertThat(loaded).extracting(Etablissement::getNomEtablissement).containsExactly("Avec salle", "Sans salle");
        Etablissement etabSansSalle = loaded.stream()
                .filter(e -> "Sans salle".equals(e.getNomEtablissement()))
                .findFirst()
                .orElseThrow();
        assertThat(etabSansSalle.getSalles()).isEmpty();
    }

    @Test
    void nomCentreUnique_rejeteDeuxiemeInsert() {
        Instant now = Instant.now();
        Centre a = new Centre();
        a.setNomCentre("Uniqueville");
        a.setCreeLe(now);
        a.setModifieLe(now);
        centreRepository.save(a);

        Centre b = new Centre();
        b.setNomCentre("Uniqueville");
        b.setCreeLe(now);
        b.setModifieLe(now);
        org.junit.jupiter.api.Assertions.assertThrows(
                org.springframework.dao.DataIntegrityViolationException.class, () -> centreRepository.saveAndFlush(b));
    }
}
