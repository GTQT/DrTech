package com.drppp.drtech.lootgames.packets;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import com.drppp.drtech.lootgames.minigame.gameoflight.TileEntityGOLMaster;

public class SMessageGOLDrawStuff implements IMessage {
    private BlockPos pos;
    private int stuffId;

    public SMessageGOLDrawStuff() {}

    public SMessageGOLDrawStuff(BlockPos pos, int stuffId) {
        this.pos = pos;
        this.stuffId = stuffId;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(pos.toLong());
        buf.writeInt(stuffId);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        pos = BlockPos.fromLong(buf.readLong());
        stuffId = buf.readInt();
    }

    public static class Handler implements IMessageHandler<SMessageGOLDrawStuff, IMessage> {
        @Override
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(SMessageGOLDrawStuff message, MessageContext ctx) {
            if (ctx.side == Side.CLIENT) {
                Minecraft.getMinecraft().addScheduledTask(() -> {
                    TileEntity te = Minecraft.getMinecraft().world.getTileEntity(message.pos);
                    if (te instanceof TileEntityGOLMaster) {
                        ((TileEntityGOLMaster) te).addStuffToDraw(new TileEntityGOLMaster.DrawInfo(
                                System.currentTimeMillis(),
                                TileEntityGOLMaster.EnumDrawStuff.values()[message.stuffId]));
                    }
                });
            }
            return null;
        }
    }
}
