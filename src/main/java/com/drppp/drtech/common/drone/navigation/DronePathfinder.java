package com.drppp.drtech.common.drone.navigation;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

/** Bounded six-direction A* suitable for a 1x1 flying entity. */
public final class DronePathfinder {

    public static final int DEFAULT_MAX_RANGE = 64;
    public static final int DEFAULT_MAX_NODES = 4_096;

    public DronePathResult findPath(BlockPos start, BlockPos goal, DroneNavigationWorld world) {
        return findPath(start, goal, world, DEFAULT_MAX_RANGE, DEFAULT_MAX_NODES);
    }

    public DronePathResult findPath(BlockPos start, BlockPos goal, DroneNavigationWorld world, int maxRange,
            int maxNodes) {
        if (maxRange <= 0 || maxNodes <= 0) throw new IllegalArgumentException("Path limits must be positive");
        if (start.distanceSq(goal) > (double) maxRange * maxRange) {
            return new DronePathResult(DronePathStatus.OUT_OF_RANGE, Collections.emptyList(), 0);
        }
        if (!world.isPassable(start) || !world.isPassable(goal)) {
            return new DronePathResult(DronePathStatus.NO_PATH, Collections.emptyList(), 0);
        }
        if (start.equals(goal)) {
            return new DronePathResult(DronePathStatus.FOUND, Collections.singletonList(start), 1);
        }

        PriorityQueue<SearchNode> open = new PriorityQueue<>(Comparator.comparingInt(SearchNode::score));
        Map<BlockPos, Integer> costs = new HashMap<>();
        Map<BlockPos, BlockPos> parents = new HashMap<>();
        Set<BlockPos> closed = new HashSet<>();
        costs.put(start, 0);
        open.add(new SearchNode(start, 0, heuristic(start, goal)));
        int discovered = 1;

        while (!open.isEmpty()) {
            SearchNode current = open.poll();
            if (!closed.add(current.position)) continue;
            if (current.position.equals(goal)) {
                return new DronePathResult(DronePathStatus.FOUND, reconstruct(goal, parents), closed.size());
            }
            for (EnumFacing facing : EnumFacing.VALUES) {
                BlockPos next = current.position.offset(facing);
                if (closed.contains(next) || start.distanceSq(next) > (double) maxRange * maxRange
                        || !world.isPassable(next)) continue;
                int stepCost = facing.getAxis().isVertical() ? 12 : 10;
                int nextCost = current.cost + stepCost;
                Integer known = costs.get(next);
                if (known != null && known <= nextCost) continue;
                if (known == null && ++discovered > maxNodes) {
                    return new DronePathResult(DronePathStatus.NODE_LIMIT_REACHED, Collections.emptyList(), closed.size());
                }
                costs.put(next, nextCost);
                parents.put(next, current.position);
                open.add(new SearchNode(next, nextCost, heuristic(next, goal)));
            }
        }
        return new DronePathResult(DronePathStatus.NO_PATH, Collections.emptyList(), closed.size());
    }

    private static int heuristic(BlockPos from, BlockPos to) {
        return (Math.abs(from.getX() - to.getX()) + Math.abs(from.getZ() - to.getZ())) * 10
                + Math.abs(from.getY() - to.getY()) * 12;
    }

    private static List<BlockPos> reconstruct(BlockPos goal, Map<BlockPos, BlockPos> parents) {
        List<BlockPos> reversed = new ArrayList<>();
        BlockPos current = goal;
        reversed.add(current);
        while (parents.containsKey(current)) {
            current = parents.get(current);
            reversed.add(current);
        }
        Collections.reverse(reversed);
        return reversed;
    }

    private static final class SearchNode {
        private final BlockPos position;
        private final int cost;
        private final int heuristic;
        private SearchNode(BlockPos position, int cost, int heuristic) {
            this.position = position;
            this.cost = cost;
            this.heuristic = heuristic;
        }
        private int score() { return cost + heuristic; }
    }
}
