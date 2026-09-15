package com.meowmel.cropQT.api;

import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.stack.UnificationEntry;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.oredict.OreDictionary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * 「一组方块」的匹配器。
 *
 * <p>土壤组（{@link SoilList}）和底土要求（{@link SubSoilRequirement}）都要回答同一个问题：
 * <b>某个方块算不算我这一组的？</b> 本类把这个判断抽出来共用。
 *
 * <p>三路匹配，任意一路命中即算：
 * <ol>
 *     <li><b>方块（忽略元数据）</b> —— {@link #addBlock(Block)}</li>
 *     <li><b>方块 + 元数据</b> —— {@link #addBlock(Block, int)}</li>
 *     <li><b>矿物词典</b> / <b>GT 材料</b> —— {@link #addOreDict(String)} / {@link #addMaterial(Material)}，
 *         一次覆盖某材料的所有形态（矿石、块…）</li>
 * </ol>
 *
 * <p>第 3 路要把方块转成 {@link ItemStack} 再查表，比前两路慢；调用方如果高频查询应当自行加缓存
 * （{@link SoilRegistry} 就是这么做的）。
 */
public class BlockMatcher {

    /** 忽略元数据的方块。 */
    private final Set<Block> anyMeta = new HashSet<>();
    /** 精确到元数据的方块。 */
    private final Set<BlockMeta> exactMeta = new HashSet<>();
    private final Set<String> oreDictNames = new HashSet<>();
    private final Set<Material> materials = new HashSet<>();
    /** 按注册名登记的方块，形如 {@code "mod:name"} 或 {@code "mod:name:meta"}。 */
    private final Set<String> blockIds = new HashSet<>();

    /**
     * 给玩家看的代表物品。
     *
     * <p>来源是「登记的第一个方块」或定义者显式调 {@link #setRepresentative}——
     * 本类<b>不猜</b>：同一种材料可以是矿石也可以是压缩块，哪个更合适只有定义者知道。
     */
    private ItemStack representative = ItemStack.EMPTY;

    // ==================== 登记 ====================

    /** 登记一个方块，忽略其元数据（例如石头会同时覆盖花岗岩/闪长岩/安山岩）。 */
    public BlockMatcher addBlock(Block block) {
        if (anyMeta.add(block)) {
            noteRepresentative(block, 0);
        }
        return this;
    }

    /** 登记一个具体的「方块 + 元数据」组合。 */
    public BlockMatcher addBlock(Block block, int meta) {
        if (exactMeta.add(new BlockMeta(block, meta))) {
            noteRepresentative(block, meta);
        }
        return this;
    }

    /** {@link #addBlock(Block, int)} 的便捷重载。 */
    public BlockMatcher addBlock(IBlockState state) {
        Block block = state.getBlock();
        return addBlock(block, block.getMetaFromState(state));
    }

    /** 登记一个矿物词典条目，例如 {@code "oreCopper"}。 */
    public BlockMatcher addOreDict(String oreDictName) {
        oreDictNames.add(oreDictName);
        return this;
    }

    /** 登记一种 GT 材料，命中该材料的任意形态。 */
    public BlockMatcher addMaterial(Material material) {
        materials.add(material);
        return this;
    }

    /**
     * 按注册名登记一个方块。
     *
     * <p>两种写法：
     * <ul>
     *     <li>{@code "modid:name"} —— 匹配该方块的任意元数据</li>
     *     <li>{@code "modid:name:meta"} —— 只匹配指定元数据</li>
     * </ul>
     *
     * <p>用于引用<em>编译期拿不到类</em>的方块——别的 mod 的方块（可能压根没装），
     * 或者 GT 按材料生成的方块（运行时才知道实例）。匹配时只比对注册名，
     * 不做注册表查询，所以对方块不存在的情况天然安全（永不命中）。
     */
    public BlockMatcher addBlockId(String registryNameWithOptionalMeta) {
        blockIds.add(registryNameWithOptionalMeta);
        return this;
    }

    /**
     * 显式指定代表物品（显示用）。
     *
     * <p>第一个生效，后续调用被忽略。想改显示名就在登记方块/矿辞之前调它，
     * 或者干脆只调它、不依赖自动推导。
     */
    public BlockMatcher setRepresentative(ItemStack stack) {
        if (representative.isEmpty() && stack != null && !stack.isEmpty()) {
            representative = stack;
        }
        return this;
    }

    // ==================== 查询 ====================

    /** 该方块（含元数据）是否命中本匹配器。 */
    public boolean matches(Block block, int meta) {
        if (anyMeta.contains(block)) {
            return true;
        }
        if (!exactMeta.isEmpty() && exactMeta.contains(new BlockMeta(block, meta))) {
            return true;
        }
        if (!blockIds.isEmpty() && matchesBlockId(block, meta)) {
            return true;
        }
        return (!oreDictNames.isEmpty() || !materials.isEmpty()) && matchesItemStack(block, meta);
    }

    /** {@link #matches(Block, int)} 的便捷重载。 */
    public boolean matches(IBlockState state) {
        Block block = state.getBlock();
        return matches(block, block.getMetaFromState(state));
    }

    /** 是否一个成员都没登记。空匹配器会拒绝一切方块。 */
    public boolean isEmpty() {
        return anyMeta.isEmpty() && exactMeta.isEmpty() && blockIds.isEmpty()
                && oreDictNames.isEmpty() && materials.isEmpty();
    }

    /** 给玩家看的代表物品；一个可转物品的成员都没有时返回空栈。 */
    public ItemStack getRepresentative() {
        return representative;
    }

    /**
     * 能给得出名字的那部分方块注册名：显式登记的 ID，加上已登记 {@link Block} 实例的注册名。
     *
     * <p>材料与矿物词典那两路给不出确定的名字，所以<b>不会</b>出现在结果里——
     * 拿到的是「能确定的那部分」，别当成全部。
     */
    public Set<String> getKnownBlockIds() {
        Set<String> ids = new HashSet<>(blockIds);
        for (Block block : anyMeta) {
            ResourceLocation id = block.getRegistryName();
            if (id != null) {
                ids.add(id.toString());
            }
        }
        for (BlockMeta entry : exactMeta) {
            ResourceLocation id = entry.block.getRegistryName();
            if (id != null) {
                ids.add(id + ":" + entry.meta);
            }
        }
        return ids;
    }

    /** 所有能转成物品的成员，用于 JEI / tooltip。 */
    public List<ItemStack> getDisplayItems() {
        List<ItemStack> items = new ArrayList<>();
        for (Block block : anyMeta) {
            addDisplayItem(items, block, 0);
        }
        for (BlockMeta entry : exactMeta) {
            addDisplayItem(items, entry.block, entry.meta);
        }
        return Collections.unmodifiableList(items);
    }

    @Override
    public String toString() {
        return "BlockMatcher[blocks=" + (anyMeta.size() + exactMeta.size())
                + ", oreDicts=" + oreDictNames.size()
                + ", materials=" + materials.size() + "]";
    }

    // ==================== 内部 ====================

    private void noteRepresentative(Block block, int meta) {
        if (!representative.isEmpty()) {
            return;
        }
        ItemStack stack = new ItemStack(block, 1, meta);
        if (!stack.isEmpty()) {
            representative = stack;
        }
    }


    private void addDisplayItem(List<ItemStack> items, Block block, int meta) {
        ItemStack stack = new ItemStack(block, 1, meta);
        if (!stack.isEmpty() && !containsStack(items, stack)) {
            items.add(stack);
        }
    }

    /** 按注册名比对。方块没有注册名（理论上不该出现）时判否。 */
    private boolean matchesBlockId(Block block, int meta) {
        ResourceLocation id = block.getRegistryName();
        if (id == null) {
            return false;
        }
        String name = id.toString();
        return blockIds.contains(name) || blockIds.contains(name + ":" + meta);
    }

    /** 走矿物词典 / GT 材料两路。方块没有对应物品时直接判否。 */
    private boolean matchesItemStack(Block block, int meta) {
        ItemStack stack = new ItemStack(block, 1, meta);
        if (stack.isEmpty()) {
            return false;
        }
        if (!oreDictNames.isEmpty()) {
            for (int id : OreDictionary.getOreIDs(stack)) {
                if (oreDictNames.contains(OreDictionary.getOreName(id))) {
                    return true;
                }
            }
        }
        if (!materials.isEmpty()) {
            UnificationEntry entry = OreDictUnifier.getUnificationEntry(stack);
            if (entry != null && entry.material != null && materials.contains(entry.material)) {
                return true;
            }
        }
        return false;
    }

    private static boolean containsStack(List<ItemStack> items, ItemStack target) {
        for (ItemStack stack : items) {
            if (ItemStack.areItemsEqual(stack, target)) {
                return true;
            }
        }
        return false;
    }

    /** 「方块 + 元数据」的值对象。 */
    private static final class BlockMeta {
        final Block block;
        final int meta;

        BlockMeta(Block block, int meta) {
            this.block = block;
            this.meta = meta;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof BlockMeta)) {
                return false;
            }
            BlockMeta other = (BlockMeta) o;
            return meta == other.meta && block == other.block;
        }

        @Override
        public int hashCode() {
            return Objects.hash(System.identityHashCode(block), meta);
        }
    }
}
