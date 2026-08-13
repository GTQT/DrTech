package com.drppp.drtech.common.drone.program.runtime;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

import java.util.UUID;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Persistent program-local memory. Internal loop counters are isolated from user variables. */
public final class DroneRuntimeMemory {

    public static final int MAX_VARIABLE_NAME_LENGTH = 24;

    private NBTTagCompound variables = new NBTTagCompound();
    private NBTTagCompound loops = new NBTTagCompound();
    private NBTTagCompound actionAmounts = new NBTTagCompound();
    private NBTTagCompound actionPositions = new NBTTagCompound();
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
    }

    public NBTTagCompound writeToNbt() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setTag("Variables", variables.copy());
        tag.setTag("Loops", loops.copy());
        tag.setTag("ActionAmounts", actionAmounts.copy());
        tag.setTag("ActionPositions", actionPositions.copy());
        tag.setString("LastActionStatus", lastActionStatus.name());
        if (!lastActionError.isEmpty()) tag.setString("LastActionError", lastActionError);
        return tag;
    }

    public void readFromNbt(NBTTagCompound tag) {
        variables = tag.hasKey("Variables", 10) ? tag.getCompoundTag("Variables").copy() : new NBTTagCompound();
        loops = tag.hasKey("Loops", 10) ? tag.getCompoundTag("Loops").copy() : new NBTTagCompound();
        actionAmounts = tag.hasKey("ActionAmounts", 10)
                ? tag.getCompoundTag("ActionAmounts").copy() : new NBTTagCompound();
        actionPositions = tag.hasKey("ActionPositions", 10)
                ? tag.getCompoundTag("ActionPositions").copy() : new NBTTagCompound();
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
        loops = new NBTTagCompound();
        actionAmounts = new NBTTagCompound();
        actionPositions = new NBTTagCompound();
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

    private static String format(double value) {
        if (value == Math.rint(value) && Math.abs(value) <= Long.MAX_VALUE) return Long.toString((long) value);
        return Double.toString(value);
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
