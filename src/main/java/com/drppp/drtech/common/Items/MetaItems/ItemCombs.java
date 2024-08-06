package com.drppp.drtech.common.Items.MetaItems;
import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemCombs {
    public static  Item ITEM_COMBS;
    public static void init()
    {
        ITEM_COMBS =  new com.drppp.drtech.intergations.Forestry.DrtCombItem();
    }
    @SideOnly(Side.CLIENT)
    public static void ClientInit()
    {
        Minecraft.getMinecraft().getItemColors().registerItemColorHandler((stack, tintIndex) -> {
            if (stack.getItem() instanceof forestry.core.items.IColoredItem coloredItem) {
                return coloredItem.getColorFromItemstack(stack, tintIndex);
            }
            return 0xFFFFFF;
        }, ITEM_COMBS);
    }
}
