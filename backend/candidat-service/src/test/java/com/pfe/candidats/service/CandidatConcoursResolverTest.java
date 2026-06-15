package com.pfe.candidats.service;



import static org.assertj.core.api.Assertions.assertThat;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import static org.mockito.Mockito.when;



import com.pfe.candidats.remote.ConcoursRemoteClient;

import com.pfe.candidats.remote.dto.ConcoursHeadJson;

import java.util.List;

import org.junit.jupiter.api.AfterEach;

import org.junit.jupiter.api.BeforeEach;

import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;

import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.mock.web.MockHttpServletRequest;

import org.springframework.web.context.request.RequestContextHolder;

import org.springframework.web.context.request.ServletRequestAttributes;

import org.springframework.web.server.ResponseStatusException;



@ExtendWith(MockitoExtension.class)

class CandidatConcoursResolverTest {



    @Mock

    private ConcoursRemoteClient concoursRemoteClient;



    private CandidatConcoursResolver resolver;



    @BeforeEach

    void setUp() {

        resolver = new CandidatConcoursResolver(concoursRemoteClient);

        MockHttpServletRequest request = new MockHttpServletRequest();

        request.addHeader("Authorization", "Bearer test-token");

        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

    }



    @AfterEach

    void tearDown() {

        RequestContextHolder.resetRequestAttributes();

    }



    @Test

    void resolveByNom_trouveUnSeul() {

        when(concoursRemoteClient.listConcours("Bearer test-token"))

                .thenReturn(List.of(

                        new ConcoursHeadJson("CA-2025", "Concours A"),

                        new ConcoursHeadJson("AU-2025", "Autre")));

        ResolvedConcours r = resolver.loadCatalog().resolveImport("concours a", null);

        assertThat(r.numeroConcours()).isEqualTo("CA-2025");

        assertThat(r.nomConcours()).isEqualTo("Concours A");

    }



    @Test

    void resolveByNom_inconnu_badRequest() {

        when(concoursRemoteClient.listConcours("Bearer test-token"))

                .thenReturn(List.of(new ConcoursHeadJson("CA-2025", "Concours A")));

        assertThatThrownBy(() -> resolver.loadCatalog().resolveImport("Inexistant", null))

                .isInstanceOf(ResponseStatusException.class)

                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode().value()).isEqualTo(400));

    }



    @Test

    void resolveByNumero_utiliseNumeroMetier() {

        when(concoursRemoteClient.listConcours("Bearer test-token"))

                .thenReturn(List.of(

                        new ConcoursHeadJson("CSP-2025", "Concours Sante Publique"),

                        new ConcoursHeadJson("AU-2025", "Autre")));

        ResolvedConcours r = resolver.loadCatalog().resolveImport(null, "CSP-2025");

        assertThat(r.numeroConcours()).isEqualTo("CSP-2025");

        assertThat(r.nomConcours()).isEqualTo("Concours Sante Publique");

    }



    @Test

    void resolveByNumero_inconnu_badRequest() {

        when(concoursRemoteClient.listConcours("Bearer test-token"))

                .thenReturn(List.of(new ConcoursHeadJson("CSP-2025", "Concours Sante Publique")));

        assertThatThrownBy(() -> resolver.loadCatalog().resolveImport(null, "XX-9999"))

                .isInstanceOf(ResponseStatusException.class)

                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode().value()).isEqualTo(400));

    }



    @Test

    void resolveByNumero_utiliseNomOfficiel() {

        when(concoursRemoteClient.fetchConcours("NO-2025", "Bearer test-token"))

                .thenReturn(new ConcoursHeadJson("NO-2025", "Nom officiel"));

        ResolvedConcours r = resolver.resolve("Nom officiel", "NO-2025");

        assertThat(r.numeroConcours()).isEqualTo("NO-2025");

        assertThat(r.nomConcours()).isEqualTo("Nom officiel");

    }

}


