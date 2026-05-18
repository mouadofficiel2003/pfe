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
import com.pfe.concours.lieux.LieuxCentreClient;
import com.pfe.concours.repository.ConcoursRepository;
import com.pfe.concours.web.dto.CentreAffectationRequest;
import com.pfe.concours.web.dto.ConcoursWriteRequest;
import java.time.Instant;
import java.util.List;
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
    void creer_avecCentreIds_appelleLieuxPourChaqueIdDistinct() {
        when(concoursRepository.save(any(Concours.class))).thenAnswer(invocation -> {
            Concours c = invocation.getArgument(0);
            if (c.getId() == null) {
                c.setId(100L);
            }
            return c;
        });

        Instant exam = Instant.parse("2026-06-01T08:00:00Z");
        ConcoursWriteRequest req = new ConcoursWriteRequest(
                "Concours A",
                null,
                exam,
                List.of(
                        new CentreAffectationRequest("Rabat", 5L),
                        new CentreAffectationRequest("Casablanca", 8L),
                        new CentreAffectationRequest("Agadir", 5L)));

        concoursService.creer(req);

        verify(lieuxCentreClient).assertCentreExists(eq(5L), eq("Bearer test-jwt"));
        verify(lieuxCentreClient).assertCentreExists(eq(8L), eq("Bearer test-jwt"));
        verify(lieuxCentreClient, times(2)).assertCentreExists(any(), any());
        verifyNoMoreInteractions(lieuxCentreClient);

        ArgumentCaptor<Concours> captor = ArgumentCaptor.forClass(Concours.class);
        verify(concoursRepository).save(captor.capture());
        assertThat(captor.getValue().getAffectationsCentres()).hasSize(3);
    }

    @Test
    void creer_sansCentreId_nAppellePasLieux() {
        when(concoursRepository.save(any(Concours.class))).thenAnswer(invocation -> {
            Concours c = invocation.getArgument(0);
            c.setId(1L);
            return c;
        });
        ConcoursWriteRequest req = new ConcoursWriteRequest(
                "Concours B",
                null,
                Instant.parse("2026-06-02T08:00:00Z"),
                List.of(new CentreAffectationRequest("Marrakech", null)));

        concoursService.creer(req);

        verify(lieuxCentreClient, never()).assertCentreExists(any(), any());
    }

    @Test
    void creer_avecCentreId_sansAuthorization_echoueAvantSave() {
        // Simule le refus de LieuxCentreClient sans Authorization (même contrat qu'en production).
        RequestContextHolder.resetRequestAttributes();
        doThrow(new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "En-tête Authorization (Bearer) requis pour valider le centre auprès du service lieux"))
                .when(lieuxCentreClient)
                .assertCentreExists(eq(12L), isNull());

        ConcoursWriteRequest req = new ConcoursWriteRequest(
                "Concours C",
                null,
                Instant.parse("2026-06-03T08:00:00Z"),
                List.of(new CentreAffectationRequest("Salé", 12L)));

        assertThatThrownBy(() -> concoursService.creer(req))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode().value()).isEqualTo(401));

        verify(concoursRepository, never()).save(any());
    }
}
