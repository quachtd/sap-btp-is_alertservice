package com.btp.is.alertservice.service;

import com.btp.is.alertservice.model.AlertEntry;
import com.btp.is.alertservice.model.Part1Result;
import com.btp.is.alertservice.model.Part2Result;
import com.btp.is.alertservice.model.PidInterfaceEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class Part2TransformationService {

    private static final Logger log = LoggerFactory.getLogger(Part2TransformationService.class);

    private final ErrorEventToPidMapper errorEventToPidMapper;
    private final ConsolidationResolver consolidationResolver;

    public Part2TransformationService(
            ErrorEventToPidMapper errorEventToPidMapper,
            ConsolidationResolver consolidationResolver) {
        this.errorEventToPidMapper = errorEventToPidMapper;
        this.consolidationResolver = consolidationResolver;
    }

    public Part2Result transform(Part1Result part1Result) {
        Map<String, Map<String, Integer>> pidToErrorEventCounts = part1Result.getPidToErrorEventCounts();
        Map<String, String> errorEventToPid =
                errorEventToPidMapper.buildErrorEventToPidMap(pidToErrorEventCounts);
        List<AlertEntry> entries = new ArrayList<>();

        Map<String, String> pidByInterface = part1Result.getPidByInterface();
        for (Map.Entry<String, Set<String>> recipientEntry : part1Result.getRecipientToErrorEvents().entrySet()) {
            String recipient = recipientEntry.getKey();
            Set<String> errorEvents = recipientEntry.getValue();
            entries.add(transformRecipient(
                    recipient, errorEvents, errorEventToPid, pidToErrorEventCounts, pidByInterface));
        }

        log.info("Part 2 result: {} alert entries", entries.size());
        entries.forEach(entry -> log.info("  {}", entry));

        return new Part2Result(entries);
    }

    private AlertEntry transformRecipient(
            String recipient,
            Set<String> errorEvents,
            Map<String, String> errorEventToPid,
            Map<String, Map<String, Integer>> pidToErrorEventCounts,
            Map<String, String> pidByInterface) {
        int errorNumber = errorEventToPidMapper.countErrorMessages(
                errorEvents, errorEventToPid, pidToErrorEventCounts);

        if (errorEvents.size() > 1) {
            List<PidInterfaceEntry> pids = errorEventToPidMapper.toUniquePids(
                    errorEvents, errorEventToPid, pidByInterface);
            return AlertEntry.group(recipient, pids, errorNumber);
        }

        String errorEvent = errorEvents.iterator().next();
        return consolidationResolver.resolveResourceName(recipient)
                .map(resourceName -> {
                    List<PidInterfaceEntry> pids = errorEventToPidMapper.toUniquePids(
                            Set.of(errorEvent), errorEventToPid, pidByInterface);
                    return AlertEntry.group(resourceName, pids, errorNumber);
                })
                .orElseGet(() -> AlertEntry.single(recipient, errorEvent, errorNumber));
    }
}
