package com.drppp.drtech.common.multiblock.mover;

import com.drppp.drtech.DrTechMain;
import com.drppp.drtech.DrtConfig;
import com.drppp.drtech.Network.SyncInit;
import com.drppp.drtech.Network.mover.ClearMoverPreviewPacket;
import com.drppp.drtech.Network.mover.StartMoverPreviewPacket;
import com.drppp.drtech.common.Items.MetaItems.DrMetaItems;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class MoverSessionManager {
    public static final MoverSessionManager INSTANCE = new MoverSessionManager();
    private static final String ITEM_TAG = "DrTechMoverSession";
    private static boolean registered;

    private final Map<UUID, MoverSession> sessions = new ConcurrentHashMap<>();

    private MoverSessionManager() {
    }

    public static synchronized void init() {
        if (!registered) {
            MinecraftForge.EVENT_BUS.register(INSTANCE);
            registered = true;
        }
    }

    public boolean hasSession(EntityPlayerMP player) {
        return sessions.containsKey(player.getUniqueID());
    }

    public void select(EntityPlayerMP player, ItemStack mover, BlockPos controllerPos) {
        cancel(player, mover, false, null);
        try {
            long captureStarted = System.nanoTime();
            MultiblockSnapshot snapshot = MultiblockCaptureService.capture(player, controllerPos, true);
            long captureNanos = System.nanoTime() - captureStarted;
            UUID id = UUID.randomUUID();
            MoverSession session = new MoverSession(
                    id, player.getUniqueID(), snapshot, captureNanos);
            sessions.put(player.getUniqueID(), session);
            writeItemSession(mover, session);
            DrTechMain.LOGGER.info("Multiblock mover session {} selected by {} ({}) in dimension {} at {}; blocks={}, tileEntities={}",
                    id, player.getName(), player.getUniqueID(), snapshot.getDimension(),
                    snapshot.getControllerPos(), snapshot.getBlockCount(), snapshot.getTileEntityCount());

            sendPreview(player, session, snapshot.getBlocks());
            player.sendStatusMessage(new TextComponentTranslation(
                    "drtech.multiblock_mover.selected",
                    snapshot.getBlockCount(), snapshot.getTileEntityCount(),
                    MoverEnergyService.calculateCost(snapshot)), true);
            MoverEffectsService.selection(player, snapshot.getControllerPos());
        } catch (MoverException error) {
            DrTechMain.LOGGER.warn("Multiblock mover selection at {} rejected for {}: {}",
                    controllerPos, player.getName(), error.getTranslationKey());
            player.sendStatusMessage(new TextComponentTranslation(error.getTranslationKey()), true);
        }
    }

    public void rotate(EntityPlayerMP player, UUID requestedSession, int direction) {
        DrTechMain.LOGGER.info("Received multiblock mover rotation request from {}; session={}; direction={}",
                player.getName(), requestedSession, direction);
        if (!DrtConfig.MultiblockMover.enableRotation) {
            player.sendStatusMessage(new TextComponentTranslation(
                    "drtech.multiblock_mover.error.rotation_disabled"), true);
            return;
        }
        MoverSession session = sessions.get(player.getUniqueID());
        if (session == null || !session.getId().equals(requestedSession) || session.isCommitting()) {
            player.sendStatusMessage(new TextComponentTranslation(
                    "drtech.multiblock_mover.error.invalid_session"), true);
            return;
        }
        ItemStack mover = findMover(player, session);
        if (mover.isEmpty()) {
            cancel(player, ItemStack.EMPTY, true, "drtech.multiblock_mover.item_missing");
            return;
        }
        if (!itemMatchesSession(player.getHeldItemMainhand(), session)
                && !itemMatchesSession(player.getHeldItemOffhand(), session)) {
            player.sendStatusMessage(new TextComponentTranslation(
                    "drtech.multiblock_mover.error.invalid_session"), true);
            return;
        }
        long worldTick = player.world.getTotalWorldTime();
        if (!session.canRotateAt(worldTick)) return;
        MoverRotation candidate = session.getRotation().step(direction);
        try {
            List<MovingBlockSnapshot> transformed = MultiblockRotationService.rotate(
                    player, session.getSnapshot(), candidate);
            session.setRotation(candidate);
            session.markRotatedAt(worldTick);
            sendPreview(player, session, transformed);
            long energyCost = MoverEnergyService.calculateCost(
                    session.getSnapshot(), candidate);
            player.sendStatusMessage(new TextComponentTranslation(
                    "drtech.multiblock_mover.rotated", candidate.getDegrees(), energyCost), true);
        } catch (MoverException error) {
            DrTechMain.LOGGER.warn("Multiblock mover rotation request {} for {} rejected: {}",
                    requestedSession, player.getName(), error.getTranslationKey());
            player.sendStatusMessage(new TextComponentTranslation(error.getTranslationKey()), true);
        }
    }

    private static void sendPreview(EntityPlayerMP player, MoverSession session,
                                    List<MovingBlockSnapshot> transformed) {
        MultiblockSnapshot source = session.getSnapshot();
        List<PreviewBlockData> preview = new ArrayList<>(source.getBlockCount());
        for (int i = 0; i < source.getBlockCount(); i++) {
            preview.add(PreviewBlockData.fromSnapshots(
                    source.getBlocks().get(i), transformed.get(i)));
        }
        SyncInit.NETWORK.sendTo(new StartMoverPreviewPacket(
                session.getId(), source.getDimension(), source.getControllerPos(),
                session.getRotation(), preview), player);
    }

    public void confirm(EntityPlayerMP player, ItemStack mover, BlockPos targetControllerPos) {
        MoverSession session = sessions.get(player.getUniqueID());
        if (session == null) {
            player.sendStatusMessage(new TextComponentTranslation(
                    "drtech.multiblock_mover.error.no_session"), true);
            clearItemSession(mover);
            return;
        }
        if (session.isCommitting() || !itemMatchesSession(mover, session)) {
            player.sendStatusMessage(new TextComponentTranslation(
                    "drtech.multiblock_mover.error.invalid_session"), true);
            cancel(player, mover, true, null);
            return;
        }

        long energyCost = MoverEnergyService.calculateCost(
                session.getSnapshot(), session.getRotation());
        if (!MoverEnergyService.canConsume(player, mover, energyCost)) {
            player.sendStatusMessage(new TextComponentTranslation(
                    "drtech.multiblock_mover.error.no_energy", energyCost), true);
            return;
        }

        session.setCommitting(true);
        try {
            DrTechMain.LOGGER.info("Multiblock mover session {} committing for {} from {} to {} with rotation {} degrees",
                    session.getId(), player.getName(), session.getSnapshot().getControllerPos(),
                    targetControllerPos, session.getRotation().getDegrees());
            int moved = MultiblockMoveTransaction.move(
                    player, session, targetControllerPos, mover, energyCost);
            DrTechMain.LOGGER.info("Multiblock mover session {} completed; moved {} blocks; consumed {} EU",
                    session.getId(), moved,
                    player.capabilities.isCreativeMode ? 0L : energyCost);
            player.sendStatusMessage(new TextComponentTranslation(
                    "drtech.multiblock_mover.success", moved), true);
            MoverEffectsService.success(player, session.getSnapshot().getControllerPos(),
                    targetControllerPos, moved);
            cancel(player, mover, true, null);
        } catch (MoverException error) {
            session.setCommitting(false);
            DrTechMain.LOGGER.warn("Multiblock mover session {} rejected for {}: {}",
                    session.getId(), player.getName(), error.getTranslationKey());
            player.sendStatusMessage(new TextComponentTranslation(error.getTranslationKey()), true);
            if ("drtech.multiblock_mover.error.source_changed".equals(error.getTranslationKey())
                    || "drtech.multiblock_mover.error.move_failed".equals(error.getTranslationKey())
                    || "drtech.multiblock_mover.error.rollback_failed".equals(error.getTranslationKey())) {
                cancel(player, mover, true, null);
            }
        }
    }

    public void cancel(EntityPlayerMP player, ItemStack mover, boolean notifyClient,
                       String messageKey) {
        MoverSession removed = sessions.remove(player.getUniqueID());
        if (removed != null) {
            clearItemSession(findMover(player, removed));
        }
        clearItemSession(mover);
        if (removed != null && notifyClient) {
            SyncInit.NETWORK.sendTo(new ClearMoverPreviewPacket(removed.getId()), player);
        }
        if (removed != null && "drtech.multiblock_mover.cancelled".equals(messageKey)) {
            MoverEffectsService.cancellation(player, removed.getSnapshot().getControllerPos());
        }
        if (messageKey != null) {
            player.sendStatusMessage(new TextComponentTranslation(messageKey), true);
        }
    }

    private static void writeItemSession(ItemStack stack, MoverSession session) {
        NBTTagCompound tag = stack.getOrCreateSubCompound(ITEM_TAG);
        tag.setUniqueId("Id", session.getId());
        tag.setInteger("Dimension", session.getSnapshot().getDimension());
        tag.setLong("Controller", session.getSnapshot().getControllerPos().toLong());
    }

    private static boolean itemMatchesSession(ItemStack stack, MoverSession session) {
        if (DrMetaItems.MULTIBLOCK_MOVER == null
                || !DrMetaItems.MULTIBLOCK_MOVER.isItemEqual(stack)) return false;
        NBTTagCompound root = stack.getTagCompound();
        if (root == null || !root.hasKey(ITEM_TAG, 10)) return false;
        NBTTagCompound tag = root.getCompoundTag(ITEM_TAG);
        return tag.hasUniqueId("Id") && session.getId().equals(tag.getUniqueId("Id"));
    }

    private static void clearItemSession(ItemStack stack) {
        if (stack == null || stack.isEmpty() || stack.getTagCompound() == null) return;
        stack.getTagCompound().removeTag(ITEM_TAG);
        if (stack.getTagCompound().getKeySet().isEmpty()) stack.setTagCompound(null);
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.world.isRemote
                || !(event.player instanceof EntityPlayerMP) || event.player.ticksExisted % 20 != 0) {
            return;
        }
        EntityPlayerMP player = (EntityPlayerMP) event.player;
        MoverSession session = sessions.get(player.getUniqueID());
        if (session == null) return;
        ItemStack boundMover = findMover(player, session);
        if (boundMover.isEmpty()) {
            DrTechMain.LOGGER.info("Multiblock mover session {} cancelled because {} no longer carries the bound item",
                    session.getId(), player.getName());
            cancel(player, ItemStack.EMPTY, true,
                    "drtech.multiblock_mover.item_missing");
            return;
        }
        long timeoutMillis = DrtConfig.MultiblockMover.sessionTimeoutTicks * 50L;
        if (System.currentTimeMillis() - session.getCreatedAt() >= timeoutMillis) {
            cancel(player, boundMover, true,
                    "drtech.multiblock_mover.expired");
        }
    }

    @SubscribeEvent
    public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.player instanceof EntityPlayerMP) {
            cancelForLifecycle((EntityPlayerMP) event.player, "logout");
        }
    }

    @SubscribeEvent
    public void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.player instanceof EntityPlayerMP) {
            cancelForLifecycle((EntityPlayerMP) event.player, "dimension change");
        }
    }

    @SubscribeEvent
    public void onPlayerDeath(LivingDeathEvent event) {
        if (event.getEntityLiving() instanceof EntityPlayerMP) {
            cancelForLifecycle((EntityPlayerMP) event.getEntityLiving(), "player death");
        }
    }

    private void cancelForLifecycle(EntityPlayerMP player, String reason) {
        MoverSession session = sessions.get(player.getUniqueID());
        if (session == null) return;
        DrTechMain.LOGGER.info("Multiblock mover session {} cancelled due to {} for {}",
                session.getId(), reason, player.getName());
        cancel(player, findMover(player, session), true, null);
    }

    private static ItemStack findMover(EntityPlayerMP player, MoverSession session) {
        for (ItemStack stack : player.inventory.mainInventory) {
            if (itemMatchesSession(stack, session)) return stack;
        }
        for (ItemStack stack : player.inventory.offHandInventory) {
            if (itemMatchesSession(stack, session)) return stack;
        }
        return ItemStack.EMPTY;
    }
}
