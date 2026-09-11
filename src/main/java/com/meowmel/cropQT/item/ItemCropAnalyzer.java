package com.meowmel.cropQT.item;

import com.drppp.drtech.DrTechMain;
import com.drppp.drtech.Tags;
import com.meowmel.cropQT.tile.TileCropStick;
import com.meowmel.cropQT.api.CropType;
import com.meowmel.cropQT.api.EnvironmentCalculator;
import com.meowmel.cropQT.api.GrowthRequirement;
import com.meowmel.cropQT.api.ISoilList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import java.util.List;

/**
 * 作物分析器 - 右键作物架查看详细信息
 */
public class ItemCropAnalyzer extends Item {

    public ItemCropAnalyzer() {
        setTranslationKey(Tags.MODID + ".crop_analyzer");
        setRegistryName(Tags.MODID, "crop_analyzer");
        setMaxStackSize(1);
        setCreativeTab(DrTechMain.DrTechTab);
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos,
                                       EnumHand hand, EnumFacing facing,
                                       float hitX, float hitY, float hitZ) {
        if (world.isRemote) return EnumActionResult.SUCCESS;

        TileEntity te = world.getTileEntity(pos);
        if (!(te instanceof TileCropStick)) return EnumActionResult.PASS;

        TileCropStick tile = (TileCropStick) te;

        player.sendMessage(new TextComponentString(
            TextFormatting.GOLD + "══════ 作物分析报告 ══════"));

        if (!tile.hasCrop()) {
            player.sendMessage(new TextComponentString(
                TextFormatting.GRAY + "状态: " +
                (tile.isDoubleCropStick() ? "杂交模式(等待中)" : "空")));
        } else {
            CropType type = tile.getCropType();
            String name = type != null ? type.getDisplayName() : tile.getCropId();

            player.sendMessage(new TextComponentString(
                TextFormatting.GREEN + "作物: " + TextFormatting.WHITE + name));
            player.sendMessage(new TextComponentString(
                TextFormatting.GREEN + "Tier: " + TextFormatting.WHITE +
                (type != null ? type.getTier() : "?")));
            player.sendMessage(new TextComponentString(
                TextFormatting.GREEN + "阶段: " + TextFormatting.WHITE +
                tile.getGrowthStage() + "/" +
                (type != null ? type.getMaxGrowthStage() : "?")));
            player.sendMessage(new TextComponentString(
                TextFormatting.GREEN + "成熟: " + TextFormatting.WHITE +
                (tile.isMature() ? "是 ✔" : "否")));

            player.sendMessage(new TextComponentString(
                TextFormatting.GOLD + "--- 属性 ---"));
            player.sendMessage(new TextComponentString(
                TextFormatting.RED + "  Growth:     " +
                buildStatBar(tile.getStats().getGrowth()) +
                TextFormatting.WHITE + " " + tile.getStats().getGrowth()));
            player.sendMessage(new TextComponentString(
                TextFormatting.YELLOW + "  Gain:       " +
                buildStatBar(tile.getStats().getGain()) +
                TextFormatting.WHITE + " " + tile.getStats().getGain()));
            player.sendMessage(new TextComponentString(
                TextFormatting.BLUE + "  Resistance: " +
                buildStatBar(tile.getStats().getResistance()) +
                TextFormatting.WHITE + " " + tile.getStats().getResistance()));

            if (tile.isWeedPlant()) {
                player.sendMessage(new TextComponentString(
                    TextFormatting.DARK_RED + "⚠ 这是杂草!"));
            }
        }

        // 环境信息
        player.sendMessage(new TextComponentString(
            TextFormatting.GOLD + "--- 环境 ---"));

        float light = EnvironmentCalculator.calcLight(world, pos);
        float humidity = EnvironmentCalculator.calcHumidity(world, pos);
        float nutrients = EnvironmentCalculator.calcNutrients(world, pos);
        float baseScore = EnvironmentCalculator.calcEnvironmentScore(world, pos);
        float envScore = EnvironmentCalculator.calcEnvironmentScore(
            world, pos, tile.getWaterRatio(), tile.getFertilizerRatio());

        player.sendMessage(new TextComponentString(
            TextFormatting.AQUA + "  光照: " + TextFormatting.WHITE +
            String.format("%.0f%%", light * 100)));
        player.sendMessage(new TextComponentString(
            TextFormatting.AQUA + "  湿度: " + TextFormatting.WHITE +
            String.format("%.0f%%", humidity * 100)));
        player.sendMessage(new TextComponentString(
            TextFormatting.AQUA + "  营养: " + TextFormatting.WHITE +
            String.format("%.0f%%", nutrients * 100)));

        // 储量：加了加成后才是实际参与生长的分数，所以两个都列出来
        player.sendMessage(new TextComponentString(
            TextFormatting.AQUA + "  水位: " + TextFormatting.WHITE +
            tile.getWaterStorage() + "/" + tile.getMaxWater() +
            TextFormatting.GRAY + String.format(" (+%.0f%%)",
                tile.getWaterRatio() * EnvironmentCalculator.WATER_BONUS * 100)));
        player.sendMessage(new TextComponentString(
            TextFormatting.AQUA + "  肥位: " + TextFormatting.WHITE +
            tile.getFertilizerStorage() + "/" + tile.getMaxFertilizer() +
            TextFormatting.GRAY + String.format(" (+%.0f%%)",
                tile.getFertilizerRatio() * EnvironmentCalculator.FERTILIZER_BONUS * 100)));

        player.sendMessage(new TextComponentString(
            TextFormatting.AQUA + "  综合: " + TextFormatting.WHITE +
            String.format("%.0f%%", envScore * 100) +
            TextFormatting.GRAY + String.format("（基础 %.0f%%）", baseScore * 100)));

        // 种植条件
        player.sendMessage(new TextComponentString(
            TextFormatting.GOLD + "--- 种植条件 ---"));

        ISoilList soil = tile.getSoilType();
        player.sendMessage(new TextComponentString(
            TextFormatting.AQUA + "  土壤: " + TextFormatting.WHITE +
            (soil == null ? "非土壤（种不下去）" : localizeSoil(soil.getName()))));

        CropType cropType = tile.getCropType();
        if (cropType == null) {
            player.sendMessage(new TextComponentString(
                TextFormatting.GRAY + "  底土: —（先种上作物）"));
        } else if (!cropType.hasSubSoilRequirement()) {
            player.sendMessage(new TextComponentString(
                TextFormatting.GRAY + "  底土: 无要求"));
        } else {
            List<GrowthRequirement> unmet = tile.getUnmetRequirements();
            if (unmet.isEmpty()) {
                player.sendMessage(new TextComponentString(
                    TextFormatting.GREEN + "  底土: 满足"));
            } else {
                player.sendMessage(new TextComponentString(
                    TextFormatting.RED + "  底土: 缺少 " + TextFormatting.WHITE
                        + unmet.get(0).getDisplayName()
                        + TextFormatting.RED + "（生长速度大幅下降）"));
            }
        }

        player.sendMessage(new TextComponentString(
            TextFormatting.GOLD + "══════════════════════════"));

        return EnumActionResult.SUCCESS;
    }

    /** 土壤组名的本地化；没配 lang key 时退回内部名。 */
    private String localizeSoil(String name) {
        String key = "cropqt.soil." + name;
        String text = new TextComponentTranslation(key).getUnformattedText();
        return text.startsWith("cropqt.soil.") ? name : text;
    }

    private String buildStatBar(int value) {
        StringBuilder bar = new StringBuilder(TextFormatting.GREEN + "[");
        int filled = value * 10 / 31;
        for (int i = 0; i < 10; i++) {
            if (i < filled) {
                bar.append(value >= 24 ? TextFormatting.RED : TextFormatting.GREEN).append("|");
            } else {
                bar.append(TextFormatting.DARK_GRAY).append(".");
            }
        }
        bar.append(TextFormatting.GREEN).append("]");
        return bar.toString();
    }
}
