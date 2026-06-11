package com.quachtd.btp.is.alertservice.client;

import com.quachtd.btp.is.alertservice.config.EventMeshProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

@Component
public class EventMeshOAuthTokenProvider {

    private static final Logger log = LoggerFactory.getLogger(EventMeshOAuthTokenProvider.class);

    private final EventMeshProperties properties;
    private final RestClient tokenClient;

    private String cachedToken;
    private Instant tokenExpiresAt = Instant.EPOCH;

    public EventMeshOAuthTokenProvider(EventMeshProperties properties) {
        this.properties = properties;
        this.tokenClient = RestClient.create();
    }

    public synchronized String getAccessToken() {
        if (cachedToken != null && Instant.now().isBefore(tokenExpiresAt)) {
            return cachedToken;
        }

        EventMeshProperties.OAuth oauth = properties.getOauth();
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "client_credentials");

        String basicAuth = Base64.getEncoder().encodeToString(
                (oauth.getClientId() + ":" + oauth.getClientSecret()).getBytes(StandardCharsets.UTF_8));

        try {
            TokenResponse response = tokenClient.post()
                    .uri(oauth.getTokenUrl())
                    .header(HttpHeaders.AUTHORIZATION, "Basic " + basicAuth)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .body(TokenResponse.class);

            if (response == null || response.accessToken == null) {
                throw new IllegalStateException("Failed to obtain Event Mesh OAuth access token");
            }

            cachedToken = response.accessToken;
            long expiresIn = response.expiresIn != null ? response.expiresIn : 3600;
            tokenExpiresAt = Instant.now().plusSeconds(Math.max(60, expiresIn - 60));
            return cachedToken;
        } catch (RuntimeException ex) {
            log.error("Event Mesh token request failed for client-id length {} at {}",
                    oauth.getClientId() != null ? oauth.getClientId().length() : 0,
                    oauth.getTokenUrl(),
                    ex);
            throw ex;
        }
    }

    private static class TokenResponse {
        @JsonProperty("access_token")
        private String accessToken;

        @JsonProperty("expires_in")
        private Long expiresIn;
    }
}
