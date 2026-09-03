package com.drppp.drtech.intergations.jei;

import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * "怪物信息"分类：一张页面 = 一只生物。
 * 布局照 JustEnoughDrops-1.1.1 的 MobInfoCategory：左上裁剪区画实体，
 * 掉落物 4 个/行网格放在右侧，鼠标悬停掉落物显示概率/条件（见 MobInfoWrapper.onTooltip）。
 */
public class MobInfoCategory implements IRecipeCategory<MobInfoWrapper> {

    private static final int BG_WIDTH = 173;
    private static final int BG_HEIGHT = 130;
    private static final int ITEMS_PER_ROW = 4;
    private static final int SLOT_SPACING = 18;
    private static final int X_FIRST_ITEM = 100;
    private static final int Y_FIRST_ITEM = 38;

    private final IDrawable background;
    private final IDrawable icon;

    public MobInfoCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(BG_WIDTH, BG_HEIGHT);
        this.icon = guiHelper.createDrawableIngredient(new ItemStack(Items.SPAWN_EGG));
    }

    @Override
    @Nonnull
    public String getUid() {
        return MobInfoJeiPlugin.UID;
    }

    @Override
    @Nonnull
    public String getTitle() {
        return I18n.format("jei.mob_info.category");
    }

    @Override
    @Nonnull
    public String getModName() {
        return "DRTech";
    }

    @Override
    @Nonnull
    public IDrawable getBackground() {
        return background;
    }

    @Nullable
    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(@Nonnull IRecipeLayout layout, @Nonnull MobInfoWrapper recipe,
                          @Nonnull IIngredients ingredients) {
        IGuiItemStackGroup stacks = layout.getItemStacks();

        // 摊平掉落列表（含烤制产物，顺序与 getIngredients 输出一致）
        List<ItemStack> flat = new ArrayList<>();
        for (LootDropInfo drop : recipe.getEntry().getDrops()) {
            flat.addAll(drop.getDrops());
        }

        int slot = 0;
        for (int i = 0; i < flat.size(); i++) {
            int row = i / ITEMS_PER_ROW;
            int col = i % ITEMS_PER_ROW;
            stacks.init(slot++, false,
                    X_FIRST_ITEM + col * SLOT_SPACING,
                    Y_FIRST_ITEM + row * SLOT_SPACING);
        }
        for (int i = 0; i < flat.size(); i++) {
            stacks.set(i, flat.get(i));
        }
        stacks.addTooltipCallback(recipe);
    }
}
