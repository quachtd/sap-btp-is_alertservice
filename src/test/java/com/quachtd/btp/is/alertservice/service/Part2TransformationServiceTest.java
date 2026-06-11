package com.quachtd.btp.is.alertservice.service;

import com.quachtd.btp.is.alertservice.model.AlertEntry;
import com.quachtd.btp.is.alertservice.model.AlertEntryType;
import com.quachtd.btp.is.alertservice.model.Part1Result;
import com.quachtd.btp.is.alertservice.model.Part2Result;
import com.quachtd.btp.is.alertservice.model.PidInterfaceEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class Part2TransformationServiceTest {

    @Mock
    private ConsolidationResolver consolidationResolver;

    private Part2TransformationService part2TransformationService;

    @BeforeEach
    void setUp() {
        part2TransformationService = new Part2TransformationService(
                new ErrorEventToPidMapper(), consolidationResolver);
    }

    @Test
    void transform_mapsMultipleErrorEventsToGroupWithUniquePids() {
        Part1Result part1Result = new Part1Result(
                Map.of("BTPSupport", new LinkedHashSet<>(List.of("EJS", "HUBX", "ADP", "BANK"))),
                Map.of(
                        "Pid1", new LinkedHashMap<>(Map.of("EJS", 2, "HUBX", 1)),
                        "Pid2", new LinkedHashMap<>(Map.of("ADP", 3)),
                        "Pid3", new LinkedHashMap<>(Map.of("BANK", 1))),
                Map.of(
                        "SAPToDownstream", "Pid1",
                        "ADPInterface", "Pid2",
                        "BankInterface", "Pid3"));

        Part2Result result = part2TransformationService.transform(part1Result);

        assertEquals(1, result.getEntries().size());
        AlertEntry entry = result.getEntries().get(0);
        assertEquals("BTPSupport", entry.getRecipient());
        assertEquals(AlertEntryType.GROUP, entry.getType());
        assertEquals(List.of(
                new PidInterfaceEntry("Pid1", "SAPToDownstream"),
                new PidInterfaceEntry("Pid2", "ADPInterface"),
                new PidInterfaceEntry("Pid3", "BankInterface")), entry.getGroupValue());
        assertEquals(7, entry.getErrorNumber());
    }

    @Test
    void transform_mapsSingleErrorEventWithoutConsolidationToSingle() {
        when(consolidationResolver.resolveResourceName(eq("EJS1"))).thenReturn(Optional.empty());

        Part1Result part1Result = new Part1Result(
                Map.of("EJS1", Set.of("EJS")),
                Map.of("Pid1", new LinkedHashMap<>(Map.of("EJS", 1))));

        Part2Result result = part2TransformationService.transform(part1Result);

        AlertEntry entry = result.getEntries().get(0);
        assertEquals("EJS1", entry.getRecipient());
        assertEquals(AlertEntryType.SINGLE, entry.getType());
        assertEquals("EJS", entry.getSingleValue());
        assertEquals(1, entry.getErrorNumber());
    }

    @Test
    void transform_mapsSingleErrorEventWithConsolidationToGroup() {
        when(consolidationResolver.resolveResourceName(eq("BTPSupport")))
                .thenReturn(Optional.of("BTPSupportGroup"));

        Part1Result part1Result = new Part1Result(
                Map.of("BTPSupport", Set.of("EJS")),
                Map.of("Pid1", new LinkedHashMap<>(Map.of("EJS", 1))),
                Map.of("SAPToDownstream", "Pid1"));

        Part2Result result = part2TransformationService.transform(part1Result);

        AlertEntry entry = result.getEntries().get(0);
        assertEquals("BTPSupportGroup", entry.getRecipient());
        assertEquals(AlertEntryType.GROUP, entry.getType());
        assertEquals(List.of(new PidInterfaceEntry("Pid1", "SAPToDownstream")), entry.getGroupValue());
        assertEquals(1, entry.getErrorNumber());
    }
}
