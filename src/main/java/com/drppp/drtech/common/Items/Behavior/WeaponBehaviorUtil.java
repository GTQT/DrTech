package com.drppp.drtech.common.Items.Behavior;

import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IElectricItem;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.Vec3d;

public final class WeaponBehaviorUtil {
    private static final String LAST_RIGHT_CLICK_KEY = "LastRightClick";

    private WeaponBehaviorUtil() {
    }

    public static NBTTagCompound getOrCreateTag(ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null) {
            tag = new NBTTagCompound();
            stack.setTagCompound(tag);
        }
        return tag;
    }

    public static long getLastRightClick(ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();
        return tag == null ? 0L : tag.getLong(LAST_RIGHT_CLICK_KEY);
    }

    public static void setLastRightClick(ItemStack stack, long time) {
        getOrCreateTag(stack).setLong(LAST_RIGHT_CLICK_KEY, time);
    }

    public static IElectricItem getElectricItem(ItemStack stack) {
        return stack.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
    }

    public static boolean canUse(ItemStack stack, long amount) {
        IElectricItem electricItem = getElectricItem(stack);
        return electricItem != null && electricItem.canUse(amount);
    }

    public static boolean drainEnergy(ItemStack stack, long amount) {
        IElectricItem electricItem = getElectricItem(stack);
        return electricItem != null && electricItem.discharge(amount, Integer.MAX_VALUE, true, false, false) == amount;
    }

    public static void moveProjectileToMuzzle(Entity projectile, EntityPlayer player, double distance) {
        Vec3d eyes = player.getPositionEyes(1.0F);
        Vec3d look = player.getLookVec();
        double x = eyes.x + look.x * distance;
        double y = eyes.y + look.y * distance;
        double z = eyes.z + look.z * distance;
        projectile.setPosition(x, y, z);
        projectile.prevPosX = x;
        projectile.prevPosY = y;
        projectile.prevPosZ = z;
        projectile.lastTickPosX = x;
        projectile.lastTickPosY = y;
        projectile.lastTickPosZ = z;
    }
}
