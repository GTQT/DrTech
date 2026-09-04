package com.drppp.drtech.lootgames.api.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import com.drppp.drtech.lootgames.api.tileentity.TileEntityGameMaster;

public class SMessageGameUpdate implements IMessage {
    private BlockPos pos;
    private String key;
    private NBTTagCompound compound;

    public SMessageGameUpdate() {}

    public SMessageGameUpdate(BlockPos pos, String key, NBTTagCompound compound) {
        this.pos = pos;
        this.key = key;
        this.compound = compound;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(pos.toLong());
        ByteBufUtils.writeUTF8String(buf, key);
        ByteBufUtils.writeTag(buf, compound);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        pos = BlockPos.fromLong(buf.readLong());
        key = ByteBufUtils.readUTF8String(buf);
        compound = ByteBufUtils.readTag(buf);
    }

    public static class Handler implements IMessageHandler<SMessageGameUpdate, IMessage> {
        @Override
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(SMessageGameUpdate message, MessageContext ctx) {
            if (ctx.side == Side.CLIENT) {
                Minecraft.getMinecraft().addScheduledTask(() -> {
                    TileEntity te = Minecraft.getMinecraft().world.getTileEntity(message.pos);
                    if (te instanceof TileEntityGameMaster<?>) {
                        ((TileEntityGameMaster<?>) te).getGame().onUpdatePacket(message.key, message.compound);
                    }
                });
            }
            return null;
        }
    }
}
