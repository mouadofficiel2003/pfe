package com.pfe.repartition.domain;

public enum RepartitionStatut {
    /** Tous les candidats ont été affectés sans alerte. */
    TERMINEE,
    /** Répartition exécutée mais avec au moins une alerte (capacité dépassée, aucun centre, etc.). */
    TERMINEE_AVEC_ALERTES,
    /** Échec global (ex. service aval indisponible) — conservé pour traçabilité. */
    ECHEC
}
