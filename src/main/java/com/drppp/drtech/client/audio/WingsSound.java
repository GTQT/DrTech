package com.drppp.drtech.Client.audio;

import com.drppp.drtech.wings.WingsFlightCapability;
import com.drppp.drtech.wings.WingsFlightData;
import net.minecraft.client.audio.MovingSound;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.MathHelper;

public final class WingsSound extends MovingSound {
    private final EntityPlayer player;

    public WingsSound(EntityPlayer player) {
        super(SoundEvents.ITEM_ELYTRA_FLYING, SoundCategory.PLAYERS);
        this.player = player;
        repeat = true;
        repeatDelay = 0;
        volume = Math.nextAfter(0.0F, 1.0D);
    }

    @Override
    public void update() {
        WingsFlightData flight = WingsFlightCapability.get(player);
        if (player.isDead) {
            donePlaying = true;
        } else if (flight != null && flight.getFlyingAmount(1.0F) > 0.0F) {
            xPosF = (float) player.posX;
            yPosF = (float) player.posY;
            zPosF = (float) player.posZ;
            float velocity = MathHelper.sqrt(player.motionX * player.motionX + player.motionY * player.motionY
                    + player.motionZ * player.motionZ);
            if (velocity >= 0.01F) {
                float halfVelocity = velocity * 0.5F;
                volume = MathHelper.clamp(halfVelocity * halfVelocity, 0.0F, 1.0F);
            } else {
                volume = 0.0F;
            }
            pitch = volume > 0.8F ? 1.0F + (volume - 0.8F) : 1.0F;
        } else {
            volume = 0.0F;
        }
    }
}
