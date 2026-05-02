package com.drppp.drtech.common.Blocks;

import com.drppp.drtech.DrTechMain;
import com.drppp.drtech.Tags;
import net.minecraft.block.BlockRotatedPillar;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockChain extends BlockRotatedPillar {
    private static final AxisAlignedBB Y_BOX = new AxisAlignedBB(6.5D / 16.0D, 0.0D, 6.5D / 16.0D, 9.5D / 16.0D, 1.0D, 9.5D / 16.0D);
    private static final AxisAlignedBB X_BOX = new AxisAlignedBB(0.0D, 6.5D / 16.0D, 6.5D / 16.0D, 1.0D, 9.5D / 16.0D, 9.5D / 16.0D);
    private static final AxisAlignedBB Z_BOX = new AxisAlignedBB(6.5D / 16.0D, 6.5D / 16.0D, 0.0D, 9.5D / 16.0D, 9.5D / 16.0D, 1.0D);

    public BlockChain() {
        super(Material.IRON);
        setRegistryName(Tags.MODID, "chain");
        setTranslationKey(Tags.MODID + ".chain");
        setCreativeTab(DrTechMain.DrTechTab);
        setHardness(5.0F);
        setResistance(6.0F);
        setSoundType(SoundType.METAL);
        setHarvestLevel("pickaxe", 0);
        setDefaultState(this.blockState.getBaseState().withProperty(AXIS, EnumFacing.Axis.Y));
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        switch (state.getValue(AXIS)) {
            case X:
                return X_BOX;
            case Z:
                return Z_BOX;
            case Y:
            default:
                return Y_BOX;
        }
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, net.minecraft.entity.EntityLivingBase placer) {
        return getDefaultState().withProperty(AXIS, facing.getAxis());
    }
}
