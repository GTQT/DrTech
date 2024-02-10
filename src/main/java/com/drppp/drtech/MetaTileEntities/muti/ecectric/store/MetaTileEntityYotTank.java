package com.drppp.drtech.MetaTileEntities.muti.ecectric.store;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.drppp.drtech.Blocks.BlocksInit;
import com.drppp.drtech.Blocks.MetaBlocks.MetaCasing;
import com.drppp.drtech.Blocks.MetaBlocks.MetaGlasses;
import com.drppp.drtech.Client.Textures;
import com.drppp.drtech.Utils.Datas;
import gregtech.api.GregTechAPI;
import gregtech.api.capability.*;
import gregtech.api.capability.impl.EnergyContainerList;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.items.metaitem.stats.ItemFluidContainer;
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
import gregtech.client.utils.TooltipHelper;
import gregtech.common.blocks.BlockGlassCasing;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.metatileentities.multi.electric.MetaTileEntityPowerSubstation;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

import java.math.BigInteger;
import java.util.*;
import java.util.function.Supplier;

import static gregtech.api.util.RelativeDirection.*;

public class MetaTileEntityYotTank extends MultiblockWithDisplayBase implements IControllable, IProgressBarMultiblock {
    private static final String NBT_FLUID_BANK = "EnergyBank";
    private boolean isActive, isWorkingEnabled = true;
    // Match Context Headers
    private static final String YOT_PART_HEADER = "YotPart_";

    private static final String NBT_FLUID = "Fluid";
    private  FluidStack fluid;
    public IMultipleTankHandler inputFluidInventory;
    public IMultipleTankHandler outputFluidInventory;
    private YotTankFluidBank fluidBank;
    public MetaTileEntityYotTank(ResourceLocation metaTileEntityId) {
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
       if(fluid!=null)
       {
           NBTTagCompound fluidNBT = new NBTTagCompound();
           fluid.writeToNBT(fluidNBT);
           data.setTag(NBT_FLUID,fluidNBT);
           if (fluidBank != null) {
               data.setTag(NBT_FLUID_BANK, fluidBank.writeToNBT(new NBTTagCompound()));
           }
       }
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        isActive = data.getBoolean("isActive");
        isWorkingEnabled = data.getBoolean("isWorkingEnabled");
        if(data.hasKey(NBT_FLUID))
        {
            NBTTagCompound fluidNBT= (NBTTagCompound) data.getTag(NBT_FLUID);
            fluid = FluidStack.loadFluidStackFromNBT(fluidNBT);
            if (data.hasKey(NBT_FLUID_BANK)) {
                fluidBank = new YotTankFluidBank(data.getCompoundTag(NBT_FLUID_BANK));

            }
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
            if (getOffsetTimer() % 20 == 0) {
                // active here is just used for rendering
                setActive(fluidBank.hasFluid());
            }

            if (isWorkingEnabled()) {
                if(inputFluidInventory.getTanks()>0){
                    for (int i = 0; i < inputFluidInventory.getTanks(); i++) {
                        if(this.fluid==null || this.fluid.isFluidEqual(inputFluidInventory.getTankAt(i).getFluid()))
                        {
                            if(this.fluid==null)  this.fluid = inputFluidInventory.getTankAt(i).getFluid();
                            long amount =  fluidBank.fill(inputFluidInventory.getTankAt(i).getFluidAmount());
                            inputFluidInventory.getTankAt(i).drain((int)amount,true);
                        }
                    }
                }
                if(outputFluidInventory.getTanks()>0 && this.fluid!=null)
                {
                    List<FluidStack> Outputs = new ArrayList<>();
                    for (int i = 0; i < outputFluidInventory.getTanks(); i++) {
                        long energyDebanked = fluidBank.drain(outputFluidInventory.getTankAt(i).getCapacity()-outputFluidInventory.getTankAt(i).getFluidAmount());
                        Outputs.add(new FluidStack(this.fluid.getFluid(), (int) energyDebanked));
                    }
                    GTTransferUtils.addFluidsToFluidHandler(outputFluidInventory ,false, Outputs);
                }
                if(!fluidBank.hasFluid())
                    fluid = null;
            }
        }
    }

