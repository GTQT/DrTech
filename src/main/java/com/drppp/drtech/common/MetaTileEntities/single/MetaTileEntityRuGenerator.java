package com.drppp.drtech.common.MetaTileEntities.single;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;

import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.drppp.drtech.api.capability.DrtechCommonCapabilities;
import com.drppp.drtech.api.capability.IRotationEnergy;
import gregtech.api.GTValues;
import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.capability.impl.EnergyContainerHandler;
import gregtech.api.gui.ModularUI;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.TieredMetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.renderer.texture.cube.SimpleSidedCubeRenderer;
import gregtech.client.utils.PipelineUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.List;


public class MetaTileEntityRuGenerator extends TieredMetaTileEntity {

    public boolean isActive = false;
    public boolean isWorkingEnabled = false;
    public int eut=0;
    protected final ICubeRenderer renderer = Textures.STEAM_TURBINE_OVERLAY;
    public MetaTileEntityRuGenerator(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, 1);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityRuGenerator(this.metaTileEntityId);
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        return null;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setBoolean("isActive", isActive);
        data.setBoolean("isWorkingEnabled", isWorkingEnabled);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
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
    public void setWorkingEnabled(boolean b) {
        this.isWorkingEnabled = b;
        markDirty();
        World world = getWorld();
        if (world != null && !world.isRemote) {
            writeCustomData(GregtechDataCodes.WORKING_ENABLED, buf -> buf.writeBoolean(isWorkingEnabled));
        }
    }
    public void setActive(boolean active) {
        if (this.isActive != active) {
            this.isActive = active;
            World world = this.getWorld();
            if (world != null && !world.isRemote) {
                this.writeCustomData(GregtechDataCodes.WORKABLE_ACTIVE, buf -> buf.writeBoolean(active));
            }
        }
    }
    @Override
    public void update() {
        super.update();
        if(!this.getWorld().isRemote)
        {
            TileEntity te = getWorld().getTileEntity(getPos().offset(getFrontFacing().getOpposite()));
            if(te!=null && te.hasCapability(DrtechCommonCapabilities.CAPABILITY_ROTATION_ENERGY,getFrontFacing()))
            {
                setActive(true);
                setWorkingEnabled(true);
                IRotationEnergy ru = te.getCapability(DrtechCommonCapabilities.CAPABILITY_ROTATION_ENERGY,getFrontFacing());
                eut = ru.getEnergyOutput()/4;
            }
            else
            {
                eut=0;
                setActive(false);
                setWorkingEnabled(false);
            }
            this.energyContainer.addEnergy(eut);
        }
    }
    @Override
    public void addInformation(ItemStack stack, World player, List<String> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add("利用RU旋转能进行发电");
        tooltip.add("RU->EU转换效率未4:1");
    }

    @Override
    protected SimpleSidedCubeRenderer getBaseRenderer() {
        return com.drppp.drtech.Client.Textures.MACHINE_CASINGS[0];
    }

    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        this.renderOverlays(renderState, translation, pipeline);
        Textures.ENERGY_OUT.renderSided(this.getFrontFacing(), renderState, translation, PipelineUtil.color(pipeline, GTValues.VC[this.getTier()]));
    }

    protected void renderOverlays(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        this.renderer.renderOrientedState(renderState, translation, pipeline, this.getFrontFacing(), isActive, isWorkingEnabled);
    }

    protected void reinitializeEnergyContainer() {
        super.reinitializeEnergyContainer();
        ((EnergyContainerHandler)this.energyContainer).setSideOutputCondition((side) -> {
            return side == this.getFrontFacing();
        });
    }
    public boolean hasFrontFacing() {
        return true;
    }
    @Override
    protected boolean isEnergyEmitter() {
        return true;
    }


    
}
