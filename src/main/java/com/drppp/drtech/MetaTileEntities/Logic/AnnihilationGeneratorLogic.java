package com.drppp.drtech.MetaTileEntities.Logic;

import com.drppp.drtech.MetaTileEntities.muti.ecectric.generator.AnnihilationGenerator;
import com.drppp.drtech.Tile.TileEntityGravitationalAnomaly;
import gregtech.api.capability.GregtechDataCodes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class AnnihilationGeneratorLogic {
    private final AnnihilationGenerator host;

    public AnnihilationGenerator getHost() {
        return host;
    }

    public int getMaxProgress() {
        return maxProgress;
    }

    public int getProgressTime() {
        return progressTime;
    }

    public boolean isActive() {
        return this.isActive && this.isWorkingEnabled;
    }

    public boolean isWorkingEnabled() {
        return isWorkingEnabled;
    }

    public void setMaxProgress(int maxProgress) {
        this.maxProgress = maxProgress;
    }

    public void setProgressTime(int progressTime) {
        this.progressTime = progressTime;
    }

    public void setActive(boolean active) {
        if (this.isActive != active) {
            this.isActive = active;
            this.host.markDirty();
            World world = this.host.getWorld();
            if (world != null && !world.isRemote) {
                this.host.writeCustomData(GregtechDataCodes.WORKABLE_ACTIVE, buf -> buf.writeBoolean(active));
            }
        }
    }

    public void setWorkingEnabled(boolean workingEnabled) {
        this.isWorkingEnabled = workingEnabled;
        this.host.markDirty();
        World world = this.host.getWorld();
        if (world != null && !world.isRemote) {
            this.host.writeCustomData(GregtechDataCodes.WORKING_ENABLED, buf -> buf.writeBoolean(workingEnabled));
        }
    }
    public void setmEUt(int mEUt) {
        this.mEUt = mEUt;
    }

    public void seteAmpereFlow(int eAmpereFlow) {
        this.eAmpereFlow = eAmpereFlow;
    }

    public long getmEUt() {
        return mEUt;
    }

    public int geteAmpereFlow() {
        return eAmpereFlow;
    }

    private int maxProgress = 200;
    private int progressTime = 0;
    private boolean isActive;
    private boolean isWorkingEnabled ;
    private long mEUt;
    private int eAmpereFlow;
    private int eVoltage;
    private int tire=0;
    public int weight=0;

    public int getTire() {
        return tire;
    }

    public AnnihilationGeneratorLogic(AnnihilationGenerator host) {
        this.host = host;
    }
    public void updateLogic(TileEntityGravitationalAnomaly entity)
    {
        if (!this.host.isWorkingEnabled()){return;}
            if(entity.weight<=0)
            {
                this.setActive(false);
                //this.setWorkingEnabled(false);
                return;
            }
            this.weight = entity.weight;

            if(progressTime>=this.maxProgress)
            {
                CalculatingTireAndAmp();
                this.eAmpereFlow = Math.min(++(this.eAmpereFlow),getMaxAmp());
                this.progressTime=0;
                entity.weight -=this.eAmpereFlow;
                this.weight = entity.weight = Math.max(0, entity.weight);
            }
            CaculatemEut();
            host.getEnergyContainer().changeEnergy(this.mEUt);
        if(this.weight<=0)
        {
            this.eAmpereFlow=0;
            this.setActive(false);
            this.host.markDirty();
        }
            progressTime++;


    }
    private void CaculatemEut()
    {
       long baseEnergy =  this.eVoltage*this.eAmpereFlow;
       float power = host.getLeve()*0.25f;
       this.mEUt = (long) (baseEnergy * power);
    }
    private void CalculatingTireAndAmp()
    {
        if(this.weight>0 && this.weight<=400)
        {
            if(this.tire != 1)
            {
                this.eAmpereFlow=1;
            }
            this.tire = 1;
            this.eVoltage =7800;
        }else if(this.weight>400 && this.weight<=800)
        {
            if(this.tire != 2)
            {
                this.eAmpereFlow=1;
            }
            this.tire = 2;
            this.eVoltage = 32400;
        }else if(this.weight>800 && this.weight<=1200)
        {
            if(this.tire != 3)
            {
                this.eAmpereFlow=1;
            }
            this.tire = 3;
            this.eVoltage = 97500;
        }else if(this.weight>1200 && this.weight<=1600)
        {
            if(this.tire != 4)
            {
                this.eAmpereFlow=1;
            }
            this.tire = 4;
            this.eVoltage = 2700000;
        }else if(this.weight>1600 && this.weight<=2000)
        {
            if(this.tire != 5)
            {
                this.eAmpereFlow=1;
            }
            this.tire = 5;
            this.eVoltage = 8540000;
        }
        else
        {
            this.tire = 1;
            this.eVoltage =7800;
        }
    }

    public int geteVoltage() {
        return eVoltage;
    }

    private int getMaxAmp()
    {
        switch (this.tire)
        {
            case 1:
                return 50;
            case 2:
                return 100;
            case 3:
                return 150;
            case 4:
                return 200;
            case 5:
                return 300;
            default:
                return 1;
        }

    }
    public NBTTagCompound writeToNBT(@Nonnull NBTTagCompound data) {
        data.setBoolean("isActive", this.isActive);
        data.setBoolean("isWorkingEnabled", this.isWorkingEnabled);
        data.setInteger("progressTime", this.progressTime);
        data.setInteger("maxProgress", this.maxProgress);
        data.setLong("mEUt", this.mEUt);
        data.setInteger("eAmpereFlow", this.eAmpereFlow);
        data.setInteger("eVoltage", this.eVoltage);
        data.setInteger("tire", this.tire);

        return data;
    }

    public void readFromNBT(@Nonnull NBTTagCompound data) {
        this.isActive = data.getBoolean("isActive");
        this.isWorkingEnabled = data.getBoolean("isWorkingEnabled");
        this.progressTime = data.getInteger("progressTime");
        this.maxProgress = data.getInteger("maxProgress");
        this.mEUt = data.getLong("mEUt");
        this.eAmpereFlow = data.getInteger("eAmpereFlow");
        this.eVoltage = data.getInteger("eVoltage");
        this.tire = data.getInteger("tire");
    }
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        if (dataId == GregtechDataCodes.WORKABLE_ACTIVE) {
            this.isActive = buf.readBoolean();
            host.scheduleRenderUpdate();
        } else if (dataId == GregtechDataCodes.WORKING_ENABLED) {
            this.isWorkingEnabled = buf.readBoolean();
            host.scheduleRenderUpdate();
        }
    }
    public void writeInitialSyncData(@Nonnull PacketBuffer buf) {
        buf.writeBoolean(this.isActive );
        buf.writeBoolean(this.isWorkingEnabled);
        buf.writeInt(this.progressTime);
        buf.writeInt(this.maxProgress);
        buf.writeLong(this.mEUt);
        buf.writeInt(this.eAmpereFlow);
        buf.writeInt(this.eVoltage);
        buf.writeInt(this.tire);
    }
    public void receiveInitialSyncData(@Nonnull PacketBuffer buf) {
        this.isActive = buf.readBoolean();
        this.isWorkingEnabled = buf.readBoolean();
        this.progressTime = buf.readInt();
        this.maxProgress = buf.readInt();
        this.mEUt = buf.readLong();
        this.eAmpereFlow = buf.readInt();
        this.eVoltage = buf.readInt();
        this.tire = buf.readInt();
    }
}
