package com.drppp.drtech.Items.MetaItems;

import com.drppp.drtech.DrTechMain;
import com.drppp.drtech.Items.Behavior.DataItemBehavior;
import com.drppp.drtech.Tile.TileEntityConnector;
import com.drppp.drtech.Tile.TileEntityGravitationalAnomaly;
import com.drppp.drtech.Utils.DrtechUtils;
import gregtech.api.items.metaitem.StandardMetaItem;
import gregtech.common.items.behaviors.TooltipBehavior;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;


public  class MetaItems1 extends StandardMetaItem {
    public MetaItems1() {
    }
    public void registerSubItems() {
        MyMetaItems.ENERGY_ELEMENT_1 = this.addItem(0,"energy_element_1").setMaxStackSize(64).setCreativeTabs(DrTechMain.Mytab)
                .addComponents(new TooltipBehavior((lines) -> {
                    lines.add(I18n.format("metaitem.weight.tooltip.1", new Object[0]));
                }));
        MyMetaItems.ENERGY_ELEMENT_2 = this.addItem(1,"energy_element_2").setMaxStackSize(64).setCreativeTabs(DrTechMain.Mytab)
                .addComponents(new TooltipBehavior((lines) -> {
                    lines.add(I18n.format("metaitem.weight.tooltip.2", new Object[0]));
                }));
        MyMetaItems.ENERGY_ELEMENT_3 = this.addItem(2,"energy_element_3").setMaxStackSize(64).setCreativeTabs(DrTechMain.Mytab)
                .addComponents(new TooltipBehavior((lines) -> {
                    lines.add(I18n.format("metaitem.weight.tooltip.3", new Object[0]));
                }));
        MyMetaItems.ENERGY_ELEMENT_4 = this.addItem(3,"energy_element_4").setMaxStackSize(64).setCreativeTabs(DrTechMain.Mytab)
                .addComponents(new TooltipBehavior((lines) -> {
                    lines.add(I18n.format("metaitem.weight.tooltip.4", new Object[0]));
                }));
        MyMetaItems.ENERGY_ELEMENT_5 = this.addItem(4,"energy_element_5").setMaxStackSize(64).setCreativeTabs(DrTechMain.Mytab)
                .addComponents(new TooltipBehavior((lines) -> {
                    lines.add(I18n.format("metaitem.weight.tooltip.5", new Object[0]));
                }));
        MyMetaItems.GRAVITY_SHIELD = this.addItem(5,"gravity_shield").setMaxStackSize(1).setCreativeTabs(DrTechMain.Mytab)
                .addComponents(new TooltipBehavior((lines) -> {
                    lines.add(I18n.format("metaitem.gravity_shield.tooltip.1", new Object[0]));
                }));
                MyMetaItems.SKULL_DUST = this.addItem(6,"skull_dust").setCreativeTabs(DrTechMain.Mytab).setMaxStackSize(64);
                MyMetaItems.SCRAP = this.addItem(7,"scrap").setCreativeTabs(DrTechMain.Mytab).setMaxStackSize(64);
                MyMetaItems.CD_ROM = this.addItem(8,"cd_rom").setCreativeTabs(DrTechMain.Mytab).setMaxStackSize(16).addComponents(new DataItemBehavior(true));
                MyMetaItems.UU_MATER = this.addItem(9,"uu_mater").setCreativeTabs(DrTechMain.Mytab).setMaxStackSize(64);
                MyMetaItems.PIPIE_1 = this.addItem(10,"pipe_1").setCreativeTabs(DrTechMain.Mytab).setMaxStackSize(64)
                        .addComponents(new TooltipBehavior((lines) -> {
                            lines.add(I18n.format("metaitem.pipe_1.tooltip.1", new Object[0]));
                        }));
                MyMetaItems.PIPIE_5 = this.addItem(11,"pipe_5").setCreativeTabs(DrTechMain.Mytab).setMaxStackSize(64)
                        .addComponents(new TooltipBehavior((lines) -> {
                            lines.add(I18n.format("metaitem.pipe_5.tooltip.1", new Object[0]));
                        }));
                MyMetaItems.PIPIE_10 = this.addItem(12,"pipe_10").setCreativeTabs(DrTechMain.Mytab).setMaxStackSize(64)
                        .addComponents(new TooltipBehavior((lines) -> {
                            lines.add(I18n.format("metaitem.pipe_10.tooltip.1", new Object[0]));
                        }));
        MyMetaItems.POS_CARD = this.addItem(13,"pos_card").setCreativeTabs(DrTechMain.Mytab).setMaxStackSize(1).addComponents(new DataItemBehavior(true));
    }

