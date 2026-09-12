package com.meowmel.cropQT.api.unification.material;

import com.meowmel.cropQT.api.CropRegistry;
import com.meowmel.cropQT.api.CropRenderType;
import com.meowmel.cropQT.api.CropType;
import com.meowmel.cropQT.api.SubSoilRequirement;
import com.meowmel.cropQT.api.unification.CropMaterialType;
import com.meowmel.cropQT.api.unification.material.properties.CropProperty;
import gregtech.api.GregTechAPI;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.info.MaterialIconType;
import gregtech.api.unification.material.registry.MaterialRegistry;
import gregtech.api.util.GTLog;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * 材料 → 作物的生成器。
 *
 * <p>凡是带 {@link CropProperty} 的材料就长出一株作物。分<b>两趟</b>跑，
 * 因为这两件事的时机窗口不一样：
 *
 * <ol>
 *     <li>{@link #applyFlags()} —— <b>必须在 {@code PostMaterialEvent} 里</b>。
 *         GT 在这个事件之后立刻冻结注册表，之后再 {@code addFlags} 会抛异常。</li>
 *     <li>{@link #registerCrops()} —— <b>必须在 {@code OreDictUnifier.init()} 之后</b>
 *         （FML init 阶段）。产物物品是 {@code OreDictUnifier.get(前缀, 材料)} 取出来的，
 *         而那个索引正是 {@code OreDictUnifier.init()} 建的；在那之前取一律是空栈。</li>
 * </ol>
 *
 * <p>中间那段时间 flags 已经加好了，所以 GT 生成产物物品时看得到它们。
 */
public final class CropMaterialScanner {

    private CropMaterialScanner() {}

    /**
     * 第一趟：给声明了作物属性的材料补上对应的材质 flag。
     *
     * <p>必须在 {@code PostMaterialEvent} 里调。
     */
    public static void applyFlags() {
        int count = 0;
        for (MaterialRegistry registry : GregTechAPI.materialManager.getRegistries()) {
            for (Material material : registry.getAllMaterials()) {
                CropProperty property = material.getProperty(CropProperty.KEY);
                if (property == null) {
                    continue;
                }
                material.addFlags(property.getMaterialType().getFlag());
                count++;
            }
        }
        GTLog.logger.info("已为 {} 种材料补上作物材质 flag", count);
    }

    /**
     * 第二趟：建出并注册作物。
     *
     * <p>必须在 {@code OreDictUnifier.init()} 之后调（我们挂在 FML init）。
     */
    public static void registerCrops() {
        List<String> registered = new ArrayList<>();
        List<String> skipped = new ArrayList<>();

        for (MaterialRegistry registry : GregTechAPI.materialManager.getRegistries()) {
            for (Material material : registry.getAllMaterials()) {
                CropProperty property = material.getProperty(CropProperty.KEY);
                if (property == null) {
                    continue;
                }
                try {
                    String id = register(material, property);
                    if (id != null) {
                        registered.add(id);
                    } else {
                        skipped.add(material.getName());
                    }
                } catch (RuntimeException e) {
                    skipped.add(material.getName() + "(" + e.getMessage() + ")");
                    GTLog.logger.error("注册作物失败: {}", material, e);
                }
            }
        }

        GTLog.logger.info("材质驱动的作物注册完成：成功 {} 株，跳过 {} 株", registered.size(), skipped.size());
        if (!skipped.isEmpty()) {
            GTLog.logger.warn("跳过的材料: {}", skipped);
        }
        if (GTLog.logger.isDebugEnabled()) {
            GTLog.logger.debug("已注册的作物: {}", registered);
        }
    }

    /**
     * 把一株作物从材料上长出来。
     *
     * @return 注册成功返回作物 id；材料不满足条件返回 {@code null}
     */
    @Nullable
    private static String register(@NotNull Material material, @NotNull CropProperty property) {
        CropMaterialType type = property.getMaterialType();

        String id = material.getName() + "_" + type.getCropIdSuffix();

        // 产物物品：GT 按 flag 生成，这里只是提前把它的 ItemStack 取出来当掉落
        ItemStack produce = OreDictUnifier.get(type.getPrefix(), material);
        if (produce.isEmpty()) {
            GTLog.logger.warn("材料 {} 声明了作物属性 {}，但产物物品取不出来（前缀 {}），跳过",
                    material.getName(), property, type.getPrefix().name);
            return null;
        }

        String soilName = property.getSoilName();
        if (soilName != null && com.meowmel.cropQT.api.SoilRegistry.get(soilName) == null) {
            GTLog.logger.warn("材料 {} 的作物属性指定了不存在的土壤组「{}」，这株作物将不限土壤",
                    material.getName(), soilName);
            soilName = null;
        }

        CropType.Builder builder = new CropType.Builder(id)
                .displayName("cropqt.crop." + id + ".name")
                .tier(property.getTier());
        // 生长渲染图：不指定的话 TESR 会按作物 id 去找 textures/blocks/crop/<id>/，
        // 而生成的 id（iron_leaf）和贴图目录名（ferrofern）对不上，必须显式给
        if (property.getRenderTexture() != null) {
            builder.texturePath(property.getRenderTexture());
        }
        // 种子形状：材料自己指定了就用它的，没指定就用所属外型的默认形状。
        // 不这么做的话 20 株作物会全部用同一个形状，只有颜色不同。
        MaterialIconType seedIcon = property.getSeedIcon() != null
                ? property.getSeedIcon()
                : type.getDefaultSeedIcon();

        builder
                .maxGrowthStage(property.getMaxGrowthStage())
                .harvestStage(property.getHarvestStage())
                .stageRequirement(property.getStageRequirement())
                .seedIcon(seedIcon)
                // 种子染成材料的颜色 —— 「名字对得上外型」就靠这一句
                .seedColor(material.getMaterialRGB())
                .addDrop(produce);

        if (soilName != null) {
            builder.soil(com.meowmel.cropQT.api.SoilRegistry.get(soilName));
        }

        // 底土：显式指定的优先，没写才按材料名自动找同名的
        SubSoilRequirement subSoil = property.getSubSoil() != null
                ? property.getSubSoil()
                : findSubSoil(material);
        if (subSoil != null) {
            builder.subSoil(subSoil);
        }

        builder.renderType(pickRenderType(property.getTier()));

        CropType crop = builder.build();
        CropRegistry.register(crop);
        return id;
    }

    /**
     * 按材料名找对应的底土要求。
     *
     * <p>{@code SubSoilRequirements} 里的成员名就是材料名（{@code iron} / {@code copper} …），
     * 所以直接按名字对。对不上的材料不挑底土 —— 这是正常情况，不是错误。
     */
    @Nullable
    private static SubSoilRequirement findSubSoil(@NotNull Material material) {
        for (SubSoilRequirement requirement : SubSoilRequirement.getAll()) {
            if (requirement.getName().equals(material.getName())) {
                return requirement;
            }
        }
        return null;
    }

    /**
     * 生长形状。
     *
     * <p>现在只有一个粗糙规则：tier 越高越像「立起来的东西」。
     * 真正的形状应该由属性指定，等有需要了再加字段。
     */
    @NotNull
    private static CropRenderType pickRenderType(int tier) {
        if (tier >= 8) {
            return CropRenderType.HASH;
        }
        return CropRenderType.CROSS;
    }

}
