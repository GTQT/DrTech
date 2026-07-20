package com.drppp.drtech.Mixin;

import com.drppp.drtech.wings.WingsFlightCapability;
import com.drppp.drtech.wings.WingsFlightData;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityLivingBase.class)
public abstract class MixinEntityLivingBaseWings {
    @Inject(method = "isElytraFlying", at = @At("HEAD"), cancellable = true)
    private void drtech$useElytraPoseForWingFlight(CallbackInfoReturnable<Boolean> callback) {
        if ((Object) this instanceof EntityPlayer) {
            WingsFlightData flight = WingsFlightCapability.get((EntityPlayer) (Object) this);
            if (flight != null && flight.isFlying()) {
                callback.setReturnValue(true);
            }
        }
    }
}
