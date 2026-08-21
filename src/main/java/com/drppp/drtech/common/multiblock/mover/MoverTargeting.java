package com.drppp.drtech.common.multiblock.mover;

import com.drppp.drtech.DrtConfig;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;

/** Shared client/server targeting rules for mover preview and confirmation. */
public final class MoverTargeting {
    private MoverTargeting() {
    }

    public static BlockPos resolve(Entity player, RayTraceResult hit, float partialTicks) {
        if (hit != null && hit.typeOfHit == RayTraceResult.Type.BLOCK
                && hit.getBlockPos() != null && hit.sideHit != null) {
            return hit.getBlockPos().offset(hit.sideHit);
        }
        Vec3d eyes = player.getPositionEyes(partialTicks);
        Vec3d look = player.getLook(partialTicks);
        double distance = Math.max(1, DrtConfig.MultiblockMover.airTargetDistance);
        return new BlockPos(eyes.add(look.scale(distance)));
    }
}
