package com.drppp.drtech.client.drone;

import com.drppp.drtech.drone.api.DroneExtensionRegistry;
import com.drppp.drtech.drone.api.DroneExtensionAvailability;
import com.drppp.drtech.drone.program.compile.DroneDiagnosticSeverity;

import com.cleanroommc.modularui.api.UpOrDown;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.drawable.GuiDraw;
import com.cleanroommc.modularui.screen.RichTooltip;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetThemeEntry;
import com.cleanroommc.modularui.widget.Widget;
import com.drppp.drtech.drone.program.edit.DroneGraphEditCommand;
import com.drppp.drtech.drone.program.edit.DroneGraphAutoLayout;
import com.drppp.drtech.drone.program.edit.DroneEdgePresentation;
import com.drppp.drtech.drone.program.edit.DroneDragPreviewPolicy;
import com.drppp.drtech.drone.program.edit.DroneGroupLayout;
import com.drppp.drtech.drone.program.edit.DronePropertyChoices;
import com.drppp.drtech.drone.program.model.DroneNodeDefinition;
import com.drppp.drtech.drone.program.model.DroneNodePropertyDefinition;
import com.drppp.drtech.drone.program.model.DroneNodePropertyType;
import com.drppp.drtech.drone.filter.DroneFilterMode;
import com.drppp.drtech.drone.filter.DroneItemFilterSpec;
import com.drppp.drtech.drone.filter.DroneBlockFilterSpec;
import com.drppp.drtech.drone.filter.DroneEntityFilterSpec;
import com.drppp.drtech.drone.program.model.DroneArea;
import com.drppp.drtech.drone.program.model.DronePortDefinition;
import com.drppp.drtech.drone.program.model.DronePortDirection;
import com.drppp.drtech.drone.program.model.DronePortType;
import com.drppp.drtech.drone.program.model.DroneProgramEdge;
import com.drppp.drtech.drone.program.model.DroneProgramGraph;
import com.drppp.drtech.drone.program.model.DroneProgramNode;
import com.drppp.drtech.drone.program.registry.DroneNodeRegistry;
import com.drppp.drtech.drone.program.registry.DrTechDroneNodes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityList;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.oredict.OreDictionary;
import org.jetbrains.annotations.NotNull;
import javax.annotation.Nullable;
import org.lwjgl.BufferUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.nio.IntBuffer;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.List;
import java.util.UUID;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.Set;

/** First native graph canvas: grid, typed ports, edge creation/removal, node selection, dragging, pan and zoom. */
public final class DroneProgramCanvasWidget extends Widget<DroneProgramCanvasWidget> implements Interactable {

    private static final int NODE_WIDTH = 96;
    private static final int NODE_HEIGHT = 32;
    private static final int PORT_SIZE = 5;
    private static final int PORT_START_Y = 17;
    private static final int PORT_STEP_Y = 9;
    private static final int MINIMAP_WIDTH = 64;
    private static final int MINIMAP_HEIGHT = 46;
    private static final int CONTEXT_MENU_WIDTH = 94;
    private static final int CONTEXT_MENU_ROW_HEIGHT = 14;
    private static final long DRAG_ACK_TIMEOUT_MS = 3_000L;

    private final Supplier<DroneProgramGraph> graphSupplier;
    private final Consumer<DroneGraphEditCommand> commandSink;
    private final BooleanSupplier editableSupplier;
    private final Supplier<Set<UUID>> diagnosticNodesSupplier;
    private Supplier<Map<UUID, DroneDiagnosticSeverity>> diagnosticSeveritySupplier = Collections::emptyMap;
    private final Supplier<UUID> activeNodeSupplier;
    private final Supplier<BlockPos> dockCoordinateSupplier;
    private final Supplier<BlockPos> droneCoordinateSupplier;
    private final DroneNodeRegistry registry = DroneExtensionRegistry.nodes();
    private final Map<UUID, PreviewPosition> previews = new HashMap<>();

    private UUID selectedNodeId;
    private final Set<UUID> selectedNodeIds = new LinkedHashSet<>();
    private ClipboardGraph clipboard;
    private int clipboardPasteCount;
    private PendingPort pendingPort;
    private UUID draggingNodeId;
    private final Map<UUID, PreviewPosition> dragOrigins = new LinkedHashMap<>();
    private int dragStartMouseX;
    private int dragStartMouseY;
    private boolean marqueeSelecting;
    private boolean marqueeAdditive;
    private int marqueeStartX;
    private int marqueeStartY;
    private int marqueeEndX;
    private int marqueeEndY;
    private boolean panning;
    private int lastMouseX;
    private int lastMouseY;
    private int panX = 8;
    private int panY = 8;
    private int zoomPercent = 100;
    private int selectedAreaCorner = 1;
    private int selectedPropertyIndex;
    private int selectedBlockStatePropertyIndex;
    private int selectedEntityAdvancedField;
    private UUID propertyDraftNodeId;
    private String propertyDraftId = "";
    private String propertyDraft = "";
    private boolean propertyDraftDirty;
    private UUID lastPropertyChangeNodeId;
    private String lastPropertyChangeId = "";
    private String lastPropertyBefore = "";
    private String lastPropertyAfter = "";
    private long areaPreviewRevision = Long.MIN_VALUE;
    private UUID areaPreviewNodeId;
    private DroneArea cachedAreaPreview;
    private String areaPreviewStatusKey = "drtech.drone.programmer.area_preview_missing";
    private long areaPreviewNextRefreshNanos;
    static final long AREA_PREVIEW_REFRESH_NANOS = 100_000_000L;
    private String connectionHint = "";
    private long connectionHintUntil;
    private int connectionHintColor = 0xFFFFA0A0;
    private ContextMenu contextMenu;

    public DroneProgramCanvasWidget(Supplier<DroneProgramGraph> graphSupplier,
            Consumer<DroneGraphEditCommand> commandSink, BooleanSupplier editableSupplier,
            Supplier<Set<UUID>> diagnosticNodesSupplier, Supplier<UUID> activeNodeSupplier) {
        this(graphSupplier, commandSink, editableSupplier, diagnosticNodesSupplier, activeNodeSupplier,
                () -> null, () -> null);
    }

    public DroneProgramCanvasWidget(Supplier<DroneProgramGraph> graphSupplier,
            Consumer<DroneGraphEditCommand> commandSink, BooleanSupplier editableSupplier,
            Supplier<Set<UUID>> diagnosticNodesSupplier, Supplier<UUID> activeNodeSupplier,
            Supplier<BlockPos> dockCoordinateSupplier, Supplier<BlockPos> droneCoordinateSupplier) {
        this.graphSupplier = graphSupplier;
        this.commandSink = commandSink;
        this.editableSupplier = editableSupplier;
        this.diagnosticNodesSupplier = diagnosticNodesSupplier;
        this.activeNodeSupplier = activeNodeSupplier;
        this.dockCoordinateSupplier = dockCoordinateSupplier;
        this.droneCoordinateSupplier = droneCoordinateSupplier;
        background();
    }

    public DroneProgramCanvasWidget withDiagnosticSeverities(
            Supplier<Map<UUID, DroneDiagnosticSeverity>> supplier) {
        diagnosticSeveritySupplier = supplier == null ? Collections::emptyMap : supplier;
        return this;
    }

    @Override
    public void draw(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
        CanvasScissor scissor = CanvasScissor.begin(context, getArea().w(), getArea().h());
        try {
            drawClippedCanvas(context);
        } finally {
            scissor.close();
        }
    }

    /** All graph primitives stay inside the canvas; foreground tooltips are deliberately drawn afterwards. */
    private void drawClippedCanvas(ModularGuiContext context) {
        int width = getArea().w();
        int height = getArea().h();
        GuiDraw.drawRect(0, 0, width, height, 0xFF151A21);
        drawGrid(width, height);
        DroneProgramGraph graph = graphSupplier.get();
        if (graph == null) {
            GuiDraw.drawText(I18n.format("drtech.drone.programmer.insert_card"), 10, 10, 1.0F, 0xFFB9C2CF, false);
            return;
        }
        reconcileAcknowledgedDragPreviews(graph);
        boolean fastDrag = draggingNodeId != null;
        UUID activeNodeId = activeNodeSupplier.get();
        Set<UUID> diagnosticNodes = diagnosticNodesSupplier.get();
        Map<UUID, DroneDiagnosticSeverity> diagnosticSeverities = diagnosticSeveritySupplier.get();
        PortConnectivity connectivity = fastDrag ? PortConnectivity.EMPTY : PortConnectivity.index(graph);
        drawGroupFrames(graph);
        Set<UUID> hiddenNodes = DroneGroupLayout.hiddenByCollapsedGroups(graph);
        PortHit hoveredPort = fastDrag ? null : findPort(graph, hiddenNodes,
                context.getMouseX(), context.getMouseY());
        drawEdges(graph, hiddenNodes, hoveredPort, fastDrag);
        if (pendingPort != null) {
            Point from = portPoint(graph.getNode(pendingPort.nodeId), pendingPort.portId, DronePortDirection.OUTPUT);
            if (from != null) {
                drawOrthogonalLine(from.x, from.y, context.getMouseX(), context.getMouseY(),
                        colorForType(pendingPort.type));
            }
        }
        for (DroneProgramNode node : graph.getNodes()) {
            if (DroneGroupLayout.isGroup(node) || hiddenNodes.contains(node.getId())) continue;
            if (!isNodeVisible(node)) continue;
            drawNode(node, hoveredPort, activeNodeId, diagnosticNodes,
                    diagnosticSeverities, connectivity, fastDrag);
        }
        drawMarquee();
        drawMiniMap(graph, width, hiddenNodes, activeNodeId);
        drawConnectionGuide(width, height);
        GuiDraw.drawText(zoomPercent + "%", width - 30, height - 10, 0.7F, 0xFF8291A5, false);
        drawContextMenu(context.getMouseX(), context.getMouseY());
    }

    /** Draw focused port connections last so crossing lines cannot hide the route being inspected. */
    private void drawEdges(DroneProgramGraph graph, Set<UUID> hiddenNodes, PortHit hoveredPort,
            boolean fastDrag) {
        List<DroneProgramEdge> ordered = DroneEdgePresentation.dataThenFlow(graph, registry);
        if (hoveredPort == null) {
            for (DroneProgramEdge edge : ordered) drawVisibleEdge(graph, hiddenNodes, edge, null, fastDrag);
            return;
        }
        for (DroneProgramEdge edge : ordered) {
            if (!touchesHoveredPort(edge, hoveredPort)) {
                drawVisibleEdge(graph, hiddenNodes, edge, hoveredPort, fastDrag);
            }
        }
        for (DroneProgramEdge edge : ordered) {
            if (touchesHoveredPort(edge, hoveredPort)) {
                drawVisibleEdge(graph, hiddenNodes, edge, hoveredPort, fastDrag);
            }
        }
    }

    private void drawVisibleEdge(DroneProgramGraph graph, Set<UUID> hiddenNodes, DroneProgramEdge edge,
            PortHit hoveredPort, boolean fastDrag) {
        if (!hiddenNodes.contains(edge.getSourceNodeId()) && !hiddenNodes.contains(edge.getTargetNodeId())) {
            drawEdge(graph, edge, hoveredPort, fastDrag);
        }
    }

    private static boolean touchesHoveredPort(DroneProgramEdge edge, PortHit hoveredPort) {
        return hoveredPort != null && DroneEdgePresentation.touchesPort(edge, hoveredPort.node.getId(),
                hoveredPort.port.getId());
    }

    /** Preserves and intersects an existing OpenGL scissor so this widget is safe inside other clipped views. */
    private static final class CanvasScissor implements AutoCloseable {

        private final boolean previouslyEnabled;
        private final int previousX;
        private final int previousY;
        private final int previousWidth;
        private final int previousHeight;

        private CanvasScissor(boolean previouslyEnabled, int previousX, int previousY,
                int previousWidth, int previousHeight) {
            this.previouslyEnabled = previouslyEnabled;
            this.previousX = previousX;
            this.previousY = previousY;
            this.previousWidth = previousWidth;
            this.previousHeight = previousHeight;
        }

        private static CanvasScissor begin(ModularGuiContext context, int width, int height) {
            Minecraft minecraft = Minecraft.getMinecraft();
            ScaledResolution resolution = new ScaledResolution(minecraft);
            int scale = resolution.getScaleFactor();
            int left = Math.min(context.transformX(0, 0), context.transformX(width, height)) * scale;
            int right = Math.max(context.transformX(0, 0), context.transformX(width, height)) * scale;
            int top = Math.min(context.transformY(0, 0), context.transformY(width, height)) * scale;
            int bottom = Math.max(context.transformY(0, 0), context.transformY(width, height)) * scale;
            int x = left;
            int y = minecraft.displayHeight - bottom;
            int scissorWidth = Math.max(0, right - left);
            int scissorHeight = Math.max(0, bottom - top);

            boolean enabled = GL11.glIsEnabled(GL11.GL_SCISSOR_TEST);
            int oldX = 0;
            int oldY = 0;
            int oldWidth = 0;
            int oldHeight = 0;
            if (enabled) {
                IntBuffer box = BufferUtils.createIntBuffer(4);
                GL11.glGetInteger(GL11.GL_SCISSOR_BOX, box);
                oldX = box.get(0);
                oldY = box.get(1);
                oldWidth = box.get(2);
                oldHeight = box.get(3);
                int clippedRight = Math.min(x + scissorWidth, oldX + oldWidth);
                int clippedTop = Math.min(y + scissorHeight, oldY + oldHeight);
                x = Math.max(x, oldX);
                y = Math.max(y, oldY);
                scissorWidth = Math.max(0, clippedRight - x);
                scissorHeight = Math.max(0, clippedTop - y);
            } else {
                GL11.glEnable(GL11.GL_SCISSOR_TEST);
            }
            GL11.glScissor(x, y, scissorWidth, scissorHeight);
            return new CanvasScissor(enabled, oldX, oldY, oldWidth, oldHeight);
        }

        @Override
        public void close() {
            if (previouslyEnabled) {
                GL11.glScissor(previousX, previousY, previousWidth, previousHeight);
            } else {
                GL11.glDisable(GL11.GL_SCISSOR_TEST);
            }
        }
    }

    private void drawGrid(int width, int height) {
        int spacing = Math.max(8, 16 * zoomPercent / 100);
        int startX = floorMod(scale(panX), spacing);
        int startY = floorMod(scale(panY), spacing);
        for (int x = startX; x < width; x += spacing) GuiDraw.drawRect(x, 0, 1, height, 0xFF202833);
        for (int y = startY; y < height; y += spacing) GuiDraw.drawRect(0, y, width, 1, 0xFF202833);
    }

    private void drawGroupFrames(DroneProgramGraph graph) {
        for (DroneProgramNode group : graph.getNodes()) {
            if (!DroneGroupLayout.isGroup(group)) continue;
            if (!isNodeVisible(group)) continue;
            Point point = nodePoint(group);
            boolean collapsed = DroneGroupLayout.isCollapsed(group);
            int width = scaled(DroneGroupLayout.width(group));
            int height = scaled(collapsed ? DroneGroupLayout.HEADER_HEIGHT : DroneGroupLayout.height(group));
            int color = groupColor(group.getConfiguration().getString("Color"));
            int fill = (collapsed ? 0x66000000 : 0x24000000) | (color & 0x00FFFFFF);
            GuiDraw.drawRect(point.x, point.y, width, Math.max(scaled(DroneGroupLayout.HEADER_HEIGHT), height), fill);
            GuiDraw.drawRect(point.x, point.y, width, scaled(DroneGroupLayout.HEADER_HEIGHT),
                    0x88000000 | (color & 0x00FFFFFF));
            int border = selectedNodeIds.contains(group.getId()) ? 0xFFB8DEFF : color;
            GuiDraw.drawBorderInsideXYWH(point.x, point.y, width,
                    Math.max(scaled(DroneGroupLayout.HEADER_HEIGHT), height), border);
            String title = group.getConfiguration().getString("Title").trim();
            if (title.isEmpty()) title = I18n.format("drtech.drone.group.default_title");
            GuiDraw.drawText((collapsed ? "+ " : "- ") + title, point.x + scaled(4), point.y + scaled(3),
                    Math.max(0.5F, zoomPercent / 150.0F), 0xFFF4F7FA, false);
        }
    }

    private static int groupColor(String color) {
        if ("GREEN".equals(color)) return 0xFF55B887;
        if ("ORANGE".equals(color)) return 0xFFE39A52;
        if ("PURPLE".equals(color)) return 0xFFA37DDA;
        if ("RED".equals(color)) return 0xFFD96868;
        if ("GRAY".equals(color)) return 0xFF8291A5;
        return 0xFF4F9EDB;
    }

    private void drawContextMenu(int mouseX, int mouseY) {
        if (contextMenu == null) return;
        int height = contextMenu.actions.size() * CONTEXT_MENU_ROW_HEIGHT + 2;
        GuiDraw.drawRect(contextMenu.x, contextMenu.y, CONTEXT_MENU_WIDTH, height, 0xF01B222C);
        GuiDraw.drawBorderInsideXYWH(contextMenu.x, contextMenu.y, CONTEXT_MENU_WIDTH, height, 0xFF6F8195);
        for (int index = 0; index < contextMenu.actions.size(); index++) {
            MenuAction action = contextMenu.actions.get(index);
            int y = contextMenu.y + 1 + index * CONTEXT_MENU_ROW_HEIGHT;
            boolean hovered = mouseX >= contextMenu.x && mouseX < contextMenu.x + CONTEXT_MENU_WIDTH
                    && mouseY >= y && mouseY < y + CONTEXT_MENU_ROW_HEIGHT;
            if (hovered && isMenuActionEnabled(action)) {
                GuiDraw.drawRect(contextMenu.x + 1, y, CONTEXT_MENU_WIDTH - 2, CONTEXT_MENU_ROW_HEIGHT, 0xFF36516D);
            }
            int color = isMenuActionEnabled(action) ? 0xFFE7EDF5 : 0xFF6F7883;
            GuiDraw.drawText(I18n.format(action.translationKey), contextMenu.x + 5, y + 3, 0.65F, color, false);
        }
    }

    private void openContextMenu(int mouseX, int mouseY, boolean onNode) {
        List<MenuAction> actions = new ArrayList<>();
        if (onNode) {
            Collections.addAll(actions, MenuAction.COPY, MenuAction.DUPLICATE, MenuAction.DELETE,
                    MenuAction.DISCONNECT, MenuAction.FOCUS, MenuAction.ALIGN_HORIZONTAL,
                    MenuAction.ALIGN_VERTICAL, MenuAction.AUTO_LAYOUT, MenuAction.GROUP_SELECTION,
                    MenuAction.ADD_COMMENT);
        } else {
            Collections.addAll(actions, MenuAction.PASTE, MenuAction.ADD_COMMENT, MenuAction.GROUP_SELECTION,
                    MenuAction.AUTO_LAYOUT, MenuAction.FIT_ALL);
        }
        int menuHeight = actions.size() * CONTEXT_MENU_ROW_HEIGHT + 2;
        int x = Math.max(0, Math.min(mouseX, getArea().w() - CONTEXT_MENU_WIDTH));
        int y = Math.max(0, Math.min(mouseY, getArea().h() - menuHeight));
        contextMenu = new ContextMenu(x, y, unscale(mouseX) - panX, unscale(mouseY) - panY, actions);
    }

    private boolean handleContextMenuClick(int mouseX, int mouseY) {
        if (contextMenu == null || mouseX < contextMenu.x || mouseX >= contextMenu.x + CONTEXT_MENU_WIDTH
                || mouseY < contextMenu.y || mouseY >= contextMenu.y + contextMenu.actions.size()
                        * CONTEXT_MENU_ROW_HEIGHT + 2) return false;
        int index = (mouseY - contextMenu.y - 1) / CONTEXT_MENU_ROW_HEIGHT;
        if (index < 0 || index >= contextMenu.actions.size()) return true;
        MenuAction action = contextMenu.actions.get(index);
        int graphX = contextMenu.graphX;
        int graphY = contextMenu.graphY;
        contextMenu = null;
        if (isMenuActionEnabled(action)) executeMenuAction(action, graphX, graphY);
        return true;
    }

    private boolean isMenuActionEnabled(MenuAction action) {
        boolean editable = editableSupplier.getAsBoolean();
        switch (action) {
            case COPY:
            case FOCUS:
            case FIT_ALL:
                return action == MenuAction.FIT_ALL || !selectedNodeIds.isEmpty();
            case PASTE:
                return editable && clipboard != null;
            case ALIGN_HORIZONTAL:
            case ALIGN_VERTICAL:
                return editable && selectedNodeIds.size() >= 2;
            case DUPLICATE:
            case DELETE:
            case DISCONNECT:
                return editable && !selectedNodeIds.isEmpty();
            case GROUP_SELECTION:
            case ADD_COMMENT:
            case AUTO_LAYOUT:
                return editable;
            default:
                return false;
        }
    }

    private void executeMenuAction(MenuAction action, int graphX, int graphY) {
        switch (action) {
            case COPY: copySelected(); break;
            case PASTE: pasteCopiedNodeAt(graphX, graphY); break;
            case DUPLICATE: copySelected(); pasteCopiedNode(); break;
            case DELETE: deleteSelected(); break;
            case DISCONNECT: disconnectSelected(); break;
            case FOCUS: fitSelectionOrAll(); break;
            case ALIGN_HORIZONTAL: alignSelectedHorizontal(); break;
            case ALIGN_VERTICAL: alignSelectedVertical(); break;
            case AUTO_LAYOUT: autoLayoutSelectedOrAll(); break;
            case GROUP_SELECTION: createGroupForSelection(graphX, graphY); break;
            case ADD_COMMENT: addCommentAt(graphX, graphY); break;
            case FIT_ALL: clearSelection(); fitAll(); break;
        }
    }

    private void drawEdge(DroneProgramGraph graph, DroneProgramEdge edge, PortHit hoveredPort,
            boolean fastDrag) {
        DroneProgramNode source = graph.getNode(edge.getSourceNodeId());
        DroneProgramNode target = graph.getNode(edge.getTargetNodeId());
        Point from = portPoint(source, edge.getSourcePortId(), DronePortDirection.OUTPUT);
        Point to = portPoint(target, edge.getTargetPortId(), DronePortDirection.INPUT);
        if (from == null || to == null) return;
        DroneNodeDefinition definition = source == null ? null : registry.get(source.getType());
        DronePortDefinition port = definition == null ? null : definition.getPort(edge.getSourcePortId());
        DronePortType type = port == null ? DronePortType.ANY_DATA : port.getType();
        boolean flow = type == DronePortType.FLOW;
        boolean portFocused = touchesHoveredPort(edge, hoveredPort);
        boolean related = hoveredPort == null
                ? DroneEdgePresentation.touchesSelection(edge, selectedNodeIds) : portFocused;
        int color = colorForType(type);
        if (!related) color = dimColor(color);
        drawRoutedEdge(from, to, edge.getId(), color, flow, portFocused);
        if (!fastDrag && flow && related && zoomPercent >= 75 && shouldLabelFlow(edge.getSourcePortId())) {
            drawEdgeLabel(from, to, portLabel(edge.getSourcePortId()), color);
        }
        if (!fastDrag && portFocused && zoomPercent >= 65) drawConnectionEndpointLabels(edge, from, to, color);
    }

