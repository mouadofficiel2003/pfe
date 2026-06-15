package com.pfe.concours.service;



import static org.assertj.core.api.Assertions.assertThat;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import static org.mockito.ArgumentMatchers.any;

import static org.mockito.ArgumentMatchers.eq;

import static org.mockito.ArgumentMatchers.isNull;

import static org.mockito.Mockito.doThrow;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

import static org.mockito.Mockito.verify;

import static org.mockito.Mockito.verifyNoMoreInteractions;

import static org.mockito.Mockito.when;



import com.pfe.concours.domain.Concours;
import com.pfe.concours.domain.ConcoursAffectationCentre;

import com.pfe.concours.lieux.LieuxCentreClient;

import com.pfe.concours.repository.ConcoursRepository;

import com.pfe.concours.web.dto.CentreAffectationRequest;

import com.pfe.concours.web.dto.ConcoursWriteRequest;

import java.time.Instant;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;

import org.junit.jupiter.api.BeforeEach;

import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;

import org.mockito.Mock;

import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;

import org.springframework.mock.web.MockHttpServletRequest;

import org.springframework.web.context.request.RequestContextHolder;

import org.springframework.web.context.request.ServletRequestAttributes;

import org.springframework.web.server.ResponseStatusException;



@ExtendWith(MockitoExtension.class)

class ConcoursServiceLieuxValidationTest {



    @Mock

    private ConcoursRepository concoursRepository;



    @Mock

    private LieuxCentreClient lieuxCentreClient;



    private ConcoursService concoursService;



    @BeforeEach

    void setUp() {

        concoursService = new ConcoursService(concoursRepository, lieuxCentreClient);

        MockHttpServletRequest request = new MockHttpServletRequest();

        request.addHeader("Authorization", "Bearer test-jwt");

        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

    }



    @AfterEach

    void tearDown() {

        RequestContextHolder.resetRequestAttributes();

    }



    @Test

    void creer_avecIdCentres_appelleLieuxPourChaqueIdDistinct() {

        when(concoursRepository.existsById("C-A")).thenReturn(false);

        when(concoursRepository.save(any(Concours.class))).thenAnswer(invocation -> invocation.getArgument(0));



        Instant exam = Instant.parse("2026-06-01T08:00:00Z");

        ConcoursWriteRequest req = new ConcoursWriteRequest(

                "Concours A",

                "C-A",

                exam,

                List.of(

                        new CentreAffectationRequest(5L, "Rabat"),

                        new CentreAffectationRequest(8L, "Casablanca"),

                        new CentreAffectationRequest(5L, "Rabat")));



        assertThatThrownBy(() -> concoursService.creer(req))

                .isInstanceOf(ResponseStatusException.class)

                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode().value()).isEqualTo(400));



        ConcoursWriteRequest reqOk = new ConcoursWriteRequest(

                "Concours A",

                "C-A",

                exam,

                List.of(

                        new CentreAffectationRequest(5L, "Rabat"),

                        new CentreAffectationRequest(8L, "Casablanca"),

                        new CentreAffectationRequest(9L, "Agadir")));



        concoursService.creer(reqOk);



        verify(lieuxCentreClient).assertCentreExists(eq(5L), eq("Bearer test-jwt"));

        verify(lieuxCentreClient).assertCentreExists(eq(8L), eq("Bearer test-jwt"));

        verify(lieuxCentreClient).assertCentreExists(eq(9L), eq("Bearer test-jwt"));

        verify(lieuxCentreClient, times(3)).assertCentreExists(any(), any());

        verifyNoMoreInteractions(lieuxCentreClient);



        ArgumentCaptor<Concours> captor = ArgumentCaptor.forClass(Concours.class);

        verify(concoursRepository).save(captor.capture());

        assertThat(captor.getValue().getAffectationsCentres()).hasSize(3);

        assertThat(captor.getValue().getNumeroConcours()).isEqualTo("C-A");

    }



    @Test

    void creer_avecIdCentre_sansAuthorization_echoueAvantSave() {

        RequestContextHolder.resetRequestAttributes();

        doThrow(new ResponseStatusException(

                        HttpStatus.UNAUTHORIZED,

                        "En-tête Authorization (Bearer) requis pour valider le centre auprès du service lieux"))

                .when(lieuxCentreClient)

                .assertCentreExists(eq(12L), isNull());



        ConcoursWriteRequest req = new ConcoursWriteRequest(

                "Concours C",

                "C-C",

                Instant.parse("2026-06-03T08:00:00Z"),

                List.of(new CentreAffectationRequest(12L, "Salé")));



        assertThatThrownBy(() -> concoursService.creer(req))

                .isInstanceOf(ResponseStatusException.class)

                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode().value()).isEqualTo(401));



        verify(concoursRepository, never()).save(any());

    }

    @Test
    void mettreAJour_conserveCentreExistantEtAjouteNouveau() {
        Instant exam = Instant.parse("2026-07-16T10:00:00Z");
        Concours existant = new Concours();
        existant.setNumeroConcours("CADM-2025");
        existant.setNomConcours("Concours Administration");
        existant.setDateHeureExamen(exam);
        existant.setCreeLe(exam);
        existant.setModifieLe(exam);

        ConcoursAffectationCentre casablanca = new ConcoursAffectationCentre();
        casablanca.setId(1L);
        casablanca.setIdCentre(2L);
        casablanca.setNomCentre("Centre Casablanca");
        casablanca.setConcours(existant);
        existant.setAffectationsCentres(new ArrayList<>(List.of(casablanca)));

        when(concoursRepository.findByIdWithAffectations("CADM-2025")).thenReturn(Optional.of(existant));
        when(concoursRepository.save(any(Concours.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ConcoursWriteRequest req = new ConcoursWriteRequest(
                "Concours Administration",
                "CADM-2025",
                exam,
                List.of(
                        new CentreAffectationRequest(2L, "Centre Casablanca"),
                        new CentreAffectationRequest(5L, "Centre Rabat")));

        concoursService.mettreAJour("CADM-2025", req);

        ArgumentCaptor<Concours> captor = ArgumentCaptor.forClass(Concours.class);
        verify(concoursRepository).save(captor.capture());
        Concours saved = captor.getValue();
        assertThat(saved.getAffectationsCentres()).hasSize(2);
        assertThat(saved.getAffectationsCentres())
                .anySatisfy(a -> {
                    assertThat(a.getId()).isEqualTo(1L);
                    assertThat(a.getIdCentre()).isEqualTo(2L);
                })
                .anySatisfy(a -> {
                    assertThat(a.getId()).isNull();
                    assertThat(a.getIdCentre()).isEqualTo(5L);
                    assertThat(a.getNomCentre()).isEqualTo("Centre Rabat");
                });
    }
}

