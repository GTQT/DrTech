package com.meowmel.cropQT.api.registries;

import com.meowmel.cropQT.api.potency.IPotencyData;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 「某样东西能提供多少水 / 肥 / 除草剂」的通用登记表。
 *
 * <p>值从原来的 {@code int} 升级为 {@link IPotencyData}：除了点数，还要能表达
 * 「存储上限」和「每次消耗几个单位」。这是源端 Potency 模型的全部输入。
 *
 * <p>查不到一律返回 {@code null} —— 调用方据此把不认识的输入当无效物品忽略掉，
 * 不需要先问「你认不认识这个」。需要 0 值时用 {@link #getPotency(Object)}。
 *
 * <p>登记采用 {@code putIfAbsent} 语义（与源端一致）：同一键重复登记时先登记的生效，
 * 避免后加载的兼容模组悄悄覆盖核心数值。需要覆盖时先 {@link #remove(Object)}。
 *
 * @param <K> 键的类型，例如 {@code Fluid} 或 {@code ItemAndMetadata}
 */
public class PotencyRegistry<K> {

    private final String name;
    private final Map<K, IPotencyData> entries = new LinkedHashMap<>();

    public PotencyRegistry(String name) {
        this.name = name;
    }

    /**
     * 登记一个键对应的强度数据。点数或每次消耗量 ≤0 直接抛异常 —— 这类错误应该在
     * 加载期暴露，而不是等运行到机器里才表现为"永远不施肥"。
     */
    public PotencyRegistry<K> register(K key, IPotencyData data) {
        if (key == null) {
            throw new IllegalArgumentException("potency key must not be null (" + name + ")");
        }
        if (data == null) {
            throw new IllegalArgumentException("potency data must not be null (" + name + ")");
        }
        if (data.getPotency() <= 0) {
            throw new IllegalArgumentException("potency must be > 0 (" + name + ")");
        }
        if (data.getUnitsConsumedPerApplication() <= 0) {
            throw new IllegalArgumentException("units per application must be > 0 (" + name + ")");
        }
        entries.putIfAbsent(key, data);
        return this;
    }

    /** 查强度数据；未登记返回 {@code null}。 */
    @Nullable
    public IPotencyData get(K key) {
        if (key == null) {
            return null;
        }
        return entries.get(key);
    }

    /** 查点数；未登记返回 0。 */
    public int getPotency(K key) {
        IPotencyData data = get(key);
        return data == null ? 0 : data.getPotency();
    }

    public boolean isRegistered(K key) {
        return key != null && entries.containsKey(key);
    }

    /** 移除一个登记项，返回旧值；用于兼容模组覆盖数值。 */
    @Nullable
    public IPotencyData remove(K key) {
        return entries.remove(key);
    }

    /** 登记表名，用于日志 / 调试。 */
    public String getName() {
        return name;
    }

    /** 只读的全部登记项，按登记顺序。 */
    public Map<K, IPotencyData> getAll() {
        return Collections.unmodifiableMap(entries);
    }
}
