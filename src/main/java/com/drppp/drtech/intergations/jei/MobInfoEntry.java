package com.drppp.drtech.intergations.jei;

import net.minecraft.entity.EntityLiving;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 一只生物在 mob_info 分类页里的全部数据。
 * 对应 JED 语境中的"实体 + 掉落 + 展示辅助信息"条目。
 */
public class MobInfoEntry {

    private final EntityLiving entity;
    private final String entityId;
    private final String displayName;
    private final int xp;
    private final ItemStack spawnEgg;
    /** 确定性分析得出的掉落列表（已排序、已去重按 item 相等） */
    private final List<LootDropInfo> drops;
    /** 生成群系显示名列表；空 = 未知 */
    private final List<String> biomes;
    /** 找不到可分析的战利品表时为 true，页面底部显示"无法识别" */
    private final boolean unrecognized;

    public MobInfoEntry(EntityLiving entity, String entityId, String displayName, int xp,
                        ItemStack spawnEgg, List<LootDropInfo> drops, List<String> biomes, boolean unrecognized) {
        this.entity = entity;
        this.entityId = entityId;
        this.displayName = displayName;
        this.xp = xp;
        this.spawnEgg = spawnEgg;
        // 去重（同 item 只保留首个，语义同 JER MobEntry.addDrop）再排序
        List<LootDropInfo> unique = new ArrayList<>();
        for (LootDropInfo drop : drops) {
            boolean dup = false;
            for (LootDropInfo kept : unique) {
                if (kept.item.isItemEqual(drop.item)) {
                    dup = true;
                    break;
                }
            }
            if (!dup) unique.add(drop);
        }
        Collections.sort(unique);
        this.drops = unique;
        this.biomes = biomes;
        this.unrecognized = unrecognized;
    }

    public EntityLiving getEntity() {
        return entity;
    }

    public String getEntityId() {
        return entityId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getXp() {
        return xp;
    }

    public ItemStack getSpawnEgg() {
        return spawnEgg;
    }

    public List<LootDropInfo> getDrops() {
        return drops;
    }

    public List<String> getBiomes() {
        return biomes;
    }

    public boolean isUnrecognized() {
        return unrecognized;
    }
}
