package com.drppp.drtech.common.drone.entity;

import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.factory.EntityGuiData;
import com.cleanroommc.modularui.factory.GuiFactories;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.utils.serialization.ByteBufAdapters;
import com.cleanroommc.modularui.value.sync.GenericSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;
import com.drppp.drtech.Tags;
import com.drppp.drtech.common.Items.ItemsInit;
import com.drppp.drtech.common.drone.energy.DroneEnergyCosts;
import com.drppp.drtech.common.drone.action.DroneTransferRequest;
import com.drppp.drtech.common.drone.action.DroneInteractionRequest;
import com.drppp.drtech.common.drone.action.DroneItemWorldRequest;
import com.drppp.drtech.common.drone.filter.DroneBlockFilterSpec;
import com.drppp.drtech.common.drone.filter.DroneFluidFilterSpec;
import com.drppp.drtech.common.drone.energy.DroneEnergyStorage;
import com.drppp.drtech.common.drone.energy.DroneEuEndpoint;
import com.drppp.drtech.common.drone.energy.DroneEuTransfer;
import com.drppp.drtech.common.drone.firmware.DroneSafetyFirmware;
import com.drppp.drtech.common.drone.firmware.DroneSafetyState;
import com.drppp.drtech.common.drone.item.DroneItemData;
import com.drppp.drtech.common.drone.hardware.DroneChassisTier;
import com.drppp.drtech.common.drone.hardware.DroneHardwareStats;
import com.drppp.drtech.common.drone.hardware.DroneUpgradeDataCodec;
import com.drppp.drtech.common.drone.hardware.DroneUpgradeType;
import com.drppp.drtech.common.drone.hardware.ItemDroneUpgradeModule;
import com.drppp.drtech.common.drone.inventory.DroneItemFilter;
import com.drppp.drtech.common.drone.inventory.DroneItemTransfer;
import com.drppp.drtech.common.drone.machine.MetaTileEntityDroneDock;
import com.drppp.drtech.common.drone.machine.MetaTileEntityDroneRedstoneEmitter;
import com.drppp.drtech.common.drone.navigation.DronePathResult;
import com.drppp.drtech.common.drone.navigation.DronePathfinder;
import com.drppp.drtech.common.drone.navigation.MinecraftDroneNavigationWorld;
import com.drppp.drtech.common.drone.network.DroneDockNetwork;
import com.drppp.drtech.common.drone.network.DroneDockRecord;
import com.drppp.drtech.common.drone.program.codec.DroneProgramFormatException;
import com.drppp.drtech.common.drone.program.codec.DroneProgramNbtCodec;
import com.drppp.drtech.common.drone.program.codec.DroneProgramMigrator;
import com.drppp.drtech.common.drone.program.compile.DroneCompileResult;
import com.drppp.drtech.common.drone.program.compile.DroneProgramCompiler;
import com.drppp.drtech.common.drone.program.model.DroneProgramGraph;
import com.drppp.drtech.common.drone.program.model.DroneArea;
import com.drppp.drtech.common.drone.program.registry.DrTechDroneNodes;
import com.drppp.drtech.common.drone.program.runtime.DrTechDroneExecutors;
import com.drppp.drtech.common.drone.program.runtime.DrTechDroneValueEvaluators;
import com.drppp.drtech.common.drone.program.runtime.DroneActionState;
import com.drppp.drtech.common.drone.program.runtime.DroneActionStatus;
import com.drppp.drtech.common.drone.program.runtime.DroneExecutionResult;
import com.drppp.drtech.common.drone.program.runtime.DroneProgramRuntime;
import com.drppp.drtech.common.drone.program.runtime.DroneRuntimeEnvironment;
import com.drppp.drtech.common.drone.program.runtime.DroneRuntimeStatus;
import com.drppp.drtech.common.drone.program.runtime.service.DroneSensorService;
import com.google.common.base.Optional;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.IElectricItem;
import net.minecraft.entity.EntityFlying;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.UUID;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Random;

public final class EntityProgrammableDrone extends EntityFlying implements IGuiHolder<EntityGuiData> {

    private static final String CONTROL_ACTION = "drone_control";

