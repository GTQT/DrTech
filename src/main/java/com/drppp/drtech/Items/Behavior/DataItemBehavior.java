package com.drppp.drtech.Items.Behavior;

import com.drppp.drtech.Items.MetaItems.MyMetaItems;
import gregtech.api.items.metaitem.stats.IDataItem;
import gregtech.api.items.metaitem.stats.IItemBehaviour;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.recipes.machines.IResearchRecipeMap;
import gregtech.api.util.AssemblyLineManager;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
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
        if(itemStack.getItem()== MyMetaItems.POS_CARD.getMetaItem() && itemStack.getMetadata()==MyMetaItems.POS_CARD.getMetaValue())
        {
            NBTTagCompound compound = itemStack.getTagCompound();
            String name  ="无";
            if (compound != null) name = "X:"+compound.getInteger("x") +"Y:"+compound.getInteger("y")+"Z:"+compound.getInteger("z");
            lines.add(I18n.format("behavior.data_item.poscard.data", name));
        }
        else
        {
            NBTTagCompound compound = itemStack.getTagCompound();
            String name  ="空";
            if (compound != null) name = compound.getString("Name");
            lines.add(I18n.format("behavior.data_item.cd_rom.title"));
            lines.add(I18n.format("behavior.data_item.cd_rom.data", name));
        }
    }
}
