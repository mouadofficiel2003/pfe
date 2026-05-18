package com.pfe.lieux.web;

import com.pfe.lieux.service.LieuxApplicationService;
import com.pfe.lieux.web.dto.SalleAvecLieuxResponse;
import com.pfe.lieux.web.dto.SalleResponse;
import com.pfe.lieux.web.dto.SalleWriteRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/salles")
public class SalleController {

    private final LieuxApplicationService lieuxApplicationService;

    public SalleController(LieuxApplicationService lieuxApplicationService) {
        this.lieuxApplicationService = lieuxApplicationService;
    }

    @GetMapping
    public List<SalleAvecLieuxResponse> listerParConcours(@RequestParam long concoursId) {
        return lieuxApplicationService.listerSallesParConcours(concoursId);
    }

    @GetMapping("/{id}")
    public SalleAvecLieuxResponse obtenir(@PathVariable Long id) {
        return lieuxApplicationService.obtenirSalle(id);
    }

    @PutMapping("/{id}")
    public SalleResponse mettreAJour(@PathVariable Long id, @Valid @RequestBody SalleWriteRequest body) {
        return lieuxApplicationService.mettreAJourSalle(id, body);
    }

    @DeleteMapping("/{id}")
    public void supprimer(@PathVariable Long id) {
        lieuxApplicationService.supprimerSalle(id);
    }
}
