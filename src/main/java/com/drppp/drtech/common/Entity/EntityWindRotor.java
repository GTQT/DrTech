package com.drppp.drtech.common.Entity;

import com.drppp.drtech.common.MetaTileEntities.muti.electric.generator.MeTaTileEntityWindDrivenGenerator;
import gregtech.api.util.GTUtility;
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
    public BlockPos machinePos=null;

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
        compound.setInteger("MX",this.machinePos.getX());
        compound.setInteger("MY",this.machinePos.getY());
        compound.setInteger("MZ",this.machinePos.getZ());
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.dataManager.set(SPEED, compound.getInteger("Speed"));
        this.machinePos = new BlockPos(compound.getInteger("MX"),compound.getInteger("MY"),compound.getInteger("MZ"));
    }
    private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event) {
       // int speed = this.dataManager.get(SPEED);
        event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.model.working", ILoopType.EDefaultLoopTypes.LOOP));
        return software.bernie.geckolib3.core.PlayState.CONTINUE;
    }

    @Override
    public boolean hasNoGravity() {
        return true;
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

    /**
     * Called to update the entity's position/logic.
     */
    @Override
    public void onUpdate() {
        super.onUpdate();
        if(!this.isDead && !this.world.isRemote && this.machinePos!=null)
        {
            if(GTUtility.getMetaTileEntity(this.world,machinePos) instanceof MeTaTileEntityWindDrivenGenerator)
            {
                var ma = (MeTaTileEntityWindDrivenGenerator)GTUtility.getMetaTileEntity(this.world,machinePos);
                if(ma.rotor!=null && ma.rotor!=this)
                    this.setDead();
                else
                    ma.rotor = this;
            }
        }
    }
}
