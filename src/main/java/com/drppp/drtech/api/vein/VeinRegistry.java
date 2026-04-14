package com.drppp.drtech.api.vein;

import java.util.*;

/**
 * 全局矿脉类型注册表。
 * 在 FMLPreInitializationEvent 或 FMLInitializationEvent 阶段完成注册。
 */
public final class VeinRegistry {

    private VeinRegistry() {}

    private static final Map<String, VeinType> REGISTRY = new LinkedHashMap<>();

    /** 注册一个矿脉类型；id 重复时抛出异常 */
    public static void register(VeinType type) {
        if (REGISTRY.containsKey(type.id)) {
            throw new IllegalStateException("VeinType already registered: " + type.id);
        }
        REGISTRY.put(type.id, type);
    }

    /** 按 id 查询，不存在返回 null */
    public static VeinType get(String id) {
        return REGISTRY.get(id);
    }

    /** 获取所有已注册类型（有序） */
    public static Collection<VeinType> getAll() {
        return Collections.unmodifiableCollection(REGISTRY.values());
    }

    /** 获取类型列表（用于按下标随机选取） */
    public static List<VeinType> getList() {
        return new ArrayList<>(REGISTRY.values());
    }

    /** 已注册数量 */
    public static int size() {
        return REGISTRY.size();
    }
}
