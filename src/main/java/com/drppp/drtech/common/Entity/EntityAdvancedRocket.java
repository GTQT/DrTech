package com.drppp.drtech.common.Entity;

import net.minecraft.entity.EntityLiving;
import net.minecraft.world.World;

public class EntityAdvancedRocket extends EntityLiving {
    public EntityAdvancedRocket(World worldIn) {
        super(worldIn);
        this.setNoGravity(true);
        this.setEntityInvulnerable(true);
        this.noClip = true;
    }
    @Override
    public boolean hasNoGravity() {
        return true;
    }

    @Override
    public boolean canBePushed() {
        return false;
    }
    @Override
    public boolean canBeCollidedWith() {
        return true;
    }
}
