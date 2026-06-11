package com.quachtd.btp.is.alertservice.client;

import com.quachtd.btp.is.alertservice.config.EventMeshProperties;
import com.quachtd.btp.is.alertservice.model.Part2Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.URI;

@Component
public class EventMeshClient {

    private static final String QOS_HEADER = "x-qos";
    private static final String QOS_AT_LEAST_ONCE = "1";

    private static final Logger log = LoggerFactory.getLogger(EventMeshClient.class);

    private final RestClient restClient;
    private final EventMeshProperties properties;

    public EventMeshClient(RestClient eventMeshRestClient, EventMeshProperties properties) {
        this.restClient = eventMeshRestClient;
        this.properties = properties;
    }

    public void publish(Part2Result result) {
        if (result.getEntries().isEmpty()) {
            log.info("No alert entries to publish to Event Mesh");
            return;
        }

        if (!properties.isConfigured()) {
            log.warn("Event Mesh is not configured; skipping publish of {} entries", result.getEntries().size());
            return;
        }

        String path = buildPublishPath();
        log.debug("Event Mesh publish path: {}{}", properties.getBaseUrl(), path);
        publishBatch(path, result);
    }

    private String buildPublishPath() {
        String topic = properties.getTopic();
        if (topic.contains("/") && !topic.contains("%2F")) {
            topic = topic.replace("/", "%2F");
        }
        return properties.getPublishPath().replace("{topic}", topic);
    }

    private void publishBatch(String path, Part2Result result) {
        try {
            restClient.post()
                    .uri(URI.create(path))
                    .header(QOS_HEADER, QOS_AT_LEAST_ONCE)
                    .body(result)
                    .retrieve()
                    .toBodilessEntity();
            log.info("Published {} alert entries to Event Mesh in a single event: {}",
                    result.getEntries().size(), result.getEntries());
        } catch (RuntimeException ex) {
            log.error("Failed to publish alert batch to Event Mesh: {}", result.getEntries(), ex);
            if (properties.isFailOnError()) {
                throw ex;
            }
        }
    }
}
