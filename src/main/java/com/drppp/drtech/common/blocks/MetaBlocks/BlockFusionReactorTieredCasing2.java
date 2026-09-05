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
 * 聚变堆分级外壳（二）：中子捕获 2~5 档、氚增殖包层 2~5 档。
 * 变种数 ≤ 16（VariantBlock 硬限制）。
 */
public class BlockFusionReactorTieredCasing2 extends VariantBlock<BlockFusionReactorTieredCasing2.CasingType> {

    public BlockFusionReactorTieredCasing2() {
        super(Material.IRON);
        setTranslationKey("fusion_reactor_tiered_casing2");
        setHardness(5.0F);
        setResistance(20.0F);
        setSoundType(SoundType.METAL);
        setHarvestLevel("wrench", 3);
        setDefaultState(getState(CasingType.NEUTRON_CAPTURE_2));
        setRegistryName("fusion_reactor_tiered_casing2");
        setCreativeTab(DrTechMain.DrTechTab);
    }

    @Override
    public boolean canCreatureSpawn(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos,
                                    @Nonnull EntityLiving.SpawnPlacementType type) {
        return false;
    }

    public enum CasingType implements IStringSerializable {
        NEUTRON_CAPTURE_2("neutron_capture_2"),
        NEUTRON_CAPTURE_3("neutron_capture_3"),
        NEUTRON_CAPTURE_4("neutron_capture_4"),
        NEUTRON_CAPTURE_5("neutron_capture_5"),
        TRITIUM_BREEDING_BLANKET_2("tritium_breeding_blanket_2"),
        TRITIUM_BREEDING_BLANKET_3("tritium_breeding_blanket_3"),
        TRITIUM_BREEDING_BLANKET_4("tritium_breeding_blanket_4"),
        TRITIUM_BREEDING_BLANKET_5("tritium_breeding_blanket_5");

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
