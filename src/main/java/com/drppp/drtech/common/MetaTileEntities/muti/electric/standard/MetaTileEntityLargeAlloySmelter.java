package com.drppp.drtech.common.MetaTileEntities.muti.electric.standard;


import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;
import gregtech.api.block.IHeatingCoilBlockStats;
import gregtech.api.capability.impl.MultiblockRecipeLogic;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.pattern.MultiblockShapeInfo;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.recipes.RecipeMaps;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.utils.TooltipHelper;
import gregtech.common.blocks.BlockBoilerCasing;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.metatileentities.MetaTileEntities;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static com.drppp.drtech.common.MetaTileEntities.DrTechMetaTileEntities.LARGE_ALLOY_SMELTER;
import static gregtech.api.util.RelativeDirection.*;

import gregtech.api.pattern.BlockPatternTemplate;

import gregtech.api.pattern.SoftTemplate;

import gregtech.api.pattern.TemplatePool;

import gregtech.api.pattern.casing.DeclarativePatternBuilder;

import gregtech.api.pattern.casing.GTCasingGroups;

import gregtech.api.pattern.casing.ICasing;

import gregtech.api.pattern.TraceabilityPredicate;

public class MetaTileEntityLargeAlloySmelter extends RecipeMapMultiblockController {
    private int leve = 1;

