package com.drppp.drtech.common.MetaTileEntities.muti.electric.store;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;

import com.drppp.drtech.common.Blocks.BlocksInit;
import com.drppp.drtech.common.Blocks.MetaBlocks.MetaCasing;
import com.drppp.drtech.Client.Textures;
import com.drppp.drtech.api.Utils.Datas;
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
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.pattern.TraceabilityPredicate;
import gregtech.api.util.BlockInfo;
import gregtech.api.util.GTTransferUtils;
import gregtech.api.util.TextComponentUtil;
import gregtech.api.util.TextFormattingUtil;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.utils.TooltipHelper;
import keqing.gtqtcore.common.block.GTQTMetaBlocks;
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
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.math.BigInteger;
import java.util.*;
import java.util.function.Supplier;

import static gregtech.api.util.RelativeDirection.*;
import static keqing.gtqtcore.common.block.blocks.BlockMultiblockGlass1.CasingType.TI_BORON_SILICATE_GLASS;

public class MetatileEntityTwentyFiveFluidTank extends MultiblockWithDisplayBase implements IControllable, IProgressBarMultiblock {
    private static final String NBT_FLUID_BANK = "FluidBank";
    private boolean isActive, isWorkingEnabled = true;
    // Match Context Headers
    private static final String TFFT_PART_HEADER = "TfftPart_";

