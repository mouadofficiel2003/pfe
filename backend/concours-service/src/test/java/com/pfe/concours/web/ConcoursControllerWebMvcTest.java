package com.pfe.concours.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.pfe.concours.security.JwtAuthenticationFilter;
import com.pfe.concours.service.ConcoursService;
import com.pfe.concours.web.dto.CentreAffectationResponse;
import com.pfe.concours.web.dto.ConcoursHeadResponse;
import com.pfe.concours.web.dto.ConcoursResponse;
import java.time.Instant;
import java.util.List;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = ConcoursController.class)
@AutoConfigureMockMvc
class ConcoursControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private ConcoursService concoursService;

    @BeforeEach
    void laisserPasserLaChaineDeFiltres() throws Exception {
        lenient()
                .doAnswer(invocation -> {
                    HttpServletRequest req = invocation.getArgument(0);
                    HttpServletResponse res = invocation.getArgument(1);
                    FilterChain chain = invocation.getArgument(2);
                    chain.doFilter(req, res);
                    return null;
                })
                .when(jwtAuthenticationFilter)
                .doFilter(any(), any(), any());
    }

    @Test
    @WithMockUser(roles = "GESTIONNAIRE")
    void getParCentre_retourne200() throws Exception {
        when(concoursService.listerEnTeteParCentre(7L)).thenReturn(List.of(new ConcoursHeadResponse(1L, "Concours A")));
        mockMvc.perform(get("/api/concours/by-centre/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].nomConcours").value("Concours A"));
    }

    @Test
    @WithMockUser(roles = "GESTIONNAIRE")
    void getListe_retourne200() throws Exception {
        when(concoursService.listerTous()).thenReturn(List.of());
        mockMvc.perform(get("/api/concours")).andExpect(status().isOk()).andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(roles = "GESTIONNAIRE")
    void postCreer_retourne200EtCorps() throws Exception {
        Instant t = Instant.parse("2026-01-10T12:00:00Z");
        ConcoursResponse resp =
                new ConcoursResponse(1L, "Mon concours", null, t, List.of(new CentreAffectationResponse(1L, "Rabat", null)), t, t);
        when(concoursService.creer(any())).thenReturn(resp);

        String json =
                """
                {"nomConcours":"Mon concours","numeroConcours":null,"dateHeureExamen":"2026-01-10T12:00:00Z","centres":[{"nomCentre":"Rabat","centreId":null}]}
                """;

        mockMvc.perform(post("/api/concours").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nomConcours").value("Mon concours"));
    }

    @Test
    @WithMockUser(roles = "GESTIONNAIRE")
    void postSansCentres_retourne400() throws Exception {
        String json = "{\"nomConcours\":\"X\",\"dateHeureExamen\":\"2026-01-10T12:00:00Z\",\"centres\":[]}";
        mockMvc.perform(post("/api/concours").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest());
    }
}
