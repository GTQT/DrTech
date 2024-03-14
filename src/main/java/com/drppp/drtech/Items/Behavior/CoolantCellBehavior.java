package com.drppp.drtech.Items.Behavior;

import com.drppp.drtech.api.capability.CoolantCellItem;
import com.drppp.drtech.api.capability.DrtechCommonCapabilities;
import com.drppp.drtech.api.capability.NeutronReflectorItem;
import gregtech.api.items.metaitem.stats.IItemBehaviour;
import gregtech.api.items.metaitem.stats.IItemCapabilityProvider;
import gregtech.api.items.metaitem.stats.IItemComponent;
import gregtech.api.items.metaitem.stats.IItemMaxStackSizeProvider;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import java.util.List;

public class CoolantCellBehavior implements IItemComponent, IItemCapabilityProvider, IItemBehaviour, IItemMaxStackSizeProvider {
    private int maxDurability;

    public CoolantCellBehavior(int maxDurability) {
        this.maxDurability = maxDurability;
    }

    @Override
    public ICapabilityProvider createProvider(ItemStack itemStack) {
        return new CoolantCellItem(itemStack,maxDurability);
    }

    @Override
    public void addInformation(ItemStack itemStack, List<String> lines) {
        var ca = itemStack.getCapability(DrtechCommonCapabilities.CAPABILITY_COOLANT_CELL,null);
        lines.add(I18n.format("heatvent.date.tip1",ca.getDurability()));
        lines.add(I18n.format("heatvent.date.tip2",ca.getMaxDurability()));
        IItemBehaviour.super.addInformation(itemStack, lines);
    }

    @Override
    public int getMaxStackSize(ItemStack itemStack, int i) {
        return 64;
    }
}
