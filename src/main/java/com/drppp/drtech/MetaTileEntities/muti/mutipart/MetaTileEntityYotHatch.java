package com.drppp.drtech.MetaTileEntities.muti.mutipart;

import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.*;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IItemList;
import appeng.api.util.AEPartLocation;
import appeng.api.util.DimensionalCoord;
import appeng.fluids.util.AEFluidStack;
import appeng.me.helpers.IGridProxyable;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.drppp.drtech.MetaTileEntities.muti.ecectric.standard.MetaTileEntityDeepGroundPump;
import com.drppp.drtech.MetaTileEntities.muti.ecectric.store.MetaTileEntityYotTank;
import com.drppp.drtech.api.capability.DrtechCapabilities;
import com.drppp.drtech.api.capability.IAEFluidContainer;
import gregtech.api.GTValues;
import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.gui.ModularUI;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.MetaTileEntityAEHostablePart;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.stack.WrappedFluidStack;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;
import java.util.List;

public class MetaTileEntityYotHatch extends MetaTileEntityAEHostablePart implements IMultiblockAbilityPart<IAEFluidContainer> , IGridProxyable, IActionHost, ICellContainer,
        IMEInventory<IAEFluidStack>, IMEInventoryHandler<IAEFluidStack> {
    private MetaTileEntityYotTank yotTank;
    private final IAEFluidContainer iaeFluidContainer;
    private WrappedFluidStack fluidStack;
    public MetaTileEntityYotHatch(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, GTValues.UV, false);
        iaeFluidContainer = null;
    }

    @Override
    public boolean isWorkingEnabled() {
        return true;
    }

    @Override
    public void setWorkingEnabled(boolean workingEnabled) {
        World world = this.getWorld();
        if (world != null && !world.isRemote) {

            writeCustomData(GregtechDataCodes.WORKING_ENABLED, buf -> buf.writeBoolean(workingEnabled));
        }
    }
    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityYotHatch(this.metaTileEntityId);
    }

    @Override
    public ICubeRenderer getBaseTexture() {
        return com.drppp.drtech.Client.Textures.YOT_TANK_CASING;
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        return null;
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        if (this.shouldRenderOverlay()) {
            Textures.ME_INPUT_BUS.renderSided(getFrontFacing(), renderState, translation, pipeline);
        }
    }

    public void setYotTank(MetaTileEntityYotTank yotTank) {
        this.yotTank = yotTank;
    }

    @Override
    public MultiblockAbility<IAEFluidContainer> getAbility() {
        return DrtechCapabilities.YOT_HATCH;
    }

    @Override
    public void registerAbilities(List<IAEFluidContainer> list) {
        list.add(iaeFluidContainer);
    }

    @Override
    public void update() {
        super.update();
        //fluidStack =  WrappedFluidStack.fromFluidStack(this.yotTank.getFluid());
    }

    @Override
    public DimensionalCoord getLocation() {
        return null;
    }

    @Nullable
    @Override
    public IGridNode getGridNode(@NotNull AEPartLocation aePartLocation) {
        return null;
    }

    @Override
    public void securityBreak() {

    }

    @Override
    public void blinkCell(int i) {

    }

    @NotNull
    @Override
    public IGridNode getActionableNode() {
        return null;
    }

    @Override
    public List<IMEInventoryHandler> getCellArray(IStorageChannel<?> iStorageChannel) {
        return null;
    }

    @Override
    public AccessRestriction getAccess() {
        return null;
    }

    @Override
    public boolean isPrioritized(IAEFluidStack iaeFluidStack) {
        return false;
    }

    @Override
    public boolean canAccept(IAEFluidStack iaeFluidStack) {
        return false;
    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public int getSlot() {
        return 0;
    }

    @Override
    public boolean validForPass(int i) {
        return false;
    }

    @Override
    public IAEFluidStack injectItems(IAEFluidStack iaeFluidStack, Actionable actionable, IActionSource iActionSource) {
        return null;
    }

    @Override
    public IAEFluidStack extractItems(IAEFluidStack iaeFluidStack, Actionable actionable, IActionSource iActionSource) {
        return null;
    }

    @Override
    public IItemList<IAEFluidStack> getAvailableItems(IItemList<IAEFluidStack> iItemList) {
        if (yotTank == null || !yotTank.isActive())
            return iItemList;
        if (yotTank.getFluid() == null || yotTank.getFluid().getLocalizedName().equals("")
                || yotTank.getFluidBank().getStored().compareTo(BigInteger.ZERO) <= 0)
            return iItemList;
        int ready;
        if (yotTank.getFluidBank().getStored().compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) > 0) {
            ready = Integer.MAX_VALUE;
        } else ready = yotTank.getFluidBank().getStored().intValue();
        //iItemList.add(StackUtils.createAEFluidStack(FluidRegistry.getFluid(yotTank.getFluid()), ready));
        iItemList.add( AEFluidStack.fromFluidStack(new FluidStack(yotTank.getFluid(),ready)));
        return iItemList;
    }

    @Override
    public IStorageChannel<IAEFluidStack> getChannel() {
        return null;
    }

    @Override
    public void saveChanges(@Nullable ICellInventory<?> iCellInventory) {

    }

}
