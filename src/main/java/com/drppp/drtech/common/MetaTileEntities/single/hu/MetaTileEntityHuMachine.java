package com.drppp.drtech.common.MetaTileEntities.single.hu;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;

import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.drppp.drtech.api.capability.DrtechCommonCapabilities;
import com.drppp.drtech.api.capability.IHeatEnergy;
import com.drppp.drtech.api.capability.IRotationEnergy;
import com.drppp.drtech.api.capability.RuMachineAcceptFacing;
import com.drppp.drtech.api.capability.impl.HeatEnergyHandler;
import com.drppp.drtech.api.capability.impl.RecipeLogicHU;
import com.drppp.drtech.api.capability.impl.RecipeLogicRU;
import com.drppp.drtech.api.capability.impl.RotationEnergyHandler;
import gregtech.api.GTValues;
import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IActiveOutputSide;
import gregtech.api.capability.IGhostSlotConfigurable;
import gregtech.api.capability.impl.*;
import gregtech.api.cover.Cover;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.resources.IGuiTexture;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.gui.widgets.*;
import gregtech.api.items.itemhandlers.GTItemStackHandler;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.WorkableTieredMetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.util.GTTransferUtils;
import gregtech.api.util.GTUtility;
import gregtech.client.particle.IMachineParticleEffect;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.renderer.texture.cube.SimpleSidedCubeRenderer;
import gregtech.client.utils.RenderUtil;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class MetaTileEntityHuMachine extends WorkableTieredMetaTileEntity  implements IActiveOutputSide, IGhostSlotConfigurable {

    private final boolean hasFrontFacing;
    private final RuMachineAcceptFacing[] acceptFacing;
    //protected final GTItemStackHandler chargerInventory;
    protected @Nullable GhostCircuitItemStackHandler circuitInventory;
    private EnumFacing outputFacingItems;
    private EnumFacing outputFacingFluids;
    private boolean autoOutputItems;
    private boolean autoOutputFluids;
    private boolean allowInputFromOutputSideItems;
    private boolean allowInputFromOutputSideFluids;
    protected IItemHandler outputItemInventory;
    protected IFluidHandler outputFluidInventory;
    private IItemHandlerModifiable actualImportItems;
    private static final int FONT_HEIGHT = 9;
    protected final @Nullable IMachineParticleEffect tickingParticle;
    protected final @Nullable IMachineParticleEffect randomParticle;
    private IHeatEnergy hu;
    public MetaTileEntityHuMachine(ResourceLocation metaTileEntityId, RecipeMap<?> recipeMap, ICubeRenderer renderer, int tier, boolean hasFrontFacing, RuMachineAcceptFacing[] acceptFacing) {
        this(metaTileEntityId, recipeMap, renderer, tier, hasFrontFacing, GTUtility.defaultTankSizeFunction,acceptFacing);
    }

    public MetaTileEntityHuMachine(ResourceLocation metaTileEntityId, RecipeMap<?> recipeMap, ICubeRenderer renderer, int tier, boolean hasFrontFacing, Function<Integer, Integer> tankScalingFunction, RuMachineAcceptFacing[] acceptFacing) {
        this(metaTileEntityId, recipeMap, renderer, tier, hasFrontFacing, tankScalingFunction, null, null,acceptFacing);
    }

    public MetaTileEntityHuMachine(ResourceLocation metaTileEntityId, RecipeMap<?> recipeMap, ICubeRenderer renderer, int tier, boolean hasFrontFacing, Function<Integer, Integer> tankScalingFunction, @Nullable IMachineParticleEffect tickingParticle, @Nullable IMachineParticleEffect randomParticle, RuMachineAcceptFacing[] acceptFacing) {
        super(metaTileEntityId, recipeMap, renderer, tier, tankScalingFunction);
        this.allowInputFromOutputSideItems = false;
        this.allowInputFromOutputSideFluids = false;
        this.hasFrontFacing = hasFrontFacing;
        //this.chargerInventory = new GTItemStackHandler(this, 1);
        this.tickingParticle = tickingParticle;
        this.randomParticle = randomParticle;
        this.acceptFacing = acceptFacing;

    }
    @Override
    protected AbstractRecipeLogic createWorkable(RecipeMap<?> recipeMap) {
        this.hu = new HeatEnergyHandler();
        return new RecipeLogicHU(this, recipeMap, hu);
    }
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityHuMachine(this.metaTileEntityId, this.workable.getRecipeMap(), this.renderer, this.getTier(), this.hasFrontFacing, this.getTankScalingFunction(), this.tickingParticle, this.randomParticle,this.acceptFacing);
    }

    protected void initializeInventory() {
        super.initializeInventory();
        this.outputItemInventory = new ItemHandlerProxy(new GTItemStackHandler(this, 0), this.exportItems);
        this.outputFluidInventory = new FluidHandlerProxy(new FluidTankList(false), this.exportFluids);
        if (this.hasGhostCircuitInventory()) {
            this.circuitInventory = new GhostCircuitItemStackHandler(this);
            this.circuitInventory.addNotifiableMetaTileEntity(this);
        }

        this.actualImportItems = null;
    }

    public IItemHandlerModifiable getImportItems() {
        if (this.actualImportItems == null) {
            this.actualImportItems = this.circuitInventory == null ? super.getImportItems() : new ItemHandlerList(Arrays.asList(super.getImportItems(), this.circuitInventory));
        }

        return this.actualImportItems;
    }

    public boolean hasFrontFacing() {
        return this.hasFrontFacing;
    }

    public boolean onWrenchClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing, CuboidRayTraceResult hitResult) {
        if (!playerIn.isSneaking()) {
            if (this.getOutputFacing() == facing) {
                return false;
            } else if (this.hasFrontFacing() && facing == this.getFrontFacing()) {
                return false;
            } else {
                if (!this.getWorld().isRemote) {
                    this.setOutputFacing(facing);
                }

                return true;
            }
        } else {
            return super.onWrenchClick(playerIn, hand, facing, hitResult);
        }
    }

    public void addCover(@NotNull EnumFacing side, @NotNull Cover cover) {
        super.addCover(side, cover);
        if (cover.canInteractWithOutputSide()) {
            if (this.getOutputFacingItems() == side) {
                this.setAllowInputFromOutputSideItems(true);
            }

            if (this.getOutputFacingFluids() == side) {
                this.setAllowInputFromOutputSideFluids(true);
            }
        }

    }
    @SideOnly(Side.CLIENT)
    protected SimpleSidedCubeRenderer getBaseRenderer() {
        return com.drppp.drtech.Client.Textures.MACHINE_CASINGS[this.getTier()-1];
    }
    @SideOnly(Side.CLIENT)
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        if (this.outputFacingFluids != null && this.getExportFluids().getTanks() > 0) {
            Textures.PIPE_OUT_OVERLAY.renderSided(this.outputFacingFluids, renderState, RenderUtil.adjustTrans(translation, this.outputFacingFluids, 2), pipeline);
        }

        if (this.outputFacingItems != null && this.getExportItems().getSlots() > 0) {
            Textures.PIPE_OUT_OVERLAY.renderSided(this.outputFacingItems, renderState, RenderUtil.adjustTrans(translation, this.outputFacingItems, 2), pipeline);
        }

        if (this.isAutoOutputItems() && this.outputFacingItems != null) {
            Textures.ITEM_OUTPUT_OVERLAY.renderSided(this.outputFacingItems, renderState, RenderUtil.adjustTrans(translation, this.outputFacingItems, 2), pipeline);
        }

        if (this.isAutoOutputFluids() && this.outputFacingFluids != null) {
            Textures.FLUID_OUTPUT_OVERLAY.renderSided(this.outputFacingFluids, renderState, RenderUtil.adjustTrans(translation, this.outputFacingFluids, 2), pipeline);
        }

    }

    public void update() {
        super.update();
        if (!this.getWorld().isRemote) {
            if(this.hu!=null)
            {
                //根据传递的方向获取
                EnumFacing[] facings = getCanAcceptFacing();
                if(facings!=null)
                {
                    boolean flag=false;
                    for (var f: facings)
                    {
                        TileEntity te = getWorld().getTileEntity(this.getPos().offset(f));
                        if(te!=null && te.hasCapability(DrtechCommonCapabilities.CAPABILITY_ROTATION_ENERGY,f.getOpposite()) && !flag)
                        {
                            flag=true;
                            IRotationEnergy iru = te.getCapability(DrtechCommonCapabilities.CAPABILITY_ROTATION_ENERGY,f.getOpposite());
                            this.hu.setHuEnergy(iru.getEnergyOutput());
                            this.energyContainer.changeEnergy(-this.energyContainer.getEnergyStored());
                            this.energyContainer.changeEnergy(iru.getEnergyOutput());
                        }
                        if(!flag)
                        {
                            clearEnergy();
                        }
                    }
                }
                else
                {
                    clearEnergy();
                }
            }
            if (this.getOffsetTimer() % 5L == 0L) {
                if (this.isAutoOutputFluids()) {
                    this.pushFluidsIntoNearbyHandlers(this.getOutputFacingFluids());
                }

                if (this.isAutoOutputItems()) {
                    this.pushItemsIntoNearbyHandlers(this.getOutputFacingItems());
                }
            }
        } else if (this.tickingParticle != null && this.isActive()) {
            this.tickingParticle.runEffect(this);
        }

    }
    private void clearEnergy()
    {
        this.energyContainer.changeEnergy(-this.energyContainer.getEnergyStored());
        this.hu.setHuEnergy(0);
    }
    private EnumFacing[] getCanAcceptFacing()
    {
        if(this.acceptFacing==null || this.acceptFacing.length==0)
            return null;
        EnumFacing[] result=new EnumFacing[this.acceptFacing.length];
        for (int i = 0; i < this.acceptFacing.length; i++)
        {
            if(this.acceptFacing[i] == RuMachineAcceptFacing.UP )
                result[i] = EnumFacing.UP;
            if(this.acceptFacing[i] == RuMachineAcceptFacing.DOWN)
                result[i] = EnumFacing.DOWN;
            if(this.acceptFacing[i] == RuMachineAcceptFacing.FRONT)
                result[i] = getFrontFacing();
            if(this.acceptFacing[i] == RuMachineAcceptFacing.BACK)
                result[i] = getFrontFacing().getOpposite();
            if(this.acceptFacing[i] == RuMachineAcceptFacing.LEFT)
                result[i] = getFrontFacing().rotateY();
            if(this.acceptFacing[i] == RuMachineAcceptFacing.RIGHT)
                result[i] = getFrontFacing().getOpposite().rotateY();
        }
        return  result;
    }
    @SideOnly(Side.CLIENT)
    public void randomDisplayTick() {
        if (this.randomParticle != null && this.isActive()) {
            this.randomParticle.runEffect(this);
        }

    }

    public boolean onScrewdriverClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing, CuboidRayTraceResult hitResult) {
        if (!this.getWorld().isRemote) {
            if (this.isAllowInputFromOutputSideItems()) {
                this.setAllowInputFromOutputSideItems(false);
                this.setAllowInputFromOutputSideFluids(false);
                playerIn.sendStatusMessage(new TextComponentTranslation("gregtech.machine.basic.input_from_output_side.disallow", new Object[0]), true);
            } else {
                this.setAllowInputFromOutputSideItems(true);
                this.setAllowInputFromOutputSideFluids(true);
                playerIn.sendStatusMessage(new TextComponentTranslation("gregtech.machine.basic.input_from_output_side.allow", new Object[0]), true);
            }
        }

        return true;
    }

    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            IFluidHandler fluidHandler = side == this.getOutputFacingFluids() && !this.isAllowInputFromOutputSideFluids() ? this.outputFluidInventory : this.fluidInventory;
            return fluidHandler.getTankProperties().length > 0 ? CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(fluidHandler) : null;
        } else if (capability != CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            if (capability == GregtechTileCapabilities.CAPABILITY_ACTIVE_OUTPUT_SIDE) {
                return side != this.getOutputFacingItems() && side != this.getOutputFacingFluids() ? null : GregtechTileCapabilities.CAPABILITY_ACTIVE_OUTPUT_SIDE.cast(this);
            } else {
                return super.getCapability(capability, side);
            }
        } else {
            IItemHandler itemHandler = side == this.getOutputFacingItems() && !this.isAllowInputFromOutputSideFluids() ? this.outputItemInventory : this.itemInventory;
            return itemHandler.getSlots() > 0 ? CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(itemHandler) : null;
        }
    }

    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        //data.setTag("ChargerInventory", this.chargerInventory.serializeNBT());
        if (this.circuitInventory != null) {
            this.circuitInventory.write(data);
        }

        data.setInteger("OutputFacing", this.getOutputFacingItems().getIndex());
        data.setInteger("OutputFacingF", this.getOutputFacingFluids().getIndex());
        data.setBoolean("AutoOutputItems", this.autoOutputItems);
        data.setBoolean("AutoOutputFluids", this.autoOutputFluids);
        data.setBoolean("AllowInputFromOutputSide", this.allowInputFromOutputSideItems);
        data.setBoolean("AllowInputFromOutputSideF", this.allowInputFromOutputSideFluids);
        return data;
    }

    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
       // this.chargerInventory.deserializeNBT(data.getCompoundTag("ChargerInventory"));
        if (this.circuitInventory != null) {
            if (data.hasKey("CircuitInventory", 10)) {
                ItemStackHandler legacyCircuitInventory = new ItemStackHandler();
                legacyCircuitInventory.deserializeNBT(data.getCompoundTag("CircuitInventory"));

                for(int i = 0; i < legacyCircuitInventory.getSlots(); ++i) {
                    ItemStack stack = legacyCircuitInventory.getStackInSlot(i);
                    if (!stack.isEmpty()) {
                        stack = GTTransferUtils.insertItem(this.importItems, stack, false);
                        this.circuitInventory.setCircuitValueFromStack(stack);
                    }
                }
            } else {
                this.circuitInventory.read(data);
            }
        }

        this.outputFacingItems = EnumFacing.VALUES[data.getInteger("OutputFacing")];
        this.outputFacingFluids = EnumFacing.VALUES[data.getInteger("OutputFacingF")];
        this.autoOutputItems = data.getBoolean("AutoOutputItems");
        this.autoOutputFluids = data.getBoolean("AutoOutputFluids");
        this.allowInputFromOutputSideItems = data.getBoolean("AllowInputFromOutputSide");
        this.allowInputFromOutputSideFluids = data.getBoolean("AllowInputFromOutputSideF");
    }

    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeByte(this.getOutputFacingItems().getIndex());
        buf.writeByte(this.getOutputFacingFluids().getIndex());
        buf.writeBoolean(this.autoOutputItems);
        buf.writeBoolean(this.autoOutputFluids);
    }

    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.outputFacingItems = EnumFacing.VALUES[buf.readByte()];
        this.outputFacingFluids = EnumFacing.VALUES[buf.readByte()];
        this.autoOutputItems = buf.readBoolean();
        this.autoOutputFluids = buf.readBoolean();
    }

    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == GregtechDataCodes.UPDATE_OUTPUT_FACING) {
            this.outputFacingItems = EnumFacing.VALUES[buf.readByte()];
            this.outputFacingFluids = EnumFacing.VALUES[buf.readByte()];
            this.scheduleRenderUpdate();
        } else if (dataId == GregtechDataCodes.UPDATE_AUTO_OUTPUT_ITEMS) {
            this.autoOutputItems = buf.readBoolean();
            this.scheduleRenderUpdate();
        } else if (dataId == GregtechDataCodes.UPDATE_AUTO_OUTPUT_FLUIDS) {
            this.autoOutputFluids = buf.readBoolean();
            this.scheduleRenderUpdate();
        }

    }

    public boolean isValidFrontFacing(EnumFacing facing) {
        return super.isValidFrontFacing(facing) && facing != this.outputFacingItems && facing != this.outputFacingFluids;
    }

    /** @deprecated */
    @Deprecated
    public void setOutputFacing(EnumFacing outputFacing) {
        this.outputFacingItems = outputFacing;
        this.outputFacingFluids = outputFacing;
        if (!this.getWorld().isRemote) {
            this.notifyBlockUpdate();
            this.writeCustomData(GregtechDataCodes.UPDATE_OUTPUT_FACING, (buf) -> {
                buf.writeByte(this.outputFacingItems.getIndex());
                buf.writeByte(this.outputFacingFluids.getIndex());
            });
            this.markDirty();
        }

    }

    public void setOutputFacingItems(EnumFacing outputFacing) {
        this.outputFacingItems = outputFacing;
        if (!this.getWorld().isRemote) {
            this.notifyBlockUpdate();
            this.writeCustomData(GregtechDataCodes.UPDATE_OUTPUT_FACING, (buf) -> {
                buf.writeByte(this.outputFacingItems.getIndex());
                buf.writeByte(this.outputFacingFluids.getIndex());
            });
            this.markDirty();
        }

    }

    public void setOutputFacingFluids(EnumFacing outputFacing) {
        this.outputFacingFluids = outputFacing;
        if (!this.getWorld().isRemote) {
            this.notifyBlockUpdate();
            this.writeCustomData(GregtechDataCodes.UPDATE_OUTPUT_FACING, (buf) -> {
                buf.writeByte(this.outputFacingItems.getIndex());
                buf.writeByte(this.outputFacingFluids.getIndex());
            });
            this.markDirty();
        }

    }

    public void setAutoOutputItems(boolean autoOutputItems) {
        this.autoOutputItems = autoOutputItems;
        if (!this.getWorld().isRemote) {
            this.writeCustomData(GregtechDataCodes.UPDATE_AUTO_OUTPUT_ITEMS, (buf) -> {
                buf.writeBoolean(autoOutputItems);
            });
            this.markDirty();
        }

    }

    public void setAutoOutputFluids(boolean autoOutputFluids) {
        this.autoOutputFluids = autoOutputFluids;
        if (!this.getWorld().isRemote) {
            this.writeCustomData(GregtechDataCodes.UPDATE_AUTO_OUTPUT_FLUIDS, (buf) -> {
                buf.writeBoolean(autoOutputFluids);
            });
            this.markDirty();
        }

    }

    public void setAllowInputFromOutputSideItems(boolean allowInputFromOutputSide) {
        this.allowInputFromOutputSideItems = allowInputFromOutputSide;
        if (!this.getWorld().isRemote) {
            this.markDirty();
        }

    }

    public void setAllowInputFromOutputSideFluids(boolean allowInputFromOutputSide) {
        this.allowInputFromOutputSideFluids = allowInputFromOutputSide;
        if (!this.getWorld().isRemote) {
            this.markDirty();
        }

    }

    public void setGhostCircuitConfig(int config) {
        if (this.circuitInventory != null && this.circuitInventory.getCircuitValue() != config) {
            this.circuitInventory.setCircuitValue(config);
            if (!this.getWorld().isRemote) {
                this.markDirty();
            }

        }
    }

    public void setFrontFacing(EnumFacing frontFacing) {
        super.setFrontFacing(frontFacing);
        if (this.outputFacingItems == null || this.outputFacingFluids == null) {
            this.setOutputFacing(frontFacing.getOpposite());
        }

    }

    /** @deprecated */
    @Deprecated
    public EnumFacing getOutputFacing() {
        return this.getOutputFacingItems();
    }

    public EnumFacing getOutputFacingItems() {
        return this.outputFacingItems == null ? EnumFacing.SOUTH : this.outputFacingItems;
    }

    public EnumFacing getOutputFacingFluids() {
        return this.outputFacingFluids == null ? EnumFacing.SOUTH : this.outputFacingFluids;
    }

    public boolean isAutoOutputItems() {
        return this.autoOutputItems;
    }

    public boolean isAutoOutputFluids() {
        return this.autoOutputFluids;
    }

    public boolean isAllowInputFromOutputSideItems() {
        return this.allowInputFromOutputSideItems;
    }

    public boolean isAllowInputFromOutputSideFluids() {
        return this.allowInputFromOutputSideFluids;
    }

    public void clearMachineInventory(NonNullList<ItemStack> itemBuffer) {
        super.clearMachineInventory(itemBuffer);
        //clearInventory(itemBuffer, this.chargerInventory);
    }
    protected ModularUI.Builder createGuiTemplate(EntityPlayer player) {

        RecipeMap<?> workableRecipeMap = this.workable.getRecipeMap();
        int yOffset = 0;
        if (workableRecipeMap.getMaxInputs() >= 6 || workableRecipeMap.getMaxFluidInputs() >= 6 || workableRecipeMap.getMaxOutputs() >= 6 || workableRecipeMap.getMaxFluidOutputs() >= 6) {
            yOffset = 9;
        }

        AbstractRecipeLogic var10001 = this.workable;
        Objects.requireNonNull(var10001);

        ModularUI.Builder var10000 = workableRecipeMap
                .createUITemplate(var10001::getProgressPercent, this.importItems, this.exportItems, this.importFluids, this.exportFluids, yOffset)
                .widget(new LabelWidget(5, 5, this.getMetaFullName()));

        // 反射修改背景纹理
        try {
            Field backgroundField = ModularUI.Builder.class.getDeclaredField("background");
            backgroundField.setAccessible(true);

            IGuiTexture customBackground = GuiTextures.BACKGROUND_STEAM.get(
                    true);
            backgroundField.set(var10000, customBackground);

        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to modify ModularUI background", e);
        }

        ImageWidget var8 = (new ImageWidget(79, 42 + yOffset, 18, 18, GuiTextures.INDICATOR_NO_ENERGY)).setIgnoreColor(true);
        AbstractRecipeLogic var10002 = this.workable;
        Objects.requireNonNull(var10002);

        ModularUI.Builder builder = var10000.widget(var8.setPredicate(var10002::isHasNotEnoughEnergy)).bindPlayerInventory(player.inventory, GuiTextures.SLOT_STEAM.get(true), yOffset);
        int leftButtonStartX = 7;
        if (this.exportItems.getSlots() > 0) {
            builder.widget((new ToggleButtonWidget(leftButtonStartX, 62 + yOffset, 18, 18, GuiTextures.BUTTON_ITEM_OUTPUT, this::isAutoOutputItems, this::setAutoOutputItems)).setTooltipText("gregtech.gui.item_auto_output.tooltip").shouldUseBaseBackground());
            leftButtonStartX += 18;
        }

        if (this.exportFluids.getTanks() > 0) {
            builder.widget((new ToggleButtonWidget(leftButtonStartX, 62 + yOffset, 18, 18, GuiTextures.BUTTON_FLUID_OUTPUT, this::isAutoOutputFluids, this::setAutoOutputFluids)).setTooltipText("gregtech.gui.fluid_auto_output.tooltip").shouldUseBaseBackground());
            leftButtonStartX += 18;
        }

        if (this.exportItems.getSlots() + this.exportFluids.getTanks() <= 9) {
            if (this.circuitInventory != null) {
                SlotWidget circuitSlot = (new GhostCircuitSlotWidget(this.circuitInventory, 0, leftButtonStartX, 62 + yOffset)).setBackgroundTexture(GuiTextures.SLOT_STEAM.get(true), this.getCircuitSlotOverlay());
                builder.widget(circuitSlot.setConsumer(this::getCircuitSlotTooltip));
            }
        }

        return builder;
    }

    public boolean hasGhostCircuitInventory() {
        return true;
    }

    protected TextureArea getCircuitSlotOverlay() {
        return GuiTextures.INT_CIRCUIT_OVERLAY;
    }

    protected void getCircuitSlotTooltip(SlotWidget widget) {
        String configString;
        if (this.circuitInventory != null && this.circuitInventory.getCircuitValue() != -1) {
            configString = String.valueOf(this.circuitInventory.getCircuitValue());
        } else {
            configString = (new TextComponentTranslation("gregtech.gui.configurator_slot.no_value", new Object[0])).getFormattedText();
        }

        widget.setTooltipText("gregtech.gui.configurator_slot.tooltip", new Object[]{configString});
    }

    protected ModularUI createUI(EntityPlayer entityPlayer) {
        return this.createGuiTemplate(entityPlayer).build(this.getHolder(), entityPlayer);
    }

    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        tooltip.add("可接受RU方向:"+getAcceptFacingString());
        tooltip.add(I18n.format("drtech.universal.tooltip.voltage_in", this.getTier(), GTValues.VNF[this.getTier()]));
        if (this.workable.getRecipeMap().getMaxFluidInputs() != 0) {
            tooltip.add(I18n.format("gregtech.universal.tooltip.fluid_storage_capacity", this.getTankScalingFunction().apply(this.getTier())));
        }
        String key = this.metaTileEntityId.getPath().split("\\.")[0];
        String mainKey = String.format("gregtech.machine.%s.tooltip", key);
        if (I18n.hasKey(mainKey)) {
            tooltip.add(1, mainKey);
        }

    }
    private String getAcceptFacingString()
    {
        StringBuilder res= new StringBuilder();
        for (var s: acceptFacing)
        {
            switch (s){
                case UP -> res.append("上/");
                case DOWN -> res.append("下/");
                case LEFT -> res.append("左/");
                case RIGHT -> res.append("右/");
                case FRONT -> res.append("前/");
                case BACK -> res.append("后/");
            }
        }
        return res.toString();
    }
    public boolean needsSneakToRotate() {
        return true;
    }

    public void addToolUsages(ItemStack stack, @Nullable World world, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gregtech.tool_action.screwdriver.auto_output_covers", new Object[0]));
        tooltip.add(I18n.format("gregtech.tool_action.wrench.set_facing", new Object[0]));
        tooltip.add(I18n.format("gregtech.tool_action.soft_mallet.reset", new Object[0]));
        super.addToolUsages(stack, world, tooltip, advanced);
    }


    
}
