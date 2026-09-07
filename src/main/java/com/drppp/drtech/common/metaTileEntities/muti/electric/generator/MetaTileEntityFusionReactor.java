package com.drppp.drtech.common.MetaTileEntities.muti.electric.generator;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import com.drppp.drtech.common.Blocks.BlocksInit;
import com.drppp.drtech.common.Blocks.MetaBlocks.BlockFusionReactorCasing;
import com.drppp.drtech.common.MetaTileEntities.muti.electric.standard.MetaTileEntityBaseWithControl;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.metatileentity.IDataInfoProvider;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.pattern.FormedStructureView;
import gregtech.api.pattern.casing.DeclarativePatternBuilder;
import gregtech.api.pattern.casing.ICasing;
import gregtech.api.pattern.casing.StructureChannel;
import gregtech.api.pattern.element.Elements;
import gregtech.api.pattern.element.StructureDefinition;
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
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;

/**
 * 发电聚变堆（纯发电）。适配 gregtech-gtqt-1.12.2-1.9.0.jar 的
 * {@link StructureDefinition} + {@link Elements} 模式 API。
 * <p>
 * 结构：21×21×11 立式胖环托卡马克（轮廓沿用仓库 GA Tokamak 蓝图，圆润、逐层全包）：
 * 最外 R 辐射屏蔽外壳，向内为 M 超导磁体环 与 N 中子捕获环，再向内为
 * 第一壁(W) / 冷却剂回路(C) / 氚增殖包层(B) 管壁层，中心 P 等离子体腔（固定）；
 * 结构外围用 {@link Elements#any()} 匹配（轮廓外任意方块均可，不围方形外壳）。
 * 仓口位 A×9 位于环形外壁（左侧外壁面 x≤2），控制器 S 在环心后侧。
 * <p>
 * 五类分级外壳（B/C/W/N/M）通过 tieredCasing 匹配档位，成型时读取各模块档位：
 * 最大输出 = 第一壁上限 × 冷却剂倍率（另加中子捕获加成），建磁需求由超导磁体档位决定，
 * 燃料消耗由氚增殖包层档位决定。
 */
public class MetaTileEntityFusionReactor extends MetaTileEntityBaseWithControl implements IDataInfoProvider {

    private final FusionReactorLogic logic;

    private FusionCasingStats firstWallStats;
    private FusionCasingStats coolantStats;
    private FusionCasingStats neutronStats;
    private FusionCasingStats breedingStats;
    private FusionCasingStats magnetStats;

    public MetaTileEntityFusionReactor(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
        this.logic = new FusionReactorLogic(this);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityFusionReactor(this.metaTileEntityId);
    }

    @Override
    public boolean usesMui2() {
        return false;
    }

    // ============ 结构 ============

    private static StructureDefinition<?> STRUCTURE;

    @Override
    protected @NotNull StructureDefinition<?> createStructureDefinition() {
        if (STRUCTURE == null) {
            STRUCTURE = buildStructureDefinition();
        }
        return STRUCTURE;
    }

