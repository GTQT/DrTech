package com.meowmel.cropQT.api.unification.material.info;

import gregtech.api.unification.material.info.MaterialIconType;

/**
 * 作物的材质类型。
 *
 * <p>两类，各管一头：
 * <ul>
 *     <li><b>种子材质</b>（8 种）—— 决定 NBT 种子物品长什么样。
 *         种子不走 GT 的材料系统，这里只是给它挑一套模型。</li>
 *     <li><b>作物材质</b>（10 种）—— 决定产物物品长什么样。
 *         产物是 GT 的材质物品，图标由 {@code MaterialIconType} × 图标集决定，
 *         再被材料的颜色自动染上色。</li>
 * </ul>
 *
 * <h2>为什么共用一个 {@code flower}</h2>
 * 种子里有 flower、作物里也有 flower。{@link MaterialIconType} 的构造函数带唯一性校验
 * （重名直接抛 {@code IllegalArgumentException}），而这些都是静态字段、类初始化就会跑，
 * 所以撞名 = 游戏起不来。两者共用同一个类型对象最省事：花的种子和花的产物本来就该长得像。
 *
 * <h2>命名规矩</h2>
 * 类型名 = {@code material_sets/<图标集>/<类型名>.json} 的文件名。
 * 每个 {@code <类型名>.json} 里写 {@code <类型名>1} / {@code <类型名>2} 两张灰度图，
 * 所以<b>类型名、模型名、贴图名前缀三者必须完全一致</b>。
 */
public final class CropQTMaterialIconType {

    // ==================== 种子材质（8）====================

    public static final MaterialIconType bonsai = new MaterialIconType("bonsai");
    public static final MaterialIconType botania = new MaterialIconType("botania");
    public static final MaterialIconType grain = new MaterialIconType("grain");
    public static final MaterialIconType magic = new MaterialIconType("magic");
    public static final MaterialIconType oreberry = new MaterialIconType("oreberry");
    public static final MaterialIconType spore = new MaterialIconType("spore");
    public static final MaterialIconType vanilla = new MaterialIconType("vanilla");

    // ==================== 作物材质（10）====================

    /** 叶片类 —— 金属叶、蕨叶这些。 */
    public static final MaterialIconType leaf = new MaterialIconType("leaf");
    /** 花类。与种子组的 flower 共用一套资源，所以只声明一次。 */
    public static final MaterialIconType flower = new MaterialIconType("flower");
    /** 浆果类。 */
    public static final MaterialIconType berry = new MaterialIconType("berry");
    /** 茎杆类。 */
    public static final MaterialIconType stem = new MaterialIconType("stem");
    /** 疣块类。 */
    public static final MaterialIconType wart = new MaterialIconType("wart");
    /** 纤维类。 */
    public static final MaterialIconType fiber = new MaterialIconType("fiber");
    /** 枝条类。 */
    public static final MaterialIconType twig = new MaterialIconType("twig");
    /** 根茎类。 */
    public static final MaterialIconType root = new MaterialIconType("root");
    /** 花苞类。 */
    public static final MaterialIconType blossom = new MaterialIconType("blossom");
    /**
     * 精华类。
     *
     * <p>本来想叫 {@code essence}，但 GT 已经声明过同名的 {@code MaterialIconType}
     * （声明了却没有任何地方用、也没有贴图）。名字被占着就换个名，不去蹭它的声明 ——
     * 免得哪天上游给 essence 补了素材，两边打架。
     */
    public static final MaterialIconType magic_essence = new MaterialIconType("magic_essence");

    private CropQTMaterialIconType() {}
}
