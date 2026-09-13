package com.meowmel.cropQT.machine;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.meowmel.cropQT.api.capability.impl.CropRangeLogic;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.metatileentity.TieredMetaTileEntity;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.utils.RenderUtil;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static gregtech.api.capability.GregtechDataCodes.IS_WORKING;
import static gregtech.api.capability.GregtechDataCodes.UPDATE_AUTO_OUTPUT_FLUIDS;
import static gregtech.api.capability.GregtechDataCodes.UPDATE_AUTO_OUTPUT_ITEMS;
import static gregtech.api.capability.GregtechDataCodes.UPDATE_OUTPUT_FACING;

/**
 * 「范围机器」的公共基类 —— 作物监管机与作物收割机共用。
 *
 * <h2>为什么自包含</h2>
 * 这两台都不是配方机器：没有输入配方查表，干活方式就是「扫半径内的作物架」。
 * 硬套一张空 RecipeMap 只为了蹭 CEU 的自动界面，是拿机器的形状迁就框架。
 * 所以照 {@code MetaTileEntityUniversalCollector} 自己来：自己的 {@code update()}、
 * 自己的自动输出、自己画界面。代价是下面这几个字段要自己维护（都是从收集器抄的）。
 *
 * <h2>档位决定的三样东西</h2>
 * 最大工作半径、槽位数、罐容量<b>全部跟着档位走</b>，一处写死就会出现
 * 「IV 机的槽位跟 LV 一样多」这种失配。
 *
 * <p>干活逻辑在 {@link CropRangeLogic} 的子类里，这里只管机器侧的东西。
 */
public abstract class MetaTileEntityCropRangeMachine extends TieredMetaTileEntity {

    /** 只用来驱动正面贴图的运行态，不参与逻辑判定。 */
    private boolean isWorking;

    // ---- 工作半径（界面可调，照收集器）----
    private final int maxRange;
    private int range;

    // ---- 自动输出（照收集器）----
    private boolean autoOutputItems;
    private boolean autoOutputFluids;
    private EnumFacing outputFacingItems;
    private EnumFacing outputFacingFluids;

    protected MetaTileEntityCropRangeMachine(ResourceLocation metaTileEntityId, int tier) {
        super(metaTileEntityId, tier);
        this.maxRange = (int) Math.pow(tier + 1, 2);
        this.range = maxRange;
        initializeInventory();
    }

    /** 本机的干活逻辑，构造器里造好后交给这个 getter。 */
    public abstract CropRangeLogic<?> getLogic();

    /**
     * 正面的贴图。
     *
     * <p>运行态由 {@link #isActive()} 决定，实现里自己挑要哪一种 —— 比如
     * {@code SimpleOverlayRenderer} 就按开关在两张贴图之间切。
     */
    @SideOnly(Side.CLIENT)
    protected abstract ICubeRenderer getOverlayRenderer();

    // ==================== 工作半径 ====================

    public int getRange() {
        return range;
    }

    public void setRange(int range) {
        this.range = MathHelper.clamp(range, 1, maxRange);
        if (!getWorld().isRemote) {
            markDirty();
        }
    }

    public int getMaxRange() {
        return maxRange;
    }

    // ==================== 主循环 ====================

    @Override
    public void update() {
        super.update();
        if (getWorld().isRemote) {
            return;
        }

        getLogic().update();

        // 自动输出：每 5 tick 往输出面外的容器推一次
        if (getOffsetTimer() % 5 == 0) {
            if (isAutoOutputItems()) {
                pushItemsIntoNearbyHandlers(getOutputFacingItems());
            }
            if (isAutoOutputFluids()) {
                pushFluidsIntoNearbyHandlers(getOutputFacingFluids());
            }
        }

        // 运行态变了才发包（正面贴图靠它切）
        boolean workingNow = getLogic().isAnyEnabled();
        if (workingNow != isWorking) {
            this.isWorking = workingNow;
            writeCustomData(IS_WORKING, buf -> buf.writeBoolean(workingNow));
        }
    }

    /** 有开着动作就算在跑。 */
    @Override
    public boolean isActive() {
        return isWorking;
    }

    /** 逻辑在别的包，够不到 protected 的 {@code energyContainer}。 */
    public IEnergyContainer getEnergyContainer() {
        return energyContainer;
    }

    @Override
    public boolean getIsWeatherOrTerrainResistant() {
        return true;
    }

    // ==================== 自动输出 ====================

