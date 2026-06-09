package com.btp.is.alertservice.client;

import com.btp.is.alertservice.config.CpiProperties;
import com.btp.is.alertservice.model.odata.MessageProcessingLogResponse;
import com.btp.is.alertservice.util.ODataDateTimeFormatter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class MessageProcessingLogClient {

    private final RestClient restClient;
    private final CpiProperties properties;
    private static final Logger log = LoggerFactory.getLogger(MessageProcessingLogClient.class);

    public MessageProcessingLogClient(RestClient cpiRestClient, CpiProperties properties) {
        this.restClient = cpiRestClient;
        this.properties = properties;
    }

    public MessageProcessingLogResponse fetchEscalatedLogs(Instant windowStart, Instant windowEnd) {
        String filter = buildFilter(windowStart, windowEnd);
        String uri = UriComponentsBuilder.fromPath("/api/v1/MessageProcessingLogs")
                .queryParam("$select", properties.getMpl().getSelect())
                .queryParam("$filter", filter)
                .queryParam("$orderby", "LogEnd asc")
                .build()
                .toUriString();

        log.debug("mpl query uri: {}", uri);

        return restClient.get()
                .uri(uri)
                .retrieve()
                .body(MessageProcessingLogResponse.class);
    }

    String buildFilter(Instant windowStart, Instant windowEnd) {
        return properties.getMpl().getFilter()
                + " and LogEnd gt datetime'"
                + ODataDateTimeFormatter.format(windowStart)
                + "' and LogEnd le datetime'"
                + ODataDateTimeFormatter.format(windowEnd)
                + "'";
    }
}
