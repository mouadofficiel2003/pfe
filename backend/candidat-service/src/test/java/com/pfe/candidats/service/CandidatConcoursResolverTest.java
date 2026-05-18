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
                .thenReturn(List.of(new ConcoursHeadJson(1L, "Concours A"), new ConcoursHeadJson(2L, "Autre")));
        ResolvedConcours r = resolver.resolve("concours a", null);
        assertThat(r.concoursId()).isEqualTo(1L);
        assertThat(r.nomConcours()).isEqualTo("Concours A");
    }

    @Test
    void resolveByNom_inconnu_badRequest() {
        when(concoursRemoteClient.listConcours("Bearer test-token"))
                .thenReturn(List.of(new ConcoursHeadJson(1L, "Concours A")));
        assertThatThrownBy(() -> resolver.resolve("Inexistant", null))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode().value()).isEqualTo(400));
    }

    @Test
    void resolveById_utiliseNomOfficiel() {
        when(concoursRemoteClient.fetchConcours(5L, "Bearer test-token"))
                .thenReturn(new ConcoursHeadJson(5L, "Nom officiel"));
        ResolvedConcours r = resolver.resolve("Nom officiel", 5L);
        assertThat(r.concoursId()).isEqualTo(5L);
        assertThat(r.nomConcours()).isEqualTo("Nom officiel");
    }
}
