package com.drppp.drtech.common.Entity;

import net.minecraft.entity.EntityLiving;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.builder.ILoopType;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

public class EntityWindRotor  extends EntityLiving implements IAnimatable {
    private static final DataParameter<Integer> SPEED = EntityDataManager.<Integer>createKey(EntityWindRotor.class, DataSerializers.VARINT);

    private static final BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

    private AnimationFactory factory = new AnimationFactory(this);
    public EntityWindRotor(World worldIn) {
        super(worldIn);
        // 关闭重力
        this.setNoGravity(true);
        // 使实体无敌，也就是没有血量的概念
        this.setEntityInvulnerable(true);
        // 设置为不可碰撞状态
        this.noClip = true;
        rideCooldown = -1;
    }
    public EntityWindRotor(World worldIn, double x, double y, double z) {
        super(worldIn);

        this.setLocationAndAngles(x, y, z, 0.F, 0.F);
        // 关闭重力
        this.setNoGravity(true);
        // 使实体无敌，也就是没有血量的概念
        this.setEntityInvulnerable(true);
        // 设置为不可碰撞状态
        this.noClip = true;
        rideCooldown = -1;
        this.setEntityBoundingBox(new AxisAlignedBB(x-1, y+0, z-1, x+1, y+1, z+1));
    }

    public EntityWindRotor(World worldIn, BlockPos pos) {
        this(worldIn, pos.getX() + 0.5F, pos.getZ() + 0.5F, pos.getZ() + 0.5F);
    }
    @Override
    public void registerControllers(AnimationData animationData) {
        animationData.addAnimationController(new AnimationController<EntityWindRotor>(this, "controller", 0, this::predicate));
    }

    @Override
    public AnimationFactory getFactory() {
        return this.factory;
    }
    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(SPEED, 0);

    }
    @Override
    public void setAir(int air) {
        super.setAir(300);
    }
    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);;
        compound.setInteger("Speed", this.dataManager.get(SPEED));
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.dataManager.set(SPEED, compound.getInteger("Speed"));
    }
    private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event) {
       // int speed = this.dataManager.get(SPEED);
        event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.windrotor.rotate", ILoopType.EDefaultLoopTypes.LOOP));
        return software.bernie.geckolib3.core.PlayState.CONTINUE;
    }
    @Override
    public boolean canBePushed() {
        // 返回false表示实体不能被推动，即没有碰撞体积
        return false;
    }
    @Override
    public boolean canBeCollidedWith() {
        // 返回false表示实体不可以和其他实体碰撞
        return false;
    }
}
