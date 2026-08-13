package com.drppp.drtech.common.drone.program.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DronePortTypeTest {

    @Test
    void dockReferenceIsAOneWayCoordinateSource() {
        assertTrue(DronePortType.COORDINATE.accepts(DronePortType.DOCK_REFERENCE));
        assertTrue(DronePortType.DOCK_REFERENCE.accepts(DronePortType.DOCK_REFERENCE));
        assertFalse(DronePortType.DOCK_REFERENCE.accepts(DronePortType.COORDINATE));
        assertFalse(DronePortType.FLOW.accepts(DronePortType.DOCK_REFERENCE));
    }
}
