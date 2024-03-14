package com.drppp.drtech.api.ItemHandler;

import com.drppp.drtech.api.TileEntity.TileEntityWithUI;
import com.drppp.drtech.api.Utils.DrtechUtils;
import gregtech.api.GregTechAPI;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.UIFactory;
import gregtech.api.util.GTUtility;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


public class TileEntityUIFactory extends UIFactory<TileEntityWithUI> {
    public static final TileEntityUIFactory INSTANCE = new TileEntityUIFactory();
    public void init() {
        GregTechAPI.UI_FACTORY_REGISTRY.register(999, GTUtility.gregtechId("common_tile_entity_factory"), this);
    }
    @Override
    protected ModularUI createUITemplate(TileEntityWithUI tileEntityWithUI, EntityPlayer entityPlayer) {
        return tileEntityWithUI.createUI(entityPlayer);
    }

    @Override
    @SideOnly(Side.CLIENT)
    protected TileEntityWithUI readHolderFromSyncData(PacketBuffer packetBuffer) {
        return (TileEntityWithUI)Minecraft.getMinecraft().world.getTileEntity(packetBuffer.readBlockPos());
    }

    @Override
    protected void writeHolderToSyncData(PacketBuffer packetBuffer, TileEntityWithUI tileEntityWithUI) {
        packetBuffer.writeBlockPos(tileEntityWithUI.getPos());
    }


}
