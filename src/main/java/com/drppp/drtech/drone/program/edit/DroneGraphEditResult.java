package com.drppp.drtech.drone.program.edit;

import com.drppp.drtech.drone.program.compile.DroneProgramDiagnostic;

import java.util.Collections;
import java.util.List;

public final class DroneGraphEditResult {

    private final DroneGraphEditStatus status;
    private final long revision;
    private final String message;
    private final List<DroneProgramDiagnostic> diagnostics;

    DroneGraphEditResult(DroneGraphEditStatus status, long revision, String message,
            List<DroneProgramDiagnostic> diagnostics) {
        this.status = status;
        this.revision = revision;
        this.message = message == null ? "" : message;
        this.diagnostics = Collections.unmodifiableList(diagnostics);
    }

    public DroneGraphEditStatus getStatus() {
        return status;
    }

    public boolean isAccepted() {
        return status == DroneGraphEditStatus.ACCEPTED;
    }

    public long getRevision() {
        return revision;
    }

    public String getMessage() {
        return message;
    }

    public List<DroneProgramDiagnostic> getDiagnostics() {
        return diagnostics;
    }
}
