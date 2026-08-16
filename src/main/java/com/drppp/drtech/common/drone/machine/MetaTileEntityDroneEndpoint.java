package com.drppp.drtech.common.drone.machine;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.utils.serialization.ByteBufAdapters;
import com.cleanroommc.modularui.value.sync.GenericSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.SyncHandlers;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;
import com.drppp.drtech.common.drone.network.DroneEndpoint;
import com.drppp.drtech.common.drone.network.DroneEndpointNetwork;
import gregtech.api.GTValues;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.TieredMetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.mui.GTGuis;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nullable;
import java.util.UUID;
import java.util.List;
import java.util.Collections;

/** Native EV logistics buffer exposing exactly the capability represented by its endpoint kind. */
public final class MetaTileEntityDroneEndpoint extends TieredMetaTileEntity {
    private static final String CONFIG_ACTION = "endpoint_config";
    private static final int ITEM_SLOTS = 9;
    private static final int FLUID_CAPACITY = 64_000;
    private final DroneEndpoint.Kind kind;
    private final ItemStackHandler itemBuffer = new ItemStackHandler(ITEM_SLOTS) {
        @Override protected void onContentsChanged(int slot) { markDirty(); }
    };
    private final FluidTank fluidBuffer = new FluidTank(FLUID_CAPACITY) {
        @Override protected void onContentsChanged() { markDirty(); }
    };
    private UUID endpointId = UUID.randomUUID();
    private UUID ownerId;
    private long requestAmount;
    private long provideAmount;
    private int priority;
    private List<String> whitelist = Collections.emptyList();
    private long minimumReserve;
    private long maximumInventory;

    public MetaTileEntityDroneEndpoint(ResourceLocation metaTileEntityId, DroneEndpoint.Kind kind) {
        super(metaTileEntityId, GTValues.EV);
        this.kind = kind;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityDroneEndpoint(metaTileEntityId, kind);
    }

    @Override public boolean usesMui2() { return true; }

    @Override
    public void update() {
        super.update();
        if (getWorld() != null && !getWorld().isRemote && getOffsetTimer() % 20 == 0) {
            DroneEndpointNetwork.get(getWorld()).heartbeat(new DroneEndpoint(endpointId, kind,
                    getWorld().provider.getDimension(), getPos(), ownerId, getWorld().getTotalWorldTime(), true,
                    requestAmount, provideAmount, priority, whitelist, minimumReserve, maximumInventory));
        }
    }

    public long getRequestAmount() { return requestAmount; }
    public long getProvideAmount() { return provideAmount; }
    public int getPriority() { return priority; }
    public List<String> getWhitelist() { return whitelist; }
    public long getMinimumReserve() { return minimumReserve; }
    public long getMaximumInventory() { return maximumInventory; }

    public void configureLogistics(long requestAmount, long provideAmount, int priority) {
        this.requestAmount = Math.max(0L, requestAmount);
        this.provideAmount = Math.max(0L, provideAmount);
        this.priority = Math.max(-100, Math.min(100, priority));
        markDirty();
    }

    public void configureInventoryPolicy(List<String> whitelist, long minimumReserve, long maximumInventory) {
        this.whitelist = whitelist == null ? Collections.emptyList() : Collections.unmodifiableList(whitelist);
        this.minimumReserve = Math.max(0L, minimumReserve);
        this.maximumInventory = Math.max(0L, maximumInventory);
        markDirty();
    }

