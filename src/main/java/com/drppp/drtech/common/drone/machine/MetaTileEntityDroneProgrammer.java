package com.drppp.drtech.common.drone.machine;

import com.drppp.drtech.common.drone.api.DroneExtensionRegistry;

import codechicken.lib.render.CCRenderState;
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
import com.cleanroommc.modularui.value.StringValue;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;
import com.cleanroommc.modularui.widgets.textfield.TextFieldWidget;
import com.drppp.drtech.client.drone.DroneProgramCanvasWidget;
import com.drppp.drtech.client.drone.DroneDiagnosticScrollWidget;
import com.drppp.drtech.client.drone.DronePropertyChoiceWidget;
import com.drppp.drtech.client.drone.DroneAreaPreviewWidget;
import com.drppp.drtech.client.drone.DroneWorldPreviewRenderer;
import com.drppp.drtech.client.drone.DroneWorldSelectionHandler;
import com.drppp.drtech.client.drone.DroneMultilineTextWidget;
import com.drppp.drtech.client.drone.DroneNodeLibraryButtonWidget;
import com.drppp.drtech.client.drone.DroneBilingualNodeSearch;
import com.drppp.drtech.common.drone.item.DroneItemData;
import com.drppp.drtech.common.drone.hardware.DroneUpgradeDataCodec;
import com.drppp.drtech.common.drone.hardware.DroneUpgradeType;
import com.drppp.drtech.common.drone.entity.EntityProgrammableDrone;
import com.drppp.drtech.common.drone.network.DroneDockNetwork;
import com.drppp.drtech.common.drone.network.DroneDockRecord;
import com.drppp.drtech.common.drone.network.DroneRegistry;
import com.drppp.drtech.common.drone.network.DroneRegistryRecord;
import com.drppp.drtech.common.drone.item.ItemDroneProgramCard;
import com.drppp.drtech.common.drone.item.ItemProgrammableDrone;
import com.drppp.drtech.common.drone.program.codec.DroneProgramFormatException;
import com.drppp.drtech.common.drone.program.codec.DroneProgramNbtCodec;
import com.drppp.drtech.common.drone.program.codec.DroneProgramTransferCodec;
import com.drppp.drtech.common.drone.program.compare.DroneProgramDiff;
import com.drppp.drtech.common.drone.program.compare.DroneProgramDiffResult;
import com.drppp.drtech.common.drone.program.compile.DroneDiagnosticSeverity;
import com.drppp.drtech.common.drone.program.compile.DroneCompileResult;
import com.drppp.drtech.common.drone.program.compile.DroneProgramDiagnostic;
import com.drppp.drtech.common.drone.program.compile.DroneProgramCompiler;
import com.drppp.drtech.common.drone.program.compile.DroneProgramHardwareValidator;
import com.drppp.drtech.common.drone.program.edit.DroneGraphCommandCodec;
import com.drppp.drtech.common.drone.program.edit.DroneGraphEditCommand;
import com.drppp.drtech.common.drone.program.edit.DroneGraphEditResult;
import com.drppp.drtech.common.drone.program.model.DroneProgramEdge;
import com.drppp.drtech.common.drone.program.model.DroneArea;
import com.drppp.drtech.common.drone.program.model.DroneProgramGraph;
import com.drppp.drtech.common.drone.program.model.DroneProgramNode;
import com.drppp.drtech.common.drone.program.model.DroneNodeDefinition;
import com.drppp.drtech.common.drone.program.library.DroneProgramLibrary;
import com.drppp.drtech.common.drone.program.library.DroneProgramLibraryRecord;
import com.drppp.drtech.common.drone.program.library.DroneProgramRevision;
import com.drppp.drtech.common.drone.program.library.DroneOfficialProgramTemplate;
import com.drppp.drtech.common.drone.program.library.DroneOfficialProgramTemplates;
import com.drppp.drtech.common.drone.program.registry.DrTechDroneNodes;
import com.drppp.drtech.common.drone.program.registry.DroneNodeLibraryIndex;
import com.drppp.drtech.common.drone.program.registry.DroneNodeRegistry;
import com.drppp.drtech.common.drone.program.edit.DroneProgramEditSession;
import com.drppp.drtech.common.drone.program.runtime.DroneExecutorRegistry;
import com.drppp.drtech.common.drone.program.runtime.DroneProgramRuntime;
import com.drppp.drtech.common.drone.program.runtime.DroneValueEvaluatorRegistry;
import gregtech.api.GTValues;
import gregtech.api.items.itemhandlers.GTItemStackHandler;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.TieredMetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.mui.GTGuis;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.oredict.OreDictionary;
import org.lwjgl.input.Keyboard;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.UUID;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/** Server-authoritative visual programmer with a single writer lock and optimistic revision checks. */
public final class MetaTileEntityDroneProgrammer extends TieredMetaTileEntity {

    public static final int MACHINE_TIER = GTValues.EV;
    private static final String EDIT_ACTION = "drone_graph_edit";
    private static final String WRITE_DRONE_ACTION = "drone_write_program";
    private static final String SAVE_AS_ACTION = "drone_save_program_copy";
    private static final String REMOTE_CONTROL_ACTION = "drone_remote_control";
    private static final String FLEET_CONTROL_ACTION = "drone_fleet_control";
    private static final String NODE_LIBRARY_ACTION = "drone_node_library";
    private static final String CREATE_TEMPLATE_ACTION = "drone_create_program_template";
    private static final String IMPORT_PROGRAM_ACTION = "drone_import_program";
    private static final String COMPARE_PROGRAM_ACTION = "drone_compare_program";
    private static final String WORLD_SELECTION_ACTION = "drone_world_selection";
    private static final int MAX_EDIT_DISTANCE_SQUARED = 64;
    private static final int MAX_REMOTE_DEBUG_RANGE = 512;
    private static final long PROGRAM_WRITE_EU = 8_192L;
    private static final DroneNodeRegistry NODE_LIBRARY = DroneExtensionRegistry.nodes();
    /** Fits the editor toolbar (ending at y=266) plus the 76px player inventory and 3px safety gap. */
    private static final int PROGRAMMER_PANEL_HEIGHT = 352;
    private static final int LIBRARY_PAGE_COUNT = 10;
    private static final int LOOPS_PAGE = 7;
    private static final int FAVORITES_PAGE = 8;
    private static final int RECENT_PAGE = 9;
    private static final int MAX_NODE_LIBRARY_ENTRIES = 18;
    private static final DroneNodeLibraryIndex NODE_LIBRARY_INDEX =
            new DroneNodeLibraryIndex(NODE_LIBRARY, MAX_NODE_LIBRARY_ENTRIES);
    private static final String NODE_FAVORITES_TAG = "DroneNodeFavorites";
    private static final String NODE_RECENT_TAG = "DroneNodeRecent";

    private DroneProgramEditSession editSession;
    private int loadedCardFingerprint = Integer.MIN_VALUE;
    private ItemStack loadedCardReference = ItemStack.EMPTY;
    private UUID editorOwner;
    private UUID worldSelectionOwner;
    private UUID worldSelectionNodeId;
    private boolean worldSelectionArea;
    private long worldSelectionExpiresAt;
    private String serverStatus = "Insert a program card";

    private DroneProgramGraph clientGraph;
    private boolean clientEditable;
    private String clientStatus = "Waiting for server";
    private int clientErrors;
    private int clientWarnings;
    private final List<String> clientDiagnosticLines = new ArrayList<>();
    private final List<ClientDiagnostic> clientDiagnostics = new ArrayList<>();
    private final Set<UUID> clientDiagnosticNodeIds = new HashSet<>();
    private final Map<UUID, DroneDiagnosticSeverity> clientDiagnosticSeverityByNode = new java.util.HashMap<>();
    private UUID clientActiveNodeId;
    private String clientRuntimeStatus = "NOT RUN";
    private boolean clientRemoteConnected;
    private int clientRemoteEntityId = -1;
    private String clientRemoteInfo = "Wireless: disconnected";
    private BlockPos clientRemotePosition;
    private List<ClientRemoteVariable> clientRemoteVariables = Collections.emptyList();
    private int clientRemoteVariableOffset;
    private String clientRemoteTrace = "";
    private List<ClientRemoteTrace> clientRemoteTraces = Collections.emptyList();
    private int clientRemoteTraceFilter;
    private int clientRemoteTraceOffset;
    private String clientRemoteActionStatus = "SUCCESS";
    private String clientRemoteActionError = "";
    private long clientRemoteNodeTicks;
    private List<ClientRemotePort> clientRemotePorts = Collections.emptyList();
    private int clientRemotePortOffset;
    private String clientRemoteOutputNode = "";
    private String clientRemoteOutputPort = "";
    private long clientRemoteOutputAmount;
    private String clientRemoteNodeType = "-";
    private BlockPos clientRemotePathTarget;
    private BlockPos clientRemotePathWaypoint;
    private int clientRemotePathIndex;
    private int clientRemotePathLength;
    private List<BlockPos> clientRemotePath = Collections.emptyList();
    private boolean clientRemotePathPreview;
    private int clientRemoteAreaIndex = -1;
    private int clientRemoteAreaTotal;
    private BlockPos clientRemoteAreaPosition;
    private boolean clientRemoteCoordinateHighlight;
    private boolean clientAreaProjectionEnabled;
    private long clientRemoteEnergyStored;
    private long clientRemoteEnergyCapacity;
    private int clientRemoteEstimatedAreaCompleted;
    private int clientRemoteEstimatedAreaTotal;
    private long clientRemoteEstimatedAreaRemainingEu = -1L;
    /** Wireless page 0 is status, page 1 is live ports, and page 2 is navigation. */
    private int clientRemoteDebugPage;
    private int clientLibraryPage;
    private int clientInspectorPage;
    private int clientTemplateIndex;
    private int clientDiagnosticFilter;
    private int clientDiagnosticPage;
    private String clientNodeSearch = "";
    private List<ResourceLocation> clientFavoriteNodeTypes = Collections.emptyList();
    private List<ResourceLocation> clientRecentNodeTypes = Collections.emptyList();
    private String clientFluidSearch = "";
    private int clientFluidResultIndex;
    private String clientFluidCacheQuery;
    private List<String> clientFluidResults = Collections.emptyList();
    private String clientItemSearch = "";
    private int clientItemResultIndex;
    private boolean clientItemOreMode;
    private String clientItemCacheQuery;
    private boolean clientItemCacheOreMode;
    private List<ClientItemResult> clientItemResults = Collections.emptyList();
    private String clientEntitySearch = "";
    private int clientEntityResultIndex;
    private String clientEntityCacheQuery;
    private List<ClientEntityResult> clientEntityResults = Collections.emptyList();
    private String clientDockSearch = "";
    private int clientDockResultIndex;
    private List<ClientDockResult> clientDockDirectory = Collections.emptyList();
    private List<ClientFleetResult> clientFleetDirectory = Collections.emptyList();
    private int clientFleetResultIndex;
    private String clientLibraryProgramSearch = "";
    private int clientLibraryProgramResultIndex;
    private List<ClientLibraryProgramResult> clientProgramDirectory = Collections.emptyList();
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
        syncManager.registerSyncedAction(SAVE_AS_ACTION, false, true,
                packet -> saveProgramAsNewCard(guiData.getPlayer()));
        syncManager.registerSyncedAction(REMOTE_CONTROL_ACTION, false, true,
                packet -> receiveRemoteControl(guiData.getPlayer(), packet.readString(24)));
        syncManager.registerSyncedAction(FLEET_CONTROL_ACTION, false, true,
                packet -> receiveFleetControl(guiData.getPlayer(), packet.readString(36), packet.readString(16)));
        syncManager.registerSyncedAction(NODE_LIBRARY_ACTION, false, true,
                packet -> receiveNodeLibraryAction(guiData.getPlayer(), packet.readString(64), packet.readBoolean()));
        syncManager.registerSyncedAction(CREATE_TEMPLATE_ACTION, false, true,
                packet -> createOfficialTemplateCard(guiData.getPlayer(), packet.readString(64)));
        syncManager.registerSyncedAction(IMPORT_PROGRAM_ACTION, false, true,
                packet -> importProgramTransfer(guiData.getPlayer(),
                        packet.readString(DroneProgramTransferCodec.MAX_TEXT_LENGTH)));
        syncManager.registerSyncedAction(COMPARE_PROGRAM_ACTION, false, true,
                packet -> compareProgramTransfer(guiData.getPlayer(),
                        packet.readString(DroneProgramTransferCodec.MAX_TEXT_LENGTH)));
        syncManager.registerSyncedAction(WORLD_SELECTION_ACTION, false, true,
                packet -> beginWorldSelection(guiData.getPlayer(), packet));
        syncManager.addOpenListener(this::onEditorOpened);
        syncManager.addCloseListener(this::onEditorClosed);

        DroneProgramCanvasWidget canvas = new DroneProgramCanvasWidget(this::getClientGraph,
                command -> sendEditCommand(syncManager, command), () -> clientEditable,
                this::getClientDiagnosticNodeIds, () -> clientActiveNodeId,
                this::getClientDockCapturePosition, this::getClientRemoteCapturePosition)
                .withDiagnosticSeverities(this::getClientDiagnosticSeverityByNode)
                .pos(91, 24).size(281, 222);

        return GTGuis.createPanel(this, 560, PROGRAMMER_PANEL_HEIGHT)
                .child(IKey.lang(getMetaFullName()).asWidget().pos(5, 5))
                .child(IKey.lang("drtech.drone.programmer.program_name").asWidget().pos(91, 8).size(34, 10))
                .child(new TextFieldWidget().pos(126, 4).size(190, 16).setMaxLength(64)
                        .setEnabledIf(widget -> clientEditable)
                        .value(new StringValue.Dynamic(this::getClientProgramName, this::setClientProgramName)))
                .child(new ButtonWidget<>().pos(319, 4).size(53, 16)
                        .overlay(IKey.lang("drtech.drone.programmer.rename"))
                        .setEnabledIf(widget -> clientEditable && clientProgramNameDirty)
                        .onMousePressed(mouse -> { sendProgramRename(syncManager); return true; }))
                .child(new ButtonWidget<>().pos(374, 4).size(139, 16)
                        .overlay(IKey.lang("drtech.drone.programmer.save_as"))
                        .tooltipStatic(tooltip -> tooltip.addLine(IKey.lang("drtech.drone.programmer.save_as.help")))
                        .setEnabledIf(widget -> clientEditable)
                        .onMousePressed(mouse -> { syncManager.callSyncedAction(SAVE_AS_ACTION); return true; }))
                .child(new ButtonWidget<>().pos(515, 4).size(41, 16)
                        .overlay(IKey.lang("drtech.drone.programmer.templates.short"))
                        .tooltipStatic(tooltip -> tooltip.addLine(IKey.lang("drtech.drone.programmer.templates.help")))
                        .onMousePressed(mouse -> { clientInspectorPage = 4; return true; }))
                .child(IKey.dynamic(this::getStatusLine).asWidget().pos(176, 270).size(376, 12))
                .child(new ItemSlot()
                        .slot(SyncHandlers.itemSlot(importItems, 0).slotGroup("programmer_items").accessibility(true, true))
                        .pos(7, 24))
                .child(new ItemSlot()
                        .slot(SyncHandlers.itemSlot(importItems, 1).slotGroup("programmer_items").accessibility(true, true))
                        .pos(49, 24))
                .child(new ButtonWidget<>().pos(5, 45).size(81, 16)
                        .overlay(IKey.lang("drtech.drone.programmer.write_drone"))
                        .onMousePressed(mouse -> {
                            if (clientErrors > 0) {
                                clientInspectorPage = 2;
                                clientDiagnosticFilter = 1;
                                clientDiagnosticPage = 0;
                            }
                            syncManager.callSyncedAction(WRITE_DRONE_ACTION);
                            return true;
                        }))
                .child(IKey.lang("drtech.drone.programmer.search").asWidget().pos(5, 68).size(20, 10))
                .child(new TextFieldWidget().pos(27, 65).size(59, 16).setMaxLength(32)
                        .value(new StringValue.Dynamic(() -> clientNodeSearch, value -> {
                            clientNodeSearch = value;
                            clientLibraryPage = 0;
                        })))
                .child(libraryNodeButton(0, syncManager, canvas))
                .child(libraryNodeButton(1, syncManager, canvas))
                .child(libraryNodeButton(2, syncManager, canvas))
                .child(libraryNodeButton(3, syncManager, canvas))
                .child(libraryNodeButton(4, syncManager, canvas))
                .child(libraryNodeButton(5, syncManager, canvas))
                .child(libraryNodeButton(6, syncManager, canvas))
                .child(libraryNodeButton(7, syncManager, canvas))
                .child(libraryNodeButton(8, syncManager, canvas))
                .child(libraryNodeButton(9, syncManager, canvas))
                .child(libraryNodeButton(10, syncManager, canvas))
                .child(libraryNodeButton(11, syncManager, canvas))
                .child(libraryNodeButton(12, syncManager, canvas))
                .child(libraryNodeButton(13, syncManager, canvas))
                .child(libraryNodeButton(14, syncManager, canvas))
                .child(libraryNodeButton(15, syncManager, canvas))
                .child(libraryNodeButton(16, syncManager, canvas))
                .child(libraryNodeButton(17, syncManager, canvas))
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
                .child(pagedSmallNodeButton(0, DrTechDroneNodes.COMMENT, 47, 203, syncManager))

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
                .child(pagedSmallNodeButton(1, DrTechDroneNodes.CRAFT_ITEMS, 47, 203, syncManager))

