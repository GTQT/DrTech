package com.drppp.drtech.common.MetaTileEntities.single;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.drppp.drtech.Client.Textures;
import com.drppp.drtech.api.ItemHandler.InOutItemStackHandler;
import com.drppp.drtech.api.ItemHandler.OnlyBeesStackhandler;
import com.drppp.drtech.api.ItemHandler.OnlyUpgradeStackhandler;
import com.drppp.drtech.api.Utils.DrtechUtils;
import com.drppp.drtech.api.Utils.GT_ApiaryUpgrade;
import com.drppp.drtech.api.Utils.ItemId;
import com.drppp.drtech.api.modularui.MetaTileEntityModularui;
import com.google.common.collect.ImmutableSet;
import com.mojang.authlib.GameProfile;
import forestry.apiculture.ModuleApiculture;
import forestry.apiculture.items.ItemBeeGE;
import forestry.apiculture.items.ItemRegistryApiculture;
import gregtech.api.GTValues;
import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IWorkable;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.*;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.util.GTTransferUtils;
import gregtech.client.renderer.ICubeRenderer;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Biomes;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import forestry.api.apiculture.BeeManager;
import forestry.api.apiculture.EnumBeeChromosome;
import forestry.api.apiculture.EnumBeeType;
import forestry.api.apiculture.FlowerManager;
import forestry.api.apiculture.IAlleleBeeEffect;
import forestry.api.apiculture.IAlleleBeeSpecies;
import forestry.api.apiculture.IApiaristTracker;
import forestry.api.apiculture.IBee;
import forestry.api.apiculture.IBeeGenome;
import forestry.api.apiculture.IBeeHousing;
import forestry.api.apiculture.IBeeHousingInventory;
import forestry.api.apiculture.IBeeListener;
import forestry.api.apiculture.IBeeModifier;
import forestry.api.apiculture.IBeeRoot;
import forestry.api.apiculture.IBeekeepingLogic;
import forestry.api.apiculture.IBeekeepingMode;
import forestry.api.arboriculture.EnumGermlingType;
import forestry.api.core.BiomeHelper;
import forestry.api.core.EnumHumidity;
import forestry.api.core.EnumTemperature;
import forestry.api.core.ForestryAPI;
import forestry.api.core.IErrorLogic;
import forestry.api.core.IErrorState;
import forestry.api.genetics.AlleleManager;
import forestry.api.genetics.IEffectData;
import forestry.api.genetics.IIndividual;
import forestry.core.errors.EnumErrorCode;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.*;

