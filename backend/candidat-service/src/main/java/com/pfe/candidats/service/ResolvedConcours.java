package com.pfe.candidats.service;

/** Concours canonique (id service concours + libellé officiel). */
public record ResolvedConcours(Long concoursId, String nomConcours) {}