                .child(pagedSmallNodeButton(4, DrTechDroneNodes.IMPORT_FLUID, 5, 83, syncManager))
                .child(pagedSmallNodeButton(4, DrTechDroneNodes.EXPORT_FLUID, 47, 83, syncManager))
                .child(pagedSmallNodeButton(4, DrTechDroneNodes.DRAIN_FLUID, 5, 98, syncManager))
                .child(pagedSmallNodeButton(4, DrTechDroneNodes.FLUID_FILTER, 47, 98, syncManager))
                .child(pagedSmallNodeButton(4, DrTechDroneNodes.DRONE_FLUID_AMOUNT, 5, 113, syncManager))
                .child(pagedSmallNodeButton(4, DrTechDroneNodes.DRONE_FLUID_PERCENT, 47, 113, syncManager))
                .child(pagedSmallNodeButton(4, DrTechDroneNodes.CONTAINER_FLUID_AMOUNT, 5, 128, syncManager))
                .child(pagedSmallNodeButton(4, DrTechDroneNodes.FIND_FLUID_CONTAINER, 47, 128, syncManager))
                .child(pagedSmallNodeButton(4, DrTechDroneNodes.WAIT_FOR_FLUID_AMOUNT, 5, 143, syncManager))
                .child(pagedSmallNodeButton(4, DrTechDroneNodes.GROUP, 47, 143, syncManager))
                .child(pagedSmallNodeButton(4, DrTechDroneNodes.USE_ITEM, 5, 158, syncManager))
                .child(pagedSmallNodeButton(4, DrTechDroneNodes.FOR_EACH_ITEM_FILTER, 47, 158, syncManager))
                .child(pagedSmallNodeButton(4, DrTechDroneNodes.STRING, 5, 173, syncManager))
                .child(pagedSmallNodeButton(4, DrTechDroneNodes.COMPARE_STRING, 47, 173, syncManager))
                .child(pagedSmallNodeButton(4, DrTechDroneNodes.GET_STRING_VARIABLE, 5, 188, syncManager))
                .child(pagedSmallNodeButton(4, DrTechDroneNodes.SET_STRING_VARIABLE, 47, 188, syncManager))
                .child(pagedSmallNodeButton(4, DrTechDroneNodes.DISPLAY_STRING, 5, 203, syncManager))
                .child(pagedSmallNodeButton(4, DrTechDroneNodes.REMOTE_ALERT, 47, 203, syncManager))

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
                .child(pagedSmallNodeButton(2, DrTechDroneNodes.CAN_CRAFT, 5, 188, syncManager))
                .child(pagedSmallNodeButton(2, DrTechDroneNodes.CRAFTABLE_COUNT, 47, 188, syncManager))
                .child(pagedSmallNodeButton(2, DrTechDroneNodes.ENTITY_FILTER, 5, 203, syncManager))
                .child(pagedSmallNodeButton(2, DrTechDroneNodes.DOCK_REFERENCE, 47, 203, syncManager))

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
                .child(pagedSmallNodeButton(5, DrTechDroneNodes.AREA_EXPAND, 5, 158, syncManager))
                .child(pagedSmallNodeButton(5, DrTechDroneNodes.AREA_INSET, 47, 158, syncManager))
                .child(pagedSmallNodeButton(5, DrTechDroneNodes.CUBOID_SURFACE_AREA, 5, 173, syncManager))
                .child(pagedSmallNodeButton(5, DrTechDroneNodes.PYRAMID_AREA, 47, 173, syncManager))
                .child(pagedSmallNodeButton(5, DrTechDroneNodes.GRID_AREA, 5, 188, syncManager))
                .child(pagedSmallNodeButton(5, DrTechDroneNodes.RANDOM_POINTS_AREA, 47, 188, syncManager))
                .child(pagedSmallNodeButton(5, DrTechDroneNodes.AREA_BOUNDARY, 5, 203, syncManager))
                .child(pagedSmallNodeButton(5, DrTechDroneNodes.AREA_SCALE, 47, 203, syncManager))
                .child(pagedSmallNodeButton(6, DrTechDroneNodes.CRAFT_GRID, 5, 83, syncManager))
                .child(pagedSmallNodeButton(6, DrTechDroneNodes.SET_MACHINE_WORKING, 47, 83, syncManager))
                .child(pagedSmallNodeButton(6, DrTechDroneNodes.WAIT_MACHINE_IDLE, 5, 98, syncManager))
                .child(pagedSmallNodeButton(6, DrTechDroneNodes.MACHINE_ACTIVE, 47, 98, syncManager))
                .child(pagedSmallNodeButton(6, DrTechDroneNodes.MACHINE_ENABLED, 5, 113, syncManager))
                .child(pagedSmallNodeButton(6, DrTechDroneNodes.MACHINE_PROGRESS, 47, 113, syncManager))
                .child(pagedSmallNodeButton(6, DrTechDroneNodes.WAIT_MACHINE_CYCLE, 5, 128, syncManager))
                .child(pagedSmallNodeButton(6, DrTechDroneNodes.MACHINE_WAITING_INPUT, 47, 128, syncManager))
                .child(pagedSmallNodeButton(6, DrTechDroneNodes.MACHINE_OUTPUT_BLOCKED, 5, 143, syncManager))
                .child(pagedSmallNodeButton(6, DrTechDroneNodes.MACHINE_LOW_ENERGY, 47, 143, syncManager))
                .child(pagedSmallNodeButton(6, DrTechDroneNodes.MACHINE_DIAGNOSTIC, 5, 158, syncManager))
                .child(pagedSmallNodeButton(6, DrTechDroneNodes.REPAIR_MACHINE, 47, 158, syncManager))
                .child(pagedSmallNodeButton(6, DrTechDroneNodes.MACHINE_NEEDS_MAINTENANCE, 5, 173, syncManager))
                .child(pagedSmallNodeButton(6, DrTechDroneNodes.MACHINE_MAINTENANCE_PROBLEMS, 47, 173, syncManager))
                .child(pagedSmallNodeButton(6, DrTechDroneNodes.PROGRAM_REFERENCE, 5, 188, syncManager))
                .child(pagedSmallNodeButton(6, DrTechDroneNodes.CALL_PROGRAM, 47, 188, syncManager))
                .child(pagedSmallNodeButton(LOOPS_PAGE, DrTechDroneNodes.REPEAT, 5, 83, syncManager))
                .child(pagedSmallNodeButton(LOOPS_PAGE, DrTechDroneNodes.WHILE, 47, 83, syncManager))
                .child(pagedSmallNodeButton(LOOPS_PAGE, DrTechDroneNodes.FOR_EACH_COORDINATE, 5, 98, syncManager))
                .child(pagedSmallNodeButton(LOOPS_PAGE, DrTechDroneNodes.FOR_EACH_ITEM_FILTER, 47, 98, syncManager))
                .child(pagedSmallNodeButton(LOOPS_PAGE, DrTechDroneNodes.BREAK_LOOP, 5, 113, syncManager))
                .child(pagedSmallNodeButton(LOOPS_PAGE, DrTechDroneNodes.CONTINUE_LOOP, 47, 113, syncManager))
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
                .child(pagedSmallNodeButton(3, DrTechDroneNodes.CURRENT_ITEM_FILTER, 47, 203, syncManager))
                .child(quickNodeButton(FAVORITES_PAGE, 0, syncManager))
                .child(quickNodeButton(FAVORITES_PAGE, 1, syncManager))
                .child(quickNodeButton(FAVORITES_PAGE, 2, syncManager))
                .child(quickNodeButton(FAVORITES_PAGE, 3, syncManager))
                .child(quickNodeButton(FAVORITES_PAGE, 4, syncManager))
                .child(quickNodeButton(FAVORITES_PAGE, 5, syncManager))
                .child(quickNodeButton(FAVORITES_PAGE, 6, syncManager))
                .child(quickNodeButton(FAVORITES_PAGE, 7, syncManager))
                .child(quickNodeButton(FAVORITES_PAGE, 8, syncManager))
                .child(quickNodeButton(FAVORITES_PAGE, 9, syncManager))
                .child(quickNodeButton(FAVORITES_PAGE, 10, syncManager))
                .child(quickNodeButton(FAVORITES_PAGE, 11, syncManager))
                .child(quickNodeButton(FAVORITES_PAGE, 12, syncManager))
                .child(quickNodeButton(FAVORITES_PAGE, 13, syncManager))
                .child(quickNodeButton(FAVORITES_PAGE, 14, syncManager))
                .child(quickNodeButton(FAVORITES_PAGE, 15, syncManager))
                .child(quickNodeButton(FAVORITES_PAGE, 16, syncManager))
                .child(quickNodeButton(FAVORITES_PAGE, 17, syncManager))
                .child(quickNodeButton(RECENT_PAGE, 0, syncManager))
                .child(quickNodeButton(RECENT_PAGE, 1, syncManager))
                .child(quickNodeButton(RECENT_PAGE, 2, syncManager))
                .child(quickNodeButton(RECENT_PAGE, 3, syncManager))
                .child(quickNodeButton(RECENT_PAGE, 4, syncManager))
                .child(quickNodeButton(RECENT_PAGE, 5, syncManager))
                .child(quickNodeButton(RECENT_PAGE, 6, syncManager))
                .child(quickNodeButton(RECENT_PAGE, 7, syncManager))
                .child(quickNodeButton(RECENT_PAGE, 8, syncManager))
                .child(quickNodeButton(RECENT_PAGE, 9, syncManager))
                .child(quickNodeButton(RECENT_PAGE, 10, syncManager))
                .child(quickNodeButton(RECENT_PAGE, 11, syncManager))
                .child(quickNodeButton(RECENT_PAGE, 12, syncManager))
                .child(quickNodeButton(RECENT_PAGE, 13, syncManager))
                .child(quickNodeButton(RECENT_PAGE, 14, syncManager))
                .child(quickNodeButton(RECENT_PAGE, 15, syncManager))
                .child(quickNodeButton(RECENT_PAGE, 16, syncManager))
                .child(quickNodeButton(RECENT_PAGE, 17, syncManager))
                .child(new ButtonWidget<>().pos(5, 225).size(23, 16).overlay(IKey.str("<"))
                        .onMousePressed(mouse -> {
                            moveClientLibraryPage(-1);
                            return true;
                        }))
                .child(IKey.dynamic(this::getClientLibraryPageLabel).asWidget().pos(29, 229).size(32, 10)
                        .scale(0.72F))
                .child(new ButtonWidget<>().pos(63, 225).size(23, 16).overlay(IKey.str(">"))
                        .onMousePressed(mouse -> {
                            moveClientLibraryPage(1);
                            return true;
                        }))
                .child(new ButtonWidget<>().pos(91, 250).size(30, 16).overlay(IKey.lang("drtech.drone.programmer.delete"))
                        .onMousePressed(mouse -> { canvas.deleteSelected(); return true; }))
                .child(new ButtonWidget<>().pos(122, 250).size(30, 16).overlay(IKey.lang("drtech.drone.programmer.reset"))
                        .tooltipStatic(tooltip -> tooltip.addLine(IKey.lang("drtech.drone.programmer.reset.help")))
                        .onMousePressed(mouse -> { canvas.resetViewOrAutoLayout(); return true; }))
                .child(new ButtonWidget<>().pos(153, 250).size(30, 16).overlay(IKey.lang("drtech.drone.programmer.undo"))
                        .onMousePressed(mouse -> { sendHistoryCommand(syncManager, true); return true; }))
                .child(new ButtonWidget<>().pos(184, 250).size(30, 16).overlay(IKey.lang("drtech.drone.programmer.redo"))
                        .onMousePressed(mouse -> { sendHistoryCommand(syncManager, false); return true; }))
                .child(new ButtonWidget<>().pos(215, 250).size(30, 16).overlay(IKey.lang("drtech.drone.programmer.copy"))
                        .onMousePressed(mouse -> { canvas.copySelected(); return true; }))
                .child(new ButtonWidget<>().pos(246, 250).size(30, 16).overlay(IKey.lang("drtech.drone.programmer.paste"))
                        .setEnabledIf(widget -> canvas.hasCopiedNode())
                        .onMousePressed(mouse -> { canvas.pasteCopiedNode(); return true; }))
                .child(new ButtonWidget<>().pos(277, 250).size(30, 16)
                        .overlay(IKey.lang("drtech.drone.programmer.align_horizontal"))
                        .tooltipStatic(tooltip -> tooltip.addLine(IKey.lang("drtech.drone.programmer.align_horizontal.help")))
                        .setEnabledIf(widget -> clientEditable && canvas.getSelectionCount() > 1)
                        .onMousePressed(mouse -> { canvas.alignSelectedHorizontal(); return true; }))
                .child(new ButtonWidget<>().pos(308, 250).size(30, 16)
                        .overlay(IKey.lang("drtech.drone.programmer.align_vertical"))
                        .tooltipStatic(tooltip -> tooltip.addLine(IKey.lang("drtech.drone.programmer.align_vertical.help")))
                        .setEnabledIf(widget -> clientEditable && canvas.getSelectionCount() > 1)
                        .onMousePressed(mouse -> { canvas.alignSelectedVertical(); return true; }))
                .child(new ButtonWidget<>().pos(339, 250).size(33, 16)
                        .overlay(IKey.dynamic(() -> I18n.format(canvas.getSelectionCount() > 0
                                ? "drtech.drone.programmer.fit_selection" : "drtech.drone.programmer.fit_all")))
                        .tooltipStatic(tooltip -> tooltip.addLine(IKey.lang("drtech.drone.programmer.fit.help")))
                        .onMousePressed(mouse -> { canvas.fitSelectionOrAll(); return true; }))
                .child(new ButtonWidget<>().pos(379, 24).size(42, 16)
                        .overlay(IKey.dynamic(() -> I18n.format(clientInspectorPage == 0
                                ? "drtech.drone.programmer.inspector.short_active"
                                : "drtech.drone.programmer.inspector.short")))
                        .onMousePressed(mouse -> { clientInspectorPage = 0; return true; }))
                .child(new ButtonWidget<>().pos(423, 24).size(43, 16)
                        .overlay(IKey.dynamic(() -> I18n.format(clientInspectorPage == 1
                                ? "drtech.drone.programmer.remote.short_active"
                                : "drtech.drone.programmer.remote.short")))
                        .onMousePressed(mouse -> { clientInspectorPage = 1; return true; }))
                .child(new ButtonWidget<>().pos(468, 24).size(44, 16)
                        .overlay(IKey.dynamic(() -> I18n.format(clientInspectorPage == 2
                                ? "drtech.drone.programmer.diagnostics.active"
                                : "drtech.drone.programmer.diagnostics")))
                        .onMousePressed(mouse -> { clientInspectorPage = 2; return true; }))
                .child(new ButtonWidget<>().pos(514, 24).size(42, 16)
                        .overlay(IKey.dynamic(() -> I18n.format(clientInspectorPage == 3
                                ? "drtech.drone.programmer.fleet.active"
                                : "drtech.drone.programmer.fleet")))
                        .onMousePressed(mouse -> { clientInspectorPage = 3; return true; }))
                .child(IKey.lang("drtech.drone.programmer.templates.title").asWidget().pos(379, 48).size(176, 12)
                        .setEnabledIf(widget -> clientInspectorPage == 4))
                .child(IKey.dynamic(this::getClientTemplateName).asWidget().pos(379, 65).size(176, 12)
                        .setEnabledIf(widget -> clientInspectorPage == 4))
                .child(IKey.dynamic(this::getClientTemplateDescription).asWidget().pos(379, 82).size(176, 45)
                        .setEnabledIf(widget -> clientInspectorPage == 4))
                .child(IKey.dynamic(this::getClientTemplateHardware).asWidget().pos(379, 132).size(176, 24)
                        .setEnabledIf(widget -> clientInspectorPage == 4))
                .child(IKey.dynamic(this::getClientTemplateSize).asWidget().pos(379, 161).size(176, 12)
                        .setEnabledIf(widget -> clientInspectorPage == 4))
                .child(actionButton("<", 379, 181, () -> moveClientTemplate(-1))
                        .setEnabledIf(widget -> clientInspectorPage == 4))
                .child(actionButton(">", 416, 181, () -> moveClientTemplate(1))
                        .setEnabledIf(widget -> clientInspectorPage == 4))
                .child(IKey.dynamic(this::getClientTemplatePage).asWidget().pos(454, 185).size(62, 10)
                        .setEnabledIf(widget -> clientInspectorPage == 4))
                .child(new ButtonWidget<>().pos(379, 205).size(176, 18)
                        .overlay(IKey.lang("drtech.drone.programmer.templates.create"))
                        .tooltipStatic(tooltip -> tooltip.addLine(IKey.lang("drtech.drone.programmer.templates.create.help")))
                        .setEnabledIf(widget -> clientInspectorPage == 4 && clientEditable)
                        .onMousePressed(mouse -> {
                            DroneOfficialProgramTemplate template = getClientTemplate();
                            if (template != null) syncManager.callSyncedAction(CREATE_TEMPLATE_ACTION,
                                    buffer -> buffer.writeString(template.getId().toString()));
                            return true;
                        }))
                .child(new ButtonWidget<>().pos(379, 228).size(86, 18)
                        .overlay(IKey.lang("drtech.drone.programmer.export"))
                        .setEnabledIf(widget -> clientInspectorPage == 4 && clientGraph != null)
                        .onMousePressed(mouse -> { exportClientProgramToClipboard(); return true; }))
                .child(new ButtonWidget<>().pos(469, 228).size(86, 18)
                        .overlay(IKey.lang("drtech.drone.programmer.import"))
                        .tooltipStatic(tooltip -> tooltip.addLine(IKey.lang("drtech.drone.programmer.import.help")))
                        .setEnabledIf(widget -> clientInspectorPage == 4 && clientEditable)
                        .onMousePressed(mouse -> {
                            importClientProgramFromClipboard(syncManager);
                            return true;
                        }))
                .child(new ButtonWidget<>().pos(379, 249).size(176, 16)
                        .overlay(IKey.lang("drtech.drone.programmer.compare"))
                        .setEnabledIf(widget -> clientInspectorPage == 4 && clientEditable)
                        .onMousePressed(mouse -> { compareClientProgramFromClipboard(syncManager); return true; }))
                .child(IKey.lang("drtech.drone.programmer.fleet.title").asWidget().pos(379, 48).size(176, 12)
                        .setEnabledIf(widget -> clientInspectorPage == 3))
                .child(IKey.dynamic(this::getClientFleetSummary).asWidget().pos(379, 64).size(176, 20)
                        .setEnabledIf(widget -> clientInspectorPage == 3))
                .child(actionButton("<", 379, 89, () -> moveClientFleetResult(-1))
                        .setEnabledIf(widget -> clientInspectorPage == 3 && !clientFleetDirectory.isEmpty()))
                .child(actionButton(">", 416, 89, () -> moveClientFleetResult(1))
                        .setEnabledIf(widget -> clientInspectorPage == 3 && !clientFleetDirectory.isEmpty()))
                .child(IKey.dynamic(this::getClientFleetPage).asWidget().pos(454, 93).size(62, 10)
                        .setEnabledIf(widget -> clientInspectorPage == 3))
                .child(IKey.dynamic(this::getClientFleetDetails).asWidget().pos(379, 112).size(176, 112)
                        .setEnabledIf(widget -> clientInspectorPage == 3))
                .child(fleetControlButton("drtech.drone.controller.start", "START", 379, syncManager)
                        .setEnabledIf(widget -> canControlSelectedFleetDrone()))
                .child(fleetControlButton("drtech.drone.controller.stop", "STOP", 438, syncManager)
                        .setEnabledIf(widget -> canControlSelectedFleetDrone()))
                .child(fleetControlButton("drtech.drone.programmer.fleet.recall", "RECALL", 497, syncManager)
                        .setEnabledIf(widget -> canControlSelectedFleetDrone()))
                .child(IKey.dynamic(canvas::getSelectedDescription).asWidget().pos(379, 44).size(133, 25)
                        .setEnabledIf(widget -> clientInspectorPage == 0))
                .child(IKey.dynamic(canvas::getSelectedPropertySummary).asWidget().pos(379, 72).size(134, 24)
                        .setEnabledIf(widget -> clientInspectorPage == 0))
                .child(actionButton("<", 379, 99, canvas::selectPreviousProperty)
                        .setEnabledIf(widget -> clientInspectorPage == 0))
                .child(actionButton(">", 416, 99, canvas::selectNextProperty)
                        .setEnabledIf(widget -> clientInspectorPage == 0))
                .child(new ButtonWidget<>().pos(454, 99).size(59, 16)
                        .overlay(IKey.lang("drtech.drone.programmer.property_reset_default"))
                        .tooltipStatic(tooltip -> tooltip.addLine(
                                IKey.lang("drtech.drone.programmer.property_reset_default.help")))
                        .setEnabledIf(widget -> clientInspectorPage == 0 && canvas.canResetSelectedProperty())
                        .onMousePressed(mouse -> { canvas.resetSelectedPropertyToDefault(); return true; }))
                .child(actionButton("-", 379, 118, () -> canvas.adjustSelectedProperty(-1))
                        .setEnabledIf(widget -> clientInspectorPage == 0 && canvas.isSelectedPropertyNumeric()))
                .child(actionButton("+", 416, 118, () -> canvas.adjustSelectedProperty(1))
                        .setEnabledIf(widget -> clientInspectorPage == 0 && canvas.isSelectedPropertyNumeric()))
                .child(new ButtonWidget<>().pos(379, 137).size(35, 16)
                        .overlay(IKey.dynamic(canvas::getSelectedPropertyActionLabel))
                        .setEnabledIf(widget -> clientInspectorPage == 0 && !canvas.isSelectedFluidSelector()
                                && !canvas.isSelectedItemSelector() && !canvas.isSelectedEntitySelector()
                                && !canvas.isSelectedDockReference()
                                && !canvas.isSelectedProgramReference()
                                && !canvas.isSelectedBlockSelector()
                                && !canvas.isSelectedChoiceProperty()
                                && !canvas.isSelectedLongTextProperty()
                                && canvas.canActivateSelectedProperty())
                        .onMousePressed(mouse -> { canvas.activateSelectedProperty(); return true; }))
                .child(actionButton(IKey.lang("drtech.drone.programmer.clear"), 416, 137,
                        canvas::clearSelectedProperty)
                        .setEnabledIf(widget -> clientInspectorPage == 0 && !canvas.isSelectedFluidSelector()
                                && !canvas.isSelectedItemSelector() && !canvas.isSelectedEntitySelector()
                                && !canvas.isSelectedDockReference()
                                && !canvas.isSelectedProgramReference()
                                && !canvas.isSelectedBlockSelector()
                                && !canvas.isSelectedChoiceProperty()
                                && !canvas.isSelectedLongTextProperty()))
                .child(new TextFieldWidget().pos(379, 156).size(134, 16).setMaxLength(128)
                        .setEnabledIf(widget -> clientInspectorPage == 0 && canvas.isSelectedInlineTextProperty())
                        .value(new StringValue.Dynamic(canvas::getSelectedPropertyInputText,
                                canvas::setSelectedPropertyInputText)))
                .child(IKey.dynamic(canvas::getSelectedPropertyValidationMessage).asWidget().pos(379, 175).size(134, 16)
                        .setEnabledIf(widget -> clientInspectorPage == 0 && canvas.isSelectedInlineTextProperty()))
                .child(new DroneMultilineTextWidget(canvas::getSelectedPropertyInputText,
                        canvas::setSelectedPropertyInputText, 1024, 32)
                        .pos(379, 118).size(134, 73)
                        .setEnabledIf(widget -> clientInspectorPage == 0 && canvas.isSelectedLongTextProperty()))
                .child(new ButtonWidget<>().pos(379, 195).size(65, 16)
                        .overlay(IKey.lang("drtech.drone.programmer.apply"))
                        .setEnabledIf(widget -> clientInspectorPage == 0 && canvas.isSelectedLongTextProperty())
                        .onMousePressed(mouse -> { canvas.activateSelectedProperty(); return true; }))
                .child(new ButtonWidget<>().pos(448, 195).size(65, 16)
                        .overlay(IKey.lang("drtech.drone.programmer.clear"))
                        .setEnabledIf(widget -> clientInspectorPage == 0 && canvas.isSelectedLongTextProperty())
                        .onMousePressed(mouse -> { canvas.clearSelectedProperty(); return true; }))
                .child(IKey.lang("drtech.drone.programmer.multiline_help").asWidget()
                        .pos(379, 214).size(134, 27)
                        .setEnabledIf(widget -> clientInspectorPage == 0 && canvas.isSelectedLongTextProperty()))
                .child(actionButton(IKey.lang("drtech.drone.programmer.filter_mode"), 379, 175,
                        canvas::toggleSelectedItemFilterMode)
                        .setEnabledIf(widget -> clientInspectorPage == 0 && canvas.isSelectedItemFilter()
                                && !canvas.isSelectedItemSelector() && !canvas.isSelectedBlockSelector()))
                .child(actionButton(IKey.lang("drtech.drone.programmer.remove_rule"), 416, 175,
                        canvas::removeLastSelectedItemFilterRule)
                        .setEnabledIf(widget -> clientInspectorPage == 0 && canvas.isSelectedItemFilter()
                                && !canvas.isSelectedItemSelector() && !canvas.isSelectedBlockSelector()))
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
                .child(IKey.dynamic(() -> I18n.format(clientItemOreMode
                        ? "drtech.drone.programmer.ore_search"
                        : "drtech.drone.programmer.item_search")).asWidget().pos(379, 122).size(24, 10)
                        .setEnabledIf(widget -> clientInspectorPage == 0 && canvas.isSelectedItemSelector()))
                .child(new TextFieldWidget().pos(404, 118).size(109, 16).setMaxLength(96)
                        .setEnabledIf(widget -> clientInspectorPage == 0 && canvas.isSelectedItemSelector())
                        .value(new StringValue.Dynamic(this::getClientItemSearch, this::setClientItemSearch)))
                .child(new ButtonWidget<>().pos(379, 137).size(134, 16)
                        .overlay(IKey.dynamic(this::getClientItemResultLabel))
                        .tooltipStatic(tooltip -> tooltip.addLine(IKey.dynamic(this::getClientItemResultTooltip)))
                        .setEnabledIf(widget -> clientInspectorPage == 0 && canvas.isSelectedItemSelector())
                        .onMousePressed(mouse -> { selectClientItemResult(canvas); return true; }))
                .child(actionButton("<", 379, 156, () -> moveClientItemResult(-1))
                        .setEnabledIf(widget -> clientInspectorPage == 0 && canvas.isSelectedItemSelector()
                                && hasClientItemResult()))
                .child(actionButton(">", 416, 156, () -> moveClientItemResult(1))
                        .setEnabledIf(widget -> clientInspectorPage == 0 && canvas.isSelectedItemSelector()
                                && hasClientItemResult()))
                .child(IKey.dynamic(this::getClientItemResultPage).asWidget().pos(454, 160).size(59, 10)
                        .setEnabledIf(widget -> clientInspectorPage == 0 && canvas.isSelectedItemSelector()))
                .child(new ButtonWidget<>().pos(379, 175).size(72, 16)
                        .overlay(IKey.dynamic(() -> I18n.format(clientItemOreMode
                                ? "drtech.drone.programmer.mode_ore"
                                : "drtech.drone.programmer.mode_registry")))
                        .setEnabledIf(widget -> clientInspectorPage == 0 && canvas.isSelectedItemSelector())
                        .onMousePressed(mouse -> { toggleClientItemSearchMode(); return true; }))
                .child(new ButtonWidget<>().pos(453, 175).size(60, 16)
                        .overlay(IKey.lang("drtech.drone.programmer.remove_rule"))
                        .setEnabledIf(widget -> clientInspectorPage == 0 && canvas.isSelectedItemSelector())
                        .onMousePressed(mouse -> { canvas.removeLastSelectedItemFilterRule(); return true; }))
                .child(new ButtonWidget<>().pos(379, 195).size(72, 16)
                        .overlay(IKey.lang("drtech.drone.programmer.held_item"))
                        .setEnabledIf(widget -> clientInspectorPage == 0 && canvas.isSelectedItemSelector())
                        .onMousePressed(mouse -> { canvas.activateSelectedProperty(); return true; }))
                .child(new ButtonWidget<>().pos(453, 195).size(60, 16)
                        .overlay(IKey.lang("drtech.drone.programmer.clear"))
                        .setEnabledIf(widget -> clientInspectorPage == 0 && canvas.isSelectedItemSelector())
                        .onMousePressed(mouse -> { canvas.clearSelectedProperty(); return true; }))
                .child(IKey.dynamic(canvas::getSelectedItemFilterBoundsLabel).asWidget()
                        .pos(379, 214).size(134, 12)
                        .setEnabledIf(widget -> clientInspectorPage == 0 && canvas.isSelectedItemSelector()))
                .child(actionButton("D-", 379, 228, () -> canvas.adjustSelectedItemFilterBound(0, -10))
                        .setEnabledIf(widget -> clientInspectorPage == 0 && canvas.hasSelectedItemFilterRule()))
                .child(actionButton("D+", 413, 228, () -> canvas.adjustSelectedItemFilterBound(0, 10))
                        .setEnabledIf(widget -> clientInspectorPage == 0 && canvas.hasSelectedItemFilterRule()))
                .child(actionButton("d-", 447, 228, () -> canvas.adjustSelectedItemFilterBound(1, -10))
                        .setEnabledIf(widget -> clientInspectorPage == 0 && canvas.hasSelectedItemFilterRule()))
                .child(actionButton("d+", 481, 228, () -> canvas.adjustSelectedItemFilterBound(1, 10))
                        .setEnabledIf(widget -> clientInspectorPage == 0 && canvas.hasSelectedItemFilterRule()))
                .child(actionButton("C-", 379, 244, () -> canvas.adjustSelectedItemFilterBound(2, -1))
                        .setEnabledIf(widget -> clientInspectorPage == 0 && canvas.hasSelectedItemFilterRule()))
                .child(actionButton("C+", 413, 244, () -> canvas.adjustSelectedItemFilterBound(2, 1))
                        .setEnabledIf(widget -> clientInspectorPage == 0 && canvas.hasSelectedItemFilterRule()))
                .child(actionButton("c-", 447, 244, () -> canvas.adjustSelectedItemFilterBound(3, -1))
                        .setEnabledIf(widget -> clientInspectorPage == 0 && canvas.hasSelectedItemFilterRule()))
                .child(actionButton("c+", 481, 244, () -> canvas.adjustSelectedItemFilterBound(3, 1))
                        .setEnabledIf(widget -> clientInspectorPage == 0 && canvas.hasSelectedItemFilterRule()))
                .child(new ButtonWidget<>().pos(379, 260).size(66, 16)
                        .overlay(IKey.dynamic(canvas::getSelectedItemFilterNbtLabel))
                        .setEnabledIf(widget -> clientInspectorPage == 0 && canvas.hasSelectedItemFilterRule())
                        .onMousePressed(mouse -> { canvas.toggleSelectedItemFilterNbtMode(); return true; }))
                .child(new ButtonWidget<>().pos(447, 260).size(66, 16)
                        .overlay(IKey.dynamic(canvas::getSelectedItemFilterNamespaceLabel))
                        .setEnabledIf(widget -> clientInspectorPage == 0 && canvas.hasSelectedItemFilterRule())
                        .onMousePressed(mouse -> { canvas.setSelectedItemFilterNamespace(clientItemSearch); return true; }))
                .child(actionButton("dup", 379, 276, canvas::duplicateLastSelectedItemFilterRule)
                        .setEnabledIf(widget -> clientInspectorPage == 0 && canvas.hasSelectedItemFilterRule()))
                .child(actionButton("up", 413, 276, () -> canvas.moveLastSelectedItemFilterRule(-1))
                        .setEnabledIf(widget -> clientInspectorPage == 0 && canvas.hasSelectedItemFilterRule()))
                .child(actionButton("down", 447, 276, () -> canvas.moveLastSelectedItemFilterRule(1))
                        .setEnabledIf(widget -> clientInspectorPage == 0 && canvas.hasSelectedItemFilterRule()))
                .child(IKey.lang("drtech.drone.programmer.entity_search").asWidget().pos(379, 122).size(24, 10)
                        .setEnabledIf(widget -> clientInspectorPage == 0 && canvas.isSelectedEntitySelector()))
                .child(new TextFieldWidget().pos(404, 118).size(109, 16).setMaxLength(96)
                        .setEnabledIf(widget -> clientInspectorPage == 0 && canvas.isSelectedEntitySelector())
                        .value(new StringValue.Dynamic(this::getClientEntitySearch, this::setClientEntitySearch)))
                .child(new ButtonWidget<>().pos(379, 137).size(134, 16)
                        .overlay(IKey.dynamic(this::getClientEntityResultLabel))
                        .tooltipStatic(tooltip -> tooltip.addLine(IKey.dynamic(this::getClientEntityResultTooltip)))
                        .setEnabledIf(widget -> clientInspectorPage == 0 && canvas.isSelectedEntitySelector())
                        .onMousePressed(mouse -> { selectClientEntityResult(canvas); return true; }))
                .child(actionButton("<", 379, 156, () -> moveClientEntityResult(-1))
                        .setEnabledIf(widget -> clientInspectorPage == 0 && canvas.isSelectedEntitySelector()
                                && hasClientEntityResult()))
                .child(actionButton(">", 416, 156, () -> moveClientEntityResult(1))
                        .setEnabledIf(widget -> clientInspectorPage == 0 && canvas.isSelectedEntitySelector()
                                && hasClientEntityResult()))
                .child(IKey.dynamic(this::getClientEntityResultPage).asWidget().pos(454, 160).size(59, 10)
                        .setEnabledIf(widget -> clientInspectorPage == 0 && canvas.isSelectedEntitySelector()))
                .child(new ButtonWidget<>().pos(379, 175).size(72, 16)
                        .overlay(IKey.dynamic(canvas::getSelectedEntityFilterModeLabel))
                        .setEnabledIf(widget -> clientInspectorPage == 0 && canvas.isSelectedEntitySelector())
                        .onMousePressed(mouse -> { canvas.toggleSelectedEntityFilterMode(); return true; }))
                .child(new ButtonWidget<>().pos(453, 175).size(60, 16)
                        .overlay(IKey.lang("drtech.drone.programmer.remove_rule"))
                        .setEnabledIf(widget -> clientInspectorPage == 0 && canvas.isSelectedEntitySelector())
                        .onMousePressed(mouse -> { canvas.removeLastSelectedEntityFilterRule(); return true; }))
                .child(new ButtonWidget<>().pos(379, 195).size(134, 16)
                        .overlay(IKey.lang("drtech.drone.programmer.clear"))
                        .setEnabledIf(widget -> clientInspectorPage == 0 && canvas.isSelectedEntitySelector())
                        .onMousePressed(mouse -> { canvas.clearSelectedProperty(); return true; }))
                .child(IKey.dynamic(canvas::getSelectedEntityAdvancedLabel).scale(0.72F).asWidget()
                        .pos(379, 214).size(134, 12)
                        .setEnabledIf(widget -> clientInspectorPage == 0 && canvas.isSelectedEntitySelector()))
                .child(actionButton("<", 379, 228, () -> canvas.cycleSelectedEntityAdvancedField(-1))
                        .setEnabledIf(widget -> clientInspectorPage == 0 && canvas.isSelectedEntitySelector()))
                .child(actionButton(">", 416, 228, () -> canvas.cycleSelectedEntityAdvancedField(1))
                        .setEnabledIf(widget -> clientInspectorPage == 0 && canvas.isSelectedEntitySelector()))
                .child(actionButton("-", 453, 228, () -> canvas.adjustSelectedEntityAdvancedValue(-1))
                        .setEnabledIf(widget -> clientInspectorPage == 0 && canvas.isSelectedEntitySelector()))
                .child(actionButton("+", 490, 228, () -> canvas.adjustSelectedEntityAdvancedValue(1))
                        .setEnabledIf(widget -> clientInspectorPage == 0 && canvas.isSelectedEntitySelector()))
                .child(new ButtonWidget<>().pos(379, 246).size(66, 16)
                        .overlay(IKey.lang("drtech.drone.programmer.apply"))
                        .setEnabledIf(widget -> clientInspectorPage == 0 && canvas.isSelectedEntitySelector())
                        .onMousePressed(mouse -> { canvas.applySelectedEntityAdvancedText(clientEntitySearch); return true; }))
                .child(new ButtonWidget<>().pos(447, 246).size(66, 16)
                        .overlay(IKey.lang("drtech.drone.programmer.clear"))
                        .setEnabledIf(widget -> clientInspectorPage == 0 && canvas.isSelectedEntitySelector())
                        .onMousePressed(mouse -> { canvas.clearSelectedEntityAdvancedValue(); return true; }))
                .child(IKey.lang("drtech.drone.programmer.dock_search").asWidget().pos(379, 122).size(24, 10)
                        .setEnabledIf(widget -> clientInspectorPage == 0 && canvas.isSelectedDockReference()))
                .child(new TextFieldWidget().pos(404, 118).size(109, 16).setMaxLength(96)
                        .setEnabledIf(widget -> clientInspectorPage == 0 && canvas.isSelectedDockReference())
                        .value(new StringValue.Dynamic(this::getClientDockSearch, this::setClientDockSearch)))
                .child(new ButtonWidget<>().pos(379, 137).size(134, 16)
                        .overlay(IKey.dynamic(this::getClientDockResultLabel))
                        .tooltipStatic(tooltip -> tooltip.addLine(IKey.dynamic(this::getClientDockResultTooltip)))
                        .setEnabledIf(widget -> clientInspectorPage == 0 && canvas.isSelectedDockReference())
                        .onMousePressed(mouse -> { selectClientDockResult(canvas); return true; }))
                .child(actionButton("<", 379, 156, () -> moveClientDockResult(-1))
                        .setEnabledIf(widget -> clientInspectorPage == 0 && canvas.isSelectedDockReference()
                                && hasClientDockResult()))
                .child(actionButton(">", 416, 156, () -> moveClientDockResult(1))
                        .setEnabledIf(widget -> clientInspectorPage == 0 && canvas.isSelectedDockReference()
                                && hasClientDockResult()))
                .child(IKey.dynamic(this::getClientDockResultPage).asWidget().pos(454, 160).size(59, 10)
                        .setEnabledIf(widget -> clientInspectorPage == 0 && canvas.isSelectedDockReference()))
                .child(IKey.dynamic(() -> getClientDockResultStatus(canvas)).asWidget().pos(379, 175).size(134, 19)
                        .setEnabledIf(widget -> clientInspectorPage == 0 && canvas.isSelectedDockReference()))
                .child(new ButtonWidget<>().pos(379, 195).size(134, 16)
                        .overlay(IKey.lang("drtech.drone.programmer.clear"))
                        .setEnabledIf(widget -> clientInspectorPage == 0 && canvas.isSelectedDockReference())
                        .onMousePressed(mouse -> { canvas.clearSelectedProperty(); return true; }))
                .child(IKey.lang("drtech.drone.programmer.dock_selector_help").asWidget()
                        .pos(379, 214).size(134, 27)
                        .setEnabledIf(widget -> clientInspectorPage == 0 && canvas.isSelectedDockReference()))
                .child(IKey.lang("drtech.drone.programmer.program_search").asWidget().pos(379, 122).size(24, 10)
                        .setEnabledIf(widget -> clientInspectorPage == 0 && canvas.isSelectedProgramReference()))
                .child(new TextFieldWidget().pos(404, 118).size(109, 16).setMaxLength(96)
                        .setEnabledIf(widget -> clientInspectorPage == 0 && canvas.isSelectedProgramReference())
                        .value(new StringValue.Dynamic(this::getClientLibraryProgramSearch,
                                this::setClientLibraryProgramSearch)))
                .child(new ButtonWidget<>().pos(379, 137).size(134, 16)
                        .overlay(IKey.dynamic(this::getClientLibraryProgramResultLabel))
                        .tooltipStatic(tooltip -> tooltip.addLine(IKey.dynamic(this::getClientLibraryProgramTooltip)))
                        .setEnabledIf(widget -> clientInspectorPage == 0 && canvas.isSelectedProgramReference())
                        .onMousePressed(mouse -> { selectClientLibraryProgramResult(canvas); return true; }))
                .child(actionButton("<", 379, 156, () -> moveClientLibraryProgramResult(-1))
                        .setEnabledIf(widget -> clientInspectorPage == 0 && canvas.isSelectedProgramReference()
                                && hasClientLibraryProgramResult()))
                .child(actionButton(">", 416, 156, () -> moveClientLibraryProgramResult(1))
                        .setEnabledIf(widget -> clientInspectorPage == 0 && canvas.isSelectedProgramReference()
                                && hasClientLibraryProgramResult()))
                .child(IKey.dynamic(this::getClientLibraryProgramPage).asWidget().pos(454, 160).size(59, 10)
                        .setEnabledIf(widget -> clientInspectorPage == 0 && canvas.isSelectedProgramReference()))
                .child(IKey.dynamic(() -> getClientLibraryProgramStatus(canvas)).asWidget()
                        .pos(379, 175).size(134, 16)
                        .setEnabledIf(widget -> clientInspectorPage == 0 && canvas.isSelectedProgramReference()))
                .child(new ButtonWidget<>().pos(379, 195).size(134, 16)
                        .overlay(IKey.lang("drtech.drone.programmer.clear"))
                        .setEnabledIf(widget -> clientInspectorPage == 0 && canvas.isSelectedProgramReference())
                        .onMousePressed(mouse -> { canvas.clearSelectedProperty(); return true; }))
                .child(IKey.lang("drtech.drone.programmer.program_selector_help").asWidget()
                        .pos(379, 214).size(134, 27)
                        .setEnabledIf(widget -> clientInspectorPage == 0 && canvas.isSelectedProgramReference()))
                .child(new ButtonWidget<>().pos(379, 118).size(72, 16)
                        .overlay(IKey.lang("drtech.drone.programmer.target_block"))
                        .setEnabledIf(widget -> clientInspectorPage == 0 && canvas.isSelectedBlockSelector())
                        .onMousePressed(mouse -> { canvas.activateSelectedProperty(); return true; }))
                .child(new ButtonWidget<>().pos(453, 118).size(60, 16)
                        .overlay(IKey.lang("drtech.drone.programmer.clear"))
                        .setEnabledIf(widget -> clientInspectorPage == 0 && canvas.isSelectedBlockSelector())
                        .onMousePressed(mouse -> { canvas.clearSelectedProperty(); return true; }))
                .child(new ButtonWidget<>().pos(379, 137).size(134, 16)
                        .overlay(IKey.dynamic(canvas::getSelectedBlockStatePropertyLabel))
                        .tooltipStatic(tooltip -> tooltip.addLine(IKey.lang("drtech.drone.programmer.block_state_cycle_help")))
                        .setEnabledIf(widget -> clientInspectorPage == 0 && canvas.isSelectedBlockSelector())
                        .onMousePressed(mouse -> { canvas.cycleSelectedBlockStatePropertyValue(); return true; }))
                .child(actionButton("<", 379, 156, () -> canvas.moveSelectedBlockStateProperty(-1))
                        .setEnabledIf(widget -> clientInspectorPage == 0 && canvas.isSelectedBlockSelector()
                                && canvas.hasSelectedBlockStateProperty()))
                .child(actionButton(">", 416, 156, () -> canvas.moveSelectedBlockStateProperty(1))
                        .setEnabledIf(widget -> clientInspectorPage == 0 && canvas.isSelectedBlockSelector()
                                && canvas.hasSelectedBlockStateProperty()))
                .child(IKey.dynamic(canvas::getSelectedBlockStatePropertyPage).asWidget().pos(454, 160).size(59, 10)
                        .setEnabledIf(widget -> clientInspectorPage == 0 && canvas.isSelectedBlockSelector()))
                .child(new ButtonWidget<>().pos(379, 175).size(72, 16)
                        .overlay(IKey.dynamic(canvas::getSelectedBlockFilterModeLabel))
                        .setEnabledIf(widget -> clientInspectorPage == 0 && canvas.isSelectedBlockSelector())
                        .onMousePressed(mouse -> { canvas.toggleSelectedItemFilterMode(); return true; }))
                .child(new ButtonWidget<>().pos(453, 175).size(60, 16)
                        .overlay(IKey.lang("drtech.drone.programmer.remove_rule"))
                        .setEnabledIf(widget -> clientInspectorPage == 0 && canvas.isSelectedBlockSelector())
                        .onMousePressed(mouse -> { canvas.removeLastSelectedItemFilterRule(); return true; }))
                .child(new ButtonWidget<>().pos(379, 195).size(43, 16)
                        .overlay(IKey.dynamic(canvas::getSelectedBlockNamespaceLabel))
                        .setEnabledIf(widget -> clientInspectorPage == 0 && canvas.isSelectedBlockSelector())
                        .onMousePressed(mouse -> { canvas.generalizeLastSelectedBlockRuleToNamespace(); return true; }))
                .child(new ButtonWidget<>().pos(424, 195).size(43, 16)
                        .overlay(IKey.dynamic(canvas::getSelectedBlockTileEntityLabel))
                        .setEnabledIf(widget -> clientInspectorPage == 0 && canvas.isSelectedBlockSelector())
                        .onMousePressed(mouse -> { canvas.cycleSelectedBlockTileEntityRequirement(); return true; }))
                .child(new ButtonWidget<>().pos(469, 195).size(44, 16)
                        .overlay(IKey.dynamic(canvas::getSelectedBlockReplaceableLabel))
                        .setEnabledIf(widget -> clientInspectorPage == 0 && canvas.isSelectedBlockSelector())
                        .onMousePressed(mouse -> { canvas.cycleSelectedBlockReplaceableRequirement(); return true; }))
                .child(new ButtonWidget<>().pos(379, 214).size(66, 16)
                        .overlay(IKey.dynamic(canvas::getSelectedBlockOreLabel))
                        .setEnabledIf(widget -> clientInspectorPage == 0 && canvas.isSelectedBlockSelector())
                        .onMousePressed(mouse -> { canvas.cycleSelectedBlockOreDictionary(); return true; }))
                .child(new ButtonWidget<>().pos(447, 214).size(66, 16)
                        .overlay(IKey.dynamic(canvas::getSelectedBlockCategoryLabel))
                        .setEnabledIf(widget -> clientInspectorPage == 0 && canvas.isSelectedBlockSelector())
                        .onMousePressed(mouse -> { canvas.cycleSelectedBlockCategory(); return true; }))
                .child(IKey.lang("drtech.drone.programmer.node_label").asWidget().pos(379, 195)
                        .setEnabledIf(widget -> clientInspectorPage == 0 && !canvas.isSelectedFluidSelector()
                                && !canvas.isSelectedItemSelector() && !canvas.isSelectedEntitySelector()
                                && !canvas.isSelectedBlockSelector()
                                && !canvas.isSelectedAreaPreviewNode()
                                && !canvas.isSelectedChoiceProperty()
                                && !canvas.isSelectedCoordinateCaptureTarget()))
                .child(new TextFieldWidget().pos(379, 206).size(134, 16).setMaxLength(32)
                        .setEnabledIf(widget -> clientInspectorPage == 0 && !canvas.isSelectedFluidSelector()
                                && !canvas.isSelectedItemSelector() && !canvas.isSelectedEntitySelector()
                                && !canvas.isSelectedBlockSelector()
                                && !canvas.isSelectedAreaPreviewNode()
                                && !canvas.isSelectedChoiceProperty()
                                && !canvas.isSelectedCoordinateCaptureTarget())
                        .value(new StringValue.Dynamic(canvas::getSelectedNodeLabel, canvas::setSelectedNodeLabel)))
                .child(new ButtonWidget<>().pos(379, 225).size(65, 16)
                        .overlay(IKey.dynamic(canvas::getSelectedBreakpointLabel))
                        .tooltipStatic(tooltip -> tooltip.addLine(IKey.lang("drtech.drone.programmer.breakpoint_help")))
                        .setEnabledIf(widget -> clientInspectorPage == 0 && !canvas.isSelectedFluidSelector()
                                && !canvas.isSelectedItemSelector() && !canvas.isSelectedEntitySelector()
                                && !canvas.isSelectedBlockSelector() && !canvas.isSelectedAreaPreviewNode()
                                && !canvas.isSelectedCoordinateCaptureTarget())
                        .onMousePressed(mouse -> { canvas.toggleSelectedBreakpoint(); return true; }))
                .child(new ButtonWidget<>().pos(448, 225).size(65, 16)
                        .overlay(IKey.dynamic(canvas::getSelectedConditionalBreakpointLabel))
                        .tooltipStatic(tooltip -> tooltip.addLine(IKey.lang("drtech.drone.programmer.breakpoint_condition_help")))
                        .setEnabledIf(widget -> clientInspectorPage == 0 && !canvas.isSelectedFluidSelector()
                                && !canvas.isSelectedItemSelector() && !canvas.isSelectedEntitySelector()
                                && !canvas.isSelectedBlockSelector() && !canvas.isSelectedAreaPreviewNode()
                                && !canvas.isSelectedCoordinateCaptureTarget())
                        .onMousePressed(mouse -> { canvas.cycleSelectedConditionalBreakpoint(); return true; }))
                 .child(new ButtonWidget<>().pos(379, 195).size(65, 16)
                         .overlay(IKey.lang("drtech.drone.programmer.capture_player"))
                         .tooltipStatic(tooltip -> tooltip.addLine(IKey.lang("drtech.drone.programmer.capture_help")))
                         .setEnabledIf(widget -> clientInspectorPage == 0 && canvas.canCapturePlayerCoordinate()
                                 && !canvas.isSelectedAreaPreviewNode())
                         .onMousePressed(mouse -> { canvas.capturePlayerCoordinate(); return true; }))
                 .child(new ButtonWidget<>().pos(448, 195).size(65, 16)
                         .overlay(IKey.lang("drtech.drone.programmer.capture_target"))
                         .tooltipStatic(tooltip -> tooltip.addLine(IKey.lang("drtech.drone.programmer.capture_help")))
                         .setEnabledIf(widget -> clientInspectorPage == 0 && canvas.canCaptureTargetedCoordinate()
                                 && !canvas.isSelectedAreaPreviewNode())
                         .onMousePressed(mouse -> { canvas.captureTargetedCoordinate(); return true; }))
                 .child(new ButtonWidget<>().pos(379, 213).size(65, 16)
                         .overlay(IKey.lang("drtech.drone.programmer.capture_dock"))
                         .tooltipStatic(tooltip -> tooltip.addLine(IKey.lang("drtech.drone.programmer.capture_dock.help")))
                         .setEnabledIf(widget -> clientInspectorPage == 0 && canvas.canCaptureDockCoordinate()
                                 && !canvas.isSelectedAreaPreviewNode())
                         .onMousePressed(mouse -> { canvas.captureDockCoordinate(); return true; }))
                 .child(new ButtonWidget<>().pos(448, 213).size(65, 16)
                         .overlay(IKey.lang("drtech.drone.programmer.capture_drone"))
                         .tooltipStatic(tooltip -> tooltip.addLine(IKey.lang("drtech.drone.programmer.capture_drone.help")))
                         .setEnabledIf(widget -> clientInspectorPage == 0 && canvas.canCaptureDroneCoordinate()
                                 && !canvas.isSelectedAreaPreviewNode())
                         .onMousePressed(mouse -> { canvas.captureDroneCoordinate(); return true; }))
                 .child(new ButtonWidget<>().pos(379, 231).size(134, 16)
                         .overlay(IKey.lang("drtech.drone.programmer.capture_world"))
                         .tooltipStatic(tooltip -> tooltip.addLine(IKey.lang("drtech.drone.programmer.capture_world.help")))
                         .setEnabledIf(widget -> clientInspectorPage == 0
                                 && canvas.isSelectedCoordinateCaptureTarget()
                                 && !canvas.isSelectedAreaPreviewNode() && clientEditable)
                         .onMousePressed(mouse -> {
                             beginClientWorldSelection(syncManager, canvas);
                             return true;
                         }))
                .child(new ButtonWidget<>().pos(379, 44).size(42, 16)
                        .overlay(IKey.dynamic(() -> I18n.format(clientRemoteDebugPage == 0
                                ? "drtech.drone.remote.status_active" : "drtech.drone.remote.status")))
                        .setEnabledIf(widget -> clientInspectorPage == 1)
                        .onMousePressed(mouse -> { clientRemoteDebugPage = 0; return true; }))
                .child(new ButtonWidget<>().pos(423, 44).size(43, 16)
                        .overlay(IKey.dynamic(() -> I18n.format(clientRemoteDebugPage == 1
                                ? "drtech.drone.remote.ports_active" : "drtech.drone.remote.ports")))
                        .setEnabledIf(widget -> clientInspectorPage == 1)
                        .onMousePressed(mouse -> { clientRemoteDebugPage = 1; return true; }))
                .child(new ButtonWidget<>().pos(468, 44).size(44, 16)
                        .overlay(IKey.dynamic(() -> I18n.format(clientRemoteDebugPage == 2
                                ? "drtech.drone.remote.navigation_active" : "drtech.drone.remote.navigation")))
                        .setEnabledIf(widget -> clientInspectorPage == 1)
                        .onMousePressed(mouse -> { clientRemoteDebugPage = 2; return true; }))
                .child(IKey.dynamic(this::getRemoteDebugText).asWidget().pos(379, 64).size(134, 40)
                        .setEnabledIf(widget -> clientInspectorPage == 1 && clientRemoteDebugPage == 0))
                .child(IKey.dynamic(this::getRemoteActionText).asWidget().pos(379, 108).size(134, 10)
                        .setEnabledIf(widget -> clientInspectorPage == 1 && clientRemoteDebugPage == 0))
                .child(IKey.dynamic(this::getRemoteActionErrorText).scale(0.72F).asWidget().pos(379, 120).size(134, 10)
                        .setEnabledIf(widget -> clientInspectorPage == 1 && clientRemoteDebugPage == 0))
                .child(IKey.lang("drtech.drone.remote.variables").asWidget().pos(379, 134).size(60, 10)
                        .setEnabledIf(widget -> clientInspectorPage == 1 && clientRemoteDebugPage == 0))
                .child(IKey.dynamic(this::getRemoteVariablePageLabel).asWidget().pos(454, 134).size(59, 10)
                        .setEnabledIf(widget -> clientInspectorPage == 1 && clientRemoteDebugPage == 0))
                .child(new DroneDiagnosticScrollWidget(this::moveRemoteVariableOffset).pos(379, 146).size(134, 50)
                        .setEnabledIf(widget -> clientInspectorPage == 1 && clientRemoteDebugPage == 0))
                .child(IKey.dynamic(() -> getRemoteVariableRow(0)).asWidget().pos(381, 146).size(130, 14)
                        .setEnabledIf(widget -> clientInspectorPage == 1 && clientRemoteDebugPage == 0))
                .child(IKey.dynamic(() -> getRemoteVariableRow(1)).asWidget().pos(381, 162).size(130, 14)
                        .setEnabledIf(widget -> clientInspectorPage == 1 && clientRemoteDebugPage == 0))
                .child(IKey.dynamic(() -> getRemoteVariableRow(2)).asWidget().pos(381, 178).size(130, 14)
                        .setEnabledIf(widget -> clientInspectorPage == 1 && clientRemoteDebugPage == 0))
                .child(remoteTraceFilterButton(0, 379)
                        .setEnabledIf(widget -> clientInspectorPage == 1 && clientRemoteDebugPage == 0))
                .child(remoteTraceFilterButton(1, 424)
                        .setEnabledIf(widget -> clientInspectorPage == 1 && clientRemoteDebugPage == 0))
                .child(remoteTraceFilterButton(2, 469)
                        .setEnabledIf(widget -> clientInspectorPage == 1 && clientRemoteDebugPage == 0))
                .child(new DroneDiagnosticScrollWidget(this::moveRemoteTraceOffset).pos(379, 216).size(134, 23)
                        .setEnabledIf(widget -> clientInspectorPage == 1 && clientRemoteDebugPage == 0))
                .child(remoteTraceRowButton(canvas, 0, 216)
                        .setEnabledIf(widget -> clientInspectorPage == 1 && clientRemoteDebugPage == 0))
                .child(remoteTraceRowButton(canvas, 1, 228)
                        .setEnabledIf(widget -> clientInspectorPage == 1 && clientRemoteDebugPage == 0))
                .child(IKey.dynamic(this::getRemotePortNodeText).asWidget().pos(379, 64).size(134, 10)
                        .setEnabledIf(widget -> clientInspectorPage == 1 && clientRemoteDebugPage == 1))
                .child(IKey.lang("drtech.drone.remote.port_inputs").asWidget().pos(379, 78).size(70, 10)
                        .setEnabledIf(widget -> clientInspectorPage == 1 && clientRemoteDebugPage == 1))
                .child(IKey.dynamic(this::getRemotePortPageLabel).asWidget().pos(454, 78).size(59, 10)
                        .setEnabledIf(widget -> clientInspectorPage == 1 && clientRemoteDebugPage == 1))
                .child(new DroneDiagnosticScrollWidget(this::moveRemotePortOffset).pos(379, 92).size(134, 104)
                        .setEnabledIf(widget -> clientInspectorPage == 1 && clientRemoteDebugPage == 1))
                .child(IKey.dynamic(() -> getRemotePortRow(0)).asWidget().pos(381, 94).size(130, 12)
                        .setEnabledIf(widget -> clientInspectorPage == 1 && clientRemoteDebugPage == 1))
                .child(IKey.dynamic(() -> getRemotePortRow(1)).asWidget().pos(381, 110).size(130, 12)
                        .setEnabledIf(widget -> clientInspectorPage == 1 && clientRemoteDebugPage == 1))
                .child(IKey.dynamic(() -> getRemotePortRow(2)).asWidget().pos(381, 126).size(130, 12)
                        .setEnabledIf(widget -> clientInspectorPage == 1 && clientRemoteDebugPage == 1))
                .child(IKey.dynamic(() -> getRemotePortRow(3)).asWidget().pos(381, 142).size(130, 12)
                        .setEnabledIf(widget -> clientInspectorPage == 1 && clientRemoteDebugPage == 1))
                .child(IKey.dynamic(() -> getRemotePortRow(4)).asWidget().pos(381, 158).size(130, 12)
                        .setEnabledIf(widget -> clientInspectorPage == 1 && clientRemoteDebugPage == 1))
                .child(IKey.dynamic(() -> getRemotePortRow(5)).asWidget().pos(381, 174).size(130, 12)
                        .setEnabledIf(widget -> clientInspectorPage == 1 && clientRemoteDebugPage == 1))
                .child(IKey.dynamic(this::getRemoteOutputText).asWidget().pos(379, 204).size(134, 28)
                        .setEnabledIf(widget -> clientInspectorPage == 1 && clientRemoteDebugPage == 1))
                .child(IKey.dynamic(this::getRemoteNavigationText).asWidget().pos(379, 64).size(134, 74)
                        .setEnabledIf(widget -> clientInspectorPage == 1 && clientRemoteDebugPage == 2))
                .child(new ButtonWidget<>().pos(379, 142).size(134, 16)
                        .overlay(IKey.dynamic(this::getRemoteCoordinateHighlightLabel))
                        .tooltipStatic(tooltip -> tooltip.addLine(
                                IKey.lang("drtech.drone.remote.coordinate_highlight.help")))
                        .setEnabledIf(widget -> clientInspectorPage == 1 && clientRemoteDebugPage == 2)
                        .onMousePressed(mouse -> { toggleRemoteCoordinateHighlight(); return true; }))
                .child(new ButtonWidget<>().pos(379, 160).size(134, 16)
                        .overlay(IKey.dynamic(this::getRemotePathPreviewLabel))
                        .tooltipStatic(tooltip -> tooltip.addLine(IKey.lang("drtech.drone.remote.path_preview.help")))
                        .setEnabledIf(widget -> clientInspectorPage == 1 && clientRemoteDebugPage == 2)
                        .onMousePressed(mouse -> { toggleRemotePathPreview(); return true; }))
                .child(remoteButton(IKey.lang("drtech.drone.controller.pause"), "PAUSE", 379, 250, syncManager)
                        .setEnabledIf(widget -> clientInspectorPage == 1))
                .child(remoteButton(IKey.lang("drtech.drone.controller.resume"), "RESUME", 404, 250, syncManager)
                        .setEnabledIf(widget -> clientInspectorPage == 1))
                .child(remoteButton(IKey.lang("drtech.drone.controller.step_into"), "STEP_INTO", 429, 250, syncManager)
                        .setEnabledIf(widget -> clientInspectorPage == 1))
                .child(remoteButton(IKey.lang("drtech.drone.controller.step_over"), "STEP_OVER", 454, 250, syncManager)
                        .setEnabledIf(widget -> clientInspectorPage == 1))
                .child(IKey.dynamic(this::getDiagnosticSummary).asWidget().pos(379, 45).size(134, 16)
                        .setEnabledIf(widget -> clientInspectorPage == 2))
                .child(diagnosticFilterButton(0, 379))
                .child(diagnosticFilterButton(1, 413))
                .child(diagnosticFilterButton(2, 447))
                .child(diagnosticFilterButton(3, 481))
                .child(new DroneDiagnosticScrollWidget(this::moveDiagnosticPage).pos(379, 82).size(134, 96)
                        .setEnabledIf(widget -> clientInspectorPage == 2))
                .child(diagnosticRowButton(canvas, 0, 84))
                .child(diagnosticRowButton(canvas, 1, 116))
                .child(diagnosticRowButton(canvas, 2, 148))
                .child(actionButton("<", 379, 181, () -> moveDiagnosticPage(-1))
                        .setEnabledIf(widget -> clientInspectorPage == 2 && getDiagnosticPageCount() > 1))
                .child(actionButton(">", 416, 181, () -> moveDiagnosticPage(1))
                        .setEnabledIf(widget -> clientInspectorPage == 2 && getDiagnosticPageCount() > 1))
                .child(IKey.dynamic(this::getDiagnosticPageLabel).asWidget().pos(454, 185).size(59, 10)
                        .setEnabledIf(widget -> clientInspectorPage == 2))
                .child(IKey.lang("drtech.drone.diagnostic.click_to_locate").asWidget().pos(379, 201).size(134, 42)
                        .setEnabledIf(widget -> clientInspectorPage == 2))
                .child(SlotGroupWidget.playerInventory(false).disableSortButtons().left(7).bottom(7))
                .child(canvas)
                 .child(new DroneAreaPreviewWidget(canvas::getSelectedAreaPreview,
                         canvas::getSelectedAreaPreviewStatus).pos(379, 193).size(134, 43)
                         .setEnabledIf(widget -> clientInspectorPage == 0 && canvas.isSelectedAreaPreviewNode()))
                 .child(new ButtonWidget<>().pos(379, 238).size(66, 16)
                         .overlay(IKey.lang("drtech.drone.programmer.capture_world.short"))
                         .tooltipStatic(tooltip -> tooltip.addLine(IKey.lang("drtech.drone.programmer.capture_world.help")))
                         .setEnabledIf(widget -> clientInspectorPage == 0
                                 && canvas.isSelectedAreaCaptureTarget() && clientEditable)
                         .onMousePressed(mouse -> {
                             beginClientWorldSelection(syncManager, canvas);
                             return true;
                         }))
                 .child(new ButtonWidget<>().pos(447, 238).size(66, 16)
                         .overlay(IKey.dynamic(this::getAreaProjectionLabel))
                         .tooltipStatic(tooltip -> tooltip.addLine(
                                 IKey.lang("drtech.drone.programmer.area_project.help")))
                         .setEnabledIf(widget -> clientInspectorPage == 0 && canvas.isSelectedAreaCaptureTarget()
                                 && canvas.getSelectedAreaPreview() != null)
                         .onMousePressed(mouse -> { toggleAreaProjection(canvas); return true; }))
                 .child(new ButtonWidget<>().pos(379, 238).size(134, 16)
                         .overlay(IKey.dynamic(this::getAreaProjectionLabel))
                         .tooltipStatic(tooltip -> tooltip.addLine(
                                 IKey.lang("drtech.drone.programmer.area_project.help")))
                         .setEnabledIf(widget -> clientInspectorPage == 0 && canvas.isSelectedAreaPreviewNode()
                                 && !canvas.isSelectedAreaCaptureTarget()
                                 && canvas.getSelectedAreaPreview() != null)
                         .onMousePressed(mouse -> { toggleAreaProjection(canvas); return true; }))
                .child(new DronePropertyChoiceWidget(canvas::getSelectedChoiceValues,
                        canvas::getSelectedChoiceValue, canvas::isSelectedDirectionProperty,
                        canvas::selectPropertyChoice).pos(379, 118).size(134, 88)
                        .setEnabledIf(widget -> clientInspectorPage == 0 && canvas.isSelectedChoiceProperty()));
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

