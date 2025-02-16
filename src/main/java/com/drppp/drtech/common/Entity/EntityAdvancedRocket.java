package com.drppp.drtech.common.Entity;

import com.drppp.drtech.Client.Sound.SoundManager;
import com.drppp.drtech.Network.DimensionTeleporter;
import com.drppp.drtech.common.Items.ItemsInit;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;

public class EntityAdvancedRocket extends Entity {
    private static final DataParameter<Integer> DIM_ID = EntityDataManager.createKey(EntityAdvancedRocket.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> FUEL_AMOUNT = EntityDataManager.createKey(EntityAdvancedRocket.class, DataSerializers.VARINT);
    private static final DataParameter<Boolean> CAN_START_FLY = EntityDataManager.createKey(EntityAdvancedRocket.class, DataSerializers.BOOLEAN);
    private int fly_time = 0;
    private boolean isStart_fly = false;
    private double fly_speed = 0.55;
    private boolean send_flag=false;
    public EntityAdvancedRocket(World worldIn) {
        super(worldIn);
        this.setSize(3.0F, 12.0F); // 设置实体大小
        this.setDimId(-9999); // 初始化 DimId
        this.setFuelAmount(0); // 初始化 fuel_amount
        this.setCanStartfly(false);
    }


    @Override
    protected void entityInit() {
        this.dataManager.register(DIM_ID, -9999);
        this.dataManager.register(FUEL_AMOUNT, 0);
        this.dataManager.register(CAN_START_FLY, false);
    }
    @Override
    public boolean canBeCollidedWith() {
        return true; // 允许实体被碰撞和点击
    }
    public boolean getCanStartfly() {
        return this.dataManager.get(CAN_START_FLY);
    }

    public void setCanStartfly(boolean canDo) {
        this.dataManager.set(CAN_START_FLY, canDo);
    }
    public int getDimId() {
        return this.dataManager.get(DIM_ID);
    }

    public void setDimId(int dimId) {
        this.dataManager.set(DIM_ID, dimId);
    }

    public int getFuelAmount() {
        return this.dataManager.get(FUEL_AMOUNT);
    }

    public void setFuelAmount(int fuelAmount) {
        this.dataManager.set(FUEL_AMOUNT, fuelAmount);
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound compound) {
        this.setDimId(compound.getInteger("DimId"));
        this.setFuelAmount(compound.getInteger("FuelAmount"));
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound compound) {
        compound.setInteger("DimId", this.getDimId());
        compound.setInteger("FuelAmount", this.getFuelAmount());
    }
    @Override
    protected boolean canBeRidden(Entity entityIn) {
        return true; // 允许骑乘
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (this.isDead) {
            return;
        }
        // 播放音效
        if (getCanStartfly() && this.ticksExisted % 5 == 0 )
        {
            world.playSound(null, this.posX , this.posY, this.posZ, SoundManager.shuttle, this.getSoundCategory(), 1f, 1F);
        }
        // 服务端逻辑
        if (!this.world.isRemote) {
            if (getCanStartfly() && getDimId() != -9999) {
                if (fly_time++ >= 60) {
                    isStart_fly = true;
                }
                if (isStart_fly) {
                    this.setFuelAmount(getFuelAmount()-1);
                    if(this.getFuelAmount()<=0)
                    {
                        this.setDead();
                        this.removePassengers();
                        this.entityDropItem(getRocketItemStack(), 0.5F); // 在实体位置掉落物品
                    }
                    // 缓慢上升
                    if (fly_time % 80 == 0) {
                        fly_speed += 0.55;
                    }
                    this.motionY = fly_speed;
                    this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);

                    // 检查是否到达 y = 1000
                    if (this.posY >= 1000)
                    {
                        if (this.isBeingRidden())
                        {
                            var riders = this.getPassengers();
                            if (!riders.isEmpty())
                            {
                                for (var rider : riders)
                                {
                                    if (rider instanceof EntityPlayerMP && !send_flag)
                                    {
                                        send_flag = true;
                                        this.removePassengers();
                                        EntityPlayerMP player = (EntityPlayerMP) rider;
                                        player.addItemStackToInventory(getRocketItemStack());
                                        DimensionTeleporter.teleportToDimension(player, getDimId(), null);
                                    }
                                }
                            }
                        }

                    }
                    if(this.posY>=1000)
                        this.setDead();
                }
            }
        }

        // 客户端逻辑
        if (world.isRemote && getCanStartfly()) {
            // 生成粒子效果
            if (this.ticksExisted % 2 == 0) {
                for (int i = 0; i < 25; i++) { // 减少粒子数量
                    double offsetX = (this.rand.nextDouble() - 0.5) * 2.0;
                    double offsetZ = (this.rand.nextDouble() - 0.5) * 2.0;

                    // 火焰粒子方向
                    double motionY = -1;
                    double motionX = (this.rand.nextDouble() - 0.5) * 0.1;
                    double motionZ = (this.rand.nextDouble() - 0.5) * 0.1;

                    // 生成火焰粒子
                    this.world.spawnParticle(EnumParticleTypes.FLAME,
                            this.posX + offsetX,
                            this.posY - 1,
                            this.posZ + offsetZ,
                            motionX, motionY, motionZ);

                    // 生成烟雾粒子
                    this.world.spawnParticle(EnumParticleTypes.SMOKE_LARGE,
                            this.posX + offsetX,
                            this.posY - 1,
                            this.posZ + offsetZ,
                            motionX * 0.5, motionY * 0.5, motionZ * 0.5);
                }
            }
        }
    }

    @Override
    public double getMountedYOffset() {
        return this.height * 0.25; // 设置骑乘者的垂直偏移
    }

    @Override
    public void updatePassenger(Entity passenger) {
        if (this.isPassenger(passenger)) {
            // 设置骑乘者的位置
            double xOffset = 0.0; // X轴偏移
            double yOffset = this.getMountedYOffset() + passenger.getYOffset()+this.motionY; // Y轴偏移
            double zOffset = 0.0; // Z轴偏移
            passenger.setPosition(this.posX + xOffset, this.posY + yOffset, this.posZ + zOffset);
        }
    }

    @Override
    public void removePassengers() {
        for (Entity entity : this.getPassengers()) {
            entity.setPosition(this.posX, this.posY , this.posZ);
        }
        super.removePassengers();
    }

    private ItemStack getRocketItemStack()
    {
        ItemStack rocketItem = new ItemStack(ItemsInit.ITEM_ROCKET); // 创建 ItemAdvancedRocket 物品
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("Fuel",getFuelAmount());
        tag.setInteger("TargetDim",getDimId());
        rocketItem.setTagCompound(tag);
        return rocketItem;
    }
    @Override
    public boolean processInitialInteract(EntityPlayer player, EnumHand hand) {
        if (!this.world.isRemote) { // 确保此逻辑在服务端执行
            if (!player.isSneaking() && !this.isBeingRidden()) { // 确保玩家未按下潜行键
                player.startRiding(this); // 让玩家骑上该实体
                return true; // 返回 true 表示交互已处理
            }else
            {
               if(!this.isDead && !this.isBeingRidden())
               {
                   this.setDead(); // 移除实体
                   this.entityDropItem(getRocketItemStack(), 0.5F); // 在实体位置掉落物品
               }
                return true; // 返回 true 表示交互已处理

            }
        }
        return super.processInitialInteract(player, hand);
    }
}