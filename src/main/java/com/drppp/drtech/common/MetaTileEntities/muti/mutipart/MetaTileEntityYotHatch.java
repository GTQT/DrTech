package com.drppp.drtech.common.MetaTileEntities.muti.mutipart;

import appeng.api.AEApi;
import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridHost;
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
import appeng.fluids.util.FluidList;
import appeng.me.GridAccessException;
import appeng.me.helpers.AENetworkProxy;
import appeng.me.helpers.IGridProxyable;
import appeng.me.storage.MEInventoryHandler;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.drppp.drtech.common.MetaTileEntities.muti.electric.store.MetaTileEntityYotTank;
import com.drppp.drtech.api.capability.DrtechCapabilities;
import com.drppp.drtech.api.capability.IAEFluidContainer;
import gregtech.api.GTValues;
import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.capability.impl.NotifiableFluidTank;
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
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MetaTileEntityYotHatch extends MetaTileEntityAEHostablePart implements IMultiblockAbilityPart<IFluidTank> ,IMEInventory<IAEFluidStack>,ICellProvider {
    private MetaTileEntityYotTank yotTank;
    private boolean online;
    private int Priority=0;
    private final HatchFluidTank fluidTank = new HatchFluidTank(Integer.MAX_VALUE,this);
    protected static final IStorageChannel<IAEFluidStack> FLUID_NET = AEApi.instance().storage().getStorageChannel(IFluidStorageChannel.class);
    private final MEInventoryHandler<IAEFluidStack> meInventoryHandler=new MEInventoryHandler<>(this, FLUID_NET);
    public MetaTileEntityYotHatch(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, GTValues.LV, false,IFluidStorageChannel.class);
        this.setWorkingEnabled(true);

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
    public MultiblockAbility<IFluidTank> getAbility() {
        return DrtechCapabilities.YOT_HATCH;
    }

    @Override
    public void registerAbilities(List<IFluidTank> list) {
        list.add(this.fluidTank);
    }


    @Override
    public void update() {
        super.update();
       if(this.yotTank==null)
           return;
        if (!getWorld().isRemote && this.isWorkingEnabled() && this.shouldSyncME()) {
            if(updateMEStatus()) {
                try {
                    IStorageGrid gridCache = this.getProxy().getGrid().getCache(IStorageGrid.class);
                    if (!online) {
                        gridCache.registerCellProvider(this);
                        online = true;
                        this.writeCustomData(32768, (buf) -> {
                            buf.writeBoolean(online);
                        });
                        this.getProxy().getGrid().postEvent(new MENetworkStorageEvent(gridCache.getInventory(FLUID_NET), FLUID_NET));
                    }
                    this.getProxy().getGrid().postEvent(new MENetworkCellArrayUpdate());
                } catch (GridAccessException e) {
                    throw new RuntimeException(e);
                }
            }
            else {
                disconnectNetwork();
            }
        }
    }
    private void disconnectNetwork()
    {
        if(online) {
            online=false;
            this.writeCustomData(32768, (buf) -> {
                buf.writeBoolean(online);
            });
            try {
                FluidStack existingFluid = this.fluidTank.getFluid();
                if (existingFluid != null) {
                    IAEFluidStack drainedFluidStack = AEFluidStack.fromFluidStack(existingFluid);
                    drainedFluidStack.setStackSize(-drainedFluidStack.getStackSize());
                    this.postDifference(Collections.singletonList(drainedFluidStack));
                    this.fluidTank.setFluid(null); // 清除 fluidTank 中的流体
                }
                this.getProxy().getTick().alertDevice(this.getProxy().getNode());
                IStorageGrid gridCache = this.getProxy().getGrid().getCache(IStorageGrid.class);
                gridCache.unregisterCellProvider(this);
            } catch (GridAccessException ex) {
                // meh
            }

        }
    }

    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeBoolean(this.online);
    }

    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.online = buf.readBoolean();
    }

    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == 32768) {
            boolean isOnline = buf.readBoolean();
            if (this.online != isOnline) {
                this.online = isOnline;
                this.scheduleRenderUpdate();
            }
        }

    }

    public int fill(GuiProgressBar.Direction from, FluidStack resource, boolean doFill) {
        if (yotTank == null  ) return 0;
        if (yotTank.getFluid() != null && !yotTank.getFluid().getLocalizedName().equals("")
                && !yotTank.getFluid().isFluidEqual(resource))
            return 0;
        if (yotTank.getFluid() == null || yotTank.getFluid().getLocalizedName().equals("")
                || yotTank.getFluid().isFluidEqual(resource)) {
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

    public FluidStack drain(GuiProgressBar.Direction from, FluidStack resource, boolean doDrain) {
        if (yotTank == null || !yotTank.isActive())
            return null;
        if (yotTank.getFluid() != null && !yotTank.getFluid().getLocalizedName().equals("")
                && !yotTank.getFluid().isFluidEqual(resource))
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
    private void postDifference(Iterable<IAEFluidStack> a) {
        try{
            IStorageGrid gridCache = this.getProxy().getGrid().getCache(IStorageGrid.class);
            gridCache.postAlterationOfStoredItems(FLUID_NET,a,getActionSource());
        }catch (GridAccessException e)
        {
        }
    }


    @Override
    public IAEFluidStack injectItems(IAEFluidStack input, Actionable type, IActionSource src) {
        FluidStack fluidStack = input.getFluidStack();

        // Insert
        int wasFillled = this.fluidTank.fill(fluidStack, type != Actionable.SIMULATE);
        this.fill(null,fluidStack,type != Actionable.SIMULATE);
        int remaining = fluidStack.amount - wasFillled;
        if (fluidStack.amount == remaining) {
            // The stack was unmodified, target tank is full

            return input;
        }

        if (type == Actionable.MODULATE) {
            IAEFluidStack added = input.copy().setStackSize(input.getStackSize() - remaining);
            //this.cache.currentlyCached.add(added);
            this.postDifference(Collections.singletonList(added));
            try {
                this.getProxy().getTick().alertDevice(this.getProxy().getNode());
            } catch (GridAccessException ex) {
                // meh
            }
        }

        fluidStack.amount = remaining;

        return AEFluidStack.fromFluidStack(fluidStack);
    }

    @Override
    public IAEFluidStack extractItems(IAEFluidStack request, Actionable mode, IActionSource src) {
        FluidStack requestedFluidStack = request.getFluidStack();
        final boolean doDrain = (mode == Actionable.MODULATE);

        // Drain the fluid from the tank
        FluidStack gathered = this.fluidTank.drain(requestedFluidStack, doDrain);
        if (gathered == null) {
            // If nothing was pulled from the tank, return null
            return null;
        }
        this.drain(null,requestedFluidStack,doDrain);
        IAEFluidStack gatheredAEFluidstack = AEFluidStack.fromFluidStack(gathered);
        if (mode == Actionable.MODULATE) {
            this.postDifference(Collections.singletonList(gatheredAEFluidstack.copy().setStackSize(-gatheredAEFluidstack.getStackSize())));
            try {
                this.getProxy().getTick().alertDevice(this.getProxy().getNode());
            } catch (GridAccessException ex) {
                // meh
            }
        }
        return gatheredAEFluidstack;
    }

    @Override
    public IItemList<IAEFluidStack> getAvailableItems(IItemList<IAEFluidStack> iItemList) {
       return getMonitor().getStorageList();
    }

    @Override
    public IStorageChannel<IAEFluidStack> getChannel() {
        return FLUID_NET;
    }

    @Override
    public List<IMEInventoryHandler> getCellArray(IStorageChannel<?> iStorageChannel) {
        if (iStorageChannel == FLUID_NET) {
            return Collections.singletonList(this.meInventoryHandler);
        }
        return Collections.EMPTY_LIST;
    }

    @Override
    public int getPriority() {
        return Priority;
    }

    private static class HatchFluidTank extends NotifiableFluidTank {
        public HatchFluidTank(int capacity, MetaTileEntityAEHostablePart entityToNotify) {
            super(capacity, entityToNotify, false);
        }

        public boolean canFillFluidType(FluidStack fluid) {
            return true;
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.Priority = data.getInteger("Priority");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setInteger("Priority",Priority);
        return data;
    }
}
