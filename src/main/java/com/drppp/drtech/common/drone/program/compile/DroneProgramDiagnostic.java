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
    private final String propertyId;
    private final List<String> arguments;

    public DroneProgramDiagnostic(DroneDiagnosticSeverity severity, DroneDiagnosticCode code, UUID nodeId,
            String portId, String... arguments) {
        this(severity, code, nodeId, portId, null, Arrays.asList(arguments));
    }

    public static DroneProgramDiagnostic withProperty(DroneDiagnosticSeverity severity, DroneDiagnosticCode code,
            UUID nodeId, String portId, String propertyId, String... arguments) {
        return new DroneProgramDiagnostic(severity, code, nodeId, portId, propertyId, Arrays.asList(arguments));
    }

    private DroneProgramDiagnostic(DroneDiagnosticSeverity severity, DroneDiagnosticCode code, UUID nodeId,
            String portId, String propertyId, List<String> arguments) {
        this.severity = severity;
        this.code = code;
        this.nodeId = nodeId;
        this.portId = portId;
        this.propertyId = propertyId;
        this.arguments = Collections.unmodifiableList(arguments);
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

    public String getPropertyId() {
        return propertyId;
    }

    public List<String> getArguments() {
        return arguments;
    }

    public String getTranslationKey() {
        return "drtech.drone.diagnostic." + code.name().toLowerCase(java.util.Locale.ROOT);
    }
}
