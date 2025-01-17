package com.drppp.drtech.common.Entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public class EntityAdvancedRocket extends EntityLiving {
    public int DimId=-9999;
    public int fuel_amount =0;

    public EntityAdvancedRocket(World worldIn) {
        super(worldIn);
        this.setSize(3,12);
        this.setNoGravity(true);
        this.setHealth(9999999);
    }

    @Override
    public void fall(float distance, float damageMultiplier) {

    }
    @Override
    public boolean isEntityInvulnerable(DamageSource source) {
        // 声明实体是无敌的
        return true;
    }
    @Override
    protected boolean canDespawn() {
        return false;
    }

    @Override
    public boolean canBeLeashedTo(@NotNull EntityPlayer player) {
        return false;
    }

    @Override
    public boolean canBePushed() {
        return false;
    }

    @Override
    public void applyEntityCollision(Entity entityIn) {
        // 阻止任何碰撞推动逻辑
    }
    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        // 禁止任何形式的伤害
        return false;
    }
    @Override
    public void move(MoverType type, double x, double y, double z) {
        // 禁止外部推动
        if (type != MoverType.SELF) {
            return;
        }
        super.move(type, x, y, z);
    }

    @Override
    public boolean canBeCollidedWith() {
        return true; // 允许碰撞，以便玩家右键交互
    }

    @Override
    public boolean canBeHitWithPotion() {
        return false;
    }

    @Override
    public void knockBack(@NotNull Entity entityIn, float strength, double xRatio, double zRatio) {

    }
    @Override
    public void onLivingUpdate() {
        // 禁止实体受重力或其他外力影响
        this.motionX = 0.0;
        this.motionY = 0.0;
        this.motionZ = 0.0;

        super.onLivingUpdate();
    }

    @Override
    public void onUpdate() {
        super.onUpdate();

        // 检查是否有骑乘者
        if (this.isBeingRidden()) {
            // 获取骑乘者
            Entity entity = this.getControllingPassenger();

            if (entity instanceof EntityPlayer) {
                EntityPlayer player = (EntityPlayer) entity;

                // 检查玩家是否按下了潜行键
                if (player.isSneaking()) {
                    player.dismountRidingEntity(); // 解除骑乘
                }
            }
        }
    }

    /**
     * Returns the Y Offset of this entity.
     */
    @Override
    public double getYOffset() {
        return -6;
    }

    @Override
    public void updatePassenger(Entity passenger) {
        if (this.isPassenger(passenger)) {
            // 设置骑乘者的位置
            double xOffset = 0.0; // X轴偏移
            double yOffset = this.getMountedYOffset() + this.getYOffset(); // Y轴偏移
            double zOffset = 0.0; // Z轴偏移
            passenger.setPosition(this.posX + xOffset, this.posY + yOffset, this.posZ + zOffset);
        }
    }
    @Override
    public void removePassengers() {
        super.removePassengers();
        for (Entity entity : this.getPassengers()) {
            double yOffset = this.getMountedYOffset() + this.getYOffset();
            entity.setPosition(this.posX, this.posY + yOffset, this.posZ);
        }
    }

    // 处理右键点击事件
    @Override
    protected boolean processInteract(EntityPlayer player, EnumHand hand) {
        if (!this.world.isRemote) { // 确保此逻辑在服务端执行
            if (!player.isSneaking()) { // 确保玩家未按下潜行键
                player.startRiding(this); // 让玩家骑上该实体
                player.sendMessage(new TextComponentString("发射维度："+DimId +" 燃料总量:"+fuel_amount));
                return true; // 返回 true 表示交互已处理
            }
        }
        return super.processInteract(player, hand); // 调用父类方法处理其他交互
    }
}