    @Override
    public boolean onWrenchClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing,
                                 CuboidRayTraceResult hitResult) {
        if (!playerIn.isSneaking()) {
            if (getOutputFacingItems() == facing) return false;
            if (hasFrontFacing() && facing == getFrontFacing()) return false;
            if (!getWorld().isRemote) {
                setOutputFacing(facing);
            }
            return true;
        }
        return super.onWrenchClick(playerIn, hand, facing, hitResult);
    }

    public void setOutputFacing(EnumFacing outputFacing) {
        this.outputFacingItems = outputFacing;
        this.outputFacingFluids = outputFacing;
        if (!getWorld().isRemote) {
            notifyBlockUpdate();
            writeCustomData(UPDATE_OUTPUT_FACING, buf -> {
                buf.writeByte(outputFacingItems.getIndex());
                buf.writeByte(outputFacingFluids.getIndex());
            });
            markDirty();
        }
    }

    @Override
    public void setFrontFacing(EnumFacing frontFacing) {
        super.setFrontFacing(frontFacing);
        if (this.outputFacingItems == null || this.outputFacingFluids == null) {
            setOutputFacing(frontFacing.getOpposite());
        }
    }

    public EnumFacing getOutputFacingItems() {
        return outputFacingItems == null ? getFrontFacing().getOpposite() : outputFacingItems;
    }

    public EnumFacing getOutputFacingFluids() {
        return outputFacingFluids == null ? getFrontFacing().getOpposite() : outputFacingFluids;
    }

    public boolean isAutoOutputItems() {
        return autoOutputItems;
    }

    public void setAutoOutputItems(boolean autoOutputItems) {
        this.autoOutputItems = autoOutputItems;
        if (!getWorld().isRemote) {
            writeCustomData(UPDATE_AUTO_OUTPUT_ITEMS, buf -> buf.writeBoolean(autoOutputItems));
            markDirty();
        }
    }

    public boolean isAutoOutputFluids() {
        return autoOutputFluids;
    }

    public void setAutoOutputFluids(boolean autoOutputFluids) {
        this.autoOutputFluids = autoOutputFluids;
        if (!getWorld().isRemote) {
            writeCustomData(UPDATE_AUTO_OUTPUT_FLUIDS, buf -> buf.writeBoolean(autoOutputFluids));
            markDirty();
        }
    }

    // ==================== 渲染 ====================

    @Override
    @SideOnly(Side.CLIENT)
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        getOverlayRenderer().renderOrientedState(renderState, translation, pipeline,
                getFrontFacing(), isActive(), true);
        // 自动输出开着的时候，在输出面上画箭头
        if (isAutoOutputItems()) {
            Textures.ITEM_OUTPUT_OVERLAY.renderSided(getOutputFacingItems(), renderState,
                    RenderUtil.adjustTrans(translation, getOutputFacingItems(), 2), pipeline);
        }
        if (isAutoOutputFluids()) {
            Textures.FLUID_OUTPUT_OVERLAY.renderSided(getOutputFacingFluids(), renderState,
                    RenderUtil.adjustTrans(translation, getOutputFacingFluids(), 2), pipeline);
        }
    }

    // ==================== 存档与同步 ====================

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setInteger("Range", range);
        data.setInteger("OutputFacing", getOutputFacingItems().getIndex());
        data.setInteger("OutputFacingF", getOutputFacingFluids().getIndex());
        data.setBoolean("AutoOutputItems", autoOutputItems);
        data.setBoolean("AutoOutputFluids", autoOutputFluids);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.range = MathHelper.clamp(data.getInteger("Range"), 1, maxRange);
        this.outputFacingItems = EnumFacing.VALUES[data.getInteger("OutputFacing")];
        this.outputFacingFluids = EnumFacing.VALUES[data.getInteger("OutputFacingF")];
        this.autoOutputItems = data.getBoolean("AutoOutputItems");
        this.autoOutputFluids = data.getBoolean("AutoOutputFluids");
    }

    @Override
    public void writeInitialSyncData(@NotNull PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeBoolean(isWorking);
        buf.writeBoolean(autoOutputItems);
        buf.writeBoolean(autoOutputFluids);
        buf.writeByte(getOutputFacingItems().getIndex());
        buf.writeByte(getOutputFacingFluids().getIndex());
        buf.writeInt(range);
    }

    @Override
    public void receiveInitialSyncData(@NotNull PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.isWorking = buf.readBoolean();
        this.autoOutputItems = buf.readBoolean();
        this.autoOutputFluids = buf.readBoolean();
        this.outputFacingItems = EnumFacing.VALUES[buf.readByte()];
        this.outputFacingFluids = EnumFacing.VALUES[buf.readByte()];
        this.range = buf.readInt();
    }

    @Override
    public void receiveCustomData(int dataId, @NotNull PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == UPDATE_OUTPUT_FACING) {
            this.outputFacingItems = EnumFacing.VALUES[buf.readByte()];
            this.outputFacingFluids = EnumFacing.VALUES[buf.readByte()];
            scheduleRenderUpdate();
        } else if (dataId == UPDATE_AUTO_OUTPUT_ITEMS) {
            this.autoOutputItems = buf.readBoolean();
            scheduleRenderUpdate();
        } else if (dataId == UPDATE_AUTO_OUTPUT_FLUIDS) {
            this.autoOutputFluids = buf.readBoolean();
            scheduleRenderUpdate();
        } else if (dataId == IS_WORKING) {
            this.isWorking = buf.readBoolean();
            scheduleRenderUpdate();
        }
    }

    // ==================== 展示 ====================

    @Override
    public void addToolUsages(ItemStack stack, @Nullable World world, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gregtech.tool_action.screwdriver.auto_output_covers"));
        tooltip.add(I18n.format("gregtech.tool_action.wrench.set_facing"));
        super.addToolUsages(stack, world, tooltip, advanced);
    }
}
