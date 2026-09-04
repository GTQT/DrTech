package com.drppp.drtech.lootgames.minigame.minesweeper.block;

import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import com.drppp.drtech.lootgames.api.block.BlockGameMaster;
import com.drppp.drtech.lootgames.minigame.minesweeper.tileentity.TileEntityMSMaster;

import javax.annotation.Nullable;

public class BlockMSMaster extends BlockGameMaster {

    public BlockMSMaster() {
        setRegistryName("lootgames_ms_master");
        setTranslationKey("lootgames.ms_master");
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileEntityMSMaster();
    }
}
