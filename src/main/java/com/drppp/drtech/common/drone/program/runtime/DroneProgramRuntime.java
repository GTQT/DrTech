package com.drppp.drtech.common.drone.program.runtime;

import com.drppp.drtech.common.drone.program.compile.CompiledDroneProgram;
import com.drppp.drtech.common.drone.program.model.DroneProgramEdge;
import com.drppp.drtech.common.drone.program.model.DroneProgramNode;
import com.drppp.drtech.common.drone.program.registry.DrTechDroneNodes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/** Server-side, tick-budgeted state machine. It never mutates the source graph. */
public final class DroneProgramRuntime {

    public static final int MAX_IMMEDIATE_TRANSITIONS_PER_TICK = 32;
    public static final int MAX_TRACE_ENTRIES = 24;

    private final CompiledDroneProgram program;
    private final DroneExecutorRegistry executors;
    private final DroneValueEvaluatorRegistry valueEvaluators;
    private final DroneRuntimeEnvironment environment;
    private UUID currentNodeId;
    private DroneRuntimeStatus status = DroneRuntimeStatus.READY;
    private NBTTagCompound nodeState = new NBTTagCompound();
    private final DroneRuntimeMemory memory = new DroneRuntimeMemory();
    private final Deque<String> trace = new ArrayDeque<>();
    private UUID breakpointBypassNode;
    private boolean singleStepRequested;
    private String error = "";

    public DroneProgramRuntime(CompiledDroneProgram program, DroneExecutorRegistry executors) {
        this(program, executors, DrTechDroneValueEvaluators.createDefaultRegistry(), DroneRuntimeEnvironment.EMPTY);
    }

    public DroneProgramRuntime(CompiledDroneProgram program, DroneExecutorRegistry executors,
            DroneValueEvaluatorRegistry valueEvaluators, DroneRuntimeEnvironment environment) {
        this.program = program;
        this.executors = executors;
        this.valueEvaluators = valueEvaluators;
        this.environment = environment;
        this.currentNodeId = program.getEntryNodeId();
    }

    public void tick() {
        if (status == DroneRuntimeStatus.COMPLETED || status == DroneRuntimeStatus.ERROR
                || status == DroneRuntimeStatus.PAUSED) {
            return;
        }
        status = DroneRuntimeStatus.RUNNING;
        for (int step = 0; step < MAX_IMMEDIATE_TRANSITIONS_PER_TICK; step++) {
            DroneProgramNode node = program.getNode(currentNodeId);
            if (node == null) {
                fail("Current node is missing: " + currentNodeId);
                return;
            }
            if (node.getConfiguration().getBoolean("Breakpoint") && !node.getId().equals(breakpointBypassNode)) {
                status = DroneRuntimeStatus.PAUSED;
                addTrace("BREAK " + node.getType().getPath());
                return;
            }
            if (node.getType().equals(DrTechDroneNodes.END)) {
                status = DroneRuntimeStatus.COMPLETED;
                addTrace("END");
                return;
            }

            DroneExecutionResult result;
            if (node.getType().equals(DrTechDroneNodes.START)) {
                result = DroneExecutionResult.success();
            } else {
                DroneNodeExecutor executor = executors.get(node.getType());
                if (executor == null) {
                    fail("No executor registered for " + node.getType());
                    return;
                }
                try {
                    result = executor.tick(new DroneNodeExecutionContext(node, nodeState, environment, memory,
                            new DroneNodeExecutionContext.InputResolver() {
                                @Override
                                public <T> T resolve(String portId, Class<T> type) {
                                    return resolveInput(node.getId(), portId, type, new HashSet<>(), 0);
                                }
                            }));
                } catch (RuntimeException exception) {
                    fail("Executor failed for " + node.getType() + ": " + exception.getMessage());
                    return;
                }
            }

            memory.setLastAction(result.getStatus(), result.getError());
            if (result.getState() != DroneActionState.RUNNING) {
                memory.setActionAmount(node.getId(), result.getAmount());
            }
            if (result.getState() == DroneActionState.RUNNING) return;
            if (result.getState() == DroneActionState.PAUSED) {
                status = DroneRuntimeStatus.PAUSED;
                addTrace(node.getType().getPath() + " PAUSED");
                return;
            }
            if (result.getState() == DroneActionState.ERROR) {
                fail(result.getError());
                return;
            }
            if (result.getState() == DroneActionState.FAILURE && result.getOutputPort() == null) {
                fail("Node failed without a failure output: " + currentNodeId);
                return;
            }
            addTrace(node.getType().getPath() + " -> " + result.getOutputPort());
            if (!advance(result.getOutputPort())) return;
            breakpointBypassNode = null;
            if (singleStepRequested) {
                singleStepRequested = false;
                status = DroneRuntimeStatus.PAUSED;
                return;
            }
            if ((node.getType().equals(DrTechDroneNodes.REPEAT) || node.getType().equals(DrTechDroneNodes.WHILE)
                    || node.getType().equals(DrTechDroneNodes.FOR_EACH_COORDINATE))
                    && "body".equals(result.getOutputPort())) return;
        }
        fail("Immediate transition budget exceeded");
    }

