package com.drppp.drtech.common.drone.machine;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.utils.serialization.ByteBufAdapters;
import com.cleanroommc.modularui.value.sync.GenericSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.SyncHandlers;
import com.cleanroommc.modularui.value.StringValue;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;
import com.cleanroommc.modularui.widgets.textfield.TextFieldWidget;
import com.drppp.drtech.Client.drone.DroneProgramCanvasWidget;
import com.drppp.drtech.common.drone.item.DroneItemData;
import com.drppp.drtech.common.drone.entity.EntityProgrammableDrone;
import com.drppp.drtech.common.drone.item.ItemDroneProgramCard;
import com.drppp.drtech.common.drone.item.ItemProgrammableDrone;
import com.drppp.drtech.common.drone.program.codec.DroneProgramFormatException;
import com.drppp.drtech.common.drone.program.codec.DroneProgramNbtCodec;
import com.drppp.drtech.common.drone.program.compile.DroneDiagnosticSeverity;
import com.drppp.drtech.common.drone.program.compile.DroneProgramDiagnostic;
import com.drppp.drtech.common.drone.program.edit.DroneGraphCommandCodec;
import com.drppp.drtech.common.drone.program.edit.DroneGraphEditCommand;
import com.drppp.drtech.common.drone.program.edit.DroneGraphEditResult;
import com.drppp.drtech.common.drone.program.model.DroneProgramEdge;
import com.drppp.drtech.common.drone.program.model.DroneProgramGraph;
import com.drppp.drtech.common.drone.program.model.DroneProgramNode;
import com.drppp.drtech.common.drone.program.registry.DrTechDroneNodes;
import com.drppp.drtech.common.drone.program.edit.DroneProgramEditSession;
import com.drppp.drtech.common.drone.program.runtime.DrTechDroneExecutors;
import com.drppp.drtech.common.drone.program.runtime.DrTechDroneValueEvaluators;
import com.drppp.drtech.common.drone.program.runtime.DroneExecutorRegistry;
import com.drppp.drtech.common.drone.program.runtime.DroneValueEvaluatorRegistry;
import gregtech.api.GTValues;
import gregtech.api.items.itemhandlers.GTItemStackHandler;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.TieredMetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.mui.GTGuis;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.IItemHandlerModifiable;

import java.util.UUID;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/** Server-authoritative visual programmer with a single writer lock and optimistic revision checks. */
public final class MetaTileEntityDroneProgrammer extends TieredMetaTileEntity {

    public static final int MACHINE_TIER = GTValues.EV;
    private static final String EDIT_ACTION = "drone_graph_edit";
    private static final String WRITE_DRONE_ACTION = "drone_write_program";
    private static final String REMOTE_CONTROL_ACTION = "drone_remote_control";
    private static final int MAX_EDIT_DISTANCE_SQUARED = 64;
    private static final int MAX_REMOTE_DEBUG_RANGE = 512;
    private static final long PROGRAM_WRITE_EU = 8_192L;

    private DroneProgramEditSession editSession;
    private int loadedCardFingerprint = Integer.MIN_VALUE;
    private ItemStack loadedCardReference = ItemStack.EMPTY;
    private UUID editorOwner;
    private String serverStatus = "Insert a program card";

    private DroneProgramGraph clientGraph;
    private boolean clientEditable;
    private String clientStatus = "Waiting for server";
    private int clientErrors;
    private int clientWarnings;
    private final List<String> clientDiagnosticLines = new ArrayList<>();
    private final Set<UUID> clientDiagnosticNodeIds = new HashSet<>();
    private UUID clientActiveNodeId;
    private String clientRuntimeStatus = "NOT RUN";
    private boolean clientRemoteConnected;
    private String clientRemoteInfo = "Wireless: disconnected";
    private int clientLibraryPage;
    private int clientInspectorPage;
    private String clientNodeSearch = "";
    private String clientFluidSearch = "";
    private int clientFluidResultIndex;
    private String clientFluidCacheQuery;
    private List<String> clientFluidResults = Collections.emptyList();
    private String clientProgramName = "";
    private boolean clientProgramNameDirty;

