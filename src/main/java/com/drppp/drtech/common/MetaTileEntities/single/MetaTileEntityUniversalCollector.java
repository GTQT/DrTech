package com.drppp.drtech.common.MetaTileEntities.single;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.drppp.drtech.Client.Textures;
import gregtech.api.GTValues;
import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IWorkable;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.capability.impl.NotifiableFluidTank;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.PhantomTankWidget;
import gregtech.api.gui.widgets.SlotWidget;
import gregtech.api.gui.widgets.TankWidget;
import gregtech.api.gui.widgets.ToggleButtonWidget;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.TieredMetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.util.GTTransferUtils;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.common.metatileentities.storage.MetaTileEntityQuantumChest;
import gregtech.common.metatileentities.storage.MetaTileEntityQuantumTank;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;


public class MetaTileEntityUniversalCollector extends TieredMetaTileEntity implements IWorkable {
    protected final ICubeRenderer renderer;
    boolean isActive=true;
    boolean isWorkingEnabled=true;
    ItemStackHandler inventory = new ItemStackHandler(45);
    private final FluidTankList fluidTankList;
    int tick=0;
    private int range=0;
    private int lx=0,ly=0,lz=0;
    public MetaTileEntityUniversalCollector(ResourceLocation metaTileEntityId, int tier, ICubeRenderer renderer) {
        super(metaTileEntityId, tier);
        this.renderer = renderer;
        FluidTank[] fluidsHandlers = new FluidTank[9];
        for (int i = 0; i < fluidsHandlers.length; i++) {
            fluidsHandlers[i] = new NotifiableFluidTank(16000, this, true);
        }
        this.fluidTankList = new FluidTankList(false, fluidsHandlers);
        this.range =(int) Math.pow((getTier()+1),2);
    }
    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityUniversalCollector(this.metaTileEntityId,getTier(),renderer);
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        this.renderOverlays(renderState, translation, pipeline);

    }
    protected void renderOverlays(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        this.renderer.renderOrientedState(renderState, translation, pipeline, this.getFrontFacing(), isActive(), isWorkingEnabled());
    }
    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        ModularUI.Builder builder;
        TankWidget tankWidget;
        builder = ModularUI.builder(Textures.BACKGROUND, 198, 208);
        for (int i = 0; i < 45; i++) {
            builder.slot(inventory,i,5+i%9*18,3+i/9*18,true,true, GuiTextures.SLOT);
        }
        for (int i = 0; i < 9; i++) {
                    builder.widget(
                            new TankWidget(fluidTankList.getTankAt(i),  i* 18+5, 95 , 18, 18)
                                    .setBackgroundTexture(GuiTextures.FLUID_SLOT)
                                    .setContainerClicking(true, true)
                                    .setAlwaysShowFull(true));
        }
        builder.bindPlayerInventory(entityPlayer.inventory, 117);
        return builder.build(this.getHolder(), entityPlayer);
    }
    @Override
    public void update() {
        super.update();
        if(!getWorld().isRemote)
        {
            if(energyContainer.getEnergyStored()<GTValues.V[getTier()]/4)
            {
                isWorkingEnabled=false;
            }
            if(isWorkingEnabled)
            {
                energyContainer.changeEnergy(-GTValues.V[getTier()]/4);
                if(++tick>80)
                {
                    tick=0;
                    getRangeTileentities();
                }
            }
        }

    }
    private void getRangeTileentities()
    {
            BlockPos centerPos=this.getPos() ;
            int radius = range;
            for (int x = -radius; x <= radius; x++) {

                for (int y = -radius; y <= radius; y++) {
                    for (int z = -radius; z <= radius; z++) {
                        BlockPos currentPos = centerPos.add(x, y, z);
                        MetaTileEntity te = GTUtility.getMetaTileEntity(this.getWorld(),currentPos);
                        if(te != null && !(te instanceof MetaTileEntityQuantumTank) &&!(te instanceof MetaTileEntityQuantumChest))
                        {
                            GTTransferUtils.moveInventoryItems(te.getExportItems(),inventory);
                            GTTransferUtils.transferFluids(te.getExportFluids(),this.fluidTankList);
                        }
                    }
                }
            }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setTag("inventory", inventory.serializeNBT());
        data.setBoolean("isActive", isActive);
        data.setBoolean("isWorkingEnabled", isWorkingEnabled);
        if(fluidTankList!=null)
           data.setTag("fluidlist", fluidTankList.serializeNBT());
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        if (data.hasKey("inventory")) {
            inventory.deserializeNBT(data.getCompoundTag("inventory"));
        }
        if(data.hasKey("fluidlist"))
            fluidTankList.deserializeNBT(data.getCompoundTag("fluidlist"));
        isActive = data.getBoolean("isActive");
        isWorkingEnabled = data.getBoolean("isWorkingEnabled");
    }
    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeBoolean(isActive);
        buf.writeBoolean(isWorkingEnabled);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        isActive = buf.readBoolean();
        isWorkingEnabled = buf.readBoolean();
    }

    @Override
    public void receiveCustomData(int dataId, @NotNull PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == GregtechDataCodes.WORKABLE_ACTIVE) {
            isActive = buf.readBoolean();
            scheduleRenderUpdate();
        } else if (dataId == GregtechDataCodes.WORKING_ENABLED) {
            isWorkingEnabled = buf.readBoolean();
            scheduleRenderUpdate();
        }
    }
    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if(capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
        {
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(this.inventory);
        }else if(capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
        {
            for (int i = 0; i < fluidTankList.getTanks(); i++)
            {
                if(fluidTankList.getTankAt(i).getFluidAmount()>0)
                {
                    return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(this.fluidTankList.getTankAt(i));
                }
            }
        }else if (capability == GregtechTileCapabilities.CAPABILITY_WORKABLE)
        {
            return GregtechTileCapabilities.CAPABILITY_WORKABLE.cast(this);
        }else if (capability == GregtechTileCapabilities.CAPABILITY_CONTROLLABLE)
        {
            return GregtechTileCapabilities.CAPABILITY_CONTROLLABLE.cast(this);
        }
        return super.getCapability(capability, side);
    }

    @Override
    public int getProgress() {
        return 0;
    }

    @Override
    public int getMaxProgress() {
        return 0;
    }

    @Override
    public boolean isWorkingEnabled() {
        return this.isWorkingEnabled;
    }

    @Override
    public void setWorkingEnabled(boolean b) {
        this.isWorkingEnabled = b;
        markDirty();
        World world = getWorld();
        if (world != null && !world.isRemote) {
            writeCustomData(GregtechDataCodes.WORKING_ENABLED, buf -> buf.writeBoolean(isWorkingEnabled));
        }
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, @NotNull List<String> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add("等级:"+GTValues.VN[getTier()]);
        tooltip.add("收集范围:"+range);
        tooltip.add("耗电:"+(GTValues.V[getTier()]/4));
    }

}
