package com.drppp.drtech.Tile;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;

public class TileEntityConnector extends TileEntity implements ITickable {
    private String NBT_STORE = "StoredEnergy";
    public BlockPos selfPos;
    public BlockPos nextPos;
    public BlockPos beforePos;
    public  long MaxEnergy;
    public long StoredEnergy=0;

    public int success=0;
    public TileEntityConnector(int tire) {
        int v = (int)Math.pow(4,(4+2*tire));
        MaxEnergy =  v;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        this.MaxEnergy = compound.getLong("MaxEnergy");
        this.StoredEnergy = compound.getLong(NBT_STORE);
        this.success = compound.getInteger("Success");
        if(compound.hasKey("selfPos"))
        {
            NBTTagCompound selfPos = compound.getCompoundTag("selfPos");
            this.selfPos = new BlockPos(selfPos.getInteger("xx"),selfPos.getInteger("yy"),selfPos.getInteger("zz"));
        }
        if(compound.hasKey("nextPos"))
        {
            NBTTagCompound nextPos = compound.getCompoundTag("nextPos");
            this.nextPos = new BlockPos(nextPos.getInteger("xx"),nextPos.getInteger("yy"),nextPos.getInteger("zz"));
        }
        if(compound.hasKey("beforePos"))
        {
            NBTTagCompound beforePos = compound.getCompoundTag("beforePos");
            this.beforePos = new BlockPos(beforePos.getInteger("xx"),beforePos.getInteger("yy"),beforePos.getInteger("zz"));
        }

    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setLong("MaxEnergy",MaxEnergy);
        compound.setLong(NBT_STORE,StoredEnergy);
        compound.setInteger("Success",success);
        if(this.selfPos!=null)
        {
            NBTTagCompound selfPos = new NBTTagCompound();
            selfPos.setInteger("xx",this.selfPos.getX());
            selfPos.setInteger("yy",this.selfPos.getY());
            selfPos.setInteger("zz",this.selfPos.getZ());
            compound.setTag("selfPos",selfPos);
        }
        if(this.nextPos != null)
        {
            NBTTagCompound nextPos = new NBTTagCompound();
            nextPos.setInteger("xx",this.nextPos.getX());
            nextPos.setInteger("yy",this.nextPos.getY());
            nextPos.setInteger("zz",this.nextPos.getZ());
            compound.setTag("nextPos",nextPos);
        }
        if(this.beforePos!=null)
        {
            NBTTagCompound beforePos = new NBTTagCompound();
            beforePos.setInteger("xx",this.beforePos.getX());
            beforePos.setInteger("yy",this.beforePos.getY());
            beforePos.setInteger("zz",this.beforePos.getZ());
            compound.setTag("beforePos",beforePos);
        }
        return compound;
    }

    @Override
    public void update() {
        if(!this.world.isRemote)
        {
            this.StoredEnergy++;
            if(this.beforePos!=null && this.success==1)
            {
                TileEntityConnector before = (TileEntityConnector)world.getTileEntity(this.beforePos);
                if(before.StoredEnergy>0 && this.StoredEnergy<this.MaxEnergy)
                {
                    before.drain(this.fill(before.StoredEnergy));
                }
            }
            this.markDirty();
        }
    }
    //返回需要扣除的能量
    public long fill(long amount)
    {
        if(amount<=0)
            return 0;
        if(this.StoredEnergy+amount >= this.MaxEnergy)
        {
            long left = this.MaxEnergy - this.StoredEnergy;
            this.StoredEnergy = this.MaxEnergy;
            return left;
        }
        else if(this.StoredEnergy+amount <this.MaxEnergy)
        {
            this.StoredEnergy += amount;
            return amount;
        }
        return 0;
    }
    //返回需要增加的能量
    public long drain(long amount)
    {
        if(amount<=0)
            return 0;
        if(amount>=this.StoredEnergy)
        {
            long left = this.StoredEnergy;
            this.StoredEnergy=0;
            return left;
        }
        this.StoredEnergy -= amount;
        return  amount;
    }
}