    private String getClientItemSearch() {
        return clientItemSearch;
    }

    private void setClientItemSearch(String value) {
        clientItemSearch = value == null ? "" : value;
        clientItemResultIndex = 0;
        clientItemCacheQuery = null;
    }

    private void toggleClientItemSearchMode() {
        clientItemOreMode = !clientItemOreMode;
        clientItemResultIndex = 0;
        clientItemCacheQuery = null;
    }

    private List<ClientItemResult> getClientItemResults() {
        String query = clientItemSearch == null ? ""
                : clientItemSearch.trim().toLowerCase(Locale.ROOT);
        if (query.equals(clientItemCacheQuery) && clientItemOreMode == clientItemCacheOreMode) {
            return clientItemResults;
        }
        List<ClientItemResult> matches = new ArrayList<>();
        if (clientItemOreMode) {
            for (String oreName : OreDictionary.getOreNames()) {
                if (query.isEmpty() || oreName.toLowerCase(Locale.ROOT).contains(query)) {
                    matches.add(new ClientItemResult(oreName, getOreDisplayName(oreName), true));
                }
            }
        } else {
            for (Item item : Item.REGISTRY) {
                ResourceLocation id = item.getRegistryName();
                if (id == null) continue;
                String localized = getLocalizedItemName(item);
                if (query.isEmpty() || id.toString().toLowerCase(Locale.ROOT).contains(query)
                        || localized.toLowerCase(Locale.ROOT).contains(query)) {
                    matches.add(new ClientItemResult(id.toString(), localized, false));
                }
            }
        }
        matches.sort((left, right) -> String.CASE_INSENSITIVE_ORDER.compare(left.value, right.value));
        clientItemCacheQuery = query;
        clientItemCacheOreMode = clientItemOreMode;
        clientItemResults = Collections.unmodifiableList(matches);
        if (matches.isEmpty()) clientItemResultIndex = 0;
        else clientItemResultIndex = Math.floorMod(clientItemResultIndex, matches.size());
        return clientItemResults;
    }

