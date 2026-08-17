package com.drppp.drtech.common.drone.compat;

import com.drppp.drtech.common.drone.program.model.DroneArea;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.common.tiles.essentia.TileAlembic;
import thaumcraft.common.tiles.essentia.TileJarFillable;
import thaumcraft.common.tiles.essentia.TileSmelter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Optional TC6 implementation for the coordinate + tube-area programming node. */
public final class ThaumcraftEssentiaNodeCompat {

    public static final int INVALID_SMELTER = -1;
    public static final int UNLOADED = -2;
    public static final int NO_SOURCE = -3;
    public static final int NO_TUBE_NETWORK = -4;
    public static final int NO_DESTINATION = -5;
    private static final int JAR_CAPACITY = 250;

    public static final class TransferResult {
        private final int amountOrError;
        private final int aspectColor;

        private TransferResult(int amountOrError, int aspectColor) {
            this.amountOrError = amountOrError;
            this.aspectColor = aspectColor;
        }

        public int getAmountOrError() { return amountOrError; }
        public int getAspectColor() { return aspectColor; }
    }

    private ThaumcraftEssentiaNodeCompat() {}

    @Optional.Method(modid = "thaumcraft")
    public static TransferResult transfer(World world, BlockPos smelterPosition, DroneArea jarArea,
            int transferLimit) {
        if (world == null || world.isRemote || smelterPosition == null || jarArea == null || transferLimit <= 0) {
            return result(INVALID_SMELTER, -1);
        }
        if (!world.isBlockLoaded(smelterPosition)) return result(UNLOADED, -1);
        if (!(world.getTileEntity(smelterPosition) instanceof TileSmelter)) return result(INVALID_SMELTER, -1);

        List<TileAlembic> sources = new ArrayList<>();
        for (int offset = 1; offset <= 8; offset++) {
            BlockPos position = smelterPosition.up(offset);
            if (!world.isBlockLoaded(position)) return result(UNLOADED, -1);
            TileEntity tile = world.getTileEntity(position);
            if (!(tile instanceof TileAlembic alembic)) break;
            if (alembic.aspect != null && alembic.amount > 0) sources.add(alembic);
        }
        if (sources.isEmpty()) return result(NO_SOURCE, -1);

        Set<TileJarFillable> areaJars = new HashSet<>();
        for (int index = 0; index < jarArea.getVolume(); index++) {
            BlockPos position = jarArea.positionAt(index);
            if (!world.isBlockLoaded(position)) return result(UNLOADED, -1);
            TileEntity tile = world.getTileEntity(position);
            if (tile instanceof TileJarFillable jar) areaJars.add(jar);
        }
        if (areaJars.isEmpty()) return result(NO_DESTINATION, -1);

        int moved = 0;
        int movedAspectColor = -1;
        boolean foundReachableJar = false;
        for (TileAlembic source : sources) {
            Aspect aspect = source.aspect;
            if (aspect == null || source.amount <= 0) continue;
            Set<TileJarFillable> reachableJars = new HashSet<>(areaJars);
            if (reachableJars.isEmpty()) continue;
            foundReachableJar = true;
            List<TileJarFillable> jars = new ArrayList<>(reachableJars);
            jars.sort(Comparator
                    .comparingInt((TileJarFillable jar) -> destinationPriority(jar, aspect))
                    .thenComparingDouble(jar -> jar.getPos().distanceSq(source.getPos())));
            for (TileJarFillable jar : jars) {
                if (!canAccept(jar, aspect)) continue;
                int amount = Math.min(transferLimit - moved,
                        Math.min(source.amount, JAR_CAPACITY - jar.amount));
                if (amount <= 0 || !source.takeFromContainer(aspect, amount)) continue;
                int remainder = jar.addToContainer(aspect, amount);
                int accepted = Math.max(0, amount - Math.max(0, remainder));
                if (remainder > 0) source.addToContainer(aspect, remainder);
                moved += accepted;
                if (accepted > 0) movedAspectColor = aspect.getColor();
                if (moved >= transferLimit) return result(moved, movedAspectColor);
                if (source.amount <= 0) break;
            }
        }
        return result(foundReachableJar ? moved : NO_DESTINATION, movedAspectColor);
    }

    private static TransferResult result(int amountOrError, int aspectColor) {
        return new TransferResult(amountOrError, aspectColor);
    }

    private static int destinationPriority(TileJarFillable jar, Aspect aspect) {
        if (jar.aspectFilter == aspect) return 0;
        if (jar.aspect == aspect && jar.amount > 0) return 1;
        return 2;
    }

    private static boolean canAccept(TileJarFillable jar, Aspect aspect) {
        return jar != null && !jar.isInvalid() && jar.amount < JAR_CAPACITY
                && (jar.aspectFilter == null || jar.aspectFilter == aspect)
                && (jar.amount <= 0 || jar.aspect == aspect);
    }
}
