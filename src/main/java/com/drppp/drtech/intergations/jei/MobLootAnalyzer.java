package com.drppp.drtech.intergations.jei;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootEntry;
import net.minecraft.world.storage.loot.LootEntryItem;
import net.minecraft.world.storage.loot.LootEntryTable;
import net.minecraft.world.storage.loot.LootPool;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraft.world.storage.loot.LootTableManager;
import net.minecraft.world.storage.loot.RandomValueRange;
import net.minecraft.world.storage.loot.conditions.EntityHasProperty;
import net.minecraft.world.storage.loot.conditions.KilledByPlayer;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraft.world.storage.loot.conditions.RandomChance;
import net.minecraft.world.storage.loot.conditions.RandomChanceWithLooting;
import net.minecraft.world.storage.loot.functions.EnchantRandomly;
import net.minecraft.world.storage.loot.functions.EnchantWithLevels;
import net.minecraft.world.storage.loot.functions.LootFunction;
import net.minecraft.world.storage.loot.functions.LootingEnchantBonus;
import net.minecraft.world.storage.loot.functions.SetCount;
import net.minecraft.world.storage.loot.functions.SetMetadata;
import net.minecraft.world.storage.loot.functions.Smelt;
import net.minecraft.world.storage.loot.properties.EntityOnFire;
import net.minecraft.world.storage.loot.properties.EntityProperty;
import net.minecraft.util.math.MathHelper;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 战利品表 -> 掉落列表的确定性分析引擎。
 *
 * 算法与 JER 的 LootTableHelper.toDrops + LootFunctionHelper + LootConditionHelper 语义一致：
 * 权重占比作概率（pool 的 rolls 数被忽略，这是 JER/JED 的既定显示口径）、
 * SetCount/SetMetadata/Smelt/LootingEnchantBonus 等函数与
 * KilledByPlayer/RandomChance/RandomChanceWithLooting/EntityOnFire 等条件改写数量/概率/条件标签。
 *
 * 关键：不依赖任何 WorldServer。本环境的 LootTableManager(File) 在文件缺失时会回退到
 * classpath 内置资源 /assets/&lt;ns&gt;/loot_tables/&lt;path&gt;.json，因此 new LootTableManager(null)
 * 即可在客户端任何时刻解析原版与 mod 的战利品表（含多人服务器场景）。
 */
public class MobLootAnalyzer {

    /** classpath 资源回退的 manager，所有分析共用（懒加载，首次 analyze 时构建） */
    private static volatile LootTableManager manager;

    private static final Map<LootTable, List<LootDropInfo>> CACHE = new HashMap<>();

    private static final Map<Class<?>, Field> POOLS_FIELD = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Field> ENTRIES_FIELD = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Field> POOL_CONDITIONS_FIELD = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Field> ITEM_FIELD = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Field> FUNCTIONS_FIELD = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Field> ENTRY_CONDITIONS_FIELD = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Field> TABLE_RL_FIELD = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Field> CHANCE_FIELD = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Field> COUNT_RANGE_FIELD = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Field> META_RANGE_FIELD = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Field> PROPERTIES_FIELD = new ConcurrentHashMap<>();

    /** 嵌套战利品表引用递归深度上限，防环形引用 */
    private static final int MAX_TABLE_DEPTH = 16;

    private MobLootAnalyzer() {
    }

    // ---------------- 字段访问：按类型匹配（不依赖字段名，dev/生产通吃） ----------------

    private static Field fieldByExactType(Class<?> owner, Class<?> type, Map<Class<?>, Field> cache) {
        Field cached = cache.get(owner);
        if (cached != null) return cached;
        synchronized (cache) {
            cached = cache.get(owner);
            if (cached != null) return cached;
            Field found = null;
            for (Field f : owner.getDeclaredFields()) {
                if (f.getType() == type) {
                    found = f;
                    break;
                }
            }
            if (found == null) return null;
            found.setAccessible(true);
            cache.put(owner, found);
            return found;
        }
    }

    private static Field fieldByGenericArg(Class<?> owner, Class<?> typeArg, Map<Class<?>, Field> cache) {
        Field cached = cache.get(owner);
        if (cached != null) return cached;
        synchronized (cache) {
            cached = cache.get(owner);
            if (cached != null) return cached;
            Field found = null;
            for (Field f : owner.getDeclaredFields()) {
                Type t = f.getGenericType();
                if (t instanceof ParameterizedType) {
                    Type[] args = ((ParameterizedType) t).getActualTypeArguments();
                    if (args.length == 1 && args[0] == typeArg) {
                        found = f;
                        break;
                    }
                }
            }
            if (found == null) return null;
            found.setAccessible(true);
            cache.put(owner, found);
            return found;
        }
    }