    private static final String NBT_FLUID = "Fluid";
    private FluidStack[] fluid = new FluidStack[25];
    public IMultipleTankHandler inputFluidInventory;
    public IMultipleTankHandler outputFluidInventory;
    protected ItemHandlerList itemImportInventory;
    protected IEnergyContainer energyContainer;
    private TFFTTankFluidBank fluidBank;
    private int circuit=0;
    private int time=0;
    private int outputflag = 0;
    public int getCircuitNo()
    {
        return  this.circuit;
    }
    public MetatileEntityTwentyFiveFluidTank(ResourceLocation metaTileEntityId) {
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
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setBoolean("isActive", isActive);
        data.setBoolean("isWorkingEnabled", isWorkingEnabled);
        data.setInteger("OutFlag",this.outputflag);
        data.setInteger("Circuit",this.circuit);
        for (int i = 0; i < 25; i++) {
            if(fluid[i]!=null)
            {
                NBTTagCompound fluidNBT = new NBTTagCompound();
                fluid[i].writeToNBT(fluidNBT);
                data.setTag(NBT_FLUID+i,fluidNBT);
            }
        }
        if (fluidBank != null) {
            data.setTag(NBT_FLUID_BANK, fluidBank.writeToNBT(new NBTTagCompound()));
        }
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        isActive = data.getBoolean("isActive");
        isWorkingEnabled = data.getBoolean("isWorkingEnabled");
        this.outputflag = data.getInteger("OutFlag");
        this.circuit = data.getInteger("Circuit");
        for (int i = 0; i < 25; i++) {
            if(data.hasKey(NBT_FLUID+i))
            {
                NBTTagCompound fluidNBT= (NBTTagCompound) data.getTag(NBT_FLUID+i);
                fluid[i] = FluidStack.loadFluidStackFromNBT(fluidNBT);

            }
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
    @Override
    protected void updateFormedValid() {
        if (!this.getWorld().isRemote ) {
            //getCircuit();
            if (getOffsetTimer() % 20 == 0) {
                // active here is just used for rendering
                for (int i = 0; i < 25; i++) {
                    if(fluidBank.hasFluid(i))
                    {
                        setActive(true);
                        break;
                    }
                    else
                        setActive(false);
                }

            }
            if(this.energyContainer.getInputVoltage() * this.energyContainer.getInputAmperage() >this.fluidBank.eut && this.energyContainer.getEnergyStored()>this.fluidBank.eut)
            {
                this.energyContainer.changeEnergy(-this.fluidBank.eut);
                this.setWorkingEnabled(true);
            }
            if (isWorkingEnabled() && time++>20 ) {
                if(inputFluidInventory.getTanks()>0){
                    for (int i = 0; i < inputFluidInventory.getTanks(); i++) {
                        if(inputFluidInventory.getTankAt(i).getFluidAmount()>0)
                            for (int j = 0; j < 25; j++) {
                                if(this.fluid[j]!=null && this.fluid[j].isFluidEqual(inputFluidInventory.getTankAt(i).getFluid()))
                                {
                                    long amount =  fluidBank.fill(inputFluidInventory.getTankAt(i).getFluidAmount(),j);
                                    inputFluidInventory.getTankAt(i).drain((int)amount,true);
                                    if(inputFluidInventory.getTankAt(i).getFluidAmount()==0)
                                        break;
                                }
                            }
                        for (int j = 0; j < 25; j++) {
                            if(this.fluid[j]==null )
                            {
                                if(this.fluid[j]==null)  this.fluid[j] = inputFluidInventory.getTankAt(i).getFluid();
                                long amount =  fluidBank.fill(inputFluidInventory.getTankAt(i).getFluidAmount(),j);
                                inputFluidInventory.getTankAt(i).drain((int)amount,true);
                                if(inputFluidInventory.getTankAt(i).getFluidAmount()==0)
                                    break;
                            }
                        }
                    }
                }
                if(outputFluidInventory.getTanks()>0 && this.fluid[circuit]!=null &&this.outputflag==1)
                {

                    List<FluidStack> Outputs = new ArrayList<>();
                    for (int i = 0; i < outputFluidInventory.getTanks(); i++) {
                        if((this.fluid[circuit]!=null && this.fluid[circuit].isFluidEqual(outputFluidInventory.getTankAt(i).getFluid())) || outputFluidInventory.getTankAt(i).getFluid()==null)
                        {
                            long energyDebanked = fluidBank.drain(outputFluidInventory.getTankAt(i).getCapacity()-outputFluidInventory.getTankAt(i).getFluidAmount(),this.circuit);
                            Outputs.add(new FluidStack(this.fluid[circuit].getFluid(), (int) energyDebanked));
                        }
                    }
                    GTTransferUtils.addFluidsToFluidHandler(outputFluidInventory ,false, Outputs);
                }
                if(!fluidBank.hasFluid(this.circuit))
                    fluid[circuit] = null;
                time=0;
            }
        }
    }
    @NotNull
    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start(RIGHT, DOWN, FRONT)
                .aisle("XXXXX", "XXXXX", "XXSXX", "XXXXX", "XXXXX")
                .aisle("GGGGG", "GBBBG", "GBBBG", "GBBBG", "GGGGG").setRepeatable(3, 14)
                .aisle("XXXXX", "XXXXX", "XXXXX", "XXXXX", "XXXXX")
                .where('S', selfPredicate())
                .where('X', states(getCasingState())
                        .or(autoAbilities())
                        .or(abilities(MultiblockAbility.IMPORT_FLUIDS).setMaxGlobalLimited(2).setPreviewCount(1))
                        .or(abilities(MultiblockAbility.EXPORT_FLUIDS).setMaxGlobalLimited(2).setPreviewCount(1)
                                .or(abilities(MultiblockAbility.IMPORT_ITEMS).setExactLimit(1))
                                .or(abilities(MultiblockAbility.INPUT_ENERGY).setMinGlobalLimited(1).setPreviewCount(1)))
                )
                .where('G', states(getGlassState()))
                .where('B', BATTERY_PREDICATE.get())
                .build();
    }
    protected IBlockState getCasingState() {
        return BlocksInit.COMMON_CASING.getState(MetaCasing.MetalCasingType.TFFT_CASING);
    }

    protected IBlockState getGlassState() {
        return GTQTMetaBlocks.blockMultiblockGlass1.getState(TI_BORON_SILICATE_GLASS);
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
    protected static final Supplier<TraceabilityPredicate> BATTERY_PREDICATE = () -> new TraceabilityPredicate(
            blockWorldState -> {
                IBlockState state = blockWorldState.getBlockState();
                if (Datas.TFFT_CASINGS.containsKey(state)) {
                    ITfftData tfft_parts = Datas.TFFT_CASINGS.get(state);
                    if (tfft_parts.getTier() != -1 && tfft_parts.getCapacity() > 0) {
                        String key = TFFT_PART_HEADER + tfft_parts.getBatteryName();
                        MetatileEntityTwentyFiveFluidTank.TfftPartMatchWrapper wrapper = blockWorldState.getMatchContext().get(key);
                        if (wrapper == null) wrapper = new MetatileEntityTwentyFiveFluidTank.TfftPartMatchWrapper(tfft_parts);
                        blockWorldState.getMatchContext().set(key, wrapper.increment());
                    }
                    return true;
                }
                return false;
            }, () -> Datas.TFFT_CASINGS.entrySet().stream()
            .sorted(Comparator.comparingInt(entry -> entry.getValue().getTier()))
            .map(entry -> new BlockInfo(entry.getKey(), null))
            .toArray(BlockInfo[]::new))
            .addTooltips("gregtech.multiblock.pattern.error.batteries");
    @Override
    public void addInformation(ItemStack stack, @Nullable World world, @NotNull List<String> tooltip,
                               boolean advanced) {
        tooltip.add(TooltipHelper.RAINBOW_SLOW + I18n.format("超超超超级量子缸！", new Object[0]));
        tooltip.add(I18n.format("能存储25种流体的超级储罐，容量由多方块内的流体单元决定"));
        tooltip.add(I18n.format("在UI中通过按钮进行流体操作"));
    }
    @Override
    protected void addDisplayText(List<ITextComponent> textList) {
        MultiblockDisplayText.builder(textList, isStructureFormed())
                .setWorkingStatus(true, isActive() && isWorkingEnabled()) // transform into two-state system for display
                .addCustom(tl -> {
                    if (isStructureFormed() && fluidBank != null) {

                        BigInteger energyCapacity = fluidBank.getCapacity(this.circuit);
                        ITextComponent capacityFormatted = TextComponentUtil.stringWithColor(
                                TextFormatting.GOLD,
                                TextFormattingUtil.formatNumbers(energyCapacity) + " L");
                        tl.add(TextComponentUtil.translationWithColor(
                                TextFormatting.GRAY,
                                "gtqt.multiblock.power_substation.stored",capacityFormatted));
                        tl.add(TextComponentUtil.translationWithColor(
                                TextFormatting.GRAY,
                                "drtech.multiblock.power_substation.eut",this.fluidBank.eut));
                        tl.add(TextComponentUtil.translationWithColor(
                                TextFormatting.GRAY,
                                "drtech.multiblock.power_substation.output",this.outputflag==0?"禁用":"启用"));
                        tl.add(TextComponentUtil.translationWithColor(TextFormatting.GOLD, "======================"));
                        for(int i=-2;i<=2;i++)
                        {

                            if(i==0) {

                                tl.add(TextComponentUtil.translationWithColor(TextFormatting.GOLD, "gtqt.multiblock.yot_tank.fluid_type", circuit, this.fluid[circuit] == null ? "空" : this.fluid[circuit].getLocalizedName(), getstoredFormatted(circuit+i)));
                            }
                            else if(circuit+i>=0&&circuit+i<25)
                            {
                                tl.add(TextComponentUtil.translationWithColor(TextFormatting.GRAY, "gtqt.multiblock.yot_tank.fluid_type", circuit + i, this.fluid[circuit + i] == null ? "空" : this.fluid[circuit + i].getLocalizedName(), getstoredFormatted(circuit+i)));
                            }
                        }
                        tl.add(TextComponentUtil.translationWithColor(TextFormatting.GOLD, "======================"));
                    }
                });
    }
    ITextComponent getstoredFormatted(int x) {
        BigInteger energyStored = null;
        if (x >= 0) {
            energyStored = fluidBank.getStored(x);
        }
        ITextComponent storedFormatted = TextComponentUtil.stringWithColor(TextFormatting.GOLD, TextFormattingUtil.formatNumbers(energyStored) + " L");
        return storedFormatted;

    }
    @Override
    public TextureArea getProgressBarTexture(int index) {
        return index == 0 ? GuiTextures.PROGRESS_BAR_HPCA_COMPUTATION : GuiTextures.PROGRESS_BAR_FUSION_HEAT;
    }
    @Override
    public void addBarHoverText(List<ITextComponent> hoverList, int index) {
        BigInteger energyStored = fluidBank.getStored(this.circuit);
        BigInteger energyCapacity = fluidBank.getCapacity(this.circuit);
        if (index == 0) {
            ITextComponent cwutInfo = TextComponentUtil.stringWithColor(
                    TextFormatting.AQUA,
                    energyStored+ " / " + energyCapacity + "L");
            hoverList.add(TextComponentUtil.translationWithColor(
                    TextFormatting.GRAY,
                    "gtqt.multiblock.tfft.computation",
                    cwutInfo));
        }
    }
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
        this.circuit = MathHelper.clamp(circuit + 1, 0, 24);
    }

    private void decrementThreshold(Widget.ClickData clickData) {
        this.circuit = MathHelper.clamp(circuit - 1, 0, 24);
    }
    private void setoutputFlag(Widget.ClickData clickData)
    {
        if( this.outputflag==0)
            this.outputflag=1;
        else if (this.outputflag==1) {
            this.outputflag=0;
        }
    }
    private void clearFluid(Widget.ClickData clickData)
    {
        this.fluid[circuit] = null;
        this.fluidBank.clearStore(circuit);
    }
    @SideOnly(Side.CLIENT)
    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        getFrontOverlay().renderOrientedState(renderState, translation, pipeline, getFrontFacing(), this.isActive(),
                this.isWorkingEnabled());
    }
    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetatileEntityTwentyFiveFluidTank(this.metaTileEntityId);
    }
    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        resetTileAbilities();
    }
    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        initializeAbilities();
        List<ITfftData> parts = new ArrayList<>();
        for (Map.Entry<String, Object> battery : context.entrySet()) {
            if (battery.getKey().startsWith(TFFT_PART_HEADER) &&
                    battery.getValue() instanceof TfftPartMatchWrapper wrapper) {
                for (int i = 0; i < wrapper.amount; i++) {
                    parts.add(wrapper.partType);
                }
            }
        }
        if (parts.isEmpty()) {
            invalidateStructure();
            return;
        }
        if (this.fluidBank == null) {
            this.fluidBank = new TFFTTankFluidBank(parts);
        } else {
            this.fluidBank = fluidBank.rebuild(parts);
        }
    }
    private void initializeAbilities() {
        this.inputFluidInventory = new FluidTankList(true, getAbilities(MultiblockAbility.IMPORT_FLUIDS));
        this.outputFluidInventory = new FluidTankList(true, getAbilities(MultiblockAbility.EXPORT_FLUIDS));
        this.itemImportInventory = new ItemHandlerList(getAbilities(MultiblockAbility.IMPORT_ITEMS));
        this.energyContainer = new EnergyContainerList(getAbilities(MultiblockAbility.INPUT_ENERGY));
    }
    private void resetTileAbilities() {
        this.inputFluidInventory = new FluidTankList(true);
        this.outputFluidInventory = new FluidTankList(true);
        this.itemImportInventory =  new ItemHandlerList(Collections.emptyList());
        this.energyContainer = new EnergyContainerList(Collections.emptyList());
    }

    @Override
    public double getFillPercentage(int i) {
        return 0;
    }

    

    private static class TfftPartMatchWrapper {

        private final ITfftData partType;
        private int amount;

        public TfftPartMatchWrapper(ITfftData partType) {
            this.partType = partType;
        }

        public TfftPartMatchWrapper increment() {
            amount++;
            return this;
        }
    }
    public static class TFFTTankFluidBank {

        private static final String NBT_SIZE = "Size";
        private static final String NBT_STORED = "Stored";
        private static final String NBT_MAX = "Max";
        private static final String NBT_EUT = "Eut";
        private final long[][] storage=new long[25][];
        private final long[][] maximums=new long[25][];
        private final BigInteger[] capacity = new BigInteger[25];
        private int index[] = new int[25];
        public  int eut = 0;

        public TFFTTankFluidBank(List<ITfftData> batteries) {
            for (int i = 0; i < 25; i++) {
                storage[i] = new long[batteries.size()];
                maximums[i] = new long[batteries.size()];
                for (int j = 0; j < batteries.size(); j++) {
                    maximums[i][j] = batteries.get(j).getCapacity()/25;
                    eut+= batteries.get(j).getEut();
                }
                capacity[i] = summarize(maximums[i]);
            }
        }

        public TFFTTankFluidBank(NBTTagCompound storageTag) {
            for (int j = 0; j < 25; j++) {
                int size = storageTag.getInteger(NBT_SIZE+j);
                storage[j] = new long[size];
                maximums[j] = new long[size];
                for (int i = 0; i < size; i++) {
                    NBTTagCompound subtag = storageTag.getCompoundTag(j+String.valueOf(i));
                    if (subtag.hasKey(NBT_STORED+j)) {
                        storage[j][i] = subtag.getLong(NBT_STORED+j);
                    }
                    maximums[j][i] = subtag.getLong(NBT_MAX+j);
                }

                capacity[j] = summarize(maximums[j]);
            }
            this.eut = storageTag.getInteger(NBT_EUT);
        }

        private NBTTagCompound writeToNBT(NBTTagCompound compound) {
            for (int j = 0; j < 25; j++) {
                compound.setInteger(NBT_SIZE+j, storage[j].length);
                for (int i = 0; i < storage[j].length; i++) {
                    NBTTagCompound subtag = new NBTTagCompound();
                    if (storage[j][i] > 0) {
                        subtag.setLong(NBT_STORED+j, storage[j][i]);
                    }
                    subtag.setLong(NBT_MAX+j, maximums[j][i]);
                    compound.setTag(j+String.valueOf(i), subtag);
                }
            }
            compound.setInteger(NBT_EUT,this.eut);
            return compound;
        }

        public TFFTTankFluidBank rebuild(@NotNull List<ITfftData> batteries) {
            if (batteries.isEmpty()) {
                throw new IllegalArgumentException("Cannot rebuild Power Substation power bank with no batteries!");
            }
            TFFTTankFluidBank newStorage = new TFFTTankFluidBank(batteries);
            for (int i = 0; i < 25; i++) {
                for (long stored : storage[i]) {
                    newStorage.fill(stored,i);
                }
            }

            return newStorage;
        }

        /** @return Amount filled into storage */
        public long fill(long amount,int circuit) {
            if (amount < 0) throw new IllegalArgumentException("Amount cannot be negative!");
            if (index[circuit] != storage[circuit].length - 1 && storage[circuit][index[circuit]] == maximums[circuit][index[circuit]]) {
                index[circuit]++;
            }
            long maxFill = Math.min(maximums[index[circuit]][circuit] - storage[circuit][index[circuit]], amount);
            if (maxFill == 0 && index[circuit] == storage[circuit].length - 1) {
                return 0;
            }
            storage[circuit][index[circuit]] += maxFill;
            amount -= maxFill;
            if (amount > 0 && index[circuit] != storage[circuit].length - 1) {
                return maxFill + fill(amount,circuit);
            }
            return maxFill;
        }

        public long drain(long amount,int circuit) {
            if (amount < 0) throw new IllegalArgumentException("Amount cannot be negative!");
            if (index[circuit] != 0 && storage[circuit][index[circuit]] == 0) {
                index[circuit]--;
            }
            long maxDrain = Math.min(storage[circuit][index[circuit]], amount);
            if (maxDrain == 0 && index[circuit] == 0) {
                return 0;
            }
            storage[circuit][index[circuit]] -= maxDrain;
            amount -= maxDrain;
            if (amount > 0 && index[circuit] != 0) {
                index[circuit]--;
                return maxDrain + drain(amount,circuit);
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
                if (l > 0) return true;
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
        public  void clearStore(int circuit)
        {
            for (int i = 0; i < storage.length; i++) {
                storage[circuit][i]=0;
            }
        }
    }
}
