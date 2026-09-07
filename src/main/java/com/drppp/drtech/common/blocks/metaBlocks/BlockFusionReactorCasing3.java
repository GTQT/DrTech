package com.drppp.drtech.common.blocks.metaBlocks;

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
 * 聚变堆外壳（三）：无档位独立方块（等离子约束腔体、辐射屏蔽外壳、RF 系列）。
 * 变种数 ≤ 16（VariantBlock 硬限制）。
 */
public class BlockFusionReactorCasing3 extends VariantBlock<BlockFusionReactorCasing3.CasingType> {

    public BlockFusionReactorCasing3() {
        super(Material.IRON);
        setTranslationKey("fusion_reactor_casing3");
        setHardness(5.0F);
        setResistance(20.0F);
        setSoundType(SoundType.METAL);
        setHarvestLevel("wrench", 3);
        setDefaultState(getState(CasingType.PLASMA_CONTAINMENT_CASING));
        setRegistryName("fusion_reactor_casing3");
        setCreativeTab(DrTechMain.DrTechTab);
    }

    @Override
    public boolean canCreatureSpawn(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos,
                                    @Nonnull EntityLiving.SpawnPlacementType type) {
        return false;
    }

    public enum CasingType implements IStringSerializable {
        PLASMA_CONTAINMENT_CASING("plasma_containment_casing"),
        RADIATION_SHIELDING_CASING("radiation_shielding_casing"),
        RF_DEVICE_CASING("rf_device_casing"),
        RF_WAVEGUIDE_CASING("rf_waveguide_casing"),
        RF_CAPACITOR_CASING("rf_capacitor_casing"),
        RF_PHASE_SYNCHRONIZER_CASING("rf_phase_synchronizer_casing"),
        RF_CERAMIC_WINDOW_CASING("rf_ceramic_window_casing");

        private final String name;

        CasingType(String name) {
            this.name = name;
        }

        @Nonnull
        @Override
        public String getName() {
            return this.name;
        }
    }
}
