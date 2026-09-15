package com.meowmel.cropQT.api.unification.material;

import com.meowmel.cropQT.api.ISoilList;
import com.meowmel.cropQT.api.SoilTypes;
import com.meowmel.cropQT.api.SubSoilRequirement;
import com.meowmel.cropQT.api.SubSoilRequirements;
import com.meowmel.cropQT.api.unification.CropMaterialType;
import com.meowmel.cropQT.api.unification.material.properties.CropProperty;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 「哪些材料是作物」的声明表。
 *
 * <p>这是整条生成链的<b>唯一一份清单</b>。每一条只写：种子形状、产物外型、tier、土壤、生长阶段。
 * 剩下的全由系统按材料推 —— 产物物品（矿物前缀）、图标颜色（材料 RGB）、
 * flag、底土（按材料名找同名要求）、矿物词典。
 *
 * <p>参数取自拆分之前的手写作物表，平衡没有改动，只是把「一株作物十几行定义」
 * 压成了「一行声明」。
 *
 * <p>别的模组要加作物，在自己的 {@code MaterialEvent} 或 {@code PostMaterialEvent} 里
 * {@code material.setProperty(CropProperty.KEY, ...)} 即可，不用碰这个文件。
 */
public final class CropMaterials {

    /** 金属叶统一用石头土壤、tier 5~8。 */
    private static final ISoilList ORE_SOIL = SoilTypes.stone;
    /** 非金属的统一用沙子土壤。 */
    private static final ISoilList SAND_SOIL = SoilTypes.sand;
    private CropMaterials() {}

    /**
     * 把声明写进材料。
     *
     * <p><b>必须在 {@code PostMaterialEvent} 里、扫描器之前调</b>：
     * 这时候别的模组的材料也都注册完了，才够得着它们。
     */
    public static void declare() {
        // ==================== 金属叶（tier 5）====================
        declare(Materials.Copper, CropMaterialType.FIBER, 5, ORE_SOIL, 5, 30, null, "coppon");
        declare(Materials.Tin, CropMaterialType.TWIG, 5, ORE_SOIL, 5, 30, null, "tine");
        declare(Materials.Iron, CropMaterialType.LEAF, 5, ORE_SOIL, 5, 30, null, "ferrofern");
        declare(Materials.Lead, CropMaterialType.LEAF, 5, ORE_SOIL, 5, 30, null, "plumbilia");
        declare(Materials.Silver, CropMaterialType.LEAF, 5, ORE_SOIL, 5, 30, null, "argentia");
        declare(Materials.Gold, CropMaterialType.LEAF, 5, ORE_SOIL, 5, 30, null, "auronia");
        declare(Materials.Aluminium, CropMaterialType.LEAF, 5, ORE_SOIL, 5, 30, null, "bauxia");

        // ==================== 金属叶（tier 6）====================
        declare(Materials.Nickel, CropMaterialType.LEAF, 6, ORE_SOIL, 5, 32, null, "nickelback");
        declare(Materials.Zinc, CropMaterialType.LEAF, 6, ORE_SOIL, 5, 32, null, "galvania");
        declare(Materials.Sulfur, CropMaterialType.FLOWER, 6, ORE_SOIL, 5, 32, null, "thiosulfine");
        declare(Materials.Tungsten, CropMaterialType.LEAF, 6, ORE_SOIL, 5, 34, null, "scheelinium");
        declare(Materials.Titanium, CropMaterialType.LEAF, 6, ORE_SOIL, 5, 34, null, "titania");

        // ==================== 金属叶（tier 7~8）====================
        declare(Materials.Platinum, CropMaterialType.LEAF, 7, ORE_SOIL, 5, 36, null, "platina");
        declare(Materials.Osmium, CropMaterialType.FLOWER, 7, ORE_SOIL, 6, 36, null, "osmianth");
        declare(Materials.Iridium, CropMaterialType.FLOWER, 7, ORE_SOIL, 6, 36, null, "iridine");
        declare(Materials.Manganese, CropMaterialType.LEAF, 8, ORE_SOIL, 6, 40, null, "pyrolusium");
        declare(Materials.Uranium238, CropMaterialType.LEAF, 8, ORE_SOIL, 6, 40, null, "reactoria");

        // ==================== 非金属 ====================
        declare(Materials.Mica, CropMaterialType.FLOWER, 5, SAND_SOIL, 5, 28, null, "micadia");
        declare(Materials.Salt, CropMaterialType.ROOT, 5, SAND_SOIL, 5, 28, null, "salty_root");
        // 石油浆果长在油田上，不是矿物，底土得显式指定
        declare(Materials.Oil, CropMaterialType.BERRY, 5, SAND_SOIL, 5, 30, SubSoilRequirements.oilSands, "oil_berry");
    }

    /**
     * 声明一株作物。
     *
     * @param subSoil 显式底土；{@code null} 表示按材料名自动找同名的要求
     * @param render  生长渲染图的目录名（{@code textures/blocks/crop/<这个>/}）。
     *                这些目录是按源端的作物名建的（{@code ferrofern} / {@code coppon} …），
     *                和材料驱动的 id（{@code iron_leaf}）对不上，所以必须显式给
     */
    private static void declare(@NotNull Material material, @NotNull CropMaterialType type,
                                int tier, @NotNull ISoilList soil,
                                int stages, int requirement, @Nullable SubSoilRequirement subSoil,
                                @NotNull String render) {
        CropProperty.Builder builder = CropProperty.builder()
                .type(type)
                .tier(tier)
                .soil(soil)
                .stages(stages, stages, requirement)
                .render(render);
        if (subSoil != null) {
            builder.subSoil(subSoil);
        }
        material.setProperty(CropProperty.KEY, builder.build());
    }
}
