package com.pfe.repartition.service;



import static org.assertj.core.api.Assertions.assertThat;



import com.pfe.repartition.geo.CarteMaroc;

import com.pfe.repartition.geo.ReferentielRegionsMaroc;

import com.pfe.repartition.remote.dto.CandidatJson;

import com.pfe.repartition.remote.dto.ConcoursJson;

import com.pfe.repartition.remote.dto.SalleAvecLieuxJson;

import com.pfe.repartition.service.RepartitionPlanner.AffectationPlanifiee;

import com.pfe.repartition.service.RepartitionPlanner.Plan;

import java.io.IOException;

import java.util.List;

import java.util.Map;

import org.junit.jupiter.api.Test;



/** Vérifie que le candidat est affecté au centre dont la ville est la plus proche de la sienne. */

class RepartitionPlannerTest {



    private final RepartitionPlanner planner = new RepartitionPlanner(carte(), referentiel());



    private static CarteMaroc carte() {

        try {

            return new CarteMaroc();

        } catch (IOException e) {

            throw new IllegalStateException(e);

        }

    }



    private static ReferentielRegionsMaroc referentiel() {

        try {

            ReferentielRegionsMaroc ref = new ReferentielRegionsMaroc(carte());

            ref.chargerPourTests();

            return ref;

        } catch (IOException e) {

            throw new IllegalStateException(e);

        }

    }



    @Test

    void affecteCandidatAuCentreDeSaVilleMemeAvecPrefixeCentre() {

        ConcoursJson concours = new ConcoursJson("CSP-2025", "Concours Sante Publique");

        SalleAvecLieuxJson salleRabat = new SalleAvecLieuxJson(

                100L, "Salle C03", 35, "CSP-2025", 1000L, "Faculte Rabat", 10L, "Centre Rabat");

        SalleAvecLieuxJson salleFes =

                new SalleAvecLieuxJson(200L, "Amphi Sud", 150, "CSP-2025", 2000L, "Universite Fes", 20L, "Centre Fes");



        CandidatJson candidatFes =

                new CandidatJson("INS-001", "CHERKAOUI", "Mohamed", "Fès", "Concours Sante Publique", "CSP-2025");

        CandidatJson candidatRabat =

                new CandidatJson("INS-002", "AMRANI", "Youssef", "Rabat", "Concours Sante Publique", "CSP-2025");



        Plan plan = planner.planifier(

                List.of(concours),

                Map.of("CSP-2025", List.of(salleRabat, salleFes)),

                List.of(candidatFes, candidatRabat));



        assertThat(plan.alertes()).isEmpty();

        assertThat(centrePour(plan, "INS-001")).isEqualTo("Centre Fes");

        assertThat(centrePour(plan, "INS-002")).isEqualTo("Centre Rabat");

    }



    @Test

    void affecteCandidatProvinceALaVilleConcoursDeSaRegion() {

        ConcoursJson concours = new ConcoursJson("CSP-2025", "Concours Sante Publique");

        SalleAvecLieuxJson salleCasablanca = new SalleAvecLieuxJson(

                100L, "Salle A", 50, "CSP-2025", 1000L, "Faculte Casa", 10L, "Centre Casablanca");

        SalleAvecLieuxJson salleMarrakech = new SalleAvecLieuxJson(

                200L, "Salle B", 50, "CSP-2025", 2000L, "Faculte Marrakech", 20L, "Centre Marrakech");



        CandidatJson candidatChichaoua = new CandidatJson(

                "INS-010", "ALAMI", "Fatima", "Chichaoua", "Concours Sante Publique", "CSP-2025");



        Plan plan = planner.planifier(

                List.of(concours),

                Map.of("CSP-2025", List.of(salleCasablanca, salleMarrakech)),

                List.of(candidatChichaoua));



        assertThat(plan.alertes()).isEmpty();

        assertThat(centrePour(plan, "INS-010")).isEqualTo("Centre Marrakech");

    }



    @Test

    void affecteCandidatKhouribgaAuCentreDeSaRegionPlutotQueCasablanca() {

        ConcoursJson concours = new ConcoursJson("CSP-2025", "Concours Sante Publique");

        SalleAvecLieuxJson salleCasablanca = new SalleAvecLieuxJson(

                100L, "Salle A", 50, "CSP-2025", 1000L, "Faculte Casa", 10L, "Centre Casablanca");

        SalleAvecLieuxJson salleBeniMellal = new SalleAvecLieuxJson(

                200L, "Salle B", 50, "CSP-2025", 2000L, "Faculte Beni Mellal", 20L, "Centre Beni Mellal");



        CandidatJson candidat = new CandidatJson(

                "INS-011", "BENALI", "Karim", "Khouribga", "Concours Sante Publique", "CSP-2025");



        Plan plan = planner.planifier(

                List.of(concours),

                Map.of("CSP-2025", List.of(salleCasablanca, salleBeniMellal)),

                List.of(candidat));



        assertThat(plan.alertes()).isEmpty();

        assertThat(centrePour(plan, "INS-011")).isEqualTo("Centre Beni Mellal");

    }



