package com.drppp.drtech.common.MetaTileEntities.single;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.cleanroommc.modularui.factory.GuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.value.sync.GuiSyncManager;
import com.drppp.drtech.api.modularui.MetaTileEntityModularui;
import com.drppp.drtech.api.modularui.DrtMetaTileEntityGuiFactory;
import gregtech.api.GTValues;
import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IDataStickIntractable;
import gregtech.api.capability.IWorkable;
import gregtech.api.cover.Cover;
import gregtech.api.cover.CoverRayTracer;
import gregtech.api.gui.ModularUI;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.common.items.MetaItems;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import org.jetbrains.annotations.NotNull;


public class MetaTileEntityIndustrialApiary extends MetaTileEntityModularui implements IWorkable {
    protected final ICubeRenderer renderer;
    int i=0;
    boolean isActive=true;
    boolean isWorkingEnabled=true;
    int process=0;
    public MetaTileEntityIndustrialApiary(ResourceLocation metaTileEntityId, ICubeRenderer renderer) {
        super(metaTileEntityId, GTValues.UV);
        this.renderer = renderer;
    }
    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityIndustrialApiary(this.metaTileEntityId,renderer);
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        return null;
    }

    @Override
    public ModularPanel buildUI(GuiData guiData, GuiSyncManager guiSyncManager) {
        ModularPanel panel = ModularPanel.defaultPanel("industrial_apiary_gui");
        panel.bindPlayerInventory();
        return panel;
    }
    @Override
    public boolean onRightClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing, CuboidRayTraceResult hitResult) {
        ItemStack heldStack = playerIn.getHeldItem(hand);
        if (this instanceof IDataStickIntractable) {
            IDataStickIntractable dsi = (IDataStickIntractable)this;
            if (MetaItems.TOOL_DATA_STICK.isItemEqual(heldStack) && dsi.onDataStickRightClick(playerIn, heldStack)) {
                return true;
            }
        }
        //在这里写UI
        if (!playerIn.isSneaking() && this.openGUIOnRightClick()) {
            if (this.getWorld() != null && !this.getWorld().isRemote) {
               //ui
                DrtMetaTileEntityGuiFactory.open(playerIn,this);
            }

            return true;
        } else if (heldStack.getItem() == Items.NAME_TAG && playerIn.isSneaking() && heldStack.getTagCompound() != null && heldStack.getTagCompound().hasKey("display")) {
            MetaTileEntityHolder mteHolder = (MetaTileEntityHolder)this.getHolder();
            mteHolder.setCustomName(heldStack.getTagCompound().getCompoundTag("display").getString("Name"));
            if (!playerIn.isCreative()) {
                heldStack.shrink(1);
            }

            return true;
        } else {
            EnumFacing hitFacing = hitResult.sideHit;
            Cover cover = hitFacing == null ? null : this.getCoverAtSide(hitFacing);
            if (cover != null && cover.onRightClick(playerIn, hand, hitResult) == EnumActionResult.SUCCESS) {
                return true;
            } else {
                EnumFacing gridSideHit = CoverRayTracer.determineGridSideHit(hitResult);
                cover = gridSideHit == null ? null : this.getCoverAtSide(gridSideHit);
                if (cover != null && playerIn.isSneaking() && playerIn.getHeldItemMainhand().isEmpty()) {
                    return cover.onScrewdriverClick(playerIn, hand, hitResult) == EnumActionResult.SUCCESS;
                } else {
                    return false;
                }
            }
        }
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        this.renderOverlays(renderState, translation, pipeline);

    }
    protected void renderOverlays(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        this.renderer.renderOrientedState(renderState, translation, pipeline, this.getFrontFacing(), isActive(), isWorkingEnabled());
    }

    @Override
    public void update() {
        if(!getWorld().isRemote)
        {
            if(i++>100)
            {
                i=0;
                setWorkingEnabled(!isWorkingEnabled);
            }
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
    public boolean isWorkingEnabled() {
        return this.isWorkingEnabled;
    }

    @Override
    public boolean isActive() {
        return this.isActive;
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
        return 100;
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (capability == GregtechTileCapabilities.CAPABILITY_WORKABLE)
        {
            return GregtechTileCapabilities.CAPABILITY_WORKABLE.cast(this);
        }else if (capability == GregtechTileCapabilities.CAPABILITY_CONTROLLABLE)
        {
            return GregtechTileCapabilities.CAPABILITY_CONTROLLABLE.cast(this);
        }
        return super.getCapability(capability, side);
    }



}
