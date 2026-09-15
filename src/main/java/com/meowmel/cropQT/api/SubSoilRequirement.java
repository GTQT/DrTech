package com.meowmel.cropQT.api;

import gregtech.api.unification.material.Material;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * 底土要求：作物架下方<b>第二格</b>必须是对应的方块。
 *
 * <pre>
 *  y+1   [空气]
 *  y     [作物架]
 *  y-1   [土壤]     ← 由 {@link ISoilList} 管
 *  y-2   [底土]     ← 本类管这一格
 * </pre>
 *
 * <p>它让「矿石叶必须长在矿脉上」「石中百合必须长在对应石头上」变成一条机制，
 * 而不是一句设定。
 *
 * <p>匹配逻辑委托给 {@link BlockMatcher}——方块、矿物词典、GT 材料三路，任意一路命中即可，
 * 所以同一份要求既能认出 GT 的压缩块，也能认出别的 mod 的矿石。
 *
 * <p><b>注意</b>：条件不满足不会阻止生长，只会把生长速度压到极低。判断结果用于提示玩家。
 */
public class SubSoilRequirement implements GrowthRequirement {

    /** 底土相对作物架的垂直偏移。 */
    public static final int SUB_SOIL_DEPTH = 2;

    /**
     * 全部实例，按定义顺序。
     *
     * <p>底土要求只有 {@link SubSoilRequirements} 一个定义处，全部经由本类构造，
     * 所以在这里登记就能拿到完整清单——JEI 的底土页靠它枚举，不用再维护第二份列表。
     */
    private static final List<SubSoilRequirement> ALL = new ArrayList<>();

    private final String name;
    private final BlockMatcher matcher = new BlockMatcher();

    public SubSoilRequirement(String name) {
        this.name = name;
        ALL.add(this);
    }

    /** 全部已登记的底土要求，按定义顺序。 */
    public static List<SubSoilRequirement> getAll() {
        return Collections.unmodifiableList(ALL);
    }

    public static SubSoilRequirement of(String name) {
        return new SubSoilRequirement(name);
    }

    /**
     * 建一个要求，并指定它的代表物品。
     *
     * @param representative 给玩家看的招牌物品——分析仪和 tooltip 会显示它的名字。
     *                       定义者自己决定用哪个形态：同一种材料可能是矿石，
     *                       也可能是压缩块，哪个更贴切只有你知道。
     */
    public static SubSoilRequirement of(String name, ItemStack representative) {
        return new SubSoilRequirement(name).representative(representative);
    }

    // ==================== 登记（链式） ====================

    /** 登记一个方块，忽略元数据。 */
    public SubSoilRequirement addBlock(Block block) {
        matcher.addBlock(block);
        return this;
    }

    /** 登记一个具体的「方块 + 元数据」组合。 */
    public SubSoilRequirement addBlock(Block block, int meta) {
        matcher.addBlock(block, meta);
        return this;
    }

    /** {@link #addBlock(Block, int)} 的便捷重载。 */
    public SubSoilRequirement addBlock(IBlockState state) {
        matcher.addBlock(state);
        return this;
    }

    /** 登记一个矿物词典条目，例如 {@code "oreCopper"}。 */
    public SubSoilRequirement addOreDict(String oreDictName) {
        matcher.addOreDict(oreDictName);
        return this;
    }

    /** 登记一种 GT 材料，命中其任意形态（矿石、压缩块…）。 */
    public SubSoilRequirement addMaterial(Material material) {
        matcher.addMaterial(material);
        return this;
    }

    /**
     * 按注册名登记方块，形如 {@code "modid:name"} 或 {@code "modid:name:meta"}。
     *
     * <p>用于引用编译期拿不到类的方块——别的 mod 的方块，或 GT 按材料生成的方块。
     */
    public SubSoilRequirement addBlockId(String registryNameWithOptionalMeta) {
        matcher.addBlockId(registryNameWithOptionalMeta);
        return this;
    }

    /**
     * 显式指定代表物品，用于 tooltip / 分析仪显示。
     *
     * <p>不调也可以——登记的第一个方块会自动成为代表。只有当你想拿别的物品当招牌
     * （例如材料的矿石而不是压缩块）时才需要它。
     */
    public SubSoilRequirement representative(ItemStack stack) {
        matcher.setRepresentative(stack);
        return this;
    }

    // ==================== GrowthRequirement ====================

    @Override
    public boolean isMet(World world, BlockPos cropPos) {
        return matches(world.getBlockState(cropPos.down(SUB_SOIL_DEPTH)));
    }

    /** 直接判断某个方块状态算不算满足本要求。 */
    public boolean matches(IBlockState state) {
        return matcher.matches(state);
    }

    @Override
    public String getDisplayName() {
        ItemStack representative = matcher.getRepresentative();
        return representative.isEmpty() ? name : representative.getDisplayName();
    }

    // ==================== 查询 ====================

    /** 内部名，用于 lang key 与调试。 */
    public String getName() {
        return name;
    }

    /** 是否一个成员都没登记。空要求永远不会满足。 */
    public boolean isEmpty() {
        return matcher.isEmpty();
    }

    /** 代表物品；没显式指定时是登记的第一个方块，都没有则返回空栈。 */
    public ItemStack getRepresentative() {
        return matcher.getRepresentative();
    }

    /** 能还原成物品的成员，供 JEI 展示。 */
    public List<ItemStack> getDisplayItems() {
        return matcher.getDisplayItems();
    }

    /**
     * 该要求能给出名字的那部分方块注册名。
     *
     * <p>按 GT 材料 / 矿物词典登记的部分给不出确定名字，不在结果里，详见
     * {@link BlockMatcher#getKnownBlockIds()}。
     */
    public Set<String> getBlockIds() {
        return matcher.getKnownBlockIds();
    }

    @Override
    public String toString() {
        return "SubSoilRequirement[" + name + ", " + matcher + "]";
    }
}