    private static Object read(Field field, Object target) {
        if (field == null) return null;
        try {
            return field.get(target);
        } catch (IllegalAccessException e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private static List<LootPool> getPools(LootTable table) {
        Object o = read(fieldByGenericArg(LootTable.class, LootPool.class, POOLS_FIELD), table);
        return o == null ? java.util.Collections.emptyList() : (List<LootPool>) o;
    }

    @SuppressWarnings("unchecked")
    private static List<LootEntry> getEntries(LootPool pool) {
        Object o = read(fieldByGenericArg(LootPool.class, LootEntry.class, ENTRIES_FIELD), pool);
        return o == null ? java.util.Collections.emptyList() : (List<LootEntry>) o;
    }

    @SuppressWarnings("unchecked")
    private static List<LootCondition> getPoolConditions(LootPool pool) {
        Object o = read(fieldByGenericArg(LootPool.class, LootCondition.class, POOL_CONDITIONS_FIELD), pool);
        return o == null ? java.util.Collections.emptyList() : (List<LootCondition>) o;
    }

    private static ItemStack getItem(LootEntryItem entry) {
        return new ItemStack((net.minecraft.item.Item) read(
                fieldByExactType(LootEntryItem.class, net.minecraft.item.Item.class, ITEM_FIELD), entry));
    }

    private static LootFunction[] getFunctions(LootEntryItem entry) {
        Object o = read(fieldByExactType(LootEntryItem.class, LootFunction[].class, FUNCTIONS_FIELD), entry);
        return o == null ? new LootFunction[0] : (LootFunction[]) o;
    }

    private static LootCondition[] getEntryConditions(LootEntry entry) {
        Object o = read(fieldByExactType(LootEntry.class, LootCondition[].class, ENTRY_CONDITIONS_FIELD), entry);
        return o == null ? new LootCondition[0] : (LootCondition[]) o;
    }

    private static ResourceLocation getReferencedTable(LootEntryTable entry) {
        return (ResourceLocation) read(
                fieldByExactType(LootEntryTable.class, ResourceLocation.class, TABLE_RL_FIELD), entry);
    }

    // ---------------- 公开入口 ----------------

    public static LootTableManager manager() {
        LootTableManager m = manager;
        if (m == null) {
            synchronized (MobLootAnalyzer.class) {
                m = manager;
                if (m == null) {
                    // null 目录 => 只走 classpath /assets/... 回退分支
                    m = new LootTableManager((File) null);
                    manager = m;
                }
            }
        }
        return m;
    }

    /** 分析某实体对应的战利品表（RL 为 null 或表缺失时返回空列表） */
    public static List<LootDropInfo> analyze(ResourceLocation lootTable) {
        if (lootTable == null) return new ArrayList<>();
        return analyze(manager().getLootTableFromLocation(lootTable));
    }

    public static List<LootDropInfo> analyze(LootTable table) {
        synchronized (CACHE) {
            List<LootDropInfo> cached = CACHE.get(table);
            if (cached != null) return new ArrayList<>(cached);
        }
        List<LootDropInfo> result = toDrops(table, new HashSet<>(), 0);
        synchronized (CACHE) {
            CACHE.put(table, new ArrayList<>(result));
        }
        return result;
    }

    private static List<LootDropInfo> toDrops(LootTable table, Set<ResourceLocation> visited, int depth) {
        List<LootDropInfo> drops = new ArrayList<>();
        if (table == null || depth > MAX_TABLE_DEPTH) return drops;

        for (LootPool pool : getPools(table)) {
            List<LootEntry> entries = getEntries(pool);
            float totalWeight = 0F;
            for (LootEntry entry : entries) {
                totalWeight += entry.getEffectiveWeight(0);
            }
            if (totalWeight <= 0F) totalWeight = 1F;
            List<LootCondition> poolConditions = getPoolConditions(pool);

            for (LootEntry entry : entries) {
                if (entry instanceof LootEntryItem) {
                    LootEntryItem itemEntry = (LootEntryItem) entry;
                    LootDropInfo drop = new LootDropInfo(
                            getItem(itemEntry),
                            itemEntry.getEffectiveWeight(0) / totalWeight);
                    // 先应用条目自身条件，再应用 pool 条件（同 JER；RandomChance 后写覆盖前写）
                    applyConditions(drop, getEntryConditions(entry));
                    applyConditions(drop, poolConditions.toArray(new LootCondition[0]));
                    applyFunctions(drop, getFunctions(itemEntry));
                    drops.add(drop);
                } else if (entry instanceof LootEntryTable) {
                    ResourceLocation ref = getReferencedTable((LootEntryTable) entry);
                    if (ref != null && visited.add(ref)) {
                        drops.addAll(toDrops(manager().getLootTableFromLocation(ref), visited, depth + 1));
                        visited.remove(ref);
                    }
                }
            }
        }
        drops.removeIf(drop -> drop == null || drop.item == null || drop.item.isEmpty());
        return drops;
    }

    private static void applyConditions(LootDropInfo drop, LootCondition[] conditions) {
        for (LootCondition condition : conditions) {
            if (condition instanceof KilledByPlayer) {
                drop.addCondition(DropCondition.PLAYER_KILL);
            } else if (condition instanceof RandomChance) {
                drop.chance = readChance((RandomChance) condition);
            } else if (condition instanceof RandomChanceWithLooting) {
                drop.chance = readChance((RandomChanceWithLooting) condition);
                drop.addCondition(DropCondition.AFFECTED_BY_LOOTING);
            } else if (condition instanceof EntityHasProperty) {
                for (EntityProperty property : readProperties((EntityHasProperty) condition)) {
                    if (property instanceof EntityOnFire) {
                        drop.addCondition(DropCondition.BURNING);
                    }
                }
            }
        }
    }

    private static void applyFunctions(LootDropInfo drop, LootFunction[] functions) {
        for (LootFunction function : functions) {
            if (function instanceof SetCount) {
                RandomValueRangeLike range = readRange(countRangeOf((SetCount) function));
                drop.minDrop = MathHelper.floor(range.min);
                if (drop.minDrop < 0) drop.minDrop = 0;
                drop.item.setCount(drop.minDrop < 1 ? 1 : drop.minDrop);
                drop.maxDrop = MathHelper.floor(range.max);
            } else if (function instanceof SetMetadata) {
                RandomValueRangeLike range = readRange(metaRangeOf((SetMetadata) function));
                drop.item.setItemDamage(MathHelper.floor(range.min));
            } else if (function instanceof EnchantRandomly || function instanceof EnchantWithLevels) {
                drop.enchanted = true;
            } else if (function instanceof Smelt) {
                try {
                    ItemStack smelted = function.apply(drop.item.copy(), null, null);
                    if (smelted != null && !ItemStack.areItemStacksEqual(drop.item, smelted)) {
                        drop.smeltedItem = smelted;
                    }
                } catch (Exception ignored) {
                }
            } else if (function instanceof LootingEnchantBonus) {
                drop.addCondition(DropCondition.AFFECTED_BY_LOOTING);
            } else {
                try {
                    drop.item = function.apply(drop.item, null, null);
                } catch (Exception ignored) {
                }
            }
        }
    }

    // ---------------- 私有字段取值小工具（RandomValueRange 等） ----------------

    private static float readChance(RandomChance c) {
        return (Float) read(fieldByExactType(RandomChance.class, float.class, CHANCE_FIELD), c);
    }

    private static float readChance(RandomChanceWithLooting c) {
        return (Float) read(fieldByExactType(RandomChanceWithLooting.class, float.class, CHANCE_FIELD), c);
    }

    private static EntityProperty[] readProperties(EntityHasProperty c) {
        Object o = read(fieldByExactType(EntityHasProperty.class, EntityProperty[].class, PROPERTIES_FIELD), c);
        return o == null ? new EntityProperty[0] : (EntityProperty[]) o;
    }

    private static Object countRangeOf(SetCount f) {
        return read(fieldByExactType(SetCount.class, RandomValueRange.class, COUNT_RANGE_FIELD), f);
    }

    private static Object metaRangeOf(SetMetadata f) {
        return read(fieldByExactType(SetMetadata.class, RandomValueRange.class, META_RANGE_FIELD), f);
    }

    private static RandomValueRangeLike readRange(Object range) {
        if (range == null) return new RandomValueRangeLike(0, 0);
        return new RandomValueRangeLike(
                ((RandomValueRange) range).getMin(),
                ((RandomValueRange) range).getMax());
    }

    private static final class RandomValueRangeLike {
        final double min, max;

        RandomValueRangeLike(double min, double max) {
            this.min = min;
            this.max = max;
        }
    }
}
