package com.drppp.drtech.compat.opencomputers;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Lua-friendly callback result without a compile-time OpenComputers dependency. */
public final class OpenComputersCallbackResult {
    private final boolean success;
    private final String error;
    private final Map<String, Object> data;
    private OpenComputersCallbackResult(boolean success, String error, Map<String, Object> data) {
        this.success = success; this.error = error;
        this.data = Collections.unmodifiableMap(new LinkedHashMap<>(data));
    }
    public static OpenComputersCallbackResult success(Map<String, Object> data) { return new OpenComputersCallbackResult(true, "", data == null ? Collections.emptyMap() : data); }
    public static OpenComputersCallbackResult failure(String error) { return new OpenComputersCallbackResult(false, error == null ? "unknown" : error, Collections.emptyMap()); }
    public boolean isSuccess() { return success; }
    public String getError() { return error; }
    public Map<String, Object> getData() { return data; }
}
