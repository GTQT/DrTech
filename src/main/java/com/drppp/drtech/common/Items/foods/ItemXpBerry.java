package com.drppp.drtech.common.Items.foods;

import com.drppp.drtech.Tags;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ItemXpBerry extends ItemFood {

    public ItemXpBerry() {
        super(2, 0.6F, false); // 6点饥饿值, 1.2饱和度
        setTranslationKey(Tags.MODID + ".xp_berry");
        setRegistryName(Tags.MODID, "xp_berry");
        setCreativeTab(CreativeTabs.FOOD);
        setAlwaysEdible(); // 随时可食用
    }
    @Override
    protected void onFoodEaten(ItemStack stack, World worldIn, EntityPlayer player) {
        super.onFoodEaten(stack, worldIn, player);
        if (!worldIn.isRemote) {
            player.addExperience(worldIn.rand.nextInt(300));
        }
    }
}
