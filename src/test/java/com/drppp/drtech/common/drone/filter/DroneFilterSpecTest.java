package com.drppp.drtech.common.drone.filter;

import com.drppp.drtech.common.drone.inventory.DroneItemFilter;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DroneFilterSpecTest {

    @Test
    void itemFilterRoundTripsMultipleStableRulesAndDefensiveNbt() {
        NBTTagCompound marker = new NBTTagCompound();
        marker.setString("Grade", "pure");
        DroneItemFilterSpec.Rule exact = new DroneItemFilterSpec.Rule(
                new ResourceLocation("minecraft", "iron_ingot"), 0, "", "minecraft", true, marker);
        DroneItemFilterSpec.Rule ore = new DroneItemFilterSpec.Rule(null, -1, "ingotCopper", "", false, null);
        DroneItemFilterSpec source = new DroneItemFilterSpec(DroneFilterMode.BLACKLIST, Arrays.asList(exact, ore));

        DroneItemFilterSpec decoded = DroneItemFilterSpec.readFromNbt(source.writeToNbt());
        marker.setString("Grade", "mutated");

        assertEquals(DroneFilterMode.BLACKLIST, decoded.getMode());
        assertEquals(2, decoded.getRules().size());
        assertEquals("minecraft:iron_ingot", decoded.getRules().get(0).getItemId().toString());
        assertEquals("pure", decoded.getRules().get(0).getNbt().getString("Grade"));
        assertEquals("ingotCopper", decoded.getRules().get(1).getOreDictionary());

        NBTTagCompound nodeConfiguration = new NBTTagCompound();
        nodeConfiguration.setTag("FilterSpec", decoded.writeToNbt());
        assertEquals(2, DroneItemFilter.fromConfiguration(nodeConfiguration).getSpec().getRules().size());
    }

    @Test
    void fluidAndEntityFiltersDiscardInvalidOrDuplicateRules() {
        DroneFluidFilterSpec fluids = new DroneFluidFilterSpec(DroneFilterMode.WHITELIST,
                Arrays.asList("water", "water", "lava"));
        DroneEntityFilterSpec entities = new DroneEntityFilterSpec(DroneFilterMode.BLACKLIST,
                Arrays.asList(new ResourceLocation("minecraft", "zombie"),
                        new ResourceLocation("minecraft", "zombie")));

        assertEquals(2, DroneFluidFilterSpec.readFromNbt(fluids.writeToNbt()).getFluidNames().size());
        assertEquals(1, DroneEntityFilterSpec.readFromNbt(entities.writeToNbt()).getEntityIds().size());
        assertTrue(DroneItemFilterSpec.readFromNbt(null).getRules().isEmpty());
    }

    @Test
    void blockFilterRoundTripsStableBlockIds() {
        DroneBlockFilterSpec source = new DroneBlockFilterSpec(DroneFilterMode.WHITELIST,
                Collections.singletonList(new DroneBlockFilterSpec.Rule(
                        new ResourceLocation("minecraft", "stone"), -1)));

        DroneBlockFilterSpec decoded = DroneBlockFilterSpec.readFromNbt(source.writeToNbt());

        assertEquals("minecraft:stone", decoded.getRules().get(0).getBlockId().toString());
        assertEquals(-1, decoded.getRules().get(0).getMetadata());
    }
}
