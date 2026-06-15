package com.pfe.lieux.web.dto;

import java.util.List;

public record CentreListItemResponse(
        Long idCentre, String nomCentre, long nombreEtablissements, List<String> concoursNumeros) {}
