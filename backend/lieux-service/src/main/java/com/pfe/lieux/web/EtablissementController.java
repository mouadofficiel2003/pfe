package com.pfe.lieux.web;

import com.pfe.lieux.service.LieuxApplicationService;
import com.pfe.lieux.web.dto.EtablissementDetailResponse;
import com.pfe.lieux.web.dto.EtablissementNomRequest;
import com.pfe.lieux.web.dto.SalleResponse;
import com.pfe.lieux.web.dto.SalleWriteRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/etablissements")
public class EtablissementController {

    private final LieuxApplicationService lieuxApplicationService;

    public EtablissementController(LieuxApplicationService lieuxApplicationService) {
        this.lieuxApplicationService = lieuxApplicationService;
    }

    @GetMapping("/{id}")
    public EtablissementDetailResponse obtenir(@PathVariable Long id) {
        return lieuxApplicationService.obtenirEtablissement(id);
    }

    @PutMapping("/{id}")
    public EtablissementDetailResponse mettreAJour(
            @PathVariable Long id, @Valid @RequestBody EtablissementNomRequest body) {
        return lieuxApplicationService.mettreAJourEtablissement(id, body);
    }

    @DeleteMapping("/{id}")
    public void supprimer(@PathVariable Long id) {
        lieuxApplicationService.supprimerEtablissement(id);
    }

    @PostMapping("/{etablissementId}/salles")
    @ResponseStatus(HttpStatus.CREATED)
    public SalleResponse creerSalle(
            @PathVariable Long etablissementId, @Valid @RequestBody SalleWriteRequest body) {
        return lieuxApplicationService.creerSalle(etablissementId, body);
    }
}
