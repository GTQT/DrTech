package com.drppp.drtech.common.Items.Behavior;

import com.drppp.drtech.api.capability.DrtechCommonCapabilities;
import com.drppp.drtech.api.capability.NeutronReflectorItem;
import gregtech.api.items.metaitem.stats.IItemBehaviour;
import gregtech.api.items.metaitem.stats.IItemCapabilityProvider;
import gregtech.api.items.metaitem.stats.IItemComponent;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import java.util.List;

public class NeutronReflectorBehavior implements IItemComponent, IItemCapabilityProvider, IItemBehaviour {
    private final int maxDurability;

    public NeutronReflectorBehavior(int maxDurability) {
        this.maxDurability = maxDurability;
    }

    @Override
    public ICapabilityProvider createProvider(ItemStack itemStack) {
        return new NeutronReflectorItem(itemStack, maxDurability);
    }

    @Override
    public void addInformation(ItemStack itemStack, List<String> lines) {
        var ca = itemStack.getCapability(DrtechCommonCapabilities.CAPABILITY_NEUTRON_REFLECTOR, null);
        lines.add(I18n.format("fuelrod.date.tip1", ca.getDurability() == 999999999 ? "无限" : ca.getDurability()));
        lines.add(I18n.format("fuelrod.date.tip2", ca.getMaxDurability() == 999999999 ? "无限" : ca.getMaxDurability()));
        IItemBehaviour.super.addInformation(itemStack, lines);
    }
}