    @Override
    public @NotNull ActionResult<ItemStack> onItemRightClick(@NotNull World world, EntityPlayer player, @NotNull EnumHand hand) {
        ItemStack item = player.getHeldItemMainhand();
        if(player.isSneaking() && item.getItem()== MyMetaItems.GRAVITY_SHIELD.getMetaItem() && item.getMetadata()==MyMetaItems.GRAVITY_SHIELD.getMetaValue())
        {
            if(!player.capabilities.allowFlying)
            {
                enableFlyingAbility(player);
            }
            else
            {
                disableFlyingAbility(player);
            }
        }
        return super.onItemRightClick(world, player, hand);
    }

    @Override
    public @NotNull EnumActionResult onItemUse(EntityPlayer player, @NotNull World world, @NotNull BlockPos pos, @NotNull EnumHand hand, @NotNull EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack stack = player.getHeldItemMainhand();
        if(player.isSneaking() && stack.getItem()== MyMetaItems.POS_CARD.getMetaItem() && stack.getMetadata()==MyMetaItems.POS_CARD.getMetaValue())
        {
            if( world.getTileEntity(pos)!=null &&  world.getTileEntity(pos)instanceof TileEntityConnector)
            {
                NBTTagCompound nbt = new NBTTagCompound();
                nbt.setInteger("x",pos.getX());
                nbt.setInteger("y",pos.getY());
                nbt.setInteger("z",pos.getZ());
                stack.setTagCompound(nbt);
                player.sendMessage(new TextComponentString("已保存坐标: x:"+pos.getX() +"y:"+pos.getY()+"z:"+pos.getZ()));
            }


        }
        else  if(stack.getItem()== MyMetaItems.POS_CARD.getMetaItem() && stack.getMetadata()==MyMetaItems.POS_CARD.getMetaValue())
        {
            if( world.getTileEntity(pos)!=null &&  world.getTileEntity(pos)instanceof TileEntityConnector)
            {
                NBTTagCompound nbt = stack.getTagCompound();
                BlockPos bpos = new BlockPos(nbt.getInteger("x"),nbt.getInteger("y"),nbt.getInteger("z"));

                if(DrtechUtils.getPosDist(pos,bpos)<=100)
                {
                    TileEntityConnector con = ((TileEntityConnector)world.getTileEntity(pos));
                        con.beforePos = bpos;
                        NBTTagCompound newnbt = new NBTTagCompound();
                        newnbt.setTag("locahost",nbt);
                        DrtechUtils.sendTileEntityUpdate(world.getTileEntity(pos),newnbt);
                        player.sendMessage(new TextComponentString("已写入坐标: x:"+bpos.getX() +"y:"+bpos.getY()+"z:"+bpos.getZ()));
                }
                else
                    player.sendMessage(new TextComponentString("距离超过100格!"));
            }
        }
        return super.onItemUse(player, world, pos, hand, facing, hitX, hitY, hitZ);
    }

    private void enableFlyingAbility(EntityPlayer player)
    {
        player.capabilities.allowFlying = true;
        player.capabilities.isFlying = false;
        player.sendPlayerAbilities();
        player.sendStatusMessage(new TextComponentString(I18n.format("metaitem.gravity_shield.info.1", new Object[0])), true);
    }

    private void disableFlyingAbility(EntityPlayer player)
    {
        player.capabilities.allowFlying = false;
        player.capabilities.isFlying = false;
        player.sendPlayerAbilities();
        player.sendStatusMessage(new TextComponentString(I18n.format("metaitem.gravity_shield.info.2", new Object[0])), true);
    }
}
