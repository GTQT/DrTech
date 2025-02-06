package com.drppp.drtech.loaders.chain;

import com.drppp.drtech.common.Blocks.BlocksInit;
import com.drppp.drtech.common.Items.MetaItems.MetaItemsReactor;
import com.drppp.drtech.common.Items.MetaItems.DrMetaItems;
import com.drppp.drtech.common.MetaTileEntities.MetaTileEntities;
import gregtech.api.fluids.store.FluidStorageKeys;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.metatileentity.multiblock.CleanroomType;
import gregtech.api.recipes.ModHandler;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.recipes.ingredients.nbtmatch.NBTCondition;
import gregtech.api.recipes.ingredients.nbtmatch.NBTMatcher;
import gregtech.api.unification.material.MarkerMaterials;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.UnificationEntry;
import gregtech.common.items.MetaItems;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

import static gregtech.api.unification.material.Materials.Oxygen;

public class NuclearRecipe {
    public static void load()
    {
        RecipeMaps.VACUUM_RECIPES.recipeBuilder()
                .inputNBT(MetaItemsReactor.HE_COOLANT_CELL_60K,1, NBTMatcher.ANY, NBTCondition.ANY)
                .output(MetaItemsReactor.HE_COOLANT_CELL_60K)
                .EUt(120)
                .duration(60)
                .buildAndRegister();
        RecipeMaps.VACUUM_RECIPES.recipeBuilder()
                .inputNBT(MetaItemsReactor.HE_COOLANT_CELL_180K,1, NBTMatcher.ANY, NBTCondition.ANY)
                .output(MetaItemsReactor.HE_COOLANT_CELL_180K)
                .EUt(120)
                .duration(180)
                .buildAndRegister();
        RecipeMaps.VACUUM_RECIPES.recipeBuilder()
                .inputNBT(MetaItemsReactor.HE_COOLANT_CELL_360K,1, NBTMatcher.ANY, NBTCondition.ANY)
                .output(MetaItemsReactor.HE_COOLANT_CELL_360K)
                .EUt(120)
                .duration(360)
                .buildAndRegister();
        RecipeMaps.VACUUM_RECIPES.recipeBuilder()
                .inputNBT(MetaItemsReactor.NAK_COOLANT_CELL_60K,1, NBTMatcher.ANY, NBTCondition.ANY)
                .output(MetaItemsReactor.NAK_COOLANT_CELL_60K)
                .EUt(120)
                .duration(60)
                .buildAndRegister();
        RecipeMaps.VACUUM_RECIPES.recipeBuilder()
                .inputNBT(MetaItemsReactor.NAK_COOLANT_CELL_180K,1, NBTMatcher.ANY, NBTCondition.ANY)
                .output(MetaItemsReactor.NAK_COOLANT_CELL_180K)
                .EUt(120)
                .duration(180)
                .buildAndRegister();
        RecipeMaps.VACUUM_RECIPES.recipeBuilder()
                .inputNBT(MetaItemsReactor.NAK_COOLANT_CELL_360K,1, NBTMatcher.ANY, NBTCondition.ANY)
                .output(MetaItemsReactor.NAK_COOLANT_CELL_360K)
                .EUt(120)
                .duration(360)
                .buildAndRegister();
        List<MetaItem.MetaValueItem> lists = new ArrayList<>();
        lists.add(MetaItemsReactor.ADVANCED_HEAT_VENT);
        lists.add(MetaItemsReactor.HEAT_VENT);
        lists.add(MetaItemsReactor.REACTOR_HEAT_VENT);
        lists.add(MetaItemsReactor.COMPONENT_HEAT_VENT);
        lists.add(MetaItemsReactor.OVERCLOCKED_HEAT_VENT);
        lists.add(MetaItemsReactor.HEAT_EXCHANGER);
        lists.add(MetaItemsReactor.ADVANCED_HEAT_EXCHANGER);
        lists.add(MetaItemsReactor.COMPONENT_HEAT_EXCHANGER);
        lists.add(MetaItemsReactor.REACTOR_HEAT_EXCHANGER);
        for (var s:lists)
        {
            RecipeMaps.VACUUM_RECIPES.recipeBuilder()
                    .inputNBT(s,1, NBTMatcher.ANY, NBTCondition.ANY)
                    .output(s)
                    .EUt(120)
                    .duration(100)
                    .buildAndRegister();
        }
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(Blocks.GLASS_PANE,4)
                .input(OrePrefix.plate, Materials.Tin,4)
                .circuitMeta(1)
                .output(MetaItemsReactor.COOLANT_NULL_CELL_1)
                .EUt(30)
                .duration(200)
                .buildAndRegister();
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(Blocks.GLASS_PANE,6)
                .input(OrePrefix.plate, Materials.Aluminium,6)
                .circuitMeta(1)
                .output(MetaItemsReactor.COOLANT_NULL_CELL_2)
                .EUt(60)
                .duration(300)
                .buildAndRegister();
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(Blocks.GLASS_PANE,8)
                .input(OrePrefix.plate, Materials.StainlessSteel,8)
                .circuitMeta(1)
                .output(MetaItemsReactor.COOLANT_NULL_CELL_3)
                .EUt(120)
                .duration(400)
                .buildAndRegister();

        ModHandler.addShapedRecipe(true, "heat_exchanger", MetaItemsReactor.HEAT_EXCHANGER.getStackForm(),
                "ADA",
                        "BCB",
                        "ABA",
                'D', new UnificationEntry(OrePrefix.circuit, MarkerMaterials.Tier.HV),
                'A', new UnificationEntry(OrePrefix.plate,Materials.Silver),
                'B', new UnificationEntry(OrePrefix.plate,Materials.Aluminium),
                'C', new UnificationEntry(OrePrefix.plate,Materials.Copper)
        );
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.circuit, MarkerMaterials.Tier.HV)
                .input(OrePrefix.plate,Materials.Silver,2)
                .input(OrePrefix.plate,Materials.Aluminium,2)
                .input(OrePrefix.plate,Materials.Copper)
                .circuitMeta(21)
                .output(MetaItemsReactor.HEAT_EXCHANGER)
                .EUt(30)
                .duration(60)
                .buildAndRegister();
        ModHandler.addShapedRecipe(true, "reactor_heat_exchanger", MetaItemsReactor.REACTOR_HEAT_EXCHANGER.getStackForm(),
                "ABA",
                        "BCB",
                        "ABA",
                'A', new UnificationEntry(OrePrefix.plateDouble,Materials.Copper),
                'B', new UnificationEntry(OrePrefix.plate,Materials.Silver),
                'C', MetaItemsReactor.HEAT_EXCHANGER
        );
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(MetaItemsReactor.HEAT_EXCHANGER)
                .input(OrePrefix.plate,Materials.Silver,2)
                .input(OrePrefix.plateDouble,Materials.Copper,2)
                .circuitMeta(21)
                .output(MetaItemsReactor.REACTOR_HEAT_EXCHANGER)
                .EUt(120)
                .duration(60)
                .buildAndRegister();
        ModHandler.addShapedRecipe(true, "reactor_heat_exchanger", MetaItemsReactor.COMPONENT_HEAT_EXCHANGER.getStackForm(),
                "ABA",
                        "BCB",
                        "ABA",
                'A', new UnificationEntry(OrePrefix.screw,Materials.StainlessSteel),
                'B', new UnificationEntry(OrePrefix.plate,Materials.Gold),
                'C', MetaItemsReactor.HEAT_EXCHANGER
        );
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(MetaItemsReactor.HEAT_EXCHANGER)
                .input(OrePrefix.plate,Materials.Gold,2)
                .fluidInputs(Materials.StainlessSteel.getFluid(72))
                .circuitMeta(21)
                .output(MetaItemsReactor.COMPONENT_HEAT_EXCHANGER)
                .EUt(120)
                .duration(60)
                .buildAndRegister();
        ModHandler.addShapedRecipe(true, "advanced_heat_exchanger", MetaItemsReactor.ADVANCED_HEAT_EXCHANGER.getStackForm(),
                "ABA",
                        "DCD",
                        "ABA",
                'A', new UnificationEntry(OrePrefix.plate,Materials.Lapis),
                'B',  new UnificationEntry(OrePrefix.circuit, MarkerMaterials.Tier.EV),
                'C', new UnificationEntry(OrePrefix.plate,Materials.Diamond),
                'D', MetaItemsReactor.HEAT_EXCHANGER
        );
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(MetaItemsReactor.HEAT_EXCHANGER)
                .input(MetaItemsReactor.HEAT_EXCHANGER)
                .input(OrePrefix.plate,Materials.Lapis,2)
                .input(OrePrefix.plate,Materials.Diamond)
                .input(OrePrefix.circuit, MarkerMaterials.Tier.EV,2)
                .circuitMeta(21)
                .output(MetaItemsReactor.ADVANCED_HEAT_EXCHANGER)
                .EUt(480)
                .duration(60)
                .buildAndRegister();
        ModHandler.addShapedRecipe(true, "heat_vent", MetaItemsReactor.HEAT_VENT.getStackForm(),
                "ABA",
                        "BCB",
                        "ABA",
                'A', new UnificationEntry(OrePrefix.plate,Materials.Aluminium),
                'B', Blocks.IRON_BARS,
                'C', MetaItems.ELECTRIC_MOTOR_LV
        );
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.plate,Materials.Aluminium,2)
                .input(Blocks.IRON_BARS,2)
                .input(MetaItems.ELECTRIC_MOTOR_LV)
                .circuitMeta(21)
                .output(MetaItemsReactor.HEAT_VENT)
                .EUt(30)
                .duration(60)
                .buildAndRegister();

        ModHandler.addShapedRecipe(true, "component_heat_vent", MetaItemsReactor.COMPONENT_HEAT_VENT.getStackForm(),
                "ABA",
                        "BCB",
                        "ABA",
                'A', new UnificationEntry(OrePrefix.screw,Materials.Steel),
                'B',new UnificationEntry(OrePrefix.plateDense,Materials.Tin),
                'C', MetaItemsReactor.HEAT_VENT
        );
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.plateDense,Materials.Tin,2)
                .input(OrePrefix.screw,Materials.Steel,2)
                .input(MetaItemsReactor.HEAT_VENT)
                .circuitMeta(21)
                .output(MetaItemsReactor.COMPONENT_HEAT_VENT)
                .EUt(120)
                .duration(60)
                .buildAndRegister();

        ModHandler.addShapedRecipe(true, "advanced_component_heat_vent", MetaItemsReactor.ADVANCED_COMPONENT_HEAT_VENT.getStackForm(),
                "ABA",
                        "ACA",
                        "ABA",
                'A', new UnificationEntry(OrePrefix.plate,Materials.Gold),
                'B',new UnificationEntry(OrePrefix.plateDense,Materials.Tin),
                'C', MetaItemsReactor.COMPONENT_HEAT_VENT
        );
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.plate,Materials.Gold,3)
                .input(OrePrefix.plateDense,Materials.Tin,1)
                .input(MetaItemsReactor.COMPONENT_HEAT_VENT)
                .circuitMeta(21)
                .output(MetaItemsReactor.ADVANCED_COMPONENT_HEAT_VENT)
                .EUt(480)
                .duration(100)
                .buildAndRegister();
        ModHandler.addShapedRecipe(true, "reactor_heat_vent", MetaItemsReactor.REACTOR_HEAT_VENT.getStackForm(),
                "ABA",
                        "BCB",
                        "ABA",
                'A', new UnificationEntry(OrePrefix.plateDouble,Materials.Copper),
                'B',new UnificationEntry(OrePrefix.plate,Materials.Silver),
                'C', MetaItemsReactor.HEAT_VENT
        );
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.plateDouble,Materials.Copper,2)
                .input(OrePrefix.plate,Materials.Silver,2)
                .input(MetaItemsReactor.HEAT_VENT)
                .circuitMeta(21)
                .output(MetaItemsReactor.REACTOR_HEAT_VENT)
                .EUt(120)
                .duration(60)
                .buildAndRegister();
        ModHandler.addShapedRecipe(true, "advanced_heat_vent", MetaItemsReactor.ADVANCED_HEAT_VENT.getStackForm(),
                "ABA",
                        "ACA",
                        "ABA",
                'A', new UnificationEntry(OrePrefix.screw,Materials.StainlessSteel),
                'B',MetaItemsReactor.HEAT_VENT,
                'C', Items.DIAMOND
        );
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .fluidInputs(Materials.StainlessSteel.getFluid(72))
                .input(Items.DIAMOND)
                .input(MetaItemsReactor.HEAT_VENT,2)
                .circuitMeta(21)
                .output(MetaItemsReactor.ADVANCED_HEAT_VENT)
                .EUt(480)
                .duration(60)
                .buildAndRegister();

        ModHandler.addShapedRecipe(true, "overclocked_heat_vent", MetaItemsReactor.OVERCLOCKED_HEAT_VENT.getStackForm(),
                "ABA",
                        "BCB",
                        "ABA",
                'A', new UnificationEntry(OrePrefix.screw,Materials.StainlessSteel),
                'B', new UnificationEntry(OrePrefix.plate,Materials.Gold),
                'C', MetaItemsReactor.ADVANCED_HEAT_VENT
        );
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(MetaItemsReactor.ADVANCED_HEAT_VENT)
                .input(OrePrefix.plate,Materials.Gold,2)
                .fluidInputs(Materials.StainlessSteel.getFluid(72))
                .circuitMeta(21)
                .output(MetaItemsReactor.OVERCLOCKED_HEAT_VENT)
                .EUt(480)
                .duration(120)
                .buildAndRegister();
        List<Material> listm = new ArrayList<>();
        listm.add(Materials.TungstenCarbide);
        listm.add(Materials.Beryllium);
        listm.add(Materials.Copper);
        int i=400;
        for (var s:listm)
        {

            RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                    .input(OrePrefix.plateDouble,s)
                    .input(OrePrefix.plate,Materials.Tin,4)
                    .input(OrePrefix.dust,Materials.Graphite,4)
                    .input(MetaItems.CARBON_FIBER_PLATE,2)
                    .circuitMeta(1)
                    .output(MetaItemsReactor.NEUTRON_REFLECTOR1)
                    .cleanroom(CleanroomType.CLEANROOM)
                    .EUt(480)
                    .duration(i)
                    .buildAndRegister();
            i+=500;
        }
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.plateDouble,Materials.TungstenCarbide,2)
                .input(MetaItemsReactor.NEUTRON_REFLECTOR1)
                .input(MetaItemsReactor.NEUTRON_REFLECTOR1)
                .input(MetaItemsReactor.NEUTRON_REFLECTOR1)
                .input(MetaItemsReactor.NEUTRON_REFLECTOR1)
                .circuitMeta(1)
                .output(MetaItemsReactor.THICK_NEUTRON_REFLECTOR)
                .cleanroom(CleanroomType.CLEANROOM)
                .EUt(1920)
                .duration(600)
                .buildAndRegister();
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.plateDouble,Materials.Beryllium,2)
                .input(MetaItemsReactor.NEUTRON_REFLECTOR1)
                .input(MetaItemsReactor.NEUTRON_REFLECTOR1)
                .input(MetaItemsReactor.NEUTRON_REFLECTOR1)
                .input(MetaItemsReactor.NEUTRON_REFLECTOR1)
                .circuitMeta(1)
                .output(MetaItemsReactor.THICK_NEUTRON_REFLECTOR)
                .cleanroom(CleanroomType.CLEANROOM)
                .EUt(1920)
                .duration(800)
                .buildAndRegister();
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.plate,Materials.Iridium,8)
                .input(MetaItemsReactor.THICK_NEUTRON_REFLECTOR)
                .input(MetaItemsReactor.THICK_NEUTRON_REFLECTOR)
                .input(MetaItemsReactor.THICK_NEUTRON_REFLECTOR)
                .input(MetaItemsReactor.THICK_NEUTRON_REFLECTOR)
                .input(MetaItemsReactor.THICK_NEUTRON_REFLECTOR)
                .input(MetaItemsReactor.THICK_NEUTRON_REFLECTOR)
                .circuitMeta(1)
                .output(MetaItemsReactor.IRIDIUM_NEUTRON_REFLECTOR)
                .cleanroom(CleanroomType.CLEANROOM)
                .EUt(7680)
                .duration(900)
                .buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.plate,Materials.Iridium,8)
                .input(OrePrefix.plateDouble,Materials.TungstenCarbide,2)
                .input(OrePrefix.plate,Materials.Tin,64)
                .input(OrePrefix.plate,Materials.Tin,48)
                .input(MetaItems.CARBON_FIBER_PLATE,48)
                .input(OrePrefix.dust,Materials.Graphite,64)
                .input(OrePrefix.dust,Materials.Graphite,32)
                .circuitMeta(1)
                .output(MetaItemsReactor.IRIDIUM_NEUTRON_REFLECTOR)
                .cleanroom(CleanroomType.CLEANROOM)
                .EUt(30720)
                .duration(1800)
                .buildAndRegister();
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.dust,Materials.Uranium238,6)
                .input(OrePrefix.dustTiny,Materials.Uranium235,3)
                .input(DrMetaItems.NULL_FUEL_ROD)
                .fluidInputs(Oxygen.getFluid(FluidStorageKeys.LIQUID, 1000))
                .circuitMeta(1)
                .output(MetaItemsReactor.U_FUEL_ROD_1X)
                .EUt(32)
                .duration(20)
                .buildAndRegister();
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(MetaItemsReactor.U_FUEL_ROD_1X)
                .input(MetaItemsReactor.U_FUEL_ROD_1X)
                .input(OrePrefix.stick,Materials.Steel,2)
                .circuitMeta(2)
                .output(MetaItemsReactor.U_FUEL_ROD_2X)
                .EUt(32)
                .duration(20)
                .buildAndRegister();
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(MetaItemsReactor.U_FUEL_ROD_1X)
                .input(MetaItemsReactor.U_FUEL_ROD_1X)
                .input(MetaItemsReactor.U_FUEL_ROD_1X)
                .input(MetaItemsReactor.U_FUEL_ROD_1X)
                .input(OrePrefix.stick,Materials.Steel,4)
                .circuitMeta(20)
                .output(MetaItemsReactor.U_FUEL_ROD_4X)
                .EUt(32)
                .duration(20)
                .buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(MetaItemsReactor.U_FUEL_ROD_2X)
                .input(MetaItemsReactor.U_FUEL_ROD_2X)
                .input(OrePrefix.stick,Materials.Steel,4)
                .circuitMeta(20)
                .output(MetaItemsReactor.U_FUEL_ROD_4X)
                .EUt(32)
                .duration(20)
                .buildAndRegister();
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.dust,Materials.Thorium,3)
                .input(DrMetaItems.NULL_FUEL_ROD)
                .circuitMeta(1)
                .output(MetaItemsReactor.Th_FUEL_ROD_1X)
                .EUt(32)
                .duration(20)
                .buildAndRegister();
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(MetaItemsReactor.Th_FUEL_ROD_1X)
                .input(MetaItemsReactor.Th_FUEL_ROD_1X)
                .input(OrePrefix.stick,Materials.Steel,2)
                .circuitMeta(2)
                .output(MetaItemsReactor.Th_FUEL_ROD_2X)
                .EUt(32)
                .duration(20)
                .buildAndRegister();
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(MetaItemsReactor.Th_FUEL_ROD_1X)
                .input(MetaItemsReactor.Th_FUEL_ROD_1X)
                .input(MetaItemsReactor.Th_FUEL_ROD_1X)
                .input(MetaItemsReactor.Th_FUEL_ROD_1X)
                .input(OrePrefix.stick,Materials.Steel,4)
                .circuitMeta(20)
                .output(MetaItemsReactor.Th_FUEL_ROD_4X)
                .EUt(32)
                .duration(20)
                .buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(MetaItemsReactor.Th_FUEL_ROD_2X)
                .input(MetaItemsReactor.Th_FUEL_ROD_2X)
                .input(OrePrefix.stick,Materials.Steel,4)
                .circuitMeta(20)
                .output(MetaItemsReactor.Th_FUEL_ROD_4X)
                .EUt(32)
                .duration(20)
                .buildAndRegister();
        RecipeMaps.BENDER_RECIPES.recipeBuilder()
                .input(OrePrefix.plate,Materials.Iron)
                .circuitMeta(21)
                .output(DrMetaItems.NULL_FUEL_ROD)
                .EUt(16)
                .duration(20)
                .buildAndRegister();
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.dust,Materials.Uranium238,6)
                .input(OrePrefix.dust,Materials.Plutonium239,3)
                .input(DrMetaItems.NULL_FUEL_ROD)
                .fluidInputs(Oxygen.getFluid(FluidStorageKeys.LIQUID, 1000))
                .circuitMeta(1)
                .output(MetaItemsReactor.U_MOX_FUEL_ROD_1X)
                .EUt(32)
                .duration(20)
                .buildAndRegister();
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(MetaItemsReactor.U_MOX_FUEL_ROD_1X)
                .input(MetaItemsReactor.U_MOX_FUEL_ROD_1X)
                .input(OrePrefix.stick,Materials.Steel,2)
                .circuitMeta(2)
                .output(MetaItemsReactor.U_MOX_FUEL_ROD_2X)
                .EUt(32)
                .duration(20)
                .buildAndRegister();
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(MetaItemsReactor.U_MOX_FUEL_ROD_1X)
                .input(MetaItemsReactor.U_MOX_FUEL_ROD_1X)
                .input(MetaItemsReactor.U_MOX_FUEL_ROD_1X)
                .input(MetaItemsReactor.U_MOX_FUEL_ROD_1X)
                .input(OrePrefix.stick,Materials.Steel,4)
                .circuitMeta(20)
                .output(MetaItemsReactor.U_MOX_FUEL_ROD_4X)
                .EUt(32)
                .duration(20)
                .buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(MetaItemsReactor.U_MOX_FUEL_ROD_2X)
                .input(MetaItemsReactor.U_MOX_FUEL_ROD_2X)
                .input(OrePrefix.stick,Materials.Steel,4)
                .circuitMeta(20)
                .output(MetaItemsReactor.U_MOX_FUEL_ROD_4X)
                .EUt(32)
                .duration(20)
                .buildAndRegister();
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.plateDense,Materials.Lead,3)
                .input(OrePrefix.plateDense,Materials.Titanium,1)
                .input(OrePrefix.plate,Materials.Steel,4)
                .circuitMeta(21)
                .outputs(new ItemStack(BlocksInit.COMMON_CASING1,1,0))
                .EUt(480)
                .duration(400)
                .buildAndRegister();
          RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                  .input(OrePrefix.plateDense,Materials.Lead,2)
                  .input(OrePrefix.plateDense,Materials.Titanium,1)
                  .inputs(new ItemStack(BlocksInit.COMMON_CASING1,1,0))
                  .input(OrePrefix.wireGtOctal,Materials.Platinum)
                  .circuitMeta(22)
                  .outputs(MetaTileEntities.NUCLEAR_GENERATOR.getStackForm())
                  .EUt(1200)
                  .duration(1200)
                 .buildAndRegister();
        RecipeMaps.THERMAL_CENTRIFUGE_RECIPES.recipeBuilder()
                .input(MetaItemsReactor.U_FUEL_ROD_1X_EX)
                .output(OrePrefix.dust,Materials.Uranium238,4)
                .output(OrePrefix.dustTiny,Materials.Plutonium239,1)
                .output(OrePrefix.dust,Materials.Iron,1)
                .EUt(48)
                .duration(500)
                .buildAndRegister();
        RecipeMaps.THERMAL_CENTRIFUGE_RECIPES.recipeBuilder()
                .input(MetaItemsReactor.U_FUEL_ROD_2X_EX)
                .output(OrePrefix.dust,Materials.Uranium238,8)
                .output(OrePrefix.dustTiny,Materials.Plutonium239,2)
                .output(OrePrefix.dust,Materials.Iron,3)
                .EUt(48)
                .duration(500)
                .buildAndRegister();
        RecipeMaps.THERMAL_CENTRIFUGE_RECIPES.recipeBuilder()
                .input(MetaItemsReactor.U_FUEL_ROD_4X_EX)
                .output(OrePrefix.dust,Materials.Uranium238,16)
                .output(OrePrefix.dustTiny,Materials.Plutonium239,4)
                .output(OrePrefix.dust,Materials.Iron,6)
                .EUt(48)
                .duration(500)
                .buildAndRegister();

        RecipeMaps.THERMAL_CENTRIFUGE_RECIPES.recipeBuilder()
                .input(MetaItemsReactor.Th_FUEL_ROD_1X_EX)
                .output(OrePrefix.dust,Materials.Thorium,1)
                .output(OrePrefix.dustSmall,Materials.Lutetium,2)
                .output(OrePrefix.dust,Materials.Iron,1)
                .EUt(48)
                .duration(500)
                .buildAndRegister();
        RecipeMaps.THERMAL_CENTRIFUGE_RECIPES.recipeBuilder()
                .input(MetaItemsReactor.Th_FUEL_ROD_2X_EX)
                .output(OrePrefix.dust,Materials.Thorium,2)
                .output(OrePrefix.dust,Materials.Lutetium,1)
                .output(OrePrefix.dust,Materials.Iron,3)
                .EUt(48)
                .duration(500)
                .buildAndRegister();
        RecipeMaps.THERMAL_CENTRIFUGE_RECIPES.recipeBuilder()
                .input(MetaItemsReactor.Th_FUEL_ROD_4X_EX)
                .output(OrePrefix.dust,Materials.Thorium,4)
                .output(OrePrefix.dust,Materials.Lutetium,4)
                .output(OrePrefix.dust,Materials.Iron,6)
                .EUt(48)
                .duration(500)
                .buildAndRegister();
        RecipeMaps.THERMAL_CENTRIFUGE_RECIPES.recipeBuilder()
                .input(MetaItemsReactor.U_MOX_FUEL_ROD_1X_EX)
                .output(OrePrefix.dust,Materials.Plutonium239,3)
                .output(OrePrefix.dustTiny,Materials.Plutonium239,1)
                .output(OrePrefix.dust,Materials.Iron,1)
                .EUt(48)
                .duration(500)
                .buildAndRegister();
        RecipeMaps.THERMAL_CENTRIFUGE_RECIPES.recipeBuilder()
                .input(MetaItemsReactor.U_MOX_FUEL_ROD_2X_EX)
                .output(OrePrefix.dust,Materials.Plutonium239,6)
                .output(OrePrefix.dustTiny,Materials.Plutonium239,2)
                .output(OrePrefix.dust,Materials.Iron,3)
                .EUt(48)
                .duration(500)
                .buildAndRegister();
        RecipeMaps.THERMAL_CENTRIFUGE_RECIPES.recipeBuilder()
                .input(MetaItemsReactor.U_MOX_FUEL_ROD_4X_EX)
                .output(OrePrefix.dust,Materials.Plutonium239,12)
                .output(OrePrefix.dustTiny,Materials.Plutonium239,4)
                .output(OrePrefix.dust,Materials.Iron,6)
                .EUt(48)
                .duration(500)
                .buildAndRegister();

        //富集硅岩mox燃料棒
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.dust,Materials.NaquadahEnriched,12)
                .input(OrePrefix.dust,Materials.Plutonium239,12)
                .input(DrMetaItems.NULL_FUEL_ROD)
                .fluidInputs(Oxygen.getFluid(FluidStorageKeys.LIQUID, 4000))
                .circuitMeta(1)
                .output(MetaItemsReactor.Nq_MOX_FUEL_ROD_1X)
                .EUt(64)
                .duration(100)
                .buildAndRegister();
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(MetaItemsReactor.Nq_MOX_FUEL_ROD_1X)
                .input(MetaItemsReactor.Nq_MOX_FUEL_ROD_1X)
                .input(OrePrefix.stick,Materials.Steel,2)
                .circuitMeta(2)
                .output(MetaItemsReactor.Nq_MOX_FUEL_ROD_2X)
                .EUt(64)
                .duration(100)
                .buildAndRegister();
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(MetaItemsReactor.Nq_MOX_FUEL_ROD_1X)
                .input(MetaItemsReactor.Nq_MOX_FUEL_ROD_1X)
                .input(MetaItemsReactor.Nq_MOX_FUEL_ROD_1X)
                .input(MetaItemsReactor.Nq_MOX_FUEL_ROD_1X)
                .input(OrePrefix.stick,Materials.Steel,4)
                .circuitMeta(20)
                .output(MetaItemsReactor.Nq_MOX_FUEL_ROD_4X)
                .EUt(64)
                .duration(100)
                .buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(MetaItemsReactor.Nq_MOX_FUEL_ROD_2X)
                .input(MetaItemsReactor.Nq_MOX_FUEL_ROD_2X)
                .input(OrePrefix.stick,Materials.Steel,4)
                .circuitMeta(20)
                .output(MetaItemsReactor.Nq_MOX_FUEL_ROD_4X)
                .EUt(64)
                .duration(100)
                .buildAndRegister();
        RecipeMaps.THERMAL_CENTRIFUGE_RECIPES.recipeBuilder()
                .input(MetaItemsReactor.Nq_MOX_FUEL_ROD_1X_EX)
                .output(OrePrefix.dust,Materials.Plutonium239,3)
                .output(OrePrefix.dustTiny,Materials.Naquadria,1)
                .output(OrePrefix.dust,Materials.Iron,1)
                .EUt(48)
                .duration(500)
                .buildAndRegister();
        RecipeMaps.THERMAL_CENTRIFUGE_RECIPES.recipeBuilder()
                .input(MetaItemsReactor.Nq_MOX_FUEL_ROD_2X_EX)
                .output(OrePrefix.dust,Materials.Plutonium239,6)
                .output(OrePrefix.dust,Materials.Iron,3)
                .output(OrePrefix.dustTiny,Materials.Naquadria,2)
                .EUt(48)
                .duration(500)
                .buildAndRegister();
        RecipeMaps.THERMAL_CENTRIFUGE_RECIPES.recipeBuilder()
                .input(MetaItemsReactor.Nq_MOX_FUEL_ROD_4X_EX)
                .output(OrePrefix.dust,Materials.Plutonium239,12)
                .output(OrePrefix.dust,Materials.Iron,6)
                .output(OrePrefix.dustTiny,Materials.Naquadria,4)
                .EUt(48)
                .duration(500)
                .buildAndRegister();
        //升级配件
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.circuit,MarkerMaterials.Tier.HV)
                .input(OrePrefix.plate, Materials.Tin,4)
                .input(MetaItems.CONVEYOR_MODULE_HV,2)
                .circuitMeta(1)
                .output(MetaItemsReactor.UPGRADE_IO)
                .EUt(480)
                .duration(200)
                .buildAndRegister();
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.circuit,MarkerMaterials.Tier.HV)
                .input(OrePrefix.plate, Materials.Tin,4)
                .input(MetaItems.COVER_MACHINE_CONTROLLER)
                .circuitMeta(1)
                .output(MetaItemsReactor.UPGRADE_STOP)
                .EUt(480)
                .duration(200)
                .buildAndRegister();
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.circuit,MarkerMaterials.Tier.HV)
                .input(OrePrefix.plate, Materials.TungstenCarbide,4)
                .input(MetaItems.COVER_ACTIVITY_DETECTOR)
                .input(OrePrefix.dust,Materials.Graphite,64)
                .input(OrePrefix.dust,Materials.Graphite,64)
                .input(OrePrefix.dust,Materials.Graphite,64)
                .circuitMeta(1)
                .output(MetaItemsReactor.UPGRADE_CATCH)
                .EUt(1920)
                .duration(200)
                .buildAndRegister();
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.circuit,MarkerMaterials.Tier.HV)
                .input(OrePrefix.plate, Materials.TungstenCarbide,4)
                .input(MetaItems.COVER_ACTIVITY_DETECTOR)
                .input(MetaItemsReactor.THICK_NEUTRON_REFLECTOR)
                .circuitMeta(1)
                .output(MetaItemsReactor.UPGRADE_REFLECT)
                .EUt(480)
                .duration(200)
                .buildAndRegister();


    }
}
