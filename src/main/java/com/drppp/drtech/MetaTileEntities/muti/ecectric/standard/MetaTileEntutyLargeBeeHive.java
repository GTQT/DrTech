package com.drppp.drtech.MetaTileEntities.muti.ecectric.standard;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.drppp.drtech.Blocks.BlocksInit;
import com.drppp.drtech.Blocks.MetaBlocks.MetaCasing1;
import com.drppp.drtech.Client.Textures;
import com.drppp.drtech.MetaTileEntities.muti.ecectric.store.MetaTileEntityYotTank;
import forestry.api.apiculture.*;
import forestry.apiculture.genetics.Bee;
import gregtech.api.block.IHeatingCoilBlockStats;
import gregtech.api.capability.*;
import gregtech.api.capability.impl.EnergyContainerList;
import gregtech.api.capability.impl.ItemHandlerList;
import gregtech.api.metatileentity.IDataInfoProvider;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockWithDisplayBase;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.util.GTTransferUtils;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.ICubeRenderer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.drppp.drtech.Utils.DrtechUtils.readItemStackFromNBT;
import static com.drppp.drtech.Utils.DrtechUtils.writeItemStackToNBT;
import static forestry.api.apiculture.BeeManager.beeRoot;

public class MetaTileEntutyLargeBeeHive extends MultiblockWithDisplayBase implements IDataInfoProvider, IWorkable, IControllable {
    private boolean isActive, isWorkingEnabled = true;
    protected IEnergyContainer energyContainer = new EnergyContainerList(new ArrayList());
    protected ItemHandlerList itemImportInventory;
    protected ItemHandlerList itemExportInventory;
    public int process=0;
    public int maxProcess = 100;
    public MetaTileEntutyLargeBeeHive(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    public int getProgress() {
        return this.process;
    }

    @Override
    public int getMaxProgress() {
        return this.maxProcess;
    }

    @Override
    public @NotNull List<ITextComponent> getDataInfo() {
        return null;
    }


    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        this.energyContainer = new EnergyContainerList(new ArrayList());
        this.itemImportInventory =  new ItemHandlerList(Collections.emptyList());
        this.itemExportInventory =  new ItemHandlerList(Collections.emptyList());
    }
    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        List<IEnergyContainer> energyContainer = new ArrayList(this.getAbilities(MultiblockAbility.OUTPUT_ENERGY));
        //energyContainer.addAll(this.getAbilities(MultiblockAbility.OUTPUT_LASER));
        this.energyContainer=new EnergyContainerList(energyContainer);
        this.itemImportInventory = new ItemHandlerList(getAbilities(MultiblockAbility.IMPORT_ITEMS));
        this.itemExportInventory = new ItemHandlerList(getAbilities(MultiblockAbility.EXPORT_ITEMS));

    }
    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntutyLargeBeeHive(this.metaTileEntityId);
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
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        isActive = data.getBoolean("isActive");
        isWorkingEnabled = data.getBoolean("isWorkingEnabled");

    }
    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (capability == GregtechTileCapabilities.CAPABILITY_CONTROLLABLE) {
            return GregtechTileCapabilities.CAPABILITY_CONTROLLABLE.cast(this);
        }
        return super.getCapability(capability, side);
    }
    @SideOnly(Side.CLIENT)
    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        getFrontOverlay().renderOrientedState(renderState, translation, pipeline, getFrontFacing(), this.isActive(),
                this.isWorkingEnabled());
    }
    protected IBlockState getCasingState() {
        return BlocksInit.COMMON_CASING1.getState(MetaCasing1.MetalCasingType.NUCLEAR_PART_CASING);
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return Textures.NUCLEAR_PART_CASING;
    }
    @Override
    protected @NotNull BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("XXX", "XXX", "XXX", "XXX")
                .aisle("XXX", "X#X", "X#X", "XXX")
                .aisle("XXX", "X#X", "X#X", "XXX")
                .aisle("XXX", "XSX", "XXX", "XXX")
                .where('S', selfPredicate())
                .where('X', states(getCasingState())
                        .or(abilities(MultiblockAbility.INPUT_ENERGY).setMaxGlobalLimited(1).setPreviewCount(1)
                                .or(abilities(MultiblockAbility.IMPORT_ITEMS).setMaxGlobalLimited(1).setPreviewCount(1)
                                .or(abilities(MultiblockAbility.EXPORT_ITEMS).setMaxGlobalLimited(1).setPreviewCount(1)
                                        .or(abilities(MultiblockAbility.MAINTENANCE_HATCH).setExactLimit(1)))
                        )))
                .where('#', any())
                .build();
    }

    @Override
    protected void updateFormedValid() {
        if(process++>=maxProcess)
        {
            process=0;
            for (int i = 0; i < itemImportInventory.getSlots(); i++) {
                ItemStack is = itemImportInventory.getStackInSlot(i);
                EnumBeeType beeType = beeRoot.getType(is);
                if(beeType == EnumBeeType.QUEEN)
                {
                    IBee bee = beeRoot.getMember(is);
                    BeeSimulator bs = new BeeSimulator(is.copy() , this.getWorld(), 16);
                    List<ItemStack> listdrops = new ArrayList<>();
                    for (var drop :bs.drops)
                    {
                        listdrops.add(drop.get((int)drop.getAmount(bee.getGenome().getSpeed())));
                    }
                    for (var drop :bs.specialDrops)
                    {
                        listdrops.add(drop.get((int)drop.getAmount(bee.getGenome().getSpeed())));
                    }
                    GTTransferUtils.addItemsToItemHandler(this.itemExportInventory, false, listdrops);
                }
            }
        }
    }



    private static class BeeSimulator {

        public final ItemStack queenStack;
        boolean isValid;
        List<BeeDrop> drops = new ArrayList<>();
        List<BeeDrop> specialDrops = new ArrayList<>();
        float beeSpeed;

        float maxBeeCycles;
        String flowerType;
        String flowerTypeDescription;
        private static IBeekeepingMode mode;

        public BeeSimulator(ItemStack queenStack, World world, float t) {
            isValid = false;
            this.queenStack = queenStack.copy();
            this.queenStack.setCount(1);
            generate(world, t);
            isValid = true;
            queenStack.setCount(queenStack.getCount()-1);
        }

        public void generate(World world, float t) {
            if (mode == null) mode = beeRoot.getBeekeepingMode(world);
            drops.clear();
            specialDrops.clear();
            if (beeRoot.getType(this.queenStack) != EnumBeeType.QUEEN) return;
            IBee queen = beeRoot.getMember(this.queenStack);
            IBeeModifier beeModifier = mode.getBeeModifier();
            float mod = beeModifier.getLifespanModifier(null, null, 1.f);
            int h = queen.getMaxHealth();
            maxBeeCycles = (float) h / (1.f / mod);
            IBeeGenome genome = queen.getGenome();
            this.flowerType = genome.getFlowerProvider()
                    .getFlowerType();
            this.flowerTypeDescription = genome.getFlowerProvider()
                    .getDescription();
            IAlleleBeeSpecies primary = genome.getPrimary();
            beeSpeed = genome.getSpeed();
            genome.getPrimary()
                    .getProductChances()
                    .forEach((key, value) -> drops.add(new BeeDrop(key, value, beeSpeed, t)));
            genome.getSecondary()
                    .getProductChances()
                    .forEach((key, value) -> drops.add(new BeeDrop(key, value / 2.f, beeSpeed, t)));
            primary.getSpecialtyChances()
                    .forEach((key, value) -> specialDrops.add(new BeeDrop(key, value, beeSpeed, t)));
        }

        public BeeSimulator(NBTTagCompound tag) {
            queenStack = readItemStackFromNBT(tag.getCompoundTag("queenStack"));
            isValid = tag.getBoolean("isValid");
            drops = new ArrayList<>();
            specialDrops = new ArrayList<>();
            for (int i = 0, isize = tag.getInteger("dropssize"); i < isize; i++)
                drops.add(new BeeDrop(tag.getCompoundTag("drops" + i)));
            for (int i = 0, isize = tag.getInteger("specialDropssize"); i < isize; i++)
                specialDrops.add(new BeeDrop(tag.getCompoundTag("specialDrops" + i)));
            beeSpeed = tag.getFloat("beeSpeed");
            maxBeeCycles = tag.getFloat("maxBeeCycles");
            if (tag.hasKey("flowerType") && tag.hasKey("flowerTypeDescription")) {
                flowerType = tag.getString("flowerType");
                flowerTypeDescription = tag.getString("flowerTypeDescription");
            } else {
                IBee queen = beeRoot.getMember(this.queenStack);
                IBeeGenome genome = queen.getGenome();
                this.flowerType = genome.getFlowerProvider()
                        .getFlowerType();
                this.flowerTypeDescription = genome.getFlowerProvider()
                        .getDescription();
            }
        }

        public NBTTagCompound toNBTTagCompound() {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setTag("queenStack", writeItemStackToNBT(queenStack));
            tag.setBoolean("isValid", isValid);
            tag.setInteger("dropssize", drops.size());
            for (int i = 0; i < drops.size(); i++) tag.setTag(
                    "drops" + i,
                    drops.get(i)
                            .toNBTTagCompound());
            tag.setInteger("specialDropssize", specialDrops.size());
            for (int i = 0; i < specialDrops.size(); i++) tag.setTag(
                    "specialDrops" + i,
                    specialDrops.get(i)
                            .toNBTTagCompound());
            tag.setFloat("beeSpeed", beeSpeed);
            tag.setFloat("maxBeeCycles", maxBeeCycles);
            tag.setString("flowerType", flowerType);
            tag.setString("flowerTypeDescription", flowerTypeDescription);
            return tag;
        }

        public ItemStack createIgnobleCopy() {
            IBee princess = beeRoot.getMember(queenStack);
            princess.setIsNatural(false);
            return beeRoot.getMemberStack(princess, EnumBeeType.PRINCESS);
        }

        public void updateTVar(World world, float t) {
            if (mode == null) mode = beeRoot.getBeekeepingMode(world);
            drops.forEach(d -> d.updateTVar(t));
            specialDrops.forEach(d -> d.updateTVar(t));
        }

        private static class BeeDrop {

            private static final float MAX_PRODUCTION_MODIFIER_FROM_UPGRADES = 17.19926784f; // 4*1.2^8
            final ItemStack stack;
            double amount;
            final ItemStack id;

            final float chance;
            final float beeSpeed;
            float t;

            public BeeDrop(ItemStack stack, float chance, float beeSpeed, float t) {
                this.stack = stack;
                this.chance = chance;
                this.beeSpeed = beeSpeed;
                this.t = t;
                id = this.stack.copy();
                evaluate();
            }

            public void updateTVar(float t) {
                if (this.t != t) {
                    this.t = t;
                    evaluate();
                }
            }

            public void evaluate() {

                this.amount = getFinalChance();
            }
            public double getFinalChance()
            {
                double d = (1+(MAX_PRODUCTION_MODIFIER_FROM_UPGRADES)/6)*(Math.pow(chance,0.5))*2*(1+beeSpeed)+Math.pow(chance,Math.pow(chance,0.333))-3;
                return d;
            }
            public double getAmount(double speedModifier) {
                return amount * speedModifier;
            }

            public ItemStack get(int amount) {
                ItemStack r = stack.copy();
                r.setCount(amount);
                return r;
            }

            public BeeDrop(NBTTagCompound tag) {
                stack = readItemStackFromNBT(tag.getCompoundTag("stack"));
                chance = tag.getFloat("chance");
                beeSpeed = tag.getFloat("beeSpeed");
                t = tag.getFloat("t");
                amount = tag.getDouble("amount");
                id = stack.copy();
            }

            public NBTTagCompound toNBTTagCompound() {
                NBTTagCompound tag = new NBTTagCompound();
                tag.setTag("stack", writeItemStackToNBT(stack));
                tag.setFloat("chance", chance);
                tag.setFloat("beeSpeed", beeSpeed);
                tag.setFloat("t", t);
                tag.setDouble("amount", amount);
                return tag;
            }

            @Override
            public int hashCode() {
                return id.hashCode();
            }
        }
    }
}
