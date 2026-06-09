package com.btp.is.alertservice.service;

import com.btp.is.alertservice.client.StringParameterClient;
import com.btp.is.alertservice.config.CpiProperties;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ConsolidationResolver {

    private final StringParameterClient stringParameterClient;
    private final CpiProperties properties;

    public ConsolidationResolver(StringParameterClient stringParameterClient, CpiProperties properties) {
        this.stringParameterClient = stringParameterClient;
        this.properties = properties;
    }

    public Optional<String> resolveResourceName(String recipient) {
        String consolidationKey = "AlertConsolidation_" + recipient;
        return stringParameterClient.getValue(properties.getGlobalPid(), consolidationKey)
                .map(String::trim)
                .filter(value -> !value.isEmpty());
    }
}