    private boolean hasClientItemResult() {
        return !getClientItemResults().isEmpty();
    }

    private ClientItemResult getClientItemResult() {
        List<ClientItemResult> results = getClientItemResults();
        return results.isEmpty() ? null
                : results.get(Math.floorMod(clientItemResultIndex, results.size()));
    }

    private String getClientItemResultLabel() {
        ClientItemResult result = getClientItemResult();
        if (result == null) return I18n.format("drtech.drone.programmer.item_no_results");
        String label = result.ore ? result.value : result.displayName;
        if (label == null || label.isEmpty()) label = result.value;
        return label.length() > 20 ? label.substring(0, 19) + "..." : label;
    }

    private String getClientItemResultTooltip() {
        ClientItemResult result = getClientItemResult();
        if (result == null) return I18n.format("drtech.drone.programmer.item_no_results");
        if (result.ore) return result.value + (result.displayName.isEmpty() ? "" : " | " + result.displayName);
        return result.displayName + " | " + result.value;
    }

    private String getClientItemResultPage() {
        List<ClientItemResult> results = getClientItemResults();
        return results.isEmpty() ? "0/0" : (clientItemResultIndex + 1) + "/" + results.size();
    }

    private void moveClientItemResult(int delta) {
        List<ClientItemResult> results = getClientItemResults();
        if (!results.isEmpty()) clientItemResultIndex = Math.floorMod(clientItemResultIndex + delta, results.size());
    }

    private void selectClientItemResult(DroneProgramCanvasWidget canvas) {
        ClientItemResult result = getClientItemResult();
        if (result == null) return;
        if (result.ore) canvas.selectOreDictionaryProperty(result.value);
        else canvas.selectRegistryItemProperty(result.value);
    }

    private static String getLocalizedItemName(Item item) {
        try {
            return new ItemStack(item).getDisplayName();
        } catch (RuntimeException ignored) {
            ResourceLocation id = item.getRegistryName();
            return id == null ? "" : id.toString();
        }
    }

    private static String getOreDisplayName(String oreName) {
        List<ItemStack> ores = OreDictionary.getOres(oreName, false);
        if (ores.isEmpty()) return "";
        ItemStack sample = ores.get(0);
        try {
            return sample.isEmpty() ? "" : sample.getDisplayName();
        } catch (RuntimeException ignored) {
            return "";
        }
    }

    private static final class ClientItemResult {
        private final String value;
        private final String displayName;
        private final boolean ore;

        private ClientItemResult(String value, String displayName, boolean ore) {
            this.value = value;
            this.displayName = displayName == null ? "" : displayName;
            this.ore = ore;
        }
    }

    private String getClientEntitySearch() {
        return clientEntitySearch;
    }

    private void setClientEntitySearch(String value) {
        clientEntitySearch = value == null ? "" : value;
        clientEntityResultIndex = 0;
        clientEntityCacheQuery = null;
    }

    private List<ClientEntityResult> getClientEntityResults() {
        String query = clientEntitySearch == null ? ""
                : clientEntitySearch.trim().toLowerCase(Locale.ROOT);
        if (query.equals(clientEntityCacheQuery)) return clientEntityResults;
        List<ClientEntityResult> matches = new ArrayList<>();
        for (ResourceLocation id : EntityList.getEntityNameList()) {
            String displayName = getLocalizedEntityName(id);
            if (query.isEmpty() || id.toString().toLowerCase(Locale.ROOT).contains(query)
                    || displayName.toLowerCase(Locale.ROOT).contains(query)) {
                matches.add(new ClientEntityResult(id.toString(), displayName));
            }
        }
        matches.sort((left, right) -> String.CASE_INSENSITIVE_ORDER.compare(left.entityId, right.entityId));
        clientEntityCacheQuery = query;
        clientEntityResults = Collections.unmodifiableList(matches);
        if (matches.isEmpty()) clientEntityResultIndex = 0;
        else clientEntityResultIndex = Math.floorMod(clientEntityResultIndex, matches.size());
        return clientEntityResults;
    }

    private boolean hasClientEntityResult() {
        return !getClientEntityResults().isEmpty();
    }

    private ClientEntityResult getClientEntityResult() {
        List<ClientEntityResult> results = getClientEntityResults();
        return results.isEmpty() ? null
                : results.get(Math.floorMod(clientEntityResultIndex, results.size()));
    }

    private String getClientEntityResultLabel() {
        ClientEntityResult result = getClientEntityResult();
        if (result == null) return I18n.format("drtech.drone.programmer.entity_no_results");
        String label = result.displayName.isEmpty() ? result.entityId : result.displayName;
        return label.length() > 20 ? label.substring(0, 19) + "..." : label;
    }

    private String getClientEntityResultTooltip() {
        ClientEntityResult result = getClientEntityResult();
        if (result == null) return I18n.format("drtech.drone.programmer.entity_no_results");
        return result.displayName + " | " + result.entityId;
    }

    private String getClientEntityResultPage() {
        List<ClientEntityResult> results = getClientEntityResults();
        return results.isEmpty() ? "0/0" : (clientEntityResultIndex + 1) + "/" + results.size();
    }

    private void moveClientEntityResult(int delta) {
        List<ClientEntityResult> results = getClientEntityResults();
        if (!results.isEmpty()) {
            clientEntityResultIndex = Math.floorMod(clientEntityResultIndex + delta, results.size());
        }
    }

    private void selectClientEntityResult(DroneProgramCanvasWidget canvas) {
        ClientEntityResult result = getClientEntityResult();
        if (result != null) canvas.selectEntityProperty(result.entityId);
    }

    private static String getLocalizedEntityName(ResourceLocation id) {
        String translationName = EntityList.getTranslationName(id);
        if (translationName == null || translationName.isEmpty()) return id.toString();
        String key = "entity." + translationName + ".name";
        String translated = I18n.format(key);
        return key.equals(translated) ? id.toString() : translated;
    }

    private static final class ClientEntityResult {
        private final String entityId;
        private final String displayName;

        private ClientEntityResult(String entityId, String displayName) {
            this.entityId = entityId;
            this.displayName = displayName == null ? "" : displayName;
        }
    }

    private String getClientDockSearch() {
        return clientDockSearch;
    }

    private void setClientDockSearch(String value) {
        clientDockSearch = value == null ? "" : value;
        clientDockResultIndex = 0;
    }

    private List<ClientDockResult> getClientDockResults() {
        String query = clientDockSearch == null ? "" : clientDockSearch.trim().toLowerCase(Locale.ROOT);
        if (query.isEmpty()) return clientDockDirectory;
        List<ClientDockResult> matches = new ArrayList<>();
        for (ClientDockResult dock : clientDockDirectory) {
            String coordinates = dock.position.getX() + "," + dock.position.getY() + "," + dock.position.getZ();
            if (dock.name.toLowerCase(Locale.ROOT).contains(query)
                    || dock.dockId.toString().toLowerCase(Locale.ROOT).contains(query)
                    || coordinates.contains(query)) matches.add(dock);
        }
        return matches;
    }

    private boolean hasClientDockResult() {
        return !getClientDockResults().isEmpty();
    }

    private ClientDockResult getClientDockResult() {
        List<ClientDockResult> results = getClientDockResults();
        if (results.isEmpty()) return null;
        clientDockResultIndex = Math.floorMod(clientDockResultIndex, results.size());
        return results.get(clientDockResultIndex);
    }

    /** The current directory result is already owner- and dimension-filtered by the server. */
    private BlockPos getClientDockCapturePosition() {
        ClientDockResult result = getClientDockResult();
        return result == null ? null : result.position;
    }

    /** Only supplied by the server while a wireless-debugged drone is in this programmer's dimension. */
    private BlockPos getClientRemoteCapturePosition() {
        return clientRemoteConnected ? clientRemotePosition : null;
    }

    private String getClientDockResultLabel() {
        ClientDockResult result = getClientDockResult();
        if (result == null) return I18n.format("drtech.drone.programmer.dock_no_results");
        String label = result.name.isEmpty() ? result.dockId.toString() : result.name;
        return label.length() > 20 ? label.substring(0, 19) + "..." : label;
    }

    private String getClientDockResultTooltip() {
        ClientDockResult result = getClientDockResult();
        if (result == null) return I18n.format("drtech.drone.programmer.dock_no_results");
        return result.name + " | " + result.dockId + " | " + result.position.getX() + ", "
                + result.position.getY() + ", " + result.position.getZ() + " | "
                + getClientDockLoadLine(result) + " | " + getClientDockRejectionReason(result);
    }

    private String getClientDockResultPage() {
        List<ClientDockResult> results = getClientDockResults();
        return results.isEmpty() ? "0/0" : (Math.floorMod(clientDockResultIndex, results.size()) + 1)
                + "/" + results.size();
    }

    private String getClientDockResultStatus(DroneProgramCanvasWidget canvas) {
        UUID selectedId = canvas.getSelectedDockReferenceId();
        if (selectedId != null && clientDockDirectory.stream().noneMatch(dock -> dock.dockId.equals(selectedId))) {
            return I18n.format("drtech.drone.programmer.dock_reference_stale");
        }
        ClientDockResult result = getClientDockResult();
        if (result == null) return I18n.format("drtech.drone.programmer.dock_directory_empty");
        return getClientDockLoadLine(result) + "\n" + getClientDockRejectionReason(result);
    }

    private String getClientDockLoadLine(ClientDockResult result) {
        String tier = result.tier >= 0 && result.tier < GTValues.VN.length ? GTValues.VN[result.tier]
                : "T" + result.tier;
        return I18n.format("drtech.drone.programmer.dock_load", tier, result.priority,
                result.currentLoad, result.availableEu);
    }

    private String getClientDockRejectionReason(ClientDockResult result) {
        String key;
        if (!result.online) key = "offline";
        else if (!result.enabled) key = "disabled";
        else if (result.availableEu <= 0L) key = "no_energy";
        else if ("RESERVED".equals(result.occupancyState)) key = "reserved";
        else if ("STORED".equals(result.occupancyState)) key = "stored";
        else if (!result.canAcceptDrone) key = "rejected";
        else key = "available";
        return I18n.format("drtech.drone.programmer.dock_reason." + key);
    }

    private void moveClientDockResult(int delta) {
        List<ClientDockResult> results = getClientDockResults();
        if (!results.isEmpty()) clientDockResultIndex = Math.floorMod(clientDockResultIndex + delta, results.size());
    }

    private void selectClientDockResult(DroneProgramCanvasWidget canvas) {
        ClientDockResult result = getClientDockResult();
        if (result != null) canvas.selectDockReference(result.dockId, result.name, result.dimension, result.position);
    }

    private ClientFleetResult getClientFleetResult() {
        if (clientFleetDirectory.isEmpty()) return null;
        clientFleetResultIndex = Math.floorMod(clientFleetResultIndex, clientFleetDirectory.size());
        return clientFleetDirectory.get(clientFleetResultIndex);
    }

    private void moveClientFleetResult(int delta) {
        if (!clientFleetDirectory.isEmpty()) {
            clientFleetResultIndex = Math.floorMod(clientFleetResultIndex + delta, clientFleetDirectory.size());
        }
    }

    private boolean canControlSelectedFleetDrone() {
        ClientFleetResult result = getClientFleetResult();
        return clientInspectorPage == 3 && result != null && result.online;
    }

    private ButtonWidget<?> fleetControlButton(String translation, String command, int x, PanelSyncManager syncManager) {
        return new ButtonWidget<>().pos(x, 230).size(57, 16).overlay(IKey.lang(translation))
                .onMousePressed(mouse -> {
                    ClientFleetResult result = getClientFleetResult();
                    if (result != null && result.online) {
                        syncManager.callSyncedAction(FLEET_CONTROL_ACTION, packet -> {
                            packet.writeString(result.droneId.toString());
                            packet.writeString(command);
                        });
                    }
                    return true;
                });
    }

