package com.drppp.drtech.drone.api;

import com.drppp.drtech.drone.entity.EntityProgrammableDrone;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

/** Posted after normal right-click authorization and before a drone changes sign text. */
@Cancelable
public final class DroneSignEditEvent extends Event {

    private final EntityProgrammableDrone drone;
    private final BlockPos target;
    private final String[] previousLines;
    private final String[] proposedLines;

    public DroneSignEditEvent(EntityProgrammableDrone drone, BlockPos target,
            String[] previousLines, String[] proposedLines) {
        if (drone == null || target == null || previousLines == null || proposedLines == null
                || previousLines.length != 4 || proposedLines.length != 4) {
            throw new IllegalArgumentException("Drone sign edit event requires two four-line payloads");
        }
        this.drone = drone;
        this.target = target.toImmutable();
        this.previousLines = previousLines.clone();
        this.proposedLines = proposedLines.clone();
    }

    public EntityProgrammableDrone getDrone() {
        return drone;
    }

    public BlockPos getTarget() {
        return target;
    }

    public String[] getPreviousLines() {
        return previousLines.clone();
    }

    public String[] getProposedLines() {
        return proposedLines.clone();
    }
}
