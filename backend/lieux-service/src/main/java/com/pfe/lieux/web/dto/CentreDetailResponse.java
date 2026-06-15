package com.pfe.lieux.web.dto;

import java.util.List;

public record CentreDetailResponse(
        Long idCentre, String nomCentre, List<String> concoursNumeros, List<EtablissementDetailResponse> etablissements) {}
