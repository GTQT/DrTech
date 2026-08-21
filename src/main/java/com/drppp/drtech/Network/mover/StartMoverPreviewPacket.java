package com.drppp.drtech.Network.mover;

import com.drppp.drtech.DrTechMain;
import com.drppp.drtech.common.multiblock.mover.PreviewBlockData;
import com.drppp.drtech.common.multiblock.mover.MoverRotation;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.nio.charset.StandardCharsets;

public final class StartMoverPreviewPacket implements IMessage {
    private static final int MAX_BLOCKS = 16384;
    private static final int MAX_RELATIVE_COORDINATE = 1024;
    private static final int MAX_META_TILE_ID_BYTES = 128;
    private static final int MAX_PAYLOAD_BYTES = 4 * 1024 * 1024;
    private UUID sessionId;
    private int dimension;
    private BlockPos sourceController;
    private MoverRotation rotation = MoverRotation.NONE;
    private List<PreviewBlockData> blocks = Collections.emptyList();

    public StartMoverPreviewPacket() {
    }

    public StartMoverPreviewPacket(UUID sessionId, int dimension, BlockPos sourceController,
                                   MoverRotation rotation,
                                   List<PreviewBlockData> blocks) {
        this.sessionId = sessionId;
        this.dimension = dimension;
        this.sourceController = sourceController.toImmutable();
        this.rotation = rotation == null ? MoverRotation.NONE : rotation;
        this.blocks = new ArrayList<>(blocks);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        sessionId = new UUID(buf.readLong(), buf.readLong());
        dimension = buf.readInt();
        sourceController = BlockPos.fromLong(buf.readLong());
        int rotationId = buf.readUnsignedByte();
        if (rotationId > 3) throw new IllegalArgumentException("Invalid mover preview rotation");
        rotation = MoverRotation.byQuarterTurns(rotationId);
        int count = buf.readInt();
        if (count < 0 || count > MAX_BLOCKS || buf.readableBytes() < count * 11
                || buf.readableBytes() > MAX_PAYLOAD_BYTES) {
            throw new IllegalArgumentException("Invalid mover preview size");
        }
        List<PreviewBlockData> decoded = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            int x = buf.readShort();
            int y = buf.readShort();
            int z = buf.readShort();
            if (Math.abs(x) > MAX_RELATIVE_COORDINATE
                    || Math.abs(y) > MAX_RELATIVE_COORDINATE
                    || Math.abs(z) > MAX_RELATIVE_COORDINATE) {
                throw new IllegalArgumentException("Invalid mover preview coordinate");
            }
            int stateId = buf.readInt();
            IBlockState state = stateId < 0 ? null : Block.getStateById(stateId);
            if (state == null || Block.getStateId(state) != stateId) {
                throw new IllegalArgumentException("Invalid mover preview block state");
            }
            int flags = buf.readUnsignedByte();
            if ((flags & ~7) != 0 || (flags & 4) != 0 && (flags & 2) == 0) {
                throw new IllegalArgumentException("Invalid mover preview flags");
            }
            ResourceLocation metaTileId = null;
            int facing = -1;
            int paintingColor = -1;
            if ((flags & 2) != 0) {
                metaTileId = readResourceLocation(buf);
                if (!buf.isReadable()) throw new IllegalArgumentException("Missing mover MTE facing");
                facing = buf.readUnsignedByte();
                if (facing > 5) throw new IllegalArgumentException("Invalid mover MTE facing");
                if ((flags & 4) != 0) {
                    if (buf.readableBytes() < 4) throw new IllegalArgumentException("Missing mover MTE color");
                    paintingColor = buf.readInt();
                }
            }
            decoded.add(new PreviewBlockData(new BlockPos(x, y, z), stateId,
                    (flags & 1) != 0, metaTileId, facing, paintingColor));
        }
        blocks = decoded;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        if (blocks.size() > MAX_BLOCKS) throw new IllegalArgumentException("Mover preview is too large");
        int packetStart = buf.writerIndex();
        buf.writeLong(sessionId.getMostSignificantBits());
        buf.writeLong(sessionId.getLeastSignificantBits());
        buf.writeInt(dimension);
        buf.writeLong(sourceController.toLong());
        buf.writeByte(rotation.getQuarterTurns());
        buf.writeInt(blocks.size());
        for (PreviewBlockData block : blocks) {
            BlockPos relative = block.getRelativePos();
            if (Math.abs(relative.getX()) > MAX_RELATIVE_COORDINATE
                    || Math.abs(relative.getY()) > MAX_RELATIVE_COORDINATE
                    || Math.abs(relative.getZ()) > MAX_RELATIVE_COORDINATE) {
                throw new IllegalArgumentException("Mover preview coordinate exceeds protocol limits");
            }
            buf.writeShort(relative.getX());
            buf.writeShort(relative.getY());
            buf.writeShort(relative.getZ());
            buf.writeInt(block.getBlockStateId());
            int flags = block.isController() ? 1 : 0;
            if (block.getMetaTileEntityId() != null) flags |= 2;
            if (block.getPaintingColor() >= 0) flags |= 4;
            buf.writeByte(flags);
            if (block.getMetaTileEntityId() != null) {
                writeResourceLocation(buf, block.getMetaTileEntityId());
                if (block.getFrontFacing() < 0 || block.getFrontFacing() > 5) {
                    throw new IllegalArgumentException("Invalid mover MTE facing");
                }
                buf.writeByte(block.getFrontFacing());
                if (block.getPaintingColor() >= 0) buf.writeInt(block.getPaintingColor());
            }
        }
        if (buf.writerIndex() - packetStart > MAX_PAYLOAD_BYTES) {
            throw new IllegalArgumentException("Mover preview payload exceeds protocol limits");
        }
    }

    private static ResourceLocation readResourceLocation(ByteBuf buf) {
        if (!buf.isReadable()) throw new IllegalArgumentException("Missing mover MTE id");
        int length = buf.readUnsignedByte();
        if (length < 1 || length > MAX_META_TILE_ID_BYTES || buf.readableBytes() < length) {
            throw new IllegalArgumentException("Invalid mover MTE id length");
        }
        byte[] bytes = new byte[length];
        buf.readBytes(bytes);
        try {
            return new ResourceLocation(new String(bytes, StandardCharsets.UTF_8));
        } catch (RuntimeException error) {
            throw new IllegalArgumentException("Invalid mover MTE id", error);
        }
    }

    private static void writeResourceLocation(ByteBuf buf, ResourceLocation id) {
        byte[] bytes = id.toString().getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 1 || bytes.length > MAX_META_TILE_ID_BYTES) {
            throw new IllegalArgumentException("Mover MTE id exceeds protocol limits");
        }
        buf.writeByte(bytes.length);
        buf.writeBytes(bytes);
    }

    public UUID getSessionId() {
        return sessionId;
    }

    public int getDimension() {
        return dimension;
    }

    public BlockPos getSourceController() {
        return sourceController;
    }

    public MoverRotation getRotation() {
        return rotation;
    }

    public List<PreviewBlockData> getBlocks() {
        return Collections.unmodifiableList(blocks);
    }

    public static final class Handler implements IMessageHandler<StartMoverPreviewPacket, IMessage> {
        @Override
        public IMessage onMessage(StartMoverPreviewPacket message, MessageContext context) {
            IThreadListener thread = FMLCommonHandler.instance().getWorldThread(context.netHandler);
            thread.addScheduledTask(() -> DrTechMain.proxy.startMultiblockMoverPreview(message));
            return null;
        }
    }
}
