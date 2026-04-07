package com.drppp.drtech.intergations.jei;


import com.drppp.drtech.common.Blocks.Crops.CropType;
import com.drppp.drtech.common.Items.ItemCropSeed;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;

import java.util.ArrayList;
import java.util.List;
public class CropOutputRecipeWrapper implements IRecipeWrapper {

    private final CropType cropType;
    private final ItemStack input;
    private final List<ItemStack> allOutputs;

    public CropOutputRecipeWrapper(CropType cropType) {
        this.cropType = cropType;
        this.input = ItemCropSeed.createSeedBag(cropType.getId());
        this.allOutputs = new ArrayList<>();
        // 固定掉落
        allOutputs.addAll(cropType.getDrops());
        // 概率掉落(也显示, 在drawInfo中标注概率)
        for (CropType.ChanceDrop cd : cropType.getChanceDrops()) {
            allOutputs.add(cd.item.copy());
        }
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        ingredients.setInput(VanillaTypes.ITEM, input);
        ingredients.setOutputs(VanillaTypes.ITEM, allOutputs);
    }

    public ItemStack getInput() { return input; }
    public List<ItemStack> getAllOutputs() { return allOutputs; }

    @Override
    public void drawInfo(Minecraft mc, int w, int h, int mx, int my) {
        mc.fontRenderer.drawString(TextFormatting.BOLD + cropType.getDisplayName(), 4, 4, 0x333333);

        String tier = "Tier " + cropType.getTier();
        mc.fontRenderer.drawString(tier, 4, 52, 0x888888);

        // 概率掉落标注
        int fixedCount = cropType.getDrops().size();
        List<CropType.ChanceDrop> chances = cropType.getChanceDrops();
        for (int i = 0; i < chances.size(); i++) {
            int slotX = 85 + (fixedCount + i) * 18;
            String pct = Math.round(chances.get(i).chance * 100) + "%";
            mc.fontRenderer.drawString(TextFormatting.GOLD + pct, slotX, 42, 0xAA8800);
        }

        // 战利品表标注
        if (cropType.getLootTable() != null) {
            mc.fontRenderer.drawString(TextFormatting.LIGHT_PURPLE + "\u2622 战利品表", 50, 52, 0x8844AA);
        }

        // 特殊需求
        int infoX = 90;
        if (cropType.getRequiredBlocks() != null && cropType.getRequiredBlocks().length > 0) {
            String blk = cropType.getRequiredBlocks()[0].replace("minecraft:", "");
            mc.fontRenderer.drawString(TextFormatting.RED + blk, infoX, 52, 0xAA4444);
        }
    }
}