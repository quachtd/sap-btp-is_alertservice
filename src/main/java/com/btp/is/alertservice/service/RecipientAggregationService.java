package com.btp.is.alertservice.service;

import com.btp.is.alertservice.model.ErrorEventMatch;
import com.btp.is.alertservice.model.MessageProcessingLog;
import com.btp.is.alertservice.model.Part1Result;
import com.btp.is.alertservice.model.RecipientAlertGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
public class RecipientAggregationService {

    private static final Logger log = LoggerFactory.getLogger(RecipientAggregationService.class);

    private final PidLookupService pidLookupService;
    private final ErrorEventResolver errorEventResolver;
    private final RecipientResolver recipientResolver;

    public RecipientAggregationService(
            PidLookupService pidLookupService,
            ErrorEventResolver errorEventResolver,
            RecipientResolver recipientResolver) {
        this.pidLookupService = pidLookupService;
        this.errorEventResolver = errorEventResolver;
        this.recipientResolver = recipientResolver;
    }

    public Part1Result process(List<MessageProcessingLog> messages) {
        Map<String, List<MessageProcessingLog>> groupedByInterface = groupByInterface(messages);
        log.info("Grouped {} messages into {} interfaces", messages.size(), groupedByInterface.size());

        List<ErrorEventMatch> matches = new ArrayList<>();
        Map<String, Optional<String>> pidByInterface = new LinkedHashMap<>();
        Map<String, Optional<String>> ruleValueByPidAndFlow = new LinkedHashMap<>();
        for (MessageProcessingLog message : messages) {
            Optional<String> pid = pidByInterface.computeIfAbsent(
                    message.interfaceKey(),
                    key -> pidLookupService.findPid(message));
            if (pid.isEmpty()) {
                log.warn("Skipping message with unknown Pid for interface {}", message.interfaceKey());
                continue;
            }

            var errorEvent = errorEventResolver.resolve(message, pid.get(), ruleValueByPidAndFlow);
            if (errorEvent.isEmpty()) {
                log.warn("Skipping message with unresolved ErrorEvent for Pid {}", pid.get());
                continue;
            }

            matches.add(new ErrorEventMatch(message, pid.get(), errorEvent.get()));
        }

        Map<String, Map<String, Integer>> pidToErrorEventCounts = collectErrorEventCounts(matches);
        RecipientAlertGroup recipientAlertGroup = buildRecipientGroups(pidToErrorEventCounts);

        Map<String, Set<String>> recipientToErrorEvents = recipientAlertGroup.getRecipientToErrorEvents();
        log.info("Part 1 result: {} recipients mapped to ErrorEvents", recipientToErrorEvents.size());
        recipientToErrorEvents.forEach((recipient, events) ->
                log.info("  {} -> {}", recipient, events));

        Map<String, String> resolvedPidByInterface = new LinkedHashMap<>();
        pidByInterface.forEach((interfaceKey, pid) ->
                pid.ifPresent(value -> resolvedPidByInterface.put(interfaceKey, value)));

        return new Part1Result(
                recipientToErrorEvents,
                Part1Result.copyCounts(pidToErrorEventCounts),
                Part1Result.copyPidByInterface(resolvedPidByInterface));
    }

    private Map<String, List<MessageProcessingLog>> groupByInterface(List<MessageProcessingLog> messages) {
        Map<String, List<MessageProcessingLog>> grouped = new LinkedHashMap<>();
        for (MessageProcessingLog message : messages) {
            grouped.computeIfAbsent(message.interfaceKey(), key -> new ArrayList<>()).add(message);
        }
        return grouped;
    }

    private Map<String, Map<String, Integer>> collectErrorEventCounts(List<ErrorEventMatch> matches) {
        Map<String, Map<String, Integer>> pidToErrorEventCounts = new LinkedHashMap<>();
        for (ErrorEventMatch match : matches) {
            pidToErrorEventCounts
                    .computeIfAbsent(match.getPid(), key -> new LinkedHashMap<>())
                    .merge(match.getErrorEvent(), 1, Integer::sum);
        }
        return pidToErrorEventCounts;
    }

    private RecipientAlertGroup buildRecipientGroups(Map<String, Map<String, Integer>> pidToErrorEventCounts) {
        RecipientAlertGroup recipientAlertGroup = new RecipientAlertGroup();
        for (Map.Entry<String, Map<String, Integer>> pidEntry : pidToErrorEventCounts.entrySet()) {
            String pid = pidEntry.getKey();
            Set<String> uniqueErrorEvents = new LinkedHashSet<>(pidEntry.getValue().keySet());
            for (String errorEvent : uniqueErrorEvents) {
                List<String> recipients = recipientResolver.resolve(pid, errorEvent);
                for (String recipient : recipients) {
                    recipientAlertGroup.add(recipient, errorEvent);
                }
            }
        }
        return recipientAlertGroup;
    }
}
