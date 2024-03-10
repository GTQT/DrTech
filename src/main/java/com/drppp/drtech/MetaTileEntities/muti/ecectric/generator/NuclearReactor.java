package com.drppp.drtech.MetaTileEntities.muti.ecectric.generator;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.drppp.drtech.Blocks.BlocksInit;
import com.drppp.drtech.Blocks.MetaBlocks.MetaCasing;
import com.drppp.drtech.Blocks.MetaBlocks.MetaCasing1;
import com.drppp.drtech.Client.Textures;
import com.drppp.drtech.api.capability.*;
import gregtech.api.capability.*;
import gregtech.api.capability.impl.EnergyContainerList;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.gui.widgets.AdvancedTextWidget;
import gregtech.api.gui.widgets.ImageCycleButtonWidget;
import gregtech.api.gui.widgets.ImageWidget;
import gregtech.api.metatileentity.IDataInfoProvider;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockWithDisplayBase;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.client.renderer.ICubeRenderer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import scala.reflect.internal.Trees;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.BooleanSupplier;

public class NuclearReactor extends MultiblockWithDisplayBase implements IDataInfoProvider, IWorkable, IControllable,INuclearDataShow {
    private boolean isActive=true, isWorkingEnabled = true;
    private int process;
    private int maxProcess;
    protected IEnergyContainer energyContainer = new EnergyContainerList(new ArrayList());
    private final ItemStackHandler inventory = new ItemStackHandler(81);
    private final ItemStackHandler upgradeInventory = new ItemStackHandler(5);
    private int tick=0,heat=0,outputEnergy=0;
    private final int MaxHeat = 10000;

    @Override
    public int getHeat() {
        return this.heat;
    }
    @Override
    public int getMaxHeat() {
        return MaxHeat;
    }

    @Override
    public int getEnergyOut() {
        return this.outputEnergy;
    }

