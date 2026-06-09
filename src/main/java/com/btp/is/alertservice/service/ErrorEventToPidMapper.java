package com.btp.is.alertservice.service;

import org.springframework.stereotype.Component;

import com.btp.is.alertservice.model.PidInterfaceEntry;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class ErrorEventToPidMapper {

    public Map<String, String> buildErrorEventToPidMap(Map<String, Map<String, Integer>> pidToErrorEventCounts) {
        Map<String, String> errorEventToPid = new LinkedHashMap<>();
        for (Map.Entry<String, Map<String, Integer>> pidEntry : pidToErrorEventCounts.entrySet()) {
            String pid = pidEntry.getKey();
            for (String errorEvent : pidEntry.getValue().keySet()) {
                errorEventToPid.put(errorEvent, pid);
            }
        }
        return errorEventToPid;
    }

    public List<PidInterfaceEntry> toUniquePids(
            Set<String> errorEvents,
            Map<String, String> errorEventToPid,
            Map<String, String> pidByInterface) {
        Map<String, String> interfaceToPid = pidByInterface != null ? pidByInterface : Map.of();
        Map<String, String> pidToInterface = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : interfaceToPid.entrySet()) {
            pidToInterface.putIfAbsent(entry.getValue(), entry.getKey());
        }

        List<PidInterfaceEntry> uniquePids = new ArrayList<>();
        Set<String> seenPids = new LinkedHashSet<>();
        for (String errorEvent : errorEvents) {
            String pid = errorEventToPid.get(errorEvent);
            if (pid == null || !seenPids.add(pid)) {
                continue;
            }
            String interfaceKey = pidToInterface.get(pid);
            if (interfaceKey != null) {
                uniquePids.add(new PidInterfaceEntry(pid, interfaceKey));
            }
        }
        return uniquePids;
    }

    public int countErrorMessages(
            Set<String> errorEvents,
            Map<String, String> errorEventToPid,
            Map<String, Map<String, Integer>> pidToErrorEventCounts) {
        int total = 0;
        for (String errorEvent : errorEvents) {
            String pid = errorEventToPid.get(errorEvent);
            if (pid == null) {
                continue;
            }
            Map<String, Integer> counts = pidToErrorEventCounts.get(pid);
            if (counts != null && counts.containsKey(errorEvent)) {
                total += counts.get(errorEvent);
            }
        }
        return total;
    }
}
