package com.drppp.drtech.common.drone.network;

import com.drppp.drtech.common.drone.hardware.DroneUpgradeType;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Server-side logistics discovery, reservation, and deterministic drone assignment. */
final class DroneLogisticsDispatcher {
    private static final int MAX_DISCOVERED_PER_TICK = 16;
    private static final long JOB_TIMEOUT_TICKS = 20L * 60L * 5L;
    private static final long RESERVATION_LEASE_TICKS = JOB_TIMEOUT_TICKS + 200L;

    private DroneLogisticsDispatcher() { }

    static void tick(World world, DroneFleetState fleet, long worldTime) {
        DroneEndpointNetwork endpoints = DroneEndpointNetwork.get(world);
        discoverLogisticsJobs(fleet, endpoints, worldTime);
        assignEligibleJobs(world, fleet, endpoints, worldTime);
    }

    private static void discoverLogisticsJobs(DroneFleetState fleet, DroneEndpointNetwork network, long worldTime) {
        List<DroneEndpoint> all = network.snapshot();
        int created = 0;
        for (DroneEndpoint target : all) {
            if (created >= MAX_DISCOVERED_PER_TICK) break;
            UUID owner = target.getOwnerId();
            if (owner == null || target.getRequestAmount() <= 0L
                    || !DroneEndpointNetwork.isOnline(target, worldTime)) continue;
            SourceChoice choice = bestSource(all, fleet, target, worldTime);
            if (choice == null || hasOpenJob(fleet, owner, target.getEndpointId(), target.getKind(),
                    choice.resourceId)) continue;
            DroneJob job = DroneJob.logistics(UUID.randomUUID(), owner,
                    Math.max(-100, Math.min(100, target.getPriority() + choice.source.getPriority())), worldTime,
                    JOB_TIMEOUT_TICKS, 5, 40L, target.getKind(), choice.resourceId,
                    choice.amount, choice.source.getEndpointId(), target.getEndpointId());
            if (fleet.submitJob(owner, job)) created++;
        }
    }

    private static SourceChoice bestSource(List<DroneEndpoint> all, DroneFleetState fleet,
            DroneEndpoint target, long worldTime) {
        List<SourceChoice> choices = new ArrayList<>();
        for (DroneEndpoint source : all) {
            if (source == target || source.getKind() != target.getKind()
                    || source.getDimension() != target.getDimension()
                    || !java.util.Objects.equals(source.getOwnerId(), target.getOwnerId())
                    || !DroneEndpointNetwork.isOnline(source, worldTime)) continue;
            for (DroneEndpointResource resource : source.getResources()) {
                if (!source.matchesResource(resource.getResourceId())
                        || !target.canStoreResource(resource.getResourceId())) continue;
                long sourceReserved = fleet.getReservations().getReservedProvidingAmount(
                        source.getEndpointId(), resource.getResourceId());
                long targetReserved = fleet.getReservations().getReservedReceivingAmount(target.getEndpointId());
                long currentTarget = amountAt(target, resource.getResourceId());
                long desired = target.getRequestAmount() > currentTarget
                        ? target.getRequestAmount() - currentTarget : 0L;
                long canReceive = Math.min(desired,
                        target.requestCapacity(currentTarget, targetReserved));
                long canProvide = source.availableToProvide(resource.getAmount(), sourceReserved);
                long amount = Math.min(canReceive, canProvide);
                if (amount <= 0L || hasOpenJob(fleet, target.getOwnerId(), target.getEndpointId(),
                        target.getKind(), resource.getResourceId())) continue;
                choices.add(new SourceChoice(source, resource.getResourceId(), amount,
                        manhattan(source, target)));
            }
        }
        return choices.stream().min(Comparator
                .comparingInt((SourceChoice choice) -> choice.source.getPriority()).reversed()
                .thenComparingLong(choice -> choice.distance)
                .thenComparing(choice -> choice.source.getEndpointId().toString())
                .thenComparing(choice -> choice.resourceId)).orElse(null);
    }

