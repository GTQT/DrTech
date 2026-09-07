package com.drppp.drtech.common.drone.program.edit;

import com.drppp.drtech.common.drone.program.model.DroneProgramGraph;
import com.drppp.drtech.common.drone.program.model.DroneProgramNode;
import com.drppp.drtech.common.drone.program.registry.DrTechDroneNodes;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

/** Geometry-only group membership rules. Membership is derived, so old program-card schemas remain compatible. */
public final class DroneGroupLayout {

    public static final int DEFAULT_WIDTH = 320;
    public static final int DEFAULT_HEIGHT = 200;
    public static final int MIN_WIDTH = 128;
    public static final int MIN_HEIGHT = 64;
    public static final int MAX_SIZE = 1600;
    public static final int HEADER_HEIGHT = 16;

    private DroneGroupLayout() {}

    public static boolean isGroup(DroneProgramNode node) {
        return node != null && node.getType().equals(DrTechDroneNodes.GROUP);
    }

    public static int width(DroneProgramNode group) {
        int value = group.getConfiguration().getInteger("Width");
        return clamp(value == 0 ? DEFAULT_WIDTH : value, MIN_WIDTH, MAX_SIZE);
    }

    public static int height(DroneProgramNode group) {
        int value = group.getConfiguration().getInteger("Height");
        return clamp(value == 0 ? DEFAULT_HEIGHT : value, MIN_HEIGHT, MAX_SIZE);
    }

    public static boolean isCollapsed(DroneProgramNode group) {
        return isGroup(group) && group.getConfiguration().getBoolean("Collapsed");
    }

    public static boolean contains(DroneProgramNode group, DroneProgramNode candidate) {
        if (!isGroup(group) || candidate == null || group.getId().equals(candidate.getId())) return false;
        return candidate.getX() >= group.getX()
                && candidate.getX() < group.getX() + width(group)
                && candidate.getY() >= group.getY() + HEADER_HEIGHT
                && candidate.getY() < group.getY() + height(group);
    }

    public static Set<UUID> members(DroneProgramGraph graph, DroneProgramNode group) {
        Set<UUID> result = new LinkedHashSet<>();
        for (DroneProgramNode node : graph.getNodes()) if (contains(group, node)) result.add(node.getId());
        return result;
    }

    /** Adds nested group contents until the selection no longer grows. */
    public static void expandSelectedGroups(DroneProgramGraph graph, Set<UUID> selection) {
        boolean changed;
        do {
            changed = false;
            for (DroneProgramNode node : graph.getNodes()) {
                if (isGroup(node) && selection.contains(node.getId())) {
                    changed |= selection.addAll(members(graph, node));
                }
            }
        } while (changed);
    }

    public static Set<UUID> hiddenByCollapsedGroups(DroneProgramGraph graph) {
        Set<UUID> hidden = new LinkedHashSet<>();
        for (DroneProgramNode group : graph.getNodes()) {
            if (isCollapsed(group)) hidden.addAll(members(graph, group));
        }
        return hidden;
    }

    public static Set<UUID> membersOfAllGroups(DroneProgramGraph graph) {
        Set<UUID> grouped = new LinkedHashSet<>();
        for (DroneProgramNode group : graph.getNodes()) {
            if (isGroup(group)) grouped.addAll(members(graph, group));
        }
        return grouped;
    }

    public static Frame surroundingFrame(DroneProgramGraph graph, Set<UUID> selection) {
        if (graph == null || selection == null || selection.isEmpty()) return null;
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        for (UUID nodeId : selection) {
            DroneProgramNode node = graph.getNode(nodeId);
            if (node == null) continue;
            int nodeWidth = isGroup(node) ? width(node) : 96;
            int nodeHeight = isGroup(node) ? height(node)
                    : node.getType().equals(DrTechDroneNodes.COMMENT) ? 46 : 32;
            minX = Math.min(minX, node.getX());
            minY = Math.min(minY, node.getY());
            maxX = Math.max(maxX, node.getX() + nodeWidth);
            maxY = Math.max(maxY, node.getY() + nodeHeight);
        }
        if (minX == Integer.MAX_VALUE) return null;
        int x = minX - 20;
        int y = minY - 32;
        return new Frame(x, y, Math.max(MIN_WIDTH, maxX - x + 20),
                Math.max(MIN_HEIGHT, maxY - y + 20));
    }

    private static int clamp(int value, int minimum, int maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }

    public static final class Frame {
        private final int x;
        private final int y;
        private final int width;
        private final int height;

        private Frame(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        public int getX() { return x; }
        public int getY() { return y; }
        public int getWidth() { return width; }
        public int getHeight() { return height; }
    }
}
