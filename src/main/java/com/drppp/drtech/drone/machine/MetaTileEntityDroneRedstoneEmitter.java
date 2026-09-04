package com.drppp.drtech.drone.machine;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.ColourMultiplier;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.GTValues;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.texture.Textures;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.ArrayUtils;

/**
 * A programmable six-sided redstone endpoint for deployed drones.
 *
 * <p>The drone pays the EU action cost. This endpoint only stores and exposes the requested vanilla redstone
 * strength; it intentionally does not accept EU cables.</p>
 */
public final class MetaTileEntityDroneRedstoneEmitter extends MetaTileEntity {

    private static final String NBT_OUTPUT_STRENGTH = "OutputStrength";
    private int outputStrength;

    public MetaTileEntityDroneRedstoneEmitter(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityDroneRedstoneEmitter(metaTileEntityId);
    }

    @Override
    protected boolean canMachineConnectRedstone(EnumFacing side) {
        return true;
    }

    public int getOutputStrength() {
        return outputStrength;
    }

    /** Sets all six output faces to the same vanilla redstone strength. Server side only in normal use. */
    public void setOutputStrength(int strength) {
        int bounded = clampStrength(strength);
        if (outputStrength == bounded && getHighestOutputRedstoneSignal() == bounded) return;
        outputStrength = bounded;
        applyOutputSignals();
        markDirty();
        notifyRedstoneNeighbors();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        applyOutputSignals();
        notifyRedstoneNeighbors();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setInteger(NBT_OUTPUT_STRENGTH, outputStrength);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        outputStrength = clampStrength(data.getInteger(NBT_OUTPUT_STRENGTH));
        applyOutputSignals();
    }

    private void applyOutputSignals() {
        for (EnumFacing side : EnumFacing.values()) setOutputRedstoneSignal(side, outputStrength);
    }

    private void notifyRedstoneNeighbors() {
        if (getWorld() == null || getWorld().isRemote || getPos() == null) return;
        getWorld().notifyNeighborsOfStateChange(getPos(), getWorld().getBlockState(getPos()).getBlock(), false);
        notifyBlockUpdate();
    }

    private static int clampStrength(int strength) {
        return Math.max(0, Math.min(15, strength));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        IVertexOperation[] colouredPipeline = ArrayUtils.add(pipeline,
                new ColourMultiplier(GTUtility.convertRGBtoOpaqueRGBA_CL(getPaintingColorForRendering())));
        Textures.VOLTAGE_CASINGS[GTValues.HV].render(renderState, translation, colouredPipeline);
        for (EnumFacing side : EnumFacing.values()) {
            Textures.INFINITE_EMITTER_FACE.renderSided(side, renderState, translation, pipeline);
        }
    }
}
