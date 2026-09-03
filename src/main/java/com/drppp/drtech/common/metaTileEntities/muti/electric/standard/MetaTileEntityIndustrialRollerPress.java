package com.drppp.drtech.common.metaTileEntities.muti.electric.standard;


import com.drppp.drtech.client.Textures;
import com.drppp.drtech.common.blocks.BlocksInit;
import com.drppp.drtech.common.blocks.MetaBlocks.MetaCasing1;
import gregtech.api.GTValues;
import gregtech.api.capability.impl.MultiblockRecipeLogic;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiMapMultiblockController;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
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

import gregtech.api.pattern.casing.DeclarativePatternBuilder;

import gregtech.api.pattern.element.Elements;

import gregtech.api.pattern.element.StructureDefinition;

public class MetaTileEntityIndustrialRollerPress extends MultiMapMultiblockController {
    public MetaTileEntityIndustrialRollerPress(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, new RecipeMap[]{RecipeMaps.BENDER_RECIPES, RecipeMaps.FORMING_PRESS_RECIPES});
        this.recipeMapWorkable = new SelfRecipeLogic(this, false);
    }

    private static final StructureDefinition<?> STRUCTURE_DEFINITION =
            StructureDefinition.getOrBuild("drtech:industrial_roller_press",
                    MetaTileEntityIndustrialRollerPress::buildTemplate);

    @Override
    protected @NotNull StructureDefinition<?> createStructureDefinition() {
        return STRUCTURE_DEFINITION;
    }

    private static StructureDefinition<?> buildTemplate() {
        return DeclarativePatternBuilder.start(RIGHT, UP, BACK)
                .aisle("XXX", "XXX", "XXX")
                .aisle("XXX", "X X", "XXX")
                .aisle("XXX", "XSX", "XXX")
                .self('S', MetaTileEntityIndustrialRollerPress.class)
                .where('X', Elements.chain(
                        Elements.counted(6, 4096, Elements.block(getCasingState())),
                        Elements.hatch(MultiblockAbility.MAINTENANCE_HATCH,
                                gregtech.common.ConfigHolder.machines.enableMaintenance ? 1 : 0, 1),
                        Elements.hatch(MultiblockAbility.INPUT_ENERGY, 1, 2, 1),
                        Elements.hatch(MultiblockAbility.IMPORT_ITEMS, 0, -1, 1),
                        Elements.hatch(MultiblockAbility.EXPORT_ITEMS, 0, -1, 1),
                        Elements.hatch(MultiblockAbility.MUFFLER_HATCH, 1, 1)))
                .buildStructureDefinition();

    }

    @Override
    public boolean canBeDistinct() {
        return true;
    }

    protected static IBlockState getCasingState() {
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
