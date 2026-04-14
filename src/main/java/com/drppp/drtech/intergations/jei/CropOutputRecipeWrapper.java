package com.drppp.drtech.intergations.jei;


import com.drppp.drtech.api.crop.CropType;
import com.drppp.drtech.common.Items.ItemCropSeed;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CropOutputRecipeWrapper implements IRecipeWrapper {

    private final CropType cropType;
    private final ItemStack input;
    private final List<ItemStack> allOutputs;
    private final boolean hasBlockDrops;

    public CropOutputRecipeWrapper(CropType cropType) {
        this.cropType = cropType;
        this.input = ItemCropSeed.createSeedBag(cropType.getId());
        this.allOutputs = new ArrayList<>();
        // 固定掉落
        allOutputs.addAll(cropType.getDrops());
        // 概率掉落
        for (CropType.ChanceDrop cd : cropType.getChanceDrops()) {
            allOutputs.add(cd.item.copy());
        }
        // 方块特定掉落(全部收集进来展示)
        this.hasBlockDrops = !cropType.getBlockDrops().isEmpty();
        for (List<ItemStack> blockItems : cropType.getBlockDrops().values()) {
            for (ItemStack item : blockItems) {
                // 避免重复添加已有的物品
                boolean dup = false;
                for (ItemStack existing : allOutputs) {
                    if (ItemStack.areItemsEqual(existing, item)) { dup = true; break; }
                }
                if (!dup) allOutputs.add(item.copy());
            }
        }
        for (List<CropType.ChanceDrop> cds : cropType.getBlockChanceDrops().values()) {
            for (CropType.ChanceDrop cd : cds) {
                boolean dup = false;
                for (ItemStack existing : allOutputs) {
                    if (ItemStack.areItemsEqual(existing, cd.item)) { dup = true; break; }
                }
                if (!dup) allOutputs.add(cd.item.copy());
            }
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

        // 方块决定产出: 列出每个方块→产物
        if (hasBlockDrops) {
            int lineY = 14;
            mc.fontRenderer.drawString(TextFormatting.GOLD + "\u25C6 底下方块决定产出:", 4, lineY, 0xAA8800);
            lineY += 10;
            for (Map.Entry<String, List<ItemStack>> entry : cropType.getBlockDrops().entrySet()) {
                String blockId = entry.getKey();
                // 简化显示: 去掉modid前缀中的"minecraft:"和"gregtech:"
                String shortName = blockId;
                if (shortName.contains(":")) {
                    String[] parts = shortName.split(":");
                    if (parts.length >= 2) {
                        shortName = parts[1];
                        if (parts.length >= 3) shortName += ":" + parts[2];
                    }
                }
                // 产物名
                StringBuilder dropNames = new StringBuilder();
                for (ItemStack drop : entry.getValue()) {
                    if (dropNames.length() > 0) dropNames.append("+");
                    dropNames.append(drop.getDisplayName());
                }
                String line = TextFormatting.GRAY + shortName + TextFormatting.WHITE + " \u2192 " + TextFormatting.GREEN + dropNames;
                mc.fontRenderer.drawString(line, 6, lineY, 0xFFFFFF);
                lineY += 9;
            }
        } else if (cropType.getRequiredBlocks() != null && cropType.getRequiredBlocks().length > 0) {
            String blk = cropType.getRequiredBlocks()[0].replace("minecraft:", "");
            mc.fontRenderer.drawString(TextFormatting.RED + "\u2623 " + blk, 50, 52, 0xAA4444);
        }
    }
}