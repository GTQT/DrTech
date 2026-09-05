package com.drppp.drtech.Mixin;

import com.drppp.drtech.wings.WingsFlightCapability;
import com.drppp.drtech.wings.WingsFlightData;
import me.paulf.wings.util.Mth;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ModelPlayer.class)
public abstract class MixinModelPlayerWings extends ModelBiped {
    @Inject(method = "setRotationAngles", at = @At("RETURN"))
    private void drtech$applyWingFlightPose(float limbSwing, float limbSwingAmount, float ageInTicks,
                                             float netHeadYaw, float headPitch, float scaleFactor,
                                             Entity entity, CallbackInfo callback) {
        if (!(entity instanceof EntityPlayer)) {
            return;
        }
        EntityPlayer player = (EntityPlayer) entity;
        WingsFlightData flight = WingsFlightCapability.get(player);
        float partialTicks = ageInTicks - player.ticksExisted;
        float amount = flight == null ? 0.0F : flight.getFlyingAmount(partialTicks);
        if (amount <= 0.0F) {
            return;
        }
        bipedHead.rotateAngleX = Mth.toRadians(Mth.lerp(headPitch, headPitch / 4.0F - 90.0F, amount));
        bipedLeftArm.rotateAngleX = Mth.lerp(bipedLeftArm.rotateAngleX, -3.2F, amount);
        bipedRightArm.rotateAngleX = Mth.lerp(bipedRightArm.rotateAngleX, -3.2F, amount);
        bipedLeftLeg.rotateAngleX = Mth.lerp(bipedLeftLeg.rotateAngleX, 0.0F, amount);
        bipedRightLeg.rotateAngleX = Mth.lerp(bipedRightLeg.rotateAngleX, 0.0F, amount);
        ModelBase.copyModelAngles(bipedHead, bipedHeadwear);
        ModelBase.copyModelAngles(bipedLeftLeg, ((ModelPlayer) (Object) this).bipedLeftLegwear);
        ModelBase.copyModelAngles(bipedRightLeg, ((ModelPlayer) (Object) this).bipedRightLegwear);
        ModelBase.copyModelAngles(bipedLeftArm, ((ModelPlayer) (Object) this).bipedLeftArmwear);
        ModelBase.copyModelAngles(bipedRightArm, ((ModelPlayer) (Object) this).bipedRightArmwear);
    }
}
