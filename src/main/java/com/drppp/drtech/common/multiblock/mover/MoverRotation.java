package com.drppp.drtech.common.multiblock.mover;

import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;

public enum MoverRotation {
    NONE(0, Rotation.NONE),
    CLOCKWISE_90(1, Rotation.CLOCKWISE_90),
    CLOCKWISE_180(2, Rotation.CLOCKWISE_180),
    COUNTERCLOCKWISE_90(3, Rotation.COUNTERCLOCKWISE_90);

    private static final MoverRotation[] VALUES = values();
    private final int quarterTurns;
    private final Rotation minecraftRotation;

    MoverRotation(int quarterTurns, Rotation minecraftRotation) {
        this.quarterTurns = quarterTurns;
        this.minecraftRotation = minecraftRotation;
    }

    public int getQuarterTurns() {
        return quarterTurns;
    }

    public int getDegrees() {
        return quarterTurns * 90;
    }

    public Rotation getMinecraftRotation() {
        return minecraftRotation;
    }

    public MoverRotation inverseRotation() {
        return VALUES[(4 - quarterTurns) & 3];
    }

    /** Minimum number of 90-degree steps needed to reach this orientation. */
    public int getChargedQuarterTurns() {
        return Math.min(quarterTurns, 4 - quarterTurns);
    }

    public MoverRotation step(int direction) {
        return VALUES[Math.floorMod(quarterTurns + (direction < 0 ? -1 : 1), 4)];
    }

    public BlockPos rotate(BlockPos pos) {
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        switch (this) {
            case CLOCKWISE_90:
                return new BlockPos(-z, y, x);
            case CLOCKWISE_180:
                return new BlockPos(-x, y, -z);
            case COUNTERCLOCKWISE_90:
                return new BlockPos(z, y, -x);
            default:
                return pos.toImmutable();
        }
    }

    public BlockPos inverse(BlockPos pos) {
        return inverseRotation().rotate(pos);
    }

    public static MoverRotation byQuarterTurns(int quarterTurns) {
        return VALUES[Math.floorMod(quarterTurns, 4)];
    }
}
