package com.drppp.drtech.intergations.opencomputers;

import com.drppp.drtech.drone.api.DroneExtensionRegistry;

import com.drppp.drtech.drone.entity.EntityProgrammableDrone;
import com.drppp.drtech.drone.machine.MetaTileEntityDroneDock;
import com.drppp.drtech.drone.network.DroneEndpoint;
import com.drppp.drtech.drone.network.DroneEndpointNetwork;
import com.drppp.drtech.drone.network.DroneEndpointResource;
import com.drppp.drtech.drone.network.DroneFleetState;
import com.drppp.drtech.drone.network.DroneJob;
import com.drppp.drtech.drone.network.DroneRegistry;
import com.drppp.drtech.drone.network.DroneRegistryRecord;
import com.drppp.drtech.drone.program.library.DroneProgramLibrary;
import com.drppp.drtech.drone.program.library.DroneProgramLibraryRecord;
import com.drppp.drtech.drone.program.codec.DroneProgramFormatException;
import com.drppp.drtech.drone.program.codec.DroneProgramTransferCodec;
import com.drppp.drtech.drone.program.model.DroneProgramGraph;
import gregtech.api.metatileentity.MetaTileEntity;
import li.cil.oc.api.Network;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.Visibility;
import li.cil.oc.api.prefab.AbstractManagedEnvironment;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import javax.annotation.Nullable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/** Authenticated callbacks for one adjacent drone machine. */
public final class OpenComputersMachineEnvironment extends AbstractManagedEnvironment {
    private static final OpenComputersCallLimiter LIMITER = new OpenComputersCallLimiter(20, 20L);
    private final World world;
    private final BlockPos position;
    private final String component;
    private String lastDockDrone;
    private final Map<UUID, String> lastDroneStates = new HashMap<>();
    private final Map<UUID, String> lastJobStates = new HashMap<>();
    private final Set<UUID> lowEnergyDrones = new HashSet<>();
    private long lastSignalTick = Long.MIN_VALUE;

    OpenComputersMachineEnvironment(World world, BlockPos position, String component) {
        this.world = world;
        this.position = position.toImmutable();
        this.component = component;
        setNode(Network.newNode(this, Visibility.Network).withComponent(component).create());
    }

    @Override public boolean canUpdate() { return true; }

    @Override
    public void update() {
        if (world == null || world.isRemote || !world.isBlockLoaded(position)) return;
        MetaTileEntity machine = OpenComputersMachineAccess.getMachine(world, position);
        if (!component.equals(OpenComputersMachineAccess.componentFor(machine))) {
            state().removeDevice(dimension(), position, component);
            if (node() != null) node().remove();
            return;
        }
        long now = world.getTotalWorldTime();
        if (lastSignalTick != Long.MIN_VALUE && now - lastSignalTick < 20L) return;
        lastSignalTick = now;
        emitStateSignals(machine);
    }

