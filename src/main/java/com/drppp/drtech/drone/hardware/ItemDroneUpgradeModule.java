package com.drppp.drtech.drone.hardware;

import com.drppp.drtech.DrTechMain;
import com.drppp.drtech.Tags;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public final class ItemDroneUpgradeModule extends Item {

    public ItemDroneUpgradeModule() {
        setRegistryName(Tags.MODID, "drone_upgrade_module");
        setTranslationKey(Tags.MODID + ".drone_upgrade_module");
        setCreativeTab(DrTechMain.DrTechTab);
        setHasSubtypes(true);
        setMaxDamage(0);
        setMaxStackSize(1);
    }

    @Override
    public String getTranslationKey(ItemStack stack) {
        return super.getTranslationKey() + "." + getType(stack).getSerializedName();
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (!isInCreativeTab(tab)) return;
        for (DroneUpgradeType type : DroneUpgradeType.values()) {
            items.add(new ItemStack(this, 1, type.getMetadata()));
        }
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
        super.addInformation(stack, world, tooltip, flag);
        tooltip.add(I18n.format("drtech.drone.upgrade." + getType(stack).getSerializedName() + ".tooltip"));
    }

    @Nullable
    public static DroneUpgradeType getType(ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof ItemDroneUpgradeModule)) return null;
        return DroneUpgradeType.fromMetadata(stack.getMetadata());
    }
}
