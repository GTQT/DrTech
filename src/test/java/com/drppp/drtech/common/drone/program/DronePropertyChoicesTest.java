package com.drppp.drtech.common.drone.program;

import com.drppp.drtech.common.drone.program.edit.DronePropertyChoices;
import com.drppp.drtech.common.drone.program.model.DroneNodePropertyDefinition;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DronePropertyChoicesTest {

    @Test
    void dropdownHidesUnsetSentinelAndRejectsUnknownValues() {
        DroneNodePropertyDefinition property = DroneNodePropertyDefinition.enumeration("Operator", "", "AND", "OR");

        assertEquals(Arrays.asList("AND", "OR"), DronePropertyChoices.visibleValues(property));
        assertTrue(DronePropertyChoices.accepts(property, "AND"));
        assertFalse(DronePropertyChoices.accepts(property, "XOR"));
    }

    @Test
    void recognizesCompleteSixFaceDirectionButNotPartialEnums() {
        DroneNodePropertyDefinition direction = DroneNodePropertyDefinition.enumeration("Direction",
                "AUTO", "DOWN", "UP", "NORTH", "SOUTH", "WEST", "EAST");
        DroneNodePropertyDefinition partial = DroneNodePropertyDefinition.enumeration("Direction", "UP", "DOWN");

        assertTrue(DronePropertyChoices.isSixFaceDirection(direction));
        assertFalse(DronePropertyChoices.isSixFaceDirection(partial));
    }
}
