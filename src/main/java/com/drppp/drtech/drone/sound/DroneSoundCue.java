package com.drppp.drtech.drone.sound;

import com.drppp.drtech.common.sound.DrTechSounds;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.SoundEvent;

/** Stable semantic sound cues; resource packs may provide the final audio assets. */
public enum DroneSoundCue {
    ROTOR_LOOP(true, 32F, 20), TAKEOFF(false, 48F, 10), LANDING(false, 48F, 10), DOCK_LOCK(false, 24F, 10),
    EU_CHARGE(true, 24F, 20), TOOL_ARM(false, 24F, 4), INSTANT_BREAK(false, 32F, 4), PLACE_CONFIRM(false, 32F, 4),
    PROGRAM_START(false, 24F, 10), PROGRAM_END(false, 24F, 10), ERROR(false, 24F, 20), LOW_ENERGY(false, 24F, 60),
    DAMAGED(false, 32F, 10), DEATH(false, 64F, 20);
    private final boolean loop; private final float maxDistance; private final long minIntervalTicks;
    DroneSoundCue(boolean loop,float maxDistance,long minIntervalTicks){this.loop=loop;this.maxDistance=maxDistance;this.minIntervalTicks=minIntervalTicks;}
    public boolean isLoop(){return loop;} public float getMaxDistance(){return maxDistance;} public long getMinIntervalTicks(){return minIntervalTicks;}

    /** Vanilla-backed defaults keep the feature functional while allowing later resource-pack replacement. */
    public SoundEvent getDefaultSound() {
        switch (this) {
            case ROTOR_LOOP: return DrTechSounds.DRONE_ROTOR;
            case TAKEOFF: return SoundEvents.ENTITY_FIREWORK_LAUNCH;
            case LANDING: return SoundEvents.BLOCK_PISTON_CONTRACT;
            case DOCK_LOCK: return SoundEvents.BLOCK_IRON_TRAPDOOR_CLOSE;
            case EU_CHARGE: return SoundEvents.BLOCK_REDSTONE_TORCH_BURNOUT;
            case TOOL_ARM: return SoundEvents.BLOCK_IRON_TRAPDOOR_OPEN;
            case INSTANT_BREAK: return SoundEvents.BLOCK_STONE_BREAK;
            case PLACE_CONFIRM: return SoundEvents.BLOCK_METAL_PLACE;
            case PROGRAM_START: return SoundEvents.BLOCK_NOTE_PLING;
            case PROGRAM_END: return SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP;
            case ERROR: return SoundEvents.BLOCK_NOTE_BASS;
            case LOW_ENERGY: return SoundEvents.BLOCK_NOTE_HAT;
            case DAMAGED: return SoundEvents.ENTITY_IRONGOLEM_HURT;
            case DEATH: return SoundEvents.ENTITY_IRONGOLEM_DEATH;
            default: return SoundEvents.BLOCK_NOTE_HAT;
        }
    }
}
