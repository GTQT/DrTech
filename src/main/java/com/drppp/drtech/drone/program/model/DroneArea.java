package com.drppp.drtech.drone.program.model;

import net.minecraft.util.math.BlockPos;

import java.util.Objects;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Comparator;
import java.util.Random;
import java.util.function.Predicate;

/** Immutable bounded coordinate region shared by the compiler, editor and runtime. */
public final class DroneArea {

    public enum TraversalOrder {
        SERPENTINE,
        XYZ,
        XZY,
        YXZ,
        YZX,
        ZXY,
        ZYX,
        NEAREST_FIRST,
        BOTTOM_UP,
        REVERSE,
        TOP_DOWN,
        RANDOMIZED;

        public static TraversalOrder fromName(String name) {
            if (name == null || name.isEmpty()) return SERPENTINE;
            try {
                return valueOf(name);
            } catch (IllegalArgumentException ignored) {
                return SERPENTINE;
            }
        }
    }

    public enum CuboidSurface {
        HOLLOW,
        FRAME,
        UP,
        DOWN,
        NORTH,
        SOUTH,
        WEST,
        EAST;

        public static CuboidSurface fromName(String name) {
            if (name == null || name.isEmpty()) return HOLLOW;
            try {
                return valueOf(name);
            } catch (IllegalArgumentException ignored) {
                return HOLLOW;
            }
        }
    }

    public enum Axis {
        X,
        Y,
        Z;

        public static Axis fromName(String name) {
            if (name == null || name.isEmpty()) return Y;
            try {
                return valueOf(name);
            } catch (IllegalArgumentException ignored) {
                return Y;
            }
        }
    }

    public static final int MAX_AXIS_LENGTH = 32;
    public static final int MAX_BLOCKS = 4_096;

