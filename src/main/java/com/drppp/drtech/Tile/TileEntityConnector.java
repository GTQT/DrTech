package com.drppp.drtech.Tile;

import com.drppp.drtech.common.Items.MetaItems.DrMetaItems;
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

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class TileEntityConnector extends TileEntity implements ITickable {
    private static final int[] MAX_WIRE_LENGTH = {0, 32, 48, 64};
    private static final int[] WIRE_COLOR = {0, 0xB87333, 0xD7B35A, 0xC9C9C9};
    private static final long MAX_TRANSFER_AMPERAGE = 10;

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

    public static int getMaxWireLength(int tier) {
        return MAX_WIRE_LENGTH[clampTier(tier)];
    }

    public static int getWireColor(int tier) {
        return WIRE_COLOR[clampTier(tier)];
    }

    public static long getCapacityForTier(int tier) {
        return (long) Math.pow(4, 4 + 2 * clampTier(tier));
    }

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
        switch (clampTier(tier)) {
            case 1:
                return DrMetaItems.LOW_VOLTAGE_WIRE == null ? ItemStack.EMPTY : DrMetaItems.LOW_VOLTAGE_WIRE.getStackForm();
            case 2:
                return DrMetaItems.MEDIUM_VOLTAGE_WIRE == null ? ItemStack.EMPTY : DrMetaItems.MEDIUM_VOLTAGE_WIRE.getStackForm();
            case 3:
                return DrMetaItems.HIGH_VOLTAGE_WIRE == null ? ItemStack.EMPTY : DrMetaItems.HIGH_VOLTAGE_WIRE.getStackForm();
            default:
                return ItemStack.EMPTY;
        }
    }

    public int getConnectorTier() {
        this.connectorTier = clampTier(this.connectorTier);
        return this.connectorTier;
    }

    public boolean canConnectWire(int wireTier) {
        return getConnectorTier() == clampTier(wireTier);
    }

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
        if (target == null || target.equals(pos) || hasConnection(target) || !canConnectWire(wireTier)) {
            return false;
        }
        int tier = clampTier(wireTier);
        if (length > getMaxWireLength(tier)) {
            return false;
        }

        connections.add(new WireConnection(target.toImmutable(), tier, length, null));
        updateLegacyFields();
        markDirtyAndSync();
        return true;
    }

    public boolean addMachineConnection(BlockPos target, int wireTier, int length, @Nullable EnumFacing targetSide) {
        if (target == null || target.equals(pos) || hasConnection(target) || !canConnectWire(wireTier)) {
            return false;
        }
        int tier = clampTier(wireTier);
        if (length > getMaxWireLength(tier)) {
            return false;
        }

        connections.add(new WireConnection(target.toImmutable(), tier, length, targetSide));
        updateLegacyFields();
        markDirtyAndSync();
        return true;
    }

    public boolean removeConnection(BlockPos target) {
        Iterator<WireConnection> iterator = connections.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().target.equals(target)) {
                iterator.remove();
                updateLegacyFields();
                markDirtyAndSync();
                return true;
            }
        }
        return false;
    }

    public void removeAllConnections(boolean dropItems) {
        if (world == null) {
            connections.clear();
            updateLegacyFields();
            return;
        }

        List<WireConnection> copy = new ArrayList<>(connections);
        for (WireConnection connection : copy) {
            if (world.isBlockLoaded(connection.target)) {
                TileEntity tileEntity = world.getTileEntity(connection.target);
                if (tileEntity instanceof TileEntityConnector) {
                    ((TileEntityConnector) tileEntity).removeConnection(pos);
                }
            }
            if (dropItems && !world.isRemote) {
                ItemStack wireStack = getWireStack(connection.wireTier);
                if (!wireStack.isEmpty()) {
                    InventoryHelper.spawnItemStack(world, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, wireStack);
                }
            }
        }

        connections.clear();
        updateLegacyFields();
        markDirtyAndSync();
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);

        long savedMaxEnergy = compound.getLong("MaxEnergy");
        this.connectorTier = compound.hasKey("ConnectorTier") ? clampTier(compound.getInteger("ConnectorTier")) : inferTier(savedMaxEnergy);
        this.MaxEnergy = savedMaxEnergy > 0 ? savedMaxEnergy : getCapacityForTier(this.connectorTier);
        this.StoredEnergy = clampEnergy(compound.getLong("StoredEnergy"), 0, this.MaxEnergy);
        this.success = compound.getInteger("Success");

        this.selfPos = readBlockPos(compound, "selfPos");
        this.nextPos = readBlockPos(compound, "nextPos");
        this.beforePos = readBlockPos(compound, "beforePos");

        this.connections.clear();
        NBTTagList connectionList = compound.getTagList("Connections", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < connectionList.tagCount(); i++) {
            NBTTagCompound connectionTag = connectionList.getCompoundTagAt(i);
            BlockPos target = readBlockPos(connectionTag, "Target");
            int wireTier = clampTier(connectionTag.getInteger("WireTier"));
            int length = connectionTag.getInteger("Length");
            if (target != null && !target.equals(pos) && !hasConnection(target)) {
                EnumFacing targetSide = connectionTag.hasKey("TargetSide") ? EnumFacing.byIndex(connectionTag.getInteger("TargetSide")) : null;
                this.connections.add(new WireConnection(target, wireTier, length, targetSide));
            }
        }
        updateLegacyFields();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setInteger("ConnectorTier", getConnectorTier());
        compound.setLong("MaxEnergy", MaxEnergy);
        compound.setLong("StoredEnergy", StoredEnergy);
        compound.setInteger("Success", success);

        writeBlockPos(compound, "selfPos", selfPos);
        writeBlockPos(compound, "nextPos", nextPos);
        writeBlockPos(compound, "beforePos", beforePos);

        NBTTagList connectionList = new NBTTagList();
        for (WireConnection connection : connections) {
            NBTTagCompound connectionTag = new NBTTagCompound();
            writeBlockPos(connectionTag, "Target", connection.target);
            connectionTag.setInteger("WireTier", connection.wireTier);
            connectionTag.setInteger("Length", connection.length);
            if (connection.targetSide != null) {
                connectionTag.setInteger("TargetSide", connection.targetSide.getIndex());
            }
            connectionList.appendTag(connectionTag);
        }
        compound.setTag("Connections", connectionList);
        return compound;
    }

    @Override
    public void update() {
        if (world == null || world.isRemote) {
            return;
        }

        transferEnergyToConnections();

        if (tick++ >= 20) {
            tick = 0;
            validateConnections();
        }
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(pos, 0, getUpdateTag());
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet) {
        readFromNBT(packet.getNbtCompound());
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        return writeToNBT(new NBTTagCompound());
    }

    @Override
    public void handleUpdateTag(NBTTagCompound tag) {
        readFromNBT(tag);
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        int range = getMaxWireLength(getConnectorTier()) + 1;
        return new AxisAlignedBB(
                pos.getX() - range, pos.getY() - range, pos.getZ() - range,
                pos.getX() + range + 1, pos.getY() + range + 1, pos.getZ() + range + 1);
    }

    public long acceptEnergyFromNetwork(EnumFacing side, long voltage, long amperage) {
        if (voltage <= 0 || amperage <= 0 || voltage > getInputVoltage()) {
            return 0;
        }

        long acceptedAmperage = Math.min(amperage, getInputAmperage());
        long roomAmperage = (MaxEnergy - StoredEnergy) / voltage;
        acceptedAmperage = Math.min(acceptedAmperage, roomAmperage);
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
        long accepted = Math.min(amount, getTransferLimit());
        accepted = Math.min(accepted, MaxEnergy - StoredEnergy);
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
        long removed = Math.min(amount, getTransferLimit());
        removed = Math.min(removed, StoredEnergy);
        if (removed > 0) {
            StoredEnergy -= removed;
            markDirty();
        }
        return removed;
    }

    public boolean shouldRender() {
        return !connections.isEmpty();
    }

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

        long accepted = Math.min(amount, getTransferLimit());
        accepted = Math.min(accepted, MaxEnergy - StoredEnergy);
        if (accepted > 0) {
            StoredEnergy += accepted;
            markDirty();
        }
        return accepted;
    }

    private void transferEnergyToConnector(TileEntityConnector target) {
        double ownRatio = getStorageRatio();
        double targetRatio = target.getStorageRatio();
        if (ownRatio <= targetRatio || StoredEnergy < getOutputVoltage()) {
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

        IEnergyContainer targetSideContainer = getEnergyContainer(tileEntity, targetSide);
        if (targetSideContainer != null) {
            if (targetSideContainer.outputsEnergy(targetSide)) {
                pullEnergyFromMachine(targetSideContainer, targetSide);
                return;
            }
            if (targetSideContainer.inputsEnergy(targetSide)) {
                pushEnergyToMachine(targetSideContainer, targetSide);
                return;
            }
        }

        EnergyConnection inputConnection = findEnergyConnection(tileEntity, targetSide, true);
        if (inputConnection != null) {
            pushEnergyToMachine(inputConnection.energyContainer, inputConnection.side);
            return;
        }

        EnergyConnection outputConnection = findEnergyConnection(tileEntity, targetSide, false);
        if (outputConnection != null) {
            pullEnergyFromMachine(outputConnection.energyContainer, outputConnection.side);
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

        long roomAmperage = (MaxEnergy - StoredEnergy) / voltage;
        long availableAmperage = Math.min(MAX_TRANSFER_AMPERAGE, energyContainer.getOutputAmperage());
        availableAmperage = Math.min(availableAmperage, energyContainer.getEnergyStored() / voltage);
        availableAmperage = Math.min(availableAmperage, roomAmperage);
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

            if (findEnergyConnection(tileEntity, connection.targetSide, true) == null && findEnergyConnection(tileEntity, connection.targetSide, false) == null) {
                iterator.remove();
                changed = true;
            }
        }

        if (changed) {
            updateLegacyFields();
            markDirtyAndSync();
        }
    }

    private void updateLegacyFields() {
        this.selfPos = pos;
        if (connections.isEmpty()) {
            this.success = 0;
            this.nextPos = null;
            this.beforePos = null;
        } else {
            this.success = 1;
            this.nextPos = connections.get(0).target;
            this.beforePos = connections.get(0).target;
        }
    }

    private void markDirtyAndSync() {
        markDirty();
        if (world != null && !world.isRemote) {
            IBlockState state = world.getBlockState(pos);
            world.notifyBlockUpdate(pos, state, state, 3);
        }
    }

    private int getGtTier() {
        switch (getConnectorTier()) {
            case 2:
                return GTValues.MV;
            case 3:
                return GTValues.HV;
            case 1:
            default:
                return GTValues.LV;
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
        return tileEntity == null ? null : tileEntity.getCapability(GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER, side);
    }

    @Nullable
    private static EnergyConnection findEnergyConnection(@Nullable TileEntity tileEntity, @Nullable EnumFacing preferredSide, boolean input) {
        EnergyConnection preferredConnection = getUsableEnergyConnection(tileEntity, preferredSide, input);
        if (preferredConnection != null) {
            return preferredConnection;
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

        return preferredSide == null ? null : getUsableEnergyConnection(tileEntity, null, input);
    }

    @Nullable
    private static EnergyConnection getUsableEnergyConnection(@Nullable TileEntity tileEntity, @Nullable EnumFacing side, boolean input) {
        IEnergyContainer energyContainer = getEnergyContainer(tileEntity, side);
        if (energyContainer == null) {
            return null;
        }
        if (input ? energyContainer.inputsEnergy(side) : energyContainer.outputsEnergy(side)) {
            return new EnergyConnection(energyContainer, side);
        }
        return null;
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
        if (tier < 1) {
            return 1;
        }
        if (tier > 3) {
            return 3;
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
        return new BlockPos(posTag.getInteger("xx"), posTag.getInteger("yy"), posTag.getInteger("zz"));
    }

    private static void writeBlockPos(NBTTagCompound compound, String key, @Nullable BlockPos blockPos) {
        if (blockPos == null) {
            return;
        }
        NBTTagCompound posTag = new NBTTagCompound();
        posTag.setInteger("xx", blockPos.getX());
        posTag.setInteger("yy", blockPos.getY());
        posTag.setInteger("zz", blockPos.getZ());
        compound.setTag(key, posTag);
    }

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