    @Override
    public ModularPanel buildUI(PosGuiData guiData, PanelSyncManager syncManager, UISettings settings) {
        bindOwner(guiData.getPlayer());
        syncManager.registerSlotGroup("endpoint_items", ITEM_SLOTS);
        syncManager.registerSyncedAction(CONFIG_ACTION, false, true,
                packet -> receiveConfigAction(guiData.getPlayer(), packet.readString(24)));
        syncManager.syncValue("endpoint_config_state", GenericSyncValue.builder(NBTTagCompound.class)
                .getter(this::createConfigState)
                .setter(this::receiveConfigState)
                .adapter(ByteBufAdapters.NBT)
                .copy(NBTTagCompound::copy)
                .build());
        ModularPanel panel = GTGuis.createPanel(this, 176, 158)
                .child(IKey.lang(getMetaFullName()).asWidget().pos(7, 6))
                .child(IKey.dynamic(this::statusLine).asWidget().pos(7, 24).size(162, 22));
        if (kind == DroneEndpoint.Kind.ITEM) {
            for (int slot = 0; slot < ITEM_SLOTS; slot++) {
                panel.child(new ItemSlot().slot(SyncHandlers.itemSlot(itemBuffer, slot)
                        .slotGroup("endpoint_items").accessibility(true, true))
                        .pos(7 + slot * 18, 54));
            }
        }
        addConfigRow(panel, syncManager, "drtech.drone.endpoint.request", () -> requestAmount,
                "REQUEST_MINUS", "REQUEST_PLUS", 82);
        addConfigRow(panel, syncManager, "drtech.drone.endpoint.provide", () -> provideAmount,
                "PROVIDE_MINUS", "PROVIDE_PLUS", 96);
        addConfigRow(panel, syncManager, "drtech.drone.endpoint.priority", () -> (long) priority,
                "PRIORITY_MINUS", "PRIORITY_PLUS", 110);
        addConfigRow(panel, syncManager, "drtech.drone.endpoint.reserve", () -> minimumReserve,
                "RESERVE_MINUS", "RESERVE_PLUS", 124);
        addConfigRow(panel, syncManager, "drtech.drone.endpoint.maximum", () -> maximumInventory,
                "MAXIMUM_MINUS", "MAXIMUM_PLUS", 138);
        return panel;
    }

    private void addConfigRow(ModularPanel panel, PanelSyncManager syncManager, String label,
            java.util.function.Supplier<Long> value, String minus, String plus, int y) {
        panel.child(IKey.lang(label).asWidget().pos(7, y + 2).size(62, 10))
                .child(IKey.dynamic(() -> Long.toString(value.get())).asWidget().pos(70, y + 2).size(48, 10))
                .child(configButton("-", minus, 120, y, syncManager))
                .child(configButton("+", plus, 145, y, syncManager));
    }

    private ButtonWidget<?> configButton(String label, String command, int x, int y,
            PanelSyncManager syncManager) {
        return new ButtonWidget<>().pos(x, y).size(22, 12).overlay(IKey.str(label))
                .onMousePressed(mouse -> {
                    syncManager.callSyncedAction(CONFIG_ACTION, packet -> packet.writeString(command));
                    return true;
                });
    }

    private void receiveConfigAction(EntityPlayer player, String command) {
        if (player == null || ownerId == null || !ownerId.equals(player.getUniqueID())) return;
        switch (command) {
            case "REQUEST_MINUS": requestAmount = decrease(requestAmount, 100L); break;
            case "REQUEST_PLUS": requestAmount = increase(requestAmount, 100L); break;
            case "PROVIDE_MINUS": provideAmount = decrease(provideAmount, 100L); break;
            case "PROVIDE_PLUS": provideAmount = increase(provideAmount, 100L); break;
            case "PRIORITY_MINUS": priority = Math.max(-100, priority - 1); break;
            case "PRIORITY_PLUS": priority = Math.min(100, priority + 1); break;
            case "RESERVE_MINUS": minimumReserve = decrease(minimumReserve, 100L); break;
            case "RESERVE_PLUS": minimumReserve = increase(minimumReserve, 100L); break;
            case "MAXIMUM_MINUS": maximumInventory = decrease(maximumInventory, 100L); break;
            case "MAXIMUM_PLUS": maximumInventory = increase(maximumInventory, 100L); break;
            default: return;
        }
        markDirty();
    }

    private NBTTagCompound createConfigState() {
        NBTTagCompound state = new NBTTagCompound();
        state.setLong("Request", requestAmount);
        state.setLong("Provide", provideAmount);
        state.setInteger("Priority", priority);
        state.setLong("Reserve", minimumReserve);
        state.setLong("Maximum", maximumInventory);
        return state;
    }

    private void receiveConfigState(NBTTagCompound state) {
        requestAmount = Math.max(0L, state.getLong("Request"));
        provideAmount = Math.max(0L, state.getLong("Provide"));
        priority = Math.max(-100, Math.min(100, state.getInteger("Priority")));
        minimumReserve = Math.max(0L, state.getLong("Reserve"));
        maximumInventory = Math.max(0L, state.getLong("Maximum"));
    }

