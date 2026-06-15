package com.pfe.repartition.geo;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DistanceSidiIfniTest {

    private CarteMaroc carte;
    private ReferentielRegionsMaroc referentiel;

    @BeforeEach
    void setUp() throws IOException {
        carte = new CarteMaroc();
        referentiel = new ReferentielRegionsMaroc(carte);
        referentiel.chargerPourTests();
    }

    @Test
    void sidiIfniEstPlusProcheDeMarrakechQueDeCasablanca() {
        Optional<Coordonnee> sidiIfni = carte.coord("Sidi Ifni");

        double kmMarrakech = CarteMaroc.distanceKm(sidiIfni.get(), carte.coord("Marrakech").get());
        double kmCasablanca = CarteMaroc.distanceKm(sidiIfni.get(), carte.coord("Casablanca").get());
        double kmAgadir = CarteMaroc.distanceKm(sidiIfni.get(), carte.coord("Agadir").get());

        assertThat(kmMarrakech).isLessThan(kmCasablanca);
        assertThat(kmAgadir).isLessThan(kmMarrakech);

        double scoreMarrakech =
                referentiel.scoreRegional("Sidi Ifni", "Centre Marrakech", sidiIfni);
        double scoreCasablanca =
                referentiel.scoreRegional("Sidi Ifni", "Centre Casablanca", sidiIfni);
        double scoreAgadir = referentiel.scoreRegional("Sidi Ifni", "Centre Agadir", sidiIfni);

        assertThat(scoreMarrakech).isLessThan(scoreCasablanca);
        assertThat(scoreAgadir).isLessThan(scoreMarrakech);
    }
}
