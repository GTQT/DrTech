package com.drppp.drtech.common.MetaTileEntities.muti.electric.generator;

import com.drppp.drtech.common.Blocks.BlocksInit;
import com.drppp.drtech.common.Blocks.MetaBlocks.BlockFusionReactorCasing;
import com.drppp.drtech.common.Blocks.MetaBlocks.BlockFusionReactorTieredCasing;
import com.drppp.drtech.common.Blocks.MetaBlocks.BlockFusionReactorTieredCasing2;
import gregtech.api.pattern.casing.CasingDefinition;
import gregtech.api.pattern.casing.CasingRegistration;
import net.minecraft.block.state.IBlockState;

import java.util.HashMap;
import java.util.Map;

/**
 * 聚变堆五类分级外壳的 CasingRegistration（分组 + 频道）。
 * 用法与 {@code GTCasingGroups.heatingCoils()} 相同：
 * {@code .tieredCasing('W', FusionCasingGroups.firstWall().group()).withChannel(FusionCasingGroups.firstWall().channel())}
 * <p>
 * 分级方块因 {@code VariantBlock} 的 16 变种上限被拆为三个方块
 * （基础 1 档 + 两个分级方块），这里统一收集为分组。
 */
public class FusionCasingGroups {

    private static CasingRegistration firstWall;
    private static CasingRegistration coolant;
    private static CasingRegistration neutronCapture;
    private static CasingRegistration tritiumBreeding;
    private static CasingRegistration magnet;

    public static CasingRegistration firstWall() {
        if (firstWall == null) firstWall = build("fusion_first_wall", FusionCasingStats.Module.FIRST_WALL);
        return firstWall;
    }

    public static CasingRegistration coolant() {
        if (coolant == null) coolant = build("fusion_coolant", FusionCasingStats.Module.COOLANT);
        return coolant;
    }

    public static CasingRegistration neutronCapture() {
        if (neutronCapture == null) neutronCapture = build("fusion_neutron_capture", FusionCasingStats.Module.NEUTRON_CAPTURE);
        return neutronCapture;
    }

    public static CasingRegistration tritiumBreeding() {
        if (tritiumBreeding == null) tritiumBreeding = build("fusion_tritium_breeding", FusionCasingStats.Module.TRITIUM_BREEDING);
        return tritiumBreeding;
    }

    public static CasingRegistration magnet() {
        if (magnet == null) magnet = build("fusion_magnet", FusionCasingStats.Module.MAGNET);
        return magnet;
    }

    private static CasingRegistration build(String groupId, FusionCasingStats.Module module) {
        Map<IBlockState, FusionCasingStats> map = new HashMap<>();
        for (BlockFusionReactorCasing.CasingType type : BlockFusionReactorCasing.CasingType.values()) {
            FusionCasingStats stats = safeOf(type);
            if (stats != null && stats.module == module) {
                map.put(BlocksInit.FUSION_REACTOR_CASING.getState(type), stats);
            }
        }
        for (BlockFusionReactorTieredCasing.CasingType type : BlockFusionReactorTieredCasing.CasingType.values()) {
            FusionCasingStats stats = FusionCasingStats.of(type);
            if (stats.module == module) {
                map.put(BlocksInit.FUSION_REACTOR_TIERED_CASING.getState(type), stats);
            }
        }
        for (BlockFusionReactorTieredCasing2.CasingType type : BlockFusionReactorTieredCasing2.CasingType.values()) {
            FusionCasingStats stats = FusionCasingStats.of(type);
            if (stats.module == module) {
                map.put(BlocksInit.FUSION_REACTOR_TIERED_CASING2.getState(type), stats);
            }
        }
        return CasingDefinition.fromMap(groupId, true, map, s -> s.tier, s -> s.registrySuffix);
    }

    private static FusionCasingStats safeOf(BlockFusionReactorCasing.CasingType type) {
        try {
            return FusionCasingStats.of(type);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
