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

public class MetaCasing extends VariantBlock<MetaCasing.MetalCasingType> {

    public MetaCasing() {
        super(Material.IRON);
        setTranslationKey("Meta_Machine_Casing");
        setHardness(2.0f);
        setResistance(5.0f);
        setSoundType(SoundType.METAL);
        setHarvestLevel("wrench", 2);
        setDefaultState(getState(MetalCasingType.GRAVITATION_FIELD_CASING));
        setRegistryName("meta_machine_casing");
        setCreativeTab(DrTechMain.Mytab);
    }

    @Override
    public boolean canCreatureSpawn(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull EntityLiving.SpawnPlacementType type) {
        return false;
    }

    public enum MetalCasingType implements IStringSerializable {

        GRAVITATION_FIELD_CASING("gravitation_field_casing"),
        NEUTRON_MACHINE_CASING("neutron_mechanical_casing"),
        MASS_GENERATION_CASING("mass_generation_casing"),
        MASS_GENERATION_COIL_CASING("mass_generation_coil_casing"),
        BUNCHER_CASING("buncher_casing"),
        RESONATOR_CASING("resonator_casing"),
        HIGH_VOLTAGE_CAPACITOR_BLOCK_CASING("high_voltage_capacitor_block_casing"),
        ELEMENT_CONSTRAINS_MACHINE_CASING("element_constrains_machine_casing"),
        ASEPTIC_MACHINE_CASING("aseptic_machine_casing"),
        YOT_TANK_CASING("yot_tank_casing"),
        TFFT_CASING("tfft_casing"),
        HEAT_CUT_OFF_CASING("heat_cut_off_casing"),
        SOLAR_TOWER_CASING("solar_tower_casing"),
        SALT_INHIBITION_CASING("salt_inhibition_casing"),
        HEAT_INHIBITION_CASING("heat_inhibition_casing"),
        SOLAR_REFLECTION_CASING("solar_reflection_casing");
        private final String name;

        MetalCasingType(String name) {
            this.name = name;
        }

        @Nonnull
        @Override
        public String getName() {
            return this.name;
        }

    }

}