package com.drppp.drtech.common.metaTileEntities.muti.electric.store;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.utils.serialization.ByteBufAdapters;
import com.drppp.drtech.common.blocks.BlocksInit;
import com.drppp.drtech.common.blocks.metaBlocks.MetaCasing;
import com.drppp.drtech.client.Textures;
import com.drppp.drtech.api.utils.Datas;
import gregtech.api.capability.*;
import gregtech.api.capability.impl.EnergyContainerList;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.capability.impl.ItemHandlerList;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.Widget;
import gregtech.api.gui.resources.TextureArea;
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
import gregtech.api.util.BlockInfo;
import gregtech.api.util.GTTransferUtils;
import gregtech.api.util.KeyUtil;
import gregtech.api.util.TextComponentUtil;
import gregtech.api.util.TextFormattingUtil;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.utils.TooltipHelper;
import gregtech.common.blocks.BlockGlassCasing;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
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

public class MetatileEntityTwentyFiveFluidTank extends MultiblockWithDisplayBase implements IControllable, IProgressBarMultiblock {

    public static final int FLUID_SLOTS = 25;
    public static final int DISPLAY_SLOTS = 5;

    private static final String NBT_FLUID_BANK = "FluidBank";
    private static final String NBT_FLUID = "Fluid";

    private static final String NBT_KEY_ACTIVE = "isActive";
    private static final String NBT_KEY_WORKING = "isWorkingEnabled";
    private static final String NBT_KEY_OUTPUT_FLAG = "OutFlag";
    private static final String NBT_KEY_CIRCUIT = "Circuit";

    private boolean isActive;
    private boolean isWorkingEnabled = true;
    private int circuit = 0;
    private int time = 0;
    private int outputflag = 0;

    private final FluidStack[] fluid = new FluidStack[FLUID_SLOTS];

    public IMultipleTankHandler inputFluidInventory;
    public IMultipleTankHandler outputFluidInventory;
    protected ItemHandlerList itemImportInventory;
    protected IEnergyContainer energyContainer;
    private TFFTTankFluidBank fluidBank;

