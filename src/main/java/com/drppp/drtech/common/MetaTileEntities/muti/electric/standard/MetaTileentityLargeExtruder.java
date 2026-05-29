package com.drppp.drtech.common.MetaTileEntities.muti.electric.standard;


import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import gregtech.api.GTValues;
import gregtech.api.capability.impl.MultiblockRecipeLogic;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.recipes.RecipeMaps;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockBoilerCasing;
import gregtech.common.blocks.BlockGlassCasing;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import gregtech.api.pattern.BlockPatternTemplate;

import gregtech.api.pattern.SoftTemplate;

import gregtech.api.pattern.TemplatePool;

import gregtech.api.pattern.casing.DeclarativePatternBuilder;

import gregtech.api.pattern.TraceabilityPredicate;

import gregtech.api.metatileentity.multiblock.MultiblockAbility;

public class MetaTileentityLargeExtruder extends RecipeMapMultiblockController {

    public MetaTileentityLargeExtruder(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, RecipeMaps.EXTRUDER_RECIPES);
        this.recipeMapWorkable = new SelfRecipeLogic(this, true);
    }

    private static IBlockState getCasingState() {
        return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.TUNGSTENSTEEL_ROBUST);
    }

    private static IBlockState getCasingState2() {
        return MetaBlocks.BOILER_CASING.getState(BlockBoilerCasing.BoilerCasingType.TITANIUM_PIPE);
    }

    private static IBlockState getCasingState3() {
        return MetaBlocks.TRANSPARENT_CASING.getState(BlockGlassCasing.CasingType.TEMPERED_GLASS);
    }

    private static final SoftTemplate TEMPLATE = TemplatePool.getInstance().register(
            "drtech:large_extruder",
            MetaTileentityLargeExtruder::buildTemplate
    );

    @Override
    protected @NotNull BlockPatternTemplate createStructureTemplate() {
        return TEMPLATE.get();
    }

    private static BlockPatternTemplate buildTemplate() {
        return DeclarativePatternBuilder.start()
                .aisle("##XXX", "##XXX", "##XXX")
                .aisleRepeatable(2, 2, "##XXX", "##XPX", "##XGX")
                .aisle("XXXXX", "XXXPX", "XXXGX")
                .aisle("XXXXX", "XXXPX", "XXXGX")
                .aisle("XXXXX", "XSXXX", "XXXXX")
                .where('S', selfPredicate(MetaTileentityLargeExtruder.class))
                .where('X', states(getCasingState()).setMinGlobalLimited(40).or(staticRecipeMapAutoAbilities(true, true, true, true, false, false, true)))
                .where('P', states(getCasingState2()))
                .where('G', states(getCasingState3()))
                .where('#', any())
                .buildTemplate();

    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileentityLargeExtruder(this.metaTileEntityId);
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return Textures.ROBUST_TUNGSTENSTEEL_CASING;
    }

    @Override
    public boolean canBeDistinct() {
        return true;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, @NotNull List<String> tooltip,
                               boolean advanced) {
        super.addInformation(stack, world, tooltip, advanced);
        tooltip.add(I18n.format("drtech.machine.large_extruder.tooltip.1"));
        tooltip.add(I18n.format("drtech.machine.large_extruder.tooltip.2"));
        tooltip.add(I18n.format("drtech.machine.large_extruder.tooltip.3"));
    }




    protected class SelfRecipeLogic extends MultiblockRecipeLogic {
        public SelfRecipeLogic(RecipeMapMultiblockController tileEntity, boolean hasPerfectOC) {
            super(tileEntity, hasPerfectOC);
        }

        @Override
        public void setMaxProgress(int maxProgress) {
            this.maxProgressTime = (int) Math.ceil(maxProgress * 0.25);
        }

        @Override
        public int getParallelLimit() {
            int tire = 1;
            for (int i = 0; i < GTValues.V.length; i++) {
                if (GTValues.V[i] == this.getMaxVoltage())
                    tire = i;
            }
            if (tire > GTValues.UV)
                return tire * 8;
            return tire * 4;
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