public class MetaTileEntityIndustrialApiary extends MetaTileEntityModularui implements IWorkable ,
        IBeeHousing, IBeeHousingInventory, IErrorLogic, IBeeModifier, IBeeListener {
    protected final ICubeRenderer renderer;
    public int i=0;
    public boolean isActive=true;
    public boolean isWorkingEnabled=true;
    public int mProgresstime=0;
    public int mMaxProgresstime=100;
    public int progressPer=0;
    public int mEUt=0;
    ItemStackHandler inventoryBees = new OnlyBeesStackhandler(2);
    ItemStackHandler inventoryUpgrade = new OnlyUpgradeStackhandler(4);
    ItemStackHandler inventoryOutput = new InOutItemStackHandler(12,false);
    public  ItemStack[] mOutputItems = new ItemStack[12];
    public static final int beeCycleLength = 550;
    public static final int baseEUtUsage = 37;
    private static final int queen = 0;
    private static final int drone = 1;
    private static final int upgradeSlot = 0;
    private static final int upgradeSlotCount = 4;
    final IBeeRoot beeRoot = (IBeeRoot) AlleleManager.alleleRegistry.getSpeciesRoot("rootBees");

    public int mSpeed = 0;
    public boolean mLockedSpeed = true;
    public boolean mAutoQueen = true;

    private ItemStack usedQueen = null;
    private IBee usedQueenBee = null;
    private IEffectData[] effectData = new IEffectData[2];
    public MetaTileEntityIndustrialApiary(ResourceLocation metaTileEntityId, ICubeRenderer renderer) {
        super(metaTileEntityId, GTValues.UHV);
        this.renderer = renderer;
    }
    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityIndustrialApiary(this.metaTileEntityId,renderer);
    }
    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        ModularUI.Builder builder;
        builder = ModularUI.builder(Textures.BACKGROUND, 176, 166);
        builder.slot(inventoryBees,0,36,21,true,true,Textures.BEE_QUEEN_LOGO);
        builder.slot(inventoryBees,1,36,41,true,true,Textures.BEE_DRONE_LOGO);
        for (int j = 0; j < inventoryUpgrade.getSlots(); j++) {
            builder.slot(inventoryUpgrade,j,61+j%2*18, 23+j/2*18,true,true, gregtech.api.gui.GuiTextures.SLOT);
        }
        for (int j = 0; j < inventoryOutput.getSlots(); j++) {
            builder.slot(inventoryOutput,j,107+j%3*18,8+ j/3*18,true,false, gregtech.api.gui.GuiTextures.SLOT);
        }
        builder.widget(new gregtech.api.gui.widgets.ProgressWidget(this::getProgressPercent, 70, 5, 20, 20, gregtech.api.gui.GuiTextures.PROGRESS_BAR_ARROW, gregtech.api.gui.widgets.ProgressWidget.MoveType.HORIZONTAL));
        ImageWidget logo = new ImageWidget(70,62, 17, 17, gregtech.api.gui.GuiTextures.GREGTECH_LOGO).setIgnoreColor(true);
        builder.widget(logo);
        var scroll = new ScrollableListWidget(8,62,60,22);
        AdvancedTextWidget textWidget = new AdvancedTextWidget(0,0,this::addErrorText,0x52135);
        scroll.addWidget(textWidget);
        builder.widget(scroll);
        var btn = new ToggleButtonWidget(13,18, 18, 18,
                Textures.CROSS, this::isWorkingEnabled,this::setWorkingEnabled)
                .setTooltipText("drtech.gui.industrial_apiary.tooltip.1")
                .shouldUseBaseBackground();
        if(isWorking())
        {
            btn.setButtonTexture(Textures.CHECK_MARK);
            btn.updateScreenOnFrame();
        }
        else {
            btn.setButtonTexture(Textures.CROSS);
            btn.updateScreenOnFrame();
        }
        builder.widget(btn);
        builder.widget(new ToggleButtonWidget(13,38, 18, 18,
                GuiTextures.ARROW_DOUBLE, this::isActive,this::cancelProcess)
                .setTooltipText("drtech.gui.industrial_apiary.tooltip.2")
                .shouldUseBaseBackground());
        builder.bindPlayerInventory(entityPlayer.inventory,86);
        return builder.build(this.getHolder(),entityPlayer);
    }
    protected void addErrorText(List<ITextComponent> textList) {
        textList.add(new TextComponentTranslation("drtech.industrial_apiary.tootip.1",this.mEUt));
        textList.add(new TextComponentTranslation("drtech.industrial_apiary.tootip.2",getTemperature()));
        textList.add(new TextComponentTranslation("drtech.industrial_apiary.tootip.3",getHumidity()));
        if(!hasErrors())
        {
            textList.add(new TextComponentTranslation("drtech.industrial_apiary.tootip.5"));
        }
        else
        {
            textList.add(new TextComponentTranslation("drtech.industrial_apiary.tootip.4"));
            mErrorStates.forEach(info->{
                textList.add(new TextComponentTranslation(info.getUnlocalizedDescription()));
            });
        }
    }
    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        this.renderOverlays(renderState, translation, pipeline);

    }
    protected void renderOverlays(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        this.renderer.renderOrientedState(renderState, translation, pipeline, this.getFrontFacing(), isActive(), isWorking());
    }

    @Override
    public void writeInitialSyncData(@NotNull PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeBoolean(this.isActive);
        buf.writeBoolean(this.isWorkingEnabled);
        if(usedQueen==null)
            usedQueen=ItemStack.EMPTY;
        buf.writeItemStack(usedQueen);
        buf.writeInt(mEUt);
        writeData(buf);
    }

    @Override
    public void receiveInitialSyncData(@NotNull PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        isActive = buf.readBoolean();
        isWorkingEnabled = buf.readBoolean();
        try {
            usedQueen=buf.readItemStack();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        mEUt = buf.readInt();
        readData(buf);
    }
    @Override
    public void setWorkingEnabled(boolean b) {
        this.isWorkingEnabled = b;
        markDirty();
        writeCustomData(GregtechDataCodes.WORKING_ENABLED, buf -> buf.writeBoolean(isWorkingEnabled));
    }

    public void setActive(boolean b) {
        this.isActive=b;
        markDirty();
        writeCustomData(GregtechDataCodes.WORKABLE_ACTIVE, buf -> buf.writeBoolean(isActive));
    }
    public void setmEut(int b) {
        this.mEUt=b;
        markDirty();
        writeCustomData(4800, buf -> buf.writeInt(this.mEUt));
    }
    public void setUsedQueen(ItemStack b) {
        if(b==null)
            b=ItemStack.EMPTY;
        this.usedQueen=b;
        markDirty();
        writeCustomData(4801, buf -> buf.writeItemStack(this.usedQueen));
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
        }else if(dataId==4800)
        {
            mEUt = buf.readInt();
        }else if(dataId==4801)
        {
            try {
                usedQueen = buf.readItemStack();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }else if(dataId==4802)
        {
            readData(buf);
        }
//        else if(dataId==4803)
//        {
//            this.error = buf.readString(32700);
//        }
    }
    @Override
    public void update() {
        super.update();
        if (getWorld().isRemote) {
            if (isWorking()) {
                if (usedQueen != null && !usedQueen.isEmpty()) {
                    if (this.getOffsetTimer() % 2 == 0) {
                        // FX on client, effect on server
                        final IBee bee = beeRoot.getMember(usedQueen);
                        effectData = bee.doFX(effectData, this);
                    }
                }
            }
        }
        if (!getWorld().isRemote) {
            if (!isWorking() && isActive()) {
                if (this.energyContainer.getEnergyStored()>mEUt) {
                    final boolean check = checkRecipe();
                    if (check ) {
                        setWorkingEnabled(true);
                    }
                }
            } else {

                if (this.mProgresstime < 0) {
                    this.mProgresstime++;
                    return;
                }
                if (this.energyContainer.addEnergy(-this.mEUt)>=0) {
                    setWorkingEnabled(false);
                    return;
                }
                if (this.hasErrors()) {
                    if (getOffsetTimer() % 100 == 0) if (!canWork(usedQueen)) setWorkingEnabled(false);
                    return;
                }
                this.mProgresstime++;
                if (usedQueen != null && !usedQueen.isEmpty()) {
                    if (usedQueenBee == null) usedQueenBee = beeRoot.getMember(usedQueen);
                    doEffect();
                    if (!retrievingPollenInThisOperation && floweringMod > 0f
                            && this.mProgresstime % pollinationDelay == 0) {
                        if (retrievedpollen == null) retrievedpollen = usedQueenBee.retrievePollen(this);
                        if (retrievedpollen != null && (usedQueenBee.pollinateRandom(this, retrievedpollen)
                                || this.mProgresstime % (pollinationDelay * 5) == 0)) retrievedpollen = null;
                    }
                }

                if (this.mProgresstime % 100 == 0) {
                    if (!canWork(usedQueen)) {
                        setWorkingEnabled(false);
                        return;
                    }
                }

                if (this.mProgresstime >= this.mMaxProgresstime) {
                    if (usedQueenBee != null && !usedQueen.isEmpty()) doAcceleratedEffects();
                    updateModifiers();
                    for (int i = 0; i < mOutputItems.length; i++)
                    {
                        if (mOutputItems[i] != null && !mOutputItems[i].isEmpty()) for (int j = 0; j < mOutputItems.length; j++) {
                            if (j == 0 && isAutomated)
                            {
                                if (beeRoot.isMember(mOutputItems[i], EnumBeeType.QUEEN) || beeRoot.isMember(mOutputItems[i], EnumBeeType.PRINCESS))
                                {
                                   inventoryBees.insertItem(queen,mOutputItems[i].copy(),false) ;
                                    mOutputItems[i]=null;
                                } else if (beeRoot.isMember(mOutputItems[i], EnumBeeType.DRONE))
                                {
                                     if(inventoryBees.insertItem(drone,mOutputItems[i].copy(),true) == ItemStack.EMPTY)
                                     {
                                         inventoryBees.insertItem(drone,mOutputItems[i].copy(),false);
                                         mOutputItems[i]=null;
                                     }
                                }
                            } else if (mAutoQueen && i == 0 && j == 0 && beeRoot.isMember(mOutputItems[0], EnumBeeType.QUEEN))
                            {
                                inventoryBees.insertItem(queen,mOutputItems[i].copy(),false);
                                mOutputItems[0]=null;
                                break;
                            }
                        }
                    }
                    for (int j = 0; j < mOutputItems.length; j++) {
                        if(mOutputItems!=null && mOutputItems[j]!=null && !mOutputItems[j].isEmpty())
                            GTTransferUtils.insertItem(inventoryOutput,mOutputItems[j].copy(),false);
                    }
                    Arrays.fill(mOutputItems, null);
                    mEUt = 0;
                    setmEut(mEUt);
                    mProgresstime = 0;
                    mMaxProgresstime = 0;
                    setUsedQueen(null);
                    setWorkingEnabled(false);
                    if (this.energyContainer.removeEnergy(this.mEUt)>0 && checkRecipe())
                        setWorkingEnabled(true);
                }
            }
            progressPer = (int)(((float)mProgresstime / (float)mMaxProgresstime) * 100);
        }
    }
    private float getFinalChance(float baseChance, float speed, float prodMod, float modifier) {
        double finalchance = (1+modifier/6)*(Math.pow(baseChance,0.5))*2f*(1+speed)+Math.pow(prodMod,Math.pow(baseChance,0.333f))-3;
        return (float) finalchance;
    }

    boolean retrievingPollenInThisOperation = false;
    IIndividual retrievedpollen = null;
    int pollinationDelay = 100;
    float usedBeeLife = 0f;
    public boolean checkRecipe() {
        updateModifiers();
        if (canWork()) {

            final ItemStack queen = getQueen();
            usedQueen = queen.copy();
            setUsedQueen(usedQueen);
            if (beeRoot.getType(queen) == EnumBeeType.QUEEN) {
                final IBee bee = beeRoot.getMember(queen);
                usedQueenBee = bee;

                // LIFE CYCLES

                float mod = this.getLifespanModifier(null, null, 1.f);
                final IBeekeepingMode mode = beeRoot.getBeekeepingMode(this.getWorld());
                final IBeeModifier beemodifier = mode.getBeeModifier();
                mod *= beemodifier.getLifespanModifier(null, null, 1.f);
                final int h = bee.getHealth();
                mod = 1.f / mod;
                final float cycles = h / mod;

                // PRODUCTS

                final HashMap<ItemId, ItemStack> pollen = new HashMap<>();

                if (isRetrievingPollen && floweringMod > 0f) {
                    final int icycles = (int) cycles
                            + (getWorld().rand.nextFloat() < (cycles - (float) ((int) cycles)) ? 1 : 0);
                    for (int z = 0; z < icycles; z++) {
                        final IIndividual p = bee.retrievePollen(this);
                        if (p != null) {
                            final ItemStack s = p.getGenome()
                                    .getSpeciesRoot()
                                    .getMemberStack(p, EnumGermlingType.POLLEN);
                            if (s != null) {
                                final ItemId id = DrtechUtils.ItemIdManager.createNoCopy(s);
                                pollen.computeIfAbsent(id, k -> {
                                    final ItemStack ns = s.copy();
                                    ns.setCount(0);
                                    return ns;
                                });
                                pollen.get(id).setCount(pollen.get(id).getCount()+ s.getCount());
                            }
                        }
                    }
                }

                retrievedpollen = null;
                retrievingPollenInThisOperation = isRetrievingPollen;
                final IBeeGenome genome = bee.getGenome();
                final IAlleleBeeSpecies primary = genome.getPrimary();
                final IAlleleBeeSpecies secondary = genome.getSecondary();

                final float speed = genome.getSpeed();
                final float prodMod = getProductionModifier(null, 0f) + beemodifier.getProductionModifier(null, 0f);

                final HashMap<ItemId, Float> drops = new HashMap<>();
                final HashMap<ItemId, ItemStack> dropstacks = new HashMap<>();
                
                for (Map.Entry<ItemStack, Float> entry : primary.getProductChances()
                        .entrySet()) {
                    final ItemId id = DrtechUtils.ItemIdManager.createNoCopy(entry.getKey());
                    drops.merge(
                            id,
                            this.getFinalChance(entry.getValue(), speed, prodMod, 8f) * (float) entry.getKey().getCount()
                                    * cycles,
                            Float::sum);
                    dropstacks.computeIfAbsent(id, k -> entry.getKey());
                }
                for (Map.Entry<ItemStack, Float> entry : secondary.getProductChances()
                        .entrySet()) {
                    final ItemId id = DrtechUtils.ItemIdManager.createNoCopy(entry.getKey());
                    drops.merge(
                            id,
                            this.getFinalChance(entry.getValue() / 2f, speed, prodMod, 8f) * (float) entry.getKey().getCount()
                                    * cycles,
                            Float::sum);
                    dropstacks.computeIfAbsent(id, k -> entry.getKey());
                }
                if (primary.isJubilant(genome, this) && secondary.isJubilant(genome, this))
                    for (Map.Entry<ItemStack, Float> entry : primary.getSpecialtyChances()
                            .entrySet()) {
                        final ItemId id = DrtechUtils.ItemIdManager.createNoCopy(entry.getKey());
                        drops.merge(
                                id,
                                this.getFinalChance(entry.getValue(), speed, prodMod, 8f)
                                        * (float) entry.getKey().getCount()
                                        * cycles,
                                Float::sum);
                        dropstacks.computeIfAbsent(id, k -> entry.getKey());
                    }

                int i = 0;
                final int imax = mOutputItems.length;

                final IApiaristTracker breedingTracker = beeRoot.getBreedingTracker(getWorld(), getOwner());

                if (!bee.canSpawn()) {
                    final ItemStack convert =  new ItemBeeGE(EnumBeeType.PRINCESS).getItemStack();
                    final NBTTagCompound nbttagcompound = new NBTTagCompound();
                    queen.writeToNBT(nbttagcompound);
                    convert.setTagCompound(nbttagcompound);
                    this.mOutputItems[i++] = convert;
                } else {
                    final IBee b = bee.spawnPrincess(this);
                    if (b != null) {
                        final ItemStack princess = beeRoot.getMemberStack(b, EnumBeeType.PRINCESS);
                        breedingTracker.registerPrincess(b);
                        this.mOutputItems[i++] = princess;
                    }
                    final List<IBee> d = bee.spawnDrones(this);
                    if (d != null && d.size() > 0) {
                        final HashMap<ItemId, ItemStack> drones = new HashMap<>(d.size());
                        for (IBee dr : d) {
                            final ItemStack drone = beeRoot.getMemberStack(dr, EnumBeeType.DRONE);
                            breedingTracker.registerDrone(dr);
                            final ItemId drid = DrtechUtils.ItemIdManager.createNoCopy(drone);
                            if (drones.containsKey(drid)) drones.get(drid).setCount(drones.get(drid).getCount() + drone.getCount());
                            else {
                                this.mOutputItems[i++] = drone;
                                drones.put(drid, drone);
                            }
                        }
                    }
                }

                final int imin = i;

                setQueen(ItemStack.EMPTY);

                for (Map.Entry<ItemId, Float> entry : drops.entrySet()) {
                    final ItemStack s = dropstacks.get(entry.getKey())
                            .copy();
                    s.setCount(entry.getValue()
                            .intValue()
                            + (getWorld().rand.nextFloat() < (entry.getValue() - (float) entry.getValue()
                            .intValue()) ? 1 : 0));
                    if (s.getCount() > 0 && i < imax) while (true) {
                        if (s.getCount() <= s.getMaxStackSize()) {
                            this.mOutputItems[i++] = s;
                            break;
                        } else this.mOutputItems[i++] = s.splitStack(s.getMaxStackSize());
                        if (i >= imax) break;
                    }
                }

                for (ItemStack s : pollen.values()) if (i < imax) this.mOutputItems[i++] = s;
                else break;

                // Overclock

                usedBeeLife = cycles * (float) beeCycleLength;
                this.mMaxProgresstime = (int) usedBeeLife;
                final int timemaxdivider = this.mMaxProgresstime / 100;
                final int useddivider = 1 << this.mSpeed;
                int actualdivider = useddivider;
                this.mMaxProgresstime /= Math.min(actualdivider, timemaxdivider);
                actualdivider /= Math.min(actualdivider, timemaxdivider);
                for (i--; i >= imin; i--) this.mOutputItems[i].setCount(this.mOutputItems[i].getCount() * actualdivider);

                pollinationDelay = Math.max((int) (this.mMaxProgresstime / cycles), 20); // don't run too often

                this.mProgresstime = 0;
                this.mEUt = (int) ((float) baseEUtUsage * this.energyMod * useddivider);
                if (useddivider == 2) this.mEUt += 32;
                else if (useddivider > 2) this.mEUt += (32 * (useddivider << (this.mSpeed - 2)));
                setmEut(this.mEUt);
            } else {
                // Breeding time
                retrievingPollenInThisOperation = true; // Don't pollinate when breeding

                this.mMaxProgresstime = 100;
                this.mProgresstime = 0;
                final int useddivider = Math.min(100, 1 << this.mSpeed);
                this.mMaxProgresstime /= useddivider;
                this.mEUt = (int) ((float) baseEUtUsage * this.energyMod * useddivider);
                if (useddivider == 2) this.mEUt += 32;
                else if (useddivider > 2) this.mEUt += (32 * (useddivider << (this.mSpeed - 2)));
                setmEut(this.mEUt);
                final IBee princess = beeRoot.getMember(getQueen());
                usedQueenBee = princess;
                final IBee drone = beeRoot.getMember(getDrone());
                princess.mate(drone);
                final NBTTagCompound nbttagcompound = new NBTTagCompound();
                princess.writeToNBT(nbttagcompound);
                ItemRegistryApiculture apicultureItems = ModuleApiculture.getItems();
                this.mOutputItems[0] = apicultureItems.beeQueenGE.getItemStack();
                this.mOutputItems[0].setTagCompound(nbttagcompound);
                beeRoot.getBreedingTracker(getWorld(), getOwner()).registerQueen(princess);

                setQueen(ItemStack.EMPTY);
                getDrone().setCount(getDrone().getCount()-1);
                if (getDrone().getCount() == 0) setDrone(ItemStack.EMPTY);
            }

            return true;
        }

        return false;
    }
    public void cancelProcess(boolean flag) {
        if (!flag && !this.getWorld().isRemote && usedQueen != null && !usedQueen.isEmpty() && beeRoot.isMember(usedQueen, EnumBeeType.QUEEN)) {
            Arrays.fill(mOutputItems, null);
            setmEut(0);
            mProgresstime = 0;
            mMaxProgresstime = 0;
            setActive(false);
            if(inventoryBees.getStackInSlot(queen).isEmpty())
                setQueen(usedQueen);
            else
                GTTransferUtils.insertItem(inventoryOutput,usedQueen,false);
            setWorkingEnabled(false);
            setUsedQueen(null);
        }else {
            setActive(flag);
            setUsedQueen(null);
        }
    }
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setBoolean("isActive", isActive);
        data.setBoolean("isWorkingEnabled", isWorkingEnabled);
        data.setInteger("Progresstime",this.mProgresstime);
        data.setInteger("maxProgresstime",this.mMaxProgresstime);
        data.setTag("invBees",inventoryBees.serializeNBT());
        data.setTag("invUpgrade",inventoryUpgrade.serializeNBT());
        data.setTag("invOutputs",inventoryOutput.serializeNBT());
        data.setInteger("mEut",mEUt);
        data.setInteger("mSpeed",mSpeed);
        NBTTagCompound tag = new NBTTagCompound();
        if(usedQueen!=null )
        {
            usedQueen.writeToNBT(tag);
            data.setTag("usedQueen",tag);
        }
        data.setBoolean("autoQueen",mAutoQueen);
        NBTTagList nbtTagList = new NBTTagList();
        for (int i = 0; i < mOutputItems.length; i++) {
            if (mOutputItems[i]!=null && !mOutputItems[i].isEmpty()) {
                NBTTagCompound itemTag = new NBTTagCompound();
                itemTag.setInteger("Slot", i);
                mOutputItems[i].writeToNBT(itemTag);
                nbtTagList.appendTag(itemTag);
            }
        }
        data.setTag("mOutputItems", nbtTagList);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        isActive = data.getBoolean("isActive");
        isWorkingEnabled = data.getBoolean("isWorkingEnabled");
        mProgresstime = data.getInteger("Progresstime");
        mMaxProgresstime = data.getInteger("maxProgresstime");
        inventoryBees.deserializeNBT(data.getCompoundTag("invBees"));
        inventoryUpgrade.deserializeNBT(data.getCompoundTag("invUpgrade"));
        inventoryOutput.deserializeNBT(data.getCompoundTag("invOutputs"));
        mEUt = data.getInteger("mEut");
        mSpeed = data.getInteger("mSpeed");
        if(data.hasKey("usedQueen"))
        {
            usedQueen = new ItemStack(data.getCompoundTag("usedQueen"));
            if (usedQueenBee == null || usedQueen.isEmpty()) usedQueenBee = beeRoot.getMember(usedQueen);
        }
        mAutoQueen = data.getBoolean("autoQueen");
        for (int i = 0; i < 12; i++) {
            mOutputItems[i] = ItemStack.EMPTY;
        }
        NBTTagList nbtTagList = data.getTagList("mOutputItems", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < nbtTagList.tagCount(); i++) {
            NBTTagCompound itemTag = nbtTagList.getCompoundTagAt(i);
            int slot = itemTag.getInteger("Slot");
            if (slot >= 0 && slot < 12) {
                mOutputItems[slot] = new ItemStack(itemTag);
            }
        }
    }

    @Override
    public boolean isWorkingEnabled() {
        return this.isWorkingEnabled;
    }

    @Override
    public boolean isActive() {
        return this.isActive;
    }

    public boolean isWorking() {
        return this.isActive &&  this.isWorkingEnabled;
    }

    @Override
    public int getProgress() {
        return this.mProgresstime;
    }

    @Override
    public int getMaxProgress() {
        return mMaxProgresstime;
    }
    public double getProgressPercent() {
        return this.getMaxProgress() == 0 ? 0.0 : (double)this.getProgress() / ((double)this.getMaxProgress() * 1.0);
    }
    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (capability == GregtechTileCapabilities.CAPABILITY_WORKABLE)
        {
            return GregtechTileCapabilities.CAPABILITY_WORKABLE.cast(this);
        }else if (capability == GregtechTileCapabilities.CAPABILITY_CONTROLLABLE)
        {
            return GregtechTileCapabilities.CAPABILITY_CONTROLLABLE.cast(this);
        }else if(capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && side==EnumFacing.UP)
        {
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(this.inventoryBees);
        }else if(capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && side!=EnumFacing.UP)
        {
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(this.inventoryOutput);
        }
        return super.getCapability(capability, side);
    }

    private String flowerType = "";
    private BlockPos flowercoords = null;
    private Block flowerBlock;
    private int flowerBlockMeta;
    private float terrorityMod = 1f;
    private float mutationMod = 1f;
    private float lifespanMod = 1f;
    private float productionMod = 2f;
    private float floweringMod = 1f;
    private float geneticDecayMod = 1f;
    private float energyMod = 1f;
    private boolean sealedMod = false;
    private boolean selfLightedMod = false;
    private boolean selfUnlightedMod = false;
    private boolean sunlightSimulatedMod = false;
    private Biome biomeOverride = null;
    private float humidityMod = 0f;
    private float temperatureMod = 0f;
    private boolean isAutomated = false;
    private boolean isRetrievingPollen = false;
    private int maxspeed = 0;
    public HashSet<IErrorState> mErrorStates = new HashSet<>();
    public void updateModifiers() {
        final GT_ApiaryModifier mods = new GT_ApiaryModifier();
        for (int i = 0; i < upgradeSlotCount; i++) {
            final ItemStack s = inventoryUpgrade.getStackInSlot(upgradeSlot + i);
            if (s == null) continue;
            if (GT_ApiaryUpgrade.isUpgrade(s)) {
                final GT_ApiaryUpgrade upgrade = GT_ApiaryUpgrade.getUpgrade(s);
                upgrade.applyModifiers(mods, s);
            }
        }

        terrorityMod = mods.territory;
        mutationMod = mods.mutation;
        lifespanMod = mods.lifespan;
        productionMod = mods.production;
        floweringMod = mods.flowering;
        geneticDecayMod = mods.geneticDecay;
        energyMod = mods.energy;
        sealedMod = mods.isSealed;
        selfLightedMod = mods.isSelfLighted;
        selfUnlightedMod = mods.isSelfUnlighted;
        sunlightSimulatedMod = mods.isSunlightSimulated;
        biomeOverride = mods.biomeOverride;
        humidityMod = mods.humidity;
        temperatureMod = mods.temperature;
        isAutomated = mods.isAutomated;
        isRetrievingPollen = mods.isCollectingPollen;
        maxspeed = mods.maxSpeed;

        if (mLockedSpeed) mSpeed = maxspeed;
        else mSpeed = Math.min(mSpeed, maxspeed);
    }
    @Override
    public boolean setCondition(boolean b, IErrorState iErrorState) {
        if (b) mErrorStates.add(iErrorState);
        else mErrorStates.remove(iErrorState);
        writeCustomData(4802,buf->writeData(buf));
        return b;
    }
    @Override
    public boolean contains(IErrorState iErrorState) {
        return mErrorStates.contains(iErrorState);
    }
    @Override
    public boolean hasErrors() {
        return !mErrorStates.isEmpty();
    }
    @Override
    public void clearErrors() {
        mErrorStates.clear();
        writeCustomData(4802,buf->writeData(buf));
    }

    @Override
    public void writeData(PacketBuffer packetBuffer) {
        packetBuffer.writeInt(mErrorStates.size());
        for (IErrorState s : mErrorStates) packetBuffer.writeString(s.getUniqueName());
    }

    @Override
    public void readData(PacketBuffer packetBuffer) {
        for (int i = packetBuffer.readInt(); i > 0; i--)
            mErrorStates.add(ForestryAPI.errorStateRegistry.getErrorState(packetBuffer.readString(32767)));
    }


    public ImmutableSet<IErrorState> getErrorStates() {
        return ImmutableSet.copyOf(mErrorStates);
    }
    private boolean canWork(ItemStack queen) {
        clearErrors();
        if (queen == null) return true; // Reloaded the chunk ?
        if (beeRoot.isMember(queen, EnumBeeType.PRINCESS)) return true;
        final IBee bee = beeRoot.getMember(queen);
        for (IErrorState err : bee.getCanWork(this)) setCondition(true, err);
        setCondition(!checkFlower(bee), EnumErrorCode.NO_FLOWER);
        return !hasErrors();
    }

    private boolean canWork() {
        clearErrors();
        final EnumBeeType beeType = beeRoot.getType(getQueen());
        if (beeType == EnumBeeType.PRINCESS) {
            setCondition(!beeRoot.isDrone(getDrone()), EnumErrorCode.NO_DRONE);
            return !hasErrors();
        }
        if (beeType == EnumBeeType.QUEEN) {
            final IBee bee = beeRoot.getMember(getQueen());
            for (IErrorState err : bee.getCanWork(this)) setCondition(true, err);
            setCondition(!checkFlower(bee), EnumErrorCode.NO_FLOWER);
            return !hasErrors();
        } else {
            setCondition(true, EnumErrorCode.NO_QUEEN);
            return false;
        }
    }

    private boolean checkFlower(IBee bee) {
        final String flowerType = bee.getGenome()
                .getFlowerProvider()
                .getFlowerType();
        if (!this.flowerType.equals(flowerType)) flowercoords = null;
        if (flowercoords != null) {
            IBlockState state = getWorld().getBlockState(flowercoords);
            Block block = state.getBlock();
            int meta = block.getMetaFromState(state);

            if (block != flowerBlock || meta != flowerBlockMeta)
                if (!FlowerManager.flowerRegistry.isAcceptedFlower(flowerType, getWorld(), flowercoords))
                    flowercoords = null;
                else {
                    flowerBlock = getWorld().getBlockState(flowercoords).getBlock();
                    flowerBlockMeta = flowerBlock.getMetaFromState(getWorld().getBlockState(flowercoords));
                }
        }
        if (flowercoords == null) {
            var s=  getTerritoryModifier(null, 1f) *  BeeManager.beeRoot.getBeekeepingMode(getWorld()).getBeeModifier().getTerritoryModifier(null, 1f);
            var flowercoordss = FlowerManager.flowerRegistry.getAcceptedFlowerCoordinates(this, bee, flowerType,(int)s);

            if(flowercoordss.size()>0)
                flowercoords = flowercoordss.get(0);
            if (flowercoords != null) {
                flowerBlock = getWorld().getBlockState(flowercoords).getBlock();
                flowerBlockMeta = flowerBlock.getMetaFromState(getWorld().getBlockState(flowercoords));
                this.flowerType = flowerType;
            }
        }
        return flowercoords != null;
    }
    private void doEffect() {
        final IBeeGenome genome = usedQueenBee.getGenome();
        final IAlleleBeeEffect effect = genome.getEffect();
        // 检查效果是否可组合
        if (!effect.isCombinable()) {
            return;
        }
        effectData[0] = effect.validateStorage(effectData[0]);
        effect.doEffect(genome, effectData[0], this);
        // 获取非活跃等位基因的效果
        final IAlleleBeeEffect secondary = (IAlleleBeeEffect) genome.getInactiveAllele(EnumBeeChromosome.EFFECT);
        // 检查第二个效果是否可组合
        if (!secondary.isCombinable()) {
            return;
        }
        effectData[1] = effect.validateStorage(effectData[1]);
        secondary.doEffect(genome, effectData[1], this);
    }
    private void doAcceleratedEffects() {
        final IBeeGenome genome = usedQueenBee.getGenome();
        final IAlleleBeeEffect effect = genome.getEffect();
        try {
            effectData[0] = effect.doEffect(genome, effectData[0],this);

            if (!effect.isCombinable()) {
                return;
            }
            final IAlleleBeeEffect secondary = (IAlleleBeeEffect) genome.getInactiveAllele(EnumBeeChromosome.EFFECT);
            effectData[1] = secondary.doEffect(genome, effectData[1],this);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public Iterable<IBeeModifier> getBeeModifiers() {
        return Collections.singletonList(this);
    }

    @Override
    public Iterable<IBeeListener> getBeeListeners() {
        return Collections.singletonList(this);
    }

    @Override
    public IBeeHousingInventory getBeeInventory() {
        return this;
    }

    @Override
    public IBeekeepingLogic getBeekeepingLogic() {
        return dummylogic;
    }

    @Override
    public int getBlockLightValue() {
        return getLightValue();
    }

    @Override
    public int getActualLightValue() {
        return getWorld().getLightFromNeighbors(getPos().up());
    }

    @Override
    public boolean canBlockSeeTheSky() {
        return getWorld().canBlockSeeSky(getPos().add(0, 2, 0));
    }

    @Override
    public boolean isRaining() {
        return this.getWorld().isRainingAt(getPos().add(0, 2, 0));
    }

    @Nullable
    @Override
    public GameProfile getOwner() {
        if(uid!=null)
            return new GameProfile(uid,name);
        return null;
    }

    @Override
    public Vec3d getBeeFXCoordinates() {
        return new Vec3d(getPos().getX(),getPos().getY(),getPos().getZ());
    }

    @Override
    public ItemStack getQueen() {
        return inventoryBees.getStackInSlot(queen);
    }

    @Override
    public ItemStack getDrone() {
        return inventoryBees.getStackInSlot(drone);
    }

    @Override
    public void setQueen(ItemStack itemStack) {
        if(itemStack==ItemStack.EMPTY)
            inventoryBees.extractItem(queen,1,false);
        if(inventoryBees.insertItem(queen, itemStack,true) == ItemStack.EMPTY)
        {
            inventoryBees.insertItem(queen, itemStack,false);
        }
    }

    @Override
    public void setDrone(ItemStack itemStack) {
        if(itemStack==ItemStack.EMPTY)
            inventoryBees.extractItem(drone,1,false);
        if(inventoryBees.insertItem(drone, itemStack,true) == ItemStack.EMPTY)
        {
            inventoryBees.insertItem(drone, itemStack,false);
        }
    }

    @Override
    public boolean addProduct(ItemStack itemStack, boolean b) {
        throw new RuntimeException("Should not happen :F");
    }

    @Override
    public void wearOutEquipment(int i) {

    }

    @Override
    public void onQueenDeath() {

    }

    @Override
    public boolean onPollenRetrieved(IIndividual iIndividual) {
        return false;
    }

    @Override
    public float getTerritoryModifier(IBeeGenome iBeeGenome, float v) {
        return Math.min(5, terrorityMod);
    }

    @Override
    public float getMutationModifier(IBeeGenome iBeeGenome, IBeeGenome iBeeGenome1, float v) {
        return mutationMod;
    }

    @Override
    public float getLifespanModifier(IBeeGenome iBeeGenome, @Nullable IBeeGenome iBeeGenome1, float v) {
        return lifespanMod;
    }

    @Override
    public float getProductionModifier(IBeeGenome iBeeGenome, float v) {
        return productionMod;
    }

    @Override
    public float getFloweringModifier(IBeeGenome iBeeGenome, float v) {
        return floweringMod;
    }

    @Override
    public float getGeneticDecay(IBeeGenome iBeeGenome, float v) {
        return geneticDecayMod;
    }

    @Override
    public boolean isSealed() {
        return sealedMod;
    }

    @Override
    public boolean isSelfLighted() {
        return selfLightedMod;
    }

    @Override
    public boolean isSunlightSimulated() {
        return selfUnlightedMod;
    }

    @Override
    public boolean isHellish() {
        return getBiome() == Biomes.HELL;
    }

    @Override
    public Biome getBiome() {
        return getWorld().getBiome(this.getPos());
    }

    @Override
    public EnumTemperature getTemperature() {
        if (BiomeHelper.isBiomeHellish(getBiome())) return EnumTemperature.HELLISH;
        return EnumTemperature.getFromValue(getBiome().getTemperature(getPos()) + temperatureMod);
    }

    @Override
    public EnumHumidity getHumidity() {
        return EnumHumidity.getFromValue(getBiome().getRainfall() + humidityMod);
    }

    @Override
    public IErrorLogic getErrorLogic() {
        return this;
    }

    @Override
    public World getWorldObj() {
        return getWorld();
    }

    @Override
    public BlockPos getCoordinates() {
        return getPos();
    }
    static final IBeekeepingLogic dummylogic = new IBeekeepingLogic() {

        @Override
        public boolean canWork() {
            return true;
        }

        @Override
        public void doWork() {}

        @Override
        public void clearCachedValues() {

        }

        @Override
        public void syncToClient() {}

        @Override
        public void syncToClient(EntityPlayerMP entityPlayerMP) {}

        @Override
        public int getBeeProgressPercent() {
            return 0;
        }

        @Override
        public boolean canDoBeeFX() {
            return false;
        }

        @Override
        public void doBeeFX() {}

        @Override
        public List<BlockPos> getFlowerPositions() {
            return new ArrayList<BlockPos>();
        }


        @Override
        public void readData(PacketBuffer data) throws IOException {
            IBeekeepingLogic.super.readData(data);
        }

        @Override
        public void writeData(PacketBuffer data) {
            IBeekeepingLogic.super.writeData(data);
        }

        @Override
        public void readFromNBT(NBTTagCompound nbtTagCompound) {

        }

        @Override
        public NBTTagCompound writeToNBT(NBTTagCompound nbtTagCompound) {
            return null;
        }
    };


    public class GT_ApiaryModifier {

        public float territory = 1f;
        public float mutation = 1f;
        public float lifespan = 1f;
        public float production = 2f;
        public float flowering = 1f;
        public float geneticDecay = 1f;
        public boolean isSealed = false;
        public boolean isSelfLighted = false;
        public boolean isSelfUnlighted = false;
        public boolean isSunlightSimulated = false;
        public boolean isAutomated = false;
        public boolean isCollectingPollen = false;
        public Biome biomeOverride = null;
        public float energy = 1f;
        public float temperature = 0f;
        public float humidity = 0f;
        public int maxSpeed = 0;
    }
}
