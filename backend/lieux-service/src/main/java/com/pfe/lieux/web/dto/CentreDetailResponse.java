package com.pfe.lieux.web.dto;

import java.util.List;

public record CentreDetailResponse(
        Long id, String nomCentre, List<Long> concoursIds, List<EtablissementDetailResponse> etablissements) {}
