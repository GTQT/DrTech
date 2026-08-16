package com.drppp.drtech.common.drone.program.runtime;

import com.drppp.drtech.common.drone.filter.DroneItemFilterSpec;
import com.drppp.drtech.common.drone.inventory.DroneItemFilter;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.math.BlockPos;

import java.util.UUID;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Persistent program-local memory. Internal loop counters are isolated from user variables. */
public final class DroneRuntimeMemory {

    public static final int MAX_VARIABLE_NAME_LENGTH = 24;

    private NBTTagCompound variables = new NBTTagCompound();
    private NBTTagCompound stringVariables = new NBTTagCompound();
    private List<NBTTagCompound> localNumberScopes = rootScope();
    private List<NBTTagCompound> localStringScopes = rootScope();
    private NBTTagCompound callNumberOutputs = new NBTTagCompound();
    private NBTTagCompound callStringOutputs = new NBTTagCompound();
    private NBTTagCompound loops = new NBTTagCompound();
    private NBTTagCompound actionAmounts = new NBTTagCompound();
    private NBTTagCompound actionPositions = new NBTTagCompound();
    private NBTTagCompound currentItemFilters = new NBTTagCompound();
    private NBTTagCompound loopTraversalIndices = new NBTTagCompound();
    private List<String> currentItemFilterStack = new ArrayList<>();
    private List<String> activeLoopStack = new ArrayList<>();
    private DroneActionStatus lastActionStatus = DroneActionStatus.SUCCESS;
    private String lastActionError = "";

    public double getNumber(String name) {
        return variables.getDouble(requireName(name));
    }

    public void setNumber(String name, double value) {
        if (!Double.isFinite(value)) throw new IllegalArgumentException("Variable value must be finite");
        variables.setDouble(requireName(name), value);
    }

    public double addNumber(String name, double amount) {
        double value = getNumber(name) + amount;
        setNumber(name, value);
        return value;
    }

    public String getString(String name) {
        return stringVariables.getString(requireName(name));
    }

    public void setString(String name, String value) {
        String bounded = value == null ? "" : value;
        if (bounded.length() > 1024) bounded = bounded.substring(0, 1024);
        stringVariables.setString(requireName(name), bounded);
    }

    public double getNumber(String name, boolean local) {
        return local ? currentScope(localNumberScopes).getDouble(requireName(name)) : getNumber(name);
    }

    public void setNumber(String name, double value, boolean local) {
        if (!local) {
            setNumber(name, value);
            return;
        }
        if (!Double.isFinite(value)) throw new IllegalArgumentException("Variable value must be finite");
        currentScope(localNumberScopes).setDouble(requireName(name), value);
    }

    public double addNumber(String name, double amount, boolean local) {
        double value = getNumber(name, local) + amount;
        setNumber(name, value, local);
        return value;
    }

    public String getString(String name, boolean local) {
        return local ? currentScope(localStringScopes).getString(requireName(name)) : getString(name);
    }

    public void setString(String name, String value, boolean local) {
        if (!local) {
            setString(name, value);
            return;
        }
        String bounded = value == null ? "" : value;
        if (bounded.length() > 1024) bounded = bounded.substring(0, 1024);
        currentScope(localStringScopes).setString(requireName(name), bounded);
    }

    void pushLocalScope() {
        if (localNumberScopes.size() >= 17) throw new IllegalStateException("Local scope depth exceeds 17");
        localNumberScopes.add(new NBTTagCompound());
        localStringScopes.add(new NBTTagCompound());
    }

    void popLocalScope() {
        if (localNumberScopes.size() <= 1) throw new IllegalStateException("Cannot remove root local scope");
        localNumberScopes.remove(localNumberScopes.size() - 1);
        localStringScopes.remove(localStringScopes.size() - 1);
    }

    void ensureLocalScopeDepth(int depth) {
        int bounded = Math.max(1, Math.min(17, depth));
        while (localNumberScopes.size() < bounded) pushLocalScope();
        while (localNumberScopes.size() > bounded) popLocalScope();
    }

    int getLocalScopeDepth() { return localNumberScopes.size(); }

    void setCallNumberOutput(UUID callNodeId, double value) {
        if (callNodeId != null && Double.isFinite(value)) callNumberOutputs.setDouble(callNodeId.toString(), value);
    }

    double getCallNumberOutput(UUID callNodeId) {
        return callNodeId == null ? 0.0D : callNumberOutputs.getDouble(callNodeId.toString());
    }

    void setCallStringOutput(UUID callNodeId, String value) {
        if (callNodeId == null) return;
        String bounded = value == null ? "" : value;
        if (bounded.length() > 1024) bounded = bounded.substring(0, 1024);
        callStringOutputs.setString(callNodeId.toString(), bounded);
    }