    public MetaTileEntityLargeAlloySmelter(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, RecipeMaps.ALLOY_SMELTER_RECIPES);
        this.recipeMapWorkable = new SelfRecipeLogic(this, true);
    }

    private static final SoftTemplate TEMPLATE = TemplatePool.getInstance().register(
            "drtech:large_alloy_smelter",
            MetaTileEntityLargeAlloySmelter::buildTemplate
    );

    @Override
    protected @NotNull BlockPatternTemplate createStructureTemplate() {
        return TEMPLATE.get();
    }

    private static BlockPatternTemplate buildTemplate() {
        return DeclarativePatternBuilder.start(RIGHT, UP, BACK)
                .aisle(" AAA ", " BBB ", " CCC ", " BBB ", " AAA ")
                .aisle("AAAAA", "B   B", "C   C", "B   B", "AAAAA")
                .aisle("AAAAA", "B   B", "C   C", "B   B", "AAMAA")
                .aisle("AAAAA", "B   B", "C   C", "B   B", "AAAAA")
                .aisle(" ASA ", " BBB ", " CCC ", " BBB ", " AAA ")
                .where('S', selfPredicate(MetaTileEntityLargeAlloySmelter.class))
                .where('C', states(MetaBlocks.BOILER_CASING.getState(BlockBoilerCasing.BoilerCasingType.STEEL_PIPE)))
                .where('A', states(getCasingState()).setMinGlobalLimited(20)
                        .or(staticRecipeMapAutoAbilities(true, true, true, true, false, false, false)))
                .tieredCasing('B', GTCasingGroups.heatingCoils().group())
                .withChannel(GTCasingGroups.heatingCoils().channel())
                .where('M', abilities(MultiblockAbility.MUFFLER_HATCH))
                .where(' ', any())
                .buildTemplate();

    }

    @Override
    public List<MultiblockShapeInfo> getMatchingShapes() {
        ArrayList<MultiblockShapeInfo> shapeInfo = new ArrayList<>();
        MultiblockShapeInfo.Builder builder = MultiblockShapeInfo.builder()
                .aisle("#XEX#", "#CCC#", "#GGG#", "#CCC#", "#XXX#")
                .aisle("XXXXX", "C###C", "G###G", "C###C", "XXXXX")
                .aisle("XXXXX", "C###C", "G###G", "C###C", "XXMXX")
                .aisle("XXXXH", "C###C", "G###G", "C###C", "XXXXX")
                .aisle("#ISO#", "#CCC#", "#GGG#", "#CCC#", "#XXX#")
                .where('S', LARGE_ALLOY_SMELTER, EnumFacing.SOUTH)
                .where('X', getCasingState())
                .where('G', MetaBlocks.BOILER_CASING.getState(BlockBoilerCasing.BoilerCasingType.STEEL_PIPE))
                .where('M', MetaTileEntities.MUFFLER_HATCH[GTValues.HV], EnumFacing.UP)
                .where('I', MetaTileEntities.ITEM_IMPORT_BUS[GTValues.HV], EnumFacing.SOUTH)
                .where('O', MetaTileEntities.ITEM_EXPORT_BUS[GTValues.HV], EnumFacing.SOUTH)
                .where('E', MetaTileEntities.ENERGY_INPUT_HATCH[GTValues.EV], EnumFacing.NORTH)
                .where('H', MetaTileEntities.MAINTENANCE_HATCH, EnumFacing.SOUTH)
                .where('#', Blocks.AIR.getDefaultState());
        GregTechAPI.HEATING_COILS.entrySet().stream()
                .sorted(Comparator.comparingInt(entry -> entry.getValue().getTier()))
                .forEach(entry -> shapeInfo.add(builder.where('C', entry.getKey()).build()));
        return shapeInfo;
    }

    protected static IBlockState getCasingState() {
        return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.INVAR_HEATPROOF);
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return Textures.HEAT_PROOF_CASING;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityLargeAlloySmelter(this.metaTileEntityId);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, @NotNull List<String> tooltip,
                               boolean advanced) {
        super.addInformation(stack, world, tooltip, advanced);
        tooltip.add(I18n.format("drtech.machine.large_alloy_smelter.tooltip.1"));
        tooltip.add(I18n.format("drtech.machine.large_alloy_smelter.tooltip.2"));
        tooltip.add(I18n.format("drtech.machine.large_alloy_smelter.tooltip.3"));
        tooltip.add(TooltipHelper.RAINBOW_SLOW + I18n.format("gregtech.machine.perfect_oc"));
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        ICasing matchedCoil = GTCasingGroups.heatingCoils().channel().getMatchedCasing(context);
        IHeatingCoilBlockStats type = matchedCoil == null ? null :
                matchedCoil.getPayloadAs(IHeatingCoilBlockStats.class);
        this.leve = type == null ? 1 : type.getLevel();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setInteger("CoinLevel", this.leve);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.leve = data.getInteger("CoinLevel");
    }




    protected class SelfRecipeLogic extends MultiblockRecipeLogic {
        public SelfRecipeLogic(RecipeMapMultiblockController tileEntity, boolean hasPerfectOC) {
            super(tileEntity, hasPerfectOC);
        }

        @Override
        public void setMaxProgress(int maxProgress) {
            this.maxProgressTime = (int) (maxProgress * 0.5);
        }

        @Override
        public int getParallelLimit() {
            int tire = 1;
            for (int i = 0; i < GTValues.V.length; i++) {
                if (GTValues.V[i] == this.getMaxVoltage())
                    tire = i;
            }
            return (tire + leve - 1) * 2;
        }
    }
    private static TraceabilityPredicate staticDisplayAutoAbilities(boolean maintenance, boolean muffler) {
        TraceabilityPredicate predicate = new TraceabilityPredicate();
        if (maintenance && true) {
            predicate = predicate.or(abilities(MultiblockAbility.MAINTENANCE_HATCH)
                    .setMinGlobalLimited(gregtech.common.ConfigHolder.machines.enableMaintenance ? 1 : 0)
                    .setMaxGlobalLimited(1));
        }
        if (muffler) {
            predicate = predicate.or(abilities(MultiblockAbility.MUFFLER_HATCH)
                    .setMinGlobalLimited(1)
                    .setMaxGlobalLimited(1));
        }
        return predicate;
    }
    private static TraceabilityPredicate staticRecipeMapAutoAbilities(boolean energyIn,
                                                                      boolean maintenance,
                                                                      boolean itemIn,
                                                                      boolean itemOut,
                                                                      boolean fluidIn,
                                                                      boolean fluidOut,
                                                                      boolean muffler) {
        return staticRecipeMapAutoAbilities(energyIn, maintenance, itemIn, itemOut, fluidIn, fluidOut, muffler, 2);
    }

    private static TraceabilityPredicate staticRecipeMapAutoAbilities(boolean energyIn,
                                                                      boolean maintenance,
                                                                      boolean itemIn,
                                                                      boolean itemOut,
                                                                      boolean fluidIn,
                                                                      boolean fluidOut,
                                                                      boolean muffler,
                                                                      int maxEnergyInputs) {
        TraceabilityPredicate predicate = staticDisplayAutoAbilities(maintenance, muffler);
        if (energyIn) {
            predicate = predicate.or(abilities(MultiblockAbility.INPUT_ENERGY)
                    .setMinGlobalLimited(1)
                    .setMaxGlobalLimited(maxEnergyInputs)
                    .setPreviewCount(1));
        }
        if (itemIn) {
            predicate = predicate.or(abilities(MultiblockAbility.IMPORT_ITEMS).setPreviewCount(1));
        }
        if (itemOut) {
            predicate = predicate.or(abilities(MultiblockAbility.EXPORT_ITEMS).setPreviewCount(1));
        }
        if (fluidIn) {
            predicate = predicate.or(abilities(MultiblockAbility.IMPORT_FLUIDS).setPreviewCount(1));
        }
        if (fluidOut) {
            predicate = predicate.or(abilities(MultiblockAbility.EXPORT_FLUIDS).setPreviewCount(1));
        }
        return predicate;
    }
}
