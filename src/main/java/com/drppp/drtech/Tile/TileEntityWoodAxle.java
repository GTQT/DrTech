package com.drppp.drtech.Tile;

import com.drppp.drtech.DrtConfig;
import com.drppp.drtech.api.capability.DrtechCommonCapabilities;
import com.drppp.drtech.api.capability.IRotationEnergy;
import com.drppp.drtech.api.capability.IRotationSpeed;
import com.drppp.drtech.api.capability.impl.RotationEnergyHandler;
import com.drppp.drtech.common.Items.ItemsInit;
import gregtech.api.util.GTUtility;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import org.jetbrains.annotations.Nullable;



public class TileEntityWoodAxle extends TileEntity implements ITickable, IRotationSpeed {

    private IRotationEnergy ru = new RotationEnergyHandler();
    private EnumFacing facing = EnumFacing.NORTH;
    private int rotationTicks = 0;
    private int rotationSpeed = 0;
    public int getRotationTicks() {
        return rotationTicks;
    }
    public int getRotationSpeed() {
        return rotationSpeed;
    }
    public void setRotationSpeed(int speed)
    {
        this.rotationSpeed = speed;
        this.markDirty();
        world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
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
        this.facing = EnumFacing.byIndex(compound.getInteger("facing"));
        this.rotationSpeed = compound.getInteger("rotationSpeed");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setInteger("facing", this.facing.getIndex());
        compound.setInteger("rotationSpeed", this.rotationSpeed);
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
            BlockPos oppositePos = pos.offset(facing.getOpposite());
            if (world.isBlockLoaded(oppositePos)) {
                var tile =  world.getTileEntity(oppositePos);
                if(tile!=null && tile.hasCapability(DrtechCommonCapabilities.CAPABILITY_ROTATION_ENERGY,facing))
                {
                    this.ru = tile.getCapability(DrtechCommonCapabilities.CAPABILITY_ROTATION_ENERGY,facing);
                    if(tile instanceof IRotationSpeed)
                        this.setRotationSpeed(((IRotationSpeed)tile).getSpeed());
                    else if(GTUtility.getMetaTileEntity(getWorld(),oppositePos) instanceof IRotationSpeed)
                        this.setRotationSpeed(((IRotationSpeed)GTUtility.getMetaTileEntity(getWorld(),oppositePos)).getSpeed());
                    if(ru.getEnergyOutput()> DrtConfig.MaxRu)
                    {
                        getWorld().setBlockToAir(getPos());
                        getWorld().spawnEntity(new EntityItem(getWorld(),getPos().getX(),getPos().getY(),getPos().getZ(),new ItemStack(ItemsInit.ITEM_BLOCK_WOOD_AXLE)));
                    }
                }else
                {
                    this.ru.setRuEnergy(0);
                    this.setRotationSpeed(0);
                }
            }
        }
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @org.jetbrains.annotations.Nullable EnumFacing facing) {
        if(capability== DrtechCommonCapabilities.CAPABILITY_ROTATION_ENERGY && facing==this.facing)
            return true;
        return super.hasCapability(capability, facing);
    }

    @org.jetbrains.annotations.Nullable
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
