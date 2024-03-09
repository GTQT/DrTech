package com.drppp.drtech.Items.Behavior;

import com.drppp.drtech.api.capability.DrtechCommonCapabilities;
import com.drppp.drtech.api.capability.HeatVentItem;
import gregtech.api.items.metaitem.stats.*;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class HeatVentBehavior implements IItemComponent, IItemCapabilityProvider, IItemBehaviour
{
    private int HeatAbsorptionRate;
    private int HeatDissipationRate;
    private int MaxHeat;
    private boolean reactorInteraction;
    private boolean elementInteraction;

    public HeatVentBehavior(int heatAbsorptionRate, int heatDissipationRate, int maxHeat, boolean reactorInteraction, boolean elementInteraction) {
        HeatAbsorptionRate = heatAbsorptionRate;
        HeatDissipationRate = heatDissipationRate;
        MaxHeat = maxHeat;
        this.reactorInteraction = reactorInteraction;
        this.elementInteraction = elementInteraction;
    }

    @Override
    public ICapabilityProvider createProvider(ItemStack itemStack) {
        return new HeatVentItem(itemStack,this.HeatAbsorptionRate,this.HeatDissipationRate,this.MaxHeat,this.reactorInteraction,this.elementInteraction);
    }
    @Override
    public void addInformation(ItemStack itemStack, List<String> lines) {
        var ca = itemStack.getCapability(DrtechCommonCapabilities.CAPABILITY_HEAT_VENT,null);
        lines.add(I18n.format("heatvent.date.tip1",ca.getHeat()));
        lines.add(I18n.format("heatvent.date.tip2",ca.getMaxHeat()));
        IItemBehaviour.super.addInformation(itemStack, lines);
    }
}
