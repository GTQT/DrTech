package com.drppp.drtech.common.MetaTileEntities.muti.mutipart;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.drppp.drtech.api.Muti.DrtMultiblockAbility;
import com.drppp.drtech.api.capability.IAssembly;
import gregtech.api.GTValues;
import gregtech.api.capability.*;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.capability.impl.GhostCircuitItemStackHandler;
import gregtech.api.capability.impl.ItemHandlerList;
import gregtech.api.capability.impl.NotifiableFluidTank;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.TankWidget;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.api.util.GTHashMaps;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.renderer.texture.cube.SimpleOverlayRenderer;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityMultiFluidHatch;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityMultiblockNotifiablePart;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MetaTileEntityInputAssembly extends MetaTileEntityMultiblockNotifiablePart
        implements IMultiblockAbilityPart<IAssembly>, IControllable, IGhostSlotConfigurable {
    private static final int BASE_TANK_SIZE = 8000;

    private final int numSlots;
    private final int tankSize;

    private boolean autoCollapse;
    private final FluidTankList fluidTankList;
    private boolean workingEnabled;
    @Nullable
    protected GhostCircuitItemStackHandler circuitInventory;
    private IItemHandlerModifiable actualImportItems;
    public MetaTileEntityInputAssembly(ResourceLocation metaTileEntityId, int tier, int numSlots, boolean isExportHatch)
    {
        super(metaTileEntityId, tier, isExportHatch);
        this.workingEnabled = true;
        this.numSlots = numSlots;
        this.tankSize = (BASE_TANK_SIZE * (1 << Math.min(GTValues.UHV, tier))) / (numSlots == 4 ? 4 : 8);
        FluidTank[] fluidsHandlers = new FluidTank[numSlots];
        for (int i = 0; i < fluidsHandlers.length; i++) {
            fluidsHandlers[i] = new NotifiableFluidTank(tankSize, this, isExportHatch);
        }
        this.fluidTankList = new FluidTankList(false, fluidsHandlers);
        initializeInventory();
    }

    @Override
    protected void initializeInventory() {
        if (this.hasGhostCircuitInventory()) {
            this.circuitInventory = new GhostCircuitItemStackHandler(this);
            this.circuitInventory.addNotifiableMetaTileEntity(this);
            this.actualImportItems = new ItemHandlerList(Arrays.asList(super.getImportItems(), this.circuitInventory));
        } else {
            this.actualImportItems = null;
        }
        if (this.fluidTankList == null) return;
        super.initializeInventory();

    }
    

    @Override
    public void setWorkingEnabled(boolean workingEnabled) {
        this.workingEnabled = workingEnabled;
        World world = getWorld();
        if (world != null && !world.isRemote) {
            writeCustomData(GregtechDataCodes.WORKING_ENABLED, buf -> buf.writeBoolean(workingEnabled));
        }
    }

    @Override
    public boolean isWorkingEnabled() {
        return workingEnabled;
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (capability == GregtechTileCapabilities.CAPABILITY_CONTROLLABLE) {
            return GregtechTileCapabilities.CAPABILITY_CONTROLLABLE.cast(this);
        }
        return super.getCapability(capability, side);
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        if (shouldRenderOverlay()) {
            SimpleOverlayRenderer renderer = isExportHatch ? Textures.PIPE_OUT_OVERLAY : Textures.PIPE_IN_OVERLAY;
            renderer.renderSided(getFrontFacing(), renderState, translation, pipeline);
            SimpleOverlayRenderer overlay = isExportHatch ? Textures.ITEM_HATCH_OUTPUT_OVERLAY :
                    Textures.ITEM_HATCH_INPUT_OVERLAY;
            overlay.renderSided(getFrontFacing(), renderState, translation, pipeline);
        }
    }
    @Override
    protected FluidTankList createImportFluidHandler() {
        return isExportHatch ? new FluidTankList(false) : fluidTankList;
    }

    @Override
    protected FluidTankList createExportFluidHandler() {
        return isExportHatch ? fluidTankList : new FluidTankList(false);
    }
    @Override
    public boolean hasGhostCircuitInventory() {
        return !this.isExportHatch;
    }

    @Override
    public void setGhostCircuitConfig(int config) {
        if (this.circuitInventory == null || this.circuitInventory.getCircuitValue() == config) {
            return;
        }
        this.circuitInventory.setCircuitValue(config);
        if (!getWorld().isRemote) {
            markDirty();
        }
    }
    @Override
    public void update() {
        super.update();
        if (!getWorld().isRemote) {
            if (workingEnabled) {
                if (isExportHatch) {
                    pushFluidsIntoNearbyHandlers(getFrontFacing());
                    pushItemsIntoNearbyHandlers(getFrontFacing());
                } else {
                    pullFluidsFromNearbyHandlers(getFrontFacing());
                    pullItemsFromNearbyHandlers(getFrontFacing());
                }
                if (isAutoCollapse()) {
                    // Exclude the ghost circuit inventory from the auto collapse, so it does not extract any ghost circuits
                    // from the slot
                    IItemHandlerModifiable inventory = (isExportHatch ? this.getExportItems() : super.getImportItems());
                    if (!isAttachedToMultiBlock() || (isExportHatch ? this.getNotifiedItemOutputList().contains(inventory) :
                            this.getNotifiedItemInputList().contains(inventory))) {
                        collapseInventorySlotContents(inventory);
                    }
                }
            }
        }
    }
    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity metaTileEntityHolder) {
        return new MetaTileEntityInputAssembly(metaTileEntityId, this.getTier(), numSlots, this.isExportHatch);
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {

        int rowSize = (int) Math.sqrt(numSlots);
        ModularUI.Builder builder = ModularUI.builder(GuiTextures.BACKGROUND, 176,
                        18 + 18 * rowSize + 94)
                .label(10, 5, getMetaFullName());

        for (int y = 0; y < rowSize; y++) {
            for (int x = 0; x < rowSize; x++) {
                int index = y * rowSize + x;
                builder.widget(
                        new TankWidget(fluidTankList.getTankAt(index), 89 - rowSize * 9 + x * 18, 18 + y * 18, 18, 18)
                                .setBackgroundTexture(GuiTextures.FLUID_SLOT)
                                .setContainerClicking(true, !isExportHatch)
                                .setAlwaysShowFull(true));
            }
        }
        builder.bindPlayerInventory(entityPlayer.inventory, GuiTextures.SLOT, 7, 18 + 18 * rowSize + 12);
        return builder.build(getHolder(), entityPlayer);
    }

    @Override
    public MultiblockAbility<IAssembly> getAbility() {
        return isExportHatch ? DrtMultiblockAbility.EXPORT_ASSEMBLY : DrtMultiblockAbility.IMPORT_ASSEMBLY;
    }

    @Override
    public void registerAbilities(List<IAssembly> abilityList) {
        for (var s:fluidTankList.getFluidTanks())
        {
            abilityList.add((IAssembly) s.getTank());
        }

        if (this.hasGhostCircuitInventory() && this.actualImportItems != null) {
            abilityList.add((IAssembly) (isExportHatch ? this.exportItems : this.actualImportItems));
        } else {
            abilityList.add((IAssembly) (isExportHatch ? this.exportItems : this.importItems));
        }
    }
    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeBoolean(workingEnabled);
        buf.writeBoolean(autoCollapse);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.workingEnabled = buf.readBoolean();
        this.autoCollapse = buf.readBoolean();
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == GregtechDataCodes.WORKING_ENABLED) {
            this.workingEnabled = buf.readBoolean();
        }else if (dataId == GregtechDataCodes.TOGGLE_COLLAPSE_ITEMS) {
            this.autoCollapse = buf.readBoolean();
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setBoolean("workingEnabled", workingEnabled);
        data.setBoolean("autoCollapse", autoCollapse);
        if (this.circuitInventory != null && !this.isExportHatch) {
            this.circuitInventory.write(data);
        }
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        if (data.hasKey("workingEnabled")) {
            this.workingEnabled = data.getBoolean("workingEnabled");
        }
        if (data.hasKey("autoCollapse")) {
            this.autoCollapse = data.getBoolean("autoCollapse");
        }
        if (this.circuitInventory != null && !this.isExportHatch) {
            this.circuitInventory.read(data);
        }
    }
    private int getInventorySize() {
        int sizeRoot = 1 + Math.min(9, getTier());
        return sizeRoot * sizeRoot;
    }
    private static void collapseInventorySlotContents(IItemHandlerModifiable inventory) {
        // Gather a snapshot of the provided inventory
        Object2IntMap<ItemStack> inventoryContents = GTHashMaps.fromItemHandler(inventory, true);

        List<ItemStack> inventoryItemContents = new ArrayList<>();

        // Populate the list of item stacks in the inventory with apportioned item stacks, for easy replacement
        for (Object2IntMap.Entry<ItemStack> e : inventoryContents.object2IntEntrySet()) {
            ItemStack stack = e.getKey();
            int count = e.getIntValue();
            int maxStackSize = stack.getMaxStackSize();
            while (count >= maxStackSize) {
                ItemStack copy = stack.copy();
                copy.setCount(maxStackSize);
                inventoryItemContents.add(copy);
                count -= maxStackSize;
            }
            if (count > 0) {
                ItemStack copy = stack.copy();
                copy.setCount(count);
                inventoryItemContents.add(copy);
            }
        }

        for (int i = 0; i < inventory.getSlots(); i++) {
            ItemStack stackToMove;
            // Ensure that we are not exceeding the List size when attempting to populate items
            if (i >= inventoryItemContents.size()) {
                stackToMove = ItemStack.EMPTY;
            } else {
                stackToMove = inventoryItemContents.get(i);
            }

            // Populate the slots
            inventory.setStackInSlot(i, stackToMove);
        }
    }

    @Override
    public boolean onScrewdriverClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing,
                                      CuboidRayTraceResult hitResult) {
        setAutoCollapse(!this.autoCollapse);

        if (!getWorld().isRemote) {
            if (this.autoCollapse) {
                playerIn.sendStatusMessage(new TextComponentTranslation("gregtech.bus.collapse_true"), true);
            } else {
                playerIn.sendStatusMessage(new TextComponentTranslation("gregtech.bus.collapse_false"), true);
            }
        }
        return true;
    }
    public boolean isAutoCollapse() {
        return autoCollapse;
    }

    public void setAutoCollapse(boolean inverted) {
        autoCollapse = inverted;
        if (!getWorld().isRemote) {
            if (autoCollapse) {
                if (isExportHatch) {
                    addNotifiedOutput(this.getExportItems());
                } else {
                    addNotifiedInput(super.getImportItems());
                }
            }
            writeCustomData(GregtechDataCodes.TOGGLE_COLLAPSE_ITEMS,
                    packetBuffer -> packetBuffer.writeBoolean(autoCollapse));
            notifyBlockUpdate();
            markDirty();
        }
    }
    @Override
    public IItemHandlerModifiable getImportItems() {
        return this.actualImportItems == null ? super.getImportItems() : this.actualImportItems;
    }

    @Override
    public void addToMultiBlock(MultiblockControllerBase controllerBase) {
        super.addToMultiBlock(controllerBase);
        if (hasGhostCircuitInventory() && this.actualImportItems instanceof ItemHandlerList) {
            for (IItemHandler handler : ((ItemHandlerList) this.actualImportItems).getBackingHandlers()) {
                if (handler instanceof INotifiableHandler notifiable) {
                    notifiable.addNotifiableMetaTileEntity(controllerBase);
                    notifiable.addToNotifiedList(this, handler, isExportHatch);
                }
            }
        }
    }

    @Override
    public void removeFromMultiBlock(MultiblockControllerBase controllerBase) {
        super.removeFromMultiBlock(controllerBase);
        if (hasGhostCircuitInventory() && this.actualImportItems instanceof ItemHandlerList) {
            for (IItemHandler handler : ((ItemHandlerList) this.actualImportItems).getBackingHandlers()) {
                if (handler instanceof INotifiableHandler notifiable) {
                    notifiable.removeNotifiableMetaTileEntity(controllerBase);
                }
            }
        }
    }
}
