package com.btp.is.alertservice.config;

import com.btp.is.alertservice.client.CpiOAuthTokenProvider;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(CpiProperties.class)
public class HttpClientConfig {

    @Bean
    public RestClient cpiRestClient(CpiProperties properties, CpiOAuthTokenProvider tokenProvider) {
        String baseUrl = properties.getBaseUrl();
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalStateException(
                    "CPI base URL is not configured. Set cpi.base-url or the CPI_BASE_URL environment variable.");
        }
        return RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .requestInterceptor((request, body, execution) -> {
                    if (properties.getOauth().isConfigured()) {
                        request.getHeaders().setBearerAuth(tokenProvider.getAccessToken());
                    }
                    return execution.execute(request, body);
                })
                .build();
    }
}
