package com.drppp.drtech.common.metaTileEntities.muti.electric.generator;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.drppp.drtech.common.tile.TileEntityGravitationalAnomaly;
import com.drppp.drtech.common.blocks.BlocksInit;
import com.drppp.drtech.common.blocks.metaBlocks.MetaCasing;
import com.drppp.drtech.common.items.metaItems.DrMetaItems;
import com.drppp.drtech.api.capability.ipml.AnnihilationGeneratorLogic;
import gregtech.api.GTValues;
import gregtech.api.block.IHeatingCoilBlockStats;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IControllable;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.IWorkable;
import gregtech.api.capability.impl.EnergyContainerList;
import gregtech.api.capability.impl.ItemHandlerList;
import gregtech.api.metatileentity.IDataInfoProvider;
import gregtech.api.metatileentity.IFastRenderMetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockWithDisplayBase;
import gregtech.api.metatileentity.multiblock.ui.MultiblockUIBuilder;
import gregtech.api.pattern.FormedStructureView;
import gregtech.api.util.GTTransferUtils;
import gregtech.api.util.GTUtility;
import gregtech.api.util.KeyUtil;
import gregtech.api.worldgen.config.OreDepositDefinition;
import gregtech.api.worldgen.config.WorldGenRegistry;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;
import gregtech.integration.jei.basic.GTOreInfo;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static gregtech.common.blocks.BlockGlassCasing.CasingType.FUSION_GLASS;

import gregtech.api.pattern.casing.DeclarativePatternBuilder;

import gregtech.api.pattern.casing.GTCasingGroups;

import gregtech.api.pattern.casing.ICasing;

import gregtech.api.pattern.element.Elements;

import gregtech.api.pattern.element.StructureDefinition;

public class AnnihilationGenerator extends MultiblockWithDisplayBase implements IDataInfoProvider, IWorkable, IControllable, IFastRenderMetaTileEntity {
    private final AnnihilationGeneratorLogic logic;
    protected IEnergyContainer energyContainer = new EnergyContainerList(new ArrayList());
    protected ItemHandlerList itemImportInventory;
    protected ItemHandlerList itemOutInventory;
    protected TileEntity entity;
    private int leve;

    public AnnihilationGenerator(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
        this.logic = new AnnihilationGeneratorLogic(this);
    }

