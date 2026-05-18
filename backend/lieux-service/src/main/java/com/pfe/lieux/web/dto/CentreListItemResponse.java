package com.pfe.lieux.web.dto;

import java.util.List;

public record CentreListItemResponse(
        Long id, String nomCentre, long nombreEtablissements, List<Long> concoursIds) {}
