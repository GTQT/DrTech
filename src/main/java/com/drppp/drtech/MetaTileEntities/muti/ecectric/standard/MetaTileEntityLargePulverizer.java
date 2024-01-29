package com.drppp.drtech.MetaTileEntities.muti.ecectric.standard;

import codechicken.lib.raytracer.IndexedCuboid6;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.vec.Matrix4;
import gregtech.api.capability.impl.HeatingCoilRecipeLogic;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.recipes.RecipeMap;
import gregtech.client.renderer.ICubeRenderer;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.common.capabilities.Capability;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MetaTileEntityLargePulverizer extends RecipeMapMultiblockController {
    public MetaTileEntityLargePulverizer(ResourceLocation metaTileEntityId, RecipeMap<?> recipeMap) {
        super(metaTileEntityId, recipeMap);
    }

    @Override
    protected @NotNull BlockPattern createStructurePattern() {
        return null;
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return null;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return null;
    }

    @Override
    public void dropAllCovers() {
        super.dropAllCovers();
    }

    @Override
    public void dropCover(@NotNull EnumFacing side) {
        super.dropCover(side);
    }

    @Override
    public void updateCovers() {
        super.updateCovers();
    }

    @Override
    public void renderCovers(@NotNull CCRenderState renderState, @NotNull Matrix4 translation, @NotNull BlockRenderLayer layer) {
        super.renderCovers(renderState, translation, layer);
    }

    @Override
    public void addCoverCollisionBoundingBox(@NotNull List<? super IndexedCuboid6> collisionList) {
        super.addCoverCollisionBoundingBox(collisionList);
    }

    @Override
    public boolean hasCapability(@NotNull Capability<?> capability, @Nullable EnumFacing facing) {
        return super.hasCapability(capability, facing);
    }

    @Override
    public boolean hasCover(@NotNull EnumFacing side) {
        return super.hasCover(side);
    }

    @Override
    public int getItemOutputLimit() {
        return super.getItemOutputLimit();
    }

    @Override
    public int getFluidOutputLimit() {
        return super.getFluidOutputLimit();
    }

    @Override
    public SoundEvent getBreakdownSound() {
        return super.getBreakdownSound();
    }
    protected class LargePulverizerLogic {

    }

}

