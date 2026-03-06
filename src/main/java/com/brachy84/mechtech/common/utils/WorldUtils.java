package com.brachy84.mechtech.common.utils;

import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class WorldUtils {

    public static List<BlockPos> getBlockBox(BlockPos center, int range) {
        System.out.println("Range: " + range);
        return getBlockBox(center.getX() - range, center.getY() - range, center.getZ() - range, center.getX() + range, center.getY() + range, center.getZ() + range);
    }

    public static List<BlockPos> getBlockBox(BlockPos posNeg, BlockPos posPos) {
        return getBlockBox(Math.min(posNeg.getX(), posPos.getX()), Math.min(posNeg.getY(), posPos.getY()), Math.min(posNeg.getZ(), posPos.getZ()), Math.max(posNeg.getX(), posPos.getX()), Math.max(posNeg.getY(), posPos.getY()), Math.max(posNeg.getZ(), posPos.getZ()));
    }

    public static List<BlockPos> getBlockBox(int x1, int y1, int z1, int x2, int y2, int z2) {
        if(x1 > x2 || y1 > y2 || z1 > z2) {
            throw new IllegalArgumentException("x1, y1 and z1 can not be larger than x2, y2 or z2");
        }
        int xD = x2 - x1, yD = y2 - y1, zD = z2 - z1;
        System.out.println("Diff: " + xD + ", " + yD + ", " + zD);
        System.out.println("Total blocks: " + (xD * yD * zD));
        List<BlockPos> posList = new ArrayList<>();
        for(int y = 0; y < yD; y++) {
            for(int x = 0; x < xD; x++) {
                for(int z = 0; z < zD; z++) {
                    posList.add(new BlockPos(x + x1, y + y1, z + z1));
                }
            }
        }
        return posList;
    }
}
