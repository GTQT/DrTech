package com.drppp.drtech.common.Items.Behavior;

import com.drppp.drtech.api.Utils.CustomeRecipe;
import com.drppp.drtech.common.Items.MetaItems.MyMetaItems;
import gregtech.api.items.metaitem.stats.IItemBehaviour;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MatrixGemsBehavior implements IItemBehaviour {
    @Override
    public void addInformation(@NotNull ItemStack itemStack, List<String> lines) {
        if(itemStack.getItem()== MyMetaItems.MATRIX_GEMS.getMetaItem() && itemStack.getMetadata()==MyMetaItems.MATRIX_GEMS.getMetaValue())
        {
            NBTTagCompound compound = itemStack.getTagCompound();
            if(compound!=null && compound.hasKey("StoreRecipe"))
            {
                NBTTagCompound tag = compound.getCompoundTag("StoreRecipe");
                CustomeRecipe recipe = new CustomeRecipe(tag);
                if(recipe!=null)
                {
                    lines.add("======配方信息======");
                    lines.add("输入信息:+++++++++++");
                    if(!recipe.inputItems.isEmpty())
                    {
                        recipe.inputItems.forEach(x->lines.add(x.getDisplayName()+"*"+x.getCount()));
                    }
                    if(!recipe.inputFluids.isEmpty())
                    {
                        recipe.inputFluids.forEach(x->lines.add(x.getLocalizedName()+"*"+x.amount));
                    }
                    lines.add("输出信息:------------");
                    if(!recipe.outputItems.isEmpty())
                    {
                        recipe.outputItems.forEach(x->lines.add(x.getDisplayName()+"*"+x.getCount()));
                    }
                    if(!recipe.outputFluids.isEmpty())
                    {
                        recipe.outputFluids.forEach(x->lines.add(x.getLocalizedName()+"*"+x.amount));
                    }
                    lines.add("====================");
                    lines.add("Eu/t:"+recipe.eut);
                    lines.add("耗时:"+recipe.during+"tick");
                    lines.add("矩阵深度:"+recipe.deep);
                }
            }
            else
            {
                lines.add(I18n.format("behavior.matrix_gems.info", "无"));
            }
        }
    }
}
