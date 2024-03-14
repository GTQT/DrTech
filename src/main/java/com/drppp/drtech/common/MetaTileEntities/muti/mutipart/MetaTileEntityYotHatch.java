package com.drppp.drtech.common.MetaTileEntities.muti.mutipart;

import appeng.api.AEApi;
import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.events.MENetworkCellArrayUpdate;
import appeng.api.networking.events.MENetworkStorageEvent;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.*;
import appeng.api.storage.channels.IFluidStorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IItemList;
import appeng.api.util.AEPartLocation;
import appeng.api.util.DimensionalCoord;
import appeng.client.gui.widgets.GuiProgressBar;
import appeng.fluids.util.AEFluidStack;
import appeng.me.helpers.AENetworkProxy;
import appeng.me.helpers.IGridProxyable;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.drppp.drtech.common.MetaTileEntities.muti.electric.store.MetaTileEntityYotTank;
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
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class MetaTileEntityYotHatch extends MetaTileEntityAEHostablePart implements IMultiblockAbilityPart<IAEFluidContainer> , IGridProxyable, IActionHost, ICellContainer,
        IMEInventory<IAEFluidStack>, IMEInventoryHandler<IAEFluidStack> {
    private MetaTileEntityYotTank yotTank;
    private final IAEFluidContainer iaeFluidContainer;
    private WrappedFluidStack fluidStack;
    private int priority=10;
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

       if(this.yotTank==null)
           return;

        if (!this.getWorld().isRemote) {
            if (isChanged()) {
                IGridNode node = getGridNode(null);
                if (node != null) {
                    IGrid grid = node.getGrid();
                    if (grid != null) {
                        grid.postEvent(new MENetworkCellArrayUpdate());
                        IStorageGrid storageGrid = grid.getCache(IStorageGrid.class);
                        if (storageGrid == null) {
                            node.getGrid().postEvent(new MENetworkStorageEvent(null, getChannel()));
                        } else {
                            node.getGrid().postEvent(
                                    new MENetworkStorageEvent(storageGrid.getInventory(getChannel()), getChannel()));
                        }
                        node.getGrid().postEvent(new MENetworkCellArrayUpdate());
                    }
                }
            } else {
            }
        }
        super.update();
    }
    private boolean isChanged() {
        if (this.yotTank == null) return false;
        if(this.yotTank.getFluid()==null) return false;
        return true;
    }
    @Override
    public DimensionalCoord getLocation() {
        return new DimensionalCoord(this.getWorld(), this.getPos().getX(), this.getPos().getY(), this.getPos().getZ());
    }

    @Nullable
    @Override
    public IGridNode getGridNode(@NotNull AEPartLocation aePartLocation) {
        AENetworkProxy gp = getProxy();
        return gp != null ? gp.getNode() : null;
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
        AENetworkProxy gp = getProxy();
        return gp != null ? gp.getNode() : null;
    }

    @Override
    public List<IMEInventoryHandler> getCellArray(IStorageChannel<?> iStorageChannel) {
        List<IMEInventoryHandler> list = new ArrayList<>();
        if (iStorageChannel == getChannel()) {
            list.add(this);
        }
        return list;
    }

    @Override
    public AccessRestriction getAccess() {
        return AccessRestriction.READ_WRITE;
    }

    @Override
    public boolean isPrioritized(IAEFluidStack iaeFluidStack) {
        return true;
    }

    @Override
    public boolean canAccept(IAEFluidStack iaeFluidStack) {
        return true;
    }

    @Override
    public int getPriority() {
        return this.priority;
    }

    @Override
    public int getSlot() {
        return 0;
    }

    @Override
    public boolean validForPass(int i) {
        return true;
    }

    @Override
    public IAEFluidStack injectItems(IAEFluidStack iaeFluidStack, Actionable actionable, IActionSource iActionSource) {
        long amt = fill(null, iaeFluidStack, false);
        if (amt == iaeFluidStack.getStackSize()) {
            if (actionable.equals(Actionable.MODULATE)) fill(null, iaeFluidStack, true);
            return null;
        }
        return iaeFluidStack;
    }

    public int fill(GuiProgressBar.Direction from, FluidStack resource, boolean doFill) {
        if (yotTank == null  || !yotTank.isActive()) return 0;
        if (yotTank.getFluid() != null && !yotTank.getFluid().getLocalizedName().equals("")
                && !yotTank.getFluid().equals(resource.getFluid().getName()))
            return 0;
        if (yotTank.getFluid() == null || yotTank.getFluid().getLocalizedName().equals("")
                || yotTank.getFluid().equals(resource.getFluid().getName())) {
            yotTank.setFluid(new FluidStack(resource.getFluid(),0));
            if (yotTank.getFluidBank().getCapacity().subtract(yotTank.getFluidBank().getStored()).compareTo(BigInteger.valueOf(resource.amount)) >= 0) {
                if (doFill) yotTank.getFluidBank().fill(resource.amount);
                return resource.amount;
            } else {
                long left=0;
                int added = yotTank.getFluidBank().getCapacity().subtract(yotTank.getFluidBank().getStored()).intValue();
                if (doFill)
                    left = yotTank.getFluidBank().fill(added);
                return (int)left;
            }
        }
        return 0;
    }

    public long fill(GuiProgressBar.Direction from, IAEFluidStack resource, boolean doFill) {
        if (yotTank == null  || !yotTank.isActive()) return 0;
        if (yotTank.getFluid() != null && !yotTank.getFluid().getLocalizedName().equals("")
                && !yotTank.getFluid().equals(resource.getFluid().getName()))
            return 0;
        if (yotTank.getFluid() == null || yotTank.getFluid().getLocalizedName().equals("")
                || yotTank.getFluid().equals(resource.getFluid().getName())){
            yotTank.setFluid(new FluidStack(resource.getFluid(),0));
            if (yotTank.getFluidBank().getCapacity().subtract(yotTank.getFluidBank().getStored()).compareTo(BigInteger.valueOf(resource.getStackSize()))
                    >= 0) {
                if (doFill) yotTank.getFluidBank().fill(resource.getStackSize());
                return resource.getStackSize();
            } else {
                long left=0;
                long added = yotTank.getFluidBank().getCapacity().subtract(yotTank.getFluidBank().getStored()).longValue();
                if (doFill)
                    left = yotTank.getFluidBank().fill(added);
                return left;
            }
        }
        return 0;
    }
    @Override
    public IAEFluidStack extractItems(IAEFluidStack iaeFluidStack, Actionable actionable, IActionSource iActionSource) {
        IAEFluidStack ready = drain(null, iaeFluidStack, false);
        if (ready != null) {
            if (actionable.equals(Actionable.MODULATE)) drain(null, ready, true);
            return ready;
        } else return null;
    }

    public FluidStack drain(GuiProgressBar.Direction from, FluidStack resource, boolean doDrain) {
        if (yotTank == null || !yotTank.isActive())
            return null;
        if (yotTank.getFluid() != null && !yotTank.getFluid().getLocalizedName().equals("")
                && !yotTank.getFluid().equals(resource.getFluid().getName()))
            return null;
        int ready;
        if (yotTank.getFluidBank().getStored().compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) > 0) {
            ready = Integer.MAX_VALUE;
        } else ready = yotTank.getFluidBank().getStored().intValue();
        ready = Math.min(ready, resource.amount);
        if (doDrain) {
            yotTank.getFluidBank().drain(ready);
        }
        return new FluidStack(resource.getFluid(), ready);
    }

    public IAEFluidStack drain(GuiProgressBar.Direction from, IAEFluidStack resource, boolean doDrain) {
        if (yotTank == null || !yotTank.isActive())
            return null;
        if (yotTank.getFluid() != null && !yotTank.getFluid().getLocalizedName().equals("")
                && !yotTank.getFluid().equals(resource.getFluid().getName()))
            return null;
        long ready;
        if (yotTank.getFluidBank().getStored().compareTo(BigInteger.valueOf(Long.MAX_VALUE)) > 0) {
            ready = Long.MAX_VALUE;
        } else ready = yotTank.getFluidBank().getStored().longValue();
        ready = Math.min(ready, resource.getStackSize());
        if (doDrain) {
            yotTank.getFluidBank().drain(ready);
        }
        IAEFluidStack copy = resource.copy();
        copy.setStackSize(ready);
        return copy;
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
        return AEApi.instance().storage().getStorageChannel(IFluidStorageChannel.class);
    }

    @Override
    public void saveChanges(@Nullable ICellInventory<?> iCellInventory) {

    }

}
