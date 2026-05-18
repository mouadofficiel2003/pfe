package com.pfe.candidats.web;

import com.pfe.candidats.service.CandidatService;
import com.pfe.candidats.web.dto.CandidatResponse;
import com.pfe.candidats.web.dto.CandidatUpdateRequest;
import com.pfe.candidats.web.dto.ImportCandidatsResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/candidats")
public class CandidatController {

    private final CandidatService candidatService;

    public CandidatController(CandidatService candidatService) {
        this.candidatService = candidatService;
    }

    @GetMapping
    public List<CandidatResponse> lister() {
        return candidatService.listerTous();
    }

    @GetMapping("/{id}")
    public CandidatResponse obtenir(@PathVariable Long id) {
        return candidatService.obtenir(id);
    }

    @PutMapping("/{id}")
    public CandidatResponse mettreAJour(@PathVariable Long id, @Valid @RequestBody CandidatUpdateRequest body) {
        return candidatService.mettreAJour(id, body);
    }

    @DeleteMapping("/{id}")
    public void supprimer(@PathVariable Long id) {
        candidatService.supprimer(id);
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ImportCandidatsResponse importer(@RequestPart("file") MultipartFile file) {
        return candidatService.importerExcel(file);
    }
}