    private final BlockPos min;
    private final BlockPos max;
    private final int sizeX;
    private final int sizeY;
    private final int sizeZ;
    private long volume;
    private List<BlockPos> positions;
    private List<BlockPos> topDownPositions;
    private Predicate<BlockPos> pendingSelection;
    private List<BlockPos> pendingPositions;
    private long pendingBoundsVolume;
    private int pendingIndex;

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
        topDownPositions = null;
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
        pendingBoundsVolume = (long) sizeX * sizeY * sizeZ;
        pendingSelection = includes;
        pendingPositions = new ArrayList<>((int) Math.min(pendingBoundsVolume, MAX_BLOCKS + 1L));
        positions = null;
        topDownPositions = null;
        volume = -1L;
    }

    private DroneArea(List<BlockPos> orderedPositions) {
        Set<BlockPos> unique = new LinkedHashSet<>();
        for (BlockPos position : orderedPositions) unique.add(position.toImmutable());
        positions = Collections.unmodifiableList(new ArrayList<>(unique));
        topDownPositions = orderedTopDown(positions);
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

    /** Selects a cuboid shell, its edge frame, or one of its six faces. */
    public static DroneArea cuboidSurface(BlockPos first, BlockPos second, CuboidSurface surface) {
        Objects.requireNonNull(first, "first");
        Objects.requireNonNull(second, "second");
        CuboidSurface checked = surface == null ? CuboidSurface.HOLLOW : surface;
        BlockPos boundsMin = new BlockPos(Math.min(first.getX(), second.getX()),
                Math.min(first.getY(), second.getY()), Math.min(first.getZ(), second.getZ()));
        BlockPos boundsMax = new BlockPos(Math.max(first.getX(), second.getX()),
                Math.max(first.getY(), second.getY()), Math.max(first.getZ(), second.getZ()));
        if (boundsMax.getX() - boundsMin.getX() + 1 > MAX_AXIS_LENGTH
                || boundsMax.getY() - boundsMin.getY() + 1 > MAX_AXIS_LENGTH
                || boundsMax.getZ() - boundsMin.getZ() + 1 > MAX_AXIS_LENGTH) {
            throw new IllegalArgumentException("Cuboid surface exceeds runtime axis limit");
        }
        DroneArea area = new DroneArea(boundsMin, boundsMax, position -> {
            boolean xBoundary = position.getX() == boundsMin.getX() || position.getX() == boundsMax.getX();
            boolean yBoundary = position.getY() == boundsMin.getY() || position.getY() == boundsMax.getY();
            boolean zBoundary = position.getZ() == boundsMin.getZ() || position.getZ() == boundsMax.getZ();
            switch (checked) {
                case FRAME:
                    return (xBoundary ? 1 : 0) + (yBoundary ? 1 : 0) + (zBoundary ? 1 : 0) >= 2;
                case UP:
                    return position.getY() == boundsMax.getY();
                case DOWN:
                    return position.getY() == boundsMin.getY();
                case NORTH:
                    return position.getZ() == boundsMin.getZ();
                case SOUTH:
                    return position.getZ() == boundsMax.getZ();
                case WEST:
                    return position.getX() == boundsMin.getX();
                case EAST:
                    return position.getX() == boundsMax.getX();
                default:
                    return xBoundary || yBoundary || zBoundary;
            }
        });
        return area;
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
        return cylinder(baseCenter, radius, height, hollow, Axis.Y);
    }

    /** Creates a cylinder extending from its base center along the positive selected axis. */
    public static DroneArea cylinder(BlockPos baseCenter, int radius, int height, boolean hollow, Axis axis) {
        Objects.requireNonNull(baseCenter, "baseCenter");
        if (radius < 1 || radius > 15) throw new IllegalArgumentException("Cylinder radius must be 1..15");
        if (height < 1 || height > MAX_AXIS_LENGTH) {
            throw new IllegalArgumentException("Cylinder height must be 1.." + MAX_AXIS_LENGTH);
        }
        Axis checked = axis == null ? Axis.Y : axis;
        int outerSquared = radius * radius;
        int innerRadius = Math.max(0, radius - 1);
        int innerSquared = innerRadius * innerRadius;
        BlockPos min;
        BlockPos max;
        switch (checked) {
            case X:
                min = baseCenter.add(0, -radius, -radius);
                max = baseCenter.add(height - 1, radius, radius);
                break;
            case Z:
                min = baseCenter.add(-radius, -radius, 0);
                max = baseCenter.add(radius, radius, height - 1);
                break;
            default:
                min = baseCenter.add(-radius, 0, -radius);
                max = baseCenter.add(radius, height - 1, radius);
        }
        return new DroneArea(min, max, position -> {
            long firstRadial;
            long secondRadial;
            int axial;
            switch (checked) {
                case X:
                    axial = position.getX() - baseCenter.getX();
                    firstRadial = position.getY() - baseCenter.getY();
                    secondRadial = position.getZ() - baseCenter.getZ();
                    break;
                case Z:
                    axial = position.getZ() - baseCenter.getZ();
                    firstRadial = position.getX() - baseCenter.getX();
                    secondRadial = position.getY() - baseCenter.getY();
                    break;
                default:
                    axial = position.getY() - baseCenter.getY();
                    firstRadial = position.getX() - baseCenter.getX();
                    secondRadial = position.getZ() - baseCenter.getZ();
            }
            long distanceSquared = firstRadial * firstRadial + secondRadial * secondRadial;
            boolean cap = axial == 0 || axial == height - 1;
            return distanceSquared <= outerSquared
                    && (!hollow || cap || distanceSquared > innerSquared);
        });
    }

    /** Creates a square pyramid extending from its base center along the positive selected axis. */
    public static DroneArea pyramid(BlockPos baseCenter, int radius, int height, boolean hollow, Axis axis) {
        Objects.requireNonNull(baseCenter, "baseCenter");
        if (radius < 1 || radius > 15) throw new IllegalArgumentException("Pyramid radius must be 1..15");
        if (height < 1 || height > MAX_AXIS_LENGTH) {
            throw new IllegalArgumentException("Pyramid height must be 1.." + MAX_AXIS_LENGTH);
        }
        Axis checked = axis == null ? Axis.Y : axis;
        BlockPos min;
        BlockPos max;
        switch (checked) {
            case X:
                min = baseCenter.add(0, -radius, -radius);
                max = baseCenter.add(height - 1, radius, radius);
                break;
            case Z:
                min = baseCenter.add(-radius, -radius, 0);
                max = baseCenter.add(radius, radius, height - 1);
                break;
            default:
                min = baseCenter.add(-radius, 0, -radius);
                max = baseCenter.add(radius, height - 1, radius);
        }
        DroneArea area = new DroneArea(min, max, position -> {
            int layer;
            int firstRadial;
            int secondRadial;
            switch (checked) {
                case X:
                    layer = position.getX() - baseCenter.getX();
                    firstRadial = position.getY() - baseCenter.getY();
                    secondRadial = position.getZ() - baseCenter.getZ();
                    break;
                case Z:
                    layer = position.getZ() - baseCenter.getZ();
                    firstRadial = position.getX() - baseCenter.getX();
                    secondRadial = position.getY() - baseCenter.getY();
                    break;
                default:
                    layer = position.getY() - baseCenter.getY();
                    firstRadial = position.getX() - baseCenter.getX();
                    secondRadial = position.getZ() - baseCenter.getZ();
            }
            int layerRadius = height == 1 ? radius : (int) Math.ceil(
                    radius * (height - 1 - layer) / (double) (height - 1));
            boolean inside = Math.abs(firstRadial) <= layerRadius && Math.abs(secondRadial) <= layerRadius;
            return inside && (!hollow || layer == 0 || Math.abs(firstRadial) == layerRadius
                    || Math.abs(secondRadial) == layerRadius);
        });
        return area;
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

    /** Selects points aligned to per-axis spacing, anchored at the area's minimum corner. */
    public DroneArea grid(int stepX, int stepY, int stepZ) {
        if (stepX < 1 || stepX > MAX_AXIS_LENGTH || stepY < 1 || stepY > MAX_AXIS_LENGTH
                || stepZ < 1 || stepZ > MAX_AXIS_LENGTH) {
            throw new IllegalArgumentException("Grid steps must be 1.." + MAX_AXIS_LENGTH);
        }
        requireMaterializable(this);
        List<BlockPos> selected = new ArrayList<>();
        for (BlockPos position : mutablePositions()) {
            if (Math.floorMod(position.getX() - min.getX(), stepX) == 0
                    && Math.floorMod(position.getY() - min.getY(), stepY) == 0
                    && Math.floorMod(position.getZ() - min.getZ(), stepZ) == 0) {
                selected.add(position);
            }
        }
        return new DroneArea(selected);
    }

    /** Selects a stable, seed-controlled set of unique points from this area. */
    public DroneArea randomPoints(int count, long seed) {
        if (count < 0 || count > MAX_BLOCKS) {
            throw new IllegalArgumentException("Random point count must be 0.." + MAX_BLOCKS);
        }
        requireMaterializable(this);
        List<BlockPos> shuffled = mutablePositions();
        Random random = new Random(seed);
        for (int index = shuffled.size(); index > 1; index--) {
            Collections.swap(shuffled, index - 1, random.nextInt(index));
        }
        int selectedCount = Math.min(count, shuffled.size());
        return new DroneArea(new ArrayList<>(shuffled.subList(0, selectedCount)));
    }

    /** Returns the selected area's outer layers using Chebyshev neighbourhood thickness. */
    public DroneArea boundary(int thickness) {
        if (thickness < 1 || thickness > 4) {
            throw new IllegalArgumentException("Area boundary thickness must be 1..4");
        }
        requireMaterializable(this);
        DroneArea interior = inset(thickness);
        return interior.volume == 0L ? this : difference(interior);
    }

    /** Uniformly rescales the selected voxel field while keeping its minimum corner fixed. */
    public DroneArea scale(double factor) {
        if (!Double.isFinite(factor) || factor < 0.25D || factor > 4.0D) {
            throw new IllegalArgumentException("Area scale factor must be 0.25..4");
        }
        requireMaterializable(this);
        if (volume == 0L || factor == 1.0D) return this;
        int targetSizeX = Math.max(1, (int) Math.round((sizeX - 1) * factor) + 1);
        int targetSizeY = Math.max(1, (int) Math.round((sizeY - 1) * factor) + 1);
        int targetSizeZ = Math.max(1, (int) Math.round((sizeZ - 1) * factor) + 1);
        long targetVolume = (long) targetSizeX * targetSizeY * targetSizeZ;
        if (targetSizeX > MAX_AXIS_LENGTH || targetSizeY > MAX_AXIS_LENGTH
                || targetSizeZ > MAX_AXIS_LENGTH || targetVolume > MAX_BLOCKS) {
            throw new IllegalArgumentException("Scaled area exceeds runtime limits");
        }
        List<BlockPos> scaled = new ArrayList<>((int) targetVolume);
        for (int y = 0; y < targetSizeY; y++) {
            int sourceY = Math.min(sizeY - 1, (int) Math.floor(y / factor));
            for (int z = 0; z < targetSizeZ; z++) {
                int sourceZ = Math.min(sizeZ - 1, (int) Math.floor(z / factor));
                for (int x = 0; x < targetSizeX; x++) {
                    int sourceX = Math.min(sizeX - 1, (int) Math.floor(x / factor));
                    if (contains(min.add(sourceX, sourceY, sourceZ))) scaled.add(min.add(x, y, z));
                }
            }
        }
        return new DroneArea(scaled);
    }

    public DroneArea offset(int x, int y, int z) {
        ensureMaterialized();
        if (positions == null) return between(checkedAdd(min, x, y, z), checkedAdd(max, x, y, z));
        List<BlockPos> shifted = new ArrayList<>(positions.size());
        for (BlockPos position : positions) shifted.add(checkedAdd(position, x, y, z));
        return new DroneArea(shifted);
    }

    /** Expands the area by a Chebyshev radius, equivalent to adding a cube around every selected coordinate. */
    public DroneArea expand(int radius) {
        ensureMaterialized();
        if (radius < 0 || radius > 4) throw new IllegalArgumentException("Area expansion radius must be 0..4");
        if (radius == 0 || volume == 0L) return this;
        if (positions == null) {
            DroneArea result = between(checkedAdd(min, -radius, -radius, -radius),
                    checkedAdd(max, radius, radius, radius));
            if (!result.isWithinRuntimeLimits()) throw new IllegalArgumentException("Expanded area exceeds runtime limits");
            return result;
        }
        Set<BlockPos> expanded = new LinkedHashSet<>((int) Math.min(MAX_BLOCKS + 1L,
                volume * (long) (radius * 2 + 1) * (radius * 2 + 1) * (radius * 2 + 1)));
        for (BlockPos position : positions) {
            for (int x = -radius; x <= radius; x++) {
                for (int y = -radius; y <= radius; y++) {
                    for (int z = -radius; z <= radius; z++) {
                        expanded.add(checkedAdd(position, x, y, z));
                        if (expanded.size() > MAX_BLOCKS) {
                            throw new IllegalArgumentException("Expanded area exceeds runtime limits");
                        }
                    }
                }
            }
        }
        DroneArea result = new DroneArea(new ArrayList<>(expanded));
        if (!result.isWithinRuntimeLimits()) throw new IllegalArgumentException("Expanded area exceeds runtime limits");
        return result;
    }

    /** Removes coordinates that do not have a complete cube of neighbours inside the source area. */
    public DroneArea inset(int radius) {
        ensureMaterialized();
        if (radius < 0 || radius > 4) throw new IllegalArgumentException("Area inset radius must be 0..4");
        if (radius == 0 || volume == 0L) return this;
        if (positions == null) {
            if (sizeX <= radius * 2 || sizeY <= radius * 2 || sizeZ <= radius * 2) {
                return new DroneArea(Collections.emptyList());
            }
            return between(min.add(radius, radius, radius), max.add(-radius, -radius, -radius));
        }
        Set<BlockPos> source = new LinkedHashSet<>(positions);
        List<BlockPos> inset = new ArrayList<>();
        for (BlockPos position : positions) {
            boolean keep = true;
            for (int x = -radius; x <= radius && keep; x++) {
                for (int y = -radius; y <= radius && keep; y++) {
                    for (int z = -radius; z <= radius; z++) {
                        if (!source.contains(position.add(x, y, z))) {
                            keep = false;
                            break;
                        }
                    }
                }
            }
            if (keep) inset.add(position);
        }
        return new DroneArea(inset);
    }

    public boolean contains(BlockPos position) {
        ensureMaterialized();
        if (position == null || volume == 0L) return false;
        if (positions != null) return positions.contains(position);
        return position.getX() >= min.getX() && position.getX() <= max.getX()
                && position.getY() >= min.getY() && position.getY() <= max.getY()
                && position.getZ() >= min.getZ() && position.getZ() <= max.getZ();
    }

    private List<BlockPos> mutablePositions() {
        ensureMaterialized();
        List<BlockPos> result = new ArrayList<>((int) volume);
        for (int index = 0; index < volume; index++) result.add(positionAt(index));
        return result;
    }

    private static void requireMaterializable(DroneArea area) {
        Objects.requireNonNull(area, "area");
        if (!area.isWithinRuntimeLimits()) throw new IllegalArgumentException("Area exceeds runtime limits");
    }

    private static List<BlockPos> orderedTopDown(List<BlockPos> source) {
        List<BlockPos> ordered = new ArrayList<>(source);
        ordered.sort(Comparator.comparingInt(BlockPos::getY).reversed()
                .thenComparingInt(BlockPos::getZ)
                .thenComparingInt(BlockPos::getX));
        return Collections.unmodifiableList(ordered);
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
        ensureMaterialized();
        return volume;
    }

    public boolean isWithinRuntimeLimits() {
        ensureMaterialized();
        return sizeX <= MAX_AXIS_LENGTH && sizeY <= MAX_AXIS_LENGTH && sizeZ <= MAX_AXIS_LENGTH
                && volume <= MAX_BLOCKS;
    }

    /**
     * Advances a predicate-backed shape by at most {@code candidateBudget} bounding-box candidates. Plain cuboids
     * and already materialized shapes consume no budget. The runtime calls this once per value chain and yields when
     * it returns incomplete, preventing a 32^3 candidate shape from monopolising one server tick.
     */
    public int materializeStep(int candidateBudget) {
        if (pendingSelection == null || candidateBudget <= 0) return 0;
        int processed = 0;
        while (pendingIndex < pendingBoundsVolume && processed < candidateBudget) {
            BlockPos candidate = cuboidPositionAt(pendingIndex++);
            if (pendingSelection.test(candidate)) pendingPositions.add(candidate);
            processed++;
        }
        if (pendingIndex >= pendingBoundsVolume) finishMaterialization();
        return processed;
    }

    public boolean isMaterialized() { return pendingSelection == null; }

    public long getMaterializationProgress() {
        return pendingSelection == null ? Math.max(0L, volume) : pendingIndex;
    }

    public long getMaterializationTotal() {
        return pendingSelection == null ? Math.max(0L, volume) : pendingBoundsVolume;
    }

    private void ensureMaterialized() {
        while (pendingSelection != null) materializeStep(1_024);
    }

    private void finishMaterialization() {
        positions = Collections.unmodifiableList(new ArrayList<>(pendingPositions));
        topDownPositions = orderedTopDown(positions);
        volume = positions.size();
        pendingSelection = null;
        pendingPositions = null;
        pendingBoundsVolume = 0L;
        pendingIndex = 0;
    }

    /** Y-major serpentine traversal reduces repositioning between adjacent targets. */
    public BlockPos positionAt(int index) {
        ensureMaterialized();
        if (index < 0 || index >= volume) throw new IndexOutOfBoundsException("Area index " + index);
        return positions == null ? cuboidPositionAt(index) : positions.get(index);
    }

    /** Returns a coordinate using a deterministic traversal strategy without mutating the area. */
    public BlockPos positionAt(int index, TraversalOrder order) {
        ensureMaterialized();
        if (index < 0 || index >= volume) throw new IndexOutOfBoundsException("Area index " + index);
        TraversalOrder checked = order == null ? TraversalOrder.SERPENTINE : order;
        int size = (int) volume;
        switch (checked) {
            case REVERSE:
                return positionAt(size - 1 - index);
            case TOP_DOWN:
                if (positions != null) return topDownPositions.get(index);
                int layerSize = sizeX * sizeZ;
                int sourceLayer = sizeY - 1 - index / layerSize;
                int layerIndex = index % layerSize;
                return cuboidPositionAt(sourceLayer * layerSize + layerIndex);
            case RANDOMIZED:
                int step = Math.max(1, size / 2 + 1);
                while (greatestCommonDivisor(step, size) != 1) step++;
                int offset = Math.floorMod(hashCode(), size);
                return positionAt((int) ((offset + (long) index * step) % size));
            default:
                return positionAt(index);
        }
    }

    /**
     * Builds a stable base-index permutation. Axis names list the fastest-changing axis first; nearest traversal uses
     * the supplied position only as its initial sort origin, so callers can persist the result for the whole loop.
     */
    public int[] traversalIndices(TraversalOrder order, BlockPos origin) {
        ensureMaterialized();
        TraversalOrder checked = order == null ? TraversalOrder.SERPENTINE : order;
        int size = (int) volume;
        int[] result = new int[size];
        for (int index = 0; index < size; index++) result[index] = index;
        if (size < 2 || checked == TraversalOrder.SERPENTINE) return result;
        if (checked == TraversalOrder.REVERSE) {
            for (int index = 0; index < size; index++) result[index] = size - 1 - index;
            return result;
        }
        if (checked == TraversalOrder.RANDOMIZED) {
            int step = Math.max(1, size / 2 + 1);
            while (greatestCommonDivisor(step, size) != 1) step++;
            int offset = Math.floorMod(hashCode(), size);
            for (int index = 0; index < size; index++) {
                result[index] = (int) ((offset + (long) index * step) % size);
            }
            return result;
        }
        List<Integer> indices = new ArrayList<>(size);
        for (int index = 0; index < size; index++) indices.add(index);
        Comparator<BlockPos> positionsComparator = traversalComparator(checked,
                origin == null ? BlockPos.ORIGIN : origin);
        indices.sort((left, right) -> positionsComparator.compare(positionAt(left), positionAt(right)));
        for (int index = 0; index < size; index++) result[index] = indices.get(index);
        return result;
    }

    private static Comparator<BlockPos> traversalComparator(TraversalOrder order, BlockPos origin) {
        switch (order) {
            case XYZ:
                return axes(BlockPos::getZ, BlockPos::getY, BlockPos::getX);
            case XZY:
                return axes(BlockPos::getY, BlockPos::getZ, BlockPos::getX);
            case YXZ:
                return axes(BlockPos::getZ, BlockPos::getX, BlockPos::getY);
            case YZX:
                return axes(BlockPos::getX, BlockPos::getZ, BlockPos::getY);
            case ZXY:
                return axes(BlockPos::getY, BlockPos::getX, BlockPos::getZ);
            case ZYX:
                return axes(BlockPos::getX, BlockPos::getY, BlockPos::getZ);
            case BOTTOM_UP:
                return axes(BlockPos::getY, BlockPos::getZ, BlockPos::getX);
            case TOP_DOWN:
                return Comparator.comparingInt(BlockPos::getY).reversed()
                        .thenComparingInt(BlockPos::getZ).thenComparingInt(BlockPos::getX);
            case NEAREST_FIRST:
                return Comparator.comparingLong((BlockPos position) -> squaredDistance(position, origin))
                        .thenComparingInt(BlockPos::getY).thenComparingInt(BlockPos::getZ)
                        .thenComparingInt(BlockPos::getX);
            default:
                return axes(BlockPos::getY, BlockPos::getZ, BlockPos::getX);
        }
    }

    private static Comparator<BlockPos> axes(java.util.function.ToIntFunction<BlockPos> slowest,
            java.util.function.ToIntFunction<BlockPos> middle,
            java.util.function.ToIntFunction<BlockPos> fastest) {
        return Comparator.comparingInt(slowest).thenComparingInt(middle).thenComparingInt(fastest);
    }

    private static long squaredDistance(BlockPos position, BlockPos origin) {
        long x = (long) position.getX() - origin.getX();
        long y = (long) position.getY() - origin.getY();
        long z = (long) position.getZ() - origin.getZ();
        return x * x + y * y + z * z;
    }

    private static int greatestCommonDivisor(int first, int second) {
        int a = Math.abs(first);
        int b = Math.abs(second);
        while (b != 0) {
            int remainder = a % b;
            a = b;
            b = remainder;
        }
        return a;
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
        ensureMaterialized();
        that.ensureMaterialized();
        return min.equals(that.min) && max.equals(that.max) && Objects.equals(positions, that.positions);
    }

    @Override
    public int hashCode() {
        ensureMaterialized();
        return 31 * (31 * min.hashCode() + max.hashCode()) + Objects.hashCode(positions);
    }
}
