package com.drppp.drtech.api.vein;


import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import java.util.*;

/**
 * 基于世界种子的确定性矿脉生成器。
 *
 * <h3>生成规则</h3>
 * <ol>
 *   <li>「密集资源点」检查：概率 = {@link #DENSE_CHANCE}（默认 0.1%）</li>
 *   <li>若命中密集点，该区块及周围 3×3 的其他 8 个区块以 {@link #SPREAD_CHANCE}（95%）生成。</li>
 *   <li>独立生成：每个区块再以 {@link #SOLO_CHANCE}（1%）独立判定一次。</li>
 *   <li>同一区块只会被生成一次（由 {@code ChunkVeinData#isGenerated()} 保证）。</li>
 * </ol>
 *
 * <h3>确定性</h3>
 * 所有随机数来自 {@link Random}，seed = {@code worldSeed ^ (chunkX * PRIME_X + chunkZ * PRIME_Z)}，
 * 保证相同种子 + 相同坐标永远产生相同结果。
 */
public final class WorldVeinGenerator {

    // ── 概率参数（可按需调整） ───────────────────────────────────

    /** 密集资源点生成概率（千分之一 = 0.001） */
    public static double DENSE_CHANCE  = 0.001;
    /** 密集点周围 8 个区块的生成概率 */
    public static double SPREAD_CHANCE = 0.95;
    /** 每个区块的独立生成概率 */
    public static double SOLO_CHANCE   = 0.01;

    // ── 种子混合质数 ─────────────────────────────────────────────

    private static final long PRIME_X = 0x9E3779B97F4A7C15L;
    private static final long PRIME_Z = 0x6C62272E07BB0142L;

    private WorldVeinGenerator() {}

    // ── 公开入口 ─────────────────────────────────────────────────

    /**
     * 对指定区块执行生成判定（仅服务端调用）。
     * 若该区块已经生成过，则直接返回。
     */
    public static void generateForChunk(World world, int chunkX, int chunkZ) {
        if (world.isRemote) return;

        Chunk chunk = world.getChunk(chunkX, chunkZ);
        ChunkVeinData data = getVeinData(chunk);
        if (data == null || data.isGenerated()) return;

        data.setGenerated(true);

        long worldSeed = world.getSeed();

        // 步骤 1：检查密集资源点
        Random denseRng = makeRng(worldSeed, chunkX, chunkZ, 0xDEAD);
        if (denseRng.nextDouble() < DENSE_CHANCE) {
            // 该区块本身标记为密集点（必然生成资源点）
            trySetVein(world, chunk, worldSeed, chunkX, chunkZ, 1.0);

            // 步骤 2：扩散到周围 8 个区块
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx == 0 && dz == 0) continue;
                    int nx = chunkX + dx, nz = chunkZ + dz;
                    Chunk neighbour = world.getChunk(nx, nz);
                    ChunkVeinData nd = getVeinData(neighbour);
                    if (nd != null && !nd.isGenerated()) {
                        nd.setGenerated(true);
                        trySetVein(world, neighbour, worldSeed, nx, nz, SPREAD_CHANCE);
                    }
                }
            }
        }

        // 步骤 3：独立生成判定（仅当该区块尚未被上面的扩散设置时）
        if (!data.hasVein()) {
            Random soloRng = makeRng(worldSeed, chunkX, chunkZ, 0xC0DE);
            if (soloRng.nextDouble() < SOLO_CHANCE) {
                assignVein(data, worldSeed, chunkX, chunkZ);
            }
        }
    }

    // ── 内部工具 ─────────────────────────────────────────────────

    /**
     * 以给定概率为区块设置矿脉。
     */
    private static void trySetVein(World world, Chunk chunk,
                                   long worldSeed, int cx, int cz, double prob) {
        ChunkVeinData data = getVeinData(chunk);
        if (data == null || data.hasVein()) return;

        Random probRng = makeRng(worldSeed, cx, cz, 0xBEEF);
        if (probRng.nextDouble() < prob) {
            assignVein(data, worldSeed, cx, cz);
        }
    }

    /**
     * 确定性地为区块分配矿脉类型和矿物列表。
     */
    private static void assignVein(ChunkVeinData data, long worldSeed, int cx, int cz) {
        List<VeinType> types = VeinRegistry.getList();
        if (types.isEmpty()) return;

        // 选矿脉类型
        Random typeRng = makeRng(worldSeed, cx, cz, 0xABCD);
        VeinType type = types.get(typeRng.nextInt(types.size()));

        // 从矿物池中随机抽 2~4 种
        List<OreEntry> pool = new ArrayList<>(type.getOrePool());
        if (pool.isEmpty()) return;

        Random pickRng = makeRng(worldSeed, cx, cz, 0x1234);
        Collections.shuffle(pool, pickRng);

        int count = type.getMinOreTypes()
                + pickRng.nextInt(type.getMaxOreTypes() - type.getMinOreTypes() + 1);
        count = Math.min(count, pool.size());

        // 为每种矿物分配随机权重（在原始权重基础上 ±30% 扰动）
        List<OreEntry> selected = new ArrayList<>();
        Random weightRng = makeRng(worldSeed, cx, cz, 0x5678);
        for (int i = 0; i < count; i++) {
            OreEntry base = pool.get(i);
            // 权重在 70%~130% 范围内随机扰动，保证正整数
            int w = Math.max(1,
                (int)(base.weight * (0.7 + weightRng.nextDouble() * 0.6)));
            selected.add(new OreEntry(base.oreName, w));
        }

        data.setVein(type.id, selected);
    }

    /**
     * 构造确定性 Random。
     * seed 混入坐标和一个用途标记，防止不同步骤产生相同序列。
     */
    private static Random makeRng(long worldSeed, int cx, int cz, long purpose) {
        long seed = worldSeed
                ^ ((long) cx * PRIME_X)
                ^ ((long) cz * PRIME_Z)
                ^ purpose;
        return new Random(seed);
    }

    // ── Capability 辅助 ──────────────────────────────────────────

    public static ChunkVeinData getVeinData(Chunk chunk) {
        VeinDataCapability.IVeinDataCapability cap =
                chunk.getCapability(VeinDataCapability.CAP, null);
        return cap != null ? cap.getData() : null;
    }
}
