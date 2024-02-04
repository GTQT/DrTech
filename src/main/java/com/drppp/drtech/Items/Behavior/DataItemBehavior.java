package com.drppp.drtech.Items.Behavior;

import gregtech.api.items.metaitem.stats.IDataItem;
import gregtech.api.items.metaitem.stats.IItemBehaviour;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.recipes.machines.IResearchRecipeMap;
import gregtech.api.util.AssemblyLineManager;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

public class DataItemBehavior implements IItemBehaviour, IDataItem {

    private final boolean requireDataBank;

    public DataItemBehavior() {
        this.requireDataBank = false;
    }

    public DataItemBehavior(boolean requireDataBank) {
        this.requireDataBank = requireDataBank;
    }

    @Override
    public boolean requireDataBank() {
        return requireDataBank;
    }

    @Override
    public void addInformation(@NotNull ItemStack itemStack, List<String> lines) {
        String researchId = AssemblyLineManager.readResearchId(itemStack);
        if (researchId == null) return;
        lines.add(I18n.format("behavior.data_item.assemblyline.title"));
        lines.add(I18n.format("behavior.data_item.assemblyline.data", researchId));
    }
}
