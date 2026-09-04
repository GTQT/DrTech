package com.drppp.drtech.lootgames.block;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import com.drppp.drtech.lootgames.api.block.BlockGameMaster;
import com.drppp.drtech.lootgames.tileentity.TileEntityPuzzleMaster;

import javax.annotation.Nullable;
import java.util.Random;

public class BlockPuzzleMaster extends BlockGameMaster {

    public BlockPuzzleMaster() {
        setRegistryName("lootgames_puzzle_master");
        setTranslationKey("lootgames.puzzle_master");
    }

    @Override
    public boolean canCreatureSpawn(IBlockState state, IBlockAccess world, BlockPos pos, EntityLiving.SpawnPlacementType type) {
        return false;
    }

    @Override
    public void randomDisplayTick(IBlockState stateIn, World worldIn, BlockPos pos, Random rand) {
        int tRnd = rand.nextInt(30);
        for (int i = 0; i <= tRnd; i++) {
            worldIn.spawnParticle(EnumParticleTypes.ENCHANTMENT_TABLE,
                    pos.getX() + 0.5F + rand.nextGaussian() * 0.8,
                    pos.getY() + rand.nextFloat(),
                    pos.getZ() + 0.5F + rand.nextGaussian() * 0.8,
                    rand.nextGaussian() * 0.02D,
                    0.5D + rand.nextGaussian() * 0.02D,
                    rand.nextGaussian() * 0.02D);
        }
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (!worldIn.isRemote) {
            TileEntity te = worldIn.getTileEntity(pos);
            if (te instanceof TileEntityPuzzleMaster) {
                ((TileEntityPuzzleMaster) te).onBlockClickedByPlayer(playerIn);
            }
        }
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileEntityPuzzleMaster();
    }
}
