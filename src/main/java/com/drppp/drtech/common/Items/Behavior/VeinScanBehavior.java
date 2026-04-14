package com.drppp.drtech.common.Items.Behavior;

import com.drppp.drtech.api.vein.VeinHelper;
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
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

public class VeinScanBehavior implements IItemBehaviour {
    @Override
    public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos posin, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
        if (world.isRemote) return EnumActionResult.PASS;
        if(player.isSneaking())
        {
            var data = VeinHelper.getVeinData(world,posin);
            player.sendMessage(new TextComponentString("该区块的矿脉为："+data.getVeinTypeId()));
            return EnumActionResult.SUCCESS;
        }
        int count = VeinHelper.scanVeinsAround(world,posin,3);
        player.sendMessage(new TextComponentString("扫描到矿脉数量:"+count));
        return EnumActionResult.SUCCESS;
    }

}
