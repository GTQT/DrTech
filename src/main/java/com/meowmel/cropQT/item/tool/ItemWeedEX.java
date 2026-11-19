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
 * WeedEX 罐（源端 {@code ItemWeedEX}）。
 *
 * <p>源端是「消耗品 + 耐久」：每点耐久提供 {@link #POTENCY_PER_USE} 点 WeedEX，
 * 罐子共 {@link #MAX_USES} 次。对着作物架右键把 WeedEX 注入架子，架子的
 * WeedEX 存储上限 {@link #MAX_STORAGE}、读数超过 {@link #THRESHOLD} 时不再接受。
 *
 * <p>本仓库没有独立的 WeedEX 物品（源端也没有），只有 GT 的流体与这个罐子；
 * 流体一侧由 {@code WeedEXRegistry} 负责。
 */
public class ItemWeedEX extends Item {

    /** 每次命中的 WeedEX 点数。 */
    public static final int POTENCY_PER_USE = 10;
    /** 架子拒绝注入的阈值（源端 addWeedEx 的 threshold）。 */
    public static final int THRESHOLD = 90;
    /** 架子的 WeedEX 存储上限。 */
    public static final int MAX_STORAGE = 100;
    /** 源端 MAX_USES = WEEDEX_CAPACITY(750) / WEEDEX_POTENCY(1) / POTENCY_PER_USE(10)。 */
    public static final int MAX_USES = 75;

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, java.util.List<String> tooltip,
                               net.minecraft.client.util.ITooltipFlag flag) {
        tooltip.add(gregtech.api.util.LocalizationUtils.format("cropqt_tooltip.item.weedEX"));
    }

    public ItemWeedEX() {
        setTranslationKey("drtech.weedex_can");
        setRegistryName("drtech", "weedex_can");
        setMaxStackSize(1);
        setMaxDamage(MAX_USES - 1);
        setCreativeTab(DrTechMain.DrTechTab);
    }

    /**
     * 作物架右键入口。
     *
     * @return true 表示这次交互被罐子消费（不再走种植/收获逻辑）
     */
    public boolean onCropStickUse(World world, BlockPos pos, TileCropStick tile,
                                  EntityPlayer player, ItemStack stack) {
        if (world.isRemote) {
            return true;
        }
        if (!tile.hasCrop() && !tile.hasWeed()) {
            return false;
        }
        if (!tile.addWeedEx(POTENCY_PER_USE, THRESHOLD, MAX_STORAGE, false)) {
            return false;
        }
        if (!player.isCreative()) {
            stack.damageItem(1, player);
        }
        return true;
    }
}