    private String getClientFleetPage() {
        return clientFleetDirectory.isEmpty() ? "0/0" : (clientFleetResultIndex + 1) + "/" + clientFleetDirectory.size();
    }

    private String getClientFleetSummary() {
        ClientFleetResult result = getClientFleetResult();
        if (result == null) return I18n.format("drtech.drone.programmer.fleet.empty");
        String id = result.droneId.toString();
        return result.chassis + " | " + localizeFleetStatus(result.online ? result.status : "OFFLINE")
                + " | " + id.substring(0, 8);
    }

    private String getClientFleetDetails() {
        ClientFleetResult result = getClientFleetResult();
        if (result == null) return I18n.format("drtech.drone.programmer.fleet.empty_help");
        long percent = result.energyCapacity <= 0L ? 0L
                : Math.min(100L, result.energyStored * 100L / result.energyCapacity);
        String dock = result.boundDock == null ? I18n.format("drtech.drone.programmer.fleet.unbound")
                : result.boundDock.getX() + ", " + result.boundDock.getY() + ", " + result.boundDock.getZ();
        String program = result.programId.isEmpty() ? I18n.format("drtech.drone.programmer.fleet.no_program")
                : result.programId.substring(0, Math.min(8, result.programId.length())) + " @" + result.programRevision;
        return I18n.format("drtech.drone.programmer.fleet.position", result.position.getX(),
                result.position.getY(), result.position.getZ()) + "\n"
                + I18n.format("drtech.drone.programmer.fleet.dimension", result.dimension) + "\n"
                + I18n.format("drtech.drone.programmer.fleet.energy", result.energyStored,
                        result.energyCapacity, percent) + "\n"
                + I18n.format("drtech.drone.programmer.fleet.cargo", result.cargoOccupiedSlots,
                        result.cargoCapacitySlots) + "\n"
                + I18n.format("drtech.drone.programmer.fleet.dock", dock) + "\n"
                + I18n.format("drtech.drone.programmer.fleet.program", program) + "\n"
                + I18n.format("drtech.drone.programmer.fleet.id", result.droneId);
    }

    private static String localizeFleetStatus(String status) {
        if (status == null) return I18n.format("drtech.drone.status.unknown");
        String key = "drtech.drone.status." + status.toLowerCase(Locale.ROOT);
        return I18n.hasKey(key) ? I18n.format(key) : status;
    }

    private static final class ClientDockResult {
        private final UUID dockId;
        private final String name;
        private final int dimension;
        private final BlockPos position;
        private final boolean online;
        private final long availableEu;
        private final int tier;
        private final int priority;
        private final int currentLoad;
        private final boolean enabled;
        private final boolean canAcceptDrone;
        private final String occupancyState;

        private ClientDockResult(UUID dockId, String name, int dimension, BlockPos position, boolean online,
                long availableEu, int tier, int priority, int currentLoad, boolean enabled,
                boolean canAcceptDrone, String occupancyState) {
            this.dockId = dockId;
            this.name = name == null ? "" : name;
            this.dimension = dimension;
            this.position = position;
            this.online = online;
            this.availableEu = availableEu;
            this.tier = tier;
            this.priority = priority;
            this.currentLoad = currentLoad;
            this.enabled = enabled;
            this.canAcceptDrone = canAcceptDrone;
            this.occupancyState = occupancyState == null ? "FREE" : occupancyState;
        }
    }

    private static final class ClientFleetResult {
        private final UUID droneId;
        private final int dimension;
        private final BlockPos position;
        private final String chassis;
        private final long energyStored;
        private final long energyCapacity;
        private final int cargoOccupiedSlots;
        private final int cargoCapacitySlots;
        private final String status;
        private final String programId;
        private final long programRevision;
        private final boolean online;
        private final BlockPos boundDock;

        private ClientFleetResult(UUID droneId, int dimension, BlockPos position, String chassis, long energyStored,
                long energyCapacity, int cargoOccupiedSlots, int cargoCapacitySlots, String status,
                String programId, long programRevision, boolean online, BlockPos boundDock) {
            this.droneId = droneId;
            this.dimension = dimension;
            this.position = position;
            this.chassis = chassis;
            this.energyStored = energyStored;
            this.energyCapacity = energyCapacity;
            this.cargoOccupiedSlots = cargoOccupiedSlots;
            this.cargoCapacitySlots = cargoCapacitySlots;
            this.status = status;
            this.programId = programId;
            this.programRevision = programRevision;
            this.online = online;
            this.boundDock = boundDock;
        }
    }

    private String getClientLibraryProgramSearch() { return clientLibraryProgramSearch; }

    private void setClientLibraryProgramSearch(String value) {
        clientLibraryProgramSearch = value == null ? "" : value;
        clientLibraryProgramResultIndex = 0;
    }

    private List<ClientLibraryProgramResult> getClientLibraryProgramResults() {
        String query = clientLibraryProgramSearch == null ? ""
                : clientLibraryProgramSearch.trim().toLowerCase(Locale.ROOT);
        if (query.isEmpty()) return clientProgramDirectory;
        List<ClientLibraryProgramResult> matches = new ArrayList<>();
        for (ClientLibraryProgramResult program : clientProgramDirectory) {
            if (program.name.toLowerCase(Locale.ROOT).contains(query)
                    || program.programId.toString().toLowerCase(Locale.ROOT).contains(query)
                    || Long.toString(program.revision).contains(query)) matches.add(program);
        }
        return matches;
    }

    private boolean hasClientLibraryProgramResult() { return !getClientLibraryProgramResults().isEmpty(); }

    private ClientLibraryProgramResult getClientLibraryProgramResult() {
        List<ClientLibraryProgramResult> results = getClientLibraryProgramResults();
        if (results.isEmpty()) return null;
        clientLibraryProgramResultIndex = Math.floorMod(clientLibraryProgramResultIndex, results.size());
        return results.get(clientLibraryProgramResultIndex);
    }

    private String getClientLibraryProgramResultLabel() {
        ClientLibraryProgramResult result = getClientLibraryProgramResult();
        if (result == null) return I18n.format("drtech.drone.programmer.program_no_results");
        String label = result.name.isEmpty() ? result.programId.toString() : result.name;
        return label.length() > 20 ? label.substring(0, 19) + "..." : label;
    }

    private String getClientLibraryProgramTooltip() {
        ClientLibraryProgramResult result = getClientLibraryProgramResult();
        if (result == null) return I18n.format("drtech.drone.programmer.program_no_results");
        String signature = result.signature.isEmpty() ? "-" : result.signature.substring(0, 12);
        String summary = I18n.format("drtech.drone.programmer.program_signature_tooltip", result.name, result.programId,
                result.revision, result.revisionCount, signature, result.nodeCount, result.edgeCount);
        return result.revisionHistory.isEmpty() ? summary : summary + "\n"
                + I18n.format("drtech.drone.programmer.program_revision_history", result.revisionHistory);
    }

    private String getClientLibraryProgramPage() {
        List<ClientLibraryProgramResult> results = getClientLibraryProgramResults();
        return results.isEmpty() ? "0/0" : (Math.floorMod(clientLibraryProgramResultIndex, results.size()) + 1)
                + "/" + results.size();
    }

    private String getClientLibraryProgramStatus(DroneProgramCanvasWidget canvas) {
        UUID selectedId = canvas.getSelectedProgramReferenceId();
        long selectedRevision = canvas.getSelectedProgramReferenceRevision();
        if (selectedId != null && clientProgramDirectory.stream().noneMatch(program ->
                program.programId.equals(selectedId) && program.revision == selectedRevision)) {
            return I18n.format("drtech.drone.programmer.program_reference_stale");
        }
        ClientLibraryProgramResult result = getClientLibraryProgramResult();
        if (result == null) return I18n.format("drtech.drone.programmer.program_directory_empty");
        return I18n.format("drtech.drone.programmer.program_result_status", result.revision,
                result.nodeCount, result.edgeCount);
    }

    private void moveClientLibraryProgramResult(int delta) {
        List<ClientLibraryProgramResult> results = getClientLibraryProgramResults();
        if (!results.isEmpty()) {
            clientLibraryProgramResultIndex = Math.floorMod(clientLibraryProgramResultIndex + delta, results.size());
        }
    }

    private void selectClientLibraryProgramResult(DroneProgramCanvasWidget canvas) {
        ClientLibraryProgramResult result = getClientLibraryProgramResult();
        if (result != null) canvas.selectProgramReference(result.programId, result.name, result.revision);
    }

    private static final class ClientLibraryProgramResult {
        private final UUID programId;
        private final String name;
        private final long revision;
        private final int nodeCount;
        private final int edgeCount;
        private final String signature;
        private final int revisionCount;
        private final String revisionHistory;

        private ClientLibraryProgramResult(UUID programId, String name, long revision, int nodeCount, int edgeCount,
                String signature, int revisionCount, String revisionHistory) {
            this.programId = programId;
            this.name = name == null ? "" : name;
            this.revision = revision;
            this.nodeCount = nodeCount;
            this.edgeCount = edgeCount;
            this.signature = signature == null ? "" : signature;
            this.revisionCount = Math.max(1, revisionCount);
            this.revisionHistory = revisionHistory == null ? "" : revisionHistory;
        }
    }

    private ButtonWidget<?> nodeButton(ResourceLocation nodeType, int x, int y,
            PanelSyncManager syncManager) {
        return new ButtonWidget<>().pos(x, y).size(81, 14)
                .overlay(compactNodeLabel(nodeType, 0.82F))
                .tooltipStatic(tooltip -> tooltip.addLine(IKey.dynamic(() -> getNodeLibraryTooltip(nodeType))))
                .onMousePressed(mouse -> { handleNodeLibraryClick(syncManager, nodeType); return true; });
    }

    private ButtonWidget<?> pagedNodeButton(int page, ResourceLocation nodeType, int x, int y,
            PanelSyncManager syncManager) {
        return nodeButton(nodeType, x, y, syncManager)
                .setEnabledIf(widget -> false);
    }

    private ButtonWidget<?> smallNodeButton(ResourceLocation nodeType, int x, int y,
            PanelSyncManager syncManager) {
        return new ButtonWidget<>().pos(x, y).size(39, 14)
                .overlay(compactNodeLabel(nodeType, 0.68F))
                .tooltipStatic(tooltip -> tooltip.addLine(IKey.dynamic(() -> getNodeLibraryTooltip(nodeType))))
                .onMousePressed(mouse -> { handleNodeLibraryClick(syncManager, nodeType); return true; });
    }

    private ButtonWidget<?> pagedSmallNodeButton(int page, ResourceLocation nodeType, int x, int y,
            PanelSyncManager syncManager) {
        return smallNodeButton(nodeType, x, y, syncManager)
                .setEnabledIf(widget -> false);
    }

    private ButtonWidget<?> libraryNodeButton(int index, PanelSyncManager syncManager,
            DroneProgramCanvasWidget canvas) {
        int column = index % 2;
        int row = index / 2;
        return new DroneNodeLibraryButtonWidget().nodeType(() -> libraryNodeAt(index))
                .pos(5 + column * 42, 83 + row * 15).size(39, 14)
                .overlay(IKey.dynamic(() -> {
                    ResourceLocation nodeType = libraryNodeAt(index);
                    return nodeType == null ? "" : "   " + compactNodeText(nodeType);
                }).scale(0.68F))
                .tooltipStatic(tooltip -> tooltip.addLine(IKey.dynamic(() -> {
                    ResourceLocation nodeType = libraryNodeAt(index);
                    return nodeType == null ? "" : getNodeLibraryTooltip(nodeType);
                })))
                .setEnabledIf(widget -> libraryNodeAt(index) != null)
                .onLibraryClicked(() -> {
                    ResourceLocation nodeType = libraryNodeAt(index);
                    if (nodeType != null) handleNodeLibraryClick(syncManager, nodeType);
                })
                .dropTarget(widget -> widget == canvas)
                .onDropped(position -> {
                    ResourceLocation nodeType = libraryNodeAt(index);
                    int[] graphPosition = canvas.graphPositionAtAbsolute(position[0], position[1]);
                    if (nodeType != null && graphPosition != null) {
                        addClientLibraryNode(syncManager, nodeType, graphPosition[0], graphPosition[1]);
                    }
                });
    }

    private ResourceLocation libraryNodeAt(int index) {
        List<ResourceLocation> entries = getClientLibraryEntries();
        return index >= 0 && index < entries.size() ? entries.get(index) : null;
    }

    private List<ResourceLocation> getClientLibraryEntries() {
        String query = clientNodeSearch == null ? "" : clientNodeSearch.trim();
        if (!query.isEmpty()) {
            List<ResourceLocation> matches = new ArrayList<>();
            for (ResourceLocation nodeType : NODE_LIBRARY_INDEX.getAllNodes()) {
                if (matchesNodeSearch(nodeType)) matches.add(nodeType);
            }
            return pageEntries(matches, normalizedClientLibraryPage(getSearchLibraryPageCount()));
        }

        int ordinaryPages = NODE_LIBRARY_INDEX.getPages().size();
        int page = normalizedClientLibraryPage(ordinaryPages + 2);
        if (page < ordinaryPages) return NODE_LIBRARY_INDEX.getPages().get(page).getNodes();
        List<ResourceLocation> quickEntries = page == ordinaryPages
                ? clientFavoriteNodeTypes : clientRecentNodeTypes;
        return pageEntries(quickEntries, 0);
    }

    private List<ResourceLocation> pageEntries(List<ResourceLocation> entries, int page) {
        int from = page * MAX_NODE_LIBRARY_ENTRIES;
        if (from >= entries.size()) return Collections.emptyList();
        return entries.subList(from, Math.min(entries.size(), from + MAX_NODE_LIBRARY_ENTRIES));
    }

    private int getSearchLibraryPageCount() {
        int matches = 0;
        for (ResourceLocation nodeType : NODE_LIBRARY_INDEX.getAllNodes()) {
            if (matchesNodeSearch(nodeType)) matches++;
        }
        return Math.max(1, (matches + MAX_NODE_LIBRARY_ENTRIES - 1) / MAX_NODE_LIBRARY_ENTRIES);
    }

    private int getClientLibraryPageCount() {
        String query = clientNodeSearch == null ? "" : clientNodeSearch.trim();
        return query.isEmpty() ? NODE_LIBRARY_INDEX.getPages().size() + 2 : getSearchLibraryPageCount();
    }

    private int normalizedClientLibraryPage(int pageCount) {
        return Math.floorMod(clientLibraryPage, Math.max(1, pageCount));
    }

    private void moveClientLibraryPage(int delta) {
        int pageCount = getClientLibraryPageCount();
        clientLibraryPage = Math.floorMod(normalizedClientLibraryPage(pageCount) + delta, pageCount);
    }

    private String getClientLibraryPageLabel() {
        String query = clientNodeSearch == null ? "" : clientNodeSearch.trim();
        if (!query.isEmpty()) {
            int count = getSearchLibraryPageCount();
            return I18n.format("drtech.drone.programmer.page.search_results",
                    normalizedClientLibraryPage(count) + 1, count);
        }
        int ordinaryPages = NODE_LIBRARY_INDEX.getPages().size();
        int page = normalizedClientLibraryPage(ordinaryPages + 2);
        if (page == ordinaryPages) return I18n.format("drtech.drone.programmer.page.favorites");
        if (page == ordinaryPages + 1) return I18n.format("drtech.drone.programmer.page.recent");
        DroneNodeLibraryIndex.Page categoryPage = NODE_LIBRARY_INDEX.getPages().get(page);
        String label = I18n.format("drtech.drone.programmer.node_category." + categoryPage.getCategory());
        if (categoryPage.getCategoryPageCount() > 1) {
            label += " " + (categoryPage.getCategoryPage() + 1) + "/" + categoryPage.getCategoryPageCount();
        }
        return label;
    }

    /** Reuses the ordinary two-column cards for persistent Favorites and most-recent nodes. */
    private ButtonWidget<?> quickNodeButton(int page, int index, PanelSyncManager syncManager) {
        int column = index % 2;
        int row = index / 2;
        return new ButtonWidget<>().pos(5 + column * 42, 83 + row * 15).size(39, 14)
                .overlay(IKey.dynamic(() -> {
                    ResourceLocation nodeType = quickNodeAt(page, index);
                    return nodeType == null ? "" : compactNodeText(nodeType);
                }).scale(0.68F))
                .tooltipStatic(tooltip -> tooltip.addLine(IKey.dynamic(() -> {
                    ResourceLocation nodeType = quickNodeAt(page, index);
                    return nodeType == null ? "" : getNodeLibraryTooltip(nodeType);
                })))
                .setEnabledIf(widget -> {
                    return false;
                })
                .onMousePressed(mouse -> {
                    ResourceLocation nodeType = quickNodeAt(page, index);
                    if (nodeType != null) handleNodeLibraryClick(syncManager, nodeType);
                    return true;
                });
    }

    private ResourceLocation quickNodeAt(int page, int index) {
        List<ResourceLocation> source = page == FAVORITES_PAGE ? clientFavoriteNodeTypes
                : page == RECENT_PAGE ? clientRecentNodeTypes : Collections.emptyList();
        return index >= 0 && index < source.size() ? source.get(index) : null;
    }

