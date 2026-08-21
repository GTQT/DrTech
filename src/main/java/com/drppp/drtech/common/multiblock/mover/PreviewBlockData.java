package com.drppp.drtech.common.multiblock.mover;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;

public final class PreviewBlockData {
    private final BlockPos relativePos;
    private final int blockStateId;
    private final boolean controller;
    @Nullable
    private final ResourceLocation metaTileEntityId;
    private final int frontFacing;
    private final int paintingColor;

    public PreviewBlockData(BlockPos relativePos, int blockStateId, boolean controller,
                            @Nullable ResourceLocation metaTileEntityId,
                            int frontFacing, int paintingColor) {
        this.relativePos = relativePos.toImmutable();
        this.blockStateId = blockStateId;
        this.controller = controller;
        this.metaTileEntityId = metaTileEntityId;
        this.frontFacing = frontFacing;
        this.paintingColor = paintingColor;
    }

    public static PreviewBlockData fromSnapshot(MovingBlockSnapshot block) {
        return fromSnapshots(block, block);
    }

    public static PreviewBlockData fromSnapshots(MovingBlockSnapshot source,
                                                 MovingBlockSnapshot rendered) {
        ResourceLocation metaTileId = null;
        int facing = -1;
        int color = -1;
        NBTTagCompound tileNbt = rendered.getTileNbt();
        if (tileNbt != null && tileNbt.hasKey("MetaId", 8)) {
            try {
                metaTileId = new ResourceLocation(tileNbt.getString("MetaId"));
                NBTTagCompound metaNbt = tileNbt.getCompoundTag("MetaTileEntity");
                facing = metaNbt.getInteger("FrontFacing");
                if (metaNbt.hasKey("PaintingColor", 3)) {
                    color = metaNbt.getInteger("PaintingColor");
                }
            } catch (RuntimeException ignored) {
                metaTileId = null;
                facing = -1;
                color = -1;
            }
        }
        return new PreviewBlockData(rendered.getRelativePos(), Block.getStateId(rendered.getState()),
                source.isController(), metaTileId, facing, color);
    }

    public BlockPos getRelativePos() {
        return relativePos;
    }

    public boolean isController() {
        return controller;
    }

    public int getBlockStateId() {
        return blockStateId;
    }

    public IBlockState getBlockState() {
        return Block.getStateById(blockStateId);
    }

    @Nullable
    public ResourceLocation getMetaTileEntityId() {
        return metaTileEntityId;
    }

    public int getFrontFacing() {
        return frontFacing;
    }

    public int getPaintingColor() {
        return paintingColor;
    }
}