    @Override
    protected void updateFormedValid() {
        if (!this.getWorld().isRemote) {

            BlockPos pos = this.getPos();
            if (this.frontFacing == EnumFacing.EAST)
                this.entity = this.getWorld().getTileEntity(new BlockPos(pos.getX() - 2, pos.getY() + 3, pos.getZ()));
            if (this.frontFacing == EnumFacing.WEST)
                this.entity = this.getWorld().getTileEntity(new BlockPos(pos.getX() + 2, pos.getY() + 3, pos.getZ()));
            if (this.frontFacing == EnumFacing.SOUTH)
                this.entity = this.getWorld().getTileEntity(new BlockPos(pos.getX(), pos.getY() + 3, pos.getZ() - 2));
            if (this.frontFacing == EnumFacing.NORTH)
                this.entity = this.getWorld().getTileEntity(new BlockPos(pos.getX(), pos.getY() + 3, pos.getZ() + 2));
            var slots = itemImportInventory.getSlots();
            for (int i = 0; i < slots; i++) {
                ItemStack item = itemImportInventory.getStackInSlot(i);
                if (item.getItem() == DrMetaItems.ENERGY_ELEMENT_1.getMetaItem() && item.getMetadata() == DrMetaItems.ENERGY_ELEMENT_1.getMetaValue()) {
                    if (((TileEntityGravitationalAnomaly) entity).weight + 50 <= 400) {
                        ((TileEntityGravitationalAnomaly) entity).weight += 50;
                        itemImportInventory.extractItem(i, 1, false);
                    }
                } else if (item.getItem() == DrMetaItems.ENERGY_ELEMENT_2.getMetaItem() && item.getMetadata() == DrMetaItems.ENERGY_ELEMENT_2.getMetaValue()) {
                    if (((TileEntityGravitationalAnomaly) entity).weight + 100 <= 800) {
                        ((TileEntityGravitationalAnomaly) entity).weight += 100;
                        itemImportInventory.extractItem(i, 1, false);
                    }
                } else if (item.getItem() == DrMetaItems.ENERGY_ELEMENT_3.getMetaItem() && item.getMetadata() == DrMetaItems.ENERGY_ELEMENT_3.getMetaValue()) {
                    if (((TileEntityGravitationalAnomaly) entity).weight + 150 <= 1200) {
                        ((TileEntityGravitationalAnomaly) entity).weight += 150;
                        itemImportInventory.extractItem(i, 1, false);
                    }
                } else if (item.getItem() == DrMetaItems.ENERGY_ELEMENT_4.getMetaItem() && item.getMetadata() == DrMetaItems.ENERGY_ELEMENT_4.getMetaValue()) {
                    if (((TileEntityGravitationalAnomaly) entity).weight + 200 <= 1600) {
                        ((TileEntityGravitationalAnomaly) entity).weight += 200;
                        itemImportInventory.extractItem(i, 1, false);
                    }
                } else if (item.getItem() == DrMetaItems.ENERGY_ELEMENT_5.getMetaItem() && item.getMetadata() == DrMetaItems.ENERGY_ELEMENT_5.getMetaValue()) {
                    if (((TileEntityGravitationalAnomaly) entity).weight + 300 <= 2000) {
                        ((TileEntityGravitationalAnomaly) entity).weight += 300;
                        itemImportInventory.extractItem(i, 1, false);
                    }
                }
            }
            if (((TileEntityGravitationalAnomaly) entity).weight > 0) {
                this.logic.setActive(true);
                this.logic.setWorkingEnabled(true);
            } else {
                this.logic.setActive(false);
                //this.logic.setWorkingEnabled(false);
            }
            this.markDirty();
            logic.updateLogic((TileEntityGravitationalAnomaly) entity);
            List<OreDepositDefinition> oreVeins = WorldGenRegistry.getOreDeposits();
            List<GTOreInfo> oreInfoList = new ArrayList<>();
            for (OreDepositDefinition vein : oreVeins) {
                if (vein.getDimensionFilter().equals(this.getWorld().provider)) {
                    var ore = new GTOreInfo(vein);
                    var items = ore.findComponentBlocksAsItemStacks();
                    if (this.itemOutInventory != null && this.itemOutInventory.getSlots() > 0)
                        GTTransferUtils.addItemsToItemHandler(this.itemOutInventory, false, items);
                }
            }


        }

    }

    private static final StructureDefinition<?> STRUCTURE_DEFINITION =
            StructureDefinition.getOrBuild("drtech:annihilation_generator",
                    AnnihilationGenerator::buildTemplate);

    @Override
    protected @NotNull StructureDefinition<?> createStructureDefinition() {
        return STRUCTURE_DEFINITION;
    }

