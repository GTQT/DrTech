package com.meowmel.cropQT.item;

import com.drppp.drtech.DrTechMain;
import com.drppp.drtech.Tags;
import com.meowmel.cropQT.api.CropRegistry;
import com.meowmel.cropQT.api.CropStats;
import com.meowmel.cropQT.api.CropType;
import com.meowmel.cropQT.api.ISoilList;
import com.meowmel.cropQT.api.SubSoilRequirement;
import gregtech.api.GTValues;
import gregtech.api.util.TextFormattingUtil;
import gregtech.api.unification.material.info.MaterialIconType;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;


/**
 * 种子物品
 *
 * <p><b>不走 GT 的材料系统</b>：一株作物一个材料没错，但种子要携带三围与「是否已分析」
 * 这些每株自己的状态，做成材质物品反而要塞 NBT，不如就用这一个物品 + NBT。
 * 它的图标由作物属性里的 {@code seedIcon} 指定材质、颜色由材料 RGB 给出，
 * 所以看上去仍然和它对应的材料是一套。
 *
 * NBT结构:
 *   cropId: String         - 作物类型ID
 *   statGrowth: int        - Growth属性
 *   statGain: int          - Gain属性
 *   statResistance: int    - Resistance属性
 *
 * 获取方式:
 * 1. 创造模式物品栏 (每种作物一个默认种子袋)
 * 2. 破坏已种植的作物架 (保留当前属性)
 * 3. 原版种子直接右键作物架自动转换 (不需要种子袋)
 */
public class ItemCropSeed extends Item {

    // 注意: 这个引用在ModItems.init()中赋值
    // createSeedBag需要通过这个引用创建ItemStack
    public static Item INSTANCE;

    public ItemCropSeed() {
        setTranslationKey(Tags.MODID + ".crop_seed");
        setRegistryName(Tags.MODID, "crop_seed");
        setMaxStackSize(64);
        setCreativeTab(DrTechMain.DrTechTab);
        setHasSubtypes(true);
        INSTANCE = this;
    }

    // ==================== 静态工具方法 ====================

    /**
     * 创建一个携带指定作物和属性的种子袋
     */
    public static ItemStack createSeedBag(String cropId, CropStats stats) {
        if (INSTANCE == null) return ItemStack.EMPTY;
        ItemStack stack = new ItemStack(INSTANCE);
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setString("cropId", cropId);
        stats.writeToNBT(nbt);
        stack.setTagCompound(nbt);
        return stack;
    }

    /**
     * 创建默认属性(1/1/1)的种子袋
     */
    public static ItemStack createSeedBag(String cropId) {
        return createSeedBag(cropId, new CropStats(1, 1, 1));
    }

    /**
     * 从种子袋读取作物ID
     */
    public static String getCropId(ItemStack stack) {
        if (stack.hasTagCompound()) {
            return stack.getTagCompound().getString("cropId");
        }
        return "";
    }

    /**
     * 把属性写回种子袋。
     *
     * <p>{@link CropStats#writeToNBT} 写的和 {@link #getCropStats} 读的是同一组键，
     * 所以直接覆写就行，{@code cropId} 不受影响。
     */
    public static void setCropStats(ItemStack stack, CropStats stats) {
        if (stack.isEmpty() || !stack.hasTagCompound()) {
            return;
        }
        stats.writeToNBT(stack.getTagCompound());
    }

    /**
     * 从种子袋读取作物属性
     */
    public static CropStats getCropStats(ItemStack stack) {
        if (stack.hasTagCompound()) {
            return CropStats.readFromNBT(stack.getTagCompound());
        }
        return new CropStats();
    }

