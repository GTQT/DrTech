package com.drppp.drtech.common.drone.inventory;

import com.drppp.drtech.common.drone.filter.DroneFilterMode;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.common.crafting.IShapedRecipe;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;

import java.util.Arrays;

/** Conservative Forge recipe planner. Every craft is simulated against a cargo copy before commit. */
public final class DroneCraftingPlanner {

    public static final int MAX_CRAFTS_PER_ACTION = 64;

    private DroneCraftingPlanner() {}

    public static int craft(ItemStackHandler cargo, int activeSlots, World world, DroneItemFilter outputFilter,
            int requestedCrafts, boolean simulate) {
        return craft(cargo, activeSlots, world, outputFilter, requestedCrafts, simulate, null, 0);
    }

    public static int craft(ItemStackHandler cargo, int activeSlots, World world, DroneItemFilter outputFilter,
            int requestedCrafts, boolean simulate, DroneItemFilter reserveFilter, int reserveAmount) {
        return craftInternal(cargo, activeSlots, world, outputFilter, requestedCrafts, simulate,
                reserveFilter, reserveAmount, null);
    }

    public static int craftGrid(ItemStackHandler cargo, int activeSlots, World world, DroneItemFilter outputFilter,
            DroneItemFilter[] gridFilters, int requestedCrafts, boolean simulate,
            DroneItemFilter reserveFilter, int reserveAmount) {
        if (gridFilters == null || gridFilters.length != 9) return 0;
        return craftInternal(cargo, activeSlots, world, outputFilter, requestedCrafts, simulate,
                reserveFilter, reserveAmount, Arrays.copyOf(gridFilters, gridFilters.length));
    }

    private static int craftInternal(ItemStackHandler cargo, int activeSlots, World world,
            DroneItemFilter outputFilter, int requestedCrafts, boolean simulate,
            DroneItemFilter reserveFilter, int reserveAmount, DroneItemFilter[] gridFilters) {
        if (cargo == null || world == null || outputFilter == null
                || outputFilter.getSpec().getMode() != DroneFilterMode.WHITELIST
                || outputFilter.getSpec().getRules().isEmpty()) return 0;
        int slots = Math.max(0, Math.min(activeSlots, cargo.getSlots()));
        int maximum = Math.max(1, Math.min(MAX_CRAFTS_PER_ACTION, requestedCrafts));
        int reserved = Math.max(0, reserveAmount);
        ItemStackHandler working = copyCargo(cargo, slots);
        int crafted = 0;
        while (crafted < maximum) {
            Plan plan = findPlan(working, slots, world, outputFilter, reserveFilter, reserved, gridFilters);
            if (plan == null) break;
            apply(working, plan);
            crafted++;
        }
        if (!simulate && crafted > 0) {
            for (int slot = 0; slot < slots; slot++) {
                cargo.setStackInSlot(slot, working.getStackInSlot(slot).copy());
            }
        }
        return crafted;
    }

    private static Plan findPlan(ItemStackHandler cargo, int slots, World world, DroneItemFilter outputFilter,
            DroneItemFilter reserveFilter, int reserveAmount, DroneItemFilter[] gridFilters) {
        for (IRecipe recipe : ForgeRegistries.RECIPES.getValuesCollection()) {
            ItemStack declared = recipe.getRecipeOutput();
            if (declared.isEmpty() || !outputFilter.matches(declared)) continue;
            NonNullList<Ingredient> ingredients = recipe.getIngredients();
            if (ingredients.isEmpty() || ingredients.size() > 9) continue;
            int[] consumed = new int[slots];
            InventoryCrafting grid = new InventoryCrafting(new NullContainer(), 3, 3);
            boolean shaped = recipe instanceof IShapedRecipe;
            if (gridFilters != null && !shaped) continue;
            int width = shaped ? Math.max(1, Math.min(3, ((IShapedRecipe) recipe).getRecipeWidth())) : 3;
            int height = shaped ? Math.max(1, Math.min(3, ((IShapedRecipe) recipe).getRecipeHeight())) : 3;
            if (gridFilters != null && !matchesGridShape(ingredients, width, height, gridFilters)) continue;
            int shapelessIndex = 0;
            boolean available = true;
            for (int ingredientIndex = 0; ingredientIndex < ingredients.size(); ingredientIndex++) {
                Ingredient ingredient = ingredients.get(ingredientIndex);
                if (ingredient == Ingredient.EMPTY || ingredient.getMatchingStacks().length == 0) continue;
                int gridSlot = shaped
                        ? ingredientIndex % width + ingredientIndex / width * 3
                        : shapelessIndex++;
                DroneItemFilter explicit = gridFilters == null ? null : gridFilters[gridSlot];
                int cargoSlot = findIngredientSlot(cargo, slots, consumed, ingredient, explicit);
                if (cargoSlot < 0) {
                    available = false;
                    break;
                }
                consumed[cargoSlot]++;
                ItemStack ingredientStack = cargo.getStackInSlot(cargoSlot).copy();
                ingredientStack.setCount(1);
                if (gridSlot < 0 || gridSlot >= grid.getSizeInventory()) {
                    available = false;
                    break;
                }
                grid.setInventorySlotContents(gridSlot, ingredientStack);
            }
            if (!available || !safeMatches(recipe, grid, world)) continue;
            ItemStack output;
            NonNullList<ItemStack> remaining;
            try {
                output = recipe.getCraftingResult(grid);
                remaining = recipe.getRemainingItems(grid);
            } catch (RuntimeException ignored) {
                continue;
            }
            if (output.isEmpty() || !outputFilter.matches(output)) continue;
            Plan plan = new Plan(consumed, output.copy(), remaining);
            ItemStackHandler preview = copyCargo(cargo, slots);
            if (apply(preview, plan) && countMatching(preview, slots, reserveFilter) >= reserveAmount) return plan;
        }
        return null;
    }