    private static final DataParameter<Integer> ENERGY_PERCENT = EntityDataManager.createKey(
            EntityProgrammableDrone.class, DataSerializers.VARINT);
    private static final DataParameter<Optional<UUID>> OWNER = EntityDataManager.createKey(
            EntityProgrammableDrone.class, DataSerializers.OPTIONAL_UNIQUE_ID);
    private static final DataParameter<Integer> RUNTIME_STATE = EntityDataManager.createKey(
            EntityProgrammableDrone.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> CHASSIS_TIER = EntityDataManager.createKey(
            EntityProgrammableDrone.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> UPGRADE_MASK = EntityDataManager.createKey(
            EntityProgrammableDrone.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> SAFETY_STATE = EntityDataManager.createKey(
            EntityProgrammableDrone.class, DataSerializers.VARINT);

    private DroneChassisTier chassis = DroneChassisTier.HV;
    private UUID droneId = UUID.randomUUID();
    private DroneEnergyStorage energy = new DroneEnergyStorage(
            DroneChassisTier.HV.getBaseCapacity(), DroneChassisTier.HV.getVoltageTier());
    private boolean loadingHardware;
    private final ItemStackHandler inventory = createCargoHandler();
    private final ItemStackHandler upgrades = createUpgradeHandler();
    private final FluidTank fluidTank = new FluidTank(0);
    private NBTTagCompound program;
    private DroneProgramRuntime runtime;
    private NBTTagCompound pendingRuntimeState;
    private BlockPos boundDock;
    private boolean recalledOrDropped;
    private boolean executionEnabled = true;
    private boolean loopProgram;
    private boolean pickupActionThisTick;
    private int dockRecoveryCooldown;
    private final DroneSafetyFirmware safetyFirmware = new DroneSafetyFirmware();
    private DroneRuntimeEnvironment safetyNavigation;

    private String clientProgram = "No program";
    private String clientStatus = "READY";
    private String clientNode = "-";
    private String clientProgress = "";
    private String clientError = "";
    private String clientDock = "Unbound";
    private long clientEnergy;
    private long clientCapacity;
    private long clientRevision;
    private boolean clientLoop;
    private String clientTrace = "No execution trace";
    private String clientChassis = "HV";
    private int clientCargoSlots = 9;
    private int clientWirelessRange = 256;
    private int clientFluidAmount;
    private int clientFluidCapacity;
    private String clientFluidName = "";
    private boolean clientBatteryLocked;
    private boolean clientCargoLocked;
    private float clientHealth;
    private float clientMaxHealth;

    public EntityProgrammableDrone(World world) {
        super(world);
        setSize(0.9F, 0.45F);
        setNoAI(true);
        enablePersistence();
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        dataManager.register(ENERGY_PERCENT, 0);
        dataManager.register(OWNER, Optional.absent());
        dataManager.register(RUNTIME_STATE, DroneRuntimeStatus.READY.ordinal());
        dataManager.register(CHASSIS_TIER, DroneChassisTier.HV.getMetadata());
        dataManager.register(UPGRADE_MASK, 0);
        dataManager.register(SAFETY_STATE, DroneSafetyState.PROGRAM.ordinal());
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(20.0D);
        getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(DroneChassisTier.HV.getArmor());
        getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.25D);
        getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(64.0D);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (world.isRemote) {
            return;
        }
        if (dockRecoveryCooldown > 0) dockRecoveryCooldown--;
        pickupActionThisTick = false;
        if (tickSafetyFirmware()) {
            if (ticksExisted % 10 == 0) {
                syncEnergyPercent();
                syncRuntimeState();
                syncHardwareState();
            }
            return;
        }
        if (consumeEnergy(DroneEnergyCosts.IDLE_PER_TICK)) {
            motionY *= 0.6D;
            if (runtime != null && executionEnabled) {
                runtime.tick();
                if (runtime.getStatus() == DroneRuntimeStatus.COMPLETED && loopProgram) {
                    runtime.restart();
                } else if (runtime.getStatus() == DroneRuntimeStatus.COMPLETED
                        || runtime.getStatus() == DroneRuntimeStatus.ERROR) {
                    executionEnabled = false;
                }
            }
            if (!pickupActionThisTick) collectNearbyItems();
        } else {
            motionY -= 0.04D;
        }
        if (ticksExisted % 10 == 0) {
            syncEnergyPercent();
            syncRuntimeState();
            syncHardwareState();
        }
    }

    /** Firmware priority path. Returning and charging suspend graph execution without resetting its memory. */
    private boolean tickSafetyFirmware() {
        int percent = actualEnergyPercent();
        MetaTileEntityDroneDock dock = findDroneDock(boundDock);
        if (safetyFirmware.getState() == DroneSafetyState.PROGRAM
                && percent <= safetyFirmware.getReturnAtPercent()) {
            if ((dock == null || !dock.isAvailableForDrone()) && selectFallbackDock()) {
                dock = findDroneDock(boundDock);
            }
            boolean recoveryAvailable = dock != null && dock.isAvailableForDrone();
            if (safetyFirmware.shouldPreemptProgram(percent, recoveryAvailable)) {
                safetyFirmware.beginReturn(executionEnabled);
            }
        }
        if (safetyFirmware.getState() == DroneSafetyState.RETURNING
                && (dock == null || !dock.isAvailableForDrone()) && selectFallbackDock()) {
            dock = findDroneDock(boundDock);
        }
        boolean reached = boundDock != null && getDistanceSqToCenter(boundDock.up()) <= 0.81D;
        DroneSafetyState state = safetyFirmware.evaluate(percent, boundDock != null,
                dock != null && dock.isAvailableForDrone(), reached);
        dataManager.set(SAFETY_STATE, state.ordinal());
        if (state == DroneSafetyState.PROGRAM) return false;
        executionEnabled = false;
        pickupActionThisTick = true;
        if (state == DroneSafetyState.RETURNING && dock != null) {
            DroneExecutionResult movement = getSafetyNavigation().moveTo(boundDock.up());
            if (movement.getState() == DroneActionState.SUCCESS) {
                safetyFirmware.markCharging();
                if (dock.acceptRecoveredDrone(this, safetyFirmware.shouldResumeProgram())) return true;
            }
        } else if (state == DroneSafetyState.CHARGING && dock != null) {
            if (dock.acceptRecoveredDrone(this, safetyFirmware.shouldResumeProgram())) return true;
            dock.chargeDeployedDrone(energy);
            syncEnergyPercent();
            if (actualEnergyPercent() >= safetyFirmware.getResumeAtPercent()) {
                safetyFirmware.evaluate(actualEnergyPercent(), true, true, true);
            }
        } else if (state == DroneSafetyState.RECOVERING) {
            safetyFirmware.finishRecovery();
            safetyNavigation = null;
            executionEnabled = safetyFirmware.shouldResumeProgram();
        } else if (state == DroneSafetyState.SAFE_IDLE) {
            motionX *= 0.25D;
            motionY *= 0.25D;
            motionZ *= 0.25D;
            if (ticksExisted % 40 == 0 && dock != null && dock.isAvailableForDrone()) {
                safetyFirmware.retryBoundDock();
            }
        } else if (state == DroneSafetyState.EMERGENCY_LAND) {
            motionX *= 0.8D;
            motionZ *= 0.8D;
            motionY = Math.max(motionY - 0.04D, -0.25D);
        }
        return true;
    }

    private int actualEnergyPercent() {
        return energy.getCapacity() <= 0L ? 0
                : (int) Math.min(100L, energy.getStored() * 100L / energy.getCapacity());
    }

    private DroneRuntimeEnvironment getSafetyNavigation() {
        if (safetyNavigation == null) safetyNavigation = createRuntimeEnvironment();
        return safetyNavigation;
    }

    private boolean selectFallbackDock() {
        BlockPos selected = findNearestNetworkDock(true);
        return selected != null && bindToDockPosition(selected);
    }

    @Nullable
    private BlockPos findNearestNetworkDock(boolean requireAccepting) {
        DroneDockNetwork network = DroneDockNetwork.get(world);
        return network.findNearest(world.provider.getDimension(), new BlockPos(posX, posY, posZ), getOwnerId(),
                chassis.getVoltageTier(), world.getTotalWorldTime(), requireAccepting, null)
                .map(DroneDockRecord::getPosition).orElse(null);
    }

    private boolean bindToDockPosition(BlockPos target) {
        MetaTileEntityDroneDock next = findDroneDock(target);
        if (next == null || next.getTier() > chassis.getVoltageTier()
                || !next.tryReserveDrone(droneId, getOwnerId())) return false;
        if (boundDock != null && !boundDock.equals(target)) {
            MetaTileEntityDroneDock previous = findDroneDock(boundDock);
            if (previous != null) previous.releaseDrone(droneId);
        }
        boundDock = target.toImmutable();
        safetyNavigation = null;
        return true;
    }

    private void unbindCurrentDock() {
        MetaTileEntityDroneDock dock = findDroneDock(boundDock);
        if (dock != null) dock.releaseDrone(droneId);
        boundDock = null;
        safetyNavigation = null;
    }

    @Nullable
    private DroneEuEndpoint findEnergyEndpoint(BlockPos target) {
        return findEnergyEndpoint(target, null);
    }

    /** @param requireOutput true for source extraction, false for target insertion, null for read-only sensors. */
    @Nullable
    private DroneEuEndpoint findEnergyEndpoint(BlockPos target, @Nullable Boolean requireOutput) {
        if (target == null || !world.isBlockLoaded(target)) return null;
        TileEntity tile = world.getTileEntity(target);
        if (tile == null) return null;
        IEnergyContainer container = tile.getCapability(GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER, null);
        DroneEuEndpoint direct = container == null ? null : adaptEnergyEndpoint(container, null);
        if (isEnergyEndpointUsable(direct, requireOutput)) return direct;
        for (EnumFacing side : EnumFacing.values()) {
            container = tile.getCapability(GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER, side);
            DroneEuEndpoint candidate = container == null ? null : adaptEnergyEndpoint(container, side);
            if (isEnergyEndpointUsable(candidate, requireOutput)) return candidate;
        }
        return requireOutput == null ? direct : null;
    }

    private static boolean isEnergyEndpointUsable(@Nullable DroneEuEndpoint endpoint, @Nullable Boolean requireOutput) {
        return endpoint != null && (requireOutput == null || (requireOutput.booleanValue()
                ? endpoint.outputsEnergy() : endpoint.inputsEnergy()));
    }

    private static DroneEuEndpoint adaptEnergyEndpoint(IEnergyContainer container, @Nullable EnumFacing side) {
        final IEnergyContainer endpoint = container;
        return new DroneEuEndpoint() {
            @Override public long getStored() { return endpoint.getEnergyStored(); }
            @Override public long getCapacity() { return endpoint.getEnergyCapacity(); }
            @Override public long getInputVoltage() { return endpoint.getInputVoltage(); }
            @Override public long getOutputVoltage() { return endpoint.getOutputVoltage(); }
            @Override public long getInputAmperage() { return endpoint.getInputAmperage(); }
            @Override public long getOutputAmperage() { return endpoint.getOutputAmperage(); }
            @Override public boolean inputsEnergy() { return endpoint.inputsEnergy(side); }
            @Override public boolean outputsEnergy() { return endpoint.outputsEnergy(side); }
            @Override public long changeEnergy(long delta) { return endpoint.changeEnergy(delta); }
        };
    }

    private DroneExecutionResult transferEu(DroneEuTransfer.Result result, boolean importing) {
        if (result.isSuccess()) {
            syncEnergyPercent();
            return DroneExecutionResult.success(result.getAmount());
        }
        switch (result.getStatus()) {
            case OVERVOLTAGE:
                return DroneExecutionResult.failure(DroneActionStatus.DENIED, "failed",
                        "EU voltage tier is incompatible with this drone chassis");
            case NO_SPACE:
                return DroneExecutionResult.failure(DroneActionStatus.NO_SPACE, "failed",
                        importing ? "Drone battery is full" : "Target EU buffer is full");
            case NO_RESOURCE:
                return DroneExecutionResult.failure(DroneActionStatus.NO_RESOURCE, "failed",
                        importing ? "Target EU buffer is empty" : "Drone battery has no EU");
            default:
                return DroneExecutionResult.failure(DroneActionStatus.INVALID_TARGET, "failed",
                        "Target does not expose the required EU direction");
        }
    }

    @Override
    protected boolean processInteract(EntityPlayer player, EnumHand hand) {
        if (!isOwner(player)) return false;
        if (!world.isRemote) {
            if (player.isSneaking()) recallToPlayer(player);
            else GuiFactories.entity().open(player, this);
        }
        return true;
    }

    public void recallToPlayer(EntityPlayer player) {
        if (recalledOrDropped || world.isRemote) {
            return;
        }
        recalledOrDropped = true;
        ItemStack stack = createDroneItem();
        unbindCurrentDock();
        if (!player.inventory.addItemStackToInventory(stack)) {
            entityDropItem(stack, 0.2F);
        }
        setDead();
    }

    public boolean isBoundToDock(BlockPos dockPosition) {
        return dockPosition != null && dockPosition.equals(boundDock);
    }

    /** Manual dock recall is an operator stop: preserve memory, pause the graph and never auto-resume it. */
    public boolean requestManualRecall() {
        if (boundDock == null || recalledOrDropped) return false;
        if (runtime != null) runtime.pause();
        safetyFirmware.beginReturn(false);
        executionEnabled = false;
        syncRuntimeState();
        return true;
    }

    /** Called by a dock after item initialization and before spawning the new flight. */
    public void prepareDockLaunch(BlockPos dockPosition) {
        if (dockPosition == null) return;
        boundDock = dockPosition.toImmutable();
        safetyFirmware.prepareForLaunch();
        safetyNavigation = null;
        dockRecoveryCooldown = 40;
        executionEnabled = runtime != null && runtime.activateForDockLaunch();
        syncRuntimeState();
    }

    public boolean canBeRecoveredByDock() {
        return dockRecoveryCooldown <= 0;
    }

    /** Creates a lossless item snapshot; the dock must simulate insertion before calling this. */
    public ItemStack createDockRecoveryItem() {
        return createDroneItem();
    }

    /** Called only after the dock has committed the recovered item to its inventory. */
    public void completeDockRecovery() {
        if (recalledOrDropped || world.isRemote) return;
        recalledOrDropped = true;
        setDead();
    }

    @Override
    public void onDeath(DamageSource cause) {
        if (!world.isRemote && !recalledOrDropped) {
            recalledOrDropped = true;
            ItemStack dropped = createDroneItem();
            unbindCurrentDock();
            entityDropItem(dropped, 0.2F);
        }
        super.onDeath(cause);
    }

    private ItemStack createDroneItem() {
        ItemStack stack = new ItemStack(ItemsInit.PROGRAMMABLE_DRONE, 1, chassis.getMetadata());
        DroneItemData.setIdentity(stack, droneId, getOwnerId());
        DroneItemData.setChassis(stack, chassis);
        DroneItemData.setUpgrades(stack, upgrades);
        IElectricItem electricItem = stack.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
        if (electricItem != null) {
            electricItem.charge(energy.getStored(), electricItem.getTier(), true, false);
        }
        DroneItemData.setProgram(stack, program);
        DroneItemData.setRuntime(stack, runtime == null ? null : runtime.writeToNbt());
        DroneItemData.setSafetyFirmware(stack, safetyFirmware.writeToNbt());
        DroneItemData.setInventory(stack, inventory.serializeNBT());
        DroneItemData.setFluid(stack, fluidTank.writeToNBT(new NBTTagCompound()));
        DroneItemData.setDock(stack, boundDock, world.provider.getDimension());
        return stack;
    }

    public void initializeFromItem(ItemStack source, @Nullable UUID ownerId) {
        DroneItemData.migrateInPlace(source, ownerId);
        this.droneId = DroneItemData.getOrCreateDroneId(source);
        this.chassis = DroneItemData.getChassis(source);
        loadingHardware = true;
        DroneUpgradeDataCodec.readInto(DroneItemData.getUpgrades(source), this.upgrades);
        this.inventory.deserializeNBT(DroneItemData.getInventory(source));
        loadFluidTank(DroneItemData.getFluid(source));
        loadingHardware = false;
        IElectricItem electricItem = source.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
        long charge = electricItem == null ? 0L : electricItem.getCharge();
        this.energy = new DroneEnergyStorage(getConfiguredCapacity(), chassis.getVoltageTier(), charge);
        this.program = DroneItemData.getProgram(source);
        this.pendingRuntimeState = DroneItemData.getRuntime(source);
        this.safetyFirmware.readFromNbt(DroneItemData.getSafetyFirmware(source));
        this.boundDock = DroneItemData.getDock(source, world.provider.getDimension());
        applyChassisAttributes(true);
        rebuildRuntime();
        UUID persistedOwner = DroneItemData.getOwnerId(source);
        setOwnerId(persistedOwner == null ? ownerId : persistedOwner);
        syncEnergyPercent();
        syncRuntimeState();
        syncHardwareState();
    }

    public DroneEnergyStorage getEnergy() {
        return energy;
    }

    public int getEnergyPercent() {
        return dataManager.get(ENERGY_PERCENT);
    }

    public ItemStackHandler getInventory() {
        return inventory;
    }

    public ItemStackHandler getUpgrades() { return upgrades; }
    public DroneChassisTier getChassis() { return chassis; }
    public UUID getDroneId() { return droneId; }
    public int getActiveCargoSlots() { return DroneHardwareStats.cargoSlots(chassis, upgrades); }
    public int getWirelessRange() { return DroneHardwareStats.wirelessRange(chassis, upgrades); }
    public FluidTank getFluidTank() { return fluidTank; }

    public DroneChassisTier getVisualChassis() {
        return DroneChassisTier.fromMetadata(dataManager.get(CHASSIS_TIER));
    }

    public int getVisualUpgradeMask() { return dataManager.get(UPGRADE_MASK); }

    public boolean hasVisualUpgrade(DroneUpgradeType type) {
        return (getVisualUpgradeMask() & (1 << type.getMetadata())) != 0;
    }

    @Nullable
    public DroneProgramRuntime getRuntime() {
        return runtime;
    }

    public void setOwnerId(@Nullable UUID ownerId) {
        dataManager.set(OWNER, Optional.fromNullable(ownerId));
    }

    @Nullable
    public UUID getOwnerId() {
        return dataManager.get(OWNER).orNull();
    }

    public boolean isOwner(EntityPlayer player) {
        UUID ownerId = getOwnerId();
        return ownerId != null && ownerId.equals(player.getUniqueID());
    }

    private ItemStackHandler createCargoHandler() {
        return new ItemStackHandler(DroneHardwareStats.MAX_CARGO_SLOTS) {
            @Override
            public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
                if (slot < 0 || slot >= getActiveCargoSlots()) return stack;
                return super.insertItem(slot, stack, simulate);
            }
        };
    }

    private ItemStackHandler createUpgradeHandler() {
        return new ItemStackHandler(DroneHardwareStats.UPGRADE_SLOTS) {
            @Override
            public int getSlotLimit(int slot) {
                return 1;
            }

            @Override
            public boolean isItemValid(int slot, ItemStack stack) {
                DroneUpgradeType type = ItemDroneUpgradeModule.getType(stack);
                return type != null && type.getMetadata() == slot;
            }

            @Override
            public void setStackInSlot(int slot, ItemStack stack) {
                if (!loadingHardware) {
                    if (!stack.isEmpty() && !isItemValid(slot, stack)) return;
                    if (stack.isEmpty() && !canRemoveUpgrade(slot)) return;
                }
                super.setStackInSlot(slot, stack);
            }

            @Override
            public ItemStack extractItem(int slot, int amount, boolean simulate) {
                if (!canRemoveUpgrade(slot)) return ItemStack.EMPTY;
                return super.extractItem(slot, amount, simulate);
            }

            @Override
            protected void onContentsChanged(int slot) {
                super.onContentsChanged(slot);
                if (!loadingHardware) {
                    recalculateEnergyCapacity();
                    recalculateFluidCapacity();
                    syncHardwareState();
                }
            }
        };
    }

    private boolean canRemoveUpgrade(int slot) {
        if (slot < 0 || slot >= upgrades.getSlots() || upgrades.getStackInSlot(slot).isEmpty()) return true;
        DroneUpgradeType type = DroneUpgradeType.fromMetadata(slot);
        if (type == DroneUpgradeType.BATTERY && energy.getStored() > chassis.getBaseCapacity()) return false;
        if (type == DroneUpgradeType.CARGO) {
            for (int cargoSlot = chassis.getBaseCargoSlots(); cargoSlot < inventory.getSlots(); cargoSlot++) {
                if (!inventory.getStackInSlot(cargoSlot).isEmpty()) return false;
            }
        }
        if (type == DroneUpgradeType.FLUID_CARGO && fluidTank.getFluidAmount() > 0) return false;
        return true;
    }

    private long getConfiguredCapacity() {
        return DroneHardwareStats.capacity(chassis, upgrades);
    }

    private void recalculateEnergyCapacity() {
        energy = new DroneEnergyStorage(getConfiguredCapacity(), chassis.getVoltageTier(), energy.getStored());
        syncEnergyPercent();
    }

    private void recalculateFluidCapacity() {
        int capacity = DroneHardwareStats.fluidCapacity(chassis, upgrades);
        fluidTank.setCapacity(capacity);
        if (fluidTank.getFluidAmount() > capacity) fluidTank.drain(fluidTank.getFluidAmount() - capacity, true);
    }

    private void loadFluidTank(@Nullable NBTTagCompound saved) {
        fluidTank.drain(Integer.MAX_VALUE, true);
        recalculateFluidCapacity();
        FluidStack restored = saved == null ? null : FluidStack.loadFluidStackFromNBT(saved);
        if (restored != null && fluidTank.getCapacity() > 0) {
            restored.amount = Math.min(restored.amount, fluidTank.getCapacity());
            fluidTank.fill(restored, true);
        }
    }

    private boolean consumeEnergy(long baseCost) {
        return energy.consume(DroneHardwareStats.energyCost(baseCost, upgrades));
    }

    @Override
    public ModularPanel buildUI(EntityGuiData data, PanelSyncManager syncManager, UISettings settings) {
        syncManager.registerSlotGroup("drone_inventory", inventory.getSlots());
        syncManager.registerSlotGroup("drone_upgrades", upgrades.getSlots());
        GenericSyncValue<NBTTagCompound> state = GenericSyncValue.builder(NBTTagCompound.class)
                .getter(this::createControlSnapshot)
                .setter(this::receiveControlSnapshot)
                .adapter(ByteBufAdapters.NBT)
                .copy(NBTTagCompound::copy)
                .build();
        syncManager.syncValue("drone_control_state", state);
        syncManager.registerSyncedAction(CONTROL_ACTION, false, true,
                packet -> handleControl(data.getPlayer(), packet.readString(24)));

        ModularPanel panel = ModularPanel.defaultPanel("drtech_drone_controller", 310, 320)
                .child(IKey.lang("drtech.drone.controller.title").asWidget().pos(7, 6))
                .child(IKey.lang("drtech.drone.controller.cargo").asWidget().pos(8, 16))
                .child(IKey.lang("drtech.drone.controller.upgrades").asWidget().pos(8, 83))
                .child(IKey.dynamic(this::getClientHardwareLine).asWidget().pos(8, 124).size(116, 48))
                .child(IKey.dynamic(this::getClientEnergyLine).asWidget().pos(132, 24).size(170, 12))
                .child(IKey.dynamic(this::getClientProgramLine).asWidget().pos(132, 39).size(170, 12))
                .child(IKey.dynamic(this::getClientStatusLine).asWidget().pos(132, 54).size(170, 12))
                .child(IKey.dynamic(this::getClientNodeLine).asWidget().pos(132, 69).size(170, 12))
                .child(IKey.dynamic(this::getClientDockLine).asWidget().pos(132, 84).size(170, 12))
                .child(IKey.dynamic(this::getClientErrorLine).asWidget().pos(132, 99).size(170, 22))
                .child(controlButton("drtech.drone.controller.start", "START", 132, 124, syncManager))
                .child(controlButton("drtech.drone.controller.pause", "PAUSE", 184, 124, syncManager))
                .child(controlButton("drtech.drone.controller.resume", "RESUME", 236, 124, syncManager))
                .child(controlButton("drtech.drone.controller.stop", "STOP", 132, 143, syncManager))
                .child(controlButton("drtech.drone.controller.restart", "RESTART", 184, 143, syncManager))
                .child(controlButton("drtech.drone.controller.loop", "LOOP", 236, 143, syncManager))
                .child(controlButton("drtech.drone.controller.step", "STEP", 132, 162, syncManager))
                .child(controlButton("drtech.drone.controller.clear_trace", "CLEAR_TRACE", 184, 162, syncManager))
                .child(IKey.lang("drtech.drone.controller.trace").asWidget().pos(132, 182).size(170, 10))
                .child(IKey.dynamic(this::getClientTraceLine).asWidget().pos(132, 194).size(170, 32))
                .child(SlotGroupWidget.playerInventory(false).left(7).bottom(7));
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            int cargoSlot = slot;
            panel.child(new ItemSlot().slot(inventory, slot)
                    .setEnabledIf(widget -> cargoSlot < (world != null && world.isRemote
                            ? clientCargoSlots : getActiveCargoSlots()))
                    .pos(8 + slot % 6 * 20, 24 + slot / 6 * 20));
        }
        for (int slot = 0; slot < upgrades.getSlots(); slot++) {
            panel.child(new ItemSlot().slot(upgrades, slot).pos(8 + slot * 20, 94));
        }
        return panel;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public ModularScreen createScreen(EntityGuiData data, ModularPanel mainPanel) {
        return new ModularScreen(Tags.MODID, mainPanel);
    }

    private ButtonWidget<?> controlButton(String translation, String command, int x, int y,
            PanelSyncManager syncManager) {
        return new ButtonWidget<>().pos(x, y).size(48, 16).overlay(IKey.lang(translation))
                .onMousePressed(mouse -> {
                    syncManager.callSyncedAction(CONTROL_ACTION, packet -> packet.writeString(command));
                    return true;
                });
    }

    private void handleControl(EntityPlayer player, String command) {
        if (world.isRemote || player.getDistanceSq(this) > 64.0D) return;
        applyControl(player, command);
    }

    public boolean handleRemoteControl(EntityPlayer player, String command) {
        return !world.isRemote && player.world == world && applyControl(player, command);
    }

    private boolean applyControl(EntityPlayer player, String command) {
        if (!isOwner(player) || runtime == null) return false;
        if (safetyFirmware.getState() != DroneSafetyState.PROGRAM
                && ("START".equals(command) || "RESUME".equals(command) || "RESTART".equals(command)
                || "STEP".equals(command))) return false;
        switch (command) {
            case "START" -> {
                if (runtime.getStatus() != DroneRuntimeStatus.READY
                        && runtime.getStatus() != DroneRuntimeStatus.COMPLETED) return false;
                if (runtime.getStatus() == DroneRuntimeStatus.COMPLETED) runtime.restart();
                else runtime.start();
                executionEnabled = true;
            }
            case "PAUSE" -> {
                if (runtime.getStatus() != DroneRuntimeStatus.RUNNING
                        && runtime.getStatus() != DroneRuntimeStatus.READY) return false;
                runtime.pause();
                executionEnabled = false;
                safetyFirmware.setResumeProgram(false);
            }
            case "RESUME" -> {
                if (runtime.getStatus() != DroneRuntimeStatus.PAUSED) return false;
                runtime.resume();
                executionEnabled = true;
            }
            case "STOP" -> {
                runtime.stop();
                executionEnabled = false;
                safetyFirmware.setResumeProgram(false);
            }
            case "RESTART" -> {
                runtime.restart();
                executionEnabled = true;
            }
            case "LOOP" -> loopProgram = !loopProgram;
            case "STEP" -> {
                if (!runtime.requestSingleStep()) return false;
                executionEnabled = true;
            }
            case "CLEAR_TRACE" -> runtime.clearTrace();
            default -> { return false; }
        }
        syncRuntimeState();
        return true;
    }

    @Nullable
    public UUID getProgramId() {
        return runtime == null ? null : runtime.getProgramId();
    }

    public long getProgramRevision() {
        return runtime == null ? -1L : runtime.getProgramRevision();
    }

    public NBTTagCompound createRemoteDebugSnapshot() {
        NBTTagCompound snapshot = new NBTTagCompound();
        snapshot.setInteger("EntityId", getEntityId());
        snapshot.setInteger("EnergyPercent", getEnergyPercent());
        snapshot.setString("Chassis", chassis.name());
        snapshot.setInteger("WirelessRange", getWirelessRange());
        snapshot.setString("Modules", getInstalledUpgradeSummary());
        snapshot.setInteger("FluidAmount", fluidTank.getFluidAmount());
        snapshot.setInteger("FluidCapacity", fluidTank.getCapacity());
        snapshot.setString("FluidName", fluidTank.getFluid() == null ? "" : fluidTank.getFluid().getLocalizedName());
        snapshot.setString("SafetyState", safetyFirmware.getState().name());
        snapshot.setBoolean("ProgramSuspended", safetyFirmware.getState() != DroneSafetyState.PROGRAM);
        snapshot.setBoolean("BatteryLocked", !canRemoveUpgrade(DroneUpgradeType.BATTERY.getMetadata()));
        snapshot.setBoolean("CargoLocked", !canRemoveUpgrade(DroneUpgradeType.CARGO.getMetadata()));
        if (runtime == null) {
            snapshot.setString("Status", "NO PROGRAM");
            snapshot.setString("Node", "-");
            snapshot.setString("Progress", "");
            snapshot.setString("Variables", "No variables");
            snapshot.setString("Trace", "No execution trace");
        } else {
            snapshot.setString("Status", runtime.getStatus().name());
            snapshot.setString("EffectiveStatus", safetyFirmware.getState() == DroneSafetyState.PROGRAM
                    ? runtime.getStatus().name() : "SUSPENDED");
            snapshot.setString("Node", runtime.getCurrentNodeType());
            snapshot.setString("CurrentNode", runtime.getCurrentNodeId().toString());
            snapshot.setString("Progress", runtime.getCurrentNodeProgress());
            snapshot.setString("Variables", runtime.getVariableSummary(3));
            snapshot.setString("Trace", runtime.getTraceSummary(2));
        }
        return snapshot;
    }

    private NBTTagCompound createControlSnapshot() {
        NBTTagCompound snapshot = new NBTTagCompound();
        snapshot.setLong("Energy", energy.getStored());
        snapshot.setLong("Capacity", energy.getCapacity());
        snapshot.setString("Chassis", chassis.name());
        snapshot.setInteger("CargoSlots", getActiveCargoSlots());
        snapshot.setInteger("WirelessRange", getWirelessRange());
        snapshot.setInteger("FluidAmount", fluidTank.getFluidAmount());
        snapshot.setInteger("FluidCapacity", fluidTank.getCapacity());
        snapshot.setString("FluidName", fluidTank.getFluid() == null ? "" : fluidTank.getFluid().getLocalizedName());
        snapshot.setBoolean("BatteryLocked", !canRemoveUpgrade(DroneUpgradeType.BATTERY.getMetadata()));
        snapshot.setBoolean("CargoLocked", !canRemoveUpgrade(DroneUpgradeType.CARGO.getMetadata()));
        snapshot.setFloat("Health", getHealth());
        snapshot.setFloat("MaxHealth", getMaxHealth());
        snapshot.setBoolean("Loop", loopProgram);
        snapshot.setString("Dock", boundDock == null ? "Unbound"
                : boundDock.getX() + ", " + boundDock.getY() + ", " + boundDock.getZ());
        if (runtime == null) {
            snapshot.setString("Program", "No program");
            snapshot.setString("Status", "READY");
            snapshot.setString("Node", "-");
            snapshot.setString("Progress", "");
            snapshot.setString("Error", "No valid program is installed");
            snapshot.setString("Trace", "No execution trace");
        } else {
            snapshot.setString("Program", runtime.getProgramName());
            snapshot.setLong("Revision", runtime.getProgramRevision());
            snapshot.setString("Status", runtime.getStatus().name());
            snapshot.setString("Node", runtime.getCurrentNodeType());
            snapshot.setString("Progress", runtime.getCurrentNodeProgress());
            snapshot.setString("Error", runtime.getError());
            snapshot.setString("Trace", runtime.getTraceSummary(3));
        }
        return snapshot;
    }

    private void receiveControlSnapshot(NBTTagCompound snapshot) {
        clientEnergy = snapshot.getLong("Energy");
        clientCapacity = snapshot.getLong("Capacity");
        clientProgram = snapshot.getString("Program");
        clientRevision = snapshot.getLong("Revision");
        clientStatus = snapshot.getString("Status");
        clientNode = snapshot.getString("Node");
        clientProgress = snapshot.getString("Progress");
        clientError = snapshot.getString("Error");
        clientDock = snapshot.getString("Dock");
        clientLoop = snapshot.getBoolean("Loop");
        clientTrace = snapshot.getString("Trace");
        clientChassis = snapshot.getString("Chassis");
        clientCargoSlots = snapshot.getInteger("CargoSlots");
        clientWirelessRange = snapshot.getInteger("WirelessRange");
        clientFluidAmount = snapshot.getInteger("FluidAmount");
        clientFluidCapacity = snapshot.getInteger("FluidCapacity");
        clientFluidName = snapshot.getString("FluidName");
        clientBatteryLocked = snapshot.getBoolean("BatteryLocked");
        clientCargoLocked = snapshot.getBoolean("CargoLocked");
        clientHealth = snapshot.getFloat("Health");
        clientMaxHealth = snapshot.getFloat("MaxHealth");
    }

    private String getClientEnergyLine() { return I18n.format("drtech.drone.ui.energy", clientEnergy, clientCapacity); }
    private String getClientProgramLine() { return I18n.format("drtech.drone.ui.program", clientProgram, clientRevision); }
    private String getClientStatusLine() {
        return I18n.format("drtech.drone.ui.status", localizeStatus(clientStatus))
                + (clientLoop ? " | " + I18n.format("drtech.drone.ui.loop") : "");
    }
    private String getClientNodeLine() {
        return I18n.format("drtech.drone.ui.node", localizeNode(clientNode))
                + (clientProgress.isEmpty() ? "" : " | " + clientProgress);
    }
    private String getClientDockLine() { return I18n.format("drtech.drone.ui.dock", clientDock); }
    private String getClientErrorLine() {
        return clientError.isEmpty() ? I18n.format("drtech.drone.ui.no_error")
                : I18n.format("drtech.drone.ui.error", clientError);
    }
    private String getClientTraceLine() {
        if (clientTrace == null || clientTrace.isEmpty() || "No execution trace".equals(clientTrace)) {
            return I18n.format("drtech.drone.remote.no_trace");
        }
        String[] entries = clientTrace.split("\\n");
        StringBuilder localized = new StringBuilder();
        for (String entry : entries) {
            if (localized.length() > 0) localized.append('\n');
            localized.append(localizeTraceEntry(entry));
        }
        return localized.toString();
    }
    private String getClientHardwareLine() {
        String line = I18n.format("drtech.drone.ui.hardware", clientChassis, (int) clientHealth, (int) clientMaxHealth)
                + "\n" + I18n.format("drtech.drone.ui.cargo", clientCargoSlots, DroneHardwareStats.MAX_CARGO_SLOTS)
                + "\n" + I18n.format("drtech.drone.ui.wireless", clientWirelessRange)
                + "\n" + I18n.format("drtech.drone.ui.fluid", clientFluidName.isEmpty()
                        ? I18n.format("drtech.drone.ui.fluid_empty") : clientFluidName,
                        clientFluidAmount, clientFluidCapacity);
        if (clientBatteryLocked) line += "\n" + I18n.format("drtech.drone.ui.battery_locked");
        if (clientCargoLocked) line += "\n" + I18n.format("drtech.drone.ui.cargo_locked");
        return line;
    }

    private static String localizeStatus(String status) {
        String key = "drtech.drone.status." + status.toLowerCase(java.util.Locale.ROOT);
        String localized = I18n.format(key);
        return key.equals(localized) ? status : localized;
    }

    private static String localizeNode(String node) {
        String path = node;
        int separator = node.indexOf(':');
        if (separator >= 0 && separator < node.length() - 1) path = node.substring(separator + 1);
        String key = "drtech.drone.node." + path;
        String localized = I18n.format(key);
        return key.equals(localized) ? node : localized;
    }

    private static String localizeTraceEntry(String entry) {
        if ("END".equals(entry)) return I18n.format("drtech.drone.trace.end");
        if (entry.startsWith("BREAK ")) {
            return I18n.format("drtech.drone.trace.breakpoint", localizeNode(entry.substring(6)));
        }
        if (entry.endsWith(" PAUSED")) {
            return I18n.format("drtech.drone.trace.paused",
                    localizeNode(entry.substring(0, entry.length() - 7)));
        }
        if (entry.startsWith("ERROR ")) {
            return I18n.format("drtech.drone.trace.error", entry.substring(6));
        }
        int arrow = entry.indexOf(" -> ");
        if (arrow > 0) {
            String node = localizeNode(entry.substring(0, arrow));
            String port = entry.substring(arrow + 4);
            String portKey = "drtech.drone.port." + port.toLowerCase(java.util.Locale.ROOT);
            String localizedPort = I18n.format(portKey);
            if (portKey.equals(localizedPort)) localizedPort = port;
            return I18n.format("drtech.drone.trace.transition", node, localizedPort);
        }
        return entry;
    }

    private String getInstalledUpgradeSummary() {
        StringBuilder builder = new StringBuilder();
        for (DroneUpgradeType type : DroneUpgradeType.values()) {
            if (!DroneHardwareStats.hasUpgrade(upgrades, type)) continue;
            if (builder.length() > 0) builder.append(", ");
            builder.append(type.getSerializedName());
        }
        return builder.length() == 0 ? "none" : builder.toString();
    }

    private void syncEnergyPercent() {
        long capacity = energy.getCapacity();
        int percent = capacity <= 0L ? 0 : (int) Math.min(100L, energy.getStored() * 100L / capacity);
        dataManager.set(ENERGY_PERCENT, percent);
    }

    private void syncRuntimeState() {
        dataManager.set(RUNTIME_STATE, runtime == null ? DroneRuntimeStatus.READY.ordinal()
                : runtime.getStatus().ordinal());
        dataManager.set(SAFETY_STATE, safetyFirmware.getState().ordinal());
    }

    private void syncHardwareState() {
        dataManager.set(CHASSIS_TIER, chassis.getMetadata());
        dataManager.set(UPGRADE_MASK, DroneHardwareStats.upgradeMask(upgrades));
    }

    private void applyChassisAttributes(boolean restoreFullHealth) {
        if (getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH) == null) return;
        getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(chassis.getMaxHealth());
        getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(chassis.getArmor());
        if (restoreFullHealth) setHealth((float) chassis.getMaxHealth());
        else if (getHealth() > getMaxHealth()) setHealth(getMaxHealth());
    }

