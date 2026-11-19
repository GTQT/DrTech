package com.meowmel.cropQT.item.tool;

import com.drppp.drtech.DrTechMain;
import com.meowmel.cropQT.api.CropType;
import com.meowmel.cropQT.api.GrowthRequirement;
import com.meowmel.cropQT.tile.TileCropStick;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 植物透镜（源端 {@code ItemPlantLens}）。
 *
 * <p>右键作物架把种子标记为「已分析」，并在聊天栏列出当前状态。
 * 源端只在服务端改数据、用 {@code getPlantLensStatus} 拼文本；本仓库等价。
 */
public class ItemPlantLens extends Item {

    public ItemPlantLens() {
        setTranslationKey("drtech.plant_lens");
        setRegistryName("drtech", "plant_lens");
        setMaxStackSize(1);
        setCreativeTab(DrTechMain.DrTechTab);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip,
                               net.minecraft.client.util.ITooltipFlag flag) {
        tooltip.add(gregtech.api.util.LocalizationUtils.format("cropqt_tooltip.item.plantLens"));
    }

    public boolean onCropStickUse(World world, BlockPos pos, TileCropStick tile,
                                  EntityPlayer player, ItemStack stack) {
        if (world.isRemote) {
            return true;
        }
        if (!tile.hasCrop()) {
            return false;
        }
        tile.analyzeSeed();
        player.sendMessage(new TextComponentString(
                TextFormatting.GOLD + "=== " + cropName(tile) + " ==="));
        player.sendMessage(line("生长", String.format("%.1f%%", tile.getGrowthPercent() * 100.0f)));
        player.sendMessage(line("水", tile.getWaterStorage() + " / " + tile.getMaxWater()));
        player.sendMessage(line("肥", tile.getFertilizerStorage() + " / " + tile.getMaxFertilizer()));
        player.sendMessage(line("WeedEX", String.valueOf(tile.getWeedExStorage())));
        player.sendMessage(line("病害", tile.isSick() ? "是" : "否"));
        List<GrowthRequirement> unmet = tile.getUnmetRequirements();
        if (!unmet.isEmpty()) {
            player.sendMessage(new TextComponentString(TextFormatting.RED + "未满足的底土要求:"));
            for (GrowthRequirement requirement : unmet) {
                player.sendMessage(new TextComponentString(
                        TextFormatting.RED + "  - " + requirement.getDisplayName()));
            }
        }
        return true;
    }

    private static String cropName(TileCropStick tile) {
        CropType type = tile.getCropType();
        return type != null ? type.getDisplayName() : tile.getCropId();
    }

    private static TextComponentString line(String key, String value) {
        return new TextComponentString(TextFormatting.GRAY + key + ": "
                + TextFormatting.WHITE + value);
    }
}