    private static boolean matchesGridShape(NonNullList<Ingredient> ingredients, int width, int height,
            DroneItemFilter[] gridFilters) {
        for (int gridSlot = 0; gridSlot < 9; gridSlot++) {
            int x = gridSlot % 3;
            int y = gridSlot / 3;
            int ingredientIndex = x < width && y < height ? y * width + x : -1;
            Ingredient ingredient = ingredientIndex >= 0 && ingredientIndex < ingredients.size()
                    ? ingredients.get(ingredientIndex) : Ingredient.EMPTY;
            boolean emptyIngredient = ingredient == Ingredient.EMPTY || ingredient.getMatchingStacks().length == 0;
            if (emptyIngredient != (gridFilters[gridSlot] == null)) return false;
        }
        return true;
    }

    private static int findIngredientSlot(ItemStackHandler cargo, int slots, int[] consumed, Ingredient ingredient,
            DroneItemFilter explicitFilter) {
        for (int slot = 0; slot < slots; slot++) {
            ItemStack stack = cargo.getStackInSlot(slot);
            if (!stack.isEmpty() && stack.getCount() > consumed[slot] && ingredient.apply(stack)
                    && (explicitFilter == null || explicitFilter.matches(stack))) return slot;
        }
        return -1;
    }

    private static boolean safeMatches(IRecipe recipe, InventoryCrafting grid, World world) {
        try {
            return recipe.matches(grid, world);
        } catch (RuntimeException ignored) {
            return false;
        }
    }

    private static boolean apply(ItemStackHandler cargo, Plan plan) {
        for (int slot = 0; slot < plan.consumed.length; slot++) {
            if (plan.consumed[slot] <= 0) continue;
            ItemStack extracted = cargo.extractItem(slot, plan.consumed[slot], false);
            if (extracted.getCount() != plan.consumed[slot]) return false;
        }
        if (!ItemHandlerHelper.insertItemStacked(cargo, plan.output.copy(), false).isEmpty()) return false;
        for (ItemStack remaining : plan.remaining) {
            if (!remaining.isEmpty()
                    && !ItemHandlerHelper.insertItemStacked(cargo, remaining.copy(), false).isEmpty()) return false;
        }
        return true;
    }

    private static int countMatching(ItemStackHandler cargo, int slots, DroneItemFilter filter) {
        if (filter == null) return Integer.MAX_VALUE;
        int count = 0;
        for (int slot = 0; slot < slots; slot++) {
            ItemStack stack = cargo.getStackInSlot(slot);
            if (!stack.isEmpty() && filter.matches(stack)) count += stack.getCount();
        }
        return count;
    }

    private static ItemStackHandler copyCargo(ItemStackHandler source, int slots) {
        ItemStackHandler copy = new ItemStackHandler(slots);
        for (int slot = 0; slot < slots; slot++) copy.setStackInSlot(slot, source.getStackInSlot(slot).copy());
        return copy;
    }

    private static final class Plan {
        private final int[] consumed;
        private final ItemStack output;
        private final NonNullList<ItemStack> remaining;

        private Plan(int[] consumed, ItemStack output, NonNullList<ItemStack> remaining) {
            this.consumed = Arrays.copyOf(consumed, consumed.length);
            this.output = output;
            this.remaining = NonNullList.create();
            for (ItemStack stack : remaining) this.remaining.add(stack.copy());
        }
    }

    private static final class NullContainer extends Container {
        @Override
        public boolean canInteractWith(EntityPlayer playerIn) {
            return false;
        }
    }
}
