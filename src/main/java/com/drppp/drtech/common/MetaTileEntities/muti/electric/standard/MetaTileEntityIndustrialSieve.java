package com.drppp.drtech.common.MetaTileEntities.muti.electric.standard;

import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.drppp.drtech.Client.Textures;
import com.drppp.drtech.common.Blocks.BlocksInit;
import com.drppp.drtech.common.Blocks.MetaBlocks.MetaCasing1;
import gregtech.api.GTValues;
import gregtech.api.capability.impl.MultiblockRecipeLogic;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.recipes.RecipeMaps;
import gregtech.client.renderer.ICubeRenderer;
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

import static gregtech.api.util.RelativeDirection.*;

public class MetaTileEntityIndustrialSieve extends RecipeMapMultiblockController {
    public MetaTileEntityIndustrialSieve(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, RecipeMaps.SIFTER_RECIPES);
        this.recipeMapWorkable = new SelfRecipeLogic(this, false);
    }

    @Override
    protected @NotNull BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start(RIGHT, UP, BACK)
                .aisle("XXXXX", "XXXXX", "XXXXX")
                .aisle("XXXXX", "XAAAX", "XAAAX")
                .aisle("XXXXX", "XAAAX", "XAAAX")
                .aisle("XXXXX", "XAAAX", "XAAAX")
                .aisle("XXXXX", "XXSXX", "XXXXX")
                .where('S', selfPredicate())
                .where('A', states(getCasingState1()))
                .where('X', states(getCasingState()).setMinGlobalLimited(35)
                        .or(autoAbilities(true, true, true, true, false, false, false))
                        .or(abilities(MultiblockAbility.MUFFLER_HATCH).setExactLimit(1))
                )
                .build();
    }


    protected IBlockState getCasingState() {
        return BlocksInit.COMMON_CASING1.getState(MetaCasing1.MetalCasingType.SIEVE_CASING);
    }

    protected IBlockState getCasingState1() {
        return BlocksInit.COMMON_CASING1.getState(MetaCasing1.MetalCasingType.SIEVE_NET_CASING);
    }

    @SideOnly(Side.CLIENT)
    @NotNull
    @Override
    protected ICubeRenderer getFrontOverlay() {
        return Textures.INDUSTRIAL_MACHINE;
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return Textures.SIEVE_CASING;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityIndustrialSieve(this.metaTileEntityId);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, @NotNull List<String> tooltip,
                               boolean advanced) {
        super.addInformation(stack, world, tooltip, advanced);
        tooltip.add(I18n.format("drtech.machine.industrial_sieve.tooltip.1"));
        tooltip.add(I18n.format("drtech.machine.industrial_sieve.tooltip.2"));
        tooltip.add(I18n.format("drtech.machine.industrial_sieve.tooltip.3"));
        tooltip.add(I18n.format("drtech.machine.industrial_sieve.tooltip.4"));
    }

    @Override
    public ModularPanel buildUI(PosGuiData data, PanelSyncManager syncManager, UISettings settings) {
        return null;
    }


    protected class SelfRecipeLogic extends MultiblockRecipeLogic {
        public SelfRecipeLogic(RecipeMapMultiblockController tileEntity, boolean hasPerfectOC) {
            super(tileEntity, hasPerfectOC);
        }

        @Override
        public void setMaxProgress(int maxProgress) {
            this.maxProgressTime = (int) Math.ceil(maxProgress * 0.2);
        }

        @Override
        protected boolean drawEnergy(long recipeEUt, boolean simulate) {
            return super.drawEnergy((long) (recipeEUt * 0.75), simulate);
        }

        @Override
        public int getParallelLimit() {
            int tire = 1;
            for (int i = 0; i < GTValues.V.length; i++) {
                if (GTValues.V[i] == this.getMaxVoltage())
                    tire = i;
            }
            return tire * 4;
        }
    }
}
