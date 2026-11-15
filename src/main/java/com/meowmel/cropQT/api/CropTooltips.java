package com.meowmel.cropQT.api;

import gregtech.api.util.TextFormattingUtil;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;

import java.text.DecimalFormat;

/**
 * 单方块作物机器的 tooltip 文本与格式。
 *
 * <p>键名与行序照 GTNH 的 {@code cropsnh_tooltip.<machine>.*} 结构，改用本模组的
 * {@code drtech_tooltip.} 前缀；数值着色也照源端：EU 黄、流体白、百分比青、档位/数字白。
 *
 * <p>物品显示名同样走这里：{@link #name} 返回 {@code drtech_tooltip.<machine>.name.<tier>}，
 * 各机器覆写 {@code getMetaName()} 指过来。这样 12 个档位各有自己的名字，
 * 和源端 {@code cropsnh_tooltip.cropBreeder.name.1..12} 一一对应。
 */
public final class CropTooltips {

    /** 与源端 {@code TooltipHelper.L_COLOR} 一致：流体量是白色。 */
    private static final TextFormatting FLUID_COLOR = TextFormatting.WHITE;
    /** 与源端 {@code TooltipHelper.EU_VOLT_COLOR} 一致：EU 数值是黄色。 */
    private static final TextFormatting EU_COLOR = TextFormatting.YELLOW;
    /** 与源端 {@code TooltipHelper.EFF_COLOR} 一致：百分比是青色。 */
    private static final TextFormatting PERCENT_COLOR = TextFormatting.AQUA;
    /** 与源端 {@code TooltipHelper.TIER_COLOR} 一致：普通数字/档位是白色。 */
    private static final TextFormatting NUMBER_COLOR = TextFormatting.WHITE;
    private static final TextFormatting RESET = TextFormatting.GRAY;

    private static final DecimalFormat PERCENT_FORMAT = new DecimalFormat("0.##%");

    private CropTooltips() {}

    /** 机器物品的 unlocalized 名：{@code drtech_tooltip.<key>.name.<tier>}。 */
    public static String name(String key, int tier) {
        return "drtech_tooltip." + key + ".name." + tier;
    }

    /** 源端 tooltip 的第一行：{@code Machine Type: §e<机器名>§r}。 */
    public static String machineType(String key) {
        return I18n.format("drtech_tooltip.machineType", I18n.format("drtech_tooltip." + key + ".type"));
    }

    /** 流体量，例如 {@code 144 L}；源端 {@code TooltipHelper.fluidText}。 */
    public static String fluid(long milliBuckets) {
        return FLUID_COLOR + TextFormattingUtil.formatNumbers(milliBuckets) + " L" + RESET;
    }

    /** EU 数值；源端 {@code TooltipHelper.euText}。 */
    public static String eu(long eu) {
        return EU_COLOR + TextFormattingUtil.formatNumbers(eu) + RESET;
    }

    /** 百分比；源端 {@code TooltipHelper.percentageFormat}，入参是 0..1 的小数。 */
    public static String percent(double fraction) {
        return PERCENT_COLOR + PERCENT_FORMAT.format(fraction) + RESET;
    }

    /** 普通数字（档位、范围、方块层数）；源端 {@code TooltipHelper.tierText} 的白色。 */
    public static String number(long value) {
        return NUMBER_COLOR + TextFormattingUtil.formatNumbers(value) + RESET;
    }
}