    private void drawRoutedEdge(Point from, Point to, UUID edgeId, int color, boolean flow, boolean focused) {
        int direction = to.x >= from.x ? 1 : -1;
        int stub = Math.max(5, scaled(8));
        int thickness = (zoomPercent < 80 ? 1 : 2) + (flow && zoomPercent >= 80 ? 1 : 0)
                + (focused ? 1 : 0);
        int laneOffset = scaled(DroneEdgePresentation.laneOffset(edgeId));
        if (to.x - from.x > stub * 2) {
            int middle = from.x + (to.x - from.x) / 2 + laneOffset;
            middle = Math.max(from.x + stub, Math.min(to.x - stub, middle));
            drawSegment(from.x, from.y, middle, from.y, thickness, color);
            drawSegment(middle, from.y, middle, to.y, thickness, color);
            drawSegment(middle, to.y, to.x, to.y, thickness, color);
        } else {
            // Backward connections leave the output to the right, route around the nodes, then enter from the left.
            int sourceLane = from.x + stub + Math.abs(laneOffset);
            int targetLane = to.x - stub - Math.abs(laneOffset);
            int detour = Math.min(from.y, to.y) - Math.max(scaled(16), Math.abs(laneOffset) + scaled(10));
            drawSegment(from.x, from.y, sourceLane, from.y, thickness, color);
            drawSegment(sourceLane, from.y, sourceLane, detour, thickness, color);
            drawSegment(sourceLane, detour, targetLane, detour, thickness, color);
            drawSegment(targetLane, detour, targetLane, to.y, thickness, color);
            drawSegment(targetLane, to.y, to.x, to.y, thickness, color);
            direction = 1;
        }
        drawEndpointMarkers(from, to, direction, color, focused);
    }

    private static void drawSegment(int x1, int y1, int x2, int y2, int thickness, int color) {
        if (x1 == x2) {
            GuiDraw.drawRect(x1, Math.min(y1, y2), thickness, Math.max(1, Math.abs(y2 - y1)), color);
        } else {
            GuiDraw.drawRect(Math.min(x1, x2), y1, Math.max(1, Math.abs(x2 - x1)), thickness, color);
        }
    }

    private void drawEndpointMarkers(Point from, Point to, int targetDirection, int color, boolean focused) {
        int marker = Math.max(2, scaled(focused ? 5 : 3));
        GuiDraw.drawBorderInsideXYWH(from.x - marker / 2, from.y - marker / 2, marker, marker, color);
        int arrow = Math.max(2, scaled(focused ? 5 : 4));
        int baseX = to.x - targetDirection * arrow;
        GuiDraw.drawRect(baseX, to.y - arrow / 2, Math.max(1, arrow), 1, color);
        GuiDraw.drawRect(to.x - targetDirection * 2, to.y - 1, Math.max(1, targetDirection * 2), 3, color);
    }

    private void drawConnectionEndpointLabels(DroneProgramEdge edge, Point from, Point to, int color) {
        float scale = Math.max(0.55F, zoomPercent / 170.0F);
        String output = I18n.format("drtech.drone.connection.output", portLabel(edge.getSourcePortId()));
        String input = I18n.format("drtech.drone.connection.input", portLabel(edge.getTargetPortId()));
        drawEndpointLabel(output, from.x - 3, from.y - 10, scale, color, true);
        drawEndpointLabel(input, to.x + 3, to.y - 10, scale, color, false);
    }

    private static void drawEndpointLabel(String text, int anchorX, int y, float scale, int color, boolean rightAlign) {
        int width = Math.round(Minecraft.getMinecraft().fontRenderer.getStringWidth(text) * scale);
        int x = rightAlign ? anchorX - width : anchorX;
        GuiDraw.drawRect(x - 2, y - 1, width + 4, Math.max(7, Math.round(9 * scale)), 0xE0151A21);
        GuiDraw.drawText(text, x, y, scale, color, false);
    }

    private void drawOrthogonalLine(int x1, int y1, int x2, int y2, int color) {
        drawOrthogonalLine(x1, y1, x2, y2, color, false);
    }

    private void drawOrthogonalLine(int x1, int y1, int x2, int y2, int color, boolean flow) {
        int middle = x1 + (x2 - x1) / 2;
        int thickness = (zoomPercent < 80 ? 1 : 2) + (flow && zoomPercent >= 80 ? 1 : 0);
        GuiDraw.drawRect(Math.min(x1, middle), y1, Math.max(1, Math.abs(middle - x1)), thickness, color);
        GuiDraw.drawRect(middle, Math.min(y1, y2), thickness, Math.max(1, Math.abs(y2 - y1)), color);
        GuiDraw.drawRect(Math.min(middle, x2), y2, Math.max(1, Math.abs(x2 - middle)), thickness, color);
    }

    private void drawEdgeLabel(Point from, Point to, String label, int color) {
        float textScale = Math.max(0.5F, zoomPercent / 175.0F);
        int textWidth = Math.round(Minecraft.getMinecraft().fontRenderer.getStringWidth(label) * textScale);
        int middleX = from.x + (to.x - from.x) / 2;
        int middleY = from.y + (to.y - from.y) / 2;
        int x = middleX - textWidth / 2;
        int y = middleY - 5;
        GuiDraw.drawRect(x - 2, y - 1, textWidth + 4, Math.max(7, Math.round(9 * textScale)), 0xE0151A21);
        GuiDraw.drawText(label, x, y, textScale, color, false);
    }

    private static boolean shouldLabelFlow(String portId) {
        return !"next".equals(portId);
    }

    private static int dimColor(int color) {
        int red = ((color >> 16) & 0xFF) * 35 / 100;
        int green = ((color >> 8) & 0xFF) * 35 / 100;
        int blue = (color & 0xFF) * 35 / 100;
        return 0xFF000000 | red << 16 | green << 8 | blue;
    }

    private void drawNode(DroneProgramNode node, PortHit hoveredPort, UUID activeNodeId,
            Set<UUID> diagnosticNodes, Map<UUID, DroneDiagnosticSeverity> diagnosticSeverities,
            PortConnectivity connectivity, boolean fastDrag) {
        Point point = nodePoint(node);
        DroneNodeDefinition definition = registry.get(node.getType());
        DroneExtensionAvailability extension = DroneExtensionRegistry.availabilityForNode(node.getType());
        boolean unavailableExtension = extension != null && extension.isPlaceholder();
        int nodeHeight = nodeHeight(definition);
        boolean active = node.getId().equals(activeNodeId);
        boolean selected = selectedNodeIds.contains(node.getId());
        int background = unavailableExtension ? 0xFF3A3030 : selected ? 0xFF314760
                : active ? 0xFF25493F : 0xFF26313E;
        GuiDraw.drawRect(point.x, point.y, scaled(NODE_WIDTH), scaled(nodeHeight), background);
        GuiDraw.drawRect(point.x, point.y, scaled(NODE_WIDTH), scaled(11), categoryColor(definition));
        int border = unavailableExtension ? 0xFFFFB24D
                : node.getId().equals(selectedNodeId) ? 0xFF8DCAFF
                : selected ? 0xFF4F9EDB
                : diagnosticNodes.contains(node.getId()) ? 0xFFFF5A5A
                : active ? 0xFF52E39E : 0xFF526274;
        GuiDraw.drawBorderInsideXYWH(point.x, point.y, scaled(NODE_WIDTH), scaled(nodeHeight), border);
        DroneDiagnosticSeverity severity = diagnosticSeverities.get(node.getId());
        if (severity != null) {
            drawDiagnosticIcon(point.x + scaled(NODE_WIDTH - 8), point.y + scaled(1), severity);
        } else if (node.getConfiguration().getBoolean("Breakpoint")) {
            drawBreakpointIcon(point.x + scaled(NODE_WIDTH - 8), point.y + scaled(1), false);
        } else if (node.getConfiguration().getBoolean("BreakpointOnFailure")
                || node.getConfiguration().getInteger("BreakpointLowEnergy") > 0
                || node.getConfiguration().getBoolean("BreakpointOnVariableWrite")) {
            // Amber means the node has a conditional, post-action or low-energy breakpoint rather than a stop on
            // entry. This stays visible even at the smallest useful zoom.
            drawBreakpointIcon(point.x + scaled(NODE_WIDTH - 8), point.y + scaled(1), true);
        }
        // Every built-in node has a dedicated texture; category glyph remains the extension-node fallback.
        GuiDraw.drawRect(point.x + scaled(1), point.y + scaled(1), scaled(10), scaled(9), 0x880C121A);
        if (!unavailableExtension && definition != null && "drtech".equals(node.getType().getNamespace())) {
            DroneNodeIconTextures.draw(node.getType(), point.x + scaled(2), point.y + scaled(2), scaled(7));
        } else {
            GuiDraw.drawText(categorySymbol(definition), point.x + scaled(4), point.y + scaled(3),
                    Math.max(0.45F, zoomPercent / 180.0F), 0xFFF4F7FA, false);
        }
        String customLabel = node.getConfiguration().getString("Label");
        String title = unavailableExtension ? I18n.format(extension.getDisplayKey())
                : !customLabel.isEmpty() ? customLabel : definition == null
                ? I18n.format("drtech.drone.canvas.missing_node", node.getType())
                : I18n.format("drtech.drone.node." + node.getType().getPath());
        GuiDraw.drawText(title, point.x + scaled(11), point.y + scaled(2), Math.max(0.5F, zoomPercent / 150.0F),
                0xFFE7EDF5, false);
        if (definition == null) return;
        if (node.getType().equals(DrTechDroneNodes.COMMENT)) {
            drawCommentBody(node, point);
        }
        for (DronePortDefinition port : definition.getPorts()) {
            Point portPoint = portPoint(node, port.getId(), port.getDirection());
            if (portPoint == null) continue;
            int size = Math.max(3, scaled(PORT_SIZE));
            boolean hovered = hoveredPort != null && hoveredPort.node.getId().equals(node.getId())
                    && hoveredPort.port.getId().equals(port.getId())
                    && hoveredPort.port.getDirection() == port.getDirection();
            boolean compatible = pendingPort == null || port.getDirection() == DronePortDirection.OUTPUT
                    || port.getType().accepts(pendingPort.type);
            boolean missing = !fastDrag && port.isRequired() && !connectivity.isConnected(node.getId(), port);
            int outline = hovered ? (compatible ? 0xFFFFFF66 : 0xFFFF4D5A)
                    : missing ? 0xFFFF4D5A : 0;
            if (outline != 0) {
                int outlineSize = size + Math.max(2, scaled(3));
                GuiDraw.drawRect(portPoint.x - outlineSize / 2, portPoint.y - outlineSize / 2,
                        outlineSize, outlineSize, outline);
            }
            drawPortShape(portPoint, port, size, colorForType(port.getType()));
            if (!fastDrag) drawPortLabel(point, portPoint, port);
        }
    }

    private void drawCommentBody(DroneProgramNode node, Point point) {
        String text = node.getConfiguration().getString("Text").replace('\n', ' ').replace('\r', ' ').trim();
        if (text.isEmpty()) text = I18n.format("drtech.drone.comment.empty");
        String first = text.length() <= 30 ? text : text.substring(0, 30);
        String second = text.length() <= 30 ? "" : text.substring(30, Math.min(60, text.length()));
        if (text.length() > 60) second += "…";
        float textScale = Math.max(0.45F, zoomPercent / 165.0F);
        GuiDraw.drawText(first, point.x + scaled(4), point.y + scaled(16), textScale, 0xFFF3E6A1, false);
        if (!second.isEmpty()) {
            GuiDraw.drawText(second, point.x + scaled(4), point.y + scaled(29), textScale, 0xFFD8CC91, false);
        }
    }

    private void drawPortShape(Point point, DronePortDefinition port, int size, int color) {
        DronePortType type = port.getType();
        int left = point.x - size / 2;
        int top = point.y - size / 2;
        int centerX = point.x;
        int centerY = point.y;
        int line = Math.max(1, size / 4);
        switch (type) {
            case FLOW:
                int direction = port.getDirection() == DronePortDirection.INPUT ? -1 : 1;
                int shaftX = direction > 0 ? left : centerX;
                GuiDraw.drawRect(shaftX, centerY - line / 2, Math.max(2, size / 2 + 1), line, color);
                for (int row = 0; row < size; row++) {
                    int distance = Math.abs(row - size / 2);
                    int width = Math.max(1, size / 2 + 1 - distance);
                    int arrowX = direction > 0 ? centerX : centerX - width + 1;
                    GuiDraw.drawRect(arrowX, top + row, width, 1, color);
                }
                break;
            case BOOLEAN:
                for (int row = 0; row < size; row++) {
                    int distance = Math.abs(row - size / 2);
                    int width = Math.max(1, size - distance * 2);
                    GuiDraw.drawRect(centerX - width / 2, top + row, width, 1, color);
                }
                break;
            case NUMBER:
                for (int row = 0; row < size; row++) {
                    int inset = row == 0 || row == size - 1 ? Math.max(1, line) : 0;
                    GuiDraw.drawRect(left + inset, top + row, Math.max(1, size - inset * 2), 1, color);
                }
                break;
            case COORDINATE:
                GuiDraw.drawRect(left, centerY - line / 2, size, line, color);
                GuiDraw.drawRect(centerX - line / 2, top, line, size, color);
                GuiDraw.drawRect(centerX - line, centerY - line, line * 2, line * 2, 0xFF151A21);
                break;
            case AREA:
                GuiDraw.drawBorderInsideXYWH(left, top, size, size, color);
                break;
            default:
                GuiDraw.drawRect(left, top, size, size, color);
        }
        if (port.allowsMultipleConnections()) {
            GuiDraw.drawBorderInsideXYWH(left - 1, top - 1, size + 2, size + 2, 0xFFF4F7FA);
        }
    }

    private void drawDiagnosticIcon(int x, int y, DroneDiagnosticSeverity severity) {
        int size = Math.max(5, scaled(6));
        int color = severity == DroneDiagnosticSeverity.ERROR ? 0xFFFF4D5A
                : severity == DroneDiagnosticSeverity.WARNING ? 0xFFFFC34D : 0xFF61B7FF;
        GuiDraw.drawRect(x, y, size, size, 0xFF151A21);
        GuiDraw.drawBorderInsideXYWH(x, y, size, size, color);
        if (severity == DroneDiagnosticSeverity.ERROR) {
            for (int index = 1; index < size - 1; index++) {
                GuiDraw.drawRect(x + index, y + index, 1, 1, color);
                GuiDraw.drawRect(x + size - 1 - index, y + index, 1, 1, color);
            }
        } else {
            int center = x + size / 2;
            GuiDraw.drawRect(center, y + 1, 1, Math.max(1, size - 3), color);
            GuiDraw.drawRect(center, y + size - 2, 1, 1, color);
        }
    }

    private void drawBreakpointIcon(int x, int y, boolean conditional) {
        int size = Math.max(5, scaled(6));
        int color = conditional ? 0xFFFFC34D : 0xFFFF4D5A;
        GuiDraw.drawRect(x, y, size, size, 0xFF151A21);
        GuiDraw.drawBorderInsideXYWH(x, y, size, size, color);
        int inset = Math.max(1, size / 3);
        GuiDraw.drawRect(x + inset, y + inset, Math.max(1, size - inset * 2),
                Math.max(1, size - inset * 2), color);
    }

    private void drawPortLabel(Point nodePoint, Point socket, DronePortDefinition port) {
        String label = portLabel(port.getId());
        float textScale = Math.max(0.45F, zoomPercent / 165.0F);
        int y = socket.y - Math.max(2, scaled(3));
        int x;
        if (port.getDirection() == DronePortDirection.INPUT) {
            x = nodePoint.x + scaled(5);
        } else {
            int width = Math.round(Minecraft.getMinecraft().fontRenderer.getStringWidth(label) * textScale);
            x = nodePoint.x + scaled(NODE_WIDTH - 5) - width;
        }
        GuiDraw.drawText(label, x, y, textScale, colorForType(port.getType()), false);
    }

    private void drawConnectionGuide(int width, int height) {
        String text;
        int color;
        if (!connectionHint.isEmpty() && System.currentTimeMillis() < connectionHintUntil) {
            text = connectionHint;
            color = connectionHintColor;
        } else if (pendingPort != null) {
            text = I18n.format("drtech.drone.canvas.connection.selected", portLabel(pendingPort.portId));
            color = 0xFFFFFF80;
        } else if (selectedNodeIds.size() > 1) {
            text = I18n.format("drtech.drone.canvas.selection.count", selectedNodeIds.size());
            color = 0xFF8DCAFF;
        } else {
            text = I18n.format("drtech.drone.canvas.connection.guide");
            color = 0xFFAEBBC9;
        }
        GuiDraw.drawRect(2, height - 14, width - 38, 12, 0xB0192029);
        GuiDraw.drawText(text, 5, height - 11, 0.55F, color, false);
    }

    private void drawMarquee() {
        if (!marqueeSelecting) return;
        int left = Math.min(marqueeStartX, marqueeEndX);
        int top = Math.min(marqueeStartY, marqueeEndY);
        int width = Math.max(1, Math.abs(marqueeEndX - marqueeStartX));
        int height = Math.max(1, Math.abs(marqueeEndY - marqueeStartY));
        GuiDraw.drawRect(left, top, width, height, 0x384C91D7);
        GuiDraw.drawBorderInsideXYWH(left, top, width, height, 0xFF68B7FF);
    }

    private void drawMiniMap(DroneProgramGraph graph, int canvasWidth, Set<UUID> hiddenNodes, UUID activeNodeId) {
        MiniMapProjection projection = miniMapProjection(graph, canvasWidth);
        if (projection == null) return;
        GuiDraw.drawRect(projection.left, projection.top, MINIMAP_WIDTH, MINIMAP_HEIGHT, 0xD0192029);
        GuiDraw.drawBorderInsideXYWH(projection.left, projection.top, MINIMAP_WIDTH, MINIMAP_HEIGHT, 0xFF526274);
        for (DroneProgramNode node : graph.getNodes()) {
            if (hiddenNodes.contains(node.getId())) continue;
            int x = projection.projectX(node.getX());
            int y = projection.projectY(node.getY());
            int color = node.getId().equals(selectedNodeId) ? 0xFF8DCAFF
                    : selectedNodeIds.contains(node.getId()) ? 0xFF4F9EDB
                    : node.getId().equals(activeNodeId) ? 0xFF52E39E : 0xFF8392A5;
            int nodeWidth = DroneGroupLayout.isGroup(node) ? DroneGroupLayout.width(node) : NODE_WIDTH;
            int nodeHeight = DroneGroupLayout.isGroup(node)
                    ? (DroneGroupLayout.isCollapsed(node) ? DroneGroupLayout.HEADER_HEIGHT : DroneGroupLayout.height(node))
                    : nodeHeight(registry.get(node.getType()));
            GuiDraw.drawRect(x, y, Math.max(2, projection.projectLength(nodeWidth)),
                    Math.max(2, projection.projectLength(nodeHeight)), color);
        }
        // Current viewport rectangle gives orientation while panning a large program.
        int rawLeft = projection.projectX(-panX);
        int rawTop = projection.projectY(-panY);
        int rawRight = rawLeft + Math.max(2, projection.projectLength(unscale(getArea().w())));
        int rawBottom = rawTop + Math.max(2, projection.projectLength(unscale(getArea().h())));
        int viewLeft = Math.max(projection.left + 1, rawLeft);
        int viewTop = Math.max(projection.top + 1, rawTop);
        int viewRight = Math.min(projection.left + MINIMAP_WIDTH - 1, rawRight);
        int viewBottom = Math.min(projection.top + MINIMAP_HEIGHT - 1, rawBottom);
        if (viewRight > viewLeft && viewBottom > viewTop) {
            GuiDraw.drawBorderInsideXYWH(viewLeft, viewTop, viewRight - viewLeft, viewBottom - viewTop, 0xFFFFFFFF);
        }
    }

    @Override
    public void drawForeground(ModularGuiContext context) {
        super.drawForeground(context);
        if (!isBelowMouseFor(0)) return;
        if (draggingNodeId != null) return;
        if (contextMenu != null) return;
        DroneProgramGraph graph = graphSupplier.get();
        if (graph == null) return;
        PortHit hit = findPort(graph, getContext().getMouseX(), getContext().getMouseY());
        if (hit == null) return;
        DronePortDefinition port = hit.port;
        String direction = I18n.format(port.getDirection() == DronePortDirection.INPUT
                ? "drtech.drone.port.tooltip.input" : "drtech.drone.port.tooltip.output");
        RichTooltip tooltip = new RichTooltip();
        tooltip.add(I18n.format("drtech.drone.port.tooltip.title", portLabel(port.getId()), direction)).newLine();
        tooltip.add(I18n.format("drtech.drone.port.tooltip.type", portTypeLabel(port.getType()))).newLine();
        tooltip.add(I18n.format(port.isRequired()
                ? "drtech.drone.port.tooltip.required" : "drtech.drone.port.tooltip.optional")).newLine();
        String descriptionKey = "drtech.drone.port.description." + port.getId();
        if (I18n.hasKey(descriptionKey)) tooltip.add(I18n.format(descriptionKey)).newLine();
        tooltip.add(I18n.format(port.getDirection() == DronePortDirection.INPUT
                ? "drtech.drone.port.tooltip.connect_input" : "drtech.drone.port.tooltip.connect_output")).newLine();
        tooltip.add(I18n.format("drtech.drone.port.tooltip.remove"));
        tooltip.draw(context);
    }

