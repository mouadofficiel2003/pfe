package com.pfe.lieux.web;

import com.pfe.lieux.service.LieuxApplicationService;
import com.pfe.lieux.web.dto.CentreDetailResponse;
import com.pfe.lieux.web.dto.CentreListItemResponse;
import com.pfe.lieux.web.dto.CentreNomRequest;
import com.pfe.lieux.web.dto.EtablissementDetailResponse;
import com.pfe.lieux.web.dto.EtablissementNomRequest;
import jakarta.validation.Valid;
import java.util.List;
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
@RequestMapping("/api/centres")
public class CentreController {

    private final LieuxApplicationService lieuxApplicationService;

    public CentreController(LieuxApplicationService lieuxApplicationService) {
        this.lieuxApplicationService = lieuxApplicationService;
    }

    @GetMapping
    public List<CentreListItemResponse> lister() {
        return lieuxApplicationService.listerCentres();
    }

    @GetMapping("/{id}")
    public CentreDetailResponse obtenir(@PathVariable Long id) {
        return lieuxApplicationService.obtenirCentre(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CentreDetailResponse creer(@Valid @RequestBody CentreNomRequest body) {
        return lieuxApplicationService.creerCentre(body);
    }

    @PutMapping("/{id}")
    public CentreDetailResponse mettreAJour(@PathVariable Long id, @Valid @RequestBody CentreNomRequest body) {
        return lieuxApplicationService.mettreAJourCentre(id, body);
    }

    @DeleteMapping("/{id}")
    public void supprimer(@PathVariable Long id) {
        lieuxApplicationService.supprimerCentre(id);
    }

    @PostMapping("/{centreId}/etablissements")
    @ResponseStatus(HttpStatus.CREATED)
    public EtablissementDetailResponse creerEtablissement(
            @PathVariable Long centreId, @Valid @RequestBody EtablissementNomRequest body) {
        return lieuxApplicationService.creerEtablissement(centreId, body);
    }
}
