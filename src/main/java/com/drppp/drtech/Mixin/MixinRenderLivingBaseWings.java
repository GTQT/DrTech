package com.drppp.drtech.Mixin;

import com.drppp.drtech.wings.WingsFlightCapability;
import com.drppp.drtech.wings.WingsFlightData;
import me.paulf.wings.util.Mth;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderLivingBase.class)
public abstract class MixinRenderLivingBaseWings {
    @Inject(method = "applyRotations", at = @At("RETURN"))
    private void drtech$applyWingFlightRotation(EntityLivingBase entity, float ageInTicks,
                                                 float rotationYaw, float partialTicks,
                                                 CallbackInfo callback) {
        if (!(entity instanceof EntityPlayer)) {
            return;
        }
        EntityPlayer player = (EntityPlayer) entity;
        WingsFlightData flight = WingsFlightCapability.get(player);
        if (flight == null) {
            return;
        }
        float amount = flight.getFlyingAmount(partialTicks);
        if (amount <= 0.0F) {
            return;
        }
        float roll = Mth.lerpDegrees(
                player.prevRenderYawOffset - player.prevRotationYaw,
                player.renderYawOffset - player.rotationYaw,
                partialTicks);
        float pitch = -Mth.lerpDegrees(player.prevRotationPitch, player.rotationPitch, partialTicks) - 90.0F;
        GlStateManager.rotate(Mth.lerpDegrees(0.0F, roll, amount), 0.0F, 0.0F, 1.0F);
        GlStateManager.rotate(Mth.lerpDegrees(0.0F, pitch, amount), 1.0F, 0.0F, 0.0F);
        GlStateManager.translate(0.0F, -1.2F * Mth.easeInOut(amount), 0.0F);
    }
}
