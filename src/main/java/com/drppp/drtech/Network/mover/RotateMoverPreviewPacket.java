package com.drppp.drtech.Network.mover;

import com.drppp.drtech.common.multiblock.mover.MoverSessionManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.UUID;

public final class RotateMoverPreviewPacket implements IMessage {
    private UUID sessionId;
    private int direction;

    public RotateMoverPreviewPacket() {
    }

    public RotateMoverPreviewPacket(UUID sessionId, int direction) {
        this.sessionId = sessionId;
        this.direction = direction < 0 ? -1 : 1;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        sessionId = new UUID(buf.readLong(), buf.readLong());
        direction = buf.readByte();
        if (direction != -1 && direction != 1) {
            throw new IllegalArgumentException("Invalid mover rotation direction");
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        if (sessionId == null || direction != -1 && direction != 1) {
            throw new IllegalArgumentException("Invalid mover rotation request");
        }
        buf.writeLong(sessionId.getMostSignificantBits());
        buf.writeLong(sessionId.getLeastSignificantBits());
        buf.writeByte(direction);
    }

    public static final class Handler implements IMessageHandler<RotateMoverPreviewPacket, IMessage> {
        @Override
        public IMessage onMessage(RotateMoverPreviewPacket message, MessageContext context) {
            EntityPlayerMP player = context.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() ->
                    MoverSessionManager.INSTANCE.rotate(
                            player, message.sessionId, message.direction));
            return null;
        }
    }
}
