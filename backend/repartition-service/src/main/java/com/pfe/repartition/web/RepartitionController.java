package com.pfe.repartition.web;

import com.pfe.repartition.service.RepartitionService;
import com.pfe.repartition.web.dto.RepartitionRunResponse;
import com.pfe.repartition.web.dto.RepartitionRunSummaryResponse;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/repartition")
public class RepartitionController {

    private final RepartitionService repartitionService;

    public RepartitionController(RepartitionService repartitionService) {
        this.repartitionService = repartitionService;
    }

    /** Déclenchement unique de la répartition automatique (« Commencer répartition »). */
    @PostMapping("/run")
    @ResponseStatus(HttpStatus.CREATED)
    public RepartitionRunResponse declencher() {
        return repartitionService.declencher();
    }

    /** Historique des exécutions (vue allégée). */
    @GetMapping("/runs")
    public List<RepartitionRunSummaryResponse> listerRuns() {
        return repartitionService.listerRuns();
    }

    /** Synthèse complète d'une exécution (affectations + alertes). */
    @GetMapping("/runs/{id}")
    public RepartitionRunResponse obtenirRun(@PathVariable Long id) {
        return repartitionService.obtenirRun(id);
    }
}
