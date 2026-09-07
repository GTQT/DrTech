package com.drppp.drtech.lootgames.minigame.minesweeper.block;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import com.drppp.drtech.lootgames.api.block.BlockGame;
import com.drppp.drtech.lootgames.config.LGConfigMinesweeper;
import com.drppp.drtech.lootgames.registry.ModBlocks;
import com.drppp.drtech.Client.Sound.SoundManager;

import java.util.Objects;

public class BlockMSActivator extends BlockGame {

    public BlockMSActivator() {
        setRegistryName("lootgames_ms_activator");
        setTranslationKey("lootgames.ms_activator");
    }

    public static void generateGameStructure(World world, BlockPos centerPos, int level) {
        int size;
        int bombCount;
        // Level config
        switch (level) {
            case 1: size = 7; bombCount = 6; break;
            case 2: size = 9; bombCount = 12; break;
            case 3: size = 11; bombCount = 20; break;
            case 4: size = 13; bombCount = 30; break;
            default: size = 7; bombCount = 6; break;
        }

        BlockPos startPos = centerPos.add(-size / 2, 0, -size / 2);

        for (int x = 0; x < size; x++) {
            for (int z = 0; z < size; z++) {
                BlockPos pos = startPos.add(x, 0, z);
                if (x == 0 && z == 0) {
                    world.setBlockState(pos, ModBlocks.MS_MASTER.getDefaultState());
                } else {
                    world.setBlockState(pos, ModBlocks.SMART_SUBORDINATE.getDefaultState());
                }
            }
        }
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (!worldIn.isRemote) {
            generateGameStructure(worldIn, pos, 1);
            worldIn.playSound(null, pos, SoundManager.msStartGame, SoundCategory.BLOCKS, 0.6F, 1.0F);
        }
        return true;
    }
}
