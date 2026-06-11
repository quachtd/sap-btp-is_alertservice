package com.quachtd.btp.is.alertservice.service;

import com.quachtd.btp.is.alertservice.model.MessageProcessingLog;
import com.quachtd.btp.is.alertservice.model.Part1Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.DefaultResourceLoader;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecipientAggregationServiceTest {

    @Mock
    private PidLookupService pidLookupService;

    @Mock
    private ErrorEventResolver errorEventResolver;

    @Mock
    private RecipientResolver recipientResolver;

    private RecipientAggregationService recipientAggregationService;

    @BeforeEach
    void setUp() {
        recipientAggregationService = new RecipientAggregationService(
                pidLookupService, errorEventResolver, recipientResolver);
    }

    @Test
    void process_looksUpPidOncePerInterface() throws Exception {
        List<MessageProcessingLog> messages = loadSampleMessages();

        when(pidLookupService.findPid(any(MessageProcessingLog.class)))
                .thenReturn(Optional.of("Pid1"));
        when(errorEventResolver.resolve(any(MessageProcessingLog.class), eq("Pid1"), anyMap()))
                .thenReturn(Optional.of("EJS"));
        when(recipientResolver.resolve(eq("Pid1"), eq("EJS")))
                .thenReturn(List.of("EJS1"));

        recipientAggregationService.process(messages);

        verify(pidLookupService, times(2)).findPid(any(MessageProcessingLog.class));
    }

    @Test
    void process_buildsRecipientToErrorEventMapFromSampleLikeMessages() throws Exception {
        List<MessageProcessingLog> messages = loadSampleMessages();

        when(pidLookupService.findPid(any(MessageProcessingLog.class)))
                .thenReturn(Optional.of("Pid1"));
        when(errorEventResolver.resolve(any(MessageProcessingLog.class), eq("Pid1"), anyMap()))
                .thenAnswer(invocation -> {
                    MessageProcessingLog message = invocation.getArgument(0);
                    if ("EJS".equals(message.getReceiver())) {
                        return Optional.of("EJS");
                    }
                    if ("HUBXMAX".equals(message.getReceiver())) {
                        return Optional.of("HUBX");
                    }
                    if (message.getReceiver() != null && message.getReceiver().contains("WAVE")) {
                        return Optional.of("WAVE");
                    }
                    if ("MaxRetriesExceeded".equals(message.getCustomStatus())) {
                        return Optional.of("BANK");
                    }
                    return Optional.empty();
                });
        when(recipientResolver.resolve(eq("Pid1"), eq("EJS")))
                .thenReturn(List.of("EJS1", "BTPSupport"));
        when(recipientResolver.resolve(eq("Pid1"), eq("HUBX")))
                .thenReturn(List.of("HUBX1", "BTPSupport"));
        when(recipientResolver.resolve(eq("Pid1"), eq("WAVE")))
                .thenReturn(List.of("BTPSupport"));
        when(recipientResolver.resolve(eq("Pid1"), eq("BANK")))
                .thenReturn(List.of("BTPSupport"));

        Part1Result result = recipientAggregationService.process(messages);

        Map<String, Set<String>> recipientMap = result.getRecipientToErrorEvents();
        assertEquals(Set.of("EJS"), recipientMap.get("EJS1"));
        assertEquals(Set.of("HUBX"), recipientMap.get("HUBX1"));
        assertTrue(recipientMap.get("BTPSupport").containsAll(Set.of("EJS", "HUBX", "WAVE", "BANK")));

        assertEquals(
                Map.of(
                        "SAP+SAPToDownstream", "Pid1",
                        "SAP+Blackline_GLAccount", "Pid1"),
                result.getPidByInterface());

        Map<String, Map<String, Integer>> pidCounts = result.getPidToErrorEventCounts();
        assertEquals(2, pidCounts.get("Pid1").get("EJS"));
        assertEquals(1, pidCounts.get("Pid1").get("HUBX"));
        assertEquals(1, pidCounts.get("Pid1").get("WAVE"));
        assertEquals(2, pidCounts.get("Pid1").get("BANK"));
    }

    private List<MessageProcessingLog> loadSampleMessages() throws Exception {
        var resource = new DefaultResourceLoader().getResource("classpath:sample_input.json");
        try (var inputStream = resource.getInputStream()) {
            var response = new ObjectMapper().readValue(
                    inputStream,
                    com.quachtd.btp.is.alertservice.model.odata.MessageProcessingLogResponse.class);
            return response.getResults();
        }
    }
}
