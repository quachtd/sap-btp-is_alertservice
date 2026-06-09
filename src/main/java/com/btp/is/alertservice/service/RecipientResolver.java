package com.btp.is.alertservice.service;

import com.btp.is.alertservice.client.StringParameterClient;
import com.btp.is.alertservice.config.CpiProperties;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class RecipientResolver {

    private static final String DEFAULT_RECIPIENT_KEY = "AlertRecipient_default";

    private final StringParameterClient stringParameterClient;
    private final CpiProperties properties;

    public RecipientResolver(StringParameterClient stringParameterClient, CpiProperties properties) {
        this.stringParameterClient = stringParameterClient;
        this.properties = properties;
    }

    public List<String> resolve(String pid, String errorEvent) {
        String recipientKey = "AlertRecipient_" + errorEvent;
        return stringParameterClient.getValue(pid, recipientKey)
                .map(this::parseRecipients)
                .filter(recipients -> !recipients.isEmpty())
                .orElseGet(() -> stringParameterClient.getValue(properties.getGlobalPid(), DEFAULT_RECIPIENT_KEY)
                        .map(this::parseRecipients)
                        .orElse(List.of()));
    }

    List<String> parseRecipients(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        Set<String> recipients = new LinkedHashSet<>();
        Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(recipient -> !recipient.isEmpty())
                .forEach(recipients::add);
        return List.copyOf(recipients);
    }
}
