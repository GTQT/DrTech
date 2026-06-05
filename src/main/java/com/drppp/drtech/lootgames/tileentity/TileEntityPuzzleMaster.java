package com.drppp.drtech.lootgames.tileentity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import com.drppp.drtech.DrTechMain;
import com.drppp.drtech.lootgames.LootGames;

import java.util.Random;

public class TileEntityPuzzleMaster extends TileEntity implements ITickable {
    private static final Random RAND = new Random();
    private long lastSoundTick = 0L;

    public void onBlockClickedByPlayer(EntityPlayer player) {
        try {
            int centerToBorder = 10;
            int masterTeOffset = 3;
            int roomHeight = 8;

            BlockPos bottomPos = new BlockPos(
                    getPos().add(-centerToBorder, -masterTeOffset, -centerToBorder));
            BlockPos topPos = bottomPos.add(
                    centerToBorder * 2 + 1, roomHeight, centerToBorder * 2 + 1);

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
