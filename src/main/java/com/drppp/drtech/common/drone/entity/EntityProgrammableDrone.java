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
import com.drppp.drtech.common.drone.api.DroneEntityTransportEvent;
import com.drppp.drtech.common.drone.api.DroneExtensionRegistry;
import com.drppp.drtech.common.drone.api.DroneSignEditEvent;
import com.drppp.drtech.common.drone.action.DroneTransferRequest;
import com.drppp.drtech.common.drone.action.DroneInteractionRequest;
import com.drppp.drtech.common.drone.action.DroneItemWorldRequest;
import com.drppp.drtech.common.drone.filter.DroneBlockFilterSpec;
import com.drppp.drtech.common.drone.filter.DroneFilterMode;
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
import com.drppp.drtech.common.drone.compat.ThaumcraftEssentiaNodeCompat;
import com.drppp.drtech.common.drone.inventory.DroneItemFilter;
import com.drppp.drtech.common.drone.inventory.DroneAutoPickupMode;
import com.drppp.drtech.common.drone.inventory.DroneCargoFullPolicy;
import com.drppp.drtech.common.drone.filter.DroneItemFilterSpec;
import com.drppp.drtech.common.drone.inventory.DroneCraftingPlanner;
import com.drppp.drtech.common.drone.inventory.DroneItemTransfer;
import com.drppp.drtech.common.drone.machine.MetaTileEntityDroneDock;
import com.drppp.drtech.common.drone.machine.MetaTileEntityDroneEndpoint;
import com.drppp.drtech.common.drone.machine.MetaTileEntityDroneRedstoneEmitter;
import com.drppp.drtech.common.drone.navigation.DronePathResult;
import com.drppp.drtech.common.drone.navigation.DronePathfinder;
import com.drppp.drtech.common.drone.navigation.MinecraftDroneNavigationWorld;
import com.drppp.drtech.common.drone.network.DroneDockNetwork;
import com.drppp.drtech.common.drone.network.DroneDockRecord;
import com.drppp.drtech.common.drone.network.DroneRegistry;
import com.drppp.drtech.common.drone.network.DroneRegistryRecord;
import com.drppp.drtech.common.drone.network.DroneEndpoint;
import com.drppp.drtech.common.drone.network.DroneEndpointNetwork;
import com.drppp.drtech.common.drone.network.DroneFleetState;
import com.drppp.drtech.common.drone.network.DroneJob;
import com.drppp.drtech.common.drone.network.DroneReservation;
import com.drppp.drtech.common.drone.sound.DroneSoundCue;
import com.drppp.drtech.common.drone.sound.DroneSoundPlayer;
import com.drppp.drtech.common.drone.visual.DroneVisualState;
import com.drppp.drtech.common.drone.program.codec.DroneProgramFormatException;
import com.drppp.drtech.common.drone.program.codec.DroneProgramNbtCodec;
import com.drppp.drtech.common.drone.program.codec.DroneProgramMigrator;
import com.drppp.drtech.common.drone.program.compile.DroneCompileResult;
import com.drppp.drtech.common.drone.program.compile.CompiledDroneProgram;
import com.drppp.drtech.common.drone.program.compile.DroneProgramCompiler;
import com.drppp.drtech.common.drone.program.compile.DroneProgramHardwareValidator;
import com.drppp.drtech.common.drone.program.compile.DroneDiagnosticSeverity;
import com.drppp.drtech.common.drone.program.model.DroneProgramGraph;
import com.drppp.drtech.common.drone.program.model.DroneProgramReference;
import com.drppp.drtech.common.drone.program.model.DroneArea;
import com.drppp.drtech.common.drone.program.library.DroneProgramLibrary;
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
import com.drppp.drtech.common.drone.program.runtime.service.DroneEntitySensorResult;
import com.drppp.drtech.common.drone.filter.DroneEntityFilterSpec;
import com.google.common.base.Optional;
import gregtech.api.GTValues;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IWorkable;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.IElectricItem;
import gregtech.api.capability.impl.AbstractRecipeLogic;
import gregtech.api.items.toolitem.ToolHelper;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.multiblock.IMaintenance;
import net.minecraft.entity.EntityFlying;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.IEntityOwnable;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.monster.IMob;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.item.ItemFishingRod;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.init.SoundEvents;
import net.minecraft.world.World;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.WorldServer;
import net.minecraft.block.material.Material;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.MinecraftForge;
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
import java.util.HashSet;
import java.util.Set;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public final class EntityProgrammableDrone extends EntityFlying implements IGuiHolder<EntityGuiData> {

    private static final int ENTITY_CHASE_TICK_LIMIT = 1200;
    private static final int ENTITY_CHASE_DISTANCE_LIMIT = 128;
    private static final int ALCHEMY_TRANSFER_INTERVAL_TICKS = 10;

    private static final String CONTROL_ACTION = "drone_control";

    private static final DataParameter<Integer> ENERGY_PERCENT = EntityDataManager.createKey(
            EntityProgrammableDrone.class, DataSerializers.VARINT);
    private static final DataParameter<Boolean> HAS_ROTOR_POWER = EntityDataManager.createKey(
            EntityProgrammableDrone.class, DataSerializers.BOOLEAN);
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
    private static final DataParameter<NBTTagCompound> WORLD_PREVIEW_STATE = EntityDataManager.createKey(
            EntityProgrammableDrone.class, DataSerializers.COMPOUND_TAG);
    private static final DataParameter<ItemStack> HELD_WEAPON = EntityDataManager.createKey(
            EntityProgrammableDrone.class, DataSerializers.ITEM_STACK);
    private static final DataParameter<ItemStack> SECONDARY_WEAPON = EntityDataManager.createKey(
            EntityProgrammableDrone.class, DataSerializers.ITEM_STACK);
    private static final DataParameter<Integer> ATTACK_ANIMATION_TICKS = EntityDataManager.createKey(
            EntityProgrammableDrone.class, DataSerializers.VARINT);
    private static final DataParameter<ItemStack> HELD_FISHING_ROD = EntityDataManager.createKey(
            EntityProgrammableDrone.class, DataSerializers.ITEM_STACK);
    private static final DataParameter<Integer> FISHING_HOOK_ID = EntityDataManager.createKey(
            EntityProgrammableDrone.class, DataSerializers.VARINT);
    private static final DataParameter<NBTTagCompound> FISHING_VISUAL_TARGET = EntityDataManager.createKey(
            EntityProgrammableDrone.class, DataSerializers.COMPOUND_TAG);
    private static final DataParameter<Integer> FISHING_CAST_TICKS = EntityDataManager.createKey(
            EntityProgrammableDrone.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> FISHING_PULL_TICKS = EntityDataManager.createKey(
            EntityProgrammableDrone.class, DataSerializers.VARINT);
    private static final DataParameter<ItemStack> HELD_ALCHEMY_JAR = EntityDataManager.createKey(
            EntityProgrammableDrone.class, DataSerializers.ITEM_STACK);
    private static final DataParameter<Integer> ALCHEMY_ASPECT_COLOR = EntityDataManager.createKey(
            EntityProgrammableDrone.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> ALCHEMY_ANIMATION_TICKS = EntityDataManager.createKey(
            EntityProgrammableDrone.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> ENTITY_TARGET_ID = EntityDataManager.createKey(
            EntityProgrammableDrone.class, DataSerializers.VARINT);
    private static final DataParameter<String> STATUS_LABEL = EntityDataManager.createKey(
            EntityProgrammableDrone.class, DataSerializers.STRING);
    private static final DataParameter<Boolean> ROTORS_ACTIVE = EntityDataManager.createKey(
            EntityProgrammableDrone.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> STATUS_LIGHT_MODE = EntityDataManager.createKey(
            EntityProgrammableDrone.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> CARGO_FILL_PERCENT = EntityDataManager.createKey(
            EntityProgrammableDrone.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> FLIGHT_MARKER = EntityDataManager.createKey(
            EntityProgrammableDrone.class, DataSerializers.VARINT);

    private DroneChassisTier chassis = DroneChassisTier.HV;
    private UUID droneId = UUID.randomUUID();
    private DroneEnergyStorage energy = new DroneEnergyStorage(
            DroneChassisTier.HV.getBaseCapacity(), DroneChassisTier.HV.getVoltageTier());
    private boolean loadingHardware;
    private final ItemStackHandler inventory = createCargoHandler();
    private final ItemStackHandler upgrades = createUpgradeHandler();
    private final ItemStackHandler weapons = createWeaponHandler();
    private final ItemStackHandler fishingRod = createFishingRodHandler();
    private final ItemStackHandler alchemyJar = createAlchemyJarHandler();
    private final FluidTank fluidTank = new FluidTank(0);
    /** EU being transported is isolated from the flight battery so movement cannot consume the payload. */
    private long logisticsEuCargo;
    private NBTTagCompound program;
    private DroneProgramRuntime runtime;
    private NBTTagCompound pendingRuntimeState;
    /** Serialized single-entity transport payload. Kept server-side and restored across chunk unloads. */
    private NBTTagCompound loadedEntityData;
    private UUID loadedEntityUuid;
    private UUID followTargetUuid;
    private BlockPos followTargetAnchor;
    private UUID avoidTargetUuid;
    private BlockPos avoidTargetAnchor;
    private UUID attackTargetUuid;
    private BlockPos attackTargetAnchor;
    private UUID interactionTargetUuid;
    private BlockPos interactionTargetAnchor;
    private UUID loadTargetUuid;
    private BlockPos loadTargetAnchor;
    private int attackCooldownTicks;
    private int attackChaseTicks;
    private int alternateWeaponSlot;
    private int interactionChaseTicks;
    private int followChaseTicks;
    private int avoidChaseTicks;
    private int loadChaseTicks;
    private EntityFishHook activeFishingHook;
    private BlockPos activeFishingTarget;
    private BlockPos boundDock;
    private final List<UUID> fallbackDockIds = new ArrayList<>();
    private boolean recalledOrDropped;
    private boolean executionEnabled = true;
    private boolean loopProgram;
    private boolean pickupActionThisTick;
    private DroneAutoPickupMode autoPickupMode = DroneAutoPickupMode.ALL;
    private DroneItemFilterSpec autoPickupFilter = DroneItemFilterSpec.ANY;
    private DroneCargoFullPolicy cargoFullPolicy = DroneCargoFullPolicy.STOP;
    private boolean cargoFullPickupSuspended;
    private int lastFluidEffectTick = Integer.MIN_VALUE / 2;
    private int lastLogisticsEffectTick = Integer.MIN_VALUE / 2;
    private int dockRecoveryCooldown;
    private int flightMarkerTicks;
    private int rotorResyncTicks;
    private long nextAlchemyTransferTick;
    private DroneRuntimeStatus lastSoundRuntimeStatus = DroneRuntimeStatus.READY;
    private final DroneSafetyFirmware safetyFirmware = new DroneSafetyFirmware();
    private DroneRuntimeEnvironment safetyNavigation;
    /** Live navigation state used by the owner-only wireless debugger. */
    private BlockPos debugPathTarget;
    private BlockPos debugPathWaypoint;
    private int debugPathWaypointIndex;
    private int debugPathLength;
    private List<BlockPos> debugPathPoints = Collections.emptyList();
    private UUID debugEnergyNodeId;
    private int debugEnergyAreaTotal;
    private int debugEnergyAreaCompleted;
    private long debugEnergyAreaConsumed;
    /** -1 means the current action has no finite area estimate yet. */
    private long debugEnergyAreaRemainingEstimate = -1L;

    private String clientProgram = "No program";
    private String clientStatus = "READY";
    private String clientNode = "-";
    private String clientProgress = "";
    private String clientError = "";
    private String clientDock = "Unbound";
    private String clientFallbackCandidate = "-";
    private String clientFallbackState = "-";
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
    private long clientLogisticsEu;
    private boolean clientBatteryLocked;
    private boolean clientCargoLocked;
    private float clientHealth;
    private float clientMaxHealth;
    private String clientEntityPayload = "";
    private String clientCargoFullPolicy = DroneCargoFullPolicy.STOP.name();
    private int fallbackCandidateIndex;

    public EntityProgrammableDrone(World world) {
        super(world);
        setSize(0.9F, 0.45F);
        setNoAI(true);
        enablePersistence();
    }

    /** Drones hover and use rotor audio; never emit the humanoid ground-step sound. */
    @Override
    protected void playStepSound(BlockPos pos, Block blockIn) {
        // Intentionally silent: flight audio is dispatched through DroneSoundPlayer.
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        dataManager.register(ENERGY_PERCENT, 0);
        dataManager.register(HAS_ROTOR_POWER, false);
        dataManager.register(OWNER, Optional.absent());
        dataManager.register(RUNTIME_STATE, DroneRuntimeStatus.READY.ordinal());
        dataManager.register(CHASSIS_TIER, DroneChassisTier.HV.getMetadata());
        dataManager.register(UPGRADE_MASK, 0);
        dataManager.register(SAFETY_STATE, DroneSafetyState.PROGRAM.ordinal());
        dataManager.register(WORLD_PREVIEW_STATE, new NBTTagCompound());
        dataManager.register(HELD_WEAPON, ItemStack.EMPTY);
        dataManager.register(SECONDARY_WEAPON, ItemStack.EMPTY);
        dataManager.register(ATTACK_ANIMATION_TICKS, 0);
        dataManager.register(HELD_FISHING_ROD, ItemStack.EMPTY);
        dataManager.register(FISHING_HOOK_ID, -1);
        dataManager.register(FISHING_VISUAL_TARGET, new NBTTagCompound());
        dataManager.register(FISHING_CAST_TICKS, 0);
        dataManager.register(FISHING_PULL_TICKS, 0);
        dataManager.register(HELD_ALCHEMY_JAR, ItemStack.EMPTY);
        dataManager.register(ALCHEMY_ASPECT_COLOR, -1);
        dataManager.register(ALCHEMY_ANIMATION_TICKS, 0);
        dataManager.register(ENTITY_TARGET_ID, -1);
        dataManager.register(STATUS_LABEL, "");
        dataManager.register(ROTORS_ACTIVE, true);
        dataManager.register(STATUS_LIGHT_MODE, 0);
        dataManager.register(CARGO_FILL_PERCENT, 0);
        dataManager.register(FLIGHT_MARKER, DroneVisualState.FlightMarker.NONE.ordinal());
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
            tickClientVisualEffects();
            return;
        }
        if (flightMarkerTicks > 0 && --flightMarkerTicks == 0) {
            dataManager.set(FLIGHT_MARKER, DroneVisualState.FlightMarker.NONE.ordinal());
        }
        int heartbeatInterval = DroneHardwareStats.hasUpgrade(upgrades, DroneUpgradeType.FLEET_COMMUNICATION)
                ? 5 : 20;
        if (ticksExisted % heartbeatInterval == 0) heartbeatRegistry();
        if (dataManager.get(ATTACK_ANIMATION_TICKS) > 0) {
            dataManager.set(ATTACK_ANIMATION_TICKS, dataManager.get(ATTACK_ANIMATION_TICKS) - 1);
        }
        if (dataManager.get(FISHING_PULL_TICKS) > 0) {
            dataManager.set(FISHING_PULL_TICKS, dataManager.get(FISHING_PULL_TICKS) - 1);
        }
        if (dataManager.get(FISHING_CAST_TICKS) > 0) {
            dataManager.set(FISHING_CAST_TICKS, dataManager.get(FISHING_CAST_TICKS) - 1);
        }
        if (dataManager.get(ALCHEMY_ANIMATION_TICKS) > 0) {
            dataManager.set(ALCHEMY_ANIMATION_TICKS, dataManager.get(ALCHEMY_ANIMATION_TICKS) - 1);
        }
        if (rotorResyncTicks > 0) {
            // Repeat the authoritative value across several metadata packets. A false->true pair in one
            // server tick can otherwise collapse before a tracking client observes the resume transition.
            dataManager.setDirty(ROTORS_ACTIVE);
            dataManager.setDirty(HAS_ROTOR_POWER);
            rotorResyncTicks--;
        }
        if (attackCooldownTicks > 0) attackCooldownTicks--;
        if (ticksExisted % 100 == 0 && getHealth() < getMaxHealth()
                && DroneHardwareStats.hasUpgrade(upgrades, DroneUpgradeType.SELF_REPAIR)
                && consumeEnergy(DroneEnergyCosts.SELF_REPAIR)) {
            heal(1.0F);
        }
        if (ticksExisted % 5 == 0) {
            syncHeldWeapon();
            syncFishingRod();
            syncAlchemyJar();
        }
        if (dockRecoveryCooldown > 0) dockRecoveryCooldown--;
        pickupActionThisTick = false;
        if (tickSafetyFirmware()) {
            if (ticksExisted % 10 == 0) {
                syncEnergyPercent();
                syncRuntimeState();
                syncHardwareState();
            }
            syncWorldPreviewState();
            tickSemanticSounds();
            return;
        }
        long energyBeforeTick = energy.getStored();
        UUID runtimeNodeBeforeTick = runtime == null ? null : runtime.getCurrentNodeId();
        int runtimeAreaTotalBeforeTick = runtime == null ? 0 : runtime.getCurrentAreaTotal();
        boolean rotorsActive = areVisualRotorsActive();
        long idleCost = rotorsActive ? DroneEnergyCosts.IDLE_PER_TICK
                : ticksExisted % 4 == 0 ? DroneEnergyCosts.IDLE_PER_TICK : 0L;
        if (consumeEnergy(idleCost)) {
            if (rotorsActive) {
                motionY *= 0.6D;
            } else {
                motionX *= 0.25D;
                motionZ *= 0.25D;
                motionY = Math.max(motionY - 0.04D, -0.12D);
            }
            boolean logisticsActive = tickAssignedLogisticsJob();
            if (!logisticsActive && runtime != null && executionEnabled) {
                runtime.tick();
                recordDebugTaskEnergy(runtimeNodeBeforeTick, runtimeAreaTotalBeforeTick,
                        Math.max(0L, energyBeforeTick - energy.getStored()));
                if (runtime.getStatus() == DroneRuntimeStatus.COMPLETED && loopProgram) {
                    runtime.restart();
                } else if (runtime.getStatus() == DroneRuntimeStatus.COMPLETED
                        || runtime.getStatus() == DroneRuntimeStatus.ERROR) {
                    executionEnabled = false;
                }
            }
            if (rotorsActive && !pickupActionThisTick && !logisticsActive) collectNearbyItems();
        } else {
            motionY -= 0.04D;
        }
        if (runtime == null || !executionEnabled || runtime.getStatus() != DroneRuntimeStatus.RUNNING) {
            setVisualEntityTarget(null);
        }
        if (ticksExisted % 10 == 0) {
            syncEnergyPercent();
            syncRuntimeState();
            syncHardwareState();
        }
        syncWorldPreviewState();
        tickSemanticSounds();
    }

    private void syncWorldPreviewState() {
        NBTTagCompound state = new NBTTagCompound();
        BlockPos coordinate = runtime == null ? null : runtime.getCurrentLoopCoordinate();
        if (coordinate != null) state.setLong("Coordinate", coordinate.toLong());
        if (runtime != null && debugPathTarget != null
                && (DrTechDroneNodes.PLACE_BLOCK.getPath().equals(runtime.getCurrentNodeType())
                || DrTechDroneNodes.PLACE_AREA.getPath().equals(runtime.getCurrentNodeType()))) {
            state.setLong("PlacementTarget", debugPathTarget.toLong());
        }
        NBTTagList path = new NBTTagList();
        for (int index = 0; index < Math.min(256, debugPathPoints.size()); index++) {
            NBTTagCompound point = new NBTTagCompound();
            point.setLong("Position", debugPathPoints.get(index).toLong());
            path.appendTag(point);
        }
        state.setTag("Path", path);
        dataManager.set(WORLD_PREVIEW_STATE, state);
    }

    public NBTTagCompound getWorldPreviewState() {
        return dataManager.get(WORLD_PREVIEW_STATE).copy();
    }

    private void heartbeatRegistry() {
        DroneSafetyState safetyState = safetyFirmware.getState();
        String status = safetyState.name();
        String programId = "";
        long revision = 0L;
        if (runtime != null) {
            if (safetyState == DroneSafetyState.PROGRAM) status = runtime.getStatus().name();
            if (runtime.getProgramId() != null) programId = runtime.getProgramId().toString();
            revision = runtime.getProgramRevision();
        }
        UUID ownerId = getOwnerId();
        if (ownerId != null) {
            DroneFleetState fleet = DroneFleetState.get(world);
            DroneJob logistics = fleet.findAssignedLogisticsJob(ownerId, droneId).orElse(null);
            if (logistics == null) logistics = fleet.findAssignedLogisticsRecovery(ownerId, droneId).orElse(null);
            if (logistics != null) {
                status = "LOGISTICS_" + logistics.getLogisticsStage().name();
            } else if (fleet.findRetainedLogisticsJob(ownerId, droneId).isPresent()) {
                status = "LOGISTICS_RETRY_WAIT";
            }
        }
        DroneRegistry.get(world).heartbeat(new DroneRegistryRecord(droneId,
                world.provider.getDimension(), new BlockPos(posX, posY, posZ), getOwnerId(), chassis.name(),
                energy.getStored(), energy.getCapacity(), status, programId,
                countOccupiedCargoSlots(), getActiveCargoSlots(), revision, world.getTotalWorldTime(), true, boundDock,
                getVisualUpgradeMask()));
    }

    private int countOccupiedCargoSlots() {
        int occupied = 0;
        for (int slot = 0; slot < getActiveCargoSlots(); slot++) {
            if (!inventory.getStackInSlot(slot).isEmpty()) occupied++;
        }
        return occupied;
    }

    /** Executes an assigned native logistics job without advancing the loaded user program. */
    private boolean tickAssignedLogisticsJob() {
        UUID ownerId = getOwnerId();
        if (ownerId == null) return false;
        DroneFleetState fleet = DroneFleetState.get(world);
        DroneJob job = fleet.findAssignedLogisticsJob(ownerId, droneId).orElse(null);
        if (job == null) {
            DroneJob recovery = fleet.findAssignedLogisticsRecovery(ownerId, droneId).orElse(null);
            if (recovery != null) return tickFailedLogisticsRecovery();
            if (fleet.findRetainedLogisticsJob(ownerId, droneId).isPresent()) {
                if (!areVisualRotorsActive()) forceRotorsActiveSync();
                pickupActionThisTick = true;
                motionX *= 0.25D;
                motionY *= 0.25D;
                motionZ *= 0.25D;
                return true;
            }
            return false;
        }
        if (ticksExisted % 20 == 0) {
            fleet.touchLogisticsJob(ownerId, job.getJobId(), world.getTotalWorldTime());
        }
        if (!areVisualRotorsActive()) forceRotorsActiveSync();
        DroneReservation reservation = fleet.getReservationForJob(ownerId, job.getJobId()).orElse(null);
        DroneEndpointNetwork network = DroneEndpointNetwork.get(world);
        DroneEndpoint source = network.getEndpoint(job.getSourceEndpointId());
        DroneEndpoint target = network.getEndpoint(job.getTargetEndpointId());
        boolean carrying = job.getPickedAmount() > job.getDeliveredAmount();
        if (reservation == null || !validLiveLogisticsEndpoint(target, job)
                || !carrying && !validLiveLogisticsEndpoint(source, job)) {
            fleet.failJob(ownerId, job.getJobId(), world.getTotalWorldTime(), "ENDPOINT_OR_RESERVATION_UNAVAILABLE");
            return true;
        }
        MetaTileEntityDroneEndpoint sourceMte = carrying ? null : findLogisticsEndpoint(source);
        MetaTileEntityDroneEndpoint targetMte = findLogisticsEndpoint(target);
        if (targetMte == null || !carrying && sourceMte == null) {
            fleet.failJob(ownerId, job.getJobId(), world.getTotalWorldTime(), "ENDPOINT_TILE_UNAVAILABLE");
            return true;
        }
        switch (job.getLogisticsStage()) {
            case DISCOVER, RESERVED, TO_SOURCE -> {
                DroneExecutionResult movement = moveToLogisticsEndpoint(source.getPosition());
                if (movement.getState() == DroneActionState.RUNNING) return true;
                if (movement.getState() != DroneActionState.SUCCESS) {
                    fleet.failJob(ownerId, job.getJobId(), world.getTotalWorldTime(),
                            "SOURCE_" + movement.getStatus().name());
                    return true;
                }
                fleet.setLogisticsStage(ownerId, job.getJobId(), DroneJob.LogisticsStage.PICKUP);
            }
            case PICKUP -> {
                if (sourceMte == null) {
                    fleet.failJob(ownerId, job.getJobId(), world.getTotalWorldTime(), "SOURCE_TILE_UNAVAILABLE");
                    return true;
                }
                long remaining = Math.max(0L, reservation.getAmount() - job.getPickedAmount());
                if (remaining <= 0L) {
                    fleet.setLogisticsStage(ownerId, job.getJobId(), DroneJob.LogisticsStage.TO_TARGET);
                    return true;
                }
                long batch = Math.min(remaining, logisticsBatchLimit(job.getResourceKind()));
                if (!hasEnergyFor(logisticsTransferEnergyCost(job.getResourceKind(), batch))) {
                    fleet.failJob(ownerId, job.getJobId(), world.getTotalWorldTime(), "TRANSFER_NO_ENERGY");
                    return true;
                }
                long moved = pickupEndpointResource(sourceMte, job.getResourceKind(), job.getResourceId(), batch);
                if (moved > 0L) {
                    consumeEnergy(logisticsTransferEnergyCost(job.getResourceKind(), moved));
                    FluidStack movedFluid = job.getResourceKind() == DroneEndpoint.Kind.FLUID
                            && matchesLogisticsFluid(fluidTank.getFluid(), job.getResourceId())
                                    ? fluidTank.getFluid().copy() : null;
                    emitLogisticsTransferFeedback(job.getResourceKind(), source.getPosition(), movedFluid, true);
                    fleet.recordLogisticsPickup(ownerId, job.getJobId(), moved);
                    if (job.getPickedAmount() >= reservation.getAmount()) {
                        fleet.setLogisticsStage(ownerId, job.getJobId(), DroneJob.LogisticsStage.TO_TARGET);
                    }
                    return true;
                }
                if (job.getPickedAmount() > job.getDeliveredAmount()) {
                    fleet.setLogisticsStage(ownerId, job.getJobId(), DroneJob.LogisticsStage.TO_TARGET);
                } else {
                    fleet.failJob(ownerId, job.getJobId(), world.getTotalWorldTime(),
                            "SOURCE_EMPTY_OR_TRANSPORT_FULL");
                }
            }
            case TO_TARGET -> {
                DroneExecutionResult movement = moveToLogisticsEndpoint(target.getPosition());
                if (movement.getState() == DroneActionState.RUNNING) return true;
                if (movement.getState() != DroneActionState.SUCCESS) {
                    fleet.failJob(ownerId, job.getJobId(), world.getTotalWorldTime(),
                            "TARGET_" + movement.getStatus().name());
                    return true;
                }
                fleet.setLogisticsStage(ownerId, job.getJobId(), DroneJob.LogisticsStage.DELIVER);
            }
            case DELIVER -> {
                long remaining = Math.max(0L, job.getPickedAmount() - job.getDeliveredAmount());
                if (remaining <= 0L) {
                    if (job.getDeliveredAmount() >= reservation.getAmount()) {
                        fleet.completeJob(ownerId, job.getJobId());
                    } else {
                        fleet.setLogisticsStage(ownerId, job.getJobId(), DroneJob.LogisticsStage.TO_SOURCE);
                    }
                    return true;
                }
                long batch = Math.min(remaining, logisticsBatchLimit(job.getResourceKind()));
                FluidStack movedFluid = job.getResourceKind() == DroneEndpoint.Kind.FLUID
                        && matchesLogisticsFluid(fluidTank.getFluid(), job.getResourceId())
                                ? fluidTank.getFluid().copy() : null;
                if (!hasEnergyFor(logisticsTransferEnergyCost(job.getResourceKind(), batch))) {
                    fleet.failJob(ownerId, job.getJobId(), world.getTotalWorldTime(), "TRANSFER_NO_ENERGY");
                    return true;
                }
                long moved = deliverEndpointResource(targetMte, job.getResourceKind(), job.getResourceId(), batch);
                if (moved > 0L) {
                    consumeEnergy(logisticsTransferEnergyCost(job.getResourceKind(), moved));
                    emitLogisticsTransferFeedback(job.getResourceKind(), target.getPosition(), movedFluid, false);
                    fleet.recordLogisticsDelivery(ownerId, job.getJobId(), moved);
                    if (job.getDeliveredAmount() >= job.getPickedAmount()) {
                        if (job.getDeliveredAmount() >= reservation.getAmount()) {
                            fleet.completeJob(ownerId, job.getJobId());
                        } else {
                            fleet.setLogisticsStage(ownerId, job.getJobId(), DroneJob.LogisticsStage.TO_SOURCE);
                        }
                    }
                    return true;
                }
                fleet.failJob(ownerId, job.getJobId(), world.getTotalWorldTime(),
                        "TARGET_FULL_OR_TRANSPORT_MISSING");
            }
            case RETURNING -> fleet.completeJob(ownerId, job.getJobId());
            default -> fleet.failJob(ownerId, job.getJobId(), world.getTotalWorldTime(), "INVALID_LOGISTICS_STAGE");
        }
        return true;
    }

    /** Returns a terminal job's retained payload to a real dock without mutating or discarding it. */
    private boolean tickFailedLogisticsRecovery() {
        if (!areVisualRotorsActive()) forceRotorsActiveSync();
        pickupActionThisTick = true;
        MetaTileEntityDroneDock dock = findDroneDock(boundDock);
        if (dock == null || !dock.isAvailableForDrone() || !dock.isAutoRecover()) {
            if (dockRecoveryCooldown <= 0) {
                selectFallbackDock();
                dockRecoveryCooldown = 20;
                dock = findDroneDock(boundDock);
            }
        }
        if (dock == null || !dock.isAvailableForDrone() || !dock.isAutoRecover()) {
            motionX *= 0.25D;
            motionY *= 0.25D;
            motionZ *= 0.25D;
            return true;
        }
        DroneExecutionResult movement = getSafetyNavigation().moveTo(boundDock.up());
        if (movement.getState() == DroneActionState.SUCCESS) {
            dataManager.set(FLIGHT_MARKER, DroneVisualState.FlightMarker.LANDING.ordinal());
            flightMarkerTicks = 20;
            dock.acceptRecoveredDrone(this, false);
        } else if (movement.getState() != DroneActionState.RUNNING && dockRecoveryCooldown <= 0) {
            selectFallbackDock();
            dockRecoveryCooldown = 20;
        }
        return true;
    }

    private boolean validLiveLogisticsEndpoint(DroneEndpoint endpoint, DroneJob job) {
        return endpoint != null && endpoint.getKind() == job.getResourceKind()
                && endpoint.getDimension() == world.provider.getDimension()
                && java.util.Objects.equals(endpoint.getOwnerId(), job.getOwnerId())
                && DroneEndpointNetwork.isOnline(endpoint, world.getTotalWorldTime());
    }

    @Nullable
    private MetaTileEntityDroneEndpoint findLogisticsEndpoint(DroneEndpoint endpoint) {
        if (endpoint == null || !world.isBlockLoaded(endpoint.getPosition())) return null;
        TileEntity tile = world.getTileEntity(endpoint.getPosition());
        if (!(tile instanceof IGregTechTileEntity holder)
                || !(holder.getMetaTileEntity() instanceof MetaTileEntityDroneEndpoint result)) return null;
        return endpoint.getEndpointId().equals(result.getEndpointId()) && endpoint.getKind() == result.getKind()
                && java.util.Objects.equals(endpoint.getOwnerId(), result.getOwnerId()) ? result : null;
    }

    private DroneExecutionResult moveToLogisticsEndpoint(BlockPos endpointPosition) {
        DroneArea neighborhood = DroneArea.between(endpointPosition.add(-1, -1, -1),
                endpointPosition.add(1, 1, 1));
        BlockPos stand = getSafetyNavigation().findNearestPassablePosition(neighborhood,
                new long[] { endpointPosition.toLong() });
        return stand == null ? DroneExecutionResult.failure(DroneActionStatus.UNREACHABLE, "failed",
                "No passable logistics interaction position") : getSafetyNavigation().moveTo(stand);
    }

    private long logisticsBatchLimit(DroneEndpoint.Kind kind) {
        if (kind == DroneEndpoint.Kind.ITEM) {
            return DroneHardwareStats.hasUpgrade(upgrades, DroneUpgradeType.ADVANCED_ITEM_HANDLING) ? 256L : 64L;
        }
        if (kind == DroneEndpoint.Kind.FLUID) return 1_000L;
        long voltage = GTValues.V[chassis.getVoltageTier()];
        return Math.max(1L, voltage * DroneHardwareStats.euTransferAmperage(upgrades));
    }

    private long logisticsTransferEnergyCost(DroneEndpoint.Kind kind, long amount) {
        if (kind == DroneEndpoint.Kind.ITEM) return DroneEnergyCosts.logisticsItems(amount);
        if (kind == DroneEndpoint.Kind.FLUID) return DroneEnergyCosts.logisticsFluid(amount);
        return DroneEnergyCosts.logisticsEu(amount, GTValues.V[chassis.getVoltageTier()]);
    }

    private long pickupEndpointResource(MetaTileEntityDroneEndpoint endpoint, DroneEndpoint.Kind kind,
            String resourceId, long maximum) {
        if (kind == DroneEndpoint.Kind.ITEM) {
            return pickupEndpointItems(endpoint, resourceId, (int) Math.min(Integer.MAX_VALUE, maximum));
        }
        if (kind == DroneEndpoint.Kind.FLUID) {
            return pickupEndpointFluid(endpoint, resourceId, (int) Math.min(Integer.MAX_VALUE, maximum));
        }
        return pickupEndpointEu(endpoint, maximum);
    }

    private long deliverEndpointResource(MetaTileEntityDroneEndpoint endpoint, DroneEndpoint.Kind kind,
            String resourceId, long maximum) {
        if (kind == DroneEndpoint.Kind.ITEM) {
            return deliverEndpointItems(endpoint, resourceId, (int) Math.min(Integer.MAX_VALUE, maximum));
        }
        if (kind == DroneEndpoint.Kind.FLUID) {
            return deliverEndpointFluid(endpoint, resourceId, (int) Math.min(Integer.MAX_VALUE, maximum));
        }
        return deliverEndpointEu(endpoint, maximum);
    }

    private long pickupEndpointFluid(MetaTileEntityDroneEndpoint endpoint, String resourceId, int maximum) {
        IFluidHandler source = endpoint.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null);
        if (source == null || maximum <= 0 || fluidTank.getCapacity() <= 0) return 0L;
        FluidStack request = findLogisticsFluid(source, resourceId, maximum);
        if (request == null) return 0L;
        FluidStack available = source.drain(request, false);
        if (available == null || available.amount <= 0) return 0L;
        int accepted = fluidTank.fill(available, false);
        if (accepted <= 0) return 0L;
        FluidStack extractRequest = available.copy();
        extractRequest.amount = Math.min(accepted, available.amount);
        FluidStack extracted = source.drain(extractRequest, true);
        if (extracted == null || extracted.amount <= 0) return 0L;
        int inserted = fluidTank.fill(extracted, true);
        if (inserted < extracted.amount) {
            FluidStack remainder = extracted.copy();
            remainder.amount -= inserted;
            source.fill(remainder, true);
        }
        return inserted;
    }

    private long deliverEndpointFluid(MetaTileEntityDroneEndpoint endpoint, String resourceId, int maximum) {
        IFluidHandler target = endpoint.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null);
        FluidStack stored = fluidTank.getFluid();
        if (target == null || maximum <= 0 || !matchesLogisticsFluid(stored, resourceId)) return 0L;
        FluidStack offered = stored.copy();
        offered.amount = Math.min(maximum, stored.amount);
        int accepted = target.fill(offered, false);
        if (accepted <= 0) return 0L;
        FluidStack extracted = fluidTank.drain(Math.min(accepted, offered.amount), true);
        if (extracted == null || extracted.amount <= 0) return 0L;
        int inserted = target.fill(extracted, true);
        if (inserted < extracted.amount) {
            FluidStack remainder = extracted.copy();
            remainder.amount -= inserted;
            fluidTank.fill(remainder, true);
        }
        return inserted;
    }

    @Nullable
    private static FluidStack findLogisticsFluid(IFluidHandler handler, String resourceId, int maximum) {
        for (IFluidTankProperties property : handler.getTankProperties()) {
            FluidStack contents = property.getContents();
            if (!property.canDrain() || !matchesLogisticsFluid(contents, resourceId)) continue;
            FluidStack request = contents.copy();
            request.amount = Math.min(maximum, contents.amount);
            return request;
        }
        return null;
    }

    private static boolean matchesLogisticsFluid(@Nullable FluidStack fluid, String resourceId) {
        return fluid != null && fluid.amount > 0 && fluid.getFluid() != null
                && fluid.getFluid().getName().equals(resourceId);
    }

    private long pickupEndpointEu(MetaTileEntityDroneEndpoint endpoint, long maximum) {
        long capacity = getConfiguredCapacity();
        long space = logisticsEuCargo >= capacity ? 0L : capacity - logisticsEuCargo;
        long moved = endpoint.extractLogisticsEu(Math.min(maximum, space));
        logisticsEuCargo += moved;
        return moved;
    }

    private long deliverEndpointEu(MetaTileEntityDroneEndpoint endpoint, long maximum) {
        long moved = endpoint.insertLogisticsEu(Math.min(maximum, logisticsEuCargo));
        logisticsEuCargo -= moved;
        return moved;
    }

    private long pickupEndpointItems(MetaTileEntityDroneEndpoint endpoint, String resourceId, int maximum) {
        IItemHandler source = endpoint.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
        if (source == null || maximum <= 0) return 0L;
        int movedTotal = 0;
        for (int slot = 0; slot < source.getSlots() && movedTotal < maximum; slot++) {
            ItemStack available = source.extractItem(slot, maximum - movedTotal, true);
            if (available.isEmpty() || !matchesLogisticsItem(available, resourceId)) continue;
            ItemStack cargoRemainder = insertCargo(available, true);
            int accepted = available.getCount() - cargoRemainder.getCount();
            if (accepted <= 0) continue;
            ItemStack extracted = source.extractItem(slot, accepted, false);
            if (extracted.isEmpty()) continue;
            ItemStack remainder = insertCargo(extracted, false);
            int inserted = extracted.getCount() - remainder.getCount();
            movedTotal += inserted;
            if (!remainder.isEmpty()) source.insertItem(slot, remainder, false);
        }
        return movedTotal;
    }

    private long deliverEndpointItems(MetaTileEntityDroneEndpoint endpoint, String resourceId, int maximum) {
        IItemHandler target = endpoint.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
        if (target == null || maximum <= 0) return 0L;
        int movedTotal = 0;
        for (int slot = 0; slot < getActiveCargoSlots() && movedTotal < maximum; slot++) {
            ItemStack available = inventory.extractItem(slot, maximum - movedTotal, true);
            if (available.isEmpty() || !matchesLogisticsItem(available, resourceId)) continue;
            ItemStack targetRemainder = insertIntoHandler(target, available, true);
            int accepted = available.getCount() - targetRemainder.getCount();
            if (accepted <= 0) continue;
            ItemStack extracted = inventory.extractItem(slot, accepted, false);
            if (extracted.isEmpty()) continue;
            ItemStack remainder = insertIntoHandler(target, extracted, false);
            int inserted = extracted.getCount() - remainder.getCount();
            movedTotal += inserted;
            if (!remainder.isEmpty()) insertCargo(remainder, false);
        }
        return movedTotal;
    }

    private ItemStack insertCargo(ItemStack stack, boolean simulate) {
        ItemStack remainder = stack.copy();
        for (int slot = 0; slot < getActiveCargoSlots() && !remainder.isEmpty(); slot++) {
            remainder = inventory.insertItem(slot, remainder, simulate);
        }
        return remainder;
    }

    private static ItemStack insertIntoHandler(IItemHandler handler, ItemStack stack, boolean simulate) {
        ItemStack remainder = stack.copy();
        for (int slot = 0; slot < handler.getSlots() && !remainder.isEmpty(); slot++) {
            remainder = handler.insertItem(slot, remainder, simulate);
        }
        return remainder;
    }

    private static boolean matchesLogisticsItem(ItemStack stack, String resourceId) {
        ResourceLocation id = stack.isEmpty() ? null : Item.REGISTRY.getNameForObject(stack.getItem());
        return id != null && id.toString().equals(resourceId);
    }

    /** Builds a conservative estimate only for bounded area actions with real completed samples. */
    private void recordDebugTaskEnergy(UUID nodeId, int areaTotalBeforeTick, long consumedEu) {
        if (runtime == null || nodeId == null) return;
        if (!nodeId.equals(debugEnergyNodeId)) {
            debugEnergyNodeId = nodeId;
            debugEnergyAreaTotal = Math.max(0, areaTotalBeforeTick);
            debugEnergyAreaCompleted = 0;
            debugEnergyAreaConsumed = 0L;
            debugEnergyAreaRemainingEstimate = -1L;
        }
        if (areaTotalBeforeTick <= 0) return;
        debugEnergyAreaTotal = areaTotalBeforeTick;
        debugEnergyAreaConsumed = Math.min(Long.MAX_VALUE / 2L, debugEnergyAreaConsumed + consumedEu);
        int completed = nodeId.equals(runtime.getCurrentNodeId())
                ? runtime.getCurrentAreaIndex() : areaTotalBeforeTick;
        debugEnergyAreaCompleted = Math.max(debugEnergyAreaCompleted,
                Math.min(debugEnergyAreaTotal, Math.max(0, completed)));
        if (debugEnergyAreaCompleted > 0) {
            long perPosition = Math.max(1L, debugEnergyAreaConsumed / debugEnergyAreaCompleted);
            long remaining = Math.max(0L, debugEnergyAreaTotal - debugEnergyAreaCompleted);
            debugEnergyAreaRemainingEstimate = remaining > Long.MAX_VALUE / perPosition
                    ? Long.MAX_VALUE : remaining * perPosition;
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
        if (reached && safetyFirmware.getState() == DroneSafetyState.RETURNING) {
            dataManager.set(FLIGHT_MARKER, DroneVisualState.FlightMarker.LANDING.ordinal());
            flightMarkerTicks = 20;
            playCue(DroneSoundCue.LANDING, 0.85F, 0.9F);
            if (world instanceof WorldServer && ticksExisted % 5 == 0) {
                ((WorldServer) world).spawnParticle(EnumParticleTypes.CLOUD,
                        posX, posY - 0.15D, posZ, 5, 0.18D, 0.03D, 0.18D, 0.01D);
            }
        }
        DroneSafetyState state = safetyFirmware.evaluate(percent, boundDock != null,
                dock != null && dock.isAvailableForDrone(), reached);
        dataManager.set(SAFETY_STATE, state.ordinal());
        if (state == DroneSafetyState.PROGRAM) return false;
        if (!areVisualRotorsActive()) dataManager.set(ROTORS_ACTIVE, true);
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

    private void tickSemanticSounds() {
        if (world.isRemote) return;
        if (areVisualRotorsActive() && energy.getStored() > 0L) {
            playCue(DroneSoundCue.ROTOR_LOOP, 0.22F, 1.45F + chassis.getMetadata() * 0.08F);
        }
        DroneRuntimeStatus current = runtime == null ? DroneRuntimeStatus.READY : runtime.getStatus();
        if (current != lastSoundRuntimeStatus) {
            if (current == DroneRuntimeStatus.RUNNING) playCue(DroneSoundCue.PROGRAM_START, 0.6F, 1.2F);
            else if (current == DroneRuntimeStatus.COMPLETED) playCue(DroneSoundCue.PROGRAM_END, 0.7F, 1.25F);
            else if (current == DroneRuntimeStatus.ERROR) playCue(DroneSoundCue.ERROR, 0.8F, 0.75F);
            lastSoundRuntimeStatus = current;
        }
        if (actualEnergyPercent() <= Math.max(10, safetyFirmware.getReturnAtPercent())) {
            playCue(DroneSoundCue.LOW_ENERGY, 0.55F, 0.65F);
        }
        if (safetyFirmware.getState() == DroneSafetyState.CHARGING) {
            playCue(DroneSoundCue.EU_CHARGE, 0.24F, 1.7F);
        }
    }

    private void tickClientVisualEffects() {
        if (getHealth() <= 0.0F || ticksExisted % 4 != 0) return;
        float damage = 1.0F - getHealth() / Math.max(1.0F, getMaxHealth());
        if (damage < 0.5F) return;
        double spread = 0.12D + damage * 0.18D;
        world.spawnParticle(damage >= 0.75F ? EnumParticleTypes.SMOKE_LARGE : EnumParticleTypes.SMOKE_NORMAL,
                posX + (rand.nextDouble() - 0.5D) * spread, posY + 0.15D,
                posZ + (rand.nextDouble() - 0.5D) * spread,
                motionX * 0.05D, 0.025D + damage * 0.025D, motionZ * 0.05D);
    }

    private void playCue(DroneSoundCue cue, float volume, float pitch) {
        DroneSoundPlayer.play(world, droneId, cue, posX, posY, posZ, volume, pitch);
    }

    private boolean selectFallbackDock() {
        DroneDockNetwork network = DroneDockNetwork.get(world);
        for (UUID preferredId : fallbackDockIds) {
            BlockPos preferred = network.findPreferred(Collections.singletonList(preferredId),
                    world.provider.getDimension(), getOwnerId(), chassis.getVoltageTier(),
                    world.getTotalWorldTime(), true, null).map(DroneDockRecord::getPosition).orElse(null);
            if (preferred != null && bindToDockPosition(preferred)) return true;
        }
        BlockPos selected = findNearestNetworkDock(true);
        return selected != null && bindToDockPosition(selected);
    }

    /** Emits a short, rate-limited colored transfer trace visible to nearby players. */
    private void emitFluidTransferEffect(BlockPos target, FluidStack fluid, boolean importing) {
        if (!(world instanceof WorldServer) || fluid == null || fluid.getFluid() == null
                || ticksExisted - lastFluidEffectTick < 4) return;
        lastFluidEffectTick = ticksExisted;
        int color = fluid.getFluid().getColor(fluid);
        double red = Math.max(0.01D, ((color >> 16) & 0xFF) / 255.0D);
        double green = Math.max(0.01D, ((color >> 8) & 0xFF) / 255.0D);
        double blue = Math.max(0.01D, (color & 0xFF) / 255.0D);
        double targetX = target.getX() + 0.5D;
        double targetY = target.getY() + 0.65D;
        double targetZ = target.getZ() + 0.5D;
        double droneX = posX;
        double droneY = posY + height * 0.45D;
        double droneZ = posZ;
        WorldServer server = (WorldServer) world;
        for (int step = 1; step <= 4; step++) {
            double progress = step / 5.0D;
            if (!importing) progress = 1.0D - progress;
            server.spawnParticle(EnumParticleTypes.SPELL_MOB,
                    targetX + (droneX - targetX) * progress,
                    targetY + (droneY - targetY) * progress,
                    targetZ + (droneZ - targetZ) * progress,
                    0, red, green, blue, 1.0D);
        }
        world.playSound(null, target, importing ? SoundEvents.ITEM_BUCKET_FILL : SoundEvents.ITEM_BUCKET_EMPTY,
                SoundCategory.BLOCKS, 0.35F, 1.15F + rand.nextFloat() * 0.15F);
    }

    /** Shows that a native logistics transfer actually moved payload, without producing per-tick sound spam. */
    private void emitLogisticsTransferFeedback(DroneEndpoint.Kind kind, BlockPos endpoint,
            @Nullable FluidStack fluid, boolean importing) {
        pickupActionThisTick = true;
        if (kind == DroneEndpoint.Kind.FLUID) {
            emitFluidTransferEffect(endpoint, fluid, importing);
            return;
        }
        if (!(world instanceof WorldServer) || ticksExisted - lastLogisticsEffectTick < 4) return;
        lastLogisticsEffectTick = ticksExisted;
        playCue(kind == DroneEndpoint.Kind.EU ? DroneSoundCue.EU_CHARGE : DroneSoundCue.TOOL_ARM,
                kind == DroneEndpoint.Kind.EU ? 0.28F : 0.45F,
                kind == DroneEndpoint.Kind.EU ? 1.65F : 1.1F);
        WorldServer server = (WorldServer) world;
        double endpointX = endpoint.getX() + 0.5D;
        double endpointY = endpoint.getY() + 0.65D;
        double endpointZ = endpoint.getZ() + 0.5D;
        for (int step = 1; step <= 4; step++) {
            double progress = step / 5.0D;
            if (!importing) progress = 1.0D - progress;
            server.spawnParticle(kind == DroneEndpoint.Kind.EU
                            ? EnumParticleTypes.FIREWORKS_SPARK : EnumParticleTypes.CRIT,
                    endpointX + (posX - endpointX) * progress,
                    endpointY + (posY + height * 0.45D - endpointY) * progress,
                    endpointZ + (posZ - endpointZ) * progress,
                    1, 0.015D, 0.015D, 0.015D, 0.01D);
        }
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

    @Nullable
    private IWorkable findMachineWorkable(BlockPos target) {
        if (target == null || !world.isBlockLoaded(target)) return null;
        TileEntity tile = world.getTileEntity(target);
        if (tile == null) return null;
        IWorkable workable = tile.getCapability(GregtechTileCapabilities.CAPABILITY_WORKABLE, null);
        if (workable != null) return workable;
        for (EnumFacing side : EnumFacing.values()) {
            workable = tile.getCapability(GregtechTileCapabilities.CAPABILITY_WORKABLE, side);
            if (workable != null) return workable;
        }
        return null;
    }

    @Nullable
    private AbstractRecipeLogic findMachineRecipeLogic(BlockPos target) {
        if (target == null || !world.isBlockLoaded(target)) return null;
        TileEntity tile = world.getTileEntity(target);
        if (tile == null) return null;
        AbstractRecipeLogic logic = tile.getCapability(GregtechTileCapabilities.CAPABILITY_RECIPE_LOGIC, null);
        if (logic != null) return logic;
        for (EnumFacing side : EnumFacing.values()) {
            logic = tile.getCapability(GregtechTileCapabilities.CAPABILITY_RECIPE_LOGIC, side);
            if (logic != null) return logic;
        }
        return null;
    }

    @Nullable
    private IMaintenance findMachineMaintenance(BlockPos target) {
        if (target == null || !world.isBlockLoaded(target)) return null;
        TileEntity tile = world.getTileEntity(target);
        if (tile == null) return null;
        IMaintenance maintenance = tile.getCapability(GregtechTileCapabilities.CAPABILITY_MAINTENANCE, null);
        if (maintenance != null) return maintenance;
        if (tile instanceof IGregTechTileEntity) {
            MetaTileEntity metaTileEntity = ((IGregTechTileEntity) tile).getMetaTileEntity();
            if (metaTileEntity instanceof IMaintenance) return (IMaintenance) metaTileEntity;
        }
        return null;
    }

    private static boolean isOutputDiagnostic(String diagnostic) {
        if (diagnostic == null || diagnostic.isEmpty()) return false;
        return diagnostic.contains("输出仓已满")
                || diagnostic.contains("物品输出条件")
                || diagnostic.contains("流体输出条件")
                || diagnostic.contains("输出数量超过机器限制");
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

    @Override
    public boolean canBreatheUnderwater() {
        return DroneHardwareStats.hasUpgrade(upgrades, DroneUpgradeType.WATERPROOF)
                || super.canBreatheUnderwater();
    }

    @Override
    public boolean isPushedByWater() {
        return !DroneHardwareStats.hasUpgrade(upgrades, DroneUpgradeType.WATERPROOF)
                && super.isPushedByWater();
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        Entity attacker = source == null ? null : source.getTrueSource();
        if (!world.isRemote && attacker instanceof EntityPlayer player
                && DroneHardwareStats.hasUpgrade(upgrades, DroneUpgradeType.SECURE_ACCESS)
                && !isOwner(player)) return false;
        boolean accepted = super.attackEntityFrom(source, amount);
        if (accepted && !world.isRemote) playCue(DroneSoundCue.DAMAGED, 0.75F, 0.9F + rand.nextFloat() * 0.2F);
        return accepted;
    }

    public void recallToPlayer(EntityPlayer player) {
        if (recalledOrDropped || world.isRemote) {
            return;
        }
        recalledOrDropped = true;
        ItemStack stack = createDroneItem();
        settleLogisticsForRecoveredItem();
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
        flightMarkerTicks = 30;
        dataManager.set(FLIGHT_MARKER, DroneVisualState.FlightMarker.TAKEOFF.ordinal());
        executionEnabled = runtime != null && runtime.activateForDockLaunch();
        syncRuntimeState();
        playCue(DroneSoundCue.TAKEOFF, 0.9F, 1.0F);
        if (world instanceof WorldServer) {
            ((WorldServer) world).spawnParticle(EnumParticleTypes.FIREWORKS_SPARK,
                    posX, posY - 0.15D, posZ, 12, 0.25D, 0.05D, 0.25D, 0.02D);
        }
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
        settleLogisticsForRecoveredItem();
        recalledOrDropped = true;
        setDead();
    }

    /** Closes the task only after its physical payload has been preserved in the recovered drone item. */
    private void settleLogisticsForRecoveredItem() {
        UUID ownerId = getOwnerId();
        if (ownerId != null) {
            DroneFleetState fleet = DroneFleetState.get(world);
            DroneJob active = fleet.findAssignedLogisticsJob(ownerId, droneId).orElse(null);
            if (active != null) fleet.cancelJob(ownerId, active.getJobId());
            DroneJob recovery = fleet.findAssignedLogisticsRecovery(ownerId, droneId).orElse(null);
            if (recovery != null) fleet.finishLogisticsRecovery(ownerId, recovery.getJobId());
        }
    }

    @Override
    public void onDeath(DamageSource cause) {
        if (!world.isRemote && !recalledOrDropped) {
            playCue(DroneSoundCue.DEATH, 1.0F, 0.8F);
            UUID ownerId = getOwnerId();
            if (ownerId != null) {
                DroneFleetState fleet = DroneFleetState.get(world);
                DroneJob active = fleet.findAssignedLogisticsJob(ownerId, droneId).orElse(null);
                if (active != null) {
                    fleet.failJob(ownerId, active.getJobId(), world.getTotalWorldTime(), "DRONE_DESTROYED");
                }
            }
            recalledOrDropped = true;
            ItemStack dropped = createDroneItem();
            unbindCurrentDock();
            entityDropItem(dropped, 0.2F);
        }
        super.onDeath(cause);
    }

    @Override
    public void setDead() {
        cancelFishingHook();
        super.setDead();
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
        DroneItemData.setAutoPickupMode(stack, autoPickupMode.name());
        DroneItemData.setAutoPickupFilter(stack, autoPickupFilter);
        DroneItemData.setCargoFullPolicy(stack, cargoFullPolicy.name());
        DroneItemData.setInventory(stack, inventory.serializeNBT());
        DroneItemData.setWeapons(stack, weapons.serializeNBT());
        DroneItemData.setFishingRod(stack, fishingRod.serializeNBT());
        DroneItemData.setAlchemyJar(stack, alchemyJar.serializeNBT());
        DroneItemData.setFluid(stack, fluidTank.writeToNBT(new NBTTagCompound()));
        DroneItemData.setLogisticsEu(stack, logisticsEuCargo);
        DroneItemData.setDock(stack, boundDock, world.provider.getDimension());
        DroneItemData.setFallbackDocks(stack, fallbackDockIds);
        DroneItemData.setLoadedEntity(stack, loadedEntityData, loadedEntityUuid);
        DroneItemData.setEntityTargetLock(stack, true, followTargetUuid, followTargetAnchor);
        DroneItemData.setEntityTargetLock(stack, false, avoidTargetUuid, avoidTargetAnchor);
        DroneItemData.setAttackTargetLock(stack, attackTargetUuid, attackTargetAnchor);
        DroneItemData.setInteractionTargetLock(stack, false, interactionTargetUuid, interactionTargetAnchor);
        DroneItemData.setInteractionTargetLock(stack, true, loadTargetUuid, loadTargetAnchor);
        if (hasCustomName()) stack.setStackDisplayName(getCustomNameTag());
        DroneItemData.setStatusLabel(stack, getVisualStatusLabel());
        DroneItemData.setRotorsActive(stack, areVisualRotorsActive());
        DroneItemData.setStatusLightMode(stack, getVisualStatusLightMode());
        return stack;
    }

    public void initializeFromItem(ItemStack source, @Nullable UUID ownerId) {
        DroneItemData.migrateInPlace(source, ownerId);
        this.droneId = DroneItemData.getOrCreateDroneId(source);
        this.chassis = DroneItemData.getChassis(source);
        loadingHardware = true;
        DroneUpgradeDataCodec.readInto(DroneItemData.getUpgrades(source), this.upgrades);
        this.inventory.deserializeNBT(DroneItemData.getInventory(source));
        this.weapons.deserializeNBT(DroneItemData.getWeapons(source));
        this.fishingRod.deserializeNBT(DroneItemData.getFishingRod(source));
        this.alchemyJar.deserializeNBT(DroneItemData.getAlchemyJar(source));
        loadFluidTank(DroneItemData.getFluid(source));
        logisticsEuCargo = DroneItemData.getLogisticsEu(source);
        loadingHardware = false;
        IElectricItem electricItem = source.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
        long charge = electricItem == null ? 0L : electricItem.getCharge();
        this.energy = new DroneEnergyStorage(getConfiguredCapacity(), chassis.getVoltageTier(), charge);
        this.program = DroneItemData.getProgram(source);
        this.pendingRuntimeState = DroneItemData.getRuntime(source);
        this.safetyFirmware.readFromNbt(DroneItemData.getSafetyFirmware(source));
        this.autoPickupMode = DroneAutoPickupMode.fromName(DroneItemData.getAutoPickupMode(source));
        this.autoPickupFilter = DroneItemData.getAutoPickupFilter(source);
        this.cargoFullPolicy = DroneCargoFullPolicy.fromName(DroneItemData.getCargoFullPolicy(source));
        this.boundDock = DroneItemData.getDock(source, world.provider.getDimension());
        setFallbackDockIds(DroneItemData.getFallbackDocks(source));
        this.loadedEntityData = DroneItemData.getLoadedEntity(source);
        this.loadedEntityUuid = DroneItemData.getLoadedEntityUuid(source);
        this.followTargetUuid = DroneItemData.getEntityTargetId(source, true);
        this.followTargetAnchor = DroneItemData.getEntityTargetAnchor(source, true);
        this.avoidTargetUuid = DroneItemData.getEntityTargetId(source, false);
        this.avoidTargetAnchor = DroneItemData.getEntityTargetAnchor(source, false);
        this.attackTargetUuid = DroneItemData.getAttackTargetId(source);
        this.attackTargetAnchor = DroneItemData.getAttackTargetAnchor(source);
        this.interactionTargetUuid = DroneItemData.getInteractionTargetId(source, false);
        this.interactionTargetAnchor = DroneItemData.getInteractionTargetAnchor(source, false);
        this.loadTargetUuid = DroneItemData.getInteractionTargetId(source, true);
        this.loadTargetAnchor = DroneItemData.getInteractionTargetAnchor(source, true);
        if (source.hasDisplayName()) {
            setCustomNameTag(source.getDisplayName().replace('\u00a7', '?'));
            setAlwaysRenderNameTag(true);
        }
        dataManager.set(STATUS_LABEL, sanitizeStatusLabel(DroneItemData.getStatusLabel(source)));
        dataManager.set(ROTORS_ACTIVE, DroneItemData.areRotorsActive(source));
        dataManager.set(STATUS_LIGHT_MODE, DroneItemData.getStatusLightMode(source));
        if (loadedEntityData != null && (loadedEntityData.getString("id").isEmpty()
                || !isEntityPayloadWithinLimit(loadedEntityData))) {
            loadedEntityData = null;
            loadedEntityUuid = null;
        }
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

    public List<UUID> getFallbackDockIds() {
        return Collections.unmodifiableList(new ArrayList<>(fallbackDockIds));
    }

    public void setFallbackDockIds(@Nullable List<UUID> dockIds) {
        fallbackDockIds.clear();
        if (dockIds == null) return;
        for (UUID id : dockIds) {
            if (id != null && !fallbackDockIds.contains(id)) fallbackDockIds.add(id);
            if (fallbackDockIds.size() >= 8) break;
        }
    }

    public DroneAutoPickupMode getAutoPickupMode() {
        return autoPickupMode;
    }

    public void setAutoPickupMode(@Nullable DroneAutoPickupMode mode) {
        autoPickupMode = mode == null ? DroneAutoPickupMode.ALL : mode;
    }

    public DroneItemFilterSpec getAutoPickupFilter() {
        return autoPickupFilter;
    }

    public void setAutoPickupFilter(@Nullable DroneItemFilterSpec filter) {
        autoPickupFilter = filter == null ? DroneItemFilterSpec.ANY : filter;
    }

    public int getEnergyPercent() {
        return dataManager.get(ENERGY_PERCENT);
    }

    public boolean hasVisualRotorPower() { return dataManager.get(HAS_ROTOR_POWER); }

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

    public ItemStack getVisualHeldWeapon(int slot) {
        return dataManager.get(slot == 1 ? SECONDARY_WEAPON : HELD_WEAPON);
    }

    public ItemStack getVisualFishingRod() {
        return dataManager.get(HELD_FISHING_ROD);
    }

    public ItemStack getVisualAlchemyJar() { return dataManager.get(HELD_ALCHEMY_JAR); }
    public int getVisualAlchemyAspectColor() { return dataManager.get(ALCHEMY_ASPECT_COLOR); }

    public float getAlchemyAnimationProgress(float partialTicks) {
        int remaining = dataManager.get(ALCHEMY_ANIMATION_TICKS);
        if (remaining <= 0) return 0.0F;
        float elapsed = Math.max(0.0F, Math.min(16.0F, 16.0F - remaining + partialTicks));
        return (float) Math.sin(elapsed / 16.0F * Math.PI);
    }

    /** Monotonic transfer fill used by the jar contents; unlike arm motion it stays full afterwards. */
    public float getAlchemyFillProgress(float partialTicks) {
        if (getVisualAlchemyAspectColor() < 0) return 0.0F;
        int remaining = dataManager.get(ALCHEMY_ANIMATION_TICKS);
        if (remaining <= 0) return 1.0F;
        return Math.max(0.0F, Math.min(1.0F, (16.0F - remaining + partialTicks) / 16.0F));
    }

    @Nullable
    public EntityFishHook getVisualFishingHook() {
        int entityId = dataManager.get(FISHING_HOOK_ID);
        Entity entity = entityId < 0 || world == null ? null : world.getEntityByID(entityId);
        return entity instanceof EntityFishHook ? (EntityFishHook) entity : null;
    }

    @Nullable
    public BlockPos getVisualFishingTarget() {
        NBTTagCompound state = dataManager.get(FISHING_VISUAL_TARGET);
        return state != null && state.getBoolean("Active")
                ? new BlockPos(state.getInteger("X"), state.getInteger("Y"), state.getInteger("Z")) : null;
    }

    /** Arm swing for the visible throw; the server-side hook still owns all vanilla fishing rules. */
    public float getFishingCastProgress(float partialTicks) {
        float flight = getFishingCastFlightProgress(partialTicks);
        return flight >= 1.0F ? 0.0F : (float) Math.sin(flight * Math.PI);
    }

    /** Linear progress used to animate the client fallback bobber from the claw to the water. */
    public float getFishingCastFlightProgress(float partialTicks) {
        int remaining = dataManager.get(FISHING_CAST_TICKS);
        if (remaining <= 0) return 1.0F;
        return Math.max(0.0F, Math.min(1.0F, (12.0F - remaining + partialTicks) / 12.0F));
    }

    public float getFishingPullProgress(float partialTicks) {
        int remaining = dataManager.get(FISHING_PULL_TICKS);
        if (remaining <= 0) return 0.0F;
        float elapsed = Math.max(0.0F, Math.min(10.0F, 10.0F - remaining + partialTicks));
        return (float) Math.sin(elapsed / 10.0F * Math.PI);
    }

    @Nullable
    public Entity getVisualEntityTarget() {
        int entityId = dataManager.get(ENTITY_TARGET_ID);
        return entityId < 0 || world == null ? null : world.getEntityByID(entityId);
    }

    private void setVisualEntityTarget(@Nullable Entity target) {
        if (!world.isRemote) dataManager.set(ENTITY_TARGET_ID, target == null ? -1 : target.getEntityId());
    }

    public float getAttackAnimationProgress(float partialTicks) {
        int remaining = dataManager.get(ATTACK_ANIMATION_TICKS);
        if (remaining <= 0) return 0.0F;
        float elapsed = Math.max(0.0F, Math.min(8.0F, 8.0F - remaining + partialTicks));
        return (float) Math.sin(elapsed / 8.0F * Math.PI);
    }

    public String getVisualStatusLabel() { return dataManager.get(STATUS_LABEL); }
    public boolean areVisualRotorsActive() { return dataManager.get(ROTORS_ACTIVE); }
    public int getVisualStatusLightMode() { return dataManager.get(STATUS_LIGHT_MODE); }

    private void syncHeldWeapon() {
        boolean enabled = DroneHardwareStats.hasUpgrade(upgrades, DroneUpgradeType.COMBAT);
        if (enabled && !world.isRemote) {
            DroneWorldActions.activateLightsaber(weapons.getStackInSlot(0));
            DroneWorldActions.activateLightsaber(weapons.getStackInSlot(1));
        }
        syncWeaponParameter(HELD_WEAPON, enabled ? weapons.getStackInSlot(0).copy() : ItemStack.EMPTY);
        syncWeaponParameter(SECONDARY_WEAPON, enabled ? weapons.getStackInSlot(1).copy() : ItemStack.EMPTY);
    }

    private void syncFishingRod() {
        ItemStack visible = DroneHardwareStats.hasUpgrade(upgrades, DroneUpgradeType.FISHING)
                ? fishingRod.getStackInSlot(0).copy() : ItemStack.EMPTY;
        syncWeaponParameter(HELD_FISHING_ROD, visible);
    }

    private void syncAlchemyJar() {
        ItemStack visible = DroneHardwareStats.hasUpgrade(upgrades, DroneUpgradeType.THAUMCRAFT_ALCHEMY)
                ? alchemyJar.getStackInSlot(0).copy() : ItemStack.EMPTY;
        syncWeaponParameter(HELD_ALCHEMY_JAR, visible);
        if (visible.isEmpty()) dataManager.set(ALCHEMY_ASPECT_COLOR, -1);
    }

    private void syncWeaponParameter(DataParameter<ItemStack> parameter, ItemStack weapon) {
        if (!ItemStack.areItemStacksEqual(dataManager.get(parameter), weapon)) dataManager.set(parameter, weapon);
    }

    private void triggerAttackAnimation() {
        syncHeldWeapon();
        dataManager.set(ATTACK_ANIMATION_TICKS, 8);
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
                return type != null && DroneHardwareStats.findUpgradeSlot(this, type, slot) < 0;
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

    private ItemStackHandler createWeaponHandler() {
        return new ItemStackHandler(2) {
            @Override
            public int getSlotLimit(int slot) { return 1; }

            @Override
            public boolean isItemValid(int slot, ItemStack stack) {
                return DroneHardwareStats.hasUpgrade(upgrades, DroneUpgradeType.COMBAT)
                        && DroneWorldActions.isWeapon(stack);
            }

            @Override
            protected void onContentsChanged(int slot) {
                super.onContentsChanged(slot);
                if (!world.isRemote) {
                    DroneWorldActions.activateLightsaber(getStackInSlot(slot));
                    syncHeldWeapon();
                }
            }
        };
    }

    private ItemStackHandler createFishingRodHandler() {
        return new ItemStackHandler(1) {
            @Override
            public int getSlotLimit(int slot) { return 1; }

            @Override
            public boolean isItemValid(int slot, ItemStack stack) {
                return DroneHardwareStats.hasUpgrade(upgrades, DroneUpgradeType.FISHING)
                        && stack.getItem() instanceof ItemFishingRod;
            }

            @Override
            public ItemStack extractItem(int slot, int amount, boolean simulate) {
                if (activeFishingHook != null && !activeFishingHook.isDead) return ItemStack.EMPTY;
                return super.extractItem(slot, amount, simulate);
            }

            @Override
            protected void onContentsChanged(int slot) {
                super.onContentsChanged(slot);
                if (!world.isRemote) syncFishingRod();
            }
        };
    }

    private ItemStackHandler createAlchemyJarHandler() {
        return new ItemStackHandler(1) {
            @Override public int getSlotLimit(int slot) { return 1; }

            @Override
            public boolean isItemValid(int slot, ItemStack stack) {
                return DroneHardwareStats.hasUpgrade(upgrades, DroneUpgradeType.THAUMCRAFT_ALCHEMY)
                        && isThaumcraftEssentiaJar(stack);
            }

            @Override
            public ItemStack extractItem(int slot, int amount, boolean simulate) {
                if (dataManager.get(ALCHEMY_ANIMATION_TICKS) > 0) return ItemStack.EMPTY;
                return super.extractItem(slot, amount, simulate);
            }

            @Override
            protected void onContentsChanged(int slot) {
                super.onContentsChanged(slot);
                if (!world.isRemote) syncAlchemyJar();
            }
        };
    }

    private static boolean isThaumcraftEssentiaJar(ItemStack stack) {
        ResourceLocation id = stack.isEmpty() ? null : stack.getItem().getRegistryName();
        return id != null && "thaumcraft".equals(id.getNamespace())
                && ("jar_normal".equals(id.getPath()) || "jar_void".equals(id.getPath()));
    }

    private boolean canRemoveUpgrade(int slot) {
        if (slot < 0 || slot >= upgrades.getSlots() || upgrades.getStackInSlot(slot).isEmpty()) return true;
        DroneUpgradeType type = ItemDroneUpgradeModule.getType(upgrades.getStackInSlot(slot));
        return type == null || !isUpgradeRemovalLocked(type);
    }

    private boolean isUpgradeRemovalLocked(DroneUpgradeType type) {
        if (type == DroneUpgradeType.BATTERY && energy.getStored() > chassis.getBaseCapacity()) return true;
        if (type == DroneUpgradeType.CARGO) {
            for (int cargoSlot = chassis.getBaseCargoSlots(); cargoSlot < inventory.getSlots(); cargoSlot++) {
                if (!inventory.getStackInSlot(cargoSlot).isEmpty()) return true;
            }
        }
        if (type == DroneUpgradeType.FLUID_CARGO && fluidTank.getFluidAmount() > 0) return true;
        if (type == DroneUpgradeType.EU_INTERFACE && logisticsEuCargo > 0L) return true;
        if (type == DroneUpgradeType.COMBAT
                && (!weapons.getStackInSlot(0).isEmpty() || !weapons.getStackInSlot(1).isEmpty())) return true;
        if (type == DroneUpgradeType.FISHING && !fishingRod.getStackInSlot(0).isEmpty()) return true;
        if (type == DroneUpgradeType.THAUMCRAFT_ALCHEMY && !alchemyJar.getStackInSlot(0).isEmpty()) return true;
        if (type == DroneUpgradeType.ENTITY_CONTAINMENT && loadedEntityData != null) return true;
        return false;
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

    private boolean hasEnergyFor(long baseCost) {
        return energy.getStored() >= DroneHardwareStats.energyCost(baseCost, upgrades);
    }

    private boolean hasEnergy(long baseCost) {
        return energy.getStored() >= DroneHardwareStats.energyCost(baseCost, upgrades);
    }

    @Override
    public ModularPanel buildUI(EntityGuiData data, PanelSyncManager syncManager, UISettings settings) {
        syncManager.registerSlotGroup("drone_inventory", inventory.getSlots());
        syncManager.registerSlotGroup("drone_upgrades", upgrades.getSlots());
        syncManager.registerSlotGroup("drone_weapons", weapons.getSlots());
        syncManager.registerSlotGroup("drone_fishing_rod", fishingRod.getSlots());
        syncManager.registerSlotGroup("drone_alchemy_jar", alchemyJar.getSlots());
        GenericSyncValue<NBTTagCompound> state = GenericSyncValue.builder(NBTTagCompound.class)
                .getter(this::createControlSnapshot)
                .setter(this::receiveControlSnapshot)
                .adapter(ByteBufAdapters.NBT)
                .copy(NBTTagCompound::copy)
                .build();
        syncManager.syncValue("drone_control_state", state);
        syncManager.registerSyncedAction(CONTROL_ACTION, false, true,
                packet -> handleControl(data.getPlayer(), packet.readString(24)));

        // Keep the controller within a 352 logical-pixel height. At GUI scale 4 this is
        // the largest height that still fits a 1440p screen, so the player inventory is
        // never pushed below the visible area.
        ModularPanel panel = ModularPanel.defaultPanel("drtech_drone_controller", 310, 352)
                .child(IKey.lang("drtech.drone.controller.title").asWidget().pos(7, 6))
                .child(IKey.lang("drtech.drone.controller.cargo").asWidget().pos(8, 16))
                .child(IKey.lang("drtech.drone.controller.upgrades").asWidget().pos(8, 83))
                .child(IKey.lang("drtech.drone.controller.upgrade_hint").scale(0.78F).asWidget()
                        .pos(8, 175).size(116, 18))
                .child(IKey.lang("drtech.drone.controller.weapons").scale(0.62F).asWidget()
                        .pos(8, 230).size(40, 10))
                .child(IKey.lang("drtech.drone.controller.fishing_rod").scale(0.55F).asWidget()
                        .pos(50, 230).size(20, 10))
                .child(IKey.lang("drtech.drone.controller.alchemy_jar").scale(0.62F).asWidget()
                        .pos(72, 230).size(52, 10))
                .child(IKey.dynamic(this::getClientHardwareLine).scale(0.65F).asWidget().pos(8, 195).size(116, 31))
                .child(IKey.dynamic(this::getClientEnergyLine).asWidget().pos(132, 24).size(170, 12))
                .child(IKey.dynamic(this::getClientProgramLine).asWidget().pos(132, 39).size(170, 12))
                .child(IKey.dynamic(this::getClientStatusLine).asWidget().pos(132, 54).size(170, 12))
                .child(IKey.dynamic(this::getClientNodeLine).asWidget().pos(132, 69).size(170, 12))
                .child(IKey.dynamic(this::getClientDockLine).asWidget().pos(132, 84).size(170, 12))
                .child(IKey.dynamic(this::getClientErrorLine).scale(0.72F).asWidget().pos(132, 99).size(170, 18))
                .child(controlButton("drtech.drone.controller.start", "START", 132, 122, syncManager))
                .child(controlButton("drtech.drone.controller.pause", "PAUSE", 184, 122, syncManager))
                .child(controlButton("drtech.drone.controller.resume", "RESUME", 236, 122, syncManager))
                .child(controlButton("drtech.drone.controller.stop", "STOP", 132, 141, syncManager))
                .child(controlButton("drtech.drone.controller.restart", "RESTART", 184, 141, syncManager))
                .child(controlButton("drtech.drone.controller.loop", "LOOP", 236, 141, syncManager))
                .child(controlButton("drtech.drone.controller.step_into", "STEP_INTO", 132, 160, syncManager))
                .child(controlButton("drtech.drone.controller.clear_trace", "CLEAR_TRACE", 184, 160, syncManager))
                .child(controlButton("drtech.drone.controller.step_over", "STEP_OVER", 236, 160, syncManager))
                .child(IKey.lang("drtech.drone.controller.trace").asWidget().pos(132, 180).size(170, 10))
                .child(IKey.dynamic(this::getClientTraceLine).asWidget().pos(132, 192).size(170, 28))
                .child(IKey.lang("drtech.drone.controller.fallback_docks").asWidget().pos(132, 222))
                .child(IKey.dynamic(this::getClientFallbackSummaryLine).scale(0.65F).asWidget()
                        .pos(132, 234).size(170, 10))
                .child(compactControlButton("drtech.drone.controller.previous", "FALLBACK_PREV", 132, syncManager))
                .child(compactControlButton("drtech.drone.controller.next", "FALLBACK_NEXT", 156, syncManager))
                .child(compactControlButton("drtech.drone.controller.bind", "FALLBACK_BIND", 180, syncManager))
                .child(compactControlButton("drtech.drone.controller.add", "FALLBACK_ADD", 204, syncManager))
                .child(compactControlButton("drtech.drone.controller.remove", "FALLBACK_REMOVE", 228, syncManager))
                .child(compactControlButton("drtech.drone.controller.move_up", "FALLBACK_UP", 252, syncManager))
                .child(compactControlButton("drtech.drone.controller.move_down", "FALLBACK_DOWN", 276, syncManager))
                .child(cargoPolicyButton(syncManager))
                .child(SlotGroupWidget.playerInventory(false).left(7).bottom(7));
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            int cargoSlot = slot;
            panel.child(new ItemSlot().slot(inventory, slot)
                    .setEnabledIf(widget -> cargoSlot < (world != null && world.isRemote
                            ? clientCargoSlots : getActiveCargoSlots()))
                    .pos(8 + slot % 6 * 20, 24 + slot / 6 * 20));
        }
        for (int slot = 0; slot < upgrades.getSlots(); slot++) {
            panel.child(new ItemSlot().slot(upgrades, slot)
                    .pos(8 + slot % 6 * 20, 94 + slot / 6 * 20));
        }
        for (int slot = 0; slot < weapons.getSlots(); slot++) {
            panel.child(new ItemSlot().slot(weapons, slot)
                    .setEnabledIf(widget -> world != null && (world.isRemote
                            ? hasVisualUpgrade(DroneUpgradeType.COMBAT)
                            : DroneHardwareStats.hasUpgrade(upgrades, DroneUpgradeType.COMBAT)))
                    .pos(8 + slot * 20, 244));
        }
        panel.child(new ItemSlot().slot(fishingRod, 0)
                .setEnabledIf(widget -> world != null && (world.isRemote
                        ? hasVisualUpgrade(DroneUpgradeType.FISHING)
                        : DroneHardwareStats.hasUpgrade(upgrades, DroneUpgradeType.FISHING)))
                .pos(50, 244));
        panel.child(new ItemSlot().slot(alchemyJar, 0)
                .setEnabledIf(widget -> world != null && (world.isRemote
                        ? hasVisualUpgrade(DroneUpgradeType.THAUMCRAFT_ALCHEMY)
                        : DroneHardwareStats.hasUpgrade(upgrades, DroneUpgradeType.THAUMCRAFT_ALCHEMY)))
                .pos(72, 244));
        return panel;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public ModularScreen createScreen(EntityGuiData data, ModularPanel mainPanel) {
        return new ModularScreen(Tags.MODID, mainPanel);
    }

    private ButtonWidget<?> controlButton(String translation, String command, int x, int y,
            PanelSyncManager syncManager) {
        return new ButtonWidget<>().pos(x, y).size(48, 16).overlay(IKey.lang(translation).scale(0.82F))
                .onMousePressed(mouse -> {
                    syncManager.callSyncedAction(CONTROL_ACTION, packet -> packet.writeString(command));
                    return true;
                });
    }

    private ButtonWidget<?> compactControlButton(String translation, String command, int x,
            PanelSyncManager syncManager) {
        return new ButtonWidget<>().pos(x, 249).size(22, 15).overlay(IKey.lang(translation).scale(0.52F))
                .tooltipStatic(tooltip -> tooltip.addLine(IKey.lang(translation)))
                .onMousePressed(mouse -> {
                    syncManager.callSyncedAction(CONTROL_ACTION, packet -> packet.writeString(command));
                    return true;
                });
    }

    private ButtonWidget<?> cargoPolicyButton(PanelSyncManager syncManager) {
        return new ButtonWidget<>().pos(94, 249).size(30, 15)
                .overlay(IKey.dynamic(this::getClientCargoPolicyShort).scale(0.56F))
                .tooltipDynamic(tooltip -> tooltip.addLine(IKey.dynamic(this::getClientCargoPolicyLine)))
                .onMousePressed(mouse -> {
                    syncManager.callSyncedAction(CONTROL_ACTION, packet -> packet.writeString("CARGO_POLICY"));
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
        return player != null && applyControl(player.getUniqueID(), command);
    }

    /** Fleet commands are authorized by identity because the operator may be in another dimension. */
    public boolean handleFleetControl(UUID requesterId, String command) {
        if (world.isRemote || requesterId == null || !requesterId.equals(getOwnerId())) return false;
        if ("RECALL".equals(command)) return requestManualRecall();
        if (!"START".equals(command) && !"STOP".equals(command)) return false;
        return applyControl(requesterId, command);
    }

    private boolean applyControl(UUID requesterId, String command) {
        if (requesterId == null || !requesterId.equals(getOwnerId())) return false;
        if (command.startsWith("FALLBACK_")) return applyFallbackControl(command);
        if ("CARGO_POLICY".equals(command)) {
            cargoFullPolicy = cargoFullPolicy.next();
            cargoFullPickupSuspended = false;
            return true;
        }
        if (runtime == null) return false;
        if (safetyFirmware.getState() != DroneSafetyState.PROGRAM
                && ("START".equals(command) || "RESUME".equals(command) || "RESTART".equals(command)
                || "STEP".equals(command) || "STEP_INTO".equals(command) || "STEP_OVER".equals(command))) return false;
        switch (command) {
            case "START" -> {
                if (runtime.getStatus() != DroneRuntimeStatus.READY
                        && runtime.getStatus() != DroneRuntimeStatus.COMPLETED
                        && runtime.getStatus() != DroneRuntimeStatus.PAUSED) return false;
                if (runtime.getStatus() == DroneRuntimeStatus.COMPLETED) runtime.restart();
                else if (runtime.getStatus() == DroneRuntimeStatus.PAUSED) runtime.resume();
                else runtime.start();
                executionEnabled = true;
                forceRotorsActiveSync();
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
                forceRotorsActiveSync();
            }
            case "STOP" -> {
                runtime.stop();
                executionEnabled = false;
                safetyFirmware.setResumeProgram(false);
                cancelFishingHook();
            }
            case "RESTART" -> {
                cancelFishingHook();
                runtime.restart();
                executionEnabled = true;
                forceRotorsActiveSync();
            }
            case "LOOP" -> loopProgram = !loopProgram;
            case "STEP", "STEP_INTO" -> {
                if (!runtime.requestSingleStepInto()) return false;
                executionEnabled = true;
            }
            case "STEP_OVER" -> {
                if (!runtime.requestSingleStepOver()) return false;
                executionEnabled = true;
            }
            case "CLEAR_TRACE" -> runtime.clearTrace();
            default -> { return false; }
        }
        syncRuntimeState();
        return true;
    }

    private void cancelFishingHook() {
        if (activeFishingHook != null && !activeFishingHook.isDead) activeFishingHook.setDead();
        activeFishingHook = null;
        activeFishingTarget = null;
        if (!world.isRemote) {
            dataManager.set(FISHING_HOOK_ID, -1);
            setFishingVisualTarget(null);
            dataManager.set(FISHING_CAST_TICKS, 0);
        }
        DroneWorldActions.cleanupFishingHooks(this);
    }

    private void setFishingVisualTarget(@Nullable BlockPos target) {
        NBTTagCompound state = new NBTTagCompound();
        if (target != null) {
            state.setBoolean("Active", true);
            state.setInteger("X", target.getX());
            state.setInteger("Y", target.getY());
            state.setInteger("Z", target.getZ());
        }
        dataManager.set(FISHING_VISUAL_TARGET, state);
    }

    /** Reasserts active rotors for tracking clients after a runtime pause/resume transition. */
    private void forceRotorsActiveSync() {
        dataManager.set(ROTORS_ACTIVE, true);
        syncEnergyPercent();
        dataManager.setDirty(ROTORS_ACTIVE);
        dataManager.setDirty(HAS_ROTOR_POWER);
        rotorResyncTicks = 10;
    }

    private boolean applyFallbackControl(String command) {
        List<DroneDockRecord> candidates = getFallbackDockCandidates();
        if (candidates.isEmpty()) {
            fallbackCandidateIndex = 0;
            return false;
        }
        fallbackCandidateIndex = Math.floorMod(fallbackCandidateIndex, candidates.size());
        if ("FALLBACK_PREV".equals(command)) {
            fallbackCandidateIndex = Math.floorMod(fallbackCandidateIndex - 1, candidates.size());
            return true;
        }
        if ("FALLBACK_NEXT".equals(command)) {
            fallbackCandidateIndex = (fallbackCandidateIndex + 1) % candidates.size();
            return true;
        }
        DroneDockRecord selected = candidates.get(fallbackCandidateIndex);
        int existing = fallbackDockIds.indexOf(selected.getDockId());
        switch (command) {
            case "FALLBACK_ADD" -> {
                if (existing >= 0 || fallbackDockIds.size() >= 8) return false;
                fallbackDockIds.add(selected.getDockId());
            }
            case "FALLBACK_REMOVE" -> {
                if (existing < 0) return false;
                fallbackDockIds.remove(existing);
            }
            case "FALLBACK_UP" -> {
                if (existing <= 0) return false;
                Collections.swap(fallbackDockIds, existing, existing - 1);
            }
            case "FALLBACK_DOWN" -> {
                if (existing < 0 || existing + 1 >= fallbackDockIds.size()) return false;
                Collections.swap(fallbackDockIds, existing, existing + 1);
            }
            case "FALLBACK_BIND" -> {
                DroneDockRecord usable = DroneDockNetwork.get(world).findPreferred(
                        Collections.singletonList(selected.getDockId()), world.provider.getDimension(),
                        getOwnerId(), chassis.getVoltageTier(), world.getTotalWorldTime(), true, null)
                        .orElse(null);
                return usable != null && bindToDockPosition(usable.getPosition());
            }
            default -> { return false; }
        }
        return true;
    }

    private List<DroneDockRecord> getFallbackDockCandidates() {
        if (world == null || world.isRemote) return Collections.emptyList();
        return DroneDockNetwork.get(world).listForOwner(getOwnerId(), world.provider.getDimension());
    }

    @Nullable
    public UUID getProgramId() {
        return runtime == null ? null : runtime.getProgramId();
    }

    public long getProgramRevision() {
        return runtime == null ? -1L : runtime.getProgramRevision();
    }

    /** Installs a validated library program through an authenticated external-control service. */
    public boolean assignLibraryProgram(UUID requesterId, DroneProgramGraph graph) {
        if (world == null || world.isRemote || requesterId == null || graph == null
                || !requesterId.equals(getOwnerId()) || hasAssignedLogisticsWork(requesterId)
                || runtime != null && runtime.getStatus() == DroneRuntimeStatus.RUNNING) return false;
        DroneCompileResult result = new DroneProgramCompiler(DroneExtensionRegistry.nodes()).compile(graph);
        if (result.hasErrors() || DroneProgramHardwareValidator.validate(graph, createDroneItem(), world).stream()
                .anyMatch(diagnostic -> diagnostic.getSeverity() == DroneDiagnosticSeverity.ERROR)) return false;
        program = DroneProgramNbtCodec.write(graph);
        pendingRuntimeState = null;
        rebuildRuntime();
        markVelocityChanged();
        return runtime != null;
    }

    private boolean hasAssignedLogisticsWork(UUID ownerId) {
        DroneFleetState fleet = DroneFleetState.get(world);
        return fleet.findAssignedLogisticsJob(ownerId, droneId).isPresent()
                || fleet.findAssignedLogisticsRecovery(ownerId, droneId).isPresent()
                || fleet.findRetainedLogisticsJob(ownerId, droneId).isPresent();
    }

    public NBTTagCompound createRemoteDebugSnapshot() {
        NBTTagCompound snapshot = new NBTTagCompound();
        snapshot.setInteger("EntityId", getEntityId());
        snapshot.setLong("Position", new BlockPos(posX, posY, posZ).toLong());
        snapshot.setInteger("EnergyPercent", getEnergyPercent());
        snapshot.setLong("EnergyStored", energy.getStored());
        snapshot.setLong("EnergyCapacity", energy.getCapacity());
        snapshot.setString("Chassis", chassis.name());
        snapshot.setInteger("WirelessRange", getWirelessRange());
        snapshot.setString("Modules", getInstalledUpgradeSummary());
        snapshot.setInteger("FluidAmount", fluidTank.getFluidAmount());
        snapshot.setInteger("FluidCapacity", fluidTank.getCapacity());
        snapshot.setString("FluidName", fluidTank.getFluid() == null ? "" : fluidTank.getFluid().getLocalizedName());
        snapshot.setLong("LogisticsEuCargo", logisticsEuCargo);
        snapshot.setString("SafetyState", safetyFirmware.getState().name());
        snapshot.setString("StatusLabel", getVisualStatusLabel());
        snapshot.setBoolean("RotorsActive", areVisualRotorsActive());
        snapshot.setInteger("StatusLightMode", getVisualStatusLightMode());
        snapshot.setBoolean("ProgramSuspended", safetyFirmware.getState() != DroneSafetyState.PROGRAM);
        snapshot.setBoolean("BatteryLocked", isUpgradeRemovalLocked(DroneUpgradeType.BATTERY));
        snapshot.setBoolean("CargoLocked", isUpgradeRemovalLocked(DroneUpgradeType.CARGO));
        if (debugPathTarget != null) snapshot.setLong("PathTarget", debugPathTarget.toLong());
        if (debugPathWaypoint != null) snapshot.setLong("PathWaypoint", debugPathWaypoint.toLong());
        snapshot.setInteger("PathIndex", Math.max(0, debugPathWaypointIndex));
        snapshot.setInteger("PathLength", Math.max(0, debugPathLength));
        NBTTagList pathPoints = new NBTTagList();
        for (int index = 0; index < Math.min(256, debugPathPoints.size()); index++) {
            NBTTagCompound point = new NBTTagCompound();
            point.setLong("Position", debugPathPoints.get(index).toLong());
            pathPoints.appendTag(point);
        }
        snapshot.setTag("PathPoints", pathPoints);
        snapshot.setInteger("EstimatedAreaCompleted", Math.max(0, debugEnergyAreaCompleted));
        snapshot.setInteger("EstimatedAreaTotal", Math.max(0, debugEnergyAreaTotal));
        snapshot.setLong("EstimatedAreaRemainingEu", debugEnergyAreaRemainingEstimate);
        if (runtime == null) {
            snapshot.setString("Status", "NO PROGRAM");
            snapshot.setString("Node", "-");
            snapshot.setString("Progress", "");
            snapshot.setString("Variables", "No variables");
            snapshot.setTag("VariableList", new NBTTagList());
            snapshot.setString("ActionStatus", "SUCCESS");
            snapshot.setString("ActionError", "");
            snapshot.setLong("NodeTicks", 0L);
            snapshot.setTag("PortInputs", new NBTTagList());
            snapshot.setString("InputNode", "");
            snapshot.setString("OutputNode", "");
            snapshot.setString("OutputPort", "");
            snapshot.setLong("OutputAmount", 0L);
            snapshot.setInteger("AreaIndex", -1);
            snapshot.setInteger("AreaTotal", 0);
            snapshot.setString("Trace", "No execution trace");
            snapshot.setTag("TraceList", new NBTTagList());
        } else {
            snapshot.setString("Status", runtime.getStatus().name());
            snapshot.setString("EffectiveStatus", safetyFirmware.getState() == DroneSafetyState.PROGRAM
                    ? runtime.getStatus().name() : "SUSPENDED");
            snapshot.setString("Node", runtime.getCurrentNodeType());
            snapshot.setString("CurrentNode", runtime.getCurrentNodeId().toString());
            snapshot.setString("Progress", runtime.getCurrentNodeProgress());
            snapshot.setString("Variables", runtime.getVariableSummary(3));
            snapshot.setTag("VariableList", runtime.getVariableSnapshot(64));
            snapshot.setString("ActionStatus", runtime.getLastActionStatus().name());
            snapshot.setString("ActionError", runtime.getLastActionError());
            snapshot.setLong("NodeTicks", runtime.getCurrentNodeElapsedTicks());
            snapshot.setTag("PortInputs", runtime.getCurrentNodeInputSnapshot(16));
            snapshot.setString("InputNode", runtime.getDebugInputNodeType());
            snapshot.setString("OutputNode", runtime.getLastOutputNodeType());
            snapshot.setString("OutputPort", runtime.getLastOutputPort());
            snapshot.setLong("OutputAmount", runtime.getLastOutputAmount());
            snapshot.setInteger("AreaIndex", runtime.getCurrentAreaIndex());
            snapshot.setInteger("AreaTotal", runtime.getCurrentAreaTotal());
            BlockPos loopCoordinate = runtime.getCurrentLoopCoordinate();
            if (loopCoordinate != null) snapshot.setLong("CurrentAreaPosition", loopCoordinate.toLong());
            snapshot.setString("Trace", runtime.getTraceSummary(2));
            snapshot.setTag("TraceList", runtime.getTraceSnapshot(DroneProgramRuntime.MAX_TRACE_ENTRIES));
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
        snapshot.setLong("LogisticsEuCargo", logisticsEuCargo);
        snapshot.setBoolean("BatteryLocked", isUpgradeRemovalLocked(DroneUpgradeType.BATTERY));
        snapshot.setBoolean("CargoLocked", isUpgradeRemovalLocked(DroneUpgradeType.CARGO));
        snapshot.setFloat("Health", getHealth());
        snapshot.setFloat("MaxHealth", getMaxHealth());
        snapshot.setString("EntityPayload", getLoadedEntitySummary());
        snapshot.setString("CargoFullPolicy", cargoFullPolicy.name());
        snapshot.setBoolean("Loop", loopProgram);
        snapshot.setString("Dock", boundDock == null ? "Unbound"
                : boundDock.getX() + ", " + boundDock.getY() + ", " + boundDock.getZ());
        List<DroneDockRecord> dockCandidates = getFallbackDockCandidates();
        if (dockCandidates.isEmpty()) {
            snapshot.setString("FallbackCandidate", "-");
            snapshot.setString("FallbackState", "EMPTY");
        } else {
            fallbackCandidateIndex = Math.floorMod(fallbackCandidateIndex, dockCandidates.size());
            DroneDockRecord candidate = dockCandidates.get(fallbackCandidateIndex);
            int order = fallbackDockIds.indexOf(candidate.getDockId());
            snapshot.setString("FallbackCandidate", candidate.getName() + " @ "
                    + candidate.getPosition().getX() + "," + candidate.getPosition().getY() + ","
                    + candidate.getPosition().getZ());
            snapshot.setString("FallbackState", getDockCandidateState(candidate) + " | "
                    + (order < 0 ? "NOT_ADDED" : "ORDER: " + (order + 1)) + " | "
                    + (fallbackCandidateIndex + 1) + "/" + dockCandidates.size());
        }
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
        clientFallbackCandidate = snapshot.getString("FallbackCandidate");
        clientFallbackState = snapshot.getString("FallbackState");
        clientLoop = snapshot.getBoolean("Loop");
        clientTrace = snapshot.getString("Trace");
        clientChassis = snapshot.getString("Chassis");
        clientCargoSlots = snapshot.getInteger("CargoSlots");
        clientWirelessRange = snapshot.getInteger("WirelessRange");
        clientFluidAmount = snapshot.getInteger("FluidAmount");
        clientFluidCapacity = snapshot.getInteger("FluidCapacity");
        clientFluidName = snapshot.getString("FluidName");
        clientLogisticsEu = Math.max(0L, snapshot.getLong("LogisticsEuCargo"));
        clientBatteryLocked = snapshot.getBoolean("BatteryLocked");
        clientCargoLocked = snapshot.getBoolean("CargoLocked");
        clientHealth = snapshot.getFloat("Health");
        clientMaxHealth = snapshot.getFloat("MaxHealth");
        clientEntityPayload = snapshot.getString("EntityPayload");
        clientCargoFullPolicy = snapshot.hasKey("CargoFullPolicy", 8)
                ? snapshot.getString("CargoFullPolicy") : DroneCargoFullPolicy.STOP.name();
    }

    private String getClientCargoPolicyShort() {
        DroneCargoFullPolicy policy = DroneCargoFullPolicy.fromName(clientCargoFullPolicy);
        return I18n.format("drtech.drone.cargo_full.short." + policy.name().toLowerCase(java.util.Locale.ROOT));
    }

    private String getClientCargoPolicyLine() {
        DroneCargoFullPolicy policy = DroneCargoFullPolicy.fromName(clientCargoFullPolicy);
        return I18n.format("drtech.drone.cargo_full.policy",
                I18n.format("drtech.drone.cargo_full." + policy.name().toLowerCase(java.util.Locale.ROOT)));
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
    private String getClientFallbackCandidateLine() {
        return I18n.format("drtech.drone.ui.fallback_candidate", clientFallbackCandidate);
    }
    private String getClientFallbackStateLine() {
        String localized = clientFallbackState.replace("OFFLINE", I18n.format("drtech.drone.dock.state.offline"))
                .replace("DISABLED", I18n.format("drtech.drone.dock.state.disabled"))
                .replace("INCOMPATIBLE", I18n.format("drtech.drone.dock.state.incompatible"))
                .replace("NO_ENERGY", I18n.format("drtech.drone.dock.state.no_energy"))
                .replace("OCCUPIED", I18n.format("drtech.drone.dock.state.occupied"))
                .replace("AVAILABLE", I18n.format("drtech.drone.dock.state.available"))
                .replace("NOT_ADDED", I18n.format("drtech.drone.dock.state.not_added"))
                .replace("ORDER:", I18n.format("drtech.drone.dock.state.order"));
        return I18n.format("drtech.drone.ui.fallback_state", localized);
    }

    private String getClientFallbackSummaryLine() {
        return compactUiText(getClientFallbackCandidateLine() + " | " + getClientFallbackStateLine(), 38);
    }

    private String getDockCandidateState(DroneDockRecord record) {
        if (!DroneDockNetwork.isRecordOnline(record, world.getTotalWorldTime())) return "OFFLINE";
        if (!record.isEnabled()) return "DISABLED";
        if (record.getTier() > chassis.getVoltageTier()) return "INCOMPATIBLE";
        if (record.getAvailableEu() <= 0L) return "NO_ENERGY";
        if (!record.canAcceptDrone()) return "OCCUPIED";
        return "AVAILABLE";
    }
    private String getClientErrorLine() {
        return clientError.isEmpty() ? I18n.format("drtech.drone.ui.no_error")
                : compactUiText(I18n.format("drtech.drone.ui.error", clientError), 38);
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
        return compactUiText(localized.toString(), 56);
    }

    private static String compactUiText(String text, int maxLength) {
        if (text == null) return "";
        String compact = text.replace('\r', ' ').replace('\n', ' ').replaceAll("\\s+", " ").trim();
        return compact.length() <= maxLength ? compact : compact.substring(0, Math.max(0, maxLength - 1)) + "…";
    }
    private String getClientHardwareLine() {
        String line = I18n.format("drtech.drone.ui.hardware", clientChassis, (int) clientHealth, (int) clientMaxHealth)
                + "\n" + I18n.format("drtech.drone.ui.cargo", clientCargoSlots, DroneHardwareStats.MAX_CARGO_SLOTS)
                + "\n" + I18n.format("drtech.drone.ui.wireless", clientWirelessRange)
                + "\n" + I18n.format("drtech.drone.ui.fluid", clientFluidName.isEmpty()
                        ? I18n.format("drtech.drone.ui.fluid_empty") : clientFluidName,
                        clientFluidAmount, clientFluidCapacity)
                + "\n" + I18n.format("drtech.drone.ui.logistics_eu", clientLogisticsEu)
                + "\n" + I18n.format("drtech.drone.ui.entity_payload", clientEntityPayload.isEmpty()
                        ? I18n.format("drtech.drone.ui.entity_payload_empty") : clientEntityPayload);
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
        dataManager.set(HAS_ROTOR_POWER, energy.getStored() > 0L);
    }

    private void syncRuntimeState() {
        dataManager.set(RUNTIME_STATE, runtime == null ? DroneRuntimeStatus.READY.ordinal()
                : runtime.getStatus().ordinal());
        dataManager.set(SAFETY_STATE, safetyFirmware.getState().ordinal());
    }

    private void syncHardwareState() {
        dataManager.set(CHASSIS_TIER, chassis.getMetadata());
        dataManager.set(UPGRADE_MASK, DroneHardwareStats.upgradeMask(upgrades));
        int slots = Math.max(1, getActiveCargoSlots());
        dataManager.set(CARGO_FILL_PERCENT, Math.min(100, countOccupiedCargoSlots() * 100 / slots));
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

    public float getVisualCargoFill() { return dataManager.get(CARGO_FILL_PERCENT) / 100.0F; }

    public DroneVisualState.FlightMarker getVisualFlightMarker() {
        int ordinal = dataManager.get(FLIGHT_MARKER);
        DroneVisualState.FlightMarker[] values = DroneVisualState.FlightMarker.values();
        return ordinal >= 0 && ordinal < values.length ? values[ordinal] : DroneVisualState.FlightMarker.NONE;
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
        if (loadedEntityData != null) {
            compound.setTag("LoadedEntity", loadedEntityData.copy());
            if (loadedEntityUuid != null) compound.setString("LoadedEntityUuid", loadedEntityUuid.toString());
        }
        writeEntityTargetLock(compound, "FollowTarget", followTargetUuid, followTargetAnchor);
        writeEntityTargetLock(compound, "AvoidTarget", avoidTargetUuid, avoidTargetAnchor);
        writeEntityTargetLock(compound, "AttackTarget", attackTargetUuid, attackTargetAnchor);
        writeEntityTargetLock(compound, "InteractionTarget", interactionTargetUuid, interactionTargetAnchor);
        writeEntityTargetLock(compound, "LoadTarget", loadTargetUuid, loadTargetAnchor);
        compound.setTag("DroneSafetyFirmware", safetyFirmware.writeToNbt());
        compound.setString("AutoPickupMode", autoPickupMode.name());
        compound.setTag("AutoPickupFilter", autoPickupFilter.writeToNbt());
        compound.setString("CargoFullPolicy", cargoFullPolicy.name());
        compound.setTag("DroneInventory", inventory.serializeNBT());
        compound.setTag("DroneWeapons", weapons.serializeNBT());
        compound.setTag("DroneFishingRod", fishingRod.serializeNBT());
        compound.setTag("DroneAlchemyJar", alchemyJar.serializeNBT());
        compound.setString("StatusLabel", getVisualStatusLabel());
        compound.setBoolean("RotorsActive", areVisualRotorsActive());
        compound.setInteger("StatusLightMode", getVisualStatusLightMode());
        compound.setTag("DroneFluidTank", fluidTank.writeToNBT(new NBTTagCompound()));
        compound.setLong("LogisticsEuCargo", logisticsEuCargo);
        if (boundDock != null) compound.setLong("BoundDock", boundDock.toLong());
        NBTTagList fallbackDocks = new NBTTagList();
        for (UUID id : fallbackDockIds) fallbackDocks.appendTag(new NBTTagString(id.toString()));
        compound.setTag("FallbackDocks", fallbackDocks);
        compound.setBoolean("ExecutionEnabled", executionEnabled);
        compound.setInteger("DockRecoveryCooldown", dockRecoveryCooldown);
        compound.setInteger("AlternateWeaponSlot", alternateWeaponSlot);
        compound.setBoolean("LoopProgram", loopProgram);
        compound.setLong("NextAlchemyTransferTick", nextAlchemyTransferTick);
        UUID ownerId = getOwnerId();
        if (ownerId != null) {
            compound.setString("Owner", ownerId.toString());
        }
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        nextAlchemyTransferTick = Math.max(0L, compound.getLong("NextAlchemyTransferTick"));
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
        loadedEntityData = null;
        loadedEntityUuid = null;
        if (compound.hasKey("LoadedEntity", 10)) {
            NBTTagCompound candidate = compound.getCompoundTag("LoadedEntity").copy();
            String id = candidate.getString("id");
            if (!id.isEmpty() && isEntityPayloadWithinLimit(candidate)) {
                loadedEntityData = candidate;
                loadedEntityUuid = readUuid(compound, "LoadedEntityUuid");
            }
        }
        followTargetUuid = readTargetUuid(compound, "FollowTarget");
        followTargetAnchor = readTargetAnchor(compound, "FollowTarget");
        avoidTargetUuid = readTargetUuid(compound, "AvoidTarget");
        avoidTargetAnchor = readTargetAnchor(compound, "AvoidTarget");
        attackTargetUuid = readTargetUuid(compound, "AttackTarget");
        attackTargetAnchor = readTargetAnchor(compound, "AttackTarget");
        interactionTargetUuid = readTargetUuid(compound, "InteractionTarget");
        interactionTargetAnchor = readTargetAnchor(compound, "InteractionTarget");
        loadTargetUuid = readTargetUuid(compound, "LoadTarget");
        loadTargetAnchor = readTargetAnchor(compound, "LoadTarget");
        if (compound.hasKey("DroneSafetyFirmware", 10)) {
            safetyFirmware.readFromNbt(compound.getCompoundTag("DroneSafetyFirmware"));
        }
        autoPickupMode = DroneAutoPickupMode.fromName(compound.getString("AutoPickupMode"));
        autoPickupFilter = compound.hasKey("AutoPickupFilter", 10)
                ? DroneItemFilterSpec.readFromNbt(compound.getCompoundTag("AutoPickupFilter"))
                : DroneItemFilterSpec.ANY;
        cargoFullPolicy = DroneCargoFullPolicy.fromName(compound.getString("CargoFullPolicy"));
        cargoFullPickupSuspended = false;
        if (compound.hasKey("DroneInventory", 10)) {
            NBTTagCompound savedInventory = compound.getCompoundTag("DroneInventory").copy();
            savedInventory.setInteger("Size", DroneHardwareStats.MAX_CARGO_SLOTS);
            inventory.deserializeNBT(savedInventory);
        }
        if (compound.hasKey("DroneWeapons", 10)) {
            NBTTagCompound savedWeapons = compound.getCompoundTag("DroneWeapons").copy();
            savedWeapons.setInteger("Size", 2);
            weapons.deserializeNBT(savedWeapons);
        }
        if (compound.hasKey("DroneFishingRod", 10)) {
            NBTTagCompound savedRod = compound.getCompoundTag("DroneFishingRod").copy();
            savedRod.setInteger("Size", 1);
            fishingRod.deserializeNBT(savedRod);
        }
        if (compound.hasKey("DroneAlchemyJar", 10)) {
            NBTTagCompound savedJar = compound.getCompoundTag("DroneAlchemyJar").copy();
            savedJar.setInteger("Size", 1);
            alchemyJar.deserializeNBT(savedJar);
        }
        dataManager.set(STATUS_LABEL, sanitizeStatusLabel(compound.getString("StatusLabel")));
        dataManager.set(ROTORS_ACTIVE, !compound.hasKey("RotorsActive") || compound.getBoolean("RotorsActive"));
        dataManager.set(STATUS_LIGHT_MODE, Math.max(0, Math.min(4, compound.getInteger("StatusLightMode"))));
        loadFluidTank(compound.hasKey("DroneFluidTank", 10) ? compound.getCompoundTag("DroneFluidTank") : null);
        logisticsEuCargo = Math.max(0L, compound.getLong("LogisticsEuCargo"));
        loadingHardware = false;
        DroneEnergyStorage savedEnergy = DroneEnergyStorage.readFromNbt(compound.getCompoundTag("DroneEnergy"),
                getConfiguredCapacity(), chassis.getVoltageTier());
        energy = new DroneEnergyStorage(getConfiguredCapacity(), chassis.getVoltageTier(), savedEnergy.getStored());
        applyChassisAttributes(false);
        boundDock = compound.hasKey("BoundDock", 4) ? BlockPos.fromLong(compound.getLong("BoundDock")) : null;
        fallbackDockIds.clear();
        NBTTagList fallbackDocks = compound.getTagList("FallbackDocks", 8);
        for (int index = 0; index < fallbackDocks.tagCount() && fallbackDockIds.size() < 8; index++) {
            try {
                UUID id = UUID.fromString(fallbackDocks.getStringTagAt(index));
                if (!fallbackDockIds.contains(id)) fallbackDockIds.add(id);
            } catch (IllegalArgumentException ignored) {
                // Ignore malformed saved preferences.
            }
        }
        executionEnabled = !compound.hasKey("ExecutionEnabled") || compound.getBoolean("ExecutionEnabled");
        dockRecoveryCooldown = Math.max(0, compound.getInteger("DockRecoveryCooldown"));
        alternateWeaponSlot = Math.max(0, compound.getInteger("AlternateWeaponSlot")) % 2;
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

    private static void writeEntityTargetLock(NBTTagCompound root, String key,
            @Nullable UUID targetId, @Nullable BlockPos anchor) {
        if (targetId == null || anchor == null) return;
        NBTTagCompound lock = new NBTTagCompound();
        lock.setString("Target", targetId.toString());
        lock.setLong("Anchor", anchor.toLong());
        root.setTag(key, lock);
    }

    @Nullable
    private static UUID readTargetUuid(NBTTagCompound root, String key) {
        return root.hasKey(key, 10) ? readUuid(root.getCompoundTag(key), "Target") : null;
    }

    @Nullable
    private static BlockPos readTargetAnchor(NBTTagCompound root, String key) {
        if (!root.hasKey(key, 10)) return null;
        NBTTagCompound lock = root.getCompoundTag(key);
        return lock.hasKey("Anchor", 4) ? BlockPos.fromLong(lock.getLong("Anchor")) : null;
    }

    private static String sanitizeStatusLabel(String value) {
        if (value == null) return "";
        StringBuilder result = new StringBuilder(Math.min(64, value.length()));
        for (int i = 0; i < value.length() && result.length() < 64; i++) {
            char c = value.charAt(i);
            if (c >= 32 && c != '\u007f' && c != '\u00a7') result.append(c);
        }
        return result.toString().trim();
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
            DroneCompileResult result = new DroneProgramCompiler(DroneExtensionRegistry.nodes()).compile(graph);
            result.getProgram().ifPresent(compiled -> {
                runtime = new DroneProgramRuntime(compiled, DroneExtensionRegistry.actions(),
                        DroneExtensionRegistry.sensors(), createRuntimeEnvironment());
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
            public BlockPos getCurrentPosition() {
                return EntityProgrammableDrone.this.getPosition();
            }

            @Override
            public int getAreaBlockLimit() {
                return DroneHardwareStats.areaBlockLimit(chassis);
            }

            @Override
            public boolean hasUpgrade(DroneUpgradeType type) {
                return DroneHardwareStats.hasUpgrade(upgrades, type);
            }

            @Override
            public void auditAction(ResourceLocation action, DroneExecutionResult result) {
                DroneActionAuditLog.record(EntityProgrammableDrone.this, action, result);
                if (result == null || result.getState() != DroneActionState.SUCCESS || action == null) return;
                String path = action.getPath();
                if (path.contains("break_block") || path.contains("fell_tree")) {
                    playCue(DroneSoundCue.INSTANT_BREAK, 0.7F, 1.0F);
                }
                else if (path.contains("place_block") || path.contains("place_area")) {
                    playCue(DroneSoundCue.PLACE_CONFIRM, 0.65F, 1.05F);
                } else if (path.contains("harvest") || path.contains("replant") || path.contains("repair") || path.contains("interact")
                        || path.contains("use_item")) {
                    playCue(DroneSoundCue.TOOL_ARM, 0.6F, 1.0F);
                }
            }

            @Override
            public DroneExecutionResult remoteAlert(String message) {
                UUID ownerId = getOwnerId();
                EntityPlayer owner = ownerId == null ? null : world.getPlayerEntityByUUID(ownerId);
                if (owner == null) {
                    return DroneExecutionResult.failure(DroneActionStatus.NOT_FOUND, "failed",
                            "Drone owner is not online");
                }
                String bounded = message == null ? "" : message;
                if (bounded.length() > 256) bounded = bounded.substring(0, 256);
                owner.sendMessage(new TextComponentString("[Drone " + droneId.toString().substring(0, 8)
                        + "] " + bounded));
                return DroneExecutionResult.success();
            }

            @Override
            public double getEnergyPercent() {
                return energy.getCapacity() == 0L ? 0.0D : energy.getStored() * 100.0D / energy.getCapacity();
            }

            @Override
            public CompiledDroneProgram resolveProgram(DroneProgramReference reference) {
                UUID owner = getOwnerId();
                if (owner == null || reference == null || world == null) return null;
                return DroneProgramLibrary.get(world).resolve(owner, reference)
                        .map(graph -> new DroneProgramCompiler(DroneExtensionRegistry.nodes()).compile(graph)
                                .getProgram().orElse(null)).orElse(null);
            }

            @Override
            public DroneExecutionResult moveTo(BlockPos target) {
                if (!areVisualRotorsActive() && safetyFirmware.getState() == DroneSafetyState.PROGRAM) {
                    return DroneExecutionResult.failure(DroneActionStatus.DENIED, "failed",
                            "Movement is unavailable while rotors are in standby mode");
                }
                BlockPos current = new BlockPos(posX, posY, posZ);
                debugPathTarget = target;
                if (current.equals(target) && getDistanceSqToCenter(target) <= 0.64D) {
                    motionX *= 0.25D;
                    motionY *= 0.25D;
                    motionZ *= 0.25D;
                    debugPathWaypoint = target;
                    debugPathWaypointIndex = Math.max(0, debugPathLength);
                    return DroneExecutionResult.success();
                }
                if (!target.equals(pathTarget) || path.isEmpty() || waypointIndex >= path.size()) {
                    if (!consumeEnergy(DroneEnergyCosts.PATHFIND)) return insufficientEnergy();
                    DronePathResult result = pathfinder.findPath(current, target, navigationWorld,
                            DroneHardwareStats.navigationRange(upgrades),
                            DroneHardwareStats.navigationNodeBudget(upgrades));
                    if (!result.isFound()) {
                        debugPathWaypoint = null;
                        debugPathWaypointIndex = 0;
                        debugPathLength = 0;
                        DroneActionStatus status = result.getStatus()
                                == com.drppp.drtech.common.drone.navigation.DronePathStatus.OUT_OF_RANGE
                                ? DroneActionStatus.OUT_OF_RANGE : DroneActionStatus.UNREACHABLE;
                        return DroneExecutionResult.failure(status, "failed",
                                "Pathfinding failed: " + result.getStatus());
                    }
                    path = result.getPath();
                    debugPathPoints = path;
                    pathTarget = target;
                    waypointIndex = path.size() > 1 ? 1 : 0;
                    debugPathLength = path.size();
                }
                BlockPos waypoint = path.get(waypointIndex);
                debugPathWaypoint = waypoint;
                debugPathWaypointIndex = waypointIndex;
                if (!navigationWorld.isPassable(waypoint)) {
                    path = Collections.emptyList();
                    debugPathPoints = Collections.emptyList();
                    debugPathWaypoint = null;
                    debugPathLength = 0;
                    return DroneExecutionResult.failure(DroneActionStatus.UNREACHABLE, "failed",
                            "Navigation waypoint became blocked");
                }
                double dx = waypoint.getX() + 0.5D - posX;
                double dy = waypoint.getY() + 0.5D - posY;
                double dz = waypoint.getZ() + 0.5D - posZ;
                double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
                if (distance <= 0.28D) {
                    waypointIndex++;
                    debugPathWaypointIndex = waypointIndex;
                    if (waypointIndex >= path.size()) {
                        motionX *= 0.25D;
                        motionY *= 0.25D;
                        motionZ *= 0.25D;
                        debugPathWaypoint = target;
                        return DroneExecutionResult.success();
                    }
                    debugPathWaypoint = path.get(waypointIndex);
                    return DroneExecutionResult.running();
                }
                if (!consumeEnergy(DroneEnergyCosts.MOVE_PER_TICK)) return insufficientEnergy();
                double speed = DroneHardwareStats.movementSpeed(chassis, upgrades);
                motionX = dx / distance * speed;
                motionY = dy / distance * speed;
                motionZ = dz / distance * speed;
                rotationYaw = (float) (Math.atan2(motionZ, motionX) * 180.0D / Math.PI) - 90.0F;
                return DroneExecutionResult.running();
            }

            @Nullable
            @Override
            public BlockPos findNearestPassablePosition(DroneArea area, long[] excludedPositions) {
                if (area == null || area.getVolume() <= 0L) return null;
                BlockPos current = EntityProgrammableDrone.this.getPosition();
                java.util.Set<Long> excluded = new java.util.HashSet<>();
                if (excludedPositions != null) {
                    for (long packed : excludedPositions) excluded.add(packed);
                }
                if (area.contains(current) && navigationWorld.isPassable(current)
                        && !excluded.contains(current.toLong())) return current;
                BlockPos nearest = null;
                long nearestDistance = Long.MAX_VALUE;
                for (int index = 0; index < area.getVolume(); index++) {
                    BlockPos candidate = area.positionAt(index);
                    if (excluded.contains(candidate.toLong()) || !navigationWorld.isPassable(candidate)) continue;
                    long dx = (long) candidate.getX() - current.getX();
                    long dy = (long) candidate.getY() - current.getY();
                    long dz = (long) candidate.getZ() - current.getZ();
                    long distance = dx * dx + dy * dy + dz * dz;
                    if (distance < nearestDistance) {
                        nearest = candidate;
                        nearestDistance = distance;
                    }
                }
                return nearest;
            }

            @Override
            public DroneExecutionResult breakBlock(BlockPos target) {
                if (!world.isBlockLoaded(target)) return DroneExecutionResult.error("Break target is not loaded");
                if (world.isAirBlock(target)) return DroneExecutionResult.success();
                DroneExecutionResult approach = approach(target);
                if (approach.getState() != DroneActionState.SUCCESS) {
                    return approach;
                }
                if (!consumeEnergy(DroneEnergyCosts.BLOCK_INTERACTION)) return insufficientEnergy();
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
                if (!consumeEnergy(DroneEnergyCosts.BLOCK_INTERACTION)) return insufficientEnergy();
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
                if (!consumeEnergy(DroneEnergyCosts.BLOCK_INTERACTION)) return insufficientEnergy();
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
            public DroneExecutionResult useItem(DroneItemFilter filter, boolean sneaking) {
                if (!consumeEnergy(DroneEnergyCosts.BLOCK_INTERACTION)) return insufficientEnergy();
                DroneWorldActions.InteractionOutcome outcome = DroneWorldActions.useItem(
                        EntityProgrammableDrone.this, inventory, filter, sneaking);
                if (outcome == DroneWorldActions.InteractionOutcome.SUCCESS) return DroneExecutionResult.success();
                if (outcome == DroneWorldActions.InteractionOutcome.NO_ITEM) {
                    return DroneExecutionResult.failure(DroneActionStatus.NO_RESOURCE, "failed",
                            "No matching item is available");
                }
                return DroneExecutionResult.failure(DroneActionStatus.DENIED, "failed",
                        "Item use was denied or had no effect");
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
                if (!consumeEnergy(DroneEnergyCosts.BLOCK_INTERACTION)) return insufficientEnergy();
                return DroneWorldActions.harvestCrop(EntityProgrammableDrone.this, inventory, target)
                        ? DroneExecutionResult.success()
                        : DroneExecutionResult.failure(DroneActionStatus.DENIED, "failed",
                                "Crop harvest was denied");
            }

            @Override
            public DroneExecutionResult fellTreeBlock(BlockPos target) {
                if (!world.isBlockLoaded(target)) return DroneExecutionResult.failure(DroneActionStatus.UNLOADED,
                        "failed", "Tree target is not loaded");
                if (!DroneWorldActions.isTreeLog((net.minecraft.world.WorldServer) world, target)) {
                    return DroneExecutionResult.success(0);
                }
                DroneExecutionResult movement = approach(target);
                if (movement.getState() != DroneActionState.SUCCESS) return movement;
                if (!consumeEnergy(DroneEnergyCosts.BLOCK_INTERACTION)) return insufficientEnergy();
                return DroneWorldActions.breakBlock(EntityProgrammableDrone.this, inventory, target)
                        ? DroneExecutionResult.success(1)
                        : DroneExecutionResult.failure(DroneActionStatus.DENIED, "failed",
                                "Tree log break was denied or the block is unbreakable");
            }

            @Override
            public DroneExecutionResult replant(BlockPos target, DroneItemFilter filter) {
                if (!world.isBlockLoaded(target) || !world.isBlockLoaded(target.down())) {
                    return DroneExecutionResult.failure(DroneActionStatus.UNLOADED, "failed",
                            "Planting target or soil is not loaded");
                }
                if (!DroneWorldActions.isPotentialPlantingTarget((net.minecraft.world.WorldServer) world, target)) {
                    return DroneExecutionResult.success(0);
                }
                DroneExecutionResult movement = approach(target);
                if (movement.getState() != DroneActionState.SUCCESS) return movement;
                if (!hasEnergy(DroneEnergyCosts.BLOCK_INTERACTION)) return insufficientEnergy();
                DroneWorldActions.PlantingOutcome outcome = DroneWorldActions.replant(
                        EntityProgrammableDrone.this, inventory, target, filter);
                if (outcome == DroneWorldActions.PlantingOutcome.PLANTED) {
                    consumeEnergy(DroneEnergyCosts.BLOCK_INTERACTION);
                    return DroneExecutionResult.success(1);
                }
                if (outcome == DroneWorldActions.PlantingOutcome.SKIPPED) return DroneExecutionResult.success(0);
                if (outcome == DroneWorldActions.PlantingOutcome.NO_ITEM) {
                    return DroneExecutionResult.failure(DroneActionStatus.NO_RESOURCE, "failed",
                            "No matching sapling or crop seed is available in drone cargo");
                }
                return DroneExecutionResult.failure(DroneActionStatus.DENIED, "failed",
                        "Planting was denied or the selected plant cannot survive on this soil");
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
                if (!consumeEnergy(DroneEnergyCosts.BLOCK_INTERACTION)) return insufficientEnergy();
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
                return transferEu(DroneEuTransfer.importToDrone(energy, endpoint, maximumEu,
                        DroneHardwareStats.euTransferAmperage(upgrades)), true);
            }

            @Override
            public DroneExecutionResult exportEnergy(BlockPos target, long maximumEu) {
                DroneExecutionResult movement = approach(target);
                if (movement.getState() != DroneActionState.SUCCESS) return movement;
                DroneEuEndpoint endpoint = findEnergyEndpoint(target);
                if (endpoint == null) return DroneExecutionResult.failure(DroneActionStatus.INVALID_TARGET,
                        "failed", "Target has no GregTech EU container");
                return transferEu(DroneEuTransfer.exportFromDrone(energy, endpoint, maximumEu,
                        DroneHardwareStats.euTransferAmperage(upgrades)), false);
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
                if (!consumeEnergy(DroneEnergyCosts.ENTITY_INTERACTION)) return insufficientEnergy();
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
                if (stored > 0) emitFluidTransferEffect(target, drained, true);
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
                if (!consumeEnergy(DroneEnergyCosts.ENTITY_INTERACTION)) return insufficientEnergy();
                FluidStack drained = fluidTank.drain(accepted, true);
                if (drained == null || drained.amount <= 0) return DroneExecutionResult.error(
                        "Drone fluid cargo changed while exporting");
                int moved = remote.fill(drained, true);
                if (moved < drained.amount) {
                    FluidStack remainder = drained.copy();
                    remainder.amount -= moved;
                    fluidTank.fill(remainder, true);
                }
                if (moved > 0) emitFluidTransferEffect(target, drained, false);
                return moved > 0 ? DroneExecutionResult.running(moved)
                        : DroneExecutionResult.error("Fluid export committed no fluid");
            }

            @Override
            public DroneExecutionResult drainFluid(int maximumAmount, DroneFluidFilterSpec filter) {
                FluidStack stored = fluidTank.getFluid();
                if (stored == null || !filter.matches(stored)) return DroneExecutionResult.failure(
                        DroneActionStatus.NO_RESOURCE, "failed", "Drone fluid cargo contains no matching fluid");
                FluidStack drained = fluidTank.drain(Math.max(1, maximumAmount), true);
                if (drained != null && drained.amount > 0) {
                    emitFluidTransferEffect(new BlockPos(EntityProgrammableDrone.this), drained, false);
                }
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
                if (possible <= 0) return handleItemCargoFull(
                        "Drone cargo has no space for matching dropped items");
                if (!consumeEnergy(DroneEnergyCosts.ENTITY_INTERACTION)) return insufficientEnergy();
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
                if (!consumeEnergy(DroneEnergyCosts.ENTITY_INTERACTION)) return insufficientEnergy();
                int dropped = dropCargoItems(filter, request.getMaximumAmount());
                return dropped > 0 ? DroneExecutionResult.success(dropped)
                        : DroneExecutionResult.error("Items changed while dropping");
            }

            @Override
            public DroneExecutionResult craftItems(DroneItemFilter outputFilter, int maximumCrafts,
                    boolean simulate, boolean requireExactCount, DroneItemFilter reserveFilter, int reserveAmount) {
                if (!DroneHardwareStats.hasUpgrade(upgrades, DroneUpgradeType.CRAFTING)) {
                    return DroneExecutionResult.failure(DroneActionStatus.DENIED, "failed",
                            "Drone crafting module is not installed");
                }
                if (outputFilter == null || outputFilter.getSpec().getMode() != DroneFilterMode.WHITELIST
                        || outputFilter.getSpec().getRules().isEmpty()) {
                    return DroneExecutionResult.failure(DroneActionStatus.INVALID_TARGET, "failed",
                            "Crafting requires a non-empty output whitelist");
                }
                int requested = Math.max(1, Math.min(DroneCraftingPlanner.MAX_CRAFTS_PER_ACTION, maximumCrafts));
                int possible = DroneCraftingPlanner.craft(inventory, getActiveCargoSlots(), world,
                        outputFilter, requested, true, reserveFilter, reserveAmount);
                if (possible <= 0 || requireExactCount && possible < requested) {
                    return DroneExecutionResult.failure(DroneActionStatus.NO_RESOURCE, "failed",
                            "Cargo lacks ingredients or output space for the requested recipe");
                }
                int crafts = requireExactCount ? requested : possible;
                if (simulate) return DroneExecutionResult.success(crafts);
                long energyCost = Math.min(Long.MAX_VALUE / 2L,
                        DroneEnergyCosts.ENTITY_INTERACTION * (long) crafts);
                if (!consumeEnergy(energyCost)) return insufficientEnergy();
                int crafted = DroneCraftingPlanner.craft(inventory, getActiveCargoSlots(), world,
                        outputFilter, crafts, false, reserveFilter, reserveAmount);
                return crafted > 0 ? DroneExecutionResult.success(crafted)
                        : DroneExecutionResult.error("Cargo changed while committing the crafting plan");
            }

            @Override
            public int getCraftableCount(DroneItemFilter outputFilter, int limit,
                    DroneItemFilter reserveFilter, int reserveAmount) {
                if (!DroneHardwareStats.hasUpgrade(upgrades, DroneUpgradeType.CRAFTING)) return 0;
                return DroneCraftingPlanner.craft(inventory, getActiveCargoSlots(), world, outputFilter,
                        Math.max(1, Math.min(DroneCraftingPlanner.MAX_CRAFTS_PER_ACTION, limit)), true,
                        reserveFilter, reserveAmount);
            }

            @Override
            public DroneExecutionResult craftGrid(DroneItemFilter outputFilter, DroneItemFilter[] gridFilters,
                    int maximumCrafts, boolean requireExactCount,
                    DroneItemFilter reserveFilter, int reserveAmount) {
                if (!DroneHardwareStats.hasUpgrade(upgrades, DroneUpgradeType.CRAFTING)) {
                    return DroneExecutionResult.failure(DroneActionStatus.DENIED, "failed",
                            "Drone crafting module is not installed");
                }
                if (outputFilter == null || outputFilter.getSpec().getMode() != DroneFilterMode.WHITELIST
                        || outputFilter.getSpec().getRules().isEmpty()) {
                    return DroneExecutionResult.failure(DroneActionStatus.INVALID_TARGET, "failed",
                            "Explicit crafting requires a non-empty output whitelist");
                }
                int requested = Math.max(1, Math.min(DroneCraftingPlanner.MAX_CRAFTS_PER_ACTION, maximumCrafts));
                int possible = DroneCraftingPlanner.craftGrid(inventory, getActiveCargoSlots(), world,
                        outputFilter, gridFilters, requested, true, reserveFilter, reserveAmount);
                if (possible <= 0 || requireExactCount && possible < requested) {
                    return DroneExecutionResult.failure(DroneActionStatus.NO_RESOURCE, "failed",
                            "Cargo, output space, reserve floor, or explicit 3x3 recipe does not match");
                }
                int crafts = requireExactCount ? requested : possible;
                long energyCost = Math.min(Long.MAX_VALUE / 2L,
                        DroneEnergyCosts.ENTITY_INTERACTION * (long) crafts);
                if (!consumeEnergy(energyCost)) return insufficientEnergy();
                int crafted = DroneCraftingPlanner.craftGrid(inventory, getActiveCargoSlots(), world,
                        outputFilter, gridFilters, crafts, false, reserveFilter, reserveAmount);
                return crafted > 0 ? DroneExecutionResult.success(crafted)
                        : DroneExecutionResult.error("Cargo changed while committing the explicit crafting plan");
            }

            @Override
            public DroneExecutionResult setMachineWorking(BlockPos target, boolean enabled) {
                if (!world.isBlockLoaded(target)) return DroneExecutionResult.failure(DroneActionStatus.UNLOADED,
                        "failed", "GregTech machine target is not loaded");
                IWorkable workable = findMachineWorkable(target);
                if (workable == null) return DroneExecutionResult.failure(DroneActionStatus.INVALID_TARGET,
                        "failed", "Target exposes no GregTech workable capability");
                DroneExecutionResult movement = approach(target);
                if (movement.getState() != DroneActionState.SUCCESS) return movement;
                if (!consumeEnergy(DroneEnergyCosts.BLOCK_INTERACTION)) return insufficientEnergy();
                workable.setWorkingEnabled(enabled);
                return DroneExecutionResult.success();
            }

            @Override
            public DroneExecutionResult waitForMachineIdle(BlockPos target) {
                if (!world.isBlockLoaded(target)) return DroneExecutionResult.failure(DroneActionStatus.UNLOADED,
                        "failed", "GregTech machine target is not loaded");
                IWorkable workable = findMachineWorkable(target);
                if (workable == null) return DroneExecutionResult.failure(DroneActionStatus.INVALID_TARGET,
                        "failed", "Target exposes no GregTech workable capability");
                DroneExecutionResult movement = approach(target);
                if (movement.getState() != DroneActionState.SUCCESS) return movement;
                return workable.isActive() ? DroneExecutionResult.running() : DroneExecutionResult.success();
            }

            @Override
            public DroneExecutionResult waitForMachineCycle(BlockPos target, boolean observedActive,
                    double previousProgressPercent) {
                if (!world.isBlockLoaded(target)) return DroneExecutionResult.failure(DroneActionStatus.UNLOADED,
                        "failed", "GregTech machine target is not loaded");
                IWorkable workable = findMachineWorkable(target);
                if (workable == null) return DroneExecutionResult.failure(DroneActionStatus.INVALID_TARGET,
                        "failed", "Target exposes no GregTech workable capability");
                DroneExecutionResult movement = approach(target);
                if (movement.getState() != DroneActionState.SUCCESS) return movement;
                if (!observedActive || previousProgressPercent < 0.0D) return DroneExecutionResult.running();

                double currentProgress = getMachineProgressPercent(target);
                AbstractRecipeLogic logic = findMachineRecipeLogic(target);
                boolean lowEnergy = logic != null && logic.isHasNotEnoughEnergy();
                boolean completedToIdle = previousProgressPercent > 0.0D && currentProgress <= 0.0D
                        && !workable.isActive();
                boolean startedNextRecipe = previousProgressPercent >= 75.0D
                        && currentProgress + 0.001D < previousProgressPercent && workable.isActive();
                return workable.isWorkingEnabled() && !lowEnergy && (completedToIdle || startedNextRecipe)
                        ? DroneExecutionResult.success() : DroneExecutionResult.running();
            }

            @Override
            public boolean isMachineActive(BlockPos target) {
                IWorkable workable = findMachineWorkable(target);
                return workable != null && workable.isActive();
            }

            @Override
            public boolean isMachineWorkingEnabled(BlockPos target) {
                IWorkable workable = findMachineWorkable(target);
                return workable != null && workable.isWorkingEnabled();
            }

            @Override
            public double getMachineProgressPercent(BlockPos target) {
                IWorkable workable = findMachineWorkable(target);
                if (workable == null) return -1.0D;
                int maximum = workable.getMaxProgress();
                return maximum <= 0 ? 0.0D : Math.min(100.0D, workable.getProgress() * 100.0D / maximum);
            }

            @Override
            public boolean isMachineWaitingForInput(BlockPos target) {
                AbstractRecipeLogic logic = findMachineRecipeLogic(target);
                if (logic == null || logic.isActive() || !logic.isWorkingEnabled()) return false;
                String diagnostic = logic.getWhyFailed();
                return "NoneRecipes".equals(diagnostic)
                        || (diagnostic != null && diagnostic.contains("输入可能被"));
            }

            @Override
            public boolean isMachineOutputBlocked(BlockPos target) {
                AbstractRecipeLogic logic = findMachineRecipeLogic(target);
                return logic != null && isOutputDiagnostic(logic.getWhyFailed());
            }

            @Override
            public boolean isMachineLowEnergy(BlockPos target) {
                AbstractRecipeLogic logic = findMachineRecipeLogic(target);
                return logic != null && logic.isHasNotEnoughEnergy();
            }

            @Override
            public String getMachineDiagnostic(BlockPos target) {
                AbstractRecipeLogic logic = findMachineRecipeLogic(target);
                if (logic == null) return "目标不是支持配方诊断的 GregTech 机器";
                String diagnostic = logic.getWhyFailed();
                if (diagnostic == null || diagnostic.isEmpty()) return "";
                return "NoneRecipes".equals(diagnostic)
                        ? "没有匹配的配方（通常是输入不足或输入不匹配）" : diagnostic;
            }

            @Override
            public DroneExecutionResult repairMachine(BlockPos target, boolean requireAllTools) {
                if (!world.isBlockLoaded(target)) return DroneExecutionResult.failure(DroneActionStatus.UNLOADED,
                        "failed", "GT 机器维修目标区块未加载");
                IMaintenance maintenance = findMachineMaintenance(target);
                if (maintenance == null) return DroneExecutionResult.failure(DroneActionStatus.INVALID_TARGET,
                        "failed", "目标不是支持维护能力的 GT 多方块控制器");
                if (!maintenance.hasMaintenanceMechanics() || !maintenance.hasMaintenanceProblems()) {
                    return DroneExecutionResult.success(0L);
                }
                DroneExecutionResult movement = approach(target);
                if (movement.getState() != DroneActionState.SUCCESS) return movement;

                List<int[]> matches = new ArrayList<>();
                Set<Integer> usedSlots = new HashSet<>();
                int required = maintenance.getToolsForMaintenance().size();
                for (it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry<String> problem
                        : maintenance.getToolsForMaintenance()) {
                    int matchedSlot = -1;
                    for (int slot = 0; slot < getActiveCargoSlots(); slot++) {
                        if (usedSlots.contains(slot)) continue;
                        ItemStack stack = inventory.getStackInSlot(slot);
                        if (!stack.isEmpty() && ToolHelper.isTool(stack, problem.getValue())) {
                            matchedSlot = slot;
                            break;
                        }
                    }
                    if (matchedSlot >= 0) {
                        matches.add(new int[] { problem.getIntKey(), matchedSlot });
                        usedSlots.add(matchedSlot);
                    }
                }
                if ((requireAllTools && matches.size() < required) || matches.isEmpty()) {
                    return DroneExecutionResult.failure(DroneActionStatus.NO_RESOURCE, "failed",
                            "无人机货舱缺少当前维护故障所需的 GT 工具");
                }
                long energyCost = DroneEnergyCosts.BLOCK_INTERACTION * matches.size();
                if (!consumeEnergy(energyCost)) return insufficientEnergy();
                int repaired = 0;
                for (int[] match : matches) {
                    ItemStack stack = inventory.getStackInSlot(match[1]);
                    if (stack.isEmpty()) continue;
                    ToolHelper.damageItemWhenCrafting(stack, EntityProgrammableDrone.this);
                    inventory.setStackInSlot(match[1], stack);
                    maintenance.setMaintenanceFixed(match[0]);
                    repaired++;
                }
                if (repaired > 0) {
                    world.playSound(null, target, SoundEvents.BLOCK_ANVIL_USE, SoundCategory.BLOCKS, 0.55F, 1.25F);
                }
                return DroneExecutionResult.success(repaired);
            }

            @Override
            public boolean needsMachineMaintenance(BlockPos target) {
                IMaintenance maintenance = findMachineMaintenance(target);
                return maintenance != null && maintenance.hasMaintenanceProblems();
            }

            @Override
            public int getMachineMaintenanceProblemCount(BlockPos target) {
                IMaintenance maintenance = findMachineMaintenance(target);
                return maintenance == null ? 0 : maintenance.getNumMaintenanceProblems();
            }

            @Override
            public DroneExecutionResult transferThaumcraftEssentia(BlockPos smelter, DroneArea tubeArea,
                    int maxAmount) {
                if (!net.minecraftforge.fml.common.Loader.isModLoaded("thaumcraft")) {
                    return DroneExecutionResult.failure(DroneActionStatus.NOT_FOUND, "failed",
                            "Thaumcraft 6 is not installed");
                }
                if (!DroneHardwareStats.hasUpgrade(upgrades, DroneUpgradeType.THAUMCRAFT_ALCHEMY)) {
                    return DroneExecutionResult.failure(DroneActionStatus.DENIED, "failed",
                            "Thaumcraft alchemy upgrade is required");
                }
                if (!isThaumcraftEssentiaJar(alchemyJar.getStackInSlot(0))) {
                    return DroneExecutionResult.failure(DroneActionStatus.NO_RESOURCE, "failed",
                            "Place a Thaumcraft essentia jar in the alchemy jar slot");
                }
                if (tubeArea == null && smelter != null) {
                    tubeArea = DroneArea.between(smelter.add(-2, -2, -2), smelter.add(2, 2, 2));
                }
                if (smelter == null || tubeArea == null || !tubeArea.isWithinRuntimeLimits()
                        || tubeArea.getVolume() > getAreaBlockLimit()) {
                    return DroneExecutionResult.failure(DroneActionStatus.INVALID_TARGET, "failed",
                            "Smelter coordinate and a valid tube area are required");
                }
                if (!world.isBlockLoaded(smelter)) {
                    return DroneExecutionResult.failure(DroneActionStatus.UNLOADED, "failed",
                            "Thaumcraft smelter chunk is not loaded");
                }
                DroneExecutionResult movement = approach(smelter);
                if (movement.getState() != DroneActionState.SUCCESS) return movement;
                if (!hasEnergy(DroneEnergyCosts.ENTITY_INTERACTION)) return insufficientEnergy();
                if (world.getTotalWorldTime() < nextAlchemyTransferTick) {
                    return DroneExecutionResult.running();
                }
                ThaumcraftEssentiaNodeCompat.TransferResult transfer = ThaumcraftEssentiaNodeCompat.transfer(
                        world, smelter, tubeArea, Math.max(1, Math.min(250, maxAmount)));
                int moved = transfer.getAmountOrError();
                if (moved > 0) {
                    consumeEnergy(DroneEnergyCosts.ENTITY_INTERACTION);
                    syncAlchemyJar();
                    dataManager.set(ALCHEMY_ASPECT_COLOR, transfer.getAspectColor());
                    dataManager.set(ALCHEMY_ANIMATION_TICKS, 16);
                    nextAlchemyTransferTick = world.getTotalWorldTime() + ALCHEMY_TRANSFER_INTERVAL_TICKS;
                    return DroneExecutionResult.success(moved);
                }
                if (moved == ThaumcraftEssentiaNodeCompat.UNLOADED) {
                    return DroneExecutionResult.failure(DroneActionStatus.UNLOADED, "failed",
                            "Part of the smelter, alembic stack, or tube area is not loaded");
                }
                if (moved == ThaumcraftEssentiaNodeCompat.INVALID_SMELTER) {
                    return DroneExecutionResult.failure(DroneActionStatus.INVALID_TARGET, "failed",
                            "The supplied coordinate is not a Thaumcraft 6 essentia smelter");
                }
                if (moved == ThaumcraftEssentiaNodeCompat.NO_SOURCE) {
                    return DroneExecutionResult.failure(DroneActionStatus.NO_RESOURCE, "failed",
                            "No essentia is available in the alembics above the smelter");
                }
                if (moved == ThaumcraftEssentiaNodeCompat.NO_TUBE_NETWORK) {
                    return DroneExecutionResult.failure(DroneActionStatus.INVALID_TARGET, "failed",
                            "The supplied jar area contains no Thaumcraft essentia jars");
                }
                if (moved == ThaumcraftEssentiaNodeCompat.NO_DESTINATION) {
                    return DroneExecutionResult.failure(DroneActionStatus.NOT_FOUND, "failed",
                            "No essentia jar is connected to the selected tube network");
                }
                return DroneExecutionResult.failure(DroneActionStatus.NO_SPACE, "failed",
                        "Connected jars are full, labeled for another aspect, or contain another aspect");
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
                        && (filter == null ? DroneBlockFilterSpec.ANY : filter).matches(world, target);
            }

            @Override
            public boolean isAirBlock(BlockPos target) {
                return target != null && world.isBlockLoaded(target) && world.isAirBlock(target);
            }

            @Override
            public boolean isCoordinateReachable(BlockPos target) {
                if (target == null || !world.isBlockLoaded(target)) return false;
                BlockPos current = new BlockPos(posX, posY, posZ);
                return pathfinder.findPath(current, target, navigationWorld,
                        DroneHardwareStats.navigationRange(upgrades),
                        DroneHardwareStats.navigationNodeBudget(upgrades)).isFound();
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
                    if (world.isBlockLoaded(target) && checked.matches(world, target)) count++;
                }
                return count;
            }

            @Override
            public DroneExecutionResult interactWithNearestEntity(BlockPos target,
                    DroneEntityFilterSpec entityFilter) {
                return interactEntity(target, false, DroneItemFilter.ANY, entityFilter);
            }

            @Override
            public DroneExecutionResult useItemOnEntity(BlockPos target) {
                return useItemOnEntity(target, DroneItemFilter.ANY);
            }

            @Override
            public DroneExecutionResult useItemOnEntity(BlockPos target, DroneItemFilter filter,
                    DroneEntityFilterSpec entityFilter) {
                return interactEntity(target, true, filter, entityFilter);
            }

            @Override
            public DroneExecutionResult attackEntity(BlockPos target, DroneEntityFilterSpec filter) {
                return attackEntityInternal(target, filter, false, null, 0, "", false,
                        "STRONGEST", 8);
            }

            @Override
            public DroneExecutionResult attackEntity(BlockPos target, DroneEntityFilterSpec filter,
                    boolean untilDefeated) {
                return attackEntityInternal(target, filter, untilDefeated, null, 0, "", false,
                        "STRONGEST", 8);
            }

            @Override
            public DroneExecutionResult attackEntity(BlockPos target, DroneEntityFilterSpec filter,
                    boolean untilDefeated, String weaponMode, int attackIntervalTicks) {
                return attackEntityInternal(target, filter, untilDefeated, null, 0, "", false,
                        weaponMode, attackIntervalTicks);
            }

            @Override
            public DroneExecutionResult attackEntityInArea(BlockPos target, DroneEntityFilterSpec filter,
                    boolean untilDefeated, DroneArea patrolArea, int maxChaseDistance) {
                return attackEntityInternal(target, filter, untilDefeated, patrolArea,
                        Math.max(0, Math.min(128, maxChaseDistance)), "", false, "STRONGEST", 8);
            }

            @Override
            public DroneExecutionResult attackEntityInArea(BlockPos target, DroneEntityFilterSpec filter,
                    boolean untilDefeated, DroneArea patrolArea, int maxChaseDistance,
                    String expectedEntityUuid, boolean hostileOnly) {
                return attackEntityInternal(target, filter, untilDefeated, patrolArea,
                        Math.max(0, Math.min(128, maxChaseDistance)), expectedEntityUuid, hostileOnly,
                        "STRONGEST", 8);
            }

            @Override
            public DroneExecutionResult attackEntityInArea(BlockPos target, DroneEntityFilterSpec filter,
                    boolean untilDefeated, DroneArea patrolArea, int maxChaseDistance,
                    String expectedEntityUuid, boolean hostileOnly, String weaponMode, int attackIntervalTicks) {
                return attackEntityInternal(target, filter, untilDefeated, patrolArea,
                        Math.max(0, Math.min(128, maxChaseDistance)), expectedEntityUuid, hostileOnly,
                        weaponMode, attackIntervalTicks);
            }

            private DroneExecutionResult attackEntityInternal(BlockPos target, DroneEntityFilterSpec filter,
                    boolean untilDefeated, @Nullable DroneArea patrolArea, int maxChaseDistance,
                    String expectedEntityUuid, boolean hostileOnly, String weaponMode, int attackIntervalTicks) {
                if (!DroneHardwareStats.hasUpgrade(upgrades, DroneUpgradeType.COMBAT)) {
                    return DroneExecutionResult.failure(DroneActionStatus.DENIED, "failed",
                            "Combat module is not installed");
                }
                if (!DroneWorldActions.hasUsableWeapon(weapons, weaponMode, alternateWeaponSlot)) {
                    return DroneExecutionResult.failure(DroneActionStatus.NO_RESOURCE, "failed",
                            "The selected combat weapon slot does not contain a usable weapon");
                }
                if (attackCooldownTicks > 0) return DroneExecutionResult.running(attackCooldownTicks);
                setVisualEntityTarget(null);
                EntityLivingBase entity = expectedEntityUuid == null || expectedEntityUuid.isEmpty()
                        ? lockedAttackTarget(target, filter)
                        : expectedAttackTarget(target, filter, expectedEntityUuid, hostileOnly);
                if (entity == null) {
                    setVisualEntityTarget(null);
                    return DroneExecutionResult.failure(DroneActionStatus.NOT_FOUND,
                            "failed", "No living entity exists near the target coordinate");
                }
                if (++attackChaseTicks > ENTITY_CHASE_TICK_LIMIT || isBeyondEntityChaseLimit(entity, target)) {
                    clearAttackTarget();
                    setVisualEntityTarget(null);
                    attackChaseTicks = 0;
                    return entityChaseExceeded();
                }
                if (patrolArea != null && !isInsidePatrolLeash(entity.getPosition(), patrolArea, maxChaseDistance)) {
                    clearAttackTarget();
                    return DroneExecutionResult.failure(DroneActionStatus.OUT_OF_RANGE, "failed",
                            "Patrol target moved beyond the chase boundary");
                }
                if (!entity.isNonBoss()) return DroneExecutionResult.failure(DroneActionStatus.DENIED,
                        "failed", "Boss entities cannot be attacked by drones");
                if (entity instanceof IEntityOwnable ownable && ownable.getOwnerId() != null) {
                    if (!isDroneOwner(ownable.getOwnerId())) return DroneExecutionResult.failure(
                            DroneActionStatus.DENIED, "failed", "Entities owned by another player cannot be attacked");
                    if (filter == null || !filter.isAllowTransport()) return DroneExecutionResult.failure(
                            DroneActionStatus.DENIED, "failed",
                            "Entities owned by the drone owner require explicit transport-entity permission");
                }
                if (entity instanceof EntityPlayer player && isDroneOwner(player.getUniqueID())) {
                    return DroneExecutionResult.failure(DroneActionStatus.DENIED, "failed",
                            "The drone owner cannot be attacked");
                }
                if (entity instanceof EntityPlayer && !com.drppp.drtech.DrtConfig.EnableDronePlayerAttack) {
                    return DroneExecutionResult.failure(DroneActionStatus.DENIED, "failed",
                            "Drone attacks against players are disabled");
                }
                if (entity instanceof EntityPlayer player) {
                    EntityPlayer ownerPlayer = getOwnerId() == null ? null
                            : world.getPlayerEntityByUUID(getOwnerId());
                    if (ownerPlayer != null && player.isOnSameTeam(ownerPlayer)) {
                        return DroneExecutionResult.failure(DroneActionStatus.DENIED, "failed",
                                "Players on the drone owner's team cannot be attacked");
                    }
                }
                if (!com.drppp.drtech.DrtConfig.EnableDroneCombat) return DroneExecutionResult.failure(
                        DroneActionStatus.DENIED, "failed", "Drone combat is disabled by the server");
                setVisualEntityTarget(entity);
                DroneExecutionResult movement = approach(entity.getPosition());
                if (movement.getState() != DroneActionState.SUCCESS) return movement;
                if (!hasEnergy(DroneEnergyCosts.ENTITY_INTERACTION)) return insufficientEnergy();
                int usedWeaponSlot = DroneWorldActions.attackEntity(EntityProgrammableDrone.this, weapons, entity,
                        weaponMode, alternateWeaponSlot);
                if (usedWeaponSlot >= 0) {
                    if ("ALTERNATE".equalsIgnoreCase(weaponMode)) alternateWeaponSlot = usedWeaponSlot == 0 ? 1 : 0;
                    consumeEnergy(DroneEnergyCosts.ENTITY_INTERACTION);
                    triggerAttackAnimation();
                    world.playSound(null, posX, posY, posZ, SoundEvents.ENTITY_PLAYER_ATTACK_STRONG,
                            SoundCategory.NEUTRAL, 0.7F, 1.1F);
                    ((WorldServer) world).spawnParticle(EnumParticleTypes.CRIT, entity.posX,
                            entity.posY + entity.height * 0.6D, entity.posZ, 6, 0.25D, 0.2D, 0.25D, 0.02D);
                    attackCooldownTicks = Math.max(4, Math.min(40, attackIntervalTicks));
                    if (!untilDefeated || !entity.isEntityAlive()) attackChaseTicks = 0;
                    if (!untilDefeated || !entity.isEntityAlive()) setVisualEntityTarget(null);
                    return untilDefeated && entity.isEntityAlive()
                            ? DroneExecutionResult.running(attackCooldownTicks)
                            : DroneExecutionResult.success();
                }
                setVisualEntityTarget(null);
                return DroneExecutionResult.failure(DroneActionStatus.DENIED, "failed",
                        "Entity attack was rejected by Forge or protection rules");
            }

            private boolean isInsidePatrolLeash(BlockPos position, DroneArea area, int margin) {
                BlockPos min = area.getMin();
                BlockPos max = area.getMax();
                return position.getX() >= min.getX() - margin && position.getX() <= max.getX() + margin
                        && position.getY() >= min.getY() - margin && position.getY() <= max.getY() + margin
                        && position.getZ() >= min.getZ() - margin && position.getZ() <= max.getZ() + margin;
            }

            @Nullable
            private EntityLivingBase expectedAttackTarget(BlockPos anchor, DroneEntityFilterSpec filter,
                    String expectedEntityUuid, boolean hostileOnly) {
                UUID expected;
                try {
                    expected = UUID.fromString(expectedEntityUuid);
                } catch (IllegalArgumentException ignored) {
                    return null;
                }
                Entity candidate = ((WorldServer) world).getEntityFromUuid(expected);
                if (!(candidate instanceof EntityLivingBase living) || !living.isEntityAlive()
                        || filter != null && !filter.matches(living)
                        || hostileOnly && !(living instanceof IMob)) return null;
                attackTargetUuid = expected;
                attackTargetAnchor = anchor;
                return living;
            }

            @Override
            public void clearAttackTarget() {
                attackTargetUuid = null;
                attackTargetAnchor = null;
            }

            private DroneExecutionResult interactEntity(BlockPos target, boolean useItem, DroneItemFilter filter,
                    DroneEntityFilterSpec entityFilter) {
                if (!DroneHardwareStats.hasUpgrade(upgrades, DroneUpgradeType.TOOL_ARM)) {
                    return DroneExecutionResult.failure(DroneActionStatus.DENIED, "failed",
                            "Entity tool arm module is not installed");
                }
                EntityLivingBase entity = lockedInteractionTarget(target, entityFilter);
                if (entity == null) {
                    setVisualEntityTarget(null);
                    return DroneExecutionResult.failure(DroneActionStatus.NOT_FOUND,
                            "failed", "No living entity exists near the target coordinate");
                }
                setVisualEntityTarget(entity);
                if (++interactionChaseTicks > ENTITY_CHASE_TICK_LIMIT || isBeyondEntityChaseLimit(entity, target)) {
                    interactionTargetUuid = null;
                    interactionTargetAnchor = null;
                    interactionChaseTicks = 0;
                    setVisualEntityTarget(null);
                    return entityChaseExceeded();
                }
                DroneExecutionResult movement = approach(entity.getPosition());
                if (movement.getState() != DroneActionState.SUCCESS) return movement;
                if (!hasEnergy(DroneEnergyCosts.ENTITY_INTERACTION)) return insufficientEnergy();
                DroneWorldActions.InteractionOutcome outcome = DroneWorldActions.interactEntity(
                        EntityProgrammableDrone.this, inventory, entity, useItem,
                        filter == null ? DroneItemFilter.ANY : filter);
                if (outcome == DroneWorldActions.InteractionOutcome.SUCCESS) {
                    consumeEnergy(DroneEnergyCosts.ENTITY_INTERACTION);
                    world.playSound(null, entity.posX, entity.posY, entity.posZ,
                            SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.NEUTRAL, 0.35F, 1.35F);
                    interactionChaseTicks = 0;
                    setVisualEntityTarget(null);
                    return DroneExecutionResult.success();
                }
                if (outcome == DroneWorldActions.InteractionOutcome.NO_ITEM) {
                    setVisualEntityTarget(null);
                    return DroneExecutionResult.failure(
                            DroneActionStatus.NO_RESOURCE, "failed", "Drone cargo contains no usable item");
                }
                setVisualEntityTarget(null);
                return DroneExecutionResult.failure(DroneActionStatus.DENIED, "failed",
                        "Entity interaction was rejected by the target or protection rules");
            }

            @Override
            public DroneExecutionResult followEntity(BlockPos target, double distance, DroneEntityFilterSpec filter) {
                if (!DroneHardwareStats.hasUpgrade(upgrades, DroneUpgradeType.ENTITY_SCANNER)) {
                    return DroneExecutionResult.failure(DroneActionStatus.DENIED, "failed",
                            "Entity scanner module is not installed");
                }
                if (distance < 1.0D || distance > 64.0D) return DroneExecutionResult.failure(
                        DroneActionStatus.INVALID_TARGET, "failed", "Follow distance must be 1..64");
                EntityLivingBase entity = lockedEntityTarget(target, true, filter);
                if (entity == null) {
                    setVisualEntityTarget(null);
                    return DroneExecutionResult.failure(DroneActionStatus.NOT_FOUND,
                            "failed", "No living entity exists near the target coordinate");
                }
                setVisualEntityTarget(entity);
                if (++followChaseTicks > ENTITY_CHASE_TICK_LIMIT || isBeyondEntityChaseLimit(entity, target)) {
                    clearLockedEntityTarget(true);
                    followChaseTicks = 0;
                    setVisualEntityTarget(null);
                    return entityChaseExceeded();
                }
                if (getDistanceSq(entity) <= distance * distance) {
                    followChaseTicks = 0;
                    setVisualEntityTarget(null);
                    return DroneExecutionResult.success();
                }
                return approach(entity.getPosition());
            }

            @Override
            public DroneExecutionResult moveAwayFromEntity(BlockPos target, double distance, DroneEntityFilterSpec filter) {
                if (!DroneHardwareStats.hasUpgrade(upgrades, DroneUpgradeType.ENTITY_SCANNER)) {
                    return DroneExecutionResult.failure(DroneActionStatus.DENIED, "failed",
                            "Entity scanner module is not installed");
                }
                if (distance < 1.0D || distance > 64.0D) return DroneExecutionResult.failure(
                        DroneActionStatus.INVALID_TARGET, "failed", "Retreat distance must be 1..64");
                EntityLivingBase entity = lockedEntityTarget(target, false, filter);
                if (entity == null) {
                    setVisualEntityTarget(null);
                    return DroneExecutionResult.failure(DroneActionStatus.NOT_FOUND,
                            "failed", "No living entity exists near the target coordinate");
                }
                setVisualEntityTarget(entity);
                if (++avoidChaseTicks > ENTITY_CHASE_TICK_LIMIT || isBeyondEntityChaseLimit(entity, target)) {
                    clearLockedEntityTarget(false);
                    avoidChaseTicks = 0;
                    setVisualEntityTarget(null);
                    return entityChaseExceeded();
                }
                if (getDistanceSq(entity) >= distance * distance) {
                    avoidChaseTicks = 0;
                    setVisualEntityTarget(null);
                    return DroneExecutionResult.success();
                }
                double dx = posX - entity.posX;
                double dz = posZ - entity.posZ;
                double length = Math.sqrt(dx * dx + dz * dz);
                if (length < 0.01D) { dx = 1.0D; dz = 0.0D; length = 1.0D; }
                BlockPos destination = new BlockPos(entity.posX + dx / length * distance,
                        posY, entity.posZ + dz / length * distance);
                return moveTo(destination);
            }

            @Override
            public DroneExecutionResult loadEntity(BlockPos target, DroneEntityFilterSpec filter) {
                if (!DroneHardwareStats.hasUpgrade(upgrades, DroneUpgradeType.ENTITY_CONTAINMENT)) {
                    return DroneExecutionResult.failure(DroneActionStatus.DENIED, "failed",
                            "Entity containment module is not installed");
                }
                if (loadedEntityData != null) return DroneExecutionResult.failure(DroneActionStatus.NO_SPACE,
                        "failed", "Drone already carries an entity");
                if (target == null || !world.isBlockLoaded(target)) return DroneExecutionResult.failure(
                        DroneActionStatus.UNLOADED, "failed", "Entity target is not loaded");
                UUID owner = getOwnerId();
                EntityLivingBase candidate = lockedLoadTarget(target, filter, owner);
                if (candidate == null) {
                    setVisualEntityTarget(null);
                    return DroneExecutionResult.failure(DroneActionStatus.NOT_FOUND, "failed",
                            "No transportable entity owned by the drone owner exists near the target");
                }
                if (++loadChaseTicks > ENTITY_CHASE_TICK_LIMIT || isBeyondEntityChaseLimit(candidate, target)) {
                    loadTargetUuid = null;
                    loadTargetAnchor = null;
                    loadChaseTicks = 0;
                    setVisualEntityTarget(null);
                    return entityChaseExceeded();
                }
                if (!isTransportOwnedEntity(candidate, owner)) {
                    return DroneExecutionResult.failure(DroneActionStatus.DENIED, "failed",
                            "Entity is not owned by the drone owner or cannot be transported");
                }
                setVisualEntityTarget(candidate);
                DroneExecutionResult movement = approach(candidate.getPosition());
                if (movement.getState() != DroneActionState.SUCCESS) return movement;
                NBTTagCompound serialized = new NBTTagCompound();
                candidate.writeToNBT(serialized);
                if (serialized.getString("id").isEmpty() || !isEntityPayloadWithinLimit(serialized)) {
                    return DroneExecutionResult.failure(DroneActionStatus.DENIED, "failed",
                            "Entity data is unsupported or too large");
                }
                DroneEntityTransportEvent event = new DroneEntityTransportEvent(
                        DroneEntityTransportEvent.Action.LOAD, EntityProgrammableDrone.this,
                        candidate, target);
                if (MinecraftForge.EVENT_BUS.post(event) || !candidate.isEntityAlive()) {
                    return DroneExecutionResult.failure(DroneActionStatus.DENIED, "failed",
                            "Entity loading was rejected by protection rules");
                }
                if (!hasEnergy(DroneEnergyCosts.ENTITY_INTERACTION)) return insufficientEnergy();
                loadedEntityData = serialized;
                loadedEntityUuid = candidate.getUniqueID();
                candidate.setDead();
                consumeEnergy(DroneEnergyCosts.ENTITY_INTERACTION);
                world.playSound(null, candidate.posX, candidate.posY, candidate.posZ,
                        SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.NEUTRAL, 0.65F, 0.8F);
                ((WorldServer) world).spawnParticle(EnumParticleTypes.PORTAL, candidate.posX,
                        candidate.posY + candidate.height * 0.5D, candidate.posZ,
                        20, 0.35D, 0.45D, 0.35D, 0.05D);
                loadTargetUuid = null;
                loadTargetAnchor = null;
                loadChaseTicks = 0;
                setVisualEntityTarget(null);
                return DroneExecutionResult.success();
            }

            @Override
            public DroneExecutionResult releaseEntity(BlockPos target) {
                if (!DroneHardwareStats.hasUpgrade(upgrades, DroneUpgradeType.ENTITY_CONTAINMENT)) {
                    return DroneExecutionResult.failure(DroneActionStatus.DENIED, "failed",
                            "Entity containment module is not installed");
                }
                if (loadedEntityData == null) return DroneExecutionResult.failure(DroneActionStatus.NO_RESOURCE,
                        "failed", "Drone is not carrying an entity");
                if (target == null || !world.isBlockLoaded(target)) return DroneExecutionResult.failure(
                        DroneActionStatus.UNLOADED, "failed", "Release target is not loaded");
                DroneExecutionResult movement = approach(target);
                if (movement.getState() != DroneActionState.SUCCESS) return movement;
                Entity restored = EntityList.createEntityFromNBT(loadedEntityData.copy(), world);
                if (!(restored instanceof EntityLivingBase)) return DroneExecutionResult.failure(
                        DroneActionStatus.DENIED, "failed", "Entity data could not be restored");
                UUID owner = getOwnerId();
                if (!isTransportOwnedEntity((EntityLivingBase) restored, owner)) {
                    return DroneExecutionResult.failure(DroneActionStatus.DENIED, "failed",
                            "Stored entity is not owned by the drone owner or cannot be transported");
                }
                EntityLivingBase living = (EntityLivingBase) restored;
                living.setPosition(target.getX() + 0.5D, target.getY(), target.getZ() + 0.5D);
                DroneEntityTransportEvent event = new DroneEntityTransportEvent(
                        DroneEntityTransportEvent.Action.RELEASE, EntityProgrammableDrone.this,
                        living, target);
                if (MinecraftForge.EVENT_BUS.post(event)) {
                    return DroneExecutionResult.failure(DroneActionStatus.DENIED, "failed",
                            "Entity release was rejected by protection rules");
                }
                living.setPosition(target.getX() + 0.5D, target.getY(), target.getZ() + 0.5D);
                AxisAlignedBB releaseBounds = living.getEntityBoundingBox();
                if (world.collidesWithAnyBlock(releaseBounds)
                        || !world.checkNoEntityCollision(releaseBounds, living)) {
                    return DroneExecutionResult.failure(DroneActionStatus.NO_SPACE, "failed",
                            "Release target is obstructed by blocks or entities");
                }
                if (!hasEnergy(DroneEnergyCosts.ENTITY_INTERACTION)) return insufficientEnergy();
                if (!world.spawnEntity(living)) return DroneExecutionResult.failure(DroneActionStatus.DENIED,
                        "failed", "World rejected the entity spawn");
                consumeEnergy(DroneEnergyCosts.ENTITY_INTERACTION);
                world.playSound(null, living.posX, living.posY, living.posZ,
                        SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.NEUTRAL, 0.65F, 1.2F);
                ((WorldServer) world).spawnParticle(EnumParticleTypes.PORTAL, living.posX,
                        living.posY + living.height * 0.5D, living.posZ,
                        20, 0.35D, 0.45D, 0.35D, 0.05D);
                loadedEntityData = null;
                loadedEntityUuid = null;
                return DroneExecutionResult.success();
            }

            @Override
            public DroneExecutionResult fishAt(BlockPos target) {
                if (!DroneHardwareStats.hasUpgrade(upgrades, DroneUpgradeType.FISHING)) {
                    return DroneExecutionResult.failure(DroneActionStatus.DENIED, "failed",
                            "Fishing module is not installed");
                }
                if (target == null || !world.isBlockLoaded(target)) {
                    return DroneExecutionResult.failure(DroneActionStatus.UNLOADED, "failed",
                            "Fishing target is not loaded");
                }
                if (world.getBlockState(target).getMaterial() != Material.WATER) {
                    return DroneExecutionResult.failure(DroneActionStatus.INVALID_TARGET, "failed",
                            "Fishing target must be a water block");
                }
                if (activeFishingHook != null && (activeFishingHook.isDead
                        || activeFishingTarget == null || !activeFishingTarget.equals(target))) {
                    activeFishingHook.setDead();
                    activeFishingHook = null;
                    activeFishingTarget = null;
                    dataManager.set(FISHING_HOOK_ID, -1);
                    setFishingVisualTarget(null);
                    dataManager.set(FISHING_CAST_TICKS, 0);
                }
                ItemStack rod = fishingRod.getStackInSlot(0);
                if (activeFishingHook == null) {
                    BlockPos hover = target.up(2);
                    if (!navigationWorld.isPassable(hover)) {
                        return DroneExecutionResult.failure(DroneActionStatus.UNREACHABLE, "failed",
                                "Fishing hover position two blocks above the water is obstructed");
                    }
                    // Move directly to a fixed Y offset. approach() would choose a side neighbour instead.
                    DroneExecutionResult movement = moveTo(hover);
                    if (movement.getState() != DroneActionState.SUCCESS) return movement;
                    rod = acquireFishingRod(target);
                    if (rod.isEmpty()) {
                        return DroneExecutionResult.failure(DroneActionStatus.NO_RESOURCE, "failed",
                                "No fishing rod exists in the rod slot, drone cargo, or a nearby container");
                    }
                    if (!hasEnergy(DroneEnergyCosts.ENTITY_INTERACTION)) return insufficientEnergy();
                    DroneWorldActions.cleanupFishingHooks(EntityProgrammableDrone.this);
                    activeFishingHook = DroneWorldActions.castFishingHook(
                            EntityProgrammableDrone.this, rod, target);
                    if (activeFishingHook == null) {
                        return DroneExecutionResult.failure(DroneActionStatus.DENIED, "failed",
                                "World rejected the fishing hook");
                    }
                    consumeEnergy(DroneEnergyCosts.ENTITY_INTERACTION);
                    activeFishingTarget = target.toImmutable();
                    dataManager.set(FISHING_HOOK_ID, activeFishingHook.getEntityId());
                    setFishingVisualTarget(activeFishingTarget);
                    dataManager.set(FISHING_CAST_TICKS, 12);
                    syncFishingRod();
                    return DroneExecutionResult.running();
                }
                if (rod.isEmpty() || !(rod.getItem() instanceof ItemFishingRod)) {
                    cancelFishingHook();
                    return DroneExecutionResult.failure(DroneActionStatus.NO_RESOURCE, "failed",
                            "The active fishing rod is no longer available");
                }
                if (!DroneWorldActions.maintainFishingHook(
                        EntityProgrammableDrone.this, rod, activeFishingHook)) {
                    activeFishingHook = null;
                    activeFishingTarget = null;
                    dataManager.set(FISHING_HOOK_ID, -1);
                    setFishingVisualTarget(null);
                    dataManager.set(FISHING_CAST_TICKS, 0);
                    return DroneExecutionResult.failure(DroneActionStatus.NOT_FOUND, "failed",
                            "Fishing hook was lost");
                }
                if (!DroneWorldActions.isFishingCatchReady(activeFishingHook)) {
                    return DroneExecutionResult.running();
                }
                if (isProtectedFishingCatch(activeFishingHook.caughtEntity)) {
                    activeFishingHook.setDead();
                    activeFishingHook = null;
                    activeFishingTarget = null;
                    dataManager.set(FISHING_HOOK_ID, -1);
                    setFishingVisualTarget(null);
                    dataManager.set(FISHING_CAST_TICKS, 0);
                    return DroneExecutionResult.failure(DroneActionStatus.DENIED, "failed",
                            "Fishing hooks cannot pull protected players or owned entities");
                }
                DroneWorldActions.FishingRetraction retraction = DroneWorldActions.retractFishingHook(
                        EntityProgrammableDrone.this, rod.copy(), activeFishingHook);
                activeFishingHook = null;
                activeFishingTarget = null;
                dataManager.set(FISHING_HOOK_ID, -1);
                setFishingVisualTarget(null);
                dataManager.set(FISHING_CAST_TICKS, 0);
                dataManager.set(FISHING_PULL_TICKS, 10);
                fishingRod.setStackInSlot(0, retraction.rod);
                syncFishingRod();
                return DroneExecutionResult.success(Math.max(1, storeFishingLoot(retraction.loot)));
            }

            @Override
            public DroneExecutionResult renameDrone(String name) {
                String checked = name == null ? "" : name.trim();
                if (checked.isEmpty() || checked.length() > 32) return DroneExecutionResult.failure(
                        DroneActionStatus.INVALID_TARGET, "failed", "Drone name must contain 1..32 characters");
                setCustomNameTag(checked.replace('\u00a7', '?'));
                setAlwaysRenderNameTag(true);
                return DroneExecutionResult.success();
            }

            @Override
            public DroneExecutionResult setStatusLabel(String label) {
                if (label == null || label.length() > 64) return DroneExecutionResult.failure(
                        DroneActionStatus.INVALID_TARGET, "failed", "Status label is limited to 64 characters");
                dataManager.set(STATUS_LABEL, sanitizeStatusLabel(label));
                return DroneExecutionResult.success();
            }

            @Override
            public DroneExecutionResult setRotorMode(String mode) {
                if (mode == null || !("ACTIVE".equalsIgnoreCase(mode) || "STANDBY".equalsIgnoreCase(mode))) {
                    return DroneExecutionResult.failure(DroneActionStatus.INVALID_TARGET, "failed",
                            "Rotor mode must be ACTIVE or STANDBY");
                }
                dataManager.set(ROTORS_ACTIVE, "ACTIVE".equalsIgnoreCase(mode));
                return DroneExecutionResult.success();
            }

            @Override
            public DroneExecutionResult setStatusLight(String mode) {
                if (mode == null) return DroneExecutionResult.failure(DroneActionStatus.INVALID_TARGET, "failed",
                        "Status light mode is required");
                int value;
                if ("AUTO".equalsIgnoreCase(mode)) value = 0;
                else if ("GREEN".equalsIgnoreCase(mode)) value = 1;
                else if ("YELLOW".equalsIgnoreCase(mode)) value = 2;
                else if ("RED".equalsIgnoreCase(mode)) value = 3;
                else if ("OFF".equalsIgnoreCase(mode)) value = 4;
                else return DroneExecutionResult.failure(DroneActionStatus.INVALID_TARGET, "failed",
                        "Status light mode must be AUTO, GREEN, YELLOW, RED or OFF");
                dataManager.set(STATUS_LIGHT_MODE, value);
                return DroneExecutionResult.success();
            }

            @Override
            public DroneExecutionResult editSign(BlockPos target, String[] lines) {
                if (!DroneHardwareStats.hasUpgrade(upgrades, DroneUpgradeType.TOOL_ARM)) {
                    return DroneExecutionResult.failure(DroneActionStatus.DENIED, "failed",
                            "Entity tool arm module is not installed");
                }
                if (target == null || !world.isBlockLoaded(target)) return DroneExecutionResult.failure(
                        DroneActionStatus.UNLOADED, "failed", "Sign target is not loaded");
                TileEntity tile = world.getTileEntity(target);
                if (!(tile instanceof TileEntitySign sign)) return DroneExecutionResult.failure(
                        DroneActionStatus.INVALID_TARGET, "failed", "Target is not a sign");
                if (lines == null || lines.length != 4) return DroneExecutionResult.failure(
                        DroneActionStatus.INVALID_TARGET, "failed", "A sign requires exactly four lines");
                String[] checked = new String[4];
                for (int index = 0; index < checked.length; index++) {
                    if (lines[index] != null && lines[index].length() > 64) return DroneExecutionResult.failure(
                            DroneActionStatus.INVALID_TARGET, "failed", "Each sign line is limited to 64 characters");
                    checked[index] = sanitizeStatusLabel(lines[index]);
                }
                DroneExecutionResult movement = approach(target);
                if (movement.getState() != DroneActionState.SUCCESS) return movement;
                if (!DroneWorldActions.authorizeSignEdit(EntityProgrammableDrone.this, target)) {
                    return DroneExecutionResult.failure(DroneActionStatus.DENIED, "failed",
                            "Sign editing was rejected by Forge or protection rules");
                }
                String[] previous = new String[4];
                for (int index = 0; index < previous.length; index++) {
                    previous[index] = sign.signText[index] == null
                            ? "" : sign.signText[index].getUnformattedText();
                }
                if (MinecraftForge.EVENT_BUS.post(new DroneSignEditEvent(
                        EntityProgrammableDrone.this, target, previous, checked))) {
                    return DroneExecutionResult.failure(DroneActionStatus.DENIED, "failed",
                            "Sign editing was canceled by protection rules");
                }
                if (world.getTileEntity(target) != sign) {
                    return DroneExecutionResult.failure(DroneActionStatus.INVALID_TARGET, "failed",
                            "Sign target changed while editing");
                }
                if (!consumeEnergy(DroneEnergyCosts.ENTITY_INTERACTION)) return insufficientEnergy();
                for (int index = 0; index < checked.length; index++) {
                    sign.signText[index] = new TextComponentString(checked[index]);
                }
                sign.markDirty();
                net.minecraft.block.state.IBlockState state = world.getBlockState(target);
                world.notifyBlockUpdate(target, state, state, 3);
                return DroneExecutionResult.success();
            }

            private EntityLivingBase nearestEntityAt(BlockPos target) {
                return nearestEntityAt(target, null);
            }

            private EntityLivingBase nearestEntityAt(BlockPos target, DroneEntityFilterSpec filter) {
                if (target == null || !world.isBlockLoaded(target)) return null;
                AxisAlignedBB bounds = new AxisAlignedBB(target).grow(1.5D);
                EntityLivingBase nearest = null;
                double best = Double.MAX_VALUE;
                for (EntityLivingBase entity : world.getEntitiesWithinAABB(EntityLivingBase.class, bounds)) {
                    if (entity == EntityProgrammableDrone.this || !entity.isEntityAlive()
                            || filter != null && !filter.matches(entity)) continue;
                    double distance = entity.getDistanceSqToCenter(target);
                    if (nearest == null || distance < best
                            || distance == best && entity.getUniqueID().compareTo(nearest.getUniqueID()) < 0) {
                        nearest = entity; best = distance;
                    }
                }
                return nearest;
            }

            private EntityLivingBase lockedEntityTarget(BlockPos anchor, boolean following) {
                return lockedEntityTarget(anchor, following, DroneEntityFilterSpec.any());
            }

            private EntityLivingBase lockedEntityTarget(BlockPos anchor, boolean following,
                    DroneEntityFilterSpec filter) {
                UUID lockedId = following ? followTargetUuid : avoidTargetUuid;
                BlockPos lockedAnchor = following ? followTargetAnchor : avoidTargetAnchor;
                if (lockedId != null && anchor != null && anchor.equals(lockedAnchor)) {
                    Entity locked = ((WorldServer) world).getEntityFromUuid(lockedId);
                    if (locked instanceof EntityLivingBase living && living.isEntityAlive()
                            && (filter == null || filter.matches(living))) return living;
                }
                clearLockedEntityTarget(following);
                EntityLivingBase selected = nearestEntityAt(anchor,
                        filter);
                if (selected != null) {
                    if (following) {
                        followChaseTicks = 0;
                        followTargetUuid = selected.getUniqueID();
                        followTargetAnchor = anchor;
                    } else {
                        avoidChaseTicks = 0;
                        avoidTargetUuid = selected.getUniqueID();
                        avoidTargetAnchor = anchor;
                    }
                }
                return selected;
            }

            private EntityLivingBase lockedAttackTarget(BlockPos anchor, DroneEntityFilterSpec filter) {
                if (attackTargetUuid != null && anchor != null && anchor.equals(attackTargetAnchor)) {
                    Entity locked = ((WorldServer) world).getEntityFromUuid(attackTargetUuid);
                    if (locked instanceof EntityLivingBase living && living.isEntityAlive()
                            && (filter == null || filter.matches(living))) return living;
                }
                attackTargetUuid = null;
                attackTargetAnchor = null;
                EntityLivingBase selected = nearestEntityAt(anchor, filter);
                if (selected != null) {
                    attackChaseTicks = 0;
                    attackTargetUuid = selected.getUniqueID();
                    attackTargetAnchor = anchor;
                }
                return selected;
            }

            private EntityLivingBase lockedInteractionTarget(BlockPos anchor, DroneEntityFilterSpec filter) {
                if (interactionTargetUuid != null && anchor != null && anchor.equals(interactionTargetAnchor)) {
                    Entity locked = ((WorldServer) world).getEntityFromUuid(interactionTargetUuid);
                    if (locked instanceof EntityLivingBase living && living.isEntityAlive()
                            && (filter == null || filter.matches(living))) return living;
                }
                interactionTargetUuid = null;
                interactionTargetAnchor = null;
                EntityLivingBase selected = nearestEntityAt(anchor, filter);
                if (selected != null) {
                    interactionChaseTicks = 0;
                    interactionTargetUuid = selected.getUniqueID();
                    interactionTargetAnchor = anchor.toImmutable();
                }
                return selected;
            }

            private EntityLivingBase lockedLoadTarget(BlockPos anchor, DroneEntityFilterSpec filter, UUID owner) {
                if (loadTargetUuid != null && anchor != null && anchor.equals(loadTargetAnchor)) {
                    Entity locked = ((WorldServer) world).getEntityFromUuid(loadTargetUuid);
                    if (locked instanceof EntityLivingBase living && living.isEntityAlive()
                            && (filter == null || filter.matches(living))
                            && isTransportOwnedEntity(living, owner)) return living;
                }
                loadTargetUuid = null;
                loadTargetAnchor = null;
                EntityLivingBase selected = nearestTransportEntityAt(anchor, filter, owner);
                if (selected != null) {
                    loadChaseTicks = 0;
                    loadTargetUuid = selected.getUniqueID();
                    loadTargetAnchor = anchor.toImmutable();
                }
                return selected;
            }

            private EntityLivingBase nearestTransportEntityAt(BlockPos anchor,
                    DroneEntityFilterSpec filter, UUID owner) {
                if (anchor == null || owner == null || !world.isBlockLoaded(anchor)) return null;
                EntityLivingBase nearest = null;
                double best = Double.MAX_VALUE;
                for (EntityLivingBase entity : world.getEntitiesWithinAABB(EntityLivingBase.class,
                        new AxisAlignedBB(anchor).grow(1.5D))) {
                    if (entity == EntityProgrammableDrone.this || !entity.isEntityAlive()
                            || filter != null && !filter.matches(entity)
                            || !isTransportOwnedEntity(entity, owner)) continue;
                    double distance = entity.getDistanceSqToCenter(anchor);
                    if (nearest == null || distance < best
                            || distance == best && entity.getUniqueID().compareTo(nearest.getUniqueID()) < 0) {
                        nearest = entity;
                        best = distance;
                    }
                }
                return nearest;
            }

            private void clearLockedEntityTarget(boolean following) {
                if (following) {
                    followTargetUuid = null;
                    followTargetAnchor = null;
                } else {
                    avoidTargetUuid = null;
                    avoidTargetAnchor = null;
                }
            }

            private boolean isDroneOwner(UUID candidate) {
                UUID owner = getOwnerId();
                return owner != null && owner.equals(candidate);
            }

            private boolean isProtectedFishingCatch(@Nullable Entity caught) {
                if (caught == null) return false;
                if (caught instanceof EntityPlayer) return true;
                return caught instanceof IEntityOwnable && ((IEntityOwnable) caught).getOwnerId() != null;
            }

            private boolean isBeyondEntityChaseLimit(EntityLivingBase entity, BlockPos anchor) {
                return entity == null || anchor == null
                        || entity.getDistanceSqToCenter(anchor) > ENTITY_CHASE_DISTANCE_LIMIT * ENTITY_CHASE_DISTANCE_LIMIT;
            }

            private DroneExecutionResult entityChaseExceeded() {
                return DroneExecutionResult.failure(DroneActionStatus.OUT_OF_RANGE, "failed",
                        "Entity chase exceeded 1200 ticks or 128 blocks from the target anchor");
            }

            private boolean isTransportOwnedEntity(EntityLivingBase entity, UUID owner) {
                if (entity == null || owner == null || !(entity instanceof IEntityOwnable)) return false;
                if (entity instanceof EntityTameable && !((EntityTameable) entity).isTamed()) return false;
                UUID entityOwner = ((IEntityOwnable) entity).getOwnerId();
                return entityOwner != null && owner.equals(entityOwner);
            }

            @Override
            public int countEntities(DroneArea area, DroneEntityFilterSpec filter, int limit) {
                if (!DroneHardwareStats.hasUpgrade(upgrades, DroneUpgradeType.ENTITY_SCANNER)) return 0;
                if (area == null || !area.isWithinRuntimeLimits()) return 0;
                DroneEntityFilterSpec checked = filter == null ? DroneEntityFilterSpec.any() : filter;
                int boundedLimit = Math.max(1, Math.min(256, limit));
                int count = 0;
                for (EntityLivingBase entity : entitiesInArea(area)) {
                    if (entity != EntityProgrammableDrone.this && checked.matches(entity) && ++count >= boundedLimit) break;
                }
                return count;
            }

            @Override
            public DroneEntitySensorResult senseNearestEntity(DroneArea area, DroneEntityFilterSpec filter) {
                return senseEntity(area, filter, DroneSensorService.EntityPriority.NEAREST);
            }

            @Override
            public DroneEntitySensorResult senseEntity(DroneArea area, DroneEntityFilterSpec filter,
                    DroneSensorService.EntityPriority priority) {
                return senseEntity(area, filter, priority, false);
            }

            @Override
            public DroneEntitySensorResult senseEntity(DroneArea area, DroneEntityFilterSpec filter,
                    DroneSensorService.EntityPriority priority, boolean hostileOnly) {
                return senseEntityInternal(area, filter, priority, hostileOnly, false);
            }

            @Override
            public DroneEntitySensorResult senseAttackableEntity(DroneArea area, DroneEntityFilterSpec filter,
                    DroneSensorService.EntityPriority priority, boolean hostileOnly) {
                return senseEntityInternal(area, filter, priority, hostileOnly, true);
            }

            private DroneEntitySensorResult senseEntityInternal(DroneArea area, DroneEntityFilterSpec filter,
                    DroneSensorService.EntityPriority priority, boolean hostileOnly, boolean attackableOnly) {
                if (!DroneHardwareStats.hasUpgrade(upgrades, DroneUpgradeType.ENTITY_SCANNER)) {
                    return DroneEntitySensorResult.EMPTY;
                }
                if (area == null || !area.isWithinRuntimeLimits()) return DroneEntitySensorResult.EMPTY;
                DroneEntityFilterSpec checked = filter == null ? DroneEntityFilterSpec.any() : filter;
                EntityLivingBase selected = null;
                int count = 0;
                for (EntityLivingBase entity : entitiesInArea(area)) {
                    if (entity == EntityProgrammableDrone.this || !checked.matches(entity)
                            || hostileOnly && !(entity instanceof IMob)
                            || attackableOnly && !isAttackablePatrolCandidate(entity)) continue;
                    if (count < 256) count++;
                    if (selected == null || isPreferredEntity(entity, selected, priority)) selected = entity;
                }
                if (selected == null) return DroneEntitySensorResult.EMPTY;
                UUID owner = getOwnerId();
                boolean owned = owner != null && selected instanceof IEntityOwnable
                        && owner.equals(((IEntityOwnable) selected).getOwnerId());
                ResourceLocation entityId = EntityList.getKey(selected);
                return new DroneEntitySensorResult(count, selected.getPosition(), selected.getHealth(),
                        selected.getMaxHealth(), selected.getName(), entityId == null ? "" : entityId.toString(),
                        selected.getUniqueID().toString(), owner != null && owner.equals(selected.getUniqueID()),
                        owned, selected instanceof IMob, droneDamageRatio());
            }

            private boolean isAttackablePatrolCandidate(EntityLivingBase entity) {
                if (!entity.isNonBoss()) return false;
                if (entity instanceof IEntityOwnable ownable && ownable.getOwnerId() != null
                        && !isDroneOwner(ownable.getOwnerId())) return false;
                if (entity instanceof EntityPlayer player && isDroneOwner(player.getUniqueID())) return false;
                return !(entity instanceof EntityPlayer) || com.drppp.drtech.DrtConfig.EnableDronePlayerAttack;
            }

            private boolean isPreferredEntity(EntityLivingBase candidate, EntityLivingBase current,
                    DroneSensorService.EntityPriority priority) {
                DroneSensorService.EntityPriority checked = priority == null
                        ? DroneSensorService.EntityPriority.NEAREST : priority;
                int comparison = 0;
                if (checked == DroneSensorService.EntityPriority.LOWEST_HEALTH) {
                    comparison = Float.compare(candidate.getHealth(), current.getHealth());
                } else if (checked == DroneSensorService.EntityPriority.HIGHEST_HEALTH) {
                    comparison = Float.compare(current.getHealth(), candidate.getHealth());
                } else if (checked == DroneSensorService.EntityPriority.HOSTILE_FIRST) {
                    comparison = Boolean.compare(current instanceof IMob, candidate instanceof IMob);
                }
                if (comparison != 0) return comparison < 0;
                comparison = Double.compare(candidate.getDistanceSq(EntityProgrammableDrone.this),
                        current.getDistanceSq(EntityProgrammableDrone.this));
                return comparison < 0 || comparison == 0
                        && candidate.getUniqueID().compareTo(current.getUniqueID()) < 0;
            }

            @Override
            public DroneEntitySensorResult senseDroneDamage() {
                return new DroneEntitySensorResult(0, EntityProgrammableDrone.this.getPosition(), getHealth(),
                        getMaxHealth(), false, false, droneDamageRatio());
            }

            private List<EntityLivingBase> entitiesInArea(DroneArea area) {
                BlockPos min = area.getMin();
                BlockPos max = area.getMax();
                AxisAlignedBB bounds = new AxisAlignedBB(min.getX(), min.getY(), min.getZ(),
                        max.getX() + 1.0D, max.getY() + 1.0D, max.getZ() + 1.0D);
                List<EntityLivingBase> entities = world.getEntitiesWithinAABB(EntityLivingBase.class, bounds,
                        entity -> entity != null && entity.isEntityAlive() && area.contains(entity.getPosition()));
                entities.sort(Comparator.comparing(Entity::getUniqueID));
                return entities;
            }

            private float droneDamageRatio() {
                float maximum = EntityProgrammableDrone.this.getMaxHealth();
                return maximum <= 0F ? 0F : Math.max(0F, Math.min(1F,
                        1F - EntityProgrammableDrone.this.getHealth() / maximum));
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
                        if (importing && selection.status == DroneActionStatus.NO_SPACE) {
                            return handleItemCargoFull(selection.message);
                        }
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
                if (possible <= 0) return importing
                        ? handleItemCargoFull("Drone cargo has no space for matching items")
                        : DroneExecutionResult.failure(DroneActionStatus.NO_SPACE, "failed",
                                "Destination has no space for matching items");
                if (!consumeEnergy(DroneEnergyCosts.ENTITY_INTERACTION)) return insufficientEnergy();
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
                return stand == null ? DroneExecutionResult.failure(DroneActionStatus.UNREACHABLE, "failed",
                        "No passable interaction position") : moveTo(stand);
            }

            private DroneExecutionResult insufficientEnergy() {
                return DroneExecutionResult.failure(DroneActionStatus.NO_ENERGY, "failed",
                        "Insufficient drone energy");
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
        if (autoPickupMode == DroneAutoPickupMode.OFF || autoPickupMode == DroneAutoPickupMode.PROGRAM_ONLY) return;
        if (cargoFullPickupSuspended) {
            if (!hasAnyCargoSpace()) return;
            cargoFullPickupSuspended = false;
        }
        DroneItemFilter filter = autoPickupMode == DroneAutoPickupMode.FILTER_MATCHING
                ? DroneItemFilter.fromSpec(autoPickupFilter) : DroneItemFilter.ANY;
        if (!hasMatchingDroppedItem(1.25D, filter)) return;
        int moved = collectDroppedItems(1.25D, filter, Integer.MAX_VALUE, false);
        if (moved <= 0) handleAutomaticCargoFull();
    }

    private void handleAutomaticCargoFull() {
        switch (cargoFullPolicy) {
            case DROP_ONE_STACK -> dropOneCargoStack();
            case RETURN_TO_DOCK -> {
                cargoFullPickupSuspended = true;
                requestManualRecall();
            }
            default -> cargoFullPickupSuspended = true;
        }
    }

    private DroneExecutionResult handleItemCargoFull(String message) {
        if (cargoFullPolicy == DroneCargoFullPolicy.DROP_ONE_STACK && dropOneCargoStack()) {
            return DroneExecutionResult.running();
        }
        if (cargoFullPolicy == DroneCargoFullPolicy.RETURN_TO_DOCK) requestManualRecall();
        return DroneExecutionResult.failure(DroneActionStatus.NO_SPACE, "failed", message);
    }

    private boolean hasAnyCargoSpace() {
        for (int slot = 0; slot < getActiveCargoSlots(); slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (stack.isEmpty() || stack.getCount() < Math.min(stack.getMaxStackSize(), inventory.getSlotLimit(slot))) {
                return true;
            }
        }
        return false;
    }

    private boolean dropOneCargoStack() {
        for (int slot = getActiveCargoSlots() - 1; slot >= 0; slot--) {
            ItemStack preview = inventory.extractItem(slot, Integer.MAX_VALUE, true);
            if (preview.isEmpty()) continue;
            ItemStack extracted = inventory.extractItem(slot, preview.getCount(), false);
            if (extracted.isEmpty()) continue;
            EntityItem dropped = entityDropItem(extracted, -0.2F);
            if (dropped != null) {
                dropped.setPickupDelay(100);
                return true;
            }
            insertCargo(extracted, false);
            return false;
        }
        return false;
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

    /** Moves only entities created by the current hook retraction; pre-existing nearby drops are never consumed. */
    private int storeFishingLoot(List<EntityItem> caughtItems) {
        int caught = 0;
        if (caughtItems == null) return caught;
        for (EntityItem entityItem : caughtItems) {
            if (entityItem == null || entityItem.isDead || entityItem.getItem().isEmpty()) continue;
            ItemStack original = entityItem.getItem();
            caught += original.getCount();
            ItemStack remainder = original.copy();
            for (int slot = 0; slot < getActiveCargoSlots() && !remainder.isEmpty(); slot++) {
                remainder = inventory.insertItem(slot, remainder, false);
            }
            if (remainder.isEmpty()) entityItem.setDead();
            else entityItem.setItem(remainder);
        }
        return caught;
    }

    /** Loads one rod from the dedicated slot, cargo, or a container within three blocks of the water target. */
    private ItemStack acquireFishingRod(BlockPos waterTarget) {
        ItemStack current = fishingRod.getStackInSlot(0);
        if (!current.isEmpty() && current.getItem() instanceof ItemFishingRod) return current;
        if (!current.isEmpty()) return ItemStack.EMPTY;
        if (moveFishingRodFrom(inventory)) return fishingRod.getStackInSlot(0);
        if (waterTarget == null) return ItemStack.EMPTY;
        java.util.Set<IItemHandler> visited = java.util.Collections.newSetFromMap(
                new java.util.IdentityHashMap<>());
        BlockPos min = waterTarget.add(-3, -1, -3);
        BlockPos max = waterTarget.add(3, 3, 3);
        for (BlockPos.MutableBlockPos mutable : BlockPos.getAllInBoxMutable(min, max)) {
            if (!world.isBlockLoaded(mutable)) continue;
            TileEntity tile = world.getTileEntity(mutable);
            if (tile == null) continue;
            IItemHandler unsided = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
            if (unsided != null && visited.add(unsided) && moveFishingRodFrom(unsided)) {
                return fishingRod.getStackInSlot(0);
            }
            for (EnumFacing side : EnumFacing.values()) {
                IItemHandler handler = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side);
                if (handler != null && visited.add(handler) && moveFishingRodFrom(handler)) {
                    return fishingRod.getStackInSlot(0);
                }
            }
        }
        return ItemStack.EMPTY;
    }

    private boolean moveFishingRodFrom(IItemHandler source) {
        if (source == null || !fishingRod.getStackInSlot(0).isEmpty()) return false;
        for (int slot = 0; slot < source.getSlots(); slot++) {
            ItemStack simulated = source.extractItem(slot, 1, true);
            if (simulated.isEmpty() || !(simulated.getItem() instanceof ItemFishingRod)
                    || !fishingRod.insertItem(0, simulated, true).isEmpty()) continue;
            ItemStack extracted = source.extractItem(slot, 1, false);
            if (extracted.isEmpty()) continue;
            ItemStack remainder = fishingRod.insertItem(0, extracted, false);
            if (remainder.isEmpty()) {
                syncFishingRod();
                return true;
            }
            source.insertItem(slot, remainder, false);
        }
        return false;
    }

    private static boolean isEntityPayloadWithinLimit(@Nullable NBTTagCompound payload) {
        int size = entityPayloadSize(payload);
        return size >= 0 && size <= 32768;
    }

    private static int entityPayloadSize(@Nullable NBTTagCompound payload) {
        if (payload == null) return -1;
        try (ByteArrayOutputStream bytes = new ByteArrayOutputStream();
             DataOutputStream output = new DataOutputStream(bytes)) {
            CompressedStreamTools.write(payload, output);
            output.flush();
            return bytes.size();
        } catch (IOException | RuntimeException ignored) {
            return -1;
        }
    }

    private String getLoadedEntitySummary() {
        if (loadedEntityData == null) return "";
        String id = loadedEntityData.getString("id");
        String customName = loadedEntityData.getString("CustomName");
        String identity = loadedEntityUuid == null ? "" : loadedEntityUuid.toString().substring(0, 8);
        int bytes = entityPayloadSize(loadedEntityData);
        return (customName.isEmpty() ? id : customName + " (" + id + ")")
                + (identity.isEmpty() ? "" : " #" + identity)
                + (bytes < 0 ? "" : " " + bytes + "B");
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
