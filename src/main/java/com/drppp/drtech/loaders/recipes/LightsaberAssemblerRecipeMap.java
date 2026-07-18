package com.drppp.drtech.loaders.recipes;

import com.drppp.drtech.common.Items.ItemsInit;
import com.drppp.drtech.common.Items.lightsaber.FocusingCrystal;
import com.drppp.drtech.common.Items.lightsaber.ItemFocusingCrystal;
import com.drppp.drtech.common.Items.lightsaber.ItemDoubleLightsaber;
import com.drppp.drtech.common.Items.lightsaber.ItemLightsaber;
import com.drppp.drtech.common.Items.lightsaber.ItemLightsaberPart;
import com.drppp.drtech.common.Items.lightsaber.LightsaberColor;
import com.drppp.drtech.common.Items.lightsaber.LightsaberHilt;
import com.drppp.drtech.common.Items.lightsaber.LightsaberPartType;
import gregtech.api.GTValues;
import gregtech.api.mui.GTGuiTextures;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.builders.SimpleRecipeBuilder;
import gregtech.api.recipes.ui.RecipeMapUI;
import gregtech.core.sound.GTSoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;

public final class LightsaberAssemblerRecipeMap extends RecipeMap<SimpleRecipeBuilder> {
    public LightsaberAssemblerRecipeMap() {
        // MUI2 lays out eight inputs as a complete 3x3 grid and requires a ninth backing slot.
        super("lightsaber_assembler", new SimpleRecipeBuilder(),
                map -> new RecipeMapUI<>(map, false, false, false, false), 9, 1, 0, 0);
        setSound(GTSoundEvents.ASSEMBLER);
        getRecipeMapUI().buildMui2(builder -> builder.progressBar(GTGuiTextures.PROGRESS_BAR_CIRCUIT));
    }

    @Override
    public Recipe findRecipe(long voltage, List<ItemStack> itemInputs, List<FluidStack> fluidInputs,
                             boolean exactVoltage) {
        boolean hasFluids = fluidInputs != null
                && fluidInputs.stream().anyMatch(stack -> stack != null && stack.amount > 0);
        FocusingUpgradeInput focusingInput = hasFluids ? null : FocusingUpgradeInput.parse(itemInputs);
        if (focusingInput != null) {
            return createFocusingUpgradeRecipe(voltage, focusingInput);
        }
        DoubleAssemblyInput doubleInput = hasFluids ? null : DoubleAssemblyInput.parse(itemInputs);
        if (doubleInput != null) {
            return createDoubleAssemblyRecipe(voltage, doubleInput);
        }
        AssemblyInput input = hasFluids ? null : AssemblyInput.parse(itemInputs);
        if (input != null) {
            return createAssemblyRecipe(voltage, input);
        }
        if (AssemblyInput.containsAssemblyComponent(itemInputs)
                || DoubleAssemblyInput.containsLightsaber(itemInputs)) {
            return null;
        }
        return super.findRecipe(voltage, itemInputs, fluidInputs, exactVoltage);
    }

    private Recipe createAssemblyRecipe(long voltage, AssemblyInput input) {
        ItemStack output = ItemLightsaber.create(input.color, input.parts[LightsaberPartType.EMITTER.ordinal()],
                input.parts[LightsaberPartType.SWITCH_SECTION.ordinal()],
                input.parts[LightsaberPartType.BODY.ordinal()],
                input.parts[LightsaberPartType.POMMEL.ordinal()], input.focusingCrystalMask);
        Recipe recipe = recipeBuilder()
                .inputs(input.consumedStacks.toArray(new ItemStack[0]))
                .outputs(output)
                .duration(200)
                .EUt(GTValues.VA[GTValues.LV])
                .build()
                .getResult();
        return recipe != null && voltage >= Math.abs(recipe.getEUt()) ? recipe : null;
    }

    private Recipe createFocusingUpgradeRecipe(long voltage, FocusingUpgradeInput input) {
        ItemStack output = input.lightsaber.copy();
        ItemLightsaber.setFocusingCrystalMask(output, input.resultMask);
        ItemLightsaber.setActive(output, false);
        Recipe recipe = recipeBuilder()
                .inputs(input.consumedStacks.toArray(new ItemStack[0]))
                .outputs(output)
                .duration(100 * input.crystalCount)
                .EUt(GTValues.VA[GTValues.LV])
                .build()
                .getResult();
        return recipe != null && voltage >= Math.abs(recipe.getEUt()) ? recipe : null;
    }

    private Recipe createDoubleAssemblyRecipe(long voltage, DoubleAssemblyInput input) {
        Recipe recipe = recipeBuilder()
                .inputs(input.upper.copy(), input.lower.copy())
                .outputs(ItemDoubleLightsaber.create(input.upper, input.lower))
                .duration(200)
                .EUt(GTValues.VA[GTValues.LV])
                .build()
                .getResult();
        return recipe != null && voltage >= Math.abs(recipe.getEUt()) ? recipe : null;
    }

    private static final class DoubleAssemblyInput {
        private final ItemStack upper;
        private final ItemStack lower;

        private DoubleAssemblyInput(ItemStack upper, ItemStack lower) {
            this.upper = single(upper);
            this.lower = single(lower);
        }