    private static long decrease(long value, long step) { return Math.max(0L, value - Math.min(value, step)); }
    private static long increase(long value, long step) {
        return value > Long.MAX_VALUE - step ? Long.MAX_VALUE : value + step;
    }

    private void bindOwner(EntityPlayer player) {
        if (getWorld() != null && !getWorld().isRemote && ownerId == null && player != null) {
            ownerId = player.getUniqueID();
            markDirty();
        }
    }

    private String statusLine() {
        String config = " | Req: " + requestAmount + " | Out: " + provideAmount + " | P: " + priority;
        if (kind == DroneEndpoint.Kind.ITEM) return "Slots: " + ITEM_SLOTS + config + " | " + shortId();
        if (kind == DroneEndpoint.Kind.FLUID) return "Fluid: " + fluidBuffer.getFluidAmount() + "/"
                + fluidBuffer.getCapacity() + " mB" + config + " | " + shortId();
        return "EU: " + energyContainer.getEnergyStored() + "/" + energyContainer.getEnergyCapacity()
                + config + " | " + shortId();
    }

    private String shortId() { return endpointId.toString().substring(0, 8); }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setString("EndpointId", endpointId.toString());
        if (ownerId != null) data.setString("Owner", ownerId.toString());
        data.setLong("RequestAmount", requestAmount);
        data.setLong("ProvideAmount", provideAmount);
        data.setInteger("Priority", priority);
        net.minecraft.nbt.NBTTagList allowed = new net.minecraft.nbt.NBTTagList();
        for (String value : whitelist) { NBTTagCompound entry = new NBTTagCompound(); entry.setString("Value", value); allowed.appendTag(entry); }
        data.setTag("Whitelist", allowed);
        data.setLong("MinimumReserve", minimumReserve);
        data.setLong("MaximumInventory", maximumInventory);
        data.setTag("ItemBuffer", itemBuffer.serializeNBT());
        data.setTag("FluidBuffer", fluidBuffer.writeToNBT(new NBTTagCompound()));
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        endpointId = readUuid(data, "EndpointId", UUID.randomUUID());
        ownerId = readUuid(data, "Owner", null);
        requestAmount = data.hasKey("RequestAmount", 4) ? Math.max(0L, data.getLong("RequestAmount")) : 0L;
        provideAmount = data.hasKey("ProvideAmount", 4) ? Math.max(0L, data.getLong("ProvideAmount")) : 0L;
        priority = data.hasKey("Priority", 3) ? Math.max(-100, Math.min(100, data.getInteger("Priority"))) : 0;
        java.util.ArrayList<String> allowed = new java.util.ArrayList<>();
        net.minecraft.nbt.NBTTagList list = data.getTagList("Whitelist", 10);
        for (int i = 0; i < list.tagCount() && allowed.size() < 64; i++) {
            String value = list.getCompoundTagAt(i).getString("Value");
            if (!value.trim().isEmpty()) allowed.add(value.trim());
        }
        whitelist = Collections.unmodifiableList(allowed);
        minimumReserve = data.hasKey("MinimumReserve", 4) ? Math.max(0L, data.getLong("MinimumReserve")) : 0L;
        maximumInventory = data.hasKey("MaximumInventory", 4) ? Math.max(0L, data.getLong("MaximumInventory")) : 0L;
        if (data.hasKey("ItemBuffer", 10)) itemBuffer.deserializeNBT(data.getCompoundTag("ItemBuffer"));
        if (data.hasKey("FluidBuffer", 10)) fluidBuffer.readFromNBT(data.getCompoundTag("FluidBuffer"));
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        return kind == DroneEndpoint.Kind.ITEM && capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY
                || kind == DroneEndpoint.Kind.FLUID && capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY
                || super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (kind == DroneEndpoint.Kind.ITEM && capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(itemBuffer);
        }
        if (kind == DroneEndpoint.Kind.FLUID && capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(fluidBuffer);
        }
        return super.getCapability(capability, facing);
    }

    @Nullable
    private static UUID readUuid(NBTTagCompound data, String key, @Nullable UUID fallback) {
        try { return data.hasKey(key, 8) ? UUID.fromString(data.getString(key)) : fallback; }
        catch (IllegalArgumentException ignored) { return fallback; }
    }
}
