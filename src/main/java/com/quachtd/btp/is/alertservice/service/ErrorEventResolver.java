package com.quachtd.btp.is.alertservice.service;

import com.quachtd.btp.is.alertservice.client.StringParameterClient;
import com.quachtd.btp.is.alertservice.config.CpiProperties;
import com.quachtd.btp.is.alertservice.model.MessageProcessingLog;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class ErrorEventResolver {

    private final StringParameterClient stringParameterClient;
    private final CpiProperties properties;
    private final String ALERT_ERROR_EVENT_PREFIX = "AlertErrorEvent_";

    public ErrorEventResolver(StringParameterClient stringParameterClient, CpiProperties properties) {
        this.stringParameterClient = stringParameterClient;
        this.properties = properties;
    }

    public Optional<String> resolve(MessageProcessingLog message, String pid) {
        return resolve(message, pid, new LinkedHashMap<>());
    }

    public Optional<String> resolve(
            MessageProcessingLog message,
            String pid,
            Map<String, Optional<String>> ruleValueByPidAndFlow) {
        Optional<String> partnerMatch = matchFromParameter(message, pid, ruleValueByPidAndFlow);
        if (partnerMatch.isPresent()) {
            return partnerMatch;
        }
        // don't use global pid for now
        //return matchFromParameter(message, properties.getGlobalPid(), ruleValueByPidAndFlow);
        return Optional.empty();
    }

    static String ruleValueCacheKey(String pid, String integrationFlowName) {
        return pid + "+" + integrationFlowName;
    }

    private Optional<String> matchFromParameter(
            MessageProcessingLog message,
            String pid,
            Map<String, Optional<String>> ruleValueByPidAndFlow) {
        String integrationFlowName = message.getIntegrationFlowName();
        if (integrationFlowName == null || integrationFlowName.isBlank()) {
            return Optional.empty();
        }

        String cacheKey = ruleValueCacheKey(pid, integrationFlowName);
        Optional<String> ruleValue = ruleValueByPidAndFlow.computeIfAbsent(
                cacheKey,
                key -> stringParameterClient.getValue(pid, ALERT_ERROR_EVENT_PREFIX + integrationFlowName));

        return ruleValue.flatMap(value -> matchRule(message, value));
    }

    Optional<String> matchRule(MessageProcessingLog message, String ruleValue) {
        int separatorIndex = ruleValue.indexOf('-');
        if (separatorIndex <= 0 || separatorIndex == ruleValue.length() - 1) {
            return Optional.empty();
        }

        String fieldName = ruleValue.substring(0, separatorIndex);
        String configuredValue = ruleValue.substring(separatorIndex + 1);
        String fieldValue = message.getFieldValue(fieldName);

        if (fieldValue == null) {
            return Optional.empty();
        }

        String normalizedFieldValue = normalizeFieldValue(fieldValue);
        if (normalizedFieldValue.contains(configuredValue)) {
            return Optional.of(configuredValue);
        }
        return Optional.empty();
    }

    private String normalizeFieldValue(String fieldValue) {
        String trimmed = fieldValue.trim();
        if (trimmed.length() >= 2 && trimmed.startsWith("\"") && trimmed.endsWith("\"")) {
            return trimmed.substring(1, trimmed.length() - 1);
        }
        return trimmed;
    }
}
