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

public class CMessageGOLFeedback implements IMessage {
    private BlockPos pos;

    public CMessageGOLFeedback() {}

    public CMessageGOLFeedback(BlockPos pos) {
        this.pos = pos;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(pos.toLong());
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        pos = BlockPos.fromLong(buf.readLong());
    }

    public static class Handler implements IMessageHandler<CMessageGOLFeedback, IMessage> {
        @Override
        public IMessage onMessage(CMessageGOLFeedback message, MessageContext ctx) {
            if (ctx.side == Side.SERVER) {
                ctx.getServerHandler().player.getServerWorld().addScheduledTask(() -> {
                    TileEntity te = ctx.getServerHandler().player.getServerWorld().getTileEntity(message.pos);
                    if (te instanceof TileEntityGOLMaster) {
                        ((TileEntityGOLMaster) te).onClientThingsDone();
                    }
                });
            }
            return null;
        }
    }
}
