package com.brachy84.mechtech.common.utils;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

public class BlockPosDim extends BlockPos {

    private final int dimension;

    public BlockPosDim(int x, int y, int z, int dimension) {
        super(x, y, z);
        this.dimension = dimension;
    }

    public BlockPosDim(double x, double y, double z, int dimension) {
        super(x, y, z);
        this.dimension = dimension;
    }

    public BlockPosDim(Entity source) {
        super(source);
        dimension = source.dimension;
    }

    public BlockPosDim(Vec3d vec, int dimension) {
        super(vec);
        this.dimension = dimension;
    }

    public BlockPosDim(Vec3i source, int dimension) {
        super(source);
        this.dimension = dimension;
    }

    public int getDim() {
        return dimension;
    }

    public double getDistance(BlockPos pos) {
        return getDistance(pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public String toString() {
        return "X:" + getX() + ", Y:" + getY() + ", Z:" + getZ() + ", Dim:" + getDim();
    }
}
