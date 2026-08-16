package com.drppp.drtech.compat.opencomputers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Bounded page helper for Lua table responses. */
public final class OpenComputersPage {
    private OpenComputersPage() { }
    public static <T> List<T> slice(List<T> values, int page, int pageSize) {
        if (values == null || values.isEmpty()) return Collections.emptyList();
        int safePage = Math.max(0, page);
        int safeSize = Math.max(1, Math.min(64, pageSize));
        int from = Math.min(values.size(), safePage * safeSize);
        int to = Math.min(values.size(), from + safeSize);
        return Collections.unmodifiableList(new ArrayList<>(values.subList(from, to)));
    }
}
