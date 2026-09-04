package com.drppp.drtech.lootgames.packets;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SMessageGOLParticle implements IMessage {
    private BlockPos pos;
    private int particleId;

    public SMessageGOLParticle() {}

    public SMessageGOLParticle(BlockPos pos, int particleId) {
        this.pos = pos;
        this.particleId = particleId;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(pos.toLong());
        buf.writeInt(particleId);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        pos = BlockPos.fromLong(buf.readLong());
        particleId = buf.readInt();
    }

    public static class Handler implements IMessageHandler<SMessageGOLParticle, IMessage> {
        @Override
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(SMessageGOLParticle message, MessageContext ctx) {
            if (ctx.side == Side.CLIENT) {
                Minecraft.getMinecraft().addScheduledTask(() -> {
                    EnumParticleTypes particle = EnumParticleTypes.getParticleFromId(message.particleId);
                    if (particle != null) {
                        for (int i = 0; i < 20; i++) {
                            Minecraft.getMinecraft().world.spawnParticle(particle,
                                    message.pos.getX() + 0.5F,
                                    message.pos.getY() + 1.0F,
                                    message.pos.getZ() + 0.5F,
                                    0, 0.5, 0);
                        }
                    }
                });
            }
            return null;
        }
    }
}