    public MetatileEntityTwentyFiveFluidTank(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    public int getCircuitNo() {
        return circuit;
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
        data.setInteger(NBT_KEY_CIRCUIT, circuit);

        for (int i = 0; i < FLUID_SLOTS; i++) {
            if (fluid[i] == null) {
                continue;
            }
            NBTTagCompound fluidNBT = new NBTTagCompound();
            fluid[i].writeToNBT(fluidNBT);
            data.setTag(NBT_FLUID + i, fluidNBT);
        }
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
        circuit = data.getInteger(NBT_KEY_CIRCUIT);

        for (int i = 0; i < FLUID_SLOTS; i++) {
            if (!data.hasKey(NBT_FLUID + i)) {
                continue;
            }
            fluid[i] = FluidStack.loadFluidStackFromNBT((NBTTagCompound) data.getTag(NBT_FLUID + i));
        }
        if (data.hasKey(NBT_FLUID_BANK)) {
            fluidBank = new TFFTTankFluidBank(data.getCompoundTag(NBT_FLUID_BANK));
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

        updateActiveState();
        consumeEnergy();

        if (!isWorkingEnabled() || time++ <= 20) {
            return;
        }

        importFluids();
        exportFluids();

        if (!fluidBank.hasFluid(circuit)) {
            fluid[circuit] = null;
        }
        time = 0;
    }

    private void updateActiveState() {
        if (getOffsetTimer() % 20 != 0) {
            return;
        }
        boolean anyStored = false;
        for (int i = 0; i < FLUID_SLOTS; i++) {
            if (fluidBank.hasFluid(i)) {
                anyStored = true;
                break;
            }
        }
        setActive(anyStored);
    }

    private void consumeEnergy() {
        if (energyContainer == null || fluidBank == null) {
            return;
        }
        long availableInput = energyContainer.getInputVoltage() * energyContainer.getInputAmperage();
        if (availableInput > fluidBank.eut && energyContainer.getEnergyStored() > fluidBank.eut) {
            energyContainer.changeEnergy(-fluidBank.eut);
            setWorkingEnabled(true);
        }
    }

    private void importFluids() {
        if (inputFluidInventory.getTanks() <= 0) {
            return;
        }
        for (int i = 0; i < inputFluidInventory.getTanks(); i++) {
            IMultipleTankHandler.ITankEntry tank = inputFluidInventory.getTankAt(i);
            if (tank.getFluidAmount() <= 0) {
                continue;
            }
            // Fill matching existing slots first
            for (int j = 0; j < FLUID_SLOTS; j++) {
                if (fluid[j] != null && fluid[j].isFluidEqual(tank.getFluid())) {
                    long amount = fluidBank.fill(tank.getFluidAmount(), j);
                    tank.drain((int) amount, true);
                    if (tank.getFluidAmount() == 0) {
                        break;
                    }
                }
            }
            // Then fill empty slots
            for (int j = 0; j < FLUID_SLOTS; j++) {
                if (fluid[j] == null) {
                    fluid[j] = tank.getFluid();
                    long amount = fluidBank.fill(tank.getFluidAmount(), j);
                    tank.drain((int) amount, true);
                    if (tank.getFluidAmount() == 0) {
                        break;
                    }
                }
            }
        }
    }

    private void exportFluids() {
        if (outputFluidInventory.getTanks() <= 0 || fluid[circuit] == null || outputflag != 1) {
            return;
        }
        List<FluidStack> outputs = new ArrayList<>();
        for (int i = 0; i < outputFluidInventory.getTanks(); i++) {
            IMultipleTankHandler.ITankEntry tank = outputFluidInventory.getTankAt(i);
            boolean empty = tank.getFluid() == null;
            boolean matching = fluid[circuit].isFluidEqual(tank.getFluid());
            if (!empty && !matching) {
                continue;
            }
            long drained = fluidBank.drain(tank.getCapacity() - tank.getFluidAmount(), circuit);
            outputs.add(new FluidStack(fluid[circuit].getFluid(), (int) drained));
        }
        GTTransferUtils.addFluidsToFluidHandler(outputFluidInventory, false, outputs);
    }

    // ---------------------------------------------------------------------
    // Structure
    // ---------------------------------------------------------------------

    private static final StructureContributionKey<ITFFTData, List<ITFFTData>> BATTERY_KEY =
            StructureContributionKey.orderedList("drtech:tfft_battery_cells");

    private static final IStructureElement<?> BATTERY_ELEMENT = new BatteryContributionElement<Object>(
            "drtech:tfft_battery_cells",
            state -> {
                if (!Datas.TFFT_CASINGS.containsKey(state)) return null;
                ITFFTData data = Datas.TFFT_CASINGS.get(state);
                if (data.getTier() == -1 || data.getCapacity() <= 0) return null;
                return data;
            },
            () -> Datas.TFFT_CASINGS.entrySet().stream()
                    .sorted(Comparator.comparingInt(e -> e.getValue().getTier()))
                    .map(e -> new BlockInfo(e.getKey(), null))
                    .toArray(BlockInfo[]::new));

    @NotNull
    private static final StructureDefinition<?> STRUCTURE_DEFINITION =
            StructureDefinition.getOrBuild("drtech:tfft_tank", MetatileEntityTwentyFiveFluidTank::buildTemplate);

    @Override
    protected @NotNull StructureDefinition<?> createStructureDefinition() {
        return STRUCTURE_DEFINITION;
    }

    private static StructureDefinition<?> buildTemplate() {
        return DeclarativePatternBuilder.start(RIGHT, DOWN, FRONT)
                .piece("start")
                .aisle("XXXXX", "XXXXX", "XXSXX", "XXXXX", "XXXXX")
                .repeatablePiece("body", 3, 14)
                .aisle("GGGGG", "GBBBG", "GBBBG", "GBBBG", "GGGGG")
                .piece("end")
                .aisle("XXXXX", "XXXXX", "XXXXX", "XXXXX", "XXXXX")
                .self('S', MetatileEntityTwentyFiveFluidTank.class)
                .where('X', Elements.chain(
                        Elements.counted(0, 4096, Elements.block(getCasingState())),
                        Elements.hatch(MultiblockAbility.MAINTENANCE_HATCH,
                                gregtech.common.ConfigHolder.machines.enableMaintenance ? 1 : 0, 1),
                        Elements.hatch(MultiblockAbility.MUFFLER_HATCH, 1, 1),
                        Elements.hatch(MultiblockAbility.IMPORT_FLUIDS, 0, 2, 1),
                        Elements.hatch(MultiblockAbility.EXPORT_FLUIDS, 0, 2, 1),
                        Elements.hatch(MultiblockAbility.IMPORT_ITEMS, 1, 1),
                        Elements.hatch(MultiblockAbility.INPUT_ENERGY, 1, -1, 1)))
                .blocks('G', getGlassState())
                .where('B', Elements.withTooltips(BATTERY_ELEMENT,
                        "gregtech.multiblock.pattern.error.batteries"))
                .buildStructureDefinition();
    }

    protected static IBlockState getCasingState() {
        return BlocksInit.COMMON_CASING.getState(MetaCasing.MetalCasingType.TFFT_CASING);
    }

    protected static IBlockState getGlassState() {
        return MetaBlocks.TRANSPARENT_CASING.getState(BlockGlassCasing.CasingType.LAMINATED_GLASS);
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return Textures.TFFT_TANK_CASING;
    }

    @SideOnly(Side.CLIENT)
    @NotNull
    @Override
    protected ICubeRenderer getFrontOverlay() {
        return Textures.TFFT_OVERLAY;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, @NotNull List<String> tooltip, boolean advanced) {
        tooltip.add(TooltipHelper.RAINBOW_SLOW + I18n.format("超超超超级量子缸！", new Object[0]));
        tooltip.add(I18n.format("能存储25种流体的超级储罐，容量由多方块内的流体单元决定"));
        tooltip.add(I18n.format("在UI中通过按钮进行流体操作"));
    }

    // ---------------------------------------------------------------------
    // Display
    // ---------------------------------------------------------------------

    @Override
    protected void configureDisplayText(MultiblockUIBuilder builder) {
        super.configureDisplayText(builder);
        builder.setWorkingStatus(true, isActive() && isWorkingEnabled())
                .addCustom((keyManager, syncer) -> {
                    if (!isStructureFormed()) {
                        return;
                    }
                    boolean hasBank = syncer.syncBoolean(() -> fluidBank != null);
                    int syncedCircuit = syncer.syncInt(() -> circuit);
                    int output = syncer.syncInt(() -> outputflag);
                    int eut = syncer.syncInt(() -> fluidBank == null ? 0 : fluidBank.eut);
                    String capacityText = syncer.<String>syncObject(
                            () -> fluidBank == null ? "0 L"
                                    : TextFormattingUtil.formatNumbers(fluidBank.getCapacity(syncedCircuit)) + " L",
                            ByteBufAdapters.STRING);

                    int[] slotIndex = new int[DISPLAY_SLOTS];
                    String[] slotFluid = new String[DISPLAY_SLOTS];
                    String[] slotStored = new String[DISPLAY_SLOTS];
                    for (int j = 0; j < DISPLAY_SLOTS; j++) {
                        final int idx = syncedCircuit - 2 + j;
                        slotIndex[j] = idx;
                        slotFluid[j] = syncer.<String>syncObject(() -> {
                            if (idx < 0 || idx >= FLUID_SLOTS || fluid[idx] == null) {
                                return "空";
                            }
                            return fluid[idx].getLocalizedName();
                        }, ByteBufAdapters.STRING);
                        slotStored[j] = syncer.<String>syncObject(() -> {
                            if (idx < 0 || idx >= FLUID_SLOTS || fluidBank == null) {
                                return "";
                            }
                            return TextFormattingUtil.formatNumbers(fluidBank.getStored(idx)) + " L";
                        }, ByteBufAdapters.STRING);
                    }

                    keyManager.add(richText -> {
                        if (!hasBank) {
                            return;
                        }
                        richText.add(KeyUtil.lang(TextFormatting.GRAY,
                                        "gtqt.multiblock.power_substation.stored",
                                        KeyUtil.string(TextFormatting.GOLD, capacityText)))
                                .newLine();
                        richText.add(KeyUtil.lang(TextFormatting.GRAY,
                                        "drtech.multiblock.power_substation.eut",
                                        KeyUtil.string(TextFormatting.AQUA, String.valueOf(eut))))
                                .newLine();
                        richText.add(KeyUtil.lang(TextFormatting.GRAY,
                                        "drtech.multiblock.power_substation.output",
                                        KeyUtil.string(TextFormatting.WHITE, output == 0 ? "禁用" : "启用")))
                                .newLine();
                        richText.add(IKey.str(TextFormatting.GOLD + "======================")).newLine();
                        for (int j = 0; j < DISPLAY_SLOTS; j++) {
                            int idx = slotIndex[j];
                            if (idx < 0 || idx >= FLUID_SLOTS) {
                                continue;
                            }
                            TextFormatting color = idx == syncedCircuit ? TextFormatting.GOLD : TextFormatting.GRAY;
                            richText.add(KeyUtil.lang(color, "gtqt.multiblock.yot_tank.fluid_type",
                                            KeyUtil.string(TextFormatting.WHITE, String.valueOf(idx)),
                                            KeyUtil.string(TextFormatting.WHITE, slotFluid[j]),
                                            KeyUtil.string(TextFormatting.WHITE, slotStored[j])))
                                    .newLine();
                        }
                        richText.add(IKey.str(TextFormatting.GOLD + "======================")).newLine();
                    });
                });
    }

    @Override
    public TextureArea getProgressBarTexture(int index) {
        return index == 0 ? GuiTextures.PROGRESS_BAR_HPCA_COMPUTATION : GuiTextures.PROGRESS_BAR_FUSION_HEAT;
    }

    @Override
    public void addBarHoverText(List<ITextComponent> hoverList, int index) {
        if (fluidBank == null) {
            return;
        }
        BigInteger energyStored = fluidBank.getStored(circuit);
        BigInteger energyCapacity = fluidBank.getCapacity(circuit);
        if (index == 0) {
            ITextComponent info = TextComponentUtil.stringWithColor(
                    TextFormatting.AQUA, energyStored + " / " + energyCapacity + "L");
            hoverList.add(TextComponentUtil.translationWithColor(
                    TextFormatting.GRAY, "gtqt.multiblock.tfft.computation", info));
        }
    }

    // ---------------------------------------------------------------------
    // GUI
    // ---------------------------------------------------------------------

    @Override
    @Nonnull
    protected Widget getFlexButton(int x, int y, int width, int height) {
        WidgetGroup group = new WidgetGroup(x, y, width, height);
        group.addWidget(new ClickButtonWidget(0, 0, 9, 9, "", this::decrementThreshold)
                .setButtonTexture(GuiTextures.BUTTON_THROTTLE_MINUS)
                .setTooltipText("gtqtcore.multiblock.tfft.threshold_decrement"));
        group.addWidget(new ClickButtonWidget(9, 0, 9, 9, "", this::incrementThreshold)
                .setButtonTexture(GuiTextures.BUTTON_THROTTLE_PLUS)
                .setTooltipText("gtqtcore.multiblock.tfft.threshold_increment"));
        group.addWidget(new ClickButtonWidget(0, 9, 9, 9, "", this::clearFluid)
                .setButtonTexture(GuiTextures.BUTTON_CLEAR_GRID)
                .setTooltipText("gtqtcore.multiblock.tfft.clearfluid"));
        group.addWidget(new ClickButtonWidget(9, 9, 9, 9, "", this::setoutputFlag)
                .setButtonTexture(GuiTextures.LOCK)
                .setTooltipText("gtqtcore.multiblock.tfft.isoutput"));
        return group;
    }

    private void incrementThreshold(Widget.ClickData clickData) {
        circuit = MathHelper.clamp(circuit + 1, 0, FLUID_SLOTS - 1);
    }

    private void decrementThreshold(Widget.ClickData clickData) {
        circuit = MathHelper.clamp(circuit - 1, 0, FLUID_SLOTS - 1);
    }

    private void setoutputFlag(Widget.ClickData clickData) {
        outputflag = outputflag == 0 ? 1 : 0;
    }

    private void clearFluid(Widget.ClickData clickData) {
        fluid[circuit] = null;
        fluidBank.clearStore(circuit);
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
        return new MetatileEntityTwentyFiveFluidTank(metaTileEntityId);
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

        List<ITFFTData> aggregate = formed.getAggregate(BATTERY_KEY);
        List<ITFFTData> parts = aggregate == null ? new ArrayList<>() : new ArrayList<>(aggregate);
        if (parts.isEmpty()) {
            invalidateStructure();
            return;
        }

        fluidBank = (fluidBank == null) ? new TFFTTankFluidBank(parts) : fluidBank.rebuild(parts);
    }

    private void initializeAbilities() {
        inputFluidInventory = new FluidTankList(true, getAbilities(MultiblockAbility.IMPORT_FLUIDS));
        outputFluidInventory = new FluidTankList(true, getAbilities(MultiblockAbility.EXPORT_FLUIDS));
        itemImportInventory = new ItemHandlerList(getAbilities(MultiblockAbility.IMPORT_ITEMS));
        energyContainer = new EnergyContainerList(getAbilities(MultiblockAbility.INPUT_ENERGY));
    }

    private void resetTileAbilities() {
        inputFluidInventory = new FluidTankList(true);
        outputFluidInventory = new FluidTankList(true);
        itemImportInventory = new ItemHandlerList(Collections.emptyList());
        energyContainer = new EnergyContainerList(Collections.emptyList());
    }

    @Override
    public double getFillPercentage(int i) {
        return 0;
    }

    // ---------------------------------------------------------------------
    // Fluid bank
    // ---------------------------------------------------------------------

    public static class TFFTTankFluidBank {

        private static final String NBT_SIZE = "Size";
        private static final String NBT_STORED = "Stored";
        private static final String NBT_MAX = "Max";
        private static final String NBT_EUT = "Eut";

        private final long[][] storage = new long[FLUID_SLOTS][];
        private final long[][] maximums = new long[FLUID_SLOTS][];
        private final BigInteger[] capacity = new BigInteger[FLUID_SLOTS];
        private final int[] index = new int[FLUID_SLOTS];
        public int eut = 0;

        public TFFTTankFluidBank(List<ITFFTData> batteries) {
            int size = batteries.size();
            for (int i = 0; i < FLUID_SLOTS; i++) {
                storage[i] = new long[size];
                maximums[i] = new long[size];
                for (int j = 0; j < size; j++) {
                    maximums[i][j] = batteries.get(j).getCapacity() / FLUID_SLOTS;
                }
                capacity[i] = summarize(maximums[i]);
            }
            for (ITFFTData battery : batteries) {
                eut += battery.getEut();
            }
        }

        public TFFTTankFluidBank(NBTTagCompound storageTag) {
            for (int j = 0; j < FLUID_SLOTS; j++) {
                int size = storageTag.getInteger(NBT_SIZE + j);
                storage[j] = new long[size];
                maximums[j] = new long[size];
                for (int i = 0; i < size; i++) {
                    NBTTagCompound subtag = storageTag.getCompoundTag(j + String.valueOf(i));
                    if (subtag.hasKey(NBT_STORED + j)) {
                        storage[j][i] = subtag.getLong(NBT_STORED + j);
                    }
                    maximums[j][i] = subtag.getLong(NBT_MAX + j);
                }
                capacity[j] = summarize(maximums[j]);
            }
            eut = storageTag.getInteger(NBT_EUT);
        }

        private NBTTagCompound writeToNBT(NBTTagCompound compound) {
            for (int j = 0; j < FLUID_SLOTS; j++) {
                compound.setInteger(NBT_SIZE + j, storage[j].length);
                for (int i = 0; i < storage[j].length; i++) {
                    NBTTagCompound subtag = new NBTTagCompound();
                    if (storage[j][i] > 0) {
                        subtag.setLong(NBT_STORED + j, storage[j][i]);
                    }
                    subtag.setLong(NBT_MAX + j, maximums[j][i]);
                    compound.setTag(j + String.valueOf(i), subtag);
                }
            }
            compound.setInteger(NBT_EUT, eut);
            return compound;
        }

        public TFFTTankFluidBank rebuild(@NotNull List<ITFFTData> batteries) {
            if (batteries.isEmpty()) {
                throw new IllegalArgumentException("Cannot rebuild Power Substation power bank with no batteries!");
            }
            TFFTTankFluidBank newStorage = new TFFTTankFluidBank(batteries);
            for (int i = 0; i < FLUID_SLOTS; i++) {
                for (long stored : storage[i]) {
                    newStorage.fill(stored, i);
                }
            }
            return newStorage;
        }

        /** @return Amount filled into storage */
        public long fill(long amount, int circuit) {
            if (amount < 0) {
                throw new IllegalArgumentException("Amount cannot be negative!");
            }
            int idx = index[circuit];
            if (idx != storage[circuit].length - 1 && storage[circuit][idx] == maximums[circuit][idx]) {
                idx++;
                index[circuit] = idx;
            }
            long maxFill = Math.min(maximums[circuit][idx] - storage[circuit][idx], amount);
            if (maxFill == 0 && idx == storage[circuit].length - 1) {
                return 0;
            }
            storage[circuit][idx] += maxFill;
            amount -= maxFill;
            if (amount > 0 && idx != storage[circuit].length - 1) {
                return maxFill + fill(amount, circuit);
            }
            return maxFill;
        }

        public long drain(long amount, int circuit) {
            if (amount < 0) {
                throw new IllegalArgumentException("Amount cannot be negative!");
            }
            int idx = index[circuit];
            if (idx != 0 && storage[circuit][idx] == 0) {
                idx--;
                index[circuit] = idx;
            }
            long maxDrain = Math.min(storage[circuit][idx], amount);
            if (maxDrain == 0 && idx == 0) {
                return 0;
            }
            storage[circuit][idx] -= maxDrain;
            amount -= maxDrain;
            if (amount > 0 && idx != 0) {
                index[circuit] = idx - 1;
                return maxDrain + drain(amount, circuit);
            }
            return maxDrain;
        }

        public BigInteger getCapacity(int circuit) {
            return capacity[circuit];
        }

        public BigInteger getStored(int circuit) {
            return summarize(storage[circuit]);
        }

        public boolean hasFluid(int circuit) {
            for (long l : storage[circuit]) {
                if (l > 0) {
                    return true;
                }
            }
            return false;
        }

        private static BigInteger summarize(long[] values) {
            BigInteger retVal = BigInteger.ZERO;
            long currentSum = 0;
            for (long value : values) {
                if (currentSum != 0 && value > Long.MAX_VALUE - currentSum) {
                    retVal = retVal.add(BigInteger.valueOf(currentSum));
                    currentSum = 0;
                }
                currentSum += value;
            }
            if (currentSum != 0) {
                retVal = retVal.add(BigInteger.valueOf(currentSum));
            }
            return retVal;
        }

        public void clearStore(int circuit) {
            Arrays.fill(storage[circuit], 0L);
        }
    }
}