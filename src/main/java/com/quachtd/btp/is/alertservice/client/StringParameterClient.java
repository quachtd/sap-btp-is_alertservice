package com.quachtd.btp.is.alertservice.client;

import com.quachtd.btp.is.alertservice.model.odata.StringParameterResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.Map;
import java.util.Optional;

@Component
public class StringParameterClient {

    private static final Logger log = LoggerFactory.getLogger(StringParameterClient.class);

    private final RestClient restClient;

    public StringParameterClient(RestClient cpiRestClient) {
        this.restClient = cpiRestClient;
    }

    public Optional<String> getValue(String pid, String id) {
        String uri = String.format("/api/v1/StringParameters(Pid='%s',Id='%s')?$select=Value", pid, id);
        log.debug("uri: {}", uri);
        try {
            StringParameterResponse response = restClient.get()
                    .uri(uri)
                    .retrieve()
                    .body(StringParameterResponse.class);
            if (response == null || response.getValue() == null || response.getValue().isBlank()) {
                return Optional.empty();
            }
            return Optional.of(response.getValue());
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == 404) {
                return Optional.empty();
            }
            throw ex;
        }
    }

    public void setValue(String pid, String id, String value) {
        CsrfContext csrfContext = fetchCsrfContext();
        try {
            putValue(pid, id, value, csrfContext);
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == 404) {
                createValue(pid, id, value, csrfContext);
                return;
            }
            throw ex;
        }
    }

    private void putValue(String pid, String id, String value, CsrfContext csrfContext) {
        String uri = String.format("/api/v1/StringParameters(Pid='%s',Id='%s')", pid, id);
        log.debug("Updating StringParameter {} / {}", pid, id);
        restClient.put()
                .uri(uri)
                .header("X-CSRF-Token", csrfContext.token())
                .headers(headers -> applyCsrfCookie(headers, csrfContext))
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("Value", value))
                .retrieve()
                .toBodilessEntity();
    }

    private void createValue(String pid, String id, String value, CsrfContext csrfContext) {
        log.debug("Creating StringParameter {} / {}", pid, id);
        restClient.post()
                .uri("/api/v1/StringParameters")
                .header("X-CSRF-Token", csrfContext.token())
                .headers(headers -> applyCsrfCookie(headers, csrfContext))
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("Pid", pid, "Id", id, "Value", value))
                .retrieve()
                .toBodilessEntity();
    }

    private CsrfContext fetchCsrfContext() {
        return restClient.get()
                .uri("/api/v1/$metadata")
                .header("X-CSRF-Token", "Fetch")
                .exchange((request, response) -> {
                    String token = response.getHeaders().getFirst("X-CSRF-Token");
                    if (token == null || token.isBlank()) {
                        throw new IllegalStateException("CPI did not return X-CSRF-Token");
                    }
                    String cookie = response.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
                    response.close();
                    return new CsrfContext(token, cookie);
                });
    }

    private void applyCsrfCookie(HttpHeaders headers, CsrfContext csrfContext) {
        if (csrfContext.cookie() != null && !csrfContext.cookie().isBlank()) {
            headers.set(HttpHeaders.COOKIE, csrfContext.cookie());
        }
    }

    private record CsrfContext(String token, String cookie) {
    }
}