    String getCallStringOutput(UUID callNodeId) {
        return callNodeId == null ? "" : callStringOutputs.getString(callNodeId.toString());
    }

    public DroneActionStatus getLastActionStatus() {
        return lastActionStatus;
    }

    public String getLastActionError() {
        return lastActionError;
    }

    void setLastAction(DroneActionStatus status, String error) {
        lastActionStatus = status == null ? DroneActionStatus.ERROR : status;
        lastActionError = error == null ? "" : error;
        if (lastActionError.length() > 512) lastActionError = lastActionError.substring(0, 512);
    }

    public long getActionAmount(UUID nodeId) {
        return nodeId == null ? 0L : Math.max(0L, actionAmounts.getLong(nodeId.toString()));
    }

    void setActionAmount(UUID nodeId, long amount) {
        if (nodeId != null) actionAmounts.setLong(nodeId.toString(), Math.max(0L, amount));
    }

    public BlockPos getActionPosition(UUID nodeId) {
        if (nodeId == null || !actionPositions.hasKey(nodeId.toString(), 10)) return null;
        NBTTagCompound position = actionPositions.getCompoundTag(nodeId.toString());
        return new BlockPos(position.getInteger("X"), position.getInteger("Y"), position.getInteger("Z"));
    }

    void setActionPosition(UUID nodeId, BlockPos value) {
        if (nodeId == null || value == null) return;
        NBTTagCompound position = new NBTTagCompound();
        position.setInteger("X", value.getX());
        position.setInteger("Y", value.getY());
        position.setInteger("Z", value.getZ());
        actionPositions.setTag(nodeId.toString(), position);
    }

    int getLoopIteration(UUID nodeId) {
        return loops.getInteger(nodeId.toString());
    }

    void setLoopIteration(UUID nodeId, int iteration) {
        loops.setInteger(nodeId.toString(), Math.max(0, iteration));
    }

    void clearLoop(UUID nodeId) {
        loops.removeTag(nodeId.toString());
        loopTraversalIndices.removeTag(nodeId.toString());
    }

    void setLoopTraversalIndices(UUID nodeId, int[] indices) {
        if (nodeId == null || indices == null || indices.length > 4_096) return;
        loopTraversalIndices.setIntArray(nodeId.toString(), indices);
    }

    int getLoopTraversalIndex(UUID nodeId, int iteration, int expectedSize) {
        if (nodeId == null || iteration < 0 || expectedSize < 0) return -1;
        int[] indices = loopTraversalIndices.getIntArray(nodeId.toString());
        if (indices.length != expectedSize || iteration >= indices.length) return -1;
        int selected = indices[iteration];
        return selected >= 0 && selected < expectedSize ? selected : -1;
    }

    void enterLoop(UUID programId, UUID nodeId) {
        if (programId == null || nodeId == null) return;
        String frame = loopFrame(programId, nodeId);
        if (!activeLoopStack.isEmpty() && frame.equals(activeLoopStack.get(activeLoopStack.size() - 1))) return;
        if (activeLoopStack.size() >= 64) throw new IllegalStateException("Loop nesting exceeds 64");
        activeLoopStack.add(frame);
    }

    void leaveLoop(UUID programId, UUID nodeId) {
        if (programId == null || nodeId == null || activeLoopStack.isEmpty()) return;
        String frame = loopFrame(programId, nodeId);
        int top = activeLoopStack.size() - 1;
        if (frame.equals(activeLoopStack.get(top))) activeLoopStack.remove(top);
    }

