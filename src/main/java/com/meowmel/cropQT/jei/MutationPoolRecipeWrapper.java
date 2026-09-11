package com.meowmel.cropQT.jei;

import com.meowmel.cropQT.api.CropRegistry;
import com.meowmel.cropQT.api.mutation.MutationPool;
import com.meowmel.cropQT.item.ItemCropSeed;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;

import java.util.ArrayList;
import java.util.List;

/**
 * JEI 的「变异池」页：一个池一页。
 *
 * <p>池管的是「两株同类作物放在一起、又没有确定性配方时能杂交出什么」。
 * 页面上把池的成员全摆出来——玩家据此知道自己的田里能随机蹦出哪些作物。
 */
public class MutationPoolRecipeWrapper implements IRecipeWrapper {

    /** 一页最多摆几个成员。 */
    public static final int MAX_DISPLAY = 12;

    private final MutationPool pool;
    private final List<ItemStack> members;

    public MutationPoolRecipeWrapper(MutationPool pool) {
        this.pool = pool;
        this.members = new ArrayList<>();
        for (String cropId : pool.getMembers()) {
            // 只摆认得出的作物：createSeedBag 对没登记的 id 也会造出一袋空气种子，
            // 摆出来只会是一堆缺贴图的问号
            if (CropRegistry.get(cropId) == null) {
                continue;
            }
            ItemStack seedBag = ItemCropSeed.createSeedBag(cropId);
            if (!seedBag.isEmpty()) {
                this.members.add(seedBag);
            }
            if (this.members.size() >= MAX_DISPLAY) {
                break;
            }
        }
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        ingredients.setInputs(VanillaTypes.ITEM, members);
    }

    public MutationPool getPool() {
        return pool;
    }

    public List<ItemStack> getMembers() {
        return members;
    }

    @Override
    public void drawInfo(Minecraft mc, int width, int height, int mouseX, int mouseY) {
        String title = "变异池: " + pool.getName();
        mc.fontRenderer.drawString(TextFormatting.BOLD + title, 4, 4, 0x333333);

        int total = pool.getMembers().size();
        int shown = members.size();
        String count = shown < total ? ("显示 " + shown + " / " + total) : ("共 " + total + " 种");
        mc.fontRenderer.drawString(TextFormatting.GRAY + count, 4, 4 + mc.fontRenderer.FONT_HEIGHT + 1, 0x888888);

        mc.fontRenderer.drawString(TextFormatting.DARK_GRAY + "两株同类成熟作物相邻时，有几率产出池中成员",
                4, height - mc.fontRenderer.FONT_HEIGHT - 3, 0x555555);
    }

    @Override
    public String toString() {
        return "MutationPoolRecipeWrapper[" + pool.getName() + "]";
    }
}
