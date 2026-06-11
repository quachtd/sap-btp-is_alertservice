package com.quachtd.btp.is.alertservice.model;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class Part1Result {

    private final Map<String, Set<String>> recipientToErrorEvents;
    private final Map<String, Map<String, Integer>> pidToErrorEventCounts;
    private final Map<String, String> pidByInterface;

    public Part1Result(
            Map<String, Set<String>> recipientToErrorEvents,
            Map<String, Map<String, Integer>> pidToErrorEventCounts) {
        this(recipientToErrorEvents, pidToErrorEventCounts, Collections.emptyMap());
    }

    public Part1Result(
            Map<String, Set<String>> recipientToErrorEvents,
            Map<String, Map<String, Integer>> pidToErrorEventCounts,
            Map<String, String> pidByInterface) {
        this.recipientToErrorEvents = recipientToErrorEvents;
        this.pidToErrorEventCounts = pidToErrorEventCounts;
        this.pidByInterface = pidByInterface;
    }

    public Map<String, Set<String>> getRecipientToErrorEvents() {
        return recipientToErrorEvents;
    }

    public Map<String, Map<String, Integer>> getPidToErrorEventCounts() {
        return pidToErrorEventCounts;
    }

    public Map<String, String> getPidByInterface() {
        return pidByInterface;
    }

    public static Part1Result empty() {
        return new Part1Result(Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap());
    }

    public static Map<String, Map<String, Integer>> copyCounts(Map<String, Map<String, Integer>> source) {
        Map<String, Map<String, Integer>> copy = new LinkedHashMap<>();
        source.forEach((pid, counts) -> copy.put(pid, new LinkedHashMap<>(counts)));
        return Collections.unmodifiableMap(copy);
    }

    public static Map<String, String> copyPidByInterface(Map<String, String> source) {
        return Collections.unmodifiableMap(new LinkedHashMap<>(source));
    }
}
