package com.drppp.drtech.Tile;

import com.drppp.drtech.DrtConfig;
import com.drppp.drtech.api.Utils.WaterWheelHelper;
import com.drppp.drtech.api.capability.DrtechCommonCapabilities;
import com.drppp.drtech.api.capability.IRotationEnergy;
import com.drppp.drtech.api.capability.IRotationSpeed;
import com.drppp.drtech.api.capability.impl.RotationEnergyHandler;
import com.drppp.drtech.common.Items.ItemsInit;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.capabilities.Capability;
import org.jetbrains.annotations.Nullable;

public class TileEntityWaterMill extends TileEntity implements ITickable, IRotationSpeed {

    private IRotationEnergy ru = new RotationEnergyHandler();
    private EnumFacing facing = EnumFacing.NORTH;
    private int rotationTicks = 0;
    private int serverTicks = 0;
    private int rotationSpeed = 0;
    double flowMagnitude = 0d;
    Vec3d push = new Vec3d(0,0,0);
    public TileEntityWaterMill() {
        ru.setRuEnergy(0);
    }

    public int getRotationTicks() {
        return rotationTicks;
    }
    public int getRotationSpeed() {
        return rotationSpeed;
    }
    public void setRotationSpeed(int speed) {
        this.rotationSpeed = speed;
        this.markDirty();
        world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3); // 通知客户端更新
    }
    public EnumFacing getFacing() {
        return facing;
    }

    public void setFacing(EnumFacing facing) {
        this.facing = facing;
        this.markDirty();
    }
    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        NBTTagCompound tag = new NBTTagCompound();
        this.writeToNBT(tag);
        return new SPacketUpdateTileEntity(this.pos, 1, tag);
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        this.readFromNBT(pkt.getNbtCompound());
    }
    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        this.facing = EnumFacing.byHorizontalIndex(compound.getInteger("facing"));
        this.rotationSpeed = compound.getInteger("rotationSpeed");
        this.serverTicks = compound.getInteger("serverTicks");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setInteger("facing", this.facing.getHorizontalIndex());
        compound.setInteger("rotationSpeed", this.rotationSpeed);
        compound.setInteger("serverTicks", this.serverTicks);
        return compound;
    }


    @Override
    public void update() {
        if (getWorld().isRemote) {
            rotationTicks++;
            if (rotationTicks >= 360) {
                rotationTicks = 0;
            }
        }
        if(!getWorld().isRemote)
        {
            if(++serverTicks>=20)
            {
                serverTicks=0;
                push = WaterWheelHelper.calculateWaterFlowPush(getWorld(),getPos());
                flowMagnitude= push.length();
                setRotationSpeed(calculateSpeedFromFlow(flowMagnitude));
            }
            ru.setRuEnergy((int)(DrtConfig.MillExchangeRate * flowMagnitude));
            if(getWorld().getTileEntity(getPos().offset(facing.getOpposite())) instanceof TileEntityWaterMill)
            {
                TileEntityWaterMill before =  (TileEntityWaterMill)getWorld().getTileEntity(getPos().offset(facing.getOpposite()));
                ru.setRuEnergy(before.getEnergy().getEnergyOutput()+this.ru.getEnergyOutput());
                if(ru.getEnergyOutput()>155)
                {
                    getWorld().setBlockToAir(getPos());
                    getWorld().spawnEntity(new EntityItem(getWorld(),getPos().getX(),getPos().getY(),getPos().getZ(),new ItemStack(ItemsInit.ITEM_BLOCK_WATER_MILL)));
                }
            }
        }
    }
    private int calculateSpeedFromFlow(double flowMagnitude) {
        if (flowMagnitude < 1.0) {
            return 0; // 静止或几乎无流动
        } else if (flowMagnitude < 2.0) {
            return 3; // 缓慢流动
        } else if (flowMagnitude < 3.0) {
            return 5; // 中等流动
        } else {
            return 6; // 快速流动
        }
    }
    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        if(capability== DrtechCommonCapabilities.CAPABILITY_ROTATION_ENERGY && facing==this.facing)
            return true;
        return super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if(capability== DrtechCommonCapabilities.CAPABILITY_ROTATION_ENERGY && facing==this.facing)
            return DrtechCommonCapabilities.CAPABILITY_ROTATION_ENERGY.cast(ru);
        return super.getCapability(capability, facing);
    }

    @Override
    public int getSpeed() {
        return this.getRotationSpeed();
    }

    @Override
    public void setSpeed(int speed) {
        this.setRotationSpeed(speed);
    }

    @Override
    public IRotationEnergy getEnergy() {
        return ru;
    }
}