    @Callback(doc = "function():table -- public component type and pairing status")
    public Object[] componentInfo(Context context, Arguments arguments) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("component", component);
        data.put("paired", state().isPaired(dimension(), position, component));
        data.put("dimension", dimension());
        data.put("x", position.getX()); data.put("y", position.getY()); data.put("z", position.getZ());
        return result(true, "", data);
    }

    @Callback(doc = "function(token:string):table -- validate a pairing token")
    public Object[] isPaired(Context context, Arguments arguments) {
        UUID owner = authorize(context, arguments, "isPaired");
        return owner == null ? failure("unauthorized") : audited(context, owner, "isPaired", true,
                success(singleton("paired", true)));
    }

    @Callback(doc = "function(token:string):table -- query this dock")
    public Object[] getDock(Context context, Arguments arguments) {
        UUID owner = authorize(context, arguments, "getDock");
        if (owner == null) return failure("unauthorized");
        MetaTileEntity machine = machine();
        if (!(machine instanceof MetaTileEntityDroneDock)) return audited(context, owner, "getDock", false,
                failure("wrong_component"));
        MetaTileEntityDroneDock dock = (MetaTileEntityDroneDock) machine;
        if (!owner.equals(dock.getOwnerId())) return audited(context, owner, "getDock", false,
                failure("owner_mismatch"));
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", dock.getDockId().toString());
        data.put("name", dock.getDockName());
        data.put("enabled", dock.isEnabled());
        data.put("available", dock.isAvailableForDrone());
        data.put("autoLaunch", dock.isAutoLaunch());
        data.put("autoRecover", dock.isAutoRecover());
        data.put("priority", dock.getPriority());
        data.put("redstoneMode", dock.getRedstoneMode());
        data.put("drone", dock.getCurrentDroneId() == null ? "" : dock.getCurrentDroneId().toString());
        return audited(context, owner, "getDock", true, success(data));
    }

    @Callback(doc = "function(token:string):table -- launch the stored drone")
    public Object[] launch(Context context, Arguments arguments) {
        UUID owner = authorize(context, arguments, "launch");
        if (owner == null) return failure("unauthorized");
        MetaTileEntity machine = machine();
        boolean ok = machine instanceof MetaTileEntityDroneDock
                && owner.equals(((MetaTileEntityDroneDock) machine).getOwnerId())
                && ((MetaTileEntityDroneDock) machine).tryLaunchStoredDrone(null);
        return audited(context, owner, "launch", ok, ok ? success(singleton("launched", true))
                : failure("launch_failed"));
    }

    @Callback(doc = "function(token:string):table -- recall the drone bound to this dock")
    public Object[] recall(Context context, Arguments arguments) {
        UUID owner = authorize(context, arguments, "recall");
        if (owner == null) return failure("unauthorized");
        MetaTileEntity machine = machine();
        boolean ok = machine instanceof MetaTileEntityDroneDock
                && ((MetaTileEntityDroneDock) machine).requestBoundDroneRecall(owner);
        return audited(context, owner, "recall", ok, ok ? success(singleton("recalled", true))
                : failure("recall_failed"));
    }

    @Callback(doc = "function(token:string,command:string):table -- START, STOP or RECALL the dock drone")
    public Object[] controlDockDrone(Context context, Arguments arguments) {
        UUID owner = authorize(context, arguments, "controlDockDrone");
        if (owner == null) return failure("unauthorized");
        MetaTileEntity machine = machine();
        String rawCommand = stringArg(arguments, 1);
        if (rawCommand == null) return audited(context, owner, "controlDockDrone", false,
                failure("invalid_arguments"));
        String command = rawCommand.toUpperCase(Locale.ROOT);
        UUID droneId = machine instanceof MetaTileEntityDroneDock
                ? ((MetaTileEntityDroneDock) machine).getCurrentDroneId() : null;
        boolean ok = droneId != null && ("START".equals(command) || "STOP".equals(command)
                || "RECALL".equals(command)) && control(owner, droneId, command);
        return audited(context, owner, "controlDockDrone", ok,
                ok ? success(singleton("controlled", true)) : failure("control_failed"));
    }

    @Callback(doc = "function(token:string,page:number=0,size:number=16):table -- owner program page")
    public Object[] listPrograms(Context context, Arguments arguments) {
        UUID owner = authorize(context, arguments, "listPrograms");
        if (owner == null) return failure("unauthorized");
        if (!OpenComputersComponentIds.DRONE_PROGRAMMER.equals(component)) {
            return audited(context, owner, "listPrograms", false, failure("wrong_component"));
        }
        Integer requestedPage = intArg(arguments, 1, 0), requestedSize = intArg(arguments, 2, 16);
        if (requestedPage == null || requestedSize == null) return audited(context, owner,
                "listPrograms", false, failure("invalid_arguments"));
        int page = Math.max(0, requestedPage);
        int size = Math.max(1, Math.min(64, requestedSize));
        List<DroneProgramLibraryRecord> all = DroneProgramLibrary.get(world).listAccessibleForOwner(owner);
        List<Map<String, Object>> entries = new ArrayList<>();
        for (DroneProgramLibraryRecord record : OpenComputersPage.slice(all, page, size)) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", record.getProgramId().toString()); item.put("name", record.getName());
            item.put("owner", record.getOwnerId().toString()); item.put("revision", record.getRevision());
            item.put("nodes", record.getNodeCount()); item.put("edges", record.getEdgeCount());
            item.put("updated", record.getUpdatedAt()); entries.add(item);
        }
        return audited(context, owner, "listPrograms", true, success(pageData(entries, all.size(), page, size)));
    }

    @Callback(doc = "function(token:string,transfer:string):table -- validate and catalogue DRTECH-PROGRAM-1 text")
    public Object[] compileProgram(Context context, Arguments arguments) {
        UUID owner = authorize(context, arguments, "compileProgram");
        if (owner == null) return failure("unauthorized");
        if (!OpenComputersComponentIds.DRONE_PROGRAMMER.equals(component)) {
            return audited(context, owner, "compileProgram", false, failure("wrong_component"));
        }
        try {
            DroneProgramGraph graph = DroneProgramTransferCodec.decodeAndValidate(
                    arguments.checkString(1), DroneExtensionRegistry.nodes());
            DroneProgramLibrary.RegisterResult registered = DroneProgramLibrary.get(world).registerChecked(
                    owner, graph, world.getTotalWorldTime());
            boolean ok = registered.isAccepted();
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("program", graph.getProgramId().toString()); data.put("revision", graph.getRevision());
            data.put("result", registered.name());
            return audited(context, owner, "compileProgram", ok,
                    ok ? success(data) : failure(registered.name().toLowerCase(Locale.ROOT)));
        } catch (DroneProgramFormatException | RuntimeException exception) {
            return audited(context, owner, "compileProgram", false, failure("invalid_program"));
        }
    }

    @Callback(doc = "function(token:string,drone:string,program:string,revision:number=-1):table -- assign to idle drone")
    public Object[] assignProgram(Context context, Arguments arguments) {
        UUID owner = authorize(context, arguments, "assignProgram");
        if (owner == null) return failure("unauthorized");
        if (!OpenComputersComponentIds.DRONE_PROGRAMMER.equals(component)) {
            return audited(context, owner, "assignProgram", false, failure("wrong_component"));
        }
        String rawDrone = stringArg(arguments, 1), rawProgram = stringArg(arguments, 2);
        Long requestedRevision = longArg(arguments, 3, -1L);
        if (rawDrone == null || rawProgram == null || requestedRevision == null) return audited(context, owner,
                "assignProgram", false, failure("invalid_arguments"));
        UUID droneId = parseUuid(rawDrone);
        UUID programId = parseUuid(rawProgram);
        long revision = requestedRevision;
        DroneProgramGraph graph = programId == null ? null : resolveProgram(owner, programId, revision);
        boolean ok = droneId != null && graph != null && assignProgram(owner, droneId, graph);
        return audited(context, owner, "assignProgram", ok,
                ok ? success(singleton("assigned", true)) : failure("assign_failed"));
    }

    @Callback(doc = "function(token:string,page:number=0,size:number=16):table -- owner drone page")
    public Object[] queryDrones(Context context, Arguments arguments) {
        UUID owner = authorize(context, arguments, "queryDrones");
        if (owner == null) return failure("unauthorized");
        if (!OpenComputersComponentIds.DRONE_FLEET.equals(component)) {
            return audited(context, owner, "queryDrones", false, failure("wrong_component"));
        }
        Integer requestedPage = intArg(arguments, 1, 0), requestedSize = intArg(arguments, 2, 16);
        if (requestedPage == null || requestedSize == null) return audited(context, owner,
                "queryDrones", false, failure("invalid_arguments"));
        int page = Math.max(0, requestedPage);
        int size = Math.max(1, Math.min(64, requestedSize));
        List<DroneRegistryRecord> all = DroneRegistry.get(world).listForOwner(owner);
        List<Map<String, Object>> entries = new ArrayList<>();
        for (DroneRegistryRecord record : OpenComputersPage.slice(all, page, size)) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", record.getDroneId().toString()); item.put("status", record.getStatus());
            item.put("chassis", record.getChassis()); item.put("dimension", record.getDimension());
            item.put("x", record.getPosition().getX()); item.put("y", record.getPosition().getY());
            item.put("z", record.getPosition().getZ()); item.put("energy", record.getEnergyStored());
            item.put("energyCapacity", record.getEnergyCapacity());
            item.put("online", DroneRegistry.isOnline(record, world.getTotalWorldTime())); entries.add(item);
        }
        return audited(context, owner, "queryDrones", true, success(pageData(entries, all.size(), page, size)));
    }

    @Callback(doc = "function(token:string,page:number=0,size:number=16):table -- owner job page")
    public Object[] queryJobs(Context context, Arguments arguments) {
        UUID owner = authorize(context, arguments, "queryJobs");
        if (owner == null) return failure("unauthorized");
        if (!OpenComputersComponentIds.DRONE_FLEET.equals(component)) {
            return audited(context, owner, "queryJobs", false, failure("wrong_component"));
        }
        Integer requestedPage = intArg(arguments, 1, 0), requestedSize = intArg(arguments, 2, 16);
        if (requestedPage == null || requestedSize == null) return audited(context, owner,
                "queryJobs", false, failure("invalid_arguments"));
        int page = Math.max(0, requestedPage);
        int size = Math.max(1, Math.min(64, requestedSize));
        List<DroneJob> all = new ArrayList<>(DroneFleetState.get(world).getJobsForOwner(owner));
        all.sort((left, right) -> Long.compare(right.getSubmittedTick(), left.getSubmittedTick()));
        List<Map<String, Object>> entries = new ArrayList<>();
        for (DroneJob job : OpenComputersPage.slice(all, page, size)) entries.add(jobData(job));
        return audited(context, owner, "queryJobs", true, success(pageData(entries, all.size(), page, size)));
    }

    @Callback(doc = "function(token:string,kind:string='',page:number=0,size:number=16):table -- endpoint page")
    public Object[] queryEndpoints(Context context, Arguments arguments) {
        UUID owner = authorize(context, arguments, "queryEndpoints");
        if (owner == null) return failure("unauthorized");
        if (!OpenComputersComponentIds.DRONE_FLEET.equals(component)) {
            return audited(context, owner, "queryEndpoints", false, failure("wrong_component"));
        }
        String rawKind = optionalStringArg(arguments, 1, "");
        Integer requestedPage = intArg(arguments, 2, 0), requestedSize = intArg(arguments, 3, 16);
        if (rawKind == null || requestedPage == null || requestedSize == null) return audited(context, owner,
                "queryEndpoints", false, failure("invalid_arguments"));
        DroneEndpoint.Kind kind = null;
        if (!rawKind.trim().isEmpty()) {
            try { kind = DroneEndpoint.Kind.valueOf(rawKind.trim().toUpperCase(Locale.ROOT)); }
            catch (IllegalArgumentException invalid) {
                return audited(context, owner, "queryEndpoints", false, failure("invalid_kind"));
            }
        }
        int page = Math.max(0, requestedPage), size = Math.max(1, Math.min(64, requestedSize));
        List<DroneEndpoint> all = DroneEndpointNetwork.get(world).listForOwner(owner, kind);
        List<Map<String, Object>> entries = new ArrayList<>();
        for (DroneEndpoint endpoint : OpenComputersPage.slice(all, page, size)) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", endpoint.getEndpointId().toString()); item.put("kind", endpoint.getKind().name());
            item.put("dimension", endpoint.getDimension()); item.put("x", endpoint.getPosition().getX());
            item.put("y", endpoint.getPosition().getY()); item.put("z", endpoint.getPosition().getZ());
            item.put("online", DroneEndpointNetwork.isOnline(endpoint, world.getTotalWorldTime()));
            item.put("priority", endpoint.getPriority()); item.put("stored", endpoint.getStoredAmount());
            item.put("capacity", endpoint.getStorageCapacity()); item.put("request", endpoint.getRequestAmount());
            item.put("provide", endpoint.getProvideAmount()); item.put("resourceCount", endpoint.getResources().size());
            entries.add(item);
        }
        return audited(context, owner, "queryEndpoints", true,
                success(pageData(entries, all.size(), page, size)));
    }

    @Callback(doc = "function(token:string,endpoint:string,page:number=0,size:number=16):table -- resource page")
    public Object[] queryEndpointResources(Context context, Arguments arguments) {
        UUID owner = authorize(context, arguments, "queryEndpointResources");
        if (owner == null) return failure("unauthorized");
        if (!OpenComputersComponentIds.DRONE_FLEET.equals(component)) {
            return audited(context, owner, "queryEndpointResources", false, failure("wrong_component"));
        }
        String rawEndpoint = stringArg(arguments, 1);
        Integer requestedPage = intArg(arguments, 2, 0), requestedSize = intArg(arguments, 3, 16);
        UUID endpointId = rawEndpoint == null ? null : parseUuid(rawEndpoint);
        if (endpointId == null || requestedPage == null || requestedSize == null) return audited(context, owner,
                "queryEndpointResources", false, failure("invalid_arguments"));
        DroneEndpoint selected = null;
        for (DroneEndpoint endpoint : DroneEndpointNetwork.get(world).listForOwner(owner, null)) {
            if (endpointId.equals(endpoint.getEndpointId())) { selected = endpoint; break; }
        }
        if (selected == null) return audited(context, owner, "queryEndpointResources", false,
                failure("endpoint_not_found"));
        int page = Math.max(0, requestedPage), size = Math.max(1, Math.min(64, requestedSize));
        List<DroneEndpointResource> all = selected.getResources();
        List<Map<String, Object>> entries = new ArrayList<>();
        for (DroneEndpointResource resource : OpenComputersPage.slice(all, page, size)) {
            Map<String, Object> item = new LinkedHashMap<>(); item.put("id", resource.getResourceId());
            item.put("amount", resource.getAmount()); item.put("capacity", resource.getCapacity());
            entries.add(item);
        }
        Map<String, Object> data = pageData(entries, all.size(), page, size);
        data.put("endpoint", endpointId.toString()); data.put("kind", selected.getKind().name());
        return audited(context, owner, "queryEndpointResources", true, success(data));
    }

    @Callback(doc = "function(token:string,drone:string,command:string):table -- START, STOP or RECALL")
    public Object[] controlDrone(Context context, Arguments arguments) {
        UUID owner = authorize(context, arguments, "controlDrone");
        if (owner == null) return failure("unauthorized");
        if (!OpenComputersComponentIds.DRONE_FLEET.equals(component)) {
            return audited(context, owner, "controlDrone", false, failure("wrong_component"));
        }
        String rawDrone = stringArg(arguments, 1), rawCommand = stringArg(arguments, 2);
        if (rawDrone == null || rawCommand == null) return audited(context, owner,
                "controlDrone", false, failure("invalid_arguments"));
        UUID droneId = parseUuid(rawDrone);
        String command = rawCommand.toUpperCase(Locale.ROOT);
        boolean ok = droneId != null && ("START".equals(command) || "STOP".equals(command)
                || "RECALL".equals(command)) && control(owner, droneId, command);
        return audited(context, owner, "controlDrone", ok, ok ? success(singleton("controlled", true))
                : failure("control_failed"));
    }

    @Callback(doc = "function(token:string,job:string):table -- cancel an owner job")
    public Object[] cancelJob(Context context, Arguments arguments) {
        UUID owner = authorize(context, arguments, "cancelJob");
        if (owner == null) return failure("unauthorized");
        String rawJob = stringArg(arguments, 1);
        if (rawJob == null) return audited(context, owner, "cancelJob", false,
                failure("invalid_arguments"));
        UUID jobId = parseUuid(rawJob);
        boolean ok = OpenComputersComponentIds.DRONE_FLEET.equals(component) && jobId != null
                && DroneFleetState.get(world).cancelJob(owner, jobId);
        return audited(context, owner, "cancelJob", ok, ok ? success(singleton("cancelled", true))
                : failure("cancel_failed"));
    }

    @Callback(doc = "function(token,kind,resource,amount,source,target,priority=0):table -- submit logistics")
    public Object[] submitLogistics(Context context, Arguments arguments) {
        UUID owner = authorize(context, arguments, "submitLogistics");
        if (owner == null) return failure("unauthorized");
        if (!OpenComputersComponentIds.DRONE_FLEET.equals(component)) {
            return audited(context, owner, "submitLogistics", false, failure("wrong_component"));
        }
        try {
            String rawKind = stringArg(arguments, 1), resource = stringArg(arguments, 2);
            Long requestedAmount = longArg(arguments, 3, null);
            String rawSource = stringArg(arguments, 4), rawTarget = stringArg(arguments, 5);
            Integer requestedPriority = intArg(arguments, 6, 0);
            if (rawKind == null || resource == null || requestedAmount == null || rawSource == null
                    || rawTarget == null || requestedPriority == null) return audited(context, owner,
                    "submitLogistics", false, failure("invalid_arguments"));
            DroneEndpoint.Kind kind = DroneEndpoint.Kind.valueOf(rawKind.toUpperCase(Locale.ROOT));
            long amount = requestedAmount;
            UUID source = parseUuid(rawSource);
            UUID target = parseUuid(rawTarget);
            int priority = Math.max(-100, Math.min(100, requestedPriority));
            if (resource.length() > 128 || amount <= 0L || source == null || target == null
                    || source.equals(target) || !ownsEndpoint(owner, source, kind) || !ownsEndpoint(owner, target, kind)) {
                return audited(context, owner, "submitLogistics", false, failure("invalid_request"));
            }
            UUID jobId = UUID.randomUUID();
            DroneJob job = DroneJob.logistics(jobId, owner, priority, world.getTotalWorldTime(),
                    20L * 60L, 5, 20L, kind, resource, amount, source, target);
            boolean ok = DroneFleetState.get(world).submitJob(owner, job);
            return audited(context, owner, "submitLogistics", ok,
                    ok ? success(singleton("job", jobId.toString())) : failure("submit_failed"));
        } catch (IllegalArgumentException exception) {
            return audited(context, owner, "submitLogistics", false, failure("invalid_request"));
        }
    }

    @Nullable
    private UUID authorize(Context context, Arguments arguments, String method) {
        UUID computer = caller(context);
        if (computer == null) return null;
        if (!LIMITER.tryAcquire(computer, world.getTotalWorldTime())) {
            state().audit(computer, component, method, false, world.getTotalWorldTime());
            return null;
        }
        String token;
        try { token = arguments.checkString(0); }
        catch (RuntimeException invalid) { state().audit(computer, component, method, false, world.getTotalWorldTime()); return null; }
        UUID owner = state().authenticate(dimension(), position, component, token);
        if (owner == null) state().audit(computer, component, method, false, world.getTotalWorldTime());
        return owner;
    }

    private Object[] audited(Context context, UUID owner, String method, boolean ok, Object[] result) {
        UUID computer = caller(context);
        if (computer != null) state().audit(computer, component, method, ok, world.getTotalWorldTime());
        return result;
    }

    @Nullable private MetaTileEntity machine() { return OpenComputersMachineAccess.getMachine(world, position); }
    private int dimension() { return world.provider.getDimension(); }
    private OpenComputersPairingState state() { return OpenComputersPairingState.get(world); }

    private boolean ownsEndpoint(UUID owner, UUID endpointId, DroneEndpoint.Kind kind) {
        for (DroneEndpoint endpoint : DroneEndpointNetwork.get(world).listForOwner(owner, kind)) {
            if (endpointId.equals(endpoint.getEndpointId())) return true;
        }
        return false;
    }

    private boolean control(UUID owner, UUID droneId, String command) {
        DroneRegistryRecord record = DroneRegistry.get(world).getRecord(droneId).orElse(null);
        if (record == null || !owner.equals(record.getOwnerId())) return false;
        MinecraftServer server = world.getMinecraftServer();
        WorldServer target = server == null ? null : server.getWorld(record.getDimension());
        if (target == null) return false;
        for (Entity entity : target.loadedEntityList) {
            if (entity instanceof EntityProgrammableDrone
                    && droneId.equals(((EntityProgrammableDrone) entity).getDroneId())) {
                return ((EntityProgrammableDrone) entity).handleFleetControl(owner, command);
            }
        }
        return false;
    }

    @Nullable
    private DroneProgramGraph resolveProgram(UUID owner, UUID programId, long revision) {
        for (DroneProgramLibraryRecord record : DroneProgramLibrary.get(world).listAccessibleForOwner(owner)) {
            if (programId.equals(record.getProgramId()) && (revision < 0L || revision == record.getRevision())) {
                return record.getGraph();
            }
        }
        return null;
    }

    private boolean assignProgram(UUID owner, UUID droneId, DroneProgramGraph graph) {
        DroneRegistryRecord record = DroneRegistry.get(world).getRecord(droneId).orElse(null);
        if (record == null || !owner.equals(record.getOwnerId())) return false;
        MinecraftServer server = world.getMinecraftServer();
        WorldServer target = server == null ? null : server.getWorld(record.getDimension());
        if (target == null) return false;
        for (Entity entity : target.loadedEntityList) {
            if (entity instanceof EntityProgrammableDrone
                    && droneId.equals(((EntityProgrammableDrone) entity).getDroneId())) {
                return ((EntityProgrammableDrone) entity).assignLibraryProgram(owner, graph);
            }
        }
        return false;
    }

    private void emitStateSignals(MetaTileEntity machine) {
        UUID owner = state().ownerFor(dimension(), position, component);
        if (owner == null) {
            lastDockDrone = null; lastDroneStates.clear(); lastJobStates.clear(); lowEnergyDrones.clear();
            return;
        }
        if (machine instanceof MetaTileEntityDroneDock) {
            UUID current = ((MetaTileEntityDroneDock) machine).getCurrentDroneId();
            String value = current == null ? "" : current.toString();
            if (lastDockDrone != null && !lastDockDrone.equals(value)) {
                if (value.isEmpty()) emit("drtech_drone_dock", lastDockDrone);
                else emit("drtech_drone_launch", value);
            }
            lastDockDrone = value;
            return;
        }
        if (!OpenComputersComponentIds.DRONE_FLEET.equals(component)) return;
        Map<UUID, String> currentDrones = new HashMap<>();
        for (DroneRegistryRecord record : DroneRegistry.get(world).listForOwner(owner)) {
            UUID id = record.getDroneId();
            String status = record.getStatus();
            currentDrones.put(id, status);
            String previous = lastDroneStates.get(id);
            if (previous != null && !previous.equals(status)) emit("drtech_drone_status",
                    id.toString(), status);
            if (previous != null && status.toUpperCase(Locale.ROOT).contains("ERROR")) {
                emit("drtech_drone_error", id.toString(), status);
            }
            boolean low = record.getEnergyCapacity() > 0L
                    && record.getEnergyStored() * 5L <= record.getEnergyCapacity();
            if (low && lowEnergyDrones.add(id)) emit("drtech_drone_low_energy", id.toString(),
                    record.getEnergyStored(), record.getEnergyCapacity());
            else if (!low) lowEnergyDrones.remove(id);
        }
        lastDroneStates.clear(); lastDroneStates.putAll(currentDrones);
        lowEnergyDrones.retainAll(currentDrones.keySet());

        Map<UUID, String> currentJobs = new HashMap<>();
        for (DroneJob job : DroneFleetState.get(world).getJobsForOwner(owner)) {
            UUID id = job.getJobId(); String status = job.getState().name();
            currentJobs.put(id, status);
            String previous = lastJobStates.get(id);
            if (previous != null && !previous.equals(status)) {
                if (job.getState() == DroneJob.State.COMPLETED) emit("drtech_drone_task_complete", id.toString());
                else if (job.getState() == DroneJob.State.FAILED) emit("drtech_drone_error",
                        id.toString(), job.getLastFailure());
            }
        }
        lastJobStates.clear(); lastJobStates.putAll(currentJobs);
    }

    private void emit(String signal, Object... values) {
        if (node() == null || node().network() == null) return;
        Object[] payload = new Object[values.length + 1]; payload[0] = signal;
        System.arraycopy(values, 0, payload, 1, values.length);
        node().sendToReachable("computer.signal", payload);
    }

    private static Map<String, Object> jobData(DroneJob job) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", job.getJobId().toString()); item.put("state", job.getState().name());
        item.put("priority", job.getPriority()); item.put("attempts", job.getAttempts());
        item.put("failure", job.getLastFailure()); item.put("logistics", job.isLogisticsJob());
        if (job.isLogisticsJob()) {
            item.put("kind", job.getResourceKind().name()); item.put("resource", job.getResourceId());
            item.put("requested", job.getRequestedAmount()); item.put("picked", job.getPickedAmount());
            item.put("delivered", job.getDeliveredAmount()); item.put("stage", job.getLogisticsStage().name());
            item.put("drone", job.getAssignedDroneId() == null ? "" : job.getAssignedDroneId().toString());
        }
        return item;
    }

    private static Map<String, Object> pageData(Collection<?> entries, int total, int page, int size) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("entries", entries); data.put("total", total); data.put("page", page); data.put("pageSize", size);
        data.put("pages", total == 0 ? 0 : (total + size - 1) / size); return data;
    }

    private static Map<String, Object> singleton(String key, Object value) {
        Map<String, Object> map = new LinkedHashMap<>(); map.put(key, value); return map;
    }

    private static Object[] success(Map<String, Object> data) { return result(true, "", data); }
    private static Object[] failure(String error) { return result(false, error, null); }
    private static Object[] result(boolean ok, String error, @Nullable Map<String, Object> data) {
        Map<String, Object> result = new LinkedHashMap<>(); result.put("ok", ok);
        if (ok) result.put("data", data == null ? new LinkedHashMap<>() : data); else result.put("error", error);
        return new Object[]{result};
    }

    @Nullable private static String stringArg(Arguments arguments, int index) {
        try { return arguments.checkString(index); } catch (RuntimeException invalid) { return null; }
    }

    @Nullable private static String optionalStringArg(Arguments arguments, int index, String fallback) {
        try { return arguments.optString(index, fallback); } catch (RuntimeException invalid) { return null; }
    }

    @Nullable private static Integer intArg(Arguments arguments, int index, int fallback) {
        try { return arguments.optInteger(index, fallback); } catch (RuntimeException invalid) { return null; }
    }

    @Nullable private static Long longArg(Arguments arguments, int index, @Nullable Long fallback) {
        try {
            if (arguments.count() <= index) return fallback;
            return arguments.checkLong(index);
        } catch (RuntimeException invalid) { return null; }
    }

    @Nullable private static UUID parseUuid(String value) {
        try { return UUID.fromString(value); } catch (IllegalArgumentException ignored) { return null; }
    }

    @Nullable private static UUID caller(Context context) {
        if (context == null || context.node() == null || context.node().address() == null) return null;
        return UUID.nameUUIDFromBytes(context.node().address().getBytes(StandardCharsets.UTF_8));
    }
}
