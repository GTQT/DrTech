package com.drppp.drtech.common.drone.api;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.eventhandler.Event;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Forge event for third-party action handlers; values are runtime-owned callbacks. */
public class DroneActionRegistryEvent extends Event {
    private final Map<ResourceLocation, Object> actions = new LinkedHashMap<>();
    public void register(ResourceLocation id, Object handler) { if (id == null || handler == null) throw new IllegalArgumentException("Action id and handler are required"); if (actions.putIfAbsent(id, handler) != null) throw new IllegalArgumentException("Duplicate action id " + id); }
    public Map<ResourceLocation, Object> getActions() { return Collections.unmodifiableMap(actions); }
}
