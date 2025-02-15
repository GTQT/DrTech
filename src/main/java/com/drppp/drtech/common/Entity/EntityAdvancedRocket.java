package com.drppp.drtech.common.Entity;

import com.drppp.drtech.common.Items.ItemsInit;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
public class EntityAdvancedRocket extends Entity {
    private static final DataParameter<Integer> DIM_ID = EntityDataManager.createKey(EntityAdvancedRocket.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> FUEL_AMOUNT = EntityDataManager.createKey(EntityAdvancedRocket.class, DataSerializers.VARINT);
    public EntityAdvancedRocket(World worldIn) {
        super(worldIn);
        this.setSize(3.0F, 12.0F); // 设置实体大小
        this.setDimId(-9999); // 初始化 DimId
        this.setFuelAmount(0); // 初始化 fuel_amount
    }


    @Override
    protected void entityInit() {
        this.dataManager.register(DIM_ID, -9999);
        this.dataManager.register(FUEL_AMOUNT, 0);
    }
    @Override
    public boolean canBeCollidedWith() {
        return true; // 允许实体被碰撞和点击
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
        // 只在服务端运行
        if (!this.world.isRemote) {
            // 检查是否有骑乘者
            if (this.isBeingRidden()) {
                Entity rider = this.getRidingEntity();

                // 检查骑乘者是否是玩家
                if (rider instanceof EntityPlayer) {
                    EntityPlayer player = (EntityPlayer) rider;

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
            double yOffset = this.getMountedYOffset() + passenger.getYOffset(); // Y轴偏移
            double zOffset = 0.0; // Z轴偏移
            passenger.setPosition(this.posX + xOffset, this.posY + yOffset, this.posZ + zOffset);
        }
    }

    @Override
    public void removePassengers() {
        for (Entity entity : this.getPassengers()) {
            double yOffset = this.getMountedYOffset() + entity.getYOffset();
            entity.setPosition(this.posX, this.posY + yOffset, this.posZ);
        }
        super.removePassengers();
    }

    @Override
    public boolean processInitialInteract(EntityPlayer player, EnumHand hand) {
        if (!this.world.isRemote) { // 确保此逻辑在服务端执行
            if (!player.isSneaking() && !this.isBeingRidden()) { // 确保玩家未按下潜行键
                player.startRiding(this); // 让玩家骑上该实体
                player.sendMessage(new TextComponentString("发射维度：" + getDimId() + " 燃料总量:" + getFuelAmount()));
                return true; // 返回 true 表示交互已处理
            }else
            {
               if(!this.isDead && !this.isBeingRidden())
               {
                   this.setDead(); // 移除实体
                   ItemStack rocketItem = new ItemStack(ItemsInit.ITEM_ROCKET); // 创建 ItemAdvancedRocket 物品
                   NBTTagCompound tag = new NBTTagCompound();
                   tag.setInteger("Fuel",getFuelAmount());
                   tag.setInteger("TargetDim",getDimId());
                   rocketItem.setTagCompound(tag);
                   this.entityDropItem(rocketItem, 0.5F); // 在实体位置掉落物品
               }
                return true; // 返回 true 表示交互已处理

            }
        }
        return super.processInitialInteract(player, hand); // 调用父类方法处理其他交互
    }
}