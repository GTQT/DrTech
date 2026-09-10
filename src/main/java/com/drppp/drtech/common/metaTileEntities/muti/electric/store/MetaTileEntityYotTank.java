package com.drppp.drtech.common.metaTileEntities.muti.electric.store;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;

import com.cleanroommc.modularui.utils.serialization.ByteBufAdapters;
import com.drppp.drtech.common.blocks.BlocksInit;
import com.drppp.drtech.common.blocks.metaBlocks.MetaCasing;
import com.drppp.drtech.client.Textures;
import com.drppp.drtech.common.metaTileEntities.muti.mutipart.MetaTileEntityYotHatch;
import com.drppp.drtech.api.utils.Datas;
import com.drppp.drtech.api.utils.DrtechUtils;
import com.drppp.drtech.api.capability.DrtechCapabilities;
import gregtech.api.capability.*;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.ClickButtonWidget;
import gregtech.api.gui.widgets.WidgetGroup;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.*;
import gregtech.api.metatileentity.multiblock.ui.MultiblockUIBuilder;
import gregtech.api.pattern.FormedStructureView;
import gregtech.api.pattern.StructureContributionKey;
import gregtech.api.pattern.element.Elements;
import gregtech.api.pattern.element.IStructureElement;
import gregtech.api.pattern.element.StructureDefinition;
import gregtech.api.unification.material.Materials;
import gregtech.api.util.BlockInfo;
import gregtech.api.util.GTTransferUtils;
import gregtech.api.util.KeyUtil;
import gregtech.api.util.TextFormattingUtil;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.common.blocks.BlockGlassCasing;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.math.BigInteger;
import java.util.*;

import static gregtech.api.util.RelativeDirection.*;

import gregtech.api.pattern.casing.DeclarativePatternBuilder;

import gregtech.api.metatileentity.multiblock.MultiblockAbility;

public class MetaTileEntityYotTank extends MultiblockWithDisplayBase implements IControllable, IProgressBarMultiblock {

    private static final String NBT_FLUID_BANK = "EnergyBank";
    private static final String NBT_FLUID = "Fluid";

    private static final String NBT_KEY_ACTIVE = "isActive";
    private static final String NBT_KEY_WORKING = "isWorkingEnabled";
    private static final String NBT_KEY_OUTPUT_FLAG = "OutFlag";

    private boolean isActive;
    private boolean isWorkingEnabled = true;
    private int outputflag = 0;
    private int time = 0;

    private FluidStack fluid;
    public IMultipleTankHandler inputFluidInventory;
    public IMultipleTankHandler outputFluidInventory;
    private YotTankFluidBank fluidBank;

    protected final ArrayList<MetaTileEntityYotHatch> mYottaHatch = new ArrayList<>();

    public MetaTileEntityYotTank(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    public FluidStack getFluid() {
        return fluid;
    }

    public void setFluid(FluidStack fluid) {
        this.fluid = fluid;
    }

    public YotTankFluidBank getFluidBank() {
        return fluidBank;
    }

    // ---------------------------------------------------------------------
    // Working / active state
    // ---------------------------------------------------------------------

    @Override
    public boolean isWorkingEnabled() {
        return isWorkingEnabled;
    }

    @Override
    public boolean usesMui2() {
        return false;
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
        return super.isActive() && isActive;
    }

    public void setActive(boolean active) {
        if (this.isActive == active) {
            return;
        }
        this.isActive = active;
        markDirty();
        World world = getWorld();
        if (world != null && !world.isRemote) {
            writeCustomData(GregtechDataCodes.WORKABLE_ACTIVE, buf -> buf.writeBoolean(active));
        }
    }

    // ---------------------------------------------------------------------
    // Sync
    // ---------------------------------------------------------------------

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

    // ---------------------------------------------------------------------
    // NBT
    // ---------------------------------------------------------------------

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setBoolean(NBT_KEY_ACTIVE, isActive);
        data.setBoolean(NBT_KEY_WORKING, isWorkingEnabled);
        data.setInteger(NBT_KEY_OUTPUT_FLAG, outputflag);
        if (fluid == null) {
            return data;
        }
        NBTTagCompound fluidNBT = new NBTTagCompound();
        fluid.writeToNBT(fluidNBT);
        data.setTag(NBT_FLUID, fluidNBT);
        if (fluidBank != null) {
            data.setTag(NBT_FLUID_BANK, fluidBank.writeToNBT(new NBTTagCompound()));
        }
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        isActive = data.getBoolean(NBT_KEY_ACTIVE);
        isWorkingEnabled = data.getBoolean(NBT_KEY_WORKING);
        outputflag = data.getInteger(NBT_KEY_OUTPUT_FLAG);
        if (!data.hasKey(NBT_FLUID)) {
            return;
        }
        fluid = FluidStack.loadFluidStackFromNBT((NBTTagCompound) data.getTag(NBT_FLUID));
        if (data.hasKey(NBT_FLUID_BANK)) {
            fluidBank = new YotTankFluidBank(data.getCompoundTag(NBT_FLUID_BANK));
        }
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (capability == GregtechTileCapabilities.CAPABILITY_CONTROLLABLE) {
            return GregtechTileCapabilities.CAPABILITY_CONTROLLABLE.cast(this);
        }
        return super.getCapability(capability, side);
    }

