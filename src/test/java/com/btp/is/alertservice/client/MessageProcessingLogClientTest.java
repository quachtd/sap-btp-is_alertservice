package com.btp.is.alertservice.client;

import com.btp.is.alertservice.config.CpiProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MessageProcessingLogClientTest {

    private MessageProcessingLogClient client;

    @BeforeEach
    void setUp() {
        CpiProperties properties = new CpiProperties();
        properties.getMpl().setFilter("Status eq 'ESCALATED'");
        properties.getMpl().setSelect(
                "IntegrationArtifact,Status,ApplicationMessageType,Sender,Receiver,"
                        + "IntegrationFlowName,CustomStatus,LogStart,LogEnd");
        client = new MessageProcessingLogClient(null, properties);
    }

    @Test
    void mplSelect_usesConfiguredSelectFields() {
        CpiProperties properties = new CpiProperties();

        assertEquals(
                "IntegrationArtifact,Status,ApplicationMessageType,Sender,Receiver,"
                        + "IntegrationFlowName,CustomStatus,LogStart,LogEnd",
                properties.getMpl().getSelect());
    }

    @Test
    void buildFilter_appendsLogEndWindowToConfiguredFilter() {
        Instant windowStart = Instant.parse("2026-06-08T17:00:00Z");
        Instant windowEnd = Instant.parse("2026-06-08T17:15:00Z");

        String filter = client.buildFilter(windowStart, windowEnd);

        assertEquals(
                "Status eq 'ESCALATED' and LogEnd gt datetime'2026-06-08T17:00:00' and LogEnd le datetime'2026-06-08T17:15:00'",
                filter);
    }
}
