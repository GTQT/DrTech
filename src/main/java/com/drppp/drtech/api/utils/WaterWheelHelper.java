package com.drppp.drtech.api.Utils;

import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class WaterWheelHelper {

    /**
     * 检测水车周围的水，并计算流动水的总推力。
     *
     * @param world 世界对象
     * @param wheelPos 水车的位置（BlockPos）
     * @return 推力向量（Vec3d）
     */
    public static Vec3d calculateWaterFlowPush(World world, BlockPos wheelPos) {
        Vec3d totalForce = Vec3d.ZERO; // 初始化总推力向量

        // 遍历水车周围的所有水平方向
        for (EnumFacing direction : EnumFacing.Plane.HORIZONTAL) {
            BlockPos adjacentPos = wheelPos.offset(direction); // 获取水车周围的位置
            IBlockState blockState = world.getBlockState(adjacentPos); // 获取方块状态

            // 判断是否为水方块
            if (blockState.getMaterial().isLiquid()) {
                // 计算水的流动方向
                Vec3d flowVec = getWaterFlow(world, adjacentPos, blockState);

                // 将流动方向的向量添加到总推力中
                totalForce = totalForce.add(flowVec);
            }
        }

        return totalForce; // 返回总推力向量
    }

    /**
     * 计算指定水方块的流动方向。
     *
     * @param world 世界对象
     * @param pos 水方块的位置
     * @param state 水方块的状态
     * @return 流动方向向量（Vec3d）
     */
    private static Vec3d getWaterFlow(World world, BlockPos pos, IBlockState state) {
        double d0 = 0.0D;
        double d1 = 0.0D;
        double d2 = 0.0D;

        int level = state.getValue(BlockLiquid.LEVEL); // 获取水的 LEVEL 属性（0 表示源方块）
        BlockPos.PooledMutableBlockPos mutablePos = BlockPos.PooledMutableBlockPos.retain();

        for (EnumFacing direction : EnumFacing.Plane.HORIZONTAL) {
            mutablePos.setPos(pos).move(direction);
            IBlockState neighborState = world.getBlockState(mutablePos);

            if (neighborState.getMaterial().isLiquid()) {
                int neighborLevel = neighborState.getValue(BlockLiquid.LEVEL);

                // 根据水位差计算流动方向
                int levelDifference = neighborLevel - level;
                d0 += direction.getXOffset() * levelDifference;
                d1 += direction.getYOffset() * levelDifference;
                d2 += direction.getZOffset() * levelDifference;
            }
        }

        mutablePos.release();
        return new Vec3d(d0, d1, d2).normalize(); // 返回归一化后的流动方向
    }
}
