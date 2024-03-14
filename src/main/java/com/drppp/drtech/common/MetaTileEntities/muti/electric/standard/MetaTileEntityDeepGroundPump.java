package com.drppp.drtech.common.MetaTileEntities.muti.electric.standard;

import com.drppp.drtech.common.Items.MetaItems.MyMetaItems;
import com.drppp.drtech.loaders.DrtechReceipes;
import gregtech.api.capability.impl.MultiblockRecipeLogic;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.unification.material.Materials;
import gregtech.api.util.GTTransferUtils;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockBoilerCasing;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MetaTileEntityDeepGroundPump extends RecipeMapMultiblockController {
    public int Deep=0;
    public MetaTileEntityDeepGroundPump(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, DrtechReceipes.DRRP_GROUND_PUMP);
        this.recipeMapWorkable = new DGP_Logic(this, false);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        return super.writeToNBT(data);
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
    }

    @Override
    protected @NotNull BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("FXXXF", "FXXXF", "#####", "FXXXF", "#####", "XXXXX","XXXXX")
                .aisle("XXXXX", "XXXXX", "#####", "XXXXX", "#####", "X###X","X###X")
                .aisle("XXXXX", "XXXXX", "##G##", "XXGXX", "##G##", "X#G#X","X###X")
                .aisle("XXXXX", "XXXXX", "#####", "XXXXX", "#####", "X#S#X","X###X")
                .aisle("FXXXF", "FXXXF", "#####", "FXXXF", "#####", "XXXXX","XXXXX")
                .where('S', selfPredicate())
                .where('X',any() )
                .where('G',states(MetaBlocks.BOILER_CASING.getState(BlockBoilerCasing.BoilerCasingType.TITANIUM_PIPE)))
                .where('F', frames(Materials.Steel))
                .where('#', states(MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.INVAR_HEATPROOF))
                                .or(abilities(MultiblockAbility.MAINTENANCE_HATCH).setExactLimit(1))
                                .or(abilities(MultiblockAbility.INPUT_ENERGY).setMaxGlobalLimited(1))
                                .or(abilities(MultiblockAbility.IMPORT_FLUIDS).setMinGlobalLimited(1).setPreviewCount(1))
                                .or(abilities(MultiblockAbility.EXPORT_FLUIDS).setMinGlobalLimited(1).setPreviewCount(1))
                                .or(abilities(MultiblockAbility.IMPORT_ITEMS).setMinGlobalLimited(1).setPreviewCount(1))
                )
                .build();
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return Textures.HEAT_PROOF_CASING;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityDeepGroundPump(this.metaTileEntityId);
    }

    @Override
    protected void addDisplayText(List<ITextComponent> textList) {
        super.addDisplayText(textList);
        textList.add(new TextComponentTranslation("drtech.deep_ground_pump.deep",this.Deep));
    }

    protected class DGP_Logic extends MultiblockRecipeLogic {

        public DGP_Logic(RecipeMapMultiblockController tileEntity, boolean hasPerfectOC) {
            super(tileEntity, hasPerfectOC);
        }
        @Override
        protected void outputRecipeOutputs() {
            if(getDeep()!=0)
                GTTransferUtils.addFluidsToFluidHandler(this.getOutputTank(), false, this.fluidOutputs);
        }
        @Override
        public void setMaxProgress(int maxProgress) {
            this.maxProgressTime=20;
        }
        private int getDeep()
        {
            ItemStack item;
            int deep=0;
            for (int i = 0; i < this.getInputInventory().getSlots(); i++) {
                item = this.getInputInventory().getStackInSlot(i);
                if(item.getItem()== MyMetaItems.PIPIE_1.getMetaItem() && item.getMetadata()==MyMetaItems.PIPIE_1.getMetaValue())
                {
                    deep+= item.getCount();
                }else if(item.getItem()== MyMetaItems.PIPIE_5.getMetaItem() && item.getMetadata()==MyMetaItems.PIPIE_5.getMetaValue())
                {
                    deep+= item.getCount()*5;
                }
                else if(item.getItem()== MyMetaItems.PIPIE_10.getMetaItem() && item.getMetadata()==MyMetaItems.PIPIE_10.getMetaValue())
                {
                    deep+= item.getCount()*10;
                }
            }
            ( (MetaTileEntityDeepGroundPump) this.metaTileEntity).Deep = deep;
            return deep;
        }

    }
}
