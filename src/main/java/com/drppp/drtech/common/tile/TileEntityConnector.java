package com.drppp.drtech.common.tile;

import com.drppp.drtech.common.items.metaItems.DrMetaItems;
import gregtech.api.GTValues;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IEnergyContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class TileEntityConnector extends TileEntity implements ITickable {

    // ---------------------------------------------------------------------
    // Constants
    // ---------------------------------------------------------------------

    private static final int MIN_TIER = 1;
    private static final int MAX_TIER = 3;

    private static final int[] MAX_WIRE_LENGTH = {0, 32, 48, 64};
    private static final int[] WIRE_COLOR = {0, 0xB87333, 0xD7B35A, 0xC9C9C9};
    private static final int[] GT_TIER_BY_TIER = {GTValues.LV, GTValues.LV, GTValues.MV, GTValues.HV};

    private static final long MAX_TRANSFER_AMPERAGE = 10;
    private static final int VALIDATE_INTERVAL = 20;

    private static final String NBT_CONNECTOR_TIER = "ConnectorTier";
    private static final String NBT_MAX_ENERGY = "MaxEnergy";
    private static final String NBT_STORED_ENERGY = "StoredEnergy";
    private static final String NBT_SUCCESS = "Success";
    private static final String NBT_SELF_POS = "selfPos";
    private static final String NBT_NEXT_POS = "nextPos";
    private static final String NBT_BEFORE_POS = "beforePos";
    private static final String NBT_CONNECTIONS = "Connections";
    private static final String NBT_TARGET = "Target";
    private static final String NBT_WIRE_TIER = "WireTier";
    private static final String NBT_LENGTH = "Length";
    private static final String NBT_TARGET_SIDE = "TargetSide";
    private static final String NBT_POS_X = "xx";
    private static final String NBT_POS_Y = "yy";
    private static final String NBT_POS_Z = "zz";

    // ---------------------------------------------------------------------
    // State
    // ---------------------------------------------------------------------

    private final List<WireConnection> connections = new ArrayList<>();
    private int connectorTier = 1;
    private int tick = 0;

    public BlockPos selfPos;
    public BlockPos nextPos;
    public BlockPos beforePos;
    public long MaxEnergy = getCapacityForTier(1);
    public long StoredEnergy = 0;
    public int success = 0;

    public TileEntityConnector() {
    }

    public TileEntityConnector(int tier) {
        this.connectorTier = clampTier(tier);
        this.MaxEnergy = getCapacityForTier(this.connectorTier);
    }

    // ---------------------------------------------------------------------
    // Tier utilities
    // ---------------------------------------------------------------------

    public static int getMaxWireLength(int tier) {
        return MAX_WIRE_LENGTH[clampTier(tier)];
    }

    public static int getWireColor(int tier) {
        return WIRE_COLOR[clampTier(tier)];
    }

    public static long getCapacityForTier(int tier) {
        // 4^(4 + 2 * tier) == 2^(8 + 4 * tier)
        return 1L << (8 + 4 * clampTier(tier));
    }

    public int getConnectorTier() {
        this.connectorTier = clampTier(this.connectorTier);
        return this.connectorTier;
    }

    public boolean canConnectWire(int wireTier) {
        return getConnectorTier() == clampTier(wireTier);
    }

    private int getGtTier() {
        return GT_TIER_BY_TIER[getConnectorTier()];
    }

    // ---------------------------------------------------------------------
    // Static connection API
    // ---------------------------------------------------------------------

    public static boolean connect(WorldAccess worldAccess, BlockPos firstPos, BlockPos secondPos, int wireTier) {
        TileEntity first = worldAccess.getTileEntity(firstPos);
        TileEntity second = worldAccess.getTileEntity(secondPos);
        if (!(first instanceof TileEntityConnector) || !(second instanceof TileEntityConnector)) {
            return false;
        }

        TileEntityConnector firstConnector = (TileEntityConnector) first;
        TileEntityConnector secondConnector = (TileEntityConnector) second;
        int tier = clampTier(wireTier);
        int length = (int) Math.ceil(Math.sqrt(firstPos.distanceSq(secondPos)));

        if (!firstConnector.canConnectWire(tier) || !secondConnector.canConnectWire(tier)) {
            return false;
        }
        if (length > getMaxWireLength(tier)) {
            return false;
        }
        if (firstConnector.hasConnection(secondPos) || secondConnector.hasConnection(firstPos)) {
            return false;
        }

        boolean firstAdded = firstConnector.addConnection(secondPos, tier, length);
        boolean secondAdded = secondConnector.addConnection(firstPos, tier, length);
        if (!firstAdded || !secondAdded) {
            firstConnector.removeConnection(secondPos);
            secondConnector.removeConnection(firstPos);
            return false;
        }
        return true;
    }

    public static boolean connect(net.minecraft.world.World world, BlockPos firstPos, BlockPos secondPos, int wireTier) {
        return connect(world::getTileEntity, firstPos, secondPos, wireTier);
    }

    public static ItemStack getWireStack(int tier) {
        return switch (clampTier(tier)) {
            case 1 ->
                    DrMetaItems.LOW_VOLTAGE_WIRE == null ? ItemStack.EMPTY : DrMetaItems.LOW_VOLTAGE_WIRE.getStackForm();
            case 2 ->
                    DrMetaItems.MEDIUM_VOLTAGE_WIRE == null ? ItemStack.EMPTY : DrMetaItems.MEDIUM_VOLTAGE_WIRE.getStackForm();
            case 3 ->
                    DrMetaItems.HIGH_VOLTAGE_WIRE == null ? ItemStack.EMPTY : DrMetaItems.HIGH_VOLTAGE_WIRE.getStackForm();
            default -> ItemStack.EMPTY;
        };
    }

    // ---------------------------------------------------------------------
    // Connection management
    // ---------------------------------------------------------------------

    public boolean hasConnection(BlockPos target) {
        for (WireConnection connection : connections) {
            if (connection.target.equals(target)) {
                return true;
            }
        }
        return false;
    }

    public List<WireConnection> getConnections() {
        return Collections.unmodifiableList(connections);
    }

    public boolean addConnection(BlockPos target, int wireTier, int length) {
        return addMachineConnection(target, wireTier, length, null);
    }

    public boolean addMachineConnection(BlockPos target, int wireTier, int length, @Nullable EnumFacing targetSide) {
        if (!canAddConnection(target, wireTier, length)) {
            return false;
        }
        connections.add(new WireConnection(target, wireTier, length, targetSide));
        updateLegacyFields();
        markDirtyAndSync();
        return true;
    }

    private boolean canAddConnection(BlockPos target, int wireTier, int length) {
        if (target == null || target.equals(pos) || hasConnection(target) || !canConnectWire(wireTier)) {
            return false;
        }
        return length <= getMaxWireLength(clampTier(wireTier));
    }

    public void removeConnection(BlockPos target) {
        Iterator<WireConnection> iterator = connections.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().target.equals(target)) {
                iterator.remove();
                updateLegacyFields();
                markDirtyAndSync();
                return;
            }
        }
    }

    public void removeAllConnections(boolean dropItems) {
        if (world == null) {
            connections.clear();
            updateLegacyFields();
            return;
        }

        for (WireConnection connection : new ArrayList<>(connections)) {
            if (world.isBlockLoaded(connection.target)) {
                TileEntity tileEntity = world.getTileEntity(connection.target);
                if (tileEntity instanceof TileEntityConnector) {
                    ((TileEntityConnector) tileEntity).removeConnection(pos);
                }
            }
            if (dropItems && !world.isRemote) {
                ItemStack wireStack = getWireStack(connection.wireTier);
                if (!wireStack.isEmpty()) {
                    InventoryHelper.spawnItemStack(world,
                            pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, wireStack);
                }
            }
        }

        connections.clear();
        updateLegacyFields();
        markDirtyAndSync();
    }

    // ---------------------------------------------------------------------
    // NBT
    // ---------------------------------------------------------------------

    @Override
    public void readFromNBT(@NotNull NBTTagCompound compound) {
        super.readFromNBT(compound);

        long savedMaxEnergy = compound.getLong(NBT_MAX_ENERGY);
        this.connectorTier = compound.hasKey(NBT_CONNECTOR_TIER)
                ? clampTier(compound.getInteger(NBT_CONNECTOR_TIER))
                : inferTier(savedMaxEnergy);
        this.MaxEnergy = savedMaxEnergy > 0 ? savedMaxEnergy : getCapacityForTier(this.connectorTier);
        this.StoredEnergy = clampEnergy(compound.getLong(NBT_STORED_ENERGY), 0, this.MaxEnergy);
        this.success = compound.getInteger(NBT_SUCCESS);

        this.selfPos = readBlockPos(compound, NBT_SELF_POS);
        this.nextPos = readBlockPos(compound, NBT_NEXT_POS);
        this.beforePos = readBlockPos(compound, NBT_BEFORE_POS);

        this.connections.clear();
        NBTTagList connectionList = compound.getTagList(NBT_CONNECTIONS, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < connectionList.tagCount(); i++) {
            NBTTagCompound connectionTag = connectionList.getCompoundTagAt(i);
            BlockPos target = readBlockPos(connectionTag, NBT_TARGET);
            if (target == null || target.equals(pos) || hasConnection(target)) {
                continue;
            }
            int wireTier = clampTier(connectionTag.getInteger(NBT_WIRE_TIER));
            int length = connectionTag.getInteger(NBT_LENGTH);
            EnumFacing targetSide = connectionTag.hasKey(NBT_TARGET_SIDE)
                    ? EnumFacing.byIndex(connectionTag.getInteger(NBT_TARGET_SIDE))
                    : null;
            this.connections.add(new WireConnection(target, wireTier, length, targetSide));
        }
        updateLegacyFields();
    }

    @Override
    public @NotNull NBTTagCompound writeToNBT(@NotNull NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setInteger(NBT_CONNECTOR_TIER, getConnectorTier());
        compound.setLong(NBT_MAX_ENERGY, MaxEnergy);
        compound.setLong(NBT_STORED_ENERGY, StoredEnergy);
        compound.setInteger(NBT_SUCCESS, success);

        writeBlockPos(compound, NBT_SELF_POS, selfPos);
        writeBlockPos(compound, NBT_NEXT_POS, nextPos);
        writeBlockPos(compound, NBT_BEFORE_POS, beforePos);

        NBTTagList connectionList = new NBTTagList();
        for (WireConnection connection : connections) {
            NBTTagCompound connectionTag = new NBTTagCompound();
            writeBlockPos(connectionTag, NBT_TARGET, connection.target);
            connectionTag.setInteger(NBT_WIRE_TIER, connection.wireTier);
            connectionTag.setInteger(NBT_LENGTH, connection.length);
            if (connection.targetSide != null) {
                connectionTag.setInteger(NBT_TARGET_SIDE, connection.targetSide.getIndex());
            }
            connectionList.appendTag(connectionTag);
        }
        compound.setTag(NBT_CONNECTIONS, connectionList);
        return compound;
    }

    // ---------------------------------------------------------------------
    // Tick
    // ---------------------------------------------------------------------

    @Override
    public void update() {
        if (world == null || world.isRemote) {
            return;
        }

        transferEnergyToConnections();

        if (++tick >= VALIDATE_INTERVAL) {
            tick = 0;
            validateConnections();
        }
    }

    // ---------------------------------------------------------------------
    // Client sync
    // ---------------------------------------------------------------------

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(pos, 0, getUpdateTag());
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet) {
        readFromNBT(packet.getNbtCompound());
    }

    @Override
    public @NotNull NBTTagCompound getUpdateTag() {
        return writeToNBT(new NBTTagCompound());
    }

    @Override
    public void handleUpdateTag(@NotNull NBTTagCompound tag) {
        readFromNBT(tag);
    }

    @Override
    public @NotNull AxisAlignedBB getRenderBoundingBox() {
        int range = getMaxWireLength(getConnectorTier()) + 1;
        return new AxisAlignedBB(
                pos.getX() - range, pos.getY() - range, pos.getZ() - range,
                pos.getX() + range + 1, pos.getY() + range + 1, pos.getZ() + range + 1);
    }

    public boolean shouldRender() {
        return !connections.isEmpty();
    }

    // ---------------------------------------------------------------------
    // Energy API
    // ---------------------------------------------------------------------

    public long acceptEnergyFromNetwork(EnumFacing side, long voltage, long amperage) {
        if (voltage <= 0 || amperage <= 0 || voltage > getInputVoltage()) {
            return 0;
        }

        long acceptedAmperage = Math.min(amperage, getInputAmperage());
        acceptedAmperage = Math.min(acceptedAmperage, (MaxEnergy - StoredEnergy) / voltage);
        if (acceptedAmperage <= 0) {
            return 0;
        }

        StoredEnergy += acceptedAmperage * voltage;
        markDirty();
        return acceptedAmperage;
    }

    public boolean inputsEnergy(EnumFacing side) {
        return true;
    }

    public boolean outputsEnergy(EnumFacing side) {
        return true;
    }

    public long changeEnergy(long differenceAmount) {
        long oldEnergy = StoredEnergy;
        StoredEnergy = clampEnergy(StoredEnergy + differenceAmount, 0, MaxEnergy);
        if (oldEnergy != StoredEnergy) {
            markDirty();
        }
        return StoredEnergy - oldEnergy;
    }

    public long getEnergyStored() {
        return StoredEnergy;
    }

    public long getEnergyCapacity() {
        return MaxEnergy;
    }

    public long getOutputAmperage() {
        return MAX_TRANSFER_AMPERAGE;
    }

    public long getOutputVoltage() {
        return GTValues.V[getGtTier()];
    }

    public long getInputAmperage() {
        return MAX_TRANSFER_AMPERAGE;
    }

    public long getInputVoltage() {
        return GTValues.V[getGtTier()];
    }

    public long fill(long amount) {
        if (amount <= 0) {
            return 0;
        }
        long accepted = Math.min(Math.min(amount, getTransferLimit()), MaxEnergy - StoredEnergy);
        if (accepted > 0) {
            StoredEnergy += accepted;
            markDirty();
        }
        return accepted;
    }

    public long drain(long amount) {
        if (amount <= 0) {
            return 0;
        }
        long removed = Math.min(Math.min(amount, getTransferLimit()), StoredEnergy);
        if (removed > 0) {
            StoredEnergy -= removed;
            markDirty();
        }
        return removed;
    }

    // ---------------------------------------------------------------------
    // Energy transfer
    // ---------------------------------------------------------------------

    private void transferEnergyToConnections() {
        if (connections.isEmpty()) {
            return;
        }

        for (WireConnection connection : new ArrayList<>(connections)) {
            if (!world.isBlockLoaded(connection.target)) {
                continue;
            }
            TileEntity tileEntity = world.getTileEntity(connection.target);
            if (tileEntity instanceof TileEntityConnector) {
                transferEnergyToConnector((TileEntityConnector) tileEntity);
            } else {
                transferEnergyToMachine(tileEntity, connection.targetSide);
            }
        }
    }

    private long insertEnergyFromConnector(long amount, long voltage) {
        if (amount <= 0 || voltage > getInputVoltage()) {
            return 0;
        }

        long accepted = Math.min(Math.min(amount, getTransferLimit()), MaxEnergy - StoredEnergy);
        if (accepted > 0) {
            StoredEnergy += accepted;
            markDirty();
        }
        return accepted;
    }

    private void transferEnergyToConnector(TileEntityConnector target) {
        // Only balance toward connectors with a lower storage ratio (energy spreading).
        if (getStorageRatio() <= target.getStorageRatio() || StoredEnergy < getOutputVoltage()) {
            return;
        }

        long transferAmount = Math.min(getTransferLimit(), StoredEnergy);
        long inserted = target.insertEnergyFromConnector(transferAmount, getOutputVoltage());
        if (inserted > 0) {
            drain(inserted);
        }
    }

    private void transferEnergyToMachine(@Nullable TileEntity tileEntity, @Nullable EnumFacing targetSide) {
        if (tileEntity == null) {
            return;
        }

        IEnergyContainer directContainer = getEnergyContainer(tileEntity, targetSide);
        if (directContainer != null) {
            if (directContainer.outputsEnergy(targetSide)) {
                pullEnergyFromMachine(directContainer, targetSide);
                return;
            }
            if (directContainer.inputsEnergy(targetSide)) {
                pushEnergyToMachine(directContainer, targetSide);
                return;
            }
        }

        EnergyConnection input = findEnergyConnection(tileEntity, targetSide, true);
        if (input != null) {
            pushEnergyToMachine(input.energyContainer, input.side);
            return;
        }

        EnergyConnection output = findEnergyConnection(tileEntity, targetSide, false);
        if (output != null) {
            pullEnergyFromMachine(output.energyContainer, output.side);
        }
    }

    private void pushEnergyToMachine(IEnergyContainer energyContainer, @Nullable EnumFacing targetSide) {
        if (StoredEnergy < getOutputVoltage()) {
            return;
        }
        long voltage = getOutputVoltage();
        long availableAmperage = Math.min(getOutputAmperage(), StoredEnergy / voltage);
        long acceptedAmperage = energyContainer.acceptEnergyFromNetwork(targetSide, voltage, availableAmperage);
        if (acceptedAmperage > 0) {
            drain(acceptedAmperage * voltage);
        }
    }

    private void pullEnergyFromMachine(IEnergyContainer energyContainer, @Nullable EnumFacing targetSide) {
        long voltage = energyContainer.getOutputVoltage();
        if (voltage <= 0 || voltage > getInputVoltage()) {
            return;
        }

        long availableAmperage = Math.min(MAX_TRANSFER_AMPERAGE, energyContainer.getOutputAmperage());
        availableAmperage = Math.min(availableAmperage, energyContainer.getEnergyStored() / voltage);
        availableAmperage = Math.min(availableAmperage, (MaxEnergy - StoredEnergy) / voltage);
        if (availableAmperage <= 0) {
            return;
        }

        long requested = availableAmperage * voltage;
        long changed = energyContainer.changeEnergy(-requested);
        long removed = changed < 0 ? -changed : 0;
        if (removed > 0) {
            fill(removed);
        }
    }

    // ---------------------------------------------------------------------
    // Validation
    // ---------------------------------------------------------------------

    private void validateConnections() {
        boolean changed = false;
        Iterator<WireConnection> iterator = connections.iterator();
        while (iterator.hasNext()) {
            WireConnection connection = iterator.next();

            if (connection.target.equals(pos) || connection.length > getMaxWireLength(connection.wireTier)) {
                iterator.remove();
                changed = true;
                continue;
            }
            if (!world.isBlockLoaded(connection.target)) {
                continue;
            }

            TileEntity tileEntity = world.getTileEntity(connection.target);
            if (tileEntity instanceof TileEntityConnector) {
                if (!((TileEntityConnector) tileEntity).hasConnection(pos)) {
                    iterator.remove();
                    changed = true;
                }
                continue;
            }

            boolean hasInput = findEnergyConnection(tileEntity, connection.targetSide, true) != null;
            boolean hasOutput = findEnergyConnection(tileEntity, connection.targetSide, false) != null;
            if (!hasInput && !hasOutput) {
                iterator.remove();
                changed = true;
            }
        }

        if (changed) {
            updateLegacyFields();
            markDirtyAndSync();
        }
    }

    // ---------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------

    private void updateLegacyFields() {
        this.selfPos = pos;
        if (connections.isEmpty()) {
            this.success = 0;
            this.nextPos = null;
            this.beforePos = null;
            return;
        }
        this.success = 1;
        this.nextPos = connections.get(0).target;
        this.beforePos = connections.get(0).target;
    }

    private void markDirtyAndSync() {
        markDirty();
        if (world != null && !world.isRemote) {
            IBlockState state = world.getBlockState(pos);
            world.notifyBlockUpdate(pos, state, state, 3);
        }
    }

    private long getTransferLimit() {
        return getOutputVoltage() * MAX_TRANSFER_AMPERAGE;
    }

    private double getStorageRatio() {
        return MaxEnergy <= 0 ? 0.0D : StoredEnergy / (double) MaxEnergy;
    }

    @Nullable
    private static IEnergyContainer getEnergyContainer(@Nullable TileEntity tileEntity, @Nullable EnumFacing side) {
        return tileEntity == null
                ? null
                : tileEntity.getCapability(GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER, side);
    }

    @Nullable
    private static EnergyConnection findEnergyConnection(@Nullable TileEntity tileEntity,
                                                         @Nullable EnumFacing preferredSide,
                                                         boolean input) {
        if (tileEntity == null) {
            return null;
        }

        EnergyConnection preferred = getUsableEnergyConnection(tileEntity, preferredSide, input);
        if (preferred != null) {
            return preferred;
        }

        for (EnumFacing side : EnumFacing.VALUES) {
            if (side == preferredSide) {
                continue;
            }
            EnergyConnection connection = getUsableEnergyConnection(tileEntity, side, input);
            if (connection != null) {
                return connection;
            }
        }

        if (preferredSide == null) {
            return null;
        }
        return getUsableEnergyConnection(tileEntity, null, input);
    }

    @Nullable
    private static EnergyConnection getUsableEnergyConnection(@Nullable TileEntity tileEntity,
                                                              @Nullable EnumFacing side,
                                                              boolean input) {
        IEnergyContainer energyContainer = getEnergyContainer(tileEntity, side);
        if (energyContainer == null) {
            return null;
        }
        boolean usable = input ? energyContainer.inputsEnergy(side) : energyContainer.outputsEnergy(side);
        return usable ? new EnergyConnection(energyContainer, side) : null;
    }

    private static int inferTier(long capacity) {
        if (capacity >= getCapacityForTier(3)) {
            return 3;
        }
        if (capacity >= getCapacityForTier(2)) {
            return 2;
        }
        return 1;
    }

    private static int clampTier(int tier) {
        if (tier < MIN_TIER) {
            return MIN_TIER;
        }
        if (tier > MAX_TIER) {
            return MAX_TIER;
        }
        return tier;
    }

    private static long clampEnergy(long value, long min, long max) {
        if (value < min) {
            return min;
        }
        return Math.min(value, max);
    }

    @Nullable
    private static BlockPos readBlockPos(NBTTagCompound compound, String key) {
        if (!compound.hasKey(key, Constants.NBT.TAG_COMPOUND)) {
            return null;
        }
        NBTTagCompound posTag = compound.getCompoundTag(key);
        return new BlockPos(posTag.getInteger(NBT_POS_X), posTag.getInteger(NBT_POS_Y), posTag.getInteger(NBT_POS_Z));
    }

    private static void writeBlockPos(NBTTagCompound compound, String key, @Nullable BlockPos blockPos) {
        if (blockPos == null) {
            return;
        }
        NBTTagCompound posTag = new NBTTagCompound();
        posTag.setInteger(NBT_POS_X, blockPos.getX());
        posTag.setInteger(NBT_POS_Y, blockPos.getY());
        posTag.setInteger(NBT_POS_Z, blockPos.getZ());
        compound.setTag(key, posTag);
    }

    // ---------------------------------------------------------------------
    // Inner types
    // ---------------------------------------------------------------------

    public static class WireConnection {
        public final BlockPos target;
        public final int wireTier;
        public final int length;
        @Nullable
        public final EnumFacing targetSide;

        public WireConnection(BlockPos target, int wireTier, int length, @Nullable EnumFacing targetSide) {
            this.target = target.toImmutable();
            this.wireTier = clampTier(wireTier);
            this.length = length;
            this.targetSide = targetSide;
        }
    }

    private static class EnergyConnection {
        private final IEnergyContainer energyContainer;
        @Nullable
        private final EnumFacing side;

        private EnergyConnection(IEnergyContainer energyContainer, @Nullable EnumFacing side) {
            this.energyContainer = energyContainer;
            this.side = side;
        }
    }

    public interface WorldAccess {
        TileEntity getTileEntity(BlockPos pos);
    }
}