package com.drppp.drtech.loaders;

import com.drppp.drtech.common.Items.MetaItems.MyMetaItems;
import com.drppp.drtech.intergations.GTFOLinkage;
import com.drppp.drtech.intergations.GtqtCoreLinkage;
import com.drppp.drtech.api.unification.Materials.DrtechMaterials;
import com.drppp.drtech.api.Utils.DrtechUtils;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.properties.DustProperty;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.util.GTUtility;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.items.MetaItems;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import com.drppp.drtech.common.Blocks.BlocksInit;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Loader;

import static com.drppp.drtech.loaders.DrtechReceipes.*;
import static gregtech.api.recipes.RecipeMaps.*;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.material.info.MaterialFlags.EXPLOSIVE;
import static gregtech.api.unification.material.info.MaterialFlags.FLAMMABLE;
import static gregtech.api.unification.ore.OrePrefix.dust;
import static gregtech.api.unification.ore.OrePrefix.ingot;

public class MachineReceipe {
    public static void load()
    {
        DRONE_PAD.recipeBuilder()
                .input(ingot, Materials.Iron)
                .output(Items.BEEF, 16)
                .duration(10)
                .dimension(0)
                .EUt(2)
                .buildAndRegister();

        MACERATOR_RECIPES.recipeBuilder()
                .input(Items.SKULL,1,1)
                .output(MyMetaItems.SKULL_DUST,2)
                .duration(200)
                .EUt(1920)
                .buildAndRegister();
        MIXER_RECIPES.recipeBuilder()
                .input(MyMetaItems.SKULL_DUST,1)
                .input(Blocks.SOUL_SAND,2)
                .output(OrePrefix.dust,NetherStar,1)
                .duration(300)
                .EUt(1920)
                .buildAndRegister();
        ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(new ItemStack(MetaBlocks.TRANSPARENT_CASING,1,2))
                .input(MetaBlocks.TRANSPARENT_CASING)
                .fluidInputs(Materials.Polybenzimidazole.getFluid(144))
                .output(BlocksInit.MY_LASER_PIPE)
                .duration(100)
                .EUt(7680)
                .buildAndRegister();
        OrePrefix.dust.addProcessingHandler(PropertyKey.DUST, MachineReceipe::processDust);
        EIMPLOSION_RECIPES.recipeBuilder()
                .input(OrePrefix.dust,Carbon,63)
                .output(Items.DIAMOND)
                .explosivesAmount(OreDictUnifier.get(OrePrefix.dust, Carbon))
                .buildAndRegister();
        //160秒
        UU_RECIPES.recipeBuilder()
                .circuitMeta(1)
                .fluidOutputs(Materials.UUMatter.getFluid(1))
                .EUt(256)
                .duration(3200)
                .buildAndRegister();
        UU_RECIPES.recipeBuilder()
                .input(MyMetaItems.SCRAP)
                .fluidOutputs(Materials.UUMatter.getFluid(1))
                .EUt(256)
                .duration(1600)
                .buildAndRegister();
        //扫描和复制配方
        DrtechUtils.initList();
        for (int i = 0; i < DrtechUtils.listMater.size(); i++) {
            ItemStack is = MyMetaItems.CD_ROM.getStackForm();
            NBTTagCompound compound = new NBTTagCompound();
            compound.setString("Name",DrtechUtils.getName(DrtechUtils.listMater.get(i)));
            is.setTagCompound(compound);
            int mass  =(int)DrtechUtils.listMater.get(i).getMass();
            var buid =  SCANNER_RECIPES.recipeBuilder()
                    .input(MyMetaItems.CD_ROM)
                    .outputs(is)
                    .duration(DrtechUtils.baseTime*2*mass)
                    .EUt(30);
           var copybuild =  COPY_RECIPES.recipeBuilder()
                    .notConsumable(is)
                    .fluidInputs(Materials.UUMatter.getFluid(mass))
                    .duration(DrtechUtils.baseTime*mass)
                    .EUt(30);
            if(DrtechUtils.listMater.get(i).hasProperty(PropertyKey.DUST))
            {
                buid.input(dust,DrtechUtils.listMater.get(i),1);
                copybuild.output(dust,DrtechUtils.listMater.get(i),1);
            }
            else if(DrtechUtils.listMater.get(i).hasFluid())
            {
                buid.fluidInputs(DrtechUtils.listMater.get(i).getFluid(144));
                copybuild.fluidOutputs(DrtechUtils.listMater.get(i).getFluid(144));
            }
            else
                continue;
            buid.buildAndRegister();
            copybuild.buildAndRegister();
            SCANNER_RECIPES.recipeBuilder()
                    .input(MyMetaItems.CD_ROM)
                    .notConsumable(is)
                    .outputs(is)
                    .duration(100)
                    .EUt(30)
                    .buildAndRegister();
        }
        for (int i = 0; i < 4; i++) {
            DrtechUtils.addLogCreate(30,100,1,i);
        }
        for (int i = 0; i < 2; i++) {
            DrtechUtils.addLog2Create(30,100,1,i);
        }
        LOG_CREATE.recipeBuilder()
                .notConsumable(new ItemStack(MetaBlocks.RUBBER_SAPLING,1))
                .output(MetaBlocks.RUBBER_LOG)
                .EUt(30)
                .duration(100)
                .buildAndRegister();
        MOLECULAR_RECOMBINATION.recipeBuilder()
                .input(OrePrefix.dust,Carbon,1)
                .output(dust,Graphene,1)
                .EUt(7680)
                .duration(652)
                .buildAndRegister();
        MOLECULAR_RECOMBINATION.recipeBuilder()
                .input(OrePrefix.dust,Tin,1)
                .output(dust,Silver,1)
                .EUt(7680)
                .duration(326)
                .buildAndRegister();
        MOLECULAR_RECOMBINATION.recipeBuilder()
                .input(OrePrefix.dust,Copper,1)
                .output(dust,Nickel,1)
                .EUt(7680)
                .duration(326)
                .buildAndRegister();
        MOLECULAR_RECOMBINATION.recipeBuilder()
                .input(Items.REDSTONE)
                .output(dust,Ruby,1)
                .EUt(7680)
                .duration(326)
                .buildAndRegister();
        MOLECULAR_RECOMBINATION.recipeBuilder()
                .input(Items.COAL)
                .output(Items.DIAMOND)
                .EUt(7680)
                .duration(652)
                .buildAndRegister();
        if (Loader.isModLoaded(GtqtCoreLinkage.GTQTCORE_ID))
        {
            GtqtCoreLinkage.MachineRecipeInit();
        }
        if (Loader.isModLoaded(GTFOLinkage.GTFO_ID))
        {
            GTFOLinkage.MachineRecipeInit();
        }
        DRRP_GROUND_PUMP.recipeBuilder()
                .fluidInputs(new FluidStack(FluidRegistry.WATER, 1000))
                .fluidOutputs(new FluidStack(Steam.getFluid(), 1000))
                .EUt(512)
                .duration(20)
                .buildAndRegister();
        ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.plate, Polyethylene)
                .input(dust, Silver)
                .fluidInputs(Glue.getFluid(144))
                .output(MyMetaItems.CD_ROM)
                .EUt(30)
                .duration(200)
                .buildAndRegister();
        FLUID_SOLIDFICATION_RECIPES.recipeBuilder()
                .input(OrePrefix.block, BorosilicateGlass)
                .fluidInputs(Titanium.getFluid(576))
                .outputs(new ItemStack(BlocksInit.TRANSPARENT_CASING,1,0))
                .EUt(480)
                .duration(200)
                .buildAndRegister();
        FLUID_SOLIDFICATION_RECIPES.recipeBuilder()
                .input(OrePrefix.block, BorosilicateGlass)
                .fluidInputs(Tungsten.getFluid(576))
                .outputs(new ItemStack(BlocksInit.TRANSPARENT_CASING,1,1))
                .EUt(1920)
                .duration(200)
                .buildAndRegister();
        FLUID_SOLIDFICATION_RECIPES.recipeBuilder()
                .input(OrePrefix.block, BorosilicateGlass)
                .fluidInputs(Chrome.getFluid(576))
                .outputs(new ItemStack(BlocksInit.TRANSPARENT_CASING,1,3))
                .EUt(7680)
                .duration(200)
                .buildAndRegister();
        FLUID_SOLIDFICATION_RECIPES.recipeBuilder()
            .input(OrePrefix.block, BorosilicateGlass)
            .fluidInputs(Iridium.getFluid(576))
                .outputs(new ItemStack(BlocksInit.TRANSPARENT_CASING,1,4))
            .EUt(30720)
            .duration(200)
            .buildAndRegister();
        FLUID_SOLIDFICATION_RECIPES.recipeBuilder()
                .input(OrePrefix.block, BorosilicateGlass)
                .fluidInputs(Osmium.getFluid(576))
                .outputs(new ItemStack(BlocksInit.TRANSPARENT_CASING,1,5))
                .EUt(122880)
                .duration(200)
                .buildAndRegister();
        FLUID_SOLIDFICATION_RECIPES.recipeBuilder()
                .input(OrePrefix.block, BorosilicateGlass)
                .fluidInputs(Neutronium.getFluid(576))
                .outputs(new ItemStack(BlocksInit.TRANSPARENT_CASING,1,6))
                .EUt(491520)
                .duration(200)
                .buildAndRegister();
        FLUID_SOLIDFICATION_RECIPES.recipeBuilder()
                .fluidInputs(UUMatter.getFluid(1000))
                .output(MyMetaItems.UU_MATER)
                .EUt(16)
                .duration(10)
                .buildAndRegister();
        EXTRACTOR_RECIPES.recipeBuilder()
                .fluidOutputs(UUMatter.getFluid(1000))
                .input(MyMetaItems.UU_MATER)
                .EUt(16)
                .duration(10)
                .buildAndRegister();
        ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.frameGt, Wood)
                .input(OrePrefix.pipeTinyFluid,Steel,2)
                .input(MetaItems.VOLTAGE_COIL_MV)
                .input(MetaItems.PLANT_BALL,4)
                .input(OrePrefix.plank, Wood,3)
                .outputs(new ItemStack(BlocksInit.COMMON_CASING,1,8))
                .circuitMeta(4)
                .duration(100)
                .EUt(64)
                .buildAndRegister();
        ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.frameGt,Steel)
                .input(OrePrefix.plate, EnderPearl,4)
                .input(OrePrefix.plate, Steel,4)
                .circuitMeta(6)
                .fluidInputs(Polytetrafluoroethylene.getFluid(144))
                .outputs(new ItemStack(BlocksInit.COMMON_CASING,1,10))
                .EUt(480)
                .duration(100)
                .buildAndRegister();
        ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.frameGt,StainlessSteel)
                .input(OrePrefix.plate, EnderPearl,4)
                .input(OrePrefix.plate, StainlessSteel,4)
                .circuitMeta(3)
                .fluidInputs(Polytetrafluoroethylene.getFluid(144))
                .outputs(new ItemStack(BlocksInit.COMMON_CASING,1,9))
                .EUt(480)
                .duration(100)
                .buildAndRegister();
        ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.plate, Polyethylene)
                .input(Items.PAPER)
                .output(MyMetaItems.POS_CARD)
                .circuitMeta(24)
                .EUt(16)
                .duration(100)
                .buildAndRegister();
        ASSEMBLER_RECIPES.recipeBuilder()
                .input(Blocks.GOLD_BLOCK,64)
                .output(MyMetaItems.GOLD_COIN)
                .EUt(16)
                .duration(300)
                .buildAndRegister();
        SOLAR_TOWER.recipeBuilder()
                .fluidInputs(DrtechMaterials.ColdSunSalt.getFluid(1000))
                .fluidOutputs(DrtechMaterials.HotSunSalt.getFluid(1000))
                .EUt(1)
                .duration(200)
                .buildAndRegister();
    }
    public static void processDust(OrePrefix dustPrefix, Material mat, DustProperty property)
    {
        ItemStack dustStack = OreDictUnifier.get(dustPrefix, mat);
        if (mat.hasProperty(PropertyKey.GEM))
        {
            ItemStack gemStack = OreDictUnifier.get(OrePrefix.gem, mat);
            if (!mat.hasFlag(EXPLOSIVE) && !mat.hasFlag(FLAMMABLE)) {
               EIMPLOSION_RECIPES.recipeBuilder()
                        .inputs(GTUtility.copy(3, dustStack))
                        .outputs(GTUtility.copy(3, gemStack))
                        .chancedOutput(dust, Materials.DarkAsh, 2500, 0)
                       .explosivesAmount(dustStack)
                        .buildAndRegister();
            }
        }
    }
}
