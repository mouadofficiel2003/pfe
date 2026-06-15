package com.pfe.candidats.service;



import com.pfe.candidats.remote.ConcoursRemoteClient;

import com.pfe.candidats.remote.dto.ConcoursHeadJson;

import com.pfe.candidats.support.HttpRequestContext;

import java.util.ArrayList;

import java.util.List;

import java.util.Locale;

import org.springframework.http.HttpStatus;

import org.springframework.stereotype.Component;

import org.springframework.web.server.ResponseStatusException;



/**

 * Résout le concours d'un candidat : {@code numero_concours} est la référence canonique ; {@code nom_concours} est

 * toujours recopié depuis concours-service.

 */

@Component

public class CandidatConcoursResolver {



    private final ConcoursRemoteClient concoursRemoteClient;



    public CandidatConcoursResolver(ConcoursRemoteClient concoursRemoteClient) {

        this.concoursRemoteClient = concoursRemoteClient;

    }



    /** Catalogue chargé une fois par import Excel. */

    public ConcoursCatalog loadCatalog() {

        String auth = HttpRequestContext.authorizationHeaderOrNull();

        return new ConcoursCatalog(concoursRemoteClient.listConcours(auth));

    }



    /**

     * @param nomLibelle libellé fourni (Excel ou formulaire)

     * @param numeroConcours s'il est présent, il prime et est validé via GET /api/concours/{numeroConcours}

     */

    public ResolvedConcours resolve(String nomLibelle, String numeroConcours) {

        String auth = HttpRequestContext.authorizationHeaderOrNull();

        if (numeroConcours != null && !numeroConcours.isBlank()) {

            ConcoursHeadJson byNumero = concoursRemoteClient.fetchConcours(numeroConcours.trim(), auth);

            if (nomLibelle != null && !nomLibelle.isBlank()) {

                assertSameNom(nomLibelle, byNumero.nomConcours());

            }

            return toResolved(byNumero);

        }

        return loadCatalog().resolveByNom(nomLibelle);

    }



    public final class ConcoursCatalog {



        private final List<ConcoursHeadJson> concours;



        private ConcoursCatalog(List<ConcoursHeadJson> concours) {

            this.concours = List.copyOf(concours);

        }



        /**

         * Résolution depuis le catalogue déjà chargé (import Excel). Si le numéro de concours (clé métier,

         * ex. « CSP-2025 ») est fourni il prime — et le libellé, s'il est présent, doit correspondre ;

         * sinon on résout par nom.

         */

        public ResolvedConcours resolveImport(String nomLibelle, String numeroConcours) {

            boolean aNumero = numeroConcours != null && !numeroConcours.isBlank();

            return aNumero ? resolveByNumero(numeroConcours, nomLibelle) : resolveByNom(nomLibelle);

        }



        public ResolvedConcours resolveByNumero(String numeroConcours, String nomLibelle) {

            if (numeroConcours == null || numeroConcours.isBlank()) {

                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Numéro de concours manquant");

            }

            String key = normalize(numeroConcours);

            for (ConcoursHeadJson c : concours) {

                if (c.numeroConcours() != null && normalize(c.numeroConcours()).equals(key)) {

                    if (nomLibelle != null && !nomLibelle.isBlank()) {

                        assertSameNom(nomLibelle, c.nomConcours());

                    }

                    return toResolved(c);

                }

            }

            throw new ResponseStatusException(

                    HttpStatus.BAD_REQUEST,

                    "Aucun concours enregistré n'a le numéro « " + numeroConcours.trim() + " »");

        }



        public ResolvedConcours resolveByNom(String nomLibelle) {

            if (nomLibelle == null || nomLibelle.isBlank()) {

                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nom du concours manquant");

            }

            List<ConcoursHeadJson> matches = new ArrayList<>();

            String key = normalize(nomLibelle);

            for (ConcoursHeadJson c : concours) {

                if (c.nomConcours() != null && normalize(c.nomConcours()).equals(key)) {

                    matches.add(c);

                }

            }

            if (matches.isEmpty()) {

                throw new ResponseStatusException(

                        HttpStatus.BAD_REQUEST,

                        "Aucun concours enregistré ne correspond au libellé « " + nomLibelle.trim() + " »");

            }

            if (matches.size() > 1) {

                throw new ResponseStatusException(

                        HttpStatus.BAD_REQUEST,

                        "Plusieurs concours correspondent au libellé « "

                                + nomLibelle.trim()

                                + " » : précisez numeroConcours");

            }

            return toResolved(matches.get(0));

        }

    }



    private static ResolvedConcours toResolved(ConcoursHeadJson c) {

        return new ResolvedConcours(c.numeroConcours().trim(), c.nomConcours().trim());

    }



    private static void assertSameNom(String nomLibelle, String nomService) {

        if (!normalize(nomLibelle).equals(normalize(nomService))) {

            throw new ResponseStatusException(

                    HttpStatus.BAD_REQUEST,

                    "Le libellé « nom concours » ne correspond pas au concours d'identifiant fourni");

        }

    }



    private static String normalize(String s) {

        return s.trim().toLowerCase(Locale.ROOT);

    }

}


