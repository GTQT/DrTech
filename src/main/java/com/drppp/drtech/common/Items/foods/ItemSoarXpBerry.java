package com.drppp.drtech.common.Items.foods;

import com.drppp.drtech.Tags;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ItemSoarXpBerry extends ItemFood {

    public ItemSoarXpBerry() {
        super(6, 1.2F, false); // 6点饥饿值, 1.2饱和度
        setTranslationKey(Tags.MODID + ".soar_xp_berry");
        setRegistryName(Tags.MODID, "soar_xp_berry");
        setCreativeTab(CreativeTabs.FOOD);
        setAlwaysEdible(); // 随时可食用
    }
    @Override
    protected void onFoodEaten(ItemStack stack, World worldIn, EntityPlayer player) {
        super.onFoodEaten(stack, worldIn, player);
        if (!worldIn.isRemote) {
            player.addExperienceLevel(worldIn.rand.nextInt(3));
        }
    }
}
