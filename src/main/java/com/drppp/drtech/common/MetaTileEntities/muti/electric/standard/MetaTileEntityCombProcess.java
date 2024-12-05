package com.drppp.drtech.common.MetaTileEntities.muti.electric.standard;

import com.drppp.drtech.loaders.DrtechReceipes;
import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;
import gregtech.api.block.IHeatingCoilBlockStats;
import gregtech.api.capability.impl.MultiblockRecipeLogic;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.MultiblockShapeInfo;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.unification.material.Materials;
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

import static com.drppp.drtech.common.MetaTileEntities.MetaTileEntities.LARGE_ALLOY_SMELTER;
import static gregtech.api.util.RelativeDirection.*;

public class MetaTileEntityCombProcess extends RecipeMapMultiblockController {
    public MetaTileEntityCombProcess(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, DrtechReceipes.COMBS_PRODUCT);
        this.recipeMapWorkable = new SelfRecipeLogic(this, true);
    }

    @Override
    protected @NotNull BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start(RIGHT, UP,BACK )
                .aisle("AAAAA", "B   B", "B   B", "B   B", "AAAAA")
                .aisle("AAAAA", " CCC ", " BBB ", " CCC ", "AAAAA")
                .aisle("AAAAA", " CCC ", " BBB ", " CCC ", "AAAAA")
                .aisle("AAAAA", " CCC ", " BBB ", " CCC ", "AAAAA")
                .aisle("AASAA", "B   B", "B   B", "B   B", "AAAAA")
                .where('S', selfPredicate())
                .where('C',states(MetaBlocks.BOILER_CASING.getState(BlockBoilerCasing.BoilerCasingType.TITANIUM_PIPE)))
                .where('A', states(getCasingState()).setMinGlobalLimited(20)
                        .or(autoAbilities(true, true, true, true, true, false, false)))
                .where('B', frames(Materials.StainlessSteel))
                .where('M', abilities(MultiblockAbility.MUFFLER_HATCH))
                .where(' ', any())
                .build();
    }


    private static IBlockState getCasingState() {
        return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STAINLESS_CLEAN);
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
        public void setParallelLimit(int amount) {
            super.setParallelLimit(amount);
        }

        @Override
        public int getParallelLimit() {
            int tire = 1;
            for (int i = 0; i < GTValues.V.length; i++) {
                if(GTValues.V[i]==this.getMaxVoltage())
                    tire = i;
            }
            return (tire)*2;
        }
    }
}
