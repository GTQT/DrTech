package com.drppp.drtech.loaders.misc;

import gregtech.api.recipes.ModHandler;
import gregtech.api.unification.stack.UnificationEntry;
import gregtech.common.items.MetaItems;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;

import static com.drppp.drtech.common.Items.MetaItems.MyMetaItems.*;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.ore.OrePrefix.*;
import static keqing.gtqtcore.api.unification.GTQTMaterials.Wool;
import static keqing.gtqtcore.api.unification.GTQTMaterials.Zylon;
import static keqing.gtqtcore.api.unification.ore.GTQTOrePrefix.wrap;

public class ArmorRecipes {
    public static void init() {
        GasMask();
    }

    private static void GasMask() {
        ModHandler.addShapedRecipe(true, "simple_gas_mask", SIMPLE_GAS_MASK.getStackForm(),
                "CPC", "FHF", "CPC",
                'P', new UnificationEntry(dust, Carbon),
                'H', Blocks.WOOL,
                'C', Items.STRING,
                'F', new UnificationEntry(wrap, Wool));

        ModHandler.addShapedRecipe(true, "gas_mask", GAS_MASK.getStackForm(),
                "CPC", "FHF", "CPC",
                'P', new UnificationEntry(dust, Carbon),
                'H', MetaItems.ITEM_FILTER,
                'C', Items.STRING,
                'F', new UnificationEntry(wrap, PolyvinylChloride));

        ModHandler.addShapedRecipe(true, "gas_tank", GAS_TANK.getStackForm(),
                "CXC", "FTF", "PVP",
                'P', MetaItems.FLUID_CELL_LARGE_STEEL,
                'T', new UnificationEntry(pipeSmallFluid, Steel),
                'C', Items.STRING,
                'X', new UnificationEntry(ring, Steel),
                'V', MetaItems.ELECTRIC_PUMP_MV,
                'F', new UnificationEntry(wrap, PolyvinylChloride));

        //石棉套
        ModHandler.addShapedRecipe(true, "asbestos_mask", ASBESTOS_MASK.getStackForm(),
                "FFF", "FTF", "FPF",
                'P', GAS_MASK,
                'T', Items.IRON_HELMET,
                'F', new UnificationEntry(wrap, Asbestos));

        ModHandler.addShapedRecipe(true, "asbestos_chestplate", ASBESTOS_CHESTPLATE.getStackForm(),
                "FFF", "FTF", "FPF",
                'P', GAS_TANK,
                'T', Items.IRON_CHESTPLATE,
                'F', new UnificationEntry(wrap, Asbestos));

        ModHandler.addShapedRecipe(true, "asbestos_leggings", ASBESTOS_LEGGINGS.getStackForm(),
                "FFF", "FTF", "FFF",
                'T', Items.IRON_LEGGINGS,
                'F', new UnificationEntry(wrap, Asbestos));

        ModHandler.addShapedRecipe(true, "asbestos_boots", ASBESTOS_BOOTS.getStackForm(),
                "FFF", "FTF", "FFF",
                'T', Items.IRON_BOOTS,
                'F', new UnificationEntry(wrap, Asbestos));

        ModHandler.addShapedRecipe(true, "rebreather_tank", REBREATHER_TANK.getStackForm(),
                "CXC", "FTF", "PVP",
                'P', MetaItems.FLUID_CELL_LARGE_ALUMINIUM,
                'T', new UnificationEntry(pipeSmallFluid, StainlessSteel),
                'X', new UnificationEntry(ring, Aluminium),
                'C', Items.STRING,
                'V', MetaItems.ELECTRIC_PUMP_EV,
                'F', new UnificationEntry(wrap, Epoxy));

        //热反射
        ModHandler.addShapedRecipe(true, "reflective_mask", REFLECTIVE_MASK.getStackForm(),
                "FFF", "FTF", "FPF",
                'P', GAS_MASK,
                'T', Items.GOLDEN_HELMET,
                'F', new UnificationEntry(wrap, ReinforcedEpoxyResin));

        ModHandler.addShapedRecipe(true, "reflective_chestplate", REFLECTIVE_CHESTPLATE.getStackForm(),
                "FFF", "FTF", "FPF",
                'P', REBREATHER_TANK,
                'T', Items.GOLDEN_CHESTPLATE,
                'F', new UnificationEntry(wrap, ReinforcedEpoxyResin));

        ModHandler.addShapedRecipe(true, "reflective_leggings", REFLECTIVE_LEGGINGS.getStackForm(),
                "FFF", "FTF", "FFF",
                'T', Items.GOLDEN_LEGGINGS,
                'F', new UnificationEntry(wrap, ReinforcedEpoxyResin));

        ModHandler.addShapedRecipe(true, "reflective_boots", REFLECTIVE_BOOTS.getStackForm(),
                "FFF", "FTF", "FFF",
                'T', Items.GOLDEN_BOOTS,
                'F', new UnificationEntry(wrap, ReinforcedEpoxyResin));

        ModHandler.addShapedRecipe(true, "filtered_tank", FILTERED_TANK.getStackForm(),
                "CXC", "FTF", "PVP",
                'P', MetaItems.FLUID_CELL_LARGE_STAINLESS_STEEL,
                'T', new UnificationEntry(pipeSmallFluid, TungstenSteel),
                'X', new UnificationEntry(ring, StainlessSteel),
                'C', Items.STRING,
                'V', MetaItems.ELECTRIC_PUMP_IV,
                'F', new UnificationEntry(wrap, Polytetrafluoroethylene));

        //诺美克
        ModHandler.addShapedRecipe(true, "nomex_mask", NOMEX_MASK.getStackForm(),
                "FFF", "FTF", "FPF",
                'P', GAS_MASK,
                'T', Items.DIAMOND_HELMET,
                'F', new UnificationEntry(wrap, Zylon));

        ModHandler.addShapedRecipe(true, "nomex_chestplate", NOMEX_CHESTPLATE.getStackForm(),
                "FFF", "FTF", "FPF",
                'P', FILTERED_TANK,
                'T', Items.DIAMOND_CHESTPLATE,
                'F', new UnificationEntry(wrap, Zylon));

        ModHandler.addShapedRecipe(true, "nomex_leggings", NOMEX_LEGGINGS.getStackForm(),
                "FFF", "FTF", "FFF",
                'T', Items.DIAMOND_LEGGINGS,
                'F', new UnificationEntry(wrap, Zylon));

        ModHandler.addShapedRecipe(true, "nomex_boots", NOMEX_BOOTS.getStackForm(),
                "FFF", "FTF", "FFF",
                'T', Items.DIAMOND_BOOTS,
                'F', new UnificationEntry(wrap, Zylon));
    }
}
