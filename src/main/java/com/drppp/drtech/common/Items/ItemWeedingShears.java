package com.drppp.drtech.common.Items;

import com.drppp.drtech.Tags;
import com.drppp.drtech.Tile.TileCropStick;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

public class ItemWeedingShears extends Item {

    public ItemWeedingShears() {
        setTranslationKey(Tags.MODID + ".weeding_shears");
        setRegistryName(Tags.MODID, "weeding_shears");
        setMaxStackSize(1);
        setMaxDamage(256);
        setCreativeTab(CreativeTabs.TOOLS);
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos,
                                      EnumHand hand, EnumFacing facing,
                                      float hitX, float hitY, float hitZ) {
        if (world.isRemote) return EnumActionResult.SUCCESS;

        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileCropStick) {
            TileCropStick tile = (TileCropStick) te;
            if (tile.isWeedPlant()) {
                tile.destroyCrop();
                player.getHeldItem(hand).damageItem(1, player);
                player.sendMessage(new TextComponentString(
                        TextFormatting.GREEN + "杂草已清除!"));
                return EnumActionResult.SUCCESS;
            } else if (tile.hasCrop()) {
                player.sendMessage(new TextComponentString(
                        TextFormatting.YELLOW + "这不是杂草。"));
                return EnumActionResult.FAIL;
            }
        }
        return EnumActionResult.PASS;
    }

    @Override
    public boolean onBlockStartBreak(ItemStack stack, BlockPos pos,
                                     EntityPlayer player) {
        // 左键也能除草(不破坏方块)
        if (player.world.isRemote) return false;

        TileEntity te = player.world.getTileEntity(pos);
        if (te instanceof TileCropStick) {
            TileCropStick tile = (TileCropStick) te;
            if (tile.isWeedPlant()) {
                tile.destroyCrop();
                stack.damageItem(1, player);
                player.sendMessage(new TextComponentString(
                        TextFormatting.GREEN + "杂草已清除!"));
                return true; // true = 取消方块破坏
            }
        }
        return false;
    }
}