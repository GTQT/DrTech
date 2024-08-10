package com.drppp.drtech.api.ItemHandler;

import forestry.api.apiculture.BeeManager;
import forestry.api.apiculture.EnumBeeType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

public class OnlyBeesStackhandler  extends ItemStackHandler {
    public OnlyBeesStackhandler(int size )
    {
        stacks = NonNullList.withSize(size, ItemStack.EMPTY);
    }
    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        if(slot==0)
        {
            if(BeeManager.beeRoot.getType(stack) == EnumBeeType.PRINCESS || BeeManager.beeRoot.getType(stack) ==EnumBeeType.QUEEN)
                return true;
        } else if (slot==1) {
            if(BeeManager.beeRoot.getType(stack) == EnumBeeType.DRONE)
                return true;
        }
        return false;
    }

}
