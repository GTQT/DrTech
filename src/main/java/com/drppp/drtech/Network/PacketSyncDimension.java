package com.drppp.drtech.Network;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketSyncDimension implements IMessage {
    private int dimension;
    private BlockPos pos;

    // 无参构造函数（必须）
    public PacketSyncDimension() {}

    public PacketSyncDimension(int dimension, BlockPos pos) {
        this.dimension = dimension;
        this.pos = pos;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        // 从字节缓冲区读取数据
        dimension = buf.readInt();
        pos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
    }

    @Override
    public void toBytes(ByteBuf buf) {
        // 将数据写入字节缓冲区
        buf.writeInt(dimension);
        buf.writeInt(pos.getX());
        buf.writeInt(pos.getY());
        buf.writeInt(pos.getZ());
    }

    public static class Handler implements IMessageHandler<PacketSyncDimension, IMessage> {
        @Override
        public IMessage onMessage(PacketSyncDimension message, MessageContext ctx) {
            // 在客户端主线程执行
            if (ctx.side.isClient()) {
                Minecraft.getMinecraft().addScheduledTask(() -> {
                    EntityPlayer player = Minecraft.getMinecraft().player;
                    if (player != null) {
                        // 更新客户端玩家的维度和位置
                        player.dimension = message.dimension;
                        player.setPositionAndUpdate(
                                message.pos.getX() + 0.5,
                                message.pos.getY(),
                                message.pos.getZ() + 0.5
                        );
                    }
                });
            }
            return null; // 不需要返回消息
        }
    }
}