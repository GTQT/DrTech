package com.drppp.drtech.common.MetaTileEntities.muti.electric.standard;

import com.drppp.drtech.common.Blocks.BlocksInit;
import com.drppp.drtech.common.Blocks.MetaBlocks.MetaCasing;
import com.drppp.drtech.common.Blocks.MetaBlocks.MetaGlasses1;
import com.drppp.drtech.Client.Textures;
import com.drppp.drtech.loaders.DrtechReceipes;
import gregtech.api.GTValues;
import gregtech.api.capability.impl.MultiblockRecipeLogic;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.TraceabilityPredicate;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.utils.TooltipHelper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MetaTileEntityLargeUUProducter extends RecipeMapMultiblockController {

    public MetaTileEntityLargeUUProducter(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, DrtechReceipes.UU_RECIPES);
        this.recipeMapWorkable = new UUProducterRecipeLogic(this, true);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityLargeUUProducter(metaTileEntityId);
    }

    @Override
    protected BlockPattern createStructurePattern() {
        TraceabilityPredicate abilities = autoAbilities();
        return FactoryBlockPattern.start()
                .aisle("XXXXX", "XGGGX", "XGGGX", "XXXXX")
                .aisle("XCCCX", "G###G", "G###G", "XXXXX")
                .aisle("XCCCX", "G###G", "G###G", "XXXXX")
                .aisle("XCCCX", "G###G", "G###G", "XXXXX")
                .aisle("XXSXX", "XGGGX", "XGGGX", "XXXXX")
                .where('S', selfPredicate())
                .where('C', states(BlocksInit.COMMON_CASING.getState(MetaCasing.MetalCasingType.MASS_GENERATION_COIL_CASING)))
                .where('G', states(BlocksInit.TRANSPARENT_CASING1.getState(MetaGlasses1.CasingType.UU_GALSS)))
                .where('X', states(getCasingState()).setMinGlobalLimited(25).or(abilities))
                .where('#', air())
                .build();
    }

    @SideOnly(Side.CLIENT)
    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return Textures.MASS_GENERATION_CASING;
    }

    protected IBlockState getCasingState() {
        return BlocksInit.COMMON_CASING.getState(MetaCasing.MetalCasingType.MASS_GENERATION_CASING);
    }


    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(I18n.format("drtech.machine.uuproducter.tip.1"));
        tooltip.add(I18n.format("drtech.machine.uuproducter.tip.2"));
        tooltip.add(I18n.format("drtech.machine.uuproducter.tip.3"));
        tooltip.add(TooltipHelper.RAINBOW_SLOW + I18n.format("gregtech.machine.perfect_oc"));
    }

    @SideOnly(Side.CLIENT)
    @NotNull
    @Override
    protected ICubeRenderer getFrontOverlay() {
        return Textures.LARGE_UU_PRODUCTER;
    }
    protected class UUProducterRecipeLogic extends MultiblockRecipeLogic{


        public UUProducterRecipeLogic(RecipeMapMultiblockController tileEntity) {
            super(tileEntity);
        }

        public UUProducterRecipeLogic(RecipeMapMultiblockController tileEntity, boolean hasPerfectOC) {
            super(tileEntity, hasPerfectOC);
        }
        @Override
        protected boolean drawEnergy(int recipeEUt, boolean simulate) {
            long resultEnergy = this.getEnergyStored() - (long)recipeEUt/2;
            if (resultEnergy >= 0L && resultEnergy <= this.getEnergyCapacity()) {
                if (!simulate) {
                    this.getEnergyContainer().changeEnergy((long)(-recipeEUt));
                }

                return true;
            } else {
                return false;
            }
        }
        @Override
        public int getParallelLimit() {
            int tire = 1;
            for (int i = 0; i < GTValues.V.length; i++) {
                if(GTValues.V[i]==this.getMaxVoltage())
                    tire = i;
            }

            return tire*8;
        }
    }
}