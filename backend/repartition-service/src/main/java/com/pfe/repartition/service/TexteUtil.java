package com.pfe.repartition.service;

import java.text.Normalizer;

/** Normalisation de libellés de villes (ville candidat ↔ nom de centre ↔ carte). */
public final class TexteUtil {

    private TexteUtil() {}

    /** Minuscule, sans accents, espaces normalisés. {@code null} → chaîne vide. */
    public static String normaliser(String s) {
        if (s == null) {
            return "";
        }
        String sansAccents = Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}+", "");
        return sansAccents.toLowerCase().trim().replaceAll("\\s+", " ");
    }
}
