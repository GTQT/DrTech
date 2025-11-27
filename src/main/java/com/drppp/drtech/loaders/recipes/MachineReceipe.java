package com.drppp.drtech.loaders.recipes;

import com.drppp.drtech.api.Utils.DrtechUtils;
import com.drppp.drtech.api.unification.Materials.DrtechMaterials;
import com.drppp.drtech.common.Blocks.BlocksInit;
import com.drppp.drtech.common.Items.MetaItems.DrMetaItems;
import com.drppp.drtech.common.MetaTileEntities.DrTechMetaTileEntities;
import com.drppp.drtech.intergations.GTFOLinkage;
import com.drppp.drtech.intergations.GtqtCoreLinkage;
import com.drppp.drtech.intergations.HarvestcraftLinkage;
import forestry.apiculture.ModuleApiculture;
import forestry.apiculture.blocks.BlockAlvearyType;
import gregtech.api.GTValues;
import gregtech.api.unification.material.MarkerMaterials;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.common.blocks.BlockMachineCasing;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.items.MetaItems;
import keqing.gtqtcore.api.unification.GTQTMaterials;
import keqing.gtqtcore.common.metatileentities.GTQTMetaTileEntities;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Loader;

import static com.drppp.drtech.loaders.recipes.DrtechReceipes.*;
import static gregtech.api.GTValues.*;
import static gregtech.api.recipes.RecipeMaps.*;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.ore.OrePrefix.*;

