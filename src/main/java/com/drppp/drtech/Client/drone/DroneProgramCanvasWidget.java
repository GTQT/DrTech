package com.drppp.drtech.Client.drone;

import com.cleanroommc.modularui.api.UpOrDown;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.drawable.GuiDraw;
import com.cleanroommc.modularui.screen.RichTooltip;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetThemeEntry;
import com.cleanroommc.modularui.widget.Widget;
import com.drppp.drtech.common.drone.program.edit.DroneGraphEditCommand;
import com.drppp.drtech.common.drone.program.model.DroneNodeDefinition;
import com.drppp.drtech.common.drone.program.model.DroneNodePropertyDefinition;
import com.drppp.drtech.common.drone.program.model.DroneNodePropertyType;
import com.drppp.drtech.common.drone.filter.DroneFilterMode;
import com.drppp.drtech.common.drone.filter.DroneItemFilterSpec;
import com.drppp.drtech.common.drone.filter.DroneBlockFilterSpec;
import com.drppp.drtech.common.drone.program.model.DroneArea;
import com.drppp.drtech.common.drone.program.model.DronePortDefinition;
import com.drppp.drtech.common.drone.program.model.DronePortDirection;
import com.drppp.drtech.common.drone.program.model.DronePortType;
import com.drppp.drtech.common.drone.program.model.DroneProgramEdge;
import com.drppp.drtech.common.drone.program.model.DroneProgramGraph;
import com.drppp.drtech.common.drone.program.model.DroneProgramNode;
import com.drppp.drtech.common.drone.program.registry.DroneNodeRegistry;
import com.drppp.drtech.common.drone.program.registry.DrTechDroneNodes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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

    private final Supplier<DroneProgramGraph> graphSupplier;
    private final Consumer<DroneGraphEditCommand> commandSink;
    private final BooleanSupplier editableSupplier;
    private final Supplier<Set<UUID>> diagnosticNodesSupplier;
    private final Supplier<UUID> activeNodeSupplier;
    private final DroneNodeRegistry registry = DrTechDroneNodes.createDefaultRegistry();
    private final Map<UUID, PreviewPosition> previews = new HashMap<>();

    private UUID selectedNodeId;
    private DroneProgramNode copiedNode;
    private PendingPort pendingPort;
    private UUID draggingNodeId;
    private int dragOffsetX;
    private int dragOffsetY;
    private boolean panning;
    private int lastMouseX;
    private int lastMouseY;
    private int panX = 8;
    private int panY = 8;
    private int zoomPercent = 100;
    private int selectedAreaCorner = 1;
    private int selectedPropertyIndex;
    private UUID propertyDraftNodeId;
    private String propertyDraftId = "";
    private String propertyDraft = "";
    private String connectionHint = "";
    private long connectionHintUntil;
    private int connectionHintColor = 0xFFFFA0A0;

    public DroneProgramCanvasWidget(Supplier<DroneProgramGraph> graphSupplier,
            Consumer<DroneGraphEditCommand> commandSink, BooleanSupplier editableSupplier,
            Supplier<Set<UUID>> diagnosticNodesSupplier, Supplier<UUID> activeNodeSupplier) {
        this.graphSupplier = graphSupplier;
        this.commandSink = commandSink;
        this.editableSupplier = editableSupplier;
        this.diagnosticNodesSupplier = diagnosticNodesSupplier;
        this.activeNodeSupplier = activeNodeSupplier;
        background();
    }

    @Override
    public void draw(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
        int width = getArea().w();
        int height = getArea().h();
        GuiDraw.drawRect(0, 0, width, height, 0xFF151A21);
        drawGrid(width, height);
        DroneProgramGraph graph = graphSupplier.get();
        if (graph == null) {
            GuiDraw.drawText(I18n.format("drtech.drone.programmer.insert_card"), 10, 10, 1.0F, 0xFFB9C2CF, false);
            return;
        }
        for (DroneProgramEdge edge : graph.getEdges()) {
            drawEdge(graph, edge);
        }
        if (pendingPort != null) {
            Point from = portPoint(graph.getNode(pendingPort.nodeId), pendingPort.portId, DronePortDirection.OUTPUT);
            if (from != null) {
                drawOrthogonalLine(from.x, from.y, context.getMouseX(), context.getMouseY(),
                        colorForType(pendingPort.type));
            }
        }
        PortHit hoveredPort = findPort(graph, context.getMouseX(), context.getMouseY());
        for (DroneProgramNode node : graph.getNodes()) {
            drawNode(graph, node, hoveredPort);
        }
        drawConnectionGuide(width, height);
        GuiDraw.drawText(zoomPercent + "%", width - 30, height - 10, 0.7F, 0xFF8291A5, false);
    }

    private void drawGrid(int width, int height) {
        int spacing = Math.max(8, 16 * zoomPercent / 100);
        int startX = floorMod(scale(panX), spacing);
        int startY = floorMod(scale(panY), spacing);
        for (int x = startX; x < width; x += spacing) GuiDraw.drawRect(x, 0, 1, height, 0xFF202833);
        for (int y = startY; y < height; y += spacing) GuiDraw.drawRect(0, y, width, 1, 0xFF202833);
    }

    private void drawEdge(DroneProgramGraph graph, DroneProgramEdge edge) {
        DroneProgramNode source = graph.getNode(edge.getSourceNodeId());
        DroneProgramNode target = graph.getNode(edge.getTargetNodeId());
        Point from = portPoint(source, edge.getSourcePortId(), DronePortDirection.OUTPUT);
        Point to = portPoint(target, edge.getTargetPortId(), DronePortDirection.INPUT);
        if (from == null || to == null) return;
        DroneNodeDefinition definition = source == null ? null : registry.get(source.getType());
        DronePortDefinition port = definition == null ? null : definition.getPort(edge.getSourcePortId());
        drawOrthogonalLine(from.x, from.y, to.x, to.y, colorForType(port == null ? DronePortType.ANY_DATA : port.getType()));
    }

    private void drawOrthogonalLine(int x1, int y1, int x2, int y2, int color) {
        int middle = x1 + (x2 - x1) / 2;
        int thickness = zoomPercent < 80 ? 1 : 2;
        GuiDraw.drawRect(Math.min(x1, middle), y1, Math.max(1, Math.abs(middle - x1)), thickness, color);
        GuiDraw.drawRect(middle, Math.min(y1, y2), thickness, Math.max(1, Math.abs(y2 - y1)), color);
        GuiDraw.drawRect(Math.min(middle, x2), y2, Math.max(1, Math.abs(x2 - middle)), thickness, color);
    }

    private void drawNode(DroneProgramGraph graph, DroneProgramNode node, PortHit hoveredPort) {
        Point point = nodePoint(node);
        DroneNodeDefinition definition = registry.get(node.getType());
        int nodeHeight = nodeHeight(definition);
        boolean active = node.getId().equals(activeNodeSupplier.get());
        int background = node.getId().equals(selectedNodeId) ? 0xFF314760
                : active ? 0xFF25493F : 0xFF26313E;
        GuiDraw.drawRect(point.x, point.y, scaled(NODE_WIDTH), scaled(nodeHeight), background);
        GuiDraw.drawRect(point.x, point.y, scaled(NODE_WIDTH), scaled(11), categoryColor(definition));
        int border = node.getId().equals(selectedNodeId) ? 0xFF68B7FF
                : diagnosticNodesSupplier.get().contains(node.getId()) ? 0xFFFF5A5A
                : active ? 0xFF52E39E : 0xFF526274;
        GuiDraw.drawBorderInsideXYWH(point.x, point.y, scaled(NODE_WIDTH), scaled(nodeHeight), border);
        if (node.getConfiguration().getBoolean("Breakpoint")) {
            GuiDraw.drawRect(point.x + scaled(NODE_WIDTH - 7), point.y + scaled(1), scaled(5), scaled(5),
                    0xFFFF4D5A);
        }
        String customLabel = node.getConfiguration().getString("Label");
        String title = !customLabel.isEmpty() ? customLabel : definition == null
                ? I18n.format("drtech.drone.canvas.missing_node", node.getType())
                : I18n.format("drtech.drone.node." + node.getType().getPath());
        GuiDraw.drawText(title, point.x + scaled(4), point.y + scaled(2), Math.max(0.5F, zoomPercent / 150.0F),
                0xFFE7EDF5, false);
        if (definition == null) return;
        for (DronePortDefinition port : definition.getPorts()) {
            Point portPoint = portPoint(node, port.getId(), port.getDirection());
            if (portPoint == null) continue;
            int size = Math.max(3, scaled(PORT_SIZE));
            boolean hovered = hoveredPort != null && hoveredPort.node.getId().equals(node.getId())
                    && hoveredPort.port.getId().equals(port.getId())
                    && hoveredPort.port.getDirection() == port.getDirection();
            boolean compatible = pendingPort == null || port.getDirection() == DronePortDirection.OUTPUT
                    || port.getType().accepts(pendingPort.type);
            boolean missing = port.isRequired() && !isPortConnected(graph, node, port);
            int outline = hovered ? (compatible ? 0xFFFFFF66 : 0xFFFF4D5A)
                    : missing ? 0xFFFF4D5A : 0;
            if (outline != 0) {
                int outlineSize = size + Math.max(2, scaled(3));
                GuiDraw.drawRect(portPoint.x - outlineSize / 2, portPoint.y - outlineSize / 2,
                        outlineSize, outlineSize, outline);
            }
            GuiDraw.drawRect(portPoint.x - size / 2, portPoint.y - size / 2, size, size, colorForType(port.getType()));
            drawPortLabel(point, portPoint, port);
        }
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
        } else {
            text = I18n.format("drtech.drone.canvas.connection.guide");
            color = 0xFFAEBBC9;
        }
        GuiDraw.drawRect(2, height - 14, width - 38, 12, 0xB0192029);
        GuiDraw.drawText(text, 5, height - 11, 0.55F, color, false);
    }

    @Override
    public void drawForeground(ModularGuiContext context) {
        super.drawForeground(context);
        if (!isBelowMouseFor(0)) return;
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
        UUID previousSelection = selectedNodeId;
        selectedNodeId = node == null ? null : node.getId();
        if (!java.util.Objects.equals(previousSelection, selectedNodeId)) {
            selectedPropertyIndex = 0;
            clearPropertyDraft();
        }
        if (mouseButton == 0 && node != null && editableSupplier.getAsBoolean()) {
            Point point = nodePoint(node);
            draggingNodeId = node.getId();
            dragOffsetX = mouseX - point.x;
            dragOffsetY = mouseY - point.y;
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
            previews.put(draggingNodeId, new PreviewPosition(unscale(mouseX - dragOffsetX) - panX,
                    unscale(mouseY - dragOffsetY) - panY));
        }
    }

    @Override
    public boolean onMouseRelease(int mouseButton) {
        panning = false;
        if (draggingNodeId != null && mouseButton == 0) {
            DroneProgramGraph graph = graphSupplier.get();
            PreviewPosition preview = previews.remove(draggingNodeId);
            if (graph != null && preview != null && editableSupplier.getAsBoolean()) {
                commandSink.accept(DroneGraphEditCommand.moveNode(graph.getRevision(), draggingNodeId,
                        preview.x, preview.y));
            }
            draggingNodeId = null;
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
        int before = zoomPercent;
        zoomPercent = Math.max(50, Math.min(150, zoomPercent + direction.modifier * 10));
        return before != zoomPercent;
    }

    public void deleteSelected() {
        DroneProgramGraph graph = graphSupplier.get();
        if (graph != null && selectedNodeId != null && editableSupplier.getAsBoolean()) {
            commandSink.accept(DroneGraphEditCommand.removeNode(graph.getRevision(), selectedNodeId));
            selectedNodeId = null;
            pendingPort = null;
        }
    }

    /** Copies the selected node's type and settings. Connections are deliberately not copied. */
    public void copySelected() {
        DroneProgramGraph graph = graphSupplier.get();
        DroneProgramNode node = graph == null || selectedNodeId == null ? null : graph.getNode(selectedNodeId);
        copiedNode = node == null ? null : new DroneProgramNode(node.getId(), node.getType(), node.getX(), node.getY(),
                node.getConfiguration());
    }

    /** Pastes a fresh node near its source so it remains a safe, independent graph mutation. */
    public void pasteCopiedNode() {
        DroneProgramGraph graph = graphSupplier.get();
        if (graph == null || copiedNode == null || !editableSupplier.getAsBoolean()) return;
        UUID pastedId = UUID.randomUUID();
        int offset = 24 + graph.getNodes().size() % 3 * 8;
        commandSink.accept(DroneGraphEditCommand.addNode(graph.getRevision(), pastedId, copiedNode.getType(),
                copiedNode.getX() + offset, copiedNode.getY() + offset, copiedNode.getConfiguration()));
        selectedNodeId = pastedId;
        selectedPropertyIndex = 0;
    }

    public boolean hasCopiedNode() {
        return copiedNode != null;
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
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        for (DroneProgramNode node : graph.getNodes()) {
            minX = Math.min(minX, node.getX());
            minY = Math.min(minY, node.getY());
            maxX = Math.max(maxX, node.getX() + NODE_WIDTH);
            maxY = Math.max(maxY, node.getY() + nodeHeight(registry.get(node.getType())));
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
            case ENUM:
            case DIRECTION:
                value = config.getString(property.getId());
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
                value = blockSpec.getRules().isEmpty() ? "any"
                        : (blockSpec.getMode() == DroneFilterMode.WHITELIST ? "allow " : "deny ")
                                + blockSpec.getRules().size();
                break;
            case FLUID_SELECTOR:
                value = config.getString(property.getId());
                if (value.isEmpty()) value = I18n.format("drtech.drone.programmer.any_fluid");
                break;
            default:
                value = config.hasKey(property.getId()) ? "configured" : "unset";
        }
        return property.getId() + " [" + (selected.index + 1) + "/" + selected.count + "]: " + value;
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

    public boolean isSelectedPropertyTextEditable() {
        SelectedProperty selected = selectedProperty();
        if (selected == null) return false;
        DroneNodePropertyType type = selected.property.getType();
        return type == DroneNodePropertyType.INTEGER || type == DroneNodePropertyType.NUMBER
                || type == DroneNodePropertyType.STRING || type == DroneNodePropertyType.FLUID_SELECTOR;
    }

    public boolean isSelectedItemFilter() {
        SelectedProperty selected = selectedProperty();
        return selected != null && (selected.property.getType() == DroneNodePropertyType.ITEM_SELECTOR
                || selected.property.getType() == DroneNodePropertyType.BLOCK_SELECTOR);
    }

    public boolean isSelectedFluidSelector() {
        SelectedProperty selected = selectedProperty();
        return selected != null && selected.property.getType() == DroneNodePropertyType.FLUID_SELECTOR;
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
        int metadata;
        try {
            metadata = state.getBlock().getMetaFromState(state);
        } catch (RuntimeException ignored) {
            metadata = -1;
        }
        final int capturedMetadata = metadata;
        configureSelected((node, config) -> {
            DroneBlockFilterSpec previous = readBlockFilter(config, property.getId());
            List<DroneBlockFilterSpec.Rule> rules = new ArrayList<>(previous.getRules());
            if (rules.size() >= DroneBlockFilterSpec.MAX_RULES) return;
            rules.add(new DroneBlockFilterSpec.Rule(blockId, capturedMetadata));
            writeBlockFilter(config, property.getId(), new DroneBlockFilterSpec(previous.getMode(), rules));
        });
    }

    public String getSelectedPropertyInputText() {
        SelectedProperty selected = selectedProperty();
        if (selected == null || !isSelectedPropertyTextEditable()) return "";
        ensurePropertyDraft(selected);
        return propertyDraft;
    }

    public void setSelectedPropertyInputText(String value) {
        SelectedProperty selected = selectedProperty();
        if (selected == null || !isSelectedPropertyTextEditable() || value == null || value.length() > 128) return;
        ensurePropertyDraft(selected);
        propertyDraft = value;
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
            DroneArea area = DroneArea.between(
                    new net.minecraft.util.math.BlockPos(config.integer("X1", 0), config.integer("Y1", 0),
                            config.integer("Z1", 0)),
                    new net.minecraft.util.math.BlockPos(config.integer("X2", 0), config.integer("Y2", 0),
                            config.integer("Z2", 0)));
            return I18n.format("drtech.drone.inspector.area", selectedAreaCorner == 1 ? "A" : "B",
                    config.integer("X" + selectedAreaCorner, 0), config.integer("Y" + selectedAreaCorner, 0),
                    config.integer("Z" + selectedAreaCorner, 0), area.getVolume());
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
                String key = axis + selectedAreaCorner;
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
        mutation.apply(node, configuration);
        commandSink.accept(DroneGraphEditCommand.configureNode(graph.getRevision(), node.getId(), configuration));
    }

    private DroneProgramNode findNode(DroneProgramGraph graph, int x, int y) {
        DroneProgramNode found = null;
        for (DroneProgramNode node : graph.getNodes()) {
            Point point = nodePoint(node);
            if (x >= point.x && x < point.x + scaled(NODE_WIDTH)
                    && y >= point.y && y < point.y + scaled(nodeHeight(registry.get(node.getType())))) found = node;
        }
        return found;
    }

    private PortHit findPort(DroneProgramGraph graph, int x, int y) {
        for (DroneProgramNode node : graph.getNodes()) {
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
        int inputs = 0;
        int outputs = 0;
        for (DronePortDefinition port : definition.getPorts()) {
            if (port.getDirection() == DronePortDirection.INPUT) inputs++;
            else outputs++;
        }
        int rows = Math.max(inputs, outputs);
        return Math.max(NODE_HEIGHT, PORT_START_Y + Math.max(0, rows - 1) * PORT_STEP_Y + 7);
    }

    private static boolean isPortConnected(DroneProgramGraph graph, DroneProgramNode node,
            DronePortDefinition port) {
        for (DroneProgramEdge edge : graph.getEdges()) {
            if (port.getDirection() == DronePortDirection.INPUT
                    && edge.getTargetNodeId().equals(node.getId())
                    && edge.getTargetPortId().equals(port.getId())) return true;
            if (port.getDirection() == DronePortDirection.OUTPUT
                    && edge.getSourceNodeId().equals(node.getId())
                    && edge.getSourcePortId().equals(port.getId())) return true;
        }
        return false;
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

    private int scale(int value) {
        return value * zoomPercent / 100;
    }

    private int scaled(int value) {
        return Math.max(1, scale(value));
    }

    private int unscale(int value) {
        return value * 100 / zoomPercent;
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
            default -> 0xFF697A8D;
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

    private static final class Point {
        private final int x;
        private final int y;
        private Point(int x, int y) { this.x = x; this.y = y; }
    }

    private static final class PreviewPosition {
        private final int x;
        private final int y;
        private PreviewPosition(int x, int y) { this.x = x; this.y = y; }
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
