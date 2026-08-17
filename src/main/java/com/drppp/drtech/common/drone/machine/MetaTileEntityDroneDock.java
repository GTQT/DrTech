package com.drppp.drtech.common.drone.machine;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.ColourMultiplier;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.utils.serialization.ByteBufAdapters;
import com.cleanroommc.modularui.value.sync.GenericSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.SyncHandlers;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;
import com.cleanroommc.modularui.widgets.textfield.TextFieldWidget;
import com.drppp.drtech.common.drone.energy.DroneEnergyStorage;
import com.drppp.drtech.common.drone.entity.EntityProgrammableDrone;
import com.drppp.drtech.common.drone.item.DroneItemData;
import com.drppp.drtech.common.drone.item.ItemDroneProgramCard;
import com.drppp.drtech.common.drone.item.ItemProgrammableDrone;
import com.drppp.drtech.common.drone.network.DroneDockNetwork;
import com.drppp.drtech.common.drone.network.DroneDockRecord;
import com.drppp.drtech.common.drone.sound.DroneSoundCue;
import com.drppp.drtech.common.drone.sound.DroneSoundPlayer;
import gregtech.api.GTValues;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IElectricItem;
import gregtech.api.items.itemhandlers.GTItemStackHandler;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.TieredMetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.mui.GTGuis;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.texture.Textures;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.apache.commons.lang3.ArrayUtils;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/** Tiered EU endpoint, program deployer and persistent home for programmable drones. */
public final class MetaTileEntityDroneDock extends TieredMetaTileEntity {

    private static final String CONTROL_ACTION = "dock_control";
    private static final int DRONE_SLOT = 0;
    private static final int PROGRAM_SLOT = 1;

    private final UUID initialDockId = UUID.randomUUID();
    private UUID dockId = initialDockId;
    private UUID ownerId;
    private UUID currentDroneId;
    private String dockName = "Drone Dock";
    private boolean enabled = true;
    private boolean autoLaunch;
    private boolean autoRecover = true;
    private boolean pendingResume;
    private int priority;
    /** 0: ignore redstone, 1: require powered, 2: require unpowered. */
    private int redstoneMode;

    /**
     * UI snapshots.  The dock logic always uses the server fields above; these copies only exist so the client can
     * show the real, current control state instead of a misleading static pair of toggle buttons.
     */
    private boolean clientDockEnabled = true;
    private boolean clientAutoLaunch;
    private boolean clientLaunchReady;
    private int clientPriority;
    private boolean clientRedstonePowered;
    private boolean clientRedstoneUnpowered;

    /** Compatibility constructor retained for the original fixed ID 901 HV dock. */
    public MetaTileEntityDroneDock(ResourceLocation metaTileEntityId) {
        this(metaTileEntityId, GTValues.HV);
    }

    public MetaTileEntityDroneDock(ResourceLocation metaTileEntityId, int tier) {
        super(metaTileEntityId, tier);
    }

