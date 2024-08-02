package com.drppp.drtech.common.Items.Behavior;

import gregtech.api.items.metaitem.stats.IItemBehaviour;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class KillGrassBehavior implements IItemBehaviour {
    @Override
    public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos posin, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand){
        if (world.isRemote) return EnumActionResult.PASS;
        BlockPos pos = player.getPosition();
        int radius = 16; // 清除半径
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                for (int y = (int)hitY-4; y < (int)hitY+4; y++) {
                    BlockPos currentPos = pos.add(x, y, z);
                    Block block = world.getBlockState(currentPos).getBlock();
                    if (block == Blocks.TALLGRASS || block instanceof BlockTallGrass || block instanceof BlockDoublePlant) {
                        world.setBlockToAir(currentPos);
                    }
                }
            }
        }
        return EnumActionResult.SUCCESS;
    }
}
