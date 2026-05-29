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

public class BlockFusionReactorCasing extends VariantBlock<BlockFusionReactorCasing.CasingType> {

    public BlockFusionReactorCasing() {
        super(Material.IRON);
        setTranslationKey("fusion_reactor_casing");
        setHardness(5.0F);
        setResistance(20.0F);
        setSoundType(SoundType.METAL);
        setHarvestLevel("wrench", 3);
        setDefaultState(getState(CasingType.PLASMA_CONTAINMENT_CASING));
        setRegistryName("fusion_reactor_casing");
        setCreativeTab(DrTechMain.DrTechTab);
    }

    @Override
    public boolean canCreatureSpawn(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos,
                                    @Nonnull EntityLiving.SpawnPlacementType type) {
        return false;
    }

    public enum CasingType implements IStringSerializable {
        PLASMA_CONTAINMENT_CASING("plasma_containment_casing"),
        FIRST_WALL_CASING("first_wall_casing"),
        SUPERCONDUCTING_MAGNET_CASING("superconducting_magnet_casing"),
        COOLING_CHANNEL_CASING("cooling_channel_casing"),
        TRITIUM_BREEDING_BLANKET_CASING("tritium_breeding_blanket_casing"),
        NEUTRON_CAPTURE_CASING("neutron_capture_casing"),
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
