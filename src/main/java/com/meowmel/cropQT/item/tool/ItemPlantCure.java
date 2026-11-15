package com.meowmel.cropQT.item.tool;

import com.drppp.drtech.DrTechMain;
import com.meowmel.cropQT.tile.TileCropStick;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/**
 * 植物药（源端 {@code ItemPlantCure}）。
 *
 * <p>右键病株：先治病（{@code cureDisease()}），成功后额外补一次肥
 * （源端 {@code addFertilizer(25, 25, 25, false)}），并消耗 1 点耐久。共 100 次。
 */
public class ItemPlantCure extends Item {

    public static final int MAX_USES = 100;
    public static final int FERTILIZER_BONUS = 25;

    public ItemPlantCure() {
        setTranslationKey("drtech.plant_cure");
        setRegistryName("drtech", "plant_cure");
        setMaxStackSize(1);
        setMaxDamage(MAX_USES - 1);
        setCreativeTab(DrTechMain.DrTechTab);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, java.util.List<String> tooltip,
                               net.minecraft.client.util.ITooltipFlag flag) {
        tooltip.add(gregtech.api.util.LocalizationUtils.format("cropqt_tooltip.item.plantCure"));
    }

    public boolean onCropStickUse(World world, BlockPos pos, TileCropStick tile,
                                  EntityPlayer player, ItemStack stack) {
        if (world.isRemote) {
            return true;
        }
        if (!tile.cureDisease()) {
            return false;
        }
        tile.addFertilizer(FERTILIZER_BONUS, FERTILIZER_BONUS, FERTILIZER_BONUS, false);
        if (!player.isCreative()) {
            stack.damageItem(1, player);
        }
        return true;
    }
}
