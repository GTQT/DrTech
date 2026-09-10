package com.drppp.drtech.api.utils;

import com.drppp.drtech.common.items.metaItems.DrMetaItems;
import com.drppp.drtech.common.metaTileEntities.single.MetaTileEntityIndustrialApiary;
import net.minecraft.init.Biomes;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * 工业蜂箱的升级件定义。
 *
 * <p>每一条枚举就是一个升级，把"物品元数据"、"可叠加数量上限"和"对
 * {@link MetaTileEntityIndustrialApiary.GT_ApiaryModifier} 施加的修正"绑在一起。
 * 蜂箱在 {@code MetaTileEntityIndustrialApiary#updateModifiers()} 里遍历升级槽，
 * 反复调用 {@link #applyModifiers} 把修正值累加成一组环境/速率参数。
 *
 * <p>所有升级共用同一个物品（{@code DrMetaItems.UPGRADE_PLAIN}），靠元数据区分，
 * 元数据取值范围见 {@link #META_RANGE_MIN} ~ {@link #META_RANGE_MAX}。
 *
 * <p>{@link #applyModifiers} 的第二个参数是<em>堆叠数量</em>而不是"装了几个槽位"，
 * 所以像增产、授粉这类上限大于 1 的升级，效果是随数量指数/线性增长的。
 */
public enum GT_ApiaryUpgrade {

    // ---- 加速：互斥，同一台机器只能装一件 ----

    speed1(UNIQUE_INDEX.SPEED_UPGRADE, 29, 1, (mods, n) -> mods.maxSpeed = 1),
    speed2(UNIQUE_INDEX.SPEED_UPGRADE, 30, 1, (mods, n) -> mods.maxSpeed = 2),
    speed3(UNIQUE_INDEX.SPEED_UPGRADE, 31, 1, (mods, n) -> mods.maxSpeed = 3),
    speed4(UNIQUE_INDEX.SPEED_UPGRADE, 32, 1, (mods, n) -> mods.maxSpeed = 4),
    speed5(UNIQUE_INDEX.SPEED_UPGRADE, 33, 1, (mods, n) -> mods.maxSpeed = 5),
    speed6(UNIQUE_INDEX.SPEED_UPGRADE, 34, 1, (mods, n) -> mods.maxSpeed = 6),
    speed7(UNIQUE_INDEX.SPEED_UPGRADE, 35, 1, (mods, n) -> mods.maxSpeed = 7),
    speed8(UNIQUE_INDEX.SPEED_UPGRADE, 36, 1, (mods, n) -> mods.maxSpeed = 8),
    /** 满速 + 增产，代价是耗电翻了将近 15 倍。 */
    speed8upgraded(UNIQUE_INDEX.SPEED_UPGRADE, 37, 1, (mods, n) -> {
        mods.maxSpeed = 8;
        mods.production = 17.19926784f;
        mods.energy *= 14.75;
    }),

    // ---- 产量 ----

    production(UNIQUE_INDEX.PRODUCTION_UPGRADE, 38, 8, (mods, n) -> {
        mods.production = 4.f * (float) Math.pow(1.2d, n);
        mods.energy *= Math.pow(1.4f, n);
    }),

    // ---- 环境模拟：覆盖温度 / 湿度 / 生物群系 ----

    plains(UNIQUE_INDEX.PLAINS_UPGRADE, 39, 1, (mods, n) -> {
        mods.biomeOverride = Biomes.PLAINS;
        mods.energy *= 1.2f;
    }),
    winter(UNIQUE_INDEX.WINTER_UPGRADE, 42, 1, (mods, n) -> {
        mods.biomeOverride = Biomes.TAIGA;
        mods.energy *= 1.5f;
    }),
    hell(UNIQUE_INDEX.HELL_UPGRADE, 46, 1, (mods, n) -> {
        mods.biomeOverride = Biomes.HELL;
        mods.energy *= 1.5f;
    }),
    desert(UNIQUE_INDEX.DESERT_UPGRADE, 48, 1, (mods, n) -> {
        mods.biomeOverride = Biomes.DESERT;
        mods.energy *= 1.2f;
    }),
    jungle(UNIQUE_INDEX.JUNGLE_UPGRADE, 53, 1, (mods, n) -> {
        mods.biomeOverride = Biomes.JUNGLE;
        mods.energy *= 1.20f;
    }),
    ocean(UNIQUE_INDEX.OCEAN_UPGRADE, 55, 1, (mods, n) -> {
        mods.biomeOverride = Biomes.OCEAN;
        mods.energy *= 1.20f;
    }),
    /** 每件降低 0.125 湿度。 */
    dryer(UNIQUE_INDEX.DRYER_UPGRADE, 43, 16, (mods, n) -> {
        mods.humidity -= 0.125f * n;
        mods.energy *= Math.pow(1.025f, n);
    }),
    /** 每件提升 0.125 湿度。 */
    humidifier(UNIQUE_INDEX.HUMIDIFIER_UPGRADE, 45, 16, (mods, n) -> {
        mods.humidity += 0.125f * n;
        mods.energy *= Math.pow(1.05f, n);
    }),
    /** 每件降低 0.125 温度。 */
    cooler(UNIQUE_INDEX.COOLER_UPGRADE, 49, 16, (mods, n) -> {
        mods.temperature -= 0.125f * n;
        mods.energy *= Math.pow(1.025f, n);
    }),
    /** 每件提升 0.125 温度。 */
    heater(UNIQUE_INDEX.HEATER_UPGRADE, 57, 16, (mods, n) -> {
        mods.temperature += 0.125f * n;
        mods.energy *= Math.pow(1.025f, n);
    }),

    // ---- 行为开关 ----

    /** 内部光照，让蜂箱在黑夜里也能工作。 */
    light(UNIQUE_INDEX.LIGHT_UPGRADE, 40, 1, (mods, n) -> {
        mods.isSelfLighted = true;
        mods.energy *= 1.05f;
    }),
    /** 内部黑暗，屏蔽"夜晚不能工作"之类的限制，并把光照视为 0。 */
    unlight(UNIQUE_INDEX.LIGHT_UPGRADE, 59, 1, (mods, n) -> {
        mods.isSelfUnlighted = true;
        mods.energy *= 1.05f;
    }),
    /** 模拟露天，不需要真的能看到天空。 */
    sky(UNIQUE_INDEX.SKY_UPGRADE, 56, 1, (mods, n) -> {
        mods.isSunlightSimulated = true;
        mods.energy *= 1.05f;
    }),
    /** 封闭蜂箱，隔绝雨水等外界影响。 */
    seal(UNIQUE_INDEX.SEAL_UPGRADE, 51, 1, (mods, n) -> {
        mods.isSealed = true;
        mods.energy *= 1.05f;
    }),
    /** 自动把产出的蜜蜂放回内部槽位。 */
    automation(UNIQUE_INDEX.AUTOMATION_UPGRADE, 44, 1, (mods, n) -> {
        mods.isAutomated = true;
        mods.energy *= 1.1f;
    }),

    // ---- 属性强化 ----

    flowering(UNIQUE_INDEX.FLOWERING_UPGRADE, 41, 8, (mods, n) -> {
        mods.flowering *= Math.pow(1.2f, n);
        mods.energy *= Math.pow(1.1f, n);
    }),
    /** 关闭授粉并转而收集花粉。 */
    pollen(UNIQUE_INDEX.POLLEN_UPGRADE, 47, 1, (mods, n) -> {
        mods.flowering = 0f;
        mods.energy *= 1.3f;
    }),
    /** 缩短寿命，等价于加快单个流程的结算速度。 */
    lifespan(UNIQUE_INDEX.LIFESPAN_UPGRADE, 50, 4, (mods, n) -> {
        mods.lifespan /= Math.pow(1.5f, n);
        mods.energy *= Math.pow(1.05f, n);
    }),
    /** 消除基因退化。 */
    stabilizer(UNIQUE_INDEX.STABILIZER_UPGRADE, 52, 1, (mods, n) -> {
        mods.geneticDecay = 0f;
        mods.energy *= 2.50f;
    }),
    /** 扩大搜索花朵的范围。 */
    territory(UNIQUE_INDEX.TERRITORY_UPGRADE, 54, 4, (mods, n) -> {
        mods.territory *= Math.pow(1.5f, n);
        mods.energy *= Math.pow(1.05f, n);
    }),
    /** 收集花粉的筛网，与授粉互斥。 */
    sieve(UNIQUE_INDEX.SIEVE_UPGRADE, 58, 1, (mods, n) -> {
        mods.isCollectingPollen = true;
        mods.energy *= 1.05f;
    });

    /** 有效升级的元数据下界，同时也是 {@link #speed1} 的元数据。 */
    public static final int META_RANGE_MIN = 29;
    /** 有效升级的元数据上界。 */
    public static final int META_RANGE_MAX = 59;
    /** 加速类升级的元数据上界（{@link #speed8upgraded}），同类之间互斥。 */
    public static final int META_SPEED_MAX = 37;
    /** 增产升级的元数据，与 {@link #speed8upgraded} 互斥。 */
    public static final int META_PRODUCTION = 38;

    /** 升级的分组，同一组内的升级互斥，同组升级也共用一条配置/配方来源。 */
    private enum UNIQUE_INDEX {

        SPEED_UPGRADE,
        PRODUCTION_UPGRADE,
        PLAINS_UPGRADE,
        /** {@link #light} 与 {@link #unlight} 共用，两者互斥。 */
        LIGHT_UPGRADE,
        FLOWERING_UPGRADE,
        WINTER_UPGRADE,
        DRYER_UPGRADE,
        AUTOMATION_UPGRADE,
        HUMIDIFIER_UPGRADE,
        HELL_UPGRADE,
        POLLEN_UPGRADE,
        DESERT_UPGRADE,
        COOLER_UPGRADE,
        LIFESPAN_UPGRADE,
        SEAL_UPGRADE,
        STABILIZER_UPGRADE,
        JUNGLE_UPGRADE,
        TERRITORY_UPGRADE,
        OCEAN_UPGRADE,
        SKY_UPGRADE,
        HEATER_UPGRADE,
        SIEVE_UPGRADE;

        /** 对该分组下的每一个升级执行一次操作。 */
        void apply(Consumer<GT_ApiaryUpgrade> fn) {
            UNIQUE_UPGRADE_LIST.get(this).forEach(fn);
        }
    }

    /** 分组 → 该分组下的全部升级，由 {@link #register()} 在静态初始化时填好。 */
    private static final Map<UNIQUE_INDEX, List<GT_ApiaryUpgrade>> UNIQUE_UPGRADE_LIST = new EnumMap<>(UNIQUE_INDEX.class);
    /** 元数据 → 升级，供 {@link #getUpgrade} 快速查表。 */
    private static final Map<Integer, GT_ApiaryUpgrade> quickLookup = new HashMap<>();

    private final UNIQUE_INDEX uniqueIndex;
    private final int meta;
    private final int maxNumber;
    private final BiConsumer<MetaTileEntityIndustrialApiary.GT_ApiaryModifier, Integer> applier;

    GT_ApiaryUpgrade(UNIQUE_INDEX uniqueIndex, int meta, int maxNumber,
                     BiConsumer<MetaTileEntityIndustrialApiary.GT_ApiaryModifier, Integer> applier) {
        this.uniqueIndex = uniqueIndex;
        this.meta = meta;
        this.maxNumber = maxNumber;
        this.applier = applier;
    }

    /** 把本升级登记进两张表，只应该在类初始化时调用。 */
    private void register() {
        quickLookup.put(this.meta, this);
        UNIQUE_UPGRADE_LIST.computeIfAbsent(this.uniqueIndex, key -> new ArrayList<>(1)).add(this);
    }

    public static GT_ApiaryUpgrade getUpgrade(ItemStack s) {
        return isUpgrade(s) ? quickLookup.get(s.getMetadata()) : null;
    }

    public int getMaxNumber() {
        return maxNumber;
    }

    /**
     * 把本升级的效果叠加到修正值上。
     *
     * @param stack 升级物品，其堆叠数量会作为强度系数传给计算式
     */
    public void applyModifiers(MetaTileEntityIndustrialApiary.GT_ApiaryModifier mods, ItemStack stack) {
        if (applier != null) {
            applier.accept(mods, stack.getCount());
        }
    }

    /** 造出指定数量的本升级物品。 */
    public ItemStack get(int count) {
        return new ItemStack(DrMetaItems.UPGRADE_PLAIN.getMetaItem(), count, meta);
    }

    /** 判断物品是否为工业蜂箱升级。所有升级共用同一个物品，靠元数据区间区分。 */
    public static boolean isUpgrade(ItemStack s) {
        return s != null && !s.isEmpty()
                && s.getItem() == DrMetaItems.UPGRADE_SPEED1.getMetaItem()
                && s.getMetadata() >= META_RANGE_MIN && s.getMetadata() <= META_RANGE_MAX;
    }

    /** 是否为加速类升级，任意两件加速升级互斥。 */
    public static boolean isSpeedUpgrade(ItemStack s) {
        return isUpgrade(s) && s.getMetadata() <= META_SPEED_MAX;
    }

    static {
        EnumSet.allOf(GT_ApiaryUpgrade.class).forEach(GT_ApiaryUpgrade::register);
    }
}
