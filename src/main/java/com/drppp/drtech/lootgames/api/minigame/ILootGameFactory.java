package com.drppp.drtech.lootgames.api.minigame;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface ILootGameFactory {
    void genOnPuzzleMasterClick(World world, BlockPos puzzleMasterPos, BlockPos bottomPos, BlockPos topPos);
}
