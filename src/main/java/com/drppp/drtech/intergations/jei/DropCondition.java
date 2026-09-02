package com.drppp.drtech.intergations.jei;

import net.minecraft.client.resources.I18n;

/**
 * 掉落条件（移植自 JustEnoughDrops / JustEnoughResources 的 Conditional 语义子集）。
 * 战利品表结构分析只会产出这三种，外加"燃烧时掉落"。
 */
public enum DropCondition {
    PLAYER_KILL("drtech.mob_info.cond.player_kill", ""),
    AFFECTED_BY_LOOTING("drtech.mob_info.cond.looting", "§b"),
    BURNING("drtech.mob_info.cond.burning", "§c");

    private final String key;
    private final String colour;

    DropCondition(String key, String colour) {
        this.key = key;
        this.colour = colour;
    }

    /** tooltip 里显示的一行（含颜色前缀） */
    public String toLine() {
        return colour + I18n.format(key);
    }
}
