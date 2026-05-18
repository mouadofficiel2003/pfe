package com.pfe.lieux.web.dto;

import java.util.List;

public record EtablissementDetailResponse(
        Long id, String nomEtablissement, List<Long> concoursIds, List<SalleResponse> salles) {}