    private void handleNodeLibraryClick(PanelSyncManager syncManager, ResourceLocation nodeType) {
        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            boolean favorite = !clientFavoriteNodeTypes.contains(nodeType);
            updateClientFavorite(nodeType, favorite);
            syncManager.callSyncedAction(NODE_LIBRARY_ACTION, packet -> {
                packet.writeString(nodeType.toString());
                packet.writeBoolean(favorite);
            });
            return;
        }
        DroneProgramGraph graph = getClientGraph();
        if (graph == null || !clientEditable) return;
        int offset = graph.getNodes().size() * 12;
        addClientLibraryNode(syncManager, nodeType, 40 + offset, 40 + offset);
    }

    private void addClientLibraryNode(PanelSyncManager syncManager, ResourceLocation nodeType, int x, int y) {
        DroneProgramGraph graph = getClientGraph();
        if (graph == null || !clientEditable || nodeType == null) return;
        sendEditCommand(syncManager, DroneGraphEditCommand.addNode(graph.getRevision(), UUID.randomUUID(),
                nodeType, x, y, defaultConfiguration(nodeType)));
        updateClientRecent(nodeType);
    }

    private void updateClientFavorite(ResourceLocation nodeType, boolean favorite) {
        List<ResourceLocation> result = new ArrayList<>(clientFavoriteNodeTypes);
        result.remove(nodeType);
        if (favorite) result.add(0, nodeType);
        if (result.size() > MAX_NODE_LIBRARY_ENTRIES) result = result.subList(0, MAX_NODE_LIBRARY_ENTRIES);
        clientFavoriteNodeTypes = Collections.unmodifiableList(new ArrayList<>(result));
    }

    private void updateClientRecent(ResourceLocation nodeType) {
        List<ResourceLocation> result = new ArrayList<>(clientRecentNodeTypes);
        result.remove(nodeType);
        result.add(0, nodeType);
        if (result.size() > MAX_NODE_LIBRARY_ENTRIES) result = result.subList(0, MAX_NODE_LIBRARY_ENTRIES);
        clientRecentNodeTypes = Collections.unmodifiableList(new ArrayList<>(result));
    }

    private boolean matchesNodeSearch(ResourceLocation nodeType) {
        DroneNodeDefinition definition = NODE_LIBRARY.get(nodeType);
        return DroneBilingualNodeSearch.matches(nodeType, definition, clientNodeSearch);
    }

    /** Builds a compact, dynamic node-card tooltip without changing the program schema. */
    private String getNodeLibraryTooltip(ResourceLocation nodeType) {
        DroneNodeDefinition definition = NODE_LIBRARY.get(nodeType);
        StringBuilder tooltip = new StringBuilder(I18n.format("drtech.drone.node." + nodeType.getPath()));
        if (definition != null) {
            String category = definition.getCategory();
            tooltip.append('\n').append(I18n.format("drtech.drone.programmer.node_library.category",
                    I18n.format("drtech.drone.programmer.node_category." + category)));
            tooltip.append('\n').append(I18n.format("drtech.drone.programmer.node_library.ports",
                    definition.getPorts().size()));
        }
        List<DroneUpgradeType> requirements = DroneProgramHardwareValidator.getRequiredUpgrades(nodeType);
        if (requirements.isEmpty()) {
            tooltip.append('\n').append(I18n.format("drtech.drone.programmer.node_library.no_module"));
        } else {
            List<String> names = new ArrayList<>();
            boolean ready = true;
            for (DroneUpgradeType type : requirements) {
                names.add(I18n.format("drtech.drone.upgrade." + type.getSerializedName()));
                ready &= targetDroneHasUpgrade(type);
            }
            tooltip.append('\n').append(I18n.format(ready
                    ? "drtech.drone.programmer.node_library.module_ready"
                    : "drtech.drone.programmer.node_library.module_missing", String.join("、", names)));
        }
        tooltip.append('\n').append(I18n.format("drtech.drone.programmer.node_library.minimum_tier", "HV"));
        tooltip.append('\n').append(I18n.format("drtech.drone.programmer.add_node_hint"));
        tooltip.append('\n').append(I18n.format("drtech.drone.programmer.node_library.favorite_hint"));
        return tooltip.toString();
    }

    private boolean targetDroneHasUpgrade(DroneUpgradeType type) {
        ItemStack target = importItems.getStackInSlot(1);
        return !target.isEmpty() && target.getItem() instanceof ItemProgrammableDrone
                && DroneUpgradeDataCodec.getLevel(DroneItemData.getUpgrades(target), type) > 0;
    }

    /** Keeps the two-column library readable while node cards retain their full localized titles. */
    private static IKey compactNodeLabel(ResourceLocation nodeType, float scale) {
        return IKey.dynamic(() -> compactNodeText(nodeType)).scale(scale);
    }

    private static String compactNodeText(ResourceLocation nodeType) {
        String compactKey = "drtech.drone.node.short." + nodeType.getPath();
        String compact = I18n.format(compactKey);
        return compactKey.equals(compact) ? I18n.format("drtech.drone.node." + nodeType.getPath()) : compact;
    }

    private ButtonWidget<?> actionButton(String label, int x, int y, Runnable action) {
        return actionButton(IKey.str(label), x, y, action);
    }

    private ButtonWidget<?> actionButton(IKey label, int x, int y, Runnable action) {
        return new ButtonWidget<>().pos(x, y).size(35, 16).overlay(label)
                .onMousePressed(mouse -> { action.run(); return true; });
    }

    private ButtonWidget<?> diagnosticFilterButton(int filter, int x) {
        return new ButtonWidget<>().pos(x, 64).size(32, 16)
                .overlay(IKey.dynamic(() -> getDiagnosticFilterLabel(filter)).scale(0.72F))
                .setEnabledIf(widget -> clientInspectorPage == 2)
                .onMousePressed(mouse -> {
                    clientDiagnosticFilter = filter;
                    clientDiagnosticPage = 0;
                    return true;
                });
    }

    private ButtonWidget<?> diagnosticRowButton(DroneProgramCanvasWidget canvas, int row, int y) {
        return new ButtonWidget<>().pos(379, y).size(134, 29)
                // Diagnostic details belong in the tooltip; the fixed, scaled label keeps
                // even long translated errors inside its row and above the pager/tool bar.
                .overlay(IKey.dynamic(() -> getDiagnosticRowLabel(row)).scale(0.68F))
                .tooltipStatic(tooltip -> {
                    tooltip.addLine(IKey.dynamic(() -> getDiagnosticRowTooltip(row)));
                    tooltip.addLine(IKey.dynamic(() -> getDiagnosticRepairTooltip(row)));
                })
                .setEnabledIf(widget -> clientInspectorPage == 2 && getDiagnosticAtRow(row) != null)
                .onMousePressed(mouse -> {
                    ClientDiagnostic diagnostic = getDiagnosticAtRow(row);
                    if (diagnostic != null && diagnostic.nodeId != null) {
                        if (!diagnostic.property.isEmpty()
                                && canvas.focusNodeProperty(diagnostic.nodeId, diagnostic.property)) {
                            clientInspectorPage = 0;
                        } else {
                            canvas.focusNode(diagnostic.nodeId);
                        }
                    }
                    return true;
                });
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

    private ButtonWidget<?> remoteTraceFilterButton(int filter, int x) {
        return new ButtonWidget<>().pos(x, 198).size(43, 16)
                .overlay(IKey.dynamic(() -> I18n.format(remoteTraceFilterKey(filter))))
                .onMousePressed(mouse -> {
                    clientRemoteTraceFilter = filter;
                    clientRemoteTraceOffset = 0;
                    return true;
                });
    }

    private ButtonWidget<?> remoteTraceRowButton(DroneProgramCanvasWidget canvas, int row, int y) {
        return new ButtonWidget<>().pos(379, y).size(134, 10)
                .overlay(IKey.dynamic(() -> getRemoteTraceRow(row)).scale(0.68F))
                .tooltipStatic(tooltip -> {
                    tooltip.addLine(IKey.dynamic(() -> getRemoteTraceTooltip(row)));
                    tooltip.addLine(IKey.lang("drtech.drone.remote.trace_focus_help"));
                })
                .onMousePressed(mouse -> {
                    focusRemoteTrace(canvas, row);
                    return true;
                });
    }

    private static NBTTagCompound defaultConfiguration(ResourceLocation nodeType) {
        NBTTagCompound configuration = new NBTTagCompound();
        if (nodeType.equals(DrTechDroneNodes.COMMENT)) configuration.setString("Text", "");
        if (nodeType.equals(DrTechDroneNodes.GROUP)) {
            configuration.setString("Title", "");
            configuration.setInteger("Width", 320);
            configuration.setInteger("Height", 200);
            configuration.setString("Color", "BLUE");
            configuration.setBoolean("Collapsed", false);
        }
        if (nodeType.equals(DrTechDroneNodes.WAIT)) configuration.setInteger("Ticks", 20);
        if (nodeType.equals(DrTechDroneNodes.PATROL_ATTACK_AREA)) {
            configuration.setString("Priority", "HOSTILE_FIRST");
            configuration.setBoolean("UntilAreaClear", true);
            configuration.setBoolean("HostileOnly", true);
            configuration.setInteger("MaxChaseTicks", 1_200);
            configuration.setInteger("MaxChaseDistance", 16);
            configuration.setString("NoTargetMode", "COMPLETE");
            configuration.setInteger("RescanTicks", 20);
            configuration.setBoolean("ReacquireLostTarget", true);
            configuration.setBoolean("ReturnToAreaOnComplete", true);
        }
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
        if (nodeType.equals(DrTechDroneNodes.CRAFT_ITEMS)) {
            configuration.setInteger("Count", 1);
            configuration.setInteger("ReserveCount", 0);
            configuration.setString("Mode", "EXACT");
            configuration.setBoolean("Simulate", false);
        }
        if (nodeType.equals(DrTechDroneNodes.CRAFT_GRID)) {
            configuration.setInteger("Count", 1);
            configuration.setInteger("ReserveCount", 0);
            configuration.setString("Mode", "EXACT");
        }
        if (nodeType.equals(DrTechDroneNodes.SET_MACHINE_WORKING)) configuration.setBoolean("Enabled", true);
        if (nodeType.equals(DrTechDroneNodes.REPAIR_MACHINE)) configuration.setBoolean("RequireAll", true);
        if (nodeType.equals(DrTechDroneNodes.CAN_CRAFT)) {
            configuration.setInteger("Count", 1);
            configuration.setInteger("ReserveCount", 0);
        }
        if (nodeType.equals(DrTechDroneNodes.CRAFTABLE_COUNT)) {
            configuration.setInteger("Limit", 64);
            configuration.setInteger("ReserveCount", 0);
        }
        if (nodeType.equals(DrTechDroneNodes.AREA)) {
            configuration.setInteger("X2", 2);
            configuration.setInteger("Z2", 2);
        }
        if (nodeType.equals(DrTechDroneNodes.FOR_EACH_COORDINATE)) configuration.setString("Order", "SERPENTINE");
        if (nodeType.equals(DrTechDroneNodes.AREA_EXPAND)
                || nodeType.equals(DrTechDroneNodes.AREA_INSET)) configuration.setInteger("Radius", 1);
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
            configuration.setString("Axis", "Y");
        }
        if (nodeType.equals(DrTechDroneNodes.PYRAMID_AREA)) {
            configuration.setInteger("Radius", 3);
            configuration.setInteger("Height", 4);
            configuration.setString("Axis", "Y");
        }
        if (nodeType.equals(DrTechDroneNodes.CUBOID_SURFACE_AREA)) configuration.setString("Surface", "HOLLOW");
        if (nodeType.equals(DrTechDroneNodes.GRID_AREA)) {
            configuration.setInteger("StepX", 2);
            configuration.setInteger("StepY", 1);
            configuration.setInteger("StepZ", 2);
        }
        if (nodeType.equals(DrTechDroneNodes.RANDOM_POINTS_AREA)) {
            configuration.setInteger("Count", 16);
            configuration.setInteger("Seed", 0);
        }
        if (nodeType.equals(DrTechDroneNodes.AREA_BOUNDARY)) configuration.setInteger("Thickness", 1);
        if (nodeType.equals(DrTechDroneNodes.AREA_SCALE)) configuration.setDouble("Factor", 1.0D);
        if (nodeType.equals(DrTechDroneNodes.PATH_AREA)) configuration.setInteger("Radius", 0);
        if (nodeType.equals(DrTechDroneNodes.GET_NUMBER_VARIABLE)
                || nodeType.equals(DrTechDroneNodes.SET_NUMBER_VARIABLE)
                || nodeType.equals(DrTechDroneNodes.ADD_NUMBER_VARIABLE)) configuration.setString("Name", "value");
        return configuration;
    }

    private void sendEditCommand(PanelSyncManager syncManager, DroneGraphEditCommand command) {
        syncManager.callSyncedAction(EDIT_ACTION, packet -> packet.writeCompoundTag(DroneGraphCommandCodec.write(command)));
    }

    private void beginClientWorldSelection(PanelSyncManager syncManager, DroneProgramCanvasWidget canvas) {
        UUID nodeId = canvas.getSelectedCoordinateCaptureNodeId();
        if (nodeId == null || !clientEditable) return;
        boolean area = canvas.isSelectedAreaCaptureTarget();
        syncManager.callSyncedAction(WORLD_SELECTION_ACTION, packet -> {
            packet.writeString(nodeId.toString());
            packet.writeBoolean(area);
        });
        DroneWorldSelectionHandler.begin(getPos(), nodeId, area);
    }

    private void beginWorldSelection(EntityPlayer player, PacketBuffer packet) {
        if (getWorld() == null || getWorld().isRemote || player.getDistanceSq(getPos()) > MAX_EDIT_DISTANCE_SQUARED) return;
        refreshCardSession();
        // The client closes the GUI immediately after requesting world picking. The close
        // callback can therefore clear editorOwner before this packet is handled; a released
        // lock is safe here because the request is still bound to the nearby programmer and
        // the loaded program card. A lock held by another player remains rejected.
        if (editSession == null || editorOwner != null && !editorOwner.equals(player.getUniqueID())) return;
        UUID nodeId;
        try {
            nodeId = UUID.fromString(packet.readString(36));
        } catch (IllegalArgumentException exception) {
            return;
        }
        boolean area = packet.readBoolean();
        DroneProgramNode node = editSession.getGraph().getNode(nodeId);
        if (node == null || area != node.getType().equals(DrTechDroneNodes.AREA)
                || !area && !node.getType().equals(DrTechDroneNodes.COORDINATE)) return;
        worldSelectionOwner = player.getUniqueID();
        worldSelectionNodeId = nodeId;
        worldSelectionArea = area;
        worldSelectionExpiresAt = getWorld().getTotalWorldTime() + 1200L;
        serverStatus = area ? "World area selection active" : "World coordinate selection active";
    }

    public void applyWorldSelection(EntityPlayer player, UUID nodeId, BlockPos first, BlockPos second) {
        if (getWorld() == null || getWorld().isRemote || player == null || first == null
                || worldSelectionOwner == null || !worldSelectionOwner.equals(player.getUniqueID())
                || worldSelectionNodeId == null || !worldSelectionNodeId.equals(nodeId)
                || getWorld().getTotalWorldTime() > worldSelectionExpiresAt) {
            clearWorldSelection();
            return;
        }
        refreshCardSession();
        if (editSession == null) {
            clearWorldSelection();
            return;
        }
        DroneProgramNode node = editSession.getGraph().getNode(nodeId);
        if (node == null || worldSelectionArea && second == null
                || worldSelectionArea != node.getType().equals(DrTechDroneNodes.AREA)) {
            clearWorldSelection();
            return;
        }
        NBTTagCompound config = node.getConfiguration();
        if (worldSelectionArea) {
            config.setInteger("X1", first.getX());
            config.setInteger("Y1", first.getY());
            config.setInteger("Z1", first.getZ());
            config.setInteger("X2", second.getX());
            config.setInteger("Y2", second.getY());
            config.setInteger("Z2", second.getZ());
        } else {
            config.setInteger("X", first.getX());
            config.setInteger("Y", first.getY());
            config.setInteger("Z", first.getZ());
        }
        DroneGraphEditResult result = editSession.apply(DroneGraphEditCommand.configureNode(
                editSession.getGraph().getRevision(), nodeId, config));
        if (result.isAccepted()) {
            saveSessionToCard();
            serverStatus = worldSelectionArea ? "World area saved" : "World coordinate saved";
            markDirty();
        } else {
            serverStatus = result.getStatus().name() + ": " + result.getMessage();
        }
        clearWorldSelection();
    }

    private void clearWorldSelection() {
        worldSelectionOwner = null;
        worldSelectionNodeId = null;
        worldSelectionArea = false;
        worldSelectionExpiresAt = 0L;
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
            DroneGraphEditCommand command = DroneGraphCommandCodec.read(commandTag);
            DroneGraphEditResult result = editSession.apply(command);
            if (result.isAccepted()) {
                saveSessionToCard();
                recordNodeLibraryUse(command);
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

    private void receiveFleetControl(EntityPlayer player, String rawDroneId, String command) {
        if (getWorld() == null || getWorld().isRemote || player == null
                || player.getDistanceSq(getPos()) > MAX_EDIT_DISTANCE_SQUARED) return;
        if (!"START".equals(command) && !"STOP".equals(command) && !"RECALL".equals(command)) {
            serverStatus = "Fleet command rejected";
            return;
        }
        UUID droneId;
        try {
            droneId = UUID.fromString(rawDroneId);
        } catch (IllegalArgumentException ignored) {
            serverStatus = "Fleet drone ID is invalid";
            return;
        }
        DroneRegistryRecord record = DroneRegistry.get(getWorld()).getRecord(droneId).orElse(null);
        if (record == null || !player.getUniqueID().equals(record.getOwnerId())) {
            serverStatus = "Fleet command denied";
            return;
        }
        MinecraftServer server = getWorld().getMinecraftServer();
        WorldServer targetWorld = server == null ? null : server.getWorld(record.getDimension());
        if (targetWorld == null || !DroneRegistry.isOnline(record, targetWorld.getTotalWorldTime())) {
            serverStatus = "Fleet drone is offline";
            return;
        }
        for (Entity entity : targetWorld.loadedEntityList) {
            if (entity instanceof EntityProgrammableDrone && droneId.equals(((EntityProgrammableDrone) entity).getDroneId())) {
                serverStatus = ((EntityProgrammableDrone) entity).handleFleetControl(player.getUniqueID(), command)
                        ? "Fleet command: " + command : "Fleet command rejected for current runtime state";
                return;
            }
        }
        serverStatus = "Fleet drone is not loaded";
    }

    /** Saves a card-local node favourite after the regular editor lock and distance checks. */
    private void receiveNodeLibraryAction(EntityPlayer player, String rawNodeType, boolean favorite) {
        if (getWorld() == null || getWorld().isRemote || player.getDistanceSq(getPos()) > MAX_EDIT_DISTANCE_SQUARED) {
            return;
        }
        refreshCardSession();
        if (editorOwner == null || !editorOwner.equals(player.getUniqueID()) || editSession == null) return;
        ResourceLocation nodeType;
        try {
            nodeType = new ResourceLocation(rawNodeType);
        } catch (RuntimeException ignored) {
            return;
        }
        if (NODE_LIBRARY.get(nodeType) == null) return;
        List<ResourceLocation> favorites = readNodeLibraryEntries(NODE_FAVORITES_TAG);
        favorites.remove(nodeType);
        if (favorite) favorites.add(0, nodeType);
        writeNodeLibraryEntries(NODE_FAVORITES_TAG, favorites);
        loadedCardFingerprint = fingerprint(importItems.getStackInSlot(0));
        serverStatus = favorite ? "Node added to favorites" : "Node removed from favorites";
        markDirty();
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
        DroneExecutorRegistry executors = DroneExtensionRegistry.actions();
        DroneValueEvaluatorRegistry evaluators = DroneExtensionRegistry.sensors();
        for (DroneProgramNode node : editSession.getGraph().getNodes()) {
            boolean builtIn = node.getType().equals(DrTechDroneNodes.START)
                    || node.getType().equals(DrTechDroneNodes.END)
                    || node.getType().equals(DrTechDroneNodes.CALL_PROGRAM)
                    || DrTechDroneNodes.isEditorOnly(node.getType());
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
        List<DroneProgramDiagnostic> hardwareDiagnostics = DroneProgramHardwareValidator.validate(
                editSession.getGraph(), drone, getWorld());
        if (hardwareDiagnostics.stream()
                .anyMatch(diagnostic -> diagnostic.getSeverity() == DroneDiagnosticSeverity.ERROR)) {
            serverStatus = "Cannot write: target drone hardware incompatible";
            return;
        }
        if (energyContainer.getEnergyStored() < PROGRAM_WRITE_EU) {
            serverStatus = "Cannot write: requires " + PROGRAM_WRITE_EU + " EU";
            return;
        }
        DroneProgramLibrary.RegisterResult libraryResult = DroneProgramLibrary.get(getWorld()).registerChecked(
                player.getUniqueID(), editSession.getGraph(), getWorld().getTotalWorldTime());
        if (libraryResult == DroneProgramLibrary.RegisterResult.CONFLICT) {
            serverStatus = "Cannot write: a newer program revision exists";
            return;
        }
        if (libraryResult == DroneProgramLibrary.RegisterResult.OWNER_MISMATCH) {
            serverStatus = "Cannot write: program is owned by another player";
            return;
        }
        energyContainer.removeEnergy(PROGRAM_WRITE_EU);
        DroneItemData.setProgram(drone, DroneProgramNbtCodec.write(editSession.getGraph()));
        serverStatus = libraryResult.isAccepted() ? "Program written to drone and program library"
                : "Program written to drone; program library is full";
        markDirty();
    }

    /** Saves the current graph into a player-provided blank card without mutating the source card. */
    private void saveProgramAsNewCard(EntityPlayer player) {
        if (getWorld() == null || getWorld().isRemote || player.getDistanceSq(getPos()) > MAX_EDIT_DISTANCE_SQUARED) return;
        refreshCardSession();
        if (editorOwner == null || !editorOwner.equals(player.getUniqueID())) {
            serverStatus = "Read-only: another player owns the editor lock";
            return;
        }
        if (editSession == null) {
            serverStatus = "Cannot save copy: insert a program card";
            return;
        }
        ItemStack blankCard = findBlankProgramCard(player);
        if (blankCard.isEmpty()) {
            serverStatus = "Cannot save copy: keep one blank program card in your inventory";
            return;
        }
        DroneProgramGraph source = editSession.getGraph();
        DroneProgramGraph copy = source.copyAsNewProgram(copyProgramName(source.getName()));
        ((ItemDroneProgramCard) blankCard.getItem()).writeProgram(blankCard, copy);
        player.inventory.markDirty();

        boolean catalogued = !editSession.getLastCompileResult().hasErrors()
                && DroneProgramLibrary.get(getWorld()).register(player.getUniqueID(), copy, getWorld().getTotalWorldTime());
        if (editSession.getLastCompileResult().hasErrors()) {
            serverStatus = "Program copy saved; fix compile errors before it can enter the program library";
        } else {
            serverStatus = catalogued ? "Program copy saved to a blank card and program library"
                    : "Program copy saved to a blank card; program library is full";
        }
        markDirty();
    }

    /** Creates only templates present in the server-owned allow-list. */
    private void createOfficialTemplateCard(EntityPlayer player, String templateId) {
        if (getWorld() == null || getWorld().isRemote
                || player.getDistanceSq(getPos()) > MAX_EDIT_DISTANCE_SQUARED) return;
        if (editorOwner == null || !editorOwner.equals(player.getUniqueID())) {
            serverStatus = "Read-only: another player owns the editor lock";
            return;
        }
        DroneOfficialProgramTemplate template;
        try {
            template = DroneOfficialProgramTemplates.get(new ResourceLocation(templateId));
        } catch (RuntimeException exception) {
            template = null;
        }
        if (template == null) {
            serverStatus = "Cannot create template: unknown official template";
            return;
        }
        ItemStack blankCard = findBlankProgramCard(player);
        if (blankCard.isEmpty()) {
            serverStatus = "Cannot create template: keep one blank program card in your inventory";
            return;
        }
        DroneProgramGraph graph = template.createGraph();
        DroneCompileResult compileResult = new DroneProgramCompiler(NODE_LIBRARY).compile(graph);
        if (compileResult.hasErrors()) {
            serverStatus = "Cannot create template: built-in template failed validation";
            return;
        }
        ((ItemDroneProgramCard) blankCard.getItem()).writeProgram(blankCard, graph);
        player.inventory.markDirty();
        boolean catalogued = DroneProgramLibrary.get(getWorld()).register(player.getUniqueID(), graph,
                getWorld().getTotalWorldTime());
        serverStatus = catalogued ? "Official template saved to a blank card and program library"
                : "Official template saved to a blank card; program library is full";
        markDirty();
    }

    private void exportClientProgramToClipboard() {
        if (clientGraph == null) return;
        try {
            GuiScreen.setClipboardString(DroneProgramTransferCodec.encode(clientGraph));
            clientStatus = I18n.format("drtech.drone.programmer.export.success");
        } catch (DroneProgramFormatException exception) {
            clientStatus = I18n.format("drtech.drone.programmer.export.failed");
        }
    }

    private void importClientProgramFromClipboard(PanelSyncManager syncManager) {
        String text = GuiScreen.getClipboardString();
        if (text == null || text.length() > DroneProgramTransferCodec.MAX_TEXT_LENGTH
                || !text.startsWith(DroneProgramTransferCodec.PREFIX)) {
            clientStatus = I18n.format("drtech.drone.programmer.import.invalid_clipboard");
            return;
        }
        syncManager.callSyncedAction(IMPORT_PROGRAM_ACTION, buffer -> buffer.writeString(text));
    }

    private void compareClientProgramFromClipboard(PanelSyncManager syncManager) {
        String text = GuiScreen.getClipboardString();
        if (text == null || text.length() > DroneProgramTransferCodec.MAX_TEXT_LENGTH
                || !text.startsWith(DroneProgramTransferCodec.PREFIX)) {
            clientStatus = I18n.format("drtech.drone.programmer.import.invalid_clipboard");
            return;
        }
        syncManager.callSyncedAction(COMPARE_PROGRAM_ACTION, buffer -> buffer.writeString(text));
    }

    private void importProgramTransfer(EntityPlayer player, String text) {
        if (getWorld() == null || getWorld().isRemote
                || player.getDistanceSq(getPos()) > MAX_EDIT_DISTANCE_SQUARED) return;
        if (editorOwner == null || !editorOwner.equals(player.getUniqueID())) {
            serverStatus = "Read-only: another player owns the editor lock";
            return;
        }
        ItemStack blankCard = findBlankProgramCard(player);
        if (blankCard.isEmpty()) {
            serverStatus = "Cannot import: keep one blank program card in your inventory";
            return;
        }
        try {
            DroneProgramGraph decoded = DroneProgramTransferCodec.decodeAndValidate(text, NODE_LIBRARY);
            DroneProgramGraph imported = decoded.copyAsNewProgram(decoded.getName());
            ((ItemDroneProgramCard) blankCard.getItem()).writeProgram(blankCard, imported);
            player.inventory.markDirty();
            boolean catalogued = DroneProgramLibrary.get(getWorld()).register(player.getUniqueID(), imported,
                    getWorld().getTotalWorldTime());
            serverStatus = catalogued ? "Imported program saved to a blank card and program library"
                    : "Imported program saved to a blank card; program library is full";
            markDirty();
        } catch (DroneProgramFormatException exception) {
            serverStatus = "Cannot import: " + exception.getMessage();
        }
    }

    private void compareProgramTransfer(EntityPlayer player, String text) {
        if (getWorld() == null || getWorld().isRemote
                || player.getDistanceSq(getPos()) > MAX_EDIT_DISTANCE_SQUARED) return;
        if (editorOwner == null || !editorOwner.equals(player.getUniqueID()) || editSession == null) {
            serverStatus = "Cannot compare: acquire the editor lock with a program loaded";
            return;
        }
        try {
            DroneProgramGraph candidate = DroneProgramTransferCodec.decodeAndValidate(text, NODE_LIBRARY);
            DroneProgramDiffResult diff = DroneProgramDiff.compare(editSession.getGraph(), candidate);
            serverStatus = diff.isIdentical() ? "Programs are identical"
                    : "Diff: +" + diff.getAddedNodes() + "/-" + diff.getRemovedNodes()
                    + "/~" + diff.getChangedNodes() + " nodes, +" + diff.getAddedEdges()
                    + "/-" + diff.getRemovedEdges() + "/~" + diff.getChangedEdges() + " links";
        } catch (DroneProgramFormatException exception) {
            serverStatus = "Cannot compare: " + exception.getMessage();
        }
    }

    private DroneOfficialProgramTemplate getClientTemplate() {
        List<DroneOfficialProgramTemplate> templates = DroneOfficialProgramTemplates.all();
        if (templates.isEmpty()) return null;
        clientTemplateIndex = Math.floorMod(clientTemplateIndex, templates.size());
        return templates.get(clientTemplateIndex);
    }

    private void moveClientTemplate(int delta) {
        int size = DroneOfficialProgramTemplates.all().size();
        if (size > 0) clientTemplateIndex = Math.floorMod(clientTemplateIndex + delta, size);
    }

    private String getClientTemplateName() {
        DroneOfficialProgramTemplate template = getClientTemplate();
        return template == null ? "-" : I18n.format(template.getNameKey());
    }

    private String getClientTemplateDescription() {
        DroneOfficialProgramTemplate template = getClientTemplate();
        return template == null ? "" : I18n.format(template.getDescriptionKey());
    }

    private String getClientTemplateHardware() {
        DroneOfficialProgramTemplate template = getClientTemplate();
        return template == null ? "" : I18n.format("drtech.drone.programmer.templates.hardware",
                I18n.format(template.getRequiredHardwareKey()));
    }

    private String getClientTemplateSize() {
        DroneOfficialProgramTemplate template = getClientTemplate();
        if (template == null) return "";
        DroneProgramGraph graph = template.createGraph();
        return I18n.format("drtech.drone.programmer.templates.size", graph.getNodes().size(), graph.getEdges().size());
    }

    private String getClientTemplatePage() {
        int size = DroneOfficialProgramTemplates.all().size();
        return size == 0 ? "0/0" : (Math.floorMod(clientTemplateIndex, size) + 1) + "/" + size;
    }

    private static ItemStack findBlankProgramCard(EntityPlayer player) {
        for (int slot = 0; slot < player.inventory.getSizeInventory(); slot++) {
            ItemStack candidate = player.inventory.getStackInSlot(slot);
            if (!candidate.isEmpty() && candidate.getItem() instanceof ItemDroneProgramCard
                    && !((ItemDroneProgramCard) candidate.getItem()).hasProgram(candidate)) {
                return candidate;
            }
        }
        return ItemStack.EMPTY;
    }

    private static String copyProgramName(String name) {
        String base = name == null ? "" : name.trim();
        if (base.isEmpty()) base = "New Drone Program";
        String suffix = " Copy";
        return base.length() <= 64 - suffix.length()
                ? base + suffix : base.substring(0, 64 - suffix.length()) + suffix;
    }

    private int countErrors(DroneGraphEditResult result) {
        return (int) result.getDiagnostics().stream()
                .filter(diagnostic -> diagnostic.getSeverity() == DroneDiagnosticSeverity.ERROR).count();
    }

    private void onEditorOpened(EntityPlayer player) {
        if (getWorld() == null || getWorld().isRemote) return;
        if (editorOwner == null) {
            editorOwner = player.getUniqueID();
            refreshCardSession();
            if (editSession != null && getWorld() != null) {
                DroneProgramLibrary.get(getWorld()).recordOpened(player.getUniqueID(),
                        editSession.getGraph().getProgramId());
            }
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
            editSession = new DroneProgramEditSession(graph, DroneExtensionRegistry.nodes());
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

    private void recordNodeLibraryUse(DroneGraphEditCommand command) {
        if (command == null) return;
        if (command.getType() == com.drppp.drtech.common.drone.program.edit.DroneGraphCommandType.BATCH) {
            for (DroneGraphEditCommand child : command.getCommands()) recordNodeLibraryUse(child);
            return;
        }
        if (command.getType() != com.drppp.drtech.common.drone.program.edit.DroneGraphCommandType.ADD_NODE
                || command.getNodeType() == null || NODE_LIBRARY.get(command.getNodeType()) == null) return;
        List<ResourceLocation> recent = readNodeLibraryEntries(NODE_RECENT_TAG);
        recent.remove(command.getNodeType());
        recent.add(0, command.getNodeType());
        writeNodeLibraryEntries(NODE_RECENT_TAG, recent);
        loadedCardFingerprint = fingerprint(importItems.getStackInSlot(0));
    }

    private List<ResourceLocation> readNodeLibraryEntries(String key) {
        ItemStack card = importItems.getStackInSlot(0);
        if (card.isEmpty() || !(card.getItem() instanceof ItemDroneProgramCard)) return new ArrayList<>();
        NBTTagCompound root = card.getTagCompound();
        if (root == null) return new ArrayList<>();
        NBTTagList entries = root.getTagList(key, 8);
        List<ResourceLocation> result = new ArrayList<>();
        for (int index = 0; index < entries.tagCount() && result.size() < MAX_NODE_LIBRARY_ENTRIES; index++) {
            try {
                ResourceLocation nodeType = new ResourceLocation(entries.getStringTagAt(index));
                if (NODE_LIBRARY.get(nodeType) != null && !result.contains(nodeType)) result.add(nodeType);
            } catch (RuntimeException ignored) {}
        }
        return result;
    }

    private NBTTagList writeNodeLibraryEntriesTag(List<ResourceLocation> entries) {
        NBTTagList result = new NBTTagList();
        for (int index = 0; entries != null && index < entries.size() && index < MAX_NODE_LIBRARY_ENTRIES; index++) {
            ResourceLocation nodeType = entries.get(index);
            if (nodeType != null && NODE_LIBRARY.get(nodeType) != null) {
                result.appendTag(new NBTTagString(nodeType.toString()));
            }
        }
        return result;
    }

    private void writeNodeLibraryEntries(String key, List<ResourceLocation> entries) {
        ItemStack card = importItems.getStackInSlot(0);
        if (card.isEmpty() || !(card.getItem() instanceof ItemDroneProgramCard)) return;
        NBTTagCompound root = card.getTagCompound();
        if (root == null) {
            root = new NBTTagCompound();
            card.setTagCompound(root);
        }
        root.setTag(key, writeNodeLibraryEntriesTag(entries));
    }

    private NBTTagCompound createEditorState(EntityPlayer viewer) {
        refreshCardSession();
        NBTTagCompound state = new NBTTagCompound();
        state.setBoolean("Editable", editorOwner != null && editorOwner.equals(viewer.getUniqueID()));
        state.setString("Status", serverStatus);
        NBTTagList docks = new NBTTagList();
        if (getWorld() != null) {
            List<DroneDockRecord> visibleDocks = DroneDockNetwork.get(getWorld()).listForOwner(
                    viewer.getUniqueID(), getWorld().provider.getDimension());
            long worldTime = getWorld().getTotalWorldTime();
            for (int index = 0; index < Math.min(256, visibleDocks.size()); index++) {
                DroneDockRecord record = visibleDocks.get(index);
                NBTTagCompound dock = record.writeToNbt();
                dock.setBoolean("Online", DroneDockNetwork.isRecordOnline(record, worldTime));
                docks.appendTag(dock);
            }
        }
        state.setTag("Docks", docks);
        NBTTagList drones = new NBTTagList();
        if (getWorld() != null) {
            List<DroneRegistryRecord> visibleDrones = DroneRegistry.get(getWorld()).listForOwner(viewer.getUniqueID());
            long worldTime = getWorld().getTotalWorldTime();
            for (int index = 0; index < Math.min(256, visibleDrones.size()); index++) {
                DroneRegistryRecord record = visibleDrones.get(index);
                NBTTagCompound drone = record.writeToNbt();
                drone.setBoolean("Online", DroneRegistry.isOnline(record, worldTime));
                drones.appendTag(drone);
            }
        }
        state.setTag("Drones", drones);
        NBTTagList programs = new NBTTagList();
        if (getWorld() != null) {
            List<DroneProgramLibraryRecord> visiblePrograms = DroneProgramLibrary.get(getWorld())
                    .listAccessibleForOwner(viewer.getUniqueID());
            for (int index = 0; index < Math.min(128, visiblePrograms.size()); index++) {
                DroneProgramLibraryRecord record = visiblePrograms.get(index);
                NBTTagCompound program = new NBTTagCompound();
                program.setString("ProgramId", record.getProgramId().toString());
                if (!viewer.getUniqueID().equals(record.getOwnerId())) {
                    program.setString("OwnerId", record.getOwnerId().toString());
                }
                program.setString("Name", record.getName());
                program.setLong("Revision", record.getRevision());
                program.setInteger("Nodes", record.getNodeCount());
                program.setInteger("Edges", record.getEdgeCount());
                program.setString("Signature", record.getSignature());
                List<DroneProgramRevision> history = DroneProgramLibrary.get(getWorld())
                        .listRevisions(record.getOwnerId(), record.getProgramId());
                program.setInteger("RevisionCount", history.size());
                StringBuilder historyText = new StringBuilder();
                for (int historyIndex = history.size() - 1, shown = 0; historyIndex >= 0 && shown < 8;
                        historyIndex--, shown++) {
                    DroneProgramRevision revision = history.get(historyIndex);
                    if (historyText.length() > 0) historyText.append("; ");
                    String revisionSignature = revision.getSignature();
                    historyText.append('r').append(revision.getRevision()).append('@').append(revision.getUpdatedAt())
                            .append(' ').append(revisionSignature.length() < 12 ? revisionSignature
                                    : revisionSignature.substring(0, 12));
                }
                program.setString("RevisionHistory", historyText.toString());
                programs.appendTag(program);
            }
        }
        state.setTag("Programs", programs);
        NBTTagList recentPrograms = new NBTTagList();
        if (getWorld() != null) {
            for (UUID programId : DroneProgramLibrary.get(getWorld()).listRecentlyOpened(viewer.getUniqueID())) {
                recentPrograms.appendTag(new NBTTagString(programId.toString()));
            }
        }
        state.setTag("RecentPrograms", recentPrograms);
        state.setTag("NodeFavorites", writeNodeLibraryEntriesTag(readNodeLibraryEntries(NODE_FAVORITES_TAG)));
        state.setTag("NodeRecent", writeNodeLibraryEntriesTag(readNodeLibraryEntries(NODE_RECENT_TAG)));
        state.setString("RemoteReason", getRemoteDisconnectReason(viewer));
        if (editSession == null) return state;
        state.setTag("Program", DroneProgramNbtCodec.write(editSession.getGraph()));
        NBTTagList diagnostics = new NBTTagList();
        List<DroneProgramDiagnostic> editorDiagnostics = new ArrayList<>(
                editSession.getLastCompileResult().getDiagnostics());
        editorDiagnostics.addAll(DroneProgramHardwareValidator.validate(editSession.getGraph(),
                importItems.getStackInSlot(1), getWorld()));
        for (DroneProgramDiagnostic diagnostic : editorDiagnostics) {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setString("Severity", diagnostic.getSeverity().name());
            tag.setString("Code", diagnostic.getCode().name());
            if (diagnostic.getNodeId() != null) tag.setString("Node", diagnostic.getNodeId().toString());
            if (diagnostic.getPortId() != null) tag.setString("Port", diagnostic.getPortId());
            if (diagnostic.getPropertyId() != null) tag.setString("Property", diagnostic.getPropertyId());
            for (int argument = 0; argument < Math.min(3, diagnostic.getArguments().size()); argument++) {
                tag.setString("Detail" + argument, diagnostic.getArguments().get(argument));
            }
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
        clientDiagnostics.clear();
        clientDiagnosticNodeIds.clear();
        clientDiagnosticSeverityByNode.clear();
        clientActiveNodeId = null;
        clientRuntimeStatus = "NOT RUN";
        clientRemoteConnected = false;
        clientRemoteEntityId = -1;
        updateLiveWorldPreviewTracking();
        clientRemoteInfo = localizeRemoteDisconnectReason(state.getString("RemoteReason"));
        clientRemotePosition = null;
        clientRemoteVariables = Collections.emptyList();
        clientRemoteVariableOffset = 0;
        clientRemoteTrace = "";
        clientRemoteTraces = Collections.emptyList();
        clientRemoteTraceFilter = 0;
        clientRemoteTraceOffset = 0;
        clientRemoteActionStatus = "SUCCESS";
        clientRemoteActionError = "";
        clientRemoteNodeTicks = 0L;
        clientRemotePorts = Collections.emptyList();
        clientRemotePortOffset = 0;
        clientRemoteOutputNode = "";
        clientRemoteOutputPort = "";
        clientRemoteOutputAmount = 0L;
        clientRemoteNodeType = "-";
        clientRemotePathTarget = null;
        clientRemotePathWaypoint = null;
        clientRemotePathIndex = 0;
        clientRemotePathLength = 0;
        clientRemotePath = Collections.emptyList();
        if (clientRemotePathPreview) DroneWorldPreviewRenderer.clearNavigationPath();
        clientRemoteAreaIndex = -1;
        clientRemoteAreaTotal = 0;
        clientRemoteAreaPosition = null;
        if (clientRemoteCoordinateHighlight) DroneWorldPreviewRenderer.clearExecutionCoordinate();
        clientRemoteEnergyStored = 0L;
        clientRemoteEnergyCapacity = 0L;
        clientRemoteEstimatedAreaCompleted = 0;
        clientRemoteEstimatedAreaTotal = 0;
        clientRemoteEstimatedAreaRemainingEu = -1L;
        List<ClientDockResult> dockDirectory = new ArrayList<>();
        NBTTagList docks = state.getTagList("Docks", 10);
        for (int index = 0; index < docks.tagCount(); index++) {
            NBTTagCompound dock = docks.getCompoundTagAt(index);
            if (!dock.hasKey("DockId", 8) || !dock.hasKey("Position", 4)) continue;
            try {
                dockDirectory.add(new ClientDockResult(UUID.fromString(dock.getString("DockId")),
                        dock.getString("Name"), dock.getInteger("Dimension"),
                        BlockPos.fromLong(dock.getLong("Position")), dock.getBoolean("Online"),
                        dock.getLong("AvailableEU"), dock.getInteger("Tier"), dock.getInteger("Priority"),
                        dock.getInteger("CurrentLoad"), dock.getBoolean("Enabled"),
                        dock.getBoolean("CanAcceptDrone"), dock.getString("OccupancyState")));
            } catch (IllegalArgumentException ignored) {}
        }
        clientDockDirectory = Collections.unmodifiableList(dockDirectory);
        if (clientDockDirectory.isEmpty()) clientDockResultIndex = 0;
        else clientDockResultIndex = Math.floorMod(clientDockResultIndex, clientDockDirectory.size());
        List<ClientFleetResult> fleetDirectory = new ArrayList<>();
        NBTTagList drones = state.getTagList("Drones", 10);
        for (int index = 0; index < drones.tagCount(); index++) {
            NBTTagCompound drone = drones.getCompoundTagAt(index);
            if (!drone.hasKey("DroneId", 8) || !drone.hasKey("Position", 4)) continue;
            try {
                fleetDirectory.add(new ClientFleetResult(UUID.fromString(drone.getString("DroneId")),
                        drone.getInteger("Dimension"),
                        BlockPos.fromLong(drone.getLong("Position")), drone.getString("Chassis"),
                        drone.getLong("EnergyStored"), drone.getLong("EnergyCapacity"),
                        drone.getInteger("CargoOccupiedSlots"), drone.getInteger("CargoCapacitySlots"),
                        drone.getString("Status"), drone.getString("ProgramId"),
                        drone.getLong("ProgramRevision"), drone.getBoolean("Online"),
                        drone.hasKey("BoundDock", 4) ? BlockPos.fromLong(drone.getLong("BoundDock")) : null));
            } catch (IllegalArgumentException ignored) {}
        }
        clientFleetDirectory = Collections.unmodifiableList(fleetDirectory);
        if (clientFleetDirectory.isEmpty()) clientFleetResultIndex = 0;
        else clientFleetResultIndex = Math.floorMod(clientFleetResultIndex, clientFleetDirectory.size());
        List<ClientLibraryProgramResult> programDirectory = new ArrayList<>();
        NBTTagList programs = state.getTagList("Programs", 10);
        for (int index = 0; index < programs.tagCount(); index++) {
            NBTTagCompound program = programs.getCompoundTagAt(index);
            if (!program.hasKey("ProgramId", 8) || !program.hasKey("Revision", 99)) continue;
            try {
                programDirectory.add(new ClientLibraryProgramResult(
                        UUID.fromString(program.getString("ProgramId")), program.getString("Name"),
                        program.getLong("Revision"), program.getInteger("Nodes"), program.getInteger("Edges"),
                        program.getString("Signature"), program.getInteger("RevisionCount"),
                        program.getString("RevisionHistory")));
            } catch (IllegalArgumentException ignored) {}
        }
        clientProgramDirectory = Collections.unmodifiableList(programDirectory);
        NBTTagList recentPrograms = state.getTagList("RecentPrograms", 8);
        final List<UUID> recentOrder = new ArrayList<>();
        for (int index = 0; index < recentPrograms.tagCount(); index++) {
            try { recentOrder.add(UUID.fromString(recentPrograms.getStringTagAt(index))); }
            catch (IllegalArgumentException ignored) { }
        }
        if (!recentOrder.isEmpty()) {
            programDirectory.sort((left, right) -> {
                int leftIndex = recentOrder.indexOf(left.programId);
                int rightIndex = recentOrder.indexOf(right.programId);
                if (leftIndex < 0) leftIndex = Integer.MAX_VALUE;
                if (rightIndex < 0) rightIndex = Integer.MAX_VALUE;
                return Integer.compare(leftIndex, rightIndex);
            });
            clientProgramDirectory = Collections.unmodifiableList(programDirectory);
        }
        if (clientProgramDirectory.isEmpty()) clientLibraryProgramResultIndex = 0;
        else clientLibraryProgramResultIndex = Math.floorMod(clientLibraryProgramResultIndex,
                clientProgramDirectory.size());
        clientFavoriteNodeTypes = readClientNodeLibraryEntries(state.getTagList("NodeFavorites", 8));
        clientRecentNodeTypes = readClientNodeLibraryEntries(state.getTagList("NodeRecent", 8));
        NBTTagList diagnostics = state.getTagList("Diagnostics", 10);
        for (int i = 0; i < diagnostics.tagCount(); i++) {
            String severity = diagnostics.getCompoundTagAt(i).getString("Severity");
            if (DroneDiagnosticSeverity.ERROR.name().equals(severity)) clientErrors++;
            if (DroneDiagnosticSeverity.WARNING.name().equals(severity)) clientWarnings++;
            NBTTagCompound diagnostic = diagnostics.getCompoundTagAt(i);
            String node = diagnostic.getString("Node");
            String port = diagnostic.getString("Port");
            String property = diagnostic.getString("Property");
            String detail = diagnostic.getString("Detail0");
            String detail2 = diagnostic.getString("Detail1");
            String detail3 = diagnostic.getString("Detail2");
            String localizedSeverity = localizeDiagnosticSeverity(severity);
            String code = diagnostic.getString("Code");
            String localizedCode = localizeDiagnosticCode(code);
            String localizedPort = port.isEmpty() ? "" : " [" + localizePort(port) + "]";
            clientDiagnosticLines.add(localizedSeverity + "：" + localizedCode + localizedPort);
            UUID nodeId = null;
            if (!node.isEmpty()) {
                try {
                    nodeId = UUID.fromString(node);
                    clientDiagnosticNodeIds.add(nodeId);
                    DroneDiagnosticSeverity parsedSeverity;
                    try { parsedSeverity = DroneDiagnosticSeverity.valueOf(severity); }
                    catch (IllegalArgumentException invalid) { parsedSeverity = DroneDiagnosticSeverity.INFO; }
                    DroneDiagnosticSeverity previous = clientDiagnosticSeverityByNode.get(nodeId);
                    if (previous == null
                            || diagnosticSeverityOrder(parsedSeverity) < diagnosticSeverityOrder(previous)) {
                        clientDiagnosticSeverityByNode.put(nodeId, parsedSeverity);
                    }
                } catch (IllegalArgumentException ignored) {}
            }
            clientDiagnostics.add(new ClientDiagnostic(severity, code, nodeId, port, property,
                    detail, detail2, detail3));
        }
        clampDiagnosticPage();
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
            clientRemoteEntityId = remote.getInteger("EntityId");
            if (remote.hasKey("Position", 4)) clientRemotePosition = BlockPos.fromLong(remote.getLong("Position"));
            String effectiveStatus = remote.hasKey("EffectiveStatus", 8)
                    ? remote.getString("EffectiveStatus") : remote.getString("Status");
            clientRuntimeStatus = localizeRuntimeStatus(effectiveStatus);
            String progress = remote.getString("Progress");
            clientRemoteNodeType = remote.getString("InputNode");
            if (clientRemoteNodeType.isEmpty()) clientRemoteNodeType = remote.getString("Node");
            String modules = remote.getString("Modules");
            if ("none".equals(modules)) modules = I18n.format("drtech.drone.remote.no_modules");
            String trace = remote.getString("Trace");
            clientRemoteTrace = "No execution trace".equals(trace)
                    ? I18n.format("drtech.drone.remote.no_trace") : localizeRemoteTrace(trace);
            List<ClientRemoteTrace> traces = new ArrayList<>();
            NBTTagList traceList = remote.getTagList("TraceList", 10);
            for (int index = 0; index < traceList.tagCount()
                    && traces.size() < DroneProgramRuntime.MAX_TRACE_ENTRIES; index++) {
                NBTTagCompound entry = traceList.getCompoundTagAt(index);
                String traceText = entry.getString("Text");
                if (traceText.isEmpty()) continue;
                UUID traceNodeId = null;
                try {
                    if (entry.hasKey("Node", 8)) traceNodeId = UUID.fromString(entry.getString("Node"));
                } catch (IllegalArgumentException ignored) { }
                traces.add(new ClientRemoteTrace(traceText, traceNodeId));
            }
            clientRemoteTraces = Collections.unmodifiableList(traces);
            if (clientGraph != null) clientGraph.updateEditorTimeline(traceList);
            clampRemoteTraceOffset();
            clientRemoteActionStatus = remote.getString("ActionStatus");
            if (clientRemoteActionStatus.isEmpty()) clientRemoteActionStatus = "SUCCESS";
            clientRemoteActionError = remote.getString("ActionError");
            clientRemoteNodeTicks = Math.max(0L, remote.getLong("NodeTicks"));
            List<ClientRemotePort> ports = new ArrayList<>();
            NBTTagList portInputs = remote.getTagList("PortInputs", 10);
            for (int index = 0; index < portInputs.tagCount() && ports.size() < 16; index++) {
                NBTTagCompound input = portInputs.getCompoundTagAt(index);
                String port = input.getString("Port");
                if (!port.isEmpty()) ports.add(new ClientRemotePort(port, input.getString("Value")));
            }
            clientRemotePorts = Collections.unmodifiableList(ports);
            clampRemotePortOffset();
            clientRemoteOutputNode = remote.getString("OutputNode");
            clientRemoteOutputPort = remote.getString("OutputPort");
            clientRemoteOutputAmount = Math.max(0L, remote.getLong("OutputAmount"));
            clientRemotePathTarget = remote.hasKey("PathTarget", 4)
                    ? BlockPos.fromLong(remote.getLong("PathTarget")) : null;
            clientRemotePathWaypoint = remote.hasKey("PathWaypoint", 4)
                    ? BlockPos.fromLong(remote.getLong("PathWaypoint")) : null;
            clientRemotePathIndex = Math.max(0, remote.getInteger("PathIndex"));
            clientRemotePathLength = Math.max(0, remote.getInteger("PathLength"));
            List<BlockPos> pathPoints = new ArrayList<>();
            NBTTagList remotePath = remote.getTagList("PathPoints", 10);
            for (int index = 0; index < remotePath.tagCount() && pathPoints.size() < 256; index++) {
                NBTTagCompound point = remotePath.getCompoundTagAt(index);
                if (point.hasKey("Position", 4)) pathPoints.add(BlockPos.fromLong(point.getLong("Position")));
            }
            clientRemotePath = Collections.unmodifiableList(pathPoints);
            if (clientRemotePathPreview && !clientRemotePath.isEmpty()) {
                DroneWorldPreviewRenderer.setNavigationPath(clientRemotePath);
            }
            clientRemoteAreaIndex = remote.getInteger("AreaIndex");
            clientRemoteAreaTotal = Math.max(0, remote.getInteger("AreaTotal"));
            clientRemoteAreaPosition = remote.hasKey("CurrentAreaPosition", 4)
                    ? BlockPos.fromLong(remote.getLong("CurrentAreaPosition")) : null;
            if (clientRemoteCoordinateHighlight && clientRemoteAreaPosition != null) {
                DroneWorldPreviewRenderer.setExecutionCoordinate(clientRemoteAreaPosition);
            }
            clientRemoteEnergyStored = Math.max(0L, remote.getLong("EnergyStored"));
            clientRemoteEnergyCapacity = Math.max(0L, remote.getLong("EnergyCapacity"));
            clientRemoteEstimatedAreaCompleted = Math.max(0, remote.getInteger("EstimatedAreaCompleted"));
            clientRemoteEstimatedAreaTotal = Math.max(0, remote.getInteger("EstimatedAreaTotal"));
            clientRemoteEstimatedAreaRemainingEu = remote.getLong("EstimatedAreaRemainingEu");
            List<ClientRemoteVariable> variables = new ArrayList<>();
            NBTTagList variableList = remote.getTagList("VariableList", 10);
            for (int index = 0; index < variableList.tagCount() && variables.size() < 64; index++) {
                NBTTagCompound variable = variableList.getCompoundTagAt(index);
                String name = variable.getString("Name");
                double value = variable.getDouble("Value");
                if (!name.isEmpty() && Double.isFinite(value)) variables.add(new ClientRemoteVariable(name, value));
            }
            clientRemoteVariables = Collections.unmodifiableList(variables);
            clampRemoteVariableOffset();
            updateLiveWorldPreviewTracking();
            StringBuilder info = new StringBuilder(remote.getString("Chassis"))
                    .append(" | EU ").append(remote.getInteger("EnergyPercent")).append("% | ")
                    .append(localizeRuntimeStatus(effectiveStatus));
            info.append('\n').append(localizeNodePath(remote.getString("Node")))
                    .append(progress.isEmpty() ? "" : " " + progress);
            if (remote.getBoolean("ProgramSuspended")) {
                info.append('\n').append(I18n.format("drtech.drone.remote.safety",
                        localizeSafetyState(remote.getString("SafetyState"))));
            } else if (clientRemotePosition != null) {
                info.append('\n').append(I18n.format("drtech.drone.programmer.capture_drone_position",
                        clientRemotePosition.getX(), clientRemotePosition.getY(), clientRemotePosition.getZ()));
            }
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

    private List<ResourceLocation> readClientNodeLibraryEntries(NBTTagList entries) {
        List<ResourceLocation> result = new ArrayList<>();
        for (int index = 0; index < entries.tagCount() && result.size() < MAX_NODE_LIBRARY_ENTRIES; index++) {
            try {
                ResourceLocation nodeType = new ResourceLocation(entries.getStringTagAt(index));
                if (NODE_LIBRARY.get(nodeType) != null && !result.contains(nodeType)) result.add(nodeType);
            } catch (RuntimeException ignored) {}
        }
        return Collections.unmodifiableList(result);
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

    private Map<UUID, DroneDiagnosticSeverity> getClientDiagnosticSeverityByNode() {
        return Collections.unmodifiableMap(clientDiagnosticSeverityByNode);
    }

    private static int diagnosticSeverityOrder(DroneDiagnosticSeverity severity) {
        return severity == DroneDiagnosticSeverity.ERROR ? 0
                : severity == DroneDiagnosticSeverity.WARNING ? 1 : 2;
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

    private String getDiagnosticSummary() {
        return I18n.format("drtech.drone.diagnostic.summary", clientErrors, clientWarnings,
                filteredDiagnostics().size());
    }

    private String getDiagnosticFilterLabel(int filter) {
        String name = I18n.format("drtech.drone.diagnostic.filter." + switch (filter) {
            case 1 -> "errors";
            case 2 -> "warnings";
            case 3 -> "info";
            default -> "all";
        } + ".short");
        return clientDiagnosticFilter == filter ? "[" + name + "]" : name;
    }

    private List<ClientDiagnostic> filteredDiagnostics() {
        if (clientDiagnosticFilter == 0) return clientDiagnostics;
        String severity = switch (clientDiagnosticFilter) {
            case 1 -> DroneDiagnosticSeverity.ERROR.name();
            case 2 -> DroneDiagnosticSeverity.WARNING.name();
            case 3 -> DroneDiagnosticSeverity.INFO.name();
            default -> "";
        };
        List<ClientDiagnostic> filtered = new ArrayList<>();
        for (ClientDiagnostic diagnostic : clientDiagnostics) {
            if (severity.equals(diagnostic.severity)) filtered.add(diagnostic);
        }
        return filtered;
    }

    private ClientDiagnostic getDiagnosticAtRow(int row) {
        List<ClientDiagnostic> diagnostics = filteredDiagnostics();
        int index = clientDiagnosticPage * 3 + row;
        return index >= 0 && index < diagnostics.size() ? diagnostics.get(index) : null;
    }

    private String getDiagnosticRowLabel(int row) {
        ClientDiagnostic diagnostic = getDiagnosticAtRow(row);
        if (diagnostic == null) return "";
        int number = clientDiagnosticPage * 3 + row + 1;
        String severity = localizeDiagnosticSeverity(diagnostic.severity);
        String code = localizeDiagnosticCode(diagnostic.code);
        String node = diagnostic.nodeId == null ? "" : " · " + diagnosticNodeLabel(diagnostic.nodeId);
        return number + ". " + shorten(severity, 4) + " · " + shorten(code, 10) + shorten(node, 7);
    }

    private String getDiagnosticRowTooltip(int row) {
        ClientDiagnostic diagnostic = getDiagnosticAtRow(row);
        if (diagnostic == null) return "";
        StringBuilder text = new StringBuilder(localizeDiagnosticSeverity(diagnostic.severity))
                .append("：").append(localizeDiagnosticCode(diagnostic.code));
        if (diagnostic.nodeId != null) {
            text.append(" | ").append(I18n.format("drtech.drone.diagnostic.node",
                    diagnosticNodeLabel(diagnostic.nodeId)));
        }
        if (!diagnostic.port.isEmpty()) {
            text.append(" | ").append(I18n.format("drtech.drone.diagnostic.port",
                    localizePort(diagnostic.port)));
        }
        if (!diagnostic.property.isEmpty()) {
            text.append(" | ").append(I18n.format("drtech.drone.diagnostic.property",
                    localizeOrFallback("drtech.drone.property."
                            + diagnostic.property.toLowerCase(java.util.Locale.ROOT), diagnostic.property)));
        }
        if (!diagnostic.detail.isEmpty()) {
            text.append(" | ").append(I18n.format("drtech.drone.diagnostic.detail",
                    localizeDiagnosticDetail(diagnostic)));
        }
        return text.toString();
    }

    private String getDiagnosticRepairTooltip(int row) {
        ClientDiagnostic diagnostic = getDiagnosticAtRow(row);
        if (diagnostic == null || diagnostic.code.isEmpty()) return "";
        String key = "drtech.drone.diagnostic.fix." + diagnostic.code.toLowerCase(java.util.Locale.ROOT);
        return localizeOrFallback(key, I18n.format("drtech.drone.diagnostic.fix.default"));
    }

    private String diagnosticNodeLabel(UUID nodeId) {
        DroneProgramNode node = clientGraph == null ? null : clientGraph.getNode(nodeId);
        if (node == null) return nodeId.toString().substring(0, 8);
        String alias = node.getConfiguration().getString("Label");
        return alias.isEmpty() ? localizeNodePath(node.getType().toString()) : alias;
    }

    private int getDiagnosticPageCount() {
        return Math.max(1, (filteredDiagnostics().size() + 2) / 3);
    }

    private String getDiagnosticPageLabel() {
        return I18n.format("drtech.drone.diagnostic.page", clientDiagnosticPage + 1, getDiagnosticPageCount());
    }

    private void moveDiagnosticPage(int delta) {
        int pages = getDiagnosticPageCount();
        clientDiagnosticPage = (clientDiagnosticPage + delta + pages) % pages;
    }

    private void clampDiagnosticPage() {
        clientDiagnosticPage = Math.max(0, Math.min(clientDiagnosticPage, getDiagnosticPageCount() - 1));
    }

    private static String shorten(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) return value == null ? "" : value;
        return value.substring(0, Math.max(1, maxLength - 1)) + "…";
    }

    private String getRemoteDebugText() {
        return clientRemoteInfo;
    }

    private String getRemoteActionText() {
        return I18n.format("drtech.drone.remote.action", localizeActionStatus(clientRemoteActionStatus),
                clientRemoteNodeTicks);
    }

    private String getRemoteActionErrorText() {
        if (clientRemoteActionError == null || clientRemoteActionError.isEmpty()) return "";
        return shorten(I18n.format("drtech.drone.remote.action_error", clientRemoteActionError), 28);
    }

    private String getRemotePortNodeText() {
        return I18n.format("drtech.drone.remote.port_node", localizeNodePath(clientRemoteNodeType));
    }

    private String getRemotePortPageLabel() {
        if (clientRemotePorts.isEmpty()) return "";
        int first = clientRemotePortOffset + 1;
        int last = Math.min(clientRemotePorts.size(), clientRemotePortOffset + 6);
        return I18n.format("drtech.drone.remote.variable_page", first, last, clientRemotePorts.size());
    }

    private String getRemotePortRow(int row) {
        if (clientRemotePorts.isEmpty()) {
            return row == 0 ? I18n.format("drtech.drone.remote.no_port_inputs") : "";
        }
        int index = clientRemotePortOffset + row;
        if (index < 0 || index >= clientRemotePorts.size()) return "";
        ClientRemotePort input = clientRemotePorts.get(index);
        return localizePort(input.port) + " = " + abbreviateRemoteValue(input.value, 72);
    }

    private void moveRemotePortOffset(int delta) {
        clientRemotePortOffset += delta;
        clampRemotePortOffset();
    }

    private void clampRemotePortOffset() {
        int maximum = Math.max(0, clientRemotePorts.size() - 6);
        clientRemotePortOffset = Math.max(0, Math.min(maximum, clientRemotePortOffset));
    }

    private String getRemoteOutputText() {
        if (clientRemoteOutputPort == null || clientRemoteOutputPort.isEmpty()) {
            return I18n.format("drtech.drone.remote.no_port_output");
        }
        String source = clientRemoteOutputNode == null || clientRemoteOutputNode.isEmpty()
                ? localizeNodePath(clientRemoteNodeType) : localizeNodePath(clientRemoteOutputNode);
        return I18n.format("drtech.drone.remote.port_output", source, localizePort(clientRemoteOutputPort),
                clientRemoteOutputAmount);
    }

    private String getRemoteNavigationText() {
        StringBuilder text = new StringBuilder();
        text.append(I18n.format("drtech.drone.remote.navigation_position", formatRemotePosition(clientRemotePosition)));
        text.append('\n').append(I18n.format("drtech.drone.remote.navigation_target",
                formatRemotePosition(clientRemotePathTarget)));
        if (clientRemotePathLength > 0) {
            text.append('\n').append(I18n.format("drtech.drone.remote.navigation_path",
                    Math.min(clientRemotePathLength, clientRemotePathIndex + 1), clientRemotePathLength,
                    formatRemotePosition(clientRemotePathWaypoint)));
        } else {
            text.append('\n').append(I18n.format("drtech.drone.remote.navigation_path_idle"));
        }
        if (clientRemoteAreaIndex >= 0 && clientRemoteAreaTotal > 0) {
            text.append('\n').append(I18n.format("drtech.drone.remote.navigation_area",
                    Math.min(clientRemoteAreaTotal, clientRemoteAreaIndex + 1), clientRemoteAreaTotal));
        }
        text.append('\n').append(I18n.format("drtech.drone.remote.energy_remaining",
                clientRemoteEnergyStored, clientRemoteEnergyCapacity));
        if (clientRemoteEstimatedAreaRemainingEu >= 0L && clientRemoteEstimatedAreaTotal > 0) {
            text.append('\n').append(I18n.format("drtech.drone.remote.energy_area_estimate",
                    clientRemoteEstimatedAreaRemainingEu, clientRemoteEstimatedAreaCompleted,
                    clientRemoteEstimatedAreaTotal));
        } else {
            text.append('\n').append(I18n.format("drtech.drone.remote.energy_estimate_pending"));
        }
        return text.toString();
    }

    private String getRemoteCoordinateHighlightLabel() {
        return I18n.format(clientRemoteCoordinateHighlight
                ? "drtech.drone.remote.coordinate_highlight_on"
                : "drtech.drone.remote.coordinate_highlight_off");
    }

    private void toggleRemoteCoordinateHighlight() {
        clientRemoteCoordinateHighlight = !clientRemoteCoordinateHighlight;
        updateLiveWorldPreviewTracking();
    }

    private String getRemotePathPreviewLabel() {
        return I18n.format(clientRemotePathPreview
                ? "drtech.drone.remote.path_preview_on" : "drtech.drone.remote.path_preview_off");
    }

    private void toggleRemotePathPreview() {
        clientRemotePathPreview = !clientRemotePathPreview;
        updateLiveWorldPreviewTracking();
    }

    private void updateLiveWorldPreviewTracking() {
        DroneWorldPreviewRenderer.trackLiveDrone(clientRemoteEntityId,
                clientRemoteCoordinateHighlight, clientRemotePathPreview);
    }

    private String getAreaProjectionLabel() {
        return I18n.format(clientAreaProjectionEnabled
                ? "drtech.drone.programmer.area_project_hide.short"
                : "drtech.drone.programmer.area_project_show.short");
    }

    private void toggleAreaProjection(DroneProgramCanvasWidget canvas) {
        if (clientAreaProjectionEnabled) {
            DroneWorldPreviewRenderer.clearProjectedArea();
            clientAreaProjectionEnabled = false;
            return;
        }
        DroneArea area = canvas.getSelectedAreaPreview();
        if (area == null || !area.isWithinRuntimeLimits()) return;
        DroneWorldPreviewRenderer.setProjectedArea(area);
        clientAreaProjectionEnabled = true;
    }

    /** Three visible rows plus wheel scrolling keeps wireless debug readable at every supported GUI scale. */
    private String getRemoteVariableRow(int row) {
        if (clientRemoteVariables.isEmpty()) {
            return row == 0 ? I18n.format("drtech.drone.remote.no_variables") : "";
        }
        int index = clientRemoteVariableOffset + row;
        if (index < 0 || index >= clientRemoteVariables.size()) return "";
        ClientRemoteVariable variable = clientRemoteVariables.get(index);
        return variable.name + " = " + formatRemoteNumber(variable.value);
    }

    private String getRemoteVariablePageLabel() {
        if (clientRemoteVariables.isEmpty()) return "";
        int first = clientRemoteVariableOffset + 1;
        int last = Math.min(clientRemoteVariables.size(), clientRemoteVariableOffset + 3);
        return I18n.format("drtech.drone.remote.variable_page", first, last, clientRemoteVariables.size());
    }

    private void moveRemoteVariableOffset(int delta) {
        clientRemoteVariableOffset += delta;
        clampRemoteVariableOffset();
    }

    private void clampRemoteVariableOffset() {
        int maximum = Math.max(0, clientRemoteVariables.size() - 3);
        clientRemoteVariableOffset = Math.max(0, Math.min(maximum, clientRemoteVariableOffset));
    }

    private String getRemoteTraceText() {
        return clientRemoteTrace == null || clientRemoteTrace.isEmpty()
                ? I18n.format("drtech.drone.remote.no_trace") : clientRemoteTrace;
    }

    private String getRemoteTraceRow(int row) {
        ClientRemoteTrace trace = getRemoteTraceAt(row);
        if (trace == null) {
            return row == 0 && filteredRemoteTraces().isEmpty()
                    ? I18n.format("drtech.drone.remote.no_trace") : "";
        }
        String localized = localizeRemoteTrace(trace.text).replace('\r', ' ').replace('\n', ' ')
                .replaceAll("\\s+", " ").trim();
        return shorten(localized, 28);
    }

    private String getRemoteTraceTooltip(int row) {
        ClientRemoteTrace trace = getRemoteTraceAt(row);
        return trace == null ? "" : localizeRemoteTrace(trace.text);
    }

    private void moveRemoteTraceOffset(int delta) {
        clientRemoteTraceOffset += delta;
        clampRemoteTraceOffset();
    }

    private void clampRemoteTraceOffset() {
        clientRemoteTraceOffset = Math.max(0, Math.min(Math.max(0, filteredRemoteTraces().size() - 2),
                clientRemoteTraceOffset));
    }

    private void focusRemoteTrace(DroneProgramCanvasWidget canvas, int row) {
        ClientRemoteTrace trace = getRemoteTraceAt(row);
        if (trace == null || trace.nodeId == null || !canvas.focusNode(trace.nodeId)) return;
        clientInspectorPage = 0;
    }

    private ClientRemoteTrace getRemoteTraceAt(int row) {
        List<ClientRemoteTrace> traces = filteredRemoteTraces();
        int index = traces.size() - 1 - clientRemoteTraceOffset - row;
        return index < 0 || index >= traces.size() ? null : traces.get(index);
    }

    private List<ClientRemoteTrace> filteredRemoteTraces() {
        if (clientRemoteTraceFilter == 0 || clientRemoteTraces.isEmpty()) return clientRemoteTraces;
        List<ClientRemoteTrace> filtered = new ArrayList<>();
        for (ClientRemoteTrace trace : clientRemoteTraces) {
            if (clientRemoteTraceFilter == 1 && trace.text.startsWith("ERROR ")) filtered.add(trace);
            if (clientRemoteTraceFilter == 2 && trace.text.startsWith("BREAK ")) filtered.add(trace);
        }
        return filtered;
    }

    private String remoteTraceFilterKey(int filter) {
        String suffix = filter == 1 ? "error" : filter == 2 ? "break" : "all";
        return "drtech.drone.remote.trace_filter." + suffix + (clientRemoteTraceFilter == filter ? "_active" : "");
    }

    private static String formatRemoteNumber(double value) {
        if (value == Math.rint(value) && Math.abs(value) <= Long.MAX_VALUE) return Long.toString((long) value);
        return Double.toString(value);
    }

    private static String abbreviateRemoteValue(String value, int maxLength) {
        if (value == null || value.isEmpty()) return "<empty>";
        String normalized = value.replace('\n', ' ').replace('\r', ' ');
        return normalized.length() <= maxLength ? normalized : normalized.substring(0, Math.max(1, maxLength - 3)) + "...";
    }

    private static String formatRemotePosition(BlockPos position) {
        return position == null ? "-" : position.getX() + ", " + position.getY() + ", " + position.getZ();
    }

    private static String localizeActionStatus(String status) {
        String fallback = status == null || status.isEmpty() ? "SUCCESS" : status;
        String key = "drtech.drone.action_status." + fallback.toLowerCase(Locale.ROOT);
        String localized = I18n.format(key);
        return key.equals(localized) ? fallback : localized;
    }

    private static String localizeRemoteTrace(String trace) {
        if (trace == null || trace.isEmpty()) return I18n.format("drtech.drone.remote.no_trace");
        StringBuilder localized = new StringBuilder();
        for (String entry : trace.split("\\n")) {
            if (localized.length() > 0) localized.append('\n');
            if ("END".equals(entry)) {
                localized.append(I18n.format("drtech.drone.trace.end"));
            } else if (entry.startsWith("BREAK ")) {
                localized.append(I18n.format("drtech.drone.trace.breakpoint", localizeNodePath(entry.substring(6))));
            } else if (entry.endsWith(" PAUSED")) {
                localized.append(I18n.format("drtech.drone.trace.paused",
                        localizeNodePath(entry.substring(0, entry.length() - 7))));
            } else if (entry.startsWith("ERROR ")) {
                localized.append(I18n.format("drtech.drone.trace.error", entry.substring(6)));
            } else {
                int arrow = entry.indexOf(" -> ");
                if (arrow > 0) {
                    String port = entry.substring(arrow + 4);
                    localized.append(I18n.format("drtech.drone.trace.transition", localizeNodePath(entry.substring(0, arrow)),
                            localizePort(port)));
                } else {
                    localized.append(localizeNodePath(entry));
                }
            }
        }
        return localized.toString();
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

    private static String localizeDiagnosticDetail(ClientDiagnostic diagnostic) {
        if ("REQUIRED_UPGRADE_MISSING".equals(diagnostic.code)) {
            return localizeOrFallback("drtech.drone.upgrade." + diagnostic.detail, diagnostic.detail);
        }
        if ("CHASSIS_TIER_RECOMMENDED".equals(diagnostic.code)) {
            return I18n.format("drtech.drone.diagnostic.detail.chassis", diagnostic.detail,
                    diagnostic.detail3, diagnostic.detail2);
        }
        if ("VOLTAGE_TIER_MISMATCH".equals(diagnostic.code)) {
            return I18n.format("drtech.drone.diagnostic.detail.voltage", diagnostic.detail,
                    diagnostic.detail2, localizeOrFallback("drtech.drone.diagnostic.voltage."
                            + diagnostic.detail3, diagnostic.detail3));
        }
        return diagnostic.detail;
    }

    private static String localizeOrFallback(String key, String fallback) {
        String localized = I18n.format(key);
        return key.equals(localized) ? fallback : localized;
    }

    private static String localizeRemoteDisconnectReason(String reason) {
        String normalized = reason == null || reason.isEmpty() ? "TARGET_UNLOADED" : reason;
        return localizeOrFallback("drtech.drone.remote.reason."
                + normalized.toLowerCase(Locale.ROOT), I18n.format("drtech.drone.remote.disconnected"));
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
            case "Cannot write: target drone hardware incompatible":
                return I18n.format("drtech.drone.programmer.status.hardware_incompatible");
            case "Cannot write: a newer program revision exists":
                return I18n.format("drtech.drone.programmer.status.revision_conflict");
            case "Cannot write: program is owned by another player":
                return I18n.format("drtech.drone.programmer.status.program_owner_mismatch");
            case "Insert a programmable drone in the second slot":
                return I18n.format("drtech.drone.programmer.status.insert_drone");
            case "Program written to drone": return I18n.format("drtech.drone.programmer.status.written");
            case "Program written to drone and program library":
                return I18n.format("drtech.drone.programmer.status.written_library");
            case "Program written to drone; program library is full":
                return I18n.format("drtech.drone.programmer.status.written_library_full");
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

    /**
     * Keeps an absent remote target explainable without exposing another owner's drone position or program data.
     * The search is deliberately limited to loaded entities in the programmer's world.
     */
    private String getRemoteDisconnectReason(EntityPlayer viewer) {
        if (getWorld() == null || getWorld().isRemote || viewer == null || viewer.world != getWorld()) {
            return "WRONG_WORLD";
        }
        if (editSession == null) return "NO_PROGRAM_CARD";
        if (editorOwner == null || !editorOwner.equals(viewer.getUniqueID())) return "EDITOR_LOCKED";
        UUID programId = editSession.getGraph().getProgramId();
        long programRevision = editSession.getGraph().getRevision();
        AxisAlignedBB search = new AxisAlignedBB(getPos()).grow(MAX_REMOTE_DEBUG_RANGE);
        boolean hasOwnedDrone = false;
        boolean hasProgramMatch = false;
        boolean hasRevisionMatch = false;
        for (EntityProgrammableDrone drone : getWorld().getEntitiesWithinAABB(EntityProgrammableDrone.class, search)) {
            if (drone.isDead || !drone.isOwner(viewer)) continue;
            hasOwnedDrone = true;
            if (!programId.equals(drone.getProgramId())) continue;
            hasProgramMatch = true;
            if (programRevision != drone.getProgramRevision()) continue;
            hasRevisionMatch = true;
            double distance = drone.getDistanceSqToCenter(getPos());
            if (distance > (double) drone.getWirelessRange() * drone.getWirelessRange()) return "OUT_OF_RANGE";
            return "CONNECTED";
        }
        if (!hasOwnedDrone) return "TARGET_UNLOADED";
        if (!hasProgramMatch) return "PROGRAM_MISMATCH";
        if (!hasRevisionMatch) return "REVISION_MISMATCH";
        return "TARGET_UNLOADED";
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

    private static final class ClientDiagnostic {
        private final String severity;
        private final String code;
        private final UUID nodeId;
        private final String port;
        private final String property;
        private final String detail;
        private final String detail2;
        private final String detail3;

        private ClientDiagnostic(String severity, String code, UUID nodeId, String port, String property,
                String detail, String detail2, String detail3) {
            this.severity = severity == null ? "" : severity;
            this.code = code == null ? "" : code;
            this.nodeId = nodeId;
            this.port = port == null ? "" : port;
            this.property = property == null ? "" : property;
            this.detail = detail == null ? "" : detail;
            this.detail2 = detail2 == null ? "" : detail2;
            this.detail3 = detail3 == null ? "" : detail3;
        }
    }

    private static final class ClientRemoteVariable {
        private final String name;
        private final double value;

        private ClientRemoteVariable(String name, double value) {
            this.name = name;
            this.value = value;
        }
    }

    private static final class ClientRemotePort {
        private final String port;
        private final String value;

        private ClientRemotePort(String port, String value) {
            this.port = port;
            this.value = value == null ? "" : value;
        }
    }

    private static final class ClientRemoteTrace {
        private final String text;
        private final UUID nodeId;

        private ClientRemoteTrace(String text, UUID nodeId) {
            this.text = text;
            this.nodeId = nodeId;
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation,
            IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        com.drppp.drtech.client.Textures.DRONE_PROGRAMMER_CASING.render(
                renderState, translation, pipeline);
        com.drppp.drtech.client.Textures.DRONE_PROGRAMMER_OVERLAY.renderSided(
                getFrontFacing(), renderState, translation, pipeline);
    }

    @Override
    protected boolean shouldSerializeInventories() {
        return true;
    }
}
