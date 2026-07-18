package com.drppp.drtech.loaders.recipes;

import com.drppp.drtech.common.Items.ItemsInit;
import com.drppp.drtech.common.Items.lightsaber.FocusingCrystal;
import com.drppp.drtech.common.Items.lightsaber.ItemDoubleLightsaber;
import com.drppp.drtech.common.Items.lightsaber.ItemLightsaber;
import com.drppp.drtech.common.Items.lightsaber.LightsaberPartType;
import gregtech.api.GTValues;
import gregtech.api.mui.GTGuiTextures;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.builders.SimpleRecipeBuilder;
import gregtech.api.recipes.ui.RecipeMapUI;
import gregtech.core.sound.GTSoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;

public final class LightsaberDisassemblerRecipeMap extends RecipeMap<SimpleRecipeBuilder> {
    public LightsaberDisassemblerRecipeMap() {
        super("disassembler", new SimpleRecipeBuilder(),
                map -> new RecipeMapUI<>(map, false, false, false, false), 1, 16, 0, 0);
        setSound(GTSoundEvents.ASSEMBLER);
        getRecipeMapUI().buildMui2(builder -> builder
                .itemSlotOverlay(GTGuiTextures.CIRCUIT_OVERLAY, false)
                .progressBar(GTGuiTextures.PROGRESS_BAR_CIRCUIT));
    }

    @Override
    public Recipe findRecipe(long voltage, List<ItemStack> itemInputs, List<FluidStack> fluidInputs,
                             boolean exactVoltage) {
        ItemStack input = getLightsaberInput(itemInputs, fluidInputs);
        if (!input.isEmpty()) {
            List<ItemStack> outputs = getDisassemblyOutputs(input);
            Recipe recipe = recipeBuilder()
                    .inputs(single(input))
                    .outputs(outputs.toArray(new ItemStack[0]))
                    .duration(input.getItem() instanceof ItemDoubleLightsaber ? 400 : 200)
                    .EUt(GTValues.VA[GTValues.LV])
                    .build()
                    .getResult();
            return recipe != null && voltage >= Math.abs(recipe.getEUt()) ? recipe : null;
        }
        if (containsLightsaber(itemInputs)) {
            return null;
        }
        return super.findRecipe(voltage, itemInputs, fluidInputs, exactVoltage);
    }

    public static List<ItemStack> getDisassemblyOutputs(ItemStack input) {
        List<ItemStack> outputs = new ArrayList<>();
        if (input.getItem() instanceof ItemDoubleLightsaber) {
            addSingleLightsaberOutputs(outputs, ItemDoubleLightsaber.getUpper(input));
            addSingleLightsaberOutputs(outputs, ItemDoubleLightsaber.getLower(input));
        } else if (input.getItem() instanceof ItemLightsaber) {
            addSingleLightsaberOutputs(outputs, input);
        }
        return outputs;
    }

    private static void addSingleLightsaberOutputs(List<ItemStack> outputs, ItemStack lightsaber) {
        for (LightsaberPartType type : LightsaberPartType.values()) {
            outputs.add(new ItemStack(getPartItem(type), 1,
                    ItemLightsaber.getPart(lightsaber, type).getMetadata()));
        }
        outputs.add(new ItemStack(ItemsInit.LIGHTSABER_CIRCUITRY));
        outputs.add(new ItemStack(ItemsInit.LIGHTSABER_CRYSTAL, 1,
                ItemLightsaber.getColor(lightsaber).getMetadata()));
        int focusingMask = ItemLightsaber.getFocusingCrystalMask(lightsaber);
        for (FocusingCrystal crystal : FocusingCrystal.values()) {
            if ((focusingMask & crystal.getMask()) != 0) {
                outputs.add(new ItemStack(ItemsInit.FOCUSING_CRYSTAL, 1, crystal.getMetadata()));
            }
        }
    }

    private static Item getPartItem(LightsaberPartType type) {
        switch (type) {
            case EMITTER:
                return ItemsInit.LIGHTSABER_EMITTER;
            case SWITCH_SECTION:
                return ItemsInit.LIGHTSABER_SWITCH;
            case BODY:
                return ItemsInit.LIGHTSABER_GRIP;
            case POMMEL:
                return ItemsInit.LIGHTSABER_POMMEL;
            default:
                throw new IllegalArgumentException("Unknown lightsaber part type: " + type);
        }
    }

    private static ItemStack getLightsaberInput(List<ItemStack> itemInputs, List<FluidStack> fluidInputs) {
        if (fluidInputs != null && fluidInputs.stream().anyMatch(stack -> stack != null && stack.amount > 0)) {
            return ItemStack.EMPTY;
        }
        ItemStack input = ItemStack.EMPTY;
        if (itemInputs != null) {
            for (ItemStack stack : itemInputs) {
                if (stack == null || stack.isEmpty()) {
                    continue;
                }
                if (!input.isEmpty() || !(stack.getItem() instanceof ItemLightsaber
                        || stack.getItem() instanceof ItemDoubleLightsaber)) {
                    return ItemStack.EMPTY;
                }
                input = stack;
            }
        }
        return input;
    }

    private static boolean containsLightsaber(List<ItemStack> itemInputs) {
        if (itemInputs != null) {
            for (ItemStack stack : itemInputs) {
                if (stack != null && !stack.isEmpty()
                        && (stack.getItem() instanceof ItemLightsaber
                        || stack.getItem() instanceof ItemDoubleLightsaber)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static ItemStack single(ItemStack stack) {
        ItemStack copy = stack.copy();
        copy.setCount(1);
        return copy;
    }
}
