package com.drppp.drtech.common.metaTileEntities.muti.electric.standard;

import com.drppp.drtech.client.Textures;
import com.drppp.drtech.common.blocks.BlocksInit;
import com.drppp.drtech.common.blocks.MetaBlocks.MetaCasing1;
import gregtech.api.GTValues;
import gregtech.api.capability.impl.MultiblockRecipeLogic;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
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

public class MetaTileEntityIndustrialSieve extends RecipeMapMultiblockController {
    public MetaTileEntityIndustrialSieve(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, RecipeMaps.SIFTER_RECIPES);
        this.recipeMapWorkable = new SelfRecipeLogic(this, false);
    }

    private static final StructureDefinition<?> STRUCTURE_DEFINITION =
            StructureDefinition.getOrBuild("drtech:industrial_sieve",
                    MetaTileEntityIndustrialSieve::buildTemplate);

    @Override
    protected @NotNull StructureDefinition<?> createStructureDefinition() {
        return STRUCTURE_DEFINITION;
    }

    private static StructureDefinition<?> buildTemplate() {
        return DeclarativePatternBuilder.start(RIGHT, UP, BACK)
                .aisle("XXXXX", "XXXXX", "XXXXX")
                .aisle("XXXXX", "XAAAX", "XAAAX")
                .aisle("XXXXX", "XAAAX", "XAAAX")
                .aisle("XXXXX", "XAAAX", "XAAAX")
                .aisle("XXXXX", "XXSXX", "XXXXX")
                .self('S', MetaTileEntityIndustrialSieve.class)
                .blocks('A', getCasingState1())
                .where('X', Elements.chain(
                        Elements.counted(35, 4096, Elements.block(getCasingState())),
                        Elements.hatch(MultiblockAbility.MAINTENANCE_HATCH,
                                gregtech.common.ConfigHolder.machines.enableMaintenance ? 1 : 0, 1),
                        Elements.hatch(MultiblockAbility.INPUT_ENERGY, 1, 2, 1),
                        Elements.hatch(MultiblockAbility.IMPORT_ITEMS, 0, -1, 1),
                        Elements.hatch(MultiblockAbility.EXPORT_ITEMS, 0, -1, 1),
                        Elements.hatch(MultiblockAbility.MUFFLER_HATCH, 1, 1)))
                .buildStructureDefinition();

    }


    protected static IBlockState getCasingState() {
        return BlocksInit.COMMON_CASING1.getState(MetaCasing1.MetalCasingType.SIEVE_CASING);
    }

    protected static IBlockState getCasingState1() {
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
