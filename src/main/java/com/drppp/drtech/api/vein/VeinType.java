package com.drppp.drtech.api.vein;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 矿脉类型：定义名称、可选矿物池，以及生成时随机抽取 2~4 种矿物的规则。
 *
 * 注册示例（在 VeinRegistry 中）：
 * <pre>
 *   VeinType iron = new VeinType("iron_vein")
 *       .addOre("minecraft:iron_ore",   60)
 *       .addOre("minecraft:copper_ore", 30)
 *       .addOre("minecraft:coal_ore",   10);
 *   VeinRegistry.register(iron);
 * </pre>
 */
public class VeinType {

    /** 矿脉类型唯一标识，全小写，用下划线分隔 */
    public final String id;

    /** 矿物候选池（不可变，build 后锁定） */
    private final List<OreEntry> orePool = new ArrayList<>();

    /** 生成时从 orePool 中随机挑选的矿物种数范围 */
    private int minOreTypes = 2;
    private int maxOreTypes = 4;

    public VeinType(String id) {
        this.id = id;
    }

    public VeinType addOre(String name, int weight) {
        orePool.add(new OreEntry(name, weight));
        return this;
    }

    public VeinType setOreTypeRange(int min, int max) {
        this.minOreTypes = min;
        this.maxOreTypes = max;
        return this;
    }

    public List<OreEntry> getOrePool() {
        return Collections.unmodifiableList(orePool);
    }

    public int getMinOreTypes() { return minOreTypes; }
    public int getMaxOreTypes() { return maxOreTypes; }
}
