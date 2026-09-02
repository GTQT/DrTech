package com.drppp.drtech.common.MetaTileEntities.muti.electric.store;

import gregtech.api.pattern.StructureContributionKey;
import gregtech.api.pattern.StructureEvaluationContext;
import gregtech.api.pattern.StructureMatchCollector;
import gregtech.api.pattern.element.ITypedStructureElement;
import gregtech.api.pattern.element.StructureElementCapability;
import gregtech.api.pattern.element.StructureElementPreview;
import gregtech.api.util.BlockInfo;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 结构自定义元素：匹配一组方块（如储能单元外壳）并把每个匹配格的"部件数据"
 * 贡献到结构聚合中（StructureContributionKey.orderedList），成型后经
 * FormedStructureView.getAggregate(key) 读取。
 *
 * 替代旧版"自定义 TraceabilityPredicate + PatternMatchContext 计数"方案，
 * 供 TFFT / YotTank 等多方块使用。
 */
public class BatteryContributionElement<T> implements ITypedStructureElement<Object> {

    @NotNull
    private final Function<IBlockState, T> extractor;
    @NotNull
    private final StructureContributionKey<T, List<T>> key;
    @Nullable
    private final Supplier<BlockInfo[]> candidates;
    @NotNull
    private final StructureElementPreview preview;

    /**
     * @param id         聚合键 id（全局唯一稳定字符串）
     * @param extractor  从方块状态提取部件数据；不匹配返回 null
     * @param candidates 预览/自动建造候选块
     */
    public BatteryContributionElement(@NotNull String id,
                                      @NotNull Function<IBlockState, T> extractor,
                                      @Nullable Supplier<BlockInfo[]> candidates) {
        this.extractor = extractor;
        this.key = StructureContributionKey.orderedList(id);
        this.candidates = candidates;
        this.preview = candidates == null
                ? StructureElementPreview.empty()
                : StructureElementPreview.of(candidates);
    }

    @NotNull
    public StructureContributionKey<T, List<T>> getKey() {
        return key;
    }

    @Override
    public boolean check(@NotNull StructureEvaluationContext<Object> context) {
        T part = extractor.apply(context.getBlockState());
        if (part == null) {
            return false;
        }
        // 仅成型检查（收集阶段）时贡献一次该格部件
        if (context.collectsFormationState()) {
            StructureMatchCollector collector = context.getCollector();
            if (collector != null) {
                collector.emit(key, part);
            }
        }
        return true;
    }

    @Override
    public Set<StructureElementCapability> getCapabilities() {
        return StructureElementCapability.snapshotSafe();
    }

    @Override
    public BlockInfo[] getCandidates() {
        return candidates == null ? new BlockInfo[0] : candidates.get();
    }

    @NotNull
    @Override
    public StructureElementPreview getPreview() {
        return preview;
    }

    @Override
    public boolean placeBlock(@NotNull StructureEvaluationContext<Object> context,
                              @NotNull EntityPlayer player) {
        World world = context.getWorld();
        if (world == null) {
            return false;
        }
        BlockInfo[] infos = getCandidates();
        if (infos.length == 0 || infos[0] == null || infos[0].getBlockState() == null) {
            return false;
        }
        world.setBlockState(context.getPos(), infos[0].getBlockState());
        return true;
    }
}