    UUID getCurrentLoop(UUID programId) {
        if (programId == null || activeLoopStack.isEmpty()) return null;
        String prefix = programId.toString() + ":";
        String frame = activeLoopStack.get(activeLoopStack.size() - 1);
        if (!frame.startsWith(prefix)) return null;
        try {
            return UUID.fromString(frame.substring(prefix.length()));
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    UUID popCurrentLoop(UUID programId) {
        UUID loopId = getCurrentLoop(programId);
        if (loopId != null) activeLoopStack.remove(activeLoopStack.size() - 1);
        return loopId;
    }

    void setCurrentItemFilter(UUID loopNodeId, DroneItemFilter filter) {
        if (loopNodeId == null || filter == null) return;
        String key = loopNodeId.toString();
        currentItemFilters.setTag(key, filter.getSpec().writeToNbt());
        currentItemFilterStack.remove(key);
        currentItemFilterStack.add(key);
    }

    DroneItemFilter getCurrentItemFilter() {
        while (!currentItemFilterStack.isEmpty()) {
            String key = currentItemFilterStack.get(currentItemFilterStack.size() - 1);
            if (currentItemFilters.hasKey(key, 10)) {
                return DroneItemFilter.fromSpec(DroneItemFilterSpec.readFromNbt(
                        currentItemFilters.getCompoundTag(key)));
            }
            currentItemFilterStack.remove(currentItemFilterStack.size() - 1);
        }
        return null;
    }

    void clearCurrentItemFilter(UUID loopNodeId) {
        if (loopNodeId == null) return;
        String key = loopNodeId.toString();
        currentItemFilters.removeTag(key);
        currentItemFilterStack.remove(key);
    }

    public NBTTagCompound writeToNbt() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setTag("Variables", variables.copy());
        tag.setTag("StringVariables", stringVariables.copy());
        tag.setTag("LocalNumberScopes", writeScopes(localNumberScopes));
        tag.setTag("LocalStringScopes", writeScopes(localStringScopes));
        tag.setTag("CallNumberOutputs", callNumberOutputs.copy());
        tag.setTag("CallStringOutputs", callStringOutputs.copy());
        tag.setTag("Loops", loops.copy());
        tag.setTag("ActionAmounts", actionAmounts.copy());
        tag.setTag("ActionPositions", actionPositions.copy());
        tag.setTag("CurrentItemFilters", currentItemFilters.copy());
        tag.setTag("LoopTraversalIndices", loopTraversalIndices.copy());
        NBTTagList itemFilterStack = new NBTTagList();
        for (String loopId : currentItemFilterStack) itemFilterStack.appendTag(new NBTTagString(loopId));
        tag.setTag("CurrentItemFilterStack", itemFilterStack);
        NBTTagList loopStack = new NBTTagList();
        for (String frame : activeLoopStack) loopStack.appendTag(new NBTTagString(frame));
        tag.setTag("ActiveLoopStack", loopStack);
        tag.setString("LastActionStatus", lastActionStatus.name());
        if (!lastActionError.isEmpty()) tag.setString("LastActionError", lastActionError);
        return tag;
    }

    public void readFromNbt(NBTTagCompound tag) {
        variables = tag.hasKey("Variables", 10) ? tag.getCompoundTag("Variables").copy() : new NBTTagCompound();
        stringVariables = tag.hasKey("StringVariables", 10)
                ? tag.getCompoundTag("StringVariables").copy() : new NBTTagCompound();
        localNumberScopes = readScopes(tag.getTagList("LocalNumberScopes", 10));
        localStringScopes = readScopes(tag.getTagList("LocalStringScopes", 10));
        while (localStringScopes.size() < localNumberScopes.size()) localStringScopes.add(new NBTTagCompound());
        while (localStringScopes.size() > localNumberScopes.size()) {
            localStringScopes.remove(localStringScopes.size() - 1);
        }
        callNumberOutputs = tag.hasKey("CallNumberOutputs", 10)
                ? tag.getCompoundTag("CallNumberOutputs").copy() : new NBTTagCompound();
        callStringOutputs = tag.hasKey("CallStringOutputs", 10)
                ? tag.getCompoundTag("CallStringOutputs").copy() : new NBTTagCompound();
        loops = tag.hasKey("Loops", 10) ? tag.getCompoundTag("Loops").copy() : new NBTTagCompound();
        actionAmounts = tag.hasKey("ActionAmounts", 10)
                ? tag.getCompoundTag("ActionAmounts").copy() : new NBTTagCompound();
        actionPositions = tag.hasKey("ActionPositions", 10)
                ? tag.getCompoundTag("ActionPositions").copy() : new NBTTagCompound();
        currentItemFilters = tag.hasKey("CurrentItemFilters", 10)
                ? tag.getCompoundTag("CurrentItemFilters").copy() : new NBTTagCompound();
        loopTraversalIndices = tag.hasKey("LoopTraversalIndices", 10)
                ? tag.getCompoundTag("LoopTraversalIndices").copy() : new NBTTagCompound();
        currentItemFilterStack = new ArrayList<>();
        NBTTagList itemFilterStack = tag.getTagList("CurrentItemFilterStack", 8);
        for (int index = 0; index < itemFilterStack.tagCount()
                && currentItemFilterStack.size() < 64; index++) {
            String loopId = itemFilterStack.getStringTagAt(index);
            if (currentItemFilters.hasKey(loopId, 10) && !currentItemFilterStack.contains(loopId)) {
                currentItemFilterStack.add(loopId);
            }
        }
        activeLoopStack = new ArrayList<>();
        NBTTagList loopStack = tag.getTagList("ActiveLoopStack", 8);
        for (int index = 0; index < loopStack.tagCount() && activeLoopStack.size() < 64; index++) {
            String frame = loopStack.getStringTagAt(index);
            if (isValidLoopFrame(frame)) activeLoopStack.add(frame);
        }
        try {
            lastActionStatus = tag.hasKey("LastActionStatus", 8)
                    ? DroneActionStatus.valueOf(tag.getString("LastActionStatus")) : DroneActionStatus.SUCCESS;
        } catch (IllegalArgumentException ignored) {
            lastActionStatus = DroneActionStatus.ERROR;
        }
        lastActionError = tag.getString("LastActionError");
        if (lastActionError.length() > 512) lastActionError = lastActionError.substring(0, 512);
    }

    public void clear() {
        variables = new NBTTagCompound();
        stringVariables = new NBTTagCompound();
        localNumberScopes = rootScope();
        localStringScopes = rootScope();
        callNumberOutputs = new NBTTagCompound();
        callStringOutputs = new NBTTagCompound();
        loops = new NBTTagCompound();
        actionAmounts = new NBTTagCompound();
        actionPositions = new NBTTagCompound();
        currentItemFilters = new NBTTagCompound();
        loopTraversalIndices = new NBTTagCompound();
        currentItemFilterStack = new ArrayList<>();
        activeLoopStack = new ArrayList<>();
        lastActionStatus = DroneActionStatus.SUCCESS;
        lastActionError = "";
    }

    public String getNumberSummary(int maxEntries) {
        if (maxEntries <= 0 || variables.getKeySet().isEmpty()) return "No variables";
        List<String> names = new ArrayList<>(variables.getKeySet());
        Collections.sort(names);
        StringBuilder builder = new StringBuilder();
        int limit = Math.min(maxEntries, names.size());
        for (int i = 0; i < limit; i++) {
            if (i > 0) builder.append('\n');
            String name = names.get(i);
            builder.append(name).append('=').append(format(variables.getDouble(name)));
        }
        if (names.size() > limit) builder.append("\n+").append(names.size() - limit);
        return builder.toString();
    }

    /**
     * Produces a small, deterministic debug-only representation. The runtime remains authoritative; callers receive
     * fresh NBT values so a wireless UI cannot mutate program memory.
     */
    public NBTTagList getNumberSnapshot(int maxEntries) {
        NBTTagList snapshot = new NBTTagList();
        if (maxEntries <= 0 || variables.getKeySet().isEmpty()) return snapshot;
        List<String> names = new ArrayList<>(variables.getKeySet());
        Collections.sort(names);
        int limit = Math.min(Math.min(maxEntries, 64), names.size());
        for (int index = 0; index < limit; index++) {
            String name = names.get(index);
            NBTTagCompound entry = new NBTTagCompound();
            entry.setString("Name", name);
            entry.setDouble("Value", variables.getDouble(name));
            snapshot.appendTag(entry);
        }
        return snapshot;
    }

    private static String format(double value) {
        if (value == Math.rint(value) && Math.abs(value) <= Long.MAX_VALUE) return Long.toString((long) value);
        return Double.toString(value);
    }

    private static List<NBTTagCompound> rootScope() {
        List<NBTTagCompound> scopes = new ArrayList<>();
        scopes.add(new NBTTagCompound());
        return scopes;
    }

    private static NBTTagCompound currentScope(List<NBTTagCompound> scopes) {
        return scopes.get(scopes.size() - 1);
    }

    private static NBTTagList writeScopes(List<NBTTagCompound> scopes) {
        NBTTagList list = new NBTTagList();
        for (NBTTagCompound scope : scopes) list.appendTag(scope.copy());
        return list;
    }

    private static List<NBTTagCompound> readScopes(NBTTagList list) {
        List<NBTTagCompound> scopes = new ArrayList<>();
        for (int index = 0; index < list.tagCount() && scopes.size() < 17; index++) {
            scopes.add(list.getCompoundTagAt(index).copy());
        }
        if (scopes.isEmpty()) scopes.add(new NBTTagCompound());
        return scopes;
    }

    private static String loopFrame(UUID programId, UUID nodeId) {
        return programId.toString() + ":" + nodeId.toString();
    }

    private static boolean isValidLoopFrame(String frame) {
        if (frame == null || frame.length() != 73 || frame.charAt(36) != ':') return false;
        try {
            UUID.fromString(frame.substring(0, 36));
            UUID.fromString(frame.substring(37));
            return true;
        } catch (IllegalArgumentException ignored) {
            return false;
        }
    }

    public static boolean isValidName(String name) {
        if (name == null || name.isEmpty() || name.length() > MAX_VARIABLE_NAME_LENGTH) return false;
        if (!isLetterOrUnderscore(name.charAt(0))) return false;
        for (int i = 1; i < name.length(); i++) {
            char value = name.charAt(i);
            if (!isLetterOrUnderscore(value) && (value < '0' || value > '9')) return false;
        }
        return true;
    }

    private static String requireName(String name) {
        if (!isValidName(name)) throw new IllegalArgumentException("Invalid variable name: " + name);
        return name;
    }

    private static boolean isLetterOrUnderscore(char value) {
        return value == '_' || value >= 'a' && value <= 'z' || value >= 'A' && value <= 'Z';
    }
}
