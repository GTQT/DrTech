package com.drppp.drtech.common.Blocks.MetaBlocks;

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
 * 聚变堆分级外壳（一）：第一壁 2~5 档、冷却剂回路 2~5 档、超导磁体 2~4 档。
 * 变种数 ≤ 16（VariantBlock 硬限制）。
 */
public class BlockFusionReactorTieredCasing extends VariantBlock<BlockFusionReactorTieredCasing.CasingType> {

    public BlockFusionReactorTieredCasing() {
        super(Material.IRON);
        setTranslationKey("fusion_reactor_tiered_casing");
        setHardness(5.0F);
        setResistance(20.0F);
        setSoundType(SoundType.METAL);
        setHarvestLevel("wrench", 3);
        setDefaultState(getState(CasingType.FIRST_WALL_2));
        setRegistryName("fusion_reactor_tiered_casing");
        setCreativeTab(DrTechMain.DrTechTab);
    }

    @Override
    public boolean canCreatureSpawn(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos,
                                    @Nonnull EntityLiving.SpawnPlacementType type) {
        return false;
    }

    public enum CasingType implements IStringSerializable {
        FIRST_WALL_2("first_wall_2"),
        FIRST_WALL_3("first_wall_3"),
        FIRST_WALL_4("first_wall_4"),
        FIRST_WALL_5("first_wall_5"),
        COOLING_CHANNEL_2("cooling_channel_2"),
        COOLING_CHANNEL_3("cooling_channel_3"),
        COOLING_CHANNEL_4("cooling_channel_4"),
        COOLING_CHANNEL_5("cooling_channel_5"),
        SUPERCONDUCTING_MAGNET_2("superconducting_magnet_2"),
        SUPERCONDUCTING_MAGNET_3("superconducting_magnet_3"),
        SUPERCONDUCTING_MAGNET_4("superconducting_magnet_4");

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
