package com.btp.is.alertservice.config;

import com.btp.is.alertservice.client.EventMeshOAuthTokenProvider;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(EventMeshProperties.class)
public class EventMeshHttpClientConfig {

    @Bean
    public RestClient eventMeshRestClient(
            EventMeshProperties properties,
            EventMeshOAuthTokenProvider tokenProvider) {
        RestClient.Builder builder = RestClient.builder()
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        if (properties.getBaseUrl() != null && !properties.getBaseUrl().isBlank()) {
            builder.baseUrl(properties.getBaseUrl());
        }

        return builder.requestInterceptor((request, body, execution) -> {
                    if (properties.getOauth().isConfigured()) {
                        request.getHeaders().setBearerAuth(tokenProvider.getAccessToken());
                    }
                    return execution.execute(request, body);
                })
                .build();
    }
}
