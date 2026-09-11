package com.drppp.drtech.common.metaTileEntities.muti.electric.standard;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.drawable.ItemDrawable;
import com.drppp.drtech.api.utils.DrtechUtils;
import gregtech.api.GTValues;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.ui.MultiblockUIBuilder;
import gregtech.api.pattern.casing.DeclarativePatternBuilder;
import gregtech.api.pattern.element.Elements;
import gregtech.api.pattern.element.StructureDefinition;
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
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * 作物模拟机：**只跑原版作物**。
 *
 * <p>喂 IC2 风格的作物种子（{@link DrtechUtils#ItemCrops} 里登记的那些），
 * 机器把它们"种"在内部的虚拟田里，按周期结算产出。三级流程靠螺丝刀切换：
 * 输入 → 运行 → 输出。
 *
 * <p><b>CropQT 的作物已经从这里剥离</b>，由 {@code MetaTileEntityIndustrialFarm}
 * 负责——那台机器有完整的土壤 / 底土 / 营养模型，这里留着的老实现只会跟它算出两套数。
 * 现在这里只剩原版作物这一条路径，逻辑没动过。
 */
public class MetaTileentityCropsSimulateMachine extends MetaTileEntityBaseWithControl {
    private static final int WATER_PER_CROP = 1000;
    private static final long NORMAL_BASE_CAPACITY = 64;
    private static final int NORMAL_FERTILIZER_PER_CROP = 2;
    private static final int PREVIEW_LIMIT = 4;

    private ItemStack seed = ItemStack.EMPTY;
    private int seedCout = 0;
    private WorkPhase workPhase = WorkPhase.INPUT;
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

    private static final StructureDefinition<?> STRUCTURE_DEFINITION =
            StructureDefinition.getOrBuild("drtech:crops_simulate_machine",
                    MetaTileentityCropsSimulateMachine::buildTemplate);

    @Override
    protected @NotNull StructureDefinition<?> createStructureDefinition() {
        return STRUCTURE_DEFINITION;
    }

    private static StructureDefinition<?> buildTemplate() {
        return DeclarativePatternBuilder.start()
                .aisle("AAAAA", "AAAAA", "BBBBB", "BBBBB", "BBBBB", "AAAAA")
                .aisle("AAAAA", "AAAAA", "BXXXB", "B###B", "B###B", "AAAAA")
                .aisle("AAAAA", "AAAAA", "BXWXB", "B###B", "B###B", "AAAAA")
                .aisle("AAAAA", "AAAAA", "BXXXB", "B###B", "B###B", "AAAAA")
                .aisle("AASAA", "AAAAA", "BBBBB", "BBBBB", "BBBBB", "AAAAA")
                .self('S', MetaTileentityCropsSimulateMachine.class)
                .blocks('B', getGlassesState())
                .blocks('X', Blocks.FARMLAND)
                .blocks('W', Blocks.WATER)
                .where('A', Elements.chain(
                        Elements.counted(0, 4096, Elements.block(getCasingState())),
                        Elements.hatch(MultiblockAbility.MAINTENANCE_HATCH,
                                gregtech.common.ConfigHolder.machines.enableMaintenance ? 1 : 0, 1),
                        Elements.hatch(MultiblockAbility.INPUT_ENERGY, 0, 2),
                        Elements.hatch(MultiblockAbility.IMPORT_FLUIDS, 1, 4),
                        Elements.hatch(MultiblockAbility.IMPORT_ITEMS, 1, 4),
                        Elements.hatch(MultiblockAbility.EXPORT_ITEMS, 1, 4)))
                .any('#')
                .buildStructureDefinition();
    }

    public static IBlockState getCasingState() {
        return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STAINLESS_CLEAN);
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
                    int deployed = syncer.syncInt(getTotalDeployedCount());
                    int varieties = syncer.syncInt(getDeployedVarietyCount());
                    int capacity = syncer.syncInt(getCapacity());
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
            this.workPhase = this.workPhase.next();
            this.process = 0;
            this.maxProcess = getCurrentProcessTime();
            markDirty();
            playerIn.sendMessage(new TextComponentString(TextFormatting.GREEN + "工作阶段: " + this.workPhase.displayName));
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

    // ==================== 部署 / 收回 ====================

    private void deployFromInput() {
        if (this.inputInventory == null) {
            return;
        }
        deployNormalCrops();
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

    private void undeployToOutput() {
        if (this.deployedCrops.isEmpty() || this.outputInventory == null) {
            return;
        }

        NonNullList<ItemStack> outlist = NonNullList.create();
        for (DeployedCropState state : this.deployedCrops) {
            outlist.addAll(splitStacks(state.displaySeed, state.seedCount));
        }
        if (!outlist.isEmpty()) {
            GTTransferUtils.addItemsToItemHandler(this.outputInventory, false, outlist);
        }
        this.deployedCrops.clear();
    }

    // ==================== 运行 ====================

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
            remainingFertilizer = runNormalEntry(state, outlist, remainingFertilizer);
        }

        int fertilizerUsed = availableFertilizer - remainingFertilizer;
        if (fertilizerUsed > 0) {
            extractFertilizer(fertilizerUsed);
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

    private boolean canRunCurrentMode() {
        if (this.deployedCrops.isEmpty()) {
            return false;
        }

        for (DeployedCropState state : this.deployedCrops) {
            if (!state.hasCrop()) {
                return false;
            }
            if (!DrtechUtils.ItemCrops.containsKey(state.displaySeed.getItem())) {
                return false;
            }
        }
        return true;
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

    // ==================== 摘要 / 预览 ====================

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
        IBlockState cropState = DrtechUtils.ItemCrops.get(state.displaySeed.getItem());
        if (cropState == null) {
            return Collections.emptyList();
        }
        NonNullList<ItemStack> sample = NonNullList.create();
        appendCropDrops(sample, cropState);
        return sample;
    }

    private String buildPreviewKey(ItemStack stack) {
        String registryName = stack.getItem().getRegistryName() == null
                ? stack.getItem().getTranslationKey()
                : stack.getItem().getRegistryName().toString();
        String nbt = stack.hasTagCompound() ? stack.getTagCompound().toString() : "";
        return registryName + ":" + stack.getMetadata() + ":" + nbt;
    }

    // ==================== 库存辅助 ====================

    private boolean isFertilizer(ItemStack stack) {
        return !stack.isEmpty()
                && stack.getItem() == MetaItems.FERTILIZER.getMetaItem()
                && stack.getMetadata() == MetaItems.FERTILIZER.getMetaValue();
    }

    private DeployedCropState findNormalEntry(Item seedItem) {
        for (DeployedCropState state : this.deployedCrops) {
            if (!state.displaySeed.isEmpty() && state.displaySeed.getItem() == seedItem) {
                return state;
            }
        }
        return null;
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

    // ==================== 数值 ====================

    private long getRunEnergyCost() {
        long inputVoltage = this.energyContainer == null ? 0 : this.energyContainer.getInputVoltage();
        if (inputVoltage <= 0) {
            return 0;
        }
        int amp = getAbilities(MultiblockAbility.INPUT_ENERGY).size() >= 2 ? 4 : 1;
        return inputVoltage * amp;
    }

    private int getCurrentProcessTime() {
        return getNormalProcessTime();
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

    private int getCapacity() {
        return getNormalCapacity();
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

    // ==================== 内部类型 ====================

    private enum WorkPhase {
        INPUT("输入模式"),
        OUTPUT("输出模式"),
        RUN("运行模式");

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

    private static class DeployedCropState {
        private ItemStack displaySeed = ItemStack.EMPTY;
        private int seedCount;

        private boolean hasCrop() {
            return this.seedCount > 0 && !this.displaySeed.isEmpty();
        }

        private NBTTagCompound writeToNBT() {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setInteger("SeedCount", this.seedCount);
            if (!this.displaySeed.isEmpty()) {
                tag.setTag("DisplaySeed", this.displaySeed.writeToNBT(new NBTTagCompound()));
            }
            return tag;
        }

        private void readFromNBT(NBTTagCompound tag) {
            this.displaySeed = ItemStack.EMPTY;
            this.seedCount = tag.getInteger("SeedCount");
            if (tag.hasKey("DisplaySeed", Constants.NBT.TAG_COMPOUND)) {
                this.displaySeed = new ItemStack(tag.getCompoundTag("DisplaySeed"));
            }
        }
    }
}
