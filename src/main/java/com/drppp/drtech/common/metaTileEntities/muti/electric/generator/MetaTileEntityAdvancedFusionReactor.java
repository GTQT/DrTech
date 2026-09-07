package com.drppp.drtech.common.metaTileEntities.muti.electric.generator;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import com.drppp.drtech.client.Textures;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.drppp.drtech.common.blocks.BlocksInit;
import com.drppp.drtech.common.blocks.metaBlocks.BlockFusionReactorCasing3;
import com.drppp.drtech.common.metaTileEntities.muti.electric.standard.MetaTileEntityBaseWithControl;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.ui.MultiblockUIBuilder;
import gregtech.api.pattern.FormedStructureView;
import gregtech.api.pattern.casing.DeclarativePatternBuilder;
import gregtech.api.pattern.casing.ICasing;
import gregtech.api.pattern.casing.StructureChannel;
import gregtech.api.pattern.element.Elements;
import gregtech.api.pattern.element.StructureDefinition;
import gregtech.api.util.KeyUtil;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.cube.OrientedOverlayRenderer;
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
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;

public class MetaTileEntityAdvancedFusionReactor extends MetaTileEntityBaseWithControl {

    private final FusionReactorLogic logic;

    private FusionCasingStats firstWallStats;
    private FusionCasingStats coolantStats;
    private FusionCasingStats neutronStats;
    private FusionCasingStats breedingStats;
    private FusionCasingStats magnetStats;

    private static StructureDefinition<?> STRUCTURE;

    @Override
    protected @NotNull StructureDefinition<?> createStructureDefinition() {
        if (STRUCTURE == null) {
            STRUCTURE = buildStructureDefinition();
        }
        return STRUCTURE;
    }