    public NuclearReactor(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    public boolean isWorkingEnabled() {
        return this.isWorkingEnabled;
    }

    @Override
    public void setWorkingEnabled(boolean b) {
        this.isWorkingEnabled = b;
        markDirty();
        World world = getWorld();
        if (world != null && !world.isRemote) {
            writeCustomData(GregtechDataCodes.WORKING_ENABLED, buf -> buf.writeBoolean(isWorkingEnabled));
        }
    }

    @Override
    public int getProgress() {
        return this.process;
    }

    @Override
    public int getMaxProgress() {
        return this.maxProcess;
    }

    @Override
    public boolean isActive() {
        return super.isActive() && this.isActive;
    }

    public void setActive(boolean active) {
        if (this.isActive != active) {
            this.isActive = active;
            markDirty();
            World world = getWorld();
            if (world != null && !world.isRemote) {
                writeCustomData(GregtechDataCodes.WORKABLE_ACTIVE, buf -> buf.writeBoolean(active));
            }
        }
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new NuclearReactor(this.metaTileEntityId);
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeBoolean(isActive);
        buf.writeBoolean(isWorkingEnabled);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        isActive = buf.readBoolean();
        isWorkingEnabled = buf.readBoolean();
    }

    @Override
    public void receiveCustomData(int dataId, @NotNull PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == GregtechDataCodes.WORKABLE_ACTIVE) {
            isActive = buf.readBoolean();
            scheduleRenderUpdate();
        } else if (dataId == GregtechDataCodes.WORKING_ENABLED) {
            isWorkingEnabled = buf.readBoolean();
            scheduleRenderUpdate();
        }
    }
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setBoolean("isActive", isActive);
        data.setBoolean("isWorkingEnabled", isWorkingEnabled);
        data.setTag("inventory", inventory.serializeNBT());
        data.setTag("inventoryUpgrade", upgradeInventory.serializeNBT());
        data.setInteger("Heat",this.heat);
        return data;
    }

    @Override
    protected void updateFormedValid() {
        if(tick++>10 )
        {
            tick=0;
            this.outputEnergy=0;
            for (int i = 0; i < inventory.getSlots(); i++) {
                ItemStack stack = inventory.getStackInSlot(i);
                    if(stack.hasCapability(DrtechCommonCapabilities.CAPABILITY_FUEL_ROAD,null) && this.isWorkingEnabled())
                    {
                        var ca = stack.getCapability(DrtechCommonCapabilities.CAPABILITY_FUEL_ROAD,null);
                        if (ca != null) {
                            ca.setDurability(ca.getDurability()-1);
                            if(ca.getDurability()<=0)
                            {
                                inventory.extractItem(i,1,false);

                                inventory.insertItem(i, ca.outItem(),false);
                            }
                            FuelRodOperation(i,ca);
                        }
                    }else if(stack.hasCapability(DrtechCommonCapabilities.CAPABILITY_HEAT_VENT,null))
                    {
                        var ca = stack.getCapability(DrtechCommonCapabilities.CAPABILITY_HEAT_VENT,null);
                        if (ca != null) {
                            if(ca.getHeat()>=ca.getMaxHeat() && ca.getMaxHeat()>0)
                            {
                                inventory.extractItem(i,1,false);
                            }
                            HeatVentOperation(i,ca);
                        }
                    }else if(stack.hasCapability(DrtechCommonCapabilities.CAPABILITY_HEAT_EXCHANGER,null))
                    {
                        var ca = stack.getCapability(DrtechCommonCapabilities.CAPABILITY_HEAT_EXCHANGER,null);
                        if (ca != null) {
                            if(ca.getHeat()>=ca.getMaxHeat())
                            {
                                inventory.extractItem(i,1,false);
                            }
                            HeatExchangerOperation(i,ca);
                        }
                    }
                if(this.heat>=getMaxHeat())
                {
                    setWorkingEnabled(false);
                    for (int j = 0; j < inventory.getSlots(); j++) {
                        inventory.extractItem(j,1,false);
                    }
                    for (int j = 0; j < upgradeInventory.getSlots(); j++) {
                        upgradeInventory.extractItem(j,1,false);
                    }
                    this.getWorld().createExplosion(null, this.getPos().getX(), this.getPos().getY(), this.getPos().getZ(), 10.0F, true);
                }
            }
        }
        this.energyContainer.addEnergy(this.outputEnergy);
    }
    private void HeatExchangerOperation(int i, IHeatExchanger data)
    {

        if(data.reactorInteraction()==true && data.elementInteraction()==false)
        {
            int selfHeatRate = (int)((float)data.getHeat()/(float) data.getMaxHeat() *100);
            int reactorRate = (int)((float)this.heat/(float) getMaxHeat() *100);
            //反应堆交互
            if(selfHeatRate>reactorRate)
            {
                //送热
                int canTeansheat =Math.min( getMaxHeat() - this.heat,data.getReactorExchangeHeatRate());
                canTeansheat = Math.min(canTeansheat,data.getHeat());
                this.heat +=canTeansheat;
                data.setHeat(Math.max(0,data.getHeat()-canTeansheat));

            }else
            {
                //吸热
                int canAbHeat = Math.min(data.getMaxHeat()-data.getHeat(),data.getReactorExchangeHeatRate());
                this.heat -=canAbHeat;
                this.heat = Math.max(0,this.heat);
                data.setHeat(data.getHeat()+canAbHeat);
                if(data.getHeat()>=data.getMaxHeat())
                    inventory.extractItem(i,1,false);
            }
        }else if(data.reactorInteraction()==false && data.elementInteraction()==true)
        {
            List<Integer> list= new ArrayList<>();
            int canSoreAmount = getCanStoreHeatElementAmount(i,list,0);
            //元件交互
            if(canSoreAmount>0)
            {
                for (int loca :list)
                {
                    setTransHeat(data.getElementExchangeHeatRate(),loca,data);
                }
            }
        }else if(data.reactorInteraction()==true && data.elementInteraction()==true)
        {
            List<Integer> list= new ArrayList<>();
            int selfHeatRate = (int)((float)data.getHeat()/(float) data.getMaxHeat() *100);
            int reactorRate = (int)((float)this.heat/(float) getMaxHeat() *100);
            int canSoreAmount = getCanStoreHeatElementAmount(i,list,0);
            //反应堆交互
            if(selfHeatRate>reactorRate)
            {
                //送热
                int canTeansheat =Math.min( getMaxHeat() - this.heat,data.getReactorExchangeHeatRate());
                canTeansheat = Math.min(canTeansheat,data.getHeat());
                this.heat +=canTeansheat;
                data.setHeat(Math.max(0,data.getHeat()-canTeansheat));

            }else
            {
                //吸热
                int canAbHeat = Math.min(data.getMaxHeat()-data.getHeat(),data.getReactorExchangeHeatRate());
                this.heat -=canAbHeat;
                this.heat = Math.max(0,this.heat);
                data.setHeat(data.getHeat()+canAbHeat);
                if(data.getHeat()>=data.getMaxHeat())
                    inventory.extractItem(i,1,false);
            }
            //元件交互
            if(canSoreAmount>0)
            {
                for (int loca :list)
                {
                    setTransHeat(data.getElementExchangeHeatRate(),loca,data);
                }
            }
        }


 }
    private void HeatVentOperation(int i, IHeatVent data)
    {
      if(data.reactorInteraction()==false && data.elementInteraction()==false)
      {
          data.setHeat(Math.max(0,data.getHeat()-data.getHeatDissipationRate()));
      }else if(data.reactorInteraction()==true && data.elementInteraction()==false)
      {

          //最大吸热
            int can_ab_heat = Math.min( Math.min(data.getMaxHeat()-data.getHeat(),data.getHeatAbsorptionRate()),this.heat);
            this.heat -=can_ab_heat;
            data.setHeat(data.getHeat()+can_ab_heat);
            if(data.getHeat()>=data.getMaxHeat())
                inventory.extractItem(i,1,false);
            data.setHeat(data.getHeat()-data.getHeatDissipationRate());

      }else if(data.reactorInteraction()==false && data.elementInteraction()==true)
      {
               if(i%9!=0 && inventory.getStackInSlot(i-1).hasCapability(DrtechCommonCapabilities.CAPABILITY_HEAT_VENT,null))
               {
                   var ca = inventory.getStackInSlot(i-1).getCapability(DrtechCommonCapabilities.CAPABILITY_HEAT_VENT,null);
                   ca.setHeat(Math.max(0,ca.getHeat()-data.getHeatDissipationRate()));

               }else if(i%9!=0 && inventory.getStackInSlot(i-1).hasCapability(DrtechCommonCapabilities.CAPABILITY_HEAT_EXCHANGER,null))
               {
                   var ca = inventory.getStackInSlot(i-1).getCapability(DrtechCommonCapabilities.CAPABILITY_HEAT_EXCHANGER,null);
                   ca.setHeat(Math.max(0,ca.getHeat()-data.getHeatDissipationRate()));
               }

                if((i+1)%9!=0 && inventory.getStackInSlot(i+1).hasCapability(DrtechCommonCapabilities.CAPABILITY_HEAT_VENT,null))
               {
                   var ca = inventory.getStackInSlot(i+1).getCapability(DrtechCommonCapabilities.CAPABILITY_HEAT_VENT,null);
                   ca.setHeat(Math.max(0,ca.getHeat()-data.getHeatDissipationRate()));
               }
               else if((i+1)%9!=0 &&  inventory.getStackInSlot(i+1).hasCapability(DrtechCommonCapabilities.CAPABILITY_HEAT_EXCHANGER,null))
               {
                   var ca = inventory.getStackInSlot(i+1).getCapability(DrtechCommonCapabilities.CAPABILITY_HEAT_EXCHANGER,null);
                   ca.setHeat(Math.max(0,ca.getHeat()-data.getHeatDissipationRate()));
               }

                if(i>8 && inventory.getStackInSlot(i-9).hasCapability(DrtechCommonCapabilities.CAPABILITY_HEAT_VENT,null))
               {
                   var ca = inventory.getStackInSlot(i-9).getCapability(DrtechCommonCapabilities.CAPABILITY_HEAT_VENT,null);
                   ca.setHeat(Math.max(0,ca.getHeat()-data.getHeatDissipationRate()));
               }
               else if(i>8 && inventory.getStackInSlot(i-9).hasCapability(DrtechCommonCapabilities.CAPABILITY_HEAT_EXCHANGER,null))
               {
                   var ca = inventory.getStackInSlot(i-9).getCapability(DrtechCommonCapabilities.CAPABILITY_HEAT_EXCHANGER,null);
                   ca.setHeat(Math.max(0,ca.getHeat()-data.getHeatDissipationRate()));
               }

               if(i<72 && inventory.getStackInSlot(i+9).hasCapability(DrtechCommonCapabilities.CAPABILITY_HEAT_VENT,null))
               {
                   var ca = inventory.getStackInSlot(i+9).getCapability(DrtechCommonCapabilities.CAPABILITY_HEAT_VENT,null);
                   ca.setHeat(Math.max(0,ca.getHeat()-data.getHeatDissipationRate()));
               }
               else if(i<72 && inventory.getStackInSlot(i+9).hasCapability(DrtechCommonCapabilities.CAPABILITY_HEAT_EXCHANGER,null))
               {
                   var ca = inventory.getStackInSlot(i+9).getCapability(DrtechCommonCapabilities.CAPABILITY_HEAT_EXCHANGER,null);
                   ca.setHeat(Math.max(0,ca.getHeat()-data.getHeatDissipationRate()));
               }


      }
    }
