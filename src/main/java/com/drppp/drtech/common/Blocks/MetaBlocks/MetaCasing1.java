package com.drppp.drtech.common.Blocks.MetaBlocks;


import com.drppp.drtech.DrTechMain;
import gregtech.api.block.VariantBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import javax.annotation.Nonnull;

public class MetaCasing1 extends VariantBlock<MetaCasing1.MetalCasingType> {

    public MetaCasing1() {
        super(Material.IRON);
        setTranslationKey("Meta_Machine_Casing");
        setHardness(2.0f);
        setResistance(5.0f);
        setSoundType(SoundType.METAL);
        setHarvestLevel("wrench", 2);
        setDefaultState(getState(MetalCasingType.NUCLEAR_PART_CASING));
        setRegistryName("meta_machine_casing1");
        setCreativeTab(DrTechMain.Mytab);
    }
    @Override
    @Nonnull
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }
    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }
    @Override
    public boolean canCreatureSpawn(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull EntityLiving.SpawnPlacementType type) {
        return false;
    }

    public enum MetalCasingType implements IStringSerializable {

        NUCLEAR_PART_CASING("nuclear_part_casing"),
        JIAO_BAN_CASING("jiao_ban_casing"),
        METAL_PRESS_CASING("metal_press_casing"),
        CABLE_PRESS_CASING("cable_press_casing"),
        SIEVE_NET_CASING("sieve_net_casing"),
        SIEVE_CASING("sieve_casing"),
        CENTRIFUGE_CASING("centrifuge_casing"),
        MATRIX_CASING("matrix_casing");
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