package com.drppp.drtech.common.metaTileEntities.muti.mutipart;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;

import com.drppp.drtech.common.metaTileEntities.muti.electric.store.MetaTileEntityYotTank;
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

public class MetaTileEntityYotHatch extends MetaTileEntityMultiblockNotifiablePart implements IMultiblockAbilityPart<IFluidTank> {

    private static final BigInteger MAX_INT = BigInteger.valueOf(Integer.MAX_VALUE);

    private MetaTileEntityYotTank yotTank;
    public YotTankHatch tankHatch;

    public MetaTileEntityYotHatch(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, GTValues.LV, false);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityYotHatch(metaTileEntityId);
    }

    @Override
    public ICubeRenderer getBaseTexture() {
        return com.drppp.drtech.client.Textures.YOT_TANK_CASING;
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        return null;
    }

    @Override
    public void update() {
        super.update();
        if (getWorld().isRemote) {
            return;
        }
        if (getController() == null) {
            yotTank = null;
        }
        if (tankHatch != null && (getController() == null || yotTank == null || yotTank.getFluid() == null)) {
            tankHatch.drain(tankHatch.getFluid(), true);
        }
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        if (shouldRenderOverlay()) {
            Textures.ME_INPUT_BUS.renderSided(getFrontFacing(), renderState, translation, pipeline);
        }
    }

    public void setYotTank(MetaTileEntityYotTank yotTank) {
        this.yotTank = yotTank;
        tankHatch = new YotTankHatch(Integer.MAX_VALUE, yotTank);
    }

    @Override
    public MultiblockAbility<IFluidTank> getAbility() {
        return DrtechCapabilities.YOT_HATCH;
    }

    @Override
    public void registerAbilities(@NotNull AbilityInstances abilityInstances) {
        if (tankHatch != null) {
            abilityInstances.add(tankHatch);
        }
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && tankHatch != null) {
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(tankHatch);
        }
        return super.getCapability(capability, side);
    }

    private static class YotTankHatch extends FluidTank {

        private final MetaTileEntityYotTank yotTank;

        public YotTankHatch(int capacity, MetaTileEntityYotTank yotTank) {
            super(capacity);
            this.yotTank = yotTank;
            syncFluid();
        }

        private void syncFluid() {
            if (yotTank == null || yotTank.getFluid() == null) {
                fluid = null;
                return;
            }
            BigInteger stored = yotTank.getFluidBank().getStored();
            fluid = new FluidStack(yotTank.getFluid(), stored.compareTo(MAX_INT) > 0 ? Integer.MAX_VALUE : stored.intValue());
        }

        @Override
        public FluidStack drain(FluidStack resource, boolean doDrain) {
            if (resource == null || yotTank == null || fluid == null || !resource.isFluidEqual(fluid)) {
                return null;
            }
            if (yotTank.getFluid() == null || yotTank.getFluidBank().getStored().signum() <= 0) {
                return null;
            }
            int drain = 0;
            if (doDrain) {
                if (resource.amount >= 0) {
                    drain = (int) yotTank.getFluidBank().drain(resource.amount);
                }
            } else {
                FluidStack simulated = super.drain(resource, false);
                drain = simulated == null ? 0 : simulated.amount;
            }
            FluidStack resultFluid = yotTank.getFluid();
            syncFluid();
            return resultFluid == null ? null : new FluidStack(resultFluid, drain);
        }

        @Override
        public FluidStack drain(int maxDrain, boolean doDrain) {
            if (yotTank == null || maxDrain <= 0 || yotTank.getFluid() == null || yotTank.getFluidBank().getStored().signum() <= 0) {
                return null;
            }
            FluidStack resultFluid = yotTank.getFluid();
            int drain = 0;
            if (doDrain) {
                drain = (int) yotTank.getFluidBank().drain(maxDrain);
                if (yotTank.getFluidBank().getStored().signum() == 0) {
                    yotTank.setFluid(null);
                }
            } else {
                FluidStack simulated = super.drain(maxDrain, false);
                drain = simulated == null ? 0 : simulated.amount;
            }
            syncFluid();
            return resultFluid == null ? null : new FluidStack(resultFluid, drain);
        }

        @Override
        public int fill(FluidStack resource, boolean doFill) {
            if (yotTank == null || resource == null) {
                return 0;
            }
            if (yotTank.getFluid() == null) {
                yotTank.setFluid(resource);
                syncFluid();
            }
            int fillAmount;
            if (doFill) {
                fillAmount = (int) yotTank.getFluidBank().fill(resource.amount);
            } else {
                fillAmount = super.fill(resource, false);
                if (fluid != null && fluid.amount == Integer.MAX_VALUE) {
                    BigInteger capacity = yotTank.getFluidBank().getCapacity();
                    BigInteger stored = yotTank.getFluidBank().getStored();
                    if (capacity.subtract(stored).compareTo(BigInteger.valueOf(resource.amount)) > 0) {
                        fillAmount = resource.amount;
                    }
                }
            }
            syncFluid();
            return fillAmount;
        }
    }
}