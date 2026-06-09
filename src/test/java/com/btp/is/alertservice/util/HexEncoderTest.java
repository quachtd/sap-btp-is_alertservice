package com.btp.is.alertservice.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HexEncoderTest {

    @Test
    void encodesSenderToHex() {
        assertEquals("534150", HexEncoder.encode("SAP"));
    }

    @Test
    void encodesSenderInterfaceToHex() {
        assertEquals("53656e646572496e74657266616365", HexEncoder.encode("SenderInterface"));
    }

    @Test
    void encodesEmptyString() {
        assertEquals("", HexEncoder.encode(""));
    }
}
