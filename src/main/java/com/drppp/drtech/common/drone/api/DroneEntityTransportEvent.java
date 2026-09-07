package com.drppp.drtech.common.drone.api;

import com.drppp.drtech.common.drone.entity.EntityProgrammableDrone;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

/** Posted immediately before a drone removes or respawns an owned entity. */
@Cancelable
public final class DroneEntityTransportEvent extends Event {

    public enum Action {
        LOAD,
        RELEASE
    }

    private final Action action;
    private final EntityProgrammableDrone drone;
    private final EntityLivingBase entity;
    private final BlockPos target;

    public DroneEntityTransportEvent(Action action, EntityProgrammableDrone drone,
            EntityLivingBase entity, BlockPos target) {
        if (action == null || drone == null || entity == null || target == null) {
            throw new IllegalArgumentException("Transport event fields are required");
        }
        this.action = action;
        this.drone = drone;
        this.entity = entity;
        this.target = target.toImmutable();
    }

    public Action getAction() {
        return action;
    }

    public EntityProgrammableDrone getDrone() {
        return drone;
    }

    public EntityLivingBase getEntity() {
        return entity;
    }

    public BlockPos getTarget() {
        return target;
    }
}
