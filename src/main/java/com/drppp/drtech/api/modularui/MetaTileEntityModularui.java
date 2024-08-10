package com.drppp.drtech.api.modularui;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.factory.GuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.value.sync.GuiSyncManager;
import gregtech.api.gui.ModularUI;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.TieredMetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

import java.util.UUID;

public class MetaTileEntityModularui extends TieredMetaTileEntity implements IGuiHolder {
    public UUID uid= null;
    public String name= null;
    public MetaTileEntityModularui(ResourceLocation metaTileEntityId, int tier) {
        super(metaTileEntityId, tier);
    }

    @Override
    public ModularPanel buildUI(GuiData guiData, GuiSyncManager guiSyncManager) {
        return null;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return null;
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        return null;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        if(uid!=null)
            data.setUniqueId("PlayerUUID",uid);
        if(name!=null)
            data.setString("PlayerName",name);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        if(data.hasKey("PlayerUUIDMost"))
            uid = data.getUniqueId("PlayerUUID");
        if(data.hasKey("PlayerName"))
            name = data.getString("PlayerName");
    }
    public void setUUID(EntityPlayer player) {
        this.uid = player.getUniqueID();
        this.name = player.getName();
        this.writeCustomData(1919, (b) -> {
            b.writeUniqueId(this.uid);
        });
        this.writeCustomData(1920, (b) -> {
            b.writeString(name);
        });
    }
    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == 1919)
        {
            this.uid = buf.readUniqueId();
        }
        if(dataId==1920)
        {
            this.name = buf.readString(500);
        }

    }


}
