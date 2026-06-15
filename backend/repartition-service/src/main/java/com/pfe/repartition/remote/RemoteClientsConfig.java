package com.pfe.repartition.remote;

import java.net.http.HttpClient;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class RemoteClientsConfig {

    public static final String CONCOURS_REST_CLIENT = "concoursRestClient";
    public static final String LIEUX_REST_CLIENT = "lieuxRestClient";
    public static final String CANDIDAT_REST_CLIENT = "candidatRestClient";

    @Bean(name = CONCOURS_REST_CLIENT)
    RestClient concoursRestClient(@Value("${concours.service.base-url:http://localhost:8083}") String baseUrl) {
        return RestClient.builder()
                .baseUrl(trimTrailingSlash(baseUrl))
                .requestFactory(timeoutRequestFactory())
                .build();
    }

    @Bean(name = LIEUX_REST_CLIENT)
    RestClient lieuxRestClient(@Value("${lieux.service.base-url:http://localhost:8084}") String baseUrl) {
        return RestClient.builder()
                .baseUrl(trimTrailingSlash(baseUrl))
                .requestFactory(timeoutRequestFactory())
                .build();
    }

    @Bean(name = CANDIDAT_REST_CLIENT)
    RestClient candidatRestClient(@Value("${candidat.service.base-url:http://localhost:8082}") String baseUrl) {
        return RestClient.builder()
                .baseUrl(trimTrailingSlash(baseUrl))
                .requestFactory(timeoutRequestFactory())
                .build();
    }

    /**
     * Fabrique basée sur le client HTTP du JDK (java.net.http.HttpClient) : contrairement à
     * {@code SimpleClientHttpRequestFactory} (HttpURLConnection), elle supporte la méthode PATCH,
     * nécessaire pour écrire les affectations sur le service candidat.
     */
    private static ClientHttpRequestFactory timeoutRequestFactory() {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(Duration.ofSeconds(15));
        return factory;
    }

    private static String trimTrailingSlash(String baseUrl) {
        return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }
}
