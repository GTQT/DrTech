package com.meowmel.cropQT.jei;

import com.meowmel.cropQT.api.CropType;
import com.meowmel.cropQT.item.ItemCropSeed;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

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

        allOutputs.addAll(cropType.getDrops());
        for (CropType.ChanceDrop chanceDrop : cropType.getChanceDrops()) {
            allOutputs.add(chanceDrop.item.copy());
        }

        this.hasBlockDrops = !cropType.getBlockDrops().isEmpty();
        for (List<ItemStack> blockItems : cropType.getBlockDrops().values()) {
            for (ItemStack item : blockItems) {
                if (!containsItem(allOutputs, item)) {
                    allOutputs.add(item.copy());
                }
            }
        }
        for (List<CropType.ChanceDrop> chanceDrops : cropType.getBlockChanceDrops().values()) {
            for (CropType.ChanceDrop chanceDrop : chanceDrops) {
                if (!containsItem(allOutputs, chanceDrop.item)) {
                    allOutputs.add(chanceDrop.item.copy());
                }
            }
        }
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        ingredients.setInput(VanillaTypes.ITEM, input);
        ingredients.setOutputs(VanillaTypes.ITEM, allOutputs);
    }

    public ItemStack getInput() {
        return input;
    }

    public List<ItemStack> getAllOutputs() {
        return allOutputs;
    }

    @Override
    public void drawInfo(Minecraft mc, int width, int height, int mouseX, int mouseY) {
        mc.fontRenderer.drawString(TextFormatting.BOLD + cropType.getDisplayName(), 4, 4, 0x333333);
        mc.fontRenderer.drawString("Tier " + cropType.getTier(), 4, 14, 0x888888);

        int visibleOutputs = Math.min(allOutputs.size(), 8);
        drawChanceDropLabels(mc, visibleOutputs);

        if (cropType.getLootTable() != null) {
            mc.fontRenderer.drawString(TextFormatting.LIGHT_PURPLE + "* 战利品表", 50, 14, 0x8844AA);
        } else if (cropType.getRequiredBlocks() != null && cropType.getRequiredBlocks().length > 0) {
            String requiredBlock = getDisplayBlockName(cropType.getRequiredBlocks()[0]);
            mc.fontRenderer.drawString(TextFormatting.RED + "* " + requiredBlock, 50, 14, 0xAA4444);
        }

        if (!hasBlockDrops) {
            return;
        }

        int outputRows = Math.max(1, (visibleOutputs + 3) / 4);
        int sectionY = 24 + outputRows * 18 + 8;
        int textWidth = Math.max(40, width - 10);

        mc.fontRenderer.drawString(TextFormatting.GOLD + "底下方块决定产出:", 4, sectionY, 0xAA8800);
        sectionY += mc.fontRenderer.FONT_HEIGHT + 2;

        List<String> wrappedLines = new ArrayList<>();
        for (Map.Entry<String, List<ItemStack>> entry : cropType.getBlockDrops().entrySet()) {
            StringBuilder dropNames = new StringBuilder();
            for (ItemStack drop : entry.getValue()) {
                if (dropNames.length() > 0) {
                    dropNames.append(" + ");
                }
                dropNames.append(drop.getDisplayName());
            }

            String line = TextFormatting.GRAY + getDisplayBlockName(entry.getKey())
                    + TextFormatting.WHITE + " -> "
                    + TextFormatting.GREEN + dropNames;
            wrappedLines.addAll(mc.fontRenderer.listFormattedStringToWidth(line, textWidth));
        }

        int availableLines = Math.max(0, (height - sectionY - 2) / mc.fontRenderer.FONT_HEIGHT);
        if (availableLines <= 0) {
            return;
        }

        int visibleLines = Math.min(wrappedLines.size(), availableLines);
        boolean truncated = wrappedLines.size() > availableLines;
        if (truncated) {
            visibleLines = Math.max(0, availableLines - 1);
        }

        for (int i = 0; i < visibleLines; i++) {
            mc.fontRenderer.drawString(wrappedLines.get(i), 6, sectionY, 0xFFFFFF);
            sectionY += mc.fontRenderer.FONT_HEIGHT;
        }

        if (truncated) {
            int hiddenLines = wrappedLines.size() - visibleLines;
            mc.fontRenderer.drawString(TextFormatting.GRAY + "... 还有 " + hiddenLines + " 行", 6, sectionY, 0x888888);
        }
    }

    private void drawChanceDropLabels(Minecraft mc, int visibleOutputs) {
        int fixedCount = cropType.getDrops().size();
        List<CropType.ChanceDrop> chanceDrops = cropType.getChanceDrops();

        for (int i = 0; i < chanceDrops.size(); i++) {
            int visibleSlot = fixedCount + i;
            if (visibleSlot >= visibleOutputs) {
                break;
            }

            int row = visibleSlot / 4;
            int col = visibleSlot % 4;
            int slotX = 85 + col * 18;
            int labelY = 24 + row * 18 + 17;
            String percent = Math.round(chanceDrops.get(i).chance * 100) + "%";
            mc.fontRenderer.drawString(TextFormatting.GOLD + percent, slotX, labelY, 0xAA8800);
        }
    }

    private String getDisplayBlockName(String blockId) {
        String registryName = blockId;
        int meta = 0;

        int firstColon = blockId.indexOf(':');
        int lastColon = blockId.lastIndexOf(':');
        if (firstColon != -1 && lastColon > firstColon) {
            String suffix = blockId.substring(lastColon + 1);
            if (isNumeric(suffix)) {
                registryName = blockId.substring(0, lastColon);
                meta = Integer.parseInt(suffix);
            }
        }

        try {
            Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(registryName));
            if (block != null && block != Blocks.AIR) {
                ItemStack stack = new ItemStack(block, 1, meta);
                if (!stack.isEmpty()) {
                    return stack.getDisplayName();
                }
            }
        } catch (IllegalArgumentException ignored) {
        }

        return shortenBlockId(blockId);
    }

    private String shortenBlockId(String blockId) {
        String shortName = blockId;
        int firstColon = shortName.indexOf(':');
        int lastColon = shortName.lastIndexOf(':');
        if (firstColon != -1 && lastColon > firstColon && isNumeric(shortName.substring(lastColon + 1))) {
            shortName = shortName.substring(0, lastColon);
        }
        if (shortName.contains(":")) {
            shortName = shortName.substring(shortName.indexOf(':') + 1);
        }
        return shortName.replace('_', ' ');
    }

    private boolean containsItem(List<ItemStack> stacks, ItemStack target) {
        for (ItemStack stack : stacks) {
            if (ItemStack.areItemsEqual(stack, target)) {
                return true;
            }
        }
        return false;
    }

    private boolean isNumeric(String value) {
        if (value.isEmpty()) {
            return false;
        }
        for (int i = 0; i < value.length(); i++) {
            if (!Character.isDigit(value.charAt(i))) {
                return false;
            }
        }
        return true;
    }
}
