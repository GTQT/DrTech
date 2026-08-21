package com.drppp.drtech.common.multiblock.mover;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class MultiblockSnapshot {
    private static final Comparator<MovingBlockSnapshot> POSITION_ORDER =
            Comparator.comparingInt((MovingBlockSnapshot block) -> block.getRelativePos().getY())
                    .thenComparingInt(block -> block.getRelativePos().getX())
                    .thenComparingInt(block -> block.getRelativePos().getZ());

    private final int dimension;
    private final BlockPos controllerPos;
    private final EnumFacing frontFacing;
    private final EnumFacing upwardsFacing;
    private final boolean flipped;
    private final List<MovingBlockSnapshot> blocks;
    private final Set<BlockPos> sourcePositions;
    private final int tileEntityCount;

    public MultiblockSnapshot(int dimension, BlockPos controllerPos, EnumFacing frontFacing,
                              EnumFacing upwardsFacing, boolean flipped,
                              List<MovingBlockSnapshot> blocks) {
        this.dimension = dimension;
        this.controllerPos = controllerPos.toImmutable();
        this.frontFacing = frontFacing;
        this.upwardsFacing = upwardsFacing;
        this.flipped = flipped;

        List<MovingBlockSnapshot> sorted = new ArrayList<>(blocks);
        sorted.sort(POSITION_ORDER);
        this.blocks = Collections.unmodifiableList(sorted);

        Set<BlockPos> positions = new HashSet<>();
        int tiles = 0;
        for (MovingBlockSnapshot block : sorted) {
            positions.add(this.controllerPos.add(block.getRelativePos()));
            if (block.getTileNbt() != null) tiles++;
        }
        this.sourcePositions = Collections.unmodifiableSet(positions);
        this.tileEntityCount = tiles;
    }

    public int getDimension() {
        return dimension;
    }

    public BlockPos getControllerPos() {
        return controllerPos;
    }

    public EnumFacing getFrontFacing() {
        return frontFacing;
    }

    public EnumFacing getUpwardsFacing() {
        return upwardsFacing;
    }

    public boolean isFlipped() {
        return flipped;
    }

    public List<MovingBlockSnapshot> getBlocks() {
        return blocks;
    }

    public Set<BlockPos> getSourcePositions() {
        return sourcePositions;
    }

    public int getBlockCount() {
        return blocks.size();
    }

    public int getTileEntityCount() {
        return tileEntityCount;
    }

    public boolean contentEquals(MultiblockSnapshot other) {
        if (other == null
                || dimension != other.dimension
                || !controllerPos.equals(other.controllerPos)
                || frontFacing != other.frontFacing
                || upwardsFacing != other.upwardsFacing
                || flipped != other.flipped
                || blocks.size() != other.blocks.size()) {
            return false;
        }
        for (int i = 0; i < blocks.size(); i++) {
            if (!blocks.get(i).contentEquals(other.blocks.get(i))) return false;
        }
        return true;
    }
}
