package com.drppp.drtech.compat.opencomputers;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/** Bounded server audit trail for optional OpenComputers callbacks. */
public final class OpenComputersAuditLog {
    private static final int MAX_ENTRIES = 512;
    private final List<Entry> entries = new ArrayList<>();
    public synchronized void record(UUID caller, String component, String method, boolean success, long worldTime) {
        if (caller == null || component == null || method == null) return;
        if (entries.size() >= MAX_ENTRIES) entries.remove(0);
        entries.add(new Entry(caller, component.substring(0, Math.min(64, component.length())), method.substring(0, Math.min(64, method.length())), success, Math.max(0L, worldTime)));
    }
    public synchronized List<Entry> snapshot() { return Collections.unmodifiableList(new ArrayList<>(entries)); }
    public synchronized NBTTagList writeToNbt() { NBTTagList list = new NBTTagList(); for (Entry entry : entries) { NBTTagCompound tag = new NBTTagCompound(); tag.setString("Caller", entry.caller.toString()); tag.setString("Component", entry.component); tag.setString("Method", entry.method); tag.setBoolean("Success", entry.success); tag.setLong("WorldTime", entry.worldTime); list.appendTag(tag); } return list; }
    public synchronized void readFromNbt(NBTTagList list) { entries.clear(); if (list == null) return; for (int i = Math.max(0, list.tagCount() - MAX_ENTRIES); i < list.tagCount(); i++) { NBTTagCompound tag = list.getCompoundTagAt(i); try { record(UUID.fromString(tag.getString("Caller")), tag.getString("Component"), tag.getString("Method"), tag.getBoolean("Success"), tag.getLong("WorldTime")); } catch (IllegalArgumentException ignored) { } } }
    public static final class Entry {
        private final UUID caller; private final String component; private final String method; private final boolean success; private final long worldTime;
        private Entry(UUID caller, String component, String method, boolean success, long worldTime) { this.caller = caller; this.component = component; this.method = method; this.success = success; this.worldTime = worldTime; }
        public UUID getCaller() { return caller; } public String getComponent() { return component; } public String getMethod() { return method; } public boolean isSuccess() { return success; } public long getWorldTime() { return worldTime; }
    }
}
