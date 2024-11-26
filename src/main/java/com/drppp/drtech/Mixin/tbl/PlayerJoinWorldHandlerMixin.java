package com.drppp.drtech.Mixin.tbl;

import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import thebetweenlands.common.handler.PlayerJoinWorldHandler;

@Mixin(PlayerJoinWorldHandler.class)
public class PlayerJoinWorldHandlerMixin {

    @Inject(method = "onPlayerLogin", at = @At("HEAD"))
    private static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event, CallbackInfo ci) {
        return;
    }
}