    // ---------------------------------------------------------------------
    // Tick logic
    // ---------------------------------------------------------------------

    @Override
    protected void updateFormedValid() {
        if (getWorld().isRemote) {
            return;
        }

        if (getOffsetTimer() % 20 == 0) {
            // active here is just used for rendering
            setActive(fluidBank.hasFluid());
        }

        time++;
        if (!isWorkingEnabled() || time <= 20) {
            return;
        }

        importFluids();
        exportFluids();

        if (!fluidBank.hasFluid()) {
            fluid = null;
        }
        time = 0;
    }

    private void importFluids() {
        if (inputFluidInventory.getTanks() <= 0) {
            return;
        }
        for (int i = 0; i < inputFluidInventory.getTanks(); i++) {
            IMultipleTankHandler.ITankEntry tank = inputFluidInventory.getTankAt(i);
            if (fluid != null && !fluid.isFluidEqual(tank.getFluid())) {
                continue;
            }
            if (fluid == null) {
                fluid = tank.getFluid();
            }
            long amount = fluidBank.fill(tank.getFluidAmount());
            tank.drain((int) amount, true);
        }
    }

    private void exportFluids() {
        if (outputFluidInventory.getTanks() <= 0 || fluid == null || outputflag != 1) {
            return;
        }
        List<FluidStack> outputs = new ArrayList<>();
        for (int i = 0; i < outputFluidInventory.getTanks(); i++) {
            IMultipleTankHandler.ITankEntry tank = outputFluidInventory.getTankAt(i);
            if (tank.getFluid() != null && !tank.getFluid().isFluidEqual(fluid)) {
                continue;
            }
            long drained = fluidBank.drain(tank.getCapacity() - tank.getFluidAmount());
            outputs.add(new FluidStack(fluid.getFluid(), (int) drained));
        }
        GTTransferUtils.addFluidsToFluidHandler(outputFluidInventory, false, outputs);
    }

    // ---------------------------------------------------------------------
    // GUI
    // ---------------------------------------------------------------------

    @Override
    @Nonnull
    protected Widget getFlexButton(int x, int y, int width, int height) {
        WidgetGroup group = new WidgetGroup(x, y, width, height);
        group.addWidget(new ClickButtonWidget(0, 0, 18, 9, "", this::clearFluid)
                .setButtonTexture(GuiTextures.BUTTON_CLEAR_GRID)
                .setTooltipText("gtqtcore.multiblock.tfft.clearfluid"));
        group.addWidget(new ClickButtonWidget(0, 9, 18, 9, "", this::setoutputFlag)
                .setButtonTexture(GuiTextures.LOCK)
                .setTooltipText("gtqtcore.multiblock.tfft.isoutput"));
        return group;
    }

    private void setoutputFlag(Widget.ClickData clickData) {
        outputflag = outputflag == 0 ? 1 : 0;
    }

    private void clearFluid(Widget.ClickData clickData) {
        fluid = null;
        fluidBank.clearStore();
    }

    // ---------------------------------------------------------------------
    // Structure
    // ---------------------------------------------------------------------

    private static final StructureContributionKey<IStoreData, List<IStoreData>> BATTERY_KEY =
            StructureContributionKey.orderedList("drtech:yot_storage_cells");

