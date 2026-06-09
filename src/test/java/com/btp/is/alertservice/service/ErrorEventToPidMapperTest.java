package com.btp.is.alertservice.service;

import com.btp.is.alertservice.model.PidInterfaceEntry;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ErrorEventToPidMapperTest {

    private final ErrorEventToPidMapper mapper = new ErrorEventToPidMapper();

    @Test
    void buildErrorEventToPidMap_invertsPidToErrorEventCounts() {
        Map<String, Map<String, Integer>> pidToErrorEventCounts = Map.of(
                "Pid1", new LinkedHashMap<>(Map.of("EJS", 2, "HUBX", 1)),
                "Pid2", new LinkedHashMap<>(Map.of("ADP", 3)),
                "Pid3", new LinkedHashMap<>(Map.of("BANK", 1)));

        Map<String, String> result = mapper.buildErrorEventToPidMap(pidToErrorEventCounts);

        assertEquals("Pid1", result.get("EJS"));
        assertEquals("Pid1", result.get("HUBX"));
        assertEquals("Pid2", result.get("ADP"));
        assertEquals("Pid3", result.get("BANK"));
    }

    @Test
    void toUniquePids_deduplicatesPidsAndMapsToInterface() {
        Map<String, String> errorEventToPid = Map.of(
                "EJS", "Pid1",
                "HUBX", "Pid1",
                "ADP", "Pid2",
                "BANK", "Pid3");
        Map<String, String> pidByInterface = new LinkedHashMap<>(Map.of(
                "SAPToDownstream", "Pid1",
                "ADPInterface", "Pid2",
                "BankInterface", "Pid3"));

        Set<String> errorEvents = new LinkedHashSet<>(List.of("EJS", "HUBX", "ADP", "BANK"));
        List<PidInterfaceEntry> pids = mapper.toUniquePids(errorEvents, errorEventToPid, pidByInterface);

        assertEquals(List.of(
                new PidInterfaceEntry("Pid1", "SAPToDownstream"),
                new PidInterfaceEntry("Pid2", "ADPInterface"),
                new PidInterfaceEntry("Pid3", "BankInterface")), pids);
    }

    @Test
    void countErrorMessages_sumsCountsForRecipientErrorEvents() {
        Map<String, Map<String, Integer>> pidToErrorEventCounts = Map.of(
                "Pid1", new LinkedHashMap<>(Map.of("EJS", 2, "HUBX", 1)),
                "Pid2", new LinkedHashMap<>(Map.of("ADP", 3)),
                "Pid3", new LinkedHashMap<>(Map.of("BANK", 1)));
        Map<String, String> errorEventToPid = mapper.buildErrorEventToPidMap(pidToErrorEventCounts);

        int total = mapper.countErrorMessages(
                Set.of("EJS", "HUBX", "ADP", "BANK"), errorEventToPid, pidToErrorEventCounts);

        assertEquals(7, total);
    }

    @Test
    void countErrorMessages_returnsSingleErrorEventCount() {
        Map<String, Map<String, Integer>> pidToErrorEventCounts =
                Map.of("Pid1", new LinkedHashMap<>(Map.of("EJS", 2)));
        Map<String, String> errorEventToPid = mapper.buildErrorEventToPidMap(pidToErrorEventCounts);

        int total = mapper.countErrorMessages(Set.of("EJS"), errorEventToPid, pidToErrorEventCounts);

        assertEquals(2, total);
    }
}
