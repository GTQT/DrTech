package com.drppp.drtech.MetaTileEntities.muti.ecectric.store;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.drppp.drtech.Blocks.BlocksInit;
import com.drppp.drtech.Blocks.MetaBlocks.MetaCasing;
import com.drppp.drtech.Blocks.MetaBlocks.MetaGlasses;
import com.drppp.drtech.Client.Textures;
import com.drppp.drtech.MetaTileEntities.muti.mutipart.MetaTileEntityYotHatch;
import com.drppp.drtech.Utils.Datas;
import com.drppp.drtech.Utils.DrtechUtils;
import com.drppp.drtech.api.capability.DrtechCapabilities;
import gregtech.api.GregTechAPI;
import gregtech.api.capability.*;
import gregtech.api.capability.impl.EnergyContainerList;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.ClickButtonWidget;
import gregtech.api.gui.widgets.WidgetGroup;
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
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
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

import javax.annotation.Nonnull;
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

    public FluidStack getFluid() {
        return fluid;
    }

    private  FluidStack fluid;
    public IMultipleTankHandler inputFluidInventory;
    public IMultipleTankHandler outputFluidInventory;
    private YotTankFluidBank fluidBank;
    private int outputflag = 0;

    public YotTankFluidBank getFluidBank() {
        return fluidBank;
    }
    protected final ArrayList<MetaTileEntityYotHatch> mYottaHatch = new ArrayList<>();
    int time=0;
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
        data.setInteger("OutFlag",this.outputflag);
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
        this.outputflag = data.getInteger("OutFlag");
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
            time++;
            if (isWorkingEnabled() && time>20) {
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
                if(outputFluidInventory.getTanks()>0 && this.fluid!=null && this.outputflag==1)
                {

                    List<FluidStack> Outputs = new ArrayList<>();
                    for (int i = 0; i < outputFluidInventory.getTanks(); i++) {
                        if(outputFluidInventory.getTankAt(i).getFluid()!=null && !outputFluidInventory.getTankAt(i).getFluid().isFluidEqual(this.fluid))
                            continue;
                        long energyDebanked = fluidBank.drain(outputFluidInventory.getTankAt(i).getCapacity()-outputFluidInventory.getTankAt(i).getFluidAmount());
                        Outputs.add(new FluidStack(this.fluid.getFluid(), (int) energyDebanked));
                    }
                    GTTransferUtils.addFluidsToFluidHandler(outputFluidInventory ,false, Outputs);
                }
                if(!fluidBank.hasFluid())
                    fluid = null;
                time=0;
            }
        }
    }
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
        this.fluid = null;
        this.fluidBank.clearStore();
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
                        .or(abilities(DrtechCapabilities.YOT_HATCH).setMaxGlobalLimited(1))
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
                    IStoreData yot_parts = Datas.YOT_CASINGS.get(state);
                    if (yot_parts.getTier() != -1 && yot_parts.getCapacity().compareTo(BigInteger.ZERO)==1) {
                        String key = YOT_PART_HEADER + yot_parts.getStoreName();
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
                                this.fluid==null?"空":this.fluid.getLocalizedName())
                        );
                        tl.add(TextComponentUtil.translationWithColor(
                                TextFormatting.GRAY,
                                "drtech.multiblock.power_substation.output",this.outputflag==0?"禁用":"启用"));
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
        List<IStoreData> parts = new ArrayList<>();
        for (Map.Entry<String, Object> battery : context.entrySet()) {
            if (battery.getKey().startsWith(YOT_PART_HEADER) &&
                    battery.getValue() instanceof MetaTileEntityYotTank.YotPartMatchWrapper wrapper) {
                for (int i = 0; i < wrapper.amount; i++) {
                    parts.add(wrapper.partType);
                }
            }
            else if(battery.getKey().startsWith("Multi")  ) {
                HashSet set = (HashSet) battery.getValue();
                for (var s: set
                     ) {
                    if(s instanceof MetaTileEntityYotHatch)
                    {
                        MetaTileEntityYotHatch yotHatch = (MetaTileEntityYotHatch)s;
                        yotHatch.setYotTank(this);
                    }
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

        private final IStoreData partType;
        private int amount;

        public YotPartMatchWrapper(IStoreData partType) {
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
        private final BigInteger[] storage;
        private final BigInteger[] maximums;
        private final BigInteger capacity;
        private int index;

        public YotTankFluidBank(List<IStoreData> batteries) {
            storage = new BigInteger[batteries.size()];
            for (int i = 0; i < batteries.size(); i++) {
                storage[i] = BigInteger.ZERO;
            }
            maximums = new BigInteger[batteries.size()];
            for (int i = 0; i < batteries.size(); i++) {
                maximums[i] = batteries.get(i).getCapacity();
            }
            capacity = summarize(maximums);
        }

        public YotTankFluidBank(NBTTagCompound storageTag) {
            int size = storageTag.getInteger(NBT_SIZE);
            storage = new BigInteger[size];
            for (int i = 0; i < size; i++) {
                storage[i] = BigInteger.ZERO;
            }
            maximums = new BigInteger[size];
            for (int i = 0; i < size; i++) {
                NBTTagCompound subtag = storageTag.getCompoundTag(String.valueOf(i));
                if (subtag.hasKey(NBT_STORED)) {
                    storage[i] = new BigInteger(subtag.getString(NBT_STORED));
                }
                maximums[i] = new BigInteger(subtag.getString(NBT_MAX));
            }

            capacity = summarize(maximums);
        }

        private NBTTagCompound writeToNBT(NBTTagCompound compound) {
            compound.setInteger(NBT_SIZE, storage.length);
            for (int i = 0; i < storage.length; i++) {
                NBTTagCompound subtag = new NBTTagCompound();
                if (storage[i].compareTo(BigInteger.ZERO)==1) {
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
            if (amount < 0) throw new IllegalArgumentException("Amount cannot be negative!");
            if (index != storage.length - 1 && storage[index].compareTo(maximums[index])==0) {
                index++;
            }
            BigInteger maxFill = DrtechUtils.getBigIntegerMin(maximums[index].subtract(storage[index]), new BigInteger(String.valueOf(amount)));
            if (maxFill.compareTo(BigInteger.ZERO)==0 && index == storage.length - 1) {
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
            if (amount.compareTo(BigInteger.ZERO) ==-1) throw new IllegalArgumentException("Amount cannot be negative!");
            if (index != storage.length - 1 && storage[index].compareTo(maximums[index])==0) {
                index++;
            }
            BigInteger maxFill = DrtechUtils.getBigIntegerMin(maximums[index].subtract(storage[index]), new BigInteger(String.valueOf(amount)));
            if (maxFill.compareTo(BigInteger.ZERO)==0 && index == storage.length - 1) {
                return BigInteger.ZERO;
            }
            storage[index] = storage[index].add(maxFill);
            amount = amount.subtract(maxFill);
            if (amount.compareTo(BigInteger.ZERO)==1 && index != storage.length - 1) {
                return maxFill.add(fill(amount));
            }
            return maxFill;
        }
        /** @return Amount drained from storage */
        public long drain(long amount) {
            if (amount < 0) throw new IllegalArgumentException("Amount cannot be negative!");

            // ensure index
            if (index != 0 && storage[index].compareTo(BigInteger.ZERO) == 0) {
                index--;
            }

            BigInteger maxDrain = DrtechUtils.getBigIntegerMin(storage[index], new BigInteger(String.valueOf(amount)));

            // storage is completely empty
            if (maxDrain.compareTo(BigInteger.ZERO) == 0 && index == 0) {
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
                if (l.compareTo(BigInteger.ZERO)==1) return true;
            }
            return false;
        }

        private static BigInteger summarize(BigInteger[] values) {
            BigInteger retVal = BigInteger.ZERO;
            for (BigInteger value : values) {
                if(value!=null)
                    retVal = retVal.add(value);
            }
            return retVal;
        }
        public  void clearStore()
        {
            for (int i = 0; i < storage.length; i++) {
                storage[i] = BigInteger.ZERO;
            }
        }
    }
}
