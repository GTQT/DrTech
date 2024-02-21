package com.drppp.drtech.MetaTileEntities.muti.ecectric.store;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.drppp.drtech.Blocks.BlocksInit;
import com.drppp.drtech.Blocks.MetaBlocks.MetaCasing;
import com.drppp.drtech.MetaTileEntities.muti.mutipart.MetaTileEntityYotHatch;
import com.drppp.drtech.Tile.TileEntityConnector;
import com.drppp.drtech.api.capability.DrtechCapabilities;
import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IControllable;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.impl.EnergyContainerList;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockWithDisplayBase;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.unification.material.Materials;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static gregtech.api.util.RelativeDirection.*;

public class MetaTileEntityEnergyTransTower extends MultiblockWithDisplayBase implements IControllable {
    private boolean isActive=true, isWorkingEnabled = true;
    private IEnergyContainer inenergyContainer;
    private IEnergyContainer outenergyContainer;
    private TileEntityConnector connector;
    public MetaTileEntityEnergyTransTower(ResourceLocation metaTileEntityId) {
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
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (capability == GregtechTileCapabilities.CAPABILITY_CONTROLLABLE) {
            return GregtechTileCapabilities.CAPABILITY_CONTROLLABLE.cast(this);
        }
        return super.getCapability(capability, side);
    }
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        this.getFrontOverlay().renderOrientedState(renderState, translation, pipeline, this.getFrontFacing(), this.isActive(), this.isWorkingEnabled());
    }
    @Override
    protected void updateFormedValid() {
        if(this.connector !=null && this.inenergyContainer!= null)
        {
            this.inenergyContainer.changeEnergy(-this.connector.fill(this.inenergyContainer.getEnergyStored()));
        }
        if(this.connector !=null && this.outenergyContainer!= null)
        {
            this.outenergyContainer.changeEnergy(this.connector.drain(this.outenergyContainer.getEnergyCapacity() -this.outenergyContainer.getEnergyStored()));
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        return super.writeToNBT(data);
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        resetTileAbilities();
        this.connector.success = 0;
    }
    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        initializeAbilities();
        getConnectorPos();
    }
    private void getConnectorPos()
    {
        BlockPos pos = null;
        switch (this.getFrontFacing()){
            case SOUTH:
                pos = new BlockPos(this.getPos().getX(),this.getPos().getY()+10,this.getPos().getZ()-1);
                break;
            case NORTH:
                pos = new BlockPos(this.getPos().getX(),this.getPos().getY()+10,this.getPos().getZ()+1);
                break;
            case EAST:
                pos = new BlockPos(this.getPos().getX()-1,this.getPos().getY()+10,this.getPos().getZ());
                break;
            case WEST:
                pos = new BlockPos(this.getPos().getX()+1,this.getPos().getY()+10,this.getPos().getZ());
                break;
        }
        if(pos!= null && this.getWorld().getTileEntity(pos)!= null && this.getWorld().getTileEntity(pos) instanceof TileEntityConnector)
        {
            this.connector = (TileEntityConnector) this.getWorld().getTileEntity(pos);
            this.connector.success = 1;
        }
    }
    private void initializeAbilities() {
        this.inenergyContainer = new EnergyContainerList(getAbilities(MultiblockAbility.INPUT_ENERGY));
        this.outenergyContainer =new EnergyContainerList(getAbilities(MultiblockAbility.OUTPUT_ENERGY));
    }
    private void resetTileAbilities() {
        this.inenergyContainer = new EnergyContainerList(new ArrayList());
        this.outenergyContainer = new EnergyContainerList(new ArrayList());
    }
    @Override
    protected @NotNull BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start(RIGHT, FRONT, UP)
                .aisle("CSC", "CCC", "CCC")
                .aisle("###", "GLG", "#G#").setRepeatable(6, 6)
                .aisle("#C#", "CCC", "#C#")
                .aisle("#G#", "GCG", "#G#")
                .aisle("#G#", "GCG", "#G#")
                .aisle("###", "#D#", "###")
                .where('S', selfPredicate())
                .where('#', any())
                .where('C', states(getCasingState())
                        .or(abilities(MultiblockAbility.MAINTENANCE_HATCH).setExactLimit(1))
                        .or(abilities(MultiblockAbility.INPUT_ENERGY).setMaxGlobalLimited(2).setPreviewCount(1))
                        .or(abilities(MultiblockAbility.OUTPUT_ENERGY).setMaxGlobalLimited(2).setPreviewCount(1))
                )
                .where('G', blocks(Blocks.IRON_BARS))
                .where('L', frames(Materials.Steel))
                .where('D', blocks(BlocksInit.BLOCK_CONNECTOR1,BlocksInit.BLOCK_CONNECTOR2,BlocksInit.BLOCK_CONNECTOR3))
                .build();
    }
    private static IBlockState getCasingState() {
        return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STEEL_SOLID);
    }
    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return Textures.SOLID_STEEL_CASING;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityEnergyTransTower(this.metaTileEntityId);
    }
    @Override
    public void addInformation(ItemStack stack, @Nullable World world, @NotNull List<String> tooltip,
                               boolean advanced) {
        tooltip.add(I18n.format("drtech.machine.energytrans.tooltip.1"));
        tooltip.add(I18n.format("drtech.machine.energytrans.tooltip.2"));
    }
}
