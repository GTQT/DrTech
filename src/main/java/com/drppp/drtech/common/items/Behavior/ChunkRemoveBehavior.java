package com.drppp.drtech.common.Items.Behavior;

import gregtech.api.items.metaitem.stats.IItemBehaviour;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ChunkRemoveBehavior implements IItemBehaviour {
    @Override
    public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
        if (world.isRemote) return EnumActionResult.PASS;
        for (int tX = (pos.getX() & ~15), eX = (pos.getX() & ~15) + 16; tX < eX; tX++)
            for (int tZ = (pos.getZ() & ~15), eZ = (pos.getZ() & ~15) + 16; tZ < eZ; tZ++)
                for (int tY = 1; tY < 250; tY++) {
                    world.setBlockToAir(new BlockPos(tX, tY, tZ));
                }
        return EnumActionResult.SUCCESS;
    }

}
