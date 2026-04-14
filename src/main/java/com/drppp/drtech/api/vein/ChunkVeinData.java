package com.drppp.drtech.api.vein;


import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 一个区块的虚拟矿脉数据。
 *
 * <p>字段说明：
 * <ul>
 *   <li>{@code generated} — 是否已执行过生成判定（避免重复生成）</li>
 *   <li>{@code hasVein}   — 该区块是否存在基岩资源点</li>
 *   <li>{@code veinTypeId} — 矿脉类型 id（对应 VeinRegistry 中的 key）</li>
 *   <li>{@code ores}      — 实际出现的矿物及权重列表（2~4 种）</li>
 *   <li>{@code totalWeight} — ores 权重之和，用于采集时快速抽样</li>
 *   <li>{@code boundDrillPos} — 占用该资源点的钻机坐标（Long 编码），-1 表示未绑定</li>
 * </ul>
 */
public class ChunkVeinData {

    // ── 状态 ────────────────────────────────────────────────────
    private boolean generated   = false;
    private boolean hasVein     = false;
    private String  veinTypeId  = "";
    private final List<OreEntry> ores = new ArrayList<>();
    private int     totalWeight = 0;
    /** 绑定钻机的 BlockPos long 编码，-1 = 无 */
    private long    boundDrillPos = -1L;

    // ── 构造 ────────────────────────────────────────────────────

    public ChunkVeinData() {}

    // ── 修改 ────────────────────────────────────────────────────

    public void setGenerated(boolean generated) { this.generated = generated; }

    /**
     * 设置资源点内容。调用后 hasVein=true，并自动计算 totalWeight。
     */
    public void setVein(String typeId, List<OreEntry> selectedOres) {
        this.hasVein    = true;
        this.veinTypeId = typeId;
        this.ores.clear();
        this.ores.addAll(selectedOres);
        this.totalWeight = selectedOres.stream().mapToInt(o -> o.weight).sum();
    }

    public void clearVein() {
        this.hasVein    = false;
        this.veinTypeId = "";
        this.ores.clear();
        this.totalWeight = 0;
    }

    public void setBoundDrillPos(long encodedPos) { this.boundDrillPos = encodedPos; }
    public void clearBoundDrill()                 { this.boundDrillPos = -1L; }

    // ── 查询 ────────────────────────────────────────────────────

    public boolean isGenerated()  { return generated; }
    public boolean hasVein()      { return hasVein; }
    public String  getVeinTypeId(){ return veinTypeId; }
    public List<OreEntry> getOres(){ return Collections.unmodifiableList(ores); }
    public int  getTotalWeight()  { return totalWeight; }
    public long getBoundDrillPos(){ return boundDrillPos; }
    public boolean isBound()      { return boundDrillPos != -1L; }

    /**
     * 按权重随机抽取一种矿物。
     *
     * @param rand [0, totalWeight) 范围内的随机整数
     * @return 抽中的 OreEntry，或 null（ores 为空时）
     */
    public OreEntry pickOre(int rand) {
        if (ores.isEmpty() || totalWeight <= 0) return null;
        int cursor = rand % totalWeight;
        for (OreEntry ore : ores) {
            cursor -= ore.weight;
            if (cursor < 0) return ore;
        }
        return ores.get(ores.size() - 1); // 保险兜底
    }

    // ── NBT 序列化 ──────────────────────────────────────────────

    public void writeToNBT(NBTTagCompound tag) {
        tag.setBoolean("generated",    generated);
        tag.setBoolean("hasVein",      hasVein);
        tag.setString ("veinTypeId",   veinTypeId);
        tag.setLong   ("boundDrill",   boundDrillPos);
        tag.setInteger("totalWeight",  totalWeight);

        NBTTagList oreList = new NBTTagList();
        for (OreEntry ore : ores) {
            oreList.appendTag(ore.writeToNBT());
        }
        tag.setTag("ores", oreList);
    }

    public void readFromNBT(NBTTagCompound tag) {
        generated     = tag.getBoolean("generated");
        hasVein       = tag.getBoolean("hasVein");
        veinTypeId    = tag.getString ("veinTypeId");
        boundDrillPos = tag.getLong   ("boundDrill");
        totalWeight   = tag.getInteger("totalWeight");

        ores.clear();
        NBTTagList oreList = tag.getTagList("ores", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < oreList.tagCount(); i++) {
            ores.add(OreEntry.readFromNBT(oreList.getCompoundTagAt(i)));
        }
    }
}
