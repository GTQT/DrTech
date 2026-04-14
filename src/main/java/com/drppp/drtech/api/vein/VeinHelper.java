package com.drppp.drtech.api.vein;


import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.*;

/**
 * 机器（TileEntity）查询矿脉数据的统一入口。
 *
 * <h3>使用示例</h3>
 * <pre>
 * // 在 TileEntity.update() 中：
 * ChunkVeinData vein = VeinHelper.getVeinData(world, pos);
 * if (vein != null && vein.hasVein()) {
 *     // 按权重随机采集一个矿物
 *     int rand = world.rand.nextInt(vein.getTotalWeight());
 *     OreEntry ore = vein.pickOre(rand);
 *     // ore.oreName 即矿物名
 * }
 * </pre>
 */
public final class VeinHelper {

    private VeinHelper() {}

    // ── 查询 ─────────────────────────────────────────────────────

    /**
     * 获取指定坐标所在区块的矿脉数据。
     *
     * @param world 世界（服务端）
     * @param pos   任意方块坐标，会自动换算到对应区块
     * @return 该区块的 {@link ChunkVeinData}，若 Capability 未挂载则返回 null
     */
    @Nullable
    public static ChunkVeinData getVeinData(World world, BlockPos pos) {
        return getVeinDataByChunk(world, pos.getX() >> 4, pos.getZ() >> 4);
    }

    /**
     * 按区块坐标直接获取矿脉数据。
     */
    @Nullable
    public static ChunkVeinData getVeinDataByChunk(World world, int chunkX, int chunkZ) {
        if (world.isRemote) return null;

        Chunk chunk = world.getChunk(chunkX, chunkZ);
        return WorldVeinGenerator.getVeinData(chunk);
    }

    /**
     * 该坐标所在区块是否存在资源点（快捷方法）。
     */
    public static boolean hasVein(World world, BlockPos pos) {
        ChunkVeinData data = getVeinData(world, pos);
        return data != null && data.hasVein();
    }

    // ── 钻机绑定 / 解绑 ──────────────────────────────────────────

    /**
     * 尝试将钻机绑定到目标区块的资源点。
     *
     * <p>绑定条件：
     * <ul>
     *   <li>目标区块存在资源点（{@code hasVein == true}）</li>
     *   <li>资源点尚未被其他钻机占用（{@code !isBound()}）</li>
     * </ul>
     *
     * @param world    世界
     * @param drillPos 钻机自身的 BlockPos（用作绑定凭证）
     * @param targetPos 钻机正下方对应区块的任意坐标（通常就是 drillPos）
     * @return true 表示绑定成功
     */
    public static boolean tryBind(World world, BlockPos drillPos, BlockPos targetPos) {
        ChunkVeinData data = getVeinData(world, targetPos);
        if (data == null || !data.hasVein() || data.isBound()) return false;
        data.setBoundDrillPos(drillPos.toLong());
        markChunkDirty(world, targetPos);
        return true;
    }

    /**
     * 解除绑定（钻机被破坏时调用）。
     *
     * @param world    世界
     * @param drillPos 钻机自身的 BlockPos
     * @param targetPos 目标区块坐标
     */
    public static void unbind(World world, BlockPos drillPos, BlockPos targetPos) {
        ChunkVeinData data = getVeinData(world, targetPos);
        if (data == null) return;
        if (data.getBoundDrillPos() == drillPos.toLong()) {
            data.clearBoundDrill();
            markChunkDirty(world, targetPos);
        }
    }

    /**
     * 验证某个钻机是否仍然是该资源点的合法占用者。
     */
    public static boolean isBoundBy(World world, BlockPos drillPos, BlockPos targetPos) {
        ChunkVeinData data = getVeinData(world, targetPos);
        return data != null && data.isBound()
                && data.getBoundDrillPos() == drillPos.toLong();
    }

    // ── 内部 ────────────────────────────────────────────────────

