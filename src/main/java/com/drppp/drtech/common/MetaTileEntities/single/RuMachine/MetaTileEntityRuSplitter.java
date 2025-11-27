package com.drppp.drtech.common.MetaTileEntities.single.RuMachine;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.ColourMultiplier;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;

import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.drppp.drtech.Client.Textures;
import com.drppp.drtech.api.capability.DrtechCommonCapabilities;
import com.drppp.drtech.api.capability.IRotationEnergy;
import com.drppp.drtech.api.capability.IRotationSpeed;
import com.drppp.drtech.api.capability.impl.RotationEnergyHandler;
import com.drppp.drtech.common.MetaTileEntities.single.MetaTileEntityRuGenerator;
import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.gui.ModularUI;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.cube.SimpleSidedCubeRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class MetaTileEntityRuSplitter extends MetaTileEntity implements IRotationSpeed {
    public boolean isActive;
    protected final ICubeRenderer rendererBASE = Textures.RU_SPLITTER;
    IRotationEnergy ru = new RotationEnergyHandler();
    IRotationEnergy ru_count = new RotationEnergyHandler();
    private int speed=0;
    public MetaTileEntityRuSplitter(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityRuSplitter(this.metaTileEntityId);
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        return null;
    }
    @SideOnly(Side.CLIENT)
    protected SimpleSidedCubeRenderer getBaseRenderer() {
        return Textures.MACHINE_CASINGS[0];
    }
    @SideOnly(Side.CLIENT)
    public Pair<TextureAtlasSprite, Integer> getParticleTexture() {
        return Pair.of(getBaseRenderer().getParticleSprite(),this.getPaintingColorForRendering());
    }
    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        IVertexOperation[] colouredPipeline = ArrayUtils.add(pipeline, new ColourMultiplier(GTUtility.convertRGBtoOpaqueRGBA_CL(this.getPaintingColorForRendering())));
        this.getBaseRenderer().render(renderState, translation, colouredPipeline);
        this.rendererBASE.renderOrientedState(renderState, translation, pipeline, this.getFrontFacing(), isActive, isActive);
    }
    @Override
    public void update() {
        super.update();
        if(!getWorld().isRemote)
        {
            var tile = getWorld().getTileEntity(getPos().offset(getFrontFacing()));
            if(tile!=null && tile.hasCapability(DrtechCommonCapabilities.CAPABILITY_ROTATION_ENERGY,getFrontFacing().getOpposite()))
            {
                IRotationEnergy get = tile.getCapability(DrtechCommonCapabilities.CAPABILITY_ROTATION_ENERGY,getFrontFacing().getOpposite());
                this.ru.setRuEnergy(get.getEnergyOutput());
                if(tile instanceof IRotationSpeed)
                {
                    IRotationSpeed sp = (IRotationSpeed) tile;
                    this.speed = sp.getSpeed();
                }
                setActive(true);
            }else
            {
                this.ru.setRuEnergy(0);
                setActive(false);
            }
            int count=0;
            List<EnumFacing> list = new ArrayList<>();
            for (var facing: EnumFacing.VALUES)
            {
                if(facing==getFrontFacing())
                    continue;
                var tl = getWorld().getTileEntity(getPos().offset(facing));
                if(tl!=null && tl.hasCapability(DrtechCommonCapabilities.CAPABILITY_ROTATION_ENERGY,facing))
                {
                    list.add(facing);
                    count++;
                }
                var mte = GTUtility.getMetaTileEntity(getWorld(),getPos().offset(facing));
                if(mte!=null && mte instanceof MetaTileEntityRuGenerator)
                {
                    list.add(facing);
                    count++;
                }
            }
            if(count!=0)
            {
                ru_count.setRuEnergy(ru.getEnergyOutput()/count);
                this.setSpeed(Math.max(this.ru.getEnergyOutput()==0?0:1,getSpeed()/count));
            }
            else
                ru_count.setRuEnergy(0);
        }
    }

    @Override
    public boolean hasCapability(@NotNull Capability<?> capability, @Nullable EnumFacing facing) {
        if(capability==DrtechCommonCapabilities.CAPABILITY_ROTATION_ENERGY && facing!=getFrontFacing())
            return true;
        return super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if(capability==DrtechCommonCapabilities.CAPABILITY_ROTATION_ENERGY && side!=getFrontFacing())
            return DrtechCommonCapabilities.CAPABILITY_ROTATION_ENERGY.cast(ru_count);
        return super.getCapability(capability, side);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setBoolean("isActive", isActive);
        data.setInteger("speed", speed);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        isActive = data.getBoolean("isActive");
        speed = data.getInteger("speed");
    }
    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeBoolean(isActive);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        isActive = buf.readBoolean();
    }

    @Override
    public void receiveCustomData(int dataId, @NotNull PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == GregtechDataCodes.WORKABLE_ACTIVE) {
            isActive = buf.readBoolean();
            scheduleRenderUpdate();
        }
    }
    protected void setActive(boolean active) {
        if (this.isActive != active) {
            this.isActive = active;
            this.markDirty();
            if (getWorld() != null && !getWorld().isRemote) {
                this.writeCustomData(GregtechDataCodes.WORKABLE_ACTIVE, (buf) -> {
                    buf.writeBoolean(active);
                });
            }
        }
    }

    @Override
    public int getSpeed() {
        return this.speed;
    }

    @Override
    public void setSpeed(int speed) {
        this.speed = speed;
    }

    @Override
    public IRotationEnergy getEnergy() {
        return this.ru;
    }

    @Override
    public EnumFacing getFacing() {
        return getFrontFacing();
    }


    @Override
    public ModularPanel buildUI(PosGuiData data, PanelSyncManager syncManager, UISettings settings) {
        return null;
    }
}
