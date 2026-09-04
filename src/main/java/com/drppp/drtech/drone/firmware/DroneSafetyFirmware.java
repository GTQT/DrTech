package com.drppp.drtech.drone.firmware;

import net.minecraft.nbt.NBTTagCompound;

/**
 * Built-in persistent low-energy policy kept outside the visual program runtime. It is part of every chassis,
 * rather than a removable module, so recall and emergency landing remain available to legacy drones.
 *
 * <p>The firmware only decides state; the entity owns movement, docking and EU mutation.</p>
 */
public final class DroneSafetyFirmware {

    public static final int DEFAULT_RETURN_PERCENT = 20;
    public static final int DEFAULT_RESUME_PERCENT = 90;

    private int returnAtPercent = DEFAULT_RETURN_PERCENT;
    private int resumeAtPercent = DEFAULT_RESUME_PERCENT;
    private DroneSafetyState state = DroneSafetyState.PROGRAM;
    private boolean resumeProgram;

    public DroneSafetyState evaluate(int energyPercent, boolean hasBoundDock, boolean dockAvailable,
            boolean dockReached) {
        int energy = clampPercent(energyPercent);
        // SAFE_IDLE used to be terminal when no dock existed: recalling, charging and redeploying the drone
        // could leave a valid program suspended forever. Recover on the next evaluation when there is still
        // usable energy and no dock can service the drone; the entity policy decides whether to pre-empt again.
        if (state == DroneSafetyState.SAFE_IDLE) {
            if (energy <= 1) state = DroneSafetyState.EMERGENCY_LAND;
            else if (!dockAvailable) state = DroneSafetyState.RECOVERING;
        }
        if (state == DroneSafetyState.PROGRAM && energy <= returnAtPercent) {
            if (hasBoundDock) state = DroneSafetyState.RETURNING;
            else state = energy <= 1 ? DroneSafetyState.EMERGENCY_LAND : DroneSafetyState.SAFE_IDLE;
        }
        if (state == DroneSafetyState.RETURNING) {
            if (energy <= 0 && !dockReached) {
                state = DroneSafetyState.EMERGENCY_LAND;
            } else if (!hasBoundDock || !dockAvailable) {
                state = energy <= 1 ? DroneSafetyState.EMERGENCY_LAND : DroneSafetyState.SAFE_IDLE;
            } else if (dockReached) {
                state = DroneSafetyState.CHARGING;
            }
        } else if (state == DroneSafetyState.CHARGING && energy >= resumeAtPercent) {
            state = DroneSafetyState.RECOVERING;
        }
        return state;
    }

    public void beginReturn(boolean shouldResumeProgram) {
        resumeProgram = shouldResumeProgram;
        state = DroneSafetyState.RETURNING;
    }

    public void markCharging() {
        state = DroneSafetyState.CHARGING;
    }

    public void finishRecovery() {
        state = DroneSafetyState.PROGRAM;
    }

    /** A dock launch is a new flight; persisted RETURNING/CHARGING state must not recall it immediately. */
    public void prepareForLaunch() {
        state = DroneSafetyState.PROGRAM;
        resumeProgram = false;
    }

    public void retryBoundDock() {
        if (state == DroneSafetyState.SAFE_IDLE) state = DroneSafetyState.RETURNING;
    }

    public DroneSafetyState getState() {
        return state;
    }

    public boolean shouldResumeProgram() {
        return resumeProgram;
    }

    /** Low energy only pre-empts the graph when recovery is possible, except for the final emergency reserve. */
    public boolean shouldPreemptProgram(int energyPercent, boolean usableDockAvailable) {
        int energy = clampPercent(energyPercent);
        return energy <= returnAtPercent && (usableDockAvailable || energy <= 1);
    }

    public void setResumeProgram(boolean resumeProgram) {
        this.resumeProgram = resumeProgram;
    }

    public int getReturnAtPercent() {
        return returnAtPercent;
    }

    public int getResumeAtPercent() {
        return resumeAtPercent;
    }

    public void setThresholds(int returnAtPercent, int resumeAtPercent) {
        int boundedReturn = Math.max(1, Math.min(95, returnAtPercent));
        int boundedResume = Math.max(boundedReturn + 1, Math.min(100, resumeAtPercent));
        this.returnAtPercent = boundedReturn;
        this.resumeAtPercent = boundedResume;
    }

    public NBTTagCompound writeToNbt() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("ReturnAtPercent", returnAtPercent);
        tag.setInteger("ResumeAtPercent", resumeAtPercent);
        tag.setString("State", state.name());
        tag.setBoolean("ResumeProgram", resumeProgram);
        return tag;
    }

    public void readFromNbt(NBTTagCompound tag) {
        if (tag == null) return;
        setThresholds(tag.hasKey("ReturnAtPercent") ? tag.getInteger("ReturnAtPercent") : DEFAULT_RETURN_PERCENT,
                tag.hasKey("ResumeAtPercent") ? tag.getInteger("ResumeAtPercent") : DEFAULT_RESUME_PERCENT);
        try {
            state = DroneSafetyState.valueOf(tag.getString("State"));
        } catch (IllegalArgumentException ignored) {
            state = DroneSafetyState.PROGRAM;
        }
        resumeProgram = tag.getBoolean("ResumeProgram");
    }

    private static int clampPercent(int percent) {
        return Math.max(0, Math.min(100, percent));
    }
}
