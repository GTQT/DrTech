package com.drppp.drtech.Mixin.minecraft;

import com.drppp.drtech.api.Utils.ElytraFlyingUtils;
import keqing.gtqtcore.api.utils.GTQTLog;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(EntityPlayer.class)
public abstract class EntityPlayerMixin extends EntityLivingBase {

    public EntityPlayerMixin(World worldIn) {
        super(worldIn);
    }

    @Override
    public boolean isElytraFlying() {
        return super.isElytraFlying() || ElytraFlyingUtils.isElytraFlying(this);
    }
}