    private static final IStructureElement<?> BATTERY_ELEMENT = new BatteryContributionElement<Object> (
            "drtech:yot_storage_cells",
            state -> {
                if (!Datas.YOT_CASINGS.containsKey(state)) return null;
                IStoreData data = Datas.YOT_CASINGS.get(state);
                if (data.getTier() == -1 || data.getCapacity().compareTo(BigInteger.ZERO) <= 0) return null;
                return data;
            },
            () -> Datas.YOT_CASINGS.entrySet().stream()
                    .sorted(Comparator.comparingInt(e -> e.getValue().getTier()))
                    .map(e -> new BlockInfo(e.getKey(), null))
                    .toArray(BlockInfo[]::new));

    @NotNull
    private static final StructureDefinition<?> STRUCTURE_DEFINITION =
            StructureDefinition.getOrBuild("drtech:yot_tank", MetaTileEntityYotTank::buildTemplate);

    @Override
    protected StructureDefinition<?> createStructureDefinition() {
        return STRUCTURE_DEFINITION;
    }

    private static StructureDefinition<?> buildTemplate() {
        return DeclarativePatternBuilder.start(RIGHT, FRONT, UP)
                .piece("start")
                .aisle("#####", "#XXX#", "#XXX#", "#XXX#", "#####")
                .aisle("XXSXX", "XCCCX", "XCCCX", "XCCCX", "XXXXX")
                .repeatablePiece("body", 1, 14)
                .aisle("GGGGG", "GBBBG", "GBBBG", "GBBBG", "GGGGG")
                .piece("end")
                .aisle("XXXXX", "XXXXX", "XXXXX", "XXXXX", "XXXXX")
                .aisle("LLLLL", "L###L", "L###L", "L###L", "LLLLL")
                .self('S', MetaTileEntityYotTank.class)
                .any('#')
                .blocks('C', getCasingState())
                .where('X', Elements.chain(
                        Elements.counted(0, 4096, Elements.block(getCasingState())),
                        Elements.hatch(MultiblockAbility.MAINTENANCE_HATCH,
                                gregtech.common.ConfigHolder.machines.enableMaintenance ? 1 : 0, 1),
                        Elements.hatch(MultiblockAbility.MUFFLER_HATCH, 1, 1),
                        Elements.hatch(MultiblockAbility.IMPORT_FLUIDS, 0, 2, 1),
                        Elements.hatch(MultiblockAbility.EXPORT_FLUIDS, 0, 2, 1),
                        Elements.hatch(DrtechCapabilities.YOT_HATCH, 0, 1)))
                .blocks('G', getGlassState())
                .frames('L', Materials.Steel)
                .where('B', Elements.withTooltips(BATTERY_ELEMENT, "gregtech.multiblock.pattern.error.batteries"))
                .buildStructureDefinition();
    }

    protected static IBlockState getCasingState() {
        return BlocksInit.COMMON_CASING.getState(MetaCasing.MetalCasingType.YOT_TANK_CASING);
    }

    protected static IBlockState getGlassState() {
        return MetaBlocks.TRANSPARENT_CASING.getState(BlockGlassCasing.CasingType.FUSION_GLASS);
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return Textures.YOT_TANK_CASING;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, @NotNull List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gregtech.machine.yot_tank.tooltip1"));
        tooltip.add(I18n.format("gregtech.machine.yot_tank.tooltip2"));
    }

    // ---------------------------------------------------------------------
    // Display
    // ---------------------------------------------------------------------

