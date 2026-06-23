package com.drppp.drtech.common.MetaTileEntities.single;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.drppp.drtech.Client.Textures;
import com.drppp.drtech.DrtConfig;
import com.drppp.drtech.Network.SyncInit;
import com.drppp.drtech.Network.UpdateTileEntityPacket;
import com.drppp.drtech.api.ItemHandler.InOutItemStackHandler;
import com.drppp.drtech.api.ItemHandler.OnlyBeesStackhandler;
import com.drppp.drtech.api.ItemHandler.OnlyUpgradeStackhandler;
import com.drppp.drtech.api.Utils.DrtechUtils;
import com.drppp.drtech.api.Utils.GT_ApiaryUpgrade;
import com.drppp.drtech.api.Utils.ItemId;
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
import gregtech.api.metatileentity.TieredMetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.util.GTTransferUtils;
import gregtech.client.renderer.ICubeRenderer;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
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

public class MetaTileEntityIndustrialApiary extends TieredMetaTileEntity implements IWorkable ,
        IBeeHousing, IBeeHousingInventory, IErrorLogic, IBeeModifier, IBeeListener {
    protected final ICubeRenderer renderer;
    public int i=0;
    public boolean isActive=true;
    public boolean isWorkingEnabled=true;
    private boolean isProcessing = false;
    public int mProgresstime=0;
    public int mMaxProgresstime=100;
    public int progressPer=0;
    public int mEUt=0;
    private final ItemStackHandler inventoryBees = new OnlyBeesStackhandler(2) {
        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);
            markDirty();
            if (slot == queen) {
                clearBeeCaches();
            }
        }
    };
    private final ItemStackHandler inventoryUpgrade = new OnlyUpgradeStackhandler(4) {
        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);
            upgradeInventoryDirty = true;
            markDirty();
        }
    };
    private final ItemStackHandler inventoryOutput = new InOutItemStackHandler(12,false) {
        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);
            markDirty();
        }
    };
    public ItemStack[] mOutputItems = new ItemStack[12];
    public static final int beeCycleLength = 550;
    public static final int baseEUtUsage = 37;
    private static final int DATA_ID_PROGRESS = 4804;
    private static final int queen = 0;
    private static final int drone = 1;
    private static final int upgradeSlot = 0;
    private static final int upgradeSlotCount = 4;
    final IBeeRoot beeRoot = (IBeeRoot) AlleleManager.alleleRegistry.getSpeciesRoot("rootBees");

    public int mSpeed = 0;
    public boolean mLockedSpeed = true;
    public boolean mAutoQueen = true;
    public UUID uid= null;
    public String name= null;
    private ItemStack usedQueen = null;
    private IBee usedQueenBee = null;
    private IEffectData[] effectData = new IEffectData[2];
    private boolean upgradeInventoryDirty = true;
    private boolean errorStatesDirty = false;
    private int lastSyncedProgressTime = Integer.MIN_VALUE;
    private int lastSyncedMaxProgress = Integer.MIN_VALUE;
    private int lastSyncedProgressPercent = Integer.MIN_VALUE;
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
        ModularUI.Builder builder = ModularUI.builder(Textures.BACKGROUND, 176, 166);
        buildApiaryHeader(builder);
        buildBeeInventory(builder);
        buildUpgradeInventory(builder);
        buildOutputInventory(builder);
        buildApiaryControls(builder);
        builder.bindPlayerInventory(entityPlayer.inventory,88);
        return builder.build(this.getHolder(),entityPlayer);
    }

    private void buildApiaryHeader(ModularUI.Builder builder) {
        builder.widget(new LabelWidget(7, 5, "drtech.gui.industrial_apiary.title"));
        builder.widget(new DynamicLabelWidget(7, 80, this::getApiaryStatusText, 0x404040));
        builder.widget(new gregtech.api.gui.widgets.ProgressWidget(this::getProgressPercent, 86, 39, 20, 20,
                gregtech.api.gui.GuiTextures.PROGRESS_BAR_ARROW, gregtech.api.gui.widgets.ProgressWidget.MoveType.HORIZONTAL).setHoverTextConsumer(this::addErrorText));
    }

    private void buildBeeInventory(ModularUI.Builder builder) {
        builder.widget(new LabelWidget(8, 17, "drtech.gui.industrial_apiary.bees", 0x404040));
        builder.slot(inventoryBees, queen, 8, 28, true, true, Textures.BEE_QUEEN_LOGO);
        builder.slot(inventoryBees, drone, 8, 50, true, true, Textures.BEE_DRONE_LOGO);
    }

    private void buildUpgradeInventory(ModularUI.Builder builder) {
        builder.widget(new LabelWidget(40, 17, "drtech.gui.industrial_apiary.upgrades", 0x404040));
        for (int j = 0; j < inventoryUpgrade.getSlots(); j++) {
            builder.slot(inventoryUpgrade, j, 40 + j % 2 * 18, 28 + j / 2 * 18, true, true, gregtech.api.gui.GuiTextures.SLOT);
        }
    }

    private void buildOutputInventory(ModularUI.Builder builder) {
        builder.widget(new LabelWidget(118, 5, "drtech.gui.industrial_apiary.output", 0x404040));
        for (int j = 0; j < inventoryOutput.getSlots(); j++) {
            builder.slot(inventoryOutput, j, 116 + j % 3 * 18, 14 + j / 3 * 18, true, false, gregtech.api.gui.GuiTextures.SLOT);
        }
    }

    private void buildApiaryControls(ModularUI.Builder builder) {
        builder.widget(new ApiaryActionButton(38, 66, 34, 14,
                "drtech.gui.industrial_apiary.pause",
                "drtech.gui.industrial_apiary.tooltip.1",
                "pause"));
        builder.widget(new ApiaryActionButton(76, 66, 34, 14,
                "drtech.gui.industrial_apiary.stop",
                "drtech.gui.industrial_apiary.tooltip.2",
                "stop"));
    }
    private String getApiaryStatusText() {
        if (!isActive()) return localize("drtech.gui.industrial_apiary.status.stopped");
        if (!isWorkingEnabled()) return localize("drtech.gui.industrial_apiary.status.paused");
        if (isWorking()) return localize("drtech.gui.industrial_apiary.status.working", progressPer);
        if (hasPendingProcess()) return localize("drtech.gui.industrial_apiary.status.waiting_eu");
        return localize("drtech.gui.industrial_apiary.status.ready");
    }

    private String localize(String key, Object... args) {
        return new TextComponentTranslation(key, args).getFormattedText();
    }

    private void pauseApiary() {
        setWorkingEnabled(false);
        setProcessing(false);
    }

    private void resumeApiary() {
        setActive(true);
        setWorkingEnabled(true);
    }

    private void stopApiary() {
        cancelProcess(false);
        setWorkingEnabled(false);
    }

    private void togglePauseApiary() {
        if (isWorkingEnabled()) {
            pauseApiary();
        } else {
            resumeApiary();
        }
    }

    private void toggleStopApiary() {
        if (isActive()) {
            stopApiary();
        } else {
            resumeApiary();
        }
    }

    public void handleApiaryClientAction(String action) {
        if ("pause".equals(action)) {
            togglePauseApiary();
        } else if ("stop".equals(action)) {
            toggleStopApiary();
        }
    }

    private class ApiaryActionButton extends gregtech.api.gui.Widget {
        private final String labelKey;
        private final String tooltipKey;
        private final String action;

        private ApiaryActionButton(int x, int y, int width, int height, String labelKey, String tooltipKey, String action) {
            super(x, y, width, height);
            this.labelKey = labelKey;
            this.tooltipKey = tooltipKey;
            this.action = action;
        }

        @Override
        public void drawInBackground(int mouseX, int mouseY, float partialTicks, gregtech.api.gui.IRenderContext context) {
            super.drawInBackground(mouseX, mouseY, partialTicks, context);
            int x = getPosition().x;
            int y = getPosition().y;
            int width = getSize().width;
            int height = getSize().height;
            boolean hovered = isMouseOverElement(mouseX, mouseY);
            drawSolidRect(x, y, width, height, hovered ? 0xFF546477 : 0xFF3F4A5A);
            drawBorder(x, y, width, height, 1, hovered ? 0xFF8DEBFF : 0xFF6F7C8F);
            drawStringSized(localize(getDisplayLabelKey()), x + width / 2.0, y + 3.0, 0xFFFFFFFF, true, 0.75F, true);
        }

        private String getDisplayLabelKey() {
            if ("pause".equals(action) && !isWorkingEnabled()) {
                return "drtech.gui.industrial_apiary.start";
            }
            if ("stop".equals(action) && !isActive()) {
                return "drtech.gui.industrial_apiary.start";
            }
            return labelKey;
        }

        @Override
        public void drawInForeground(int mouseX, int mouseY) {
            super.drawInForeground(mouseX, mouseY);
            if (isMouseOverElement(mouseX, mouseY)) {
                drawHoveringText(ItemStack.EMPTY, Collections.singletonList(localize(tooltipKey)), 300, mouseX, mouseY);
            }
        }

        @Override
        public boolean mouseClicked(int mouseX, int mouseY, int button) {
            if (button == 0 && isMouseOverElement(mouseX, mouseY)) {
                NBTTagCompound tag = new NBTTagCompound();
                tag.setString("industrialApiaryAction", action);
                SyncInit.NETWORK.sendToServer(new UpdateTileEntityPacket(MetaTileEntityIndustrialApiary.this.getPos(), tag));
                playButtonClickSound();
                return true;
            }
            return false;
        }
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
        buf.writeBoolean(this.isProcessing);
        if(usedQueen==null)
            usedQueen=ItemStack.EMPTY;
        buf.writeItemStack(usedQueen);
        buf.writeInt(mEUt);
        buf.writeInt(mProgresstime);
        buf.writeInt(mMaxProgresstime);
        buf.writeInt(progressPer);
        writeData(buf);
    }

    @Override
    public void receiveInitialSyncData(@NotNull PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        isActive = buf.readBoolean();
        isWorkingEnabled = buf.readBoolean();
        isProcessing = buf.readBoolean();
        try {
            usedQueen=buf.readItemStack();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        mEUt = buf.readInt();
        mProgresstime = buf.readInt();
        mMaxProgresstime = buf.readInt();
        progressPer = buf.readInt();
        readData(buf);
    }
    @Override
    public void setWorkingEnabled(boolean b) {
        if (this.isWorkingEnabled == b) {
            if (!b) {
                setProcessing(false);
            }
            return;
        }
        this.isWorkingEnabled = b;
        markDirty();
        writeCustomData(GregtechDataCodes.WORKING_ENABLED, buf -> buf.writeBoolean(isWorkingEnabled));
        if (!b) {
            setProcessing(false);
        }
    }

    private void setProcessing(boolean processing) {
        if (this.isProcessing == processing) {
            return;
        }
        this.isProcessing = processing;
        markDirty();
        writeCustomData(4803, buf -> buf.writeBoolean(this.isProcessing));
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
        if (dataId == 1919)
        {
            this.uid = buf.readUniqueId();
        }
        if(dataId==1920)
        {
            this.name = buf.readString(500);
        }
        if (dataId == GregtechDataCodes.WORKABLE_ACTIVE) {
            isActive = buf.readBoolean();
            scheduleRenderUpdate();
        } else if (dataId == GregtechDataCodes.WORKING_ENABLED) {
            isWorkingEnabled = buf.readBoolean();
            scheduleRenderUpdate();
        } else if (dataId == 4803) {
            isProcessing = buf.readBoolean();
            scheduleRenderUpdate();
        } else if (dataId == DATA_ID_PROGRESS) {
            mProgresstime = buf.readInt();
            mMaxProgresstime = buf.readInt();
            progressPer = buf.readInt();
        } else if(dataId==4800)
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
    }
    @Nullable
    @Override
    public GameProfile getOwner() {
        if(uid!=null)
            return new GameProfile(uid,name);
        return null;
    }
    @Override
    public void update() {
        super.update();
        if (getWorld().isRemote && DrtConfig.MachineSwitch.EnableIndustrialApiaryTx)
        {
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
            if (isWorking()) {
                if (this.mProgresstime < 0) {
                    this.mProgresstime++;
                    return;
                }
                if (this.energyContainer.addEnergy(-this.mEUt)>=0) {
                    setProcessing(false);
                    return;
                }
                if (this.hasErrors()) {
                    if (getOffsetTimer() % 100 == 0 && !canWork(usedQueen)) {
                        setProcessing(false);
                    }
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
                        setProcessing(false);
                        return;
                    }
                }

                if (this.mProgresstime >= this.mMaxProgresstime) {
                    if (usedQueenBee != null && !usedQueen.isEmpty()) doAcceleratedEffects();
                    updateModifiers();
                    flushPendingOutputs();
                    resetProcessingState();
                }
            } else if (isActive() && isWorkingEnabled()) {
                if (hasPendingProcess()) {
                    if (this.energyContainer.getEnergyStored() > mEUt) {
                        setProcessing(true);
                    }
                } else if (this.energyContainer.getEnergyStored()>mEUt) {
                    final boolean check = checkRecipe();
                    if (check ) {
                        setProcessing(true);
                    }
                }
            }
            progressPer = this.mMaxProgresstime > 0 ? (int) (((float) this.mProgresstime / (float) this.mMaxProgresstime) * 100) : 0;
            if (getOffsetTimer() % 5 == 0 || mProgresstime == 0 || mProgresstime >= mMaxProgresstime) {
                syncProgressData();
            }
        }
    }

    private void syncProgressData() {
        if (getWorld() == null || getWorld().isRemote) {
            return;
        }
        if (lastSyncedProgressTime == mProgresstime
                && lastSyncedMaxProgress == mMaxProgresstime
                && lastSyncedProgressPercent == progressPer) {
            return;
        }
        lastSyncedProgressTime = mProgresstime;
        lastSyncedMaxProgress = mMaxProgresstime;
        lastSyncedProgressPercent = progressPer;
        writeCustomData(DATA_ID_PROGRESS, buf -> {
            buf.writeInt(mProgresstime);
            buf.writeInt(mMaxProgresstime);
            buf.writeInt(progressPer);
        });
    }
    private float getFinalChance(float baseChance, float speed, float prodMod, float modifier) {
        double finalchance = (1+modifier/6)*(Math.pow(baseChance,0.5))*2f*(1+speed)+Math.pow(prodMod,Math.pow(baseChance,0.333f))-3;
        return (float) finalchance;
    }

    private void clearBeeCaches() {
        usedQueenBee = null;
        retrievedpollen = null;
        retrievingPollenInThisOperation = false;
        flowercoords = null;
        flowerBlock = null;
        flowerBlockMeta = 0;
        flowerType = "";
        Arrays.fill(effectData, null);
    }
    private void dropInventoryContents(ItemStackHandler handler) {
        BlockPos pos = getPos();
        World world = getWorld();
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            ItemStack stack = handler.getStackInSlot(slot);
            if (stack.isEmpty()) {
                continue;
            }
            world.spawnEntity(new EntityItem(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack.copy()));
            handler.extractItem(slot, stack.getCount(), false);
        }
    }

    private boolean tryRouteBeeOutput(ItemStack stack, int slotIndex) {
        if (stack == null || stack.isEmpty()) {
            return true;
        }
        if (isAutomated) {
            if (beeRoot.isMember(stack, EnumBeeType.QUEEN) || beeRoot.isMember(stack, EnumBeeType.PRINCESS)) {
                if (inventoryBees.insertItem(queen, stack.copy(), true).isEmpty()) {
                    inventoryBees.insertItem(queen, stack.copy(), false);
                    return true;
                }
            } else if (beeRoot.isMember(stack, EnumBeeType.DRONE) && inventoryBees.insertItem(drone, stack.copy(), true).isEmpty()) {
                inventoryBees.insertItem(drone, stack.copy(), false);
                return true;
            }
        }
        if (mAutoQueen && slotIndex == 0 && beeRoot.isMember(stack, EnumBeeType.QUEEN)
                && inventoryBees.insertItem(queen, stack.copy(), true).isEmpty()) {
            inventoryBees.insertItem(queen, stack.copy(), false);
            return true;
        }
        return false;
    }

    private void flushPendingOutputs() {
        for (int slot = 0; slot < mOutputItems.length; slot++) {
            ItemStack output = mOutputItems[slot];
            if (output == null || output.isEmpty()) {
                continue;
            }
            if (tryRouteBeeOutput(output, slot)) {
                mOutputItems[slot] = null;
                continue;
            }
            GTTransferUtils.insertItem(inventoryOutput, output.copy(), false);
            mOutputItems[slot] = null;
        }
    }

    private void resetProcessingState() {
        Arrays.fill(mOutputItems, null);
        setmEut(0);
        mProgresstime = 0;
        mMaxProgresstime = 0;
        progressPer = 0;
        setUsedQueen(null);
        setProcessing(false);
        clearBeeCaches();
        syncProgressData();
    }

    @Override
    public void onRemoval() {
        super.onRemoval();
        dropInventoryContents(inventoryBees);
        dropInventoryContents(inventoryUpgrade);
        dropInventoryContents(inventoryOutput);
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
            progressPer = 0;
            setProcessing(false);
            setActive(false);
            if(inventoryBees.getStackInSlot(queen).isEmpty())
                setQueen(usedQueen);
            else
                GTTransferUtils.insertItem(inventoryOutput,usedQueen,false);
            setWorkingEnabled(false);
            setUsedQueen(null);
            clearBeeCaches();
            syncProgressData();
        }else {
            setProcessing(false);
            setActive(flag);
            setUsedQueen(null);
            clearBeeCaches();
        }
    }
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setBoolean("isActive", isActive);
        data.setBoolean("isWorkingEnabled", isWorkingEnabled);
        data.setBoolean("isProcessing", isProcessing);
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
        if(uid!=null)
            data.setUniqueId("PlayerUUID",uid);
        if(name!=null)
            data.setString("PlayerName",name);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        isActive = data.getBoolean("isActive");
        isWorkingEnabled = data.getBoolean("isWorkingEnabled");
        isProcessing = data.getBoolean("isProcessing");
        mProgresstime = data.getInteger("Progresstime");
        mMaxProgresstime = data.getInteger("maxProgresstime");
        inventoryBees.deserializeNBT(data.getCompoundTag("invBees"));
        inventoryUpgrade.deserializeNBT(data.getCompoundTag("invUpgrade"));
        inventoryOutput.deserializeNBT(data.getCompoundTag("invOutputs"));
        upgradeInventoryDirty = true;
        clearBeeCaches();
        mEUt = data.getInteger("mEut");
        mSpeed = data.getInteger("mSpeed");
        if(data.hasKey("usedQueen"))
        {
            usedQueen = new ItemStack(data.getCompoundTag("usedQueen"));
            if (!usedQueen.isEmpty()) {
                usedQueenBee = beeRoot.getMember(usedQueen);
            }
        } else {
            usedQueen = null;
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
        if(data.hasKey("PlayerUUIDMost"))
            uid = data.getUniqueId("PlayerUUID");
        if(data.hasKey("PlayerName"))
            name = data.getString("PlayerName");
        errorStatesDirty = false;
    }
    public void setUUID(EntityPlayer player) {
        this.uid = player.getUniqueID();
        this.name = player.getName();
        this.writeCustomData(1919, (b) -> {
            b.writeUniqueId(this.uid);
        });
        this.writeCustomData(1920, (b) -> {
            b.writeString(name);
        });
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
        return this.isActive && this.isProcessing;
    }

    private boolean hasPendingProcess() {
        return usedQueen != null && !usedQueen.isEmpty() && this.mMaxProgresstime > 0;
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
        if (!upgradeInventoryDirty) {
            return;
        }
        final GT_ApiaryModifier mods = new GT_ApiaryModifier();
        for (int i = 0; i < upgradeSlotCount; i++) {
            final ItemStack s = inventoryUpgrade.getStackInSlot(upgradeSlot + i);
            if (s.isEmpty()) continue;
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
        upgradeInventoryDirty = false;
    }
    @Override
    public boolean setCondition(boolean b, IErrorState iErrorState) {
        final boolean changed = b ? mErrorStates.add(iErrorState) : mErrorStates.remove(iErrorState);
        if (changed) {
            errorStatesDirty = true;
        }
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
        if (!mErrorStates.isEmpty()) {
            mErrorStates.clear();
            errorStatesDirty = true;
        }
    }

    @Override
    public void writeData(PacketBuffer packetBuffer) {
        packetBuffer.writeInt(mErrorStates.size());
        for (IErrorState s : mErrorStates) packetBuffer.writeString(s.getUniqueName());
    }

    @Override
    public void readData(PacketBuffer packetBuffer) {
        mErrorStates.clear();
        for (int i = packetBuffer.readInt(); i > 0; i--) {
            IErrorState errorState = ForestryAPI.errorStateRegistry.getErrorState(packetBuffer.readString(32767));
            if (errorState != null) {
                mErrorStates.add(errorState);
            }
        }
        errorStatesDirty = false;
    }

    public ImmutableSet<IErrorState> getErrorStates() {
        return ImmutableSet.copyOf(mErrorStates);
    }

    private void syncErrorStatesIfNeeded() {
        if (!getWorld().isRemote && errorStatesDirty) {
            errorStatesDirty = false;
            writeCustomData(4802, buf -> writeData(buf));
        }
    }

    private boolean applyBeeConditions(IBee bee) {
        for (IErrorState err : bee.getCanWork(this)) {
            setCondition(true, err);
        }
        setCondition(!checkFlower(bee), EnumErrorCode.NO_FLOWER);
        if (contains(EnumErrorCode.NOT_NIGHT) && isselfUnlightedMod()) {
            setCondition(false, EnumErrorCode.NOT_NIGHT);
        }
        syncErrorStatesIfNeeded();
        return !hasErrors();
    }

    private boolean canWork(ItemStack queen) {
        clearErrors();
        if (queen == null || queen.isEmpty()) {
            syncErrorStatesIfNeeded();
            return true;
        }
        if (beeRoot.isMember(queen, EnumBeeType.PRINCESS)) {
            syncErrorStatesIfNeeded();
            return true;
        }
        return applyBeeConditions(beeRoot.getMember(queen));
    }

    private boolean canWork() {
        clearErrors();
        final ItemStack queenStack = getQueen();
        final EnumBeeType beeType = beeRoot.getType(queenStack);
        if (beeType == EnumBeeType.PRINCESS) {
            setCondition(!beeRoot.isDrone(getDrone()), EnumErrorCode.NO_DRONE);
            syncErrorStatesIfNeeded();
            return !hasErrors();
        }
        if (beeType == EnumBeeType.QUEEN) {
            return applyBeeConditions(beeRoot.getMember(queenStack));
        }
        setCondition(true, EnumErrorCode.NO_QUEEN);
        syncErrorStatesIfNeeded();
        return false;
    }

    private boolean checkFlower(IBee bee) {
        final World world = getWorld();
        final String currentFlowerType = bee.getGenome()
                .getFlowerProvider()
                .getFlowerType();
        if (!this.flowerType.equals(currentFlowerType)) {
            flowercoords = null;
        }
        if (flowercoords != null) {
            IBlockState state = world.getBlockState(flowercoords);
            Block block = state.getBlock();
            int meta = block.getMetaFromState(state);
            if (block != flowerBlock || meta != flowerBlockMeta) {
                if (!FlowerManager.flowerRegistry.isAcceptedFlower(currentFlowerType, world, flowercoords)) {
                    flowercoords = null;
                } else {
                    flowerBlock = block;
                    flowerBlockMeta = meta;
                }
            }
        }
        if (flowercoords == null) {
            float territory = getTerritoryModifier(null, 1f) * BeeManager.beeRoot.getBeekeepingMode(world).getBeeModifier().getTerritoryModifier(null, 1f);
            List<BlockPos> acceptedFlowers = FlowerManager.flowerRegistry.getAcceptedFlowerCoordinates(this, bee, currentFlowerType, (int) territory);
            if (!acceptedFlowers.isEmpty()) {
                flowercoords = acceptedFlowers.get(0);
                IBlockState state = world.getBlockState(flowercoords);
                flowerBlock = state.getBlock();
                flowerBlockMeta = flowerBlock.getMetaFromState(state);
                this.flowerType = currentFlowerType;
            }
        }
        return flowercoords != null;
    }
    private void doEffect() {
        final IBeeGenome genome = usedQueenBee.getGenome();
        final IAlleleBeeEffect effect = genome.getEffect();
        // Skip effects that cannot stack.
        if (!effect.isCombinable()) {
            return;
        }
        effectData[0] = effect.validateStorage(effectData[0]);
        effect.doEffect(genome, effectData[0], this);
        // Apply the inactive effect allele when it can stack too.
        final IAlleleBeeEffect secondary = (IAlleleBeeEffect) genome.getInactiveAllele(EnumBeeChromosome.EFFECT);
        // Skip the secondary effect when it is not combinable.
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
        if(selfUnlightedMod)
            return 0;
        return getLightValue();
    }

    @Override
    public int getActualLightValue() {
        if(selfUnlightedMod)
            return 0;
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
        if(itemStack == null || itemStack.isEmpty()) {
            ItemStack existing = inventoryBees.getStackInSlot(queen);
            if (!existing.isEmpty()) {
                inventoryBees.extractItem(queen, existing.getCount(), false);
            }
            return;
        }
        if(inventoryBees.insertItem(queen, itemStack, true).isEmpty())
        {
            inventoryBees.insertItem(queen, itemStack, false);
        }
    }

    @Override
    public void setDrone(ItemStack itemStack) {
        if(itemStack == null || itemStack.isEmpty()) {
            ItemStack existing = inventoryBees.getStackInSlot(drone);
            if (!existing.isEmpty()) {
                inventoryBees.extractItem(drone, existing.getCount(), false);
            }
            return;
        }
        if(inventoryBees.insertItem(drone, itemStack, true).isEmpty())
        {
            inventoryBees.insertItem(drone, itemStack, false);
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
        if(selfUnlightedMod)
            return false;
        return selfLightedMod;
    }
    public boolean isselfUnlightedMod() {
        return  selfUnlightedMod;
    }
    @Override
    public boolean isSunlightSimulated() {
        return sunlightSimulatedMod;
    }

    @Override
    public boolean isHellish() {
        return getBiome() == Biomes.HELL;
    }

    @Override
    public Biome getBiome() {
        if(biomeOverride!=null)
            return biomeOverride;
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