    public DroneRuntimeStatus getVisualRuntimeStatus() {
        int ordinal = dataManager.get(RUNTIME_STATE);
        DroneRuntimeStatus[] values = DroneRuntimeStatus.values();
        return ordinal >= 0 && ordinal < values.length ? values[ordinal] : DroneRuntimeStatus.ERROR;
    }

    public DroneSafetyState getVisualSafetyState() {
        int ordinal = dataManager.get(SAFETY_STATE);
        DroneSafetyState[] values = DroneSafetyState.values();
        return ordinal >= 0 && ordinal < values.length ? values[ordinal] : DroneSafetyState.SAFE_IDLE;
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setTag("DroneEnergy", energy.writeToNbt());
        compound.setInteger("DroneDataVersion", DroneItemData.CURRENT_DATA_VERSION);
        compound.setString("DroneId", droneId.toString());
        compound.setInteger("DroneChassis", chassis.getMetadata());
        compound.setString("DroneChassisId", chassis.getId().toString());
        compound.setTag("DroneUpgrades", DroneUpgradeDataCodec.write(upgrades));
        if (program != null) {
            compound.setTag("DroneProgram", program.copy());
        }
        if (runtime != null) {
            compound.setTag("DroneRuntime", runtime.writeToNbt());
        }
        compound.setTag("DroneSafetyFirmware", safetyFirmware.writeToNbt());
        compound.setTag("DroneInventory", inventory.serializeNBT());
        compound.setTag("DroneFluidTank", fluidTank.writeToNBT(new NBTTagCompound()));
        if (boundDock != null) compound.setLong("BoundDock", boundDock.toLong());
        compound.setBoolean("ExecutionEnabled", executionEnabled);
        compound.setInteger("DockRecoveryCooldown", dockRecoveryCooldown);
        compound.setBoolean("LoopProgram", loopProgram);
        UUID ownerId = getOwnerId();
        if (ownerId != null) {
            compound.setString("Owner", ownerId.toString());
        }
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        UUID savedDroneId = readUuid(compound, "DroneId");
        if (savedDroneId != null) droneId = savedDroneId;
        chassis = DroneChassisTier.fromId(compound.getString("DroneChassisId"),
                DroneChassisTier.fromMetadata(compound.getInteger("DroneChassis")));
        loadingHardware = true;
        if (compound.hasKey("DroneUpgrades", 10)) {
            DroneUpgradeDataCodec.readInto(compound.getCompoundTag("DroneUpgrades"), upgrades);
        }
        program = compound.hasKey("DroneProgram", 10) ? migrateProgramOrPreserve(compound.getCompoundTag("DroneProgram")) : null;
        pendingRuntimeState = compound.hasKey("DroneRuntime", 10) ? compound.getCompoundTag("DroneRuntime").copy() : null;
        if (compound.hasKey("DroneSafetyFirmware", 10)) {
            safetyFirmware.readFromNbt(compound.getCompoundTag("DroneSafetyFirmware"));
        }
        if (compound.hasKey("DroneInventory", 10)) {
            NBTTagCompound savedInventory = compound.getCompoundTag("DroneInventory").copy();
            savedInventory.setInteger("Size", DroneHardwareStats.MAX_CARGO_SLOTS);
            inventory.deserializeNBT(savedInventory);
        }
        loadFluidTank(compound.hasKey("DroneFluidTank", 10) ? compound.getCompoundTag("DroneFluidTank") : null);
        loadingHardware = false;
        DroneEnergyStorage savedEnergy = DroneEnergyStorage.readFromNbt(compound.getCompoundTag("DroneEnergy"),
                getConfiguredCapacity(), chassis.getVoltageTier());
        energy = new DroneEnergyStorage(getConfiguredCapacity(), chassis.getVoltageTier(), savedEnergy.getStored());
        applyChassisAttributes(false);
        boundDock = compound.hasKey("BoundDock", 4) ? BlockPos.fromLong(compound.getLong("BoundDock")) : null;
        executionEnabled = !compound.hasKey("ExecutionEnabled") || compound.getBoolean("ExecutionEnabled");
        dockRecoveryCooldown = Math.max(0, compound.getInteger("DockRecoveryCooldown"));
        loopProgram = compound.getBoolean("LoopProgram");
        setOwnerId(readUuid(compound, "Owner"));
        rebuildRuntime();
        syncEnergyPercent();
        syncRuntimeState();
        syncHardwareState();
    }