    private <T> T resolveInput(UUID targetNodeId, String targetPort, Class<T> expectedType, Set<String> visiting,
            int depth) {
        if (depth >= 32) throw new IllegalStateException("Value dependency depth exceeded");
        List<DroneProgramEdge> incoming = program.getIncoming(targetNodeId, targetPort);
        if (incoming.isEmpty()) return null;
        if (incoming.size() != 1) throw new IllegalStateException("Value input has multiple connections: " + targetPort);
        DroneProgramEdge edge = incoming.get(0);
        String address = edge.getSourceNodeId() + ":" + edge.getSourcePortId();
        if (!visiting.add(address)) throw new IllegalStateException("Cyclic value dependency at " + address);
        try {
            DroneProgramNode source = program.getNode(edge.getSourceNodeId());
            if (source == null) throw new IllegalStateException("Value source node is missing");
            DroneValueEvaluator evaluator = valueEvaluators.get(source.getType());
            if (evaluator == null) throw new IllegalStateException("No value evaluator for " + source.getType());
            DroneNodeExecutionContext.InputResolver resolver = new DroneNodeExecutionContext.InputResolver() {
                @Override
                public <V> V resolve(String portId, Class<V> type) {
                    return resolveInput(source.getId(), portId, type, visiting, depth + 1);
                }
            };
            Object value = evaluator.evaluate(new DroneValueEvaluationContext(source, environment, memory, resolver),
                    edge.getSourcePortId());
            if (value == null) return null;
            if (!expectedType.isInstance(value)) {
                throw new IllegalStateException("Value type mismatch: expected " + expectedType.getSimpleName()
                        + ", got " + value.getClass().getSimpleName());
            }
            return expectedType.cast(value);
        } finally {
            visiting.remove(address);
        }
    }

    private boolean advance(String outputPort) {
        if (outputPort == null || outputPort.isEmpty()) {
            fail("Node did not select an output port: " + currentNodeId);
            return false;
        }
        List<DroneProgramEdge> edges = program.getOutgoing(currentNodeId, outputPort);
        if (edges.size() != 1) {
            fail("Expected one flow edge from " + currentNodeId + ":" + outputPort + ", got " + edges.size());
            return false;
        }
        currentNodeId = edges.get(0).getTargetNodeId();
        nodeState = new NBTTagCompound();
        return true;
    }

    public void pause() {
        if (status == DroneRuntimeStatus.RUNNING || status == DroneRuntimeStatus.READY) {
            status = DroneRuntimeStatus.PAUSED;
        }
    }

    public void resume() {
        if (status == DroneRuntimeStatus.PAUSED) {
            breakpointBypassNode = currentNodeId;
            status = DroneRuntimeStatus.RUNNING;
        }
    }

    public boolean requestSingleStep() {
        if (status == DroneRuntimeStatus.PAUSED || status == DroneRuntimeStatus.READY) {
            breakpointBypassNode = currentNodeId;
            singleStepRequested = true;
            status = DroneRuntimeStatus.RUNNING;
            return true;
        }
        return false;
    }

    public void start() {
        if (status == DroneRuntimeStatus.READY) status = DroneRuntimeStatus.RUNNING;
    }

    /**
     * Activates a valid program when an item is deployed from a dock.
     * A manually recalled PAUSED runtime keeps its node memory and resumes in place; a completed flight starts a
     * fresh cycle. ERROR is deliberately preserved so a broken program cannot enter a launch/error/recovery loop.
     */
    public boolean activateForDockLaunch() {
        switch (status) {
            case READY -> start();
            case PAUSED -> resume();
            case COMPLETED -> restart();
            case RUNNING -> { return true; }
            case ERROR -> { return false; }
        }
        return status == DroneRuntimeStatus.RUNNING;
    }

