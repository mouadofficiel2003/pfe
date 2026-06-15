package com.pfe.repartition.geo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pfe.repartition.service.TexteUtil;
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
 * « Carte du Maroc » : référentiel de villes marocaines géolocalisées, chargé depuis
 * {@code classpath:geo/villes-maroc.json}. Sert à mesurer la distance réelle (km, Haversine)
 * entre la ville d'un candidat et la ville d'un centre afin de choisir le centre le plus proche.
 */
@Component
public class CarteMaroc {

    private static final double RAYON_TERRE_KM = 6371.0088;

    private final Map<String, Coordonnee> villesParNomNormalise;

    public CarteMaroc() throws IOException {
        this.villesParNomNormalise = charger();
    }

    /** Coordonnées d'une ville (recherche insensible à la casse/aux accents), si connue. */
    public Optional<Coordonnee> coord(String nomVille) {
        String cle = TexteUtil.normaliser(nomVille);
        if (cle.isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(villesParNomNormalise.get(cle));
    }

    /**
     * Coordonnées de la ville d'un centre. Le nom d'un centre vaut idéalement une ville
     * (ex. « Rabat »), mais peut être préfixé dans les données (ex. « Centre Rabat »). On tente
     * d'abord le nom complet, puis chaque suffixe de mots (« centre rabat » → « rabat ») et on
     * retient la première ville connue — donc la correspondance la plus longue en priorité, ce qui
     * préserve les villes en plusieurs mots (« Sale Al Jadida »).
     */
    public Optional<Coordonnee> coordCentre(String nomCentre) {
        String norm = TexteUtil.normaliser(nomCentre);
        if (norm.isEmpty()) {
            return Optional.empty();
        }
        String[] mots = norm.split(" ");
        for (int i = 0; i < mots.length; i++) {
            String candidat = String.join(" ", Arrays.copyOfRange(mots, i, mots.length));
            Coordonnee coord = villesParNomNormalise.get(candidat);
            if (coord != null) {
                return Optional.of(coord);
            }
        }
        return Optional.empty();
    }

    /** Nombre de villes connues (diagnostic/tests). */
    public int taille() {
        return villesParNomNormalise.size();
    }

    /** Distance orthodromique en kilomètres entre deux points (formule de Haversine). */
    public static double distanceKm(Coordonnee a, Coordonnee b) {
        double dLat = Math.toRadians(b.lat() - a.lat());
        double dLon = Math.toRadians(b.lon() - a.lon());
        double latA = Math.toRadians(a.lat());
        double latB = Math.toRadians(b.lat());
        double h = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.sin(dLon / 2) * Math.sin(dLon / 2) * Math.cos(latA) * Math.cos(latB);
        return 2 * RAYON_TERRE_KM * Math.asin(Math.min(1.0, Math.sqrt(h)));
    }

    private static Map<String, Coordonnee> charger() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        try (InputStream in = new ClassPathResource("geo/villes-maroc.json").getInputStream()) {
            List<VilleJson> villes = mapper.readValue(in, mapper.getTypeFactory()
                    .constructCollectionType(List.class, VilleJson.class));
            Map<String, Coordonnee> map = new HashMap<>();
            for (VilleJson v : villes) {
                if (v.nom() == null) {
                    continue;
                }
                String cle = TexteUtil.normaliser(v.nom());
                if (!cle.isEmpty()) {
                    map.putIfAbsent(cle, new Coordonnee(v.lat(), v.lon()));
                }
            }
            return Map.copyOf(map);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record VilleJson(String nom, double lat, double lon) {}
}
