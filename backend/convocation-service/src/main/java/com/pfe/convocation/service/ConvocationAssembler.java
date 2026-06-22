package com.pfe.convocation.service;

import com.pfe.convocation.remote.CandidatRemoteClient;
import com.pfe.convocation.remote.ConcoursRemoteClient;
import com.pfe.convocation.remote.LieuxRemoteClient;
import com.pfe.convocation.remote.dto.CandidatJson;
import com.pfe.convocation.remote.dto.ConcoursJson;
import com.pfe.convocation.remote.dto.SalleAvecLieuxJson;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Agrège, pour chaque candidat affecté, l'ensemble des informations de sa convocation en lisant
 * les trois référentiels (candidat, concours, lieux) via REST (JWT du gestionnaire relayé).
 *
 * <p>Seuls les candidats disposant d'une affectation complète (issue de la répartition :
 * centre + établissement + salle + place) donnent lieu à une convocation.
 */
@Component
public class ConvocationAssembler {

    private final CandidatRemoteClient candidatRemoteClient;
    private final ConcoursRemoteClient concoursRemoteClient;
    private final LieuxRemoteClient lieuxRemoteClient;

    public ConvocationAssembler(
            CandidatRemoteClient candidatRemoteClient,
            ConcoursRemoteClient concoursRemoteClient,
            LieuxRemoteClient lieuxRemoteClient) {
        this.candidatRemoteClient = candidatRemoteClient;
        this.concoursRemoteClient = concoursRemoteClient;
        this.lieuxRemoteClient = lieuxRemoteClient;
    }

    /** Construit la liste des convocations prêtes (candidats affectés), triée par numéro d'inscription. */
    public List<ConvocationData> assemblerToutes(String auth) {
        List<CandidatJson> candidats = candidatRemoteClient.listCandidats(auth);

        Map<String, ConcoursJson> concoursParNumero = indexerConcours(concoursRemoteClient.listConcours(auth));
        // Cache des salles par concours (chargées à la demande) : numeroConcours -> (idSalle -> salle).
        Map<String, Map<Long, SalleAvecLieuxJson>> sallesParConcours = new HashMap<>();

        List<ConvocationData> convocations = new ArrayList<>();
        for (CandidatJson c : candidats) {
            if (c == null || !c.estAffecte() || c.numeroInscription() == null || c.numeroInscription().isBlank()) {
                continue;
            }
            convocations.add(construire(c, concoursParNumero, sallesParConcours, auth));
        }
        convocations.sort((a, b) -> a.numeroInscription().compareToIgnoreCase(b.numeroInscription()));
        return convocations;
    }

    /** Construit la convocation d'un candidat précis (numéro d'inscription), ou {@code null} si non affecté. */
    public ConvocationData assemblerUn(String numeroInscription, String auth) {
        Map<String, ConcoursJson> concoursParNumero = indexerConcours(concoursRemoteClient.listConcours(auth));
        Map<String, Map<Long, SalleAvecLieuxJson>> sallesParConcours = new HashMap<>();

        for (CandidatJson c : candidatRemoteClient.listCandidats(auth)) {
            if (c != null && numeroInscription.equalsIgnoreCase(c.numeroInscription())) {
                return c.estAffecte() ? construire(c, concoursParNumero, sallesParConcours, auth) : null;
            }
        }
        return null;
    }

    private ConvocationData construire(
            CandidatJson c,
            Map<String, ConcoursJson> concoursParNumero,
            Map<String, Map<Long, SalleAvecLieuxJson>> sallesParConcours,
            String auth) {
        SalleAvecLieuxJson salle = resoudreSalle(c, sallesParConcours, auth);
        ConcoursJson concours = c.numeroConcours() == null ? null : concoursParNumero.get(c.numeroConcours());

        String nomConcours = c.nomConcours() != null && !c.nomConcours().isBlank()
                ? c.nomConcours()
                : (concours != null ? concours.nomConcours() : null);

        return new ConvocationData(
                c.numeroInscription(),
                c.nom(),
                c.prenom(),
                c.email(),
                c.numeroConcours(),
                nomConcours,
                salle != null ? salle.nomCentre() : null,
                salle != null ? salle.nomEtablissement() : null,
                salle != null ? salle.nomSalle() : null,
                concours != null ? concours.dateHeureExamen() : null,
                c.numeroPlace());
    }

    private SalleAvecLieuxJson resoudreSalle(
            CandidatJson c, Map<String, Map<Long, SalleAvecLieuxJson>> cache, String auth) {
        if (c.numeroConcours() == null || c.numeroConcours().isBlank() || c.idSalle() == null) {
            return null;
        }
        Map<Long, SalleAvecLieuxJson> salles = cache.computeIfAbsent(
                c.numeroConcours(),
                numero -> indexerSalles(lieuxRemoteClient.listSallesParConcours(numero, auth)));
        return salles.get(c.idSalle());
    }

    private static Map<String, ConcoursJson> indexerConcours(List<ConcoursJson> concours) {
        Map<String, ConcoursJson> map = new LinkedHashMap<>();
        for (ConcoursJson c : concours) {
            if (c != null && c.numeroConcours() != null) {
                map.put(c.numeroConcours(), c);
            }
        }
        return map;
    }

    private static Map<Long, SalleAvecLieuxJson> indexerSalles(List<SalleAvecLieuxJson> salles) {
        Map<Long, SalleAvecLieuxJson> map = new HashMap<>();
        for (SalleAvecLieuxJson s : salles) {
            if (s != null && s.idSalle() != null) {
                map.put(s.idSalle(), s);
            }
        }
        return map;
    }
}
