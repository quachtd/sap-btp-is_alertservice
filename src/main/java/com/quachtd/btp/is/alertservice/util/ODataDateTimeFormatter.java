package com.quachtd.btp.is.alertservice.util;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public final class ODataDateTimeFormatter {

    private static final DateTimeFormatter CPI_ODATA_DATETIME =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss").withZone(ZoneOffset.UTC);

    private ODataDateTimeFormatter() {
    }

    public static String format(Instant instant) {
        return CPI_ODATA_DATETIME.format(instant);
    }
}
