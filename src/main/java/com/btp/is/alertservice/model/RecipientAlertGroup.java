package com.btp.is.alertservice.model;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class RecipientAlertGroup {

    private final Map<String, Set<String>> recipientToErrorEvents = new LinkedHashMap<>();

    public void add(String recipient, String errorEvent) {
        recipientToErrorEvents
                .computeIfAbsent(recipient, key -> new LinkedHashSet<>())
                .add(errorEvent);
    }

    public Map<String, Set<String>> getRecipientToErrorEvents() {
        Map<String, Set<String>> copy = new LinkedHashMap<>();
        recipientToErrorEvents.forEach((recipient, events) ->
                copy.put(recipient, Collections.unmodifiableSet(new LinkedHashSet<>(events))));
        return Collections.unmodifiableMap(copy);
    }
}
