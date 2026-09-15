package com.meowmel.cropQT.api.unification;

import com.meowmel.cropQT.api.unification.material.info.CropQTMaterialIconType;
import gregtech.api.unification.material.info.MaterialFlag;
import gregtech.api.unification.material.info.MaterialIconType;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.common.items.MetaItems;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static gregtech.api.GTValues.M;
import static gregtech.api.unification.ore.OrePrefix.Flags.ENABLE_UNIFICATION;

/**
 * 作物的 10 种外型。
 *
 * <p>每种外型把三样东西锁在一起，它们是同一件事的三个面：
 * <ul>
 *     <li>{@link #getIcon() 材质类型} —— 产物图标用哪套资源</li>
 *     <li>{@link #getFlag() flag} —— 材料靠它声明「我是这种作物」</li>
 *     <li>{@link #getPrefix() 矿物前缀} —— GT 靠它给材料生成产物物品</li>
 * </ul>
 *
 * <p><b>为什么做成枚举而不是三份并列的静态字段</b>：三者必须严格一一对应
 * （前缀名 = 图标名 = 贴图名前缀），分成三个文件写迟早会漂 ——
 * 这个仓库里已经有过一次 {@code grain} / {@code grains} 对不上的事故。
 * 写成枚举就只有一处定义，编译器帮着看住。
 *
 * <p>flag 由系统在 {@code PostMaterialEvent} 里按材料声明的
 * {@link com.meowmel.cropQT.api.unification.material.properties.CropProperty 作物属性}
 * 自动补上，材料作者不需要手写 flag。
 */
public enum CropMaterialType {

    /** 叶片类 —— 金属叶、蕨叶这些。 */
    LEAF(CropQTMaterialIconType.leaf, CropQTMaterialIconType.vanilla),
    /** 花类。 */
    FLOWER(CropQTMaterialIconType.flower, CropQTMaterialIconType.flower),
    /** 浆果类。 */
    BERRY(CropQTMaterialIconType.berry, CropQTMaterialIconType.oreberry),
    /** 茎杆类。 */
    STEM(CropQTMaterialIconType.stem, CropQTMaterialIconType.grain),
    /** 疣块类。 */
    WART(CropQTMaterialIconType.wart, CropQTMaterialIconType.spore),
    /** 纤维类。 */
    FIBER(CropQTMaterialIconType.fiber, CropQTMaterialIconType.grain),
    /** 枝条类。 */
    TWIG(CropQTMaterialIconType.twig, CropQTMaterialIconType.bonsai),
    /** 根茎类。 */
    ROOT(CropQTMaterialIconType.root, CropQTMaterialIconType.vanilla),
    /** 花苞类。 */
    BLOSSOM(CropQTMaterialIconType.blossom, CropQTMaterialIconType.botania),
    /** 精华类。 */
    MAGIC_ESSENCE(CropQTMaterialIconType.magic_essence, CropQTMaterialIconType.magic),
    ;

    private final MaterialIconType icon;
    /** 默认的种子形状；材料可以在作物属性里覆盖。 */
    private final MaterialIconType defaultSeedIcon;
    private final MaterialFlag flag;
    private final OrePrefix prefix;

    CropMaterialType(@NotNull MaterialIconType icon, @NotNull MaterialIconType defaultSeedIcon) {
        this.icon = icon;
        this.defaultSeedIcon = defaultSeedIcon;
        MaterialFlag flag = new MaterialFlag.Builder("generate_" + icon.name)
                .requireProps(PropertyKey.DUST)
                .build();
        this.flag = flag;
        // 谓词用局部变量而不是 this.flag —— 枚举构造函数里读实例字段有初始化顺序的坑
        this.prefix = new OrePrefix(icon.name, M, null, icon, ENABLE_UNIFICATION,
                mat -> mat.hasFlag(flag));
    }

    /**
     * 这种外型默认配哪种种子形状。
     *
     * <p>八种种子形状正好分完：叶/根用最朴素的 {@code vanilla}，浆果用 {@code oreberry}，
     * 花用 {@code flower}，秆与纤维用 {@code grain}，疣与菌用 {@code spore}，
     * 枝用 {@code bonsai}，花苞用 {@code botania}，精华用 {@code magic}。
     * 材料想要别的形状，在 {@code CropProperty} 里自己指定。
     */
    @NotNull
    public MaterialIconType getDefaultSeedIcon() {
        return defaultSeedIcon;
    }

    @NotNull
    public MaterialIconType getIcon() {
        return icon;
    }

    @NotNull
    public MaterialFlag getFlag() {
        return flag;
    }

    @NotNull
    public OrePrefix getPrefix() {
        return prefix;
    }

    /** 外型名，同时是材质类型名、前缀名，以及作物 id 的后缀（{@code iron_<这个>}）。 */
    @NotNull
    public String getName() {
        return icon.name;
    }

    /** 作物 id 的后缀写法，例如 {@code iron_leaf} 里的 {@code leaf}。 */
    @NotNull
    public String getCropIdSuffix() {
        return icon.name;
    }

    /**
     * 按名字找外型；找不到返回 {@code null}。
     *
     * <p>读存档 / 读 lang 时用。
     */
    @Nullable
    public static CropMaterialType byName(@Nullable String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }
        for (CropMaterialType type : values()) {
            if (type.getName().equals(name)) {
                return type;
            }
        }
        return null;
    }

    /**
     * 把这 10 个矿物前缀登记给 GT。
     *
     * <p><b>必须在 {@code MaterialEvent} 里调</b>，因为 GT 的 {@code MetaItems.init()}
     * 会遍历前缀表为每个前缀建 {@code MetaPrefixItem}，晚了就漏。
     */
    public static void registerOrePrefixes() {
        for (CropMaterialType type : values()) {
            MetaItems.addOrePrefix(type.getPrefix());
        }
    }
}
