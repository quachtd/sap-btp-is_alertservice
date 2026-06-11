package com.quachtd.btp.is.alertservice.util;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ODataDateTimeFormatterTest {

    @Test
    void format_usesUtcODataDateTimeFormat() {
        Instant instant = Instant.parse("2026-06-08T17:30:45.123Z");

        assertEquals("2026-06-08T17:30:45", ODataDateTimeFormatter.format(instant));
    }
}