public class MachineReceipe {
    public static void load() {
        DRONE_PAD.recipeBuilder()
                .input(ingot, Iron)
                .chancedOutput(new ItemStack(Items.BEEF, 8), 5000, 0)
                .chancedOutput(new ItemStack(Items.CHICKEN, 8), 5000, 0)
                .chancedOutput(new ItemStack(Items.PORKCHOP, 8), 5000, 0)
                .chancedOutput(new ItemStack(Items.MUTTON, 8), 5000, 0)
                .chancedOutput(new ItemStack(Items.RABBIT, 8), 5000, 0)
                .chancedOutput(new ItemStack(Items.ROTTEN_FLESH, 8), 5000, 0)
                .circuitMeta(1)
                .duration(10)
                .dimension(0)
                .EUt(2)
                .buildAndRegister();

        DRONE_PAD.recipeBuilder()
                .input(ingot, Copper)
                .chancedOutput(new ItemStack(Items.POTATO, 8), 5000, 0)
                .chancedOutput(new ItemStack(Items.CARROT, 8), 5000, 0)
                .chancedOutput(new ItemStack(Items.MELON, 8), 5000, 0)
                .chancedOutput(new ItemStack(Items.WHEAT, 8), 5000, 0)
                .chancedOutput(new ItemStack(Items.BEETROOT, 8), 5000, 0)
                .chancedOutput(new ItemStack(Blocks.CACTUS, 8), 5000, 0)
                .circuitMeta(1)
                .duration(10)
                .dimension(0)
                .EUt(2)
                .buildAndRegister();

        DRONE_PAD.recipeBuilder()
                .input(ingot, Tin)
                .chancedOutput(new ItemStack(Items.FISH, 8), 5000, 0)
                .chancedOutput(new ItemStack(Items.FISH, 8), 5000, 0)
                .chancedOutput(new ItemStack(Items.FISH, 8), 5000, 0)
                .chancedOutput(new ItemStack(Items.FISH, 8), 5000, 0)
                .chancedOutput(new ItemStack(Items.FISH, 8), 5000, 0)
                .chancedOutput(new ItemStack(Items.FISH, 8), 5000, 0)
                .circuitMeta(1)
                .duration(10)
                .dimension(0)
                .EUt(2)
                .buildAndRegister();

        DRONE_PAD.recipeBuilder()
                .input(ingot, Gold)
                .chancedOutput(new ItemStack(Items.ROTTEN_FLESH, 8), 5000, 0)
                .chancedOutput(new ItemStack(Items.SPIDER_EYE, 8), 5000, 0)
                .chancedOutput(new ItemStack(Items.STRING, 8), 5000, 0)
                .chancedOutput(new ItemStack(Items.GUNPOWDER, 8), 5000, 0)
                .chancedOutput(new ItemStack(Items.BONE, 8), 5000, 0)
                .chancedOutput(new ItemStack(Items.POISONOUS_POTATO, 1), 5000, 0)
                .circuitMeta(1)
                .duration(10)
                .dimension(0)
                .EUt(2)
                .buildAndRegister();

        DRONE_PAD.recipeBuilder()
                .input(ingot, Copper)
                .chancedOutput(new ItemStack(Items.GLOWSTONE_DUST, 8), 5000, 0)
                .chancedOutput(new ItemStack(Items.BLAZE_POWDER, 8), 5000, 0)
                .chancedOutput(new ItemStack(Items.COAL, 8), 5000, 0)
                .chancedOutput(new ItemStack(Items.NETHER_WART, 8), 5000, 0)
                .chancedOutput(new ItemStack(Items.MAGMA_CREAM, 8), 5000, 0)
                .chancedOutput(new ItemStack(Items.GOLD_NUGGET, 8), 5000, 0)
                .circuitMeta(2)
                .duration(10)
                .dimension(-1)
                .EUt(2)
                .buildAndRegister();

        MACERATOR_RECIPES.recipeBuilder()
                .input(Items.SKULL, 1, 1)
                .output(DrMetaItems.SKULL_DUST, 2)
                .duration(200)
                .EUt(1920)
                .buildAndRegister();
        MIXER_RECIPES.recipeBuilder()
                .input(DrMetaItems.SKULL_DUST, 1)
                .input(Blocks.SOUL_SAND, 2)
                .output(OrePrefix.dust, NetherStar, 1)
                .duration(300)
                .EUt(1920)
                .buildAndRegister();

        for (int i = 0; i < 4; i++) {
            DrtechUtils.addLogCreate(30, 100, 1, i);
        }
        for (int i = 0; i < 2; i++) {
            DrtechUtils.addLog2Create(30, 100, 1, i);
        }
        LOG_CREATE.recipeBuilder()
                .notConsumable(new ItemStack(MetaBlocks.RUBBER_SAPLING, 1))
                .output(MetaBlocks.RUBBER_LOG)
                .EUt(30)
                .duration(100)
                .buildAndRegister();
        MOLECULAR_RECOMBINATION.recipeBuilder()
                .input(OrePrefix.dust, Carbon, 1)
                .output(dust, Graphene, 1)
                .EUt(7680)
                .duration(652)
                .buildAndRegister();
        MOLECULAR_RECOMBINATION.recipeBuilder()
                .input(OrePrefix.dust, Tin, 1)
                .output(dust, Silver, 1)
                .EUt(7680)
                .duration(326)
                .buildAndRegister();
        MOLECULAR_RECOMBINATION.recipeBuilder()
                .input(OrePrefix.dust, Copper, 1)
                .output(dust, Nickel, 1)
                .EUt(7680)
                .duration(326)
                .buildAndRegister();
        MOLECULAR_RECOMBINATION.recipeBuilder()
                .input(Items.REDSTONE)
                .output(dust, Ruby, 1)
                .EUt(7680)
                .duration(326)
                .buildAndRegister();
        MOLECULAR_RECOMBINATION.recipeBuilder()
                .input(Items.COAL)
                .output(Items.DIAMOND)
                .EUt(7680)
                .duration(652)
                .buildAndRegister();
        if (Loader.isModLoaded(GtqtCoreLinkage.GTQTCORE_ID)) {
            GtqtCoreLinkage.MachineRecipeInit();
        }
        if (Loader.isModLoaded(GTFOLinkage.GTFO_ID)) {
            GTFOLinkage.MachineRecipeInit();
        }
        if (Loader.isModLoaded(HarvestcraftLinkage.CRAFT_ID)) {
            HarvestcraftLinkage.MachineRecipeInit();
        }
        ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.frameGt, Wood)
                .input(OrePrefix.pipeTinyFluid, Steel, 2)
                .input(MetaItems.VOLTAGE_COIL_MV)
                .input(MetaItems.PLANT_BALL, 4)
                .input(OrePrefix.plank, Wood, 3)
                .outputs(new ItemStack(BlocksInit.COMMON_CASING, 1, 2))
                .circuitMeta(4)
                .duration(100)
                .EUt(64)
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.frameGt, StainlessSteel)
                .input(OrePrefix.plate, EnderPearl, 4)
                .input(OrePrefix.plate, StainlessSteel, 4)
                .circuitMeta(3)
                .fluidInputs(Polytetrafluoroethylene.getFluid(144))
                .outputs(new ItemStack(BlocksInit.COMMON_CASING, 1, 3))
                .EUt(480)
                .duration(100)
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.frameGt, Steel)
                .input(OrePrefix.plate, EnderPearl, 4)
                .input(OrePrefix.plate, Steel, 4)
                .circuitMeta(6)
                .fluidInputs(Polytetrafluoroethylene.getFluid(144))
                .outputs(new ItemStack(BlocksInit.COMMON_CASING, 1, 4))
                .EUt(480)
                .duration(100)
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.plate, Polyethylene)
                .input(Items.PAPER)
                .output(DrMetaItems.POS_CARD)
                .circuitMeta(24)
                .EUt(16)
                .duration(100)
                .buildAndRegister();
        ASSEMBLER_RECIPES.recipeBuilder()
                .input(Blocks.GOLD_BLOCK, 64)
                .output(DrMetaItems.GOLD_COIN)
                .EUt(16)
                .duration(300)
                .buildAndRegister();
        SOLAR_TOWER.recipeBuilder()
                .fluidInputs(DrtechMaterials.ColdSunSalt.getFluid(1000))
                .fluidOutputs(DrtechMaterials.HotSunSalt.getFluid(1000))
                .EUt(1)
                .duration(200)
                .buildAndRegister();
        ASSEMBLER_RECIPES.recipeBuilder()
                .input(MetaItems.ELECTRIC_PUMP_LV)
                .inputs(MetaItems.FLUID_CELL_LARGE_ALUMINIUM.getStackForm(2))
                .input(OrePrefix.stick, Iron)
                .output(DrMetaItems.HAND_PUMP)
                .fluidInputs(Tin.getFluid(1440))
                .input(OrePrefix.ring, Rubber)
                .EUt(30)
                .duration(200)
                .buildAndRegister();
        ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .inputs(MetaBlocks.MACHINE_CASING.getItemVariant(BlockMachineCasing.MachineCasingType.ZPM))
                .input(circuit, MarkerMaterials.Tier.ZPM, 64)
                .input(circuit, MarkerMaterials.Tier.IV, 32)
                .inputs(MetaBlocks.METAL_CASING.getItemVariant(BlockMetalCasing.MetalCasingType.BRONZE_BRICKS, 32))
                .input(MetaItems.ROBOT_ARM_ZPM, 32)
                .input(MetaItems.CONVEYOR_MODULE_ZPM)
                .input(ModuleApiculture.getBlocks().getAlvearyBlock(BlockAlvearyType.PLAIN), 16)
                .input(ModuleApiculture.getBlocks().apiary, 16)
                .fluidInputs(SolderingAlloy.getFluid(1152))
                .fluidInputs(UUMatter.getFluid(144))
                .outputs(DrTechMetaTileEntities.LARGE_BEE_HIVE.getStackForm())
                .scannerResearch(b -> b
                        .researchStack(new ItemStack(ModuleApiculture.getBlocks().getAlvearyBlock(BlockAlvearyType.PLAIN)))
                        .duration(1200)
                        .EUt(VA[IV]))
                .duration(800).EUt(VA[ZPM]).buildAndRegister();


        ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .input(GTQTMetaTileEntities.LIGHTNING_ROD[2], 4)
                .input(circuit, MarkerMaterials.Tier.UHV, 16)
                .input(plate, Iridium, 48)
                .input(plate, Iridium, 48)
                .input(cableGtDouble, Gold, 64)
                .input(cableGtDouble, Gold, 64)
                .input(MetaItems.ULTIMATE_BATTERY)
                .fluidInputs(SolderingAlloy.getFluid(1440))
                .fluidInputs(UUMatter.getFluid(1440))
                .outputs(DrTechMetaTileEntities.LARGE_LIGHTING_ROD.getStackForm())
                .scannerResearch(b -> b
                        .researchStack(GTQTMetaTileEntities.LIGHTNING_ROD[2].getStackForm())
                        .duration(1200)
                        .EUt(VA[ZPM]))
                .EUt(GTValues.VA[ZPM])
                .duration(600)
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(DrTechMetaTileEntities.INDUSTRIAL_APIARY, 4)
                .input(circuit, MarkerMaterials.Tier.HV, 16)
                .input(MetaItems.ROBOT_ARM_HV, 16)
                .input(plate, StainlessSteel, 64)
                .fluidInputs(SolderingAlloy.getFluid(1440))
                .outputs(DrTechMetaTileEntities.COMB_PROVESS.getStackForm())
                .EUt(GTValues.VA[HV])
                .duration(600)
                .buildAndRegister();
        ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.plate, Polyethylene)
                .input(cableGtSingle, Copper, 4)
                .output(DrMetaItems.UPGRADE_NULL)
                .circuitMeta(9)
                .EUt(16)
                .duration(100)
                .buildAndRegister();
        ASSEMBLER_RECIPES.recipeBuilder()
                .input(plate, StainlessSteel, 2)
                .input(plate, GTQTMaterials.Staballoy, 4)
                .input(frameGt, GTQTMaterials.ZirconiumCarbide, 1)
                .outputs(new ItemStack(BlocksInit.COMMON_CASING1, 1, 1))
                .circuitMeta(1)
                .EUt(16)
                .duration(100)
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(stickLong, RoseGold, 2)
                .input(plate, Titanium, 4)
                .input(stick, TungstenSteel, 2)
                .input(frameGt, Titanium, 1)
                .outputs(new ItemStack(BlocksInit.COMMON_CASING1, 1, 2))
                .circuitMeta(1)
                .EUt(16)
                .duration(100)
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(plate, BlueSteel, 4)
                .input(stick, BlueSteel, 4)
                .input(frameGt, BlueSteel, 1)
                .outputs(new ItemStack(BlocksInit.COMMON_CASING1, 1, 3))
                .circuitMeta(1)
                .EUt(16)
                .duration(100)
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(wireFine, Steel, 5)
                .input(frameGt, GTQTMaterials.EglinSteel, 4)
                .outputs(new ItemStack(BlocksInit.COMMON_CASING1, 1, 4))
                .circuitMeta(1)
                .EUt(16)
                .duration(100)
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(plate, GTQTMaterials.EglinSteel, 8)
                .input(frameGt, GTQTMaterials.EnergeticAlloy, 1)
                .outputs(new ItemStack(BlocksInit.COMMON_CASING1, 1, 5))
                .circuitMeta(1)
                .EUt(16)
                .duration(100)
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(stick, RoseGold, 3)
                .input(plate, GTQTMaterials.Inconel792, 2)
                .input(plate, GTQTMaterials.MaragingSteel250, 4)
                .outputs(new ItemStack(BlocksInit.COMMON_CASING1, 1, 6))
                .circuitMeta(1)
                .EUt(16)
                .duration(100)
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(CraftingReceipe.getItemStack("item('gregtech:meta_item_1', 717)"))
                .input(dust, Thorium, 8)
                .output(DrMetaItems.NUCLEAR_BATTERY_LV)
                .circuitMeta(24)
                .EUt(24)
                .duration(100)
                .buildAndRegister();
        ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(CraftingReceipe.getItemStack("item('gregtech:meta_item_1', 718)"))
                .input(dust, Thorium, 32)
                .output(DrMetaItems.NUCLEAR_BATTERY_MV)
                .circuitMeta(24)
                .EUt(60)
                .duration(100)
                .buildAndRegister();
        ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(CraftingReceipe.getItemStack("item('gregtech:meta_item_1', 719)"))
                .input(dust, Thorium, 64)
                .output(DrMetaItems.NUCLEAR_BATTERY_HV)
                .circuitMeta(24)
                .EUt(420)
                .duration(100)
                .buildAndRegister();
        EXTRACTOR_RECIPES.recipeBuilder()
                .input(DrMetaItems.XJC,16)
                .output(DrMetaItems.NATURAL_RUBBER,1)
                .output(DrMetaItems.INULIN,8)
                .EUt(80)
                .duration(100)
                .buildAndRegister();
        ALLOY_SMELTER_RECIPES.recipeBuilder()
                .input(DrMetaItems.NATURAL_RUBBER,1)
                .input(dust,Carbon,2)
                .output(ingot,Rubber,1)
                .EUt(80)
                .duration(100)
                .buildAndRegister();
        FERMENTING_RECIPES.recipeBuilder()
                .input(DrMetaItems.INULIN,4)
                .fluidInput(Water.getFluid(),1000)
                .fluidOutputs(Ethanol.getFluid(750))
                .EUt(8)
                .duration(100)
                .buildAndRegister();
    }

}
