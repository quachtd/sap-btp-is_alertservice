package com.btp.is.alertservice.client;

import com.btp.is.alertservice.config.CpiProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.time.Instant;

@Component
public class CpiOAuthTokenProvider {

    private final CpiProperties properties;
    private final RestClient tokenClient;

    private String cachedToken;
    private Instant tokenExpiresAt = Instant.EPOCH;

    public CpiOAuthTokenProvider(CpiProperties properties) {
        this.properties = properties;
        this.tokenClient = RestClient.create();
    }

    public synchronized String getAccessToken() {
        if (cachedToken != null && Instant.now().isBefore(tokenExpiresAt)) {
            return cachedToken;
        }

        CpiProperties.OAuth oauth = properties.getOauth();
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "client_credentials");
        form.add("client_id", oauth.getClientId());
        form.add("client_secret", oauth.getClientSecret());

        TokenResponse response = tokenClient.post()
                .uri(oauth.getTokenUrl())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(TokenResponse.class);

        if (response == null || response.accessToken == null) {
            throw new IllegalStateException("Failed to obtain CPI OAuth access token");
        }

        cachedToken = response.accessToken;
        long expiresIn = response.expiresIn != null ? response.expiresIn : 3600;
        tokenExpiresAt = Instant.now().plusSeconds(Math.max(60, expiresIn - 60));
        return cachedToken;
    }

    private static class TokenResponse {
        @JsonProperty("access_token")
        private String accessToken;

        @JsonProperty("expires_in")
        private Long expiresIn;
    }
}
