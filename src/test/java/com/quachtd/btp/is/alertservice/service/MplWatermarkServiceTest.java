package com.quachtd.btp.is.alertservice.service;

import com.quachtd.btp.is.alertservice.client.StringParameterClient;
import com.quachtd.btp.is.alertservice.config.CpiProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MplWatermarkServiceTest {

    @Mock
    private StringParameterClient stringParameterClient;

    private MplWatermarkService mplWatermarkService;

    @BeforeEach
    void setUp() {
        CpiProperties properties = new CpiProperties();
        properties.setGlobalPid("Global_Alert");
        properties.getMpl().setLookbackMinutes(15);
        properties.getMpl().setWatermarkParameterId("AlertWatermark");
        mplWatermarkService = new MplWatermarkService(stringParameterClient, properties);
    }

    @Test
    void loadWindowStart_returnsStoredWatermark() {
        Instant windowEnd = Instant.parse("2026-06-08T18:00:00Z");
        when(stringParameterClient.getValue("Global_Alert", "AlertWatermark"))
                .thenReturn(Optional.of("2026-06-08T17:45:00Z"));

        Instant windowStart = mplWatermarkService.loadWindowStart(windowEnd);

        assertEquals(Instant.parse("2026-06-08T17:45:00Z"), windowStart);
    }

    @Test
    void loadWindowStart_usesLookbackWhenWatermarkMissing() {
        Instant windowEnd = Instant.parse("2026-06-08T18:00:00Z");
        when(stringParameterClient.getValue("Global_Alert", "AlertWatermark"))
                .thenReturn(Optional.empty());

        Instant windowStart = mplWatermarkService.loadWindowStart(windowEnd);

        assertEquals(Instant.parse("2026-06-08T17:45:00Z"), windowStart);
    }

    @Test
    void save_persistsWatermarkToPartnerDirectory() {
        Instant watermark = Instant.parse("2026-06-08T18:00:00Z");

        mplWatermarkService.save(watermark);

        verify(stringParameterClient).setValue("Global_Alert", "AlertWatermark", "2026-06-08T18:00:00Z");
    }
}
