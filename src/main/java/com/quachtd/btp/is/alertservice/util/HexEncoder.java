package com.quachtd.btp.is.alertservice.util;

import java.nio.charset.StandardCharsets;

public final class HexEncoder {

    private HexEncoder() {
    }

    public static String encode(String value) {
        if (value == null) {
            return "";
        }
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        StringBuilder hex = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            hex.append(String.format("%02x", b));
        }
        return hex.toString();
    }
}
