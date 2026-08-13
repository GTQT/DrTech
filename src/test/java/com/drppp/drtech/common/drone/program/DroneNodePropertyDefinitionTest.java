package com.drppp.drtech.common.drone.program;

import com.drppp.drtech.common.drone.program.model.DroneNodePropertyDefinition;
import com.drppp.drtech.common.drone.program.model.DroneNodePropertyType;
import net.minecraft.nbt.NBTTagCompound;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class DroneNodePropertyDefinitionTest {

    @Test
    void validatesNumbersEnumsAndRequiredPropertiesWithoutClientCode() {
        DroneNodePropertyDefinition count = DroneNodePropertyDefinition.integer("Count", 0, 10).required();
        DroneNodePropertyDefinition operator = DroneNodePropertyDefinition.enumeration("Operator", "AND", "OR");
        NBTTagCompound configuration = new NBTTagCompound();

        assertNotNull(count.validate(configuration));
        configuration.setInteger("Count", 4);
        configuration.setString("Operator", "AND");
        assertNull(count.validate(configuration));
        assertNull(operator.validate(configuration));
        configuration.setString("Operator", "XOR");
        assertNotNull(operator.validate(configuration));
        configuration.setDouble("Count", 4.5D);
        assertNotNull(count.validate(configuration));
    }

    @Test
    void fluidSelectorAcceptsEmptyAndBoundsRegistryNameLength() {
        DroneNodePropertyDefinition fluid = DroneNodePropertyDefinition.selector("Fluid",
                DroneNodePropertyType.FLUID_SELECTOR);
        NBTTagCompound configuration = new NBTTagCompound();

        assertNull(fluid.validate(configuration));
        configuration.setString("Fluid", "");
        assertNull(fluid.validate(configuration));
        configuration.setString("Fluid", "example:industrial_coolant");
        assertNull(fluid.validate(configuration));
        configuration.setString("Fluid", new String(new char[129]).replace('\0', 'x'));
        assertNotNull(fluid.validate(configuration));
    }
}
