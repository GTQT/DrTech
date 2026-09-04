package com.drppp.drtech.drone.program.runtime;

import java.util.Objects;

public final class DroneExecutionResult {

    private static final DroneExecutionResult RUNNING = new DroneExecutionResult(
            DroneActionState.RUNNING, DroneActionStatus.RUNNING, null, null);
    private static final DroneExecutionResult PAUSED = new DroneExecutionResult(
            DroneActionState.PAUSED, DroneActionStatus.PAUSED, null, null);
    private final DroneActionState state;
    private final DroneActionStatus status;
    private final String outputPort;
    private final String error;
    private final long amount;

    private DroneExecutionResult(DroneActionState state, DroneActionStatus status, String outputPort, String error) {
        this(state, status, outputPort, error, 0L);
    }

    private DroneExecutionResult(DroneActionState state, DroneActionStatus status, String outputPort, String error,
            long amount) {
        this.state = Objects.requireNonNull(state, "state");
        this.status = Objects.requireNonNull(status, "status");
        this.outputPort = outputPort;
        this.error = error;
        this.amount = Math.max(0L, amount);
    }

    public static DroneExecutionResult running() {
        return RUNNING;
    }

    public static DroneExecutionResult paused() {
        return PAUSED;
    }

    public static DroneExecutionResult running(long amount) {
        return amount <= 0L ? RUNNING : new DroneExecutionResult(
                DroneActionState.RUNNING, DroneActionStatus.RUNNING, null, null, amount);
    }

    public static DroneExecutionResult success() {
        return success("next");
    }

    public static DroneExecutionResult success(String outputPort) {
        return new DroneExecutionResult(DroneActionState.SUCCESS, DroneActionStatus.SUCCESS, outputPort, null);
    }

    public static DroneExecutionResult success(long amount) {
        return new DroneExecutionResult(DroneActionState.SUCCESS, DroneActionStatus.SUCCESS, "next", null, amount);
    }

    public static DroneExecutionResult failure(String outputPort) {
        return failure(DroneActionStatus.FAILURE, outputPort, null);
    }

    public static DroneExecutionResult failure(DroneActionStatus status, String outputPort, String message) {
        if (status == null || !status.isFailure()) throw new IllegalArgumentException("Status is not a failure");
        return new DroneExecutionResult(DroneActionState.FAILURE, status,
                outputPort == null ? "failed" : outputPort, message);
    }

    public static DroneExecutionResult error(String message) {
        return new DroneExecutionResult(DroneActionState.ERROR, DroneActionStatus.ERROR, null,
                message == null ? "unknown" : message);
    }

    public DroneActionState getState() {
        return state;
    }

    public DroneActionStatus getStatus() { return status; }

    public String getOutputPort() {
        return outputPort;
    }

    public String getError() {
        return error;
    }

    public long getAmount() { return amount; }
}
