package com.drppp.drtech.api.Utils;

import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;

public class ItemId{
    public Item item;
    public int meta;
    public NBTTagCompound tag;

    public ItemId(Item item, int meta, NBTTagCompound tag) {
        this.item = item;
        this.meta = meta;
        this.tag = tag;
    }
}