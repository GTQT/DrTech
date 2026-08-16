package com.drppp.drtech.compat.opencomputers;

import java.util.Collections;

/** Permission boundary for optional OC callbacks; server implementations override these methods. */
public interface OpenComputersDroneCallbacks {
    default OpenComputersCallbackResult queryDocks(int page, int pageSize) { return OpenComputersCallbackResult.success(Collections.emptyMap()); }
    default OpenComputersCallbackResult launch(String droneId) { return OpenComputersCallbackResult.failure("launch unavailable"); }
    default OpenComputersCallbackResult recall(String droneId) { return OpenComputersCallbackResult.failure("recall unavailable"); }
    default OpenComputersCallbackResult control(String droneId, String command) { return OpenComputersCallbackResult.failure("control unavailable"); }
    default OpenComputersCallbackResult listPrograms(int page, int pageSize) { return OpenComputersCallbackResult.success(Collections.emptyMap()); }
    default OpenComputersCallbackResult compileProgram(String source) { return OpenComputersCallbackResult.failure("compile unavailable"); }
    default OpenComputersCallbackResult assignProgram(String droneId, String programId) { return OpenComputersCallbackResult.failure("assign unavailable"); }
    default OpenComputersCallbackResult queryFleet(int page, int pageSize) { return OpenComputersCallbackResult.success(Collections.emptyMap()); }
    default OpenComputersCallbackResult submitJob(String request) { return OpenComputersCallbackResult.failure("submit unavailable"); }
    default OpenComputersCallbackResult cancelJob(String jobId) { return OpenComputersCallbackResult.failure("cancel unavailable"); }
}
