package com.drppp.drtech.api.ItemHandler;

import com.drppp.drtech.api.Utils.GT_ApiaryUpgrade;
import forestry.api.apiculture.BeeManager;
import forestry.api.apiculture.EnumBeeType;
import net.minecraft.init.Items;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

public class OnlyUpgradeStackhandler extends ItemStackHandler {
    public OnlyUpgradeStackhandler(int size )
    {
        stacks = NonNullList.withSize(size, ItemStack.EMPTY);
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        if (!GT_ApiaryUpgrade.isUpgrade(stack)) return false;
        if(hasSameItem(stack))
        {
            int max  =GT_ApiaryUpgrade.getUpgrade(stack).getMaxNumber();
            int slotindex = getSameItemSlot(stack);
            ItemStack slotindexitem = getStackInSlot(slotindex);
            if(slotindexitem.getCount()>=max)
                return false;
            if(slot!=slotindex)
                return false;
            if((max - slotindexitem.getCount())<stack.getCount())
                return false;
        }else {
            int max  =GT_ApiaryUpgrade.getUpgrade(stack).getMaxNumber();
            if(hasSpeedItem(stack))
                return false;
            if(stack.getCount()>max)
                return false;
        }
        return true;
    }
    private boolean hasSameItem(ItemStack is)
    {
        for (int i = 0; i < getSlots(); i++) {
            if(getStackInSlot(i).getItem()==is.getItem() && getStackInSlot(i).getMetadata()==is.getMetadata())
                return true;
        }
        return false;
    }
    private int getSameItemSlot(ItemStack is)
    {
        for (int i = 0; i < getSlots(); i++) {
            if(getStackInSlot(i).getItem()==is.getItem() && getStackInSlot(i).getMetadata()==is.getMetadata())
                return i;
        }
        return -1;
    }
    private boolean hasSpeedItem(ItemStack is)
    {
        if( is.getMetadata()>=29 && is.getMetadata()<=37)
        {
            for (int i = 0; i < getSlots(); i++) {
                if(getStackInSlot(i).getMetadata()>=29 && getStackInSlot(i).getMetadata()<=37 && getStackInSlot(i).getItem()!= Items.AIR)
                    return true;
            }
        }
        return false;
    }
}
