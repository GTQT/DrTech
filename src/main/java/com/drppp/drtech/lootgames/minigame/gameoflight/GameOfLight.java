package com.drppp.drtech.lootgames.minigame.gameoflight;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import com.drppp.drtech.lootgames.api.minigame.ILootGameFactory;
import com.drppp.drtech.lootgames.api.minigame.LootGame;
import com.drppp.drtech.lootgames.registry.ModBlocks;

public class GameOfLight extends LootGame {
    @Override
    protected BlockPos getCentralRoomPos() {
        return getMasterPos();
    }

    @Override
    protected BlockPos getRoomFloorPos() {
        return getMasterPos();
    }

    public static class Factory implements ILootGameFactory {
        @Override
        public void genOnPuzzleMasterClick(World world, BlockPos puzzleMasterPos, BlockPos bottomPos, BlockPos topPos) {
            int masterTeOffset = 3;
            BlockPos floorCenterPos = puzzleMasterPos.add(0, -masterTeOffset + 1, 0);
            world.setBlockState(floorCenterPos, ModBlocks.GOL_MASTER.getDefaultState());
        }
    }
}
