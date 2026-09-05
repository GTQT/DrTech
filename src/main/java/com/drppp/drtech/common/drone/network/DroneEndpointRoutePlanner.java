package com.drppp.drtech.common.drone.network;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.ArrayList;

/** Owner-isolated planner for deterministic same-dimension endpoint routes. */
public final class DroneEndpointRoutePlanner {
    private DroneEndpointRoutePlanner() { }

    public static Optional<DroneEndpointRoute> plan(DroneEndpointNetwork network, @Nullable UUID ownerId,
            @Nullable UUID sourceId, @Nullable UUID targetId, long worldTime) {
        if (network == null || sourceId == null || targetId == null || sourceId.equals(targetId)) return Optional.empty();
        List<DroneEndpoint> endpoints = network.listForOwner(ownerId, null);
        DroneEndpoint source = endpoints.stream().filter(endpoint -> sourceId.equals(endpoint.getEndpointId())).findFirst().orElse(null);
        DroneEndpoint target = endpoints.stream().filter(endpoint -> targetId.equals(endpoint.getEndpointId())).findFirst().orElse(null);
        if (!compatible(source, target, worldTime)) return Optional.empty();
        return Optional.of(new DroneEndpointRoute(source, target, distance(source, target)));
    }

    public static List<DroneEndpointRoute> planFrom(DroneEndpointNetwork network, @Nullable UUID ownerId,
            @Nullable UUID sourceId, @Nullable DroneEndpoint.Kind kind, long worldTime) {
        if (network == null || sourceId == null) return java.util.Collections.emptyList();
        DroneEndpoint source = network.listForOwner(ownerId, kind).stream()
                .filter(endpoint -> sourceId.equals(endpoint.getEndpointId())).findFirst().orElse(null);
        if (source == null || !DroneEndpointNetwork.isOnline(source, worldTime)) return java.util.Collections.emptyList();
        java.util.ArrayList<DroneEndpointRoute> routes = new java.util.ArrayList<>();
        for (DroneEndpoint target : network.listForOwner(ownerId, source.getKind())) {
            if (!source.getEndpointId().equals(target.getEndpointId()) && compatible(source, target, worldTime)) {
                routes.add(new DroneEndpointRoute(source, target, distance(source, target)));
            }
        }
        routes.sort(Comparator.comparingLong(DroneEndpointRoute::getDistance)
                .thenComparing(route -> route.getTarget().getEndpointId().toString()));
        return java.util.Collections.unmodifiableList(routes);
    }

    /** Builds an immutable world-overlay snapshot for the client renderer. */
    public static List<DroneEndpointWorldLink> worldLinks(DroneEndpointNetwork network, @Nullable UUID ownerId,
            @Nullable UUID sourceId, @Nullable DroneEndpoint.Kind kind, long worldTime) {
        List<DroneEndpointRoute> routes = planFrom(network, ownerId, sourceId, kind, worldTime);
        ArrayList<DroneEndpointWorldLink> links = new ArrayList<>();
        for (DroneEndpointRoute route : routes) links.add(new DroneEndpointWorldLink(route));
        return java.util.Collections.unmodifiableList(links);
    }

    private static boolean compatible(DroneEndpoint source, DroneEndpoint target, long worldTime) {
        return source != null && target != null && source.getKind() == target.getKind()
                && source.getDimension() == target.getDimension()
                && DroneEndpointNetwork.isOnline(source, worldTime)
                && DroneEndpointNetwork.isOnline(target, worldTime);
    }

    private static long distance(DroneEndpoint source, DroneEndpoint target) {
        long dx = Math.abs((long) source.getPosition().getX() - target.getPosition().getX());
        long dy = Math.abs((long) source.getPosition().getY() - target.getPosition().getY());
        long dz = Math.abs((long) source.getPosition().getZ() - target.getPosition().getZ());
        long result = dx + dy;
        return Long.MAX_VALUE - result < dz ? Long.MAX_VALUE : result + dz;
    }
}
