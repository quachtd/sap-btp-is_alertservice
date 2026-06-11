package com.quachtd.btp.is.alertservice.service;

import com.quachtd.btp.is.alertservice.client.MessageProcessingLogClient;
import com.quachtd.btp.is.alertservice.config.CpiProperties;
import com.quachtd.btp.is.alertservice.model.MessageProcessingLog;
import com.quachtd.btp.is.alertservice.model.MplFetchResult;
import com.quachtd.btp.is.alertservice.model.odata.MessageProcessingLogResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.DefaultResourceLoader;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MplQueryServiceTest {

    @Mock
    private MessageProcessingLogClient messageProcessingLogClient;

    @Mock
    private MplWatermarkService mplWatermarkService;

    private MplQueryService mplQueryService;

    @BeforeEach
    void setUp() {
        CpiProperties properties = new CpiProperties();
        mplQueryService = new MplQueryService(
                messageProcessingLogClient,
                mplWatermarkService,
                properties,
                new DefaultResourceLoader(),
                new ObjectMapper());
    }

    @Test
    void fetchMessages_loadsSampleFileFromClasspathWithoutWatermark() {
        CpiProperties properties = new CpiProperties();
        properties.getMpl().setSampleFile("sample_input.json");
        MplQueryService sampleService = new MplQueryService(
                messageProcessingLogClient,
                mplWatermarkService,
                properties,
                new DefaultResourceLoader(),
                new ObjectMapper());

        MplFetchResult result = sampleService.fetchMessages();

        assertFalse(result.isWatermarkEligible());
        assertEquals(6, result.getMessages().size());
    }

    @Test
    void fetchMessages_queriesMplUsingWatermarkWindow() throws Exception {
        Instant windowStart = Instant.parse("2026-06-08T17:45:00Z");
        MessageProcessingLogResponse response = new ObjectMapper().readValue("""
                {"d":{"results":[{"Status":"ESCALATED"}]}}
                """, MessageProcessingLogResponse.class);

        when(mplWatermarkService.loadWindowStart(any(Instant.class))).thenReturn(windowStart);
        when(messageProcessingLogClient.fetchEscalatedLogs(any(Instant.class), any(Instant.class)))
                .thenReturn(response);

        MplFetchResult result = mplQueryService.fetchMessages();

        assertTrue(result.isWatermarkEligible());
        assertEquals(1, result.getMessages().size());

        ArgumentCaptor<Instant> startCaptor = ArgumentCaptor.forClass(Instant.class);
        ArgumentCaptor<Instant> endCaptor = ArgumentCaptor.forClass(Instant.class);
        verify(messageProcessingLogClient).fetchEscalatedLogs(startCaptor.capture(), endCaptor.capture());
        assertEquals(windowStart, startCaptor.getValue());
    }
}
