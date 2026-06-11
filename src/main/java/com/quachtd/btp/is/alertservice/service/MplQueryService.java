package com.quachtd.btp.is.alertservice.service;

import com.quachtd.btp.is.alertservice.client.MessageProcessingLogClient;
import com.quachtd.btp.is.alertservice.config.CpiProperties;
import com.quachtd.btp.is.alertservice.model.MessageProcessingLog;
import com.quachtd.btp.is.alertservice.model.MplFetchResult;
import com.quachtd.btp.is.alertservice.model.odata.MessageProcessingLogResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.List;

@Service
public class MplQueryService {

    private static final Logger log = LoggerFactory.getLogger(MplQueryService.class);

    private final MessageProcessingLogClient messageProcessingLogClient;
    private final MplWatermarkService mplWatermarkService;
    private final CpiProperties properties;
    private final ResourceLoader resourceLoader;
    private final ObjectMapper objectMapper;

    public MplQueryService(
            MessageProcessingLogClient messageProcessingLogClient,
            MplWatermarkService mplWatermarkService,
            CpiProperties properties,
            ResourceLoader resourceLoader,
            ObjectMapper objectMapper) {
        this.messageProcessingLogClient = messageProcessingLogClient;
        this.mplWatermarkService = mplWatermarkService;
        this.properties = properties;
        this.resourceLoader = resourceLoader;
        this.objectMapper = objectMapper;
    }

    public MplFetchResult fetchMessages() {
        String sampleFile = properties.getMpl().getSampleFile();
        if (sampleFile != null && !sampleFile.isBlank()) {
            return MplFetchResult.sample(loadFromSampleFile(sampleFile));
        }

        Instant windowEnd = Instant.now();
        Instant windowStart = mplWatermarkService.loadWindowStart(windowEnd);
        log.info("Querying MPL from {} to {} (exclusive start, inclusive end)", windowStart, windowEnd);

        MessageProcessingLogResponse response =
                messageProcessingLogClient.fetchEscalatedLogs(windowStart, windowEnd);
        List<MessageProcessingLog> results = response.getResults();
        log.info("Fetched {} MPL messages for window {} to {}", results.size(), windowStart, windowEnd);
        return new MplFetchResult(results, windowStart, windowEnd, true);
    }

    private List<MessageProcessingLog> loadFromSampleFile(String sampleFile) {
        try {
            Resource resource = resourceLoader.getResource("file:" + sampleFile);
            if (!resource.exists()) {
                resource = resourceLoader.getResource("classpath:" + sampleFile);
            }
            try (InputStream inputStream = resource.getInputStream()) {
                MessageProcessingLogResponse response =
                        objectMapper.readValue(inputStream, MessageProcessingLogResponse.class);
                List<MessageProcessingLog> results = response.getResults();
                log.info("Loaded {} messages from sample file {}", results.size(), sampleFile);
                return results;
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to load sample MPL file: " + sampleFile, ex);
        }
    }
}