    @Override
    protected IItemHandlerModifiable createImportItemHandler() {
        return new GTItemStackHandler(this, 2) {
            @Override
            public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
                if (slot == DRONE_SLOT && !(stack.getItem() instanceof ItemProgrammableDrone)) return stack;
                if (slot == PROGRAM_SLOT && !(stack.getItem() instanceof ItemDroneProgramCard)) return stack;
                return super.insertItem(slot, stack, simulate);
            }
        };
    }

    @Override
    public void update() {
        super.update();
        if (getWorld() == null || getWorld().isRemote) return;
        if (getWorld().getTotalWorldTime() % 20L == 0L) updateNetworkHeartbeat();
        emitDockVisuals();
        deployProgramToStoredDrone();
        chargeStoredDrone();
        if ((pendingResume || autoLaunch) && canLaunchStoredDrone()) tryLaunchStoredDrone(null);
    }

    private void deployProgramToStoredDrone() {
        ItemStack drone = importItems.getStackInSlot(DRONE_SLOT);
        ItemStack card = importItems.getStackInSlot(PROGRAM_SLOT);
        if (drone.isEmpty() || card.isEmpty()) return;
        NBTTagCompound cardProgram = DroneItemData.getProgram(card);
        if (cardProgram == null) return;
        NBTTagCompound installed = DroneItemData.getProgram(drone);
        if (!cardProgram.equals(installed)) {
            DroneItemData.setProgram(drone, cardProgram);
            markDirty();
        }
    }

    private void chargeStoredDrone() {
        ItemStack stack = importItems.getStackInSlot(DRONE_SLOT);
        if (stack.isEmpty() || energyContainer.getEnergyStored() <= 0L) return;
        IElectricItem electricItem = stack.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
        if (electricItem == null) return;
        long offered = Math.min(energyContainer.getEnergyStored(),
                Math.min(electricItem.getTransferLimit(), GTValues.V[getTier()]));
        long accepted = electricItem.charge(offered, getTier(), false, true);
        if (accepted > 0L && energyContainer.getEnergyStored() >= accepted) {
            energyContainer.removeEnergy(accepted);
            electricItem.charge(accepted, getTier(), false, false);
            DroneSoundPlayer.play(getWorld(), dockId, DroneSoundCue.EU_CHARGE,
                    getPos().getX() + 0.5D, getPos().getY() + 0.7D, getPos().getZ() + 0.5D,
                    0.22F, 1.6F);
            markDirty();
        }
    }

    private boolean canLaunchStoredDrone() {
        ItemStack stack = importItems.getStackInSlot(DRONE_SLOT);
        if (!enabled || !isRedstoneAllowed() || stack.isEmpty()) return false;
        IElectricItem electricItem = stack.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
        if (electricItem == null || electricItem.getMaxCharge() <= 0L) return false;
        return electricItem.getCharge() * 100L / electricItem.getMaxCharge() >= 90L;
    }

    public boolean tryLaunchStoredDrone(@Nullable EntityPlayer requester) {
        if (getWorld() == null || getWorld().isRemote) return false;
        if (!enabled) return failLaunch(requester, "drtech.drone.dock.launch.disabled");
        if (!canControl(requester)) return failLaunch(requester, "drtech.drone.dock.launch.denied");
        ItemStack stored = importItems.getStackInSlot(DRONE_SLOT);
        if (stored.isEmpty()) return failLaunch(requester, "drtech.drone.dock.launch.empty");
        if (!canLaunchStoredDrone()) return failLaunch(requester, "drtech.drone.dock.launch.charging");
        ItemStack deployed = stored.copy();
        UUID itemOwner = DroneItemData.getOwnerId(deployed);
        if (ownerId == null) ownerId = itemOwner != null ? itemOwner
                : requester == null ? null : requester.getUniqueID();
        if (ownerId == null || itemOwner != null && !ownerId.equals(itemOwner)) {
            return failLaunch(requester, "drtech.drone.dock.launch.denied");
        }
        DroneItemData.migrateInPlace(deployed, ownerId);
        DroneItemData.setDock(deployed, getPos(), getWorld().provider.getDimension());
        EntityProgrammableDrone drone = new EntityProgrammableDrone(getWorld());
        drone.setPosition(getPos().getX() + 0.5D, getPos().getY() + 1.35D, getPos().getZ() + 0.5D);
        drone.initializeFromItem(deployed, ownerId);
        drone.prepareDockLaunch(getPos());
        if (!getWorld().spawnEntity(drone)) {
            return failLaunch(requester, "drtech.drone.dock.launch.spawn_failed");
        }
        importItems.setStackInSlot(DRONE_SLOT, ItemStack.EMPTY);
        currentDroneId = drone.getDroneId();
        pendingResume = false;
        markDirty();
        updateNetworkHeartbeat();
        notifyPlayer(requester, "drtech.drone.dock.launch.success");
        return true;
    }

    private boolean failLaunch(@Nullable EntityPlayer requester, String translationKey) {
        notifyPlayer(requester, translationKey);
        return false;
    }

    private static void notifyPlayer(@Nullable EntityPlayer requester, String translationKey) {
        if (requester != null) requester.sendStatusMessage(new TextComponentTranslation(translationKey), true);
    }

    public boolean acceptRecoveredDrone(EntityProgrammableDrone drone, boolean resumeAfterCharge) {
        if (getWorld() == null || getWorld().isRemote || drone == null || !autoRecover
                || !drone.isBoundToDock(getPos()) || !drone.canBeRecoveredByDock()) return false;
        UUID droneOwner = drone.getOwnerId();
        if (ownerId != null && !ownerId.equals(droneOwner)) return false;
        if (ownerId == null) ownerId = droneOwner;
        ItemStack recovered = drone.createDockRecoveryItem();
        if (!importItems.insertItem(DRONE_SLOT, recovered, true).isEmpty()) return false;
        if (!importItems.insertItem(DRONE_SLOT, recovered, false).isEmpty()) return false;
        pendingResume = resumeAfterCharge;
        currentDroneId = null;
        DroneSoundPlayer.play(getWorld(), dockId, DroneSoundCue.DOCK_LOCK,
                getPos().getX() + 0.5D, getPos().getY() + 0.8D, getPos().getZ() + 0.5D,
                0.8F, 1.0F);
        drone.completeDockRecovery();
        markDirty();
        updateNetworkHeartbeat();
        return true;
    }

    /** Transfers at most one tier packet per tick without exposing an EU cable capability on the entity. */
    public long chargeDeployedDrone(DroneEnergyStorage droneEnergy) {
        if (getWorld() == null || getWorld().isRemote || !enabled || droneEnergy == null
                || energyContainer.getEnergyStored() <= 0L) return 0L;
        long offered = Math.min(energyContainer.getEnergyStored(), GTValues.V[getTier()]);
        long accepted = droneEnergy.insert(offered, getTier(), true);
        if (accepted <= 0L || energyContainer.getEnergyStored() < accepted) return 0L;
        energyContainer.removeEnergy(accepted);
        long inserted = droneEnergy.insert(accepted, getTier(), false);
        if (inserted > 0L) {
            DroneSoundPlayer.play(getWorld(), dockId, DroneSoundCue.EU_CHARGE,
                    getPos().getX() + 0.5D, getPos().getY() + 1.0D, getPos().getZ() + 0.5D,
                    0.22F, 1.7F);
            markDirty();
        }
        return inserted;
    }

    private void emitDockVisuals() {
        if (!(getWorld() instanceof WorldServer) || getPos() == null) return;
        long time = getWorld().getTotalWorldTime();
        if (time % 10L != 0L || getWorld().getClosestPlayer(
                getPos().getX() + 0.5D, getPos().getY() + 0.5D, getPos().getZ() + 0.5D,
                64.0D, false) == null) return;
        WorldServer server = (WorldServer) getWorld();
        double red = enabled && isRedstoneAllowed() ? 0.12D : 1.0D;
        double green = enabled && isRedstoneAllowed() ? 1.0D : 0.08D;
        for (int x = 0; x <= 1; x++) for (int z = 0; z <= 1; z++) {
            server.spawnParticle(EnumParticleTypes.SPELL_MOB,
                    getPos().getX() + 0.18D + x * 0.64D, getPos().getY() + 1.02D,
                    getPos().getZ() + 0.18D + z * 0.64D, 0, red, green, 0.08D, 1.0D);
        }
        if (time % 20L == 0L) {
            server.spawnParticle(EnumParticleTypes.END_ROD, getPos().getX() + 0.5D,
                    getPos().getY() + 1.25D, getPos().getZ() + 0.5D,
                    2, 0.04D, 0.45D, 0.04D, 0.01D);
            ItemStack stored = importItems.getStackInSlot(DRONE_SLOT);
            IElectricItem electric = stored.isEmpty() ? null
                    : stored.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
            if (electric != null && electric.getMaxCharge() > 0L) {
                double progress = Math.max(0.0D, Math.min(1.0D,
                        electric.getCharge() / (double) electric.getMaxCharge()));
                server.spawnParticle(EnumParticleTypes.SPELL_MOB,
                        getPos().getX() + 0.5D, getPos().getY() + 0.25D + progress * 0.7D,
                        getPos().getZ() + 0.5D, 0, 0.1D, 0.45D + progress * 0.55D, 1.0D, 1.0D);
            }
        }
    }

    public boolean requestBoundDroneRecall(EntityPlayer requester) {
        if (getWorld() == null || getWorld().isRemote || !isRedstoneAllowed() || !canControl(requester)) return false;
        int radius = 128 << Math.max(0, getTier() - GTValues.HV);
        AxisAlignedBB search = new AxisAlignedBB(getPos()).grow(radius);
        List<EntityProgrammableDrone> drones = getWorld().getEntitiesWithinAABB(
                EntityProgrammableDrone.class, search);
        for (EntityProgrammableDrone drone : drones) {
            if (drone.isBoundToDock(getPos()) && drone.isOwner(requester)) {
                if (!drone.requestManualRecall()) continue;
                currentDroneId = drone.getDroneId();
                markDirty();
                notifyPlayer(requester, "drtech.drone.dock.recall.success");
                return true;
            }
        }
        notifyPlayer(requester, "drtech.drone.dock.recall.not_found");
        return false;
    }

    public boolean tryReserveDrone(UUID droneId, @Nullable UUID droneOwner) {
        if (!enabled || droneId == null || ownerId != null && !ownerId.equals(droneOwner)) return false;
        if (currentDroneId != null && !currentDroneId.equals(droneId)) return false;
        if (ownerId == null) ownerId = droneOwner;
        currentDroneId = droneId;
        markDirty();
        updateNetworkHeartbeat();
        return true;
    }

    public void releaseDrone(UUID droneId) {
        if (droneId != null && droneId.equals(currentDroneId)) {
            currentDroneId = null;
            markDirty();
            updateNetworkHeartbeat();
        }
    }

    private void updateNetworkHeartbeat() {
        if (getWorld() == null || getWorld().isRemote || getPos() == null) return;
        long now = getWorld().getTotalWorldTime();
        boolean canAccept = enabled && isRedstoneAllowed() && currentDroneId == null
                && importItems.getStackInSlot(DRONE_SLOT).isEmpty();
        boolean storedDrone = !importItems.getStackInSlot(DRONE_SLOT).isEmpty();
        String occupancyState = currentDroneId != null ? "RESERVED" : storedDrone ? "STORED" : "FREE";
        DroneDockNetwork network = DroneDockNetwork.get(getWorld());
        network.heartbeat(new DroneDockRecord(dockId, getWorld().provider.getDimension(), getPos(), ownerId,
                dockName, getTier(), priority, now, true,
                currentDroneId == null && importItems.getStackInSlot(DRONE_SLOT).isEmpty() ? 0 : 1,
                energyContainer.getEnergyStored(), enabled, canAccept, occupancyState));
        if (now % 1_200L == 0L) network.prune(now);
    }

    private boolean canControl(@Nullable EntityPlayer player) {
        if (player == null) return true;
        if (ownerId == null) {
            ownerId = player.getUniqueID();
            markDirty();
        }
        return ownerId.equals(player.getUniqueID());
    }

    public boolean isAvailableForDrone() {
        return getWorld() != null && !getWorld().isRemote && enabled && isRedstoneAllowed()
                && energyContainer.getEnergyStored() > 0L;
    }

    public UUID getDockId() { return dockId; }
    public UUID getOwnerId() { return ownerId; }
    public UUID getCurrentDroneId() { return currentDroneId; }
    public String getDockName() { return dockName; }
    public boolean isEnabled() { return enabled; }
    public boolean isAutoLaunch() { return autoLaunch; }
    public boolean isAutoRecover() { return autoRecover; }
    public int getPriority() { return priority; }
    public int getRedstoneMode() { return redstoneMode; }

    public static int clampPriority(int value) {
        return Math.max(-100, Math.min(100, value));
    }

    public static int cycleRedstoneMode(int mode) {
        return (mode + 1) % 3;
    }

    public static String sanitizeDockName(String value) {
        if (value == null) return "Drone Dock";
        StringBuilder clean = new StringBuilder(32);
        String source = value.trim();
        for (int index = 0; index < source.length() && clean.length() < 32; index++) {
            char character = source.charAt(index);
            if (character == '\u00a7') {
                if (index + 1 < source.length()) index++;
                continue;
            }
            if (!Character.isISOControl(character)) clean.append(character);
        }
        String result = clean.toString().trim();
        return result.isEmpty() ? "Drone Dock" : result;
    }

    private void updateDockNameFromUi(@Nullable EntityPlayer player, String value) {
        String clean = sanitizeDockName(value);
        if (getWorld() != null && getWorld().isRemote) {
            dockName = clean;
            return;
        }
        if (!canControl(player) || Objects.equals(dockName, clean)) return;
        dockName = clean;
        markDirty();
        updateNetworkHeartbeat();
    }

    private boolean isRedstoneAllowed() {
        if (redstoneMode == 0 || getWorld() == null) return true;
        boolean powered = getWorld().isBlockPowered(getPos())
                || getWorld().getRedstonePowerFromNeighbors(getPos()) > 0;
        return redstoneMode == 1 ? powered : !powered;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setString("DockId", dockId.toString());
        if (ownerId != null) data.setString("Owner", ownerId.toString());
        if (currentDroneId != null) data.setString("CurrentDrone", currentDroneId.toString());
        data.setString("DockName", dockName);
        data.setBoolean("Enabled", enabled);
        data.setBoolean("AutoLaunch", autoLaunch);
        data.setBoolean("AutoRecover", autoRecover);
        data.setBoolean("PendingResume", pendingResume);
        data.setInteger("Priority", priority);
        data.setInteger("RedstoneMode", redstoneMode);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        dockId = readUuid(data, "DockId", initialDockId);
        ownerId = readUuid(data, "Owner", null);
        currentDroneId = readUuid(data, "CurrentDrone", null);
        dockName = sanitizeDockName(data.hasKey("DockName", 8) ? data.getString("DockName") : "Drone Dock");
        enabled = !data.hasKey("Enabled") || data.getBoolean("Enabled");
        autoLaunch = data.getBoolean("AutoLaunch");
        autoRecover = !data.hasKey("AutoRecover") || data.getBoolean("AutoRecover");
        pendingResume = data.getBoolean("PendingResume");
        priority = clampPriority(data.getInteger("Priority"));
        redstoneMode = Math.max(0, Math.min(2, data.getInteger("RedstoneMode")));
    }

    @Nullable
    private static UUID readUuid(NBTTagCompound data, String key, @Nullable UUID fallback) {
        if (!data.hasKey(key, 8)) return fallback;
        try {
            return UUID.fromString(data.getString(key));
        } catch (IllegalArgumentException ignored) {
            return fallback;
        }
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityDroneDock(metaTileEntityId, getTier());
    }

    @Override
    public boolean usesMui2() {
        return true;
    }

    @Override
    public ModularPanel buildUI(PosGuiData guiData, PanelSyncManager syncManager, UISettings settings) {
        if (getWorld() != null && !getWorld().isRemote) canControl(guiData.getPlayer());
        registerControlStateSync(syncManager);
        syncManager.registerSlotGroup("drone", 2);
        syncManager.registerSyncedAction(CONTROL_ACTION, false, true, packet -> {
            String command = packet.readString(16);
            if ("LAUNCH".equals(command)) tryLaunchStoredDrone(guiData.getPlayer());
            else if ("RECALL".equals(command)) requestBoundDroneRecall(guiData.getPlayer());
            else if ("AUTO".equals(command) && canControl(guiData.getPlayer())) {
                autoLaunch = !autoLaunch;
                markDirty();
                updateNetworkHeartbeat();
            } else if ("ENABLE".equals(command) && canControl(guiData.getPlayer())) {
                enabled = !enabled;
                markDirty();
                updateNetworkHeartbeat();
            } else if ("PRIORITY_DOWN".equals(command) && canControl(guiData.getPlayer())) {
                priority = clampPriority(priority - 1);
                markDirty();
                updateNetworkHeartbeat();
            } else if ("PRIORITY_UP".equals(command) && canControl(guiData.getPlayer())) {
                priority = clampPriority(priority + 1);
                markDirty();
                updateNetworkHeartbeat();
            } else if ("REDSTONE".equals(command) && canControl(guiData.getPlayer())) {
                redstoneMode = cycleRedstoneMode(redstoneMode);
                markDirty();
                updateNetworkHeartbeat();
            }
        });
        return GTGuis.createPanel(this, 176, 230)
                .child(IKey.lang(getMetaFullName()).asWidget().pos(5, 5))
                .child(IKey.lang("drtech.drone.dock.name").asWidget().pos(5, 18))
                .child(new TextFieldWidget().pos(39, 14).size(129, 14).setMaxLength(32)
                        .setValidator(MetaTileEntityDroneDock::sanitizeDockName)
                        .value(SyncHandlers.string(() -> dockName,
                                value -> updateDockNameFromUi(guiData.getPlayer(), value))))
                .child(IKey.lang("drtech.drone.dock.charge_slot").asWidget().pos(25, 31))
                .child(IKey.lang("drtech.drone.dock.program_slot").asWidget().pos(101, 31))
                .child(new ItemSlot().slot(SyncHandlers.itemSlot(importItems, DRONE_SLOT)
                        .slotGroup("drone").accessibility(true, true)).pos(46, 48))
                .child(new ItemSlot().slot(SyncHandlers.itemSlot(importItems, PROGRAM_SLOT)
                        .slotGroup("drone").accessibility(true, true)).pos(112, 48))
                .child(controlButton("drtech.drone.dock.launch", "LAUNCH", 8, 73, syncManager))
                .child(controlButton("drtech.drone.dock.recall", "RECALL", 62, 73, syncManager))
                .child(controlButton(IKey.lang(this::getAutoLaunchToggleKey), "AUTO", 116, 73, 52, syncManager))
                .child(controlButton(IKey.lang(this::getDockEnableToggleKey), "ENABLE", 8, 92, 160, syncManager))
                .child(controlButton(IKey.lang("drtech.drone.dock.priority.down"),
                        "PRIORITY_DOWN", 8, 111, 24, syncManager))
                .child(IKey.lang("drtech.drone.dock.priority.status",
                        () -> new Object[] {clientPriority}).asWidget().pos(37, 115))
                .child(controlButton(IKey.lang("drtech.drone.dock.priority.up"),
                        "PRIORITY_UP", 145, 111, 24, syncManager))
                .child(controlButton(IKey.lang(this::getRedstoneModeKey), "REDSTONE", 8, 130, 160, syncManager))
                .child(IKey.lang(this::getDockEnabledStatusKey).asWidget().pos(8, 149))
                .child(IKey.lang(this::getAutoLaunchStatusKey).asWidget().pos(8, 160))
                .child(IKey.lang(this::getLaunchReadinessStatusKey).asWidget().pos(8, 171))
                .child(SlotGroupWidget.playerInventory(false).left(7).bottom(7));
    }

    private ButtonWidget<?> controlButton(String translation, String command, int x, int y,
            PanelSyncManager syncManager) {
        return controlButton(IKey.lang(translation), command, x, y, 52, syncManager);
    }

    private ButtonWidget<?> controlButton(IKey label, String command, int x, int y, int width,
            PanelSyncManager syncManager) {
        return new ButtonWidget<>().pos(x, y).size(width, 16).overlay(label)
                .onMousePressed(mouse -> {
                    syncManager.callSyncedAction(CONTROL_ACTION, packet -> packet.writeString(command));
                    return true;
                });
    }

    /** Registers compact server-to-client state values used by the dock control/status area. */
    private void registerControlStateSync(PanelSyncManager syncManager) {
        syncManager.syncValue("dock_enabled", 0, GenericSyncValue.builder(Boolean.class)
                .getter(() -> enabled)
                .setter(value -> clientDockEnabled = value)
                .adapter(ByteBufAdapters.BOOL)
                .copyImmutable()
                .build());
        syncManager.syncValue("dock_auto_launch", 0, GenericSyncValue.builder(Boolean.class)
                .getter(() -> autoLaunch)
                .setter(value -> clientAutoLaunch = value)
                .adapter(ByteBufAdapters.BOOL)
                .copyImmutable()
                .build());
        syncManager.syncValue("dock_launch_ready", 0, GenericSyncValue.builder(Boolean.class)
                .getter(this::canLaunchStoredDrone)
                .setter(value -> clientLaunchReady = value)
                .adapter(ByteBufAdapters.BOOL)
                .copyImmutable()
                .build());
        syncManager.syncValue("dock_priority", 0, GenericSyncValue.builder(Integer.class)
                .getter(() -> priority)
                .setter(value -> clientPriority = value)
                .adapter(ByteBufAdapters.INT)
                .copyImmutable()
                .build());
        syncManager.syncValue("dock_redstone_powered", 0, GenericSyncValue.builder(Boolean.class)
                .getter(() -> redstoneMode == 1)
                .setter(value -> clientRedstonePowered = value)
                .adapter(ByteBufAdapters.BOOL)
                .copyImmutable()
                .build());
        syncManager.syncValue("dock_redstone_unpowered", 0, GenericSyncValue.builder(Boolean.class)
                .getter(() -> redstoneMode == 2)
                .setter(value -> clientRedstoneUnpowered = value)
                .adapter(ByteBufAdapters.BOOL)
                .copyImmutable()
                .build());
    }

    private String getDockEnableToggleKey() {
        return clientDockEnabled ? "drtech.drone.dock.enable.disable" : "drtech.drone.dock.enable.enable";
    }

    private String getAutoLaunchToggleKey() {
        return clientAutoLaunch ? "drtech.drone.dock.auto.disable" : "drtech.drone.dock.auto.enable";
    }

    private String getDockEnabledStatusKey() {
        return clientDockEnabled ? "drtech.drone.dock.status.enabled" : "drtech.drone.dock.status.disabled";
    }

    private String getAutoLaunchStatusKey() {
        return clientAutoLaunch ? "drtech.drone.dock.auto.status.enabled"
                : "drtech.drone.dock.auto.status.disabled";
    }

    private String getLaunchReadinessStatusKey() {
        if (!clientDockEnabled) return "drtech.drone.dock.status.launch.disabled";
        return clientLaunchReady ? "drtech.drone.dock.status.launch.ready"
                : "drtech.drone.dock.status.launch.charging";
    }

    private String getRedstoneModeKey() {
        return !clientRedstonePowered && !clientRedstoneUnpowered ? "drtech.drone.dock.redstone.ignore"
                : clientRedstonePowered ? "drtech.drone.dock.redstone.powered"
                : "drtech.drone.dock.redstone.unpowered";
    }

    @Override
    protected boolean shouldSerializeInventories() {
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        IVertexOperation[] colouredPipeline = ArrayUtils.add(pipeline,
                new ColourMultiplier(GTUtility.convertRGBtoOpaqueRGBA_CL(getPaintingColorForRendering())));
        Textures.VOLTAGE_CASINGS[getTier()].render(renderState, translation, colouredPipeline);
        com.drppp.drtech.Client.Textures.DRONE_DOCK_CASING.render(renderState, translation, pipeline);
        Textures.INFINITE_EMITTER_FACE.renderSided(EnumFacing.UP, renderState, translation, pipeline);
        com.drppp.drtech.Client.Textures.DRONE_DOCK_OVERLAY.renderSided(
                EnumFacing.UP, renderState, translation, pipeline);
    }
}
