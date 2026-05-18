package com.pfe.candidats.web.dto;

import java.util.List;

public record ImportCandidatsResponse(
        int inserted, int updated, int skipped, List<ImportCandidatsError> errors) {

    public record ImportCandidatsError(int rowNumber, String message) {}
}
