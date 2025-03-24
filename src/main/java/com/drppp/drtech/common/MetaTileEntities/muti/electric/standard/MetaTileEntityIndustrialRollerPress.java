package com.drppp.drtech.common.MetaTileEntities.muti.electric.standard;

import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.value.sync.GuiSyncManager;
import com.drppp.drtech.Client.Textures;
import com.drppp.drtech.common.Blocks.BlocksInit;
import com.drppp.drtech.common.Blocks.MetaBlocks.MetaCasing1;
import gregtech.api.GTValues;
import gregtech.api.capability.impl.MultiblockRecipeLogic;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiMapMultiblockController;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.recipes.RecipeMap;
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

public class MetaTileEntityIndustrialRollerPress extends MultiMapMultiblockController {
    public MetaTileEntityIndustrialRollerPress(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, new RecipeMap[]{RecipeMaps.BENDER_RECIPES, RecipeMaps.FORMING_PRESS_RECIPES});
        this.recipeMapWorkable = new SelfRecipeLogic(this, false);
    }

    @Override
    protected @NotNull BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start(RIGHT, UP, BACK)
                .aisle("XXX", "XXX", "XXX")
                .aisle("XXX", "X X", "XXX")
                .aisle("XXX", "XSX", "XXX")
                .where('S', selfPredicate())
                .where(' ', any())
                .where('X', states(getCasingState()).setMinGlobalLimited(6)
                        .or(autoAbilities(true, true, true, true, false, false, false))
                        .or(abilities(MultiblockAbility.MUFFLER_HATCH).setExactLimit(1))
                )
                .build();
    }

    @Override
    public boolean canBeDistinct() {
        return true;
    }

    protected IBlockState getCasingState() {
        return BlocksInit.COMMON_CASING1.getState(MetaCasing1.MetalCasingType.METAL_PRESS_CASING);
    }

    @SideOnly(Side.CLIENT)
    @NotNull
    @Override
    protected ICubeRenderer getFrontOverlay() {
        return Textures.INDUSTRIAL_MACHINE;
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return gregtech.client.renderer.texture.Textures.STABLE_TITANIUM_CASING;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityIndustrialRollerPress(this.metaTileEntityId);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, @NotNull List<String> tooltip,
                               boolean advanced) {
        super.addInformation(stack, world, tooltip, advanced);
        tooltip.add(I18n.format("drtech.machine.industrial_roller_press.tooltip.1"));
        tooltip.add(I18n.format("drtech.machine.industrial_roller_press.tooltip.2"));
        tooltip.add(I18n.format("drtech.machine.industrial_roller_press.tooltip.3"));
    }

    @Override
    public ModularPanel buildUI(PosGuiData posGuiData, GuiSyncManager guiSyncManager) {
        return null;
    }


    protected class SelfRecipeLogic extends MultiblockRecipeLogic {
        public SelfRecipeLogic(RecipeMapMultiblockController tileEntity, boolean hasPerfectOC) {
            super(tileEntity, hasPerfectOC);
        }

        @Override
        public void setMaxProgress(int maxProgress) {
            this.maxProgressTime = (int) Math.ceil(maxProgress * 0.166);
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
