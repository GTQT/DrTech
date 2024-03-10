package com.drppp.drtech.api.capability;

import net.minecraft.item.ItemStack;

import java.util.function.BiConsumer;

public interface IFuelRodData {
     //基础热量输出
     float getBaseHeat();
     //基础能量输出
     int getBaseEnergy();
     //单棒发电
     int get1XEnergy();
     //核脉冲数量
     int getPulseNum();
     //耐久度
     int getDurability();
     //最大耐久度
     int getMaxDurability();
     float getMoxMulti();
     //设置当前耐久
     void setDurability(int now);
     ItemStack outItem();
     boolean isMox();
     void addChargeListener(BiConsumer<ItemStack, Integer> chargeListener);
}
