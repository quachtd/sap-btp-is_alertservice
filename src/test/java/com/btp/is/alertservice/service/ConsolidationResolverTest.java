package com.btp.is.alertservice.service;

import com.btp.is.alertservice.client.StringParameterClient;
import com.btp.is.alertservice.config.CpiProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsolidationResolverTest {

    @Mock
    private StringParameterClient stringParameterClient;

    private ConsolidationResolver consolidationResolver;

    @BeforeEach
    void setUp() {
        CpiProperties properties = new CpiProperties();
        properties.setGlobalPid("Global_Alert");
        consolidationResolver = new ConsolidationResolver(stringParameterClient, properties);
    }

    @Test
    void resolveResourceName_returnsValueFromGlobalAlertConsolidationKey() {
        when(stringParameterClient.getValue(eq("Global_Alert"), eq("AlertConsolidation_BTPSupport")))
                .thenReturn(Optional.of("BTPSupportGroup"));

        Optional<String> result = consolidationResolver.resolveResourceName("BTPSupport");

        assertEquals(Optional.of("BTPSupportGroup"), result);
    }

    @Test
    void resolveResourceName_returnsEmptyWhenNotConfigured() {
        when(stringParameterClient.getValue(eq("Global_Alert"), eq("AlertConsolidation_EJS1")))
                .thenReturn(Optional.empty());

        assertTrue(consolidationResolver.resolveResourceName("EJS1").isEmpty());
    }
}
