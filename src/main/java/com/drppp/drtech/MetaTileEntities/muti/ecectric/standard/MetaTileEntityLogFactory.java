package com.drppp.drtech.MetaTileEntities.muti.ecectric.standard;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.drppp.drtech.Blocks.BlocksInit;
import com.drppp.drtech.Blocks.MetaBlocks.MetaCasing;
import com.drppp.drtech.Blocks.MetaBlocks.MetaGlasses1;
import com.drppp.drtech.Client.Textures;
import com.drppp.drtech.Load.DrtechReceipes;
import gregtech.api.GTValues;
import gregtech.api.capability.impl.MultiblockRecipeLogic;
import gregtech.api.items.toolitem.ItemGTTool;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.TraceabilityPredicate;
import gregtech.api.util.GTTransferUtils;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.utils.TooltipHelper;
import gregtech.common.items.ToolItems;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class MetaTileEntityLogFactory extends RecipeMapMultiblockController {

    public MetaTileEntityLogFactory(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, DrtechReceipes.LOG_CREATE);
        this.recipeMapWorkable = new LogFactoryRecipeLogic(this, true);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityLogFactory(metaTileEntityId);
    }

    @Override
    protected BlockPattern createStructurePattern() {
        TraceabilityPredicate abilities = autoAbilities();
        return FactoryBlockPattern.start()
                .aisle("XXX", "XXX", "XXX")
                .aisle("XXX", "X#X", "XXX")
                .aisle("XXX", "XSX", "XXX")
                .where('S', selfPredicate())
                .where('X', states(getCasingState()).setMinGlobalLimited(8).or(abilities).or(abilities(MultiblockAbility.MUFFLER_HATCH).setExactLimit(1)))
                .where('#', air())
                .build();
    }

    @SideOnly(Side.CLIENT)
    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return Textures.ASEPTIC_MACHINE_CASING;
    }
    protected IBlockState getCasingState() {
        return BlocksInit.COMMON_CASING.getState(MetaCasing.MetalCasingType.ASEPTIC_MACHINE_CASING);
    }


    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(I18n.format("drtech.machine.uuproducter.tip.1"));
        tooltip.add(I18n.format("drtech.machine.logcreate.tip.1"));
        tooltip.add(I18n.format("drtech.machine.logcreate.tip.2"));
        tooltip.add(I18n.format("drtech.machine.logcreate.tip.3"));

    }

    protected class LogFactoryRecipeLogic extends MultiblockRecipeLogic{
        public LogFactoryRecipeLogic(RecipeMapMultiblockController tileEntity, boolean hasPerfectOC) {
            super(tileEntity, hasPerfectOC);
        }
        @Override
        public void setMaxProgress(int maxProgress) {
            this.maxProgressTime = 100;
        }
        @Override
        protected void outputRecipeOutputs() {
            int voltire= getVolLevel();
            int outNum = (2*voltire*voltire-2*voltire+5)*5*getCoe();
            setOutPutItem(outNum);
            GTTransferUtils.addItemsToItemHandler(this.getOutputInventory(), false, this.itemOutputs);
            GTTransferUtils.addFluidsToFluidHandler(this.getOutputTank(), false, this.fluidOutputs);
        }
        private void setOutPutItem(int num)
        {
           if(num !=0)
           {
               List<ItemStack> MyitemOutputs = new ArrayList<>();
               for (ItemStack item: this.itemOutputs
               ) {
                   int group = num/64;
                   for (int i = 0; i < group; i++) {
                       MyitemOutputs.add(new ItemStack(item.getItem(),64));
                   }
                   MyitemOutputs.add(new ItemStack(item.getItem(),num%64));
               }
               GTTransferUtils.addItemsToItemHandler(this.getOutputInventory(), false, MyitemOutputs);
           }
        }
    private int getCoe() {
        int coe = 0;
        var slots = this.getInputInventory().getSlots();
        for (int i = 0; i < slots; i++) {
            ItemStack item =  this.getInputInventory().getStackInSlot(i);
            if(item.getItem().getRegistryName().getPath().equals("saw") )
            {
                coe = 1;
            }else if(item.getItem().getRegistryName().getPath().equals("buzzsaw") )
            {
                coe = 2;
            }
            else if(item.getItem().getRegistryName().getPath().equals("chainsaw_lv") )
            {
                coe = 4;
            }
        }
        return coe;
    }
    public int getVolLevel() {
        int tire = 1;
        for (int i = 0; i < GTValues.V.length; i++) {
            if(GTValues.V[i]==this.getMaxVoltage())
                tire = i;
        }
        return tire;
    }
    }
}