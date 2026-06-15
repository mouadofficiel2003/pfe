package com.pfe.lieux.web.dto;

import java.util.List;

public record EtablissementDetailResponse(
        Long idEtablissement, String nomEtablissement, List<String> concoursNumeros, List<SalleResponse> salles) {}
