package com.drppp.drtech.MetaTileEntities.muti.ecectric.standard;

import com.drppp.drtech.Blocks.BlocksInit;
import com.drppp.drtech.Blocks.MetaBlocks.MetaCasing;
import com.drppp.drtech.Blocks.MetaBlocks.MetaGlasses1;
import com.drppp.drtech.Client.Textures;
import com.drppp.drtech.Load.DrtechReceipes;
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

public class MetaTileEntityLargeElementDuplicator extends RecipeMapMultiblockController {

    public MetaTileEntityLargeElementDuplicator(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, DrtechReceipes.COPY_RECIPES);
        this.recipeMapWorkable = new DuplicatorRecipeLogic(this, true);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityLargeElementDuplicator(metaTileEntityId);
    }

    @Override
    protected BlockPattern createStructurePattern() {
        TraceabilityPredicate abilities = autoAbilities();
        return FactoryBlockPattern.start()
                .aisle("###XXX###", "##XXXXX##", "#XXXXXXX#", "XXXXXXXXX","XXXXXXXXX","XXXXXXXXX","#XXXXXXX#", "##XXXXX##", "###XXX###")
                .aisle("###XWX###", "##WQZQW##", "#WQZTZQW#", "XQZTYTZQX","WQZYYYZQW","XQZTYTZQX","#WQZTZQW#", "##WQZQW##", "###XWX###")
                .aisle("###XGX###", "##G###G##", "#G#####G#", "X###Y###X","G##YYY##G","X###Y###X","#G#####G#", "##G###G##", "###XGX###")
                .aisle("###XGX###", "##G###G##", "#G#####G#", "X###Y###X","G##YYY##G","X###Y###X","#G#####G#", "##G###G##", "###XGX###")
                .aisle("###XWX###", "##WQZQW##", "#WQZTZQW#", "XQZTYTZQX","WQZYYYZQW","XQZTYTZQX","#WQZTZQW#", "##WQZQW##", "###XWX###")
                .aisle("###XXX###", "##XXXXX##", "#XXXXXXX#", "XXXXXXXXX","XXXXSXXXX","XXXXXXXXX","#XXXXXXX#", "##XXXXX##", "###XXX###")
                .where('S', selfPredicate())
                .where('C', states(BlocksInit.COMMON_CASING.getState(MetaCasing.MetalCasingType.MASS_GENERATION_COIL_CASING)))
                .where('G', states(BlocksInit.TRANSPARENT_CASING1.getState(MetaGlasses1.CasingType.COPY_GALSS)))
                .where('X', states(getCasingState()).setMinGlobalLimited(138).or(abilities))
                .where('W', states(getCasingState1()))
                .where('Q', states(getCasingState2()))
                .where('Z', states(BlocksInit.COMMON_CASING.getState(MetaCasing.MetalCasingType.RESONATOR_CASING)))
                .where('T', states(BlocksInit.COMMON_CASING.getState(MetaCasing.MetalCasingType.BUNCHER_CASING)))
                .where('Y', states(BlocksInit.COMMON_CASING.getState(MetaCasing.MetalCasingType.HIGH_VOLTAGE_CAPACITOR_BLOCK_CASING)))
                .where('#', air())
                .build();
    }

    @SideOnly(Side.CLIENT)
    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return Textures.ELEMENT_CONSTRAINS_MACHINE_CASING;
    }

    protected IBlockState getCasingState() {
        return BlocksInit.COMMON_CASING.getState(MetaCasing.MetalCasingType.ELEMENT_CONSTRAINS_MACHINE_CASING);
    }
    protected IBlockState getCasingState1() {
        return BlocksInit.COMMON_CASING.getState(MetaCasing.MetalCasingType.MASS_GENERATION_CASING);
    }
    protected IBlockState getCasingState2() {
        return BlocksInit.COMMON_CASING.getState(MetaCasing.MetalCasingType.MASS_GENERATION_COIL_CASING);
    }
    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(I18n.format("drtech.machine.duplicator.tip.1"));
        tooltip.add(I18n.format("drtech.machine.duplicator.tip.2"));
        tooltip.add(I18n.format("drtech.machine.duplicator.tip.3"));
        tooltip.add(TooltipHelper.RAINBOW_SLOW + I18n.format("gregtech.machine.perfect_oc"));
    }

    @SideOnly(Side.CLIENT)
    @NotNull
    @Override
    protected ICubeRenderer getFrontOverlay() {
        return Textures.LARGE_UU_PRODUCTER;
    }
    protected class DuplicatorRecipeLogic extends MultiblockRecipeLogic{

        public DuplicatorRecipeLogic(RecipeMapMultiblockController tileEntity, boolean hasPerfectOC) {
            super(tileEntity, hasPerfectOC);
        }

        @Override
        public void setMaxProgress(int maxProgress) {
            this.maxProgressTime = maxProgress / 2;
        }

        @Override
        protected boolean drawEnergy(int recipeEUt, boolean simulate) {
            long resultEnergy = this.getEnergyStored() - (long)recipeEUt;
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