    /**
     * 标记 Chunk 为已修改，确保 NBT 数据被保存到磁盘。
     */
    private static void markChunkDirty(World world, BlockPos pos) {
        // setChunkModified 会使 Chunk 在下次保存时写入磁盘
        world.getChunk(pos).markDirty();
    }

    // ── 探测器扫描 ───────────────────────────────────────────────

    /**
     * 以给定坐标所在区块为中心，扫描 (2r+1)×(2r+1) 范围内是否存在资源点。
     * 默认 radius=3，即 7×7 区块。
     *
     * @param world  世界
     * @param center 探测器所在坐标
     * @param radius 扫描半径（区块数）
     * @return 扫描范围内资源点数量
     */
    public static int scanVeinsAround(World world, BlockPos center, int radius) {
        if (world.isRemote) return 0;
        int cx = center.getX() >> 4;
        int cz = center.getZ() >> 4;
        int count = 0;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                ChunkVeinData data = getVeinDataByChunk(world, cx + dx, cz + dz);
                if (data != null && data.hasVein()) count++;
            }
        }
        return count;
    }
    /**
     * 将 oreName（如 "minecraft:iron_ore"）转为 ItemStack。
     * 支持带 metadata 的格式："minecraft:stone:1"（石头变种）
     *
     * @return 对应 ItemStack，解析失败返回 ItemStack.EMPTY
     */
    public static ItemStack oreNameToItemStack(String oreName) {
        String[] parts = oreName.split(":");

        int meta = 0;
        String domain, path;

        if (parts.length == 3) {
            domain = parts[0];
            path   = parts[1];
            try { meta = Integer.parseInt(parts[2]); }
            catch (NumberFormatException e) { meta = 0; }
        } else if (parts.length == 2) {
            domain = parts[0];
            path   = parts[1];
        } else {
            return ItemStack.EMPTY;
        }

        ResourceLocation rl = new ResourceLocation(domain, path);

        // 优先查 Block（矿石一般都是 Block）
        Block block = ForgeRegistries.BLOCKS.getValue(rl);
        if (block != null && block != Blocks.AIR) {
            Item item = Item.getItemFromBlock(block);
            if (item != Items.AIR) {
                return new ItemStack(item, 1, meta);
            }
        }

        // 备选：直接查 Item（某些东西只有 Item 没有 Block）
        Item item = ForgeRegistries.ITEMS.getValue(rl);
        if (item != null && item != Items.AIR) {
            return new ItemStack(item, 1, meta);
        }

        return ItemStack.EMPTY;
    }
    /**
     * 按权重随机抽取矿物，返回 List<ItemStack>。
     *
     * @param data       区块矿脉数据
     * @param count      本次抽取总数（钻机每周期产出量）
     * @param rand       随机源，传入 world.rand 保证可重现
     * @return 按矿物归并后的 ItemStack 列表（相同矿物合并为一个 stack）
     */
    public static List<ItemStack> extractOres(ChunkVeinData data, int count, Random rand) {
        if (data == null || !data.hasVein() || data.getTotalWeight() <= 0 || count <= 0) {
            return Collections.emptyList();
        }

        // 先统计每种矿物抽中几次，避免产生大量单个 ItemStack
        Map<String, Integer> tally = new LinkedHashMap<>();
        int totalWeight = data.getTotalWeight();

        for (int i = 0; i < count; i++) {
            OreEntry ore = data.pickOre(rand.nextInt(totalWeight));
            if (ore != null) {
                tally.merge(ore.oreName, 1, Integer::sum);
            }
        }

        // 将统计结果转为 ItemStack 列表，超过堆叠上限则拆分
        List<ItemStack> result = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : tally.entrySet()) {
            ItemStack template = oreNameToItemStack(entry.getKey());
            if (template.isEmpty()) continue;

            int remaining = entry.getValue();
            int maxStack  = template.getMaxStackSize();

            while (remaining > 0) {
                int batch = Math.min(remaining, maxStack);
                ItemStack stack = template.copy();
                stack.setCount(batch);
                result.add(stack);
                remaining -= batch;
            }
        }

        return result;
    }
}
