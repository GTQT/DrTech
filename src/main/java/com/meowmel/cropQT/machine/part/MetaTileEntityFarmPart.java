package com.meowmel.cropQT.machine.part;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.drppp.drtech.api.metaTileEntity.DrtechMultiblockAbility;
import com.meowmel.cropQT.api.capability.FarmType;
import com.meowmel.cropQT.api.capability.IFarmPart;
import com.meowmel.cropQT.block.BlockSeedBed;
import gregtech.api.GTValues;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.AbilityInstances;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.client.renderer.texture.cube.SimpleOverlayRenderer;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityMultiblockPart;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * 工业农场的升级仓。
 *
 * <p>五种升级各是一种类型（{@link FarmType}），摆进农场的体段顶部 —— 每段一个。
 * 农场靠数仓的种类与个数决定装了哪些升级，不看方块。
 *
 * <h2>档次决定农场的等级</h2>
 * 仓的档次就是它所在农场的「升级等级」：结构里所有升级仓、以及所有苗床
 * <b>必须同一档</b>，那个档位决定容量、基础耗电、水肥消耗与收割轮数加成。
 * 同时它还<b>限制能量仓的电压上限</b> —— 想接高压电就得整套升上去。
 *
 * <p>档次外观不用自己画：基类 {@code MetaTileEntityMultiblockPart.getBaseTexture()}
 * 已经按档位给了 {@code VOLTAGE_CASINGS[tier]}，这里只负责正面那层类型贴图。
 */
public class MetaTileEntityFarmPart extends MetaTileEntityMultiblockPart implements
        IMultiblockAbilityPart<IFarmPart>, IFarmPart {

    /** 每个类型、每一档一张正面贴图，照搬源端。下标是档位号。 */
    private static final Map<FarmType, SimpleOverlayRenderer[]> OVERLAYS = new EnumMap<>(FarmType.class);

    static {
        for (FarmType type : FarmType.values()) {
            SimpleOverlayRenderer[] byTier = new SimpleOverlayRenderer[BlockSeedBed.MAX_TIER + 1];
            for (int tier = BlockSeedBed.MIN_TIER; tier <= BlockSeedBed.MAX_TIER; tier++) {
                byTier[tier] = new SimpleOverlayRenderer("drtech:industrial_farm/" + type.getName() + "_" + tier);
            }
            OVERLAYS.put(type, byTier);
        }
    }

    private final FarmType farmType;

    public MetaTileEntityFarmPart(ResourceLocation metaTileEntityId, int tier, FarmType farmType) {
        super(metaTileEntityId, tier);
        this.farmType = farmType;
    }

    @Override
    public FarmType getFarmType() {
        return farmType;
    }

    @Override
    public int getPartTier() {
        return getTier();
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityFarmPart(this.metaTileEntityId, getTier(), farmType);
    }

    @Override
    public void registerAbilities(AbilityInstances abilityInstances) {
        abilityInstances.add(this);
    }

    @Override
    public MultiblockAbility<IFarmPart> getAbility() {
        return DrtechMultiblockAbility.FARM_PART;
    }

    // ==================== 渲染 ====================

    @Override
    @SideOnly(Side.CLIENT)
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        if (shouldRenderOverlay()) {
            overlayFor(farmType, getTier()).renderSided(getFrontFacing(), renderState, translation, pipeline);
        }
    }

    @SideOnly(Side.CLIENT)
    private static SimpleOverlayRenderer overlayFor(FarmType type, int tier) {
        SimpleOverlayRenderer[] byTier = OVERLAYS.get(type);
        int clamped = Math.max(BlockSeedBed.MIN_TIER, Math.min(BlockSeedBed.MAX_TIER, tier));
        return byTier[clamped];
    }

    // ==================== 展示 ====================

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, @NotNull List<String> tooltip, boolean advanced) {
        super.addInformation(stack, world, tooltip, advanced);
        tooltip.add(I18n.format("drtech.machine.farm_part.tooltip.1", GTValues.VNF[getTier()]));
        switch (farmType) {
            case ENVIRONMENTAL_ENHANCEMENT:
                tooltip.add(I18n.format("drtech.machine.farm_part.environmental_enhancement.tooltip.1"));
                tooltip.add(I18n.format("drtech.machine.farm_part.environmental_enhancement.tooltip.2"));
                break;
            case GROWTH_ACCELERATION:
                tooltip.add(I18n.format("drtech.machine.farm_part.growth_acceleration.tooltip.1",
                        FarmType.GROWTH_ACCELERATION_BONUS));
                tooltip.add(I18n.format("drtech.machine.farm_part.growth_acceleration.tooltip.2"));
                break;
            case FERTILIZER:
                tooltip.add(I18n.format("drtech.machine.farm_part.fertilizer.tooltip.1",
                        1.0d + FarmType.FERTILIZER_GROWTH_MULTIPLIER));
                tooltip.add(I18n.format("drtech.machine.farm_part.fertilizer.tooltip.2",
                        FarmType.FERTILIZER_HARVEST_ROUND_BONUS));
                tooltip.add(I18n.format("drtech.machine.farm_part.fertilizer.tooltip.3"));
                break;
            case ADVANCED_HARVESTING:
                tooltip.add(I18n.format("drtech.machine.farm_part.advanced_harvesting.tooltip.1",
                        1.0d + FarmType.ADVANCED_HARVESTING_ROUND_MULTIPLIER));
                break;
            case OVERCLOCKED_GROWTH_ACCELERATION:
                tooltip.add(I18n.format("drtech.machine.farm_part.overclocked_growth_acceleration.tooltip.1"));
                tooltip.add(I18n.format("drtech.machine.farm_part.overclocked_growth_acceleration.tooltip.2"));
                tooltip.add(I18n.format("drtech.machine.farm_part.overclocked_growth_acceleration.tooltip.3"));
                break;
            default:
                break;
        }
        if (farmType.isCapped()) {
            tooltip.add(I18n.format("drtech.machine.farm_part.tooltip.max", farmType.getMaxCount()));
        }
        tooltip.add(I18n.format("drtech.machine.farm_part.tooltip.2"));
    }

    @Override
    public void addToolUsages(ItemStack stack, @Nullable World world, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gregtech.tool_action.screwdriver.access_covers"));
        tooltip.add(I18n.format("gregtech.tool_action.wrench.set_facing"));
        super.addToolUsages(stack, world, tooltip, advanced);
    }
}
