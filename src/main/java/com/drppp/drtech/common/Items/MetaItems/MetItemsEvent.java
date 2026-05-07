package com.drppp.drtech.common.Items.MetaItems;

import com.drppp.drtech.Tile.TileEntityConnector;
import com.drppp.drtech.api.Utils.DrtechUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public class MetItemsEvent {
    public static void onItemUse(EntityPlayer player, @NotNull World world, @NotNull BlockPos pos, @NotNull EnumHand hand, @NotNull EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack stack = player.getHeldItem(hand);
        if (player.isSneaking() && stack.getItem() == DrMetaItems.POS_CARD.getMetaItem() && stack.getMetadata() == DrMetaItems.POS_CARD.getMetaValue()) {
            if (world.getTileEntity(pos) instanceof TileEntityConnector) {
                NBTTagCompound nbt = new NBTTagCompound();
                nbt.setInteger("x", pos.getX());
                nbt.setInteger("y", pos.getY());
                nbt.setInteger("z", pos.getZ());
                stack.setTagCompound(nbt);
                player.sendMessage(new TextComponentString("Saved position: x=" + pos.getX() + ", y=" + pos.getY() + ", z=" + pos.getZ()));
            }
        } else if (stack.getItem() == DrMetaItems.POS_CARD.getMetaItem() && stack.getMetadata() == DrMetaItems.POS_CARD.getMetaValue()) {
            if (world.getTileEntity(pos) instanceof TileEntityConnector && stack.hasTagCompound()) {
                NBTTagCompound nbt = stack.getTagCompound();
                BlockPos savedPos = new BlockPos(nbt.getInteger("x"), nbt.getInteger("y"), nbt.getInteger("z"));
                if (DrtechUtils.getPosDist(pos, savedPos) <= 100) {
                    TileEntityConnector connector = (TileEntityConnector) world.getTileEntity(pos);
                    connector.beforePos = savedPos;
                    NBTTagCompound updateNbt = new NBTTagCompound();
                    updateNbt.setTag("locahost", nbt);
                    DrtechUtils.sendTileEntityUpdate(world.getTileEntity(pos), updateNbt);
                    player.sendMessage(new TextComponentString("Linked position: x=" + savedPos.getX() + ", y=" + savedPos.getY() + ", z=" + savedPos.getZ()));
                } else {
                    player.sendMessage(new TextComponentString("Distance exceeds 100 blocks."));
                }
            }
        }
    }
}