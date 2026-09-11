package com.meowmel.cropQT.jei;

import com.meowmel.cropQT.api.ISoilList;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;

import java.util.ArrayList;
import java.util.List;

/**
 * JEI 的「土壤」页：一个土壤组一页。
 *
 * <p>土壤是作物架脚下那一格，决定<b>能种什么</b>（硬门槛）与<b>能存多少水肥</b>。
 * 这里把该组的代表方块、水肥上限、基础营养、每周期水肥消耗一次列清。
 */
public class SoilRecipeWrapper implements IRecipeWrapper {

    /** 一页最多摆几个代表方块。 */
    public static final int MAX_DISPLAY = 8;

    /** 方块区起始 Y 与行高，{@link SoilCategory} 的排版要和这里对齐。 */
    public static final int SLOTS_Y = 20;
    public static final int SLOT_STEP = 18;

    private final ISoilList soil;
    private final List<ItemStack> displayItems;

    public SoilRecipeWrapper(ISoilList soil) {
        this.soil = soil;
        this.displayItems = new ArrayList<>();
        for (ItemStack stack : soil.getDisplayItems()) {
            if (stack.isEmpty()) {
                continue;
            }
            this.displayItems.add(stack.copy());
            if (this.displayItems.size() >= MAX_DISPLAY) {
                break;
            }
        }
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        ingredients.setInputs(VanillaTypes.ITEM, displayItems);
    }

    public ISoilList getSoil() {
        return soil;
    }

    public List<ItemStack> getDisplayItems() {
        return displayItems;
    }

    /** 本地化组名；没配 lang key 时退回内部名。 */
    public String getSoilName() {
        String key = "cropqt.soil." + soil.getName();
        String text = I18n.format(key);
        return text.startsWith("cropqt.soil.") ? soil.getName() : text;
    }

    @Override
    public void drawInfo(Minecraft mc, int width, int height, int mouseX, int mouseY) {
        mc.fontRenderer.drawString(TextFormatting.BOLD + getSoilName(), 4, 4, 0x333333);

        // 文字从方块区下面开始，别压住格子
        int rows = Math.max(1, (displayItems.size() + 3) / 4);
        int y = SLOTS_Y + rows * SLOT_STEP + 4;

        y = drawLine(mc, TextFormatting.AQUA, "保水 / 保肥上限: ",
                soil.getWaterCapacity() + " / " + soil.getFertilizerCapacity(), y);
        y = drawLine(mc, TextFormatting.GOLD, "基础营养: ",
                String.format("%.2f", soil.getBaseNutrients()), y);
        y = drawLine(mc, TextFormatting.GRAY, "每周期耗水 / 耗肥 (tier 1): ",
                soil.getWaterUsage(1) + " / " + soil.getFertilizerUsage(1), y);

        mc.fontRenderer.drawString(TextFormatting.DARK_GRAY + "作物 tier 越高，每周期消耗越多",
                4, y + 2, 0x555555);
    }

    private int drawLine(Minecraft mc, TextFormatting color, String label, String value, int y) {
        mc.fontRenderer.drawString(color + label + TextFormatting.WHITE + value, 4, y, 0xFFFFFF);
        return y + mc.fontRenderer.FONT_HEIGHT + 1;
    }

    @Override
    public String toString() {
        return "SoilRecipeWrapper[" + soil.getName() + "]";
    }
}