private void FuelRodOperation(int i, IFuelRodData data)
{
        int outenergy=data.getBaseEnergy();
        float baseHeat = data.getBaseHeat();
        int outheat = 0;
        int amount=0;
        List<Integer> list = new ArrayList<>();
        if( i%9!=0 && inventory.getStackInSlot(i-1).hasCapability(DrtechCommonCapabilities.CAPABILITY_FUEL_ROAD,null))
            amount++;
        else if( i%9!=0 && inventory.getStackInSlot(i-1).hasCapability(DrtechCommonCapabilities.CAPABILITY_NEUTRON_REFLECTOR,null))
        {
            amount++;
            INeutronReflector ca= inventory.getStackInSlot(i-1).getCapability(DrtechCommonCapabilities.CAPABILITY_NEUTRON_REFLECTOR,null);
            if(ca.getMaxDurability()!=999999999)
                ca.setDurability(ca.getDurability()-data.getPulseNum());
            if(ca.getDurability()<=0)
                inventory.extractItem(i-1,1,false);
        }
        if((i+1)%9!=0 && inventory.getStackInSlot(i+1).hasCapability(DrtechCommonCapabilities.CAPABILITY_FUEL_ROAD,null))
            amount++;
        else if((i+1)%9!=0 &&  inventory.getStackInSlot(i+1).hasCapability(DrtechCommonCapabilities.CAPABILITY_NEUTRON_REFLECTOR,null))
        {
            amount++;
            INeutronReflector ca= inventory.getStackInSlot(i+1).getCapability(DrtechCommonCapabilities.CAPABILITY_NEUTRON_REFLECTOR,null);
            if(ca.getMaxDurability()!=999999999)
                ca.setDurability(ca.getDurability()-data.getPulseNum());
            if(ca.getDurability()<=0)
                inventory.extractItem(i+1,1,false);
        }
        if(i>8 && inventory.getStackInSlot(i-9).hasCapability(DrtechCommonCapabilities.CAPABILITY_FUEL_ROAD,null))
            amount++;
        else if(i>8 && inventory.getStackInSlot(i-9).hasCapability(DrtechCommonCapabilities.CAPABILITY_NEUTRON_REFLECTOR,null))
        {
            amount++;
            INeutronReflector ca= inventory.getStackInSlot(i-9).getCapability(DrtechCommonCapabilities.CAPABILITY_NEUTRON_REFLECTOR,null);
            if(ca.getMaxDurability()!=999999999)
                ca.setDurability(ca.getDurability()-data.getPulseNum());
            if(ca.getDurability()<=0)
                inventory.extractItem(i-9,1,false);
        }
        if(i<72 && inventory.getStackInSlot(i+9).hasCapability(DrtechCommonCapabilities.CAPABILITY_FUEL_ROAD,null))
            amount++;
        else if(i<72 && inventory.getStackInSlot(i+9).hasCapability(DrtechCommonCapabilities.CAPABILITY_NEUTRON_REFLECTOR,null))
        {
            amount++;
            INeutronReflector ca= inventory.getStackInSlot(i+9).getCapability(DrtechCommonCapabilities.CAPABILITY_NEUTRON_REFLECTOR,null);
            if(ca.getMaxDurability()!=999999999)
                ca.setDurability(ca.getDurability()-data.getPulseNum());
            if(ca.getDurability()<=0)
                inventory.extractItem(i+9,1,false);
        }
        int x = 1+data.getPulseNum()/2+amount;
        outheat = (int)(baseHeat*x*(x+1));//2 6 12
        if(amount!=0)
        {
            outenergy = outenergy + data.get1XEnergy()*amount*data.getPulseNum();
        }
        if(data.isMox())
        {
            outenergy = (int)(outenergy * (1+data.getMoxMulti()*((float)this.heat/(float)this.MaxHeat)));
            if((int)(((float)this.heat/(float)this.MaxHeat)*100)>50)
            {
                outheat *=2;
            }
        }
        this.outputEnergy += outenergy;
        int num = getCanStoreHeatElementAmount(i,list,1);
        int leftheat=0;
       if(num==0)
           this.heat+=outheat;
       else
       {
           leftheat = outheat%num;
           for (int j = 0; j < list.size(); j++) {
               leftheat = setHeat((int)((float)outheat/(float)num)+leftheat,list.get(j));
           }
           this.heat+=leftheat;
       }
}
//返回没有写入的热量
private int setHeat(int heatAmount,int i)
{
     if(  inventory.getStackInSlot(i).hasCapability(DrtechCommonCapabilities.CAPABILITY_HEAT_EXCHANGER ,null))
        {
            IHeatExchanger ca =  inventory.getStackInSlot(i).getCapability(DrtechCommonCapabilities.CAPABILITY_HEAT_EXCHANGER,null);
            if(ca.getMaxHeat()-ca.getHeat()>=heatAmount)
            {
                ca.setHeat(ca.getHeat()+heatAmount);
            }else
            {
                inventory.extractItem(i,1,false);
                return  heatAmount-(ca.getMaxHeat()-ca.getHeat());
            }
        }
    else if(inventory.getStackInSlot(i).hasCapability(DrtechCommonCapabilities.CAPABILITY_COOLANT_CELL,null))
    {
        var ca =  inventory.getStackInSlot(i).getCapability(DrtechCommonCapabilities.CAPABILITY_COOLANT_CELL,null);
        if(ca.getMaxDurability()-ca.getDurability()>=heatAmount)
        {
            ca.setDurability(ca.getDurability()+heatAmount);
        }else
        {
            inventory.extractItem(i,1,false);
            return  heatAmount-(ca.getMaxDurability()-ca.getDurability());
        }
    }else if(inventory.getStackInSlot(i).hasCapability(DrtechCommonCapabilities.CAPABILITY_HEAT_VENT,null))
    {
        var ca =  inventory.getStackInSlot(i).getCapability(DrtechCommonCapabilities.CAPABILITY_HEAT_VENT,null);
          if(ca.getMaxHeat()>0)
          {
              if(ca.getMaxHeat()-ca.getHeat()>=heatAmount)
              {
                  ca.setHeat(ca.getHeat()+heatAmount);
              }else
              {
                  inventory.extractItem(i,1,false);
                  return  heatAmount-(ca.getMaxHeat()-ca.getHeat());
              }
          }
    }
    return 0;
}
    //返回没有写入的热量
    private int setTransHeat(int heatAmount,int i,IHeatExchanger data)
    {
        if(  inventory.getStackInSlot(i).hasCapability(DrtechCommonCapabilities.CAPABILITY_HEAT_EXCHANGER ,null))
        {
            IHeatExchanger ca =  inventory.getStackInSlot(i).getCapability(DrtechCommonCapabilities.CAPABILITY_HEAT_EXCHANGER,null);
            int selfHeatRate = (int)((float)data.getHeat()/(float) data.getMaxHeat() *100);
            int transHeatRate = (int)((float)ca.getHeat()/(float) ca.getMaxHeat() *100);
            //送热
            if(selfHeatRate>transHeatRate)
            {
                if(ca.getMaxHeat()-ca.getHeat()>=heatAmount)
                {
                    data.setHeat(Math.max(data.getHeat()-heatAmount,0));
                    ca.setHeat(ca.getHeat()+heatAmount);
                }else
                {
                    inventory.extractItem(i,1,false);
                    return  heatAmount-(ca.getMaxHeat()-ca.getHeat());
                }
            }
            //吸热
            else
            {
                if(data.getMaxHeat()-data.getHeat()>=heatAmount)
                {
                    data.setHeat(data.getHeat()+heatAmount);
                    ca.setHeat(Math.max(ca.getHeat()-heatAmount,0));
                }else
                {
                    inventory.extractItem(i,1,false);
                    return  heatAmount-(data.getMaxHeat()-data.getHeat());
                }
            }

        }
        else if(inventory.getStackInSlot(i).hasCapability(DrtechCommonCapabilities.CAPABILITY_HEAT_VENT,null))
        {
            var ca =  inventory.getStackInSlot(i).getCapability(DrtechCommonCapabilities.CAPABILITY_HEAT_VENT,null);
            int selfHeatRate = (int)((float)data.getHeat()/(float) data.getMaxHeat() *100);
            int transHeatRate = (int)((float)ca.getHeat()/(float) ca.getMaxHeat() *100);
            //送热
            if(selfHeatRate>transHeatRate)
            {
                if(ca.getMaxHeat()-ca.getHeat()>=heatAmount)
                {
                    data.setHeat(Math.max(data.getHeat()-heatAmount,0));
                    ca.setHeat(ca.getHeat()+heatAmount);
                }else
                {
                    inventory.extractItem(i,1,false);
                    return  heatAmount-(ca.getMaxHeat()-ca.getHeat());
                }
            }
            //吸热
            else
            {
                if(data.getMaxHeat()-data.getHeat()>=heatAmount)
                {
                    data.setHeat(data.getHeat()+heatAmount);
                    ca.setHeat(Math.max(ca.getHeat()-heatAmount,0));
                }else
                {
                    inventory.extractItem(i,1,false);
                    return  heatAmount-(data.getMaxHeat()-data.getHeat());
                }
            }
        }
        return 0;
    }
    private int getCanStoreHeatElementAmount(int i,List<Integer> list,int flag)
    {
        int amount=0;
        //1
        if( i%9!=0 && inventory.getStackInSlot(i-1).hasCapability(DrtechCommonCapabilities.CAPABILITY_COOLANT_CELL,null) &&flag==1)
        {
            amount++;
            list.add(i-1);
        }else if( i%9!=0 && inventory.getStackInSlot(i-1).hasCapability(DrtechCommonCapabilities.CAPABILITY_HEAT_EXCHANGER,null))
        {
            amount++;
            list.add(i-1);
        }else if( i%9!=0 && inventory.getStackInSlot(i-1).hasCapability(DrtechCommonCapabilities.CAPABILITY_HEAT_VENT,null))
        {
            if(inventory.getStackInSlot(i-1).getCapability(DrtechCommonCapabilities.CAPABILITY_HEAT_VENT,null).getMaxHeat()>0)
            {
                amount++;
                list.add(i-1);
            }
        }
        //2
        if((i+1)%9!=0 && inventory.getStackInSlot(i+1).hasCapability(DrtechCommonCapabilities.CAPABILITY_COOLANT_CELL,null) &&flag==1)
        {
            amount++;
            list.add(i+1);
        }else  if((i+1)%9!=0 && inventory.getStackInSlot(i+1).hasCapability(DrtechCommonCapabilities.CAPABILITY_HEAT_EXCHANGER,null))
        {
            amount++;
            list.add(i+1);
        }else  if((i+1)%9!=0 && inventory.getStackInSlot(i+1).hasCapability(DrtechCommonCapabilities.CAPABILITY_HEAT_VENT,null))
        {
            if(inventory.getStackInSlot(i+1).getCapability(DrtechCommonCapabilities.CAPABILITY_HEAT_VENT,null).getMaxHeat()>0)
            {
                amount++;
                list.add(i+1);
            }
        }
        //3
        if(i>9 && inventory.getStackInSlot(i-9).hasCapability(DrtechCommonCapabilities.CAPABILITY_COOLANT_CELL,null) &&flag==1)
        {
            amount++;
            list.add(i-9);
        }else if(i>9 && inventory.getStackInSlot(i-9).hasCapability(DrtechCommonCapabilities.CAPABILITY_HEAT_EXCHANGER,null))
        {
            amount++;
            list.add(i-9);
        }else if(i>9 && inventory.getStackInSlot(i-9).hasCapability(DrtechCommonCapabilities.CAPABILITY_HEAT_VENT,null))
        {
            if(inventory.getStackInSlot(i-9).getCapability(DrtechCommonCapabilities.CAPABILITY_HEAT_VENT,null).getMaxHeat()>0)
            {
                amount++;
                list.add(i-9);
            }
        }
        //4
        if(i<72 && inventory.getStackInSlot(i+9).hasCapability(DrtechCommonCapabilities.CAPABILITY_COOLANT_CELL,null) &&flag==1)
        {
            amount++;
            list.add(i+9);
        }else  if(i<72 && inventory.getStackInSlot(i+9).hasCapability(DrtechCommonCapabilities.CAPABILITY_HEAT_EXCHANGER,null))
        {
            amount++;
            list.add(i+9);
        }else  if(i<72 && inventory.getStackInSlot(i+9).hasCapability(DrtechCommonCapabilities.CAPABILITY_HEAT_VENT,null))
        {
            if(inventory.getStackInSlot(i+9).getCapability(DrtechCommonCapabilities.CAPABILITY_HEAT_VENT,null).getMaxHeat()>0)
            {
                list.add(i+9);
                amount++;
            }
        }
        return amount;
    }
    @Override
    protected @NotNull BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("XXX", "XXX", "XXX", "XXX")
                .aisle("XXX", "X#X", "X#X", "XXX")
                .aisle("XXX", "XSX", "XXX", "XXX")
                .where('S', selfPredicate())
                .where('X', states(getCasingState())
                        .or(abilities(MultiblockAbility.OUTPUT_ENERGY).setMaxGlobalLimited(1).setPreviewCount(1)
                        .or(abilities(MultiblockAbility.OUTPUT_LASER).setMaxGlobalLimited(1).setPreviewCount(1)
                                .or(abilities(MultiblockAbility.MAINTENANCE_HATCH).setExactLimit(1)))
                ))
                .where('#', any())
                .build();
    }
    protected IBlockState getCasingState() {
        return BlocksInit.COMMON_CASING1.getState(MetaCasing1.MetalCasingType.NUCLEAR_PART_CASING);
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return Textures.NUCLEAR_PART_CASING;
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        ModularUI.Builder builder;
        builder = ModularUI.builder(Textures.BACKGROUND, 198, 258);
        for (int i = 0; i < 81; i++) {
            builder.slot(inventory,i,5+i%9*18,3+i/9*18,true,true,GuiTextures.SLOT);
        }
        for (int i = 0; i < 5; i++) {
            builder.slot(upgradeInventory,i,170,3+i%9*18,true,true,GuiTextures.SLOT);
        }
        builder.widget((new AdvancedTextWidget(9, 168, this::addDisplayText, 16777215)).setMaxWidthLimit(181).setClickHandler(this::handleDisplayClick));
        IControllable controllable = (IControllable)this.getCapability(GregtechTileCapabilities.CAPABILITY_CONTROLLABLE, (EnumFacing)null);
        TextureArea var10007;
        BooleanSupplier var10008;
        if (controllable != null) {
            var10007 = GuiTextures.BUTTON_POWER;
            Objects.requireNonNull(controllable);
            var10008 = controllable::isWorkingEnabled;
            Objects.requireNonNull(controllable);
            builder.widget(new ImageCycleButtonWidget(170, 183, 18, 18, var10007, var10008, controllable::setWorkingEnabled));
            builder.widget(new ImageWidget(170, 201, 18, 6, GuiTextures.BUTTON_POWER_DETAIL));
        }

        builder.bindPlayerInventory(entityPlayer.inventory, 180);
        return builder.build(this.getHolder(), entityPlayer);
    }

    @Override
    protected void addDisplayText(List<ITextComponent> textList) {
        super.addDisplayText(textList);
        textList.add(new TextComponentString("发电量："+this.outputEnergy   +"热量:"+this.heat+"/"+this.MaxHeat +"热量:"+(int)(((float)this.heat/(float)this.MaxHeat)*100)+"%"));
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        List<IEnergyContainer> energyContainer = new ArrayList(this.getAbilities(MultiblockAbility.OUTPUT_ENERGY));
        energyContainer.addAll(this.getAbilities(MultiblockAbility.OUTPUT_LASER));
        this.energyContainer=new EnergyContainerList(energyContainer);
    }

    @Override
    public ModularUI getModularUI(EntityPlayer entityPlayer) {
        return super.getModularUI(entityPlayer);
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        this.energyContainer =  new EnergyContainerList(new ArrayList());
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        getFrontOverlay().renderOrientedState(renderState, translation, pipeline, getFrontFacing(), this.isActive(),
                this.isWorkingEnabled());
    }
    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        isActive = data.getBoolean("isActive");
        isWorkingEnabled = data.getBoolean("isWorkingEnabled");
        if (data.hasKey("inventory")) {
            inventory.deserializeNBT(data.getCompoundTag("inventory"));
        }
        if (data.hasKey("inventoryUpgrade")) {
            upgradeInventory.deserializeNBT(data.getCompoundTag("inventoryUpgrade"));
        }
        if (data.hasKey("Heat")) {
          this.heat = data.getInteger("Heat");
        }
    }
    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (capability == GregtechTileCapabilities.CAPABILITY_CONTROLLABLE) {
            return GregtechTileCapabilities.CAPABILITY_CONTROLLABLE.cast(this);
        }else if(capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(this.inventory);
        }else if(capability == DrtechCapabilities.CAPABILITY_NUCLEAR_DATA) {
            return DrtechCapabilities.CAPABILITY_NUCLEAR_DATA.cast(this);
        }
        return super.getCapability(capability, side);
    }

    @Override
    public @NotNull List<ITextComponent> getDataInfo() {
        return new LinkedList<>();
    }
}
