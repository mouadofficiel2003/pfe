package com.pfe.repartition.geo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pfe.repartition.service.TexteUtil;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

/**
 * Référentiel des 12 régions administratives du Maroc et de leurs villes/provinces.
 * Sert à prioriser un centre dans la même région que le candidat, puis la région la plus proche.
 */
@Component
public class ReferentielRegionsMaroc {

    private static final double PENALITE_HORS_REGION_KM = 10_000.0;

    private final CarteMaroc carteMaroc;
    private final Map<String, String> regionParVilleNormalisee = new HashMap<>();
    private final Map<String, Coordonnee> centroidesParRegion = new HashMap<>();

    public ReferentielRegionsMaroc(CarteMaroc carteMaroc) {
        this.carteMaroc = carteMaroc;
    }

    @PostConstruct
    void initialiser() throws IOException {
        chargerPourTests();
    }

    /** Chargement explicite (tests unitaires sans conteneur Spring). */
    public void chargerPourTests() throws IOException {
        chargerRegions();
        calculerCentroides();
    }

    /** Région administrative d'une ville ou province, si reconnue. */
    public Optional<String> regionDe(String nomVille) {
        String cle = TexteUtil.normaliser(nomVille);
        if (cle.isEmpty()) {
            return Optional.empty();
        }
        String direct = regionParVilleNormalisee.get(cle);
        if (direct != null) {
            return Optional.of(direct);
        }
        String[] mots = cle.split(" ");
        for (int i = 0; i < mots.length; i++) {
            String candidat = String.join(" ", Arrays.copyOfRange(mots, i, mots.length));
            String region = regionParVilleNormalisee.get(candidat);
            if (region != null) {
                return Optional.of(region);
            }
        }
        return Optional.empty();
    }

    /** Région déduite du nom d'un centre (ex. « Centre Rabat » → Rabat-Salé-Kénitra). */
    public Optional<String> regionDuCentre(String nomCentre) {
        String norm = TexteUtil.normaliser(nomCentre);
        if (norm.isEmpty()) {
            return Optional.empty();
        }
        String[] mots = norm.split(" ");
        for (int i = 0; i < mots.length; i++) {
            String candidat = String.join(" ", java.util.Arrays.copyOfRange(mots, i, mots.length));
            String region = regionParVilleNormalisee.get(candidat);
            if (region != null) {
                return Optional.of(region);
            }
        }
        return Optional.empty();
    }

    /**
     * Score de proximité entre la ville du candidat et un centre.
     *
     * @param privilegierRegion {@code true} si au moins un centre du concours est dans la région du
     *     candidat (priorité régionale) ; sinon comparaison par distance GPS uniquement.
     */
    public double scoreProximite(
            String villeCandidat, String nomCentre, Optional<Coordonnee> coordCandidat, boolean privilegierRegion) {
        if (!privilegierRegion) {
            return scoreDistancePure(villeCandidat, nomCentre, coordCandidat);
        }
        return scoreRegional(villeCandidat, nomCentre, coordCandidat);
    }

