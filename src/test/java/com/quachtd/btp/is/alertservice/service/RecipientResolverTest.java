package com.quachtd.btp.is.alertservice.service;

import com.quachtd.btp.is.alertservice.client.StringParameterClient;
import com.quachtd.btp.is.alertservice.config.CpiProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecipientResolverTest {

    @Mock
    private StringParameterClient stringParameterClient;

    private RecipientResolver recipientResolver;

    @BeforeEach
    void setUp() {
        CpiProperties properties = new CpiProperties();
        properties.setGlobalPid("Global_Alert");
        recipientResolver = new RecipientResolver(stringParameterClient, properties);
    }

    @Test
    void parseRecipients_splitsCommaSeparatedValues() {
        assertEquals(List.of("EJS1", "BTPSupport"), recipientResolver.parseRecipients("EJS1, BTPSupport"));
    }

    @Test
    void resolve_usesPidSpecificRecipients() {
        when(stringParameterClient.getValue(eq("Pid1"), eq("AlertRecipient_EJS")))
                .thenReturn(Optional.of("EJS1, BTPSupport"));

        assertEquals(List.of("EJS1", "BTPSupport"), recipientResolver.resolve("Pid1", "EJS"));
    }

    @Test
    void resolve_fallsBackToGlobalDefaultRecipients() {
        when(stringParameterClient.getValue(eq("Pid1"), eq("AlertRecipient_BANK")))
                .thenReturn(Optional.empty());
        when(stringParameterClient.getValue(eq("Global_Alert"), eq("AlertRecipient_default")))
                .thenReturn(Optional.of("BTPSupport"));

        assertEquals(List.of("BTPSupport"), recipientResolver.resolve("Pid1", "BANK"));
    }
}
