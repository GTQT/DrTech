package com.drppp.drtech.drone.sound;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Per-drone sound throttle with linear distance attenuation. */
public final class DroneSoundLimiter {
    private final Map<String,Long> lastPlayed=new HashMap<>();
    public synchronized boolean shouldPlay(UUID droneId,DroneSoundCue cue,long worldTime){if(droneId==null||cue==null)return false;String key=droneId+":"+cue.name();long last=lastPlayed.getOrDefault(key,Long.MIN_VALUE);if(last!=Long.MIN_VALUE&&worldTime>=last&&worldTime-last<cue.getMinIntervalTicks())return false;lastPlayed.put(key,worldTime);if(lastPlayed.size()>4096)lastPlayed.entrySet().removeIf(entry->worldTime>=entry.getValue()&&worldTime-entry.getValue()>2400L);return true;}
    public static float volume(DroneSoundCue cue,double distance){if(cue==null||distance>=cue.getMaxDistance())return 0F;return Math.max(0F,1F-(float)(distance/cue.getMaxDistance()));}
}
