package com.drppp.drtech.hooked;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.util.Constants;

import java.util.UUID;

public class HookInfo {
    public Vec3d pos = Vec3d.ZERO;
    public Vec3d direction = Vec3d.ZERO;
    public EnumHookStatus status = EnumHookStatus.PLANTED;
    public BlockPos block;
    public EnumFacing side;
    private double rawWeight = 1.0;
    public UUID uuid = UUID.randomUUID();

    public HookInfo() {
    }

    public HookInfo(Vec3d pos, Vec3d direction, EnumHookStatus status, BlockPos block, EnumFacing side) {
        this.pos = pos;
        this.direction = direction;
        this.status = status;
        this.block = block;
        this.side = side;
    }

    public double getWeight() {
        return rawWeight == 0.0 || !Double.isFinite(rawWeight) ? 1.0 : rawWeight;
    }

    public void setWeight(double weight) {
        this.rawWeight = weight;
    }

    public NBTTagCompound serialize() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setDouble("x", pos.x);
        tag.setDouble("y", pos.y);
        tag.setDouble("z", pos.z);
        tag.setDouble("dx", direction.x);
        tag.setDouble("dy", direction.y);
        tag.setDouble("dz", direction.z);
        tag.setInteger("status", status.ordinal());
        tag.setDouble("weight", rawWeight);
        tag.setUniqueId("uuid", uuid);
        if (block != null) {
            tag.setInteger("bx", block.getX());
            tag.setInteger("by", block.getY());
            tag.setInteger("bz", block.getZ());
        }
        if (side != null) {
            tag.setInteger("side", side.getIndex());
        }
        return tag;
    }

    public static HookInfo deserialize(NBTTagCompound tag) {
        HookInfo hook = new HookInfo();
        hook.pos = new Vec3d(tag.getDouble("x"), tag.getDouble("y"), tag.getDouble("z"));
        hook.direction = new Vec3d(tag.getDouble("dx"), tag.getDouble("dy"), tag.getDouble("dz"));
        int statusIndex = Math.max(0, Math.min(tag.getInteger("status"), EnumHookStatus.values().length - 1));
        hook.status = EnumHookStatus.values()[statusIndex];
        hook.rawWeight = tag.getDouble("weight");
        hook.uuid = tag.hasUniqueId("uuid") ? tag.getUniqueId("uuid") : UUID.randomUUID();
        if (tag.hasKey("bx", Constants.NBT.TAG_INT)) {
            hook.block = new BlockPos(tag.getInteger("bx"), tag.getInteger("by"), tag.getInteger("bz"));
        }
        if (tag.hasKey("side", Constants.NBT.TAG_INT)) {
            hook.side = EnumFacing.byIndex(tag.getInteger("side"));
        }
        return hook;
    }
}
