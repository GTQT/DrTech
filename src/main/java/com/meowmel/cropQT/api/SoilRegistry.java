package com.meowmel.cropQT.api;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 全局土壤注册表。
 *
 * <p>两个职责：
 * <ol>
 *     <li>按名字保存所有 {@link ISoilList}（{@link #register}）</li>
 *     <li>反向查询：给定一个方块，它是哪一组的（{@link #getSoilFor}）</li>
 * </ol>
 *
 * <p>反向查询会被每个作物的每次生长 tick 调用，所以带缓存。缓存按「方块 + 元数据」定格，
 * 只要没登记新成员就是稳定的；{@link #invalidateCache()} 由 {@link SoilList} 的登记方法自动触发。
 *
 * <p>一个方块同时属于多组时<b>先登记的赢</b>，所以 {@link SoilTypes} 里的登记顺序
 * 就是匹配优先级——具体的组要排在笼统的组前面。
 */
public final class SoilRegistry {

    private static final Map<String, ISoilList> REGISTRY = new LinkedHashMap<>();
    private static final Map<BlockMeta, ISoilList> LOOKUP_CACHE = new HashMap<>();

    /** 「任意土壤」的并集，第一次用到时构建。 */
    @Nullable
    private static CompoundSoilList allSoils;

    private SoilRegistry() {
    }

    // ==================== 登记 ====================

    public static void register(ISoilList soil) {
        REGISTRY.put(soil.getName(), soil);
        invalidateCache();
    }

    @Nullable
    public static ISoilList get(String name) {
        return REGISTRY.get(name);
    }

    public static boolean exists(String name) {
        return REGISTRY.containsKey(name);
    }

    /** 所有已登记的土壤组，按登记顺序。 */
    public static Collection<ISoilList> getAll() {
        return REGISTRY.values();
    }

    /** 「任意土壤」——所有已登记组的并集。没有登记过任何组时返回空组。 */
    public static CompoundSoilList getAllSoils() {
        if (allSoils == null) {
            allSoils = new CompoundSoilList("all", REGISTRY.values());
        }
        return allSoils;
    }

    // ==================== 反查 ====================

    /**
     * 这个方块属于哪一组土壤？没有登记过任何一组时返回 {@code null}。
     *
     * <p>返回 null 表示「不是土壤」——作物不能种在上面。
     */
    @Nullable
    public static ISoilList getSoilFor(Block block, int meta) {
        BlockMeta key = new BlockMeta(block, meta);
        if (LOOKUP_CACHE.containsKey(key)) {
            return LOOKUP_CACHE.get(key);
        }
        ISoilList found = null;
        for (ISoilList soil : REGISTRY.values()) {
            if (soil.contains(block, meta)) {
                found = soil;
                break;
            }
        }
        LOOKUP_CACHE.put(key, found);
        return found;
    }

    /**
     * {@link #getSoilFor(Block, int)} 的便捷重载。
     *
     * @return 土壤组；该方块不是土壤时返回 {@code null}
     */
    @Nullable
    public static ISoilList getSoilFor(IBlockState state) {
        Block block = state.getBlock();
        return getSoilFor(block, block.getMetaFromState(state));
    }

    // ==================== 缓存 ====================

    /** 清空反查缓存与并集快照。登记新成员时自动调用，一般不需要手动调。 */
    public static void invalidateCache() {
        LOOKUP_CACHE.clear();
        allSoils = null;
    }

    // ==================== 内部 ====================

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
