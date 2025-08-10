package com.drppp.drtech.common.MetaTileEntities.muti.electric.standard;


import com.drppp.drtech.Client.Textures;
import com.drppp.drtech.common.Blocks.BlocksInit;
import com.drppp.drtech.common.Blocks.MetaBlocks.MetaGlasses1;
import gregtech.api.GTValues;
import gregtech.api.capability.impl.MultiblockRecipeLogic;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.unification.material.Materials;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.utils.TooltipHelper;
import keqing.gtqtcore.common.block.GTQTMetaBlocks;
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

import static com.drppp.drtech.loaders.recipes.DrtechReceipes.MOLECULAR_RECOMBINATION;
import static keqing.gtqtcore.common.block.blocks.BlockMultiblockCasing.CasingType.MASS_GENERATION_CASING;
import static keqing.gtqtcore.common.block.blocks.BlockMultiblockCasing.CasingType.MASS_GENERATION_COIL_CASING;


public class MetaTileEntityLargeMolecularRecombination extends RecipeMapMultiblockController {

    public MetaTileEntityLargeMolecularRecombination(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, MOLECULAR_RECOMBINATION);
        this.recipeMapWorkable = new LargeMoecularRecombinationLogic(this, true);
    }

    private static IBlockState getCasingState() {
        return GTQTMetaBlocks.blockMultiblockCasing.getState(MASS_GENERATION_CASING);
    }

    private static IBlockState getCasingState2() {
        return GTQTMetaBlocks.blockMultiblockCasing.getState(MASS_GENERATION_COIL_CASING);
    }

    private static IBlockState getCasingState3() {
        return BlocksInit.TRANSPARENT_CASING1.getState(MetaGlasses1.CasingType.RECOMBINATION_GLASS_BLOCK);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity metaTileEntityHolder) {
        return new MetaTileEntityLargeMolecularRecombination(this.metaTileEntityId);
    }
    @Override
    public boolean usesMui2() {
        return false;
    }
    @Override
    protected @NotNull BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("XXXXX", "F###F", "F###F", "F###F", "F###F", "XXXXX")
                .aisle("XXXXX", "#PGP#", "#PGP#", "#PGP#", "#PGP#", "XXXXX")
                .aisle("XXXXX", "#GAG#", "#GAG#", "#GAG#", "#GAG#", "XXXXX")
                .aisle("XXXXX", "#PGP#", "#PGP#", "#PGP#", "#PGP#", "XXXXX")
                .aisle("XXSXX", "F###F", "F###F", "F###F", "F###F", "XXXXX")
                .where('S', selfPredicate())
                .where('X',
                        states(getCasingState()).setMinGlobalLimited(40)
                                .or(autoAbilities(false, true, true, true, true, true, false))
                                .or(abilities(MultiblockAbility.INPUT_ENERGY).setMaxGlobalLimited(2))
                )
                .where('P', states(getCasingState2()))
                .where('G', states(getCasingState3()))
                .where('F', frames(Materials.TungstenCarbide))
                .where('A', air())
                .where('#', any())
                .build();
    }

    @SideOnly(Side.CLIENT)
    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return Textures.MASS_GENERATION_CASING;
    }

    @SideOnly(Side.CLIENT)
    @Override
    protected @NotNull ICubeRenderer getFrontOverlay() {
        return Textures.LARGE_UU_PRODUCTER;
    }

    @Override
    public boolean hasMufflerMechanics() {
        return false;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(I18n.format("drtech.machine.recombination.tip.1"));
        tooltip.add(I18n.format("drtech.machine.recombination.tip.2"));
        tooltip.add(TooltipHelper.RAINBOW_SLOW + I18n.format("gregtech.machine.perfect_oc"));
    }


    protected class LargeMoecularRecombinationLogic extends MultiblockRecipeLogic {

        public LargeMoecularRecombinationLogic(RecipeMapMultiblockController tileEntity, boolean hasPerfectOC) {
            super(tileEntity, hasPerfectOC);
        }

        @Override
        public int getParallelLimit() {
            int tire = 1;
            for (int i = 0; i < GTValues.V.length; i++) {
                if (GTValues.V[i] == this.getMaxVoltage())
                    tire = i;
            }
            return tire * 2;
        }

        @Override
        public void setMaxProgress(int maxProgress) {
            this.maxProgressTime = (int) Math.ceil(maxProgress * 0.75);
        }
    }

}
