package com.drppp.drtech.common.MetaTileEntities.muti;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.value.sync.GuiSyncManager;
import gregtech.api.GTValues;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.capability.impl.NotifiableFluidTank;
import gregtech.api.capability.impl.PrimitiveRecipeLogic;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.*;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityUIFactory;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.RecipeMapPrimitiveMultiblockController;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.util.GTTransferUtils;
import gregtech.client.particle.VanillaParticleEffects;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.ConfigHolder;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.metatileentities.MetaTileEntities;
import gregtech.common.metatileentities.multi.MetaTileEntityCokeOven;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class MetaTileEntityIndustrialCokeOven extends RecipeMapPrimitiveMultiblockController {
    public MetaTileEntityIndustrialCokeOven(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, RecipeMaps.COKE_OVEN_RECIPES);
        this.exportFluids = new FluidTankList(false, makeFluidTanks(this.recipeMapWorkable.getRecipeMap().getMaxFluidOutputs(), true));
        this.recipeMapWorkable = new IndustrialRecipeLogic(this, RecipeMaps.COKE_OVEN_RECIPES);
    }
    private List<FluidTank> makeFluidTanks(int length, boolean isExport) {
        List<FluidTank> fluidTankList = new ArrayList(length);
        for(int i = 0; i < length; ++i) {
            fluidTankList.add(new NotifiableFluidTank(256000, this, isExport));
        }
        return fluidTankList;
    }
    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityIndustrialCokeOven(metaTileEntityId);
    }

    @NotNull
    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("XXX", "XXX", "XXX")
                .aisle("XXX", "X#X", "XXX")
                .aisle("XXX", "X#X", "XXX")
                .aisle("XXX", "X#X", "XXX")
                .aisle("XXX", "X#X", "XXX")
                .aisle("XXX", "X#X", "XXX")
                .aisle("XXX", "XYX", "XXX")
                .where('X',
                        states(getCasingState())
                                .or(metaTileEntities(MetaTileEntities.COKE_OVEN_HATCH).setMaxGlobalLimited(5)))
                .where('#', air())
                .where('Y', selfPredicate())
                .build();
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, @NotNull List<String> tooltip, boolean advanced) {
        super.addInformation(stack, world, tooltip, advanced);
        tooltip.add("拥有32并行的焦炉");
        tooltip.add("能够两倍产出杂酚油,虽然不知道有什么用");
    }

    protected IBlockState getCasingState() {
        return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.COKE_BRICKS);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return Textures.COKE_BRICKS;
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        getFrontOverlay().renderOrientedState(renderState, translation, pipeline, getFrontFacing(),
                recipeMapWorkable.isActive(), recipeMapWorkable.isWorkingEnabled());
    }

    @SideOnly(Side.CLIENT)
    @NotNull
    @Override
    protected ICubeRenderer getFrontOverlay() {
        return Textures.COKE_OVEN_OVERLAY;
    }

    @Override
    public boolean hasMaintenanceMechanics() {
        return false;
    }

    @Override
    protected ModularUI.Builder createUITemplate(EntityPlayer entityPlayer) {
        return ModularUI.builder(GuiTextures.PRIMITIVE_BACKGROUND, 176, 166)
                .shouldColor(false)
                .widget(new LabelWidget(5, 5, getMetaFullName()))
                .widget(new SlotWidget(importItems, 0, 52, 30, true, true)
                        .setBackgroundTexture(GuiTextures.PRIMITIVE_SLOT, GuiTextures.PRIMITIVE_FURNACE_OVERLAY))
                .widget(new RecipeProgressWidget(recipeMapWorkable::getProgressPercent, 76, 32, 20, 15,
                        GuiTextures.PRIMITIVE_BLAST_FURNACE_PROGRESS_BAR, ProgressWidget.MoveType.HORIZONTAL,
                        RecipeMaps.COKE_OVEN_RECIPES))
                .widget(new SlotWidget(exportItems, 0, 103, 30, true, false)
                        .setBackgroundTexture(GuiTextures.PRIMITIVE_SLOT, GuiTextures.PRIMITIVE_FURNACE_OVERLAY))
                .widget(new TankWidget(exportFluids.getTankAt(0), 134, 13, 20, 58)
                        .setBackgroundTexture(GuiTextures.PRIMITIVE_LARGE_FLUID_TANK)
                        .setOverlayTexture(GuiTextures.PRIMITIVE_LARGE_FLUID_TANK_OVERLAY)
                        .setContainerClicking(true, false))
                .bindPlayerInventory(entityPlayer.inventory, GuiTextures.PRIMITIVE_SLOT, 0);
    }

    @Override
    public void randomDisplayTick() {
        if (this.isActive()) {
            VanillaParticleEffects.defaultFrontEffect(this, 0.3F, EnumParticleTypes.SMOKE_LARGE,
                    EnumParticleTypes.FLAME);
            if (ConfigHolder.machines.machineSounds && GTValues.RNG.nextDouble() < 0.1) {
                BlockPos pos = getPos();
                getWorld().playSound(pos.getX(), pos.getY(), pos.getZ(),
                        SoundEvents.BLOCK_FURNACE_FIRE_CRACKLE, SoundCategory.BLOCKS, 1.0F, 1.0F, false);
            }
        }
    }

    @Override
    public boolean onRightClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing,
                                CuboidRayTraceResult hitResult) {
        if (playerIn.getHeldItem(hand).hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null)) {
            if (!playerIn.isSneaking()) {
                return getWorld().isRemote || FluidUtil.interactWithFluidHandler(playerIn, hand, getFluidInventory());
            } else {
                if (getWorld() != null && !getWorld().isRemote) {
                    MetaTileEntityUIFactory.INSTANCE.openUI(getHolder(), (EntityPlayerMP) playerIn);
                }
                return true;
            }
        }
        return super.onRightClick(playerIn, hand, facing, hitResult);
    }

    @Override
    public ModularPanel buildUI(PosGuiData posGuiData, GuiSyncManager guiSyncManager) {
        return null;
    }

    private class IndustrialRecipeLogic  extends  PrimitiveRecipeLogic
    {

        public IndustrialRecipeLogic(RecipeMapPrimitiveMultiblockController tileEntity, RecipeMap<?> recipeMap) {
            super(tileEntity, recipeMap);
            this.setParallelLimit(32);

        }
        public long getMaxVoltage() {
            return 32L;
        }
        @Override
        protected void outputRecipeOutputs() {
            GTTransferUtils.addItemsToItemHandler(this.getOutputInventory(), false, this.itemOutputs);
            for (int i = 0; i < this.fluidOutputs.size(); i++) {
                this.fluidOutputs.get(i).amount = this.fluidOutputs.get(i).amount*2;
            }
            GTTransferUtils.addFluidsToFluidHandler(this.getOutputTank(), false, this.fluidOutputs);
        }
    }
}
