package com.pfe.repartition.geo;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ReferentielRegionsMarocTest {

    private CarteMaroc carte;

    private ReferentielRegionsMaroc referentiel;

    @BeforeEach
    void setUp() throws IOException {
        carte = new CarteMaroc();
        referentiel = new ReferentielRegionsMaroc(carte);
        referentiel.chargerPourTests();
    }

    @Test
    void reconnaitLesVillesDesRegionsAdministratives() {
        assertThat(referentiel.regionDe("Chichaoua")).contains("Marrakech-Safi");
        assertThat(referentiel.regionDe("Province de Chichaoua")).contains("Marrakech-Safi");
        assertThat(referentiel.regionDe("Tanger")).contains("Tanger-Tetouan-Al Hoceima");
        assertThat(referentiel.regionDuCentre("Centre Rabat")).contains("Rabat-Sale-Kenitra");
        assertThat(referentiel.regionDuCentre("Centre Marrackech")).contains("Marrakech-Safi");
        assertThat(carte.coordCentre("Centre Marrackech")).isPresent();
    }

    @Test
    void prioriseLaMemeRegionSurUneRegionPlusProcheEnKm() {
        double scoreMemeRegion = referentiel.scoreRegional(
                "Chichaoua", "Centre Marrakech", java.util.Optional.empty());
        double scoreAutreRegion = referentiel.scoreRegional(
                "Chichaoua", "Centre Casablanca", java.util.Optional.empty());

        assertThat(scoreMemeRegion).isLessThan(scoreAutreRegion);

        double gpsMarrakech = referentiel.scoreProximite(
                "Sidi Ifni", "Centre Marrakech", carte.coord("Sidi Ifni"), false);
        double gpsCasablanca = referentiel.scoreProximite(
                "Sidi Ifni", "Centre Casablanca", carte.coord("Sidi Ifni"), false);

        assertThat(gpsMarrakech).isLessThan(gpsCasablanca);
    }
}
