package com.drppp.drtech.MetaTileEntities.muti.ecectric.standard;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.drppp.drtech.Blocks.BlocksInit;
import com.drppp.drtech.Blocks.MetaBlocks.MetaCasing1;
import com.drppp.drtech.Client.Textures;
import com.drppp.drtech.MetaTileEntities.muti.ecectric.store.MetaTileEntityYotTank;
import forestry.api.apiculture.BeeManager;
import forestry.api.apiculture.EnumBeeType;
import forestry.api.apiculture.IBee;
import forestry.apiculture.genetics.Bee;
import gregtech.api.block.IHeatingCoilBlockStats;
import gregtech.api.capability.*;
import gregtech.api.capability.impl.EnergyContainerList;
import gregtech.api.capability.impl.ItemHandlerList;
import gregtech.api.metatileentity.IDataInfoProvider;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockWithDisplayBase;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.util.GTTransferUtils;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.ICubeRenderer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class MetaTileEntutyLargeBeeHive extends MultiblockWithDisplayBase implements IDataInfoProvider, IWorkable, IControllable {
    private boolean isActive, isWorkingEnabled = true;
    protected IEnergyContainer energyContainer = new EnergyContainerList(new ArrayList());
    protected ItemHandlerList itemImportInventory;
    protected ItemHandlerList itemExportInventory;
    public int process=0;
    public int maxProcess = 100;
    public MetaTileEntutyLargeBeeHive(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
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
    public @NotNull List<ITextComponent> getDataInfo() {
        return null;
    }


    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        this.energyContainer = new EnergyContainerList(new ArrayList());
        this.itemImportInventory =  new ItemHandlerList(Collections.emptyList());
        this.itemExportInventory =  new ItemHandlerList(Collections.emptyList());
    }
    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        List<IEnergyContainer> energyContainer = new ArrayList(this.getAbilities(MultiblockAbility.OUTPUT_ENERGY));
        //energyContainer.addAll(this.getAbilities(MultiblockAbility.OUTPUT_LASER));
        this.energyContainer=new EnergyContainerList(energyContainer);
        this.itemImportInventory = new ItemHandlerList(getAbilities(MultiblockAbility.IMPORT_ITEMS));
        this.itemExportInventory = new ItemHandlerList(getAbilities(MultiblockAbility.EXPORT_ITEMS));

    }
    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntutyLargeBeeHive(this.metaTileEntityId);
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
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        isActive = data.getBoolean("isActive");
        isWorkingEnabled = data.getBoolean("isWorkingEnabled");

    }
    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (capability == GregtechTileCapabilities.CAPABILITY_CONTROLLABLE) {
            return GregtechTileCapabilities.CAPABILITY_CONTROLLABLE.cast(this);
        }
        return super.getCapability(capability, side);
    }
    @SideOnly(Side.CLIENT)
    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        getFrontOverlay().renderOrientedState(renderState, translation, pipeline, getFrontFacing(), this.isActive(),
                this.isWorkingEnabled());
    }
    protected IBlockState getCasingState() {
        return BlocksInit.COMMON_CASING1.getState(MetaCasing1.MetalCasingType.NUCLEAR_PART_CASING);
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return Textures.NUCLEAR_PART_CASING;
    }
    @Override
    protected @NotNull BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("XXX", "XXX", "XXX", "XXX")
                .aisle("XXX", "X#X", "X#X", "XXX")
                .aisle("XXX", "X#X", "X#X", "XXX")
                .aisle("XXX", "XSX", "XXX", "XXX")
                .where('S', selfPredicate())
                .where('X', states(getCasingState())
                        .or(abilities(MultiblockAbility.INPUT_ENERGY).setMaxGlobalLimited(1).setPreviewCount(1)
                                .or(abilities(MultiblockAbility.IMPORT_ITEMS).setMaxGlobalLimited(1).setPreviewCount(1)
                                .or(abilities(MultiblockAbility.EXPORT_ITEMS).setMaxGlobalLimited(1).setPreviewCount(1)
                                        .or(abilities(MultiblockAbility.MAINTENANCE_HATCH).setExactLimit(1)))
                        )))
                .where('#', any())
                .build();
    }

    @Override
    protected void updateFormedValid() {
        if(process++>=maxProcess)
        {
            process=0;
            for (int i = 0; i < itemImportInventory.getSlots(); i++) {
                ItemStack is = itemImportInventory.getStackInSlot(i);
                EnumBeeType beeType = BeeManager.beeRoot.getType(is);
                if(beeType == EnumBeeType.QUEEN)
                {
                    IBee bee = BeeManager.beeRoot.getMember(is);

                    GTTransferUtils.addItemsToItemHandler(this.itemExportInventory, false, bee.getProduceList());
                    GTTransferUtils.addItemsToItemHandler(this.itemExportInventory, false, bee.getSpecialtyList());
                    bee.getGeneration();
                }
            }
        }
    }

}
