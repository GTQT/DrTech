package com.drppp.drtech.Tile;

import com.drppp.drtech.common.Blocks.BlockDriedGhast;
import com.drppp.drtech.common.Entity.moster.EntityHappyGhast;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TileEntityDriedGhast extends TileEntity implements ITickable {
    private static final int HYDRATION_STAGE_TICKS = 6000;
    private static final int TRANSFORM_TICKS = 24000;
    private int hydrationTicks;

    @Override
    public void update() {
        if (this.world == null || this.world.isRemote) {
            return;
        }
        if (!(this.world.getBlockState(this.pos).getBlock() instanceof BlockDriedGhast)) {
            return;
        }

        if (!isFullySubmerged(this.world, this.pos)) {
            return;
        }

        if (this.hydrationTicks < TRANSFORM_TICKS) {
            this.hydrationTicks++;
            updateHydrationStage();
            markDirty();
        }

        if (this.hydrationTicks >= TRANSFORM_TICKS) {
            transformIntoGhastling();
        }
    }

    private void updateHydrationStage() {
        IBlockState state = this.world.getBlockState(this.pos);
        int hydrationStage = Math.min(3, this.hydrationTicks / HYDRATION_STAGE_TICKS);
        if (state.getValue(BlockDriedGhast.HYDRATION) != hydrationStage) {
            this.world.setBlockState(this.pos, state.withProperty(BlockDriedGhast.HYDRATION, hydrationStage), 2);
        }
    }

    private void transformIntoGhastling() {
        if (this.world == null) {
            return;
        }

        EntityHappyGhast ghastling = new EntityHappyGhast(this.world);
        ghastling.setChildForm(true);
        ghastling.setRemainingGrowthTicks(EntityHappyGhast.DEFAULT_GROWTH_TICKS);
        ghastling.setHome(this.pos);
        ghastling.setLocationAndAngles(this.pos.getX() + 0.5D, this.pos.getY() + 0.2D, this.pos.getZ() + 0.5D,
                this.world.rand.nextFloat() * 360.0F, 0.0F);
        ghastling.enablePersistence();

        this.world.setBlockState(this.pos, Blocks.WATER.getDefaultState().withProperty(BlockLiquid.LEVEL, 0), 3);
        this.world.spawnEntity(ghastling);
    }

    public static boolean isFullySubmerged(World world, BlockPos pos) {
        for (EnumFacing facing : EnumFacing.values()) {
            if (!isWater(world.getBlockState(pos.offset(facing)))) {
                return false;
            }
        }
        return true;
    }

    private static boolean isWater(IBlockState state) {
        Block block = state.getBlock();
        return block == Blocks.WATER || block == Blocks.FLOWING_WATER;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setInteger("HydrationTicks", this.hydrationTicks);
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        this.hydrationTicks = compound.getInteger("HydrationTicks");
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newSate) {
        return oldState.getBlock() != newSate.getBlock();
    }
}
