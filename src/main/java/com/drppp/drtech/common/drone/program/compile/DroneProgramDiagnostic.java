package com.drppp.drtech.common.drone.program.compile;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public final class DroneProgramDiagnostic {

    private final DroneDiagnosticSeverity severity;
    private final DroneDiagnosticCode code;
    private final UUID nodeId;
    private final String portId;
    private final List<String> arguments;

    public DroneProgramDiagnostic(DroneDiagnosticSeverity severity, DroneDiagnosticCode code, UUID nodeId,
            String portId, String... arguments) {
        this.severity = severity;
        this.code = code;
        this.nodeId = nodeId;
        this.portId = portId;
        this.arguments = Collections.unmodifiableList(Arrays.asList(arguments));
    }

    public DroneDiagnosticSeverity getSeverity() {
        return severity;
    }

    public DroneDiagnosticCode getCode() {
        return code;
    }

    public UUID getNodeId() {
        return nodeId;
    }

    public String getPortId() {
        return portId;
    }

    public List<String> getArguments() {
        return arguments;
    }

    public String getTranslationKey() {
        return "drtech.drone.diagnostic." + code.name().toLowerCase(java.util.Locale.ROOT);
    }
}
