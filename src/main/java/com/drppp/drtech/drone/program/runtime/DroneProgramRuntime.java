package com.drppp.drtech.drone.program.runtime;

import com.drppp.drtech.drone.api.DroneExtensionRegistry;

import com.drppp.drtech.drone.program.compile.CompiledDroneProgram;
import com.drppp.drtech.drone.program.compile.DroneProgramHardwareValidator;
import com.drppp.drtech.drone.hardware.DroneUpgradeType;
import com.drppp.drtech.drone.program.model.DroneProgramEdge;
import com.drppp.drtech.drone.program.model.DroneProgramNode;
import com.drppp.drtech.drone.program.model.DroneArea;
import com.drppp.drtech.drone.program.model.DroneProgramReference;
import com.drppp.drtech.drone.program.registry.DrTechDroneNodes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/** Server-side, tick-budgeted state machine. It never mutates the source graph. */
public final class DroneProgramRuntime {
    private static final int AREA_BUILD_CANDIDATES_PER_TICK = 1_024;

    public static final int MAX_IMMEDIATE_TRANSITIONS_PER_TICK = 32;
    public static final int MAX_TRACE_ENTRIES = 256;

    private final CompiledDroneProgram rootProgram;
    /** The currently executing program; differs from rootProgram only while inside a subprogram call. */
    private CompiledDroneProgram program;
    private final DroneExecutorRegistry executors;
    private final DroneValueEvaluatorRegistry valueEvaluators;
    private final DroneRuntimeEnvironment environment;
    private final Map<String, DroneArea> pendingAreaValues = new HashMap<>();
    private int areaBuildBudgetRemaining;
    private long areaBuildProgress;
    private long areaBuildTotal;
    private UUID currentNodeId;
    private DroneRuntimeStatus status = DroneRuntimeStatus.READY;
    private NBTTagCompound nodeState = new NBTTagCompound();
    private final DroneRuntimeMemory memory = new DroneRuntimeMemory();
    private final Deque<String> trace = new ArrayDeque<>();
    private final Deque<CallFrame> callStack = new ArrayDeque<>();
    /** Parallel node ids keep persisted traces clickable without changing the legacy text trace format. */
    private final Deque<String> traceNodeIds = new ArrayDeque<>();
    private final Deque<Long> traceTicks = new ArrayDeque<>();
    private long traceSequence;
    private UUID breakpointBypassNode;
    private boolean singleStepRequested;
    /** Caller stack depth to pause at after a step-over subprogram returns; -1 when inactive. */
    private int stepOverCallDepth = -1;
    /**
     * A result that has already changed the world/memory but is waiting at a conditional breakpoint.  Keeping this
     * transition is important: resuming a breakpoint after an export, harvest or variable write must never execute
     * that action a second time.
     */
    private DroneActionState pendingBreakpointState;
    private DroneActionStatus pendingBreakpointStatus;
    private String pendingBreakpointOutput = "";
    private String pendingBreakpointError = "";
    private long pendingBreakpointAmount;
    private String error = "";
    /** Number of server ticks spent in the currently selected node. */
    private long currentNodeElapsedTicks;
    /** Last values actually requested by the active node executor, bounded for remote debugging. */
    private UUID debugInputNodeId;
    private String debugInputNodeType = "";
    private final Map<String, String> currentNodeInputs = new LinkedHashMap<>();
    private String lastOutputNodeType = "";
    private String lastOutputPort = "";
    private long lastOutputAmount;

    public DroneProgramRuntime(CompiledDroneProgram program, DroneExecutorRegistry executors) {
        this(program, executors, DroneExtensionRegistry.sensors(), DroneRuntimeEnvironment.EMPTY);
    }

