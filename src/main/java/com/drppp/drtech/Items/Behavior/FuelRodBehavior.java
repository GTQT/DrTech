package com.drppp.drtech.Items.Behavior;

import com.drppp.drtech.api.capability.DrtechCommonCapabilities;
import com.drppp.drtech.api.capability.FuelRodItem;
import com.drppp.drtech.api.capability.IFuelRodData;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IElectricItem;
import gregtech.api.items.metaitem.ElectricStats;
import gregtech.api.items.metaitem.stats.*;
import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import java.util.List;

public class FuelRodBehavior  implements IItemComponent, IItemCapabilityProvider, IItemMaxStackSizeProvider,
        IItemBehaviour, ISubItemHandler {
    private int baseOutEnergy;
    private float baseOutHeat;
    private int pulseNum;
    private int maxDurability;
    private int X1Energy;
    private ItemStack outItem;
    private boolean isMox;
    private float moxMulti;
    //燃料棒能量输出，燃料棒热量输出，燃料棒发射脉冲数量(默认和棒数一致1 2 4,此数值影响相邻燃料或与反射板相邻的发电),是否是MOX，最大燃烧时间，枯竭输出物品，单棒能量输出
    public FuelRodBehavior(int baseOutEnergy, float baseOutHeat, int pulseNum,boolean isMox, float moxMulti, int maxDurability,ItemStack outItem,int X1Energy) {
        this.baseOutEnergy = baseOutEnergy;
        this.baseOutHeat = baseOutHeat;
        this.pulseNum = pulseNum;
        this.maxDurability = maxDurability;
        this.outItem = outItem;
        this.isMox = isMox;
        this.X1Energy = X1Energy;
        this.moxMulti = moxMulti;
    }

    @Override
    public void addInformation(ItemStack itemStack, List<String> lines) {
        var ca = itemStack.getCapability(DrtechCommonCapabilities.CAPABILITY_FUEL_ROAD,null);
        lines.add(I18n.format("fuelrod.date.tip1",ca.getDurability()));
        lines.add(I18n.format("fuelrod.date.tip2",ca.getMaxDurability()));
        lines.add(I18n.format("fuelrod.date.tip3",ca.isMox()));
        IItemBehaviour.super.addInformation(itemStack, lines);
    }

    @Override
    public ICapabilityProvider createProvider(ItemStack itemStack) {
        return new FuelRodItem(itemStack,baseOutEnergy,baseOutHeat,pulseNum,isMox,moxMulti,maxDurability,outItem,X1Energy);
    }

    @Override
    public int getMaxStackSize(ItemStack itemStack, int i) {
        return 64;
    }

    @Override
    public String getItemSubType(ItemStack itemStack) {
        return "";
    }

    @Override
    public void getSubItems(ItemStack itemStack, CreativeTabs creativeTabs, NonNullList<ItemStack> nonNullList) {
        ItemStack copy = itemStack.copy();
        IFuelRodData electricItem = copy.getCapability(DrtechCommonCapabilities.CAPABILITY_FUEL_ROAD, null);
        if (electricItem != null) {
           // electricItem.charge(electricItem.getMaxCharge(), electricItem.getTier(), true, false);
            nonNullList.add(copy);
        } else {
            nonNullList.add(itemStack);
        }
    }

}
