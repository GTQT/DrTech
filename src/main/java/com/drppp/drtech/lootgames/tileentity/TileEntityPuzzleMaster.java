package com.drppp.drtech.lootgames.tileentity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import com.drppp.drtech.DrTechMain;
import com.drppp.drtech.lootgames.LootGames;
import com.drppp.drtech.lootgames.world.gen.DungeonGenerator;

import java.util.Random;

public class TileEntityPuzzleMaster extends TileEntity implements ITickable {
    private static final Random RAND = new Random();
    private long lastSoundTick = 0L;

    public void onBlockClickedByPlayer(EntityPlayer player) {
        try {
            int centerToBorder = DungeonGenerator.PUZZLEROOM_CENTER_TO_BORDER;
            int masterTeOffset = DungeonGenerator.PUZZLEROOM_MASTER_TE_OFFSET;
            int roomHeight = DungeonGenerator.PUZZLEROOM_HEIGHT;

            BlockPos bottomPos = new BlockPos(
                    getPos().add(-centerToBorder, -masterTeOffset, -centerToBorder));
            BlockPos topPos = bottomPos.add(
                    centerToBorder * 2 + 1, roomHeight, centerToBorder * 2 + 1);

            // Reset shielded floor to regular floor so the game is playable
            BlockPos floorPos = new BlockPos(
                    getPos().add(-centerToBorder, -masterTeOffset + 1, -centerToBorder));
            DungeonGenerator.resetUnbreakablePlayfield(world, floorPos);

            LootGames.gameManager.generateRandomGame(world, getPos(), bottomPos, topPos);

            if (world.getTileEntity(getPos()) == this) {
                world.setBlockToAir(getPos());
            }

        } catch (Throwable e) {
            DrTechMain.LOGGER.error("Error in PuzzleMaster activation", e);
        }
    }

    @Override
    public void update() {
        if (!world.isRemote) {
            if (RAND.nextInt(100) <= 10) {
                if (lastSoundTick < System.currentTimeMillis()) {
                    lastSoundTick = System.currentTimeMillis() + (RAND.nextInt(90) + 30) * 1000L;
                }
            }
        }
    }
}
