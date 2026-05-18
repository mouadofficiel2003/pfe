package com.pfe.candidats.remote;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RemoteClientsConfig {

    public static final String CONCOURS_REST_CLIENT = "concoursRestClient";
    public static final String LIEUX_REST_CLIENT = "lieuxRestClient";

    @Bean(name = CONCOURS_REST_CLIENT)
    RestClient concoursRestClient(@Value("${concours.service.base-url:http://localhost:8083}") String baseUrl) {
        return RestClient.builder().baseUrl(trimTrailingSlash(baseUrl)).build();
    }

    @Bean(name = LIEUX_REST_CLIENT)
    RestClient lieuxRestClient(@Value("${lieux.service.base-url:http://localhost:8084}") String baseUrl) {
        return RestClient.builder().baseUrl(trimTrailingSlash(baseUrl)).build();
    }

    private static String trimTrailingSlash(String baseUrl) {
        return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }
}