    /** Au moins un centre est dans la même région administrative que le candidat. */
    public boolean auMoinsUnCentreDansRegion(String villeCandidat, Iterable<String> nomsCentres) {
        Optional<String> regionCandidat = regionDe(villeCandidat);
        if (regionCandidat.isEmpty()) {
            return false;
        }
        for (String nomCentre : nomsCentres) {
            Optional<String> regionCentre = regionDuCentre(nomCentre);
            if (regionCentre.isPresent() && regionCandidat.get().equals(regionCentre.get())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Score de proximité régionale (km) entre la ville du candidat et un centre :
     * même région → distance GPS ; autre région → pénalité + distance entre centroïdes + GPS.
     */
    public double scoreRegional(String villeCandidat, String nomCentre, Optional<Coordonnee> coordCandidat) {
        Optional<Coordonnee> coordCentre = carteMaroc.coordCentre(nomCentre);
        double distanceCentreKm = distanceKm(coordCandidat, coordCentre);

        Optional<String> regionCandidat = regionDe(villeCandidat);
        Optional<String> regionCentre = regionDuCentre(nomCentre);

        if (regionCandidat.isEmpty() || regionCentre.isEmpty()) {
            return distanceCentreKm < Double.MAX_VALUE ? distanceCentreKm : Double.MAX_VALUE;
        }

        if (regionCandidat.get().equals(regionCentre.get())) {
            if (memeVille(villeCandidat, nomCentre)) {
                return 0.0;
            }
            if (distanceCentreKm < Double.MAX_VALUE) {
                return distanceCentreKm;
            }
            return distanceCentroides(regionCandidat.get(), regionCentre.get());
        }

        double interRegionKm = distanceCentroides(regionCandidat.get(), regionCentre.get());
        if (distanceCentreKm < Double.MAX_VALUE) {
            return PENALITE_HORS_REGION_KM + interRegionKm + distanceCentreKm;
        }
        return PENALITE_HORS_REGION_KM + interRegionKm;
    }

    private double scoreDistancePure(String villeCandidat, String nomCentre, Optional<Coordonnee> coordCandidat) {
        if (memeVille(villeCandidat, nomCentre)) {
            return 0.0;
        }
        double distanceCentreKm = distanceKm(coordCandidat, carteMaroc.coordCentre(nomCentre));
        if (distanceCentreKm < Double.MAX_VALUE) {
            return distanceCentreKm;
        }
        Optional<String> regionCandidat = regionDe(villeCandidat);
        Optional<String> regionCentre = regionDuCentre(nomCentre);
        if (regionCandidat.isPresent() && regionCentre.isPresent()) {
            return distanceCentroides(regionCandidat.get(), regionCentre.get());
        }
        return Double.MAX_VALUE;
    }

    /** Distance en km entre les centroïdes de deux régions. */
    public double distanceCentroides(String regionA, String regionB) {
        Coordonnee a = centroidesParRegion.get(regionA);
        Coordonnee b = centroidesParRegion.get(regionB);
        if (a == null || b == null) {
            return Double.MAX_VALUE;
        }
        return CarteMaroc.distanceKm(a, b);
    }

    public int nombreRegions() {
        return centroidesParRegion.size();
    }

    private static boolean memeVille(String villeCandidat, String nomCentre) {
        String villeNorm = TexteUtil.normaliser(villeCandidat);
        String centreNorm = TexteUtil.normaliser(nomCentre);
        if (villeNorm.isEmpty()) {
            return false;
        }
        return centreNorm.equals(villeNorm) || centreNorm.endsWith(" " + villeNorm);
    }

    private static double distanceKm(Optional<Coordonnee> a, Optional<Coordonnee> b) {
        if (a.isEmpty() || b.isEmpty()) {
            return Double.MAX_VALUE;
        }
        return CarteMaroc.distanceKm(a.get(), b.get());
    }

    private void chargerRegions() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        try (InputStream in = new ClassPathResource("geo/regions-maroc.json").getInputStream()) {
            List<RegionJson> regions = mapper.readValue(in, mapper.getTypeFactory()
                    .constructCollectionType(List.class, RegionJson.class));
            for (RegionJson region : regions) {
                if (region.nom() == null || region.villes() == null) {
                    continue;
                }
                for (String ville : region.villes()) {
                    if (ville == null || ville.isBlank()) {
                        continue;
                    }
                    String cle = TexteUtil.normaliser(ville);
                    if (!cle.isEmpty()) {
                        regionParVilleNormalisee.putIfAbsent(cle, region.nom());
                    }
                }
            }
        }
    }

    private void calculerCentroides() {
        Map<String, double[]> accum = new HashMap<>();
        for (Map.Entry<String, String> entry : regionParVilleNormalisee.entrySet()) {
            carteMaroc.coord(entry.getKey()).ifPresent(coord -> {
                String region = entry.getValue();
                double[] sum = accum.computeIfAbsent(region, k -> new double[3]);
                sum[0] += coord.lat();
                sum[1] += coord.lon();
                sum[2] += 1;
            });
        }
        for (Map.Entry<String, double[]> entry : accum.entrySet()) {
            double[] sum = entry.getValue();
            if (sum[2] > 0) {
                centroidesParRegion.put(
                        entry.getKey(), new Coordonnee(sum[0] / sum[2], sum[1] / sum[2]));
            }
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record RegionJson(String nom, List<String> villes) {}
}
