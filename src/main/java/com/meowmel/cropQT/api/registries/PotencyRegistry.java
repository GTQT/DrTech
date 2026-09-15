package com.meowmel.cropQT.api.registries;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 「某样东西能提供多少水 / 肥」的通用登记表。
 *
 * <p>值是整数点数（potency）。<b>查不到一律返回 0</b>——调用方据此把不认识的输入当作
 * 无效物品忽略掉，而不需要先问「你认不认识这个」。
 *
 * @param <K> 键的类型，例如 {@code Fluid} 或 {@code ItemAndMetadata}
 */
public class PotencyRegistry<K> {

    private final String name;
    private final Map<K, Integer> potencies = new LinkedHashMap<>();

    public PotencyRegistry(String name) {
        this.name = name;
    }

    /** 登记一个键对应的点数。重复登记会覆盖。 */
    public PotencyRegistry<K> register(K key, int potency) {
        potencies.put(key, potency);
        return this;
    }

    /** 查点数；未登记返回 0。 */
    public int getPotency(K key) {
        if (key == null) {
            return 0;
        }
        Integer value = potencies.get(key);
        return value == null ? 0 : value;
    }

    public boolean exists(K key) {
        return key != null && potencies.containsKey(key);
    }

    /** 登记表名，用于日志 / 调试。 */
    public String getName() {
        return name;
    }

    /** 只读的全部登记项，按登记顺序。 */
    public Map<K, Integer> getAll() {
        return Collections.unmodifiableMap(potencies);
    }
}
