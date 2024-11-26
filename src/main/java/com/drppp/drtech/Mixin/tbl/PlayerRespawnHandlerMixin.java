package com.drppp.drtech.Mixin.tbl;

import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import thebetweenlands.common.handler.PlayerRespawnHandler;

@Mixin(PlayerRespawnHandler.class)
public class PlayerRespawnHandlerMixin {

    @ModifyVariable(
            method = "onRespawn",
            at = @At(value = "STORE"),
            ordinal = 0,
            name = "shouldTeleportToBL"
    )
    private static boolean modifyShouldTeleportToBL(boolean originalValue, PlayerEvent.PlayerRespawnEvent event) {
        if (event.player.dimension == -1) {
            return true;
        }
        return originalValue;
    }

    @ModifyVariable(
            method = "onRespawn",
            at = @At(value = "STORE"),
            ordinal = 1,
            name = "Lnet/minecraftforge/event/entity/player/PlayerEvent$PlayerRespawnEvent;dimension:I"
    )
    private static int modifyDimension(int originalValue, PlayerEvent.PlayerRespawnEvent event) {
        if (event.player.dimension == -1) {
            return -20;
        }
        return originalValue;
    }
}