package com.drppp.drtech.common.MetaTileEntities.muti.electric.standard;


import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.drppp.drtech.loaders.recipes.DrtechReceipes;
import gregtech.api.GTValues;
import gregtech.api.capability.impl.MultiblockRecipeLogic;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.unification.material.Materials;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import static gregtech.api.util.RelativeDirection.*;

import gregtech.api.pattern.BlockPatternTemplate;

import gregtech.api.pattern.SoftTemplate;

import gregtech.api.pattern.TemplatePool;

import gregtech.api.pattern.casing.DeclarativePatternBuilder;

import gregtech.api.pattern.TraceabilityPredicate;

public class MetaTileEntityCombProcess extends RecipeMapMultiblockController {
    public MetaTileEntityCombProcess(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, RecipeMaps.COMBS_PRODUCT);
        this.recipeMapWorkable = new SelfRecipeLogic(this, true);
    }

    private static IBlockState getCasingState() {
        return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STAINLESS_CLEAN);
    }

    private static final SoftTemplate TEMPLATE = TemplatePool.getInstance().register(
            "drtech:comb_process",
            MetaTileEntityCombProcess::buildTemplate
    );

    @Override
    protected @NotNull BlockPatternTemplate createStructureTemplate() {
        return TEMPLATE.get();
    }

    private static BlockPatternTemplate buildTemplate() {
        return DeclarativePatternBuilder.start(RIGHT, UP, BACK)
                .aisle("AAAAA", "B   B", "B   B", "B   B", "AAAAA")
                .aisle("AAAAA", " CCC ", " BBB ", " CCC ", "AAAAA")
                .aisle("AAAAA", " CCC ", " BBB ", " CCC ", "AAAAA")
                .aisle("AAAAA", " CCC ", " BBB ", " CCC ", "AAAAA")
                .aisle("AASAA", "B   B", "B   B", "B   B", "AAAAA")
                .where('S', selfPredicate(MetaTileEntityCombProcess.class))
                .where('C', states(MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.PTFE_INERT_CASING)))
                .where('A', states(getCasingState()).setMinGlobalLimited(20)
                        .or(staticRecipeMapAutoAbilities(true, true, true, true, true, false, false)))
                .where('B', frames(Materials.StainlessSteel))
                .where('M', abilities(MultiblockAbility.MUFFLER_HATCH))
                .where(' ', any())
                .buildTemplate();

    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return Textures.CLEAN_STAINLESS_STEEL_CASING;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityCombProcess(this.metaTileEntityId);
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
    }




    protected class SelfRecipeLogic extends MultiblockRecipeLogic {
        public SelfRecipeLogic(RecipeMapMultiblockController tileEntity, boolean hasPerfectOC) {
            super(tileEntity, hasPerfectOC);
        }

        @Override
        public int getParallelLimit() {
            int tire = 1;
            for (int i = 0; i < GTValues.V.length; i++) {
                if (GTValues.V[i] == this.getMaxVoltage())
                    tire = i;
            }
            return (tire) * 2;
        }

        @Override
        public void setParallelLimit(int amount) {
            super.setParallelLimit(amount);
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