    private static StructureDefinition<?> buildStructureDefinition() {
        IBlockState radiation = getCasingState(BlockFusionReactorCasing3.CasingType.RADIATION_SHIELDING_CASING);
        return DeclarativePatternBuilder.start()
                .aisle("                     ", "                     ", "                     ", "                     ", "                     ", "       RRRMRRR       ", "                     ", "                     ", "                     ", "                     ", "                     ")
                .aisle("                     ", "                     ", "                     ", "       RRRMRRR       ", "       RRRMRRR       ", "     RRNNNNNNNRR     ", "       RRRMRRR       ", "       RRRMRRR       ", "                     ", "                     ", "                     ")
                .aisle("                     ", "                     ", "       RRRMRRR       ", "     RRWWWWWWWRR     ", "     RRWWWWWWWRR     ", "    RNN       NNR    ", "     RRWWWWWWWRR     ", "     RRWWWWWWWRR     ", "       RRRMRRR       ", "                     ", "                     ")
                .aisle("                     ", "       RRRMRRR       ", "     RRWWWWWWWRR     ", "   MRWW       WWRM   ", "   MRWW       WWRM   ", "   MN           NM   ", "   MRWW       WWRM   ", "   MRWW       WWRM   ", "     RRWWWWWWWRR     ", "       RRRMRRR       ", "                     ")
                .aisle("          M          ", "    MRRCCCCCCCRRM    ", "    MWW       WWM    ", "   RW           WR   ", "   RW           WR   ", "  RN             NR  ", "   RW           WR   ", "   RW           WR   ", "    MWW       WWM    ", "    MRRBBBBBBBRRM    ", "          M          ")
                .aisle("     M    M    M     ", "    RCCCCCCCCCCCR    ", "   RW           WR   ", "  RW             WR  ", "  RW             WR  ", " RN               NR ", "  RW             WR  ", "  RW             WR  ", "   RW           WR   ", "    RBBBBBBBBBBBR    ", "     M    M    M     ")
                .aisle("      M       M      ", "    RCCC  M  CCCR    ", "   RW   WWWWW   WR   ", "  RW             WR  ", "  RW             WR  ", " RN               NR ", "  RW             WR  ", "  RW             WR  ", "   RW   WWWWW   WR   ", "    RBBB  M  BBBR    ", "      M       M      ")
                .aisle("                     ", "   RCCCM     MCCCR   ", "  RW   W     W   WR  ", " RW     WWWWW     WR ", " RW      WWW      WR ", "RN       NNN       NR", " RW      WWW      WR ", " RW     WWWWW     WR ", "  RW   W     W   WR  ", "   RBBBM     MBBBR   ", "                     ")
                .aisle("                     ", "   RCC         CCR   ", "  RW  W M   M W  WR  ", " RW    WM   MW    WR ", " RW     W   W     WR ", "RN      N   N      NR", " RW     W   W     WR ", " RW    WM   MW    WR ", "  AW  W M   M W  WR  ", "   RBB         BBR   ", "                     ")
                .aisle("                     ", "   RCC         CCR   ", "  AW  W   P   W  WR  ", " AW    W PMP W    WR ", " AW    W MMM W    WR ", "AN     N MMM N     NR", " AW    W MMM W    WR ", " AW    W PMP W    WR ", "  RW  W   P   W  WR  ", "   RBB         BBR   ", "                     ")
                .aisle("    MM         MM    ", "   MCCM   P   MCCM   ", "  MW  W  PMP  W  WM  ", " MW    W MMM W    WM ", " MW    W MMM W    WM ", "MN     N MMM N     NM", " MW    W MMM W    WM ", " MW    W MMM W    WM ", "  MW  W  PMP  W  WM  ", "   MBBM   P   MBBM   ", "    MM         MM    ")
                .aisle("                     ", "   RCC         CCR   ", "  RW  W   P   W  WR  ", " RW    W PMP W    WR ", " RW    W MMM W    WR ", "RN     N MMM N     NR", " RW    W MMM W    WR ", " RW    W PMP W    WR ", "  AW  W   P   W  WR  ", "   RBB         BBR   ", "                     ")
                .aisle("                     ", "   RCC         CCR   ", "  RW  W M   M W  WR  ", " RW    WM   MW    WR ", " RW     W   W     WR ", "RN      N   N      NR", " RW     W   W     WR ", " AW    WM   MW    WR ", "  RW  W M   M W  WR  ", "   RBB         BBR   ", "                     ")
                .aisle("                     ", "   RCCCM     MCCCR   ", "  RW   W     W   WR  ", " RW     WWWWW     WR ", " RW      WWW      WR ", "RN       NNN       NR", " RW      WWW      WR ", " RW     WWWWW     WR ", "  RW   W     W   WR  ", "   RBBBM     MBBBR   ", "                     ")
                .aisle("      M       M      ", "    RCCC  M  CCCR    ", "   RW   WWWWW   WR   ", "  RW             WR  ", "  RW             WR  ", " RN               NR ", "  RW             WR  ", "  RW             WR  ", "   RW   WWWWW   WR   ", "    RBBB  M  BBBR    ", "      M       M      ")
                .aisle("     M    M    M     ", "    RCCCCCCCCCCCR    ", "   RW           WR   ", "  RW             WR  ", "  RW             WR  ", " RN               NR ", "  RW             WR  ", "  RW             WR  ", "   RW           WR   ", "    RBBBBBBBBBBBR    ", "     M    M    M     ")
                .aisle("          M          ", "    MRRCCCCCCCRRM    ", "    MWW       WWM    ", "   RW           WR   ", "   RW           WR   ", "  RN             NR  ", "   RW           WR   ", "   RW           WR   ", "    MWW       WWM    ", "    MRRBBBBBBBRRM    ", "          M          ")
                .aisle("                     ", "       RRRMRRR       ", "     RRWWWWWWWRR     ", "   MRWW       WWRM   ", "   MRWW       WWRM   ", "   MN           NM   ", "   MRWW       WWRM   ", "   MRWW       WWRM   ", "     RRWWWWWWWRR     ", "       RRRMRRR       ", "                     ")
                .aisle("                     ", "                     ", "       RRRMRRR       ", "     RRWWWWWWWRR     ", "     RRWWWWWWWRR     ", "    RNN       NNR    ", "     RRWWWWWWWRR     ", "     RRWWWWWWWRR     ", "       RRRMRRR       ", "                     ", "                     ")
                .aisle("                     ", "                     ", "                     ", "       RRRMRRR       ", "       RRRMRRR       ", "     RRNNNNNNNRR     ", "       RRRMRRR       ", "       RRRMRRR       ", "                     ", "                     ", "                     ")
                .aisle("                     ", "                     ", "                     ", "                     ", "          S          ", "       RRRMRRR       ", "                     ", "                     ", "                     ", "                     ", "                     ")
                .where('S', Elements.self(MetaTileEntityAdvancedFusionReactor.class))
                .where(' ', Elements.any())
                .where('R', Elements.block(radiation))
                .where('P', Elements.block(getCasingState(BlockFusionReactorCasing3.CasingType.PLASMA_CONTAINMENT_CASING)))
                .where('A', Elements.chain(
                        Elements.blocks(radiation),
                        Elements.abilities(1, 1, MultiblockAbility.MAINTENANCE_HATCH),
                        Elements.abilities(0, 4, MultiblockAbility.OUTPUT_ENERGY),
                        Elements.abilities(0, 2, MultiblockAbility.OUTPUT_LASER),
                        Elements.abilities(0, 4, MultiblockAbility.INPUT_ENERGY),
                        Elements.abilities(1, 4, MultiblockAbility.IMPORT_FLUIDS),
                        Elements.abilities(0, 1, MultiblockAbility.EXPORT_FLUIDS)
                ))
                .tieredCasing('B', FusionCasingGroups.tritiumBreeding().group())
                .withChannel(FusionCasingGroups.tritiumBreeding().channel())
                .tieredCasing('C', FusionCasingGroups.coolant().group())
                .withChannel(FusionCasingGroups.coolant().channel())
                .tieredCasing('W', FusionCasingGroups.firstWall().group())
                .withChannel(FusionCasingGroups.firstWall().channel())
                .tieredCasing('N', FusionCasingGroups.neutronCapture().group())
                .withChannel(FusionCasingGroups.neutronCapture().channel())
                .tieredCasing('M', FusionCasingGroups.magnet().group())
                .withChannel(FusionCasingGroups.magnet().channel())
                .done()
                .buildStructureDefinition();
    }