    @Test

    void affecteSidiIfniAMarrakechQuandLesDeuxCentresExist() {

        ConcoursJson concours = new ConcoursJson("CSP-2025", "Concours Sante Publique");

        SalleAvecLieuxJson salleCasablanca = new SalleAvecLieuxJson(

                100L, "Salle A", 50, "CSP-2025", 1000L, "Faculte Casa", 10L, "Centre Casablanca");

        SalleAvecLieuxJson salleMarrakech = new SalleAvecLieuxJson(

                200L, "Salle B", 50, "CSP-2025", 2000L, "Faculte Marrakech", 20L, "Centre Marrakech");



        CandidatJson candidat = new CandidatJson(

                "INS-020", "MESSI", "Lionel", "Sidi Ifni", "Concours Sante Publique", "CSP-2025");



        Plan plan = planner.planifier(

                List.of(concours),

                Map.of("CSP-2025", List.of(salleCasablanca, salleMarrakech)),

                List.of(candidat));



        assertThat(plan.alertes()).isEmpty();

        assertThat(centrePour(plan, "INS-020")).isEqualTo("Centre Marrakech");

    }



    @Test

    void affecteSidiIfniACasablancaSiMarrakechNonConfigure() {

        ConcoursJson concours = new ConcoursJson("CSP-2025", "Concours Sante Publique");

        SalleAvecLieuxJson salleCasablanca = new SalleAvecLieuxJson(

                100L, "Salle A", 50, "CSP-2025", 1000L, "Faculte Casa", 10L, "Centre Casablanca");

        SalleAvecLieuxJson salleRabat = new SalleAvecLieuxJson(

                300L, "Salle C", 50, "CSP-2025", 3000L, "Faculte Rabat", 30L, "Centre Rabat");



        CandidatJson candidat = new CandidatJson(

                "INS-021", "MESSI", "Lionel", "Sidi Ifni", "Concours Sante Publique", "CSP-2025");



        Plan plan = planner.planifier(

                List.of(concours),

                Map.of("CSP-2025", List.of(salleCasablanca, salleRabat)),

                List.of(candidat));



        assertThat(plan.alertes()).isEmpty();

        assertThat(centrePour(plan, "INS-021")).isEqualTo("Centre Casablanca");

    }



    @Test

    void reconnaitCentreMarrackechAvecFauteDeOrthographe() {

        ConcoursJson concours = new ConcoursJson("CSP-2025", "Concours Sante Publique");

        SalleAvecLieuxJson salleCasablanca = new SalleAvecLieuxJson(

                100L, "Salle c", 30, "CSP-2025", 1000L, "Ibn Tofail", 10L, "Centre Casablanca");

        SalleAvecLieuxJson salleMarrackech = new SalleAvecLieuxJson(

                200L, "Salle C", 30, "CSP-2025", 2000L, "Madaris Maria", 20L, "Centre Marrackech");



        CandidatJson candidatMarrakech = new CandidatJson(

                "INS-030", "DAOUDI", "Salma", "Marrakech", "Concours Sante Publique", "CSP-2025");

        CandidatJson candidatSidiIfni = new CandidatJson(

                "INS-031", "MESSI", "Lionel", "Sidi Ifni", "Concours Sante Publique", "CSP-2025");



        Plan plan = planner.planifier(

                List.of(concours),

                Map.of("CSP-2025", List.of(salleCasablanca, salleMarrackech)),

                List.of(candidatMarrakech, candidatSidiIfni));



        assertThat(plan.alertes()).isEmpty();

        assertThat(centrePour(plan, "INS-030")).isEqualTo("Centre Marrackech");

        assertThat(centrePour(plan, "INS-031")).isEqualTo("Centre Marrackech");

    }



    private static String centrePour(Plan plan, String numeroInscription) {

        return plan.affectations().stream()

                .filter(a -> numeroInscription.equals(a.candidat().numeroInscription()))

                .map(AffectationPlanifiee::salle)

                .map(SalleAvecLieuxJson::nomCentre)

                .findFirst()

                .orElseThrow();

    }

}