    @Override
    protected void configureDisplayText(MultiblockUIBuilder builder) {
        super.configureDisplayText(builder);
        builder.setWorkingStatus(true, isActive() && isWorkingEnabled())
                .setWorkingStatusKeys(
                        "gregtech.multiblock.idling",
                        "gregtech.multiblock.idling",
                        "gregtech.machine.active_transformer.routing")
                .addCustom((keyManager, syncer) -> {
                    if (!isStructureFormed()) {
                        return;
                    }
                    boolean hasBank = syncer.syncBoolean(() -> fluidBank != null);
                    String storedText = syncer.<String>syncObject(
                            () -> fluidBank == null ? "0 L" : TextFormattingUtil.formatNumbers(fluidBank.getStored()) + " L",
                            ByteBufAdapters.STRING);
                    String capacityText = syncer.<String>syncObject(
                            () -> fluidBank == null ? "0 L" : TextFormattingUtil.formatNumbers(fluidBank.getCapacity()) + " L",
                            ByteBufAdapters.STRING);
                    String fluidName = syncer.<String>syncObject(
                            () -> fluid == null ? "空" : fluid.getLocalizedName(),
                            ByteBufAdapters.STRING);
                    int output = syncer.syncInt(() -> outputflag);

                    keyManager.add(richText -> {
                        if (!hasBank) {
                            return;
                        }
                        richText.add(KeyUtil.lang(TextFormatting.GRAY,
                                        "gregtech.multiblock.power_substation.stored",
                                        KeyUtil.string(TextFormatting.GOLD, storedText)))
                                .newLine();
                        richText.add(KeyUtil.lang(TextFormatting.GRAY,
                                        "gregtech.multiblock.power_substation.capacity",
                                        KeyUtil.string(TextFormatting.GOLD, capacityText)))
                                .newLine();
                        richText.add(KeyUtil.lang(TextFormatting.GOLD, "drtech.multiblock.yot_tank.fluid_type",
                                        KeyUtil.string(TextFormatting.WHITE, fluidName)))
                                .newLine();
                        richText.add(KeyUtil.lang(TextFormatting.GRAY, "drtech.multiblock.power_substation.output",
                                        KeyUtil.string(TextFormatting.WHITE, output == 0 ? "禁用" : "启用")))
                                .newLine();
                    });
                })
                .addWorkingStatusLine();
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        getFrontOverlay().renderOrientedState(renderState, translation, pipeline, getFrontFacing(),
                isActive(), isWorkingEnabled());
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityYotTank(metaTileEntityId);
    }