    public DroneProgramRuntime(CompiledDroneProgram program, DroneExecutorRegistry executors,
            DroneValueEvaluatorRegistry valueEvaluators, DroneRuntimeEnvironment environment) {
        this.rootProgram = program;
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
        areaBuildBudgetRemaining = AREA_BUILD_CANDIDATES_PER_TICK;
        if (pendingBreakpointState != null) {
            continuePendingBreakpoint();
            return;
        }
        for (int step = 0; step < MAX_IMMEDIATE_TRANSITIONS_PER_TICK; step++) {
            DroneProgramNode node = program.getNode(currentNodeId);
            if (node == null) {
                fail("Current node is missing: " + currentNodeId);
                return;
            }
            prepareDebugInputs(node);
            if (node.getConfiguration().getBoolean("Breakpoint") && !node.getId().equals(breakpointBypassNode)) {
                status = DroneRuntimeStatus.PAUSED;
                addTrace("BREAK " + node.getType().getPath());
                return;
            }
            int lowEnergyThreshold = node.getConfiguration().getInteger("BreakpointLowEnergy");
            if (lowEnergyThreshold > 0 && !node.getId().equals(breakpointBypassNode)
                    && environment.getEnergyPercent() * 100.0D <= lowEnergyThreshold) {
                status = DroneRuntimeStatus.PAUSED;
                addTrace("BREAK LOW_ENERGY " + lowEnergyThreshold + "%");
                return;
            }
            if (node.getType().equals(DrTechDroneNodes.END)) {
                if (!callStack.isEmpty()) {
                    returnFromSubprogram();
                    // A "step over" pauses immediately after returning to the caller.  Do
                    // not let this tick consume the caller's next node as well.
                    if (status != DroneRuntimeStatus.RUNNING) {
                        return;
                    }
                    continue;
                }
                status = DroneRuntimeStatus.COMPLETED;
                addTrace("END");
                return;
            }

            DroneUpgradeType missingUpgrade = DroneProgramHardwareValidator.getFirstMissingUpgrade(
                    node.getType(), environment::hasUpgrade);
            if (missingUpgrade != null) {
                fail("Required drone upgrade is missing: " + missingUpgrade.getId());
                return;
            }

            if (node.getType().equals(DrTechDroneNodes.CALL_PROGRAM)) {
                DroneProgramReference reference;
                try {
                    reference = resolveInput(node.getId(), "program", DroneProgramReference.class,
                            new HashSet<>(), 0);
                } catch (DroneAreaBuildPendingException pending) {
                    currentNodeElapsedTicks = Math.min(Long.MAX_VALUE - 1L, currentNodeElapsedTicks + 1L);
                    memory.setLastAction(DroneActionStatus.RUNNING, "");
                    return;
                } catch (RuntimeException exception) {
                    fail("Subprogram reference failed: " + exception.getMessage());
                    return;
                }
                if (!enterSubprogram(node, reference)) return;
                continue;
            }

            if (node.getType().equals(DrTechDroneNodes.BREAK_LOOP)) {
                if (!breakCurrentLoop()) return;
                continue;
            }
            if (node.getType().equals(DrTechDroneNodes.CONTINUE_LOOP)) {
                if (!continueCurrentLoop()) return;
                continue;
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
                                    T value = resolveInput(node.getId(), portId, type, new HashSet<>(), 0);
                                    recordDebugInput(portId, value);
                                    return value;
                                }
                            }));
                } catch (RuntimeException exception) {
                    fail("Executor failed for " + node.getType() + ": " + exception.getMessage());
                    return;
                }
            }

            if (result.getState() != DroneActionState.RUNNING) environment.auditAction(node.getType(), result);
            memory.setLastAction(result.getStatus(), result.getError());
            recordDebugOutput(node, result);
            currentNodeElapsedTicks = Math.min(Long.MAX_VALUE - 1L, currentNodeElapsedTicks + 1L);
            if (result.getState() != DroneActionState.RUNNING) {
                memory.setActionAmount(node.getId(), result.getAmount());
            }
            if (result.getState() == DroneActionState.RUNNING) return;
            if (result.getState() == DroneActionState.PAUSED) {
                status = DroneRuntimeStatus.PAUSED;
                addTrace(node.getType().getPath() + " PAUSED");
                return;
            }
            if (shouldPauseAfterResult(node, result)) {
                pauseAfterResult(node, result);
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
            updateLoopStack(node, result.getOutputPort());
            if (!advance(result.getOutputPort())) return;
            breakpointBypassNode = null;
            if (singleStepRequested) {
                singleStepRequested = false;
                status = DroneRuntimeStatus.PAUSED;
                return;
            }
            if ((node.getType().equals(DrTechDroneNodes.REPEAT) || node.getType().equals(DrTechDroneNodes.WHILE)
                    || node.getType().equals(DrTechDroneNodes.FOR_EACH_COORDINATE)
                    || node.getType().equals(DrTechDroneNodes.FOR_EACH_ITEM_FILTER))
                    && "body".equals(result.getOutputPort())) return;
        }
        fail("Immediate transition budget exceeded");
    }

    private boolean shouldPauseAfterResult(DroneProgramNode node, DroneExecutionResult result) {
        if (node.getId().equals(breakpointBypassNode)) return false;
        if (node.getConfiguration().getBoolean("BreakpointOnFailure")) {
            return result.getState() == DroneActionState.FAILURE || result.getState() == DroneActionState.ERROR;
        }
        return node.getConfiguration().getBoolean("BreakpointOnVariableWrite")
                && result.getState() == DroneActionState.SUCCESS
                && (node.getType().equals(DrTechDroneNodes.SET_NUMBER_VARIABLE)
                || node.getType().equals(DrTechDroneNodes.ADD_NUMBER_VARIABLE)
                || node.getType().equals(DrTechDroneNodes.SET_STRING_VARIABLE));
    }

    private void pauseAfterResult(DroneProgramNode node, DroneExecutionResult result) {
        pendingBreakpointState = result.getState();
        pendingBreakpointStatus = result.getStatus();
        pendingBreakpointOutput = result.getOutputPort() == null ? "" : result.getOutputPort();
        pendingBreakpointError = result.getError() == null ? "" : result.getError();
        pendingBreakpointAmount = result.getAmount();
        status = DroneRuntimeStatus.PAUSED;
        if (node.getConfiguration().getBoolean("BreakpointOnFailure")) {
            addTrace("BREAK FAILURE " + result.getStatus().name());
        } else {
            String variable = node.getConfiguration().getString("Name");
            addTrace("BREAK VARIABLE " + (variable.isEmpty() ? "value" : variable));
        }
    }

    /** Completes a world result that was deliberately paused for inspection. */
    private void continuePendingBreakpoint() {
        DroneActionState state = pendingBreakpointState;
        String output = pendingBreakpointOutput;
        String pendingError = pendingBreakpointError;
        clearPendingBreakpoint();
        if (state == DroneActionState.ERROR) {
            fail(pendingError);
            return;
        }
        if (state == DroneActionState.FAILURE && output.isEmpty()) {
            fail("Node failed without a failure output: " + currentNodeId);
            return;
        }
        DroneProgramNode node = program.getNode(currentNodeId);
        if (node == null) {
            fail("Current node is missing: " + currentNodeId);
            return;
        }
        addTrace(node.getType().getPath() + " -> " + output);
        advance(output);
        breakpointBypassNode = null;
    }

    private void clearPendingBreakpoint() {
        pendingBreakpointState = null;
        pendingBreakpointStatus = null;
        pendingBreakpointOutput = "";
        pendingBreakpointError = "";
        pendingBreakpointAmount = 0L;
    }

    private boolean enterSubprogram(DroneProgramNode callNode, DroneProgramReference reference) {
        if (reference == null) {
            fail("Subprogram reference is not connected");
            return false;
        }
        if (callStack.size() >= 16) {
            fail("Subprogram call depth exceeds 16");
            return false;
        }
        if (sameProgram(program, reference) || isProgramOnStack(reference)) {
            fail("Recursive subprogram call is not allowed: " + reference.getProgramId());
            return false;
        }
        CompiledDroneProgram resolved = environment.resolveProgram(reference);
        if (resolved == null || !sameProgram(resolved, reference)) {
            fail("Subprogram is unavailable, unauthorized, or revision-locked differently");
            return false;
        }
        Number numberInput;
        String stringInput;
        try {
            numberInput = resolveInput(callNode.getId(), "number_input", Number.class, new HashSet<>(), 0);
            stringInput = resolveInput(callNode.getId(), "string_input", String.class, new HashSet<>(), 0);
        } catch (RuntimeException exception) {
            fail("Subprogram parameter failed: " + exception.getMessage());
            return false;
        }
        callStack.push(new CallFrame(program, callNode.getId()));
        memory.pushLocalScope();
        NBTTagCompound configuration = callNode.getConfiguration();
        if (numberInput != null) {
            memory.setNumber(parameterName(configuration, "NumberInputName", "number_input"),
                    numberInput.doubleValue(), true);
        }
        if (stringInput != null) {
            memory.setString(parameterName(configuration, "StringInputName", "string_input"), stringInput, true);
        }
        program = resolved;
        clearAreaValueCache();
        currentNodeId = resolved.getEntryNodeId();
        nodeState = new NBTTagCompound();
        currentNodeElapsedTicks = 0L;
        breakpointBypassNode = null;
        addTrace("CALL " + (reference.getName().isEmpty() ? reference.getProgramId() : reference.getName()));
        return true;
    }

    private void returnFromSubprogram() {
        CallFrame frame = callStack.pop();
        DroneProgramNode callNode = frame.callerProgram.getNode(frame.callNodeId);
        if (callNode == null) {
            fail("Subprogram caller node is missing");
            return;
        }
        NBTTagCompound configuration = callNode.getConfiguration();
        memory.setCallNumberOutput(frame.callNodeId, memory.getNumber(
                parameterName(configuration, "NumberOutputName", "number_output"), true));
        memory.setCallStringOutput(frame.callNodeId, memory.getString(
                parameterName(configuration, "StringOutputName", "string_output"), true));
        memory.popLocalScope();
        program = frame.callerProgram;
        clearAreaValueCache();
        currentNodeId = frame.callNodeId;
        nodeState = new NBTTagCompound();
        currentNodeElapsedTicks = 0L;
        addTrace("RETURN " + program.getName());
        if (!advance("next")) return;
        breakpointBypassNode = null;
        if (stepOverCallDepth >= 0 && callStack.size() == stepOverCallDepth) {
            stepOverCallDepth = -1;
            status = DroneRuntimeStatus.PAUSED;
        }
    }

    private boolean isProgramOnStack(DroneProgramReference reference) {
        for (CallFrame frame : callStack) if (sameProgram(frame.callerProgram, reference)) return true;
        return false;
    }

    private static boolean sameProgram(CompiledDroneProgram candidate, DroneProgramReference reference) {
        return candidate != null && reference != null && candidate.getProgramId().equals(reference.getProgramId())
                && candidate.getSourceRevision() == reference.getRevision();
    }

    private static String parameterName(NBTTagCompound configuration, String key, String fallback) {
        String configured = configuration.getString(key);
        return DroneRuntimeMemory.isValidName(configured) ? configured : fallback;
    }

    private <T> T resolveInput(UUID targetNodeId, String targetPort, Class<T> expectedType, Set<String> visiting,
            int depth) {
        if (depth >= 32) throw new IllegalStateException("Value dependency depth exceeded");
        List<DroneProgramEdge> incoming = program.getIncoming(targetNodeId, targetPort);
        if (incoming.isEmpty()) return null;
        if (incoming.size() != 1) throw new IllegalStateException("Value input has multiple connections: " + targetPort);
        DroneProgramEdge edge = incoming.get(0);
        String address = program.getProgramId() + ":" + edge.getSourceNodeId() + ":" + edge.getSourcePortId();
        if (!visiting.add(address)) throw new IllegalStateException("Cyclic value dependency at " + address);
        try {
            DroneProgramNode source = program.getNode(edge.getSourceNodeId());
            if (source == null) throw new IllegalStateException("Value source node is missing");
            DroneUpgradeType missingUpgrade = DroneProgramHardwareValidator.getFirstMissingUpgrade(
                    source.getType(), environment::hasUpgrade);
            if (missingUpgrade != null) {
                throw new IllegalStateException("Required drone upgrade is missing: " + missingUpgrade.getId());
            }
            DroneValueEvaluator evaluator = valueEvaluators.get(source.getType());
            if (evaluator == null) throw new IllegalStateException("No value evaluator for " + source.getType());
            DroneArea cachedArea = pendingAreaValues.get(address);
            if (cachedArea != null && expectedType.isInstance(cachedArea)) {
                prepareAreaValue(cachedArea);
                return expectedType.cast(cachedArea);
            }
            DroneNodeExecutionContext.InputResolver resolver = new DroneNodeExecutionContext.InputResolver() {
                @Override
                public <V> V resolve(String portId, Class<V> type) {
                    return resolveInput(source.getId(), portId, type, visiting, depth + 1);
                }
            };
            Object value = evaluator.evaluate(new DroneValueEvaluationContext(source, environment, memory, resolver),
                    edge.getSourcePortId());
            if (value == null) return null;
            if (value instanceof DroneArea) {
                DroneArea area = (DroneArea) value;
                pendingAreaValues.put(address, area);
                prepareAreaValue(area);
                value = area;
            }
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
        pendingAreaValues.clear();
        areaBuildProgress = 0L;
        areaBuildTotal = 0L;
        nodeState = new NBTTagCompound();
        currentNodeElapsedTicks = 0L;
        return true;
    }

    private void prepareAreaValue(DroneArea area) {
        if (!area.isMaterialized()) {
            int processed = area.materializeStep(areaBuildBudgetRemaining);
            areaBuildBudgetRemaining = Math.max(0, areaBuildBudgetRemaining - processed);
            areaBuildProgress = area.getMaterializationProgress();
            areaBuildTotal = area.getMaterializationTotal();
            if (!area.isMaterialized()) throw DroneAreaBuildPendingException.INSTANCE;
        }
        int limit = Math.max(1, Math.min(DroneArea.MAX_BLOCKS, environment.getAreaBlockLimit()));
        if (area.getVolume() > limit) {
            throw new IllegalStateException("Area contains " + area.getVolume()
                    + " coordinates, but this chassis supports " + limit);
        }
    }

    private void clearAreaValueCache() {
        pendingAreaValues.clear();
        areaBuildProgress = 0L;
        areaBuildTotal = 0L;
    }

    private void updateLoopStack(DroneProgramNode node, String outputPort) {
        if (!isLoopNode(node)) return;
        if ("body".equals(outputPort)) memory.enterLoop(program.getProgramId(), node.getId());
        else if ("done".equals(outputPort)) memory.leaveLoop(program.getProgramId(), node.getId());
    }

    private boolean continueCurrentLoop() {
        UUID loopId = memory.getCurrentLoop(program.getProgramId());
        if (loopId == null || !isLoopNode(program.getNode(loopId))) {
            fail("Continue Loop is not inside an active loop");
            return false;
        }
        addTrace("continue_loop -> " + loopId);
        jumpTo(loopId);
        return true;
    }

    private boolean breakCurrentLoop() {
        UUID loopId = memory.getCurrentLoop(program.getProgramId());
        if (loopId == null || !isLoopNode(program.getNode(loopId))) {
            fail("Break Loop is not inside an active loop");
            return false;
        }
        List<DroneProgramEdge> edges = program.getOutgoing(loopId, "done");
        if (edges.size() != 1) {
            fail("Expected one flow edge from " + loopId + ":done, got " + edges.size());
            return false;
        }
        memory.popCurrentLoop(program.getProgramId());
        memory.clearLoop(loopId);
        memory.clearCurrentItemFilter(loopId);
        addTrace("break_loop -> " + loopId + ":done");
        jumpTo(edges.get(0).getTargetNodeId());
        return true;
    }

    private void jumpTo(UUID nodeId) {
        clearAreaValueCache();
        currentNodeId = nodeId;
        nodeState = new NBTTagCompound();
        currentNodeElapsedTicks = 0L;
        breakpointBypassNode = null;
    }

    private static boolean isLoopNode(DroneProgramNode node) {
        if (node == null) return false;
        return node.getType().equals(DrTechDroneNodes.REPEAT)
                || node.getType().equals(DrTechDroneNodes.WHILE)
                || node.getType().equals(DrTechDroneNodes.FOR_EACH_COORDINATE)
                || node.getType().equals(DrTechDroneNodes.FOR_EACH_ITEM_FILTER);
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
        return requestSingleStepInto();
    }

    /** Executes one node; at a call node this enters the referenced program and pauses at its first completed node. */
    public boolean requestSingleStepInto() {
        if (status == DroneRuntimeStatus.PAUSED || status == DroneRuntimeStatus.READY) {
            breakpointBypassNode = currentNodeId;
            singleStepRequested = true;
            stepOverCallDepth = -1;
            status = DroneRuntimeStatus.RUNNING;
            return true;
        }
        return false;
    }

    /** Runs a called subprogram to its return point, then pauses on the next node in the caller. */
    public boolean requestSingleStepOver() {
        if (status != DroneRuntimeStatus.PAUSED && status != DroneRuntimeStatus.READY) return false;
        DroneProgramNode node = program.getNode(currentNodeId);
        if (node == null || !node.getType().equals(DrTechDroneNodes.CALL_PROGRAM)) return requestSingleStepInto();
        breakpointBypassNode = currentNodeId;
        singleStepRequested = false;
        stepOverCallDepth = callStack.size();
        status = DroneRuntimeStatus.RUNNING;
        return true;
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
        program = rootProgram;
        callStack.clear();
        currentNodeId = rootProgram.getEntryNodeId();
        nodeState = new NBTTagCompound();
        currentNodeElapsedTicks = 0L;
        debugInputNodeId = null;
        debugInputNodeType = "";
        currentNodeInputs.clear();
        lastOutputNodeType = "";
        lastOutputPort = "";
        lastOutputAmount = 0L;
        clearAreaValueCache();
        clearPendingBreakpoint();
        memory.clear();
        breakpointBypassNode = null;
        singleStepRequested = false;
        stepOverCallDepth = -1;
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
        return rootProgram.getName();
    }

    public long getProgramRevision() {
        return rootProgram.getSourceRevision();
    }

    public UUID getProgramId() {
        return rootProgram.getProgramId();
    }

    public String getCurrentNodeType() {
        DroneProgramNode node = program.getNode(currentNodeId);
        return node == null ? "missing" : node.getType().getPath();
    }

    public String getCurrentNodeProgress() {
        if (areaBuildTotal > 0L && areaBuildProgress < areaBuildTotal) {
            return "AREA " + areaBuildProgress + "/" + areaBuildTotal;
        }
        if (nodeState.hasKey("AreaTotal")) {
            return nodeState.getInteger("AreaIndex") + "/" + nodeState.getInteger("AreaTotal");
        }
        if (nodeState.hasKey("Remaining")) return Integer.toString(nodeState.getInteger("Remaining"));
        return "";
    }

    /** Zero-based current region position; -1 means that the active node is not iterating an area. */
    public int getCurrentAreaIndex() {
        return nodeState.hasKey("AreaTotal") ? Math.max(0, nodeState.getInteger("AreaIndex")) : -1;
    }

    /** Total region positions for the active area iterator; 0 means no active area iteration. */
    public int getCurrentAreaTotal() {
        return nodeState.hasKey("AreaTotal") ? Math.max(0, nodeState.getInteger("AreaTotal")) : 0;
    }

    /** Current coordinate selected by the innermost active coordinate loop, including nested body nodes. */
    public BlockPos getCurrentLoopCoordinate() {
        UUID loopId = memory.getCurrentLoop(program.getProgramId());
        DroneProgramNode loop = loopId == null ? null : program.getNode(loopId);
        return loop != null && loop.getType().equals(DrTechDroneNodes.FOR_EACH_COORDINATE)
                ? memory.getActionPosition(loopId) : null;
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

    /** Bounded structured variable list for authenticated wireless-debug clients. */
    public NBTTagList getVariableSnapshot(int maxEntries) {
        return memory.getNumberSnapshot(maxEntries);
    }

    public DroneActionStatus getLastActionStatus() {
        return memory.getLastActionStatus();
    }

    public String getLastActionError() {
        return memory.getLastActionError();
    }

    public long getCurrentNodeElapsedTicks() {
        return currentNodeElapsedTicks;
    }

    /** Bounded, display-safe values actually read by the active node's data input ports. */
    public NBTTagList getCurrentNodeInputSnapshot(int maxEntries) {
        NBTTagList snapshot = new NBTTagList();
        int limit = Math.min(Math.max(0, maxEntries), 16);
        int count = 0;
        for (Map.Entry<String, String> entry : currentNodeInputs.entrySet()) {
            if (count++ >= limit) break;
            NBTTagCompound value = new NBTTagCompound();
            value.setString("Port", entry.getKey());
            value.setString("Value", entry.getValue());
            snapshot.appendTag(value);
        }
        return snapshot;
    }

    /** Node that most recently resolved a data input; retained through terminal nodes for post-run inspection. */
    public String getDebugInputNodeType() {
        return debugInputNodeType;
    }

    public String getLastOutputNodeType() {
        return lastOutputNodeType;
    }

    public String getLastOutputPort() {
        return lastOutputPort;
    }

    public long getLastOutputAmount() {
        return lastOutputAmount;
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

    /** Bounded structured trace for authenticated remote debuggers. */
    public NBTTagList getTraceSnapshot(int maxEntries) {
        NBTTagList snapshot = new NBTTagList();
        if (maxEntries <= 0 || trace.isEmpty()) return snapshot;
        int skip = Math.max(0, trace.size() - Math.min(maxEntries, MAX_TRACE_ENTRIES));
        Iterator<String> textIterator = trace.iterator();
        Iterator<String> nodeIterator = traceNodeIds.iterator();
        Iterator<Long> tickIterator = traceTicks.iterator();
        int index = 0;
        while (textIterator.hasNext()) {
            String text = textIterator.next();
            String nodeId = nodeIterator.hasNext() ? nodeIterator.next() : "";
            long tick = tickIterator.hasNext() ? tickIterator.next() : index;
            if (index++ < skip) continue;
            NBTTagCompound entry = new NBTTagCompound();
            entry.setString("Text", text);
            entry.setLong("Tick", tick);
            if (!nodeId.isEmpty()) entry.setString("Node", nodeId);
            snapshot.appendTag(entry);
        }
        return snapshot;
    }

    public void clearTrace() {
        trace.clear();
        traceNodeIds.clear();
        traceTicks.clear();
        traceSequence = 0L;
    }

    private void addTrace(String entry) {
        while (trace.size() >= MAX_TRACE_ENTRIES) {
            trace.removeFirst();
            if (!traceNodeIds.isEmpty()) traceNodeIds.removeFirst();
            if (!traceTicks.isEmpty()) traceTicks.removeFirst();
        }
        String safe = entry == null ? "unknown" : entry.replace('\r', ' ').replace('\n', ' ');
        trace.addLast(safe.substring(0, Math.min(160, safe.length())));
        traceNodeIds.addLast(currentNodeId == null ? "" : currentNodeId.toString());
        traceTicks.addLast(++traceSequence);
    }

    private void prepareDebugInputs(DroneProgramNode node) {
        if (node == null || node.getType().equals(DrTechDroneNodes.START) || node.getType().equals(DrTechDroneNodes.END)
                || node.getId().equals(debugInputNodeId)) return;
        debugInputNodeId = node.getId();
        debugInputNodeType = node.getType().getPath();
        currentNodeInputs.clear();
    }

    private void recordDebugInput(String portId, Object value) {
        if (portId == null || portId.isEmpty() || currentNodeInputs.size() >= 16 && !currentNodeInputs.containsKey(portId)) {
            return;
        }
        currentNodeInputs.put(portId, formatDebugValue(value));
    }

    private void recordDebugOutput(DroneProgramNode node, DroneExecutionResult result) {
        lastOutputNodeType = node.getType().getPath();
        lastOutputPort = result.getOutputPort() == null ? "" : result.getOutputPort();
        lastOutputAmount = result.getAmount();
    }

    private static String formatDebugValue(Object value) {
        if (value == null) return "<empty>";
        if (value instanceof BlockPos) {
            BlockPos position = (BlockPos) value;
            return position.getX() + "," + position.getY() + "," + position.getZ();
        }
        if (value instanceof DroneArea) {
            DroneArea area = (DroneArea) value;
            return area.getMin().getX() + "," + area.getMin().getY() + "," + area.getMin().getZ()
                    + " -> " + area.getMax().getX() + "," + area.getMax().getY() + "," + area.getMax().getZ()
                    + " (" + area.getVolume() + ")";
        }
        String text = String.valueOf(value).replace('\n', ' ').replace('\r', ' ');
        return text.length() <= 72 ? text : text.substring(0, 71) + "…";
    }

    private void fail(String message) {
        clearAreaValueCache();
        stepOverCallDepth = -1;
        status = DroneRuntimeStatus.ERROR;
        error = message == null ? "unknown" : message;
        memory.setLastAction(DroneActionStatus.ERROR, error);
        addTrace("ERROR " + error);
    }

    public NBTTagCompound writeToNbt() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("ProgramId", rootProgram.getProgramId().toString());
        tag.setLong("ProgramRevision", rootProgram.getSourceRevision());
        tag.setString("ActiveProgramId", program.getProgramId().toString());
        tag.setLong("ActiveProgramRevision", program.getSourceRevision());
        tag.setString("CurrentNode", currentNodeId.toString());
        tag.setString("Status", status.name());
        tag.setTag("NodeState", nodeState.copy());
        tag.setLong("NodeElapsedTicks", currentNodeElapsedTicks);
        if (debugInputNodeId != null) tag.setString("DebugInputNode", debugInputNodeId.toString());
        tag.setString("DebugInputNodeType", debugInputNodeType);
        tag.setTag("DebugInputs", getCurrentNodeInputSnapshot(16));
        tag.setString("LastOutputNode", lastOutputNodeType);
        tag.setString("LastOutputPort", lastOutputPort);
        tag.setLong("LastOutputAmount", lastOutputAmount);
        if (stepOverCallDepth >= 0) tag.setInteger("StepOverDepth", stepOverCallDepth);
        if (pendingBreakpointState != null) {
            tag.setString("PendingBreakpointState", pendingBreakpointState.name());
            tag.setString("PendingBreakpointStatus", pendingBreakpointStatus == null
                    ? DroneActionStatus.ERROR.name() : pendingBreakpointStatus.name());
            tag.setString("PendingBreakpointOutput", pendingBreakpointOutput);
            tag.setString("PendingBreakpointError", pendingBreakpointError);
            tag.setLong("PendingBreakpointAmount", pendingBreakpointAmount);
        }
        tag.setTag("Memory", memory.writeToNbt());
        NBTTagList calls = new NBTTagList();
        for (CallFrame frame : callStack) {
            NBTTagCompound call = new NBTTagCompound();
            call.setString("ProgramId", frame.callerProgram.getProgramId().toString());
            call.setLong("Revision", frame.callerProgram.getSourceRevision());
            call.setString("CallNode", frame.callNodeId.toString());
            calls.appendTag(call);
        }
        tag.setTag("CallStack", calls);
        NBTTagList traceTag = new NBTTagList();
        for (String entry : trace) traceTag.appendTag(new NBTTagString(entry));
        tag.setTag("Trace", traceTag);
        NBTTagList traceNodesTag = new NBTTagList();
        for (String nodeId : traceNodeIds) traceNodesTag.appendTag(new NBTTagString(nodeId));
        tag.setTag("TraceNodes", traceNodesTag);
        NBTTagList traceTicksTag = new NBTTagList();
        for (Long tick : traceTicks) { NBTTagCompound tickTag = new NBTTagCompound();
            tickTag.setLong("Tick", tick == null ? 0L : tick); traceTicksTag.appendTag(tickTag); }
        tag.setTag("TraceTicks", traceTicksTag);
        tag.setLong("TraceSequence", traceSequence);
        tag.setString("Error", error);
        return tag;
    }

    public void readFromNbt(NBTTagCompound tag) {
        if (!rootProgram.getProgramId().toString().equals(tag.getString("ProgramId"))
                || rootProgram.getSourceRevision() != tag.getLong("ProgramRevision")) {
            fail("Saved runtime belongs to a different program revision");
            return;
        }
        try {
            program = resolveSavedProgram(tag.getString("ActiveProgramId"), tag.getLong("ActiveProgramRevision"));
            if (program == null) {
                fail("Saved active subprogram is unavailable or has changed revision");
                return;
            }
            callStack.clear();
            NBTTagList calls = tag.getTagList("CallStack", 10);
            if (calls.tagCount() > 16) {
                fail("Saved subprogram call stack is too deep");
                return;
            }
            // NBT writes the top frame first because ArrayDeque iteration follows push order.
            List<CallFrame> restoredFrames = new java.util.ArrayList<>();
            for (int index = 0; index < calls.tagCount(); index++) {
                NBTTagCompound call = calls.getCompoundTagAt(index);
                CompiledDroneProgram caller = resolveSavedProgram(call.getString("ProgramId"), call.getLong("Revision"));
                UUID callNode = UUID.fromString(call.getString("CallNode"));
                if (caller == null || caller.getNode(callNode) == null) {
                    fail("Saved subprogram caller is unavailable or invalid");
                    return;
                }
                restoredFrames.add(new CallFrame(caller, callNode));
            }
            for (int index = restoredFrames.size() - 1; index >= 0; index--) callStack.push(restoredFrames.get(index));
            UUID savedNode = UUID.fromString(tag.getString("CurrentNode"));
            if (program.getNode(savedNode) == null) {
                fail("Saved current node is missing");
                return;
            }
            currentNodeId = savedNode;
            status = DroneRuntimeStatus.valueOf(tag.getString("Status"));
            nodeState = tag.getCompoundTag("NodeState").copy();
            currentNodeElapsedTicks = Math.max(0L, tag.getLong("NodeElapsedTicks"));
            debugInputNodeId = null;
            debugInputNodeType = tag.getString("DebugInputNodeType");
            if (tag.hasKey("DebugInputNode", 8)) {
                try {
                    debugInputNodeId = UUID.fromString(tag.getString("DebugInputNode"));
                } catch (IllegalArgumentException ignored) { }
            }
            currentNodeInputs.clear();
            NBTTagList inputs = tag.getTagList("DebugInputs", 10);
            for (int index = 0; index < inputs.tagCount() && currentNodeInputs.size() < 16; index++) {
                NBTTagCompound input = inputs.getCompoundTagAt(index);
                String port = input.getString("Port");
                if (!port.isEmpty()) currentNodeInputs.put(port, input.getString("Value"));
            }
            lastOutputNodeType = tag.getString("LastOutputNode");
            lastOutputPort = tag.getString("LastOutputPort");
            lastOutputAmount = Math.max(0L, tag.getLong("LastOutputAmount"));
            stepOverCallDepth = tag.hasKey("StepOverDepth", 3) ? Math.max(-1, tag.getInteger("StepOverDepth")) : -1;
            clearPendingBreakpoint();
            if (tag.hasKey("PendingBreakpointState", 8)) {
                pendingBreakpointState = DroneActionState.valueOf(tag.getString("PendingBreakpointState"));
                pendingBreakpointStatus = tag.hasKey("PendingBreakpointStatus", 8)
                        ? DroneActionStatus.valueOf(tag.getString("PendingBreakpointStatus"))
                        : DroneActionStatus.ERROR;
                pendingBreakpointOutput = tag.getString("PendingBreakpointOutput");
                pendingBreakpointError = tag.getString("PendingBreakpointError");
                pendingBreakpointAmount = Math.max(0L, tag.getLong("PendingBreakpointAmount"));
            }
            memory.readFromNbt(tag.getCompoundTag("Memory"));
            memory.ensureLocalScopeDepth(callStack.size() + 1);
            trace.clear();
            traceNodeIds.clear();
            traceTicks.clear();
            NBTTagList traceTag = tag.getTagList("Trace", 8);
            NBTTagList traceNodesTag = tag.getTagList("TraceNodes", 8);
            NBTTagList traceTicksTag = tag.getTagList("TraceTicks", 10);
            int first = Math.max(0, traceTag.tagCount() - MAX_TRACE_ENTRIES);
            for (int i = first; i < traceTag.tagCount(); i++) {
                trace.addLast(traceTag.getStringTagAt(i));
                traceNodeIds.addLast(i < traceNodesTag.tagCount() ? traceNodesTag.getStringTagAt(i) : "");
                traceTicks.addLast(i < traceTicksTag.tagCount() ? Math.max(0L,
                        traceTicksTag.getCompoundTagAt(i).getLong("Tick")) : (long) (i - first + 1));
            }
            traceSequence = Math.max(tag.getLong("TraceSequence"), traceTicks.isEmpty() ? 0L : traceTicks.getLast());
            error = tag.getString("Error");
        } catch (IllegalArgumentException exception) {
            fail("Saved runtime state is malformed");
        }
    }

    private CompiledDroneProgram resolveSavedProgram(String programId, long revision) {
        if (programId == null || programId.isEmpty()) return rootProgram;
        try {
            UUID id = UUID.fromString(programId);
            if (rootProgram.getProgramId().equals(id) && rootProgram.getSourceRevision() == revision) return rootProgram;
            return environment.resolveProgram(new DroneProgramReference(id, revision, ""));
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private static final class CallFrame {
        private final CompiledDroneProgram callerProgram;
        private final UUID callNodeId;

        private CallFrame(CompiledDroneProgram callerProgram, UUID callNodeId) {
            this.callerProgram = callerProgram;
            this.callNodeId = callNodeId;
        }
    }
}
