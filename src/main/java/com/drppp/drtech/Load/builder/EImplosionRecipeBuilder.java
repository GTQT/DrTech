package com.drppp.drtech.Load.builder;

import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.ingredients.GTRecipeItemInput;
import gregtech.api.recipes.recipeproperties.ImplosionExplosiveProperty;
import gregtech.api.util.EnumValidationResult;
import gregtech.api.util.GTLog;
import gregtech.api.util.ValidationResult;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.jetbrains.annotations.NotNull;


public class EImplosionRecipeBuilder extends RecipeBuilder<EImplosionRecipeBuilder> {
    public EImplosionRecipeBuilder() {
    }

    public EImplosionRecipeBuilder(Recipe recipe, RecipeMap<EImplosionRecipeBuilder> recipeMap) {
        super(recipe, recipeMap);
    }

    public EImplosionRecipeBuilder(RecipeBuilder<EImplosionRecipeBuilder> recipeBuilder) {
        super(recipeBuilder);
    }

    public EImplosionRecipeBuilder copy() {
        return new EImplosionRecipeBuilder(this);
    }

    public boolean applyProperty(@NotNull String key, ItemStack value) {
        if (key.equals(ImplosionExplosiveProperty.KEY)) {
            if (value instanceof ItemStack) {
                this.applyProperty(ImplosionExplosiveProperty.getInstance(), value);
                return true;
            } else {
                this.applyProperty(ImplosionExplosiveProperty.getInstance(), value);
            }

        }
        return super.applyProperty(key, value);
    }

    public EImplosionRecipeBuilder explosivesAmount(ItemStack explosivesAmount) {
        this.applyProperty(ImplosionExplosiveProperty.getInstance(), explosivesAmount);
        return this;
    }

    public EImplosionRecipeBuilder explosivesType(ItemStack explosivesType) {
        if (1 > explosivesType.getCount() || explosivesType.getCount() > 64) {
            GTLog.logger.error("Amount of explosives should be from 1 to 64 inclusive", new IllegalArgumentException());
            this.recipeStatus = EnumValidationResult.INVALID;
        }

        this.applyProperty(ImplosionExplosiveProperty.getInstance(), explosivesType);
        return this;
    }

    public ItemStack getExplosivesType() {
        return this.recipePropertyStorage == null ? ItemStack.EMPTY : (ItemStack)this.recipePropertyStorage.getRecipePropertyValue(ImplosionExplosiveProperty.getInstance(), ItemStack.EMPTY);
    }

    public ValidationResult<Recipe> build() {
        ItemStack explosivesType = this.getExplosivesType();
        if (!explosivesType.isEmpty()) {
            this.inputs.add(new GTRecipeItemInput(explosivesType));
        } else {
            this.recipePropertyStorageErrored = true;
        }

        return super.build();
    }

    public String toString() {
        return (new ToStringBuilder(this)).appendSuper(super.toString()).append(ImplosionExplosiveProperty.getInstance().getKey(), this.getExplosivesType()).toString();
    }
}