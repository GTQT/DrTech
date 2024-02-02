package com.drppp.drtech.Items.ItemCropSeed;

import com.drppp.drtech.Blocks.Crops.CropsInit;
import com.drppp.drtech.DrTechMain;
import com.drppp.drtech.Tags;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemLapisCropSeed extends Item {
    public ItemLapisCropSeed(String RegistryName) {
        this.setRegistryName(Tags.MODID, RegistryName);
        this.setTranslationKey(Tags.MODID + "." + RegistryName);
        this.setCreativeTab(DrTechMain.Mytab);
    }
    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack stack = player.getHeldItem(hand);

        if (worldIn.isAirBlock(pos.up()) && CropsInit.LAPIS_CROP.getDefaultState().getBlock().canPlaceBlockAt(worldIn, pos.up()))
            {
                worldIn.setBlockState(pos.up(), CropsInit.LAPIS_CROP.getDefaultState());
                stack.shrink(1);
                return EnumActionResult.SUCCESS;
            }
        else
            return EnumActionResult.FAIL;
        }

}
