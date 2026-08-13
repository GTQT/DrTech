package com.drppp.drtech.common.drone.filter;

import com.drppp.drtech.common.drone.inventory.DroneItemFilter;
import com.drppp.drtech.common.drone.program.model.DroneNodePropertyType;
import com.drppp.drtech.common.drone.program.model.DronePortType;
import com.drppp.drtech.common.drone.program.model.DroneNodeDefinition;
import com.drppp.drtech.common.drone.program.registry.DrTechDroneNodes;
import com.drppp.drtech.common.drone.program.runtime.DrTechDroneValueEvaluators;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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

    @Test
    void blockFilterRoundTripsReadableBlockStatePredicatesWithoutBreakingLegacyMetadata() {
        Map<String, String> properties = new LinkedHashMap<>();
        properties.put("facing", "north");
        properties.put("half", "top");
        DroneBlockFilterSpec source = new DroneBlockFilterSpec(DroneFilterMode.BLACKLIST,
                Arrays.asList(new DroneBlockFilterSpec.Rule(new ResourceLocation("minecraft", "stairs"),
                                -1, properties),
                        new DroneBlockFilterSpec.Rule(new ResourceLocation("minecraft", "wool"), 14)));

        DroneBlockFilterSpec decoded = DroneBlockFilterSpec.readFromNbt(source.writeToNbt());

        assertEquals(DroneFilterMode.BLACKLIST, decoded.getMode());
        assertEquals("north", decoded.getRules().get(0).getStateProperties().get("facing"));
        assertEquals("top", decoded.getRules().get(0).getStateProperties().get("half"));
        assertEquals(14, decoded.getRules().get(1).getMetadata());
        assertTrue(decoded.getRules().get(1).getStateProperties().isEmpty());
    }

    @Test
    void searchableItemRulesPreserveRegistryWildcardAndPureOreSelectors() {
        DroneItemFilterSpec source = new DroneItemFilterSpec(DroneFilterMode.WHITELIST, Arrays.asList(
                new DroneItemFilterSpec.Rule(new ResourceLocation("minecraft", "wool"), -1,
                        "", "", false, null),
                new DroneItemFilterSpec.Rule(null, -1, "ingotIron", "", false, null)));

        DroneItemFilterSpec decoded = DroneItemFilterSpec.readFromNbt(source.writeToNbt());

        assertEquals("minecraft:wool", decoded.getRules().get(0).getItemId().toString());
        assertEquals(-1, decoded.getRules().get(0).getMetadata());
        assertNull(decoded.getRules().get(1).getItemId());
        assertEquals("ingotIron", decoded.getRules().get(1).getOreDictionary());
    }

    @Test
    void entityFilterIsAUsableTypedValueNodeWithRuntimeEvaluator() {
        DroneNodeDefinition definition = DrTechDroneNodes.createDefaultRegistry().get(DrTechDroneNodes.ENTITY_FILTER);

        assertNotNull(definition);
        assertEquals(DronePortType.ENTITY_FILTER, definition.getPorts().iterator().next().getType());
        assertEquals(DroneNodePropertyType.ENTITY_SELECTOR,
                definition.getProperties().iterator().next().getType());
        assertNotNull(DrTechDroneValueEvaluators.createDefaultRegistry().get(DrTechDroneNodes.ENTITY_FILTER));
    }
}