    @Nullable
    private static UUID readUuid(NBTTagCompound compound, String key) {
        if (!compound.hasKey(key, 8)) return null;
        try {
            return UUID.fromString(compound.getString(key));
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private static NBTTagCompound migrateProgramOrPreserve(NBTTagCompound source) {
        try {
            return DroneProgramMigrator.migrate(source);
        } catch (DroneProgramFormatException ignored) {
            return source.copy();
        }
    }

    @Nullable
    private MetaTileEntityDroneDock findDroneDock(@Nullable BlockPos target) {
        if (target == null || !world.isBlockLoaded(target)) return null;
        TileEntity tile = world.getTileEntity(target);
        if (!(tile instanceof IGregTechTileEntity holder)) return null;
        return holder.getMetaTileEntity() instanceof MetaTileEntityDroneDock dock ? dock : null;
    }

    private void rebuildRuntime() {
        runtime = null;
        if (program == null) {
            return;
        }
        try {
            DroneProgramGraph graph = DroneProgramNbtCodec.read(program);
            DroneCompileResult result = new DroneProgramCompiler(DrTechDroneNodes.createDefaultRegistry()).compile(graph);
            result.getProgram().ifPresent(compiled -> {
                runtime = new DroneProgramRuntime(compiled, DrTechDroneExecutors.createDefaultRegistry(),
                        DrTechDroneValueEvaluators.createDefaultRegistry(), createRuntimeEnvironment());
                if (pendingRuntimeState != null) {
                    runtime.readFromNbt(pendingRuntimeState);
                }
            });
        } catch (DroneProgramFormatException ignored) {
            runtime = null;
        } finally {
            pendingRuntimeState = null;
        }
    }

    private DroneRuntimeEnvironment createRuntimeEnvironment() {
        return new DroneRuntimeEnvironment() {
            private final DronePathfinder pathfinder = new DronePathfinder();
            private final MinecraftDroneNavigationWorld navigationWorld = new MinecraftDroneNavigationWorld(world);
            private List<BlockPos> path = Collections.emptyList();
            private BlockPos pathTarget;
            private int waypointIndex;

            @Override
            public double getEnergyPercent() {
                return energy.getCapacity() == 0L ? 0.0D : energy.getStored() * 100.0D / energy.getCapacity();
            }

            @Override
            public DroneExecutionResult moveTo(BlockPos target) {
                BlockPos current = new BlockPos(posX, posY, posZ);
                if (current.equals(target) && getDistanceSqToCenter(target) <= 0.64D) {
                    motionX *= 0.25D;
                    motionY *= 0.25D;
                    motionZ *= 0.25D;
                    return DroneExecutionResult.success();
                }
                if (!target.equals(pathTarget) || path.isEmpty() || waypointIndex >= path.size()) {
                    if (!consumeEnergy(DroneEnergyCosts.PATHFIND)) return DroneExecutionResult.running();
                    DronePathResult result = pathfinder.findPath(current, target, navigationWorld);
                    if (!result.isFound()) {
                        return DroneExecutionResult.error("Pathfinding failed: " + result.getStatus());
                    }
                    path = result.getPath();
                    pathTarget = target;
                    waypointIndex = path.size() > 1 ? 1 : 0;
                }
                BlockPos waypoint = path.get(waypointIndex);
                if (!navigationWorld.isPassable(waypoint)) {
                    path = Collections.emptyList();
                    return DroneExecutionResult.running();
                }
                double dx = waypoint.getX() + 0.5D - posX;
                double dy = waypoint.getY() + 0.5D - posY;
                double dz = waypoint.getZ() + 0.5D - posZ;
                double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
                if (distance <= 0.28D) {
                    waypointIndex++;
                    if (waypointIndex >= path.size()) {
                        motionX *= 0.25D;
                        motionY *= 0.25D;
                        motionZ *= 0.25D;
                        return DroneExecutionResult.success();
                    }
                    return DroneExecutionResult.running();
                }
                if (!consumeEnergy(DroneEnergyCosts.MOVE_PER_TICK)) return DroneExecutionResult.running();
                double speed = DroneHardwareStats.movementSpeed(chassis, upgrades);
                motionX = dx / distance * speed;
                motionY = dy / distance * speed;
                motionZ = dz / distance * speed;
                rotationYaw = (float) (Math.atan2(motionZ, motionX) * 180.0D / Math.PI) - 90.0F;
                return DroneExecutionResult.running();
            }

            @Override
            public DroneExecutionResult breakBlock(BlockPos target) {
                if (!world.isBlockLoaded(target)) return DroneExecutionResult.error("Break target is not loaded");
                if (world.isAirBlock(target)) return DroneExecutionResult.success();
                DroneExecutionResult approach = approach(target);
                if (approach.getState() != DroneActionState.SUCCESS) {
                    return approach;
                }
                if (!consumeEnergy(DroneEnergyCosts.BLOCK_INTERACTION)) return DroneExecutionResult.running();
                return DroneWorldActions.breakBlock(EntityProgrammableDrone.this, inventory, target)
                        ? DroneExecutionResult.success()
                        : DroneExecutionResult.error("Block break was denied or the block is unbreakable");
            }

            @Override
            public DroneExecutionResult placeBlock(BlockPos target, DroneItemFilter filter) {
                if (!world.isBlockLoaded(target)) return DroneExecutionResult.error("Place target is not loaded");
                DroneExecutionResult approach = approach(target);
                if (approach.getState() != DroneActionState.SUCCESS) {
                    return approach;
                }
                if (!consumeEnergy(DroneEnergyCosts.BLOCK_INTERACTION)) return DroneExecutionResult.running();
                return DroneWorldActions.placeBlock(EntityProgrammableDrone.this, inventory, target, filter)
                        ? DroneExecutionResult.success()
                        : DroneExecutionResult.error("Block placement failed, was denied, or no block item is available");
            }

            @Override
            public DroneExecutionResult placeBlockInArea(BlockPos target, DroneItemFilter filter) {
                if (!world.isBlockLoaded(target)) return DroneExecutionResult.error("Place target is not loaded");
                if (!world.getBlockState(target).getBlock().isReplaceable(world, target)) {
                    return DroneExecutionResult.success();
                }
                return placeBlock(target, filter);
            }

            @Override
            public DroneExecutionResult interactBlock(DroneInteractionRequest request) {
                BlockPos target = request.getTarget();
                if (!world.isBlockLoaded(target)) return DroneExecutionResult.failure(DroneActionStatus.UNLOADED,
                        "failed", "Interaction target is not loaded");
                if (world.isAirBlock(target)) return DroneExecutionResult.failure(DroneActionStatus.INVALID_TARGET,
                        "failed", "Interaction target is air");
                DroneExecutionResult movement = approach(target);
                if (movement.getState() != DroneActionState.SUCCESS) return movement;
                if (!consumeEnergy(DroneEnergyCosts.BLOCK_INTERACTION)) return DroneExecutionResult.running();
                DroneWorldActions.InteractionOutcome outcome = DroneWorldActions.interactBlock(
                        EntityProgrammableDrone.this, inventory, request);
                if (outcome == DroneWorldActions.InteractionOutcome.SUCCESS) return DroneExecutionResult.success();
                if (outcome == DroneWorldActions.InteractionOutcome.NO_ITEM) {
                    return DroneExecutionResult.failure(DroneActionStatus.NO_RESOURCE, "failed",
                            "No matching held item is available");
                }
                return DroneExecutionResult.failure(DroneActionStatus.DENIED, "failed",
                        "Block interaction was denied or had no effect");
            }

            @Override
            public DroneExecutionResult harvestCrop(BlockPos target) {
                if (!world.isBlockLoaded(target)) return DroneExecutionResult.failure(DroneActionStatus.UNLOADED,
                        "failed", "Crop target is not loaded");
                if (!DroneWorldActions.isMatureCrop((net.minecraft.world.WorldServer) world, target)) {
                    return DroneExecutionResult.failure(DroneActionStatus.NOT_FOUND, "failed",
                            "No mature supported crop exists at the target");
                }
                DroneExecutionResult movement = approach(target);
                if (movement.getState() != DroneActionState.SUCCESS) return movement;
                if (!consumeEnergy(DroneEnergyCosts.BLOCK_INTERACTION)) return DroneExecutionResult.running();
                return DroneWorldActions.harvestCrop(EntityProgrammableDrone.this, inventory, target)
                        ? DroneExecutionResult.success()
                        : DroneExecutionResult.failure(DroneActionStatus.DENIED, "failed",
                                "Crop harvest was denied");
            }

            @Override
            public DroneExecutionResult setRedstoneOutput(BlockPos target, int strength) {
                if (!world.isBlockLoaded(target)) return DroneExecutionResult.failure(DroneActionStatus.UNLOADED,
                        "failed", "Redstone emitter target is not loaded");
                MetaTileEntityDroneRedstoneEmitter emitter = findRedstoneEmitter(target);
                if (emitter == null) return DroneExecutionResult.failure(DroneActionStatus.INVALID_TARGET,
                        "failed", "Target is not a drone redstone emitter");
                DroneExecutionResult movement = approach(target);
                if (movement.getState() != DroneActionState.SUCCESS) return movement;
                if (!consumeEnergy(DroneEnergyCosts.BLOCK_INTERACTION)) return DroneExecutionResult.running();
                emitter.setOutputStrength(strength);
                return DroneExecutionResult.success();
            }

            @Override
            public DroneExecutionResult returnToDock(BlockPos target) {
                if (!bindToDockPosition(target)) {
                    return DroneExecutionResult.error("Target dock is missing, disabled, occupied, or owned by another player");
                }
                DroneExecutionResult movement = approach(target);
                return movement;
            }

            @Override
            public BlockPos findNearestDock() {
                return findNearestNetworkDock(true);
            }

            @Override
            public DroneExecutionResult bindDock(BlockPos target) {
                return bindToDockPosition(target) ? DroneExecutionResult.success()
                        : DroneExecutionResult.error("Dock is missing, disabled, occupied, overvoltage, or not owned by this drone owner");
            }

            @Override
            public DroneExecutionResult unbindDock() {
                unbindCurrentDock();
                return DroneExecutionResult.success();
            }

            @Override
            public DroneExecutionResult configureSafety(int returnAtPercent, int resumeAtPercent) {
                safetyFirmware.setThresholds(returnAtPercent, resumeAtPercent);
                return DroneExecutionResult.success();
            }

            @Override
            public DroneExecutionResult chargeUntil(double percent) {
                if (boundDock == null) return DroneExecutionResult.error("No drone dock is bound; return to a dock first");
                MetaTileEntityDroneDock dock = findDock(boundDock);
                if (dock == null) return DroneExecutionResult.error("Bound drone dock is missing or unloaded");
                DroneExecutionResult movement = approach(boundDock);
                if (movement.getState() != DroneActionState.SUCCESS) return movement;
                double targetPercent = Math.max(1.0D, Math.min(100.0D, percent));
                if (getEnergyPercent() >= targetPercent) return DroneExecutionResult.success();
                dock.chargeDeployedDrone(energy);
                syncEnergyPercent();
                return getEnergyPercent() >= targetPercent
                        ? DroneExecutionResult.success() : DroneExecutionResult.running();
            }

            @Override
            public long getTargetEnergy(BlockPos target) {
                DroneEuEndpoint endpoint = findEnergyEndpoint(target, true);
                return endpoint == null ? -1L : endpoint.getStored();
            }

            @Override
            public long getTargetEnergyCapacity(BlockPos target) {
                DroneEuEndpoint endpoint = findEnergyEndpoint(target, false);
                return endpoint == null ? -1L : endpoint.getCapacity();
            }

            @Override
            public DroneExecutionResult importEnergy(BlockPos target, long maximumEu) {
                DroneExecutionResult movement = approach(target);
                if (movement.getState() != DroneActionState.SUCCESS) return movement;
                DroneEuEndpoint endpoint = findEnergyEndpoint(target, false);
                if (endpoint == null) return DroneExecutionResult.failure(DroneActionStatus.INVALID_TARGET,
                        "failed", "Target has no GregTech EU container");
                return transferEu(DroneEuTransfer.importToDrone(energy, endpoint, maximumEu), true);
            }

            @Override
            public DroneExecutionResult exportEnergy(BlockPos target, long maximumEu) {
                DroneExecutionResult movement = approach(target);
                if (movement.getState() != DroneActionState.SUCCESS) return movement;
                DroneEuEndpoint endpoint = findEnergyEndpoint(target);
                if (endpoint == null) return DroneExecutionResult.failure(DroneActionStatus.INVALID_TARGET,
                        "failed", "Target has no GregTech EU container");
                return transferEu(DroneEuTransfer.exportFromDrone(energy, endpoint, maximumEu), false);
            }

            @Override
            public DroneExecutionResult chargeTargetUntil(BlockPos target, double percent, long maximumEu) {
                DroneEuEndpoint endpoint = findEnergyEndpoint(target);
                if (endpoint == null) return DroneExecutionResult.failure(DroneActionStatus.INVALID_TARGET,
                        "failed", "Target has no GregTech EU container");
                if (DroneEuTransfer.percent(endpoint) >= percent) return DroneExecutionResult.success();
                DroneExecutionResult result = exportEnergy(target, maximumEu);
                return result.getState() == DroneActionState.SUCCESS ? DroneExecutionResult.running(result.getAmount()) : result;
            }

            @Override
            public DroneExecutionResult importFluid(BlockPos target, @Nullable EnumFacing side, int maximumAmount,
                    DroneFluidFilterSpec filter) {
                if (fluidTank.getCapacity() <= 0) return DroneExecutionResult.failure(DroneActionStatus.NO_SPACE,
                        "failed", "Fluid cargo module is not installed");
                if (!world.isBlockLoaded(target)) return DroneExecutionResult.failure(DroneActionStatus.UNLOADED,
                        "failed", "Fluid target is not loaded");
                DroneExecutionResult movement = approach(target);
                if (movement.getState() != DroneActionState.SUCCESS) return movement;
                IFluidHandler remote = findTargetFluidHandler(target, side);
                if (remote == null) return DroneExecutionResult.failure(DroneActionStatus.INVALID_TARGET,
                        "failed", "Target exposes no fluid handler on the requested side");
                FluidStack candidate = firstMatchingFluid(remote, filter);
                if (candidate == null) return DroneExecutionResult.failure(DroneActionStatus.NO_RESOURCE,
                        "failed", "Target contains no matching fluid");
                FluidStack request = candidate.copy();
                request.amount = Math.max(1, maximumAmount);
                FluidStack preview = remote.drain(request, false);
                if (preview == null || preview.amount <= 0 || !filter.matches(preview)) {
                    return DroneExecutionResult.failure(DroneActionStatus.NO_RESOURCE, "failed",
                            "Target cannot drain the matching fluid");
                }
                int accepted = fluidTank.fill(preview, false);
                if (accepted <= 0) return DroneExecutionResult.failure(DroneActionStatus.NO_SPACE,
                        "failed", "Drone fluid cargo is full or contains another fluid");
                if (!consumeEnergy(DroneEnergyCosts.ENTITY_INTERACTION)) return DroneExecutionResult.running();
                request.amount = accepted;
                FluidStack drained = remote.drain(request, true);
                if (drained == null || drained.amount <= 0) return DroneExecutionResult.error(
                        "Fluid container changed while importing");
                int stored = fluidTank.fill(drained, true);
                if (stored < drained.amount) {
                    FluidStack remainder = drained.copy();
                    remainder.amount -= stored;
                    remote.fill(remainder, true);
                }
                return stored > 0 ? DroneExecutionResult.running(stored)
                        : DroneExecutionResult.error("Fluid import committed no fluid");
            }

            @Override
            public DroneExecutionResult exportFluid(BlockPos target, @Nullable EnumFacing side, int maximumAmount,
                    DroneFluidFilterSpec filter) {
                FluidStack storedFluid = fluidTank.getFluid();
                if (storedFluid == null || storedFluid.amount <= 0 || !filter.matches(storedFluid)) {
                    return DroneExecutionResult.failure(DroneActionStatus.NO_RESOURCE, "failed",
                            "Drone fluid cargo contains no matching fluid");
                }
                if (!world.isBlockLoaded(target)) return DroneExecutionResult.failure(DroneActionStatus.UNLOADED,
                        "failed", "Fluid target is not loaded");
                DroneExecutionResult movement = approach(target);
                if (movement.getState() != DroneActionState.SUCCESS) return movement;
                IFluidHandler remote = findTargetFluidHandler(target, side);
                if (remote == null) return DroneExecutionResult.failure(DroneActionStatus.INVALID_TARGET,
                        "failed", "Target exposes no fluid handler on the requested side");
                FluidStack offer = storedFluid.copy();
                offer.amount = Math.min(offer.amount, Math.max(1, maximumAmount));
                int accepted = remote.fill(offer, false);
                if (accepted <= 0) return DroneExecutionResult.failure(DroneActionStatus.NO_SPACE,
                        "failed", "Target fluid container cannot accept this fluid");
                if (!consumeEnergy(DroneEnergyCosts.ENTITY_INTERACTION)) return DroneExecutionResult.running();
                FluidStack drained = fluidTank.drain(accepted, true);
                if (drained == null || drained.amount <= 0) return DroneExecutionResult.error(
                        "Drone fluid cargo changed while exporting");
                int moved = remote.fill(drained, true);
                if (moved < drained.amount) {
                    FluidStack remainder = drained.copy();
                    remainder.amount -= moved;
                    fluidTank.fill(remainder, true);
                }
                return moved > 0 ? DroneExecutionResult.running(moved)
                        : DroneExecutionResult.error("Fluid export committed no fluid");
            }

            @Override
            public DroneExecutionResult drainFluid(int maximumAmount, DroneFluidFilterSpec filter) {
                FluidStack stored = fluidTank.getFluid();
                if (stored == null || !filter.matches(stored)) return DroneExecutionResult.failure(
                        DroneActionStatus.NO_RESOURCE, "failed", "Drone fluid cargo contains no matching fluid");
                FluidStack drained = fluidTank.drain(Math.max(1, maximumAmount), true);
                return drained == null ? DroneExecutionResult.failure(DroneActionStatus.NO_RESOURCE,
                        "failed", "Drone fluid cargo is empty") : DroneExecutionResult.running(drained.amount);
            }

            @Override
            public int getDroneFluidAmount(DroneFluidFilterSpec filter) {
                FluidStack stored = fluidTank.getFluid();
                return stored != null && filter.matches(stored) ? stored.amount : 0;
            }

            @Override
            public int getDroneFluidCapacity() { return fluidTank.getCapacity(); }

            @Override
            public int getContainerFluidAmount(BlockPos target, @Nullable EnumFacing side,
                    DroneFluidFilterSpec filter) {
                if (!world.isBlockLoaded(target)) return 0;
                IFluidHandler handler = findTargetFluidHandler(target, side);
                if (handler == null) return 0;
                long amount = 0L;
                for (IFluidTankProperties property : handler.getTankProperties()) {
                    FluidStack contents = property.getContents();
                    if (contents != null && filter.matches(contents)) amount += contents.amount;
                }
                return (int) Math.min(Integer.MAX_VALUE, amount);
            }

            @Nullable
            @Override
            public BlockPos findFluidContainer(DroneArea area, @Nullable EnumFacing side,
                    DroneFluidFilterSpec filter, int minimumAmount) {
                if (area == null || !area.isWithinRuntimeLimits()) return null;
                int minimum = Math.max(1, Math.min(1_000_000, minimumAmount));
                BlockPos nearest = null;
                double nearestDistance = Double.MAX_VALUE;
                for (int index = 0; index < area.getVolume(); index++) {
                    BlockPos candidate = area.positionAt(index);
                    if (!world.isBlockLoaded(candidate)
                            || getContainerFluidAmount(candidate, side, filter) < minimum) continue;
                    double distance = getDistanceSqToCenter(candidate);
                    if (distance < nearestDistance) {
                        nearest = candidate.toImmutable();
                        nearestDistance = distance;
                    }
                }
                return nearest;
            }

            @Override
            public DroneExecutionResult importItems(BlockPos target, DroneItemFilter filter) {
                return transferItems(target, filter, true);
            }

            @Override
            public DroneExecutionResult exportItems(BlockPos target, DroneItemFilter filter) {
                return transferItems(target, filter, false);
            }

            @Override
            public DroneExecutionResult importItems(DroneTransferRequest request) {
                return transferItems(request, true);
            }

            @Override
            public DroneExecutionResult exportItems(DroneTransferRequest request) {
                return transferItems(request, false);
            }

            @Override
            public DroneExecutionResult pickupDroppedItems(DroneItemWorldRequest request) {
                pickupActionThisTick = true;
                DroneItemFilter filter = DroneItemFilter.fromSpec(request.getFilter());
                if (!hasMatchingDroppedItem(request.getRadius(), filter)) {
                    return DroneExecutionResult.failure(DroneActionStatus.NOT_FOUND, "failed",
                            "No matching dropped item is in range");
                }
                int possible = collectDroppedItems(request.getRadius(), filter, request.getMaximumAmount(), true);
                if (possible <= 0) return DroneExecutionResult.failure(DroneActionStatus.NO_SPACE, "failed",
                        "Drone cargo has no space for matching dropped items");
                if (!consumeEnergy(DroneEnergyCosts.ENTITY_INTERACTION)) return DroneExecutionResult.running();
                int moved = collectDroppedItems(request.getRadius(), filter,
                        Math.min(request.getMaximumAmount(), possible), false);
                return moved > 0 ? DroneExecutionResult.success(moved)
                        : DroneExecutionResult.error("Dropped items changed while collecting");
            }

            @Override
            public DroneExecutionResult dropItems(DroneItemWorldRequest request) {
                pickupActionThisTick = true;
                DroneItemFilter filter = DroneItemFilter.fromSpec(request.getFilter());
                if (getCargoItemCount(filter) <= 0) return DroneExecutionResult.failure(DroneActionStatus.NO_RESOURCE,
                        "failed", "Drone cargo contains no matching items");
                if (!consumeEnergy(DroneEnergyCosts.ENTITY_INTERACTION)) return DroneExecutionResult.running();
                int dropped = dropCargoItems(filter, request.getMaximumAmount());
                return dropped > 0 ? DroneExecutionResult.success(dropped)
                        : DroneExecutionResult.error("Items changed while dropping");
            }

            @Override
            public boolean isRedstonePowered(@Nullable BlockPos target) {
                BlockPos checked = target == null ? new BlockPos(posX, posY, posZ) : target;
                return world.isBlockLoaded(checked) && world.isBlockPowered(checked);
            }

            @Override
            public boolean isOwnerWithin(double radius) {
                UUID ownerId = getOwnerId();
                EntityPlayer owner = ownerId == null ? null : world.getPlayerEntityByUUID(ownerId);
                return owner != null && !owner.isSpectator() && owner.getDistanceSq(EntityProgrammableDrone.this)
                        <= radius * radius;
            }

            @Override
            public int getCargoItemCount(DroneItemFilter filter) {
                int count = 0;
                DroneItemFilter checked = filter == null ? DroneItemFilter.ANY : filter;
                for (int slot = 0; slot < getActiveCargoSlots(); slot++) {
                    ItemStack stack = inventory.getStackInSlot(slot);
                    if (!stack.isEmpty() && checked.matches(stack)) count += stack.getCount();
                }
                return count;
            }

            @Override
            public int getCargoFreeSlots() {
                int free = 0;
                for (int slot = 0; slot < getActiveCargoSlots(); slot++) {
                    if (inventory.getStackInSlot(slot).isEmpty()) free++;
                }
                return free;
            }

            @Override
            public double getCargoUsedPercent() {
                int slots = getActiveCargoSlots();
                if (slots <= 0) return 0.0D;
                long count = 0L;
                for (int slot = 0; slot < slots; slot++) count += inventory.getStackInSlot(slot).getCount();
                return Math.min(100.0D, count * 100.0D / (slots * 64.0D));
            }

            @Override
            public int getInventoryItemCount(BlockPos target, @Nullable EnumFacing side, DroneItemFilter filter) {
                if (target == null || !world.isBlockLoaded(target)) return 0;
                IItemHandler handler = findTargetInventory(target, side);
                if (handler == null) return 0;
                DroneItemFilter checked = filter == null ? DroneItemFilter.ANY : filter;
                int count = 0;
                for (int slot = 0; slot < handler.getSlots(); slot++) {
                    ItemStack stack = handler.getStackInSlot(slot);
                    if (!stack.isEmpty() && checked.matches(stack)) count += stack.getCount();
                }
                return count;
            }

            @Override
            public int getRedstoneStrength(BlockPos target) {
                return target != null && world.isBlockLoaded(target)
                        ? world.getRedstonePowerFromNeighbors(target) : 0;
            }

            @Override
            public int getRedstoneOutputStrength(BlockPos target) {
                MetaTileEntityDroneRedstoneEmitter emitter = target == null ? null : findRedstoneEmitter(target);
                return emitter == null ? 0 : emitter.getOutputStrength();
            }

            @Override
            public int getLightLevel(BlockPos target, DroneSensorService.LightType type) {
                if (target == null || !world.isBlockLoaded(target)) return 0;
                if (type == DroneSensorService.LightType.BLOCK) return world.getLightFor(EnumSkyBlock.BLOCK, target);
                if (type == DroneSensorService.LightType.SKY) return world.getLightFor(EnumSkyBlock.SKY, target);
                return world.getLight(target);
            }

            @Override
            public boolean matchesBlock(BlockPos target, DroneBlockFilterSpec filter) {
                return target != null && world.isBlockLoaded(target)
                        && (filter == null ? DroneBlockFilterSpec.ANY : filter).matches(world.getBlockState(target));
            }

            @Override
            public boolean isCoordinateReachable(BlockPos target) {
                if (target == null || !world.isBlockLoaded(target)) return false;
                BlockPos current = new BlockPos(posX, posY, posZ);
                return pathfinder.findPath(current, target, navigationWorld).isFound();
            }

            @Override
            public boolean isDockAvailable(BlockPos target) {
                MetaTileEntityDroneDock dock = target == null ? null : findDock(target);
                return dock != null && dock.isAvailableForDrone();
            }

            @Override
            public int countMatchingBlocks(DroneArea area, DroneBlockFilterSpec filter, int limit) {
                if (area == null || !area.isWithinRuntimeLimits()) return 0;
                DroneBlockFilterSpec checked = filter == null ? DroneBlockFilterSpec.ANY : filter;
                int boundedLimit = Math.max(1, Math.min(DroneArea.MAX_BLOCKS, limit));
                int count = 0;
                for (int index = 0; index < area.getVolume() && count < boundedLimit; index++) {
                    BlockPos target = area.positionAt(index);
                    if (world.isBlockLoaded(target) && checked.matches(world.getBlockState(target))) count++;
                }
                return count;
            }

            private DroneExecutionResult transferItems(BlockPos target, DroneItemFilter filter, boolean importing) {
                DroneTransferRequest legacy = DroneTransferRequest.at(target, null,
                        DroneHardwareStats.transferLimit(upgrades), DroneHardwareStats.transferLimit(upgrades),
                        com.drppp.drtech.common.drone.action.DroneFailurePolicy.ERROR, filter.getSpec());
                return transferItems(legacy, importing);
            }

            private DroneExecutionResult transferItems(DroneTransferRequest request, boolean importing) {
                DroneItemFilter filter = DroneItemFilter.fromSpec(request.getFilter());
                BlockPos target = request.getTarget();
                if (target == null) {
                    TransferTargetSelection selection = selectAreaTransferTarget(request, importing, filter);
                    if (selection.target == null) {
                        return DroneExecutionResult.failure(selection.status, "failed", selection.message);
                    }
                    target = selection.target;
                }
                if (!world.isBlockLoaded(target)) return DroneExecutionResult.failure(DroneActionStatus.UNLOADED,
                        "failed", "Inventory target is not loaded");
                if (world.getTileEntity(target) == null) return DroneExecutionResult.failure(DroneActionStatus.NOT_FOUND,
                        "failed", "Inventory target is missing");
                DroneExecutionResult movement = approach(target);
                if (movement.getState() != DroneActionState.SUCCESS) return movement;
                IItemHandler remote = findTargetInventory(target, request.getSide());
                if (remote == null) return DroneExecutionResult.failure(DroneActionStatus.INVALID_TARGET, "failed",
                        "Target exposes no item handler on the requested side");
                IItemHandler source = importing ? remote : inventory;
                IItemHandler destination = importing ? inventory : remote;
                int limit = Math.min(request.getBatchSize(), DroneHardwareStats.transferLimit(upgrades));
                if (!hasExtractableMatchingItem(source, filter)) {
                    return DroneExecutionResult.failure(DroneActionStatus.NO_RESOURCE, "failed",
                            "Source contains no extractable matching items");
                }
                int possible = DroneItemTransfer.transfer(source, destination, filter, limit, true);
                if (possible <= 0) return DroneExecutionResult.failure(DroneActionStatus.NO_SPACE, "failed",
                        "Destination has no space for matching items");
                if (!consumeEnergy(DroneEnergyCosts.ENTITY_INTERACTION)) return DroneExecutionResult.running();
                int moved = DroneItemTransfer.transfer(source, destination, filter, possible, false);
                return moved > 0 ? DroneExecutionResult.running(moved)
                        : DroneExecutionResult.error("Inventory changed while transferring items");
            }

            private TransferTargetSelection selectAreaTransferTarget(DroneTransferRequest request,
                    boolean importing, DroneItemFilter filter) {
                DroneArea area = request.getArea();
                if (area == null || !area.isWithinRuntimeLimits()) {
                    return new TransferTargetSelection(null, DroneActionStatus.INVALID_TARGET,
                            "Inventory search area is invalid or too large");
                }
                List<BlockPos> candidates = new ArrayList<>((int) area.getVolume());
                for (int index = 0; index < area.getVolume(); index++) candidates.add(area.positionAt(index));
                if (request.getSearchMode() == com.drppp.drtech.common.drone.action.DroneSearchMode.NEAREST) {
                    BlockPos current = new BlockPos(posX, posY, posZ);
                    candidates.sort(Comparator.comparingDouble(position -> position.distanceSq(current)));
                } else if (request.getSearchMode() == com.drppp.drtech.common.drone.action.DroneSearchMode.RANDOM) {
                    long seed = droneId.getMostSignificantBits() ^ droneId.getLeastSignificantBits()
                            ^ area.getMin().toLong() ^ area.getMax().toLong();
                    Collections.shuffle(candidates, new Random(seed));
                }
                boolean inventoryFound = false;
                boolean sourceFound = false;
                int limit = Math.min(request.getBatchSize(), DroneHardwareStats.transferLimit(upgrades));
                for (BlockPos candidate : candidates) {
                    if (!world.isBlockLoaded(candidate)) continue;
                    IItemHandler remote = findTargetInventory(candidate, request.getSide());
                    if (remote == null) continue;
                    inventoryFound = true;
                    IItemHandler source = importing ? remote : inventory;
                    IItemHandler destination = importing ? inventory : remote;
                    boolean hasSource = hasExtractableMatchingItem(source, filter);
                    sourceFound |= hasSource;
                    if (!request.isSkipUnavailable()
                            || hasSource && DroneItemTransfer.transfer(source, destination, filter, limit, true) > 0) {
                        return new TransferTargetSelection(candidate, DroneActionStatus.SUCCESS, "");
                    }
                }
                if (!inventoryFound) return new TransferTargetSelection(null, DroneActionStatus.NOT_FOUND,
                        "No loaded item inventory exists in the search area");
                if (!sourceFound) return new TransferTargetSelection(null, DroneActionStatus.NO_RESOURCE,
                        "No inventory in the search area contains matching items");
                return new TransferTargetSelection(null, DroneActionStatus.NO_SPACE,
                        "All matching inventories or drone cargo destinations are full");
            }

            private boolean hasExtractableMatchingItem(IItemHandler handler, DroneItemFilter filter) {
                for (int slot = 0; slot < handler.getSlots(); slot++) {
                    ItemStack preview = handler.extractItem(slot, 1, true);
                    if (!preview.isEmpty() && filter.matches(preview)) return true;
                }
                return false;
            }

            private DroneExecutionResult approach(BlockPos target) {
                BlockPos stand = findInteractionPosition(target);
                return stand == null ? DroneExecutionResult.error("No passable interaction position") : moveTo(stand);
            }

            private BlockPos findInteractionPosition(BlockPos target) {
                BlockPos current = new BlockPos(posX, posY, posZ);
                BlockPos selected = null;
                double selectedDistance = Double.MAX_VALUE;
                for (EnumFacing facing : EnumFacing.values()) {
                    BlockPos candidate = target.offset(facing);
                    if (!navigationWorld.isPassable(candidate)) continue;
                    double distance = candidate.distanceSq(current);
                    if (distance < selectedDistance) {
                        selected = candidate;
                        selectedDistance = distance;
                    }
                }
                return selected;
            }

            private MetaTileEntityDroneDock findDock(BlockPos target) {
                return findDroneDock(target);
            }

            private MetaTileEntityDroneRedstoneEmitter findRedstoneEmitter(BlockPos target) {
                if (!world.isBlockLoaded(target)) return null;
                TileEntity tile = world.getTileEntity(target);
                if (!(tile instanceof IGregTechTileEntity holder)) return null;
                return holder.getMetaTileEntity() instanceof MetaTileEntityDroneRedstoneEmitter emitter
                        ? emitter : null;
            }

            private IItemHandler findTargetInventory(BlockPos target, @Nullable EnumFacing requestedSide) {
                TileEntity tile = world.getTileEntity(target);
                if (tile == null) return null;
                if (requestedSide != null) {
                    return tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, requestedSide);
                }
                IItemHandler handler = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
                if (handler != null) return handler;
                EnumFacing accessSide = EnumFacing.getFacingFromVector(
                        (float) (posX - target.getX() - 0.5D),
                        (float) (posY - target.getY() - 0.5D),
                        (float) (posZ - target.getZ() - 0.5D));
                handler = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, accessSide);
                if (handler != null) return handler;
                for (EnumFacing side : EnumFacing.values()) {
                    handler = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side);
                    if (handler != null) return handler;
                }
                return null;
            }

            private IFluidHandler findTargetFluidHandler(BlockPos target, @Nullable EnumFacing requestedSide) {
                TileEntity tile = world.getTileEntity(target);
                if (tile == null) return null;
                if (requestedSide != null) {
                    return tile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, requestedSide);
                }
                IFluidHandler handler = tile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null);
                if (handler != null) return handler;
                for (EnumFacing candidate : EnumFacing.values()) {
                    handler = tile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, candidate);
                    if (handler != null) return handler;
                }
                return null;
            }

            private FluidStack firstMatchingFluid(IFluidHandler handler, DroneFluidFilterSpec filter) {
                for (IFluidTankProperties property : handler.getTankProperties()) {
                    FluidStack contents = property.getContents();
                    if (contents != null && contents.amount > 0 && property.canDrain() && filter.matches(contents)) {
                        return contents.copy();
                    }
                }
                return null;
            }

            final class TransferTargetSelection {
                private final BlockPos target;
                private final DroneActionStatus status;
                private final String message;

                private TransferTargetSelection(BlockPos target, DroneActionStatus status, String message) {
                    this.target = target;
                    this.status = status;
                    this.message = message;
                }

            }
        };
    }

    private void collectNearbyItems() {
        collectDroppedItems(1.25D, DroneItemFilter.ANY, Integer.MAX_VALUE, false);
    }

    private boolean hasMatchingDroppedItem(double radius, DroneItemFilter filter) {
        for (EntityItem entityItem : world.getEntitiesWithinAABB(EntityItem.class,
                getEntityBoundingBox().grow(radius))) {
            if (!entityItem.isDead && !entityItem.cannotPickup() && !entityItem.getItem().isEmpty()
                    && filter.matches(entityItem.getItem())) return true;
        }
        return false;
    }

    private int collectDroppedItems(double radius, DroneItemFilter filter, int maximum, boolean simulate) {
        int movedTotal = 0;
        for (EntityItem entityItem : world.getEntitiesWithinAABB(EntityItem.class,
                getEntityBoundingBox().grow(radius))) {
            if (movedTotal >= maximum || entityItem.isDead || entityItem.cannotPickup()
                    || entityItem.getItem().isEmpty() || !filter.matches(entityItem.getItem())) continue;
            ItemStack original = entityItem.getItem();
            ItemStack offered = original.copy();
            offered.setCount(Math.min(offered.getCount(), maximum - movedTotal));
            ItemStack remainder = offered;
            for (int slot = 0; slot < getActiveCargoSlots() && !remainder.isEmpty(); slot++) {
                remainder = inventory.insertItem(slot, remainder, simulate);
            }
            int moved = offered.getCount() - remainder.getCount();
            if (moved <= 0) continue;
            movedTotal += moved;
            if (!simulate) {
                ItemStack remainingEntity = original.copy();
                remainingEntity.shrink(moved);
                if (remainingEntity.isEmpty()) entityItem.setDead();
                else entityItem.setItem(remainingEntity);
            }
        }
        return movedTotal;
    }

    private int dropCargoItems(DroneItemFilter filter, int maximum) {
        int droppedTotal = 0;
        for (int slot = 0; slot < getActiveCargoSlots() && droppedTotal < maximum; slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (stack.isEmpty() || !filter.matches(stack)) continue;
            int amount = Math.min(stack.getCount(), maximum - droppedTotal);
            ItemStack preview = inventory.extractItem(slot, amount, true);
            if (preview.isEmpty()) continue;
            EntityItem dropped = new EntityItem(world, posX, posY + 0.2D, posZ, preview.copy());
            dropped.setDefaultPickupDelay();
            if (!world.spawnEntity(dropped)) continue;
            ItemStack extracted = inventory.extractItem(slot, preview.getCount(), false);
            if (extracted.isEmpty()) {
                dropped.setDead();
                continue;
            }
            dropped.setItem(extracted);
            droppedTotal += extracted.getCount();
        }
        return droppedTotal;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY
                || capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY
                || super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(inventory);
        }
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(fluidTank);
        }
        return super.getCapability(capability, facing);
    }

    @Override
    protected boolean canDespawn() {
        return false;
    }
}
