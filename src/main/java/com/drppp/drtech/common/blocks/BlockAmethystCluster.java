package com.drppp.drtech.common.Blocks;

import com.drppp.drtech.DrTechMain;
import com.drppp.drtech.Tags;
import com.drppp.drtech.common.Items.ItemsInit;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.Random;

public class BlockAmethystCluster extends Block {
    public static final PropertyDirection FACING = BlockDirectional.FACING;

    private final AxisAlignedBB upBox;
    private final AxisAlignedBB downBox;
    private final AxisAlignedBB northBox;
    private final AxisAlignedBB southBox;
    private final AxisAlignedBB westBox;
    private final AxisAlignedBB eastBox;
    private final boolean cluster;

    public BlockAmethystCluster(String name, int inset, int height, int lightValue, boolean cluster) {
        super(Material.GLASS);
        setRegistryName(Tags.MODID, name);
        setTranslationKey(Tags.MODID + "." + name);
        setCreativeTab(DrTechMain.DrTechTab);
        setHardness(1.5F);
        setResistance(1.5F);
        setSoundType(SoundType.GLASS);
        setLightLevel(lightValue / 15.0F);
        setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.UP));
        this.cluster = cluster;

        double min = inset / 16.0D;
        double max = 1.0D - min;
        double depth = height / 16.0D;
        this.upBox = new AxisAlignedBB(min, 0.0D, min, max, depth, max);
        this.downBox = new AxisAlignedBB(min, 1.0D - depth, min, max, 1.0D, max);
        this.northBox = new AxisAlignedBB(min, min, 1.0D - depth, max, max, 1.0D);
        this.southBox = new AxisAlignedBB(min, min, 0.0D, max, max, depth);
        this.westBox = new AxisAlignedBB(1.0D - depth, min, min, 1.0D, max, max);
        this.eastBox = new AxisAlignedBB(0.0D, min, min, depth, max, max);
    }

    @Override
    public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, net.minecraft.entity.EntityLivingBase placer) {
        return this.getDefaultState().withProperty(FACING, facing);
    }

    @Override
    public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
        for (EnumFacing facing : EnumFacing.values()) {
            if (canAttachTo(worldIn, pos, facing)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean canPlaceBlockOnSide(World worldIn, BlockPos pos, EnumFacing side) {
        return canAttachTo(worldIn, pos, side);
    }

    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        EnumFacing facing = state.getValue(FACING);
        if (!canAttachTo(worldIn, pos, facing)) {
            worldIn.setBlockToAir(pos);
        }
    }

    private boolean canAttachTo(IBlockAccess world, BlockPos pos, EnumFacing facing) {
        BlockPos supportPos = pos.offset(facing.getOpposite());
        IBlockState supportState = world.getBlockState(supportPos);
        return supportState.isSideSolid(world, supportPos, facing);
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        switch (state.getValue(FACING)) {
            case DOWN:
                return downBox;
            case NORTH:
                return northBox;
            case SOUTH:
                return southBox;
            case WEST:
                return westBox;
            case EAST:
                return eastBox;
            case UP:
            default:
                return upBox;
        }
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
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(FACING, EnumFacing.byIndex(meta % 6));
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(FACING).getIndex();
    }

    @Override
    public IBlockState withRotation(IBlockState state, Rotation rot) {
        return state.withProperty(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    public IBlockState withMirror(IBlockState state, Mirror mirrorIn) {
        return state.withProperty(FACING, mirrorIn.mirror(state.getValue(FACING)));
    }

    @Override
    protected boolean canSilkHarvest() {
        return true;
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return cluster ? ItemsInit.AMETHYST_SHARD : Items.AIR;
    }

    @Override
    public int quantityDroppedWithBonus(int fortune, Random random) {
        if (!cluster) {
            return 0;
        }
        return 4 + random.nextInt(Math.max(1, fortune + 1));
    }
}
