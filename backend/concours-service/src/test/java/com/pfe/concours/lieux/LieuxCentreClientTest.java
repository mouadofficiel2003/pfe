package com.pfe.concours.lieux;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

/**
 * Tests HTTP du client lieux via {@link MockWebServer} (recommandé pour RestClient dans la doc Spring).
 */
class LieuxCentreClientTest {

    private MockWebServer mockWebServer;
    private LieuxCentreClient client;

    @BeforeEach
    void setUp() throws Exception {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        String baseUrl = "http://localhost:" + mockWebServer.getPort();
        RestClient restClient = RestClient.builder().baseUrl(baseUrl).build();
        client = new LieuxCentreClient(restClient);
    }

    @AfterEach
    void tearDown() throws Exception {
        mockWebServer.shutdown();
    }

    @Test
    void assertCentreExists_nullId_nePasAppelerHttp() throws Exception {
        client.assertCentreExists(null, "Bearer token");
        assertThat(mockWebServer.getRequestCount()).isZero();
    }

    @Test
    void assertCentreExists_200_nePasLever() throws Exception {
        mockWebServer.enqueue(new MockResponse().setResponseCode(200));
        client.assertCentreExists(7L, "Bearer abc.def.ghi");
        var req = mockWebServer.takeRequest();
        assertThat(req.getMethod()).isEqualTo("GET");
        assertThat(req.getPath()).isEqualTo("/api/centres/7");
        assertThat(req.getHeader("Authorization")).isEqualTo("Bearer abc.def.ghi");
    }

    @Test
    void assertCentreExists_404_badRequest() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(404));
        assertThatThrownBy(() -> client.assertCentreExists(99L, "Bearer x"))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode().value()).isEqualTo(400));
    }

    @Test
    void assertCentreExists_401_badGateway() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(401));
        assertThatThrownBy(() -> client.assertCentreExists(1L, "Bearer x"))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode().value()).isEqualTo(502));
    }

    @Test
    void assertCentreExists_sansBearer_unauthorized() {
        assertThatThrownBy(() -> client.assertCentreExists(1L, "Basic xxx"))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode().value()).isEqualTo(401));
    }

    @Test
    void assertCentreExists_sansAuthorization_unauthorized() {
        assertThatThrownBy(() -> client.assertCentreExists(1L, null))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode().value()).isEqualTo(401));
    }
}
