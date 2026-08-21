package com.drppp.drtech.api.multiblock.mover;

import com.drppp.drtech.DrTechMain;
import com.drppp.drtech.Tags;
import com.drppp.drtech.common.Items.MetaItems.DrMetaItems;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class MultiblockMovePermissionRegistry {
    public static final ResourceLocation VANILLA_PERMISSION =
            new ResourceLocation(Tags.MODID, "vanilla_world_edit");

    private static final Map<ResourceLocation, IMultiblockMovePermission> CHECKERS =
            new LinkedHashMap<>();
    private static boolean builtinsRegistered;

    private MultiblockMovePermissionRegistry() {
    }

    public static synchronized void register(ResourceLocation id,
                                             IMultiblockMovePermission checker) {
        if (id == null || checker == null) {
            throw new IllegalArgumentException("Permission checker id and instance are required");
        }
        ensureBuiltins();
        if (CHECKERS.containsKey(id)) {
            throw new IllegalArgumentException("Duplicate multiblock move permission checker: " + id);
        }
        CHECKERS.put(id, checker);
    }

    public static synchronized boolean canRemove(EntityPlayerMP player, WorldServer world,
                                                 BlockPos sourcePos) {
        ensureBuiltins();
        for (Map.Entry<ResourceLocation, IMultiblockMovePermission> entry : CHECKERS.entrySet()) {
            try {
                if (!entry.getValue().canRemove(player, world, sourcePos)) return false;
            } catch (Throwable error) {
                DrTechMain.LOGGER.error("Multiblock mover permission checker {} failed for removal at {}",
                        entry.getKey(), sourcePos, error);
                return false;
            }
        }
        return true;
    }

    public static synchronized boolean canPlace(EntityPlayerMP player, WorldServer world,
                                                BlockPos destinationPos) {
        ensureBuiltins();
        for (Map.Entry<ResourceLocation, IMultiblockMovePermission> entry : CHECKERS.entrySet()) {
            try {
                if (!entry.getValue().canPlace(player, world, destinationPos)) return false;
            } catch (Throwable error) {
                DrTechMain.LOGGER.error("Multiblock mover permission checker {} failed for placement at {}",
                        entry.getKey(), destinationPos, error);
                return false;
            }
        }
        return true;
    }

    public static synchronized Map<ResourceLocation, IMultiblockMovePermission> view() {
        ensureBuiltins();
        return Collections.unmodifiableMap(new LinkedHashMap<>(CHECKERS));
    }

    private static void ensureBuiltins() {
        if (builtinsRegistered) return;
        builtinsRegistered = true;
        CHECKERS.put(VANILLA_PERMISSION, new IMultiblockMovePermission() {
            @Override
            public boolean canRemove(EntityPlayerMP player, WorldServer world, BlockPos sourcePos) {
                return canEdit(player, world, sourcePos);
            }

            @Override
            public boolean canPlace(EntityPlayerMP player, WorldServer world, BlockPos destinationPos) {
                return canEdit(player, world, destinationPos);
            }

            private boolean canEdit(EntityPlayerMP player, WorldServer world, BlockPos pos) {
                ItemStack editingStack = player.getHeldItemMainhand();
                ItemStack offhand = player.getHeldItemOffhand();
                if (DrMetaItems.MULTIBLOCK_MOVER != null
                        && DrMetaItems.MULTIBLOCK_MOVER.isItemEqual(offhand)) {
                    editingStack = offhand;
                }
                return world.isBlockModifiable(player, pos)
                        && player.canPlayerEdit(pos, EnumFacing.UP, editingStack);
            }
        });
    }
}
