package com.drppp.drtech.common.Items.Behavior;

import com.drppp.drtech.common.Items.MetaItems.DrMetaItems;
import gregtech.api.items.metaitem.stats.IItemBehaviour;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MatrixGemsBehavior implements IItemBehaviour {
    @Override
    public void addInformation(@NotNull ItemStack itemStack, List<String> lines) {
        if (itemStack.getItem() == DrMetaItems.MATRIX_GEMS.getMetaItem() && itemStack.getMetadata() == DrMetaItems.MATRIX_GEMS.getMetaValue()) {
            lines.add(I18n.format("behavior.matrix_gems.info", "无"));
        }
    }
}

