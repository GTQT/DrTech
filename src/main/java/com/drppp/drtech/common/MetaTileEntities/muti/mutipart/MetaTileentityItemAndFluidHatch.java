package com.drppp.drtech.common.MetaTileEntities.muti.mutipart;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.drppp.drtech.api.Muti.DrtMultiblockAbility;
import com.drppp.drtech.api.capability.IItemAndFluidHandler;
import gregtech.api.capability.*;
import gregtech.api.capability.impl.*;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.resources.IGuiTexture;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.gui.widgets.GhostCircuitSlotWidget;
import gregtech.api.gui.widgets.SlotWidget;
import gregtech.api.gui.widgets.TankWidget;
import gregtech.api.items.itemhandlers.GTItemStackHandler;
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
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class MetaTileentityItemAndFluidHatch  extends MetaTileEntityMultiblockNotifiablePart implements IMultiblockAbilityPart<IItemAndFluidHandler>, IControllable, IGhostSlotConfigurable {
    private static final int BASE_TANK_SIZE = 8000;
    private final int numSlots;
    private final int tankSize;
    private  IItemAndFluidHandler FluidAndItemStore;
    private boolean workingEnabled = true;
    protected @Nullable GhostCircuitItemStackHandler circuitInventory;
    private IItemHandlerModifiable actualImportItems;
    private boolean autoCollapse;
    public MetaTileentityItemAndFluidHatch(ResourceLocation metaTileEntityId, int tier, boolean isExportHatch) {
        super(metaTileEntityId, tier, isExportHatch);
        this.numSlots =  (int)Math.sqrt(this.getInventorySize());
        this.tankSize = BASE_TANK_SIZE * (1 << Math.min(9, tier)) / numSlots +(BASE_TANK_SIZE * (1 << Math.min(9, tier))%numSlots);
        this.initializeInventory();
    }
    protected void initializeInventory() {



        FluidTank[] fluidsHandlers = new FluidTank[numSlots];
        List<FluidTank> tankslist = new ArrayList<>();
        for(int i = 0; i < fluidsHandlers.length; ++i) {
            fluidsHandlers[i] = new NotifiableFluidTank(this.tankSize, this, isExportHatch);
            tankslist.add(fluidsHandlers[i]);
        }
        if (this.hasGhostCircuitInventory()) {
            this.circuitInventory = new GhostCircuitItemStackHandler(this);
            this.circuitInventory.addNotifiableMetaTileEntity(this);
            var list = Arrays.asList(this.createNewImportItemHandler(), this.circuitInventory);
            this.FluidAndItemStore = new ItemAndFluidHandler(false, tankslist,list);
            this.actualImportItems =  this.FluidAndItemStore;
        } else {
            var list = Arrays.asList(this.createNewExportItemHandler());
            this.FluidAndItemStore = new ItemAndFluidHandler(false,  tankslist,list);
            this.actualImportItems = null;
        }
        this.importFluids = this.createImportFluidHandler();
        this.exportFluids = this.createExportFluidHandler();
        this.fluidInventory = new FluidHandlerProxy(this.importFluids, this.exportFluids);
        this.importItems = this.createImportItemHandler();
        this.exportItems = this.createExportItemHandler();
        this.itemInventory = new ItemHandlerProxy(this.importItems, this.exportItems);
    }
    public IItemHandlerModifiable getImportItems() {
        return this.actualImportItems == null ? super.getImportItems() : this.FluidAndItemStore;
    }
    public void addToMultiBlock(MultiblockControllerBase controllerBase) {
        super.addToMultiBlock(controllerBase);
        if (this.hasGhostCircuitInventory() && this.actualImportItems instanceof ItemHandlerList) {
            Iterator var2 = ((ItemHandlerList)this.actualImportItems).getBackingHandlers().iterator();

            while(var2.hasNext()) {
                IItemHandler handler = (IItemHandler)var2.next();
                if (handler instanceof INotifiableHandler) {
                    INotifiableHandler notifiable = (INotifiableHandler)handler;
                    notifiable.addNotifiableMetaTileEntity(controllerBase);
                    notifiable.addToNotifiedList(this, handler, this.isExportHatch);
                }
            }
        }

    }

    public void removeFromMultiBlock(MultiblockControllerBase controllerBase) {
        super.removeFromMultiBlock(controllerBase);
        if (this.hasGhostCircuitInventory() && this.actualImportItems instanceof ItemHandlerList) {
            Iterator var2 = ((ItemHandlerList)this.actualImportItems).getBackingHandlers().iterator();

            while(var2.hasNext()) {
                IItemHandler handler = (IItemHandler)var2.next();
                if (handler instanceof INotifiableHandler) {
                    INotifiableHandler notifiable = (INotifiableHandler)handler;
                    notifiable.removeNotifiableMetaTileEntity(controllerBase);
                }
            }
        }

    }
    protected FluidTankList createImportFluidHandler() {
        return this.isExportHatch ? new FluidTankList(false) : (FluidTankList) this.FluidAndItemStore;
    }

    protected FluidTankList createExportFluidHandler() {
        return this.isExportHatch ? (FluidTankList) this.FluidAndItemStore : new FluidTankList(false);
    }
    private int getInventorySize() {
        int sizeRoot = 1 + Math.min(9, this.getTier());
        return sizeRoot * sizeRoot;
    }

    protected IItemHandlerModifiable createExportItemHandler() {
        return this.isExportHatch ?
                this.FluidAndItemStore
                :
                new GTItemStackHandler(this, 0);
    }

    protected IItemHandlerModifiable createImportItemHandler() {
        return this.isExportHatch ?
                new GTItemStackHandler(this, 0)
                :
                this.FluidAndItemStore;
    }
    protected IItemHandlerModifiable createNewExportItemHandler() {
        return this.isExportHatch ?
                new NotifiableItemStackHandler(this, this.getInventorySize(), this.getController(), true)
                :
                new GTItemStackHandler(this, 0);
    }

    protected IItemHandlerModifiable createNewImportItemHandler() {
        return this.isExportHatch ?
                new GTItemStackHandler(this, 0)
                :
                new NotifiableItemStackHandler(this, this.getInventorySize(), this.getController(), false);
    }
    @Override
    public boolean isWorkingEnabled() {
        return this.workingEnabled;
    }

    @Override
    public void setWorkingEnabled(boolean workingEnabled) {
        this.workingEnabled = workingEnabled;
        World world = this.getWorld();
        if (world != null && !world.isRemote) {
            this.writeCustomData(GregtechDataCodes.WORKING_ENABLED, (buf) -> {
                buf.writeBoolean(workingEnabled);
            });
        }
    }
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeBoolean(this.workingEnabled);
        buf.writeBoolean(this.autoCollapse);
    }

    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.workingEnabled = buf.readBoolean();
        this.autoCollapse = buf.readBoolean();
    }

    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == GregtechDataCodes.WORKING_ENABLED) {
            this.workingEnabled = buf.readBoolean();
        }
        else if (dataId == GregtechDataCodes.TOGGLE_COLLAPSE_ITEMS) {
            this.autoCollapse = buf.readBoolean();
        }

    }

    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setBoolean("workingEnabled", this.workingEnabled);
        data.setBoolean("autoCollapse", this.autoCollapse);
        if (this.circuitInventory != null && !this.isExportHatch) {
            this.circuitInventory.write(data);
        }
        return data;
    }

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
    @Override
    public boolean hasGhostCircuitInventory() {
        return !this.isExportHatch;
    }

    @Override
    public void setGhostCircuitConfig(int config) {
        if (this.circuitInventory != null && this.circuitInventory.getCircuitValue() != config) {
            this.circuitInventory.setCircuitValue(config);
            if (!this.getWorld().isRemote) {
                this.markDirty();
            }

        }
    }
    public void update() {
        super.update();
        if (!this.getWorld().isRemote && this.workingEnabled) {
            if (this.isExportHatch) {
                this.pushFluidsIntoNearbyHandlers(this.getFrontFacing());
                this.pushItemsIntoNearbyHandlers(this.getFrontFacing());
            } else {
                this.pullFluidsFromNearbyHandlers(this.getFrontFacing());
                this.pullItemsFromNearbyHandlers(this.getFrontFacing());
            }
            if (this.isAutoCollapse()) {
                IItemHandlerModifiable inventory = this.isExportHatch ? this.getExportItems() : super.getImportItems();
                if (this.isExportHatch) {
                    if (!this.getNotifiedItemOutputList().contains(inventory)) {
                        return;
                    }
                } else if (!this.getNotifiedItemInputList().contains(inventory)) {
                    return;
                }

                collapseInventorySlotContents(inventory);
            }
        }

    }
    public boolean isAutoCollapse() {
        return this.autoCollapse;
    }

    private static void collapseInventorySlotContents(IItemHandlerModifiable inventory) {
        Object2IntMap<ItemStack> inventoryContents = GTHashMaps.fromItemHandler(inventory, true);
        List<ItemStack> inventoryItemContents = new ArrayList();
        ObjectIterator var3 = inventoryContents.object2IntEntrySet().iterator();

        while(var3.hasNext()) {
            Object2IntMap.Entry<ItemStack> e = (Object2IntMap.Entry)var3.next();
            ItemStack stack = e.getKey();
            int count = e.getIntValue();

            ItemStack copy;
            for(int maxStackSize = stack.getMaxStackSize(); count >= maxStackSize; count -= maxStackSize) {
                copy = stack.copy();
                copy.setCount(maxStackSize);
                inventoryItemContents.add(copy);
            }

            if (count > 0) {
                copy = stack.copy();
                copy.setCount(count);
                inventoryItemContents.add(copy);
            }
        }

        for(int i = 0; i < inventory.getSlots(); ++i) {
            ItemStack stackToMove;
            if (i >= inventoryItemContents.size()) {
                stackToMove = ItemStack.EMPTY;
            } else {
                stackToMove = inventoryItemContents.get(i);
            }

            inventory.setStackInSlot(i, stackToMove);
        }

    }
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        return capability == GregtechTileCapabilities.CAPABILITY_CONTROLLABLE ? GregtechTileCapabilities.CAPABILITY_CONTROLLABLE.cast(this) : super.getCapability(capability, side);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileentityItemAndFluidHatch(this.metaTileEntityId, this.getTier(), this.isExportHatch);
    }
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        if (this.shouldRenderOverlay()) {
            SimpleOverlayRenderer renderer = this.numSlots == 4 ? Textures.PIPE_4X_OVERLAY : Textures.PIPE_9X_OVERLAY;
            renderer.renderSided(this.getFrontFacing(), renderState, translation, pipeline);
        }

    }
    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        int rowSize = (int)Math.sqrt(this.getInventorySize());
        return this.createUITemplate(entityPlayer, rowSize).build(this.getHolder(), entityPlayer);
    }
    private ModularUI.Builder createUITemplate(EntityPlayer player, int gridSize) {
        int backgroundWidth = gridSize > 6 ? 176 + (gridSize - 6) * 18 : 176;
        int center = backgroundWidth / 2;
        int gridStartX = center - gridSize * 9;
        int inventoryStartX = center - 9 - 72;
        int inventoryStartY = 18 + 18 * gridSize + 12;
        ModularUI.Builder builder = ModularUI.builder(GuiTextures.BACKGROUND, backgroundWidth, 36 + 18 * gridSize + 94).label(10, 5, this.getMetaFullName());

        int circuitX;
        int circuitY;
        for(circuitX = 0; circuitX < gridSize; ++circuitX) {
            for(circuitY = 0; circuitY < gridSize; ++circuitY) {
                int index = circuitX * gridSize + circuitY;
                builder.widget((new SlotWidget(this.isExportHatch ? this.exportItems : this.importItems, index, gridStartX + circuitY * 18, 18 + circuitX * 18, true, !this.isExportHatch)).setBackgroundTexture(GuiTextures.SLOT));
            }
            if(circuitX==gridSize-1)
            {
                for(circuitY = 0; circuitY < gridSize; ++circuitY) {
                    builder.widget((new TankWidget(this.FluidAndItemStore.getTankAt(circuitY), gridStartX + circuitY * 18, 36 + circuitX * 18, 18, 18)).setBackgroundTexture(GuiTextures.FLUID_SLOT).setContainerClicking(true, !this.isExportHatch).setAlwaysShowFull(true));
                }
            }
        }

        if (this.hasGhostCircuitInventory() && this.circuitInventory != null) {
            circuitX = gridSize > 6 ? gridStartX + gridSize * 18 + 9 : inventoryStartX + 144;
            circuitY = gridSize * 18+18;
            SlotWidget circuitSlot = (new GhostCircuitSlotWidget(this.circuitInventory, 0, circuitX, circuitY)).setBackgroundTexture(GuiTextures.SLOT, this.getCircuitSlotOverlay());
            builder.widget(circuitSlot.setConsumer(this::getCircuitSlotTooltip));
        }

        return builder.bindPlayerInventory(player.inventory, GuiTextures.SLOT, inventoryStartX, inventoryStartY+18);
    }
    protected TextureArea getCircuitSlotOverlay() {
        return GuiTextures.INT_CIRCUIT_OVERLAY;
    }

    protected void getCircuitSlotTooltip(@NotNull SlotWidget widget) {
        String configString;
        if (this.circuitInventory != null && this.circuitInventory.getCircuitValue() != -1) {
            configString = String.valueOf(this.circuitInventory.getCircuitValue());
        } else {
            configString = (new TextComponentTranslation("gregtech.gui.configurator_slot.no_value")).getFormattedText();
        }

        widget.setTooltipText("gregtech.gui.configurator_slot.tooltip", configString);
    }
    @Override
    public MultiblockAbility<IItemAndFluidHandler> getAbility() {
        return this.isExportHatch ? DrtMultiblockAbility.EXPORT_ITEM_FLUID : DrtMultiblockAbility.IMPORT_ITEM_FLUID;
    }

    @Override
    public void registerAbilities(List<IItemAndFluidHandler> list) {
        list.addAll(Collections.singleton(this.FluidAndItemStore));
    }
    private class ItemAndFluidHandler extends FluidTankList implements IItemAndFluidHandler,IItemHandlerModifiable
    {
        private final Int2ObjectMap<IItemHandler> handlerBySlotIndex = new Int2ObjectOpenHashMap();
        private final Map<IItemHandler, Integer> baseIndexOffset = new IdentityHashMap();
        public ItemAndFluidHandler(boolean allowSameFluidFill, @NotNull List<? extends IFluidTank> fluidTanks,List<? extends IItemHandler> itemHandlerList) {
            super(allowSameFluidFill, fluidTanks);
            int currentSlotIndex = 0;

            int slotsCount;
            for(Iterator var3 = itemHandlerList.iterator(); var3.hasNext(); currentSlotIndex += slotsCount) {
                IItemHandler itemHandler = (IItemHandler)var3.next();
                if (this.baseIndexOffset.containsKey(itemHandler)) {
                    throw new IllegalArgumentException("Attempted to add item handler " + itemHandler + " twice");
                }

                this.baseIndexOffset.put(itemHandler, currentSlotIndex);
                slotsCount = itemHandler.getSlots();

                for(int slotIndex = 0; slotIndex < slotsCount; ++slotIndex) {
                    this.handlerBySlotIndex.put(currentSlotIndex + slotIndex, itemHandler);
                }
            }
        }

        public int getSlots() {
            return this.handlerBySlotIndex.size();
        }

        public void setStackInSlot(int slot, @NotNull ItemStack stack) {
            IItemHandler itemHandler = this.handlerBySlotIndex.get(slot);
            if (!(itemHandler instanceof IItemHandlerModifiable)) {
                throw new UnsupportedOperationException("Handler " + itemHandler + " does not support this method");
            } else {
                ((IItemHandlerModifiable)itemHandler).setStackInSlot(slot - (Integer)this.baseIndexOffset.get(itemHandler), stack);
            }
        }

        public @NotNull ItemStack getStackInSlot(int slot) {
            IItemHandler itemHandler = this.handlerBySlotIndex.get(slot);
            int var10000 = slot - this.baseIndexOffset.get(itemHandler);
            return itemHandler.getStackInSlot(slot - this.baseIndexOffset.get(itemHandler));
        }

        public int getSlotLimit(int slot) {
            IItemHandler itemHandler = this.handlerBySlotIndex.get(slot);
            return itemHandler.getSlotLimit(slot - this.baseIndexOffset.get(itemHandler));
        }

        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            IItemHandler itemHandler = this.handlerBySlotIndex.get(slot);
            return itemHandler.insertItem(slot - this.baseIndexOffset.get(itemHandler), stack, simulate);
        }

        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            IItemHandler itemHandler = this.handlerBySlotIndex.get(slot);
            return itemHandler.extractItem(slot - this.baseIndexOffset.get(itemHandler), amount, simulate);
        }
        public @NotNull Collection<IItemHandler> getBackingHandlers() {
            return Collections.unmodifiableCollection(this.handlerBySlotIndex.values());
        }
    }
}
