package com.drppp.drtech.common.MetaTileEntities.muti.mutipart;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.value.sync.GuiSyncManager;
import com.drppp.drtech.api.Utils.DrtechUtils;
import com.drppp.drtech.common.MetaTileEntities.muti.electric.store.MetaTileEntityYotTank;
import com.drppp.drtech.api.capability.DrtechCapabilities;
import gregtech.api.GTValues;
import gregtech.api.gui.ModularUI;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.AbilityInstances;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityMultiblockNotifiablePart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;
import java.util.List;

public class MetaTileEntityYotHatch extends MetaTileEntityMultiblockNotifiablePart implements IMultiblockAbilityPart<IFluidTank>{
    private MetaTileEntityYotTank yotTank;
    public YotTankHatch tankHatch;
    public MetaTileEntityYotHatch(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, GTValues.LV, false);

    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityYotHatch(this.metaTileEntityId);
    }

    @Override
    public ICubeRenderer getBaseTexture() {
        return com.drppp.drtech.Client.Textures.YOT_TANK_CASING;
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        return null;
    }

    @Override
    public void update() {
        super.update();
        if(!getWorld().isRemote)
        {
            if(this.getController()==null)
            {
                this.yotTank=null;
                if(this.tankHatch!=null)
                    this.tankHatch.drain(this.tankHatch.getFluid(),true);
            }
            else
            {
                if(this.yotTank.getFluid()==null )
                    this.tankHatch.drain(this.tankHatch.getFluid(),true);
            }

        }
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        if (this.shouldRenderOverlay()) {
            Textures.ME_INPUT_BUS.renderSided(getFrontFacing(), renderState, translation, pipeline);
        }
    }

    public void setYotTank(MetaTileEntityYotTank yotTank) {
        this.yotTank = yotTank;
        this.tankHatch = new YotTankHatch(Integer.MAX_VALUE,this.yotTank);
    }

    @Override
    public MultiblockAbility<IFluidTank> getAbility() {
        return DrtechCapabilities.YOT_HATCH;
    }

    @Override
    public void registerAbilities(@NotNull AbilityInstances abilityInstances) {
        if(tankHatch!=null)
            abilityInstances.add(tankHatch);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if(capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(this.tankHatch);
        return super.getCapability(capability, side);
    }

    @Override
    public ModularPanel buildUI(PosGuiData posGuiData, GuiSyncManager guiSyncManager) {
        return null;
    }

    private class YotTankHatch extends FluidTank {
        private final MetaTileEntityYotTank yotTank;
        public YotTankHatch(int capacity,MetaTileEntityYotTank yotTank) {
            super(capacity);
            this.yotTank = yotTank;
            if(this.yotTank!=null && this.yotTank.getFluid()!=null)
            {
                if(this.yotTank.getFluidBank().getStored().compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) > 0)
                    this.fluid = new FluidStack(this.yotTank.getFluid(),Integer.MAX_VALUE);
                else
                    this.fluid = new FluidStack(this.yotTank.getFluid(),this.yotTank.getFluidBank().getStored().intValue());
            }
        }
        @Override
        public FluidStack drain(FluidStack resource, boolean doDrain) {
            if(this.yotTank==null || this.fluid==null || !resource.isFluidEqual(this.fluid))
                return null;
            if(this.yotTank.getFluid()==null)
                return null;
            if(this.yotTank.getFluidBank().getStored().longValue()<=0)
                return null;
            int drain = 0;
            if(doDrain)
            {
                if(resource.amount>=0)
                    drain = (int) this.yotTank.getFluidBank().drain(resource.amount);
                else
                    drain=0;
            }
            else drain = super.drain(resource,false)==null?0:super.drain(resource,false).amount;
            if(this.yotTank.getFluidBank().getStored().compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) > 0)
                this.fluid = new FluidStack(this.yotTank.getFluid(),Integer.MAX_VALUE);
            else
                this.fluid = new FluidStack(this.yotTank.getFluid(),this.yotTank.getFluidBank().getStored().intValue());
            return  new FluidStack(this.yotTank.getFluid(),drain);
        }

        @Override
        public FluidStack drain(int maxDrain, boolean doDrain) {
            if(this.yotTank==null || maxDrain<=0)
                return null;
            if(this.yotTank.getFluid()==null)
                return null;
            if(this.yotTank.getFluidBank().getStored().longValue()<=0)
                return null;
            if(doDrain)
            {
                this.yotTank.getFluidBank().drain(maxDrain);
                if(this.yotTank.getFluidBank().getStored().intValue()==0)
                    this.yotTank.setFluid(null);
            }
            if(this.yotTank.getFluidBank().getStored().compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) > 0)
                this.fluid = new FluidStack(this.yotTank.getFluid(),Integer.MAX_VALUE);
            else
                this.fluid = new FluidStack(this.yotTank.getFluid(),this.yotTank.getFluidBank().getStored().intValue());
            return super.drain(maxDrain, doDrain);
        }

        @Override
        public boolean canFillFluidType(FluidStack fluid) {
            return super.canFillFluidType(fluid);
        }

        @Override
        public int fill(FluidStack resource, boolean doFill) {
            if(this.yotTank==null)
                return 0;
            int fillamount =0;
            if(this.yotTank.getFluid()==null)
                this.yotTank.setFluid(resource);
            if(doFill)
            {
                fillamount = (int) this.yotTank.getFluidBank().fill(resource.amount);
            }
            else
            {
                fillamount = super.fill(resource,false);
                if(this.fluid.amount==Integer.MAX_VALUE)
                {
                   var cap =  this.yotTank.getFluidBank().getCapacity();
                   var store = this.yotTank.getFluidBank().getStored();
                   if(cap.subtract(store).compareTo(BigInteger.valueOf(resource.amount))>0)
                   {
                       fillamount = resource.amount;
                   }
                }
            }
            if(this.yotTank.getFluidBank().getStored().compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) > 0)
                this.fluid = new FluidStack(this.yotTank.getFluid(),Integer.MAX_VALUE);
            else
                this.fluid = new FluidStack(this.yotTank.getFluid(),this.yotTank.getFluidBank().getStored().intValue());
            return fillamount;
        }

    }
}
