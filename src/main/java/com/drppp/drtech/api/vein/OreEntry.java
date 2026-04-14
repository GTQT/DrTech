package com.drppp.drtech.api.vein;

import net.minecraft.nbt.NBTTagCompound;

/**
 * 单种矿物及其在矿脉中的权重。
 * weight 越大，产出该矿物的概率越高。
 */
public class OreEntry {

    /** 矿物的注册名，例如 "minecraft:iron_ore" 或自定义字符串 */
    public final String oreName;
    /** 相对权重（正整数） */
    public final int weight;

    public OreEntry(String oreName, int weight) {
        if (weight <= 0) throw new IllegalArgumentException("weight must be > 0");
        this.oreName = oreName;
        this.weight  = weight;
    }

    // ── NBT 序列化 ──────────────────────────────────────────────

    public NBTTagCompound writeToNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("name",   oreName);
        tag.setInteger("weight", weight);
        return tag;
    }

    public static OreEntry readFromNBT(NBTTagCompound tag) {
        return new OreEntry(
            tag.getString("name"),
            tag.getInteger("weight")
        );
    }

    @Override
    public String toString() {
        return oreName + "×" + weight;
    }
}