    private static StructureDefinition<?> buildTemplate() {
        return DeclarativePatternBuilder.start()
                .aisle("AAAAA", "AAAAA", "BBBBB", "BBBBB", "BBBBB", "TTTTT")
                .aisle("AAAAA", "AAAAA", "BXXXB", "B###B", "BXXXB", "TTTTT")
                .aisle("AAAAA", "AAAAA", "BXXXB", "B#W#B", "BXXXB", "TTTTT")
                .aisle("AAAAA", "AAAAA", "BXXXB", "B###B", "BXXXB", "TTTTT")
                .aisle("AASAA", "AAAAA", "BBBBB", "BBBBB", "BBBBB", "TTTTT")
                .self('S', AnnihilationGenerator.class)
                .blocks('T', getCasingState())
                .blocks('B', getGlassesState())
                .blocks('W', BlocksInit.BLOCK_GRAVITATIONAL_ANOMALY)
                .tieredCasing('X', GTCasingGroups.heatingCoils().group())
                .withChannel(GTCasingGroups.heatingCoils().channel())
                .where('A', Elements.chain(
                        Elements.counted(0, 4096, Elements.block(
                                MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.TITANIUM_STABLE))),
                        Elements.hatch(MultiblockAbility.MAINTENANCE_HATCH, 1, 1),
                        Elements.hatch(MultiblockAbility.OUTPUT_ENERGY, 0, 1),
                        Elements.hatch(MultiblockAbility.OUTPUT_LASER, 0, 1),
                        Elements.hatch(MultiblockAbility.IMPORT_FLUIDS, 1, -1),
                        Elements.hatch(MultiblockAbility.IMPORT_ITEMS, 1, -1),
                        Elements.hatch(MultiblockAbility.EXPORT_ITEMS, 0, 1)))
                .any('#')
                .buildStructureDefinition();

    }
    @Override
    public boolean usesMui2() {
        return false;
    }
    protected static IBlockState getCasingState() {
        return BlocksInit.COMMON_CASING.getState(MetaCasing.MetalCasingType.GRAVITATION_FIELD_CASING);
    }

    protected static IBlockState getGlassesState() {
        return MetaBlocks.TRANSPARENT_CASING.getState(FUSION_GLASS);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return Textures.STABLE_TITANIUM_CASING;
    }

    public void addInformation(ItemStack stack, @Nullable World world, @NotNull List<String> tooltip, boolean advanced) {
        super.addInformation(stack, world, tooltip, advanced);
        tooltip.add(I18n.format("drtech.machine.annihilation_generator.tooltip.1"));
        tooltip.add(I18n.format("drtech.machine.annihilation_generator.tooltip.2"));
        tooltip.add(I18n.format("drtech.machine.annihilation_generator.tooltip.3"));
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new AnnihilationGenerator(this.metaTileEntityId);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (capability == GregtechTileCapabilities.CAPABILITY_CONTROLLABLE) {
            return GregtechTileCapabilities.CAPABILITY_CONTROLLABLE.cast(this);
        }
        return super.getCapability(capability, side);
    }

    @Override
    protected void configureDisplayText(MultiblockUIBuilder builder) {
        super.configureDisplayText(builder);
        builder.setWorkingStatus(this.isWorkingEnabled(), this.isActive())
                .addCustom((keyManager, syncer) -> {
                    if (!isStructureFormed()) {
                        return;
                    }
                    boolean active = syncer.syncBoolean(this::isActive);
                    boolean working = syncer.syncBoolean(this::isWorkingEnabled);
                    long capacity = syncer.syncLong(
                            () -> this.energyContainer == null ? 0 : this.energyContainer.getEnergyCapacity());
                    long maxVoltage = syncer.syncLong(() -> this.energyContainer == null ? 0
                            : Math.max(this.energyContainer.getInputVoltage(), this.energyContainer.getOutputVoltage()));
                    int progress = syncer.syncInt(this::getProgress);
                    int maxProgress = syncer.syncInt(this::getMaxProgress);
                    int leve = syncer.syncInt(() -> this.leve);
                    int weight = syncer.syncInt(() -> this.logic.weight);
                    long mEUt = syncer.syncLong(this.logic::getmEUt);

                    keyManager.add(richText -> {
                        if (capacity > 0) {
                            String voltageName = GTValues.VN[GTUtility.getFloorTierByVoltage(maxVoltage)];
                            richText.add(KeyUtil.lang(TextFormatting.GRAY, "gregtech.multiblock.max_energy_per_tick",
                                    KeyUtil.number(TextFormatting.WHITE, maxVoltage),
                                    KeyUtil.string(TextFormatting.WHITE, voltageName)))
                                    .newLine();
                        }
                        if (!working) {
                            richText.add(IKey.lang("gregtech.multiblock.work_paused")).newLine();
                        } else if (active) {
                            richText.add(IKey.lang("gregtech.multiblock.running")).newLine();
                            int currentProgress = maxProgress <= 0 ? 0
                                    : (int) (((float) progress / (float) maxProgress) * 100);
                            richText.add(IKey.lang("gregtech.multiblock.progress",
                                    KeyUtil.number(TextFormatting.WHITE, currentProgress)))
                                    .newLine();
                        } else {
                            richText.add(IKey.lang("gregtech.multiblock.idling")).newLine();
                        }
                        richText.add(KeyUtil.lang(TextFormatting.GRAY, "drtech.multiblock.tire",
                                KeyUtil.number(TextFormatting.WHITE, leve)))
                                .newLine();
                        richText.add(KeyUtil.lang(TextFormatting.GRAY, "drtech.multiblock.beilv",
                                KeyUtil.string(TextFormatting.WHITE, String.valueOf(leve * 0.25))))
                                .newLine();
                        richText.add(KeyUtil.lang(TextFormatting.YELLOW, "gregtech.multiblock.weight",
                                KeyUtil.number(TextFormatting.WHITE, weight)))
                                .newLine();
                        richText.add(KeyUtil.lang(TextFormatting.YELLOW, "gregtech.multiblock.mEUt",
                                KeyUtil.number(TextFormatting.WHITE, mEUt),
                                KeyUtil.string(TextFormatting.WHITE, "EU/T")))
                                .newLine();
                    });
                });
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        this.energyContainer = new EnergyContainerList(new ArrayList());
        this.itemImportInventory = new ItemHandlerList(Collections.emptyList());
        this.itemOutInventory = new ItemHandlerList(Collections.emptyList());
    }

    @Override
    protected void formStructure(@NotNull FormedStructureView formed) {
        super.formStructure(formed);
        List<IEnergyContainer> energyContainer = new ArrayList(this.getAbilities(MultiblockAbility.OUTPUT_ENERGY));
        energyContainer.addAll(this.getAbilities(MultiblockAbility.OUTPUT_LASER));
        this.energyContainer = new EnergyContainerList(energyContainer);
        this.itemImportInventory = new ItemHandlerList(getAbilities(MultiblockAbility.IMPORT_ITEMS));
        this.itemOutInventory = new ItemHandlerList(getAbilities(MultiblockAbility.EXPORT_ITEMS));

        ICasing matchedCoil = GTCasingGroups.heatingCoils().channel().getMatchedCasing(formed);
        IHeatingCoilBlockStats type = matchedCoil == null ? null :
                matchedCoil.getPayloadAs(IHeatingCoilBlockStats.class);
        this.leve = type == null ? 1 : type.getLevel();
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        this.getFrontOverlay().renderOrientedState(renderState, translation, pipeline, this.getFrontFacing(), this.isActive(), this.isWorkingEnabled());
    }

    @Override
    public boolean isActive() {
        return (isStructureFormed() && this.logic.isActive() && this.logic.isWorkingEnabled());
    }

    @Override
    public boolean isWorkingEnabled() {
        return logic.isWorkingEnabled();
    }

    @Override
    public void setWorkingEnabled(boolean b) {
        logic.setWorkingEnabled(b);
    }

    @Override
    public int getProgress() {
        return logic.getProgressTime();
    }

    @Override
    public int getMaxProgress() {
        return logic.getMaxProgress();
    }

    @Override
    public List<ITextComponent> getDataInfo() {
        return new LinkedList<>();
    }


    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        return this.logic.writeToNBT(data);
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.logic.readFromNBT(data);
    }

    public IEnergyContainer getEnergyContainer() {
        return energyContainer;
    }

    public ItemHandlerList getItemImportInventory() {
        return itemImportInventory;
    }

    public int getLeve() {
        return leve;
    }

    public boolean shouldShowVoidingModeButton() {
        return false;
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        this.logic.receiveCustomData(dataId, buf);
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        this.logic.writeInitialSyncData(buf);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.logic.receiveInitialSyncData(buf);
    }

    @Override
    public void addToolUsages(ItemStack stack, @Nullable World world, List<String> tooltip, boolean advanced) {
        super.addToolUsages(stack, world, tooltip, advanced);
    }


    @Override
    public void renderMetaTileEntity(double x, double y, double z, float partialTicks) {
        IFastRenderMetaTileEntity.super.renderMetaTileEntity(x, y, z, partialTicks);

    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return new AxisAlignedBB(getPos(), getPos().add(5, 10, 5));
    }


}
