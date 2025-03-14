package com.drppp.drtech.common.Items.Behavior;

import com.drppp.drtech.api.capability.DrtechCommonCapabilities;
import com.drppp.drtech.api.capability.HeatExchangerItem;
import gregtech.api.items.metaitem.stats.IItemBehaviour;
import gregtech.api.items.metaitem.stats.IItemCapabilityProvider;
import gregtech.api.items.metaitem.stats.IItemComponent;
import gregtech.api.items.metaitem.stats.IItemMaxStackSizeProvider;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import java.util.List;

public class HeatExchangerBehavior implements IItemComponent, IItemCapabilityProvider, IItemBehaviour, IItemMaxStackSizeProvider {
    private final int reactorExchangeHeatRate;
    private final int elementExchangeHeatRate;
    private final int maxHeat;
    private final boolean reactorInteraction;
    private final boolean elementInteraction;

    public HeatExchangerBehavior(int reactorExchangeHeatRate, int elementExchangeHeatRate, int maxHeat, boolean reactorInteraction, boolean elementInteraction) {
        this.reactorExchangeHeatRate = reactorExchangeHeatRate;
        this.elementExchangeHeatRate = elementExchangeHeatRate;
        this.maxHeat = maxHeat;
        this.reactorInteraction = reactorInteraction;
        this.elementInteraction = elementInteraction;
    }

    @Override
    public ICapabilityProvider createProvider(ItemStack itemStack) {
        return new HeatExchangerItem(itemStack, this.reactorExchangeHeatRate, this.elementExchangeHeatRate, this.maxHeat, this.reactorInteraction, this.elementInteraction);
    }

    @Override
    public void addInformation(ItemStack itemStack, List<String> lines) {
        var ca = itemStack.getCapability(DrtechCommonCapabilities.CAPABILITY_HEAT_EXCHANGER, null);
        lines.add(I18n.format("heatvent.date.tip1", ca.getHeat()));
        lines.add(I18n.format("heatvent.date.tip2", ca.getMaxHeat()));
        IItemBehaviour.super.addInformation(itemStack, lines);
    }

    @Override
    public int getMaxStackSize(ItemStack itemStack, int i) {
        return 64;
    }
}
