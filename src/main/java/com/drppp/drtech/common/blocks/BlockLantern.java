package com.drppp.drtech.common.Blocks;

import com.drppp.drtech.DrTechMain;
import com.drppp.drtech.Tags;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockLantern extends Block {
    public static final PropertyBool HANGING = PropertyBool.create("hanging");
    private static final AxisAlignedBB STANDING_BOX = new AxisAlignedBB(5.0D / 16.0D, 0.0D, 5.0D / 16.0D, 11.0D / 16.0D, 9.0D / 16.0D, 11.0D / 16.0D);
    private static final AxisAlignedBB HANGING_BOX = new AxisAlignedBB(5.0D / 16.0D, 1.0D / 16.0D, 5.0D / 16.0D, 11.0D / 16.0D, 10.0D / 16.0D, 11.0D / 16.0D);

    public BlockLantern(String name, int lightValue) {
        super(Material.IRON);
        setRegistryName(Tags.MODID, name);
        setTranslationKey(Tags.MODID + "." + name);
        setCreativeTab(DrTechMain.DrTechTab);
        setHardness(3.5F);
        setResistance(3.5F);
        setSoundType(SoundType.METAL);
        setLightLevel(lightValue / 15.0F);
        setDefaultState(this.blockState.getBaseState().withProperty(HANGING, false));
    }

    @Override
    public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        if (facing == EnumFacing.DOWN && canHangAt(worldIn, pos)) {
            return getDefaultState().withProperty(HANGING, true);
        }
        if (canStandAt(worldIn, pos)) {
            return getDefaultState().withProperty(HANGING, false);
        }
        return getDefaultState().withProperty(HANGING, canHangAt(worldIn, pos));
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        if (!canBlockStay(worldIn, pos, state)) {
            spawnAsEntity(worldIn, pos, new ItemStack(this));
            worldIn.setBlockToAir(pos);
        }
    }

    @Override
    public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
        return canStandAt(worldIn, pos) || canHangAt(worldIn, pos);
    }

    @Override
    public boolean canPlaceBlockOnSide(World worldIn, BlockPos pos, EnumFacing side) {
        return canPlaceBlockAt(worldIn, pos);
    }

    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        if (!canBlockStay(worldIn, pos, state)) {
            spawnAsEntity(worldIn, pos, new ItemStack(this));
            worldIn.setBlockToAir(pos);
        }
    }

    private boolean canBlockStay(World world, BlockPos pos, IBlockState state) {
        return state.getValue(HANGING) ? canHangAt(world, pos) : canStandAt(world, pos);
    }

    private boolean canStandAt(World world, BlockPos pos) {
        BlockPos supportPos = pos.down();
        return world.getBlockState(supportPos).isSideSolid(world, supportPos, EnumFacing.UP);
    }

    private boolean canHangAt(World world, BlockPos pos) {
        BlockPos supportPos = pos.up();
        IBlockState supportState = world.getBlockState(supportPos);
        return supportState.isSideSolid(world, supportPos, EnumFacing.DOWN) || supportState.getBlock() == BlocksInit.BLOCK_CHAIN;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return state.getValue(HANGING) ? HANGING_BOX : STANDING_BOX;
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
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, HANGING);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(HANGING, meta == 1);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(HANGING) ? 1 : 0;
    }
}
