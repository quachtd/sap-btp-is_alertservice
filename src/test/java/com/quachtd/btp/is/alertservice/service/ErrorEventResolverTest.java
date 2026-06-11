package com.quachtd.btp.is.alertservice.service;

import com.quachtd.btp.is.alertservice.client.StringParameterClient;
import com.quachtd.btp.is.alertservice.config.CpiProperties;
import com.quachtd.btp.is.alertservice.model.MessageProcessingLog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ErrorEventResolverTest {

    @Mock
    private StringParameterClient stringParameterClient;

    private ErrorEventResolver errorEventResolver;

    @BeforeEach
    void setUp() {
        CpiProperties properties = new CpiProperties();
        properties.setGlobalPid("Global_Alert");
        errorEventResolver = new ErrorEventResolver(stringParameterClient, properties);
    }

    @Test
    void matchRule_matchesReceiverContainsConfiguredValue() {
        MessageProcessingLog message = new MessageProcessingLog();
        message.setReceiver("BC_WAVE_120");

        Optional<String> result = errorEventResolver.matchRule(message, "Receiver-WAVE");

        assertEquals(Optional.of("WAVE"), result);
    }

    @Test
    void matchRule_matchesCustomStatus() {
        MessageProcessingLog message = new MessageProcessingLog();
        message.setCustomStatus("MaxRetriesExceeded");

        Optional<String> result = errorEventResolver.matchRule(message, "CustomStatus-MaxRetriesExceeded");

        assertEquals(Optional.of("MaxRetriesExceeded"), result);
    }

    @Test
    void matchRule_stripsQuotedCustomStatus() {
        MessageProcessingLog message = new MessageProcessingLog();
        message.setCustomStatus("\"CustomError\"");

        Optional<String> result = errorEventResolver.matchRule(message, "CustomStatus-CustomError");

        assertEquals(Optional.of("CustomError"), result);
    }

    @Test
    void resolve_usesPartnerPidBeforeGlobalFallback() {
        MessageProcessingLog message = new MessageProcessingLog();
        message.setIntegrationFlowName("My_iFLOWNAME");
        message.setReceiver("BC_WAVE_120");

        when(stringParameterClient.getValue(eq("Pid1"), eq("AlertErrorEvent_My_iFLOWNAME")))
                .thenReturn(Optional.of("Receiver-WAVE"));

        Optional<String> result = errorEventResolver.resolve(message, "Pid1");

        assertEquals(Optional.of("WAVE"), result);
    }

    @Test
    void resolve_looksUpRuleValueOncePerPidAndIntegrationFlow() {
        MessageProcessingLog message1 = new MessageProcessingLog();
        message1.setIntegrationFlowName("My_iFLOWNAME");
        message1.setReceiver("BC_WAVE_120");

        MessageProcessingLog message2 = new MessageProcessingLog();
        message2.setIntegrationFlowName("My_iFLOWNAME");
        message2.setReceiver("BC_WAVE_999");

        when(stringParameterClient.getValue(eq("Pid1"), eq("AlertErrorEvent_My_iFLOWNAME")))
                .thenReturn(Optional.of("Receiver-WAVE"));

        Map<String, Optional<String>> ruleValueCache = new LinkedHashMap<>();
        errorEventResolver.resolve(message1, "Pid1", ruleValueCache);
        errorEventResolver.resolve(message2, "Pid1", ruleValueCache);

        verify(stringParameterClient, times(1))
                .getValue(eq("Pid1"), eq("AlertErrorEvent_My_iFLOWNAME"));
    }

    @Test
    void resolve_returnsEmptyWhenPartnerPidHasNoMatch() {
        MessageProcessingLog message = new MessageProcessingLog();
        message.setIntegrationFlowName("com.sap.integration.cloud.pipeline.v2.generic.step06.outbound.processing");
        message.setCustomStatus("MaxRetriesExceeded");

        when(stringParameterClient.getValue(eq("Pid1"),
                eq("AlertErrorEvent_com.sap.integration.cloud.pipeline.v2.generic.step06.outbound.processing")))
                .thenReturn(Optional.empty());

        Optional<String> result = errorEventResolver.resolve(message, "Pid1");

        assertTrue(result.isEmpty());
    }

    @Test
    void matchRule_returnsEmptyWhenFieldMissing() {
        MessageProcessingLog message = new MessageProcessingLog();

        assertTrue(errorEventResolver.matchRule(message, "Receiver-WAVE").isEmpty());
    }
}
