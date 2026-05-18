package com.pfe.concours.web;

import com.pfe.concours.service.ConcoursService;
import com.pfe.concours.web.dto.ConcoursHeadResponse;
import com.pfe.concours.web.dto.ConcoursResponse;
import com.pfe.concours.web.dto.ConcoursWriteRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/concours")
public class ConcoursController {

    private final ConcoursService concoursService;

    public ConcoursController(ConcoursService concoursService) {
        this.concoursService = concoursService;
    }

    @GetMapping
    public List<ConcoursResponse> lister() {
        return concoursService.listerTous();
    }

    @GetMapping("/by-centre/{centreId}")
    public List<ConcoursHeadResponse> listerParCentre(@PathVariable Long centreId) {
        return concoursService.listerEnTeteParCentre(centreId);
    }

    @GetMapping("/{id}")
    public ConcoursResponse obtenir(@PathVariable Long id) {
        return concoursService.obtenir(id);
    }

    @PostMapping
    public ConcoursResponse creer(@Valid @RequestBody ConcoursWriteRequest body) {
        return concoursService.creer(body);
    }

    @PutMapping("/{id}")
    public ConcoursResponse mettreAJour(@PathVariable Long id, @Valid @RequestBody ConcoursWriteRequest body) {
        return concoursService.mettreAJour(id, body);
    }

    @DeleteMapping("/{id}")
    public void supprimer(@PathVariable Long id) {
        concoursService.supprimer(id);
    }
}
