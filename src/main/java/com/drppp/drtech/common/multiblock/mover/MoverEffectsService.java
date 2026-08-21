package com.drppp.drtech.common.multiblock.mover;

import com.drppp.drtech.DrtConfig;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;

/**
 * Bounded, server-authoritative audiovisual feedback for mover state changes.
 * Effects deliberately live outside the move transaction and never affect its outcome.
 */
public final class MoverEffectsService {
    private static final int MIN_SUCCESS_PARTICLES = 16;
    private static final int MAX_SUCCESS_PARTICLES = 64;

    private MoverEffectsService() {
    }

    public static void selection(EntityPlayerMP player, BlockPos controllerPos) {
        if (!DrtConfig.MultiblockMover.enableEffects) return;
        WorldServer world = player.getServerWorld();
        world.playSound(null, controllerPos, SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP,
                SoundCategory.BLOCKS, 0.55F, 1.35F);
        spawn(world, EnumParticleTypes.SPELL_WITCH, controllerPos, 12,
                0.45D, 0.45D, 0.45D, 0.02D);
    }

    public static void success(EntityPlayerMP player, BlockPos sourceController,
                               BlockPos targetController, int blockCount) {
        if (!DrtConfig.MultiblockMover.enableEffects) return;
        WorldServer world = player.getServerWorld();
        int count = Math.min(MAX_SUCCESS_PARTICLES,
                MIN_SUCCESS_PARTICLES + (int) Math.ceil(Math.sqrt(Math.max(1, blockCount)) * 2.0D));

        world.playSound(null, sourceController, SoundEvents.ENTITY_ENDERMEN_TELEPORT,
                SoundCategory.BLOCKS, 0.65F, 0.85F);
        if (!sourceController.equals(targetController)) {
            world.playSound(null, targetController, SoundEvents.ENTITY_ENDERMEN_TELEPORT,
                    SoundCategory.BLOCKS, 0.8F, 1.15F);
        }
        spawn(world, EnumParticleTypes.PORTAL, sourceController, count,
                0.6D, 0.6D, 0.6D, 0.08D);
        if (!sourceController.equals(targetController)) {
            spawn(world, EnumParticleTypes.PORTAL, targetController, count,
                    0.6D, 0.6D, 0.6D, 0.08D);
        }
    }

    public static void cancellation(EntityPlayerMP player, BlockPos controllerPos) {
        if (!DrtConfig.MultiblockMover.enableEffects) return;
        WorldServer world = player.getServerWorld();
        world.playSound(null, controllerPos, SoundEvents.BLOCK_NOTE_HAT,
                SoundCategory.BLOCKS, 0.45F, 0.7F);
        spawn(world, EnumParticleTypes.SMOKE_NORMAL, controllerPos, 8,
                0.35D, 0.35D, 0.35D, 0.01D);
    }

    private static void spawn(WorldServer world, EnumParticleTypes type, BlockPos pos,
                              int count, double xOffset, double yOffset,
                              double zOffset, double speed) {
        world.spawnParticle(type,
                pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                count, xOffset, yOffset, zOffset, speed);
    }
}