    private static void assignEligibleJobs(World world, DroneFleetState fleet,
            DroneEndpointNetwork endpointNetwork, long worldTime) {
        Set<UUID> assignedDrones = new HashSet<>();
        for (DroneJob job : fleet.getJobs().snapshot()) {
            if (job.getAssignedDroneId() != null && (job.getState() == DroneJob.State.RUNNING
                    || job.getState() == DroneJob.State.RETRY_WAIT
                    || job.getLogisticsStage() == DroneJob.LogisticsStage.RETURNING)) {
                assignedDrones.add(job.getAssignedDroneId());
            }
        }
        for (int index = 0; index < MAX_DISCOVERED_PER_TICK; index++) {
            DroneJob job = fleet.getJobs().takeNextLogistics(worldTime).orElse(null);
            if (job == null) return;
            DroneEndpoint source = endpointNetwork.getEndpoint(job.getSourceEndpointId());
            DroneEndpoint target = endpointNetwork.getEndpoint(job.getTargetEndpointId());
            long inTransit = Math.max(0L, job.getPickedAmount() - job.getDeliveredAmount());
            if (inTransit > 0L && job.getAssignedDroneId() == null) {
                fleet.failJob(job.getOwnerId(), job.getJobId(), worldTime, "CARGO_DRONE_ID_MISSING");
                continue;
            }
            if (!validRoute(job, source, target, worldTime, inTransit <= 0L)) {
                fleet.failJob(job.getOwnerId(), job.getJobId(), worldTime, "ENDPOINT_OFFLINE_OR_CHANGED");
                continue;
            }
            DroneRegistryRecord drone = inTransit > 0L && job.getAssignedDroneId() != null
                    ? selectRetainedCargoDrone(world, job, source, worldTime)
                    : selectDrone(world, job, source, assignedDrones, worldTime);
            if (drone == null) {
                fleet.deferJob(job.getOwnerId(), job.getJobId(), worldTime, "NO_AVAILABLE_DRONE", 40L);
                continue;
            }
            DroneReservation reservation = inTransit > 0L
                    ? fleet.reserveTarget(job.getOwnerId(), job.getJobId(), target, job.getResourceId(), false,
                            amountAt(target, job.getResourceId()), inTransit, worldTime,
                            RESERVATION_LEASE_TICKS).orElse(null)
                    : fleet.reserveLogistics(job.getOwnerId(), job.getJobId(), source, target,
                            job.getResourceId(), job.getRequestedAmount(), worldTime,
                            RESERVATION_LEASE_TICKS).orElse(null);
            if (reservation == null) {
                fleet.failJob(job.getOwnerId(), job.getJobId(), worldTime, "RESERVATION_REJECTED");
                continue;
            }
            if (inTransit <= 0L) job.limitRequestedAmount(reservation.getAmount());
            if (job.getAssignedDroneId() == null) job.assignDrone(drone.getDroneId());
            job.setLogisticsStage(inTransit > 0L ? DroneJob.LogisticsStage.TO_TARGET
                    : DroneJob.LogisticsStage.TO_SOURCE);
            assignedDrones.add(drone.getDroneId());
            fleet.markDirty();
        }
    }

    private static DroneRegistryRecord selectDrone(World world, DroneJob job, DroneEndpoint source,
            Set<UUID> assigned, long worldTime) {
        int requiredMask = requiredUpgradeMask(job);
        return DroneRegistry.get(world).listForOwner(job.getOwnerId()).stream()
                .filter(record -> record.getDimension() == source.getDimension())
                .filter(record -> DroneRegistry.isOnline(record, worldTime))
                .filter(DroneLogisticsDispatcher::isLogisticsIdle)
                .filter(record -> record.getEnergyStored() > 0L)
                .filter(record -> !assigned.contains(record.getDroneId()))
                .filter(record -> job.getResourceKind() != DroneEndpoint.Kind.ITEM
                        || record.getCargoOccupiedSlots() < record.getCargoCapacitySlots())
                .filter(record -> (record.getUpgradeMask() & requiredMask) == requiredMask)
                .min(Comparator.<DroneRegistryRecord>comparingDouble(record ->
                                record.getPosition().distanceSq(source.getPosition()))
                        .thenComparing(record -> record.getDroneId().toString())).orElse(null);
    }

