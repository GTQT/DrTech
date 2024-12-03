package com.drppp.drtech.intergations.Forestry;

import com.drppp.drtech.common.Items.MetaItems.ItemCombs;
import forestry.api.apiculture.IAlleleBeeSpecies;
import forestry.api.genetics.AlleleManager;
import gregtech.api.util.Mods;
import gregtech.integration.IntegrationModule;
import gregtech.integration.forestry.ForestryConfig;
import gregtech.integration.forestry.ForestryModule;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    public static @NotNull ItemStack getDropStack(@NotNull DrtDropType type) {
        return getDropStack(type, 1);
    }
    public static @NotNull ItemStack getDropStack(@NotNull DrtDropType type, int amount) {
        if (!ForestryConfig.enableGTBees) {
            IntegrationModule.logger.error("Tried to get GregTech Drop stack, but GregTech Bees config is not enabled!");
            return ItemStack.EMPTY;
        } else if (!Mods.ForestryApiculture.isModLoaded()) {
            IntegrationModule.logger.error("Tried to get GregTech Drop stack, but Apiculture module is not enabled!");
            return ItemStack.EMPTY;
        } else {
            return new ItemStack(ItemCombs.ITEM_DROPS, amount, type.ordinal());
        }
    }
    @Nullable
    public static IAlleleBeeSpecies getSpecies(@NotNull Mods mod, @NotNull String name) {
        String s = switch (mod) {
            case ExtraBees -> "extrabees.species." + name;
            case MagicBees -> "magicbees.species" + name;
            case GregTech -> "gregtech.species." + name;
            default -> "forestry.species" + name;
        };
        return (IAlleleBeeSpecies) AlleleManager.alleleRegistry.getAllele(s);
    }
}
