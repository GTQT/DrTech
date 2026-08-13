package com.drppp.drtech.common.drone.program.model;

import net.minecraft.util.math.BlockPos;

import java.util.Objects;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Predicate;

/** Immutable bounded coordinate region shared by the compiler, editor and runtime. */
public final class DroneArea {

    public static final int MAX_AXIS_LENGTH = 32;
    public static final int MAX_BLOCKS = 4_096;

    private final BlockPos min;
    private final BlockPos max;
    private final int sizeX;
    private final int sizeY;
    private final int sizeZ;
    private final long volume;
    private final List<BlockPos> positions;

    private DroneArea(BlockPos first, BlockPos second) {
        Objects.requireNonNull(first, "first");
        Objects.requireNonNull(second, "second");
        min = new BlockPos(Math.min(first.getX(), second.getX()), Math.min(first.getY(), second.getY()),
                Math.min(first.getZ(), second.getZ()));
        max = new BlockPos(Math.max(first.getX(), second.getX()), Math.max(first.getY(), second.getY()),
                Math.max(first.getZ(), second.getZ()));
        sizeX = max.getX() - min.getX() + 1;
        sizeY = max.getY() - min.getY() + 1;
        sizeZ = max.getZ() - min.getZ() + 1;
        volume = (long) sizeX * sizeY * sizeZ;
        positions = null;
    }

    private DroneArea(BlockPos first, BlockPos second, Predicate<BlockPos> includes) {
        Objects.requireNonNull(includes, "includes");
        min = new BlockPos(Math.min(first.getX(), second.getX()), Math.min(first.getY(), second.getY()),
                Math.min(first.getZ(), second.getZ()));
        max = new BlockPos(Math.max(first.getX(), second.getX()), Math.max(first.getY(), second.getY()),
                Math.max(first.getZ(), second.getZ()));
        sizeX = max.getX() - min.getX() + 1;
        sizeY = max.getY() - min.getY() + 1;
        sizeZ = max.getZ() - min.getZ() + 1;
        long boundsVolume = (long) sizeX * sizeY * sizeZ;
        List<BlockPos> selected = new ArrayList<>((int) Math.min(boundsVolume, MAX_BLOCKS + 1L));
        for (int index = 0; index < boundsVolume; index++) {
            BlockPos candidate = cuboidPositionAt(index);
            if (includes.test(candidate)) selected.add(candidate);
        }
        positions = Collections.unmodifiableList(selected);
        volume = positions.size();
    }

    private DroneArea(List<BlockPos> orderedPositions) {
        Set<BlockPos> unique = new LinkedHashSet<>();
        for (BlockPos position : orderedPositions) unique.add(position.toImmutable());
        positions = Collections.unmodifiableList(new ArrayList<>(unique));
        volume = positions.size();
        if (positions.isEmpty()) {
            min = BlockPos.ORIGIN;
            max = BlockPos.ORIGIN;
            sizeX = sizeY = sizeZ = 0;
            return;
        }
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
        for (BlockPos position : positions) {
            minX = Math.min(minX, position.getX());
            minY = Math.min(minY, position.getY());
            minZ = Math.min(minZ, position.getZ());
            maxX = Math.max(maxX, position.getX());
            maxY = Math.max(maxY, position.getY());
            maxZ = Math.max(maxZ, position.getZ());
        }
        min = new BlockPos(minX, minY, minZ);
        max = new BlockPos(maxX, maxY, maxZ);
        sizeX = maxX - minX + 1;
        sizeY = maxY - minY + 1;
        sizeZ = maxZ - minZ + 1;
    }

    public static DroneArea between(BlockPos first, BlockPos second) {
        return new DroneArea(first, second);
    }

    public static DroneArea sphere(BlockPos center, int radius, boolean hollow) {
        Objects.requireNonNull(center, "center");
        if (radius < 1 || radius > 15) throw new IllegalArgumentException("Sphere radius must be 1..15");
        int outerSquared = radius * radius;
        int innerRadius = Math.max(0, radius - 1);
        int innerSquared = innerRadius * innerRadius;
        BlockPos min = center.add(-radius, -radius, -radius);
        BlockPos max = center.add(radius, radius, radius);
        return new DroneArea(min, max, position -> {
            long dx = position.getX() - center.getX();
            long dy = position.getY() - center.getY();
            long dz = position.getZ() - center.getZ();
            long distanceSquared = dx * dx + dy * dy + dz * dz;
            return distanceSquared <= outerSquared && (!hollow || distanceSquared > innerSquared);
        });
    }

