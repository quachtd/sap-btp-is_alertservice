package com.btp.is.alertservice.model;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

public class MplFetchResult {

    private final List<MessageProcessingLog> messages;
    private final Instant windowStart;
    private final Instant windowEnd;
    private final boolean watermarkEligible;

    public MplFetchResult(
            List<MessageProcessingLog> messages,
            Instant windowStart,
            Instant windowEnd,
            boolean watermarkEligible) {
        this.messages = List.copyOf(messages);
        this.windowStart = windowStart;
        this.windowEnd = windowEnd;
        this.watermarkEligible = watermarkEligible;
    }

    public List<MessageProcessingLog> getMessages() {
        return messages;
    }

    public Instant getWindowStart() {
        return windowStart;
    }

    public Instant getWindowEnd() {
        return windowEnd;
    }

    public boolean isWatermarkEligible() {
        return watermarkEligible;
    }

    public static MplFetchResult sample(List<MessageProcessingLog> messages) {
        Instant now = Instant.now();
        return new MplFetchResult(messages, now, now, false);
    }

    public static MplFetchResult empty(Instant windowStart, Instant windowEnd) {
        return new MplFetchResult(Collections.emptyList(), windowStart, windowEnd, true);
    }
}
