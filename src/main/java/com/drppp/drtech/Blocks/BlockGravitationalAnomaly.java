package com.drppp.drtech.Blocks;

import com.drppp.drtech.DrTechMain;
import com.drppp.drtech.Tags;
import com.drppp.drtech.Tile.TileEntityGravitationalAnomaly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockGravitationalAnomaly extends Block {
    protected AxisAlignedBB boundingBox;
    public BlockGravitationalAnomaly() {
        super(Material.IRON);
        this.setBlockUnbreakable();
        this.setResistance(5000000.0F);
        this.disableStats();
        this.setRegistryName(Tags.MODID,"gravitational_anomaly");
        this.setCreativeTab(DrTechMain.Mytab);
        this.setTranslationKey(Tags.MODID+".gravitational_anomaly");
        this.boundingBox =new AxisAlignedBB(0.30000001192092896, 0.30000001192092896, 0.30000001192092896, 0.6000000238418579, 0.6000000238418579, 0.6000000238418579);

    }
    @Nonnull
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return super.getBoundingBox(state, source, pos);
    }

    @Nullable
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
        return Block.NULL_AABB;
    }
    @Deprecated
    public boolean isNormalCube(IBlockState blockState) {
        return false;
    }

    @Deprecated
    public RayTraceResult collisionRayTrace(IBlockState state, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull Vec3d start, @Nonnull Vec3d end) {
        return super.collisionRayTrace(state, world, pos, start, end);
    }

    public boolean isPassable(IBlockAccess worldIn, BlockPos pos) {
        return true;
    }

    @Deprecated
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Deprecated
    public boolean isFullCube(IBlockState state) {
        return false;
    }
    @Nonnull
    @Deprecated
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.INVISIBLE;
    }
    public boolean canEntityDestroy(IBlockState state, IBlockAccess world, BlockPos pos, Entity entity) {
        return false;
    }
    @Nonnull
    public TileEntity createTileEntity(@Nonnull World world, @Nonnull IBlockState state) {
        return new TileEntityGravitationalAnomaly();
    }
    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        TileEntity entity = worldIn.getTileEntity(pos);
        if(entity instanceof TileEntityGravitationalAnomaly && !worldIn.isRemote)
        {
            playerIn.sendStatusMessage(new TextComponentString("Weight: " + ((TileEntityGravitationalAnomaly) entity).weight), true);
            return true;
        }
        return false;
    }
}
