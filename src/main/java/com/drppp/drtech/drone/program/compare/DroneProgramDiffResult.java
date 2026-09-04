package com.drppp.drtech.drone.program.compare;

/** Immutable counts produced by {@link DroneProgramDiff}. */
public final class DroneProgramDiffResult {
    private final boolean metadataChanged;
    private final int addedNodes, removedNodes, changedNodes;
    private final int addedEdges, removedEdges, changedEdges;

    DroneProgramDiffResult(boolean metadataChanged, int addedNodes, int removedNodes, int changedNodes,
            int addedEdges, int removedEdges, int changedEdges) {
        this.metadataChanged = metadataChanged;
        this.addedNodes = addedNodes;
        this.removedNodes = removedNodes;
        this.changedNodes = changedNodes;
        this.addedEdges = addedEdges;
        this.removedEdges = removedEdges;
        this.changedEdges = changedEdges;
    }
    public boolean isMetadataChanged() { return metadataChanged; }
    public int getAddedNodes() { return addedNodes; }
    public int getRemovedNodes() { return removedNodes; }
    public int getChangedNodes() { return changedNodes; }
    public int getAddedEdges() { return addedEdges; }
    public int getRemovedEdges() { return removedEdges; }
    public int getChangedEdges() { return changedEdges; }
    public boolean isIdentical() {
        return !metadataChanged && addedNodes == 0 && removedNodes == 0 && changedNodes == 0
                && addedEdges == 0 && removedEdges == 0 && changedEdges == 0;
    }
}
