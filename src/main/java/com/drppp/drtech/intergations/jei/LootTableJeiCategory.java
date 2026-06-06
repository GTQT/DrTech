package com.drppp.drtech.intergations.jei;

import com.drppp.drtech.common.Items.MetaItems.MetaItemLootTable;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;
import net.minecraft.client.resources.I18n;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class LootTableJeiCategory implements IRecipeCategory<LootTableJeiWrapper> {

    private final IDrawable background;
    private final IDrawable icon;

    private static final int COLS = 9;
    private static final int SLOT_SIZE = 18;
    private static final int BG_WIDTH = 176;
    private static final int BG_HEIGHT = 110;

    // 物品格居中偏移
    private static final int OUTPUT_START_X = (BG_WIDTH - COLS * SLOT_SIZE) / 2; // 7
    private static final int INPUT_X = (BG_WIDTH - SLOT_SIZE) / 2; // 79

    private static final int INPUT_Y = 6;
    private static final int OUTPUT_START_Y = 30;

    public LootTableJeiCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(BG_WIDTH, BG_HEIGHT);
        this.icon = guiHelper.createDrawableIngredient(
                MetaItemLootTable.LOOT_BAG_ITEMS.getOrDefault("test",
                        MetaItemLootTable.LOOT_BAG_ITEMS.values().stream().findFirst()
                                .orElse(net.minecraft.item.ItemStack.EMPTY)));
    }

    @Override
    @Nonnull
    public String getUid() {
        return LootTableJeiPlugin.LOOT_TABLE_UID;
    }

    @Override
    @Nonnull
    public String getTitle() {
        return I18n.format("jei.loot_table.category");
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
    public void setRecipe(@Nonnull IRecipeLayout layout, @Nonnull LootTableJeiWrapper recipe,
                          @Nonnull IIngredients ingredients) {
        IGuiItemStackGroup stacks = layout.getItemStacks();

        // Slot 0: 奖励袋（第一行居中）
        stacks.init(0, true, INPUT_X, INPUT_Y);

        // Slots 1+: 产出物品，每行9个，第三行开始
        int outputCount = recipe.getOutputs().size();
        for (int i = 0; i < outputCount && i < COLS * 4; i++) {
            int row = i / COLS;
            int col = i % COLS;
            stacks.init(1 + i, false,
                    new ChanceTextRenderer(recipe.getEntryChance(i)),
                    OUTPUT_START_X + col * SLOT_SIZE,
                    OUTPUT_START_Y + row * SLOT_SIZE,
                    SLOT_SIZE, SLOT_SIZE, 0, 0);
        }

        stacks.set(ingredients);
    }
}