    // ==================== 显示 ====================

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        String cropId = getCropId(stack);
        if (cropId.isEmpty()) {
            // 有 NBT 却没有作物 id：源端叫 unknown；完全没有 NBT 才是 empty。
            return gregtech.api.util.LocalizationUtils.format(
                    stack.hasTagCompound() ? "cropqt.seed.unknown" : "cropqt.seed.empty");
        }
        CropType type = CropRegistry.get(cropId);
        if (type == null) {
            return gregtech.api.util.LocalizationUtils.format("cropqt.seed.invalid");
        }
        return gregtech.api.util.LocalizationUtils.format(
                "cropqt.seed.format", type.getDisplayName());
    }

    /**
     * 种子 tooltip —— 按 CropsNH {@code ItemGenericSeed#addInformation} 的顺序与文案：
     * 三围 → tier → 土壤 → 不能杂交/不能蔓延 → 生长需求 → 种子床最低档。
     *
     * <p>未分析的种子只显示一句「Scan to view stats」；源端也是这样，分析之前不泄露三围。
     * 数字统一走 {@link TextFormattingUtil#formatNumbers(Object)}，与源端
     * {@code NumberFormatUtil.formatNumber} 的显示效果一致。
     */
    @Override
    public void addInformation(@NotNull ItemStack stack, @Nullable World world,
                               @NotNull List<String> tooltip, @NotNull ITooltipFlag flag) {
        if (!stack.hasTagCompound()) {
            return;
        }
        CropStats stats = getCropStats(stack);
        if (!stats.isAnalyzed()) {
            tooltip.add(gregtech.api.util.LocalizationUtils.format(
                    "cropqt_tooltip.genericSeed.scanToViewStats"));
            return;
        }

        tooltip.add(gregtech.api.util.LocalizationUtils.format(
                "cropqt_tooltip.genericSeed.growth", TextFormattingUtil.formatNumbers(stats.getGrowth())));
        tooltip.add(gregtech.api.util.LocalizationUtils.format(
                "cropqt_tooltip.genericSeed.gain", TextFormattingUtil.formatNumbers(stats.getGain())));
        tooltip.add(gregtech.api.util.LocalizationUtils.format(
                "cropqt_tooltip.genericSeed.resistance",
                TextFormattingUtil.formatNumbers(stats.getResistance())));

        String cropId = getCropId(stack);
        CropType type = cropId.isEmpty() ? null : CropRegistry.get(cropId);
        if (type == null) {
            return;
        }

        tooltip.add(gregtech.api.util.LocalizationUtils.format(
                "cropqt_tooltip.genericSeed.tier", TextFormattingUtil.formatNumbers(type.getTier())));

        // 土壤组 tooltip（源端 getSoilTypes().getUnlocalizedItemTooltip() 的等价物）
        tooltip.add(soilTooltip(type.getSoilTypes()));

        if (type.getBreedingThreshold() < 0.0f) {
            tooltip.add(gregtech.api.util.LocalizationUtils.format(
                    "cropqt_tooltip.genericSeed.cannotBreedOnSticks"));
        }
        if (type.getCrossingThreshold() < 0.0f) {
            tooltip.add(gregtech.api.util.LocalizationUtils.format(
                    "cropqt_tooltip.genericSeed.cannotSpreadToOtherSticks.0"));
            tooltip.add(gregtech.api.util.LocalizationUtils.format(
                    "cropqt_tooltip.genericSeed.cannotSpreadToOtherSticks.1"));
        }

        // cropQT 没有源端那套 IGrowthRequirement 对象，生长门槛就是光照与湿度两项，
        // 这里按同样的位置逐条列出。
        tooltip.add(gregtech.api.util.LocalizationUtils.format(
                "cropqt_tooltip.genericSeed.light", describeRange(
                        type.getLightCompare(), type.getLightRequirement(), type.getLightRequirementMax())));
        tooltip.add(gregtech.api.util.LocalizationUtils.format(
                "cropqt_tooltip.genericSeed.humidity", describeRange(
                        type.getHumidityCompare(), type.getWaterRequirement(), type.getWaterRequirementMax())));

        if (type.getMinSeedBedTier() >= 0) {
            int tier = type.getMinSeedBedTier();
            String tierName = TextFormatting.AQUA + GTValues.VN[tier] + TextFormatting.RESET;
            tooltip.add(gregtech.api.util.LocalizationUtils.format(
                    "cropqt_tooltip.genericSeed.minSeedBedTier", tierName));
        }
    }

    /** 土壤组 tooltip；{@code null} 表示不限土壤。 */
    private static String soilTooltip(@Nullable ISoilList soil) {
        String key = soil == null ? "cropqt.soil.global.tooltip" : "cropqt.soil." + soil.getName() + ".tooltip";
        String text = gregtech.api.util.LocalizationUtils.format(key);
        return text.startsWith("cropqt.soil.") ? describeSoil(soil) : text;
    }

    /** 光照/湿度门槛的区间文字：{@code 9+} / {@code ≤9} / {@code 3..9}。 */
    private static String describeRange(com.meowmel.cropQT.api.CompareMode mode, float min, float max) {
        switch (mode) {
            case LESS:
                return TextFormattingUtil.formatNumbers(min) + "-";
            case RANGE:
                return TextFormattingUtil.formatNumbers(min) + ".." + TextFormattingUtil.formatNumbers(max);
            case GREATER:
            default:
                return TextFormattingUtil.formatNumbers(min) + "+";
        }
    }

    /**
     * 创造模式物品栏 - 显示所有作物的默认种子袋
     */
    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (!isInCreativeTab(tab)) return;

        // 确保作物已注册
        if (CropRegistry.getAll().isEmpty()) return;

        for (CropType type : CropRegistry.getAll().values()) {
            if (!type.getId().equals("weed")) {
                items.add(createSeedBag(type.getId()));
            }
        }
    }

    // ==================== 种植条件的文字 ====================

    /** 土壤组的名字。 */
    private static String describeSoil(@Nullable ISoilList soil) {
        if (soil == null) {
            return "不限";
        }
        String key = "cropqt.soil." + soil.getName();
        String text = net.minecraft.client.resources.I18n.format(key);
        return text.startsWith("cropqt.soil.") ? soil.getName() : text;
    }

    /**
     * 底土要求的名字。
     *
     * <p>显示的是要求的<b>代表物品</b>（定义时挑的那个招牌），实际能用的形态往往更多 ——
     * 完整的清单看 JEI 的底土页。
     */
    private static String describeSubSoil(@Nullable SubSoilRequirement requirement) {
        return requirement == null ? "不限" : requirement.getDisplayName();
    }

    // ==================== 种子袋染色 ====================

    /**
     * 根据种子袋内的作物类型，对双层白模贴图上色。
     *
     * <p>源端 {@code ItemGenericSeed} 是双 pass：第 0 层染 {@code color1}（作物主色），
     * 第 1 层染 {@code color2}（几乎都是白色，用来留高光）。这里用 tintIndex 还原：
     * 只有第 0 层跟着 {@code seedColor} 走，其余层保持原色不染。
     */
    public static class SeedColorHandler implements net.minecraft.client.renderer.color.IItemColor {
        @Override
        public int colorMultiplier(ItemStack stack, int tintIndex) {
            if (tintIndex != 0) {
                return 0xFFFFFF; // 高光层不着色
            }
            String cropId = getCropId(stack);
            if (!cropId.isEmpty()) {
                CropType type = CropRegistry.get(cropId);
                if (type != null) {
                    return type.getSeedColor();
                }
            }
            return 0xFFFFFF; // 不着色
        }
    }

    // ==================== 种子袋贴图路由 ====================

    /**
     * 按 NBT 里的作物 id 动态挑模型。
     *
     * <p>作物设了 {@code seedIcon} 就用材质模型
     * {@code gregtech:material_sets/<图标集>/<类型名>}，形状由属性指定、
     * 颜色由 {@link SeedColorHandler} 染成材料色；没设就用默认的 {@code drtech:crop_seed}。
     *
     * <p><b>seedIcon 指向的模型必须已经注册成变体</b>，见
     * {@code ItemsInit.registerItemModels()} —— 那 9 个种子材质没有对应的矿物前缀，
     * GT 不会替我们注册。
     */
    public static class SeedMeshDefinition implements ItemMeshDefinition {
        public static final ModelResourceLocation DEFAULT_MODEL =
                new ModelResourceLocation(Tags.MODID + ":crop_seed", "inventory");

        @Override
        public ModelResourceLocation getModelLocation(ItemStack stack) {
            String cropId = getCropId(stack);
            if (!cropId.isEmpty()) {
                CropType type = CropRegistry.get(cropId);
                if (type != null) {
                    MaterialIconType seedIcon = type.getSeedIcon();
                    if (seedIcon != null) {
                        return new ModelResourceLocation(
                                GTValues.MODID + ":material_sets/dull/" + seedIcon.name, "inventory");
                    }
                }
            }
            return DEFAULT_MODEL;
        }
    }
}
