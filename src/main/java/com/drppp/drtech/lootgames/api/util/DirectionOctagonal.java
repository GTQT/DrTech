package com.drppp.drtech.lootgames.api.util;

import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

public enum DirectionOctagonal implements IStringSerializable {
    NORTH(0, "north", 0, -1),
    NORTH_EAST(1, "north_east", 1, -1),
    EAST(2, "east", 1, 0),
    SOUTH_EAST(3, "south_east", 1, 1),
    SOUTH(4, "south", 0, 1),
    SOUTH_WEST(5, "south_west", -1, 1),
    WEST(6, "west", -1, 0),
    NORTH_WEST(7, "north_west", -1, -1);

    private static final DirectionOctagonal[] LOOKUP = new DirectionOctagonal[values().length];

    static {
        for (DirectionOctagonal value : values()) {
            LOOKUP[value.index] = value;
        }
    }

    private final String name;
    private final int index;
    private final int offsetX;
    private final int offsetZ;

    DirectionOctagonal(int index, String name, int offsetX, int offsetZ) {
        this.index = index;
        this.name = name;
        this.offsetX = offsetX;
        this.offsetZ = offsetZ;
    }

    public static DirectionOctagonal byIndex(int index) {
        return LOOKUP[index];
    }

    @NotNull
    @Override
    public String getName() {
        return name;
    }

    public BlockPos getMasterBlockPos(BlockPos subordinatePos) {
        return subordinatePos.add(offsetX != 0 ? -offsetX : 0, 0, offsetZ != 0 ? -offsetZ : 0);
    }

    public BlockPos getOffsetBlockPos(BlockPos center) {
        return center.add(getOffsetX(), 0, getOffsetZ());
    }

    public int getIndex() {
        return index;
    }

    public int getOffsetX() {
        return offsetX;
    }

    public int getOffsetZ() {
        return offsetZ;
    }

    @Override
    public String toString() {
        return name;
    }
}
