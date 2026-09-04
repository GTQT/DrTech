package com.drppp.drtech.lootgames.minigame.gameoflight;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import com.drppp.drtech.lootgames.api.block.BlockGame;

import javax.annotation.Nullable;

public class BlockGOLMaster extends BlockGame {

    public BlockGOLMaster() {
        setRegistryName("lootgames_gol_master");
        setTranslationKey("lootgames.gol_master");
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (!worldIn.isRemote) {
            TileEntity te = worldIn.getTileEntity(pos);
            if (te instanceof TileEntityGOLMaster) {
                ((TileEntityGOLMaster) te).onBlockClickedByPlayer(playerIn);
            }
        }
        return true;
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileEntityGOLMaster();
    }
}