    /** Creates a vertical cylinder whose center input is the center of its bottom layer. */
    public static DroneArea cylinder(BlockPos baseCenter, int radius, int height, boolean hollow) {
        Objects.requireNonNull(baseCenter, "baseCenter");
        if (radius < 1 || radius > 15) throw new IllegalArgumentException("Cylinder radius must be 1..15");
        if (height < 1 || height > MAX_AXIS_LENGTH) {
            throw new IllegalArgumentException("Cylinder height must be 1.." + MAX_AXIS_LENGTH);
        }
        int outerSquared = radius * radius;
        int innerRadius = Math.max(0, radius - 1);
        int innerSquared = innerRadius * innerRadius;
        BlockPos min = baseCenter.add(-radius, 0, -radius);
        BlockPos max = baseCenter.add(radius, height - 1, radius);
        return new DroneArea(min, max, position -> {
            long dx = position.getX() - baseCenter.getX();
            long dz = position.getZ() - baseCenter.getZ();
            long distanceSquared = dx * dx + dz * dz;
            boolean cap = position.getY() == baseCenter.getY()
                    || position.getY() == baseCenter.getY() + height - 1;
            return distanceSquared <= outerSquared
                    && (!hollow || cap || distanceSquared > innerSquared);
        });
    }

    /** Creates a deterministic voxel line with an optional spherical thickness. */
    public static DroneArea path(BlockPos first, BlockPos second, int radius) {
        Objects.requireNonNull(first, "first");
        Objects.requireNonNull(second, "second");
        if (radius < 0 || radius > 3) throw new IllegalArgumentException("Path radius must be 0..3");
        int dx = Math.abs(second.getX() - first.getX());
        int dy = Math.abs(second.getY() - first.getY());
        int dz = Math.abs(second.getZ() - first.getZ());
        int steps = Math.max(dx, Math.max(dy, dz));
        if (dx + 1 + radius * 2 > MAX_AXIS_LENGTH || dy + 1 + radius * 2 > MAX_AXIS_LENGTH
                || dz + 1 + radius * 2 > MAX_AXIS_LENGTH) {
            throw new IllegalArgumentException("Path bounding box exceeds runtime axis limit");
        }
        List<BlockPos> result = new ArrayList<>();
        for (int step = 0; step <= steps; step++) {
            double progress = steps == 0 ? 0.0D : step / (double) steps;
            int x = (int) Math.round(first.getX() + (second.getX() - first.getX()) * progress);
            int y = (int) Math.round(first.getY() + (second.getY() - first.getY()) * progress);
            int z = (int) Math.round(first.getZ() + (second.getZ() - first.getZ()) * progress);
            for (int ox = -radius; ox <= radius; ox++) {
                for (int oy = -radius; oy <= radius; oy++) {
                    for (int oz = -radius; oz <= radius; oz++) {
                        if (ox * ox + oy * oy + oz * oz <= radius * radius) {
                            result.add(new BlockPos(x + ox, y + oy, z + oz));
                        }
                    }
                }
            }
        }
        DroneArea area = new DroneArea(result);
        if (!area.isWithinRuntimeLimits()) throw new IllegalArgumentException("Path exceeds runtime area limit");
        return area;
    }

    /** Builds a voxelized parallelogram from one origin and the endpoints of its two edge vectors. */
    public static DroneArea plane(BlockPos origin, BlockPos firstEdgeEnd, BlockPos secondEdgeEnd) {
        Objects.requireNonNull(origin, "origin");
        Objects.requireNonNull(firstEdgeEnd, "firstEdgeEnd");
        Objects.requireNonNull(secondEdgeEnd, "secondEdgeEnd");
        int ux = firstEdgeEnd.getX() - origin.getX();
        int uy = firstEdgeEnd.getY() - origin.getY();
        int uz = firstEdgeEnd.getZ() - origin.getZ();
        int vx = secondEdgeEnd.getX() - origin.getX();
        int vy = secondEdgeEnd.getY() - origin.getY();
        int vz = secondEdgeEnd.getZ() - origin.getZ();
        int uSteps = Math.max(Math.abs(ux), Math.max(Math.abs(uy), Math.abs(uz)));
        int vSteps = Math.max(Math.abs(vx), Math.max(Math.abs(vy), Math.abs(vz)));
        if (uSteps >= MAX_AXIS_LENGTH || vSteps >= MAX_AXIS_LENGTH) {
            throw new IllegalArgumentException("Plane edge exceeds runtime axis limit");
        }
        List<BlockPos> result = new ArrayList<>((uSteps + 1) * (vSteps + 1));
        for (int u = 0; u <= uSteps; u++) {
            double up = uSteps == 0 ? 0.0D : u / (double) uSteps;
            for (int v = 0; v <= vSteps; v++) {
                double vp = vSteps == 0 ? 0.0D : v / (double) vSteps;
                result.add(new BlockPos(
                        (int) Math.round(origin.getX() + ux * up + vx * vp),
                        (int) Math.round(origin.getY() + uy * up + vy * vp),
                        (int) Math.round(origin.getZ() + uz * up + vz * vp)));
            }
        }
        DroneArea area = new DroneArea(result);
        if (!area.isWithinRuntimeLimits()) throw new IllegalArgumentException("Plane exceeds runtime area limit");
        return area;
    }

