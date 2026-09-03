package com.drppp.drtech.common.metaTileEntities.muti.electric.standard;


import gregtech.api.GTValues;
import gregtech.api.capability.impl.MultiblockRecipeLogic;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.pattern.FormedStructureView;
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

import gregtech.api.pattern.casing.DeclarativePatternBuilder;

import gregtech.api.pattern.element.Elements;

import gregtech.api.pattern.element.StructureDefinition;

public class MetaTileEntityCombProcess extends RecipeMapMultiblockController {
    public MetaTileEntityCombProcess(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, RecipeMaps.COMBS_PRODUCT);
        this.recipeMapWorkable = new SelfRecipeLogic(this, true);
    }

    private static IBlockState getCasingState() {
        return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STAINLESS_CLEAN);
    }

    private static final StructureDefinition<?> STRUCTURE_DEFINITION =
            StructureDefinition.getOrBuild("drtech:comb_process",
                    MetaTileEntityCombProcess::buildTemplate);

    @Override
    protected @NotNull StructureDefinition<?> createStructureDefinition() {
        return STRUCTURE_DEFINITION;
    }

    private static StructureDefinition<?> buildTemplate() {
        return DeclarativePatternBuilder.start(RIGHT, UP, BACK)
                .aisle("AAAAA", "B   B", "B   B", "B   B", "AAAAA")
                .aisle("AAAAA", " CCC ", " BBB ", " CCC ", "AAAAA")
                .aisle("AAAAA", " CCC ", " BBB ", " CCC ", "AAAAA")
                .aisle("AAAAA", " CCC ", " BBB ", " CCC ", "AAAAA")
                .aisle("AASAA", "B   B", "B   B", "B   B", "AAAAA")
                .self('S', MetaTileEntityCombProcess.class)
                .blocks('C', MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.PTFE_INERT_CASING))
                .hatch('M', MultiblockAbility.MUFFLER_HATCH)
                .frames('B', Materials.StainlessSteel)
                .where('A', Elements.chain(
                        Elements.counted(20, 4096, Elements.block(getCasingState())),
                        Elements.hatch(MultiblockAbility.MAINTENANCE_HATCH,
                                gregtech.common.ConfigHolder.machines.enableMaintenance ? 1 : 0, 1),
                        Elements.hatch(MultiblockAbility.INPUT_ENERGY, 1, 2, 1),
                        Elements.hatch(MultiblockAbility.IMPORT_ITEMS, 0, -1, 1),
                        Elements.hatch(MultiblockAbility.EXPORT_ITEMS, 0, -1, 1),
                        Elements.hatch(MultiblockAbility.IMPORT_FLUIDS, 0, -1, 1)))
                .buildStructureDefinition();

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
    protected void formStructure(@NotNull FormedStructureView formed) {
        super.formStructure(formed);
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
}