    public MetaTileEntityDroneProgrammer(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, MACHINE_TIER);
    }

    @Override
    protected IItemHandlerModifiable createImportItemHandler() {
        return new GTItemStackHandler(this, 2) {
            @Override
            public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
                if (slot == 0 && !(stack.getItem() instanceof ItemDroneProgramCard)) return stack;
                if (slot == 1 && !(stack.getItem() instanceof ItemProgrammableDrone)) return stack;
                return super.insertItem(slot, stack, simulate);
            }
        };
    }

    @Override
    public void update() {
        super.update();
        if (getWorld() != null && !getWorld().isRemote && getOffsetTimer() % 5 == 0) {
            refreshCardSession();
        }
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityDroneProgrammer(metaTileEntityId);
    }

    @Override
    public boolean usesMui2() {
        return true;
    }

    @Override
    public ModularPanel buildUI(PosGuiData guiData, PanelSyncManager syncManager, UISettings settings) {
        if (getWorld() != null && !getWorld().isRemote) refreshCardSession();
        syncManager.registerSlotGroup("programmer_items", 2);
        GenericSyncValue<NBTTagCompound> editorState = GenericSyncValue.builder(NBTTagCompound.class)
                .getter(() -> createEditorState(guiData.getPlayer()))
                .setter(this::receiveEditorState)
                .adapter(ByteBufAdapters.NBT)
                .copy(NBTTagCompound::copy)
                .build();
        syncManager.syncValue("drone_editor_state", editorState);
        syncManager.registerSyncedAction(EDIT_ACTION, false, true,
                packet -> receiveEditAction(guiData.getPlayer(), packet));
        syncManager.registerSyncedAction(WRITE_DRONE_ACTION, false, true,
                packet -> writeProgramToDrone(guiData.getPlayer()));
        syncManager.registerSyncedAction(REMOTE_CONTROL_ACTION, false, true,
                packet -> receiveRemoteControl(guiData.getPlayer(), packet.readString(24)));
        syncManager.addOpenListener(this::onEditorOpened);
        syncManager.addCloseListener(this::onEditorClosed);

        DroneProgramCanvasWidget canvas = new DroneProgramCanvasWidget(this::getClientGraph,
                command -> sendEditCommand(syncManager, command), () -> clientEditable,
                this::getClientDiagnosticNodeIds, () -> clientActiveNodeId)
                .pos(91, 24).size(281, 222);

        return GTGuis.createPanel(this, 520, 376)
                .child(IKey.lang(getMetaFullName()).asWidget().pos(5, 5))
                .child(IKey.lang("drtech.drone.programmer.program_name").asWidget().pos(91, 8).size(34, 10))
                .child(new TextFieldWidget().pos(126, 4).size(190, 16).setMaxLength(64)
                        .setEnabledIf(widget -> clientEditable)
                        .value(new StringValue.Dynamic(this::getClientProgramName, this::setClientProgramName)))
                .child(new ButtonWidget<>().pos(319, 4).size(53, 16)
                        .overlay(IKey.lang("drtech.drone.programmer.rename"))
                        .setEnabledIf(widget -> clientEditable && clientProgramNameDirty)
                        .onMousePressed(mouse -> { sendProgramRename(syncManager); return true; }))
                .child(IKey.dynamic(this::getStatusLine).asWidget().pos(91, 270).size(421, 12))
                .child(new ItemSlot()
                        .slot(SyncHandlers.itemSlot(importItems, 0).slotGroup("programmer_items").accessibility(true, true))
                        .pos(7, 24))
                .child(new ItemSlot()
                        .slot(SyncHandlers.itemSlot(importItems, 1).slotGroup("programmer_items").accessibility(true, true))
                        .pos(49, 24))
                .child(new ButtonWidget<>().pos(5, 45).size(81, 16)
                        .overlay(IKey.lang("drtech.drone.programmer.write_drone"))
                        .onMousePressed(mouse -> { syncManager.callSyncedAction(WRITE_DRONE_ACTION); return true; }))
                .child(IKey.lang("drtech.drone.programmer.search").asWidget().pos(5, 68).size(20, 10))
                .child(new TextFieldWidget().pos(27, 65).size(59, 16).setMaxLength(32)
                        .value(new StringValue.Dynamic(() -> clientNodeSearch, value -> clientNodeSearch = value)))
                .child(pagedNodeButton(0, DrTechDroneNodes.WAIT, 5, 83, syncManager))
                .child(pagedNodeButton(0, DrTechDroneNodes.BRANCH, 5, 98, syncManager))
                .child(pagedSmallNodeButton(0, DrTechDroneNodes.END, 5, 113, syncManager))
                .child(pagedSmallNodeButton(0, DrTechDroneNodes.REPEAT, 47, 113, syncManager))
                .child(pagedSmallNodeButton(0, DrTechDroneNodes.WHILE, 5, 128, syncManager))
                .child(pagedSmallNodeButton(0, DrTechDroneNodes.WAIT_FOR_REDSTONE, 47, 128, syncManager))
                .child(pagedSmallNodeButton(0, DrTechDroneNodes.WAIT_FOR_OWNER, 5, 143, syncManager))
                .child(pagedSmallNodeButton(0, DrTechDroneNodes.MOVE_TO, 47, 143, syncManager))
                .child(pagedSmallNodeButton(0, DrTechDroneNodes.RETURN_TO_DOCK, 5, 158, syncManager))
                .child(pagedSmallNodeButton(0, DrTechDroneNodes.CHARGE_UNTIL, 47, 158, syncManager))
                .child(pagedSmallNodeButton(0, DrTechDroneNodes.FOR_EACH_COORDINATE, 5, 173, syncManager))
                .child(pagedSmallNodeButton(0, DrTechDroneNodes.FIND_NEAREST_DOCK, 47, 173, syncManager))
                .child(pagedSmallNodeButton(0, DrTechDroneNodes.BIND_DOCK, 5, 188, syncManager))
                .child(pagedSmallNodeButton(0, DrTechDroneNodes.UNBIND_DOCK, 47, 188, syncManager))
                .child(pagedSmallNodeButton(0, DrTechDroneNodes.CONFIGURE_SAFETY, 5, 203, syncManager))

                .child(pagedSmallNodeButton(1, DrTechDroneNodes.BREAK_BLOCK_AT, 5, 83, syncManager))
                .child(pagedSmallNodeButton(1, DrTechDroneNodes.PLACE_BLOCK, 47, 83, syncManager))
                .child(pagedSmallNodeButton(1, DrTechDroneNodes.BREAK_BLOCK, 5, 98, syncManager))
                .child(pagedSmallNodeButton(1, DrTechDroneNodes.PLACE_AREA, 47, 98, syncManager))
                .child(pagedSmallNodeButton(1, DrTechDroneNodes.IMPORT_ITEMS, 5, 113, syncManager))
                .child(pagedSmallNodeButton(1, DrTechDroneNodes.EXPORT_ITEMS, 47, 113, syncManager))
                .child(pagedSmallNodeButton(1, DrTechDroneNodes.ITEM_FILTER, 5, 128, syncManager))
                .child(pagedSmallNodeButton(1, DrTechDroneNodes.BLOCK_FILTER, 47, 128, syncManager))
                .child(pagedSmallNodeButton(1, DrTechDroneNodes.INTERACT_BLOCK, 5, 143, syncManager))
                .child(pagedSmallNodeButton(1, DrTechDroneNodes.USE_ITEM_ON_BLOCK, 47, 143, syncManager))
                .child(pagedSmallNodeButton(1, DrTechDroneNodes.PICKUP_DROPPED_ITEMS, 5, 158, syncManager))
                .child(pagedSmallNodeButton(1, DrTechDroneNodes.DROP_ITEMS, 47, 158, syncManager))
                .child(pagedSmallNodeButton(1, DrTechDroneNodes.HARVEST_CROP, 5, 173, syncManager))
                .child(pagedSmallNodeButton(1, DrTechDroneNodes.SET_REDSTONE_OUTPUT, 47, 173, syncManager))
                .child(pagedSmallNodeButton(1, DrTechDroneNodes.IMPORT_EU, 5, 188, syncManager))
                .child(pagedSmallNodeButton(1, DrTechDroneNodes.EXPORT_EU, 47, 188, syncManager))
                .child(pagedSmallNodeButton(1, DrTechDroneNodes.CHARGE_TARGET_PERCENT, 5, 203, syncManager))

                .child(pagedSmallNodeButton(4, DrTechDroneNodes.IMPORT_FLUID, 5, 83, syncManager))
                .child(pagedSmallNodeButton(4, DrTechDroneNodes.EXPORT_FLUID, 47, 83, syncManager))
                .child(pagedSmallNodeButton(4, DrTechDroneNodes.DRAIN_FLUID, 5, 98, syncManager))
                .child(pagedSmallNodeButton(4, DrTechDroneNodes.FLUID_FILTER, 47, 98, syncManager))
                .child(pagedSmallNodeButton(4, DrTechDroneNodes.DRONE_FLUID_AMOUNT, 5, 113, syncManager))
                .child(pagedSmallNodeButton(4, DrTechDroneNodes.DRONE_FLUID_PERCENT, 47, 113, syncManager))
                .child(pagedSmallNodeButton(4, DrTechDroneNodes.CONTAINER_FLUID_AMOUNT, 5, 128, syncManager))
                .child(pagedSmallNodeButton(4, DrTechDroneNodes.FIND_FLUID_CONTAINER, 47, 128, syncManager))
                .child(pagedSmallNodeButton(4, DrTechDroneNodes.WAIT_FOR_FLUID_AMOUNT, 5, 143, syncManager))

                .child(pagedSmallNodeButton(2, DrTechDroneNodes.NUMBER, 5, 83, syncManager))
                .child(pagedSmallNodeButton(2, DrTechDroneNodes.BOOLEAN, 47, 83, syncManager))
                .child(pagedSmallNodeButton(2, DrTechDroneNodes.COORDINATE, 5, 98, syncManager))
                .child(pagedSmallNodeButton(2, DrTechDroneNodes.AREA, 47, 98, syncManager))
                .child(pagedSmallNodeButton(2, DrTechDroneNodes.COMPARE_NUMBER, 5, 113, syncManager))
                .child(pagedSmallNodeButton(2, DrTechDroneNodes.NUMBER_MATH, 47, 113, syncManager))
                .child(pagedSmallNodeButton(2, DrTechDroneNodes.BOOLEAN_LOGIC, 5, 128, syncManager))
                .child(pagedSmallNodeButton(2, DrTechDroneNodes.BOOLEAN_NOT, 47, 128, syncManager))
                .child(pagedSmallNodeButton(2, DrTechDroneNodes.COORDINATE_OFFSET, 5, 143, syncManager))
                .child(pagedSmallNodeButton(2, DrTechDroneNodes.AREA_FROM_CORNERS, 47, 143, syncManager))
                .child(pagedSmallNodeButton(2, DrTechDroneNodes.GET_NUMBER_VARIABLE, 5, 158, syncManager))
                .child(pagedSmallNodeButton(2, DrTechDroneNodes.SET_NUMBER_VARIABLE, 47, 158, syncManager))
                .child(pagedSmallNodeButton(2, DrTechDroneNodes.ADD_NUMBER_VARIABLE, 5, 173, syncManager))
                .child(pagedSmallNodeButton(2, DrTechDroneNodes.ENERGY_LEVEL, 47, 173, syncManager))

                .child(pagedSmallNodeButton(5, DrTechDroneNodes.SPHERE_AREA, 5, 83, syncManager))
                .child(pagedSmallNodeButton(5, DrTechDroneNodes.CYLINDER_AREA, 47, 83, syncManager))
                .child(pagedSmallNodeButton(5, DrTechDroneNodes.PATH_AREA, 5, 98, syncManager))
                .child(pagedSmallNodeButton(5, DrTechDroneNodes.AREA_UNION, 47, 98, syncManager))
                .child(pagedSmallNodeButton(5, DrTechDroneNodes.AREA_INTERSECTION, 5, 113, syncManager))
                .child(pagedSmallNodeButton(5, DrTechDroneNodes.AREA_DIFFERENCE, 47, 113, syncManager))
                .child(pagedSmallNodeButton(5, DrTechDroneNodes.AREA_OFFSET, 5, 128, syncManager))
                .child(pagedSmallNodeButton(5, DrTechDroneNodes.AREA_CONTAINS, 47, 128, syncManager))
                .child(pagedSmallNodeButton(5, DrTechDroneNodes.AREA_VOLUME, 5, 143, syncManager))
                .child(pagedSmallNodeButton(5, DrTechDroneNodes.PLANE_AREA, 47, 143, syncManager))
                .child(pagedSmallNodeButton(3, DrTechDroneNodes.CARGO_ITEM_COUNT, 5, 83, syncManager))
                .child(pagedSmallNodeButton(3, DrTechDroneNodes.CARGO_FREE_SLOTS, 47, 83, syncManager))
                .child(pagedSmallNodeButton(3, DrTechDroneNodes.CARGO_USED_PERCENT, 5, 98, syncManager))
                .child(pagedSmallNodeButton(3, DrTechDroneNodes.INVENTORY_ITEM_COUNT, 47, 98, syncManager))
                .child(pagedSmallNodeButton(3, DrTechDroneNodes.REDSTONE_STRENGTH, 5, 113, syncManager))
                .child(pagedSmallNodeButton(3, DrTechDroneNodes.LIGHT_LEVEL, 47, 113, syncManager))
                .child(pagedSmallNodeButton(3, DrTechDroneNodes.LAST_ACTION_STATUS, 5, 128, syncManager))
                .child(pagedSmallNodeButton(3, DrTechDroneNodes.LAST_ACTION_ERROR, 47, 128, syncManager))
                .child(pagedSmallNodeButton(3, DrTechDroneNodes.COMPARE_ACTION_STATUS, 5, 143, syncManager))
                .child(pagedSmallNodeButton(3, DrTechDroneNodes.BLOCK_MATCHES, 47, 143, syncManager))
                .child(pagedSmallNodeButton(3, DrTechDroneNodes.COORDINATE_REACHABLE, 5, 158, syncManager))
                .child(pagedSmallNodeButton(3, DrTechDroneNodes.DOCK_AVAILABLE, 47, 158, syncManager))
                .child(pagedSmallNodeButton(3, DrTechDroneNodes.AREA_BLOCK_COUNT, 5, 173, syncManager))
                .child(pagedSmallNodeButton(3, DrTechDroneNodes.REDSTONE_OUTPUT_LEVEL, 47, 173, syncManager))
                .child(pagedSmallNodeButton(3, DrTechDroneNodes.TARGET_ENERGY, 5, 188, syncManager))
                .child(pagedSmallNodeButton(3, DrTechDroneNodes.TARGET_ENERGY_CAPACITY, 47, 188, syncManager))
                .child(pagedSmallNodeButton(3, DrTechDroneNodes.TARGET_ENERGY_PERCENT, 5, 203, syncManager))
                .child(new ButtonWidget<>().pos(5, 225).size(23, 16).overlay(IKey.str("<"))
                        .onMousePressed(mouse -> { clientLibraryPage = (clientLibraryPage + 5) % 6; return true; }))
                .child(IKey.lang("drtech.drone.programmer.page.flow").asWidget().pos(31, 229).size(29, 10)
                        .setEnabledIf(widget -> clientLibraryPage == 0))
                .child(IKey.lang("drtech.drone.programmer.page.world").asWidget().pos(31, 229).size(29, 10)
                        .setEnabledIf(widget -> clientLibraryPage == 1))
                .child(IKey.lang("drtech.drone.programmer.page.data").asWidget().pos(31, 229).size(29, 10)
                        .setEnabledIf(widget -> clientLibraryPage == 2))
                .child(IKey.lang("drtech.drone.programmer.page.sensors").asWidget().pos(31, 229).size(29, 10)
                        .setEnabledIf(widget -> clientLibraryPage == 3))
                .child(IKey.lang("drtech.drone.programmer.page.fluids").asWidget().pos(31, 229).size(29, 10)
                        .setEnabledIf(widget -> clientLibraryPage == 4))
                .child(IKey.lang("drtech.drone.programmer.page.areas").asWidget().pos(31, 229).size(29, 10)
                        .setEnabledIf(widget -> clientLibraryPage == 5))
                .child(new ButtonWidget<>().pos(63, 225).size(23, 16).overlay(IKey.str(">"))
                        .onMousePressed(mouse -> { clientLibraryPage = (clientLibraryPage + 1) % 6; return true; }))
                .child(new ButtonWidget<>().pos(91, 250).size(37, 16).overlay(IKey.lang("drtech.drone.programmer.delete"))
                        .onMousePressed(mouse -> { canvas.deleteSelected(); return true; }))
                .child(new ButtonWidget<>().pos(131, 250).size(37, 16).overlay(IKey.lang("drtech.drone.programmer.reset"))
                        .onMousePressed(mouse -> { canvas.resetView(); return true; }))
                .child(new ButtonWidget<>().pos(171, 250).size(37, 16).overlay(IKey.lang("drtech.drone.programmer.undo"))
                        .onMousePressed(mouse -> { sendHistoryCommand(syncManager, true); return true; }))
                .child(new ButtonWidget<>().pos(211, 250).size(37, 16).overlay(IKey.lang("drtech.drone.programmer.redo"))
                        .onMousePressed(mouse -> { sendHistoryCommand(syncManager, false); return true; }))
                .child(new ButtonWidget<>().pos(251, 250).size(37, 16).overlay(IKey.lang("drtech.drone.programmer.copy"))
                        .onMousePressed(mouse -> { canvas.copySelected(); return true; }))
                .child(new ButtonWidget<>().pos(291, 250).size(37, 16).overlay(IKey.lang("drtech.drone.programmer.paste"))
                        .setEnabledIf(widget -> canvas.hasCopiedNode())
                        .onMousePressed(mouse -> { canvas.pasteCopiedNode(); return true; }))
                .child(new ButtonWidget<>().pos(331, 250).size(41, 16).overlay(IKey.lang("drtech.drone.programmer.fit_all"))
                        .onMousePressed(mouse -> { canvas.fitAll(); return true; }))
                .child(new ButtonWidget<>().pos(379, 24).size(65, 16)
                        .overlay(IKey.dynamic(() -> I18n.format(clientInspectorPage == 0
                                ? "drtech.drone.programmer.inspector.active"
                                : "drtech.drone.programmer.inspector")))
                        .onMousePressed(mouse -> { clientInspectorPage = 0; return true; }))
                .child(new ButtonWidget<>().pos(447, 24).size(65, 16)
                        .overlay(IKey.dynamic(() -> I18n.format(clientInspectorPage == 1
                                ? "drtech.drone.programmer.remote.active"
                                : "drtech.drone.programmer.remote")))
                        .onMousePressed(mouse -> { clientInspectorPage = 1; return true; }))
                .child(IKey.dynamic(canvas::getSelectedDescription).asWidget().pos(379, 44).size(133, 25)
                        .setEnabledIf(widget -> clientInspectorPage == 0))
                .child(IKey.dynamic(canvas::getSelectedPropertySummary).asWidget().pos(379, 72).size(134, 24)
                        .setEnabledIf(widget -> clientInspectorPage == 0))
                .child(actionButton("<", 379, 99, canvas::selectPreviousProperty)
                        .setEnabledIf(widget -> clientInspectorPage == 0))
                .child(actionButton(">", 416, 99, canvas::selectNextProperty)
                        .setEnabledIf(widget -> clientInspectorPage == 0))
                .child(actionButton("-", 379, 118, () -> canvas.adjustSelectedProperty(-1))
                        .setEnabledIf(widget -> clientInspectorPage == 0 && canvas.isSelectedPropertyNumeric()))
                .child(actionButton("+", 416, 118, () -> canvas.adjustSelectedProperty(1))
                        .setEnabledIf(widget -> clientInspectorPage == 0 && canvas.isSelectedPropertyNumeric()))
                .child(new ButtonWidget<>().pos(379, 137).size(35, 16)
                        .overlay(IKey.dynamic(canvas::getSelectedPropertyActionLabel))
                        .setEnabledIf(widget -> clientInspectorPage == 0 && !canvas.isSelectedFluidSelector()
                                && canvas.canActivateSelectedProperty())
                        .onMousePressed(mouse -> { canvas.activateSelectedProperty(); return true; }))
                .child(actionButton(IKey.lang("drtech.drone.programmer.clear"), 416, 137,
                        canvas::clearSelectedProperty)
                        .setEnabledIf(widget -> clientInspectorPage == 0 && !canvas.isSelectedFluidSelector()))
                .child(new TextFieldWidget().pos(379, 156).size(134, 16).setMaxLength(128)
                        .setEnabledIf(widget -> clientInspectorPage == 0
                                && canvas.isSelectedPropertyTextEditable() && !canvas.isSelectedFluidSelector())
                        .value(new StringValue.Dynamic(canvas::getSelectedPropertyInputText,
                                canvas::setSelectedPropertyInputText)))
                .child(actionButton(IKey.lang("drtech.drone.programmer.filter_mode"), 379, 175,
                        canvas::toggleSelectedItemFilterMode)
                        .setEnabledIf(widget -> clientInspectorPage == 0 && canvas.isSelectedItemFilter()))
                .child(actionButton(IKey.lang("drtech.drone.programmer.remove_rule"), 416, 175,
                        canvas::removeLastSelectedItemFilterRule)
                        .setEnabledIf(widget -> clientInspectorPage == 0 && canvas.isSelectedItemFilter()))
                .child(IKey.lang("drtech.drone.programmer.fluid_search").asWidget().pos(379, 122).size(24, 10)
                        .setEnabledIf(widget -> clientInspectorPage == 0 && canvas.isSelectedFluidSelector()))
                .child(new TextFieldWidget().pos(404, 118).size(109, 16).setMaxLength(64)
                        .setEnabledIf(widget -> clientInspectorPage == 0 && canvas.isSelectedFluidSelector())
                        .value(new StringValue.Dynamic(this::getClientFluidSearch, this::setClientFluidSearch)))
                .child(new ButtonWidget<>().pos(379, 137).size(134, 16)
                        .overlay(IKey.dynamic(this::getClientFluidResultLabel))
                        .tooltipStatic(tooltip -> tooltip.addLine(IKey.dynamic(this::getClientFluidResultTooltip)))
                        .setEnabledIf(widget -> clientInspectorPage == 0 && canvas.isSelectedFluidSelector())
                        .onMousePressed(mouse -> { selectClientFluidResult(canvas); return true; }))
                .child(actionButton("<", 379, 156, () -> moveClientFluidResult(-1))
                        .setEnabledIf(widget -> clientInspectorPage == 0 && canvas.isSelectedFluidSelector()
                                && hasClientFluidResult()))
                .child(actionButton(">", 416, 156, () -> moveClientFluidResult(1))
                        .setEnabledIf(widget -> clientInspectorPage == 0 && canvas.isSelectedFluidSelector()
                                && hasClientFluidResult()))
                .child(IKey.dynamic(this::getClientFluidResultPage).asWidget().pos(454, 160).size(59, 10)
                        .setEnabledIf(widget -> clientInspectorPage == 0 && canvas.isSelectedFluidSelector()))
                .child(new ButtonWidget<>().pos(379, 175).size(72, 16)
                        .overlay(IKey.lang("drtech.drone.programmer.held_fluid"))
                        .setEnabledIf(widget -> clientInspectorPage == 0 && canvas.isSelectedFluidSelector())
                        .onMousePressed(mouse -> { canvas.captureHeldFluidProperty(); return true; }))
                .child(new ButtonWidget<>().pos(453, 175).size(60, 16)
                        .overlay(IKey.lang("drtech.drone.programmer.clear"))
                        .setEnabledIf(widget -> clientInspectorPage == 0 && canvas.isSelectedFluidSelector())
                        .onMousePressed(mouse -> { canvas.clearSelectedProperty(); return true; }))
                .child(IKey.lang("drtech.drone.programmer.fluid_selector_help").asWidget()
                        .pos(379, 195).size(134, 45)
                        .setEnabledIf(widget -> clientInspectorPage == 0 && canvas.isSelectedFluidSelector()))
                .child(IKey.lang("drtech.drone.programmer.node_label").asWidget().pos(379, 195)
                        .setEnabledIf(widget -> clientInspectorPage == 0 && !canvas.isSelectedFluidSelector()))
                .child(new TextFieldWidget().pos(379, 206).size(134, 16).setMaxLength(32)
                        .setEnabledIf(widget -> clientInspectorPage == 0 && !canvas.isSelectedFluidSelector())
                        .value(new StringValue.Dynamic(canvas::getSelectedNodeLabel, canvas::setSelectedNodeLabel)))
                .child(new ButtonWidget<>().pos(379, 225).size(134, 16)
                        .overlay(IKey.lang("drtech.drone.programmer.breakpoint"))
                        .setEnabledIf(widget -> clientInspectorPage == 0 && !canvas.isSelectedFluidSelector())
                        .onMousePressed(mouse -> { canvas.toggleSelectedBreakpoint(); return true; }))
                .child(IKey.dynamic(this::getDiagnosticsText).asWidget().pos(379, 244).size(134, 22)
                        .setEnabledIf(widget -> clientInspectorPage == 0 && !canvas.isSelectedFluidSelector()))
                .child(IKey.dynamic(this::getRemoteDebugText).asWidget().pos(379, 44).size(134, 197)
                        .setEnabledIf(widget -> clientInspectorPage == 1))
                .child(remoteButton(IKey.lang("drtech.drone.controller.pause"), "PAUSE", 379, 250, syncManager)
                        .setEnabledIf(widget -> clientInspectorPage == 1))
                .child(remoteButton(IKey.lang("drtech.drone.controller.resume"), "RESUME", 404, 250, syncManager)
                        .setEnabledIf(widget -> clientInspectorPage == 1))
                .child(remoteButton(IKey.lang("drtech.drone.controller.step"), "STEP", 429, 250, syncManager)
                        .setEnabledIf(widget -> clientInspectorPage == 1))
                .child(SlotGroupWidget.playerInventory(false).left(7).bottom(7))
                .child(canvas);
    }

    private String getClientFluidSearch() {
        return clientFluidSearch;
    }

    private void setClientFluidSearch(String value) {
        clientFluidSearch = value == null ? "" : value;
        clientFluidResultIndex = 0;
        clientFluidCacheQuery = null;
    }

    private List<String> getClientFluidResults() {
        String query = clientFluidSearch == null ? ""
                : clientFluidSearch.trim().toLowerCase(Locale.ROOT);
        if (query.equals(clientFluidCacheQuery)) return clientFluidResults;
        List<String> matches = new ArrayList<>();
        for (String fluidName : FluidRegistry.getRegisteredFluids().keySet()) {
            Fluid fluid = FluidRegistry.getFluid(fluidName);
            String localized = getLocalizedFluidName(fluid).toLowerCase(Locale.ROOT);
            if (query.isEmpty() || fluidName.toLowerCase(Locale.ROOT).contains(query)
                    || localized.contains(query)) {
                matches.add(fluidName);
            }
        }
        matches.sort(String.CASE_INSENSITIVE_ORDER);
        clientFluidCacheQuery = query;
        clientFluidResults = Collections.unmodifiableList(matches);
        if (matches.isEmpty()) clientFluidResultIndex = 0;
        else clientFluidResultIndex = Math.floorMod(clientFluidResultIndex, matches.size());
        return clientFluidResults;
    }

    private boolean hasClientFluidResult() {
        return !getClientFluidResults().isEmpty();
    }

    private String getClientFluidResult() {
        List<String> results = getClientFluidResults();
        return results.isEmpty() ? "" : results.get(Math.floorMod(clientFluidResultIndex, results.size()));
    }

    private String getClientFluidResultLabel() {
        String fluidName = getClientFluidResult();
        if (fluidName.isEmpty()) return I18n.format("drtech.drone.programmer.fluid_no_results");
        String display = fluidName;
        int namespace = display.indexOf(':');
        if (namespace >= 0 && namespace + 1 < display.length()) display = display.substring(namespace + 1);
        if (display.length() > 19) display = display.substring(0, 18) + "…";
        return display;
    }

    private String getClientFluidResultTooltip() {
        String fluidName = getClientFluidResult();
        if (fluidName.isEmpty()) return I18n.format("drtech.drone.programmer.fluid_no_results");
        Fluid fluid = FluidRegistry.getFluid(fluidName);
        return getLocalizedFluidName(fluid) + " | " + fluidName;
    }

    private String getClientFluidResultPage() {
        List<String> results = getClientFluidResults();
        return results.isEmpty() ? "0/0" : (clientFluidResultIndex + 1) + "/" + results.size();
    }

    private void moveClientFluidResult(int delta) {
        List<String> results = getClientFluidResults();
        if (!results.isEmpty()) clientFluidResultIndex = Math.floorMod(clientFluidResultIndex + delta, results.size());
    }

    private void selectClientFluidResult(DroneProgramCanvasWidget canvas) {
        String fluidName = getClientFluidResult();
        if (!fluidName.isEmpty()) canvas.selectFluidProperty(fluidName);
    }

    private static String getLocalizedFluidName(Fluid fluid) {
        if (fluid == null) return "";
        try {
            return new FluidStack(fluid, 1).getLocalizedName();
        } catch (RuntimeException ignored) {
            return fluid.getName();
        }
    }

    private ButtonWidget<?> nodeButton(ResourceLocation nodeType, int x, int y,
            PanelSyncManager syncManager) {
        return new ButtonWidget<>().pos(x, y).size(81, 14)
                .overlay(compactNodeLabel(nodeType))
                .tooltipStatic(tooltip -> tooltip
                        .addLine(IKey.lang("drtech.drone.node." + nodeType.getPath()))
                        .addLine(IKey.lang("drtech.drone.programmer.add_node_hint")))
                .onMousePressed(mouse -> {
                    DroneProgramGraph graph = getClientGraph();
                    if (graph != null && clientEditable) {
                        int offset = graph.getNodes().size() * 12;
                        sendEditCommand(syncManager, DroneGraphEditCommand.addNode(graph.getRevision(), UUID.randomUUID(),
                                nodeType, 40 + offset, 40 + offset, defaultConfiguration(nodeType)));
                    }
                    return true;
                });
    }

    private ButtonWidget<?> pagedNodeButton(int page, ResourceLocation nodeType, int x, int y,
            PanelSyncManager syncManager) {
        return nodeButton(nodeType, x, y, syncManager)
                .setEnabledIf(widget -> clientLibraryPage == page && matchesNodeSearch(nodeType));
    }

    private ButtonWidget<?> smallNodeButton(ResourceLocation nodeType, int x, int y,
            PanelSyncManager syncManager) {
        return new ButtonWidget<>().pos(x, y).size(39, 14)
                .overlay(compactNodeLabel(nodeType))
                .tooltipStatic(tooltip -> tooltip
                        .addLine(IKey.lang("drtech.drone.node." + nodeType.getPath()))
                        .addLine(IKey.lang("drtech.drone.programmer.add_node_hint")))
                .onMousePressed(mouse -> {
                    DroneProgramGraph graph = getClientGraph();
                    if (graph != null && clientEditable) {
                        int offset = graph.getNodes().size() * 12;
                        sendEditCommand(syncManager, DroneGraphEditCommand.addNode(graph.getRevision(), UUID.randomUUID(),
                                nodeType, 40 + offset, 40 + offset, defaultConfiguration(nodeType)));
                    }
                    return true;
                });
    }

    private ButtonWidget<?> pagedSmallNodeButton(int page, ResourceLocation nodeType, int x, int y,
            PanelSyncManager syncManager) {
        return smallNodeButton(nodeType, x, y, syncManager)
                .setEnabledIf(widget -> clientLibraryPage == page && matchesNodeSearch(nodeType));
    }

    private boolean matchesNodeSearch(ResourceLocation nodeType) {
        String query = clientNodeSearch == null ? "" : clientNodeSearch.trim().toLowerCase(java.util.Locale.ROOT);
        if (query.isEmpty()) return true;
        String label = I18n.format("drtech.drone.node." + nodeType.getPath()).toLowerCase(java.util.Locale.ROOT);
        return nodeType.getPath().contains(query) || label.contains(query);
    }

    /** Keeps the two-column library readable while node cards retain their full localized titles. */
    private static IKey compactNodeLabel(ResourceLocation nodeType) {
        return IKey.dynamic(() -> {
            String compactKey = "drtech.drone.node.short." + nodeType.getPath();
            String compact = I18n.format(compactKey);
            return compactKey.equals(compact)
                    ? I18n.format("drtech.drone.node." + nodeType.getPath())
                    : compact;
        });
    }

    private ButtonWidget<?> actionButton(String label, int x, int y, Runnable action) {
        return actionButton(IKey.str(label), x, y, action);
    }

    private ButtonWidget<?> actionButton(IKey label, int x, int y, Runnable action) {
        return new ButtonWidget<>().pos(x, y).size(35, 16).overlay(label)
                .onMousePressed(mouse -> { action.run(); return true; });
    }

    private ButtonWidget<?> remoteButton(String label, String command, int x, int y,
            PanelSyncManager syncManager) {
        return remoteButton(IKey.str(label), command, x, y, syncManager);
    }

    private ButtonWidget<?> remoteButton(IKey label, String command, int x, int y,
            PanelSyncManager syncManager) {
        return new ButtonWidget<>().pos(x, y).size(23, 16).overlay(label)
                .onMousePressed(mouse -> {
                    if (clientRemoteConnected) {
                        syncManager.callSyncedAction(REMOTE_CONTROL_ACTION, packet -> packet.writeString(command));
                    }
                    return true;
                });
    }

    private static NBTTagCompound defaultConfiguration(ResourceLocation nodeType) {
        NBTTagCompound configuration = new NBTTagCompound();
        if (nodeType.equals(DrTechDroneNodes.WAIT)) configuration.setInteger("Ticks", 20);
        if (nodeType.equals(DrTechDroneNodes.CHARGE_UNTIL)) configuration.setDouble("Percent", 100.0D);
        if (nodeType.equals(DrTechDroneNodes.CONFIGURE_SAFETY)) {
            configuration.setInteger("ReturnPercent", 20);
            configuration.setInteger("ResumePercent", 90);
        }
        if (nodeType.equals(DrTechDroneNodes.IMPORT_EU) || nodeType.equals(DrTechDroneNodes.EXPORT_EU)) {
            configuration.setInteger("MaxEU", 512);
        }
        if (nodeType.equals(DrTechDroneNodes.CHARGE_TARGET_PERCENT)) {
            configuration.setInteger("Percent", 100);
            configuration.setInteger("MaxEU", 512);
        }
        if (nodeType.equals(DrTechDroneNodes.IMPORT_ITEMS) || nodeType.equals(DrTechDroneNodes.EXPORT_ITEMS)) {
            configuration.setString("Direction", "AUTO");
            configuration.setInteger("MaxAmount", 64);
            configuration.setInteger("BatchSize", 64);
            configuration.setString("SearchMode", "NEAREST");
            configuration.setBoolean("SkipUnavailable", true);
        }
        if (nodeType.equals(DrTechDroneNodes.IMPORT_FLUID)
                || nodeType.equals(DrTechDroneNodes.EXPORT_FLUID)) {
            configuration.setString("Direction", "AUTO");
            configuration.setInteger("MaxAmount", 1_000);
        }
        if (nodeType.equals(DrTechDroneNodes.DRAIN_FLUID)) configuration.setInteger("MaxAmount", 1_000);
        if (nodeType.equals(DrTechDroneNodes.FLUID_FILTER)) {
            configuration.setString("Fluid", "");
            configuration.setString("Mode", "WHITELIST");
        }
        if (nodeType.equals(DrTechDroneNodes.CONTAINER_FLUID_AMOUNT)) {
            configuration.setString("Direction", "AUTO");
        }
        if (nodeType.equals(DrTechDroneNodes.FIND_FLUID_CONTAINER)) {
            configuration.setString("Direction", "AUTO");
            configuration.setInteger("MinimumAmount", 1_000);
        }
        if (nodeType.equals(DrTechDroneNodes.WAIT_FOR_FLUID_AMOUNT)) {
            configuration.setString("Direction", "AUTO");
            configuration.setInteger("Amount", 1_000);
            configuration.setString("Operator", "AT_LEAST");
        }
        if (nodeType.equals(DrTechDroneNodes.AREA)) {
            configuration.setInteger("X2", 2);
            configuration.setInteger("Z2", 2);
        }
        if (nodeType.equals(DrTechDroneNodes.REPEAT)) configuration.setInteger("Count", 3);
        if (nodeType.equals(DrTechDroneNodes.WAIT_FOR_OWNER)) configuration.setDouble("Radius", 16.0D);
        if (nodeType.equals(DrTechDroneNodes.NUMBER_MATH)) configuration.setString("Operator", "+");
        if (nodeType.equals(DrTechDroneNodes.BOOLEAN_LOGIC)) configuration.setString("Operator", "AND");
        if (nodeType.equals(DrTechDroneNodes.COMPARE_NUMBER)) configuration.setString("Operator", "==");
        if (nodeType.equals(DrTechDroneNodes.INVENTORY_ITEM_COUNT)) configuration.setString("Direction", "AUTO");
        if (nodeType.equals(DrTechDroneNodes.INTERACT_BLOCK)
                || nodeType.equals(DrTechDroneNodes.USE_ITEM_ON_BLOCK)) configuration.setString("Direction", "AUTO");
        if (nodeType.equals(DrTechDroneNodes.LIGHT_LEVEL)) configuration.setString("LightType", "MAX");
        if (nodeType.equals(DrTechDroneNodes.COMPARE_ACTION_STATUS)) configuration.setString("Status", "SUCCESS");
        if (nodeType.equals(DrTechDroneNodes.PICKUP_DROPPED_ITEMS)) {
            configuration.setDouble("Radius", 4.0D);
            configuration.setInteger("MaxAmount", 64);
        }
        if (nodeType.equals(DrTechDroneNodes.DROP_ITEMS)) configuration.setInteger("MaxAmount", 64);
        if (nodeType.equals(DrTechDroneNodes.AREA_BLOCK_COUNT)) configuration.setInteger("Limit", 4_096);
        if (nodeType.equals(DrTechDroneNodes.SET_REDSTONE_OUTPUT)) configuration.setInteger("Strength", 15);
        if (nodeType.equals(DrTechDroneNodes.SPHERE_AREA)) configuration.setInteger("Radius", 4);
        if (nodeType.equals(DrTechDroneNodes.CYLINDER_AREA)) {
            configuration.setInteger("Radius", 3);
            configuration.setInteger("Height", 4);
        }
        if (nodeType.equals(DrTechDroneNodes.PATH_AREA)) configuration.setInteger("Radius", 0);
        if (nodeType.equals(DrTechDroneNodes.GET_NUMBER_VARIABLE)
                || nodeType.equals(DrTechDroneNodes.SET_NUMBER_VARIABLE)
                || nodeType.equals(DrTechDroneNodes.ADD_NUMBER_VARIABLE)) configuration.setString("Name", "value");
        return configuration;
    }

    private void sendEditCommand(PanelSyncManager syncManager, DroneGraphEditCommand command) {
        syncManager.callSyncedAction(EDIT_ACTION, packet -> packet.writeCompoundTag(DroneGraphCommandCodec.write(command)));
    }

    private void sendHistoryCommand(PanelSyncManager syncManager, boolean undo) {
        DroneProgramGraph graph = getClientGraph();
        if (graph == null || !clientEditable) return;
        sendEditCommand(syncManager, undo ? DroneGraphEditCommand.undo(graph.getRevision())
                : DroneGraphEditCommand.redo(graph.getRevision()));
    }

    private void sendProgramRename(PanelSyncManager syncManager) {
        DroneProgramGraph graph = getClientGraph();
        if (graph == null || !clientEditable || !clientProgramNameDirty) return;
        sendEditCommand(syncManager, DroneGraphEditCommand.rename(graph.getRevision(), clientProgramName.trim()));
        clientProgramNameDirty = false;
    }

    private void receiveEditAction(EntityPlayer player, PacketBuffer packet) {
        if (getWorld() == null || getWorld().isRemote || player.getDistanceSq(getPos()) > MAX_EDIT_DISTANCE_SQUARED) {
            return;
        }
        refreshCardSession();
        if (editorOwner == null || !editorOwner.equals(player.getUniqueID())) {
            serverStatus = "Read-only: another player owns the editor lock";
            return;
        }
        if (editSession == null) {
            serverStatus = "Insert a valid program card";
            return;
        }
        try {
            NBTTagCompound commandTag = packet.readCompoundTag();
            DroneGraphEditResult result = editSession.apply(DroneGraphCommandCodec.read(commandTag));
            if (result.isAccepted()) {
                saveSessionToCard();
                serverStatus = result.getDiagnostics().isEmpty() ? "Saved"
                        : "Saved with " + countErrors(result) + " error(s)";
            } else {
                serverStatus = result.getStatus().name() + ": " + result.getMessage();
            }
            markDirty();
        } catch (Exception exception) {
            serverStatus = "INVALID_COMMAND: " + exception.getMessage();
        }
    }

    private void receiveRemoteControl(EntityPlayer player, String command) {
        if (getWorld() == null || getWorld().isRemote
                || player.getDistanceSq(getPos()) > MAX_EDIT_DISTANCE_SQUARED) return;
        refreshCardSession();
        if (editorOwner == null || !editorOwner.equals(player.getUniqueID())) {
            serverStatus = "Remote denied: editor lock required";
            return;
        }
        EntityProgrammableDrone drone = findRemoteDrone(player);
        if (drone == null) {
            serverStatus = "Remote disconnected";
            return;
        }
        if (drone.handleRemoteControl(player, command)) {
            serverStatus = "Remote command: " + command;
        } else {
            NBTTagCompound snapshot = drone.createRemoteDebugSnapshot();
            serverStatus = snapshot.getBoolean("ProgramSuspended")
                    ? "Remote blocked by safety: " + snapshot.getString("SafetyState")
                    : "Remote command rejected for current runtime state";
        }
    }

    private void writeProgramToDrone(EntityPlayer player) {
        if (getWorld() == null || getWorld().isRemote || player.getDistanceSq(getPos()) > MAX_EDIT_DISTANCE_SQUARED) return;
        refreshCardSession();
        if (editorOwner == null || !editorOwner.equals(player.getUniqueID())) {
            serverStatus = "Read-only: another player owns the editor lock";
            return;
        }
        if (editSession == null || editSession.getLastCompileResult().hasErrors()) {
            serverStatus = "Cannot write: fix all compile errors first";
            return;
        }
        DroneExecutorRegistry executors = DrTechDroneExecutors.createDefaultRegistry();
        DroneValueEvaluatorRegistry evaluators = DrTechDroneValueEvaluators.createDefaultRegistry();
        for (DroneProgramNode node : editSession.getGraph().getNodes()) {
            boolean builtIn = node.getType().equals(DrTechDroneNodes.START) || node.getType().equals(DrTechDroneNodes.END);
            if (!builtIn && executors.get(node.getType()) == null && evaluators.get(node.getType()) == null) {
                serverStatus = "Cannot write: node has no runtime implementation: " + node.getType().getPath();
                return;
            }
        }
        ItemStack drone = importItems.getStackInSlot(1);
        if (drone.isEmpty() || !(drone.getItem() instanceof ItemProgrammableDrone)) {
            serverStatus = "Insert a programmable drone in the second slot";
            return;
        }
        if (energyContainer.getEnergyStored() < PROGRAM_WRITE_EU) {
            serverStatus = "Cannot write: requires " + PROGRAM_WRITE_EU + " EU";
            return;
        }
        energyContainer.removeEnergy(PROGRAM_WRITE_EU);
        DroneItemData.setProgram(drone, DroneProgramNbtCodec.write(editSession.getGraph()));
        serverStatus = "Program written to drone";
        markDirty();
    }

    private int countErrors(DroneGraphEditResult result) {
        return (int) result.getDiagnostics().stream()
                .filter(diagnostic -> diagnostic.getSeverity() == DroneDiagnosticSeverity.ERROR).count();
    }

    private void onEditorOpened(EntityPlayer player) {
        if (getWorld() == null || getWorld().isRemote) return;
        if (editorOwner == null) {
            editorOwner = player.getUniqueID();
            serverStatus = editSession == null ? "Insert a program card" : "Editor lock acquired";
        }
    }

    private void onEditorClosed(EntityPlayer player) {
        if (getWorld() != null && !getWorld().isRemote && player.getUniqueID().equals(editorOwner)) {
            editorOwner = null;
            serverStatus = editSession == null ? "Insert a program card" : "Ready";
        }
    }

    private void refreshCardSession() {
        ItemStack stack = importItems.getStackInSlot(0);
        int fingerprint = fingerprint(stack);
        if (stack == loadedCardReference && fingerprint == loadedCardFingerprint) return;
        loadedCardReference = stack;
        loadedCardFingerprint = fingerprint;
        editSession = null;
        if (stack.isEmpty() || !(stack.getItem() instanceof ItemDroneProgramCard card)) {
            serverStatus = "Insert a program card";
            return;
        }
        try {
            DroneProgramGraph graph = card.readProgram(stack).orElseGet(MetaTileEntityDroneProgrammer::createDefaultGraph);
            editSession = new DroneProgramEditSession(graph, DrTechDroneNodes.createDefaultRegistry());
            if (!card.hasProgram(stack)) saveSessionToCard();
            serverStatus = "Ready";
        } catch (DroneProgramFormatException exception) {
            serverStatus = "Invalid program card: " + exception.getMessage();
        }
    }

    private void saveSessionToCard() {
        ItemStack stack = importItems.getStackInSlot(0);
        if (editSession == null || stack.isEmpty() || !(stack.getItem() instanceof ItemDroneProgramCard card)) return;
        card.writeProgram(stack, editSession.getGraph());
        loadedCardFingerprint = fingerprint(stack);
        markDirty();
    }

    private NBTTagCompound createEditorState(EntityPlayer viewer) {
        refreshCardSession();
        NBTTagCompound state = new NBTTagCompound();
        state.setBoolean("Editable", editorOwner != null && editorOwner.equals(viewer.getUniqueID()));
        state.setString("Status", serverStatus);
        if (editSession == null) return state;
        state.setTag("Program", DroneProgramNbtCodec.write(editSession.getGraph()));
        NBTTagList diagnostics = new NBTTagList();
        for (DroneProgramDiagnostic diagnostic : editSession.getLastCompileResult().getDiagnostics()) {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setString("Severity", diagnostic.getSeverity().name());
            tag.setString("Code", diagnostic.getCode().name());
            if (diagnostic.getNodeId() != null) tag.setString("Node", diagnostic.getNodeId().toString());
            if (diagnostic.getPortId() != null) tag.setString("Port", diagnostic.getPortId());
            diagnostics.appendTag(tag);
        }
        state.setTag("Diagnostics", diagnostics);
        NBTTagCompound runtime = DroneItemData.getRuntime(importItems.getStackInSlot(1));
        if (runtime != null) state.setTag("Runtime", runtime);
        EntityProgrammableDrone remote = findRemoteDrone(viewer);
        if (remote != null) state.setTag("Remote", remote.createRemoteDebugSnapshot());
        return state;
    }

    private void receiveEditorState(NBTTagCompound state) {
        clientEditable = state.getBoolean("Editable");
        clientStatus = localizeEditorStatus(state.getString("Status"));
        clientErrors = 0;
        clientWarnings = 0;
        clientDiagnosticLines.clear();
        clientDiagnosticNodeIds.clear();
        clientActiveNodeId = null;
        clientRuntimeStatus = "NOT RUN";
        clientRemoteConnected = false;
        clientRemoteInfo = I18n.format("drtech.drone.remote.disconnected");
        NBTTagList diagnostics = state.getTagList("Diagnostics", 10);
        for (int i = 0; i < diagnostics.tagCount(); i++) {
            String severity = diagnostics.getCompoundTagAt(i).getString("Severity");
            if (DroneDiagnosticSeverity.ERROR.name().equals(severity)) clientErrors++;
            if (DroneDiagnosticSeverity.WARNING.name().equals(severity)) clientWarnings++;
            NBTTagCompound diagnostic = diagnostics.getCompoundTagAt(i);
            String node = diagnostic.getString("Node");
            String port = diagnostic.getString("Port");
            String localizedSeverity = localizeDiagnosticSeverity(severity);
            String localizedCode = localizeDiagnosticCode(diagnostic.getString("Code"));
            String localizedPort = port.isEmpty() ? "" : " [" + localizePort(port) + "]";
            clientDiagnosticLines.add(localizedSeverity + "：" + localizedCode + localizedPort);
            if (!node.isEmpty()) {
                try {
                    clientDiagnosticNodeIds.add(UUID.fromString(node));
                } catch (IllegalArgumentException ignored) {}
            }
        }
        if (state.hasKey("Runtime", 10)) {
            NBTTagCompound runtime = state.getCompoundTag("Runtime");
            clientRuntimeStatus = runtime.getString("Status");
            try {
                clientActiveNodeId = UUID.fromString(runtime.getString("CurrentNode"));
            } catch (IllegalArgumentException ignored) {
                clientActiveNodeId = null;
            }
        }
        if (state.hasKey("Remote", 10)) {
            NBTTagCompound remote = state.getCompoundTag("Remote");
            clientRemoteConnected = true;
            String effectiveStatus = remote.hasKey("EffectiveStatus", 8)
                    ? remote.getString("EffectiveStatus") : remote.getString("Status");
            clientRuntimeStatus = localizeRuntimeStatus(effectiveStatus);
            String progress = remote.getString("Progress");
            String variables = remote.getString("Variables");
            if ("No variables".equals(variables)) variables = I18n.format("drtech.drone.remote.no_variables");
            String modules = remote.getString("Modules");
            if ("none".equals(modules)) modules = I18n.format("drtech.drone.remote.no_modules");
            String trace = remote.getString("Trace");
            if ("No execution trace".equals(trace)) trace = I18n.format("drtech.drone.remote.no_trace");
            StringBuilder info = new StringBuilder(remote.getString("Chassis"))
                    .append(" | EU ").append(remote.getInteger("EnergyPercent")).append("% | ")
                    .append(localizeRuntimeStatus(effectiveStatus));
            if (remote.getBoolean("ProgramSuspended")) {
                info.append('\n').append(I18n.format("drtech.drone.remote.safety",
                        localizeSafetyState(remote.getString("SafetyState"))));
            }
            info.append('\n').append(localizeNodePath(remote.getString("Node")))
                    .append(progress.isEmpty() ? "" : " " + progress)
                    .append('\n').append(variables)
                    .append('\n').append(I18n.format("drtech.drone.remote.modules", modules))
                    .append('\n').append(I18n.format("drtech.drone.remote.fluid",
                            remote.getString("FluidName").isEmpty() ? I18n.format("drtech.drone.ui.fluid_empty")
                                    : remote.getString("FluidName"),
                            remote.getInteger("FluidAmount"), remote.getInteger("FluidCapacity")))
                    .append('\n').append(trace);
            clientRemoteInfo = info.toString();
            try {
                clientActiveNodeId = UUID.fromString(remote.getString("CurrentNode"));
            } catch (IllegalArgumentException ignored) {
                clientActiveNodeId = null;
            }
        }
        if (!state.hasKey("Program", 10)) {
            clientGraph = null;
            return;
        }
        try {
            clientGraph = DroneProgramNbtCodec.read(state.getCompoundTag("Program"));
            if (!clientProgramNameDirty) clientProgramName = clientGraph.getName();
        } catch (DroneProgramFormatException exception) {
            clientGraph = null;
            clientStatus = I18n.format("drtech.drone.programmer.status.sync_error", exception.getMessage());
        }
    }

    private String getStatusLine() {
        long revision = clientGraph == null ? 0L : clientGraph.getRevision();
        String mode = I18n.format(clientEditable ? "drtech.drone.programmer.mode.edit"
                : "drtech.drone.programmer.mode.read_only");
        return I18n.format("drtech.drone.programmer.status_line", mode, revision, clientErrors, clientWarnings,
                localizeRuntimeStatus(clientRuntimeStatus), clientStatus);
    }

    private DroneProgramGraph getClientGraph() {
        return clientGraph;
    }

    private String getClientProgramName() {
        if (clientGraph == null && !clientProgramNameDirty) return "";
        return clientProgramName;
    }

    private void setClientProgramName(String value) {
        if (value == null || value.length() > DroneProgramNbtCodec.MAX_NAME_LENGTH) return;
        clientProgramName = value;
        clientProgramNameDirty = clientGraph != null && !value.trim().equals(clientGraph.getName());
    }

    private Set<UUID> getClientDiagnosticNodeIds() {
        return Collections.unmodifiableSet(clientDiagnosticNodeIds);
    }

    private String getDiagnosticsText() {
        if (clientDiagnosticLines.isEmpty()) return I18n.format("drtech.drone.diagnostic.none");
        StringBuilder builder = new StringBuilder();
        int limit = Math.min(3, clientDiagnosticLines.size());
        for (int i = 0; i < limit; i++) {
            if (i > 0) builder.append('\n');
            builder.append(clientDiagnosticLines.get(i));
        }
        if (clientDiagnosticLines.size() > limit) {
            builder.append('\n').append(I18n.format("drtech.drone.diagnostic.more",
                    clientDiagnosticLines.size() - limit));
        }
        return builder.toString();
    }

    private String getRemoteDebugText() {
        return clientRemoteInfo;
    }

    private static String localizeRuntimeStatus(String status) {
        String key = "drtech.drone.status." + status.toLowerCase(java.util.Locale.ROOT).replace(' ', '_');
        String localized = I18n.format(key);
        return key.equals(localized) ? status : localized;
    }

    private static String localizeSafetyState(String state) {
        String key = "drtech.drone.safety." + state.toLowerCase(java.util.Locale.ROOT);
        String localized = I18n.format(key);
        return key.equals(localized) ? state : localized;
    }

    private static String localizeNodePath(String node) {
        String path = node;
        int separator = node.indexOf(':');
        if (separator >= 0 && separator < node.length() - 1) path = node.substring(separator + 1);
        String key = "drtech.drone.node." + path;
        String localized = I18n.format(key);
        return key.equals(localized) ? node : localized;
    }

    private static String localizeDiagnosticSeverity(String severity) {
        return localizeOrFallback("drtech.drone.diagnostic.severity."
                + severity.toLowerCase(java.util.Locale.ROOT), severity);
    }

    private static String localizeDiagnosticCode(String code) {
        return localizeOrFallback("drtech.drone.diagnostic."
                + code.toLowerCase(java.util.Locale.ROOT), code);
    }

    private static String localizePort(String port) {
        return localizeOrFallback("drtech.drone.port." + port.toLowerCase(java.util.Locale.ROOT), port);
    }

    private static String localizeOrFallback(String key, String fallback) {
        String localized = I18n.format(key);
        return key.equals(localized) ? fallback : localized;
    }

    private static String localizeEditorStatus(String status) {
        if (status == null || status.isEmpty()) return "";
        switch (status) {
            case "Insert a program card": return I18n.format("drtech.drone.programmer.status.insert_card");
            case "Waiting for server": return I18n.format("drtech.drone.programmer.status.waiting_server");
            case "Read-only: another player owns the editor lock":
                return I18n.format("drtech.drone.programmer.status.editor_locked");
            case "Insert a valid program card": return I18n.format("drtech.drone.programmer.status.invalid_card");
            case "Saved": return I18n.format("drtech.drone.programmer.status.saved");
            case "Remote denied: editor lock required":
                return I18n.format("drtech.drone.programmer.status.remote_denied");
            case "Remote disconnected": return I18n.format("drtech.drone.programmer.status.remote_disconnected");
            case "Remote command rejected for current runtime state":
                return I18n.format("drtech.drone.programmer.status.remote_rejected");
            case "Cannot write: fix all compile errors first":
                return I18n.format("drtech.drone.programmer.status.fix_errors");
            case "Insert a programmable drone in the second slot":
                return I18n.format("drtech.drone.programmer.status.insert_drone");
            case "Program written to drone": return I18n.format("drtech.drone.programmer.status.written");
            case "Editor lock acquired": return I18n.format("drtech.drone.programmer.status.lock_acquired");
            case "Ready": return I18n.format("drtech.drone.programmer.status.ready");
            default:
                if (status.startsWith("Saved with ")) {
                    String count = status.substring("Saved with ".length()).split(" ", 2)[0];
                    return I18n.format("drtech.drone.programmer.status.saved_errors", count);
                }
                if (status.startsWith("Cannot write: requires ")) {
                    return I18n.format("drtech.drone.programmer.status.requires_eu",
                            status.substring("Cannot write: requires ".length()).replace(" EU", ""));
                }
                if (status.startsWith("Cannot write: node has no runtime implementation: ")) {
                    return I18n.format("drtech.drone.programmer.status.no_runtime",
                            status.substring("Cannot write: node has no runtime implementation: ".length()));
                }
                if (status.startsWith("Remote command: ")) {
                    return I18n.format("drtech.drone.programmer.status.remote_command",
                            status.substring("Remote command: ".length()));
                }
                if (status.startsWith("Remote blocked by safety: ")) {
                    return I18n.format("drtech.drone.programmer.status.remote_safety",
                            localizeSafetyState(status.substring("Remote blocked by safety: ".length())));
                }
                if (status.startsWith("Invalid program card: ")) {
                    return I18n.format("drtech.drone.programmer.status.invalid_card_detail",
                            status.substring("Invalid program card: ".length()));
                }
                return status;
        }
    }

    private EntityProgrammableDrone findRemoteDrone(EntityPlayer viewer) {
        if (getWorld() == null || getWorld().isRemote || editSession == null || viewer.world != getWorld()) return null;
        UUID programId = editSession.getGraph().getProgramId();
        long programRevision = editSession.getGraph().getRevision();
        AxisAlignedBB search = new AxisAlignedBB(getPos()).grow(MAX_REMOTE_DEBUG_RANGE);
        EntityProgrammableDrone selected = null;
        double selectedDistance = Double.MAX_VALUE;
        for (EntityProgrammableDrone drone : getWorld().getEntitiesWithinAABB(EntityProgrammableDrone.class, search)) {
            if (drone.isDead || !drone.isOwner(viewer) || !programId.equals(drone.getProgramId())
                    || programRevision != drone.getProgramRevision()) continue;
            double distance = drone.getDistanceSqToCenter(getPos());
            if (distance > (double) drone.getWirelessRange() * drone.getWirelessRange()) continue;
            if (distance < selectedDistance) {
                selected = drone;
                selectedDistance = distance;
            }
        }
        return selected;
    }

    private static DroneProgramGraph createDefaultGraph() {
        DroneProgramGraph graph = new DroneProgramGraph("New Drone Program");
        DroneProgramNode start = DroneProgramNode.create(DrTechDroneNodes.START, 20, 60);
        DroneProgramNode end = DroneProgramNode.create(DrTechDroneNodes.END, 190, 60);
        graph.addNode(start);
        graph.addNode(end);
        graph.addEdge(DroneProgramEdge.create(start.getId(), "next", end.getId(), "in"));
        return graph;
    }

    private static int fingerprint(ItemStack stack) {
        if (stack.isEmpty()) return 0;
        int result = 31 * Item.getIdFromItem(stack.getItem()) + stack.getMetadata();
        NBTTagCompound tag = stack.getTagCompound();
        return 31 * result + (tag == null ? 0 : tag.hashCode());
    }

    @Override
    protected boolean shouldSerializeInventories() {
        return true;
    }
}
