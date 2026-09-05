package com.drppp.drtech.common.drone.program.model;

/** Editor diagnostic severity with stable icon keys. */
public final class DroneDiagnostic {
    public enum Severity { ERROR, WARNING, BREAKPOINT }
    private final Severity severity;
    private final String message;
    public DroneDiagnostic(Severity severity, String message) { this.severity = severity; this.message = message == null ? "" : message; }
    public Severity getSeverity() { return severity; }
    public String getMessage() { return message; }
    public String getIconKey() { return "drtech.drone.diagnostic." + severity.name().toLowerCase(java.util.Locale.ROOT); }
}
