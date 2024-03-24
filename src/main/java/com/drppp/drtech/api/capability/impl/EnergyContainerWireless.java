package com.drppp.drtech.api.capability.impl;

import com.drppp.drtech.api.Utils.DrtechUtils;
import com.drppp.drtech.api.WirelessNetwork.WirelessNetworkManager;
import gregtech.api.capability.impl.EnergyContainerHandler;
import gregtech.api.metatileentity.MetaTileEntity;
import net.minecraft.nbt.NBTTagCompound;
import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;
import java.util.UUID;

public class EnergyContainerWireless extends EnergyContainerHandler {

    public UUID ownerUuid=null;
    
    public EnergyContainerWireless(MetaTileEntity tileEntity, boolean isExport, long voltage, long amperage){
        this(tileEntity,voltage*amperage*320,isExport?0:voltage,amperage,isExport?voltage:0,amperage);
    }
    
    public EnergyContainerWireless(MetaTileEntity tileEntity, long maxCapacity, long maxInputVoltage, long maxInputAmperage, long maxOutputVoltage, long maxOutputAmperage) {
        super(tileEntity, maxCapacity, maxInputVoltage, maxInputAmperage, maxOutputVoltage, maxOutputAmperage);
    }
    
    @Override
    public void update() {
        //super.update();
        if(!this.metaTileEntity.getWorld().isRemote){
            if(this.metaTileEntity.getOffsetTimer() % 5 == 0 && this.ownerUuid!=null){
                WirelessNetworkManager.strongCheckOrAddUser(this.ownerUuid);
                //是动力舱 给网络增加能量
                if(this.getInputVoltage()==0)
                {
                    if(this.energyStored>0)
                    {
                        WirelessNetworkManager.addEUToGlobalEnergyMap(this.ownerUuid,this.energyStored);
                        this.removeEnergy(this.energyStored);
                    }
                }//是能源仓 抽取能量
                else
                {
                    long needEnergy = this.getEnergyCapacity()-this.getEnergyStored();
                    BigInteger min = DrtechUtils.getBigIntegerMin(WirelessNetworkManager.getUserEU(this.ownerUuid),BigInteger.valueOf(needEnergy));
                    if (min.compareTo(BigInteger.valueOf(Long.MIN_VALUE)) < 0 ||
                            min.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) > 0) {
                        System.out.println("BigInteger out of long range");
                    } else {
                        long longValue = min.longValue(); // 安全转换为long
                        this.addEnergy(longValue);
                        WirelessNetworkManager.addEUToGlobalEnergyMap(this.ownerUuid,-longValue);
                    }

                }
            }
        }
    }


    @Override
    public long getEnergyCanBeInserted() {
        return 0;
    }
}