    public void stop() {
        reset(DroneRuntimeStatus.READY);
    }

    public void restart() {
        reset(DroneRuntimeStatus.RUNNING);
    }

    public void resetError() {
        if (status == DroneRuntimeStatus.ERROR) reset(DroneRuntimeStatus.READY);
    }

    private void reset(DroneRuntimeStatus newStatus) {
        currentNodeId = program.getEntryNodeId();
        nodeState = new NBTTagCompound();
        memory.clear();
        breakpointBypassNode = null;
        singleStepRequested = false;
        error = "";
        status = newStatus;
    }

    public DroneRuntimeStatus getStatus() {
        return status;
    }

    public UUID getCurrentNodeId() {
        return currentNodeId;
    }

    public String getError() {
        return error;
    }

    public String getProgramName() {
        return program.getName();
    }

    public long getProgramRevision() {
        return program.getSourceRevision();
    }

    public UUID getProgramId() {
        return program.getProgramId();
    }

    public String getCurrentNodeType() {
        DroneProgramNode node = program.getNode(currentNodeId);
        return node == null ? "missing" : node.getType().getPath();
    }

    public String getCurrentNodeProgress() {
        if (nodeState.hasKey("AreaTotal")) {
            return nodeState.getInteger("AreaIndex") + "/" + nodeState.getInteger("AreaTotal");
        }
        if (nodeState.hasKey("Remaining")) return Integer.toString(nodeState.getInteger("Remaining"));
        return "";
    }

    public double getNumberVariable(String name) {
        return memory.getNumber(name);
    }

    public long getActionAmount(UUID nodeId) {
        return memory.getActionAmount(nodeId);
    }

    public String getVariableSummary(int maxEntries) {
        return memory.getNumberSummary(maxEntries);
    }

    public String getTraceSummary(int maxEntries) {
        if (trace.isEmpty() || maxEntries <= 0) return "No execution trace";
        StringBuilder builder = new StringBuilder();
        int skip = Math.max(0, trace.size() - maxEntries);
        int index = 0;
        for (String entry : trace) {
            if (index++ < skip) continue;
            if (builder.length() > 0) builder.append('\n');
            builder.append(entry);
        }
        return builder.toString();
    }

    public void clearTrace() {
        trace.clear();
    }

    private void addTrace(String entry) {
        while (trace.size() >= MAX_TRACE_ENTRIES) trace.removeFirst();
        trace.addLast(entry == null ? "unknown" : entry);
    }

    private void fail(String message) {
        status = DroneRuntimeStatus.ERROR;
        error = message == null ? "unknown" : message;
        addTrace("ERROR " + error);
    }

    public NBTTagCompound writeToNbt() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("ProgramId", program.getProgramId().toString());
        tag.setLong("ProgramRevision", program.getSourceRevision());
        tag.setString("CurrentNode", currentNodeId.toString());
        tag.setString("Status", status.name());
        tag.setTag("NodeState", nodeState.copy());
        tag.setTag("Memory", memory.writeToNbt());
        NBTTagList traceTag = new NBTTagList();
        for (String entry : trace) traceTag.appendTag(new NBTTagString(entry));
        tag.setTag("Trace", traceTag);
        tag.setString("Error", error);
        return tag;
    }

    public void readFromNbt(NBTTagCompound tag) {
        if (!program.getProgramId().toString().equals(tag.getString("ProgramId"))
                || program.getSourceRevision() != tag.getLong("ProgramRevision")) {
            fail("Saved runtime belongs to a different program revision");
            return;
        }
        try {
            UUID savedNode = UUID.fromString(tag.getString("CurrentNode"));
            if (program.getNode(savedNode) == null) {
                fail("Saved current node is missing");
                return;
            }
            currentNodeId = savedNode;
            status = DroneRuntimeStatus.valueOf(tag.getString("Status"));
            nodeState = tag.getCompoundTag("NodeState").copy();
            memory.readFromNbt(tag.getCompoundTag("Memory"));
            trace.clear();
            NBTTagList traceTag = tag.getTagList("Trace", 8);
            int first = Math.max(0, traceTag.tagCount() - MAX_TRACE_ENTRIES);
            for (int i = first; i < traceTag.tagCount(); i++) trace.addLast(traceTag.getStringTagAt(i));
            error = tag.getString("Error");
        } catch (IllegalArgumentException exception) {
            fail("Saved runtime state is malformed");
        }
    }
}
