package com.meowmel.cropQT.block;

import com.drppp.drtech.DrTechMain;
import gregtech.api.block.VariantBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import javax.annotation.Nonnull;

/**
 * 工业农场的升级单元。
 *
 * <p>五种单元做成<b>一个方块的五个 meta</b>——源端是 5 个独立的方块类、每个 12 档 tier，
 * 但我们的农场等级由控制器电压决定，组件不分档，所以 5 个 meta 就够。
 *
 * <p>各单元的效果与上限见下方的常量。
 */
public class BlockIndustrialFarmUnit extends VariantBlock<BlockIndustrialFarmUnit.UnitType> {

    public BlockIndustrialFarmUnit() {
        super(Material.IRON);
        setTranslationKey("drtech.industrial_farm_unit");
        setHardness(3.0f);
        setResistance(6.0f);
        setSoundType(SoundType.METAL);
        setHarvestLevel("wrench", 2);
        setDefaultState(getState(UnitType.ENVIRONMENTAL_ENHANCEMENT));
        setRegistryName("industrial_farm_unit");
        setCreativeTab(DrTechMain.DrTechTab);
    }

    @Override
    public boolean canCreatureSpawn(@Nonnull IBlockState state, @Nonnull IBlockAccess world,
                                    @Nonnull BlockPos pos, @Nonnull EntityLiving.SpawnPlacementType type) {
        return false;
    }

    /** 五种升级单元。顺序与结构里 `'U'` 的判定顺序一致。 */
    public enum UnitType implements IStringSerializable {
        /** 环境强化：解锁一个环境模块槽。 */
        ENVIRONMENTAL_ENHANCEMENT("environmental_enhancement"),
        /** 生长加速：加法提升生长速度，与超频单元互斥。 */
        GROWTH_ACCELERATION("growth_acceleration"),
        /** 肥料：乘法提升生长速度并加收割轮数。 */
        FERTILIZER("fertilizer"),
        /** 高级收割：乘法提升收割轮数。 */
        ADVANCED_HARVESTING("advanced_harvesting"),
        /** 超频生长加速：用 GT 的超频计算器；与生长加速单元互斥。 */
        OVERCLOCKED_GROWTH_ACCELERATION("overclocked_growth_acceleration");

        private final String name;

        UnitType(String name) {
            this.name = name;
        }

        @Override
        public @Nonnull String getName() {
            return this.name;
        }
    }

    // ==================== 各单元的效果常量 ====================
    //
    // 数值全部照搬源端，见 cropsnh-port-m7-design.md §6。

    /** 环境强化单元：上限 2，每个 +0.5× 基础耗电。 */
    public static final int ENVIRONMENTAL_MAX_COUNT = 2;
    public static final double ENVIRONMENTAL_POWER_INCREASE = 0.5d;

    /** 生长加速单元：无数量上限（受段数限制），每个 +1.0 生长速度、+1.25× 基础耗电。 */
    public static final double GROWTH_ACCELERATION_BONUS = 1.0d;
    public static final double GROWTH_ACCELERATION_POWER_INCREASE = 1.25d;

    /** 肥料单元：上限 1，生长速度 ×1.5、收割轮数 +0.5、+0.5× 基础耗电。 */
    public static final int FERTILIZER_MAX_COUNT = 1;
    public static final double FERTILIZER_GROWTH_MULTIPLIER = 0.5d;
    public static final double FERTILIZER_HARVEST_ROUND_BONUS = 0.5d;
    public static final double FERTILIZER_POWER_INCREASE = 0.5d;

    /** 高级收割单元：上限 2，每个收割轮数 ×(1+0.2)、+0.5× 基础耗电。 */
    public static final int ADVANCED_HARVESTING_MAX_COUNT = 2;
    public static final double ADVANCED_HARVESTING_ROUND_MULTIPLIER = 0.2d;
    public static final double ADVANCED_HARVESTING_POWER_INCREASE = 0.5d;

    /** 超频生长加速单元：上限 1。 */
    public static final int OVERCLOCKED_MAX_COUNT = 1;
}