    @Override
    public @NotNull Result onMousePressed(int mouseButton) {
        DroneProgramGraph graph = graphSupplier.get();
        if (graph == null) return Result.IGNORE;
        int mouseX = getContext().getMouseX();
        int mouseY = getContext().getMouseY();
        if (contextMenu != null) {
            if (mouseButton == 0 && handleContextMenuClick(mouseX, mouseY)) return Result.SUCCESS;
            contextMenu = null;
        }
        if (mouseButton == 0 && navigateFromMiniMap(graph, mouseX, mouseY)) return Result.SUCCESS;
        if (mouseButton == 2) {
            panning = true;
            lastMouseX = mouseX;
            lastMouseY = mouseY;
            return Result.SUCCESS;
        }
        PortHit portHit = findPort(graph, mouseX, mouseY);
        if (portHit != null) {
            if (mouseButton == 1 && editableSupplier.getAsBoolean()) {
                removePortEdges(graph, portHit);
                pendingPort = null;
                return Result.SUCCESS;
            }
            if (mouseButton == 0 && editableSupplier.getAsBoolean()) {
                handlePortClick(graph, portHit);
                return Result.SUCCESS;
            }
        }
        DroneProgramNode node = findNode(graph, mouseX, mouseY);
        if (mouseButton == 1) {
            if (node != null) {
                if (!selectedNodeIds.contains(node.getId())) {
                    selectedNodeIds.clear();
                    selectedNodeIds.add(node.getId());
                }
                selectedNodeId = node.getId();
                selectedPropertyIndex = 0;
                clearPropertyDraft();
                if (DroneGroupLayout.isGroup(node)) DroneGroupLayout.expandSelectedGroups(graph, selectedNodeIds);
            }
            pendingPort = null;
            openContextMenu(mouseX, mouseY, node != null);
            return Result.SUCCESS;
        }
        boolean additive = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
        UUID previousSelection = selectedNodeId;
        if (node == null) {
            if (mouseButton == 0) {
                if (!additive) clearSelection();
                marqueeSelecting = true;
                marqueeAdditive = additive;
                marqueeStartX = marqueeEndX = mouseX;
                marqueeStartY = marqueeEndY = mouseY;
                pendingPort = null;
                return Result.SUCCESS;
            }
            selectedNodeId = null;
        } else if (additive) {
            if (selectedNodeIds.remove(node.getId())) {
                if (DroneGroupLayout.isGroup(node)) selectedNodeIds.removeAll(DroneGroupLayout.members(graph, node));
                if (node.getId().equals(selectedNodeId)) selectedNodeId = firstSelectedNodeId();
                selectedPropertyIndex = 0;
                clearPropertyDraft();
                return Result.SUCCESS;
            }
            selectedNodeIds.add(node.getId());
            selectedNodeId = node.getId();
        } else if (!selectedNodeIds.contains(node.getId())) {
            selectedNodeIds.clear();
            selectedNodeIds.add(node.getId());
            selectedNodeId = node.getId();
        } else {
            selectedNodeId = node.getId();
        }
        if (node != null && DroneGroupLayout.isGroup(node) && selectedNodeIds.contains(node.getId())) {
            DroneGroupLayout.expandSelectedGroups(graph, selectedNodeIds);
        }
        if (!java.util.Objects.equals(previousSelection, selectedNodeId)) {
            selectedPropertyIndex = 0;
            clearPropertyDraft();
        }
        if (mouseButton == 0 && node != null && editableSupplier.getAsBoolean()) {
            draggingNodeId = node.getId();
            dragStartMouseX = mouseX;
            dragStartMouseY = mouseY;
            dragOrigins.clear();
            for (DroneProgramNode selected : graph.getNodes()) {
                if (selectedNodeIds.contains(selected.getId())) {
                    PreviewPosition existing = previews.get(selected.getId());
                    dragOrigins.put(selected.getId(), existing == null
                            ? new PreviewPosition(selected.getX(), selected.getY())
                            : new PreviewPosition(existing.x, existing.y));
                }
            }
            return Result.SUCCESS;
        }
        pendingPort = null;
        return Result.ACCEPT;
    }

    private void handlePortClick(DroneProgramGraph graph, PortHit hit) {
        if (hit.port.getDirection() == DronePortDirection.OUTPUT) {
            pendingPort = new PendingPort(hit.node.getId(), hit.port.getId(), hit.port.getType());
            return;
        }
        if (pendingPort == null) {
            showConnectionHint(I18n.format("drtech.drone.canvas.connection.select_output"));
            return;
        }
        if (!hit.port.getType().accepts(pendingPort.type)) {
            showConnectionHint(I18n.format("drtech.drone.canvas.connection.incompatible",
                    portTypeLabel(pendingPort.type), portTypeLabel(hit.port.getType())));
            return;
        }
        String sourceLabel = portLabel(pendingPort.portId);
        commandSink.accept(DroneGraphEditCommand.addEdge(graph.getRevision(), UUID.randomUUID(), pendingPort.nodeId,
                pendingPort.portId, hit.node.getId(), hit.port.getId()));
        showConnectionHint(I18n.format("drtech.drone.canvas.connection.connected",
                sourceLabel, portLabel(hit.port.getId())), 0xFF8FF0A4);
        pendingPort = null;
    }

    private void removePortEdges(DroneProgramGraph graph, PortHit hit) {
        for (DroneProgramEdge edge : graph.getEdges()) {
            boolean matches = hit.port.getDirection() == DronePortDirection.INPUT
                    ? edge.getTargetNodeId().equals(hit.node.getId()) && edge.getTargetPortId().equals(hit.port.getId())
                    : edge.getSourceNodeId().equals(hit.node.getId()) && edge.getSourcePortId().equals(hit.port.getId());
            if (matches) {
                commandSink.accept(DroneGraphEditCommand.removeEdge(graph.getRevision(), edge.getId()));
                showConnectionHint(I18n.format("drtech.drone.canvas.connection.removed"), 0xFF8FF0A4);
                return;
            }
        }
        showConnectionHint(I18n.format("drtech.drone.canvas.connection.no_edge"));
    }

    private void showConnectionHint(String text) {
        showConnectionHint(text, 0xFFFFA0A0);
    }

    private void showConnectionHint(String text, int color) {
        connectionHint = text == null ? "" : text;
        connectionHintColor = color;
        connectionHintUntil = System.currentTimeMillis() + 4_000L;
    }

