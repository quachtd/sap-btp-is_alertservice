package com.quachtd.btp.is.alertservice.model;

import java.util.Collections;
import java.util.List;

public class Part2Result {

    private final List<AlertEntry> entries;

    public Part2Result(List<AlertEntry> entries) {
        this.entries = List.copyOf(entries);
    }

    public List<AlertEntry> getEntries() {
        return entries;
    }

    public static Part2Result empty() {
        return new Part2Result(Collections.emptyList());
    }
}
