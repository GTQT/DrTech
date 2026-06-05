package com.drppp.drtech.lootgames.minigame.minesweeper.tileentity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import com.drppp.drtech.lootgames.api.tileentity.TileEntityGameMaster;
import com.drppp.drtech.lootgames.api.util.Pos2i;
import com.drppp.drtech.lootgames.minigame.minesweeper.GameMineSweeper;

public class TileEntityMSMaster extends TileEntityGameMaster<GameMineSweeper> {
    public TileEntityMSMaster() {
        super(new GameMineSweeper(7, 6));
    }

    @Override
    public void onSubordinateBlockClicked(BlockPos subordinatePos, EntityPlayer player) {
        Pos2i pos = GameMineSweeper.convertToGamePos(getPos(), subordinatePos);
        game.onFieldClicked(pos, player.isSneaking());
    }

    @Override
    public void destroyGameBlocks() {
        for (int x = 0; x < game.getBoardSize(); x++) {
            for (int z = 0; z < game.getBoardSize(); z++) {
                getWorld().setBlockState(getPos().add(x, 0, z), Blocks.AIR.getDefaultState(), 2);
            }
        }
    }

    @NotNull
    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return TileEntity.INFINITE_EXTENT_AABB;
    }
}