    @Override
    public void onMouseDrag(int mouseButton, long timeSinceClick) {
        int mouseX = getContext().getMouseX();
        int mouseY = getContext().getMouseY();
        if (panning && mouseButton == 2) {
            panX += unscale(mouseX - lastMouseX);
            panY += unscale(mouseY - lastMouseY);
            lastMouseX = mouseX;
            lastMouseY = mouseY;
        } else if (draggingNodeId != null && mouseButton == 0) {
            int deltaX = unscale(mouseX - dragStartMouseX);
            int deltaY = unscale(mouseY - dragStartMouseY);
            boolean snap = Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL);
            for (Map.Entry<UUID, PreviewPosition> entry : dragOrigins.entrySet()) {
                PreviewPosition origin = entry.getValue();
                int x = origin.x + deltaX;
                int y = origin.y + deltaY;
                int previewX = snap ? snapCoordinate(x) : x;
                int previewY = snap ? snapCoordinate(y) : y;
                PreviewPosition previous = previews.get(entry.getKey());
                if (previous == null || previous.x != previewX || previous.y != previewY || previous.isAwaiting()) {
                    previews.put(entry.getKey(), new PreviewPosition(previewX, previewY));
                }
            }
        } else if (marqueeSelecting && mouseButton == 0) {
            marqueeEndX = mouseX;
            marqueeEndY = mouseY;
        }
    }

    @Override
    public boolean onMouseRelease(int mouseButton) {
        panning = false;
        if (draggingNodeId != null && mouseButton == 0) {
            DroneProgramGraph graph = graphSupplier.get();
            if (graph != null && editableSupplier.getAsBoolean()) {
                List<DroneGraphEditCommand> moves = new ArrayList<>();
                long expiresAt = System.currentTimeMillis() + DRAG_ACK_TIMEOUT_MS;
                for (Map.Entry<UUID, PreviewPosition> entry : dragOrigins.entrySet()) {
                    PreviewPosition preview = previews.get(entry.getKey());
                    if (preview != null && (preview.x != entry.getValue().x || preview.y != entry.getValue().y)) {
                        moves.add(DroneGraphEditCommand.moveNode(graph.getRevision(), entry.getKey(),
                                preview.x, preview.y));
                        previews.put(entry.getKey(), preview.awaiting(graph.getRevision(), expiresAt));
                    } else {
                        previews.remove(entry.getKey());
                    }
                }
                if (!moves.isEmpty()) commandSink.accept(DroneGraphEditCommand.batch(graph.getRevision(), moves));
            } else {
                for (UUID nodeId : dragOrigins.keySet()) previews.remove(nodeId);
            }
            draggingNodeId = null;
            dragOrigins.clear();
            return true;
        }
        if (marqueeSelecting && mouseButton == 0) {
            DroneProgramGraph graph = graphSupplier.get();
            if (graph != null) applyMarqueeSelection(graph);
            marqueeSelecting = false;
            return true;
        }
        if (mouseButton == 0 && pendingPort != null) {
            DroneProgramGraph graph = graphSupplier.get();
            PortHit hit = graph == null ? null : findPort(graph, getContext().getMouseX(), getContext().getMouseY());
            if (graph != null && hit != null && hit.port.getDirection() == DronePortDirection.INPUT) {
                handlePortClick(graph, hit);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onMouseScroll(UpOrDown direction, int amount) {
        contextMenu = null;
        int before = zoomPercent;
        zoomPercent = Math.max(50, Math.min(150, zoomPercent + direction.modifier * 10));
        return before != zoomPercent;
    }

    @Override
    public @NotNull Result onKeyPressed(char typedChar, int keyCode) {
        DroneProgramGraph graph = graphSupplier.get();
        if (keyCode == Keyboard.KEY_ESCAPE && contextMenu != null) {
            contextMenu = null;
            return Result.SUCCESS;
        }
        boolean control = Interactable.hasControlDown();
        if (control) {
            switch (keyCode) {
                case Keyboard.KEY_A:
                    selectAll();
                    return Result.SUCCESS;
                case Keyboard.KEY_C:
                    copySelected();
                    return Result.SUCCESS;
                case Keyboard.KEY_V:
                    pasteCopiedNode();
                    return Result.SUCCESS;
                case Keyboard.KEY_D:
                    copySelected();
                    pasteCopiedNode();
                    return Result.SUCCESS;
                case Keyboard.KEY_Z:
                    if (graph != null && editableSupplier.getAsBoolean()) {
                        commandSink.accept(DroneGraphEditCommand.undo(graph.getRevision()));
                    }
                    return Result.SUCCESS;
                case Keyboard.KEY_Y:
                    if (graph != null && editableSupplier.getAsBoolean()) {
                        commandSink.accept(DroneGraphEditCommand.redo(graph.getRevision()));
                    }
                    return Result.SUCCESS;
                case Keyboard.KEY_L:
                    autoLayoutSelectedOrAll();
                    return Result.SUCCESS;
                case Keyboard.KEY_G:
                    snapSelectedToGrid();
                    return Result.SUCCESS;
                default:
                    break;
            }
        }
        switch (keyCode) {
            case Keyboard.KEY_DELETE:
            case Keyboard.KEY_BACK:
                deleteSelected();
                return Result.SUCCESS;
            case Keyboard.KEY_HOME:
                fitSelectionOrAll();
                return Result.SUCCESS;
            case Keyboard.KEY_ESCAPE:
                pendingPort = null;
                clearSelection();
                return Result.SUCCESS;
            case Keyboard.KEY_LEFT:
                nudgeSelected(-nudgeAmount(), 0);
                return Result.SUCCESS;
            case Keyboard.KEY_RIGHT:
                nudgeSelected(nudgeAmount(), 0);
                return Result.SUCCESS;
            case Keyboard.KEY_UP:
                nudgeSelected(0, -nudgeAmount());
                return Result.SUCCESS;
            case Keyboard.KEY_DOWN:
                nudgeSelected(0, nudgeAmount());
                return Result.SUCCESS;
            default:
                return Result.IGNORE;
        }
    }

    public void deleteSelected() {
        DroneProgramGraph graph = graphSupplier.get();
        if (graph != null && !selectedNodeIds.isEmpty() && editableSupplier.getAsBoolean()) {
            List<DroneGraphEditCommand> removals = new ArrayList<>();
            for (UUID nodeId : selectedNodeIds) {
                if (graph.getNode(nodeId) != null) {
                    removals.add(DroneGraphEditCommand.removeNode(graph.getRevision(), nodeId));
                }
            }
            if (!removals.isEmpty()) commandSink.accept(DroneGraphEditCommand.batch(graph.getRevision(), removals));
            clearSelection();
            pendingPort = null;
        }
    }

    public void disconnectSelected() {
        DroneProgramGraph graph = graphSupplier.get();
        if (graph == null || selectedNodeIds.isEmpty() || !editableSupplier.getAsBoolean()) return;
        List<DroneGraphEditCommand> removals = new ArrayList<>();
        for (DroneProgramEdge edge : graph.getEdges()) {
            if (selectedNodeIds.contains(edge.getSourceNodeId()) || selectedNodeIds.contains(edge.getTargetNodeId())) {
                removals.add(DroneGraphEditCommand.removeEdge(graph.getRevision(), edge.getId()));
            }
        }
        if (!removals.isEmpty()) commandSink.accept(DroneGraphEditCommand.batch(graph.getRevision(), removals));
    }

    private void addCommentAt(int graphX, int graphY) {
        DroneProgramGraph graph = graphSupplier.get();
        if (graph == null || !editableSupplier.getAsBoolean() || graph.getNodes().size() >= 256) return;
        net.minecraft.nbt.NBTTagCompound config = new net.minecraft.nbt.NBTTagCompound();
        config.setString("Text", "");
        UUID nodeId = UUID.randomUUID();
        commandSink.accept(DroneGraphEditCommand.addNode(graph.getRevision(), nodeId, DrTechDroneNodes.COMMENT,
                graphX, graphY, config));
        selectedNodeIds.clear();
        selectedNodeIds.add(nodeId);
        selectedNodeId = nodeId;
        selectedPropertyIndex = 0;
    }

    private void createGroupForSelection(int graphX, int graphY) {
        DroneProgramGraph graph = graphSupplier.get();
        if (graph == null || !editableSupplier.getAsBoolean() || graph.getNodes().size() >= 256) return;
        DroneGroupLayout.Frame frame = DroneGroupLayout.surroundingFrame(graph, selectedNodeIds);
        int x = frame == null ? graphX : frame.getX();
        int y = frame == null ? graphY : frame.getY();
        int width = frame == null ? DroneGroupLayout.DEFAULT_WIDTH : frame.getWidth();
        int height = frame == null ? DroneGroupLayout.DEFAULT_HEIGHT : frame.getHeight();
        net.minecraft.nbt.NBTTagCompound config = new net.minecraft.nbt.NBTTagCompound();
        config.setString("Title", "");
        config.setInteger("Width", width);
        config.setInteger("Height", height);
        config.setString("Color", "BLUE");
        config.setBoolean("Collapsed", false);
        UUID nodeId = UUID.randomUUID();
        commandSink.accept(DroneGraphEditCommand.addNode(graph.getRevision(), nodeId, DrTechDroneNodes.GROUP,
                x, y, config));
        selectedNodeIds.add(nodeId);
        selectedNodeId = nodeId;
        selectedPropertyIndex = 0;
    }

    /** Copies all selected nodes plus connections whose endpoints are both inside the selection. */
    public void copySelected() {
        DroneProgramGraph graph = graphSupplier.get();
        if (graph == null || selectedNodeIds.isEmpty()) {
            clipboard = null;
            return;
        }
        List<DroneProgramNode> nodes = new ArrayList<>();
        List<DroneProgramEdge> edges = new ArrayList<>();
        for (DroneProgramNode node : graph.getNodes()) {
            if (selectedNodeIds.contains(node.getId())) nodes.add(new DroneProgramNode(node.getId(), node.getType(),
                    node.getX(), node.getY(), node.getConfiguration()));
        }
        for (DroneProgramEdge edge : graph.getEdges()) {
            if (selectedNodeIds.contains(edge.getSourceNodeId()) && selectedNodeIds.contains(edge.getTargetNodeId())) {
                edges.add(edge);
            }
        }
        clipboard = nodes.isEmpty() ? null : new ClipboardGraph(nodes, edges);
        clipboardPasteCount = 0;
    }

    /** Pastes a remapped independent subgraph as one server-side transaction. */
    public void pasteCopiedNode() {
        pasteCopiedNodeAt(null, null);
    }

    private void pasteCopiedNodeAt(Integer targetX, Integer targetY) {
        DroneProgramGraph graph = graphSupplier.get();
        if (graph == null || clipboard == null || !editableSupplier.getAsBoolean()) return;
        if (graph.getNodes().size() + clipboard.nodes.size() > 256
                || graph.getEdges().size() + clipboard.edges.size() > 512) return;
        int offset = 24 + (++clipboardPasteCount - 1) * 12;
        int minX = clipboard.nodes.stream().mapToInt(DroneProgramNode::getX).min().orElse(0);
        int minY = clipboard.nodes.stream().mapToInt(DroneProgramNode::getY).min().orElse(0);
        int offsetX = targetX == null ? offset : targetX - minX;
        int offsetY = targetY == null ? offset : targetY - minY;
        Map<UUID, UUID> remapped = new LinkedHashMap<>();
        List<DroneGraphEditCommand> commands = new ArrayList<>();
        for (DroneProgramNode node : clipboard.nodes) {
            UUID newId = UUID.randomUUID();
            remapped.put(node.getId(), newId);
            commands.add(DroneGraphEditCommand.addNode(graph.getRevision(), newId, node.getType(),
                    node.getX() + offsetX, node.getY() + offsetY, node.getConfiguration()));
        }
        for (DroneProgramEdge edge : clipboard.edges) {
            commands.add(DroneGraphEditCommand.addEdge(graph.getRevision(), UUID.randomUUID(),
                    remapped.get(edge.getSourceNodeId()), edge.getSourcePortId(),
                    remapped.get(edge.getTargetNodeId()), edge.getTargetPortId()));
        }
        commandSink.accept(DroneGraphEditCommand.batch(graph.getRevision(), commands));
        selectedNodeIds.clear();
        selectedNodeIds.addAll(remapped.values());
        selectedNodeId = firstSelectedNodeId();
        selectedPropertyIndex = 0;
    }

    public boolean hasCopiedNode() {
        return clipboard != null;
    }

    public void alignSelectedHorizontal() {
        if (isShiftDown()) distributeSelected(true);
        else alignSelected(true);
    }

    public void alignSelectedVertical() {
        if (isShiftDown()) distributeSelected(false);
        else alignSelected(false);
    }

    public int getSelectionCount() { return selectedNodeIds.size(); }

    public void selectAll() {
        DroneProgramGraph graph = graphSupplier.get();
        if (graph == null) return;
        selectedNodeIds.clear();
        for (DroneProgramNode node : graph.getNodes()) selectedNodeIds.add(node.getId());
        selectedNodeId = firstSelectedNodeId();
        selectedPropertyIndex = 0;
        clearPropertyDraft();
    }

    /** Selects and centers one graph node without mutating the server-side program. */
    public boolean focusNode(UUID nodeId) {
        DroneProgramGraph graph = graphSupplier.get();
        DroneProgramNode node = graph == null || nodeId == null ? null : graph.getNode(nodeId);
        if (node == null) return false;
        selectedNodeIds.clear();
        selectedNodeIds.add(nodeId);
        selectedNodeId = nodeId;
        selectedPropertyIndex = 0;
        clearPropertyDraft();
        pendingPort = null;
        int focusWidth = DroneGroupLayout.isGroup(node) ? DroneGroupLayout.width(node) : NODE_WIDTH;
        int focusHeight = DroneGroupLayout.isGroup(node) ? DroneGroupLayout.height(node)
                : nodeHeight(registry.get(node.getType()));
        panX = unscale(getArea().w() / 2) - node.getX() - focusWidth / 2;
        panY = unscale(getArea().h() / 2) - node.getY() - focusHeight / 2;
        return true;
    }

    /** Selects a declared property after focusing its node. Returns false for port/global diagnostics. */
    public boolean focusNodeProperty(UUID nodeId, String propertyId) {
        if (!focusNode(nodeId) || propertyId == null || propertyId.isEmpty()) return false;
        DroneProgramGraph graph = graphSupplier.get();
        DroneProgramNode node = graph == null ? null : graph.getNode(nodeId);
        DroneNodeDefinition definition = node == null ? null : registry.get(node.getType());
        if (definition == null) return false;
        int index = 0;
        for (DroneNodePropertyDefinition property : definition.getProperties()) {
            if (propertyId.equals(property.getId())) {
                selectedPropertyIndex = index;
                clearPropertyDraft();
                return true;
            }
            index++;
        }
        return false;
    }

    public void resetViewOrAutoLayout() {
        if (isShiftDown()) autoLayoutSelectedOrAll();
        else resetView();
    }

    public void autoLayoutSelectedOrAll() {
        DroneProgramGraph graph = graphSupplier.get();
        if (graph == null || !editableSupplier.getAsBoolean()) return;
        Collection<UUID> target = selectedNodeIds.size() > 1 ? new ArrayList<>(selectedNodeIds)
                : Collections.emptyList();
        Map<UUID, DroneGraphAutoLayout.Position> layout = DroneGraphAutoLayout.layout(graph, target, registry);
        List<DroneGraphEditCommand> moves = new ArrayList<>();
        for (Map.Entry<UUID, DroneGraphAutoLayout.Position> entry : layout.entrySet()) {
            DroneProgramNode node = graph.getNode(entry.getKey());
            DroneGraphAutoLayout.Position position = entry.getValue();
            if (node != null && (node.getX() != position.getX() || node.getY() != position.getY())) {
                moves.add(DroneGraphEditCommand.moveNode(graph.getRevision(), node.getId(),
                        position.getX(), position.getY()));
            }
        }
        if (!moves.isEmpty()) commandSink.accept(DroneGraphEditCommand.batch(graph.getRevision(), moves));
    }

    public void snapSelectedToGrid() {
        DroneProgramGraph graph = graphSupplier.get();
        if (graph == null || selectedNodeIds.isEmpty() || !editableSupplier.getAsBoolean()) return;
        List<DroneGraphEditCommand> moves = new ArrayList<>();
        for (UUID nodeId : selectedNodeIds) {
            DroneProgramNode node = graph.getNode(nodeId);
            if (node == null) continue;
            int x = snapCoordinate(node.getX());
            int y = snapCoordinate(node.getY());
            if (x != node.getX() || y != node.getY()) {
                moves.add(DroneGraphEditCommand.moveNode(graph.getRevision(), nodeId, x, y));
            }
        }
        if (!moves.isEmpty()) commandSink.accept(DroneGraphEditCommand.batch(graph.getRevision(), moves));
    }

    private void nudgeSelected(int deltaX, int deltaY) {
        DroneProgramGraph graph = graphSupplier.get();
        if (graph == null || selectedNodeIds.isEmpty() || !editableSupplier.getAsBoolean()) return;
        List<DroneGraphEditCommand> moves = new ArrayList<>();
        for (UUID nodeId : selectedNodeIds) {
            DroneProgramNode node = graph.getNode(nodeId);
            if (node != null) moves.add(DroneGraphEditCommand.moveNode(graph.getRevision(), nodeId,
                    node.getX() + deltaX, node.getY() + deltaY));
        }
        if (!moves.isEmpty()) commandSink.accept(DroneGraphEditCommand.batch(graph.getRevision(), moves));
    }

    private static int nudgeAmount() { return isShiftDown() ? 16 : 1; }

    private void alignSelected(boolean horizontal) {
        DroneProgramGraph graph = graphSupplier.get();
        DroneProgramNode anchor = graph == null || selectedNodeId == null ? null : graph.getNode(selectedNodeId);
        if (anchor == null || selectedNodeIds.size() < 2 || !editableSupplier.getAsBoolean()) return;
        List<DroneGraphEditCommand> moves = new ArrayList<>();
        for (UUID nodeId : selectedNodeIds) {
            DroneProgramNode node = graph.getNode(nodeId);
            if (node == null || node.getId().equals(anchor.getId())) continue;
            int x = horizontal ? node.getX() : anchor.getX();
            int y = horizontal ? anchor.getY() : node.getY();
            if (x != node.getX() || y != node.getY()) {
                moves.add(DroneGraphEditCommand.moveNode(graph.getRevision(), node.getId(), x, y));
            }
        }
        if (!moves.isEmpty()) commandSink.accept(DroneGraphEditCommand.batch(graph.getRevision(), moves));
    }

    private void distributeSelected(boolean horizontal) {
        DroneProgramGraph graph = graphSupplier.get();
        if (graph == null || selectedNodeIds.size() < 3 || !editableSupplier.getAsBoolean()) return;
        List<DroneProgramNode> nodes = new ArrayList<>();
        for (UUID nodeId : selectedNodeIds) {
            DroneProgramNode node = graph.getNode(nodeId);
            if (node != null) nodes.add(node);
        }
        if (nodes.size() < 3) return;
        nodes.sort((left, right) -> Integer.compare(horizontal ? left.getX() : left.getY(),
                horizontal ? right.getX() : right.getY()));
        int first = horizontal ? nodes.get(0).getX() : nodes.get(0).getY();
        int last = horizontal ? nodes.get(nodes.size() - 1).getX() : nodes.get(nodes.size() - 1).getY();
        List<DroneGraphEditCommand> moves = new ArrayList<>();
        for (int i = 1; i < nodes.size() - 1; i++) {
            DroneProgramNode node = nodes.get(i);
            int coordinate = first + (int) Math.round((last - first) * (double) i / (nodes.size() - 1));
            int x = horizontal ? coordinate : node.getX();
            int y = horizontal ? node.getY() : coordinate;
            if (x != node.getX() || y != node.getY()) {
                moves.add(DroneGraphEditCommand.moveNode(graph.getRevision(), node.getId(), x, y));
            }
        }
        if (!moves.isEmpty()) commandSink.accept(DroneGraphEditCommand.batch(graph.getRevision(), moves));
    }

    private void applyMarqueeSelection(DroneProgramGraph graph) {
        int left = Math.min(marqueeStartX, marqueeEndX);
        int right = Math.max(marqueeStartX, marqueeEndX);
        int top = Math.min(marqueeStartY, marqueeEndY);
        int bottom = Math.max(marqueeStartY, marqueeEndY);
        if (!marqueeAdditive) selectedNodeIds.clear();
        for (DroneProgramNode node : graph.getNodes()) {
            Point point = nodePoint(node);
            int nodeRight = point.x + scaled(DroneGroupLayout.isGroup(node)
                    ? DroneGroupLayout.width(node) : NODE_WIDTH);
            int nodeBottom = point.y + scaled(DroneGroupLayout.isGroup(node)
                    ? DroneGroupLayout.height(node) : nodeHeight(registry.get(node.getType())));
            if (nodeRight >= left && point.x <= right && nodeBottom >= top && point.y <= bottom) {
                selectedNodeIds.add(node.getId());
            }
        }
        DroneGroupLayout.expandSelectedGroups(graph, selectedNodeIds);
        if (selectedNodeId == null || !selectedNodeIds.contains(selectedNodeId)) {
            selectedNodeId = firstSelectedNodeId();
            selectedPropertyIndex = 0;
            clearPropertyDraft();
        }
    }

    private void clearSelection() {
        selectedNodeIds.clear();
        selectedNodeId = null;
        selectedPropertyIndex = 0;
        clearPropertyDraft();
    }

    private UUID firstSelectedNodeId() {
        return selectedNodeIds.isEmpty() ? null : selectedNodeIds.iterator().next();
    }

    public String getSelectedNodeLabel() {
        DroneProgramGraph graph = graphSupplier.get();
        DroneProgramNode node = graph == null || selectedNodeId == null ? null : graph.getNode(selectedNodeId);
        return node == null ? "" : node.getConfiguration().getString("Label");
    }

    public void setSelectedNodeLabel(String value) {
        if (value == null || value.length() > 32) return;
        configureSelected((node, config) -> {
            if (value.trim().isEmpty()) config.removeTag("Label");
            else config.setString("Label", value.trim());
        });
    }

    public void resetView() {
        panX = 8;
        panY = 8;
        zoomPercent = 100;
    }

    /** Centers every node in the canvas and chooses the largest safe zoom. */
    public void fitAll() {
        DroneProgramGraph graph = graphSupplier.get();
        if (graph == null || graph.getNodes().isEmpty()) {
            resetView();
            return;
        }
        fitNodes(graph.getNodes());
    }

    /** Fits the current selection, or the complete graph if nothing is selected. */
    public void fitSelectionOrAll() {
        DroneProgramGraph graph = graphSupplier.get();
        if (graph == null || selectedNodeIds.isEmpty()) {
            fitAll();
            return;
        }
        List<DroneProgramNode> nodes = new ArrayList<>();
        for (UUID nodeId : selectedNodeIds) {
            DroneProgramNode node = graph.getNode(nodeId);
            if (node != null) nodes.add(node);
        }
        if (nodes.isEmpty()) fitAll();
        else fitNodes(nodes);
    }

    private void fitNodes(Collection<DroneProgramNode> nodes) {
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        for (DroneProgramNode node : nodes) {
            minX = Math.min(minX, node.getX());
            minY = Math.min(minY, node.getY());
            maxX = Math.max(maxX, node.getX() + (DroneGroupLayout.isGroup(node)
                    ? DroneGroupLayout.width(node) : NODE_WIDTH));
            maxY = Math.max(maxY, node.getY() + (DroneGroupLayout.isGroup(node)
                    ? DroneGroupLayout.height(node) : nodeHeight(registry.get(node.getType()))));
        }
        int contentWidth = Math.max(1, maxX - minX);
        int contentHeight = Math.max(1, maxY - minY);
        int availableWidth = Math.max(1, getArea().w() - 16);
        int availableHeight = Math.max(1, getArea().h() - 16);
        zoomPercent = Math.max(50, Math.min(150,
                Math.min(availableWidth * 100 / contentWidth, availableHeight * 100 / contentHeight)));
        panX = 8 * 100 / zoomPercent - minX;
        panY = 8 * 100 / zoomPercent - minY;
    }

    public String getSelectedPropertySummary() {
        SelectedProperty selected = selectedProperty();
        if (selected == null) return I18n.format("drtech.drone.inspector.no_properties");
        DroneNodePropertyDefinition property = selected.property;
        net.minecraft.nbt.NBTTagCompound config = selected.node.getConfiguration();
        String value;
        switch (property.getType()) {
            case INTEGER:
                value = Integer.toString(config.getInteger(property.getId()));
                break;
            case NUMBER:
                value = Double.toString(config.getDouble(property.getId()));
                break;
            case BOOLEAN:
                value = Boolean.toString(config.getBoolean(property.getId()));
                break;
            case STRING:
                value = config.getString(property.getId());
                if (value.length() > 42 || value.indexOf('\n') >= 0 || value.indexOf('\r') >= 0) {
                    value = value.replace('\n', ' ').replace('\r', ' ').trim();
                    if (value.length() > 42) value = value.substring(0, 41) + "…";
                }
                break;
            case ENUM:
            case DIRECTION:
                value = config.getString(property.getId());
                String valueKey = "drtech.drone.value." + value.toLowerCase(java.util.Locale.ROOT);
                if (I18n.hasKey(valueKey)) value = I18n.format(valueKey);
                break;
            case ITEM_SELECTOR:
                DroneItemFilterSpec spec = readItemFilter(config, property.getId());
                if (spec.getRules().isEmpty()) value = "any";
                else {
                    int nbtRules = 0;
                    for (DroneItemFilterSpec.Rule rule : spec.getRules()) if (rule.isMatchNbt()) nbtRules++;
                    value = (spec.getMode() == DroneFilterMode.WHITELIST ? "allow " : "deny ")
                            + spec.getRules().size() + (nbtRules == 0 ? "" : " | nbt " + nbtRules);
                }
                break;
            case BLOCK_SELECTOR:
                DroneBlockFilterSpec blockSpec = readBlockFilter(config, property.getId());
                if (blockSpec.getRules().isEmpty()) value = "any";
                else {
                    int stateCount = 0;
                    for (DroneBlockFilterSpec.Rule rule : blockSpec.getRules()) {
                        stateCount += rule.getStateProperties().size();
                    }
                    value = (blockSpec.getMode() == DroneFilterMode.WHITELIST ? "allow " : "deny ")
                            + blockSpec.getRules().size() + (stateCount == 0 ? "" : " | state " + stateCount);
                }
                break;
            case FLUID_SELECTOR:
                value = config.getString(property.getId());
                if (value.isEmpty()) value = I18n.format("drtech.drone.programmer.any_fluid");
                break;
            case ENTITY_SELECTOR:
                DroneEntityFilterSpec entitySpec = readEntityFilter(config, property.getId());
                value = entitySpec.getEntityIds().isEmpty() ? "any"
                        : (entitySpec.getMode() == DroneFilterMode.WHITELIST ? "allow " : "deny ")
                                + entitySpec.getEntityIds().size();
                break;
            case DOCK_REFERENCE:
                net.minecraft.nbt.NBTTagCompound dock = config.getCompoundTag(property.getId());
                if (!dock.hasKey("DockId", 8) || !dock.hasKey("Position", 4)) {
                    value = I18n.format("drtech.drone.programmer.dock_unset");
                } else {
                    BlockPos position = BlockPos.fromLong(dock.getLong("Position"));
                    String name = dock.getString("Name");
                    value = (name.isEmpty() ? dock.getString("DockId") : name) + " @ "
                            + position.getX() + "," + position.getY() + "," + position.getZ();
                }
                break;
            case PROGRAM_REFERENCE:
                net.minecraft.nbt.NBTTagCompound program = config.getCompoundTag(property.getId());
                if (!program.hasKey("ProgramId", 8) || !program.hasKey("Revision", 99)) {
                    value = I18n.format("drtech.drone.programmer.program_unset");
                } else {
                    String name = program.getString("Name");
                    value = (name.isEmpty() ? program.getString("ProgramId") : name)
                            + " @ r" + program.getLong("Revision");
                }
                break;
            default:
                value = config.hasKey(property.getId()) ? "configured" : "unset";
        }
        String propertyKey = "drtech.drone.property." + property.getId().toLowerCase(java.util.Locale.ROOT);
        String propertyName = I18n.hasKey(propertyKey) ? I18n.format(propertyKey) : property.getId();
        String heading = propertyName + " [" + (selected.index + 1) + "/" + selected.count + "]";
        String preview = getSelectedPropertyChangePreview();
        return preview.isEmpty() ? heading + ": " + value : heading + "\n" + preview;
    }

    /** Shows a pending text edit, or the most recent committed edit, without exposing raw selector NBT. */
    public String getSelectedPropertyChangePreview() {
        SelectedProperty selected = selectedProperty();
        if (selected == null) return "";
        String current = compactPropertyValue(selected.property, selected.node.getConfiguration());
        if (isSelectedPropertyTextEditable() && propertyDraftDirty) {
            ensurePropertyDraft(selected);
            String draft = compactDraftValue(selected.property, propertyDraft);
            if (!current.equals(draft)) {
                return I18n.format("drtech.drone.programmer.property_change_pending", current, draft);
            }
        }
        if (selected.node.getId().equals(lastPropertyChangeNodeId)
                && selected.property.getId().equals(lastPropertyChangeId)) {
            return I18n.format("drtech.drone.programmer.property_change_applied",
                    lastPropertyBefore, lastPropertyAfter);
        }
        return "";
    }

    public boolean canResetSelectedProperty() {
        SelectedProperty selected = selectedProperty();
        if (selected == null) return false;
        net.minecraft.nbt.NBTTagCompound config = selected.node.getConfiguration();
        return config.hasKey(selected.property.getId())
                || (selected.property.getType() == DroneNodePropertyType.ITEM_SELECTOR
                        && (config.hasKey("Item") || config.hasKey("Meta")));
    }

    public void resetSelectedPropertyToDefault() {
        clearSelectedProperty();
    }

    public void selectPreviousProperty() { moveSelectedProperty(-1); }
    public void selectNextProperty() { moveSelectedProperty(1); }

    private void moveSelectedProperty(int delta) {
        SelectedProperty selected = selectedProperty();
        if (selected == null) return;
        selectedPropertyIndex = Math.floorMod(selected.index + delta, selected.count);
        clearPropertyDraft();
    }

    public boolean isSelectedPropertyNumeric() {
        SelectedProperty selected = selectedProperty();
        return selected != null && (selected.property.getType() == DroneNodePropertyType.INTEGER
                || selected.property.getType() == DroneNodePropertyType.NUMBER);
    }

    public boolean isSelectedChoiceProperty() {
        SelectedProperty selected = selectedProperty();
        return selected != null && DronePropertyChoices.isChoice(selected.property);
    }

    public boolean isSelectedDirectionProperty() {
        SelectedProperty selected = selectedProperty();
        return selected != null && DronePropertyChoices.isSixFaceDirection(selected.property);
    }

    public List<String> getSelectedChoiceValues() {
        SelectedProperty selected = selectedProperty();
        return selected == null ? Collections.emptyList() : DronePropertyChoices.visibleValues(selected.property);
    }

    public String getSelectedChoiceValue() {
        SelectedProperty selected = selectedProperty();
        return selected == null || !isSelectedChoiceProperty() ? ""
                : selected.node.getConfiguration().getString(selected.property.getId());
    }

    public void selectPropertyChoice(String value) {
        SelectedProperty selected = selectedProperty();
        if (selected == null || !DronePropertyChoices.accepts(selected.property, value)) return;
        configureSelected((node, config) -> config.setString(selected.property.getId(), value));
        clearPropertyDraft();
    }

    public boolean isSelectedPropertyTextEditable() {
        SelectedProperty selected = selectedProperty();
        if (selected == null) return false;
        DroneNodePropertyType type = selected.property.getType();
        return type == DroneNodePropertyType.INTEGER || type == DroneNodePropertyType.NUMBER
                || type == DroneNodePropertyType.STRING || type == DroneNodePropertyType.FLUID_SELECTOR;
    }

    /** True for the compact inspector input; selector search fields and long notes use dedicated widgets. */
    public boolean isSelectedInlineTextProperty() {
        return isSelectedPropertyTextEditable() && !isSelectedFluidSelector()
                && !isSelectedItemSelector() && !isSelectedEntitySelector()
                && !isSelectedDockReference() && !isSelectedProgramReference()
                && !isSelectedLongTextProperty();
    }

    /** Localized validation feedback for the property currently being edited. */
    public String getSelectedPropertyValidationMessage() {
        SelectedProperty selected = selectedProperty();
        if (selected == null || !isSelectedPropertyTextEditable()) return "";
        ensurePropertyDraft(selected);
        DroneNodePropertyType type = selected.property.getType();
        if (!propertyDraftDirty) return propertyAllowedRangeMessage(selected.property);
        String trimmed = propertyDraft == null ? "" : propertyDraft.trim();
        if (type == DroneNodePropertyType.INTEGER) {
            try {
                int value = Integer.parseInt(trimmed);
                return value < selected.property.getMinimum() || value > selected.property.getMaximum()
                        ? propertyOutOfRangeMessage(selected.property) : propertyAllowedRangeMessage(selected.property);
            } catch (NumberFormatException ignored) {
                return I18n.format("drtech.drone.programmer.property_invalid_integer",
                        formatBound(selected.property.getMinimum()), formatBound(selected.property.getMaximum()));
            }
        }
        if (type == DroneNodePropertyType.NUMBER) {
            try {
                double value = Double.parseDouble(trimmed);
                if (!Double.isFinite(value)) throw new NumberFormatException();
                return value < selected.property.getMinimum() || value > selected.property.getMaximum()
                        ? propertyOutOfRangeMessage(selected.property) : propertyAllowedRangeMessage(selected.property);
            } catch (NumberFormatException ignored) {
                return I18n.format("drtech.drone.programmer.property_invalid_number",
                        formatBound(selected.property.getMinimum()), formatBound(selected.property.getMaximum()));
            }
        }
        if (type == DroneNodePropertyType.FLUID_SELECTOR) {
            return trimmed.isEmpty() || FluidRegistry.getFluid(trimmed) != null
                    ? I18n.format("drtech.drone.programmer.property_fluid_hint")
                    : I18n.format("drtech.drone.programmer.property_invalid_fluid", trimmed);
        }
        if (propertyDraft.length() > selected.property.getMaxLength()) {
            return I18n.format("drtech.drone.programmer.property_too_long", selected.property.getMaxLength(),
                    propertyDraft.length());
        }
        return propertyAllowedRangeMessage(selected.property);
    }

    private static String propertyAllowedRangeMessage(DroneNodePropertyDefinition property) {
        switch (property.getType()) {
            case INTEGER:
                return I18n.format("drtech.drone.programmer.property_integer_range",
                        formatBound(property.getMinimum()), formatBound(property.getMaximum()));
            case NUMBER:
                return I18n.format("drtech.drone.programmer.property_number_range",
                        formatBound(property.getMinimum()), formatBound(property.getMaximum()));
            case STRING:
                return I18n.format("drtech.drone.programmer.property_string_limit", property.getMaxLength());
            case FLUID_SELECTOR:
                return I18n.format("drtech.drone.programmer.property_fluid_hint");
            default:
                return "";
        }
    }

    private static String propertyOutOfRangeMessage(DroneNodePropertyDefinition property) {
        return I18n.format("drtech.drone.programmer.property_out_of_range",
                formatBound(property.getMinimum()), formatBound(property.getMaximum()));
    }

    private static String formatBound(double value) {
        return Double.isFinite(value) && Math.abs(value) < Long.MAX_VALUE && value == Math.rint(value)
                ? Long.toString((long) value) : Double.toString(value);
    }

    public boolean isSelectedLongTextProperty() {
        SelectedProperty selected = selectedProperty();
        return selected != null && selected.property.getType() == DroneNodePropertyType.STRING
                && selected.property.getMaxLength() > 128;
    }

    /** Coordinate and two-corner area values accept complete XYZ capture presets. */
    public boolean isSelectedCoordinateCaptureTarget() {
        DroneProgramGraph graph = graphSupplier.get();
        DroneProgramNode node = graph == null || selectedNodeId == null ? null : graph.getNode(selectedNodeId);
        return node != null && (node.getType().equals(DrTechDroneNodes.COORDINATE)
                || node.getType().equals(DrTechDroneNodes.AREA));
    }

    @Nullable
    public UUID getSelectedCoordinateCaptureNodeId() {
        return isSelectedCoordinateCaptureTarget() ? selectedNodeId : null;
    }

    public boolean isSelectedAreaCaptureTarget() {
        DroneProgramGraph graph = graphSupplier.get();
        DroneProgramNode node = graph == null || selectedNodeId == null ? null : graph.getNode(selectedNodeId);
        return node != null && node.getType().equals(DrTechDroneNodes.AREA);
    }

    public boolean canCapturePlayerCoordinate() {
        return isSelectedCoordinateCaptureTarget() && Minecraft.getMinecraft().player != null;
    }

    public boolean canCaptureTargetedCoordinate() {
        RayTraceResult hit = Minecraft.getMinecraft().objectMouseOver;
        return isSelectedCoordinateCaptureTarget() && hit != null
                && hit.typeOfHit == RayTraceResult.Type.BLOCK && hit.getBlockPos() != null;
    }

    public boolean canCaptureDockCoordinate() {
        return isSelectedCoordinateCaptureTarget() && dockCoordinateSupplier.get() != null;
    }

    public boolean canCaptureDroneCoordinate() {
        return isSelectedCoordinateCaptureTarget() && droneCoordinateSupplier.get() != null;
    }

    public void capturePlayerCoordinate() {
        if (Minecraft.getMinecraft().player != null) captureSelectedCoordinate(Minecraft.getMinecraft().player.getPosition());
    }

    public void captureTargetedCoordinate() {
        RayTraceResult hit = Minecraft.getMinecraft().objectMouseOver;
        if (hit != null && hit.typeOfHit == RayTraceResult.Type.BLOCK) captureSelectedCoordinate(hit.getBlockPos());
    }

    public void captureDockCoordinate() { captureSelectedCoordinate(dockCoordinateSupplier.get()); }

    public void captureDroneCoordinate() { captureSelectedCoordinate(droneCoordinateSupplier.get()); }

    private void captureSelectedCoordinate(BlockPos position) {
        if (position == null || !isSelectedCoordinateCaptureTarget()) return;
        int areaCorner = selectedAreaCaptureCorner();
        configureSelected((node, config) -> {
            if (node.getType().equals(DrTechDroneNodes.COORDINATE)) {
                config.setInteger("X", position.getX());
                config.setInteger("Y", position.getY());
                config.setInteger("Z", position.getZ());
            } else if (node.getType().equals(DrTechDroneNodes.AREA)) {
                config.setInteger("X" + areaCorner, position.getX());
                config.setInteger("Y" + areaCorner, position.getY());
                config.setInteger("Z" + areaCorner, position.getZ());
            }
        });
        clearPropertyDraft();
    }

    private int selectedAreaCaptureCorner() {
        SelectedProperty property = selectedProperty();
        if (property != null && property.node.getType().equals(DrTechDroneNodes.AREA)
                && property.property.getId().endsWith("2")) return 2;
        return selectedAreaCorner;
    }

    public boolean isSelectedItemFilter() {
        SelectedProperty selected = selectedProperty();
        return selected != null && (selected.property.getType() == DroneNodePropertyType.ITEM_SELECTOR
                || selected.property.getType() == DroneNodePropertyType.BLOCK_SELECTOR);
    }

    public boolean isSelectedItemSelector() {
        SelectedProperty selected = selectedProperty();
        return selected != null && selected.property.getType() == DroneNodePropertyType.ITEM_SELECTOR;
    }

    public boolean isSelectedBlockSelector() {
        SelectedProperty selected = selectedProperty();
        return selected != null && selected.property.getType() == DroneNodePropertyType.BLOCK_SELECTOR;
    }

    public boolean isSelectedAreaPreviewNode() {
        DroneProgramGraph graph = graphSupplier.get();
        DroneProgramNode node = graph == null || selectedNodeId == null ? null : graph.getNode(selectedNodeId);
        return node != null && isAreaOutputNode(node.getType());
    }

    public DroneArea getSelectedAreaPreview() {
        refreshAreaPreview();
        return cachedAreaPreview;
    }

    public String getSelectedAreaPreviewStatus() {
        refreshAreaPreview();
        if (cachedAreaPreview == null) return I18n.format(areaPreviewStatusKey);
        return I18n.format("drtech.drone.programmer.area_preview_summary", cachedAreaPreview.getSizeX(),
                cachedAreaPreview.getSizeY(), cachedAreaPreview.getSizeZ(), cachedAreaPreview.getVolume());
    }

    private void refreshAreaPreview() {
        DroneProgramGraph graph = graphSupplier.get();
        DroneProgramNode node = graph == null || selectedNodeId == null ? null : graph.getNode(selectedNodeId);
        long revision = graph == null ? Long.MIN_VALUE : graph.getRevision();
        long now = System.nanoTime();
        if (!shouldRefreshAreaPreview(areaPreviewRevision, revision, areaPreviewNodeId, selectedNodeId,
                now, areaPreviewNextRefreshNanos)) return;
        areaPreviewNextRefreshNanos = now > Long.MAX_VALUE - AREA_PREVIEW_REFRESH_NANOS
                ? Long.MAX_VALUE : now + AREA_PREVIEW_REFRESH_NANOS;
        areaPreviewRevision = revision;
        areaPreviewNodeId = selectedNodeId;
        cachedAreaPreview = null;
        areaPreviewStatusKey = "drtech.drone.programmer.area_preview_missing";
        if (node == null || !isAreaOutputNode(node.getType())) return;
        try {
            DroneArea area = resolveStaticArea(graph, node, 0);
            if (area == null) return;
            if (!area.isWithinRuntimeLimits()) {
                areaPreviewStatusKey = "drtech.drone.programmer.area_preview_too_large";
                return;
            }
            cachedAreaPreview = area;
            areaPreviewStatusKey = "drtech.drone.programmer.area_preview_ready";
        } catch (PreviewUnavailableException ignored) {
            areaPreviewStatusKey = "drtech.drone.programmer.area_preview_dynamic";
        } catch (RuntimeException ignored) {
            areaPreviewStatusKey = "drtech.drone.programmer.area_preview_invalid";
        }
    }

    static boolean shouldRefreshAreaPreview(long cachedRevision, long currentRevision,
            UUID cachedNodeId, UUID currentNodeId, long nowNanos, long nextRefreshNanos) {
        if (!java.util.Objects.equals(cachedNodeId, currentNodeId)) return true;
        if (cachedRevision == currentRevision) return false;
        return nowNanos >= nextRefreshNanos;
    }

    private DroneArea resolveStaticArea(DroneProgramGraph graph, DroneProgramNode node, int depth) {
        if (depth > 16) return null;
        net.minecraft.nbt.NBTTagCompound config = node.getConfiguration();
        ResourceLocation type = node.getType();
        if (type.equals(DrTechDroneNodes.AREA)) {
            return DroneArea.between(new BlockPos(config.getInteger("X1"), config.getInteger("Y1"),
                    config.getInteger("Z1")), new BlockPos(config.getInteger("X2"), config.getInteger("Y2"),
                    config.getInteger("Z2")));
        }
        if (type.equals(DrTechDroneNodes.AREA_FROM_CORNERS)) {
            BlockPos first = resolveStaticCoordinateInput(graph, node, "first", depth + 1);
            BlockPos second = resolveStaticCoordinateInput(graph, node, "second", depth + 1);
            return first == null || second == null ? null : DroneArea.between(first, second);
        }
        if (type.equals(DrTechDroneNodes.SPHERE_AREA)) {
            BlockPos center = resolveStaticCoordinateInput(graph, node, "center", depth + 1);
            Number radius = resolveStaticNumberInput(graph, node, "radius", depth + 1);
            int value = radius == null ? config.getInteger("Radius") : radius.intValue();
            return center == null ? null : DroneArea.sphere(center, Math.max(1, Math.min(9, value)),
                    config.getBoolean("Hollow"));
        }
        if (type.equals(DrTechDroneNodes.CYLINDER_AREA)) {
            BlockPos center = resolveStaticCoordinateInput(graph, node, "center", depth + 1);
            Number radius = resolveStaticNumberInput(graph, node, "radius", depth + 1);
            Number height = resolveStaticNumberInput(graph, node, "height", depth + 1);
            int r = radius == null ? config.getInteger("Radius") : radius.intValue();
            int h = height == null ? config.getInteger("Height") : height.intValue();
            return center == null ? null : DroneArea.cylinder(center, Math.max(1, Math.min(8, r)),
                    Math.max(1, Math.min(16, h)), config.getBoolean("Hollow"));
        }
        if (type.equals(DrTechDroneNodes.PATH_AREA)) {
            BlockPos first = resolveStaticCoordinateInput(graph, node, "first", depth + 1);
            BlockPos second = resolveStaticCoordinateInput(graph, node, "second", depth + 1);
            Number radius = resolveStaticNumberInput(graph, node, "radius", depth + 1);
            int value = radius == null ? config.getInteger("Radius") : radius.intValue();
            return first == null || second == null ? null
                    : DroneArea.path(first, second, Math.max(0, Math.min(3, value)));
        }
        if (type.equals(DrTechDroneNodes.PLANE_AREA)) {
            BlockPos origin = resolveStaticCoordinateInput(graph, node, "origin", depth + 1);
            BlockPos first = resolveStaticCoordinateInput(graph, node, "first", depth + 1);
            BlockPos second = resolveStaticCoordinateInput(graph, node, "second", depth + 1);
            return origin == null || first == null || second == null ? null : DroneArea.plane(origin, first, second);
        }
        DroneArea first = resolveStaticAreaInput(graph, node, "first", depth + 1);
        DroneArea second = resolveStaticAreaInput(graph, node, "second", depth + 1);
        if (type.equals(DrTechDroneNodes.AREA_UNION)) return first == null || second == null ? null : first.union(second);
        if (type.equals(DrTechDroneNodes.AREA_INTERSECTION)) {
            return first == null || second == null ? null : first.intersection(second);
        }
        if (type.equals(DrTechDroneNodes.AREA_DIFFERENCE)) {
            return first == null || second == null ? null : first.difference(second);
        }
        DroneArea input = resolveStaticAreaInput(graph, node, "area", depth + 1);
        if (type.equals(DrTechDroneNodes.AREA_OFFSET)) {
            if (input == null) return null;
            Number x = resolveStaticNumberInput(graph, node, "x", depth + 1);
            Number y = resolveStaticNumberInput(graph, node, "y", depth + 1);
            Number z = resolveStaticNumberInput(graph, node, "z", depth + 1);
            return input.offset(x == null ? config.getInteger("X") : x.intValue(),
                    y == null ? config.getInteger("Y") : y.intValue(),
                    z == null ? config.getInteger("Z") : z.intValue());
        }
        if (type.equals(DrTechDroneNodes.AREA_EXPAND) || type.equals(DrTechDroneNodes.AREA_INSET)) {
            if (input == null) return null;
            Number radius = resolveStaticNumberInput(graph, node, "radius", depth + 1);
            int value = Math.max(0, Math.min(4, radius == null ? config.getInteger("Radius") : radius.intValue()));
            return type.equals(DrTechDroneNodes.AREA_EXPAND) ? input.expand(value) : input.inset(value);
        }
        return null;
    }

    private DroneArea resolveStaticAreaInput(DroneProgramGraph graph, DroneProgramNode target, String port,
            int depth) {
        DroneProgramNode source = sourceNodeForInput(graph, target, port);
        return source == null ? null : resolveStaticArea(graph, source, depth);
    }

    private BlockPos resolveStaticCoordinateInput(DroneProgramGraph graph, DroneProgramNode target, String port,
            int depth) {
        DroneProgramNode source = sourceNodeForInput(graph, target, port);
        if (source == null || depth > 16) return null;
        net.minecraft.nbt.NBTTagCompound config = source.getConfiguration();
        if (source.getType().equals(DrTechDroneNodes.COORDINATE)) {
            return new BlockPos(config.getInteger("X"), config.getInteger("Y"), config.getInteger("Z"));
        }
        if (source.getType().equals(DrTechDroneNodes.COORDINATE_OFFSET)) {
            BlockPos base = resolveStaticCoordinateInput(graph, source, "base", depth + 1);
            if (base == null) return null;
            Number x = resolveStaticNumberInput(graph, source, "x", depth + 1);
            Number y = resolveStaticNumberInput(graph, source, "y", depth + 1);
            Number z = resolveStaticNumberInput(graph, source, "z", depth + 1);
            return base.add(x == null ? config.getInteger("X") : x.intValue(),
                    y == null ? config.getInteger("Y") : y.intValue(),
                    z == null ? config.getInteger("Z") : z.intValue());
        }
        throw new PreviewUnavailableException();
    }

    private Number resolveStaticNumberInput(DroneProgramGraph graph, DroneProgramNode target, String port,
            int depth) {
        DroneProgramNode source = sourceNodeForInput(graph, target, port);
        if (source == null) return null;
        if (depth > 16 || !source.getType().equals(DrTechDroneNodes.NUMBER)) {
            throw new PreviewUnavailableException();
        }
        return source.getConfiguration().getDouble("Value");
    }

    private static DroneProgramNode sourceNodeForInput(DroneProgramGraph graph, DroneProgramNode target, String port) {
        for (DroneProgramEdge edge : graph.getEdges()) {
            if (edge.getTargetNodeId().equals(target.getId()) && edge.getTargetPortId().equals(port)) {
                return graph.getNode(edge.getSourceNodeId());
            }
        }
        return null;
    }

    private static boolean isAreaOutputNode(ResourceLocation type) {
        return type.equals(DrTechDroneNodes.AREA) || type.equals(DrTechDroneNodes.AREA_FROM_CORNERS)
                || type.equals(DrTechDroneNodes.SPHERE_AREA) || type.equals(DrTechDroneNodes.CYLINDER_AREA)
                || type.equals(DrTechDroneNodes.PATH_AREA) || type.equals(DrTechDroneNodes.PLANE_AREA)
                || type.equals(DrTechDroneNodes.AREA_UNION) || type.equals(DrTechDroneNodes.AREA_INTERSECTION)
                || type.equals(DrTechDroneNodes.AREA_DIFFERENCE) || type.equals(DrTechDroneNodes.AREA_OFFSET)
                || type.equals(DrTechDroneNodes.AREA_EXPAND) || type.equals(DrTechDroneNodes.AREA_INSET);
    }

    private static final class PreviewUnavailableException extends RuntimeException {}

    public boolean isSelectedFluidSelector() {
        SelectedProperty selected = selectedProperty();
        return selected != null && selected.property.getType() == DroneNodePropertyType.FLUID_SELECTOR;
    }

    public boolean isSelectedEntitySelector() {
        SelectedProperty selected = selectedProperty();
        return selected != null && selected.property.getType() == DroneNodePropertyType.ENTITY_SELECTOR;
    }

    public boolean isSelectedDockReference() {
        SelectedProperty selected = selectedProperty();
        return selected != null && selected.property.getType() == DroneNodePropertyType.DOCK_REFERENCE;
    }

    public UUID getSelectedDockReferenceId() {
        SelectedProperty selected = selectedProperty();
        if (selected == null || selected.property.getType() != DroneNodePropertyType.DOCK_REFERENCE) return null;
        net.minecraft.nbt.NBTTagCompound dock = selected.node.getConfiguration()
                .getCompoundTag(selected.property.getId());
        if (!dock.hasKey("DockId", 8)) return null;
        try {
            return UUID.fromString(dock.getString("DockId"));
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    public boolean isSelectedProgramReference() {
        SelectedProperty selected = selectedProperty();
        return selected != null && selected.property.getType() == DroneNodePropertyType.PROGRAM_REFERENCE;
    }

    public UUID getSelectedProgramReferenceId() {
        SelectedProperty selected = selectedProperty();
        if (selected == null || selected.property.getType() != DroneNodePropertyType.PROGRAM_REFERENCE) return null;
        net.minecraft.nbt.NBTTagCompound program = selected.node.getConfiguration()
                .getCompoundTag(selected.property.getId());
        if (!program.hasKey("ProgramId", 8)) return null;
        try {
            return UUID.fromString(program.getString("ProgramId"));
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    public long getSelectedProgramReferenceRevision() {
        SelectedProperty selected = selectedProperty();
        if (selected == null || selected.property.getType() != DroneNodePropertyType.PROGRAM_REFERENCE) return -1L;
        net.minecraft.nbt.NBTTagCompound program = selected.node.getConfiguration()
                .getCompoundTag(selected.property.getId());
        return program.hasKey("Revision", 99) ? program.getLong("Revision") : -1L;
    }

    public void selectProgramReference(UUID programId, String name, long revision) {
        SelectedProperty selected = selectedProperty();
        if (selected == null || selected.property.getType() != DroneNodePropertyType.PROGRAM_REFERENCE
                || programId == null || revision < 0L) return;
        configureSelected((node, config) -> {
            net.minecraft.nbt.NBTTagCompound program = new net.minecraft.nbt.NBTTagCompound();
            program.setString("ProgramId", programId.toString());
            program.setString("Name", name == null ? "" : name);
            program.setLong("Revision", revision);
            config.setTag(selected.property.getId(), program);
        });
        clearPropertyDraft();
    }

    /** Stores both the stable dock id and a coordinate snapshot used by the runtime value evaluator. */
    public void selectDockReference(UUID dockId, String name, int dimension, BlockPos position) {
        SelectedProperty selected = selectedProperty();
        if (selected == null || selected.property.getType() != DroneNodePropertyType.DOCK_REFERENCE
                || dockId == null || position == null) return;
        configureSelected((node, config) -> {
            net.minecraft.nbt.NBTTagCompound dock = new net.minecraft.nbt.NBTTagCompound();
            dock.setString("DockId", dockId.toString());
            dock.setString("Name", name == null ? "" : name);
            dock.setInteger("Dimension", dimension);
            dock.setLong("Position", position.toLong());
            config.setTag(selected.property.getId(), dock);
        });
        clearPropertyDraft();
    }

    /** Captures the fluid contained in the held bucket/cell without changing the held item. */
    public void captureHeldFluidProperty() {
        SelectedProperty selected = selectedProperty();
        if (selected == null || selected.property.getType() != DroneNodePropertyType.FLUID_SELECTOR
                || Minecraft.getMinecraft().player == null) return;
        ItemStack held = Minecraft.getMinecraft().player.getHeldItemMainhand();
        FluidStack contained = held.isEmpty() ? null : FluidUtil.getFluidContained(held);
        if (contained == null || contained.getFluid() == null) return;
        String fluidName = contained.getFluid().getName();
        configureSelected((node, config) -> config.setString(selected.property.getId(), fluidName));
        clearPropertyDraft();
    }

    /** Selects an exact Forge registry fluid from the programmer's searchable result list. */
    public void selectFluidProperty(String fluidName) {
        SelectedProperty selected = selectedProperty();
        if (selected == null || selected.property.getType() != DroneNodePropertyType.FLUID_SELECTOR
                || fluidName == null || FluidRegistry.getFluid(fluidName) == null) return;
        configureSelected((node, config) -> config.setString(selected.property.getId(), fluidName));
        clearPropertyDraft();
    }

    /** Appends a registry item rule. Metadata is deliberately wildcarded for searchable registry results. */
    public void selectRegistryItemProperty(String itemName) {
        SelectedProperty selected = selectedProperty();
        if (selected == null || selected.property.getType() != DroneNodePropertyType.ITEM_SELECTOR
                || itemName == null || itemName.isEmpty()) return;
        final ResourceLocation itemId;
        try {
            itemId = new ResourceLocation(itemName);
        } catch (RuntimeException ignored) {
            return;
        }
        if (!Item.REGISTRY.containsKey(itemId)) return;
        appendSelectedItemRule(selected.property,
                new DroneItemFilterSpec.Rule(itemId, -1, "", "", false, null));
    }

    /** Appends a pure Ore Dictionary rule, matching every registered stack carrying that ore name. */
    public void selectOreDictionaryProperty(String oreName) {
        SelectedProperty selected = selectedProperty();
        if (selected == null || selected.property.getType() != DroneNodePropertyType.ITEM_SELECTOR
                || oreName == null || oreName.isEmpty()
                || !OreDictionary.doesOreNameExist(oreName)) return;
        appendSelectedItemRule(selected.property,
                new DroneItemFilterSpec.Rule(null, -1, oreName, "", false, null));
    }

    public void selectEntityProperty(String entityName) {
        SelectedProperty selected = selectedProperty();
        if (selected == null || selected.property.getType() != DroneNodePropertyType.ENTITY_SELECTOR
                || entityName == null || entityName.isEmpty()) return;
        final ResourceLocation entityId;
        try {
            entityId = new ResourceLocation(entityName);
        } catch (RuntimeException ignored) {
            return;
        }
        if (!EntityList.getEntityNameList().contains(entityId)) return;
        configureSelected((node, config) -> {
            DroneEntityFilterSpec previous = readEntityFilter(config, selected.property.getId());
            List<ResourceLocation> rules = new ArrayList<>(previous.getEntityIds());
            if (rules.size() >= DroneEntityFilterSpec.MAX_RULES || rules.contains(entityId)) return;
            rules.add(entityId);
            writeEntityFilter(config, selected.property.getId(), previous.withModeAndEntityIds(previous.getMode(), rules));
        });
        clearPropertyDraft();
    }

    public void toggleSelectedEntityFilterMode() {
        SelectedProperty selected = selectedProperty();
        if (selected == null || selected.property.getType() != DroneNodePropertyType.ENTITY_SELECTOR) return;
        configureSelected((node, config) -> {
            DroneEntityFilterSpec previous = readEntityFilter(config, selected.property.getId());
            DroneFilterMode mode = previous.getMode() == DroneFilterMode.WHITELIST
                    ? DroneFilterMode.BLACKLIST : DroneFilterMode.WHITELIST;
            writeEntityFilter(config, selected.property.getId(),
                    previous.withModeAndEntityIds(mode, new ArrayList<>(previous.getEntityIds())));
        });
    }

    public String getSelectedEntityFilterModeLabel() {
        SelectedProperty selected = selectedProperty();
        if (selected == null || selected.property.getType() != DroneNodePropertyType.ENTITY_SELECTOR) return "";
        DroneEntityFilterSpec spec = readEntityFilter(selected.node.getConfiguration(), selected.property.getId());
        return I18n.format(spec.getMode() == DroneFilterMode.WHITELIST
                ? "drtech.drone.programmer.whitelist"
                : "drtech.drone.programmer.blacklist");
    }

    public void removeLastSelectedEntityFilterRule() {
        SelectedProperty selected = selectedProperty();
        if (selected == null || selected.property.getType() != DroneNodePropertyType.ENTITY_SELECTOR) return;
        configureSelected((node, config) -> {
            DroneEntityFilterSpec previous = readEntityFilter(config, selected.property.getId());
            List<ResourceLocation> rules = new ArrayList<>(previous.getEntityIds());
            if (!rules.isEmpty()) rules.remove(rules.size() - 1);
            writeEntityFilter(config, selected.property.getId(), previous.withModeAndEntityIds(previous.getMode(), rules));
        });
    }

    public void cycleSelectedEntityAdvancedField(int delta) {
        selectedEntityAdvancedField = Math.floorMod(selectedEntityAdvancedField + delta, 10);
    }

    public String getSelectedEntityAdvancedLabel() {
        SelectedProperty selected = selectedProperty();
        if (selected == null || selected.property.getType() != DroneNodePropertyType.ENTITY_SELECTOR) return "";
        DroneEntityFilterSpec spec = readEntityFilter(selected.node.getConfiguration(), selected.property.getId());
        String field = I18n.format("drtech.drone.entity_filter.field." + entityAdvancedFieldId());
        String value;
        switch (selectedEntityAdvancedField) {
            case 0: value = spec.getNames().isEmpty() ? "*" : spec.getNames().get(0); break;
            case 1: value = spec.getEntityUuid() == null ? "*" : spec.getEntityUuid().toString(); break;
            case 2: value = spec.getOwnerUuid() == null ? "*" : spec.getOwnerUuid().toString(); break;
            case 3: value = triStateLabel(spec.getAnimals()); break;
            case 4: value = triStateLabel(spec.getMonsters()); break;
            case 5: value = triStateLabel(spec.getAdult()); break;
            case 6: value = String.valueOf(spec.getMinHealth()); break;
            case 7: value = spec.getMaxHealth() == Float.MAX_VALUE ? "∞" : String.valueOf(spec.getMaxHealth()); break;
            case 8: value = booleanLabel(spec.isAllowBosses()); break;
            default: value = booleanLabel(spec.isAllowTransport()); break;
        }
        if (value.length() > 18) value = value.substring(0, 17) + "…";
        return field + ": " + value;
    }

    public void adjustSelectedEntityAdvancedValue(int delta) {
        mutateSelectedEntityAdvanced(spec -> {
            Boolean animals = spec.getAnimals();
            Boolean monsters = spec.getMonsters();
            Boolean adult = spec.getAdult();
            float minHealth = spec.getMinHealth();
            float maxHealth = spec.getMaxHealth();
            boolean allowBosses = spec.isAllowBosses();
            boolean allowTransport = spec.isAllowTransport();
            if (selectedEntityAdvancedField == 3) animals = cycleTriState(animals, delta);
            else if (selectedEntityAdvancedField == 4) monsters = cycleTriState(monsters, delta);
            else if (selectedEntityAdvancedField == 5) adult = cycleTriState(adult, delta);
            else if (selectedEntityAdvancedField == 6) minHealth = Math.max(0F, minHealth + delta);
            else if (selectedEntityAdvancedField == 7) {
                float base = maxHealth == Float.MAX_VALUE ? Math.max(minHealth, 20F) : maxHealth;
                maxHealth = Math.max(minHealth, base + delta);
            } else if (selectedEntityAdvancedField == 8) allowBosses = !allowBosses;
            else if (selectedEntityAdvancedField == 9) allowTransport = !allowTransport;
            return spec.withAdvanced(spec.getNames(), spec.getEntityUuid(), spec.getOwnerUuid(),
                    animals, monsters, adult, minHealth, maxHealth, allowBosses, allowTransport);
        });
    }

    public void applySelectedEntityAdvancedText(String text) {
        final String checked = text == null ? "" : text.trim();
        mutateSelectedEntityAdvanced(spec -> {
            List<String> names = spec.getNames();
            UUID entityUuid = spec.getEntityUuid();
            UUID ownerUuid = spec.getOwnerUuid();
            float minHealth = spec.getMinHealth();
            float maxHealth = spec.getMaxHealth();
            try {
                if (selectedEntityAdvancedField == 0) names = checked.isEmpty()
                        ? Collections.emptyList() : Collections.singletonList(checked);
                else if (selectedEntityAdvancedField == 1) entityUuid = checked.isEmpty() ? null : UUID.fromString(checked);
                else if (selectedEntityAdvancedField == 2) ownerUuid = checked.isEmpty() ? null : UUID.fromString(checked);
                else if (selectedEntityAdvancedField == 6) {
                    float parsed = Float.parseFloat(checked);
                    if (!Float.isFinite(parsed)) return spec;
                    minHealth = Math.max(0F, Math.min(1_000_000F, parsed));
                } else if (selectedEntityAdvancedField == 7) {
                    float parsed = Float.parseFloat(checked);
                    if (!Float.isFinite(parsed)) return spec;
                    maxHealth = Math.max(minHealth, Math.min(1_000_000F, parsed));
                }
            } catch (IllegalArgumentException ignored) {
                return spec;
            }
            return spec.withAdvanced(names, entityUuid, ownerUuid, spec.getAnimals(), spec.getMonsters(),
                    spec.getAdult(), minHealth, maxHealth, spec.isAllowBosses(), spec.isAllowTransport());
        });
    }

    public void clearSelectedEntityAdvancedValue() {
        mutateSelectedEntityAdvanced(spec -> {
            List<String> names = selectedEntityAdvancedField == 0 ? Collections.emptyList() : spec.getNames();
            UUID entityUuid = selectedEntityAdvancedField == 1 ? null : spec.getEntityUuid();
            UUID ownerUuid = selectedEntityAdvancedField == 2 ? null : spec.getOwnerUuid();
            Boolean animals = selectedEntityAdvancedField == 3 ? null : spec.getAnimals();
            Boolean monsters = selectedEntityAdvancedField == 4 ? null : spec.getMonsters();
            Boolean adult = selectedEntityAdvancedField == 5 ? null : spec.getAdult();
            float minHealth = selectedEntityAdvancedField == 6 ? 0F : spec.getMinHealth();
            float maxHealth = selectedEntityAdvancedField == 7 ? Float.MAX_VALUE : spec.getMaxHealth();
            boolean bosses = selectedEntityAdvancedField == 8 ? false : spec.isAllowBosses();
            boolean transport = selectedEntityAdvancedField == 9 ? false : spec.isAllowTransport();
            return spec.withAdvanced(names, entityUuid, ownerUuid, animals, monsters, adult,
                    minHealth, maxHealth, bosses, transport);
        });
    }

    private void mutateSelectedEntityAdvanced(java.util.function.UnaryOperator<DroneEntityFilterSpec> mutation) {
        SelectedProperty selected = selectedProperty();
        if (selected == null || selected.property.getType() != DroneNodePropertyType.ENTITY_SELECTOR) return;
        configureSelected((node, config) -> {
            DroneEntityFilterSpec previous = readEntityFilter(config, selected.property.getId());
            writeEntityFilter(config, selected.property.getId(), mutation.apply(previous));
        });
    }

    private String entityAdvancedFieldId() {
        return new String[] {"name", "uuid", "owner_uuid", "animals", "monsters", "adult",
                "min_health", "max_health", "bosses", "transport"}[selectedEntityAdvancedField];
    }

    private static Boolean cycleTriState(Boolean value, int delta) {
        int index = value == null ? 0 : value ? 1 : 2;
        int next = Math.floorMod(index + (delta < 0 ? -1 : 1), 3);
        return next == 0 ? null : next == 1;
    }

    private static String triStateLabel(Boolean value) {
        return value == null ? I18n.format("drtech.drone.entity_filter.any") : booleanLabel(value);
    }

    private static String booleanLabel(boolean value) {
        return I18n.format(value ? "drtech.drone.entity_filter.yes" : "drtech.drone.entity_filter.no");
    }

    private void appendSelectedItemRule(DroneNodePropertyDefinition property, DroneItemFilterSpec.Rule rule) {
        configureSelected((node, config) -> {
            DroneItemFilterSpec previous = readItemFilter(config, property.getId());
            List<DroneItemFilterSpec.Rule> rules = new ArrayList<>(previous.getRules());
            if (rules.size() >= DroneItemFilterSpec.MAX_RULES) return;
            rules.add(rule);
            writeItemFilter(config, property.getId(), new DroneItemFilterSpec(previous.getMode(), rules));
        });
        clearPropertyDraft();
    }

    public boolean canActivateSelectedProperty() {
        SelectedProperty selected = selectedProperty();
        if (selected == null) return false;
        DroneNodePropertyType type = selected.property.getType();
        if (isSelectedPropertyTextEditable()) return isPropertyDraftValid(selected);
        return type == DroneNodePropertyType.BOOLEAN || type == DroneNodePropertyType.ENUM
                || type == DroneNodePropertyType.DIRECTION || type == DroneNodePropertyType.ITEM_SELECTOR
                || type == DroneNodePropertyType.BLOCK_SELECTOR;
    }

    public String getSelectedPropertyActionLabel() {
        SelectedProperty selected = selectedProperty();
        if (selected == null) return I18n.format("drtech.drone.programmer.apply");
        switch (selected.property.getType()) {
            case BOOLEAN: return I18n.format("drtech.drone.programmer.toggle");
            case ENUM:
            case DIRECTION: return I18n.format("drtech.drone.programmer.next");
            case ITEM_SELECTOR: return I18n.format("drtech.drone.programmer.held_item");
            case BLOCK_SELECTOR: return I18n.format("drtech.drone.programmer.target_block");
            default: return I18n.format("drtech.drone.programmer.apply");
        }
    }

    public void adjustSelectedProperty(int delta) {
        SelectedProperty selected = selectedProperty();
        if (selected == null || !isSelectedPropertyNumeric()) return;
        clearPropertyDraft();
        String id = selected.property.getId();
        configureSelected((node, config) -> {
            double current = config.hasKey(id, 99) ? config.getDouble(id) : 0.0D;
            double value = Math.max(selected.property.getMinimum(),
                    Math.min(selected.property.getMaximum(), current + delta));
            if (selected.property.getType() == DroneNodePropertyType.INTEGER) config.setInteger(id, (int) value);
            else config.setDouble(id, value);
        });
    }

    public void activateSelectedProperty() {
        SelectedProperty selected = selectedProperty();
        if (selected == null) return;
        if (isSelectedPropertyTextEditable()) {
            commitSelectedPropertyText();
            return;
        }
        String id = selected.property.getId();
        switch (selected.property.getType()) {
            case BOOLEAN:
                configureSelected((node, config) -> config.setBoolean(id, !config.getBoolean(id)));
                break;
            case ENUM:
            case DIRECTION:
                configureSelected((node, config) -> {
                    List<String> values = new ArrayList<>();
                    for (String value : selected.property.getAllowedValues()) if (!value.isEmpty()) values.add(value);
                    if (values.isEmpty()) return;
                    int index = values.indexOf(config.getString(id));
                    config.setString(id, values.get((index + 1) % values.size()));
                });
                break;
            case ITEM_SELECTOR:
                captureHeldItemProperty(selected.property);
                break;
            case BLOCK_SELECTOR:
                captureTargetedBlockProperty(selected.property);
                break;
            default:
                break;
        }
    }

    private void captureHeldItemProperty(DroneNodePropertyDefinition property) {
        if (Minecraft.getMinecraft().player == null) return;
        ItemStack held = Minecraft.getMinecraft().player.getHeldItemMainhand();
        configureSelected((node, config) -> {
            if (held.isEmpty() || held.getItem().getRegistryName() == null) return;
            DroneItemFilterSpec previous = readItemFilter(config, property.getId());
            List<DroneItemFilterSpec.Rule> rules = new ArrayList<>(previous.getRules());
            if (rules.size() >= DroneItemFilterSpec.MAX_RULES) return;
            rules.add(new DroneItemFilterSpec.Rule(held.getItem().getRegistryName(), held.getMetadata(), "", "",
                    held.hasTagCompound(), held.hasTagCompound() ? held.getTagCompound() : null));
            writeItemFilter(config, property.getId(), new DroneItemFilterSpec(previous.getMode(), rules));
        });
    }

    public void toggleSelectedItemFilterMode() {
        SelectedProperty selected = selectedProperty();
        if (selected == null || !isSelectedItemFilter()) return;
        configureSelected((node, config) -> {
            if (selected.property.getType() == DroneNodePropertyType.ITEM_SELECTOR) {
                DroneItemFilterSpec previous = readItemFilter(config, selected.property.getId());
                DroneFilterMode mode = previous.getMode() == DroneFilterMode.WHITELIST
                        ? DroneFilterMode.BLACKLIST : DroneFilterMode.WHITELIST;
                writeItemFilter(config, selected.property.getId(), new DroneItemFilterSpec(mode, previous.getRules()));
            } else {
                DroneBlockFilterSpec previous = readBlockFilter(config, selected.property.getId());
                DroneFilterMode mode = previous.getMode() == DroneFilterMode.WHITELIST
                        ? DroneFilterMode.BLACKLIST : DroneFilterMode.WHITELIST;
                writeBlockFilter(config, selected.property.getId(),
                        new DroneBlockFilterSpec(mode, previous.getRules()));
            }
        });
    }

    public String getSelectedBlockFilterModeLabel() {
        SelectedProperty selected = selectedProperty();
        if (selected == null || selected.property.getType() != DroneNodePropertyType.BLOCK_SELECTOR) return "";
        DroneBlockFilterSpec spec = readBlockFilter(selected.node.getConfiguration(), selected.property.getId());
        return I18n.format(spec.getMode() == DroneFilterMode.WHITELIST
                ? "drtech.drone.programmer.whitelist"
                : "drtech.drone.programmer.blacklist");
    }

    public void removeLastSelectedItemFilterRule() {
        SelectedProperty selected = selectedProperty();
        if (selected == null || !isSelectedItemFilter()) return;
        configureSelected((node, config) -> {
            if (selected.property.getType() == DroneNodePropertyType.ITEM_SELECTOR) {
                DroneItemFilterSpec previous = readItemFilter(config, selected.property.getId());
                List<DroneItemFilterSpec.Rule> rules = new ArrayList<>(previous.getRules());
                if (!rules.isEmpty()) rules.remove(rules.size() - 1);
                writeItemFilter(config, selected.property.getId(), new DroneItemFilterSpec(previous.getMode(), rules));
            } else {
                DroneBlockFilterSpec previous = readBlockFilter(config, selected.property.getId());
                List<DroneBlockFilterSpec.Rule> rules = new ArrayList<>(previous.getRules());
                if (!rules.isEmpty()) rules.remove(rules.size() - 1);
                writeBlockFilter(config, selected.property.getId(),
                        new DroneBlockFilterSpec(previous.getMode(), rules));
            }
        });
    }

    public void duplicateLastSelectedItemFilterRule() {
        SelectedProperty selected = selectedProperty();
        if (selected == null || !isSelectedItemFilter()) return;
        configureSelected((node, config) -> {
            DroneItemFilterSpec previous = readItemFilter(config, selected.property.getId());
            List<DroneItemFilterSpec.Rule> rules = new ArrayList<>(previous.getRules());
            if (rules.isEmpty() || rules.size() >= DroneItemFilterSpec.MAX_RULES) return;
            rules.add(rules.get(rules.size() - 1));
            writeItemFilter(config, selected.property.getId(), new DroneItemFilterSpec(previous.getMode(), rules));
        });
    }

    public void moveLastSelectedItemFilterRule(int direction) {
        SelectedProperty selected = selectedProperty();
        if (selected == null || !isSelectedItemFilter() || direction == 0) return;
        configureSelected((node, config) -> {
            DroneItemFilterSpec previous = readItemFilter(config, selected.property.getId());
            List<DroneItemFilterSpec.Rule> rules = new ArrayList<>(previous.getRules());
            int from = rules.size() - 1;
            int to = from + (direction < 0 ? -1 : 1);
            if (from < 0 || to < 0 || to >= rules.size()) return;
            DroneItemFilterSpec.Rule rule = rules.remove(from);
            rules.add(to, rule);
            writeItemFilter(config, selected.property.getId(), new DroneItemFilterSpec(previous.getMode(), rules));
        });
    }

    public boolean hasSelectedItemFilterRule() {
        SelectedProperty selected = selectedProperty();
        if (selected == null || selected.property.getType() != DroneNodePropertyType.ITEM_SELECTOR) return false;
        return !readItemFilter(selected.node.getConfiguration(), selected.property.getId()).getRules().isEmpty();
    }

    public String getSelectedItemFilterBoundsLabel() {
        SelectedProperty selected = selectedProperty();
        if (!hasSelectedItemFilterRule()) return I18n.format("drtech.drone.programmer.item_bounds_unset");
        DroneItemFilterSpec.Rule rule = lastItemRule(selected);
        return I18n.format("drtech.drone.programmer.item_bounds",
                formatBound(rule.getMinDurability(), rule.getMaxDurability()),
                formatBound(rule.getMinCount(), rule.getMaxCount()));
    }

    public String getSelectedItemFilterNbtLabel() {
        SelectedProperty selected = selectedProperty();
        if (!hasSelectedItemFilterRule()) return I18n.format("drtech.drone.programmer.item_nbt_unset");
        DroneItemFilterSpec.Rule rule = lastItemRule(selected);
        if (!rule.isMatchNbt()) return I18n.format("drtech.drone.programmer.item_nbt_ignored");
        return I18n.format(rule.isMatchNbtPartially()
                ? "drtech.drone.programmer.item_nbt_partial"
                : "drtech.drone.programmer.item_nbt_exact");
    }

    public String getSelectedItemFilterNamespaceLabel() {
        SelectedProperty selected = selectedProperty();
        if (!hasSelectedItemFilterRule()) return I18n.format("drtech.drone.programmer.item_namespace_unset");
        String namespace = lastItemRule(selected).getNamespace();
        return namespace.isEmpty() ? I18n.format("drtech.drone.programmer.item_namespace_any")
                : I18n.format("drtech.drone.programmer.item_namespace", namespace);
    }

    /** Sets the namespace on the last rule; an empty value clears the constraint. */
    public void setSelectedItemFilterNamespace(String namespace) {
        SelectedProperty selected = selectedProperty();
        if (selected == null || selected.property.getType() != DroneNodePropertyType.ITEM_SELECTOR) return;
        final String value = namespace == null ? "" : namespace.trim().toLowerCase(java.util.Locale.ROOT);
        if (value.length() > 64 || (!value.isEmpty() && !value.matches("[a-z0-9_.-]+"))) return;
        configureSelected((node, config) -> {
            DroneItemFilterSpec previous = readItemFilter(config, selected.property.getId());
            List<DroneItemFilterSpec.Rule> rules = new ArrayList<>(previous.getRules());
            if (rules.isEmpty()) return;
            DroneItemFilterSpec.Rule old = rules.get(rules.size() - 1);
            rules.set(rules.size() - 1, new DroneItemFilterSpec.Rule(old.getItemId(), old.getMetadata(),
                    old.getOreDictionary(), value, old.isMatchNbt(), old.isMatchNbtPartially(), old.getNbt(),
                    old.getMinDurability(), old.getMaxDurability(), old.getMinCount(), old.getMaxCount()));
            writeItemFilter(config, selected.property.getId(), new DroneItemFilterSpec(previous.getMode(), rules));
        });
    }

    public void toggleSelectedItemFilterNbtMode() {
        SelectedProperty selected = selectedProperty();
        if (selected == null || selected.property.getType() != DroneNodePropertyType.ITEM_SELECTOR) return;
        configureSelected((node, config) -> {
            DroneItemFilterSpec previous = readItemFilter(config, selected.property.getId());
            List<DroneItemFilterSpec.Rule> rules = new ArrayList<>(previous.getRules());
            if (rules.isEmpty()) return;
            int index = rules.size() - 1;
            DroneItemFilterSpec.Rule old = rules.get(index);
            if (!old.isMatchNbt()) return;
            rules.set(index, new DroneItemFilterSpec.Rule(old.getItemId(), old.getMetadata(),
                    old.getOreDictionary(), old.getNamespace(), true, !old.isMatchNbtPartially(), old.getNbt(),
                    old.getMinDurability(), old.getMaxDurability(), old.getMinCount(), old.getMaxCount()));
            writeItemFilter(config, selected.property.getId(), new DroneItemFilterSpec(previous.getMode(), rules));
        });
    }

    /** Adjusts one bound on the last item-filter rule while preserving all other match criteria. */
    public void adjustSelectedItemFilterBound(int field, int delta) {
        SelectedProperty selected = selectedProperty();
        if (selected == null || selected.property.getType() != DroneNodePropertyType.ITEM_SELECTOR) return;
        configureSelected((node, config) -> {
            DroneItemFilterSpec previous = readItemFilter(config, selected.property.getId());
            List<DroneItemFilterSpec.Rule> rules = new ArrayList<>(previous.getRules());
            if (rules.isEmpty()) return;
            int index = rules.size() - 1;
            DroneItemFilterSpec.Rule old = rules.get(index);
            int minDurability = old.getMinDurability();
            int maxDurability = old.getMaxDurability();
            int minCount = old.getMinCount();
            int maxCount = old.getMaxCount();
            switch (field) {
                case 0: minDurability = clampBound(safeAdd(minDurability, delta), 0, maxDurability); break;
                case 1: maxDurability = clampBound(safeAdd(maxDurability, delta), minDurability, 1_000_000); break;
                case 2: minCount = clampBound(safeAdd(minCount, delta), 0, maxCount); break;
                case 3: maxCount = clampBound(safeAdd(maxCount, delta), minCount, 1_000_000); break;
                default: return;
            }
            rules.set(index, new DroneItemFilterSpec.Rule(old.getItemId(), old.getMetadata(),
                    old.getOreDictionary(), old.getNamespace(), old.isMatchNbt(), old.isMatchNbtPartially(), old.getNbt(),
                    minDurability, maxDurability, minCount, maxCount));
            writeItemFilter(config, selected.property.getId(), new DroneItemFilterSpec(previous.getMode(), rules));
        });
    }

    private static DroneItemFilterSpec.Rule lastItemRule(SelectedProperty selected) {
        List<DroneItemFilterSpec.Rule> rules = readItemFilter(selected.node.getConfiguration(),
                selected.property.getId()).getRules();
        return rules.get(rules.size() - 1);
    }

    private static int clampBound(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static int safeAdd(int value, int delta) {
        long result = (long) value + delta;
        return result > Integer.MAX_VALUE ? Integer.MAX_VALUE : result < Integer.MIN_VALUE
                ? Integer.MIN_VALUE : (int) result;
    }

    private static String formatBound(int min, int max) {
        return max >= 1_000_000 || max == Integer.MAX_VALUE ? min + "-*" : min + "-" + max;
    }

    public void clearSelectedProperty() {
        SelectedProperty selected = selectedProperty();
        if (selected == null) return;
        clearPropertyDraft();
        configureSelected((node, config) -> {
            config.removeTag(selected.property.getId());
            if (selected.property.getType() == DroneNodePropertyType.ITEM_SELECTOR) {
                config.removeTag("Item");
                config.removeTag("Meta");
            }
        });
    }

    private void captureTargetedBlockProperty(DroneNodePropertyDefinition property) {
        Minecraft minecraft = Minecraft.getMinecraft();
        RayTraceResult hit = minecraft.objectMouseOver;
        if (minecraft.world == null || hit == null || hit.typeOfHit != RayTraceResult.Type.BLOCK
                || hit.getBlockPos() == null) return;
        IBlockState state = minecraft.world.getBlockState(hit.getBlockPos());
        net.minecraft.util.ResourceLocation blockId = Block.REGISTRY.getNameForObject(state.getBlock());
        if (blockId == null) return;
        Map<String, String> capturedProperties = new LinkedHashMap<>();
        for (Map.Entry<IProperty<?>, Comparable<?>> entry : state.getProperties().entrySet()) {
            capturedProperties.put(entry.getKey().getName(), blockPropertyValueName(entry.getKey(), entry.getValue()));
        }
        configureSelected((node, config) -> {
            DroneBlockFilterSpec previous = readBlockFilter(config, property.getId());
            List<DroneBlockFilterSpec.Rule> rules = new ArrayList<>(previous.getRules());
            if (rules.size() >= DroneBlockFilterSpec.MAX_RULES) return;
            rules.add(new DroneBlockFilterSpec.Rule(blockId, -1, capturedProperties));
            writeBlockFilter(config, property.getId(), new DroneBlockFilterSpec(previous.getMode(), rules));
        });
        selectedBlockStatePropertyIndex = 0;
    }

    public boolean hasSelectedBlockStateProperty() {
        return !getSelectedBlockStateProperties().isEmpty();
    }

    public String getSelectedBlockStatePropertyLabel() {
        DroneBlockFilterSpec.Rule rule = getLastSelectedBlockRule();
        List<IProperty<?>> properties = getSelectedBlockStateProperties();
        if (rule == null) return I18n.format("drtech.drone.programmer.block_state_no_rule");
        if (properties.isEmpty()) return I18n.format("drtech.drone.programmer.block_state_none");
        IProperty<?> property = properties.get(Math.floorMod(selectedBlockStatePropertyIndex, properties.size()));
        String value = rule.getStateProperties().get(property.getName());
        return property.getName() + "=" + (value == null
                ? I18n.format("drtech.drone.programmer.block_state_any") : value);
    }

    public String getSelectedBlockNamespaceLabel() {
        DroneBlockFilterSpec.Rule rule = getLastSelectedBlockRule();
        if (rule == null) return I18n.format("drtech.drone.programmer.block_namespace_unset");
        return rule.getNamespace().isEmpty() ? I18n.format("drtech.drone.programmer.block_namespace_exact")
                : I18n.format("drtech.drone.programmer.block_namespace", rule.getNamespace());
    }

    public void generalizeLastSelectedBlockRuleToNamespace() {
        SelectedProperty selected = selectedProperty();
        DroneBlockFilterSpec.Rule current = getLastSelectedBlockRule();
        if (selected == null || current == null) return;
        String namespace = current.getNamespace();
        if (namespace.isEmpty() && current.getBlockId() != null) namespace = current.getBlockId().getNamespace();
        if (namespace.isEmpty()) return;
        final String capturedNamespace = namespace;
        configureSelected((node, config) -> {
            DroneBlockFilterSpec previous = readBlockFilter(config, selected.property.getId());
            List<DroneBlockFilterSpec.Rule> rules = new ArrayList<>(previous.getRules());
            if (rules.isEmpty()) return;
            rules.set(rules.size() - 1, new DroneBlockFilterSpec.Rule(null, capturedNamespace,
                    -1, Collections.emptyMap(), current.getTileEntityRequirement(),
                    current.getReplaceableRequirement(), current.getOreDictionary(), current.getCategory()));
            writeBlockFilter(config, selected.property.getId(), new DroneBlockFilterSpec(previous.getMode(), rules));
        });
    }

    public String getSelectedBlockTileEntityLabel() {
        DroneBlockFilterSpec.Rule rule = getLastSelectedBlockRule();
        if (rule == null || rule.getTileEntityRequirement() == null) return I18n.format("drtech.drone.programmer.block_tile_any");
        return I18n.format(rule.getTileEntityRequirement()
                ? "drtech.drone.programmer.block_tile_yes" : "drtech.drone.programmer.block_tile_no");
    }

    public void cycleSelectedBlockTileEntityRequirement() {
        SelectedProperty selected = selectedProperty();
        if (selected == null || selected.property.getType() != DroneNodePropertyType.BLOCK_SELECTOR) return;
        configureSelected((node, config) -> {
            DroneBlockFilterSpec previous = readBlockFilter(config, selected.property.getId());
            List<DroneBlockFilterSpec.Rule> rules = new ArrayList<>(previous.getRules());
            if (rules.isEmpty()) return;
            DroneBlockFilterSpec.Rule old = rules.get(rules.size() - 1);
            Boolean next = old.getTileEntityRequirement() == null ? Boolean.TRUE
                    : old.getTileEntityRequirement() ? Boolean.FALSE : null;
            rules.set(rules.size() - 1, new DroneBlockFilterSpec.Rule(old.getBlockId(), old.getNamespace(),
                    old.getMetadata(), old.getStateProperties(), next, old.getReplaceableRequirement(),
                    old.getOreDictionary(), old.getCategory()));
            writeBlockFilter(config, selected.property.getId(), new DroneBlockFilterSpec(previous.getMode(), rules));
        });
    }

    public String getSelectedBlockReplaceableLabel() {
        DroneBlockFilterSpec.Rule rule = getLastSelectedBlockRule();
        if (rule == null || rule.getReplaceableRequirement() == null) return I18n.format("drtech.drone.programmer.block_replaceable_any");
        return I18n.format(rule.getReplaceableRequirement()
                ? "drtech.drone.programmer.block_replaceable_yes" : "drtech.drone.programmer.block_replaceable_no");
    }

    public void cycleSelectedBlockReplaceableRequirement() {
        SelectedProperty selected = selectedProperty();
        if (selected == null || selected.property.getType() != DroneNodePropertyType.BLOCK_SELECTOR) return;
        configureSelected((node, config) -> {
            DroneBlockFilterSpec previous = readBlockFilter(config, selected.property.getId());
            List<DroneBlockFilterSpec.Rule> rules = new ArrayList<>(previous.getRules());
            if (rules.isEmpty()) return;
            DroneBlockFilterSpec.Rule old = rules.get(rules.size() - 1);
            Boolean next = old.getReplaceableRequirement() == null ? Boolean.TRUE
                    : old.getReplaceableRequirement() ? Boolean.FALSE : null;
            rules.set(rules.size() - 1, new DroneBlockFilterSpec.Rule(old.getBlockId(), old.getNamespace(),
                    old.getMetadata(), old.getStateProperties(), old.getTileEntityRequirement(), next,
                    old.getOreDictionary(), old.getCategory()));
            writeBlockFilter(config, selected.property.getId(), new DroneBlockFilterSpec(previous.getMode(), rules));
        });
    }

    public String getSelectedBlockOreLabel() {
        DroneBlockFilterSpec.Rule rule = getLastSelectedBlockRule();
        return rule == null || rule.getOreDictionary().isEmpty()
                ? I18n.format("drtech.drone.programmer.block_ore_any")
                : I18n.format("drtech.drone.programmer.block_ore", rule.getOreDictionary());
    }

    public void cycleSelectedBlockOreDictionary() {
        SelectedProperty selected = selectedProperty();
        DroneBlockFilterSpec.Rule current = getLastSelectedBlockRule();
        if (selected == null || current == null || current.getBlockId() == null) return;
        Block block = Block.REGISTRY.getObject(current.getBlockId());
        Item item = Item.getItemFromBlock(block);
        if (item == null) return;
        List<String> ores = new ArrayList<>();
        ores.add("");
        int meta = current.getMetadata() < 0 ? 0 : current.getMetadata();
        for (int oreId : OreDictionary.getOreIDs(new ItemStack(item, 1, meta))) {
            String ore = OreDictionary.getOreName(oreId);
            if (!ore.isEmpty() && !ores.contains(ore)) ores.add(ore);
        }
        int index = ores.indexOf(current.getOreDictionary());
        String next = ores.get((Math.max(-1, index) + 1) % ores.size());
        configureSelected((node, config) -> {
            DroneBlockFilterSpec previous = readBlockFilter(config, selected.property.getId());
            List<DroneBlockFilterSpec.Rule> rules = new ArrayList<>(previous.getRules());
            if (rules.isEmpty()) return;
            DroneBlockFilterSpec.Rule old = rules.get(rules.size() - 1);
            rules.set(rules.size() - 1, new DroneBlockFilterSpec.Rule(old.getBlockId(), old.getNamespace(),
                    old.getMetadata(), old.getStateProperties(), old.getTileEntityRequirement(),
                    old.getReplaceableRequirement(), next, old.getCategory()));
            writeBlockFilter(config, selected.property.getId(), new DroneBlockFilterSpec(previous.getMode(), rules));
        });
    }

    public String getSelectedBlockCategoryLabel() {
        DroneBlockFilterSpec.Rule rule = getLastSelectedBlockRule();
        String category = rule == null ? "" : rule.getCategory();
        return I18n.format("drtech.drone.programmer.block_category."
                + (category.isEmpty() ? "any" : category.toLowerCase(java.util.Locale.ROOT)));
    }

    public void cycleSelectedBlockCategory() {
        SelectedProperty selected = selectedProperty();
        if (selected == null || selected.property.getType() != DroneNodePropertyType.BLOCK_SELECTOR) return;
        configureSelected((node, config) -> {
            DroneBlockFilterSpec previous = readBlockFilter(config, selected.property.getId());
            List<DroneBlockFilterSpec.Rule> rules = new ArrayList<>(previous.getRules());
            if (rules.isEmpty()) return;
            DroneBlockFilterSpec.Rule old = rules.get(rules.size() - 1);
            List<String> categories = Arrays.asList("ORE", "WOOD", "CROP");
            int index = categories.indexOf(old.getCategory());
            String next = categories.get(index < 0 ? 0 : (index + 1) % categories.size());
            rules.set(rules.size() - 1, new DroneBlockFilterSpec.Rule(null, "", -1,
                    Collections.emptyMap(), old.getTileEntityRequirement(), old.getReplaceableRequirement(), "", next));
            writeBlockFilter(config, selected.property.getId(), new DroneBlockFilterSpec(previous.getMode(), rules));
        });
    }

    public String getSelectedBlockStatePropertyPage() {
        List<IProperty<?>> properties = getSelectedBlockStateProperties();
        return properties.isEmpty() ? "0/0"
                : (Math.floorMod(selectedBlockStatePropertyIndex, properties.size()) + 1) + "/" + properties.size();
    }

    public void moveSelectedBlockStateProperty(int delta) {
        List<IProperty<?>> properties = getSelectedBlockStateProperties();
        if (!properties.isEmpty()) {
            selectedBlockStatePropertyIndex = Math.floorMod(selectedBlockStatePropertyIndex + delta,
                    properties.size());
        }
    }

    public void cycleSelectedBlockStatePropertyValue() {
        SelectedProperty selected = selectedProperty();
        DroneBlockFilterSpec.Rule rule = getLastSelectedBlockRule();
        List<IProperty<?>> properties = getSelectedBlockStateProperties();
        if (selected == null || rule == null || properties.isEmpty()) return;
        IProperty<?> property = properties.get(Math.floorMod(selectedBlockStatePropertyIndex, properties.size()));
        List<String> values = new ArrayList<>();
        values.add("");
        for (Comparable<?> value : property.getAllowedValues()) {
            values.add(blockPropertyValueName(property, value));
        }
        String current = rule.getStateProperties().getOrDefault(property.getName(), "");
        String next = values.get((values.indexOf(current) + 1) % values.size());
        configureSelected((node, config) -> {
            DroneBlockFilterSpec previous = readBlockFilter(config, selected.property.getId());
            List<DroneBlockFilterSpec.Rule> rules = new ArrayList<>(previous.getRules());
            if (rules.isEmpty()) return;
            DroneBlockFilterSpec.Rule last = rules.remove(rules.size() - 1);
            Map<String, String> stateProperties = new LinkedHashMap<>(last.getStateProperties());
            if (next.isEmpty()) stateProperties.remove(property.getName());
            else stateProperties.put(property.getName(), next);
            rules.add(new DroneBlockFilterSpec.Rule(last.getBlockId(), last.getNamespace(),
                    last.getMetadata(), stateProperties, last.getTileEntityRequirement(),
                    last.getReplaceableRequirement(), last.getOreDictionary(), last.getCategory()));
            writeBlockFilter(config, selected.property.getId(), new DroneBlockFilterSpec(previous.getMode(), rules));
        });
    }

    private DroneBlockFilterSpec.Rule getLastSelectedBlockRule() {
        SelectedProperty selected = selectedProperty();
        if (selected == null || selected.property.getType() != DroneNodePropertyType.BLOCK_SELECTOR) return null;
        List<DroneBlockFilterSpec.Rule> rules = readBlockFilter(selected.node.getConfiguration(),
                selected.property.getId()).getRules();
        return rules.isEmpty() ? null : rules.get(rules.size() - 1);
    }

    private List<IProperty<?>> getSelectedBlockStateProperties() {
        DroneBlockFilterSpec.Rule rule = getLastSelectedBlockRule();
        if (rule == null) return Collections.emptyList();
        Block block = Block.REGISTRY.getObject(rule.getBlockId());
        if (block == null) return Collections.emptyList();
        List<IProperty<?>> properties = new ArrayList<>(block.getBlockState().getProperties());
        properties.sort(java.util.Comparator.comparing(IProperty::getName));
        return properties;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static String blockPropertyValueName(IProperty property, Comparable value) {
        return property.getName(value);
    }

    public String getSelectedPropertyInputText() {
        SelectedProperty selected = selectedProperty();
        if (selected == null || !isSelectedPropertyTextEditable()) return "";
        ensurePropertyDraft(selected);
        return propertyDraft;
    }

    public void setSelectedPropertyInputText(String value) {
        SelectedProperty selected = selectedProperty();
        if (selected == null || !isSelectedPropertyTextEditable() || value == null
                || value.length() > Math.max(128, selected.property.getMaxLength())) return;
        ensurePropertyDraft(selected);
        propertyDraft = value;
        propertyDraftDirty = true;
    }

    /** Applies an exact typed number or string in one revision-checked server command. */
    private void commitSelectedPropertyText() {
        SelectedProperty selected = selectedProperty();
        if (selected == null || !isSelectedPropertyTextEditable() || !isPropertyDraftValid(selected)) return;
        ensurePropertyDraft(selected);
        String value = propertyDraft;
        String id = selected.property.getId();
        DroneNodePropertyType type = selected.property.getType();
        configureSelected((node, config) -> {
            if (type == DroneNodePropertyType.INTEGER) config.setInteger(id, Integer.parseInt(value.trim()));
            else if (type == DroneNodePropertyType.NUMBER) config.setDouble(id, Double.parseDouble(value.trim()));
            else config.setString(id, value);
        });
        propertyDraftDirty = false;
    }

    private boolean isPropertyDraftValid(SelectedProperty selected) {
        ensurePropertyDraft(selected);
        String value = propertyDraft == null ? "" : propertyDraft.trim();
        try {
            if (selected.property.getType() == DroneNodePropertyType.INTEGER) {
                int number = Integer.parseInt(value);
                return number >= selected.property.getMinimum() && number <= selected.property.getMaximum();
            }
            if (selected.property.getType() == DroneNodePropertyType.NUMBER) {
                double number = Double.parseDouble(value);
                return Double.isFinite(number) && number >= selected.property.getMinimum()
                        && number <= selected.property.getMaximum();
            }
            if (selected.property.getType() == DroneNodePropertyType.FLUID_SELECTOR) {
                return value.isEmpty() || FluidRegistry.getFluid(value) != null;
            }
            return propertyDraft.length() <= selected.property.getMaxLength();
        } catch (NumberFormatException ignored) {
            return false;
        }
    }

    private void ensurePropertyDraft(SelectedProperty selected) {
        if (selected.node.getId().equals(propertyDraftNodeId) && selected.property.getId().equals(propertyDraftId)) return;
        propertyDraftNodeId = selected.node.getId();
        propertyDraftId = selected.property.getId();
        propertyDraftDirty = false;
        net.minecraft.nbt.NBTTagCompound config = selected.node.getConfiguration();
        if (selected.property.getType() == DroneNodePropertyType.INTEGER) {
            propertyDraft = Integer.toString(config.getInteger(propertyDraftId));
        } else if (selected.property.getType() == DroneNodePropertyType.NUMBER) {
            propertyDraft = Double.toString(config.getDouble(propertyDraftId));
        } else {
            propertyDraft = config.getString(propertyDraftId);
        }
    }

    private void clearPropertyDraft() {
        propertyDraftNodeId = null;
        propertyDraftId = "";
        propertyDraft = "";
        propertyDraftDirty = false;
    }

    private SelectedProperty selectedProperty() {
        DroneProgramGraph graph = graphSupplier.get();
        DroneProgramNode node = graph == null || selectedNodeId == null ? null : graph.getNode(selectedNodeId);
        if (node == null) return null;
        DroneNodeDefinition definition = registry.get(node.getType());
        if (definition == null || definition.getProperties().isEmpty()) return null;
        List<DroneNodePropertyDefinition> properties = new ArrayList<>(definition.getProperties());
        int index = Math.floorMod(selectedPropertyIndex, properties.size());
        return new SelectedProperty(node, properties.get(index), index, properties.size());
    }

    private static DroneItemFilterSpec readItemFilter(net.minecraft.nbt.NBTTagCompound config, String propertyId) {
        if (config.hasKey(propertyId, 10)) {
            return DroneItemFilterSpec.readFromNbt(config.getCompoundTag(propertyId));
        }
        String legacyItem = config.getString("Item");
        if (legacyItem.isEmpty()) return DroneItemFilterSpec.ANY;
        try {
            return new DroneItemFilterSpec(DroneFilterMode.WHITELIST, Collections.singletonList(
                    new DroneItemFilterSpec.Rule(new net.minecraft.util.ResourceLocation(legacyItem),
                            config.hasKey("Meta", 99) ? config.getInteger("Meta") : -1, "", "", false, null)));
        } catch (RuntimeException ignored) {
            return DroneItemFilterSpec.ANY;
        }
    }

    private static void writeItemFilter(net.minecraft.nbt.NBTTagCompound config, String propertyId,
            DroneItemFilterSpec spec) {
        config.setTag(propertyId, spec.writeToNbt());
        if (spec.getRules().isEmpty() || spec.getRules().get(0).getItemId() == null) {
            config.removeTag("Item");
            config.removeTag("Meta");
        } else {
            DroneItemFilterSpec.Rule first = spec.getRules().get(0);
            config.setString("Item", first.getItemId().toString());
            config.setInteger("Meta", first.getMetadata());
        }
    }

    private static DroneBlockFilterSpec readBlockFilter(net.minecraft.nbt.NBTTagCompound config, String propertyId) {
        return config.hasKey(propertyId, 10)
                ? DroneBlockFilterSpec.readFromNbt(config.getCompoundTag(propertyId)) : DroneBlockFilterSpec.ANY;
    }

    private static void writeBlockFilter(net.minecraft.nbt.NBTTagCompound config, String propertyId,
            DroneBlockFilterSpec spec) {
        config.setTag(propertyId, spec.writeToNbt());
    }

    private static DroneEntityFilterSpec readEntityFilter(net.minecraft.nbt.NBTTagCompound config,
            String propertyId) {
        return DroneEntityFilterSpec.readFromNbt(config.hasKey(propertyId, 10)
                ? config.getCompoundTag(propertyId) : null);
    }

    private static void writeEntityFilter(net.minecraft.nbt.NBTTagCompound config, String propertyId,
            DroneEntityFilterSpec spec) {
        config.setTag(propertyId, spec.writeToNbt());
    }

    public String getSelectedDescription() {
        DroneProgramGraph graph = graphSupplier.get();
        DroneProgramNode node = graph == null || selectedNodeId == null ? null : graph.getNode(selectedNodeId);
        if (node == null) return I18n.format("drtech.drone.inspector.none_help");
        NBTTagCompoundView config = new NBTTagCompoundView(node);
        if (node.getType().equals(DrTechDroneNodes.WAIT)) {
            return I18n.format("drtech.drone.inspector.wait", config.integer("Ticks", 20));
        }
        if (node.getType().equals(DrTechDroneNodes.CHARGE_UNTIL)) {
            return I18n.format("drtech.drone.inspector.charge", config.number("Percent", 100.0D));
        }
        if (node.getType().equals(DrTechDroneNodes.NUMBER)) {
            return I18n.format("drtech.drone.inspector.number", config.number("Value", 0.0D));
        }
        if (node.getType().equals(DrTechDroneNodes.BOOLEAN)) {
            return I18n.format("drtech.drone.inspector.boolean", config.bool("Value"));
        }
        if (node.getType().equals(DrTechDroneNodes.COORDINATE)) {
            return I18n.format("drtech.drone.inspector.coordinate", config.integer("X", 0),
                    config.integer("Y", 0), config.integer("Z", 0));
        }
        if (node.getType().equals(DrTechDroneNodes.AREA)) {
            int areaCorner = selectedAreaCaptureCorner();
            DroneArea area = DroneArea.between(
                    new net.minecraft.util.math.BlockPos(config.integer("X1", 0), config.integer("Y1", 0),
                            config.integer("Z1", 0)),
                    new net.minecraft.util.math.BlockPos(config.integer("X2", 0), config.integer("Y2", 0),
                            config.integer("Z2", 0)));
            return I18n.format("drtech.drone.inspector.area", areaCorner == 1 ? "A" : "B",
                    config.integer("X" + areaCorner, 0), config.integer("Y" + areaCorner, 0),
                    config.integer("Z" + areaCorner, 0), area.getVolume());
        }
        if (node.getType().equals(DrTechDroneNodes.ITEM_FILTER)) {
            String item = config.string("Item");
            return item.isEmpty() ? I18n.format("drtech.drone.inspector.filter_any")
                    : I18n.format("drtech.drone.inspector.filter", item, config.integer("Meta", 0));
        }
        if (isVariableNode(node)) {
            String name = config.string("Name");
            return I18n.format("drtech.drone.inspector.variable", name.isEmpty() ? "value" : name);
        }
        if (node.getType().equals(DrTechDroneNodes.REPEAT)) {
            return I18n.format("drtech.drone.inspector.repeat", config.integer("Count", 3));
        }
        if (node.getType().equals(DrTechDroneNodes.WAIT_FOR_OWNER)) {
            return I18n.format("drtech.drone.inspector.owner_radius", config.number("Radius", 16.0D));
        }
        if (node.getType().equals(DrTechDroneNodes.NUMBER_MATH)
                || node.getType().equals(DrTechDroneNodes.BOOLEAN_LOGIC)
                || node.getType().equals(DrTechDroneNodes.COMPARE_NUMBER)) {
            String operator = config.string("Operator");
            return I18n.format("drtech.drone.inspector.operator", operator.isEmpty() ? defaultOperator(node) : operator);
        }
        if (node.getType().equals(DrTechDroneNodes.COORDINATE_OFFSET)) {
            return I18n.format("drtech.drone.inspector.offset", config.integer("X", 0),
                    config.integer("Y", 0), config.integer("Z", 0));
        }
        return I18n.format("drtech.drone.node." + node.getType().getPath());
    }

    public void adjustSelectedValue(int delta) {
        configureSelected((node, config) -> {
            if (node.getType().equals(DrTechDroneNodes.WAIT)) {
                int value = config.hasKey("Ticks") ? config.getInteger("Ticks") : 20;
                config.setInteger("Ticks", Math.max(1, value + delta));
            } else if (node.getType().equals(DrTechDroneNodes.CHARGE_UNTIL)) {
                double value = config.hasKey("Percent") ? config.getDouble("Percent") : 100.0D;
                config.setDouble("Percent", Math.max(1.0D, Math.min(100.0D, value + delta)));
            } else if (node.getType().equals(DrTechDroneNodes.NUMBER)) {
                config.setDouble("Value", config.getDouble("Value") + delta);
            } else if (node.getType().equals(DrTechDroneNodes.REPEAT)) {
                int value = config.hasKey("Count") ? config.getInteger("Count") : 3;
                config.setInteger("Count", Math.max(0, Math.min(1_000_000, value + delta)));
            } else if (node.getType().equals(DrTechDroneNodes.WAIT_FOR_OWNER)) {
                double value = config.hasKey("Radius") ? config.getDouble("Radius") : 16.0D;
                config.setDouble("Radius", Math.max(1.0D, Math.min(128.0D, value + delta)));
            }
        });
    }

    public void toggleSelectedBoolean() {
        configureSelected((node, config) -> {
            if (node.getType().equals(DrTechDroneNodes.BOOLEAN)) {
                config.setBoolean("Value", !config.getBoolean("Value"));
            }
        });
    }

    public void adjustSelectedCoordinate(String axis, int delta) {
        configureSelected((node, config) -> {
            if (node.getType().equals(DrTechDroneNodes.COORDINATE)) {
                config.setInteger(axis, config.getInteger(axis) + delta);
            } else if (node.getType().equals(DrTechDroneNodes.COORDINATE_OFFSET)) {
                config.setInteger(axis, config.getInteger(axis) + delta);
            } else if (node.getType().equals(DrTechDroneNodes.AREA)) {
                String key = axis + selectedAreaCaptureCorner();
                config.setInteger(key, config.getInteger(key) + delta);
            }
        });
    }

    public void toggleSelectedAreaCorner() {
        selectedAreaCorner = selectedAreaCorner == 1 ? 2 : 1;
    }

    public void toggleSelectedBreakpoint() {
        configureSelected((node, config) -> config.setBoolean("Breakpoint", !config.getBoolean("Breakpoint")));
    }

    /** Cycles the condition attached to the selected node; variable-write is available only on variable mutators. */
    public void cycleSelectedConditionalBreakpoint() {
        configureSelected((node, config) -> {
            String current = selectedConditionalBreakpoint(node, config);
            String next;
            if ("none".equals(current)) next = "failure";
            else if ("failure".equals(current)) next = "energy25";
            else if ("energy25".equals(current)) next = "energy50";
            else if ("energy50".equals(current)) next = "energy75";
            else if ("energy75".equals(current) && isVariableMutationNode(node)) next = "variable";
            else next = "none";
            config.removeTag("BreakpointOnFailure");
            config.removeTag("BreakpointLowEnergy");
            config.removeTag("BreakpointOnVariableWrite");
            if ("failure".equals(next)) config.setBoolean("BreakpointOnFailure", true);
            else if ("energy25".equals(next)) config.setInteger("BreakpointLowEnergy", 25);
            else if ("energy50".equals(next)) config.setInteger("BreakpointLowEnergy", 50);
            else if ("energy75".equals(next)) config.setInteger("BreakpointLowEnergy", 75);
            else if ("variable".equals(next)) config.setBoolean("BreakpointOnVariableWrite", true);
        });
    }

    public String getSelectedBreakpointLabel() {
        DroneProgramNode node = selectedNode();
        return I18n.format(node != null && node.getConfiguration().getBoolean("Breakpoint")
                ? "drtech.drone.programmer.breakpoint_active" : "drtech.drone.programmer.breakpoint");
    }

    public String getSelectedConditionalBreakpointLabel() {
        DroneProgramNode node = selectedNode();
        if (node == null) return I18n.format("drtech.drone.programmer.breakpoint_condition_none");
        String key = "drtech.drone.programmer.breakpoint_condition_"
                + selectedConditionalBreakpoint(node, node.getConfiguration());
        return I18n.format(key);
    }

    private static String selectedConditionalBreakpoint(DroneProgramNode node,
            net.minecraft.nbt.NBTTagCompound configuration) {
        if (configuration.getBoolean("BreakpointOnFailure")) return "failure";
        int threshold = configuration.getInteger("BreakpointLowEnergy");
        if (threshold >= 75) return "energy75";
        if (threshold >= 50) return "energy50";
        if (threshold > 0) return "energy25";
        return configuration.getBoolean("BreakpointOnVariableWrite") && isVariableMutationNode(node)
                ? "variable" : "none";
    }

    private DroneProgramNode selectedNode() {
        DroneProgramGraph graph = graphSupplier.get();
        return graph == null || selectedNodeId == null ? null : graph.getNode(selectedNodeId);
    }

    private static boolean isVariableMutationNode(DroneProgramNode node) {
        return node != null && (node.getType().equals(DrTechDroneNodes.SET_NUMBER_VARIABLE)
                || node.getType().equals(DrTechDroneNodes.ADD_NUMBER_VARIABLE));
    }

    public void cycleSelectedOperator() {
        configureSelected((node, config) -> {
            String[] operators;
            if (node.getType().equals(DrTechDroneNodes.NUMBER_MATH)) {
                operators = new String[] {"+", "-", "*", "/", "%", "min", "max"};
            } else if (node.getType().equals(DrTechDroneNodes.BOOLEAN_LOGIC)) {
                operators = new String[] {"AND", "OR", "XOR"};
            } else if (node.getType().equals(DrTechDroneNodes.COMPARE_NUMBER)) {
                operators = new String[] {"==", "!=", "<", "<=", ">", ">="};
            } else return;
            String current = config.getString("Operator");
            int index = -1;
            for (int i = 0; i < operators.length; i++) if (operators[i].equals(current)) index = i;
            config.setString("Operator", operators[(index + 1) % operators.length]);
        });
    }

    private static String defaultOperator(DroneProgramNode node) {
        if (node.getType().equals(DrTechDroneNodes.BOOLEAN_LOGIC)) return "AND";
        if (node.getType().equals(DrTechDroneNodes.COMPARE_NUMBER)) return "==";
        return "+";
    }

    public String getSelectedVariableName() {
        DroneProgramGraph graph = graphSupplier.get();
        DroneProgramNode node = graph == null || selectedNodeId == null ? null : graph.getNode(selectedNodeId);
        if (node == null || !isVariableNode(node)) return "";
        String name = node.getConfiguration().getString("Name");
        return name.isEmpty() ? "value" : name;
    }

    public void setSelectedVariableName(String name) {
        if (name == null || !name.matches("[A-Za-z_][A-Za-z0-9_]{0,23}")) return;
        configureSelected((node, config) -> {
            if (isVariableNode(node)) config.setString("Name", name);
        });
    }

    private static boolean isVariableNode(DroneProgramNode node) {
        return node.getType().equals(DrTechDroneNodes.GET_NUMBER_VARIABLE)
                || node.getType().equals(DrTechDroneNodes.SET_NUMBER_VARIABLE)
                || node.getType().equals(DrTechDroneNodes.ADD_NUMBER_VARIABLE);
    }

    public void captureHeldItemFilter() {
        configureSelected((node, config) -> {
            if (!node.getType().equals(DrTechDroneNodes.ITEM_FILTER)
                    || Minecraft.getMinecraft().player == null) return;
            ItemStack held = Minecraft.getMinecraft().player.getHeldItemMainhand();
            if (held.isEmpty() || held.getItem().getRegistryName() == null) {
                config.removeTag("Item");
                config.removeTag("Meta");
                return;
            }
            config.setString("Item", held.getItem().getRegistryName().toString());
            config.setInteger("Meta", held.getMetadata());
        });
    }

    public void clearSelectedItemFilter() {
        configureSelected((node, config) -> {
            if (!node.getType().equals(DrTechDroneNodes.ITEM_FILTER)) return;
            config.removeTag("Item");
            config.removeTag("Meta");
        });
    }

    private void configureSelected(NodeConfigurationMutation mutation) {
        DroneProgramGraph graph = graphSupplier.get();
        DroneProgramNode node = graph == null || selectedNodeId == null ? null : graph.getNode(selectedNodeId);
        if (node == null || !editableSupplier.getAsBoolean()) return;
        net.minecraft.nbt.NBTTagCompound configuration = node.getConfiguration();
        SelectedProperty selected = selectedProperty();
        String before = selected != null && selected.node.getId().equals(node.getId())
                ? compactPropertyValue(selected.property, configuration) : null;
        net.minecraft.nbt.NBTBase beforeTag = selected == null ? null
                : configuration.getTag(selected.property.getId());
        mutation.apply(node, configuration);
        if (selected != null && selected.node.getId().equals(node.getId())) {
            net.minecraft.nbt.NBTBase afterTag = configuration.getTag(selected.property.getId());
            if (!java.util.Objects.equals(beforeTag, afterTag)) {
                lastPropertyChangeNodeId = node.getId();
                lastPropertyChangeId = selected.property.getId();
                lastPropertyBefore = before;
                lastPropertyAfter = compactPropertyValue(selected.property, configuration);
            }
        }
        commandSink.accept(DroneGraphEditCommand.configureNode(graph.getRevision(), node.getId(), configuration));
    }

    private static String compactDraftValue(DroneNodePropertyDefinition property, String draft) {
        String value = draft == null ? "" : draft;
        if (property.getType() == DroneNodePropertyType.INTEGER
                || property.getType() == DroneNodePropertyType.NUMBER) value = value.trim();
        return compactText(value);
    }

    private static String compactPropertyValue(DroneNodePropertyDefinition property,
            net.minecraft.nbt.NBTTagCompound config) {
        String id = property.getId();
        if (!config.hasKey(id)) return I18n.format("drtech.drone.programmer.property_default_value");
        switch (property.getType()) {
            case INTEGER:
                return Integer.toString(config.getInteger(id));
            case NUMBER:
                return Double.toString(config.getDouble(id));
            case BOOLEAN:
                return I18n.format(config.getBoolean(id)
                        ? "drtech.drone.programmer.property_true"
                        : "drtech.drone.programmer.property_false");
            case STRING:
            case FLUID_SELECTOR:
            case ENUM:
            case DIRECTION:
                return compactText(config.getString(id));
            default:
                return I18n.format("drtech.drone.programmer.property_configured_value");
        }
    }

    private static String compactText(String value) {
        String compact = value == null ? "" : value.replace('\r', ' ').replace('\n', ' ').trim();
        if (compact.isEmpty()) return I18n.format("drtech.drone.programmer.property_empty_value");
        return compact.length() > 6 ? compact.substring(0, 5) + "..." : compact;
    }

    private DroneProgramNode findNode(DroneProgramGraph graph, int x, int y) {
        DroneProgramNode found = null;
        Set<UUID> hiddenNodes = DroneGroupLayout.hiddenByCollapsedGroups(graph);
        for (DroneProgramNode node : graph.getNodes()) {
            if (DroneGroupLayout.isGroup(node) || hiddenNodes.contains(node.getId())) continue;
            Point point = nodePoint(node);
            if (x >= point.x && x < point.x + scaled(NODE_WIDTH)
                    && y >= point.y && y < point.y + scaled(nodeHeight(registry.get(node.getType())))) found = node;
        }
        if (found != null) return found;
        for (DroneProgramNode group : graph.getNodes()) {
            if (!DroneGroupLayout.isGroup(group)) continue;
            Point point = nodePoint(group);
            if (x >= point.x && x < point.x + scaled(DroneGroupLayout.width(group))
                    && y >= point.y && y < point.y + scaled(DroneGroupLayout.HEADER_HEIGHT)) found = group;
        }
        return found;
    }

    private boolean navigateFromMiniMap(DroneProgramGraph graph, int mouseX, int mouseY) {
        MiniMapProjection projection = miniMapProjection(graph, getArea().w());
        if (projection == null || mouseX < projection.left || mouseX >= projection.left + MINIMAP_WIDTH
                || mouseY < projection.top || mouseY >= projection.top + MINIMAP_HEIGHT) return false;
        int graphX = projection.unprojectX(mouseX);
        int graphY = projection.unprojectY(mouseY);
        panX = unscale(getArea().w() / 2) - graphX;
        panY = unscale(getArea().h() / 2) - graphY;
        return true;
    }

    private MiniMapProjection miniMapProjection(DroneProgramGraph graph, int canvasWidth) {
        if (graph == null || graph.getNodes().isEmpty()) return null;
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        for (DroneProgramNode node : graph.getNodes()) {
            minX = Math.min(minX, node.getX());
            minY = Math.min(minY, node.getY());
            maxX = Math.max(maxX, node.getX() + (DroneGroupLayout.isGroup(node)
                    ? DroneGroupLayout.width(node) : NODE_WIDTH));
            maxY = Math.max(maxY, node.getY() + (DroneGroupLayout.isGroup(node)
                    ? DroneGroupLayout.height(node) : nodeHeight(registry.get(node.getType()))));
        }
        return new MiniMapProjection(canvasWidth - MINIMAP_WIDTH - 4, 4, minX, minY,
                Math.max(1, maxX - minX), Math.max(1, maxY - minY));
    }

    private PortHit findPort(DroneProgramGraph graph, int x, int y) {
        return findPort(graph, DroneGroupLayout.hiddenByCollapsedGroups(graph), x, y);
    }

    private PortHit findPort(DroneProgramGraph graph, Set<UUID> hiddenNodes, int x, int y) {
        for (DroneProgramNode node : graph.getNodes()) {
            if (hiddenNodes.contains(node.getId())) continue;
            DroneNodeDefinition definition = registry.get(node.getType());
            if (definition == null) continue;
            for (DronePortDefinition port : definition.getPorts()) {
                Point point = portPoint(node, port.getId(), port.getDirection());
                int radius = Math.max(4, scaled(PORT_SIZE));
                if (point != null && Math.abs(x - point.x) <= radius && Math.abs(y - point.y) <= radius) {
                    return new PortHit(node, port);
                }
            }
        }
        return null;
    }

    private Point portPoint(DroneProgramNode node, String portId, DronePortDirection direction) {
        if (node == null) return null;
        DroneNodeDefinition definition = registry.get(node.getType());
        if (definition == null) return null;
        int index = 0;
        for (DronePortDefinition port : definition.getPorts()) {
            if (port.getDirection() != direction) continue;
            if (port.getId().equals(portId)) {
                Point nodePoint = nodePoint(node);
                int x = direction == DronePortDirection.INPUT ? nodePoint.x : nodePoint.x + scaled(NODE_WIDTH);
                return new Point(x, nodePoint.y + scaled(PORT_START_Y + index * PORT_STEP_Y));
            }
            index++;
        }
        return null;
    }

    private static int nodeHeight(DroneNodeDefinition definition) {
        if (definition == null) return NODE_HEIGHT;
        if (definition.getId().equals(DrTechDroneNodes.COMMENT)) return 46;
        int inputs = 0;
        int outputs = 0;
        for (DronePortDefinition port : definition.getPorts()) {
            if (port.getDirection() == DronePortDirection.INPUT) inputs++;
            else outputs++;
        }
        int rows = Math.max(inputs, outputs);
        return Math.max(NODE_HEIGHT, PORT_START_Y + Math.max(0, rows - 1) * PORT_STEP_Y + 7);
    }

    private static String portLabel(String portId) {
        return I18n.format("drtech.drone.port." + portId);
    }

    private static String portTypeLabel(DronePortType type) {
        return I18n.format("drtech.drone.port.type." + type.name().toLowerCase(java.util.Locale.ROOT));
    }

    private Point nodePoint(DroneProgramNode node) {
        PreviewPosition preview = previews.get(node.getId());
        int x = preview == null ? node.getX() : preview.x;
        int y = preview == null ? node.getY() : preview.y;
        return new Point(scale(x + panX), scale(y + panY));
    }

    private boolean isNodeVisible(DroneProgramNode node) {
        Point point = nodePoint(node);
        boolean group = DroneGroupLayout.isGroup(node);
        int width = scaled(group ? DroneGroupLayout.width(node) : NODE_WIDTH);
        int height = scaled(group ? (DroneGroupLayout.isCollapsed(node)
                ? DroneGroupLayout.HEADER_HEIGHT : DroneGroupLayout.height(node))
                : nodeHeight(registry.get(node.getType())));
        return point.x + width >= 0 && point.y + height >= 0
                && point.x < getArea().w() && point.y < getArea().h();
    }

    /** Converts an absolute GUI drop position into persistent graph coordinates. */
    public int[] graphPositionAtAbsolute(int absoluteX, int absoluteY) {
        int localX = absoluteX - getArea().x;
        int localY = absoluteY - getArea().y;
        if (localX < 0 || localX >= getArea().w() || localY < 0 || localY >= getArea().h()) return null;
        return new int[] { unscale(localX) - panX, unscale(localY) - panY };
    }

    /** Releases optimistic positions only after the synced graph confirms or rejects the move. */
    private void reconcileAcknowledgedDragPreviews(DroneProgramGraph graph) {
        long now = System.currentTimeMillis();
        java.util.Iterator<Map.Entry<UUID, PreviewPosition>> iterator = previews.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, PreviewPosition> entry = iterator.next();
            PreviewPosition preview = entry.getValue();
            if (!preview.isAwaiting()) continue;
            DroneProgramNode node = graph.getNode(entry.getKey());
            if (node == null || !DroneDragPreviewPolicy.shouldKeep(graph.getRevision(), preview.sourceRevision,
                    node.getX(), node.getY(), preview.x, preview.y, now, preview.expiresAt)) {
                iterator.remove();
            }
        }
    }

    private int scale(int value) {
        return value * zoomPercent / 100;
    }

    private int scaled(int value) {
        return Math.max(1, scale(value));
    }

    private int unscale(int value) {
        return value * 100 / zoomPercent;
    }

    private static int snapCoordinate(int value) {
        return Math.round(value / 16.0F) * 16;
    }

    private static boolean isShiftDown() {
        return Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
    }

    private static int floorMod(int value, int divisor) {
        int result = value % divisor;
        return result < 0 ? result + divisor : result;
    }

    private static int categoryColor(DroneNodeDefinition definition) {
        if (definition == null) return 0xFFB94A48;
        return switch (definition.getCategory()) {
            case "flow" -> 0xFF4C91D7;
            case "movement" -> 0xFF50A67A;
            case "blocks" -> 0xFFC28B4B;
            case "items" -> 0xFFA875D1;
            case "dock" -> 0xFF43A6AA;
            case "conditions" -> 0xFFD1B54A;
            case "variables" -> 0xFF8B72D6;
            case "filters" -> 0xFFA875D1;
            case "events" -> 0xFFE07155;
            case "sensors" -> 0xFF63B7A6;
            case "math" -> 0xFF5FA9C8;
            case "thaumcraft" -> 0xFF8E55C7;
            default -> 0xFF697A8D;
        };
    }

    /** Stable category glyphs supplement the header colours for node-library and canvas readability. */
    private static String categorySymbol(DroneNodeDefinition definition) {
        if (definition == null) return "?";
        return switch (definition.getCategory()) {
            case "flow" -> "F";
            case "movement" -> ">";
            case "conditions" -> "?";
            case "values" -> "V";
            case "math" -> "+";
            case "sensors" -> "S";
            case "energy" -> "E";
            case "dock" -> "D";
            case "machines" -> "M";
            case "thaumcraft" -> "T";
            default -> "•";
        };
    }

    private static int colorForType(DronePortType type) {
        return switch (type) {
            case FLOW -> 0xFFF1F3F5;
            case BOOLEAN -> 0xFFE46D6D;
            case NUMBER -> 0xFF65C986;
            case STRING -> 0xFFE0B15A;
            case COORDINATE -> 0xFF5CB9DE;
            case AREA -> 0xFF8A7FE8;
            case ITEM_FILTER, BLOCK_FILTER, FLUID_FILTER, ENTITY_FILTER -> 0xFFC481E3;
            case DIRECTION -> 0xFFEE8B4A;
            case DOCK_REFERENCE, PROGRAM_REFERENCE -> 0xFF55D2C7;
            case ACTION_STATUS -> 0xFFFF9F43;
            case ANY_DATA -> 0xFF9BA8B7;
        };
    }

    private enum MenuAction {
        COPY("drtech.drone.context.copy"),
        PASTE("drtech.drone.context.paste"),
        DUPLICATE("drtech.drone.context.duplicate"),
        DELETE("drtech.drone.context.delete"),
        DISCONNECT("drtech.drone.context.disconnect"),
        FOCUS("drtech.drone.context.focus"),
        ALIGN_HORIZONTAL("drtech.drone.context.align_horizontal"),
        ALIGN_VERTICAL("drtech.drone.context.align_vertical"),
        AUTO_LAYOUT("drtech.drone.context.auto_layout"),
        GROUP_SELECTION("drtech.drone.context.group_selection"),
        ADD_COMMENT("drtech.drone.context.add_comment"),
        FIT_ALL("drtech.drone.context.fit_all");

        private final String translationKey;
        MenuAction(String translationKey) { this.translationKey = translationKey; }
    }

    private static final class ContextMenu {
        private final int x;
        private final int y;
        private final int graphX;
        private final int graphY;
        private final List<MenuAction> actions;

        private ContextMenu(int x, int y, int graphX, int graphY, List<MenuAction> actions) {
            this.x = x;
            this.y = y;
            this.graphX = graphX;
            this.graphY = graphY;
            this.actions = Collections.unmodifiableList(new ArrayList<>(actions));
        }
    }

    private static final class Point {
        private final int x;
        private final int y;
        private Point(int x, int y) { this.x = x; this.y = y; }
    }

    /** O(E) frame index replacing the old O(port count * edge count) connection scan. */
    private static final class PortConnectivity {
        private static final PortConnectivity EMPTY = new PortConnectivity();
        private final Map<UUID, Set<String>> inputs = new HashMap<>();
        private final Map<UUID, Set<String>> outputs = new HashMap<>();

        private static PortConnectivity index(DroneProgramGraph graph) {
            PortConnectivity index = new PortConnectivity();
            for (DroneProgramEdge edge : graph.getEdges()) {
                index.inputs.computeIfAbsent(edge.getTargetNodeId(), ignored -> new java.util.HashSet<>())
                        .add(edge.getTargetPortId());
                index.outputs.computeIfAbsent(edge.getSourceNodeId(), ignored -> new java.util.HashSet<>())
                        .add(edge.getSourcePortId());
            }
            return index;
        }

        private boolean isConnected(UUID nodeId, DronePortDefinition port) {
            Map<UUID, Set<String>> side = port.getDirection() == DronePortDirection.INPUT ? inputs : outputs;
            Set<String> connected = side.get(nodeId);
            return connected != null && connected.contains(port.getId());
        }
    }

    private static final class PreviewPosition {
        private final int x;
        private final int y;
        private final long sourceRevision;
        private final long expiresAt;

        private PreviewPosition(int x, int y) { this(x, y, -1L, 0L); }

        private PreviewPosition(int x, int y, long sourceRevision, long expiresAt) {
            this.x = x;
            this.y = y;
            this.sourceRevision = sourceRevision;
            this.expiresAt = expiresAt;
        }

        private PreviewPosition awaiting(long revision, long timeout) {
            return new PreviewPosition(x, y, revision, timeout);
        }

        private boolean isAwaiting() { return sourceRevision >= 0L; }
    }

    private static final class ClipboardGraph {
        private final List<DroneProgramNode> nodes;
        private final List<DroneProgramEdge> edges;

        private ClipboardGraph(List<DroneProgramNode> nodes, List<DroneProgramEdge> edges) {
            this.nodes = Collections.unmodifiableList(new ArrayList<>(nodes));
            this.edges = Collections.unmodifiableList(new ArrayList<>(edges));
        }
    }

    private static final class MiniMapProjection {
        private static final int PADDING = 3;
        private final int left;
        private final int top;
        private final int minX;
        private final int minY;
        private final double scale;
        private final double offsetX;
        private final double offsetY;

        private MiniMapProjection(int left, int top, int minX, int minY, int width, int height) {
            this.left = left;
            this.top = top;
            this.minX = minX;
            this.minY = minY;
            double usableWidth = MINIMAP_WIDTH - PADDING * 2.0D;
            double usableHeight = MINIMAP_HEIGHT - PADDING * 2.0D;
            this.scale = Math.min(usableWidth / width, usableHeight / height);
            this.offsetX = PADDING + (usableWidth - width * scale) / 2.0D;
            this.offsetY = PADDING + (usableHeight - height * scale) / 2.0D;
        }

        private int projectX(int x) { return left + (int) Math.round(offsetX + (x - minX) * scale); }

        private int projectY(int y) { return top + (int) Math.round(offsetY + (y - minY) * scale); }

        private int projectLength(int length) { return (int) Math.round(length * scale); }

        private int unprojectX(int x) { return minX + (int) Math.round((x - left - offsetX) / scale); }

        private int unprojectY(int y) { return minY + (int) Math.round((y - top - offsetY) / scale); }
    }

    private static final class SelectedProperty {
        private final DroneProgramNode node;
        private final DroneNodePropertyDefinition property;
        private final int index;
        private final int count;

        private SelectedProperty(DroneProgramNode node, DroneNodePropertyDefinition property, int index, int count) {
            this.node = node;
            this.property = property;
            this.index = index;
            this.count = count;
        }
    }

    private static final class PendingPort {
        private final UUID nodeId;
        private final String portId;
        private final DronePortType type;
        private PendingPort(UUID nodeId, String portId, DronePortType type) {
            this.nodeId = nodeId;
            this.portId = portId;
            this.type = type;
        }
    }

    private static final class PortHit {
        private final DroneProgramNode node;
        private final DronePortDefinition port;
        private PortHit(DroneProgramNode node, DronePortDefinition port) {
            this.node = node;
            this.port = port;
        }
    }

    @FunctionalInterface
    private interface NodeConfigurationMutation {
        void apply(DroneProgramNode node, net.minecraft.nbt.NBTTagCompound configuration);
    }

    private static final class NBTTagCompoundView {
        private final net.minecraft.nbt.NBTTagCompound tag;
        private NBTTagCompoundView(DroneProgramNode node) { this.tag = node.getConfiguration(); }
        private int integer(String key, int fallback) { return tag.hasKey(key) ? tag.getInteger(key) : fallback; }
        private double number(String key, double fallback) { return tag.hasKey(key) ? tag.getDouble(key) : fallback; }
        private boolean bool(String key) { return tag.getBoolean(key); }
        private String string(String key) { return tag.getString(key); }
    }
}
