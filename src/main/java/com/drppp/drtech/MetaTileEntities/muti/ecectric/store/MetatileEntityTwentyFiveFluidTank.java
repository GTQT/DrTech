package com.drppp.drtech.MetaTileEntities.muti.ecectric.store;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.drppp.drtech.Blocks.BlocksInit;
import com.drppp.drtech.Blocks.MetaBlocks.MetaCasing;
import com.drppp.drtech.Blocks.MetaBlocks.MetaGlasses;
import com.drppp.drtech.Client.Textures;
import com.drppp.drtech.Utils.Datas;
import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IControllable;
import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.capability.impl.ItemHandlerList;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.*;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.pattern.TraceabilityPredicate;
import gregtech.api.unification.material.Materials;
import gregtech.api.util.BlockInfo;
import gregtech.api.util.GTTransferUtils;
import gregtech.api.util.TextComponentUtil;
import gregtech.api.util.TextFormattingUtil;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.common.items.MetaItems;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;
import java.util.*;
import java.util.function.Supplier;

import static gregtech.api.util.RelativeDirection.*;

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
    private TFFTTankFluidBank fluidBank;
    private int circuit=0;
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
            getCircuit();
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
            if (isWorkingEnabled()) {
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
                if(outputFluidInventory.getTanks()>0 && this.fluid[circuit]!=null)
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
            }
        }
    }
    private void getCircuit()
    {
        int circuit = 0;
        for (int i = 0; i < this.itemImportInventory.getSlots(); i++) {
            ItemStack is = this.itemImportInventory.getStackInSlot(i);
            if(is.getItem() == MetaItems.INTEGRATED_CIRCUIT.getMetaItem() && is.getMetadata()== MetaItems.INTEGRATED_CIRCUIT.getMetaValue())
            {
                NBTTagCompound compound = is.getTagCompound();
                if(compound.hasKey("Configuration"))
                    circuit = compound.getInteger("Configuration");
                else
                    circuit=0;
            }
        }
        this.circuit = Math.min(circuit,24);
    }

    @NotNull
    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start(RIGHT, UP, FRONT)
                .aisle("XXXXX", "XXXXX", "XXSXX", "XXXXX", "XXXXX")
                .aisle("GGGGG", "GBBBG", "GBBBG", "GBBBG", "GGGGG").setRepeatable(3, 14)
                .aisle("XXXXX", "XXXXX", "XXXXX", "XXXXX", "XXXXX")
                .where('S', selfPredicate())
                .where('X', states(getCasingState())
                        .or(autoAbilities())
                        .or(abilities(MultiblockAbility.IMPORT_FLUIDS).setMaxGlobalLimited(2).setPreviewCount(1))
                        .or(abilities(MultiblockAbility.EXPORT_FLUIDS).setMaxGlobalLimited(2).setPreviewCount(1)
                                .or(abilities(MultiblockAbility.IMPORT_ITEMS).setExactLimit(1)))
                )
                .where('G', states(getGlassState()))
                .where('B', BATTERY_PREDICATE.get())
                .build();
    }
    protected IBlockState getCasingState() {
        return BlocksInit.COMMON_CASING.getState(MetaCasing.MetalCasingType.TFFT_CASING);
    }

    protected IBlockState getGlassState() {
        return BlocksInit.TRANSPARENT_CASING.getState(MetaGlasses.CasingType.TI_BORON_SILICATE_GLASS_BLOCK);
    }
    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return Textures.TFFT_TANK_CASING;
    }
    protected static final Supplier<TraceabilityPredicate> BATTERY_PREDICATE = () -> new TraceabilityPredicate(
            blockWorldState -> {
                IBlockState state = blockWorldState.getBlockState();
                if (Datas.TFFT_CASINGS.containsKey(state)) {
                    IBatteryData tfft_parts = Datas.TFFT_CASINGS.get(state);
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
        tooltip.add(I18n.format("gregtech.machine.yot_tank.tooltip1"));
        tooltip.add(I18n.format("gregtech.machine.yot_tank.tooltip2"));
    }
    @Override
    protected void addDisplayText(List<ITextComponent> textList) {
        MultiblockDisplayText.builder(textList, isStructureFormed())
                .setWorkingStatus(true, isActive() && isWorkingEnabled()) // transform into two-state system for display
                .setWorkingStatusKeys(
                        "gregtech.multiblock.idling",
                        "gregtech.multiblock.idling",
                        "gregtech.machine.active_transformer.routing")
                .addCustom(tl -> {
                    if (isStructureFormed() && fluidBank != null) {
                        BigInteger energyStored = fluidBank.getStored(this.circuit);
                        BigInteger energyCapacity = fluidBank.getCapacity(this.circuit);

                        // Stored EU line
                        ITextComponent storedFormatted = TextComponentUtil.stringWithColor(
                                TextFormatting.GOLD,
                                TextFormattingUtil.formatNumbers(energyStored) + " L");
                        tl.add(TextComponentUtil.translationWithColor(
                                TextFormatting.GRAY,
                                "gregtech.multiblock.power_substation.stored",
                                storedFormatted));

                        // EU Capacity line
                        ITextComponent capacityFormatted = TextComponentUtil.stringWithColor(
                                TextFormatting.GOLD,
                                TextFormattingUtil.formatNumbers(energyCapacity) + " L");
                        tl.add(TextComponentUtil.translationWithColor(
                                TextFormatting.GRAY,
                                "gregtech.multiblock.power_substation.capacity",
                                capacityFormatted));

                        tl.add(TextComponentUtil.translationWithColor(
                                TextFormatting.GOLD,
                                "drtech.multiblock.yot_tank.fluid_type",
                                this.fluid[circuit]==null?"空":this.fluid[circuit].getLocalizedName()));
                    }
                })
                .addWorkingStatusLine();
        //textList.add(TextComponentUtil.translationWithColor(
        // TextFormatting.GOLD,
        // "drtech.multiblock.yot_tank.fluid_type", fluidBank.fluid.getUnlocalizedName()));
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
        List<IBatteryData> parts = new ArrayList<>();
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
    }
    private void resetTileAbilities() {
        this.inputFluidInventory = new FluidTankList(true);
        this.outputFluidInventory = new FluidTankList(true);
        this.itemImportInventory =  new ItemHandlerList(Collections.emptyList());
    }

    @Override
    public double getFillPercentage(int i) {
        return 0;
    }
    private static class TfftPartMatchWrapper {

        private final IBatteryData partType;
        private int amount;

        public TfftPartMatchWrapper(IBatteryData partType) {
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
        private final long[][] storage=new long[25][];
        private final long[][] maximums=new long[25][];
        private final BigInteger[] capacity = new BigInteger[25];
        private int index[] = new int[25];

        public TFFTTankFluidBank(List<IBatteryData> batteries) {
            for (int i = 0; i < 25; i++) {
                storage[i] = new long[batteries.size()];
                maximums[i] = new long[batteries.size()];
                for (int j = 0; j < batteries.size(); j++) {
                    maximums[i][j] = batteries.get(j).getCapacity()/25;
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

            return compound;
        }
        /**
         * Rebuild the power storage with a new list of batteries.
         * Will use existing stored power and try to map it onto new batteries.
         * If there was more power before the rebuild operation, it will be lost.
         */
        public TFFTTankFluidBank rebuild(@NotNull List<IBatteryData> batteries) {
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

            // ensure index
            if (index[circuit] != storage[circuit].length - 1 && storage[circuit][index[circuit]] == maximums[circuit][index[circuit]]) {
                index[circuit]++;
            }

            long maxFill = Math.min(maximums[index[circuit]][circuit] - storage[circuit][index[circuit]], amount);

            // storage is completely full
            if (maxFill == 0 && index[circuit] == storage[circuit].length - 1) {
                return 0;
            }

            // fill this "battery" as much as possible
            storage[circuit][index[circuit]] += maxFill;
            amount -= maxFill;

            // try to fill other "batteries" if necessary
            if (amount > 0 && index[circuit] != storage[circuit].length - 1) {
                return maxFill + fill(amount,circuit);
            }

            // other fill not necessary, either because the storage is now completely full,
            // or we were able to consume all the energy in this "battery"
            return maxFill;
        }

        /** @return Amount drained from storage */
        public long drain(long amount,int circuit) {
            if (amount < 0) throw new IllegalArgumentException("Amount cannot be negative!");

            // ensure index
            if (index[circuit] != 0 && storage[circuit][index[circuit]] == 0) {
                index[circuit]--;
            }

            long maxDrain = Math.min(storage[circuit][index[circuit]], amount);

            // storage is completely empty
            if (maxDrain == 0 && index[circuit] == 0) {
                return 0;
            }

            // drain this "battery" as much as possible
            storage[circuit][index[circuit]] -= maxDrain;
            amount -= maxDrain;

            // try to drain other "batteries" if necessary
            if (amount > 0 && index[circuit] != 0) {
                index[circuit]--;
                return maxDrain + drain(amount,circuit);
            }

            // other drain not necessary, either because the storage is now completely empty,
            // or we were able to drain all the energy from this "battery"
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
                    // will overflow if added
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

    }
}