    // ---------------------------------------------------------------------
    // Structure lifecycle
    // ---------------------------------------------------------------------

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        resetTileAbilities();
    }

    @Override
    protected void formStructure(@NotNull FormedStructureView formed) {
        super.formStructure(formed);
        initializeAbilities();

        List<IStoreData> aggregate = formed.getAggregate(BATTERY_KEY);
        List<IStoreData> parts = aggregate == null ? new ArrayList<>() : new ArrayList<>(aggregate);

        // 关联结构内的 Yot 仓口（旧机制经 matchContext "Multi" 集合并入，现改由成型部件列表获得）
        for (IMultiblockPart part : formed.getParts()) {
            if (part instanceof MetaTileEntityYotHatch) {
                ((MetaTileEntityYotHatch) part).setYotTank(this);
            }
        }

        if (parts.isEmpty()) {
            invalidateStructure();
            return;
        }

        fluidBank = (fluidBank == null) ? new YotTankFluidBank(parts) : fluidBank.rebuild(parts);
    }

    private void initializeAbilities() {
        inputFluidInventory = new FluidTankList(true, getAbilities(MultiblockAbility.IMPORT_FLUIDS));
        outputFluidInventory = new FluidTankList(true, getAbilities(MultiblockAbility.EXPORT_FLUIDS));
    }

    private void resetTileAbilities() {
        inputFluidInventory = new FluidTankList(true);
        outputFluidInventory = new FluidTankList(true);
    }

    @Override
    public double getFillPercentage(int i) {
        return 0;
    }

    // ---------------------------------------------------------------------
    // Fluid bank
    // ---------------------------------------------------------------------

    public static class YotTankFluidBank {

        private static final String NBT_SIZE = "Size";
        private static final String NBT_STORED = "Stored";
        private static final String NBT_MAX = "Max";

        private final BigInteger[] storage;
        private final BigInteger[] maximums;
        private final BigInteger capacity;
        private int index;

        public YotTankFluidBank(List<IStoreData> batteries) {
            int size = batteries.size();
            storage = new BigInteger[size];
            maximums = new BigInteger[size];
            for (int i = 0; i < size; i++) {
                storage[i] = BigInteger.ZERO;
                maximums[i] = batteries.get(i).getCapacity();
            }
            capacity = summarize(maximums);
        }

        public YotTankFluidBank(NBTTagCompound storageTag) {
            int size = storageTag.getInteger(NBT_SIZE);
            storage = new BigInteger[size];
            maximums = new BigInteger[size];
            for (int i = 0; i < size; i++) {
                storage[i] = BigInteger.ZERO;
                NBTTagCompound subtag = storageTag.getCompoundTag(String.valueOf(i));
                if (subtag.hasKey(NBT_STORED)) {
                    storage[i] = new BigInteger(subtag.getString(NBT_STORED));
                }
                maximums[i] = new BigInteger(subtag.getString(NBT_MAX));
            }
            capacity = summarize(maximums);
        }

        // Persist bank state
        private NBTTagCompound writeToNBT(NBTTagCompound compound) {
            compound.setInteger(NBT_SIZE, storage.length);
            for (int i = 0; i < storage.length; i++) {
                NBTTagCompound subtag = new NBTTagCompound();
                if (storage[i].signum() > 0) {
                    subtag.setString(NBT_STORED, storage[i].toString());
                }
                subtag.setString(NBT_MAX, maximums[i].toString());
                compound.setTag(String.valueOf(i), subtag);
            }
            return compound;
        }

        /**
         * Rebuild the power storage with a new list of batteries.
         * Will use existing stored power and try to map it onto new batteries.
         * If there was more power before the rebuild operation, it will be lost.
         */
        public YotTankFluidBank rebuild(@NotNull List<IStoreData> batteries) {
            if (batteries.isEmpty()) {
                throw new IllegalArgumentException("Cannot rebuild Power Substation power bank with no batteries!");
            }
            YotTankFluidBank newStorage = new YotTankFluidBank(batteries);
            for (BigInteger stored : storage) {
                newStorage.fill(stored);
            }
            return newStorage;
        }

        /** @return Amount filled into storage */
        public long fill(long amount) {
            if (amount < 0) {
                throw new IllegalArgumentException("Amount cannot be negative!");
            }
            if (index != storage.length - 1 && storage[index].compareTo(maximums[index]) == 0) {
                index++;
            }
            BigInteger maxFill = DrtechUtils.getBigIntegerMin(
                    maximums[index].subtract(storage[index]), BigInteger.valueOf(amount));
            if (maxFill.signum() == 0 && index == storage.length - 1) {
                return 0;
            }
            storage[index] = storage[index].add(maxFill);
            amount -= maxFill.longValue();
            if (amount > 0 && index != storage.length - 1) {
                return maxFill.longValue() + fill(amount);
            }
            return maxFill.longValue();
        }

        public BigInteger fill(BigInteger amount) {
            if (amount.signum() < 0) {
                throw new IllegalArgumentException("Amount cannot be negative!");
            }
            if (index != storage.length - 1 && storage[index].compareTo(maximums[index]) == 0) {
                index++;
            }
            BigInteger maxFill = DrtechUtils.getBigIntegerMin(maximums[index].subtract(storage[index]), amount);
            if (maxFill.signum() == 0 && index == storage.length - 1) {
                return BigInteger.ZERO;
            }
            storage[index] = storage[index].add(maxFill);
            amount = amount.subtract(maxFill);
            if (amount.signum() > 0 && index != storage.length - 1) {
                return maxFill.add(fill(amount));
            }
            return maxFill;
        }

        /** @return Amount drained from storage */
        public long drain(long amount) {
            if (amount < 0) {
                throw new IllegalArgumentException("Amount cannot be negative!");
            }

            // ensure index
            if (index != 0 && storage[index].signum() == 0) {
                index--;
            }

            BigInteger maxDrain = DrtechUtils.getBigIntegerMin(storage[index], BigInteger.valueOf(amount));

            // storage is completely empty
            if (maxDrain.signum() == 0 && index == 0) {
                return 0;
            }

            // drain this "battery" as much as possible
            storage[index] = storage[index].subtract(maxDrain);
            amount -= maxDrain.longValue();

            // try to drain other "batteries" if necessary
            if (amount > 0 && index != 0) {
                index--;
                return maxDrain.longValue() + drain(amount);
            }

            // other drain not necessary, either because the storage is now completely empty,
            // or we were able to drain all the energy from this "battery"
            return maxDrain.longValue();
        }

        public BigInteger getCapacity() {
            return capacity;
        }

        public BigInteger getStored() {
            return summarize(storage);
        }

        public boolean hasFluid() {
            for (BigInteger l : storage) {
                if (l.signum() > 0) {
                    return true;
                }
            }
            return false;
        }

        private static BigInteger summarize(BigInteger[] values) {
            BigInteger retVal = BigInteger.ZERO;
            for (BigInteger value : values) {
                if (value != null) {
                    retVal = retVal.add(value);
                }
            }
            return retVal;
        }

        public void clearStore() {
            Arrays.fill(storage, BigInteger.ZERO);
        }
    }
}