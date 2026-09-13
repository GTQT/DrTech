package com.drppp.drtech.intergations.top.provider;

import com.drppp.drtech.Tags;
import com.drppp.drtech.common.items.ItemsInit;
import com.drppp.drtech.common.metaTileEntities.muti.electric.standard.MetaTileentityCropsSimulateMachine;
import com.drppp.drtech.common.metaTileEntities.muti.electric.store.MetaTileEntityYotTank;
import com.meowmel.cropQT.api.CropType;
import com.meowmel.cropQT.api.EnvironmentCalculator;
import com.meowmel.cropQT.api.GrowthRequirement;
import com.meowmel.cropQT.api.ISoilList;
import com.meowmel.cropQT.tile.TileCropStick;
import gregtech.api.util.GTUtility;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.IProbeInfoProvider;
import mcjty.theoneprobe.api.ProbeMode;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;

import java.util.List;

public class TopProvider implements IProbeInfoProvider {
    @Override
    public String getID() {
        return Tags.MODID + ":top_info_provider";
    }

    @Override
    public void addProbeInfo(ProbeMode probeMode, IProbeInfo iProbeInfo, EntityPlayer entityPlayer, World world,
                             IBlockState iBlockState, IProbeHitData iProbeHitData) {
        if (GTUtility.getMetaTileEntity(world, iProbeHitData.getPos()) instanceof MetaTileentityCropsSimulateMachine) {
            MetaTileentityCropsSimulateMachine machine =
                    (MetaTileentityCropsSimulateMachine) GTUtility.getMetaTileEntity(world, iProbeHitData.getPos());
            iProbeInfo.text(new TextComponentString(
                    TextFormatting.GREEN + "阶段: " + TextFormatting.WHITE + machine.getWorkPhaseDisplayName()).getFormattedText());
            iProbeInfo.progress(machine.getProgressPercent(), 100,
                    iProbeInfo.defaultProgressStyle()
                            .showText(false)
                            .filledColor(0xFFBFBFBF)
                            .alternateFilledColor(0xFFD4D4D4)
                            .backgroundColor(0xFF111111)
                            .borderColor(0xFFFFFFFF)
                            .height(12)
                            .width(140));
            iProbeInfo.text(new TextComponentString(
                    TextFormatting.YELLOW + "已部署: " + TextFormatting.WHITE + machine.getTotalDeployedCount() +
                            TextFormatting.GRAY + " / " + TextFormatting.WHITE + machine.getDeployedVarietyCount() + " 种").getFormattedText());

            List<ItemStack> previewStacks = machine.getPreviewOutputStacks(4);
            if (previewStacks.isEmpty()) {
                iProbeInfo.text(new TextComponentString(
                        TextFormatting.GRAY + "即将产出: 无").getFormattedText());
            } else {
                iProbeInfo.text(new TextComponentString(
                        TextFormatting.GREEN + "即将产出:").getFormattedText());
                for (ItemStack stack : previewStacks) {
                    iProbeInfo.horizontal()
                            .item(stack)
                            .text(stack.getDisplayName() + (stack.getCount() > 1 ? " x" + stack.getCount() : ""));
                }
            }
        }
        if (GTUtility.getMetaTileEntity(world, iProbeHitData.getPos()) instanceof MetaTileEntityYotTank) {
            var s = (MetaTileEntityYotTank) GTUtility.getMetaTileEntity(world, iProbeHitData.getPos());
            if (s.isActive() && s.isWorkingEnabled()) {
                iProbeInfo.text("流体:" + s.getFluid().getLocalizedName());
                iProbeInfo.text("容量" + s.getFluidBank().getStored() + "/" + s.getFluidBank().getCapacity());
            }
        }
        if (world.getTileEntity(iProbeHitData.getPos()) instanceof TileCropStick) {
            TileCropStick tile = (TileCropStick) world.getTileEntity(iProbeHitData.getPos());
            CropType type = tile.getCropType();
            String name = type != null ? type.getDisplayName() : tile.getCropId();
            iProbeInfo.text(new TextComponentString(
                    TextFormatting.GREEN + "作物: " + TextFormatting.WHITE + name).getFormattedText());

            // 脚下的土壤组——作物能不能种在这里由它决定
            ISoilList soil = tile.getSoilType();
            iProbeInfo.text(new TextComponentString(
                    TextFormatting.GREEN + "土壤: " + TextFormatting.WHITE
                            + (soil == null ? "无" : I18n.translateToLocal("cropqt.soil." + soil.getName())))
                    .getFormattedText());

            // 水肥储量：土壤组决定上限，见底了生长就慢下来
            iProbeInfo.text(new TextComponentString(
                    TextFormatting.AQUA + "水: " + TextFormatting.WHITE
                            + tile.getWaterStorage() + TextFormatting.GRAY + " / " + TextFormatting.WHITE
                            + tile.getMaxWater()).getFormattedText());
            iProbeInfo.text(new TextComponentString(
                    TextFormatting.AQUA + "肥: " + TextFormatting.WHITE
                            + tile.getFertilizerStorage() + TextFormatting.GRAY + " / " + TextFormatting.WHITE
                            + tile.getMaxFertilizer()).getFormattedText());

            // 环境综合分：光照 / 湿度 / 营养 / 水肥加成的合成结果
            float envScore = EnvironmentCalculator.calcEnvironmentScore(
                    world, iProbeHitData.getPos(), tile.getWaterRatio(), tile.getFertilizerRatio());
            iProbeInfo.text(new TextComponentString(
                    TextFormatting.LIGHT_PURPLE + "环境分: " + TextFormatting.WHITE
                            + String.format("%.2f", envScore)).getFormattedText());

            // 底土是软惩罚，只在没满足时提示——满足了再报一遍纯属噪音
            for (GrowthRequirement unmet : tile.getUnmetRequirements()) {
                iProbeInfo.text(new TextComponentString(
                        TextFormatting.RED + "底土不足: " + TextFormatting.WHITE + unmet.getDisplayName())
                        .getFormattedText());
            }

            if (com.meowmel.cropQT.item.MetaItemCropTools.CROP_ANALYZER.isItemEqual(entityPlayer.getHeldItem(EnumHand.MAIN_HAND))) {
                iProbeInfo.text(new TextComponentString(
                        TextFormatting.GREEN + "Tier: " + TextFormatting.WHITE +
                                (type != null ? type.getTier() : "?")).getFormattedText());
                iProbeInfo.text(new TextComponentString(
                        TextFormatting.GREEN + "阶段: " + TextFormatting.WHITE +
                                tile.getGrowthStage() + "/" +
                                (type != null ? type.getMaxGrowthStage() : "?")).getFormattedText());
                iProbeInfo.text(new TextComponentString(
                        TextFormatting.GOLD + "--- 属性 ---").getFormattedText());
                iProbeInfo.text(new TextComponentString(
                        TextFormatting.RED + "Growth:     " +
                                TextFormatting.WHITE + " " + tile.getStats().getGrowth()).getFormattedText());
                iProbeInfo.text(new TextComponentString(
                        TextFormatting.YELLOW + "Gain:       " +
                                TextFormatting.WHITE + " " + tile.getStats().getGain()).getFormattedText());
                iProbeInfo.text(new TextComponentString(
                        TextFormatting.AQUA + "Resistance: " +
                                TextFormatting.WHITE + " " + tile.getStats().getResistance()).getFormattedText());
            }
        }
    }
}
