package com.meowmel.cropQT.jei;

import com.meowmel.cropQT.api.CropRegistry;
import com.meowmel.cropQT.api.CropType;
import com.meowmel.cropQT.api.mutation.CropMutation;
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
 * 一条杂交配方的 JEI 展示。
 *
 * <p>支持 2~4 个父本——父本越多槽位排得越开，见 {@link #slotX(int)}。
 */
public class CrossBreedingRecipeWrapper implements IRecipeWrapper {

    /** 父本槽位与产物槽位的横坐标（背景宽 160）。 */
    private static final int[] PARENT_SLOT_X = {6, 36, 66, 96};
    private static final int RESULT_SLOT_X = 132;
    private static final int SLOT_Y = 24;

    private final CropMutation mutation;
    private final CropType resultType;
    private final List<ItemStack> parentSeeds = new ArrayList<>();
    private final ItemStack resultSeed;

    public CrossBreedingRecipeWrapper(CropMutation mutation, CropType resultType, List<CropType> parentTypes) {
        this.mutation = mutation;
        this.resultType = resultType;
        for (CropType parent : parentTypes) {
            parentSeeds.add(ItemCropSeed.createSeedBag(parent.getId()));
        }
        this.resultSeed = ItemCropSeed.createSeedBag(resultType.getId());
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        ingredients.setInputs(VanillaTypes.ITEM, parentSeeds);
        ingredients.setOutput(VanillaTypes.ITEM, resultSeed);
    }

    /** 第 {@code index} 个父本的槽位横坐标。 */
    public static int slotX(int index) {
        return index < PARENT_SLOT_X.length ? PARENT_SLOT_X[index] : RESULT_SLOT_X;
    }

    public static int slotY() {
        return SLOT_Y;
    }

    public static int resultSlotX() {
        return RESULT_SLOT_X;
    }

    public List<ItemStack> getParentSeeds() {
        return parentSeeds;
    }

    public ItemStack getResultSeed() {
        return resultSeed;
    }

    public int getParentCount() {
        return parentSeeds.size();
    }

    @Override
    public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
        // 父本槽位之间的 "+"，以及指向产物的箭头
        for (int i = 1; i < parentSeeds.size(); i++) {
            minecraft.fontRenderer.drawString("+", slotX(i) - 12, SLOT_Y + 4, 0x808080);
        }
        minecraft.fontRenderer.drawString("→", slotX(parentSeeds.size() - 1) + 20, SLOT_Y + 4, 0x808080);

        // 标题用 " × " 连起来；父本多的时候缩短，免得压到产物那一列
        StringBuilder title = new StringBuilder();
        for (int i = 0; i < parentSeeds.size(); i++) {
            if (i > 0) {
                title.append(" × ");
            }
            title.append(shortName(i));
        }
        minecraft.fontRenderer.drawString(title.toString(), 4, 4, 0x333333);

        String resultName = TextFormatting.DARK_GREEN + resultType.getDisplayName();
        minecraft.fontRenderer.drawString(resultName, RESULT_SLOT_X - 8, 8, 0x336633);

        String info = TextFormatting.GRAY + "权重:" + mutation.getWeight()
                + " T" + resultType.getTier();
        minecraft.fontRenderer.drawString(info, 4, 52, 0x888888);
    }

    /** 父本名太长就截断，避免 4 父本时把标题撑爆。 */
    private String shortName(int index) {
        CropType type = CropRegistry.get(mutation.getParents().get(index));
        String name = type == null ? mutation.getParents().get(index) : type.getDisplayName();
        int limit = parentSeeds.size() >= 4 ? 3 : (parentSeeds.size() == 3 ? 4 : 6);
        return name.length() > limit ? name.substring(0, limit) : name;
    }
}
