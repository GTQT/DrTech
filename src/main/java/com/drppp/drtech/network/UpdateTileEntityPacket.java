package com.drppp.drtech.Network;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class UpdateTileEntityPacket implements IMessage {

    private BlockPos pos;
    private NBTTagCompound nbt;

    // 默认构造函数，用于反序列化
    public UpdateTileEntityPacket() {}

    public UpdateTileEntityPacket(BlockPos pos, NBTTagCompound nbt) {
        this.pos = pos;
        this.nbt = nbt;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.pos = BlockPos.fromLong(buf.readLong());
        this.nbt = ByteBufUtils.readTag(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(pos.toLong());
        ByteBufUtils.writeTag(buf, nbt);
    }

    public BlockPos getPos() {
        return pos;
    }

    public NBTTagCompound getNbt() {
        return nbt;
    }
}