package com.drppp.drtech.common.MetaTileEntities.muti.electric.generator;

import com.drppp.drtech.common.Blocks.MetaBlocks.BlockFusionReactorCasing;
import com.drppp.drtech.common.Blocks.MetaBlocks.BlockFusionReactorTieredCasing;
import com.drppp.drtech.common.Blocks.MetaBlocks.BlockFusionReactorTieredCasing2;

import javax.annotation.Nonnull;

/**
 * 聚变堆分级外壳的统计负载（payload）。
 * 对应《发电聚变堆》设计文档"模块升级方向"章节的数值表。
 */
public class FusionCasingStats {

    public enum Module {
        /** 第一壁：决定基础最大输出 EU/t */
        FIRST_WALL,
        /** 冷却剂回路：输出倍率 */
        COOLANT,
        /** 中子捕获：溢出中子转 EU 效率 */
        NEUTRON_CAPTURE,
        /** 氚增殖包层：DT 燃料消耗系数（越小越省） */
        TRITIUM_BREEDING,
        /** 超导磁体：建磁所需 EU */
        MAGNET
    }

    public final Module module;
    /** 档位（1 起） */
    public final int tier;
    /** 模块数值（含义随 module 而定） */
    public final double value;
    /** 方块注册名后缀，如 "first_wall_2"（用作翻译键） */
    public final String registrySuffix;

    private FusionCasingStats(Module module, int tier, double value, String registrySuffix) {
        this.module = module;
        this.tier = tier;
        this.value = value;
        this.registrySuffix = registrySuffix;
    }

    // ============ 第一壁：基础最大输出 (EU/t) ============
    private static final long[] FIRST_WALL_OUTPUT = {4_000_000L, 6_000_000L, 8_000_000L, 12_000_000L, 16_000_000L};
    // ============ 冷却剂：输出倍率 ============
    private static final double[] COOLANT_MULTIPLIER = {1.0, 1.4, 1.8, 2.2, 2.5};
    // ============ 中子捕获：转换效率 ============
    private static final double[] NEUTRON_EFFICIENCY = {0.45, 0.60, 0.75, 0.90, 1.10};
    // ============ 氚增殖：DT 消耗系数 ============
    private static final double[] BREEDING_CONSUMPTION = {0.90, 0.75, 0.65, 0.55, 0.40};
    // ============ 超导磁体：建磁 EU (G = 10^9) ============
    private static final long[] MAGNET_ENERGY = {100_000_000_000L, 80_000_000_000L, 65_000_000_000L, 50_000_000_000L};

    // ============ 构造器 ============

    public static FusionCasingStats firstWall(int tier) {
        return new FusionCasingStats(Module.FIRST_WALL, tier, FIRST_WALL_OUTPUT[tier - 1],
                "first_wall" + (tier == 1 ? "_casing" : "_" + tier));
    }

    public static FusionCasingStats coolant(int tier) {
        return new FusionCasingStats(Module.COOLANT, tier, COOLANT_MULTIPLIER[tier - 1],
                "cooling_channel" + (tier == 1 ? "_casing" : "_" + tier));
    }

    public static FusionCasingStats neutronCapture(int tier) {
        return new FusionCasingStats(Module.NEUTRON_CAPTURE, tier, NEUTRON_EFFICIENCY[tier - 1],
                "neutron_capture" + (tier == 1 ? "_casing" : "_" + tier));
    }

    public static FusionCasingStats tritiumBreeding(int tier) {
        return new FusionCasingStats(Module.TRITIUM_BREEDING, tier, BREEDING_CONSUMPTION[tier - 1],
                "tritium_breeding_blanket" + (tier == 1 ? "_casing" : "_" + tier));
    }

    public static FusionCasingStats magnet(int tier) {
        return new FusionCasingStats(Module.MAGNET, tier, MAGNET_ENERGY[tier - 1],
                "superconducting_magnet" + (tier == 1 ? "_casing" : "_" + tier));
    }

    /** 根据方块枚举变种构造对应模块/档位的统计（基础外壳：1 档） */
    @Nonnull
    public static FusionCasingStats of(BlockFusionReactorCasing.CasingType type) {
        switch (type) {
            case FIRST_WALL_CASING: return firstWall(1);
            case COOLING_CHANNEL_CASING: return coolant(1);
            case NEUTRON_CAPTURE_CASING: return neutronCapture(1);
            case TRITIUM_BREEDING_BLANKET_CASING: return tritiumBreeding(1);
            case SUPERCONDUCTING_MAGNET_CASING: return magnet(1);
            default:
                throw new IllegalArgumentException("Not a tiered fusion casing: " + type.getName());
        }
    }

    /** 根据方块枚举变种构造对应模块/档位的统计（分级外壳一：第一壁/冷却剂/磁体 2 档起） */
    @Nonnull
    public static FusionCasingStats of(BlockFusionReactorTieredCasing.CasingType type) {
        switch (type) {
            case FIRST_WALL_2: return firstWall(2);
            case FIRST_WALL_3: return firstWall(3);
            case FIRST_WALL_4: return firstWall(4);
            case FIRST_WALL_5: return firstWall(5);
            case COOLING_CHANNEL_2: return coolant(2);
            case COOLING_CHANNEL_3: return coolant(3);
            case COOLING_CHANNEL_4: return coolant(4);
            case COOLING_CHANNEL_5: return coolant(5);
            case SUPERCONDUCTING_MAGNET_2: return magnet(2);
            case SUPERCONDUCTING_MAGNET_3: return magnet(3);
            case SUPERCONDUCTING_MAGNET_4: return magnet(4);
            default:
                throw new IllegalArgumentException("Unknown tiered casing: " + type.getName());
        }
    }

    /** 根据方块枚举变种构造对应模块/档位的统计（分级外壳二：中子捕获/氚增殖 2 档起） */
    @Nonnull
    public static FusionCasingStats of(BlockFusionReactorTieredCasing2.CasingType type) {
        switch (type) {
            case NEUTRON_CAPTURE_2: return neutronCapture(2);
            case NEUTRON_CAPTURE_3: return neutronCapture(3);
            case NEUTRON_CAPTURE_4: return neutronCapture(4);
            case NEUTRON_CAPTURE_5: return neutronCapture(5);
            case TRITIUM_BREEDING_BLANKET_2: return tritiumBreeding(2);
            case TRITIUM_BREEDING_BLANKET_3: return tritiumBreeding(3);
            case TRITIUM_BREEDING_BLANKET_4: return tritiumBreeding(4);
            case TRITIUM_BREEDING_BLANKET_5: return tritiumBreeding(5);
            default:
                throw new IllegalArgumentException("Unknown tiered casing: " + type.getName());
        }
    }

    // ============ 类型化读取 ============

    /** 第一壁最大输出 EU/t */
    public long getMaxOutput() {
        return (long) value;
    }

    /** 冷却剂输出倍率 */
    public double getCoolantMultiplier() {
        return value;
    }

    /** 中子捕获转换效率 */
    public double getNeutronEfficiency() {
        return value;
    }

    /** 氚增殖 DT 消耗系数 */
    public double getFuelConsumptionMultiplier() {
        return value;
    }

    /** 超导磁体建磁 EU */
    public long getMagnetizeEU() {
        return (long) value;
    }
}