    @NotNull
    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start(RIGHT, FRONT, UP)
                .aisle("#####", "#XXX#", "#XXX#", "#XXX#", "#####")
                .aisle("XXSXX", "XCCCX", "XCCCX", "XCCCX", "XXXXX")
                .aisle("GGGGG", "GBBBG", "GBBBG", "GBBBG", "GGGGG").setRepeatable(1, 14)
                .aisle("XXXXX", "XXXXX", "XXXXX", "XXXXX", "XXXXX")
                .aisle("LLLLL", "L###L", "L###L", "L###L", "LLLLL")
                .where('S', selfPredicate())
                .where('#', any())
                .where('C', states(getCasingState()))
                .where('X', states(getCasingState())
                        .or(autoAbilities())
                        .or(abilities(MultiblockAbility.IMPORT_FLUIDS).setMaxGlobalLimited(2).setPreviewCount(1))
                        .or(abilities(MultiblockAbility.EXPORT_FLUIDS).setMaxGlobalLimited(2).setPreviewCount(1))
                )
                .where('G', states(getGlassState()))
                .where('L', frames(Materials.Steel))
                .where('B', BATTERY_PREDICATE.get())
                .build();
    }
    protected IBlockState getCasingState() {
        return BlocksInit.COMMON_CASING.getState(MetaCasing.MetalCasingType.YOT_TANK_CASING);
    }

    protected IBlockState getGlassState() {
        return BlocksInit.TRANSPARENT_CASING.getState(MetaGlasses.CasingType.TI_BORON_SILICATE_GLASS_BLOCK);
    }
    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return Textures.YOT_TANK_CASING;
    }
    protected static final Supplier<TraceabilityPredicate> BATTERY_PREDICATE = () -> new TraceabilityPredicate(
            blockWorldState -> {
                IBlockState state = blockWorldState.getBlockState();
                if (Datas.YOT_CASINGS.containsKey(state)) {
                    IBatteryData yot_parts = Datas.YOT_CASINGS.get(state);
                    if (yot_parts.getTier() != -1 && yot_parts.getCapacity() > 0) {
                        String key = YOT_PART_HEADER + yot_parts.getBatteryName();
                        MetaTileEntityYotTank.YotPartMatchWrapper wrapper = blockWorldState.getMatchContext().get(key);
                        if (wrapper == null) wrapper = new MetaTileEntityYotTank.YotPartMatchWrapper(yot_parts);
                        blockWorldState.getMatchContext().set(key, wrapper.increment());
                    }
                    return true;
                }
                return false;
            }, () -> Datas.YOT_CASINGS.entrySet().stream()
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
                        BigInteger energyStored = fluidBank.getStored();
                        BigInteger energyCapacity = fluidBank.getCapacity();

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
                                this.fluid==null?"空":this.fluid.getLocalizedName()));
                    }
                })
                .addWorkingStatusLine();
        //textList.add(TextComponentUtil.translationWithColor(
               // TextFormatting.GOLD,
               // "drtech.multiblock.yot_tank.fluid_type", fluidBank.fluid.getUnlocalizedName()));
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        getFrontOverlay().renderOrientedState(renderState, translation, pipeline, getFrontFacing(), this.isActive(),
                this.isWorkingEnabled());
    }
    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityYotTank(this.metaTileEntityId);
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
            if (battery.getKey().startsWith(YOT_PART_HEADER) &&
                    battery.getValue() instanceof MetaTileEntityYotTank.YotPartMatchWrapper wrapper) {
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
            this.fluidBank = new YotTankFluidBank(parts);
        } else {
            this.fluidBank = fluidBank.rebuild(parts);
        }
    }
    private void initializeAbilities() {
        this.inputFluidInventory = new FluidTankList(true, getAbilities(MultiblockAbility.IMPORT_FLUIDS));
        this.outputFluidInventory = new FluidTankList(true, getAbilities(MultiblockAbility.EXPORT_FLUIDS));
    }
    private void resetTileAbilities() {
        this.inputFluidInventory = new FluidTankList(true);
        this.outputFluidInventory = new FluidTankList(true);
    }

    @Override
    public double getFillPercentage(int i) {
        return 0;
    }
    private static class YotPartMatchWrapper {

        private final IBatteryData partType;
        private int amount;

        public YotPartMatchWrapper(IBatteryData partType) {
            this.partType = partType;
        }

        public MetaTileEntityYotTank.YotPartMatchWrapper increment() {
            amount++;
            return this;
        }
    }
    public static class YotTankFluidBank {

        private static final String NBT_SIZE = "Size";
        private static final String NBT_STORED = "Stored";
        private static final String NBT_MAX = "Max";
        private final long[] storage;
        private final long[] maximums;
        private final BigInteger capacity;
        private int index;

        public YotTankFluidBank(List<IBatteryData> batteries) {
            storage = new long[batteries.size()];
            maximums = new long[batteries.size()];
            for (int i = 0; i < batteries.size(); i++) {
                maximums[i] = batteries.get(i).getCapacity();
            }
            capacity = summarize(maximums);
        }

        public YotTankFluidBank(NBTTagCompound storageTag) {
            int size = storageTag.getInteger(NBT_SIZE);
            storage = new long[size];
            maximums = new long[size];
            for (int i = 0; i < size; i++) {
                NBTTagCompound subtag = storageTag.getCompoundTag(String.valueOf(i));
                if (subtag.hasKey(NBT_STORED)) {
                    storage[i] = subtag.getLong(NBT_STORED);
                }
                maximums[i] = subtag.getLong(NBT_MAX);
            }

            capacity = summarize(maximums);
        }

        private NBTTagCompound writeToNBT(NBTTagCompound compound) {
            compound.setInteger(NBT_SIZE, storage.length);
            for (int i = 0; i < storage.length; i++) {
                NBTTagCompound subtag = new NBTTagCompound();
                if (storage[i] > 0) {
                    subtag.setLong(NBT_STORED, storage[i]);
                }
                subtag.setLong(NBT_MAX, maximums[i]);
                compound.setTag(String.valueOf(i), subtag);
            }
            return compound;
        }
        /**
         * Rebuild the power storage with a new list of batteries.
         * Will use existing stored power and try to map it onto new batteries.
         * If there was more power before the rebuild operation, it will be lost.
         */
        public YotTankFluidBank rebuild(@NotNull List<IBatteryData> batteries) {
            if (batteries.isEmpty()) {
                throw new IllegalArgumentException("Cannot rebuild Power Substation power bank with no batteries!");
            }
            YotTankFluidBank newStorage = new YotTankFluidBank(batteries);
            for (long stored : storage) {
                newStorage.fill(stored);
            }
            return newStorage;
        }

        /** @return Amount filled into storage */
        public long fill(long amount) {
            if (amount < 0) throw new IllegalArgumentException("Amount cannot be negative!");

            // ensure index
            if (index != storage.length - 1 && storage[index] == maximums[index]) {
                index++;
            }

            long maxFill = Math.min(maximums[index] - storage[index], amount);

            // storage is completely full
            if (maxFill == 0 && index == storage.length - 1) {
                return 0;
            }

            // fill this "battery" as much as possible
            storage[index] += maxFill;
            amount -= maxFill;

            // try to fill other "batteries" if necessary
            if (amount > 0 && index != storage.length - 1) {
                return maxFill + fill(amount);
            }

            // other fill not necessary, either because the storage is now completely full,
            // or we were able to consume all the energy in this "battery"
            return maxFill;
        }

        /** @return Amount drained from storage */
        public long drain(long amount) {
            if (amount < 0) throw new IllegalArgumentException("Amount cannot be negative!");

            // ensure index
            if (index != 0 && storage[index] == 0) {
                index--;
            }

            long maxDrain = Math.min(storage[index], amount);

            // storage is completely empty
            if (maxDrain == 0 && index == 0) {
                return 0;
            }

            // drain this "battery" as much as possible
            storage[index] -= maxDrain;
            amount -= maxDrain;

            // try to drain other "batteries" if necessary
            if (amount > 0 && index != 0) {
                index--;
                return maxDrain + drain(amount);
            }

            // other drain not necessary, either because the storage is now completely empty,
            // or we were able to drain all the energy from this "battery"
            return maxDrain;
        }

        public BigInteger getCapacity() {
            return capacity;
        }

        public BigInteger getStored() {
            return summarize(storage);
        }

        public boolean hasFluid() {
            for (long l : storage) {
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
