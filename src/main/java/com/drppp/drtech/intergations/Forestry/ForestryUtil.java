package com.drppp.drtech.intergations.Forestry;

import com.drppp.drtech.common.Items.MetaItems.ItemCombs;
import gregtech.api.util.Mods;
import gregtech.integration.IntegrationModule;
import gregtech.integration.forestry.ForestryConfig;
import gregtech.integration.forestry.ForestryModule;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ForestryUtil {
    public ForestryUtil() {
    }
    public static @NotNull ItemStack getCombStack(@NotNull DrtCombType type) {
        return getCombStack(type, 1);
    }
    public static @NotNull ItemStack getCombStack(@NotNull DrtCombType type, int amount) {
        if (!ForestryConfig.enableGTBees) {
            IntegrationModule.logger.error("Tried to get GregTech Comb stack, but GregTech Bees config is not enabled!");
            return ItemStack.EMPTY;
        } else if (!Mods.ForestryApiculture.isModLoaded()) {
            IntegrationModule.logger.error("Tried to get GregTech Comb stack, but Apiculture module is not enabled!");
            return ItemStack.EMPTY;
        } else {
            return new ItemStack(ItemCombs.ITEM_COMBS, amount, type.ordinal());
        }
    }

}
