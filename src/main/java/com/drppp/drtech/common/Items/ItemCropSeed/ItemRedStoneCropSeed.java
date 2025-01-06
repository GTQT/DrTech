package com.drppp.drtech.common.Items.ItemCropSeed;

import com.drppp.drtech.Tags;
import com.drppp.drtech.common.Blocks.Crops.CropsInit;
import com.drppp.drtech.DrTechMain;
import net.minecraft.block.BlockFarmland;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemRedStoneCropSeed extends Item {
    public ItemRedStoneCropSeed(String RegistryName) {
        this.setRegistryName(Tags.MODID, RegistryName);
        this.setTranslationKey(Tags.MODID + "." + RegistryName);
        this.setCreativeTab(DrTechMain.Mytab);
    }
    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack stack = player.getHeldItem(hand);

        if (worldIn.isAirBlock(pos.up()) && CropsInit.REDSTONE_CROP.getDefaultState().getBlock().canPlaceBlockAt(worldIn, pos.up())&& worldIn.getBlockState(pos).getBlock() instanceof BlockFarmland && worldIn.getBlockState(pos).getValue(BlockFarmland.MOISTURE) > 0)
            {
                worldIn.setBlockState(pos.up(), CropsInit.REDSTONE_CROP.getDefaultState());
                stack.shrink(1);
                return EnumActionResult.SUCCESS;
            }
        else
            return EnumActionResult.FAIL;
        }

}
