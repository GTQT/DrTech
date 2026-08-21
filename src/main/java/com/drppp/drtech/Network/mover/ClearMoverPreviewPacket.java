package com.drppp.drtech.Network.mover;

import com.drppp.drtech.DrTechMain;
import io.netty.buffer.ByteBuf;
import net.minecraft.util.IThreadListener;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.UUID;

public final class ClearMoverPreviewPacket implements IMessage {
    private UUID sessionId;

    public ClearMoverPreviewPacket() {
    }

    public ClearMoverPreviewPacket(UUID sessionId) {
        this.sessionId = sessionId;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        sessionId = new UUID(buf.readLong(), buf.readLong());
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(sessionId.getMostSignificantBits());
        buf.writeLong(sessionId.getLeastSignificantBits());
    }

    public UUID getSessionId() {
        return sessionId;
    }

    public static final class Handler implements IMessageHandler<ClearMoverPreviewPacket, IMessage> {
        @Override
        public IMessage onMessage(ClearMoverPreviewPacket message, MessageContext context) {
            IThreadListener thread = FMLCommonHandler.instance().getWorldThread(context.netHandler);
            thread.addScheduledTask(() -> DrTechMain.proxy.clearMultiblockMoverPreview(message));
            return null;
        }
    }
}