    public DroneArea union(DroneArea other) {
        requireMaterializable(this);
        requireMaterializable(other);
        List<BlockPos> combined = mutablePositions();
        combined.addAll(other.mutablePositions());
        DroneArea result = new DroneArea(combined);
        if (!result.isWithinRuntimeLimits()) throw new IllegalArgumentException("Union exceeds runtime area limits");
        return result;
    }

    public DroneArea intersection(DroneArea other) {
        requireMaterializable(this);
        requireMaterializable(other);
        Set<BlockPos> accepted = new LinkedHashSet<>(other.mutablePositions());
        List<BlockPos> result = new ArrayList<>();
        for (BlockPos position : mutablePositions()) if (accepted.contains(position)) result.add(position);
        return new DroneArea(result);
    }

    public DroneArea difference(DroneArea other) {
        requireMaterializable(this);
        requireMaterializable(other);
        Set<BlockPos> removed = new LinkedHashSet<>(other.mutablePositions());
        List<BlockPos> result = new ArrayList<>();
        for (BlockPos position : mutablePositions()) if (!removed.contains(position)) result.add(position);
        return new DroneArea(result);
    }

    public DroneArea offset(int x, int y, int z) {
        if (positions == null) return between(checkedAdd(min, x, y, z), checkedAdd(max, x, y, z));
        List<BlockPos> shifted = new ArrayList<>(positions.size());
        for (BlockPos position : positions) shifted.add(checkedAdd(position, x, y, z));
        return new DroneArea(shifted);
    }

    public boolean contains(BlockPos position) {
        if (position == null || volume == 0L) return false;
        if (positions != null) return positions.contains(position);
        return position.getX() >= min.getX() && position.getX() <= max.getX()
                && position.getY() >= min.getY() && position.getY() <= max.getY()
                && position.getZ() >= min.getZ() && position.getZ() <= max.getZ();
    }

    private List<BlockPos> mutablePositions() {
        List<BlockPos> result = new ArrayList<>((int) volume);
        for (int index = 0; index < volume; index++) result.add(positionAt(index));
        return result;
    }

    private static void requireMaterializable(DroneArea area) {
        Objects.requireNonNull(area, "area");
        if (!area.isWithinRuntimeLimits()) throw new IllegalArgumentException("Area exceeds runtime limits");
    }

    private static BlockPos checkedAdd(BlockPos position, int x, int y, int z) {
        long targetX = (long) position.getX() + x;
        long targetY = (long) position.getY() + y;
        long targetZ = (long) position.getZ() + z;
        if (targetX < -30_000_000L || targetX > 30_000_000L || targetY < -2_048L || targetY > 2_047L
                || targetZ < -30_000_000L || targetZ > 30_000_000L) {
            throw new IllegalArgumentException("Offset area exceeds coordinate bounds");
        }
        return new BlockPos((int) targetX, (int) targetY, (int) targetZ);
    }

    public BlockPos getMin() {
        return min;
    }

    public BlockPos getMax() {
        return max;
    }

    public int getSizeX() {
        return sizeX;
    }

    public int getSizeY() {
        return sizeY;
    }

    public int getSizeZ() {
        return sizeZ;
    }

    public long getVolume() {
        return volume;
    }

    public boolean isWithinRuntimeLimits() {
        return sizeX <= MAX_AXIS_LENGTH && sizeY <= MAX_AXIS_LENGTH && sizeZ <= MAX_AXIS_LENGTH
                && volume <= MAX_BLOCKS;
    }

    /** Y-major serpentine traversal reduces repositioning between adjacent targets. */
    public BlockPos positionAt(int index) {
        if (index < 0 || index >= volume) throw new IndexOutOfBoundsException("Area index " + index);
        return positions == null ? cuboidPositionAt(index) : positions.get(index);
    }

    private BlockPos cuboidPositionAt(int index) {
        int layerSize = sizeX * sizeZ;
        int yOffset = index / layerSize;
        int layerIndex = index % layerSize;
        int zOffset = layerIndex / sizeX;
        int xOffset = layerIndex % sizeX;
        if (((yOffset + zOffset) & 1) != 0) xOffset = sizeX - 1 - xOffset;
        return min.add(xOffset, yOffset, zOffset);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof DroneArea)) return false;
        DroneArea that = (DroneArea) other;
        return min.equals(that.min) && max.equals(that.max) && Objects.equals(positions, that.positions);
    }

    @Override
    public int hashCode() {
        return 31 * (31 * min.hashCode() + max.hashCode()) + Objects.hashCode(positions);
    }
}
