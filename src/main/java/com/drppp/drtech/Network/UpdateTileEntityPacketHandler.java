package com.drppp.drtech.Network;

import com.drppp.drtech.common.MetaTileEntities.single.MetaTileEntityIndustrialApiary;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IThreadListener;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class UpdateTileEntityPacketHandler implements IMessageHandler<UpdateTileEntityPacket, IMessage> {

    @Override
    public IMessage onMessage(UpdateTileEntityPacket message, MessageContext ctx) {
        IThreadListener mainThread = (WorldServer) ctx.getServerHandler().player.world;
        mainThread.addScheduledTask(() -> {
            WorldServer serverWorld = ctx.getServerHandler().player.getServerWorld();
            TileEntity tileEntity = serverWorld.getTileEntity(message.getPos());
            NBTTagCompound nbt = message.getNbt();
            if (tileEntity instanceof IGregTechTileEntity && nbt.hasKey("industrialApiaryAction")) {
                MetaTileEntity metaTileEntity = ((IGregTechTileEntity) tileEntity).getMetaTileEntity();
                if (metaTileEntity instanceof MetaTileEntityIndustrialApiary) {
                    ((MetaTileEntityIndustrialApiary) metaTileEntity).handleApiaryClientAction(nbt.getString("industrialApiaryAction"));
                    tileEntity.markDirty();
                }
            }
        });
        return null;
    }
}
