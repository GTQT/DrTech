package com.drppp.drtech.Network;

import com.drppp.drtech.Tile.TileEntityConnector;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class UpdateTileEntityPacketClinetHandler implements IMessageHandler<UpdateTileEntityPacket, IMessage> {

    @Override
    public IMessage onMessage(UpdateTileEntityPacket message, MessageContext ctx) {
        Minecraft.getMinecraft().addScheduledTask(() -> {
            WorldClient serverClient= Minecraft.getMinecraft().world;
            TileEntity tileEntity = serverClient.getTileEntity(message.getPos());
            if (tileEntity != null && tileEntity instanceof TileEntityConnector) {
                NBTTagCompound nbt = message.getNbt();
                if(nbt.hasKey("ClearPos"))
                {
                    ((TileEntityConnector)tileEntity).beforePos = null;
                }
                tileEntity.markDirty();
            }
        });
        return null; // 不需要响应
    }
}