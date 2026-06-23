package com.drppp.drtech.common.MetaTileEntities.muti.electric.standard;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.drawable.ItemDrawable;
import com.drppp.drtech.api.Utils.DrtechUtils;
import com.drppp.drtech.common.Blocks.BlocksInit;
import com.drppp.drtech.common.Items.ItemsInit;
import com.meowmel.cropQT.api.CompareMode;
import com.meowmel.cropQT.api.CropRegistry;
import com.meowmel.cropQT.api.CropStats;
import com.meowmel.cropQT.api.CropType;
import com.meowmel.cropQT.item.ItemCropSeed;
import com.meowmel.cropQT.tile.TileCropStick;
import gregtech.api.GTValues;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.ui.MultiblockUIBuilder;
import gregtech.api.pattern.BlockPatternTemplate;
import gregtech.api.pattern.SoftTemplate;
import gregtech.api.pattern.TemplatePool;
import gregtech.api.pattern.casing.DeclarativePatternBuilder;
import gregtech.api.util.GTTransferUtils;
import gregtech.api.util.GTUtility;
import gregtech.api.util.KeyUtil;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockGlassCasing;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.items.MetaItems;
import net.minecraft.block.Block;
import net.minecraft.block.BlockStem;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class MetaTileentityCropsSimulateMachine extends MetaTileEntityBaseWithControl {
    private static final int WATER_PER_CROP = 1000;
    private static final long NORMAL_BASE_CAPACITY = 64;
    private static final int CROP_RACK_BASE_CAPACITY = 4;
    private static final int NORMAL_FERTILIZER_PER_CROP = 2;
    private static final int CROP_RACK_FERTILIZER_PER_CROP = 40;
    private static final int PREVIEW_LIMIT = 4;
    private static final int CROP_RACK_RUN_TIME = 100;
    private static final int MIN_CROP_RACK_MATURE_TICKS = 20;

    private ItemStack seed = ItemStack.EMPTY;
    private int seedCout = 0;
    private WorkPhase workPhase = WorkPhase.INPUT;
    private CoreMode coreMode = CoreMode.NORMAL;
    private final List<DeployedCropState> deployedCrops = new ArrayList<>();

    public MetaTileentityCropsSimulateMachine(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
        this.maxProcess = 100;
        this.process = 0;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileentityCropsSimulateMachine(this.metaTileEntityId);
    }

    private static final SoftTemplate TEMPLATE = TemplatePool.getInstance().register(
            "drtech:crops_simulate_machine",
            MetaTileentityCropsSimulateMachine::buildTemplate
    );

    @Override
    protected @NotNull BlockPatternTemplate createStructureTemplate() {
        return TEMPLATE.get();
    }

    private static BlockPatternTemplate buildTemplate() {
        return DeclarativePatternBuilder.start()
                .aisle("AAAAA", "AAAAA", "BBBBB", "BBBBB", "BBBBB", "AAAAA")
                .aisle("AAAAA", "AAAAA", "BXXXB", "B###B", "B###B", "AAAAA")
                .aisle("AAAAA", "AAAAA", "BXWXB", "B###B", "B###B", "AAAAA")
                .aisle("AAAAA", "AAAAA", "BXXXB", "B###B", "B###B", "AAAAA")
                .aisle("AASAA", "AAAAA", "BBBBB", "BBBBB", "BBBBB", "AAAAA")
                .where('S', selfPredicate(MetaTileentityCropsSimulateMachine.class))
                .where('B', states(getGlassesState()))
                .where('X', blocks(Blocks.FARMLAND))
                .where('W', blocks(Blocks.WATER))
                .where('A',
                        abilities(MultiblockAbility.MAINTENANCE_HATCH).setExactLimit(1)
                                .or(abilities(MultiblockAbility.INPUT_ENERGY).setMaxGlobalLimited(2))
                                .or(abilities(MultiblockAbility.IMPORT_FLUIDS).setMinGlobalLimited(1).setMaxGlobalLimited(4))
                                .or(abilities(MultiblockAbility.IMPORT_ITEMS).setMinGlobalLimited(1).setMaxGlobalLimited(4))
                                .or(abilities(MultiblockAbility.EXPORT_ITEMS).setMinGlobalLimited(1).setMaxGlobalLimited(4))
                                .or(states(MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STAINLESS_CLEAN)))
                )
                .where('#', any())
                .buildTemplate();
    }

    protected static IBlockState getGlassesState() {
        return MetaBlocks.TRANSPARENT_CASING.getState(BlockGlassCasing.CasingType.TEMPERED_GLASS);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return Textures.CLEAN_STAINLESS_STEEL_CASING;
    }

    @Override
    public boolean usesMui2() {
        return true;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, @NotNull List<String> tooltip, boolean advanced) {
        super.addInformation(stack, world, tooltip, advanced);
        for (int i = 1; i <= 8; i++) {
            tooltip.add(I18n.format("drtech.machine.crops.tooltip." + i));
        }
    }

    @Override
    protected void configureDisplayText(MultiblockUIBuilder builder) {
        super.configureDisplayText(builder);
        builder.setWorkingStatus(this.isWorkingEnabled(), this.isActive())
                .setWorkingStatusKeys("gregtech.multiblock.idling", "gregtech.multiblock.work_paused", "gregtech.multiblock.running")
                .addEnergyUsageLine(this.energyContainer)
                .addCustom((keyManager, syncer) -> {
                    if (!this.isStructureFormed()) {
                        return;
                    }

                    String phase = syncer.syncString(getWorkPhaseDisplayName());
                    String core = syncer.syncString(getCoreModeDisplayName());
                    int deployed = syncer.syncInt(getTotalDeployedCount());
                    int varieties = syncer.syncInt(getDeployedVarietyCount());
                    int capacity = syncer.syncInt(getCurrentCapacity());
                    ItemStack displaySeed = syncer.syncItemStack(this.seed);
                    int displayCount = syncer.syncInt(this.seedCout);
                    List<ItemStack> previewStacks = getPreviewOutputStacks(PREVIEW_LIMIT);

                    keyManager.add(rtb -> {
                        rtb.add(IKey.comp(
                                KeyUtil.lang(TextFormatting.GOLD, "drtech.machine.crops.display.phase"),
                                IKey.SPACE,
                                KeyUtil.string(TextFormatting.WHITE, phase)))
                                .newLine();
                        rtb.add(IKey.comp(
                                KeyUtil.lang(TextFormatting.AQUA, "drtech.machine.crops.display.core"),
                                IKey.SPACE,
                                KeyUtil.string(TextFormatting.WHITE, core)))
                                .newLine();
                        rtb.add(IKey.comp(
                                KeyUtil.lang(TextFormatting.GRAY, "drtech.machine.crops.display.capacity"),
                                IKey.SPACE,
                                KeyUtil.number(TextFormatting.WHITE, capacity),
                                IKey.SPACE,
                                KeyUtil.lang(TextFormatting.GRAY, "drtech.machine.crops.display.deployed"),
                                IKey.SPACE,
                                KeyUtil.number(TextFormatting.WHITE, deployed),
                                IKey.SPACE,
                                KeyUtil.lang(TextFormatting.GRAY, "drtech.machine.crops.display.varieties"),
                                IKey.SPACE,
                                KeyUtil.number(TextFormatting.WHITE, varieties)))
                                .newLine();

                        if (!displaySeed.isEmpty() && displayCount > 0) {
                            rtb.add(IKey.comp(
                                    KeyUtil.lang(TextFormatting.BLUE, "drtech.machine.crops.display.first_seed"),
                                    IKey.SPACE,
                                    KeyUtil.string(TextFormatting.WHITE, displaySeed.getDisplayName()),
                                    IKey.SPACE,
                                    KeyUtil.string(TextFormatting.WHITE, "x" + displayCount)));
                        } else {
                            rtb.add(KeyUtil.lang(TextFormatting.RED, "drtech.machine.crops.display.no_crop"));
                        }
                        rtb.newLine();
                        rtb.add(KeyUtil.lang(TextFormatting.GREEN, "drtech.machine.crops.display.outputs"))
                                .newLine();
                    });

                    boolean hasPreview = false;
                    for (int i = 0; i < PREVIEW_LIMIT; i++) {
                        ItemStack previewStack = syncer.syncItemStack(i < previewStacks.size() ? previewStacks.get(i) : ItemStack.EMPTY);
                        if (previewStack.isEmpty()) {
                            continue;
                        }
                        hasPreview = true;
                        keyManager.add(rtb -> {
                            rtb.add(new ItemDrawable(previewStack.copy()))
                                    .space()
                                    .add(KeyUtil.string(TextFormatting.WHITE, previewStack.getDisplayName()));
                            if (previewStack.getCount() > 1) {
                                rtb.space()
                                        .add(KeyUtil.string(TextFormatting.WHITE, "x" + previewStack.getCount()));
                            }
                            rtb.newLine();
                        });
                    }
                    if (!hasPreview) {
                        keyManager.add(KeyUtil.lang(TextFormatting.GRAY, "drtech.machine.crops.display.none"));
                    }
                })
                .addProgressLine(getProgress(), getMaxProgress())
                .addWorkingStatusLine();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setString("WorkPhase", this.workPhase.name());
        data.setString("CoreMode", this.coreMode.name());
        NBTTagList deployedList = new NBTTagList();
        for (DeployedCropState state : this.deployedCrops) {
            deployedList.appendTag(state.writeToNBT());
        }
        data.setTag("DeployedCrops", deployedList);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.workPhase = readEnum(data.getString("WorkPhase"), WorkPhase.INPUT, WorkPhase.class);
        this.coreMode = readEnum(data.getString("CoreMode"), CoreMode.NORMAL, CoreMode.class);
        this.deployedCrops.clear();
        NBTTagList deployedList = data.getTagList("DeployedCrops", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < deployedList.tagCount(); i++) {
            DeployedCropState state = new DeployedCropState();
            state.readFromNBT(deployedList.getCompoundTagAt(i));
            if (state.hasCrop()) {
                this.deployedCrops.add(state);
            }
        }
        refreshSummaryFields();
    }

    @Override
    public boolean onScrewdriverClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing,
                                      CuboidRayTraceResult hitResult) {
        if (!getWorld().isRemote) {
            if (playerIn.isSneaking()) {
                if (!this.deployedCrops.isEmpty()) {
                    playerIn.sendMessage(new TextComponentString(TextFormatting.RED + "请先切换到输出模式取出作物"));
                    return true;
                }
                this.coreMode = this.coreMode.next();
                markDirty();
                playerIn.sendMessage(new TextComponentString(TextFormatting.GREEN + "核心模式: " + this.coreMode.displayName));
            } else {
                this.workPhase = this.workPhase.next();
                this.process = 0;
                this.maxProcess = getCurrentProcessTime();
                markDirty();
                playerIn.sendMessage(new TextComponentString(TextFormatting.GREEN + "工作阶段: " + this.workPhase.displayName));
            }
        }
        return true;
    }

    @Override
    protected void updateFormedValid() {
        if (getWorld().isRemote) {
            return;
        }

        this.maxProcess = getCurrentProcessTime();
        switch (this.workPhase) {
            case INPUT:
                setActive(false);
                this.process = 0;
                deployFromInput();
                break;
            case OUTPUT:
                setActive(false);
                this.process = 0;
                undeployToOutput();
                break;
            case RUN:
                runDeployedCrops();
                break;
        }
        refreshSummaryFields();
    }

    private void deployFromInput() {
        if (this.inputInventory == null) {
            return;
        }
        if (this.coreMode == CoreMode.NORMAL) {
            deployNormalCrops();
        } else {
            deployCropRackCrops();
        }
    }

    private void deployNormalCrops() {
        int remainingCapacity = getNormalCapacity() - getTotalDeployedCount();
        if (remainingCapacity <= 0) {
            return;
        }

        for (int slot = 0; slot < this.inputInventory.getSlots() && remainingCapacity > 0; slot++) {
            ItemStack stack = this.inputInventory.getStackInSlot(slot);
            if (stack.isEmpty() || !DrtechUtils.ItemCrops.containsKey(stack.getItem())) {
                continue;
            }

            DeployedCropState state = findNormalEntry(stack.getItem());
            if (state == null) {
                state = new DeployedCropState();
                state.displaySeed = stack.copy();
                state.displaySeed.setCount(1);
                this.deployedCrops.add(state);
            }

            ItemStack extracted = this.inputInventory.extractItem(slot, remainingCapacity, false);
            if (extracted.isEmpty()) {
                continue;
            }

            state.seedCount += extracted.getCount();
            remainingCapacity -= extracted.getCount();
        }
    }

    private void deployCropRackCrops() {
        int remainingCapacity = getCropRackCapacity() - getTotalDeployedCount();
        if (remainingCapacity <= 0 || this.inputInventory == null) {
            return;
        }

        for (int slot = 0; slot < this.inputInventory.getSlots() && remainingCapacity > 0; slot++) {
            ItemStack stack = this.inputInventory.getStackInSlot(slot);
            if (!isCropQtSeed(stack)) {
                continue;
            }

            String cropId = ItemCropSeed.getCropId(stack);
            if (cropId.isEmpty() || !CropRegistry.exists(cropId)) {
                continue;
            }

            CropType cropType = CropRegistry.get(cropId);
            if (cropType == null) {
                continue;
            }

            DeployedCropState state = findCompatibleCropRackEntry(cropId);
            SupportBlockSelection supportSelection = resolveSupportBlockSelection(cropType,
                    state == null ? ItemStack.EMPTY : state.supportBlockTemplate);
            boolean needsSupport = cropType.getRequiredBlocks().length > 0 || supportSelection != null;

            int availableSticks = countMatchingItems(Item.getItemFromBlock(BlocksInit.CROP_STICK));
            int availableSupport = needsSupport && supportSelection != null
                    ? countMatchingSupportBlocks(supportSelection)
                    : (needsSupport ? 0 : Integer.MAX_VALUE);

            int deployCount = Math.min(remainingCapacity, stack.getCount());
            deployCount = Math.min(deployCount, availableSticks);
            if (needsSupport) {
                deployCount = Math.min(deployCount, availableSupport);
            }
            if (deployCount <= 0) {
                continue;
            }

            if (state == null) {
                state = new DeployedCropState();
                state.cropQtId = cropId;
                state.displaySeed = stack.copy();
                state.displaySeed.setCount(1);
                if (supportSelection != null) {
                    state.supportBlockTemplate = supportSelection.template.copy();
                    state.supportBlockTemplate.setCount(1);
                }
                this.deployedCrops.add(state);
            } else if (state.supportBlockTemplate.isEmpty() && supportSelection != null) {
                state.supportBlockTemplate = supportSelection.template.copy();
                state.supportBlockTemplate.setCount(1);
            }

            ItemStack extractedSeed = this.inputInventory.extractItem(slot, deployCount, false);
            if (extractedSeed.isEmpty()) {
                continue;
            }

            state.cropQtSeedStacks.add(extractedSeed);
            state.cropQtElapsedTicks.add(0);
            state.cropQtCycleFullFertilizedSeeds.add(0);
            state.cropQtCyclePartialFertilizer.add(0);
            state.seedCount += extractedSeed.getCount();
            state.cropStickCount += extractMatchingItems(Item.getItemFromBlock(BlocksInit.CROP_STICK), extractedSeed.getCount());
            if (needsSupport && supportSelection != null) {
                state.supportBlockCount += extractSupportBlocks(supportSelection, extractedSeed.getCount());
            }
            remainingCapacity -= extractedSeed.getCount();
        }
    }

    private void undeployToOutput() {
        if (this.deployedCrops.isEmpty() || this.outputInventory == null) {
            return;
        }

        NonNullList<ItemStack> outlist = NonNullList.create();
        for (DeployedCropState state : this.deployedCrops) {
            if (state.isCropQt()) {
                for (ItemStack seedStack : state.cropQtSeedStacks) {
                    outlist.add(seedStack.copy());
                }
                outlist.addAll(splitStacks(new ItemStack(BlocksInit.CROP_STICK), state.cropStickCount));
                if (!state.supportBlockTemplate.isEmpty() && state.supportBlockCount > 0) {
                    outlist.addAll(splitStacks(state.supportBlockTemplate, state.supportBlockCount));
                }
            } else {
                outlist.addAll(splitStacks(state.displaySeed, state.seedCount));
            }
        }
        if (!outlist.isEmpty()) {
            GTTransferUtils.addItemsToItemHandler(this.outputInventory, false, outlist);
        }
        this.deployedCrops.clear();
    }

    private void runDeployedCrops() {
        if (!this.isWorkingEnabled() || this.deployedCrops.isEmpty() || !canRunCurrentMode()) {
            setActive(false);
            this.process = 0;
            return;
        }

        long energyCost = getRunEnergyCost();
        if (!drainEnergy(energyCost)) {
            setActive(false);
            return;
        }

        setActive(true);
        if (this.coreMode == CoreMode.CROP_RACK) {
            tickCropRackGrowthAndHarvest();
        }
        if (++this.process < this.maxProcess) {
            return;
        }

        this.process = 0;
        if (!drainWater(getTotalDeployedCount() * WATER_PER_CROP)) {
            setActive(false);
            return;
        }

        int availableFertilizer = countFertilizer();
        int remainingFertilizer = availableFertilizer;
        NonNullList<ItemStack> outlist = NonNullList.create();

        for (DeployedCropState state : this.deployedCrops) {
            if (state.isCropQt()) {
                remainingFertilizer = prepareCropRackCycle(state, remainingFertilizer);
            } else {
                remainingFertilizer = runNormalEntry(state, outlist, remainingFertilizer);
            }
        }

        int fertilizerUsed = availableFertilizer - remainingFertilizer;
        if (fertilizerUsed > 0) {
            extractFertilizer(fertilizerUsed);
        }
        if (!outlist.isEmpty()) {
            GTTransferUtils.addItemsToItemHandler(this.outputInventory, false, outlist);
        }
    }

    private void tickCropRackGrowthAndHarvest() {
        if (this.coreMode != CoreMode.CROP_RACK || this.outputInventory == null) {
            return;
        }

        NonNullList<ItemStack> outlist = NonNullList.create();
        for (DeployedCropState state : this.deployedCrops) {
            if (!state.isCropQt()) {
                continue;
            }
            tickCropRackEntry(state, outlist);
        }
        if (!outlist.isEmpty()) {
            GTTransferUtils.addItemsToItemHandler(this.outputInventory, false, outlist);
        }
    }

    private int runNormalEntry(DeployedCropState state, NonNullList<ItemStack> outlist, int availableFertilizer) {
        IBlockState cropState = DrtechUtils.ItemCrops.get(state.displaySeed.getItem());
        if (cropState == null) {
            return availableFertilizer;
        }

        int remainingFertilizer = availableFertilizer;
        for (int i = 0; i < state.seedCount; i++) {
            int fertilizerUsed = Math.min(remainingFertilizer, NORMAL_FERTILIZER_PER_CROP);
            remainingFertilizer -= fertilizerUsed;
            int rolls = 1 + fertilizerUsed * 2;
            for (int roll = 0; roll < rolls; roll++) {
                appendCropDrops(outlist, cropState);
            }
        }
        return remainingFertilizer;
    }

    private void tickCropRackEntry(DeployedCropState state, NonNullList<ItemStack> outlist) {
        CropType cropType = CropRegistry.get(state.cropQtId);
        if (cropType == null) {
            return;
        }

        List<String> supportBlockIds = state.getSupportBlockIds();
        for (int stackIndex = 0; stackIndex < state.cropQtSeedStacks.size(); stackIndex++) {
            ItemStack seedStack = state.cropQtSeedStacks.get(stackIndex);
            if (seedStack.isEmpty()) {
                continue;
            }
            CropStats stats = ItemCropSeed.getCropStats(seedStack);
            int matureTicks = getCropRackSeedMatureTicks(cropType, stats);
            int elapsedTicks = state.getElapsedTicks(stackIndex) + 1;
            int matureTimes = elapsedTicks / matureTicks;
            state.setElapsedTicks(stackIndex, elapsedTicks % matureTicks);
            if (matureTimes <= 0) {
                continue;
            }

            int fullyFertilizedSeeds = state.getCycleFullFertilizedSeeds(stackIndex);
            int partialFertilizer = state.getCyclePartialFertilizer(stackIndex);
            for (int i = 0; i < seedStack.getCount(); i++) {
                int fertilizerUsed = 0;
                if (i < fullyFertilizedSeeds) {
                    fertilizerUsed = CROP_RACK_FERTILIZER_PER_CROP;
                } else if (i == fullyFertilizedSeeds) {
                    fertilizerUsed = partialFertilizer;
                }
                double totalRolls = matureTimes * (1.0 + fertilizerUsed * 0.1);
                int guaranteedRolls = (int) totalRolls;
                double extraRollChance = totalRolls - guaranteedRolls;

                for (int roll = 0; roll < guaranteedRolls; roll++) {
                    appendSingleCropQtHarvest(outlist, cropType, stats, supportBlockIds);
                }
                if (extraRollChance > 0 && getWorld().rand.nextFloat() < extraRollChance) {
                    appendSingleCropQtHarvest(outlist, cropType, stats, supportBlockIds);
                }
            }
        }
    }

    private int prepareCropRackCycle(DeployedCropState state, int availableFertilizer) {
        CropType cropType = CropRegistry.get(state.cropQtId);
        if (cropType == null) {
            state.clearCycleFertilizerPlan();
            return availableFertilizer;
        }

        int remainingFertilizer = availableFertilizer;
        for (int stackIndex = 0; stackIndex < state.cropQtSeedStacks.size(); stackIndex++) {
            ItemStack seedStack = state.cropQtSeedStacks.get(stackIndex);
            if (seedStack.isEmpty()) {
                state.setCycleFertilizerPlan(stackIndex, 0, 0);
                continue;
            }

            int totalFertilizerUsed = Math.min(remainingFertilizer, seedStack.getCount() * CROP_RACK_FERTILIZER_PER_CROP);
            remainingFertilizer -= totalFertilizerUsed;
            state.setCycleFertilizerPlan(stackIndex,
                    totalFertilizerUsed / CROP_RACK_FERTILIZER_PER_CROP,
                    totalFertilizerUsed % CROP_RACK_FERTILIZER_PER_CROP);
        }
        return remainingFertilizer;
    }

    private void appendSingleCropQtHarvest(NonNullList<ItemStack> outlist, CropType cropType, CropStats stats,
                                           List<String> supportBlockIds) {
        outlist.addAll(cropType.rollDrops(getWorld().rand, stats.getYieldBonus(getWorld().rand), supportBlockIds));
        appendCropQtLootDrops(outlist, cropType);
    }

    private boolean canRunCurrentMode() {
        if (this.deployedCrops.isEmpty()) {
            return false;
        }

        for (DeployedCropState state : this.deployedCrops) {
            if (!state.hasCrop()) {
                return false;
            }
            if (!state.isCropQt()) {
                if (!DrtechUtils.ItemCrops.containsKey(state.displaySeed.getItem())) {
                    return false;
                }
                continue;
            }

            CropType cropType = CropRegistry.get(state.cropQtId);
            if (cropType == null || state.cropStickCount < state.seedCount) {
                return false;
            }

            List<String> supportBlockIds = state.getSupportBlockIds();
            if (cropType.getRequiredBlocks().length > 0 && state.supportBlockCount < state.seedCount) {
                return false;
            }

            float idealLight = getIdealLight(cropType);
            float idealHumidity = getIdealHumidity(cropType);
            if (!cropType.canGrowAt(idealLight, idealHumidity, supportBlockIds)) {
                return false;
            }
        }
        return true;
    }

    private float getIdealLight(CropType cropType) {
        if (cropType.getLightCompare() == CompareMode.LESS) {
            return 0.0f;
        }
        if (cropType.getLightCompare() == CompareMode.RANGE) {
            return cropType.getLightRequirement();
        }
        return Math.max(15.0f, cropType.getLightRequirement());
    }

    private float getIdealHumidity(CropType cropType) {
        if (cropType.getHumidityCompare() == CompareMode.LESS) {
            return 0.0f;
        }
        if (cropType.getHumidityCompare() == CompareMode.RANGE) {
            return cropType.getWaterRequirement();
        }
        return 1.0f;
    }

    private void appendCropDrops(NonNullList<ItemStack> outlist, IBlockState cropState) {
        Block cropBlock = cropState.getBlock();
        if (cropBlock instanceof BlockStem) {
            cropBlock.getDrops(outlist, getWorld(), getPos(), cropState, 4);
            ItemStack fruit = getStemFruit(cropBlock);
            if (!fruit.isEmpty()) {
                outlist.add(fruit);
            }
            return;
        }
        cropBlock.getDrops(outlist, getWorld(), getPos(), cropState, 4);
    }

    private ItemStack getStemFruit(Block cropBlock) {
        if (cropBlock == Blocks.MELON_STEM) {
            return new ItemStack(Items.MELON);
        }
        if (cropBlock == Blocks.PUMPKIN_STEM) {
            return new ItemStack(Blocks.PUMPKIN);
        }
        return ItemStack.EMPTY;
    }

    private void appendCropQtLootDrops(NonNullList<ItemStack> outlist, CropType cropType) {
        if (cropType.getLootTable() == null || cropType.getLootTable().isEmpty()) {
            return;
        }
        if (!(getWorld() instanceof WorldServer)) {
            return;
        }
        WorldServer worldServer = (WorldServer) getWorld();
        LootTable table = worldServer.getLootTableManager()
                .getLootTableFromLocation(new ResourceLocation(cropType.getLootTable()));
        LootContext.Builder contextBuilder = new LootContext.Builder(worldServer);
        outlist.addAll(table.generateLootForPools(getWorld().rand, contextBuilder.build()));
    }

    private boolean drainWater(int amount) {
        if (this.inputFluidInventory == null || amount <= 0) {
            return false;
        }
        FluidStack request = new FluidStack(FluidRegistry.WATER, amount);
        FluidStack drained = this.inputFluidInventory.drain(request, false);
        if (drained == null || drained.amount < amount) {
            return false;
        }
        this.inputFluidInventory.drain(request, true);
        return true;
    }

    private void refreshSummaryFields() {
        this.seed = ItemStack.EMPTY;
        this.seedCout = 0;
        for (DeployedCropState state : this.deployedCrops) {
            if (this.seed.isEmpty()) {
                this.seed = state.displaySeed.copy();
            }
            this.seedCout += state.seedCount;
        }
    }

    public String getWorkPhaseDisplayName() {
        return this.workPhase.displayName;
    }

    public String getCoreModeDisplayName() {
        return this.coreMode.displayName;
    }

    public int getTotalDeployedCount() {
        int total = 0;
        for (DeployedCropState state : this.deployedCrops) {
            total += state.seedCount;
        }
        return total;
    }

    public int getDeployedVarietyCount() {
        return this.deployedCrops.size();
    }

    public int getProgressPercent() {
        return this.maxProcess <= 0 ? 0 : (int) ((long) this.process * 100L / this.maxProcess);
    }

    public boolean hasCropRackGrowthProgress() {
        return this.coreMode == CoreMode.CROP_RACK && !this.deployedCrops.isEmpty();
    }

    public int getCropRackGrowthProgressTicks() {
        CropRackProgressInfo info = getNextCropRackProgressInfo();
        return info == null ? 0 : info.elapsedTicks;
    }

    public int getCropRackGrowthMaxTicks() {
        CropRackProgressInfo info = getNextCropRackProgressInfo();
        return info == null ? 0 : info.matureTicks;
    }

    public double getCropRackGrowthProgressSeconds() {
        return getCropRackGrowthProgressTicks() / 20.0;
    }

    public double getCropRackGrowthMaxSeconds() {
        return getCropRackGrowthMaxTicks() / 20.0;
    }

    @Nullable
    private CropRackProgressInfo getNextCropRackProgressInfo() {
        if (this.coreMode != CoreMode.CROP_RACK) {
            return null;
        }

        CropRackProgressInfo best = null;
        for (DeployedCropState state : this.deployedCrops) {
            if (!state.isCropQt()) {
                continue;
            }
            CropType cropType = CropRegistry.get(state.cropQtId);
            if (cropType == null) {
                continue;
            }
            for (int stackIndex = 0; stackIndex < state.cropQtSeedStacks.size(); stackIndex++) {
                ItemStack seedStack = state.cropQtSeedStacks.get(stackIndex);
                if (seedStack.isEmpty()) {
                    continue;
                }
                CropStats stats = ItemCropSeed.getCropStats(seedStack);
                int matureTicks = getCropRackSeedMatureTicks(cropType, stats);
                int elapsedTicks = Math.max(0, Math.min(state.getElapsedTicks(stackIndex), Math.max(0, matureTicks - 1)));
                int remainingTicks = matureTicks - elapsedTicks;

                if (best == null || remainingTicks < best.remainingTicks) {
                    best = new CropRackProgressInfo(elapsedTicks, matureTicks, remainingTicks);
                }
            }
        }
        return best;
    }

    public String getPreviewOutputText() {
        List<ItemStack> previewStacks = getPreviewOutputStacks(PREVIEW_LIMIT);
        if (previewStacks.isEmpty()) {
            return "None";
        }
        List<String> names = new ArrayList<>();
        for (ItemStack stack : previewStacks) {
            names.add(stack.getDisplayName());
        }
        return String.join(", ", names);
    }

    public List<String> getPreviewOutputLines(int limit) {
        List<String> names = new ArrayList<>();
        for (ItemStack stack : getPreviewOutputStacks(limit)) {
            names.add(stack.getDisplayName());
        }
        return names;
    }

    public List<ItemStack> getPreviewOutputStacks(int limit) {
        LinkedHashSet<String> seenKeys = new LinkedHashSet<>();
        List<ItemStack> previewStacks = new ArrayList<>();
        for (DeployedCropState state : this.deployedCrops) {
            for (ItemStack sample : getPreviewSamples(state)) {
                if (sample.isEmpty()) {
                    continue;
                }
                String previewKey = buildPreviewKey(sample);
                if (!seenKeys.add(previewKey)) {
                    continue;
                }
                previewStacks.add(sample.copy());
                if (previewStacks.size() >= limit) {
                    return previewStacks;
                }
            }
        }
        return previewStacks;
    }

    private List<ItemStack> getPreviewSamples(DeployedCropState state) {
        if (state.isCropQt()) {
            CropType cropType = CropRegistry.get(state.cropQtId);
            if (cropType == null) {
                return Collections.emptyList();
            }
            return getSampleCropQtDrops(cropType, state.getSupportBlockIds());
        }

        IBlockState cropState = DrtechUtils.ItemCrops.get(state.displaySeed.getItem());
        if (cropState == null) {
            return Collections.emptyList();
        }
        NonNullList<ItemStack> sample = NonNullList.create();
        appendCropDrops(sample, cropState);
        return sample;
    }

    private List<ItemStack> getSampleCropQtDrops(CropType cropType, List<String> supportBlockIds) {
        List<ItemStack> sample = new ArrayList<>();
        boolean matchedSupport = false;
        for (String blockId : supportBlockIds) {
            if (cropType.getBlockDrops().containsKey(blockId)) {
                for (ItemStack stack : cropType.getBlockDrops().get(blockId)) {
                    sample.add(stack.copy());
                }
                matchedSupport = true;
                break;
            }
            String noMeta = stripMeta(blockId);
            if (cropType.getBlockDrops().containsKey(noMeta)) {
                for (ItemStack stack : cropType.getBlockDrops().get(noMeta)) {
                    sample.add(stack.copy());
                }
                matchedSupport = true;
                break;
            }
        }
        if (!matchedSupport) {
            for (ItemStack stack : cropType.getDrops()) {
                sample.add(stack.copy());
            }
        }
        if (sample.isEmpty()) {
            sample.addAll(cropType.rollDrops(new Random(0L), 0, supportBlockIds));
        }
        return sample;
    }

    private String stripMeta(String blockId) {
        int lastColon = blockId.lastIndexOf(':');
        return lastColon > blockId.indexOf(':') ? blockId.substring(0, lastColon) : blockId;
    }

    private String buildPreviewKey(ItemStack stack) {
        String registryName = stack.getItem().getRegistryName() == null
                ? stack.getItem().getTranslationKey()
                : stack.getItem().getRegistryName().toString();
        String nbt = stack.hasTagCompound() ? stack.getTagCompound().toString() : "";
        return registryName + ":" + stack.getMetadata() + ":" + nbt;
    }

    private boolean isCropQtSeed(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() == ItemsInit.CROP_SEED;
    }

    private boolean isFertilizer(ItemStack stack) {
        return !stack.isEmpty()
                && stack.getItem() == MetaItems.FERTILIZER.getMetaItem()
                && stack.getMetadata() == MetaItems.FERTILIZER.getMetaValue();
    }

    private DeployedCropState findNormalEntry(Item seedItem) {
        for (DeployedCropState state : this.deployedCrops) {
            if (!state.isCropQt() && !state.displaySeed.isEmpty() && state.displaySeed.getItem() == seedItem) {
                return state;
            }
        }
        return null;
    }

    private DeployedCropState findCompatibleCropRackEntry(String cropId) {
        for (DeployedCropState state : this.deployedCrops) {
            if (state.isCropQt() && cropId.equals(state.cropQtId)) {
                return state;
            }
        }
        return null;
    }

    private Set<String> getSupportCandidates(CropType cropType) {
        Set<String> candidates = new HashSet<>();
        Collections.addAll(candidates, cropType.getRequiredBlocks());
        candidates.addAll(cropType.getBlockDrops().keySet());
        candidates.addAll(cropType.getBlockChanceDrops().keySet());
        return candidates;
    }

    @Nullable
    private SupportBlockSelection resolveSupportBlockSelection(CropType cropType, ItemStack preferredTemplate) {
        if (!preferredTemplate.isEmpty()) {
            SupportBlockSelection selection = new SupportBlockSelection(preferredTemplate.copy());
            if (countMatchingSupportBlocks(selection) > 0) {
                return selection;
            }
        }

        Set<String> candidates = getSupportCandidates(cropType);
        if (candidates.isEmpty()) {
            return null;
        }

        for (int slot = 0; slot < this.inputInventory.getSlots(); slot++) {
            ItemStack stack = this.inputInventory.getStackInSlot(slot);
            if (stack.isEmpty() || !(stack.getItem() instanceof ItemBlock)) {
                continue;
            }
            Block block = Block.getBlockFromItem(stack.getItem());
            if (block == Blocks.AIR || block.getRegistryName() == null) {
                continue;
            }

            String blockId = block.getRegistryName().toString();
            String withMeta = blockId + ":" + stack.getMetadata();
            for (String candidate : candidates) {
                if (matchesBlockId(withMeta, candidate)) {
                    ItemStack template = stack.copy();
                    template.setCount(1);
                    return new SupportBlockSelection(template);
                }
            }
        }
        return null;
    }

    private boolean matchesBlockId(String actual, String required) {
        if (actual.equals(required)) {
            return true;
        }
        return stripMeta(actual).equals(required);
    }

    private int countMatchingItems(Item item) {
        int total = 0;
        if (this.inputInventory == null) {
            return 0;
        }
        for (int slot = 0; slot < this.inputInventory.getSlots(); slot++) {
            ItemStack stack = this.inputInventory.getStackInSlot(slot);
            if (!stack.isEmpty() && stack.getItem() == item) {
                total += stack.getCount();
            }
        }
        return total;
    }

    private int extractMatchingItems(Item item, int amount) {
        int extractedTotal = 0;
        int remaining = amount;
        if (this.inputInventory == null) {
            return 0;
        }
        for (int slot = 0; slot < this.inputInventory.getSlots() && remaining > 0; slot++) {
            ItemStack stack = this.inputInventory.getStackInSlot(slot);
            if (stack.isEmpty() || stack.getItem() != item) {
                continue;
            }
            ItemStack extracted = this.inputInventory.extractItem(slot, remaining, false);
            if (extracted.isEmpty()) {
                continue;
            }
            extractedTotal += extracted.getCount();
            remaining -= extracted.getCount();
        }
        return extractedTotal;
    }

    private int countMatchingSupportBlocks(SupportBlockSelection selection) {
        int total = 0;
        if (this.inputInventory == null) {
            return 0;
        }
        for (int slot = 0; slot < this.inputInventory.getSlots(); slot++) {
            ItemStack stack = this.inputInventory.getStackInSlot(slot);
            if (selection.sameStack(stack)) {
                total += stack.getCount();
            }
        }
        return total;
    }

    private int extractSupportBlocks(SupportBlockSelection selection, int amount) {
        int extractedTotal = 0;
        int remaining = amount;
        if (this.inputInventory == null) {
            return 0;
        }
        for (int slot = 0; slot < this.inputInventory.getSlots() && remaining > 0; slot++) {
            ItemStack stack = this.inputInventory.getStackInSlot(slot);
            if (!selection.sameStack(stack)) {
                continue;
            }
            ItemStack extracted = this.inputInventory.extractItem(slot, remaining, false);
            if (extracted.isEmpty()) {
                continue;
            }
            extractedTotal += extracted.getCount();
            remaining -= extracted.getCount();
        }
        return extractedTotal;
    }

    private int countFertilizer() {
        int total = 0;
        if (this.inputInventory == null) {
            return 0;
        }
        for (int slot = 0; slot < this.inputInventory.getSlots(); slot++) {
            ItemStack stack = this.inputInventory.getStackInSlot(slot);
            if (isFertilizer(stack)) {
                total += stack.getCount();
            }
        }
        return total;
    }

    private void extractFertilizer(int amount) {
        int remaining = amount;
        if (this.inputInventory == null) {
            return;
        }
        for (int slot = 0; slot < this.inputInventory.getSlots() && remaining > 0; slot++) {
            ItemStack stack = this.inputInventory.getStackInSlot(slot);
            if (!isFertilizer(stack)) {
                continue;
            }
            ItemStack extracted = this.inputInventory.extractItem(slot, remaining, false);
            if (extracted.isEmpty()) {
                continue;
            }
            remaining -= extracted.getCount();
        }
    }

    private List<ItemStack> splitStacks(ItemStack template, int totalCount) {
        List<ItemStack> stacks = new ArrayList<>();
        if (template.isEmpty() || totalCount <= 0) {
            return stacks;
        }
        int remaining = totalCount;
        while (remaining > 0) {
            ItemStack copy = template.copy();
            int amount = Math.min(copy.getMaxStackSize(), remaining);
            copy.setCount(amount);
            stacks.add(copy);
            remaining -= amount;
        }
        return stacks;
    }

    private long getRunEnergyCost() {
        long inputVoltage = this.energyContainer == null ? 0 : this.energyContainer.getInputVoltage();
        if (inputVoltage <= 0) {
            return 0;
        }
        int amp = getAbilities(MultiblockAbility.INPUT_ENERGY).size() >= 2 ? 4 : 1;
        return inputVoltage * amp;
    }

    private int getCurrentProcessTime() {
        return this.coreMode == CoreMode.NORMAL ? getNormalProcessTime() : CROP_RACK_RUN_TIME;
    }

    private int getCropRackSeedMatureTicks(CropType cropType, CropStats stats) {
        int progressPerGrowthCycle = Math.max(1, Math.round((stats.getGrowth() + 1.5f) * 1.5f));
        int cyclesPerStage = Math.max(1, (cropType.getStageRequirement() + progressPerGrowthCycle - 1) / progressPerGrowthCycle);
        int totalGrowthCycles = Math.max(1, cyclesPerStage * Math.max(1, cropType.getHarvestStage()));
        int totalGrowthTicks = totalGrowthCycles * TileCropStick.GROWTH_CYCLE;
        int speedFactor = Math.max(1, (getInputTier() - GTValues.EV) * 32);
        int matureTicks = (totalGrowthTicks + speedFactor - 1) / speedFactor;
        return Math.max(MIN_CROP_RACK_MATURE_TICKS, matureTicks);
    }

    private int getNormalProcessTime() {
        int tier = getInputTier();
        if (tier <= GTValues.EV) {
            return 100;
        }
        if (tier == GTValues.IV) {
            return 50;
        }
        if (tier == GTValues.LuV) {
            return 34;
        }
        if (tier == GTValues.ZPM) {
            return 26;
        }
        return 20;
    }

    private int getCurrentCapacity() {
        return this.coreMode == CoreMode.NORMAL ? getNormalCapacity() : getCropRackCapacity();
    }

    private int getNormalCapacity() {
        int tier = getInputTier();
        if (tier < GTValues.EV) {
            return 0;
        }
        int shift = Math.max(0, tier - GTValues.EV);
        long capacity = NORMAL_BASE_CAPACITY << shift;
        return capacity > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) capacity;
    }

    private int getCropRackCapacity() {
        int tier = getInputTier();
        if (tier < GTValues.IV) {
            return 0;
        }
        int capacity = CROP_RACK_BASE_CAPACITY;
        for (int i = GTValues.IV; i < tier; i++) {
            if (capacity > Integer.MAX_VALUE / 4) {
                return Integer.MAX_VALUE;
            }
            capacity *= 4;
        }
        return capacity;
    }

    private int getInputTier() {
        if (this.energyContainer == null) {
            return 0;
        }
        return GTUtility.getTierByVoltage(this.energyContainer.getInputVoltage());
    }

    private <E extends Enum<E>> E readEnum(String name, E fallback, Class<E> enumClass) {
        try {
            return Enum.valueOf(enumClass, name);
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private enum WorkPhase {
        INPUT("\u8f93\u5165\u6a21\u5f0f"),
        OUTPUT("\u8f93\u51fa\u6a21\u5f0f"),
        RUN("\u8fd0\u884c\u6a21\u5f0f");

        private final String displayName;

        WorkPhase(String displayName) {
            this.displayName = displayName;
        }

        private WorkPhase next() {
            switch (this) {
                case INPUT:
                    return RUN;
                case RUN:
                    return OUTPUT;
                case OUTPUT:
                default:
                    return INPUT;
            }
        }
    }

    private enum CoreMode {
        NORMAL("\u666e\u901a\u4f5c\u7269\u6a21\u5f0f"),
        CROP_RACK("\u4f5c\u7269\u67b6\u6a21\u5f0f");

        private final String displayName;

        CoreMode(String displayName) {
            this.displayName = displayName;
        }

        private CoreMode next() {
            return values()[(ordinal() + 1) % values().length];
        }
    }

    private static class SupportBlockSelection {
        private final ItemStack template;

        private SupportBlockSelection(ItemStack template) {
            this.template = template;
        }

        private boolean sameStack(ItemStack stack) {
            return !stack.isEmpty()
                    && stack.getItem() == this.template.getItem()
                    && stack.getMetadata() == this.template.getMetadata();
        }
    }

    private static class CropRackProgressInfo {
        private final int elapsedTicks;
        private final int matureTicks;
        private final int remainingTicks;

        private CropRackProgressInfo(int elapsedTicks, int matureTicks, int remainingTicks) {
            this.elapsedTicks = elapsedTicks;
            this.matureTicks = matureTicks;
            this.remainingTicks = remainingTicks;
        }
    }

    private static class DeployedCropState {
        private ItemStack displaySeed = ItemStack.EMPTY;
        private int seedCount;
        private String cropQtId = "";
        private final List<ItemStack> cropQtSeedStacks = new ArrayList<>();
        private final List<Integer> cropQtElapsedTicks = new ArrayList<>();
        private final List<Integer> cropQtCycleFullFertilizedSeeds = new ArrayList<>();
        private final List<Integer> cropQtCyclePartialFertilizer = new ArrayList<>();
        private int cropStickCount;
        private ItemStack supportBlockTemplate = ItemStack.EMPTY;
        private int supportBlockCount;

        private boolean hasCrop() {
            return this.seedCount > 0 && !this.displaySeed.isEmpty();
        }

        private boolean isCropQt() {
            return !this.cropQtId.isEmpty();
        }

        private List<String> getSupportBlockIds() {
            if (this.supportBlockTemplate.isEmpty()) {
                return Collections.emptyList();
            }
            Block block = Block.getBlockFromItem(this.supportBlockTemplate.getItem());
            if (block == Blocks.AIR || block.getRegistryName() == null) {
                return Collections.emptyList();
            }
            List<String> blockIds = new ArrayList<>(2);
            String blockId = block.getRegistryName().toString();
            blockIds.add(blockId + ":" + this.supportBlockTemplate.getMetadata());
            blockIds.add(blockId);
            return blockIds;
        }

        private int getElapsedTicks(int index) {
            if (index < 0 || index >= this.cropQtElapsedTicks.size()) {
                return 0;
            }
            return this.cropQtElapsedTicks.get(index);
        }

        private void setElapsedTicks(int index, int ticks) {
            while (this.cropQtElapsedTicks.size() <= index) {
                this.cropQtElapsedTicks.add(0);
            }
            this.cropQtElapsedTicks.set(index, ticks);
        }

        private int getCycleFullFertilizedSeeds(int index) {
            if (index < 0 || index >= this.cropQtCycleFullFertilizedSeeds.size()) {
                return 0;
            }
            return this.cropQtCycleFullFertilizedSeeds.get(index);
        }

        private int getCyclePartialFertilizer(int index) {
            if (index < 0 || index >= this.cropQtCyclePartialFertilizer.size()) {
                return 0;
            }
            return this.cropQtCyclePartialFertilizer.get(index);
        }

        private void setCycleFertilizerPlan(int index, int fullSeeds, int partialFertilizer) {
            while (this.cropQtCycleFullFertilizedSeeds.size() <= index) {
                this.cropQtCycleFullFertilizedSeeds.add(0);
            }
            while (this.cropQtCyclePartialFertilizer.size() <= index) {
                this.cropQtCyclePartialFertilizer.add(0);
            }
            this.cropQtCycleFullFertilizedSeeds.set(index, fullSeeds);
            this.cropQtCyclePartialFertilizer.set(index, partialFertilizer);
        }

        private void clearCycleFertilizerPlan() {
            this.cropQtCycleFullFertilizedSeeds.clear();
            this.cropQtCyclePartialFertilizer.clear();
        }

        private NBTTagCompound writeToNBT() {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setInteger("SeedCount", this.seedCount);
            tag.setString("CropQtId", this.cropQtId);
            if (!this.displaySeed.isEmpty()) {
                tag.setTag("DisplaySeed", this.displaySeed.writeToNBT(new NBTTagCompound()));
            }
            tag.setInteger("CropStickCount", this.cropStickCount);
            if (!this.supportBlockTemplate.isEmpty()) {
                tag.setTag("SupportBlockTemplate", this.supportBlockTemplate.writeToNBT(new NBTTagCompound()));
                tag.setInteger("SupportBlockCount", this.supportBlockCount);
            }
            NBTTagList seedList = new NBTTagList();
            for (ItemStack stack : this.cropQtSeedStacks) {
                seedList.appendTag(stack.writeToNBT(new NBTTagCompound()));
            }
            tag.setTag("CropQtSeedStacks", seedList);
            NBTTagList elapsedList = new NBTTagList();
            for (Integer ticks : this.cropQtElapsedTicks) {
                NBTTagCompound elapsedTag = new NBTTagCompound();
                elapsedTag.setInteger("Ticks", ticks);
                elapsedList.appendTag(elapsedTag);
            }
            tag.setTag("CropQtElapsedTicks", elapsedList);
            NBTTagList fertilizerPlanList = new NBTTagList();
            int fertilizerPlanSize = Math.max(this.cropQtCycleFullFertilizedSeeds.size(), this.cropQtCyclePartialFertilizer.size());
            for (int i = 0; i < fertilizerPlanSize; i++) {
                NBTTagCompound fertilizerTag = new NBTTagCompound();
                fertilizerTag.setInteger("Full", getCycleFullFertilizedSeeds(i));
                fertilizerTag.setInteger("Partial", getCyclePartialFertilizer(i));
                fertilizerPlanList.appendTag(fertilizerTag);
            }
            tag.setTag("CropQtCycleFertilizer", fertilizerPlanList);
            return tag;
        }

        private void readFromNBT(NBTTagCompound tag) {
            this.displaySeed = ItemStack.EMPTY;
            this.seedCount = tag.getInteger("SeedCount");
            this.cropQtId = tag.getString("CropQtId");
            this.cropQtSeedStacks.clear();
            this.cropQtElapsedTicks.clear();
            this.cropQtCycleFullFertilizedSeeds.clear();
            this.cropQtCyclePartialFertilizer.clear();
            this.cropStickCount = tag.getInteger("CropStickCount");
            this.supportBlockTemplate = ItemStack.EMPTY;
            this.supportBlockCount = tag.getInteger("SupportBlockCount");

            if (tag.hasKey("DisplaySeed", Constants.NBT.TAG_COMPOUND)) {
                this.displaySeed = new ItemStack(tag.getCompoundTag("DisplaySeed"));
            }
            if (tag.hasKey("SupportBlockTemplate", Constants.NBT.TAG_COMPOUND)) {
                this.supportBlockTemplate = new ItemStack(tag.getCompoundTag("SupportBlockTemplate"));
            }
            NBTTagList seedList = tag.getTagList("CropQtSeedStacks", Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < seedList.tagCount(); i++) {
                this.cropQtSeedStacks.add(new ItemStack(seedList.getCompoundTagAt(i)));
            }
            NBTTagList elapsedList = tag.getTagList("CropQtElapsedTicks", Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < elapsedList.tagCount(); i++) {
                this.cropQtElapsedTicks.add(elapsedList.getCompoundTagAt(i).getInteger("Ticks"));
            }
            NBTTagList fertilizerPlanList = tag.getTagList("CropQtCycleFertilizer", Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < fertilizerPlanList.tagCount(); i++) {
                NBTTagCompound fertilizerTag = fertilizerPlanList.getCompoundTagAt(i);
                this.cropQtCycleFullFertilizedSeeds.add(fertilizerTag.getInteger("Full"));
                this.cropQtCyclePartialFertilizer.add(fertilizerTag.getInteger("Partial"));
            }
            while (this.cropQtElapsedTicks.size() < this.cropQtSeedStacks.size()) {
                this.cropQtElapsedTicks.add(0);
            }
            while (this.cropQtCycleFullFertilizedSeeds.size() < this.cropQtSeedStacks.size()) {
                this.cropQtCycleFullFertilizedSeeds.add(0);
            }
            while (this.cropQtCyclePartialFertilizer.size() < this.cropQtSeedStacks.size()) {
                this.cropQtCyclePartialFertilizer.add(0);
            }
        }
    }
}