    /** PROGRAM is the normal state of a deployed drone with no user graph. */
    private static boolean isLogisticsIdle(DroneRegistryRecord record) {
        String status = record.getStatus();
        return "PROGRAM".equals(status) || "READY".equals(status)
                || "COMPLETED".equals(status) || "ERROR".equals(status);
    }

    private static DroneRegistryRecord selectRetainedCargoDrone(World world, DroneJob job,
            DroneEndpoint source, long worldTime) {
        DroneRegistryRecord record = DroneRegistry.get(world).getRecord(job.getAssignedDroneId()).orElse(null);
        int requiredMask = requiredUpgradeMask(job);
        return record != null && java.util.Objects.equals(record.getOwnerId(), job.getOwnerId())
                && record.getDimension() == source.getDimension() && DroneRegistry.isOnline(record, worldTime)
                && (record.getUpgradeMask() & requiredMask) == requiredMask ? record : null;
    }

    private static int requiredUpgradeMask(DroneJob job) {
        return job.getResourceKind() == DroneEndpoint.Kind.FLUID
                ? DroneFleetController.requiredUpgradeMask(DroneUpgradeType.FLUID_CARGO)
                : job.getResourceKind() == DroneEndpoint.Kind.EU
                ? DroneFleetController.requiredUpgradeMask(DroneUpgradeType.EU_INTERFACE) : 0;
    }

    private static boolean validRoute(DroneJob job, DroneEndpoint source, DroneEndpoint target, long worldTime,
            boolean requireSourceOnline) {
        return source != null && target != null && source.getKind() == job.getResourceKind()
                && target.getKind() == job.getResourceKind() && source.getDimension() == target.getDimension()
                && java.util.Objects.equals(source.getOwnerId(), job.getOwnerId())
                && java.util.Objects.equals(target.getOwnerId(), job.getOwnerId())
                && source.matchesResource(job.getResourceId()) && target.canStoreResource(job.getResourceId())
                && (!requireSourceOnline || DroneEndpointNetwork.isOnline(source, worldTime))
                && DroneEndpointNetwork.isOnline(target, worldTime);
    }

    private static boolean hasOpenJob(DroneFleetState fleet, UUID owner, UUID targetId,
            DroneEndpoint.Kind kind, String resourceId) {
        for (DroneJob job : fleet.getJobsForOwner(owner)) {
            if (!job.isLogisticsJob() || !targetId.equals(job.getTargetEndpointId())
                    || job.getResourceKind() != kind || !resourceId.equals(job.getResourceId())) continue;
            DroneJob.State state = job.getState();
            if (state != DroneJob.State.COMPLETED && state != DroneJob.State.FAILED
                    && state != DroneJob.State.CANCELLED) return true;
        }
        return false;
    }

    private static long amountAt(DroneEndpoint endpoint, String resourceId) {
        DroneEndpointResource resource = endpoint.getResource(resourceId);
        return resource == null ? 0L : resource.getAmount();
    }

    private static long manhattan(DroneEndpoint left, DroneEndpoint right) {
        return Math.abs((long) left.getPosition().getX() - right.getPosition().getX())
                + Math.abs((long) left.getPosition().getY() - right.getPosition().getY())
                + Math.abs((long) left.getPosition().getZ() - right.getPosition().getZ());
    }

    private static final class SourceChoice {
        private final DroneEndpoint source;
        private final String resourceId;
        private final long amount;
        private final long distance;

        private SourceChoice(DroneEndpoint source, String resourceId, long amount, long distance) {
            this.source = source;
            this.resourceId = resourceId;
            this.amount = amount;
            this.distance = distance;
        }
    }
}
