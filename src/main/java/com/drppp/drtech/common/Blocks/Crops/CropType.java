package com.drppp.drtech.common.Blocks.Crops;

import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * 定义一种作物类型的所有属性
 */

public class CropType {
    private final String id;
    private final String displayName;
    private final int tier;
    private final int maxGrowthStage;
    private final int harvestStage;
    private final int stageRequirement; // 每阶段所需生长进度, 默认20
    private final List<ItemStack> drops;
    private final String[] requiredBlocks;
    private final float lightRequirement;
    private final float waterRequirement;
    private final CropRenderType renderType;

    private CropType(Builder builder) {
        this.id = builder.id;
        this.displayName = builder.displayName;
        this.tier = builder.tier;
        this.maxGrowthStage = builder.maxGrowthStage;
        this.harvestStage = builder.harvestStage;
        this.stageRequirement = builder.stageRequirement;
        this.drops = builder.drops;
        this.requiredBlocks = builder.requiredBlocks;
        this.lightRequirement = builder.lightRequirement;
        this.waterRequirement = builder.waterRequirement;
        this.renderType = builder.renderType;
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public int getTier() { return tier; }
    public int getMaxGrowthStage() { return maxGrowthStage; }
    public int getHarvestStage() { return harvestStage; }
    public int getStageRequirement() { return stageRequirement; }
    public List<ItemStack> getDrops() { return drops; }
    public String[] getRequiredBlocks() { return requiredBlocks; }
    public float getLightRequirement() { return lightRequirement; }
    public float getWaterRequirement() { return waterRequirement; }
    public CropRenderType getRenderType() { return renderType; }

    public boolean canGrowAt(float light, float humidity, String blockBelow) {
        if (light < lightRequirement) return false;
        if (humidity < waterRequirement) return false;
        if (requiredBlocks != null && requiredBlocks.length > 0) {
            for (String req : requiredBlocks) {
                if (req.equals(blockBelow)) return true;
            }
            return false;
        }
        return true;
    }

    public static class Builder {
        private final String id;
        private String displayName;
        private int tier = 1;
        private int maxGrowthStage = 7;
        private int harvestStage = 7;
        private int stageRequirement = 20;
        private List<ItemStack> drops = new ArrayList<>();
        private String[] requiredBlocks = new String[0];
        private float lightRequirement = 9;
        private float waterRequirement = 0;
        private CropRenderType renderType = CropRenderType.CROSS;

        public Builder(String id) { this.id = id; this.displayName = id; }

        public Builder displayName(String name) { this.displayName = name; return this; }
        public Builder tier(int tier) { this.tier = tier; return this; }
        public Builder maxGrowthStage(int s) { this.maxGrowthStage = s; return this; }
        public Builder harvestStage(int s) { this.harvestStage = s; return this; }
        public Builder stageRequirement(int r) { this.stageRequirement = r; return this; }
        public Builder addDrop(ItemStack s) { this.drops.add(s); return this; }
        public Builder requiredBlocks(String... b) { this.requiredBlocks = b; return this; }
        public Builder lightRequirement(float l) { this.lightRequirement = l; return this; }
        public Builder waterRequirement(float w) { this.waterRequirement = w; return this; }
        public Builder renderType(CropRenderType t) { this.renderType = t; return this; }

        public CropType build() { return new CropType(this); }
    }
}
