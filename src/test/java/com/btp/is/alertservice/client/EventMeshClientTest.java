package com.btp.is.alertservice.client;

import com.btp.is.alertservice.config.EventMeshProperties;
import com.btp.is.alertservice.model.AlertEntry;
import com.btp.is.alertservice.model.Part2Result;
import com.btp.is.alertservice.model.PidInterfaceEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class EventMeshClientTest {

    private EventMeshProperties properties;
    private MockRestServiceServer mockServer;
    private EventMeshClient eventMeshClient;

    @BeforeEach
    void setUp() {
        properties = new EventMeshProperties();
        properties.setBaseUrl("https://event-mesh.test");
        properties.setTopic("alerts/topic");
        properties.setPublishPath("/messagingrest/v1/topics/{topic}/messages");
        properties.getOauth().setTokenUrl("https://auth.test/oauth/token");
        properties.getOauth().setClientId("client");
        properties.getOauth().setClientSecret("secret");

        RestClient.Builder builder = RestClient.builder().baseUrl(properties.getBaseUrl());
        mockServer = MockRestServiceServer.bindTo(builder).build();
        eventMeshClient = new EventMeshClient(builder.build(), properties);
    }

    @Test
    void publish_postsAllEntriesInSinglePart2ResultPayload() {
        Part2Result result = new Part2Result(List.of(
                AlertEntry.group("BTPSupport", List.of(
                        new PidInterfaceEntry("Pid1", "intf1"),
                        new PidInterfaceEntry("Pid2", "intf2")), 7),
                AlertEntry.single("EJS1", "EJS", 1)));

        mockServer.expect(requestTo("https://event-mesh.test/messagingrest/v1/topics/alerts%2Ftopic/messages"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("x-qos", "1"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("""
                        {
                          "entries": [
                            {"recipient":"BTPSupport","type":"GROUP","value":[{"pid":"Pid1","interface":"intf1"},{"pid":"Pid2","interface":"intf2"}],"errorNumber":7},
                            {"recipient":"EJS1","type":"SINGLE","value":"EJS","errorNumber":1}
                          ]
                        }
                        """))
                .andRespond(withSuccess());

        eventMeshClient.publish(result);

        mockServer.verify();
    }

    @Test
    void publish_skipsWhenNotConfigured() {
        properties.setTopic("");
        EventMeshClient unconfiguredClient = new EventMeshClient(
                RestClient.builder().baseUrl("https://event-mesh.test").build(),
                properties);

        unconfiguredClient.publish(new Part2Result(List.of(AlertEntry.single("EJS1", "EJS", 1))));

        mockServer.verify();
    }
}