    protected static IBlockState getCasingState(BlockFusionReactorCasing3.CasingType type) {
        return BlocksInit.FUSION_REACTOR_CASING3.getState(type);
    }

    public MetaTileEntityAdvancedFusionReactor(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
        this.logic = new FusionReactorLogic(this);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityAdvancedFusionReactor(this.metaTileEntityId);
    }

    // ============ 结构生命周期 ============

    @Override
    protected void formStructure(@NotNull FormedStructureView formed) {
        super.formStructure(formed);
        this.firstWallStats = readCasing(FusionCasingGroups.firstWall().channel(), formed);
        this.coolantStats = readCasing(FusionCasingGroups.coolant().channel(), formed);
        this.neutronStats = readCasing(FusionCasingGroups.neutronCapture().channel(), formed);
        this.breedingStats = readCasing(FusionCasingGroups.tritiumBreeding().channel(), formed);
        this.magnetStats = readCasing(FusionCasingGroups.magnet().channel(), formed);
        this.logic.onStructureFormed();
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        this.firstWallStats = null;
        this.coolantStats = null;
        this.neutronStats = null;
        this.breedingStats = null;
        this.magnetStats = null;
        this.logic.onStructureInvalidated();
    }

    private static FusionCasingStats readCasing(StructureChannel channel, FormedStructureView formed) {
        if (channel == null || formed == null) return null;
        ICasing casing = channel.getMatchedCasing(formed);
        return casing == null ? null : casing.getPayloadAs(FusionCasingStats.class);
    }

    // ============ 运行 ============

    @Override
    protected void updateFormedValid() {
        if (!this.getWorld().isRemote) {
            FusionReactorLogic.State old = this.logic.getState();
            this.logic.updateLogic();
            if (old != this.logic.getState()) {
                this.logic.syncState();
            }
        }
    }

    @Override
    public boolean isActive() {
        return isStructureFormed() && this.logic.isActive() && isWorkingEnabled();
    }

    // ============ 显示（MUI2） ============

