package com.drppp.drtech.common.drone.program.edit;

import com.drppp.drtech.common.drone.program.codec.DroneProgramNbtCodec;
import com.drppp.drtech.common.drone.program.compile.DroneCompileResult;
import com.drppp.drtech.common.drone.program.compile.DroneProgramCompiler;
import com.drppp.drtech.common.drone.program.model.DroneProgramGraph;
import com.drppp.drtech.common.drone.program.registry.DroneNodeRegistry;

import java.util.Collections;
import java.util.ArrayDeque;
import java.util.Deque;

/** Transactional server-side editor state. Rejected commands never partially mutate the source graph. */
public final class DroneProgramEditSession {

    public static final int MAX_ABS_COORDINATE = 1_000_000;
    public static final int MAX_HISTORY = 64;

    private final DroneNodeRegistry nodeRegistry;
    private final DroneProgramCompiler compiler;
    private DroneProgramGraph graph;
    private DroneCompileResult lastCompileResult;
    private final Deque<DroneProgramGraph> undoHistory = new ArrayDeque<>();
    private final Deque<DroneProgramGraph> redoHistory = new ArrayDeque<>();

    public DroneProgramEditSession(DroneProgramGraph graph, DroneNodeRegistry nodeRegistry) {
        this.graph = graph.copy();
        this.nodeRegistry = nodeRegistry;
        this.compiler = new DroneProgramCompiler(nodeRegistry);
        this.lastCompileResult = compiler.compile(this.graph);
    }

    public DroneGraphEditResult apply(DroneGraphEditCommand command) {
        if (command == null) {
            return rejected(DroneGraphEditStatus.INVALID_COMMAND, "Command is missing");
        }
        if (command.getExpectedRevision() != graph.getRevision()) {
            return rejected(DroneGraphEditStatus.REVISION_CONFLICT,
                    "Expected revision " + command.getExpectedRevision() + ", server has " + graph.getRevision());
        }
        if (command.getType() == DroneGraphCommandType.UNDO) return undo();
        if (command.getType() == DroneGraphCommandType.REDO) return redo();
        String validationError = validatePayload(command);
        if (validationError != null) {
            return rejected(DroneGraphEditStatus.INVALID_COMMAND, validationError);
        }

        DroneProgramGraph candidate = graph.copy();
        try {
            command.apply(candidate);
        } catch (RuntimeException exception) {
            return rejected(DroneGraphEditStatus.INVALID_COMMAND, exception.getMessage());
        }
        if (candidate.getNodes().size() > DroneProgramCompiler.MAX_NODES
                || candidate.getEdges().size() > DroneProgramCompiler.MAX_EDGES) {
            return rejected(DroneGraphEditStatus.LIMIT_EXCEEDED, "Program graph limit exceeded");
        }

        pushHistory(undoHistory, graph);
        redoHistory.clear();
        graph = candidate;
        lastCompileResult = compiler.compile(graph);
        return new DroneGraphEditResult(DroneGraphEditStatus.ACCEPTED, graph.getRevision(), "",
                lastCompileResult.getDiagnostics());
    }

    private DroneGraphEditResult undo() {
        if (undoHistory.isEmpty()) return rejected(DroneGraphEditStatus.INVALID_COMMAND, "Nothing to undo");
        pushHistory(redoHistory, graph);
        long nextRevision = graph.getRevision() + 1L;
        graph = undoHistory.removeLast().copyWithRevision(nextRevision);
        lastCompileResult = compiler.compile(graph);
        return accepted();
    }

    private DroneGraphEditResult redo() {
        if (redoHistory.isEmpty()) return rejected(DroneGraphEditStatus.INVALID_COMMAND, "Nothing to redo");
        pushHistory(undoHistory, graph);
        long nextRevision = graph.getRevision() + 1L;
        graph = redoHistory.removeLast().copyWithRevision(nextRevision);
        lastCompileResult = compiler.compile(graph);
        return accepted();
    }

    private DroneGraphEditResult accepted() {
        return new DroneGraphEditResult(DroneGraphEditStatus.ACCEPTED, graph.getRevision(), "",
                lastCompileResult.getDiagnostics());
    }

    private static void pushHistory(Deque<DroneProgramGraph> history, DroneProgramGraph snapshot) {
        history.addLast(snapshot.copy());
        while (history.size() > MAX_HISTORY) history.removeFirst();
    }

    private String validatePayload(DroneGraphEditCommand command) {
        if (Math.abs((long) command.getX()) > MAX_ABS_COORDINATE
                || Math.abs((long) command.getY()) > MAX_ABS_COORDINATE) {
            return "Node coordinate is outside the editor limit";
        }
        if (command.getType() == DroneGraphCommandType.ADD_NODE
                && nodeRegistry.get(command.getNodeType()) == null) {
            return "Unknown node type " + command.getNodeType();
        }
        if ((command.getType() == DroneGraphCommandType.REMOVE_NODE
                || command.getType() == DroneGraphCommandType.MOVE_NODE
                || command.getType() == DroneGraphCommandType.CONFIGURE_NODE)
                && graph.getNode(command.getObjectId()) == null) {
            return "Unknown node " + command.getObjectId();
        }
        if (command.getType() == DroneGraphCommandType.REMOVE_EDGE
                && graph.getEdge(command.getObjectId()) == null) {
            return "Unknown edge " + command.getObjectId();
        }
        if (command.getConfiguration() != null
                && command.getConfiguration().getKeySet().size() > DroneProgramNbtCodec.MAX_CONFIGURATION_KEYS) {
            return "Node configuration has too many keys";
        }
        return null;
    }

    private DroneGraphEditResult rejected(DroneGraphEditStatus status, String message) {
        return new DroneGraphEditResult(status, graph.getRevision(), message,
                lastCompileResult == null ? Collections.emptyList() : lastCompileResult.getDiagnostics());
    }

    public DroneProgramGraph getGraph() {
        return graph.copy();
    }

    public DroneCompileResult getLastCompileResult() {
        return lastCompileResult;
    }

    public boolean canUndo() {
        return !undoHistory.isEmpty();
    }

    public boolean canRedo() {
        return !redoHistory.isEmpty();
    }
}
