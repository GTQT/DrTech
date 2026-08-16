package com.drppp.drtech.common.drone.sound;

/** Stable semantic sound cues; resource packs may provide the final audio assets. */
public enum DroneSoundCue {
    ROTOR_LOOP(true, 32F, 20), TAKEOFF(false, 48F, 10), LANDING(false, 48F, 10), DOCK_LOCK(false, 24F, 10),
    EU_CHARGE(true, 24F, 20), TOOL_ARM(false, 24F, 4), INSTANT_BREAK(false, 32F, 4), PLACE_CONFIRM(false, 32F, 4),
    PROGRAM_START(false, 24F, 10), PROGRAM_END(false, 24F, 10), ERROR(false, 24F, 20), LOW_ENERGY(false, 24F, 60),
    DAMAGED(false, 32F, 10), DEATH(false, 64F, 20);
    private final boolean loop; private final float maxDistance; private final long minIntervalTicks;
    DroneSoundCue(boolean loop,float maxDistance,long minIntervalTicks){this.loop=loop;this.maxDistance=maxDistance;this.minIntervalTicks=minIntervalTicks;}
    public boolean isLoop(){return loop;} public float getMaxDistance(){return maxDistance;} public long getMinIntervalTicks(){return minIntervalTicks;}
}
