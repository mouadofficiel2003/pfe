package com.pfe.lieux.concours;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

class ConcoursExistenceClientTest {

    private MockWebServer mockWebServer;
    private ConcoursExistenceClient client;

    @BeforeEach
    void setUp() throws Exception {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        String baseUrl = "http://localhost:" + mockWebServer.getPort();
        RestClient restClient = RestClient.builder().baseUrl(baseUrl).build();
        client = new ConcoursExistenceClient(restClient);
    }

    @AfterEach
    void tearDown() throws Exception {
        mockWebServer.shutdown();
    }

    @Test
    void assertConcoursExists_nullNumero_nePasAppelerHttp() throws Exception {
        client.assertConcoursExists(null, "Bearer token");
        assertThat(mockWebServer.getRequestCount()).isZero();
    }

    @Test
    void assertConcoursExists_200_nePasLever() throws Exception {
        mockWebServer.enqueue(new MockResponse().setResponseCode(200));
        client.assertConcoursExists("C-3", "Bearer abc.def.ghi");
        var req = mockWebServer.takeRequest();
        assertThat(req.getMethod()).isEqualTo("GET");
        assertThat(req.getPath()).isEqualTo("/api/concours/C-3");
        assertThat(req.getHeader("Authorization")).isEqualTo("Bearer abc.def.ghi");
    }

    @Test
    void assertConcoursExists_404_badRequest() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(404));
        assertThatThrownBy(() -> client.assertConcoursExists("C-99", "Bearer x"))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode().value()).isEqualTo(400));
    }

    @Test
    void assertConcoursExists_401_badGateway() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(401));
        assertThatThrownBy(() -> client.assertConcoursExists("C-1", "Bearer x"))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode().value()).isEqualTo(502));
    }

    @Test
    void listConcoursNumerosByCentre_200_retourneNumeros() throws Exception {
        mockWebServer.enqueue(
                new MockResponse()
                        .setResponseCode(200)
                        .setHeader("Content-Type", "application/json")
                        .setBody(
                                "[{\"numeroConcours\":\"C-1\",\"nomConcours\":\"A\"},{\"numeroConcours\":\"C-2\",\"nomConcours\":\"B\"}]"));
        assertThat(client.listConcoursNumerosByCentre(7L, "Bearer token")).containsExactly("C-1", "C-2");
        var req = mockWebServer.takeRequest();
        assertThat(req.getPath()).isEqualTo("/api/concours/by-centre/7");
    }

    @Test
    void listConcoursNumerosByCentre_sansBearer_listeVide() {
        assertThat(client.listConcoursNumerosByCentre(7L, null)).isEmpty();
        assertThat(mockWebServer.getRequestCount()).isZero();
    }

    @Test
    void assertConcoursExists_sansBearer_unauthorized() {
        assertThatThrownBy(() -> client.assertConcoursExists("C-1", "Basic xxx"))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode().value()).isEqualTo(401));
    }
}