    @Override
    protected void configureDisplayText(MultiblockUIBuilder builder) {
        super.configureDisplayText(builder);
        builder.setWorkingStatus(this.isWorkingEnabled(), this.isActive())
                .addCustom((keyManager, syncer) -> {
                    if (!isStructureFormed()) {
                        return;
                    }
                    FusionReactorLogic logic = this.logic;
                    int stateOrdinal = syncer.syncInt(logic.getState().ordinal());
                    long outputEU = syncer.syncLong(logic.getOutputEU());
                    long coreOutput = syncer.syncLong(logic.getCoreOutput());
                    int heat = syncer.syncInt(logic.getHeatProgress());
                    int maxHeat = syncer.syncInt(logic.getMaxHeat());
                    long magnetizeStored = syncer.syncLong(logic.getMagnetizeStored());
                    long magnetizeNeeded = syncer.syncLong(logic.getMagnetizeNeeded());
                    int fuelConsumption = syncer.syncInt(logic.getFuelConsumption());
                    long rfPower = syncer.syncLong(logic.getRfPower());
                    int firstWallTier = syncer.syncInt(() -> tierOf(this.firstWallStats));
                    int coolantTier = syncer.syncInt(() -> tierOf(this.coolantStats));
                    int neutronTier = syncer.syncInt(() -> tierOf(this.neutronStats));
                    int breedingTier = syncer.syncInt(() -> tierOf(this.breedingStats));
                    int magnetTier = syncer.syncInt(() -> tierOf(this.magnetStats));

                    FusionReactorLogic.State state = FusionReactorLogic.State.values()[stateOrdinal];
                    keyManager.add(richText -> {
                        String stateKey;
                        switch (state) {
                            case MAGNETIZING:
                                stateKey = "drtech.multiblock.fusion.state.magnetizing";
                                break;
                            case FUEL_INJECTING:
                                stateKey = "drtech.multiblock.fusion.state.injecting";
                                break;
                            case RF_HEATING:
                                stateKey = "drtech.multiblock.fusion.state.heating";
                                break;
                            case IGNITED:
                                stateKey = "drtech.multiblock.fusion.state.ignited";
                                break;
                            case RUNNING:
                                stateKey = "drtech.multiblock.fusion.state.running";
                                break;
                            default:
                                stateKey = "drtech.multiblock.fusion.state.offline";
                                break;
                        }
                        richText.add(KeyUtil.lang(TextFormatting.AQUA, "drtech.multiblock.fusion.state",
                                IKey.lang(stateKey).style(TextFormatting.AQUA)))
                                .newLine();
                        richText.add(KeyUtil.lang(TextFormatting.YELLOW, "drtech.multiblock.fusion.output",
                                KeyUtil.number(TextFormatting.WHITE, outputEU)))
                                .newLine();
                        richText.add(KeyUtil.lang(TextFormatting.GRAY, "drtech.multiblock.fusion.core_output",
                                KeyUtil.number(TextFormatting.WHITE, coreOutput)))
                                .newLine();
                        richText.add(KeyUtil.lang(TextFormatting.GRAY, "drtech.multiblock.fusion.heat",
                                KeyUtil.number(TextFormatting.WHITE, heat),
                                KeyUtil.number(TextFormatting.WHITE, maxHeat)))
                                .newLine();
                        if (state == FusionReactorLogic.State.MAGNETIZING) {
                            richText.add(KeyUtil.lang(TextFormatting.GRAY, "drtech.multiblock.fusion.magnetize",
                                    KeyUtil.number(TextFormatting.WHITE, magnetizeStored),
                                    KeyUtil.number(TextFormatting.WHITE, magnetizeNeeded)))
                                    .newLine();
                        }
                        if (state == FusionReactorLogic.State.RUNNING) {
                            richText.add(KeyUtil.lang(TextFormatting.GRAY, "drtech.multiblock.fusion.fuel",
                                    KeyUtil.number(TextFormatting.WHITE, fuelConsumption)))
                                    .newLine();
                        }
                        if (rfPower > 0) {
                            richText.add(KeyUtil.lang(TextFormatting.GRAY, "drtech.multiblock.fusion.rf",
                                    KeyUtil.number(TextFormatting.WHITE, rfPower)))
                                    .newLine();
                        }
                        richText.add(KeyUtil.lang(TextFormatting.GRAY, "drtech.multiblock.fusion.tier",
                                KeyUtil.string(TextFormatting.WHITE, tierText(firstWallTier)),
                                KeyUtil.string(TextFormatting.WHITE, tierText(coolantTier)),
                                KeyUtil.string(TextFormatting.WHITE, tierText(neutronTier)),
                                KeyUtil.string(TextFormatting.WHITE, tierText(breedingTier)),
                                KeyUtil.string(TextFormatting.WHITE, tierText(magnetTier))))
                                .newLine();
                    });
                });
    }

    private static int tierOf(FusionCasingStats stats) {
        return stats == null ? -1 : stats.tier;
    }

    private static String tierText(int tier) {
        return tier < 0 ? "-" : String.valueOf(tier);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, @NotNull List<String> tooltip, boolean advanced) {
        super.addInformation(stack, world, tooltip, advanced);
        tooltip.add(I18n.format("drtech.machine.fusion_reactor.tooltip.1"));
        tooltip.add(I18n.format("drtech.machine.fusion_reactor.tooltip.2"));
        tooltip.add(I18n.format("drtech.machine.fusion_reactor.tooltip.3"));
    }

    @Override
    public @NotNull List<ITextComponent> getDataInfo() {
        return new LinkedList<>();
    }

    // ============ 渲染 ============

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return Textures.FUSION_REACTOR_CASING;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        if (this.getFrontOverlay() instanceof OrientedOverlayRenderer) {
            this.getFrontOverlay().renderOrientedState(renderState, translation, pipeline,
                    Cuboid6.full, this.getFrontFacing(), this.isActive(), this.isWorkingEnabled());
        } else {
            this.getFrontOverlay().render(renderState, translation, pipeline);
        }
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (capability == GregtechTileCapabilities.CAPABILITY_CONTROLLABLE) {
            return GregtechTileCapabilities.CAPABILITY_CONTROLLABLE.cast(this);
        }
        return super.getCapability(capability, side);
    }

    // ============ 存取 ============

    public FusionCasingStats getFirstWallStats() {
        return firstWallStats;
    }

    public FusionCasingStats getCoolantStats() {
        return coolantStats;
    }

    public FusionCasingStats getNeutronStats() {
        return neutronStats;
    }

    public FusionCasingStats getBreedingStats() {
        return breedingStats;
    }

    public FusionCasingStats getMagnetStats() {
        return magnetStats;
    }

    public FusionReactorLogic getLogic() {
        return logic;
    }

    // ============ NBT / 网络 ============

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
    public void receiveCustomData(int dataId, @NotNull PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        this.logic.receiveCustomData(dataId, buf);
    }
}
