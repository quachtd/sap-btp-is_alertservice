package com.quachtd.btp.is.alertservice.service;

import com.quachtd.btp.is.alertservice.client.StringParameterClient;
import com.quachtd.btp.is.alertservice.config.CpiProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
public class MplWatermarkService {

    private static final Logger log = LoggerFactory.getLogger(MplWatermarkService.class);

    private final StringParameterClient stringParameterClient;
    private final CpiProperties properties;

    public MplWatermarkService(StringParameterClient stringParameterClient, CpiProperties properties) {
        this.stringParameterClient = stringParameterClient;
        this.properties = properties;
    }

    public Instant loadWindowStart(Instant windowEnd) {
        return stringParameterClient
                .getValue(properties.getGlobalPid(), properties.getMpl().getWatermarkParameterId())
                .map(this::parseWatermark)
                .orElseGet(() -> fallbackWindowStart(windowEnd));
    }

    public void save(Instant watermark) {
        String value = watermark.toString();
        stringParameterClient.setValue(
                properties.getGlobalPid(),
                properties.getMpl().getWatermarkParameterId(),
                value);
        log.info("Saved MPL watermark {}={}", properties.getMpl().getWatermarkParameterId(), value);
    }

    private Instant parseWatermark(String value) {
        try {
            return Instant.parse(value.trim());
        } catch (RuntimeException ex) {
            throw new IllegalStateException(
                    "Invalid MPL watermark value in Partner Directory: " + value, ex);
        }
    }

    private Instant fallbackWindowStart(Instant windowEnd) {
        Instant fallback = windowEnd.minus(Duration.ofMinutes(properties.getMpl().getLookbackMinutes()));
        log.info("No MPL watermark found; using {} minute lookback from {}",
                properties.getMpl().getLookbackMinutes(), windowEnd);
        return fallback;
    }
}
