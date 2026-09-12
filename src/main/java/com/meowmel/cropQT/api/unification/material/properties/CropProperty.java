package com.meowmel.cropQT.api.unification.material.properties;

import com.meowmel.cropQT.api.ISoilList;
import com.meowmel.cropQT.api.SoilRegistry;
import com.meowmel.cropQT.api.SubSoilRequirement;
import com.meowmel.cropQT.api.unification.CropMaterialType;
import gregtech.api.unification.material.info.MaterialIconType;
import gregtech.api.unification.material.properties.IMaterialProperty;
import gregtech.api.unification.material.properties.MaterialProperties;
import gregtech.api.unification.material.properties.PropertyKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 作物属性：材料声明「我是一株作物」的唯一入口。
 *
 * <p>材料作者只写这一个属性，剩下的全自动：
 * <ul>
 *     <li>系统按 {@link #getMaterialType()} 自动补上对应的材质 flag</li>
 *     <li>GT 按 flag 生成产物物品，图标与颜色走材质系统</li>
 *     <li>扫描器按本属性建出 {@code CropType} 并注册</li>
 * </ul>
 *
 * <p>用法：
 * <pre>{@code
 * material.setProperty(CropProperty.KEY, CropProperty.builder()
 *         .seedIcon(CropQTMaterialIconType.oreberry)   // 可选，不写就用外型的默认形状
 *         .type(CropMaterialType.LEAF)
 *         .tier(5)
 *         .soil(SoilTypes.stone)
 *         .stages(5, 5, 30)
 *         .render("ferrofern")
 *         .build());
 * }</pre>
 *
 * <p>写进去的时机：材料构造完就可以写，但<b>最晚不能晚于 {@code PostMaterialEvent}</b> ——
 * 之后再动材料，GT 的注册表已经冻结，{@code addFlags} 会直接抛异常。
 */
public class CropProperty implements IMaterialProperty {

    /** 本属性的键。取它用 {@code material.getProperty(CropProperty.KEY)}。 */
    public static final PropertyKey<CropProperty> KEY = new PropertyKey<>("crop", CropProperty.class);

    private final MaterialIconType seedIcon;
    private final CropMaterialType materialType;
    private final int tier;
    private final String soilName;
    private final int maxGrowthStage;
    private final int harvestStage;
    private final int stageRequirement;
    private final String renderTexture;
    private final SubSoilRequirement subSoil;

    private CropProperty(Builder b) {
        this.seedIcon = b.seedIcon;
        this.materialType = b.materialType;
        this.tier = b.tier;
        this.soilName = b.soilName;
        this.maxGrowthStage = b.maxGrowthStage;
        this.harvestStage = b.harvestStage;
        this.stageRequirement = b.stageRequirement;
        this.renderTexture = b.renderTexture;
        this.subSoil = b.subSoil;
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * 作物要有粉尘形态。
     *
     * <p>产物物品是走矿物前缀生成的，而作物本身也该能被磨成粉 ——
     * 顺手把这条依赖补上，材料作者就不用自己记得加 {@code .dust()}。
     */
    @Override
    public void verifyProperty(MaterialProperties properties) {
        properties.ensureSet(PropertyKey.DUST, true);
    }

    // ==================== 读 ====================

    /**
     * 种子形状（给 NBT 种子物品选模型）。
     *
     * <p>{@code null} 表示没指定，用所属外型的默认形状。
     */
    @Nullable
    public MaterialIconType getSeedIcon() {
        return seedIcon;
    }

    /** 产物外型，同时决定自动补哪个 flag、作物 id 的后缀是什么。 */
    @NotNull
    public CropMaterialType getMaterialType() {
        return materialType;
    }

    public int getTier() {
        return tier;
    }

    /** 土壤组名；查不到对应组时返回 {@code null}。 */
    @Nullable
    public String getSoilName() {
        return soilName;
    }

    /** 土壤组对象；名字对不上任何已登记土壤时返回 {@code null}。 */
    @Nullable
    public ISoilList getSoil() {
        return soilName == null ? null : SoilRegistry.get(soilName);
    }

    public int getMaxGrowthStage() {
        return maxGrowthStage;
    }

    public int getHarvestStage() {
        return harvestStage;
    }

    public int getStageRequirement() {
        return stageRequirement;
    }

    /** 生长渲染图目录名（{@code textures/blocks/crop/<这个>/}）；空表示用默认。 */
    @Nullable
    public String getRenderTexture() {
        return renderTexture;
    }

    /**
     * 显式指定的底土要求。
     *
     * <p>不指定的话，扫描器会拿<b>材料名</b>去 {@code SubSoilRequirements} 里找同名的
     * （{@code iron} 材料 → {@code SubSoilRequirements.iron}），能覆盖大多数金属。
     * 对不上、或者想要别的底土时，用这个字段盖掉。
     */
    @Nullable
    public SubSoilRequirement getSubSoil() {
        return subSoil;
    }

    @Override
    public String toString() {
        return "CropProperty[" + materialType.getName() + ", tier=" + tier
                + ", soil=" + soilName + ", stages=" + maxGrowthStage + "/" + harvestStage
                + "/" + stageRequirement + ", render=" + renderTexture + "]";
    }

    // ==================== 建 ====================

    public static class Builder {

        private MaterialIconType seedIcon = null;
        private CropMaterialType materialType = CropMaterialType.LEAF;
        private int tier = 1;
        private String soilName = "dirt_grass";
        private int maxGrowthStage = 4;
        private int harvestStage = 4;
        private int stageRequirement = 16;
        private String renderTexture = null;
        private SubSoilRequirement subSoil = null;

        /** 种子形状。不指定就用所属外型的默认形状（见 CropMaterialType#getDefaultSeedIcon）。 */
        public Builder seedIcon(@NotNull MaterialIconType icon) {
            this.seedIcon = icon;
            return this;
        }

        /** 产物外型。必填项，不指定会落到 LEAF。 */
        public Builder type(@NotNull CropMaterialType type) {
            this.materialType = type;
            return this;
        }

        public Builder tier(int tier) {
            this.tier = tier;
            return this;
        }

        /** 土壤组，直接给对象。 */
        public Builder soil(@NotNull ISoilList soil) {
            this.soilName = soil.getName();
            return this;
        }

        /** 土壤组，按名字给（组名即 {@code SoilRegistry} 的键）。 */
        public Builder soil(@NotNull String soilName) {
            this.soilName = soilName;
            return this;
        }

        /**
         * 生长阶段参数。
         *
         * @param maxGrowthStage   一共几个生长阶段（对应贴图的 stage 数）
         * @param harvestStage     到第几阶段可以收
         * @param stageRequirement 每推进一个阶段要攒多少进度
         */
        public Builder stages(int maxGrowthStage, int harvestStage, int stageRequirement) {
            this.maxGrowthStage = maxGrowthStage;
            this.harvestStage = harvestStage;
            this.stageRequirement = stageRequirement;
            return this;
        }

        /** 生长渲染图的目录名。不指定就按作物 id 找，找不到用默认形状。 */
        public Builder render(@Nullable String renderTexture) {
            this.renderTexture = renderTexture;
            return this;
        }

        /** 显式指定底土要求；不写就按材料名自动找同名的。 */
        public Builder subSoil(@NotNull SubSoilRequirement requirement) {
            this.subSoil = requirement;
            return this;
        }

        public CropProperty build() {
            return new CropProperty(this);
        }
    }
}
