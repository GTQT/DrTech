package com.drppp.drtech.common.MetaTileEntities.muti.electric.standard;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.google.common.collect.Lists;
import gregtech.api.capability.*;
import gregtech.api.capability.impl.EnergyContainerList;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.capability.impl.ItemHandlerList;
import gregtech.api.items.itemhandlers.GTItemStackHandler;
import gregtech.api.metatileentity.IDataInfoProvider;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockWithDisplayBase;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.client.renderer.ICubeRenderer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class MetaTileEntityBaseWithControl extends MultiblockWithDisplayBase implements IControllable, IDataInfoProvider, IWorkable {
    public int process;
    public int maxProcess;
    protected IItemHandlerModifiable inputInventory;
    protected IItemHandlerModifiable outputInventory;
    protected IMultipleTankHandler inputFluidInventory;
    protected IMultipleTankHandler outputFluidInventory;
    protected IEnergyContainer energyContainer;
    protected IEnergyContainer outEnergyContainer;
    private boolean isActive = false;
    private boolean isWorkingEnabled = true;

    public MetaTileEntityBaseWithControl(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    public IEnergyContainer getEnergyContainer() {
        return this.energyContainer;
    }

    public IEnergyContainer getOutEnergyContainer() {
        return this.outEnergyContainer;
    }

    public IItemHandlerModifiable getInputInventory() {
        return this.inputInventory;
    }

    public IItemHandlerModifiable getOutputInventory() {
        return this.outputInventory;
    }

    public IMultipleTankHandler getInputFluidInventory() {
        return this.inputFluidInventory;
    }

    public IMultipleTankHandler getOutputFluidInventory() {
        return this.outputFluidInventory;
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

    public boolean isActive() {
        return this.isActive && isStructureFormed();
    }

    public void setActive(boolean active) {
        if (this.isActive != active) {
            this.isActive = active;
            World world = this.getWorld();
            if (world != null && !world.isRemote) {
                this.writeCustomData(GregtechDataCodes.WORKABLE_ACTIVE, buf -> buf.writeBoolean(active));
            }
        }
    }

    @Override
    protected void addDisplayText(List<ITextComponent> textList) {
        super.addDisplayText(textList);
    }

    @Override
    protected void updateFormedValid() {

    }

    @Override
    protected @NotNull BlockPattern createStructurePattern() {
        return null;
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return null;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return null;
    }

    @Override
    public @NotNull List<ITextComponent> getDataInfo() {
        return null;
    }

    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        this.initializeAbilities();
    }

    public void invalidateStructure() {
        super.invalidateStructure();
        this.resetTileAbilities();
    }
    @Override
    public boolean usesMui2() {
        return false;
    }
    protected void initializeAbilities() {
        var im_item = this.getAbilities(MultiblockAbility.IMPORT_ITEMS);
        List<IItemHandlerModifiable> itemlist = new ArrayList<>();
        itemlist.addAll(im_item);
        this.inputInventory = new ItemHandlerList(itemlist);
        var im_fluid = this.getAbilities(MultiblockAbility.IMPORT_FLUIDS);
        List<IFluidTank> tanks = new ArrayList<>();
        tanks.addAll(im_fluid);
        this.inputFluidInventory = new FluidTankList(this.allowSameFluidFillForOutputs(), tanks);

        var ex_item = this.getAbilities(MultiblockAbility.EXPORT_ITEMS);
        List<IItemHandlerModifiable> ex_itemlist = new ArrayList<>();
        ex_itemlist.addAll(ex_item);
        this.outputInventory = new ItemHandlerList(ex_itemlist);
        var ex_fluid = this.getAbilities(MultiblockAbility.EXPORT_FLUIDS);
        List<IFluidTank> extanks = new ArrayList<>();
        extanks.addAll(ex_fluid);
        this.outputFluidInventory = new FluidTankList(this.allowSameFluidFillForOutputs(), extanks);
        this.energyContainer = new EnergyContainerList(this.getAbilities(MultiblockAbility.INPUT_ENERGY));
        this.outEnergyContainer = new EnergyContainerList(this.getAbilities(MultiblockAbility.OUTPUT_ENERGY));
    }

    private void resetTileAbilities() {
        this.inputInventory = new GTItemStackHandler(this, 0);
        this.inputFluidInventory = new FluidTankList(true);
        this.outputInventory = new GTItemStackHandler(this, 0);
        this.outputFluidInventory = new FluidTankList(true);
        this.energyContainer = new EnergyContainerList(Lists.newArrayList());
        this.outEnergyContainer = new EnergyContainerList(Lists.newArrayList());
    }

    protected boolean allowSameFluidFillForOutputs() {
        return true;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        this.getFrontOverlay().renderOrientedState(renderState, translation, pipeline, getFrontFacing(), isActive(),
                isWorkingEnabled());
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setBoolean("isActive", isActive);
        data.setBoolean("isWorkingEnabled", isWorkingEnabled);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        isActive = data.getBoolean("isActive");
        isWorkingEnabled = data.getBoolean("isWorkingEnabled");
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
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (capability == GregtechTileCapabilities.CAPABILITY_CONTROLLABLE) {
            return GregtechTileCapabilities.CAPABILITY_CONTROLLABLE.cast(this);
        } else if (capability == GregtechTileCapabilities.CAPABILITY_WORKABLE) {
            return GregtechTileCapabilities.CAPABILITY_WORKABLE.cast(this);
        } else if (capability == GregtechTileCapabilities.CAPABILITY_COVER_HOLDER) {
            return GregtechTileCapabilities.CAPABILITY_COVER_HOLDER.cast(this);
        }
        return super.getCapability(capability, side);
    }

    public boolean drainEnergy(long energy) {
        if (this.energyContainer == null)
            return false;
        if (this.energyContainer.getEnergyStored() < energy)
            return false;
        this.energyContainer.changeEnergy(-energy);
        return true;
    }
    public boolean addEnergy(long energy) {
        if (this.outEnergyContainer == null)
            return false;
        this.outEnergyContainer.addEnergy(energy);
        return true;
    }

    @Override
    public ModularPanel buildUI(PosGuiData data, PanelSyncManager syncManager, UISettings settings) {
        return null;
    }
}
