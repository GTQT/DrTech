package com.drppp.drtech.common.drone.program;

import com.drppp.drtech.common.drone.program.edit.DroneDragPreviewPolicy;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DroneDragPreviewPolicyTest {

    @Test
    void keepsFinalPreviewUntilServerAcknowledgesWithoutFlashingAtOldPosition() {
        assertTrue(DroneDragPreviewPolicy.shouldKeep(12, 12, 10, 20, 80, 90, 1_000, 4_000));
        assertFalse(DroneDragPreviewPolicy.shouldKeep(13, 12, 80, 90, 80, 90, 1_100, 4_000));
    }

    @Test
    void releasesPreviewAfterRejectedRevisionOrTimeout() {
        assertFalse(DroneDragPreviewPolicy.shouldKeep(13, 12, 10, 20, 80, 90, 1_100, 4_000));
        assertFalse(DroneDragPreviewPolicy.shouldKeep(12, 12, 10, 20, 80, 90, 4_000, 4_000));
    }
}
