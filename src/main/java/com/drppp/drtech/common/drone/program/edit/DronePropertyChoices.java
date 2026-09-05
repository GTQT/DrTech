package com.drppp.drtech.common.drone.program.edit;

import com.drppp.drtech.common.drone.program.model.DroneNodePropertyDefinition;
import com.drppp.drtech.common.drone.program.model.DroneNodePropertyType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/** Stable validation/model rules behind the inspector's enum and direction controls. */
public final class DronePropertyChoices {

    private DronePropertyChoices() {}

    public static boolean isChoice(DroneNodePropertyDefinition property) {
        return property != null && (property.getType() == DroneNodePropertyType.ENUM
                || property.getType() == DroneNodePropertyType.DIRECTION);
    }

    public static List<String> visibleValues(DroneNodePropertyDefinition property) {
        if (!isChoice(property)) return Collections.emptyList();
        List<String> values = new ArrayList<>();
        for (String value : property.getAllowedValues()) if (!value.isEmpty()) values.add(value);
        return Collections.unmodifiableList(values);
    }

    public static boolean isSixFaceDirection(DroneNodePropertyDefinition property) {
        if (!isChoice(property)) return false;
        Set<String> values = property.getAllowedValues();
        return values.contains("UP") && values.contains("DOWN") && values.contains("NORTH")
                && values.contains("SOUTH") && values.contains("WEST") && values.contains("EAST");
    }

    public static boolean accepts(DroneNodePropertyDefinition property, String value) {
        return isChoice(property) && value != null && property.getAllowedValues().contains(value);
    }
}