    private static StructureDefinition<?> buildStructureDefinition() {
        IBlockState radiation = getCasingState(BlockFusionReactorCasing.CasingType.RADIATION_SHIELDING_CASING);
        return DeclarativePatternBuilder.start()
                // z=0 前
                .aisle("xxxxxxxxxxxxxxxxxxxxx", "xxxxxxxxxxxxxxxxxxxxx", "xxxxxxxxxxxxxxxxxxxxx", "xxxxxxxxxxxxxxxxxxxxx", "xxxxxxxxxxxxxxxxxxxxx", "xxxxxxxRRRMRRRxxxxxxx", "xxxxxxxxxxxxxxxxxxxxx", "xxxxxxxxxxxxxxxxxxxxx", "xxxxxxxxxxxxxxxxxxxxx", "xxxxxxxxxxxxxxxxxxxxx", "xxxxxxxxxxxxxxxxxxxxx")
                // z=1
                .aisle("xxxxxxxxxxxxxxxxxxxxx", "xxxxxxxxxxxxxxxxxxxxx", "xxxxxxxxxxxxxxxxxxxxx", "xxxxxxxRRRMRRRxxxxxxx", "xxxxxxxRRRMRRRxxxxxxx", "xxxxxRRNNNNNNNRRxxxxx", "xxxxxxxRRRMRRRxxxxxxx", "xxxxxxxRRRMRRRxxxxxxx", "xxxxxxxxxxxxxxxxxxxxx", "xxxxxxxxxxxxxxxxxxxxx", "xxxxxxxxxxxxxxxxxxxxx")
                // z=2
                .aisle("xxxxxxxxxxxxxxxxxxxxx", "xxxxxxxxxxxxxxxxxxxxx", "xxxxxxxRRRMRRRxxxxxxx", "xxxxxRRWWWWWWWRRxxxxx", "xxxxxRRWWWWWWWRRxxxxx", "xxxxRNNxxxxxxxNNRxxxx", "xxxxxRRWWWWWWWRRxxxxx", "xxxxxRRWWWWWWWRRxxxxx", "xxxxxxxRRRMRRRxxxxxxx", "xxxxxxxxxxxxxxxxxxxxx", "xxxxxxxxxxxxxxxxxxxxx")
                // z=3
                .aisle("xxxxxxxxxxxxxxxxxxxxx", "xxxxxxxRRRMRRRxxxxxxx", "xxxxxRRWWWWWWWRRxxxxx", "xxxMRWWxxxxxxxWWRMxxx", "xxxMRWWxxxxxxxWWRMxxx", "xxxMNxxxxxxxxxxxNMxxx", "xxxMRWWxxxxxxxWWRMxxx", "xxxMRWWxxxxxxxWWRMxxx", "xxxxxRRWWWWWWWRRxxxxx", "xxxxxxxRRRMRRRxxxxxxx", "xxxxxxxxxxxxxxxxxxxxx")
                // z=4
                .aisle("xxxxxxxxxxMxxxxxxxxxx", "xxxxMRRCCCCCCCRRMxxxx", "xxxxMWWxxxxxxxWWMxxxx", "xxxRWxxxxxxxxxxxWRxxx", "xxxRWxxxxxxxxxxxWRxxx", "xxRNxxxxxxxxxxxxxNRxx", "xxxRWxxxxxxxxxxxWRxxx", "xxxRWxxxxxxxxxxxWRxxx", "xxxxMWWxxxxxxxWWMxxxx", "xxxxMRRBBBBBBBRRMxxxx", "xxxxxxxxxxMxxxxxxxxxx")
                // z=5
                .aisle("xxxxxMxxxxMxxxxMxxxxx", "xxxxRCCCCCCCCCCCRxxxx", "xxxRWxxxxxxxxxxxWRxxx", "xxRWxxxxxxxxxxxxxWRxx", "xxRWxxxxxxxxxxxxxWRxx", "xRNxxxxxxxxxxxxxxxNRx", "xxRWxxxxxxxxxxxxxWRxx", "xxRWxxxxxxxxxxxxxWRxx", "xxxRWxxxxxxxxxxxWRxxx", "xxxxRBBBBBBBBBBBRxxxx", "xxxxxMxxxxMxxxxMxxxxx")
                // z=6
                .aisle("xxxxxxMxxxxxxxMxxxxxx", "xxxxRCCCxxMxxCCCRxxxx", "xxxRWxxxWWWWWxxxWRxxx", "xxRWxxxxxxxxxxxxxWRxx", "xxRWxxxxxxxxxxxxxWRxx", "xRNxxxxxxxxxxxxxxxNRx", "xxRWxxxxxxxxxxxxxWRxx", "xxRWxxxxxxxxxxxxxWRxx", "xxxRWxxxWWWWWxxxWRxxx", "xxxxRBBBxxMxxBBBRxxxx", "xxxxxxMxxxxxxxMxxxxxx")
                // z=7
                .aisle("xxxxxxxxxxxxxxxxxxxxx", "xxxRCCCMxxxxxMCCCRxxx", "xxRWxxxWxxxxxWxxxWRxx", "xRWxxxxxWWWWWxxxxxWRx", "xRWxxxxxxWWWxxxxxxWRx", "RNxxxxxxxNNNxxxxxxxNR", "xRWxxxxxxWWWxxxxxxWRx", "xRWxxxxxWWWWWxxxxxWRx", "xxRWxxxWxxxxxWxxxWRxx", "xxxRBBBMxxxxxMBBBRxxx", "xxxxxxxxxxxxxxxxxxxxx")
                // z=8
                .aisle("xxxxxxxxxxxxxxxxxxxxx", "xxxRCCxxxxxxxxxCCRxxx", "xxRWxxWxMxxxMxWxxWRxx", "xRWxxxxWMxxxMWxxxxWRx", "xRWxxxxxWxxxWxxxxxWRx", "RNxxxxxxNxxxNxxxxxxNR", "xRWxxxxxWxxxWxxxxxWRx", "xRWxxxxWMxxxMWxxxxWRx", "xxAWxxWxMxxxMxWxxWRxx", "xxxRBBxxxxxxxxxBBRxxx", "xxxxxxxxxxxxxxxxxxxxx")
                // z=9
                .aisle("xxxxxxxxxxxxxxxxxxxxx", "xxxRCCxxxxxxxxxCCRxxx", "xxAWxxWxxxPxxxWxxWRxx", "xAWxxxxWxPMPxWxxxxWRx", "xAWxxxxWxMMMxWxxxxWRx", "ANxxxxxNxMMMxNxxxxxNR", "xAWxxxxWxMMMxWxxxxWRx", "xAWxxxxWxPMPxWxxxxWRx", "xxRWxxWxxxPxxxWxxWRxx", "xxxRBBxxxxxxxxxBBRxxx", "xxxxxxxxxxxxxxxxxxxxx")
                // z=10 中
                .aisle("xxxxMMxxxxxxxxxMMxxxx", "xxxMCCMxxxPxxxMCCMxxx", "xxMWxxWxxPMPxxWxxWMxx", "xMWxxxxWxMMMxWxxxxWMx", "xMWxxxxWxMMMxWxxxxWMx", "MNxxxxxNxMMMxNxxxxxNM", "xMWxxxxWxMMMxWxxxxWMx", "xMWxxxxWxMMMxWxxxxWMx", "xxMWxxWxxPMPxxWxxWMxx", "xxxMBBMxxxPxxxMBBMxxx", "xxxxMMxxxxxxxxxMMxxxx")
                // z=11
                .aisle("xxxxxxxxxxxxxxxxxxxxx", "xxxRCCxxxxxxxxxCCRxxx", "xxRWxxWxxxPxxxWxxWRxx", "xRWxxxxWxPMPxWxxxxWRx", "xRWxxxxWxMMMxWxxxxWRx", "RNxxxxxNxMMMxNxxxxxNR", "xRWxxxxWxMMMxWxxxxWRx", "xRWxxxxWxPMPxWxxxxWRx", "xxAWxxWxxxPxxxWxxWRxx", "xxxRBBxxxxxxxxxBBRxxx", "xxxxxxxxxxxxxxxxxxxxx")
                // z=12
                .aisle("xxxxxxxxxxxxxxxxxxxxx", "xxxRCCxxxxxxxxxCCRxxx", "xxRWxxWxMxxxMxWxxWRxx", "xRWxxxxWMxxxMWxxxxWRx", "xRWxxxxxWxxxWxxxxxWRx", "RNxxxxxxNxxxNxxxxxxNR", "xRWxxxxxWxxxWxxxxxWRx", "xAWxxxxWMxxxMWxxxxWRx", "xxRWxxWxMxxxMxWxxWRxx", "xxxRBBxxxxxxxxxBBRxxx", "xxxxxxxxxxxxxxxxxxxxx")
                // z=13
                .aisle("xxxxxxxxxxxxxxxxxxxxx", "xxxRCCCMxxxxxMCCCRxxx", "xxRWxxxWxxxxxWxxxWRxx", "xRWxxxxxWWWWWxxxxxWRx", "xRWxxxxxxWWWxxxxxxWRx", "RNxxxxxxxNNNxxxxxxxNR", "xRWxxxxxxWWWxxxxxxWRx", "xRWxxxxxWWWWWxxxxxWRx", "xxRWxxxWxxxxxWxxxWRxx", "xxxRBBBMxxxxxMBBBRxxx", "xxxxxxxxxxxxxxxxxxxxx")
                // z=14
                .aisle("xxxxxxMxxxxxxxMxxxxxx", "xxxxRCCCxxMxxCCCRxxxx", "xxxRWxxxWWWWWxxxWRxxx", "xxRWxxxxxxxxxxxxxWRxx", "xxRWxxxxxxxxxxxxxWRxx", "xRNxxxxxxxxxxxxxxxNRx", "xxRWxxxxxxxxxxxxxWRxx", "xxRWxxxxxxxxxxxxxWRxx", "xxxRWxxxWWWWWxxxWRxxx", "xxxxRBBBxxMxxBBBRxxxx", "xxxxxxMxxxxxxxMxxxxxx")
                // z=15
                .aisle("xxxxxMxxxxMxxxxMxxxxx", "xxxxRCCCCCCCCCCCRxxxx", "xxxRWxxxxxxxxxxxWRxxx", "xxRWxxxxxxxxxxxxxWRxx", "xxRWxxxxxxxxxxxxxWRxx", "xRNxxxxxxxxxxxxxxxNRx", "xxRWxxxxxxxxxxxxxWRxx", "xxRWxxxxxxxxxxxxxWRxx", "xxxRWxxxxxxxxxxxWRxxx", "xxxxRBBBBBBBBBBBRxxxx", "xxxxxMxxxxMxxxxMxxxxx")
                // z=16
                .aisle("xxxxxxxxxxMxxxxxxxxxx", "xxxxMRRCCCCCCCRRMxxxx", "xxxxMWWxxxxxxxWWMxxxx", "xxxRWxxxxxxxxxxxWRxxx", "xxxRWxxxxxxxxxxxWRxxx", "xxRNxxxxxxxxxxxxxNRxx", "xxxRWxxxxxxxxxxxWRxxx", "xxxRWxxxxxxxxxxxWRxxx", "xxxxMWWxxxxxxxWWMxxxx", "xxxxMRRBBBBBBBRRMxxxx", "xxxxxxxxxxMxxxxxxxxxx")
                // z=17
                .aisle("xxxxxxxxxxxxxxxxxxxxx", "xxxxxxxRRRMRRRxxxxxxx", "xxxxxRRWWWWWWWRRxxxxx", "xxxMRWWxxxxxxxWWRMxxx", "xxxMRWWxxxxxxxWWRMxxx", "xxxMNxxxxxxxxxxxNMxxx", "xxxMRWWxxxxxxxWWRMxxx", "xxxMRWWxxxxxxxWWRMxxx", "xxxxxRRWWWWWWWRRxxxxx", "xxxxxxxRRRMRRRxxxxxxx", "xxxxxxxxxxxxxxxxxxxxx")
                // z=18
                .aisle("xxxxxxxxxxxxxxxxxxxxx", "xxxxxxxxxxxxxxxxxxxxx", "xxxxxxxRRRMRRRxxxxxxx", "xxxxxRRWWWWWWWRRxxxxx", "xxxxxRRWWWWWWWRRxxxxx", "xxxxRNNxxxxxxxNNRxxxx", "xxxxxRRWWWWWWWRRxxxxx", "xxxxxRRWWWWWWWRRxxxxx", "xxxxxxxRRRMRRRxxxxxxx", "xxxxxxxxxxxxxxxxxxxxx", "xxxxxxxxxxxxxxxxxxxxx")
                // z=19
                .aisle("xxxxxxxxxxxxxxxxxxxxx", "xxxxxxxxxxxxxxxxxxxxx", "xxxxxxxxxxxxxxxxxxxxx", "xxxxxxxRRRMRRRxxxxxxx", "xxxxxxxRRRMRRRxxxxxxx", "xxxxxRRNNNNNNNRRxxxxx", "xxxxxxxRRRMRRRxxxxxxx", "xxxxxxxRRRMRRRxxxxxxx", "xxxxxxxxxxxxxxxxxxxxx", "xxxxxxxxxxxxxxxxxxxxx", "xxxxxxxxxxxxxxxxxxxxx")
                // z=20 后
                .aisle("xxxxxxxxxxxxxxxxxxxxx", "xxxxxxxxxxxxxxxxxxxxx", "xxxxxxxxxxxxxxxxxxxxx", "xxxxxxxxxxxxxxxxxxxxx", "xxxxxxxxxxSxxxxxxxxxx", "xxxxxxxRRRMRRRxxxxxxx", "xxxxxxxxxxxxxxxxxxxxx", "xxxxxxxxxxxxxxxxxxxxx", "xxxxxxxxxxxxxxxxxxxxx", "xxxxxxxxxxxxxxxxxxxxx", "xxxxxxxxxxxxxxxxxxxxx")
                .where('S', Elements.self(MetaTileEntityFusionReactor.class))
                .where('x', Elements.any())
                .where('R', Elements.block(radiation))
                .where('P', Elements.block(getCasingState(BlockFusionReactorCasing.CasingType.PLASMA_CONTAINMENT_CASING)))
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

    protected static IBlockState getCasingState(BlockFusionReactorCasing.CasingType type) {
        return BlocksInit.FUSION_REACTOR_CASING.getState(type);
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

    // ============ 显示 ============

    @Override
    protected void addDisplayText(List<ITextComponent> textList) {
        super.addDisplayText(textList);
        if (!isStructureFormed()) {
            return;
        }
        FusionReactorLogic logic = this.logic;
        FusionReactorLogic.State state = logic.getState();
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
        textList.add(new TextComponentTranslation("drtech.multiblock.fusion.state",
                new TextComponentTranslation(stateKey))
                .setStyle(new Style().setColor(TextFormatting.AQUA)));
        textList.add(new TextComponentTranslation("drtech.multiblock.fusion.output", logic.getOutputEU())
                .setStyle(new Style().setColor(TextFormatting.YELLOW)));
        textList.add(new TextComponentTranslation("drtech.multiblock.fusion.core_output", logic.getCoreOutput()));
        textList.add(new TextComponentTranslation("drtech.multiblock.fusion.heat", logic.getHeatProgress(), logic.getMaxHeat()));
        if (state == FusionReactorLogic.State.MAGNETIZING) {
            textList.add(new TextComponentTranslation("drtech.multiblock.fusion.magnetize", logic.getMagnetizeStored(), logic.getMagnetizeNeeded()));
        }
        if (state == FusionReactorLogic.State.RUNNING) {
            textList.add(new TextComponentTranslation("drtech.multiblock.fusion.fuel", logic.getFuelConsumption()));
        }
        if (logic.getRfPower() > 0) {
            textList.add(new TextComponentTranslation("drtech.multiblock.fusion.rf", logic.getRfPower()));
        }
        textList.add(new TextComponentTranslation("drtech.multiblock.fusion.tier",
                tierText(this.firstWallStats), tierText(this.coolantStats),
                tierText(this.neutronStats), tierText(this.breedingStats), tierText(this.magnetStats)));
    }

    private static String tierText(FusionCasingStats stats) {
        return stats == null ? "-" : String.valueOf(stats.tier);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, @NotNull List<String> tooltip, boolean advanced) {
        super.addInformation(stack, world, tooltip, advanced);
        tooltip.add(I18n.format("drtech.machine.fusion_reactor.tooltip.1"));
        tooltip.add(I18n.format("drtech.machine.fusion_reactor.tooltip.2"));
        tooltip.add(I18n.format("drtech.machine.fusion_reactor.tooltip.3"));
    }

    @Override
    public List<ITextComponent> getDataInfo() {
        return new LinkedList<>();
    }

    // ============ 渲染 ============

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return com.drppp.drtech.Client.Textures.FUSION_REACTOR_CASING;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        if (this.getFrontOverlay() instanceof OrientedOverlayRenderer) {
            ((OrientedOverlayRenderer) this.getFrontOverlay()).renderOrientedState(renderState, translation, pipeline,
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
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        this.logic.receiveCustomData(dataId, buf);
    }
}
