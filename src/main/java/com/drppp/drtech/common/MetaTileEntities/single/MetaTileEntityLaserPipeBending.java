package com.drppp.drtech.common.MetaTileEntities.single;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.ColourMultiplier;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;

import gregtech.api.GTValues;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.impl.LaserContainerHandler;
import gregtech.api.gui.ModularUI;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.renderer.texture.cube.SimpleSidedCubeRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;


public class MetaTileEntityLaserPipeBending extends MetaTileEntity {

    protected LaserContainerHandler emtLaserContainer;
    protected LaserContainerHandler recLaserContainer;
    int tire;
    int amp;
    public MetaTileEntityLaserPipeBending(ResourceLocation metaTileEntityId,int tire,int amp) {
        super(metaTileEntityId);
        emtLaserContainer = LaserContainerHandler.emitterContainer(this, GTValues.V[tire]*64l*amp,GTValues.V[tire],amp);
        recLaserContainer = LaserContainerHandler.receiverContainer(this, GTValues.V[tire]*64l*amp,GTValues.V[tire],amp);
        this.tire = tire;
        this.amp = amp;
    }

    @Override
    public void update() {
        super.update();
        if(!getWorld().isRemote)
        {
            long can_input =emtLaserContainer.getEnergyCapacity() -  emtLaserContainer.getEnergyStored();
            if(can_input>0)
            {
                long store = recLaserContainer.getEnergyStored();
                long last = Math.min(can_input,store);
                emtLaserContainer.addEnergy(last);
                recLaserContainer.removeEnergy(last);
            }
            emtLaserContainer.update();
        }

    }
    public boolean isValidFrontFacing(EnumFacing facing) {
        if (this.hasFrontFacing() && this.getFrontFacing() == facing) {
            return false;
        }
        return true;
    }
    @Override
    public void addInformation(ItemStack stack, @Nullable World world, @NotNull List<String> tooltip, boolean advanced) {
        super.addInformation(stack, world, tooltip, advanced);
        tooltip.add("正面输入能量！");
        tooltip.add("其他方向输出能量");
        tooltip.add("最多同时往一个方向传输能量");
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        IVertexOperation[] colouredPipeline =  ArrayUtils.add(pipeline, new ColourMultiplier(GTUtility.convertRGBtoOpaqueRGBA_CL(this.getPaintingColorForRendering())));
        this.getBaseRenderer().render(renderState, translation, colouredPipeline);
        Textures.LASER_SOURCE.renderSided(getFrontFacing(), renderState, translation, pipeline);
    }
    @SideOnly(Side.CLIENT)
    protected SimpleSidedCubeRenderer getBaseRenderer() {
        return gregtech.client.renderer.texture.Textures.VOLTAGE_CASINGS[tire];
    }
    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityLaserPipeBending(this.metaTileEntityId,this.tire,this.amp);
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        return null;
    }

    @Override
    public boolean hasCapability(@NotNull Capability<?> capability, @Nullable EnumFacing facing) {
        return super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if(capability == GregtechTileCapabilities.CAPABILITY_LASER && side==getFrontFacing())
        {
            return GregtechTileCapabilities.CAPABILITY_LASER.cast(recLaserContainer);
        }else if(capability == GregtechTileCapabilities.CAPABILITY_LASER && side!=getFrontFacing())
        {
            return GregtechTileCapabilities.CAPABILITY_LASER.cast(emtLaserContainer);
        }
        else
            return super.getCapability(capability, side);
    }

    
}
