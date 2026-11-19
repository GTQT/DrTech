package com.meowmel.cropQT.block;

import com.drppp.drtech.DrTechMain;
import com.meowmel.cropQT.api.capability.FarmType;
import gregtech.api.block.VariantBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 工业农场的升级组件方块（环境强化 / 生长加速 / 肥料 / 高级收割 / 超频生长加速）。
 *
 * <p>照源端：这五类升级是<b>方块</b>，不是机器仓。结构里摆的是哪一档的方块，
 * 农场就记哪一档；同一座农场里所有种子床与升级组件必须同档，否则停机。
 * 每类方块的 meta 与 {@link BlockSeedBed.SeedBedType} 一一对应（MV..OpV 共 12 档），
 * 升级类型由方块本身区分，不再需要额外的 MetaTileEntity。
 *
 * <p>数量与耗电系数来自 {@link FarmType}（照搬源端常量）：
 * 环境强化最多 2、肥料最多 1、高级收割最多 2、超频最多 1、生长加速不限。
 */
public class BlockFarmUpgrade extends VariantBlock<BlockSeedBed.SeedBedType> {

    /** 方块 → 升级类型，结构匹配时用。 */
    private static final Map<BlockFarmUpgrade, FarmType> BY_BLOCK = new LinkedHashMap<>();

    private final FarmType farmType;

    public BlockFarmUpgrade(@NotNull FarmType farmType) {
        super(Material.IRON);
        this.farmType = farmType;
        setTranslationKey("drtech." + farmType.getName());
        setHardness(3.0f);
        setResistance(6.0f);
        setSoundType(SoundType.METAL);
        setHarvestLevel("wrench", 2);
        setDefaultState(getState(BlockSeedBed.SeedBedType.values()[0]));
        setRegistryName(farmType.getName());
        setCreativeTab(DrTechMain.DrTechTab);
        BY_BLOCK.put(this, farmType);
    }

    public FarmType getFarmType() {
        return farmType;
    }

    @Override
    public boolean canCreatureSpawn(@Nonnull IBlockState state, @Nonnull IBlockAccess world,
                                    @Nonnull BlockPos pos, @Nonnull EntityLiving.SpawnPlacementType type) {
        return false;
    }

    /** 方块对应的升级类型；不是升级方块则返回 {@code null}。 */
    public static FarmType getFarmType(IBlockState state) {
        return state == null ? null : BY_BLOCK.get(state.getBlock());
    }

    /** 方块状态对应的 GT 档位（MV..OpV）；不是升级方块则返回 -1。 */
    public static int getTier(IBlockState state) {
        if (!(state.getBlock() instanceof BlockFarmUpgrade upgrade)) {
            return -1;
        }
        return upgrade.getState(state).getTier();
    }

    /** 物品栈对应的档位；不是升级方块则返回 -1。 */
    public static int getTier(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !(stack.getItem().getRegistryName() != null)) {
            return -1;
        }
        if (!(net.minecraft.block.Block.getBlockFromItem(stack.getItem()) instanceof BlockFarmUpgrade upgrade)) {
            return -1;
        }
        return upgrade.getState(stack).getTier();
    }

    public static Map<BlockFarmUpgrade, FarmType> all() {
        return Collections.unmodifiableMap(BY_BLOCK);
    }
}
