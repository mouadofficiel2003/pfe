package com.pfe.repartition.domain;

public enum AlerteType {
    /** Le centre visé (le plus proche de la ville) n'a plus de place disponible. */
    CAPACITE_DEPASSEE,
    /** Le concours n'a aucun centre/salle exploitable pour placer le candidat. */
    AUCUN_CENTRE_DISPONIBLE,
    /** Le candidat n'est rattaché à aucun concours exploitable. */
    CONCOURS_INCONNU,
    /** La ville du candidat est absente de la carte du Maroc : impossible de calculer la proximité. */
    VILLE_NON_GEOLOCALISEE
}
