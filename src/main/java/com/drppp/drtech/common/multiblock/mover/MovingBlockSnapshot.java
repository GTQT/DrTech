package com.drppp.drtech.common.multiblock.mover;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.Objects;

public final class MovingBlockSnapshot {
    private final BlockPos relativePos;
    private final BlockPos sourceRelativePos;
    private final IBlockState state;
    @Nullable
    private final NBTTagCompound tileNbt;
    @Nullable
    private final ResourceLocation tileAdapterId;
    private final boolean controller;

    public MovingBlockSnapshot(BlockPos relativePos, IBlockState state,
                               @Nullable NBTTagCompound tileNbt,
                               @Nullable ResourceLocation tileAdapterId, boolean controller) {
        this(relativePos, relativePos, state, tileNbt, tileAdapterId, controller);
    }

    public MovingBlockSnapshot(BlockPos relativePos, BlockPos sourceRelativePos,
                               IBlockState state, @Nullable NBTTagCompound tileNbt,
                               @Nullable ResourceLocation tileAdapterId, boolean controller) {
        this.relativePos = relativePos.toImmutable();
        this.sourceRelativePos = sourceRelativePos.toImmutable();
        this.state = state;
        this.tileNbt = tileNbt == null ? null : tileNbt.copy();
        this.tileAdapterId = tileAdapterId;
        this.controller = controller;
    }

    public BlockPos getRelativePos() {
        return relativePos;
    }

    public BlockPos getSourceRelativePos() {
        return sourceRelativePos;
    }

    public IBlockState getState() {
        return state;
    }

    @Nullable
    public NBTTagCompound getTileNbt() {
        return tileNbt == null ? null : tileNbt.copy();
    }

    @Nullable
    public ResourceLocation getTileAdapterId() {
        return tileAdapterId;
    }

    public boolean isController() {
        return controller;
    }

    public boolean contentEquals(MovingBlockSnapshot other) {
        return other != null
                && relativePos.equals(other.relativePos)
                && sourceRelativePos.equals(other.sourceRelativePos)
                && state.equals(other.state)
                && Objects.equals(tileNbt, other.tileNbt)
                && Objects.equals(tileAdapterId, other.tileAdapterId)
                && controller == other.controller;
    }
}
