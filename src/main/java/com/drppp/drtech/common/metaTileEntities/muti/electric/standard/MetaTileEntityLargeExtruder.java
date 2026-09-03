package com.drppp.drtech.common.metaTileEntities.muti.electric.standard;


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

import gregtech.api.pattern.casing.DeclarativePatternBuilder;

import gregtech.api.pattern.element.Elements;

import gregtech.api.pattern.element.StructureDefinition;

import gregtech.api.metatileentity.multiblock.MultiblockAbility;

public class MetaTileEntityLargeExtruder extends RecipeMapMultiblockController {

    public MetaTileEntityLargeExtruder(ResourceLocation metaTileEntityId) {
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

    private static final StructureDefinition<?> STRUCTURE_DEFINITION =
            StructureDefinition.getOrBuild("drtech:large_extruder",
                    MetaTileEntityLargeExtruder::buildTemplate);

    @Override
    protected @NotNull StructureDefinition<?> createStructureDefinition() {
        return STRUCTURE_DEFINITION;
    }

    private static StructureDefinition<?> buildTemplate() {
        return DeclarativePatternBuilder.start()
                .aisle("##XXX", "##XXX", "##XXX")
                .aisle("##XXX", "##XPX", "##XGX")
                .aisle("##XXX", "##XPX", "##XGX")
                .aisle("XXXXX", "XXXPX", "XXXGX")
                .aisle("XXXXX", "XXXPX", "XXXGX")
                .aisle("XXXXX", "XSXXX", "XXXXX")
                .self('S', MetaTileEntityLargeExtruder.class)
                .any('#')
                .blocks('P', getCasingState2())
                .blocks('G', getCasingState3())
                .where('X', Elements.chain(
                        Elements.counted(40, 4096, Elements.block(getCasingState())),
                        Elements.hatch(MultiblockAbility.MAINTENANCE_HATCH,
                                gregtech.common.ConfigHolder.machines.enableMaintenance ? 1 : 0, 1),
                        Elements.hatch(MultiblockAbility.INPUT_ENERGY, 1, 2, 1),
                        Elements.hatch(MultiblockAbility.IMPORT_ITEMS, 0, -1, 1),
                        Elements.hatch(MultiblockAbility.EXPORT_ITEMS, 0, -1, 1),
                        Elements.hatch(MultiblockAbility.MUFFLER_HATCH, 1, 1)))
                .buildStructureDefinition();

    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityLargeExtruder(this.metaTileEntityId);
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
}
