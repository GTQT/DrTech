package com.meowmel.cropQT.jei;

import com.meowmel.cropQT.api.SubSoilRequirement;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;

import java.util.ArrayList;
import java.util.List;

/**
 * JEI 的「底土」页：一份底土要求一页。
 *
 * <p>底土是作物架下方<b>第二格</b>。与土壤不同，它不满足不会挡住种植——
 * 只是把生长速度压到极低，所以页面上标的是「软惩罚」而不是「必要条件」。
 */
public class SubSoilRecipeWrapper implements IRecipeWrapper {

    /** 一页最多摆几个可还原成物品的成员。 */
    public static final int MAX_DISPLAY = 8;

    private final SubSoilRequirement requirement;
    private final List<ItemStack> displayItems;

    public SubSoilRecipeWrapper(SubSoilRequirement requirement) {
        this.requirement = requirement;
        this.displayItems = new ArrayList<>();
        ItemStack representative = requirement.getRepresentative();
        if (!representative.isEmpty()) {
            this.displayItems.add(representative.copy());
        }
        for (ItemStack stack : requirement.getDisplayItems()) {
            if (stack.isEmpty() || containsSame(stack)) {
                continue;
            }
            this.displayItems.add(stack.copy());
            if (this.displayItems.size() >= MAX_DISPLAY) {
                break;
            }
        }
    }

    private boolean containsSame(ItemStack stack) {
        for (ItemStack existing : displayItems) {
            if (ItemStack.areItemsEqual(existing, stack) && ItemStack.areItemStackTagsEqual(existing, stack)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        ingredients.setInputs(VanillaTypes.ITEM, displayItems);
    }

    public SubSoilRequirement getRequirement() {
        return requirement;
    }

    public List<ItemStack> getDisplayItems() {
        return displayItems;
    }

    @Override
    public void drawInfo(Minecraft mc, int width, int height, int mouseX, int mouseY) {
        mc.fontRenderer.drawString(TextFormatting.BOLD + requirement.getDisplayName(), 4, 4, 0x333333);
        mc.fontRenderer.drawString(TextFormatting.GRAY + "内部名: " + requirement.getName(),
                4, 4 + mc.fontRenderer.FONT_HEIGHT + 1, 0x888888);

        int matched = requirement.getBlockIds().size();
        mc.fontRenderer.drawString(TextFormatting.GRAY + "可命名的方块数: " + TextFormatting.WHITE + matched,
                4, height - mc.fontRenderer.FONT_HEIGHT * 2 - 4, 0xFFFFFF);
        mc.fontRenderer.drawString(TextFormatting.DARK_RED + "不满足只是长得慢，不会挡住种植",
                4, height - mc.fontRenderer.FONT_HEIGHT - 3, 0xAA4444);
    }

    @Override
    public String toString() {
        return "SubSoilRecipeWrapper[" + requirement.getName() + "]";
    }
}
