package com.drppp.drtech.common.Items.Behavior;

import com.drppp.drtech.Client.Textures;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.items.gui.ItemUIFactory;
import gregtech.api.items.gui.PlayerInventoryHolder;
import gregtech.api.items.metaitem.stats.IItemBehaviour;
import gregtech.api.items.toolitem.IGTTool;
import gregtech.api.items.toolitem.ItemGTTool;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

public class ToolBoxBehavior implements IItemBehaviour, ItemUIFactory, INBTSerializable {
    ItemStackHandler inventory = new ItemStackHandlerTool(6);

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        if(!world.isRemote)
        {
            if(player.isSneaking())
            {
                PlayerInventoryHolder holder = new PlayerInventoryHolder(player, hand);
                holder.openUI();
            }else
            {

            }
        }
        return IItemBehaviour.super.onItemRightClick(world, player, hand);
    }

    @Override
    public ModularUI createUI(PlayerInventoryHolder playerInventoryHolder, EntityPlayer entityPlayer) {
        ModularUI.Builder builder;
        builder = ModularUI.builder(Textures.BACKGROUND, 209, 242);
        for (int i = 0; i < inventory.getSlots(); i++) {
            builder.slot(inventory,i,45+i%9*18,3+i/9*18,true,true, GuiTextures.SLOT);
        }
        builder.bindPlayerInventory(entityPlayer.inventory);
        return builder.build(playerInventoryHolder, entityPlayer);
    }

    @Override
    public NBTBase serializeNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setTag("BoxInv",inventory.serializeNBT());
        return tag;
    }

    @Override
    public void deserializeNBT(NBTBase nbt) {
        NBTTagCompound tag = (NBTTagCompound) nbt;
        if(tag.hasKey("BoxInv"))
            this.inventory.deserializeNBT(tag.getCompoundTag("BoxInv"));
    }
    private class ItemStackHandlerTool extends ItemStackHandler{
        public ItemStackHandlerTool(int size) {
            super(size);
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            if(stack.getItem() instanceof IGTTool)
            {
                return true;
            }
            return false;
        }
    }
}
