package com.drppp.drtech.Client.drone;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DroneMultilineTextWidgetTest {

    @Test
    void sanitizerNormalizesLineEndingsAndRemovesFormattingCodes() {
        assertEquals("第一行\n第二行红色", DroneMultilineTextWidget.sanitize(
                "第一行\r\n第二行§c红色", 1024, 32));
    }

    @Test
    void sanitizerBoundsCharactersAndLinesIndependently() {
        assertEquals("1234\n", DroneMultilineTextWidget.sanitize("1234\n5678\n90", 5, 2));
        assertEquals("a\nb", DroneMultilineTextWidget.sanitize("a\nb\nc", 100, 2));
    }
}
