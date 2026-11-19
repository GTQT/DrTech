package com.drppp.drtech.personalspace.world;

import javax.annotation.Nullable;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;

/**
 * 道路网格：把一个私人维度的地表划分成“地块 + 道路”。
 *
 * <p>与旧实现的区别：所有参数都以“格”为单位，而不是以区块为单位。
 * <ul>
 *     <li>地块 X / 地块 Z：每块耕地/空地沿对应轴的边长（格）。</li>
 *     <li>路宽：道路宽度（格），0 表示不生成道路，只画地块边界。</li>
 * </ul>
 * 一个周期为 {@code 地块 + 路宽}，道路占用周期末尾的 {@code 路宽} 格。
 * 道路两侧各有一格路缘（方块 B），中间是虚线（方块 C，路宽 ≥ 4 时生效），
 * 十字路口只铺路面，不画路缘和虚线。
 */
public final class RoadGrid {

    public static final int MAX_PLOT = 512;
    public static final int MAX_ROAD_WIDTH = 31;

    /** 虚线周期（格）与实线段长度（格）。 */
    private static final int DASH_PERIOD = 8;
    private static final int DASH_LENGTH = 4;

    public final int plotX;
    public final int plotZ;
    public final int roadWidth;
    public final int periodX;
    public final int periodZ;
    public final boolean roadsEnabled;
    public final boolean bordersEnabled;

    private final IBlockState roadSurface;
    private final IBlockState roadCurb;
    private final IBlockState roadDash;
    private final IBlockState borderA;
    private final IBlockState borderB;
    private final boolean roadMarkings;
    private final IBlockState centerMarker;
    private final boolean centerEnabled;
    private final int centerX;
    private final int centerZ;
    private final boolean hasCenterMarker;

    private RoadGrid(DimensionConfig config) {
        this.plotX = clamp(config.getBoundaryChunkIntervalX(), 0, MAX_PLOT);
        this.plotZ = clamp(config.getBoundaryChunkIntervalZ(), 0, MAX_PLOT);
        this.roadWidth = clamp(config.getGapWidth(), 0, MAX_ROAD_WIDTH);
        this.periodX = plotX + roadWidth;
        this.periodZ = plotZ + roadWidth;
        this.roadsEnabled = roadWidth > 0 && plotX > 0 && plotZ > 0;
        this.bordersEnabled = !roadsEnabled && plotX > 1 && plotZ > 1;

        this.roadSurface = config.stateFromSelection(config.getGapBlockA(), config.getGapMetaA());
        this.roadCurb = config.stateFromSelection(config.getGapBlockB(), config.getGapMetaB());
        this.roadDash = config.stateFromSelection(config.getGapBlockC(), config.getGapMetaC());
        this.borderA = config.stateFromSelection(config.getBoundaryBlockA(), config.getBoundaryMetaA());
        this.borderB = config.stateFromSelection(config.getBoundaryBlockB(), config.getBoundaryMetaB());
        this.roadMarkings = config.getGapPreset() == DimensionConfig.GapPreset.ROAD;
        this.centerMarker = config.stateFromSelection(config.getCenterBlock(), config.getCenterMeta());

        DimensionConfig.CenterDirection direction = config.getCenterDirection();
        this.centerX = plotX / 2 + direction.getDirOffX();
        this.centerZ = plotZ / 2 + direction.getDirOffZ();
        this.centerEnabled = config.isCenterEnabled() && plotX > 0 && plotZ > 0;
        this.hasCenterMarker = centerEnabled && usable(centerMarker);
    }

    public static RoadGrid of(DimensionConfig config) {
        return new RoadGrid(config);
    }

    /**
     * 返回该列地表应该覆盖的方块；{@code null} 表示保持地形原本的顶层方块。
     */
    @Nullable
    public IBlockState stateAt(int worldX, int worldZ) {
        if (periodX <= 0 || periodZ <= 0) {
            return null;
        }
        int rx = floorMod(worldX, periodX);
        int rz = floorMod(worldZ, periodZ);

        if (roadsEnabled) {
            boolean inRoadX = rx >= plotX;
            boolean inRoadZ = rz >= plotZ;
            if (inRoadX && inRoadZ) {
                if (usable(roadSurface)) {
                    return roadSurface;
                }
            } else if (inRoadX) {
                IBlockState state = roadState(rx - plotX, worldZ);
                if (state != null) {
                    return state;
                }
            } else if (inRoadZ) {
                IBlockState state = roadState(rz - plotZ, worldX);
                if (state != null) {
                    return state;
                }
            }
        } else if (bordersEnabled) {
            if (rx == 0 || rx == plotX - 1 || rz == 0 || rz == plotZ - 1) {
                IBlockState state = borderState(rx == 0 || rx == plotX - 1 ? rz : rx, rx == 0 || rx == plotX - 1);
                if (state != null) {
                    return state;
                }
            }
        }

        if (hasCenterMarker && rx == centerX && rz == centerZ) {
            return centerMarker;
        }
        return null;
    }

    @Nullable
    private IBlockState roadState(int offset, int alongRoad) {
        if (!usable(roadSurface)) {
            return null;
        }
        if (!roadMarkings) {
            return roadSurface;
        }
        if (roadWidth >= 2 && (offset == 0 || offset == roadWidth - 1)) {
            return usable(roadCurb) ? roadCurb : roadSurface;
        }
        if (roadWidth >= 4 && usable(roadDash)) {
            int center = roadWidth / 2;
            boolean onCenterLine = (roadWidth & 1) == 1
                    ? offset == center
                    : offset == center - 1 || offset == center;
            if (onCenterLine && floorMod(alongRoad + 2, DASH_PERIOD) < DASH_LENGTH) {
                return roadDash;
            }
        }
        return roadSurface;
    }

    @Nullable
    private IBlockState borderState(int alongBorder, boolean alongX) {
        boolean useA = (alongBorder & 1) == 0;
        IBlockState first = useA ? borderA : borderB;
        IBlockState second = useA ? borderB : borderA;
        if (usable(first)) {
            return first;
        }
        return usable(second) ? second : null;
    }

    private static boolean usable(@Nullable IBlockState state) {
        return state != null && state.getBlock() != Blocks.AIR;
    }

    private static int floorMod(int value, int modulus) {
        int result = value % modulus;
        return result < 0 ? result + modulus : result;
    }

    private static int clamp(int value, int min, int max) {
        return value < min ? min : (value > max ? max : value);
    }
}
