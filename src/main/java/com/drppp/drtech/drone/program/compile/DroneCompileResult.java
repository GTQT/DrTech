package com.drppp.drtech.drone.program.compile;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public final class DroneCompileResult {

    private final CompiledDroneProgram program;
    private final List<DroneProgramDiagnostic> diagnostics;

    DroneCompileResult(CompiledDroneProgram program, List<DroneProgramDiagnostic> diagnostics) {
        this.program = program;
        this.diagnostics = Collections.unmodifiableList(diagnostics);
    }

    public boolean hasErrors() {
        return diagnostics.stream().anyMatch(diagnostic -> diagnostic.getSeverity() == DroneDiagnosticSeverity.ERROR);
    }

    public Optional<CompiledDroneProgram> getProgram() {
        return Optional.ofNullable(program);
    }

    public List<DroneProgramDiagnostic> getDiagnostics() {
        return diagnostics;
    }
}
