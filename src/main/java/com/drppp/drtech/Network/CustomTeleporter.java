package com.drppp.drtech.Network;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.ITeleporter;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

public class CustomTeleporter implements ITeleporter {
    private final WorldServer world;
    private final BlockPos targetPos;

    public CustomTeleporter(WorldServer world, BlockPos targetPos) {
        this.world = world;
        this.targetPos = targetPos;
    }

    @Override
    public void placeEntity(World world, Entity entity, float yaw) {
        if (entity instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP) entity;

            // 从 y = 255 开始向下搜索非空气方块
            BlockPos safePos = findSafePosition(world, targetPos);

            // 设置玩家位置
            player.setPositionAndUpdate(safePos.getX() + 0.5, safePos.getY() + 1, safePos.getZ() + 0.5);

            // 确保区块加载
            world.getChunkProvider().provideChunk(safePos.getX() >> 4, safePos.getZ() >> 4);

            // 检查碰撞
            if (!world.getCollisionBoxes(player, player.getEntityBoundingBox()).isEmpty()) {
                player.setPositionAndUpdate(safePos.getX() + 0.5, safePos.getY() + 2, safePos.getZ() + 0.5);
            }

            // 触发传送事件
            MinecraftForge.EVENT_BUS.post(new PlayerEvent.PlayerChangedDimensionEvent(player, player.dimension, world.provider.getDimension()));
        }
    }

    private BlockPos findSafePosition(World world, BlockPos pos) {
        int minY = world.provider.isNether() ? 0 : 0;
        for (int y = 255; y >= minY; y--) {
            BlockPos currentPos = new BlockPos(pos.getX(), y, pos.getZ());
            IBlockState state = world.getBlockState(currentPos);

            if (!state.getBlock().isAir(state, world, currentPos)) {
                return currentPos.up();
            }
        }

        return pos;
    }
}