        private static DoubleAssemblyInput parse(List<ItemStack> stacks) {
            if (stacks == null) {
                return null;
            }
            ItemStack upper = ItemStack.EMPTY;
            ItemStack lower = ItemStack.EMPTY;
            for (ItemStack stack : stacks) {
                if (stack == null || stack.isEmpty()) {
                    continue;
                }
                if (!(stack.getItem() instanceof ItemLightsaber)) {
                    return null;
                }
                if (upper.isEmpty()) {
                    upper = stack;
                } else if (lower.isEmpty()) {
                    lower = stack;
                } else {
                    return null;
                }
            }
            return upper.isEmpty() || lower.isEmpty() ? null : new DoubleAssemblyInput(upper, lower);
        }

        private static boolean containsLightsaber(List<ItemStack> stacks) {
            if (stacks == null) {
                return false;
            }
            for (ItemStack stack : stacks) {
                if (stack != null && !stack.isEmpty() && stack.getItem() instanceof ItemLightsaber) {
                    return true;
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

    private static final class FocusingUpgradeInput {
        private final ItemStack lightsaber;
        private final List<ItemStack> consumedStacks;
        private final int resultMask;
        private final int crystalCount;

        private FocusingUpgradeInput(ItemStack lightsaber, List<ItemStack> consumedStacks,
                                     int resultMask, int crystalCount) {
            this.lightsaber = lightsaber;
            this.consumedStacks = consumedStacks;
            this.resultMask = resultMask;
            this.crystalCount = crystalCount;
        }

        private static FocusingUpgradeInput parse(List<ItemStack> stacks) {
            if (stacks == null) {
                return null;
            }
            ItemStack lightsaber = ItemStack.EMPTY;
            List<ItemStack> consumedStacks = new ArrayList<>();
            int addedMask = 0;
            int crystalCount = 0;
            for (ItemStack stack : stacks) {
                if (stack == null || stack.isEmpty()) {
                    continue;
                }
                ItemStack consumed = stack.copy();
                consumed.setCount(1);
                if (stack.getItem() instanceof ItemLightsaber) {
                    if (!lightsaber.isEmpty()) {
                        return null;
                    }
                    lightsaber = consumed;
                } else if (stack.getItem() instanceof ItemFocusingCrystal) {
                    FocusingCrystal crystal = FocusingCrystal.byMetadata(stack.getMetadata());
                    if ((addedMask & crystal.getMask()) != 0) {
                        return null;
                    }
                    addedMask |= crystal.getMask();
                    crystalCount++;
                } else {
                    return null;
                }
                consumedStacks.add(consumed);
            }
            if (lightsaber.isEmpty() || crystalCount == 0) {
                return null;
            }
            int existingMask = ItemLightsaber.getFocusingCrystalMask(lightsaber);
            if ((existingMask & addedMask) != 0 || Integer.bitCount(existingMask | addedMask) > 2) {
                return null;
            }
            return new FocusingUpgradeInput(lightsaber, consumedStacks,
                    existingMask | addedMask, crystalCount);
        }
    }

    private static final class AssemblyInput {
        private final LightsaberHilt[] parts = new LightsaberHilt[LightsaberPartType.values().length];
        private final List<ItemStack> consumedStacks = new ArrayList<>();
        private LightsaberColor color;
        private int focusingCrystalMask;
        private int focusingCrystalCount;
        private boolean circuitry;

        private static AssemblyInput parse(List<ItemStack> stacks) {
            AssemblyInput input = new AssemblyInput();
            if (stacks == null) {
                return null;
            }

            for (ItemStack stack : stacks) {
                if (stack == null || stack.isEmpty()) {
                    continue;
                }
                ItemStack consumed = stack.copy();
                consumed.setCount(1);

                if (stack.getItem() instanceof ItemLightsaberPart) {
                    ItemLightsaberPart partItem = (ItemLightsaberPart) stack.getItem();
                    int index = partItem.getPartType().ordinal();
                    if (input.parts[index] != null) {
                        return null;
                    }
                    input.parts[index] = partItem.getHilt(stack);
                } else if (stack.getItem() == ItemsInit.LIGHTSABER_CIRCUITRY) {
                    if (input.circuitry) {
                        return null;
                    }
                    input.circuitry = true;
                } else if (stack.getItem() == ItemsInit.LIGHTSABER_CRYSTAL) {
                    if (input.color != null) {
                        return null;
                    }
                    input.color = LightsaberColor.byMetadata(stack.getMetadata());
                } else if (stack.getItem() instanceof ItemFocusingCrystal) {
                    FocusingCrystal crystal = FocusingCrystal.byMetadata(stack.getMetadata());
                    if (input.focusingCrystalCount >= 2 || (input.focusingCrystalMask & crystal.getMask()) != 0) {
                        return null;
                    }
                    input.focusingCrystalMask |= crystal.getMask();
                    input.focusingCrystalCount++;
                } else {
                    return null;
                }
                input.consumedStacks.add(consumed);
            }

            if (!input.circuitry || input.color == null) {
                return null;
            }
            for (LightsaberHilt part : input.parts) {
                if (part == null) {
                    return null;
                }
            }
            return input;
        }

        private static boolean containsAssemblyComponent(List<ItemStack> stacks) {
            if (stacks == null) {
                return false;
            }
            for (ItemStack stack : stacks) {
                if (stack != null && !stack.isEmpty()
                        && (stack.getItem() instanceof ItemLightsaberPart
                        || stack.getItem() == ItemsInit.LIGHTSABER_CIRCUITRY
                        || stack.getItem() == ItemsInit.LIGHTSABER_CRYSTAL
                        || stack.getItem() instanceof ItemFocusingCrystal)) {
                    return true;
                }
            }
            return false;
        }
    }
}
